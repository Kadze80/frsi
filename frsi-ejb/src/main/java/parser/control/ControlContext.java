package parser.control;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dataform.DateEval;
import dataform.FormulaSyntaxError;
import dataform.IKeyHandler;
import dataform.NoReportDataError;
import ejb.Persistence;
import ejb.PersistenceLocal;
import entities.*;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import parser.ReportKey;
import util.Convert;
import util.IndicatorParseException;
import util.ParserHelper;

import javax.ejb.EJBException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by nuriddin on 1/16/17.
 */
public class ControlContext implements IKeyHandler {
    private Set<String> externalFormCodes = new HashSet<String>();
    private Map<String, String> data = new HashMap<String, String>();
    private Map<ControlResultKey, List<ControlResult>> controlResultMap = new HashMap<ControlResultKey, List<ControlResult>>();
    private Map<String, String> formCodeFormNameMap = new HashMap<String, String>();
    private boolean cascade = false;
    private Set<CompoundReportKey> cascadeReports = new HashSet<CompoundReportKey>();
    private Date reportDate;
    private String formCode;
    private String idn;
    private String idn2; // for SUM function
    private boolean forSuperUser;
    private Long refSubjectTypeRecId;
    private String formTypeCode;
    private Map<String, String> mapData;
    private Set<String> fullLoadedReports = new HashSet<String>();

    private Stack<String> numberStack = new Stack<String>();
    private Map<String, Set<String>> rowNumbers = new HashMap<String, Set<String>>();
    private String currentRowNumber = "";
    private Long reportId;
    private Set<ReportKey> inputReports = new HashSet<ReportKey>();
    private ReportKey tempReportKey = new ReportKey();

    private PersistenceLocal persistence;
    private DateEval dateEval = new DateEval();
    private Gson gson = new Gson();

    private NoReportDataError noReportDataError;
    private IllegalExtSystemIndicator illegalExtSystemIndicator;

    private RefCrosscheckItem currentControl;

    private long extSysId;
    private String extSysNameRu;

    public void reset() {
        reportDate = null;
        formCode = null;
        idn = null;
        forSuperUser = false;
        refSubjectTypeRecId = null;
        fullLoadedReports = new HashSet<String>();
        mapData = null;
        data = new HashMap<String, String>();
    }

    @Override
    public String onKey(String key, int startIndex, int endIndex) throws FormulaSyntaxError, NoReportDataError {
        String formCode = grabFormCode(key);

        String key_;
        if (key.indexOf(";") > -1)
            key_ = key.substring(key.indexOf(";") + 1);
        else
            key_ = key;

        if (isDynamicRowKey(key_)) {

            Map<String, String> kvMap;
            if (!fullLoadedReports.contains(formCode)) {
                if (mapData != null && this.formCode.equals(formCode)) {
                    kvMap = mapData;
                } else {
                    kvMap = loadReportFullData(formCode);
                }
                this.data.putAll(kvMap);
                fullLoadedReports.add(formCode);
                rowNumbers.put(formCode, new HashSet<String>());
                for (String k : kvMap.keySet()) {
                    int p = k.indexOf("$D.");
                    if (k.contains("$D.")) {
                        rowNumbers.get(formCode).add(k.substring(p));
                        String topRowId = k.substring(p + 3, k.indexOf(".", p + 3));
                        if (!rowNumbers.get(formCode).contains(topRowId)) {
                            rowNumbers.get(formCode).add(topRowId);
                        }
                    }
                }
            }
            key_ = getDynamicFullKey(key_, formCode);
        } else {
            if (!data.containsKey(key_))
                loadData(formCode, key_);
        }

        String value = data.get(key_);
        if (value == null)
            throw new NoReportDataError();
        return value;
    }

    public double sum(form.calcfield2.DoubleCallback cb) {
        Set<String> idns = new HashSet<String>();
        List<InputReportListItem> irList = persistence.getInputReportsByOutputReportId(reportId, "ru");
        inputReports.clear();
        for (InputReportListItem item : irList) {
            inputReports.add(new ReportKey(item.getFormCode(), item.getIdn(), item.getReportDate()));
            idns.add(item.getIdn());
        }
        double sum = 0;
        for (String idn : idns) {
            idn2 = idn;
            sum += cb.call();
        }
        return sum;
    }

