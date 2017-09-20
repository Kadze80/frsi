package form.calcfield;

import ejb.ReferenceLocal;
import entities.BalanceAccountRec;
import entities.InputValueCheck;
import util.Convert;
import util.DivisionByZeroException;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by Nuriddin.Baideuov on 16.09.2015.
 */
public class DataProvider implements IDataProvider {

    private Map<String, String> kvMap;
    private ReferenceLocal reference;
    private Date reportDate;

    private List<String> rateACurrencies;
    private Map<String, Input> inputMap;

    private Map<String, List<InputValueCheck>> dataMasks;

    public DataProvider(Map<String, String> kvMap, ReferenceLocal reference, Date reportDate, List<InputValueCheck> inputValueChecks) {
        if (kvMap == null) throw new NullPointerException();
        this.kvMap = kvMap;
        this.reference = reference;
        this.reportDate = reportDate;

        inputMap = new HashMap<String, Input>();
        for (Map.Entry<String, String> entry : kvMap.entrySet()) {
            inputMap.put(entry.getKey(), new Input(entry.getKey(), entry.getValue()));
        }

        dataMasks = new HashMap<String, List<InputValueCheck>>();
        if (inputValueChecks != null)
            for (InputValueCheck inputValueCheck : inputValueChecks) {
                if (inputValueCheck.getMask() != null && !inputValueCheck.getMask().trim().isEmpty()) {
                    if (!dataMasks.containsKey(inputValueCheck.getKey()))
                        dataMasks.put(inputValueCheck.getKey(), new ArrayList<InputValueCheck>());
                    dataMasks.get(inputValueCheck.getKey()).add(inputValueCheck);
                }
            }
    }

    @Override
    public void setValue(String key, String value) {
        kvMap.put(key, value);
    }

    @Override
    public String getValue(String key) {
        return kvMap.get(key);
    }

    @Override
    public boolean hasRateA(String currencyRecId) {
        if (rateACurrencies == null) rateACurrencies = reference.getRateACurrencyRecIds(reportDate);
        return rateACurrencies.contains(currencyRecId);
    }

    @Override
    public double sumInputValuesByGroup(String groupId) {
        int lastColonPos = groupId.lastIndexOf(':');
        String idPrefix = groupId.substring(0, lastColonPos) + ":$D." + groupId.substring(lastColonPos + 1) + ".";
        List<String> keys = new ArrayList<String>();
        for (String key : kvMap.keySet()) {
            if (key.startsWith(idPrefix)) keys.add(key);
        }
        return sumInputValues(keys);
    }

    @Override
    public double sumInputValuesByKeyValueRange(Input[] inputs, String minId, String maxId) {
        int minLastColonPos = minId.lastIndexOf(':');
        int minLastDotPos = minId.lastIndexOf('.');
        String minIdPrefix = minLastDotPos < minLastColonPos ? minId.substring(0, minLastColonPos + 1) : minId.substring(0, minLastDotPos + 1);
        int minKeyValue;
        try {
            minKeyValue = minLastDotPos < minLastColonPos ? parseInt(minId.substring(minLastColonPos + 1)) : parseInt(minId.substring(minLastDotPos + 1));
        } catch (NumberFormatException e) {
            minKeyValue = 0;
        }
        int maxKeyValue;
        try {
            maxKeyValue = minLastDotPos < minLastColonPos ? parseInt(maxId.substring(minLastColonPos + 1)) : parseInt(maxId.substring(minLastDotPos + 1));
        } catch (NumberFormatException e) {
            maxKeyValue = 0;
        }
        List<String> keys = new ArrayList<String>();
        for (int i = 0; i < inputs.length; i++) {
            int lastColonPos = inputs[i].id.lastIndexOf(':');
            int lastDotPos = inputs[i].id.lastIndexOf('.');
            String idPrefix = lastDotPos < lastColonPos ? inputs[i].id.substring(0, lastColonPos + 1) : inputs[i].id.substring(0, lastDotPos + 1);
            int keyValue;
            try {
                keyValue = lastDotPos < lastColonPos ? parseInt(inputs[i].id.substring(lastColonPos + 1)) : parseInt(inputs[i].id.substring(lastDotPos + 1));
            } catch (NumberFormatException e) {
                continue;
            }
            if (idPrefix.equals(minIdPrefix) && minKeyValue <= keyValue && keyValue <= maxKeyValue)
                keys.add(inputs[i].id);
        }
        return sumInputValues(keys);
    }

    @Override
    public double sumInputValues(List<String> keys) {
        double sum = 0;
        for (String key : keys) {
            String value = kvMap.get(key);
            sum += parseFloat(value);
        }
        return sum;
    }

    @Override
    public Input getElementById(String id) {
        return inputMap.get(id);
    }

    @Override
    public Input[] getTableInputs(String tableId) {
        List<Input> inputs = new ArrayList<Input>();
        for (Map.Entry<String, Input> entry : inputMap.entrySet())
            if (entry.getKey().startsWith(tableId))
                inputs.add(entry.getValue());
        return inputs.toArray(new Input[inputs.size()]);
    }

