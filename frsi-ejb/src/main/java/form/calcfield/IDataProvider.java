package form.calcfield;

import java.util.List;

/**
 * Created by Nuriddin.Baideuov on 16.09.2015.
 */
public interface IDataProvider {
    void setValue(String key, String value);

    String getValue(String key);

    boolean hasRateA(String currencyRecId);

    double sumInputValuesByGroup(String groupId);

    double sumInputValuesByKeyValueRange(Input[] inputs, String minId, String maxId);

    double sumInputValues(List<String> keys);

    Input getElementById(String id);

    Input[] getTableInputs(String tableId);

    void updateDynamicRowsColumn(String colGroupId, String[] argColumns);

    String autoNumeric(String key, String value);

    String autoNumeric(String key, double value);

    void calculateBalanceAccounts();

    double round0(double value);

    double round1(double value);

    double round2(double value);

    double round3(double value);

    double round4(double value);

    double round5(double value);

    double round6(double value);

    void validateNumber(Object o);
}
