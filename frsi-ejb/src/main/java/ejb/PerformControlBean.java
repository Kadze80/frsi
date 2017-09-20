package ejb;

import com.google.gson.Gson;
import dataform.DateEval;
import dataform.FormulaParser;
import dataform.FormulaSyntaxError;
import dataform.NoReportDataError;
import com.google.gson.reflect.TypeToken;
import dataform.*;
import entities.*;
import org.apache.log4j.Logger;
import parser.control.ControlContext;
import parser.control.IllegalExtSystemIndicator;
import parser.ReportKey;
import util.Convert;
import util.IndicatorParseException;
import util.ParserHelper;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by Nuriddin.Baideuov on 05.06.2015.
 */
@Stateless
public class PerformControlBean implements PerformControlLocal, PerformControlRemote {
    private static final Logger logger = Logger.getLogger(PerformControlBean.class.getName());

    @EJB
    private PersistenceLocal persistence;
    @EJB
    private ReferenceLocal reference;

    private Gson gson = new Gson();

    private DateEval dateEval = new DateEval();

    public static final String[] CONDITIONS = {"=", ">", "<", ">=", "<=", "<>"};
    private List<String> sortedConditions;

    @PostConstruct
    @Override
    public void init() {
        Date dateStart = new Date();

        sortedConditions = Arrays.asList(CONDITIONS);
        Collections.sort(sortedConditions, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });

