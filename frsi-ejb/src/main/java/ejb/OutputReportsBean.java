package ejb;

import dataform.FormulaSyntaxError;
import dataform.NoReportDataError;
import entities.*;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import parser.*;
import parser.functions.AbstractFunction;
import parser.functions.ContextProvider;
import parser.functions.SumFunction;
import parser.parser.*;
import util.PeriodUtil;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by nuriddin on 9/14/16.
 */
@Stateless
public class OutputReportsBean implements OutputReportsLocal, OutputReportsRemote {
    private static final Logger logger = Logger.getLogger("fileLogger");

    private static final String[] OLD_ALG_FORMS = new String[]{"balance_accounts_out"};

    @EJB
    private PersistenceLocal persistence;
    @EJB
    private ReferenceLocal reference;
    @EJB
    private SvodOutFormsLocal svodOutForms;


    private AbstractDataProvider dataProvider;
    private ContextProvider context;
    private ScriptEngine scriptEngine;
    private RuleParser ruleParser;
    private List<AbstractFunction> functions;
    private RowComparator rowComparator;
    private ParsedRuleComparator ruleComparator;
    private Map<String, String> outReportKeyFields;
    private ReportKey tempKey;

    @Override
    public void init() {
    }


    private EjbResponse<Long> generateAndGetId(Form form, Date reportDate, List<RefRespondentItem> respondents, PortalUser user, String idn, String initStatus, Date curDate, Date fromDate, Date toDate, Long reportId) throws Exception {
        List<String> oldAlgForms = new ArrayList<String>(Arrays.asList(OLD_ALG_FORMS));
        if (oldAlgForms.contains(form.getCode()))
            return new EjbResponse<Long>(svodOutForms.generateAndGetId(form, reportDate, respondents, user, idn, initStatus, curDate, fromDate, toDate));

        tempKey = new ReportKey();
        List<OutReportRuleItem> rules = persistence.getOutReportRuleListByFormCodeDate(form.getCode(), reportDate);
        Form outputForm = persistence.getFormsByCodeLanguageReportDate(form.getCode(), "ru", reportDate, null).get(0);
        String periodType = null;
        List<SortField> sortFields = new ArrayList<SortField>();
        if (outputForm.getFormHistory().getFormTag() != null) {
            if (outputForm.getFormHistory().getFormTag().sortFields != null) {
                sortFields = outputForm.getFormHistory().getFormTag().sortFields;
            }
            if (outputForm.getFormHistory().getFormTag().periodType != null)
                periodType = outputForm.getFormHistory().getFormTag().periodType;
        }

        scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
        URL url = getClass().getClassLoader().getResource("js/output_report.js");
        if (url == null)
            throw new Exception("Js-файл не найден");
        File scriptFile = new File(url.getFile());
        if (!scriptFile.exists()) {
            throw new IllegalStateException("Js-файл не найден");
        }
        Reader reader = Files.newBufferedReader(scriptFile.toPath(), StandardCharsets.UTF_8);
        scriptEngine.eval(reader);
        reader.close();

        URL url2 = getClass().getClassLoader().getResource("js/common.js");
        if (url2 == null)
            throw new Exception("Js-файл не найден");
        File scriptFile2 = new File(url2.getFile());
        if (!scriptFile2.exists()) {
            throw new IllegalStateException("Js-файл не найден");
        }
        Reader reader2 = Files.newBufferedReader(scriptFile2.toPath(), StandardCharsets.UTF_8);
        scriptEngine.eval(reader2);
        reader2.close();

        FormHistory formHistory = persistence.getFormHistoryWithJsCode(outputForm.getFormHistory().getId());
        scriptEngine.eval(formHistory.getJsCode());

        dataProvider = new DataProvider(persistence, reference);
        dataProvider.setOutputFormCode(form.getCode());
        dataProvider.setOutputReportDate(reportDate);
        dataProvider.setRules(rules);
        dataProvider.setRespondents(respondents);
        dataProvider.init();
        context = new ContextProvider(dataProvider);
        rowComparator = new RowComparator();
        rowComparator.setDataProvider(dataProvider);
        ruleComparator = new ParsedRuleComparator();
        outReportKeyFields = dataProvider.getKeyColumns(outputForm.getCode(), reportDate);

        scriptEngine.put("p", context);

        functions = new ArrayList<AbstractFunction>();
        SumFunction sum = new SumFunction(context);
        scriptEngine.put("sumFunction", sum);
        functions.add(sum);

        try {
            scriptEngine.eval("initFields();");
        }catch (ScriptException e){
            logger.error(e.getMessage());
            // in old forms initFields() function does not exist
        }

        Set<String> fNames = new HashSet<String>();
        for (AbstractFunction f : functions) {
            fNames.add(f.getName().toLowerCase());
        }

        ruleParser = new RuleParser();
        ruleParser.init(reportDate, fromDate, toDate, periodType, fNames);

        Map<String, RuleRow> ruleRows = makeRuleRows();
        List<ParsedRule> parsedRules = new ArrayList<ParsedRule>();
        List<RuleRow> dynamicRuleRows = new ArrayList<RuleRow>();
        List<RuleRow> staticRuleRows = new ArrayList<RuleRow>();
        for (RuleRow ruleRow : ruleRows.values()) {
            parsedRules.addAll(ruleRow.getGroupingRules());
            parsedRules.addAll(ruleRow.getNonGroupingRules());
            if (ruleRow.isDynamicRow())
                dynamicRuleRows.add(ruleRow);
            else
                staticRuleRows.add(ruleRow);

            Collections.sort(ruleRow.getGroupingRules(), ruleComparator);
            validateRuleRow(ruleRow);
        }
        Collections.sort(parsedRules, ruleComparator);

        dataProvider.loadData(ruleRows.values());

        if (dataProvider.getInputReports().size() == 0)
            throw new Exception("Нет входных отчетов");

        if (dataProvider.getRequiredReports().size() > 0) {
            List<RequiredReport> reports = new ArrayList<RequiredReport>();
            for (ReportKey k : dataProvider.getRequiredReports()) {
                RefRespondentItem resp = dataProvider.getRespByIdn(k.getBin());
                reports.add(new RequiredReport(k.getReportDate(), k.getFormCode(), resp.getRecId(), resp.getRefSubjectTypeRecId(), resp.getNameRu()));
            }
            return new EjbResponse<Long>(new RequiredReportsException(reports));
        }

        DateTimeZone dtZone = DateTimeZone.forTimeZone(TimeZone.getDefault());
        List<Date> dates = new ArrayList<Date>();
        if (fromDate != null && toDate != null && periodType != null) {
            List<LocalDate> l = PeriodUtil.generateDates(new LocalDate(fromDate, dtZone), new LocalDate(toDate, dtZone), PeriodUtil.getPeriodTypeByName(periodType));
            for (LocalDate d : l) {
                dates.add(d.toDate());
            }
        }

        for (ParsedRule parsedRule : parsedRules) {
            executeParsedRule(parsedRule, reportDate, dates);
        }

        Map<String, String> kvMap = new HashMap<String, String>();
        for (RuleRow ruleRow : dynamicRuleRows) {
            List<Row> sortedRows = new ArrayList<Row>(ruleRow.getRows().values());
            OutReportRuleItem i;
            if (ruleRow.getGroupingRules().size() > 0)
                i = dataProvider.getRuleById(ruleRow.getGroupingRules().get(0).getRuleId());
            else
                i = dataProvider.getRuleById(ruleRow.getNonGroupingRules().get(0).getRuleId());
            List<SortField> sfl = new ArrayList<SortField>();
            for (SortField sf : sortFields) {
                if (sf.name.substring(0, sf.name.indexOf("*")).equalsIgnoreCase(i.getFormCode() + "_" + i.getTableName())) {
                    sfl.add(sf);
                    if (sf.refName != null && !sf.refName.isEmpty()) {
                        dataProvider.loadReference(sf, sortedRows, ruleRow.getGroupReportDate());
                    }
                }
            }
            if (sfl.size() > 0) {
                rowComparator.setSortFields(sfl);
                Collections.sort(sortedRows, rowComparator);
                dataProvider.clearReferenceCache();
            }

            int idx = 1;
            for (Row row : sortedRows) {
                for (Cell cell : row.getCells()) {
                    String key = cell.getKey();
                    if (cell.isDynamicRow()) {
                        key = key.substring(0, key.lastIndexOf(".n") + 1) + String.valueOf(idx);
                    }
                    kvMap.put(key, cell.getValue());
                }
                idx++;
            }
        }

        for (RuleRow ruleRow : staticRuleRows) {
            for (Row row : ruleRow.getRows().values()) {
                for (Cell cell : row.getCells()) {
                    kvMap.put(cell.getKey(), cell.getValue());
                }
            }
        }

        if (reportId != 0)
            persistence.updateReportOutputReport(reportId, kvMap, dataProvider.getInputReports(), user, initStatus, curDate);
        else
            reportId = persistence.createReportOutputReport(reportDate, form, kvMap, dataProvider.getInputReports(), user, idn, initStatus, curDate);

        if (fromDate != null && toDate != null && periodType != null && !periodType.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            Map<String, String> props = new HashMap<String, String>();
            props.put(ReportProps.START_PERIOD_KEY, dateFormat.format(fromDate));
            props.put(ReportProps.END_PERIOD_KEY, dateFormat.format(toDate));
            props.put(ReportProps.PERIOD_DURATION_KEY, periodType);
            Long lastReportHistoryId = persistence.getLastReportHistoryIdByReportId(reportId, false, null);
            persistence.putReportProps(lastReportHistoryId, props);
        }

        destroy();

        return new EjbResponse<Long>(reportId);
    }