    @Override
    public void updateDynamicRowsColumn(String colGroupId, String[] argColumns) {
        int lastColonPos = colGroupId.lastIndexOf(':');
        String idPrefix = colGroupId.substring(0, lastColonPos) + ":$D." + colGroupId.substring(lastColonPos + 1) + ".";
        List<String> colGroupInputs = new ArrayList<String>();
        for (String key : kvMap.keySet())
            if (key.startsWith(idPrefix)) colGroupInputs.add(key);
        if (colGroupInputs.size() == 0) {
            colGroupInputs.addAll(findMinorId(colGroupId, argColumns));
        }
        for (String key : colGroupInputs) {
            int starPos = key.indexOf('*');
            int firstColonPos = key.indexOf(':');
            String idPart1 = key.substring(0, starPos + 1);
            String idPart2 = key.substring(firstColonPos);
            double sum = 0;
            for (int j = 0; j < argColumns.length; j++) {
                String argId = idPart1 + argColumns[j] + idPart2;
                sum += parseFloat(getValue(argId));
            }
            setValue(key, autoNumeric(key, sum));
        }
    }

    private List<String> findMinorId(String colGroupId, String[] argColumns) {
        List<String> colGroupInputs = new ArrayList<String>();
        for (String argCol : argColumns) {
            int lastColonPos = colGroupId.lastIndexOf(':');
            int mStarPos = colGroupId.indexOf('*');
            int mfirstColonPos = colGroupId.indexOf(':');
            String mIdPart1 = colGroupId.substring(0, mStarPos + 1);
            String mIdPart2 = colGroupId.substring(mfirstColonPos, lastColonPos) + ":$D." + colGroupId.substring(lastColonPos + 1) + ".";
            String searchIdPrefix = mIdPart1 + argCol + mIdPart2;
            String idPrefix = colGroupId.substring(0, lastColonPos) + ":$D." + colGroupId.substring(lastColonPos + 1) + ".";
            for (String key : kvMap.keySet()) {
                if (key.startsWith(searchIdPrefix))
                    colGroupInputs.add(idPrefix + key.substring(searchIdPrefix.length()));
            }
            if (colGroupInputs.size() > 0) {
                break;
            }
        }
        return colGroupInputs;
    }

    private double parseFloat(String initialValue) {
        return Convert.parseDouble(initialValue);
    }

    private int parseInt(String initialValue) {
        return Convert.parseInt(initialValue);
    }

    private String convertDecimalFormatSymbols(double sum, int fractionDigits) {
        sum = round(sum, fractionDigits);
        String pattern = "###";
        for (int i = 0; i < fractionDigits; i++) {
            if (i == 0)
                pattern += ".";
            pattern += "#";
        }
        DecimalFormat df = new DecimalFormat(pattern);
        DecimalFormatSymbols custom = new DecimalFormatSymbols();
        custom.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(custom);
        return df.format(sum);
    }

    @Override
    public String autoNumeric(String key, String value) {
        return convertDecimalFormatSymbols(parseFloat(value), getMask(key));
    }

    @Override
    public String autoNumeric(String key, double value) {
        return convertDecimalFormatSymbols(value, getMask(key));
    }