        Date dateEnd = new Date();
        long duration = dateEnd.getTime() - dateStart.getTime();
        logger.debug(MessageFormat.format("t: {0}, d: {1} ms", dateStart, duration));
    }

    @Override
    public List<ControlResultItem> runTask(Date reportDate, String name, String idn, boolean forSuperUser, Long refSubjectTypeRecId, boolean extSysControls) {
        return runTask(reportDate, name, idn, forSuperUser, refSubjectTypeRecId, false, extSysControls);
    }

    @Override
    public List<ControlResultItem> runTask(Date reportDate, String name, String idn, boolean forSuperUser, Long refSubjectTypeRecId, boolean cascade, boolean extSysControls) {
        ControlContext context = createContext();
        context.setCascade(cascade);
        context.setReportDate(reportDate);
        context.setFormCode(name);
        context.setIdn(idn);
        context.setForSuperUser(forSuperUser);
        context.setRefSubjectTypeRecId(refSubjectTypeRecId);
        context.setReportId(persistence.getReportId(idn, reportDate, name));

        if (context.getReportId() == null)
            return new ArrayList<ControlResultItem>();

        List<Form> forms = persistence.getFormsByCodeLanguageReportDate(context.getFormCode(), "ru", context.getReportDate(), null);
        if (forms.size() == 0) {
            throw new EJBException(MessageFormat.format("Формы с кодом {0} за {1} не существует", context.getFormCode(), context.getReportDate()));
        }
        context.setFormTypeCode(forms.get(0).getTypeCode());

        List<ControlResultItem> result = perform(context, extSysControls);
        sortControlResults(result);

        postPerform(context, extSysControls);

        return result;
    }

    @Override
    public List<ControlResultItem> runTaskAll(List<Report> reports, boolean forSuperUser, boolean cascade, long userId, String userLocation, long respondentId, Long idKindEvent, boolean extSysControls) {
        List<ControlResultItem> controlResults = new ArrayList<ControlResultItem>();

        ControlContext context = createContext();
        context.setCascade(cascade);

        for (Report report : reports) {
            RefRespondentItem respondent = reference.getRespondentByIdn(report.getIdn(), report.getReportDate());
            if (respondent != null) {
                context.reset();
                context.setReportDate(report.getReportDate());
                context.setFormCode(report.getFormCode());
                context.setIdn(report.getIdn());
                context.setForSuperUser(forSuperUser);
                context.setRefSubjectTypeRecId(respondent.getRefSubjectTypeRecId());
                context.setReportId(report.getId());
                List<Form> forms = persistence.getFormsByCodeLanguageReportDate(context.getFormCode(), "ru", context.getReportDate(), null);
                if (forms.size() == 0) {
                    throw new EJBException(MessageFormat.format("Формы с кодом {0} за {1} не существует", context.getFormCode(), context.getReportDate()));
                }
                context.setFormTypeCode(forms.get(0).getTypeCode());
                controlResults.addAll(perform(context, extSysControls));

                try {
                    AuditEvent auditEvent = new AuditEvent();
                    auditEvent.setCodeObject(context.getFormCode());
                    auditEvent.setNameObject(null);
                    //auditEvent.setIdKindEvent(forSuperUser ? 38L : 12L);
                    auditEvent.setIdKindEvent(idKindEvent);
                    auditEvent.setDateEvent(new Date());
                    auditEvent.setIdRefRespondent(respondentId);
                    auditEvent.setDateIn(context.getReportDate());
                    auditEvent.setRecId(report.getId());
                    auditEvent.setUserId(userId);
                    auditEvent.setUserLocation(userLocation);
                    persistence.insertAuditEvent(auditEvent);
                } catch (Exception e) {
                    throw new EJBException(e.getMessage());
                }
            }
        }

        sortControlResults(controlResults);

        postPerform(context, extSysControls);

        return controlResults;
    }

    @Override
    public List<ControlResultItemGroup> runTaskNGetGrouped(List<Report> reports, boolean forSuperUser, long userId, String userLocation, long respondentId, boolean extSysControls) {
        List<ControlResultItemGroup> groups = new ArrayList<ControlResultItemGroup>();
        ControlContext context = createContext();
        Map<String, ControlResultItemGroup> groupMap = new HashMap<String, ControlResultItemGroup>();
        for (Report report : reports) {
            RefRespondentItem respondent = reference.getRespondentByIdn(report.getIdn(), report.getReportDate());
            if (!groupMap.containsKey(report.getIdn())) {
                groupMap.put(report.getIdn(), new ControlResultItemGroup(respondent.getNameRu(), respondent.getIdn(), new ArrayList<ControlResultItem>()));
            }
            ControlResultItemGroup group = groupMap.get(report.getIdn());
            if (respondent != null) {
                context.reset();
                context.setReportDate(report.getReportDate());
                context.setFormCode(report.getFormCode());
                context.setIdn(report.getIdn());
                context.setForSuperUser(forSuperUser);
                context.setRefSubjectTypeRecId(respondent.getRefSubjectTypeRecId());
                context.setReportId(report.getId());
                List<Form> forms = persistence.getFormsByCodeLanguageReportDate(context.getFormCode(), "ru", context.getReportDate(), null);
                if (forms.size() == 0) {
                    throw new EJBException(MessageFormat.format("Формы с кодом {0} за {1} не существует", context.getFormCode(), context.getReportDate()));
                }
                context.setFormTypeCode(forms.get(0).getTypeCode());
                group.getItems().addAll(perform(context, extSysControls));

                try {
                    AuditEvent auditEvent = new AuditEvent();
                    auditEvent.setCodeObject(context.getFormCode());
                    auditEvent.setNameObject(null);
                    auditEvent.setIdKindEvent(38L);
                    auditEvent.setDateEvent(new Date());
                    auditEvent.setIdRefRespondent(respondentId);
                    auditEvent.setDateIn(context.getReportDate());
                    auditEvent.setRecId(report.getId());
                    auditEvent.setUserId(userId);
                    auditEvent.setUserLocation(userLocation);
                    persistence.insertAuditEvent(auditEvent);
                } catch (Exception e) {
                    throw new EJBException(e.getMessage());
                }
            }
        }

        postPerform(context, extSysControls);

        groups.addAll(groupMap.values());

        for (ControlResultItemGroup group : groups) {
            sortControlResults(group.getItems());
        }

        Collections.sort(groups, new Comparator<ControlResultItemGroup>() {
            @Override
            public int compare(ControlResultItemGroup o1, ControlResultItemGroup o2) {
                return o1.getTitle().compareTo(o1.getTitle());
            }
        });

        return groups;
    }

    @Override
    public List<ControlResultItem> runTaskUnsaved(Date reportDate, String name, String idn, Long refSubjectTypeRecId, Map<String, String> mapData, boolean extSysControls) {
        ControlContext context = createContext();
        context.setCascade(false);
        context.setReportDate(reportDate);
        context.setFormCode(name);
        context.setIdn(idn);
        context.setForSuperUser(false);
        context.setRefSubjectTypeRecId(refSubjectTypeRecId);
        context.setReportId(persistence.getReportId(idn, reportDate, name));
        List<Form> forms = persistence.getFormsByCodeLanguageReportDate(context.getFormCode(), "ru", context.getReportDate(), null);
        if (forms.size() == 0) {
            throw new EJBException(MessageFormat.format("Формы с кодом {0} за {1} не существует", context.getFormCode(), context.getReportDate()));
        }
        context.setFormTypeCode(forms.get(0).getTypeCode());

        mapData = persistence.updateCalculatedFields(context.getFormCode(), context.getReportDate(), mapData, "ru", true);

        context.setMapData(mapData);

        List<ControlResultItem> result = perform(context, extSysControls);
        sortControlResults(result);

        postPerform(context, extSysControls);

        return result;
    }

    private ControlContext createContext() {
        ControlContext context = new ControlContext();
        context.setPersistence(persistence);
        return context;
    }

    private void postPerform(ControlContext context, boolean extSysControls) {
        if (context.isCascade()) {
            for (CompoundReportKey compoundReportKey : context.getCascadeReports()) {
                runTask(compoundReportKey.getReportDate(), compoundReportKey.getFormCode(), compoundReportKey.getIdn(), context.isForSuperUser(), context.getRefSubjectTypeRecId(), extSysControls);
            }
        }
    }

    private List<ControlResultItem> perform(ControlContext context, boolean extSysControls) {
        List<ControlResultItem> sortedResult = new ArrayList<ControlResultItem>();

        List<RefCrosscheckItem> crosscheckItems;
        Form form = persistence.getFormNoHistoryByCode(context.getFormCode());
        if (form.getTypeCode().equals(Form.Type.INPUT.name()))
            crosscheckItems = reference.getRefCrosscheckListByFormCodeDateSubjectType(context.getFormCode(), context.getReportDate(), context.isForSuperUser(), context.getRefSubjectTypeRecId(), extSysControls);
        else
            crosscheckItems = reference.getRefCrosscheckListByFormCodeDate(context.getFormCode(), context.getReportDate(), context.isForSuperUser(), extSysControls);

        ScriptEngine engine = getScriptEngine();
        engine.put("p", context);
        String controlResultCode = ControlResultType.SUCCESS.name();
        for (RefCrosscheckItem crosscheckItem : crosscheckItems) {

            context.setCurrentControl(crosscheckItem);

            ControlResultKey controlResultKey = new ControlResultKey(crosscheckItem.getRecId(), context.getReportDate(), context.getIdn());

            if (!context.getControlResultMap().containsKey(controlResultKey)) {

                context.resetStep();

                while (context.hasNextStep()) {

                    context.nextStep();

                    ControlResult controlResult = new ControlResult();
                    controlResult.setCrosscheckItemRecId(crosscheckItem.getRecId());
                    controlResult.setReportDate(context.getReportDate());
                    controlResult.setIdn(context.getIdn());

                    String resultL = "", resultR = "";

                    try {
                        if (!isAvailable(crosscheckItem, context))
                            continue;
                    } catch (FormulaSyntaxError e) {
                        controlResult.setResultType(ControlResult.ResultType.ERROR.getId());
                        controlResultCode = ControlResult.ResultType.FAIL.name();

                        ControlResultItem controlResultItem = new ControlResultItem();
                        controlResultItem.setCrosscheckType(crosscheckItem.getCrosscheckType());
                        controlResultItem.setCrosscheckTypeNameRu(crosscheckItem.getCrossTypeName());
                        controlResultItem.setResultType(controlResult.getResultType());
//                        controlResultItem.setDescriptionRu(context.replaceFormCodeToFormName(crosscheckItem.getDescrRus(), "ru"));
//                controlResultItem.setDescriptionRu(crosscheckItem.getDescrRus());
                        controlResultItem.setDescriptionRu(context.formatDescription(crosscheckItem, context.getCurrentRowNumber(), null, null));
                        controlResultItem.setSortNum(crosscheckItem.getNum());
                        controlResultItem.setDynamicRowId(context.getCurrentRowNumber());
                        controlResultItem.setReportDate(context.getReportDate());
                        sortedResult.add(controlResultItem);
                        if (!context.getControlResultMap().containsKey(controlResultKey)) {
                            context.getControlResultMap().put(controlResultKey, new ArrayList<ControlResult>());
                        }
                        context.getControlResultMap().get(controlResultKey).add(controlResult);

                        continue;

                    } catch (NoReportDataError e) {
                        controlResult.setResultType(ControlResult.ResultType.NO_DATA.getId());
                        controlResultCode = ControlResult.ResultType.FAIL.name();

                        ControlResultItem controlResultItem = new ControlResultItem();
                        controlResultItem.setCrosscheckType(crosscheckItem.getCrosscheckType());
                        controlResultItem.setCrosscheckTypeNameRu(crosscheckItem.getCrossTypeName());
                        controlResultItem.setResultType(controlResult.getResultType());
//                        controlResultItem.setDescriptionRu(context.replaceFormCodeToFormName(crosscheckItem.getDescrRus(), "ru"));
//                controlResultItem.setDescriptionRu(crosscheckItem.getDescrRus());
                        controlResultItem.setDescriptionRu(context.formatDescription(crosscheckItem, context.getCurrentRowNumber(), null, null));
                        controlResultItem.setSortNum(crosscheckItem.getNum());
                        controlResultItem.setDynamicRowId(context.getCurrentRowNumber());
                        controlResultItem.setReportDate(context.getReportDate());
                        if (crosscheckItem.getCrosscheckType() == 3) {
                            controlResultItem.setExternalSystemNameRu(context.getExtSysNameRu());
                            controlResultItem.setExternalSystemId(context.getExtSysId());
                        }
                        sortedResult.add(controlResultItem);
                        if (!context.getControlResultMap().containsKey(controlResultKey)) {
                            context.getControlResultMap().put(controlResultKey, new ArrayList<ControlResult>());
                        }
                        context.getControlResultMap().get(controlResultKey).add(controlResult);

                        continue;

                    } catch (IllegalExtSystemIndicator e){
                        controlResult.setResultType(ControlResult.ResultType.ERROR.getId());
                        controlResultCode = ControlResult.ResultType.FAIL.name();

                        ControlResultItem controlResultItem = new ControlResultItem();
                        controlResultItem.setCrosscheckType(crosscheckItem.getCrosscheckType());
                        controlResultItem.setCrosscheckTypeNameRu(crosscheckItem.getCrossTypeName());
                        controlResultItem.setResultType(controlResult.getResultType());
//                        controlResultItem.setDescriptionRu(context.replaceFormCodeToFormName(crosscheckItem.getDescrRus(), "ru"));
//                controlResultItem.setDescriptionRu(crosscheckItem.getDescrRus());
                        controlResultItem.setDescriptionRu(context.formatDescription(crosscheckItem, context.getCurrentRowNumber(), null, null));
                        controlResultItem.setSortNum(crosscheckItem.getNum());
                        controlResultItem.setDynamicRowId(context.getCurrentRowNumber());
                        controlResultItem.setReportDate(context.getReportDate());
                        controlResultItem.setErrorMessage(e.getMessage());
                        if (crosscheckItem.getCrosscheckType() == 3) {
                            controlResultItem.setExternalSystemNameRu(context.getExtSysNameRu());
                        }
                        sortedResult.add(controlResultItem);
                        if (!context.getControlResultMap().containsKey(controlResultKey)) {
                            context.getControlResultMap().put(controlResultKey, new ArrayList<ControlResult>());
                        }
                        context.getControlResultMap().get(controlResultKey).add(controlResult);

                        continue;
                    }


                    if (context.isCascade() && crosscheckItem.getCrosscheckType().intValue() == 1) {
                        List<RefCrosscheckForm> refCrosscheckForms = reference.getRefCrosscheckForms(crosscheckItem.getId());
                        List<CompoundReportKey> rk = new ArrayList<CompoundReportKey>();
                        for (RefCrosscheckForm refCrosscheckForm : refCrosscheckForms) {
                            if ((context.getFormTypeCode().equalsIgnoreCase(Form.Type.OUTPUT.name()) || context.getFormTypeCode().equalsIgnoreCase(Form.Type.CONSOLIDATED.name()))
                                    && refCrosscheckForm.getFormTypeCode().equalsIgnoreCase(Form.Type.INPUT.name())) {
                                rk = new ArrayList<CompoundReportKey>();
                                break;
                            }
                            if (!refCrosscheckForm.getFormCode().equals(context.getFormCode())) {
                                rk.add(new CompoundReportKey(context.getIdn(), context.getReportDate(), refCrosscheckForm.getFormCode()));
                                // context.cascadeForms.add(refCrosscheckForm.getFormCode());
                            }
                        }
                        context.getCascadeReports().addAll(rk);
                    }

                    context.getExternalFormCodes().clear();

                    try {
                        String[] formulas = {crosscheckItem.getFormulaL(), crosscheckItem.getFormulaR()};
                        ParseResult[] parseResults = new ParseResult[2];
                        for (int i = 0; i < formulas.length; i++) {
                            FormulaParser parser = new FormulaParser(formulas[i], context);
                            String parsedFormula = parser.getResult();
                            parseResults[i] = new ParseResult();
                            if (parser.hasNoReportDataError()) {
                                parseResults[i].noData = true;
                            } else {
                                try {
                                    Object expression = eval(parsedFormula, context, engine);
                                    parseResults[i].result = Double.parseDouble(expression.toString());
                                } catch (ScriptException e) {
                                    throw new FormulaSyntaxError();
                                } catch (NoReportDataError e) {
                                    parseResults[i].noData = true;
                                } catch (NumberFormatException e) {
                                    throw new FormulaSyntaxError();
                                }
                            }
                        }

                        DecimalFormatSymbols unusualSymbols = new DecimalFormatSymbols();
                        unusualSymbols.setGroupingSeparator(' ');
                        DecimalFormat formatter = new DecimalFormat();
                        formatter.setDecimalFormatSymbols(unusualSymbols);

                        resultL = parseResults[0].noData ? "(нет данных)" : formatter.format(parseResults[0].result);
                        resultR = parseResults[1].noData ? "(нет данных)" : formatter.format(parseResults[1].result);

//                        String desc = context.replaceFormCodeToFormName(crosscheckItem.getDescrRus(), "ru");
                        String desc;
                        if (extSysControls)
                            desc = context.formatDescription(crosscheckItem, context.getCurrentRowNumber(), null, null);
                        else
                            desc = context.formatDescription(crosscheckItem, context.getCurrentRowNumber(), resultL, resultR);
//                    String desc = crosscheckItem.getDescrRus();
                        controlResult.setDescriptionRu(desc);
                        if (parseResults[0].noData || parseResults[1].noData)
                            controlResult.setResultType(ControlResult.ResultType.NO_DATA.getId());
                        else
                            controlResult.setResultType(getFormulaResult(parseResults[0].result, parseResults[1].result, ConditionEnum.valueOf(crosscheckItem.getFormulaSymbol())).getId());
                    } catch (FormulaSyntaxError e) {
//                        controlResult.setDescriptionRu(context.replaceFormCodeToFormName(crosscheckItem.getDescrRus(), "ru"));
//                    controlResult.setDescriptionRu(crosscheckItem.getDescrRus());
                        controlResult.setDescriptionRu(context.formatDescription(crosscheckItem, context.getCurrentRowNumber(), null, null));
                        controlResult.setResultType(ControlResult.ResultType.ERROR.getId());
                    } catch (IllegalExtSystemIndicator e){
                        controlResult.setDescriptionRu(context.formatDescription(crosscheckItem, context.getCurrentRowNumber(), null, null));
                        controlResult.setResultType(ControlResult.ResultType.ERROR.getId());
                        controlResult.setErrorMessage(e.getMessage());
                    }

                    ControlResultItem controlResultItem = new ControlResultItem();
                    controlResultItem.setCrosscheckType(crosscheckItem.getCrosscheckType());
                    controlResultItem.setCrosscheckTypeNameRu(crosscheckItem.getCrossTypeName());
                    controlResultItem.setResultType(controlResult.getResultType());
                    controlResultItem.setDescriptionRu(controlResult.getDescriptionRu());
                    controlResultItem.setSortNum(crosscheckItem.getNum());
                    controlResultItem.setDynamicRowId(context.getCurrentRowNumber());
                    controlResultItem.setReportDate(context.getReportDate());
                    if (crosscheckItem.getCrosscheckType() == 3) {
                        controlResultItem.setExternalSystemNameRu(context.getExtSysNameRu());
                        controlResultItem.setExternalSystemId(context.getExtSysId());
                    }
                    controlResultItem.setResultL(resultL);
                    controlResultItem.setResultR(resultR);
                    controlResultItem.setErrorMessage(controlResult.getErrorMessage());
                    sortedResult.add(controlResultItem);
                    if (!context.getControlResultMap().containsKey(controlResultKey))
                        context.getControlResultMap().put(controlResultKey, new ArrayList<ControlResult>());
                    context.getControlResultMap().get(controlResultKey).add(controlResult);

                    if (controlResult.getResultType().longValue() != ControlResult.ResultType.SUCCESS.getId().longValue())
                        controlResultCode = ControlResult.ResultType.FAIL.name();
                }
            }
        }

        if (context.getMapData() == null && !extSysControls) {
            Report report = persistence.getReportByIdnDateForm(context.getIdn(), context.getReportDate(), context.getFormCode());
            if (report != null) {
                Long lastReportHistoryId = persistence.getLastReportHistoryIdByReportId(report.getId(), context.isForSuperUser(), null);
                if (lastReportHistoryId != null) {
                    if (crosscheckItems.size() == 0) {
                        if (context.isForSuperUser())
                            persistence.updateReportControlResultCode2(lastReportHistoryId, "");
                        else
                            persistence.updateReportControlResultCode(lastReportHistoryId, "");
                    } else {
                        if (context.isForSuperUser())
                            persistence.updateReportControlResultCode2(lastReportHistoryId, controlResultCode);
                        else
                            persistence.updateReportControlResultCode(lastReportHistoryId, controlResultCode);
                    }
                }
            }
        }

        return sortedResult;
    }

    private Object eval(String script, ControlContext context, ScriptEngine engine) throws ScriptException, NoReportDataError, IllegalExtSystemIndicator {
        context.setNoReportDataError(null);
        context.setIllegalExtSystemIndicator(null);
        try {
            return engine.eval(script);
        } catch (ScriptException e) {
            if (context.getNoReportDataError() != null)
                throw context.getNoReportDataError();
            if (context.getIllegalExtSystemIndicator() != null)
                throw context.getIllegalExtSystemIndicator();
            throw e;
        }
    }

    public ScriptEngine getScriptEngine() {
        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");

        try {
            Reader common = getFile("js/common.js");
            Reader control = getFile("js/control.js");
            scriptEngine.eval(common);
            scriptEngine.eval(control);
            common.close();
            control.close();
            return scriptEngine;
        } catch (IOException e) {
            throw new EJBException(e.getMessage());
        } catch (ScriptException e) {
            throw new EJBException(e.getMessage());
        }
    }

    private Reader getFile(String fileName) throws IOException {
        URL url = getClass().getClassLoader().getResource(fileName);
        if (url == null)
            throw new EJBException(MessageFormat.format("Js-файл {0} полей не найден", fileName));
        File scriptFile = new File(url.getFile());
        if (!scriptFile.exists()) {
            throw new EJBException(MessageFormat.format("Js-файл {0} полей не найден", fileName));
        }
        return Files.newBufferedReader(scriptFile.toPath(), StandardCharsets.UTF_8);
    }

    private List<ControlResultItem> sortControlResults(List<ControlResultItem> items) {
        Collections.sort(items, new Comparator<ControlResultItem>() {
            @Override
            public int compare(ControlResultItem o1, ControlResultItem o2) {
                if (o1.getResultType().longValue() != o2.getResultType().longValue())
                    return o2.getResultType().intValue() - o1.getResultType().intValue();
                if (o2.getCrosscheckType().intValue() != o1.getCrosscheckType().intValue())
                    return o2.getCrosscheckType().intValue() - o1.getCrosscheckType().intValue();
                if (o1.getSortNum().intValue() != o2.getSortNum().intValue())
                    return o1.getSortNum().compareTo(o2.getSortNum());

                String dynRowId1 = o1.getDynamicRowId(), dynRowId2 = o2.getDynamicRowId();
                if (dynRowId1.isEmpty() || dynRowId2.isEmpty()) {
                    return 0;
                }
                if (dynRowId1.startsWith("$D."))
                    dynRowId1 = dynRowId1.substring(3);
                if (dynRowId2.startsWith("$D."))
                    dynRowId2 = dynRowId2.substring(3);

                String[] spl1 = dynRowId1.split("\\.");
                String[] spl2 = dynRowId2.split("\\.");
                int i = 0;
                while (i < spl1.length && i < spl2.length) {
                    String ch1 = spl1[i];
                    String ch2 = spl2[i];
                    int d1, d2;
                    try {
                        d1 = Integer.parseInt(ch1);
                    } catch (NumberFormatException e) {
                        d1 = 0;
                    }
                    try {
                        d2 = Integer.parseInt(ch2);
                    } catch (NumberFormatException e) {
                        d2 = 0;
                    }
                    if (d1 == d2) {
                        i++;
                    } else {
                        if (d1 > d2)
                            return 1;
                        else
                            return -1;
                    }
                }

                if (spl1.length > spl2.length) {
                    return 1;
                } else if (spl1.length < spl2.length) {
                    return -1;
                } else {
                    return 0;
                }

            }
        });
        return items;
    }

    private boolean isAvailable(RefCrosscheckItem crosscheckItem, ControlContext context) throws FormulaSyntaxError, NoReportDataError, IllegalExtSystemIndicator {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        if (crosscheckItem.getConditionL() == null || crosscheckItem.getConditionL().trim().isEmpty()
                || crosscheckItem.getConditionR() == null || crosscheckItem.getConditionR().trim().isEmpty())
            return true;

        String[] formulas = {crosscheckItem.getConditionL(), crosscheckItem.getConditionR()};
        Object[] expressions = new Object[2];
        for (int i = 0; i < formulas.length; i++) {
            FormulaParser parser = new FormulaParser(formulas[i], context);
            String parsedFormula = parser.getResult();
            if (parser.hasNoReportDataError())
                throw new NoReportDataError();
            try {
                expressions[i] = eval(parsedFormula, context, engine);
            } catch (ScriptException e) {
                throw new FormulaSyntaxError();
            }
        }

        double a, b;
        try {
            a = Double.parseDouble(expressions[0].toString());
            b = Double.parseDouble(expressions[1].toString());
        } catch (NumberFormatException e) {
            throw new FormulaSyntaxError();
        }

        return getFormulaResult(a, b, ConditionEnum.valueOf(crosscheckItem.getConditionSymbol())) == ControlResult.ResultType.SUCCESS;
    }

    private ControlResult.ResultType getFormulaResult(double a, double b, ConditionEnum condition) {
        ControlResult.ResultType result = ControlResult.ResultType.FAIL;
        switch (condition) {
            case EQ:
                if (a == b) result = ControlResult.ResultType.SUCCESS;
                break;
            case NE:
                if (a != b) result = ControlResult.ResultType.SUCCESS;
                break;
            case GE:
                if (a >= b) result = ControlResult.ResultType.SUCCESS;
                break;
            case LE:
                if (a <= b) result = ControlResult.ResultType.SUCCESS;
                break;
            case LT:
                if (a < b) result = ControlResult.ResultType.SUCCESS;
                break;
            case GT:
                if (a > b) result = ControlResult.ResultType.SUCCESS;
                break;
        }
        return result;
    }

    private class ParseResult {
        boolean noData;
        double result;
    }
}