    private Map<String, RuleRow> makeRuleRows() throws FormulaSyntaxError {
        Map<String, RuleRow> ruleRows = new HashMap<String, RuleRow>();

        for (OutReportRuleItem item : dataProvider.getRulesItems()) {
            ParsedRule parsedRule = ruleParser.parse(item);

            String index = generateIndex(item);
            if (!ruleRows.containsKey(index)) {
                RuleRow n = new RuleRow(item.getKeyValue());
                n.setOnlySelfReference(true);
                ruleRows.put(index, new RuleRow(item.getKeyValue()));
            }
            RuleRow ruleRow = ruleRows.get(index);
            parsedRule.setRuleRow(ruleRow);

            if (ruleRow.getPriority() < parsedRule.getPriority())
                ruleRow.setPriority(parsedRule.getPriority());

            if (item.isGrouping()) {
                ruleRow.getGroupingRules().add(parsedRule);
            } else {
                ruleRow.getNonGroupingRules().add(parsedRule);
            }
            ruleRow.setDynamicRow(item.getKeyValue().toLowerCase().startsWith("$d."));

            List<ParsedKey> parsedKeys = new ArrayList<ParsedKey>();
            for (ParsedItem parsedItem : parsedRule.getItems()) {
                if (parsedItem instanceof ParsedKey) {
                    parsedKeys.add((ParsedKey) parsedItem);
                } else if (parsedItem instanceof ParsedAggFunction) {
                    ParsedAggFunction parsedFunction = (ParsedAggFunction) parsedItem;
                    parsedKeys.addAll(parsedFunction.getChildren());
                    if (parsedFunction.getChildren().size() == 0)
                        throw new FormulaSyntaxError(MessageFormat.format("В функции {0} нету показателей", parsedFunction.getName()));
                    ruleRow.setHasAggrFunctions(true);
                }
            }
            for (ParsedKey parsedKey : parsedKeys) {
                if (ruleRow.getSourceDynamicRowId() == null) {
                    if (parsedKey.isDynamicRow() && !parsedKey.isMeta() && !parsedKey.getFormCode().equalsIgnoreCase(dataProvider.getOutputFormCode())) {
                        ruleRow.setSourceDynamicRowId(parsedKey.getRowId());
                        ruleRow.setGroupFormCode(parsedKey.getFormCode());
                        ruleRow.setGroupReportDate(parsedKey.getDates().values().iterator().next());
                        ruleRow.setGroupContainer(parsedKey.getContainer());
                        break;
                    }
                }
            }
            for (ParsedKey parsedKey : parsedKeys) {
                if (parsedKey.getFormCode().equalsIgnoreCase(dataProvider.getOutputFormCode())) {
                    ruleRow.setOnlySelfReference(false);
                    break;
                }
            }
        }
        return ruleRows;
    }