    public double getNumber(String key) throws FormulaSyntaxError {
        String value = getString(key);
        if (value != null)
            return Convert.parseDouble(value);
        return 0;
    }

    public String getString(String str) throws FormulaSyntaxError {
        Indicator ind;
        try {
            ind = ParserHelper.parseIndicator(str, true, true);
        } catch (IndicatorParseException e) {
            throw new EJBException(e.getMessage());
        }

        Date rd = reportDate;
        if (ind.getDateOffset() != null) {
            rd = dateEval.eval(ind.getDateOffset(), reportDate);
        }

        Report report = persistence.getReportByIdnDateForm(idn2, rd, ind.getFormCode());
        if (report == null) return "";
        ReportStatus lastStatus = persistence.getLastReportStatusByReportId(report.getId(), true, null);
        if (lastStatus == null || !ReportStatus.suUserStatuses.contains(ReportStatus.statusesMap.get(lastStatus.getStatusCode()))) {
            // если контроль запускает пользователь нац.банка, он должен видеть только свои статусы
            return null;
        }

        tempReportKey.setBin(idn2);
        tempReportKey.setFormCode(ind.getFormCode());
        tempReportKey.setReportDate(rd);
        if (!inputReports.contains(tempReportKey))
            return null;

        Long lastReportHistoryId = persistence.getLastReportHistoryIdByReportId(report.getId(), true, null);
        if (lastReportHistoryId == null) return "";

        return persistence.getReportValueByKeyReportHistoryId(ind.getIndicator(), lastReportHistoryId);
    }

    /**
     * Get value from external system by indicator code
     *
     * @param code
     * @param vt
     * @param params
     * @return
     * @throws Throwable
     */
    public Variant getExt(String code, ValueType vt, Param[] params) throws Throwable {
        List<RefExtIndicatorItem> items = persistence.getExtIndicatorItemsByCode(code);
        if (items.size() == 0)
            throwError(new NoReportDataError());

        extSysId = items.get(0).getExtSysId();
        extSysNameRu = items.get(0).getExtSysNameRu();

        String msg = "Период идентификатора внешней системы не соответсвует периоду контроля";
        DateTimeZone dtZone = DateTimeZone.forTimeZone(TimeZone.getDefault());
        LocalDate c1 = new LocalDate(currentControl.getBeginDate(), dtZone);
        LocalDate c2 = currentControl.getEndDate() == null ? null : new LocalDate(currentControl.getEndDate(), dtZone);
        for (RefExtIndicatorItem i : items) {
            LocalDate i1 = new LocalDate(i.getBeginDate(), dtZone);
            LocalDate i2 = i.getEndDate() == null ? null : new LocalDate(i.getEndDate(), dtZone);

            if (i1.isAfter(c1) && (c2 == null || (i1.isBefore(c2) || i1.equals(c2)))) {
                throwError(new IllegalExtSystemIndicator(msg));
            }

            if (i2 != null && i2.isAfter(c1) && (c2 == null || i2.isBefore(c2))) {
                throwError(new IllegalExtSystemIndicator(msg));
            }
        }

        Variant v = null;
        try {
            v = persistence.getExternalSystemIndicatorValue(code, formCode, reportDate, idn, params);
        } catch (SQLException e) {
            throwError(new IllegalExtSystemIndicator(e.getMessage()));
        }
        if (v == null)
            throwError(new NoReportDataError());

        return v;
    }

    public Variant getRef(String ref, String refColumn, long recId, ValueType vt) throws Throwable {
        Variant v = persistence.getRefItemValueByRecId(ref, refColumn, recId, reportDate, vt);
        if (v == null)
            throwError(new NoReportDataError());
        return v;
    }

    private void throwError(Throwable th) throws Throwable {
        if (th instanceof NoReportDataError) {
            noReportDataError = (NoReportDataError) th;
        }
        if (th instanceof IllegalExtSystemIndicator)
            illegalExtSystemIndicator = (IllegalExtSystemIndicator) th;
        throw th;
    }

