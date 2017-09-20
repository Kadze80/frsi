package parser.functions;

import dataform.FormulaSyntaxError;
import dataform.NoReportDataError;
import entities.RefRespondentItem;
import parser.*;
import parser.parser.ParsedAggFunction;
import parser.parser.ParsedItem;
import parser.parser.ParsedKey;
import parser.parser.ParsedRule;
import util.Convert;

import java.text.MessageFormat;
import java.util.*;

/**
 * Created by nuriddin on 9/7/16.
 */
public class ContextProvider {
    private AbstractDataProvider dataProvider;
    private ParsedRule parsedRule;
    private Map<Integer, ParsedKey> keyMap;
    private String dynamicRowId;
    private String dynamicCellId;
    private String rangeRowId;
    private String rangeField;
    private ReportKey reportKey;
    private Map<String, Set<String>> filterRows;
    private int outRowIndex;
    private Map<Integer, NoReportDataError> noReportErrors = new HashMap<Integer, NoReportDataError>();
    private NoReportDataError noReportDataError;
    private RequiredKeyError requiredKeyError;
    private IncorrectFormError incorrectFormError;

    public ContextProvider(AbstractDataProvider dataProvider) {
        this.dataProvider = dataProvider;
        reportKey = new ReportKey();
    }

    public void init() {
        dynamicCellId = null;
        dynamicRowId = null;
        filterRows = null;
    }

    public double n0(int keyId) throws RequiredKeyError, IncorrectFormError {
        try {
            String value = getStringValue(keyId);
            if (value != null) {
                if (value.equals("NaN"))
                    return Convert.parseDouble(value);
                else
                    return Convert.parseLong(value);
            }
            return 0;
        } catch (NoReportDataError ex) {
            return 0;
        }
    }

    public double n1(int keyId) throws RequiredKeyError, IncorrectFormError {
        try {
            String value = getStringValue(keyId);
            if (value != null)
                return Convert.parseDouble(value);
            return 0;
        } catch (NoReportDataError ex) {
            return 0;
        }
    }

    public double n2(int keyId) throws RequiredKeyError, IncorrectFormError {
        try {
            String value = getStringValue(keyId);
            if (value != null)
                return Convert.parseDouble(value);
            return 0;
        } catch (NoReportDataError ex) {
            return 0;
        }
    }

    public double n3(int keyId) throws RequiredKeyError, IncorrectFormError {
        try {
            String value = getStringValue(keyId);
            if (value != null)
                return Convert.parseDouble(value);
            return 0;
        } catch (NoReportDataError ex) {
            return 0;
        }
    }

    public double n4(int keyId) throws RequiredKeyError, IncorrectFormError {
        try {
            String value = getStringValue(keyId);
            if (value != null)
                return Convert.parseDouble(value);
            return 0;
        } catch (NoReportDataError ex) {
            return 0;
        }
    }

    public double n5(int keyId) throws RequiredKeyError, IncorrectFormError {
        try {
            String value = getStringValue(keyId);
            if (value != null)
                return Convert.parseDouble(value);
            return 0;
        } catch (NoReportDataError ex) {
            return 0;
        }
    }

    public double n6(int keyId) throws RequiredKeyError, IncorrectFormError {
        try {
            String value = getStringValue(keyId);
            if (value != null)
                return Convert.parseDouble(value);
            return 0;
        } catch (NoReportDataError ex) {
            return 0;
        }
    }

    public String s(int keyId) throws RequiredKeyError, IncorrectFormError {
        try {
            String value = getStringValue(keyId);
            if (value != null)
                return value;
            return "";
        } catch (NoReportDataError ex) {
            return "";
        }
    }

    public Date d(int keyId) throws RequiredKeyError, IncorrectFormError {
        try {
            String value = getStringValue(keyId);
            if (value != null)
                return Convert.getDateFromString(value);
            return null;
        } catch (NoReportDataError ex) {
            return null;
        }
    }