    private String generateIndex(OutReportRuleItem item){
        return item.getTableName() + ">" + item.getKeyValue();
    }

    @Override
    public EjbResponse<Long> generateAndGetId(Form form, Date reportDate, List<RefRespondentItem> respondents, PortalUser user, String idn, String initStatus, Date curDate, Date fromDate, Date toDate) throws Exception {
        return generateAndGetId(form, reportDate, respondents, user, idn, initStatus, curDate, fromDate, toDate, 0l);
    }

    @Override
    public void regenerate(ReportListItem reportListItem, List<RefRespondentItem> respondents, PortalUser user, Date curDate, Date fromDate, Date toDate) throws Exception {
        List<Form> forms = persistence.getFormsByCodeLanguageReportDate(reportListItem.getFormCode(), "ru", reportListItem.getReportDate(), null);
        if (forms.size() > 0) {
            String initStatus = ReportStatus.Status.COMPLETED.name();
            Form form = forms.get(0);

            List<String> oldAlgForms = new ArrayList<String>(Arrays.asList(OLD_ALG_FORMS));
            if (oldAlgForms.contains(form.getCode())) {
                svodOutForms.regenerate(reportListItem, respondents, user, curDate, fromDate, toDate);
                return;
            }

            Report report = persistence.getReport(reportListItem.getId(), null);
            generateAndGetId(form, reportListItem.getReportDate(), respondents, user, report.getIdn(), initStatus, curDate, fromDate, toDate, report.getId());
        } else {
            throw new Exception(MessageFormat.format("За дату {0} формы с кодом {1} не существует", reportListItem.getReportDate(), reportListItem.getFormCode()));
        }
    }

