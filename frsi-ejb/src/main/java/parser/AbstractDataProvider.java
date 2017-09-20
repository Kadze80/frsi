package parser;

import dataform.NoReportDataError;
import entities.*;
import parser.parser.*;
import util.Convert;
import util.Util;

import java.text.MessageFormat;
import java.util.*;

/**
 * Created by nuriddin on 9/14/16.
 */
public abstract class AbstractDataProvider {
    protected Map<Long, OutReportRuleItem> rulesItemMap;
    protected Map<ReportKey, Map<String, String>> data;
    protected String outputFormCode;
    protected List<RefRespondentItem> respondents;
    protected Map<String, RefRespondentItem> respIdnMap;
    protected Map<Long, RefRespondentItem> respRecIdMap;
    protected Map<ReportKey, Map<String, NumTree>> numTreeMap = new HashMap<ReportKey, Map<String, NumTree>>();
    protected Map<String, Map<Long, String>> refCaptions = new HashMap<String, Map<Long, String>>();
    protected ReportKey tempKey = new ReportKey();
    protected RefRespondentItem nationalBank;
    protected Set<Long> inputReports = new HashSet<Long>();
    protected Map<String, String> formTypeIdx = new HashMap<String, String>();
    protected Map<String, SortedSet<FormFacade>> formsIdx = new HashMap<String, SortedSet<FormFacade>>();
    protected Form outputForm;
    protected Date outputReportDate;
    private Map<String, List<String>> fieldMap;
    private SubjectTypeFormIndex stfIndex = new SubjectTypeFormIndex() {
        @Override
        protected List<SubjectType_Form> loadSubjectTypeForms(long stRecId) {
            return AbstractDataProvider.this.loadSubjectTypeForms(stRecId);
        }
    };
    private Set<ReportKey> requiredReports  = new HashSet<ReportKey>();

    public void setRules(List<OutReportRuleItem> rules) {
        rulesItemMap = new HashMap<Long, OutReportRuleItem>();
        for (OutReportRuleItem r : rules) {
            rulesItemMap.put(r.getId(), r);
        }
    }

    public Set<String> getRowIdsByTemplate(String template, ReportKey reportKey, String container) {
        if (!numTreeMap.containsKey(reportKey)) return new HashSet<String>();
        return numTreeMap.get(reportKey).get(container).getRowIds(template);
    }

    public Map<String, Set<String>> getDynamicRowIdsByTemplate(String template, String formCode, String container, Date reportDate) {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        ReportKey rk = new ReportKey();
        rk.setFormCode(formCode);
        rk.setReportDate(reportDate);
        List<RefRespondentItem> respondents;
        if (formCode.equalsIgnoreCase(outputFormCode))
            respondents = new ArrayList<RefRespondentItem>(Arrays.asList(new RefRespondentItem[]{getNationalBank()}));
        else
            respondents = getRespondents();
        for (RefRespondentItem r : respondents) {
            rk.setBin(r.getIdn());
            Set<String> rowIds = getRowIdsByTemplate(template, rk, container);
            result.put(r.getIdn(), rowIds);
        }
        return result;
    }

    public Set<String> getAllRowIds(ReportKey reportKey) {
        return data.get(reportKey).keySet();
    }

    public List<RefRespondentItem> getRespondents() {
        return respondents;
    }

    public void setRespondents(List<RefRespondentItem> respondents) {
        this.respondents = respondents;
        respIdnMap = new HashMap<String, RefRespondentItem>();
        respRecIdMap = new HashMap<Long, RefRespondentItem>();
        for (RefRespondentItem r : respondents) {
            respIdnMap.put(r.getIdn(), r);
            respRecIdMap.put(r.getRecId(), r);
        }
    }

    public RefRespondentItem getRespByIdn(String idn) {
        if (!respIdnMap.containsKey(idn)) {
            if (idn.equals(Constants.KAZ_POST_BIN))
                return loadRespondentByIdn(Constants.KAZ_POST_BIN, tempKey != null ? tempKey.getReportDate() : new Date());
        }
        return respIdnMap.get(idn);
    }

    public String getValue(String key, String formCode, String bin, Date reportDate) throws NoReportDataError, IncorrectFormError {
        tempKey.setFormCode(formCode);
        tempKey.setReportDate(reportDate);
        if (formCode.equalsIgnoreCase(outputFormCode))
            tempKey.setBin(getNationalBank().getIdn());
        else
            tempKey.setBin(bin);
        if (tempKey.getBin() == null || tempKey.getFormCode() == null || tempKey.getReportDate() == null)
            throw new IllegalStateException("Не все координаты указаны.");

        kazPostBIN(tempKey);
        checkFormHistoryVersion(tempKey);
        if (!checkReport(tempKey.getFormCode(), tempKey.getBin(), tempKey.getReportDate())) {
            return null;
        }

        if (!data.containsKey(tempKey))
            /*throw new NoReportDataError(MessageFormat.format("Отчет {0} за {1} из {2}",
                    tempKey.getFormCode(),
                    Convert.dateFormatRus.format(tempKey.getReportDate()),
                    getRespByIdn(tempKey.getBin()).getNameRu()));*/
            throw new NoReportDataError();
        Map<String, String> kvMap = data.get(tempKey);
        if (!kvMap.containsKey(key))
            throw new NoReportDataError();
        return kvMap.get(key);
    }