    public ValueType toValueType(String dt) {
        if (dt.equals("s")) return ValueType.STRING;
        if (dt.equals("d")) return ValueType.DATE;
        if (dt.equals("b")) return ValueType.BOOLEAN;
        if (dt.equals("n0")) return ValueType.NUMBER_0;
        if (dt.equals("n1")) return ValueType.NUMBER_1;
        if (dt.equals("n2")) return ValueType.NUMBER_2;
        if (dt.equals("n3")) return ValueType.NUMBER_3;
        if (dt.equals("n4")) return ValueType.NUMBER_4;
        if (dt.equals("n5")) return ValueType.NUMBER_5;
        if (dt.equals("n6")) return ValueType.NUMBER_6;
        if (dt.equals("n7")) return ValueType.NUMBER_7;
        if (dt.equals("n8")) return ValueType.NUMBER_8;
        throw new IllegalStateException(MessageFormat.format("Can't convert string {0} to ValueType", dt));
    }

    private void loadData(String formCode, String key) throws NoReportDataError {
        if (mapData != null && this.formCode.equals(formCode)) {
            if (mapData.containsKey(key))
                data.put(key, mapData.get(key));
        } else {
            Report report = persistence.getReportByIdnDateForm(idn, reportDate, formCode);
            if (report != null) {
                ReportStatus lastStatus = persistence.getLastReportStatusByReportId(report.getId(), forSuperUser, null);
                if (forSuperUser && (lastStatus == null || !ReportStatus.suUserStatuses.contains(ReportStatus.statusesMap.get(lastStatus.getStatusCode())))) {
                    // если контроль запускает пользователь нац.банка, он должен видеть только свои статусы
                    return;
                }
                if ((formTypeCode.equals("OUTPUT") || formTypeCode.equals("CONSOLIDATED")) && ReportStatus.statusesMap.get(lastStatus.getStatusCode()).equals(ReportStatus.Status.DRAFT)) {
                    // если контроль для выходных форм, он не должен вычислять для черновиков
                    return;
                }
                Long lastReportHistoryId = persistence.getLastReportHistoryIdByReportId(report.getId(), forSuperUser, null);
                if (lastReportHistoryId != null) {
                    String value = persistence.getReportValueByKeyReportHistoryId(key, lastReportHistoryId);
                    if (value != null)
                        data.put(key, value);
                }
            }
        }
    }

    private Map<String, String> loadReportFullData(String formCode) {
        Map<String, String> result = new HashMap<String, String>();
        Report report = persistence.getReportByIdnDateForm(idn, reportDate, formCode);
        if (report != null) {
            ReportStatus lastStatus = persistence.getLastReportStatusByReportId(report.getId(), forSuperUser, null);
            if (forSuperUser && (lastStatus == null || !ReportStatus.suUserStatuses.contains(ReportStatus.statusesMap.get(lastStatus.getStatusCode())))) {
                // если контроль запускает пользователь нац.банка, он должен видеть только свои статусы
                return result;
            }
            if ((formTypeCode.equals("OUTPUT") || formTypeCode.equals("CONSOLIDATED")) && ReportStatus.statusesMap.get(lastStatus.getStatusCode()).equals(ReportStatus.Status.DRAFT)) {
                // если контроль для выходных форм, он не должен вычислять для черновиков
                return result;
            }
            Long lastReportHistoryItemId = persistence.getLastReportHistoryIdByReportId(report.getId(), forSuperUser, null);
            if (lastReportHistoryItemId == null || lastReportHistoryItemId == 0) {
                return result;
            }
            ReportHistory reportHistoryItem = persistence.getReportHistory(lastReportHistoryItemId, true, false);
            String jsonData = reportHistoryItem.getData();
            if (jsonData == null || jsonData.isEmpty()) return result;

            Type typeMapStringString = new TypeToken<Map<String, String>>() {
            }.getType();
            result = gson.fromJson(jsonData, typeMapStringString);
        }
        return result;
    }

    private String grabFormCode(String key) throws FormulaSyntaxError {
        if (key.indexOf(";") < 0)
            throw new FormulaSyntaxError();

        String formCode_ = key.substring(0, key.indexOf(";"));
        if (formCode_.length() == 0)
            throw new FormulaSyntaxError();

        // регестрируем для того чтобы использовать в desc_ru
        externalFormCodes.add(formCode_);

        return formCode_;
    }

    private String getFormNameByFormCode(String formCode, String languageCode) {
        String key = languageCode + "_" + formCode;

        if (formCodeFormNameMap.containsKey(key))
            return formCodeFormNameMap.get(key);

        String formName = persistence.getFormNameByFormCodeLanguageCodeReportDate(formCode, languageCode, reportDate);
        if (!formName.isEmpty()) {
            formCodeFormNameMap.put(key, formName);
            return formName;
        }

        return formCode;
    }