    private void assignParentRuleRow(Map<String, RuleRow> ruleRows) {
        for (Map.Entry<String, RuleRow> e : ruleRows.entrySet()) {
            String k = e.getKey();
            if (k.toLowerCase().endsWith(".n")) {
                String p = k.toLowerCase().substring("$d.".length(), k.lastIndexOf(".n"));
                if (ruleRows.containsKey(p)) {
                    RuleRow parentRow = ruleRows.get(p);
                    parentRow.getChildren().add(e.getValue());
                    e.getValue().setParent(parentRow);
                }
            }
        }
    }

    private void destroy() {
        scriptEngine = null;
        dataProvider = null;
        context = null;
        rowComparator = null;
        ruleComparator = null;
    }

    private void validateRuleRow(RuleRow ruleRow) throws FormulaSyntaxError {
        if (!ruleRow.isDynamicRow() && ruleRow.getGroupingRules().size() > 0)
            throw new FormulaSyntaxError("В статических записях нельзя использовать группировку");

        if (ruleRow.isDynamicRow()) {
            boolean hasParsedKey = false;
            List<ParsedRule> allRules = new ArrayList<ParsedRule>();
            allRules.addAll(ruleRow.getGroupingRules());
            allRules.addAll(ruleRow.getNonGroupingRules());
            for (ParsedRule r : ruleRow.getGroupingRules()) {
                if (!hasParsedKey)
                    hasParsedKey = r.getItems().size() > 0;
            }
            for (ParsedRule r : ruleRow.getNonGroupingRules()) {
                if (!hasParsedKey)
                    hasParsedKey = r.getItems().size() > 0;

                if (ruleRow.getGroupingRules().size() > 0) {
                    for (ParsedItem i : r.getItems()) {
                        if (i instanceof ParsedKey) {
                            ParsedKey k = (ParsedKey) i;
                            if (!k.getFormCode().equalsIgnoreCase(dataProvider.getOutputFormCode())) {
                                throw new FormulaSyntaxError("При группировке нужно использовать аггрегирующую функцию");
                            }
                        }
                    }
                }
            }
            if (!hasParsedKey) {
                throw new FormulaSyntaxError("В динамических записях обязательно должен использоваться показатели");
            }
        }
    }