    @Override
    public void calculateBalanceAccounts() {
        List<BalanceAccountRec> balanceAccounts = reference.getSortedBalanceAccounts(reportDate);
        final Map<String, BalanceAccountRec> balanceAccountMap = new HashMap<String, BalanceAccountRec>();
        for (BalanceAccountRec ba : balanceAccounts) {
            balanceAccountMap.put(ba.getCode(), ba);
        }
        final String keyPrefix = "balance_accounts_array*sum:code:";
        SortedMap<String, Double> sortedMap = new TreeMap<String, Double>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String code1 = o1.substring(keyPrefix.length()),
                        code2 = o2.substring(keyPrefix.length());
                BalanceAccountRec ba1 = balanceAccountMap.get(code1),
                        ba2 = balanceAccountMap.get(code2);
                if (ba1 == null)
                    return -1;
                if (ba2 == null)
                    return 1;
                Long res = ba1.getRowNum() - ba2.getRowNum();
                return res.intValue();
            }
        });
        for (Map.Entry<String, String> entry : kvMap.entrySet()) {
            if (entry.getKey().startsWith("balance_accounts_array*sum:code")) {
                String key = entry.getKey();
                String value = entry.getValue();
                while (key != null) {
                    sortedMap.put(key, parseFloat(value));
                    String code = key.substring(key.lastIndexOf(":") + 1);
                    BalanceAccountRec ba = balanceAccountMap.get(code);
                    key = null;
                    if (ba != null && ba.getParentCode() != null && !ba.getParentCode().isEmpty()) {
                        String parentKey = "balance_accounts_array*sum:code:" + ba.getParentCode();
                        if (!kvMap.containsKey(parentKey) && !sortedMap.containsKey(parentKey)) {
                            key = parentKey;
                            value = "0";
                        }
                    }
                }
            }
        }

        Iterator<Map.Entry<String, Double>> iterator = sortedMap.entrySet().iterator();
        String[] keysByLevel = new String[4];
        double[] valuesByLevel = new double[4];

        for (int i = 0; i < valuesByLevel.length; i++)
            valuesByLevel[i] = 0;

        int prevLevel = -1;
        double tempData = 0;
        while (iterator.hasNext()) {
            Map.Entry<String, Double> entry = iterator.next();

            int level;
            String code = entry.getKey().substring(keyPrefix.length());
            BalanceAccountRec ba = balanceAccountMap.get(code);
            if (ba != null)
                level = ba.getLevel();
            else
                level = 0;

            if (prevLevel >= level && prevLevel != -1) {
                valuesByLevel[prevLevel] += tempData;
                for (int lev = prevLevel; lev > level; lev--) {
                    int upLev = lev - 1;
                    double childSum = valuesByLevel[lev];
                    valuesByLevel[upLev] += childSum;
                    kvMap.put(keysByLevel[upLev], autoNumeric(keysByLevel[upLev], childSum));
                }

                for (int i = level + 1; i < valuesByLevel.length; i++) {
                    valuesByLevel[i] = 0;
                }
            }

            keysByLevel[level] = entry.getKey();
            tempData = entry.getValue();
            prevLevel = level;
        }

        for (int lev = prevLevel; lev > 0; lev--) {
            valuesByLevel[lev] += tempData;

/*
            for (int lev2 = lev; lev2 > 0; lev2--) {
                int upLev = lev2 - 1;
                double childSum = valuesByLevel[lev2];
                valuesByLevel[upLev] += childSum;
            }
*/

            int upLevel = lev - 1;
            tempData = valuesByLevel[upLevel];
            valuesByLevel[upLevel] = valuesByLevel[lev];
            kvMap.put(keysByLevel[upLevel], autoNumeric(keysByLevel[upLevel], valuesByLevel[upLevel]));
        }
    }

    private int getMask(String key) {
        int mask = 6;
        String finalKey;
        int dPos = key.indexOf("$D.");
        if (dPos == -1)
            finalKey = key;
        else {
            finalKey = key.substring(0, dPos) + "$DynamicRowId";
        }
        if (dataMasks.containsKey(finalKey)) {
            String maskStr = "";
            List<InputValueCheck> inputValueChecks = dataMasks.get(finalKey);
            if (inputValueChecks.size() == 1)
                maskStr = inputValueChecks.get(0).getMask();
            else {
                String groupId = key.substring(0, key.lastIndexOf(".") + 1);
                for (InputValueCheck item : inputValueChecks) {
                    if (item.getGroupId() == null)
                        maskStr = item.getMask();
                    else if (item.getGroupId().equals(groupId)) {
                        maskStr = item.getMask();
                        break;
                    }
                }
            }
            if (maskStr.startsWith("money")) {
                try {
                    mask = Integer.parseInt(maskStr.substring("money".length()));
                } catch (NumberFormatException e) {
                    throw new NumberFormatException(MessageFormat.format("Неверно указана маска {0}", maskStr));
                }
            }
        }
        return mask;
    }

    public Map<String, String> getKvMap() {
        return kvMap;
    }

    public void setKvMap(Map<String, String> kvMap) {
        this.kvMap = kvMap;
    }

    private double round(double value, int digit) {
        switch (digit) {
            case 0:
                return round0(value);
            case 1:
                return round1(value);
            case 2:
                return round2(value);
            case 3:
                return round3(value);
            case 4:
                return round4(value);
            case 5:
                return round5(value);
            case 6:
                return round6(value);
            default:
                return round2(value);
        }
    }

    @Override
    public double round0(double value) {
        return (double) Math.round(value * 1) / 1;
    }

    @Override
    public double round1(double value) {
        return (double) Math.round(value * 10) / 10;
    }

    @Override
    public double round2(double value) {
        return (double) Math.round(value * 100) / 100;
    }

    @Override
    public double round3(double value) {
        return (double) Math.round(value * 1000) / 1000;
    }

    @Override
    public double round4(double value) {
        return (double) Math.round(value * 10000) / 10000;
    }

    @Override
    public double round5(double value) {
        return (double) Math.round(value * 100000) / 100000;
    }

    @Override
    public double round6(double value) {
        return (double) Math.round(value * 1000000) / 1000000;
    }

    @Override
    public void validateNumber(Object o) {
        Double d = null;
        if (o instanceof Double) {
            d = (Double) o;
        } else if (o instanceof String) {
            d = parseFloat((String) o);
        }
        if (d != null) {
            if (d == Double.NEGATIVE_INFINITY || d == Double.POSITIVE_INFINITY) {
                throw new DivisionByZeroException();
            }
        }

    }

    public static double round2t(double value) {
        return (double) Math.round(value * 100) / 100;
    }

    public static void main(String[] args) {
        /*double s = 524.6354;
        System.out.println(round2t(s));*/
        String str = "0.0";
        try {
            double d = Double.parseDouble(str);
            System.out.println(d);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
