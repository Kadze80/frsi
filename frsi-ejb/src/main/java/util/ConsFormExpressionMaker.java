package util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ejb.Persistence;
import entities.*;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by nuriddin on 10/19/16.
 */
public class ConsFormExpressionMaker {
    private Persistence persistence;
    private String formCode;
    private String outFormCode;
    private Date reportDate;
    private Map<String, OutReportRuleItem> rules;

    public ConsFormExpressionMaker(Persistence persistence, String outFormCode, Date reportDate) {
        this.persistence = persistence;
        this.outFormCode = outFormCode;
        this.reportDate = reportDate;
        rules = new HashMap<String, OutReportRuleItem>();
    }

    public void make() {
        formCode = outFormCode.substring(0, outFormCode.indexOf("_out"));
        List<Form> forms = persistence.getFormsByCodeLanguageReportDate(outFormCode, "ru", reportDate, null);
        if (forms.size() == 0)
            throw new RuntimeException(MessageFormat.format("Form {0} not found", outFormCode));
        Form consForm = forms.get(0);
        Form inputForm = persistence.getFormsByCodeLanguageReportDate(formCode, "ru", reportDate, null).get(0);
        FormHistory formHistory = persistence.getFormHistoryWithInputValueChecks(consForm.getFormHistory().getId());
        FormHistory inFormHistory = persistence.getFormHistoryWithInputValueChecks(inputForm.getFormHistory().getId());

        List<InputValueCheck> ivch;
        Gson gson = new Gson();
        Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {
        }.getType();
        ivch = gson.fromJson(formHistory.getInputValueChecks(), typeListInputValueCheck);
        if (ivch == null) ivch = new ArrayList<InputValueCheck>();
        final Map<String, List<InputValueCheck>> ivchIndex = makeIndexOfInputValueChecks(ivch);

        List<InputValueCheck> inIvch;
        inIvch = gson.fromJson(inFormHistory.getInputValueChecks(), typeListInputValueCheck);
        if (inIvch == null) inIvch = new ArrayList<InputValueCheck>();
        final Map<String, List<InputValueCheck>> inIvchIndex = makeIndexOfInputValueChecks(inIvch);

        if (inputForm.getFormHistory().getFormTag() != null && inputForm.getFormHistory().getFormTag().hasDynamicRows) {
            Map<String, Set<String>> groups = new HashMap<String, Set<String>>();
            for (InputValueCheck check : ivch) {
                Indicator ind;
                try {
                    ind = ParserHelper.parseIndicator(check.getKey(), true, true);
                } catch (IndicatorParseException e) {
                    throw new RuntimeException(MessageFormat.format("Cant parse indicator {0}", e.getIndicator()));
                }
                if (ind.getRowId().equalsIgnoreCase("$DynamicRowId"))
                    continue;
                if (!groups.containsKey(ind.getContainer()))
                    groups.put(ind.getContainer(), new HashSet<String>());
                Set<String> set = groups.get(ind.getContainer());
                String parent = null;
                if (ind.getRowId().contains("."))
                    parent = ind.getRowId().substring(0, ind.getRowId().lastIndexOf("."));
                Iterator<String> it = set.iterator();
                boolean add = true;
                while (it.hasNext()) {
                    String rowId = it.next();
                    if (parent != null && rowId.equals(parent)) {
                        it.remove();
                    } else {
                        if (rowId.startsWith(ind.getRowId() + ".")) {
                            add = false;
                            break;
                        }
                    }
                }
                if (add)
                    set.add(ind.getRowId());
            }
            for (InputValueCheck check : ivch) {
                String key = check.getKey();
                String inKey = key.replace(outFormCode, formCode);
                List<InputValueCheck> inChecks = inIvchIndex.get(inKey);
                if (inChecks == null) {
//                    throw new RuntimeException(MessageFormat.format("InputValueCheck not found for {0}", inKey));
                    continue;
                }
                Indicator ind;
                try {
                    ind = ParserHelper.parseIndicator(key, true, true);
                } catch (IndicatorParseException e) {
                    throw new RuntimeException(MessageFormat.format("Cant parse indicator {0}", key));
                }
                OutReportRuleItem item = new OutReportRuleItem();
                item.setFormCode(outFormCode);
                item.setFieldName(ind.getFieldName());
                item.setTableName(ind.getContainer().substring(outFormCode.length() + 1));
                item.setKeyValue(ind.getRowId());
                item.setDataType(getDataType(check));
                String formula = "[" + formCode + ";" + getDataType(inChecks.get(0)) + ";" + inKey + "]";

                if (ind.getRowId().equals("$DynamicRowId")) {
                    Set<String> g = groups.get(ind.getContainer());
                    if (g == null) throw new RuntimeException("Smth went wrong");
                    for (String group : g) {
                        OutReportRuleItem clone = new OutReportRuleItem(item);
                        clone.setKeyValue("$D." + group + ".n");
                        String k = key.replace("$DynamicRowId", clone.getKeyValue());
                        String f = formula.replace("$DynamicRowId", clone.getKeyValue());
                        if (formCode.equals("fs_rv") && (inKey.contains("bank") || inKey.contains("country")) ||
                                formCode.equals("fs_ikdu") && (inKey.contains("name_jurper")) ||
                                formCode.equals("fs_repo") && (inKey.contains("nin_iin")) ||
                                formCode.equals("fs_sscb") && (inKey.contains("name_emitter")) ||
                                formCode.equals("fs_rv_apk_kp") && (inKey.contains("bank") || inKey.contains("country")) ||
                                formCode.equals("fs_ikdu_apk_kp") && (inKey.contains("name_jurper")) ||
                                formCode.equals("fs_repo_apk_kp") && (inKey.contains("nin_iin")) ||
                                formCode.equals("fs_sscb_apk_kp") && (inKey.contains("name_emitter"))) {
                            clone.setFormula(f);
                            clone.setGrouping(true);
                            clone.setPriority(1);
                        } else {
                            clone.setPriority(2);
                            clone.setFormula("sum(" + f + ')');
                        }
                        rules.put(k, clone);
                    }
                } else {
                    item.setPriority(2);
                    item.setFormula("sum(" + formula + ')');
                    rules.put(key, item);
                }

            }
        } else {
            for (InputValueCheck check : ivch) {
                String dt = getDataType(check);
                if (dt.startsWith("n")) {
                    String key = check.getKey();
                    String inKey = key.replace(outFormCode, formCode);
                    List<InputValueCheck> inChecks = inIvchIndex.get(inKey);
                    if (inChecks == null) {
//                        throw new RuntimeException(MessageFormat.format("InputValueCheck not found for {0}", inKey));
                        continue;
                    }
                    Indicator ind;
                    try {
                        ind = ParserHelper.parseIndicator(key, true, true);
                    } catch (IndicatorParseException e) {
                        throw new RuntimeException(MessageFormat.format("Cant parse indicator {0}", key));
                    }
                    OutReportRuleItem item = new OutReportRuleItem();
                    item.setPriority(1);
                    item.setFormCode(outFormCode);
                    item.setFieldName(ind.getFieldName());
                    item.setTableName(ind.getContainer().substring(outFormCode.length() + 1));
                    item.setKeyValue(ind.getRowId());
                    item.setDataType(getDataType(check));
                    String formula = "[" + formCode + ";" + getDataType(inChecks.get(0)) + ";" + inKey + "]";
                    item.setFormula("sum(" + formula + ')');
                    rules.put(key, item);
                }
            }
        }

        persistence.clearOutReportRuleItems(forms.get(0).getFormHistory().getId());
        for (OutReportRuleItem item : rules.values()) {
            persistence.insertOutReportRuleItem(item, forms.get(0).getFormHistory().getId());
        }
    }