    private void executeParsedRule(ParsedRule parsedRule, Date reportDate, List<Date> dates) throws FormulaSyntaxError, RequiredKeyError {
        OutReportRuleItem ruleItem = dataProvider.getRuleById(parsedRule.getRuleId());
        RuleRow ruleRow = parsedRule.getRuleRow();
        Map<String, Row> rows = ruleRow.getRows();

        if (ruleItem.isGrouping()) {
            // grouping rule
            if (parsedRule.getRuleRow().getGroups().size() == 0) {
                makeGroups(parsedRule);

                ReportKey key = new ReportKey(dataProvider.getOutputFormCode(), dataProvider.getNationalBank().getIdn(), reportDate);
                Set<String> keySet = new HashSet<String>();
                for (Group group : parsedRule.getRuleRow().getGroups().values()) {
                    String k = ruleItem.getKeyValue();
                    k = ruleItem.getFormCode() + "_" + ruleItem.getTableName() + "*" + ruleItem.getFieldName() + ":num:" + k.substring(0, k.toLowerCase().lastIndexOf(".n") + 1) + String.valueOf(group.getIndex());
                    keySet.add(k);
                }
                dataProvider.makeNumTree(key, keySet, true);
            }
            for (Group group : parsedRule.getRuleRow().getGroups().values()) {
                Object value = group.getKey().getData().get(parsedRule.getRuleId());
                Row row = findRow(rows, ruleItem.getKeyValue(), group.getIndex());
                addToRow(row, ruleItem, value, reportDate, group.getIndex());
            }
        } else if (parsedRule.getRuleRow().getGroupingRules().size() > 0) {
            // non grouping rule, grouped rule
            for (Group group : parsedRule.getRuleRow().getGroups().values()) {
                if (parsedRule.isDynamicCell()) {
                    Row row = findRow(rows, ruleItem.getKeyValue(), group.getIndex());
                    for (Date d : dates) {
                        Object value = evalRule(parsedRule, group.getRows(), d, group.getIndex());
                        addToRow(row, ruleItem, value, d, group.getIndex());
                    }
                } else {
                    Object value = evalRule(parsedRule, group.getRows(), reportDate, group.getIndex());
                    Row row = findRow(rows, ruleItem.getKeyValue(), group.getIndex());
                    addToRow(row, ruleItem, value, reportDate, group.getIndex());
                }
            }
        } else if (parsedRule.getRuleRow().isDynamicRow()) {
            // dynamic rows without groups
            List<RefRespondentItem> respondents = new ArrayList<RefRespondentItem>();
            if (ruleRow.isOnlySelfReference())
                respondents.add(dataProvider.getNationalBank());
            else
                respondents.addAll(dataProvider.getRespondents());

            if (ruleRow.getSourceDynamicRowId() == null) {
                context.init();
                int i = 1;
                for (RefRespondentItem r : respondents) {
                    context.setContextBin(r.getIdn());
                    Row row = findRow(rows, ruleItem.getKeyValue(), i);
                    if (parsedRule.isDynamicCell()) {
                        for (Date d : dates) {
                            Object value = evalRule(parsedRule, null, d, i);
                            addToRow(row, ruleItem, value, d, i, true);
                        }
                    } else {
                        Object value = evalRule(parsedRule, null, reportDate, i);
                        addToRow(row, ruleItem, value, reportDate, i, true);
                    }
                    i++;
                }
            } else {
                if (ruleRow.getRowInputIds() == null)
                    ruleRow.setRowInputIds(dataProvider.getDynamicRowIdsByTemplate(ruleRow.getSourceDynamicRowId(), ruleRow.getGroupFormCode(), ruleRow.getGroupContainer(), ruleRow.getGroupReportDate()));
                context.init();
                for (Map.Entry<String, Set<String>> e : ruleRow.getRowInputIds().entrySet()) {
                    context.setContextBin(e.getKey());
                    int i = 1;
                    for (String r : e.getValue()) {
                        context.setDynamicRowId(r);
                        Row row = findRow(rows, ruleItem.getKeyValue(), i);
                        if (parsedRule.isDynamicCell()) {
                            for (Date d : dates) {
                                Object value = evalRule(parsedRule, null, d, i);
                                addToRow(row, ruleItem, value, d, i, true);
                            }

                        } else {
                            Object value = evalRule(parsedRule, null, reportDate, i);
                            addToRow(row, ruleItem, value, reportDate, i, true);
                        }
                        i++;
                    }
                }
            }
        } else {
            if (parsedRule.isDynamicCell()) {
                for (Date d : dates) {
                    Object value = evalRule(parsedRule, null, d, 0);
                    Row row = findRow(rows, ruleItem.getKeyValue(), 0);
                    addToRow(row, ruleItem, value, d, 0, true);
                }
            } else {
                Object value = evalRule(parsedRule, null, reportDate, 0);
                Row row = findRow(rows, ruleItem.getKeyValue(), 0);
                addToRow(row, ruleItem, value, reportDate, 0, true);
            }
        }
    }