    private boolean checkReport(String formCode, String bin, Date reportDate) {
        long stRecId = getSubjectTypeRecId(bin);
        if (!formCode.equalsIgnoreCase(outputFormCode) && !belongsToSubjectType(stRecId, formCode))
            return false;
        if (!formCode.equalsIgnoreCase(outputFormCode) && !checkPeriod(stRecId, formCode, reportDate)) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether the input form has been changed
     *
     * @param reportKey
     * @throws IncorrectFormError
     */
    protected void checkFormHistoryVersion(ReportKey reportKey) throws IncorrectFormError {
        Form inputForm = getForm(reportKey.getFormCode(), outputReportDate);
        if (inputForm == null)
            throw new IncorrectFormError(reportKey.getFormCode(), reportKey.getReportDate());
        Date iBeginDate = inputForm.getFormHistory().getBeginDate();
        Date iEndDate = inputForm.getFormHistory().getEndDate();
        if (reportKey.getReportDate().before(iBeginDate)
                || (iEndDate != null && iEndDate.before(reportKey.getReportDate()))) {

            throw new IncorrectFormError(reportKey.getFormCode(), reportKey.getReportDate());
        }
    }

    /**
     * Checks whether the passed form belongs to the passed subject type
     *
     * @param stRecId
     * @param formCode
     * @return
     */
    protected boolean belongsToSubjectType(long stRecId, String formCode) {
        SubjectType_Form stf = stfIndex.getSubjectTypeForm(stRecId, formCode);
        return stf != null;
    }

    protected boolean checkPeriod(long stRecId, String formCode, Date reportDate) {
        SubjectType_Form stf = stfIndex.getSubjectTypeForm(stRecId, formCode);
        if (stf == null)
            return false;
        String ptCode = stf.getPeriodCode();
        return Util.checkPeriod(ptCode, reportDate);
    }

    protected abstract List<SubjectType_Form> loadSubjectTypeForms(long stRecId);

    protected long getSubjectTypeRecId(String bin) {
        if (bin.equals(getNationalBank().getIdn()))
            return getNationalBank().getRefSubjectTypeRecId();
        RefRespondentItem resp = respIdnMap.get(bin);
        if (resp == null)
            throw new IllegalStateException(MessageFormat.format("can't find idn {0} in respondents", bin));
        return resp.getRefSubjectTypeRecId();
    }

    private Form getForm(String formCode, Date beginDate) {
        SortedSet<FormFacade> forms = formsIdx.get(formCode);
        if (forms == null)
            return null;
        for (FormFacade f : forms) {
            if (f.form.getFormHistory().getBeginDate().before(beginDate) || f.form.getFormHistory().getBeginDate().equals(beginDate)) {
                return f.form;
            }
        }
        return null;
    }

    private void addFormToIndex(Form form) {
        SortedSet<FormFacade> forms = formsIdx.get(form.getCode());
        if (forms == null) {
            forms = new TreeSet<FormFacade>();
            formsIdx.put(form.getCode(), forms);
        }
        forms.add(new FormFacade(form));
    }


    public ReportKey getLastUsedReportKey() {
        return new ReportKey(tempKey);
    }

    public OutReportRuleItem getRuleById(long ruleId) {
        return rulesItemMap.get(ruleId);
    }

    public Collection<OutReportRuleItem> getRulesItems() {
        return rulesItemMap.values();
    }

    public void loadData(Collection<RuleRow> rows) {
        data = new HashMap<ReportKey, Map<String, String>>();
        Map<ReportKey, List<ParsedKey>> rk = new HashMap<ReportKey, List<ParsedKey>>();
        ReportKey rKey = new ReportKey();
        rKey.setBin("");
        for (RuleRow r : rows) {
            List<ParsedRule> rules = new ArrayList<ParsedRule>();
            rules.addAll(r.getGroupingRules());
            rules.addAll(r.getNonGroupingRules());
            for (ParsedRule rule : rules) {
                List<ParsedKey> parsedKeys = new ArrayList<ParsedKey>();
                for (ParsedItem i : rule.getItems()) {
                    if (i instanceof ParsedKey) {
                        parsedKeys.add((ParsedKey) i);
                    } else if (i instanceof ParsedAggFunction) {
                        parsedKeys.addAll(((ParsedAggFunction) i).getChildren());
                    }
                }
                for (ParsedKey k : parsedKeys) {
                    if (k.getFormCode().equalsIgnoreCase(outputFormCode)) continue;
                    if (k.isMeta()) continue;
                    if (k.getFormCode().equalsIgnoreCase(outputFormCode)) continue;
                    rKey.setFormCode(k.getFormCode());
                    for (Date d : k.getDates().values()) {
                        rKey.setReportDate(d);
                        if (!rk.containsKey(rKey)) rk.put(new ReportKey(rKey), new ArrayList<ParsedKey>());
                        rk.get(rKey).add(k);
                    }
                }
            }
        }

        for (RefRespondentItem r : respondents) {
            for (Map.Entry<ReportKey, List<ParsedKey>> e : rk.entrySet()) {
                ReportKey k = new ReportKey(e.getKey());
                k.setBin(r.getIdn());
                kazPostBIN(k);

                Form form = getForm(k.getFormCode(), k.getReportDate());
                if (form == null) {
                    form = loadForm(k.getFormCode(), k.getReportDate());
                    if (form != null) {
                        addFormToIndex(form);
                    }
                }

                Map<String, String> d = new HashMap<String, String>();
                Map<String, String> kvMap;
                try {
                    kvMap = loadKvMap(k);
                } catch (NoReportDataError ex) {
                    if (!requiredReports.contains(k)) {
                        for (ParsedKey pk : e.getValue()) {
                            if (pk.isRequired()) {
                                if (checkReport(k.getFormCode(), k.getBin(), k.getReportDate())) {
                                    requiredReports.add(k);
                                }
                                break;
                            }
                        }
                    }
                    continue;
                }
                makeNumTree(k, kvMap.keySet(), false);

                for (ParsedKey pKey : e.getValue()) {
                    if (pKey.isFieldRange()) {
                        List<String> fields = Helper.getRangeFields(getFields().get(pKey.getContainer()), pKey.getStartRange(), pKey.getEndRange());
                        for (String field : fields) {
                            String key = Helper.changeIndicatorField(pKey.getKey(), field);
                            if (kvMap.containsKey(key))
                                d.put(key, kvMap.get(key));
                        }
                    } else if (pKey.isRowRange()) {
                        List<String> rowIds = Helper.getRangeRowIds(pKey.getStartRange(), pKey.getEndRange());
                        for (String rowId : rowIds) {
                            String key = Helper.changeIndicatorRow(pKey.getKey(), rowId);
                            if (kvMap.containsKey(key))
                                d.put(key, kvMap.get(key));
                        }
                    } else if (pKey.isDynamicRow()) {
                        String template = pKey.getKey();
                        String cont = template.substring(0, template.indexOf("*"));
                        String rowIdTempl = template.substring(template.lastIndexOf(":") + 1);
                        if (pKey.isDynamicRow()) rowIdTempl = rowIdTempl.substring(0, rowIdTempl.lastIndexOf(".n") + 1);
                        if (!numTreeMap.containsKey(k)) continue;
                        if (!numTreeMap.get(k).containsKey(cont)) continue;
                        for (Map.Entry<String, String> kve : kvMap.entrySet()) {
                            String cont1 = kve.getKey().substring(0, kve.getKey().indexOf("*"));
                            if (!cont.equalsIgnoreCase(cont1)) continue;
                            String field1 = kve.getKey().substring(kve.getKey().indexOf("*") + 1, kve.getKey().indexOf(":"));
                            String rowIdTempl1 = template.substring(template.lastIndexOf(":") + 1);
                            if (pKey.isDynamicRow() && !rowIdTempl.toLowerCase().startsWith(rowIdTempl) && rowIdTempl1.substring(rowIdTempl.length()).contains("."))
                                continue;
                            d.put(kve.getKey(), kve.getValue());
                        }
                    } else {
                        if (kvMap.containsKey(pKey.getKey()))
                            d.put(pKey.getKey(), kvMap.get(pKey.getKey()));
                    }
                }
                data.put(k, d);
            }
        }
    }

    public void makeNumTree(ReportKey rk, Set<String> keySet, boolean append) {
        Map<String, String> m = getKeyColumns(rk.getFormCode(), rk.getReportDate());
        if (append) {
            Map<String, NumTree> t = numTreeMap.get(rk);
            if (t != null) {
                for (Map.Entry<String, String> e : m.entrySet()) {
                    NumTree numTree = t.get(e.getKey());
                    if (numTree == null)
                        t.put(e.getKey(), new NumTree(e.getValue(), e.getKey(), keySet));
                    else
                        numTree.append(e.getValue(), e.getKey(), keySet);
                }
                return;
            }
        }
        numTreeMap.put(rk, new HashMap<String, NumTree>());
        for (Map.Entry<String, String> e : m.entrySet()) {
            numTreeMap.get(rk).put(e.getKey(), new NumTree(e.getValue(), e.getKey(), keySet));
        }

    }

    public abstract Map<String, String> getKeyColumns(String formCode, Date d);

    protected abstract Map<String, String> loadKvMap(ReportKey rk) throws NoReportDataError;

    public String getOutputFormCode() {
        return outputFormCode;
    }

    public void setOutputFormCode(String outputFormCode) {
        this.outputFormCode = outputFormCode;
    }

    public Set<ReportKey> getRequiredReports() {
        return requiredReports;
    }

    public String getRefCaption(String refName, String captionField, long recId) {
        if (recId == 0)
            return "";
        if (!refCaptions.containsKey(refName))
            throw new IllegalStateException(MessageFormat.format("Справочник {0} для сортировки не загружен", refName));
        Map<Long, String> m = refCaptions.get(refName);
        if (!m.containsKey(recId))
            return "";
        else
            return m.get(recId);
    }

    public void loadReference(SortField sf, List<Row> rows, Date reportDate) {
        String fieldName = sf.name.substring(sf.name.indexOf("*") + 1);
        for (Row r : rows) {
            Cell cell = r.getCell(fieldName);
            if (cell.lngValue > 0) {
                String c = getRefItemNameByRecId(sf.refName, sf.captionField, cell.lngValue, reportDate);
                if (!refCaptions.containsKey(sf.refName)) refCaptions.put(sf.refName, new HashMap<Long, String>());
                refCaptions.get(sf.refName).put(cell.lngValue, c);
            }
        }
    }

    public void clearReferenceCache() {
        refCaptions.clear();
    }

    protected abstract String getRefItemNameByRecId(String ref, String col, long recId, Date reportDate);

    public String writeTemporal(Cell cell, int index, Date reportDate) {
        tempKey.setFormCode(outputFormCode);
        tempKey.setReportDate(reportDate);
        tempKey.setBin(getNationalBank().getIdn());
        String key = cell.key;
        if (cell.dynamicRow)
            key = key.substring(0, key.lastIndexOf(".n") + 1) + String.valueOf(index);
        if (!data.containsKey(tempKey)) data.put(tempKey, new HashMap<String, String>());
        data.get(tempKey).put(key, cell.value);
        return key;
    }

    public Map<ReportKey, Map<String, String>> getData() {
        return data;
    }

    public RefRespondentItem getNationalBank() {
        if (nationalBank == null) {
            nationalBank = loadRespondentByIdn(Constants.NB_BIN, new Date());
        }
        return nationalBank;
    }

    protected abstract RefRespondentItem loadRespondentByIdn(String idn, Date reportDate);

    public Set<Long> getInputReports() {
        return inputReports;
    }

    /**
     * Если форма входная но БИН НБРК - меняем БИН на Казпочту (да, так нужно! Например для N109.)
     *
     * @param rk
     */
    protected void kazPostBIN(ReportKey rk) {
        if (rk.getBin() == null)
            return;
        if (!rk.getBin().equals(getNationalBank().getIdn()))
            return;
        if (rk.getFormCode() == null)
            return;
        if (!formTypeIdx.containsKey(rk.getFormCode().toLowerCase())) {
            if (rk.getReportDate() == null)
                return;
            Form form = loadForm(rk.getFormCode(), rk.getReportDate());
            if (form == null)
                return;
            formTypeIdx.put(form.getCode().toLowerCase(), form.getTypeCode());
        }
        String formType = formTypeIdx.get(rk.getFormCode().toLowerCase());
        if (formType.equals(Form.Type.INPUT.name()))
            rk.setBin(Constants.KAZ_POST_BIN);
    }

    protected abstract Form loadForm(String formCode, Date reportDate);

    public Date getOutputReportDate() {
        return outputReportDate;
    }

    public void setOutputReportDate(Date outputReportDate) {
        this.outputReportDate = outputReportDate;
    }

    public void init() {
        outputForm = loadForm(outputFormCode, outputReportDate);
        addFormToIndex(outputForm);
    }

    public Map<String, List<String>> getFields() {
        return fieldMap;
    }

    public void setFields(Map<String, List<String>> fieldMap) {
        this.fieldMap = fieldMap;
    }

    private class FormFacade implements Comparable<FormFacade> {
        private Form form;

        public FormFacade(Form form) {
            this.form = form;
        }

        @Override
        public int compareTo(FormFacade o) {
            return o.form.getFormHistory().getBeginDate().compareTo(this.form.getFormHistory().getBeginDate());
        }
    }
}