        /*private String replaceFormCodeToFormName(String text, String languageCode) {
            for (String formCode : externalFormCodes) {
                String searchText = "[" + formCode + "]";
                String formName = getFormNameByFormCode(formCode, languageCode);
                if (!formName.isEmpty())
                    text = text.replace(searchText, formName);
            }
            return text;
        }*/

    public String formatDescription(RefCrosscheckItem item, String rowId, String a, String b) {
        String text = item.getDescrRuL() + " " + ConditionEnum.valueOf(item.getFormulaSymbol()).nameToSign() + " " + item.getDescrRuR();
        if (a != null && b != null)
            text += ": " + a + " " + ConditionEnum.valueOf(item.getFormulaSymbol()).nameToSign() + " " + b;
        String s = rowId;
        if (s.startsWith("$D.")) {
            s = s.substring(3);
        }
        return text.replace("[$D.group.n]", s);
    }

    public boolean hasNextStep() {
        return numberStack.size() > 0;
    }

    public void nextStep() {
        if (numberStack.size() == 0) {
            throw new IllegalStateException("Perform control error");
        }
        currentRowNumber = numberStack.pop();
    }

    public void resetStep() {
        numberStack.clear();
        numberStack.push("");
    }

    private boolean isDynamicRowKey(String key) {
        return key.endsWith("$D.group.n");
    }

    private String getDynamicFullKey(String key_, String formCode) {
        if (currentRowNumber.isEmpty() && numberStack.size() == 0) {
            if (rowNumbers.containsKey(formCode)) {
                numberStack.addAll(rowNumbers.get(formCode));
            }
            if (numberStack.size() > 0) {
                nextStep();
            } else {
                return key_;
            }
        }
        return key_.substring(0, key_.indexOf("$D.group.n")) + currentRowNumber;
    }

    //region getters/setters

    public boolean isCascade() {
        return cascade;
    }

    public void setCascade(boolean cascade) {
        this.cascade = cascade;
    }

    public Set<CompoundReportKey> getCascadeReports() {
        return cascadeReports;
    }

    public void setCascadeReports(Set<CompoundReportKey> cascadeReports) {
        this.cascadeReports = cascadeReports;
    }

    public boolean isForSuperUser() {
        return forSuperUser;
    }

    public void setForSuperUser(boolean forSuperUser) {
        this.forSuperUser = forSuperUser;
    }

    public Long getRefSubjectTypeRecId() {
        return refSubjectTypeRecId;
    }

    public void setRefSubjectTypeRecId(Long refSubjectTypeRecId) {
        this.refSubjectTypeRecId = refSubjectTypeRecId;
    }

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    public Map<ControlResultKey, List<ControlResult>> getControlResultMap() {
        return controlResultMap;
    }

    public String getCurrentRowNumber() {
        return currentRowNumber;
    }

    public String getFormTypeCode() {
        return formTypeCode;
    }

    public void setFormTypeCode(String formTypeCode) {
        this.formTypeCode = formTypeCode;
    }

    public Set<String> getExternalFormCodes() {
        return externalFormCodes;
    }

    public Map<String, String> getMapData() {
        return mapData;
    }

    public void setMapData(Map<String, String> mapData) {
        this.mapData = mapData;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public void setPersistence(PersistenceLocal persistence) {
        this.persistence = persistence;
    }

    public NoReportDataError getNoReportDataError() {
        return noReportDataError;
    }

    public void setNoReportDataError(NoReportDataError noReportDataError) {
        this.noReportDataError = noReportDataError;
    }

    public IllegalExtSystemIndicator getIllegalExtSystemIndicator() {
        return illegalExtSystemIndicator;
    }

    public void setIllegalExtSystemIndicator(IllegalExtSystemIndicator illegalExtSystemIndicator) {
        this.illegalExtSystemIndicator = illegalExtSystemIndicator;
    }

    public RefCrosscheckItem getCurrentControl() {
        return currentControl;
    }

    public void setCurrentControl(RefCrosscheckItem currentControl) {
        this.currentControl = currentControl;
    }

    public long getExtSysId() {
        return extSysId;
    }

    public String getExtSysNameRu() {
        return extSysNameRu;
    }

    //endregion getters/setters
}