    private Row findRow(Map<String, Row> rows, String keyValue, int index) {
        if (index > 0) {
            keyValue = keyValue.substring(0, keyValue.lastIndexOf(".n") + 1) + String.valueOf(index);
        }
        if (!rows.containsKey(keyValue))
            rows.put(keyValue, new Row());
        return rows.get(keyValue);
    }

    private void makeGroups(ParsedRule parsedRule) throws FormulaSyntaxError, RequiredKeyError {
        RuleRow ruleRow = parsedRule.getRuleRow();
        List<RefRespondentItem> respondents = new ArrayList<RefRespondentItem>();
        if (ruleRow.isOnlySelfReference())
            respondents.add(dataProvider.getNationalBank());
        else
            respondents.addAll(dataProvider.getRespondents());

        if (ruleRow.getSourceDynamicRowId() != null) {
            for (RefRespondentItem respondent : respondents) {
                ReportKey reportKey = new ReportKey(ruleRow.getGroupFormCode(), respondent.getIdn(), ruleRow.getGroupReportDate());
                Set<String> rowIds = dataProvider.getRowIdsByTemplate(ruleRow.getSourceDynamicRowId(), reportKey, ruleRow.getGroupContainer());
                context.init();
                for (String rowId : rowIds) {
                    GroupKey groupKey = new GroupKey();
                    for (ParsedRule groupingRule : ruleRow.getGroupingRules()) {
                        try {
                            Object value = evalRuleByRespondent(groupingRule, rowId, respondent.getIdn(), ruleRow.getGroupReportDate());
                            groupKey.add(groupingRule.getRuleId(), value);
                        } catch (NoReportDataError ex) {
                            logger.error(ex.getMessage());
                        }
                    }
                    Group group = ruleRow.getGroup(groupKey);
                    group.addRowNum(rowId, respondent.getIdn());
                }
            }
        } else {
            context.init();
            for (RefRespondentItem respondent : respondents) {
                GroupKey groupKey = new GroupKey();
                for (ParsedRule groupingRule : ruleRow.getGroupingRules()) {
                    try {
                        Object value = evalRuleByRespondent(groupingRule, null, respondent.getIdn(), ruleRow.getGroupReportDate());
                        groupKey.add(groupingRule.getRuleId(), value);
                    } catch (NoReportDataError ex) {
                        logger.error(ex.getMessage());
                    }
                }
                if (!ruleRow.getGroups().containsKey(groupKey)) {
                    Group group = ruleRow.getGroup(groupKey);
                    ReportKey reportKey = new ReportKey(ruleRow.getGroupFormCode(), respondent.getIdn(), ruleRow.getGroupReportDate());
                    group.addRowNums(dataProvider.getAllRowIds(reportKey), respondent.getIdn());
                }
            }
        }
    }