    private String getStringValue(int keyId) throws RequiredKeyError, NoReportDataError, IncorrectFormError {
        ParsedKey k = getParsedKey(keyId);
        String key = getKey(k);
        reportKey.setFormCode(k.getFormCode());
        noReportDataError = null;
        String value;
        Date rd = k.getDates().get(reportKey.getReportDate());
        try {
            value = dataProvider.getValue(key, reportKey.getFormCode(), reportKey.getBin(), rd);
        } catch (NoReportDataError e) {
            if (k.isRequired()) {
                String m = MessageFormat.format("Нет исходного отчета {0} за {1} у организации {2}",
                        reportKey.getFormCode(), Convert.dateFormatRus.format(rd), dataProvider.getRespByIdn(dataProvider.getLastUsedReportKey().getBin()).getNameRu());
                requiredKeyError = new RequiredKeyError(m);
                throw requiredKeyError;
            } else {
                noReportDataError = e;
                throw e;
            }
        } catch (IncorrectFormError e) {
            /*if (k.isRequired()) {
                String m = MessageFormat.format("Форма изменена у исходного отчета {0} за {1} у организации {2}",
                        reportKey.getFormCode(), Convert.dateFormatRus.format(rd), dataProvider.getRespByIdn(dataProvider.getLastUsedReportKey().getBin()).getNameRu());
                requiredKeyError = new RequiredKeyError(m);
                throw requiredKeyError;
            } else {
                throw e;
            }*/
            String m = MessageFormat.format("Форма изменена у исходного отчета {0} за {1} у организации {2}",
                    reportKey.getFormCode(), Convert.dateFormatRus.format(rd), dataProvider.getRespByIdn(dataProvider.getLastUsedReportKey().getBin()).getNameRu());
            incorrectFormError = new IncorrectFormError(m, e.getFormCode(), e.getReportDate());
            throw incorrectFormError;
        }
        return value;
    }

    public ParsedKey getParsedKey(int keyId) {
        if (!keyMap.containsKey(keyId)) {
            throw new IllegalStateException(MessageFormat.format("Ключ {0} не найден", keyId));
        }
        return keyMap.get(keyId);
    }

    private String getKey(ParsedKey k) {
        String key = k.getKey();
        if (k.isDynamicRow() && !k.isRowRange()) {
            String rid;
            if (dynamicRowId == null || dynamicRowId.isEmpty()) {
                if (k.getFormCode().equalsIgnoreCase(dataProvider.getOutputFormCode())) {
                    rid = key.substring(key.toLowerCase().indexOf("$d."), key.toLowerCase().lastIndexOf(".n") + 1) + String.valueOf(outRowIndex);
                } else {
                    throw new IllegalStateException("Для динамической строки не установлена текущий rowid");
                }
            } else
                rid = dynamicRowId;
            key = key.substring(0, key.lastIndexOf(":") + 1) + rid;
        }
        if (k.isRowRange()) {
            key = Helper.changeIndicatorRow(key, rangeRowId);
        }
        if (k.isFieldRange()) {
            key = Helper.changeIndicatorField(key, rangeField);
        }
        return key;
    }

    public void setParsedRule(ParsedRule parsedRule) {
        this.parsedRule = parsedRule;
        keyMap = new HashMap<Integer, ParsedKey>();
        for (ParsedItem i : parsedRule.getItems()) {
            if (i instanceof ParsedKey) {
                ParsedKey k = (ParsedKey) i;
                keyMap.put(k.getId(), k);
            } else if (i instanceof ParsedAggFunction) {
                ParsedAggFunction f = (ParsedAggFunction) i;
                for (ParsedKey k : f.getChildren()) {
                    keyMap.put(k.getId(), k);
                }
            }
        }
    }

    public Map<String, Set<String>> getDynamicRowIdsByTemplate(ParsedKey pk) {
        /*Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        ReportKey rk = new ReportKey(reportKey);
        rk.setFormCode(pk.getFormCode());
        for (RefRespondentItem r : dataProvider.getRespondents()) {
            rk.setBin(r.getIdn());
            Set<String> rowIds = dataProvider.getRowIdsByTemplate(pk.getRowId(), rk, pk.getContainer());
            result.put(r.getIdn(), rowIds);
        }
        return result;*/
        return dataProvider.getDynamicRowIdsByTemplate(pk.getRowId(), pk.getFormCode(), pk.getContainer(), pk.getDates().get(reportKey.getReportDate()));
    }