    private String getDataType(InputValueCheck check) {
        if (check.getMask() != null && !check.getMask().isEmpty())
            return check.getMask().replace("money", "n");
        if (check.getRef() != null && !check.getRef().isEmpty())
            return "n0";
        if (check.getValueType() != null && check.getValueType().equals("date"))
            return "d";
        if (check.getValueType() != null && check.getValueType().equals("string"))
            return "s";
        return "s";
    }

    private Map<String, List<InputValueCheck>> makeIndexOfInputValueChecks(List<InputValueCheck> inputValueChecks) {
        Map<String, List<InputValueCheck>> inputValueCheckIndex = new HashMap<String, List<InputValueCheck>>();
        for (InputValueCheck inputValueCheck : inputValueChecks) {
            if (!inputValueCheckIndex.containsKey(inputValueCheck.getKey()))
                inputValueCheckIndex.put(inputValueCheck.getKey(), new ArrayList<InputValueCheck>());
            inputValueCheckIndex.get(inputValueCheck.getKey()).add(inputValueCheck);
        }
        return inputValueCheckIndex;
    }

    private Set<String> getNumericValueFields(String formCode, Date reportDate) {
        Set<String> fields = new HashSet<String>();

        List<Form> forms = persistence.getFormsByCodeReportDate(formCode, reportDate);
        if (forms.size() == 0)
            return fields;

        FormHistory formHistory = persistence.getFormHistoryWithInputValueChecks(forms.get(0).getFormHistory().getId());
        List<InputValueCheck> inputValueChecks = new ArrayList<InputValueCheck>();
        Gson gson = new Gson();
        Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {
        }.getType();
        inputValueChecks = gson.fromJson(formHistory.getInputValueChecks(), typeListInputValueCheck);

        for (InputValueCheck item : inputValueChecks) {
            if (item.getRef() != null && !item.getRef().trim().isEmpty())
                continue;
            if (item.getValueType() == null || item.getInputType().trim().isEmpty() ||
                    (!item.getValueType().equals("int") && !item.getValueType().equals("float")))
                continue;
            String key = item.getKey();
            fields.add(key.substring(key.indexOf("*") + 1, key.indexOf(":")));
        }

        return fields;
    }
}