    /*private Map<String, String> executeStaticRowRule(ParsedRule parsedRule, Date reportDate, List<Date> dates) throws FormulaSyntaxError {
        Map<String, String> kvMap = new HashMap<String, String>();
        context.init();
        OutReportRuleItem r = dataProvider.getRuleById(parsedRule.getRuleId());
        if (parsedRule.isDynamicCell()) {
            for (Date d : dates) {
                Object value = evalRule(parsedRule, null, d, 0);
                Cell cell = createCell(r, value, reportDate);
                kvMap.put(cell.getKey(), cell.getValue());
            }
        } else {
            Object value = evalRule(parsedRule, null, reportDate, 0);
            Cell cell = createCell(r, value, reportDate);
            kvMap.put(cell.getKey(), cell.getValue());
        }
        return kvMap;
    }*/

    private boolean noReport(ParsedRule parsedRule) {
        List<ParsedKey> allKeys = parsedRule.getAllParsedKeys();
        for (ParsedKey k : allKeys) {
            if (!context.getNoReportErrors().keySet().contains(k.getId()) && !k.getFormCode().equalsIgnoreCase(dataProvider.getOutputFormCode()))
                return false;
        }
        return true;
    }

    private Cell addToRow(Row row, OutReportRuleItem r, Object value, Date reportDate, int index) {
        return addToRow(row, r, value, reportDate, index, false);
    }

    private Cell addToRow(Row row, OutReportRuleItem r, Object value, Date reportDate, int index, boolean makeNumTree) {
        Cell cell = createCell(r, value, reportDate);
        row.addCell(cell);
        String key = dataProvider.writeTemporal(cell, index, reportDate);
        if (makeNumTree) {
            tempKey.setFormCode(dataProvider.getOutputFormCode());
            tempKey.setBin(dataProvider.getNationalBank().getIdn());
            tempKey.setReportDate(reportDate);
            Set<String> keySet = new HashSet<String>();
            keySet.add(key);
            dataProvider.makeNumTree(tempKey, keySet, true);
        }
        return cell;
    }

    private Cell createCell(OutReportRuleItem r, Object value, Date reportDate) {
        String kf = outReportKeyFields.get(r.getFormCode() + "_" + r.getTableName());
        return new Cell(r, value, reportDate, kf);
    }

    private Object evalRuleByRespondent(ParsedRule rule, String dynamicRowId, String bin, Date reportDate) throws FormulaSyntaxError, NoReportDataError, RequiredKeyError {
        context.setParsedRule(rule);
        context.setDynamicRowId(dynamicRowId);
        context.setContextBin(bin);
        context.setContextDate(reportDate);
        return eval(rule.getParsedFormula());
    }

    private Object evalRule(ParsedRule rule, Map<String, Set<String>> filterRows, Date reportDate, int outRowIndex) {
        context.setParsedRule(rule);
        context.setFilterRows(filterRows);
        context.setContextDate(reportDate);
        context.setOutRowIndex(outRowIndex);
        return eval(rule.getParsedFormula());
    }

    private Object eval(String formula) {
        context.getNoReportErrors().clear();
        try {
            return scriptEngine.eval(formula);
        } catch (ScriptException e) {

            if (context.getNoReportDataError() != null)
                throw new EJBException(context.getNoReportDataError().getMessage());
            else if (context.getRequiredKeyError() != null)
                throw new EJBException(context.getRequiredKeyError().getMessage());
            else if (context.getIncorrectFormError() != null)
                throw new EJBException(context.getIncorrectFormError().getMessage());
            else
                throw new EJBException(e.getMessage());
        }
    }
}