    public String getRespondentIdn() {
        return reportKey.getBin();
    }

    public Long getRespondentRecId() {
        if (reportKey.getBin() != null) {
            RefRespondentItem r = dataProvider.getRespByIdn(reportKey.getBin());
            if (r != null)
                return r.getRecId();
        }
        return null;
    }

    public List<RefRespondentItem> getRespondents() {
        return dataProvider.getRespondents();
    }

    public void setDynamicRowId(String dynamicRowId) {
        this.dynamicRowId = dynamicRowId;
    }

    public void setContextDate(Date contextDate) {
        reportKey.setReportDate(contextDate);
        dynamicCellId = Convert.dateFormatCompact.format(contextDate);
    }

    public void validateParsedKeysForAggFunction(int[] keyIds) throws FormulaSyntaxError {
        String rowId = null;
        String pkType = null;
        String formType = null;
        String formCode = null;
        String container = null;
        for (int id : keyIds) {
            ParsedKey pk = getParsedKey(id);
            if (pkType == null) {
                pkType = pk.isDynamicRow() ? "DYNAMIC" : "STATIC";
                rowId = pk.getRowId();
                formType = dataProvider.getOutputFormCode().equalsIgnoreCase(pk.getFormCode()) ? "OUTPUT" : "INPUT";
                formCode = pk.getFormCode();
                container = pk.getContainer();
                continue;
            }
            String t = pk.isDynamicRow() ? "DYNAMIC" : "STATIC";
            String f = dataProvider.getOutputFormCode().equalsIgnoreCase(pk.getFormCode()) ? "OUTPUT" : "INPUT";
            String fc = pk.getFormCode();
            String c = pk.getContainer();

            if (!pkType.equals(t))
                throw new FormulaSyntaxError("Внутри одной агрегирующей функции нельзя использовать дин. запись со статическим");

            if (t.equals("DYNAMIC")) {
                if (!rowId.equalsIgnoreCase(pk.getRowId()))
                    throw new FormulaSyntaxError("Внутри одной агрегирующей функции использованы дин. записи разных подгрупп");
                if (!fc.equalsIgnoreCase(formCode) || !c.equalsIgnoreCase(container))
                    throw new FormulaSyntaxError("Внутри одной агрегирующей функции, где записи динамические, нельзя использовать разные формы и таблицы");
            }

            if (t.equals("STATIC") && !f.equals(formType)) {
                throw new FormulaSyntaxError("Внутри одной агрегирующей функции нельзя использовать входные и выходные формы вместе");
            }
        }
    }

    public void setContextBin(String contextBin) {
        reportKey.setBin(contextBin);
    }

    public Map<String, Set<String>> getFilterRows() {
        return filterRows;
    }

    public void setFilterRows(Map<String, Set<String>> filterRows) {
        this.filterRows = filterRows;
    }

    public int getOutRowIndex() {
        return outRowIndex;
    }

    public void setOutRowIndex(int outRowIndex) {
        this.outRowIndex = outRowIndex;
    }

    public Map<Integer, NoReportDataError> getNoReportErrors() {
        return noReportErrors;
    }

    public NoReportDataError getNoReportDataError() {
        return noReportDataError;
    }

    public RequiredKeyError getRequiredKeyError() {
        return requiredKeyError;
    }

    public String getRangeRowId() {
        return rangeRowId;
    }

    public void setRangeRowId(String rangeRowId) {
        this.rangeRowId = rangeRowId;
    }

    public String getRangeField() {
        return rangeField;
    }

    public void setRangeField(String rangeField) {
        this.rangeField = rangeField;
    }

    public Map<String, List<String>> getFields() {
        return dataProvider.getFields();
    }

    public void setFields(Map<String, List<String>> fieldMap) {
        dataProvider.setFields(fieldMap);
    }

    public IncorrectFormError getIncorrectFormError() {
        return incorrectFormError;
    }
}
