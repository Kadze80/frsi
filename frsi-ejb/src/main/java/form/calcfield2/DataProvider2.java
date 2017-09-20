package form.calcfield2;

import dataform.NoReportDataError;
import parser.DataType;
import util.Convert;

import java.text.ParseException;
import java.util.*;

/**
 * Created by nuriddin on 10/6/16.
 */
public class DataProvider2 implements IDataProvider2 {

    private Map<String, String> kvMap;
    private String rowId;
    private String field;
    private Map<String, List<String>> fieldMap;

    public DataProvider2(Map<String, String> kvMap) {
        this.kvMap = kvMap;
    }

    @Override
    public String getString(String ind) throws NoReportDataError {
        ind = getIndicator(ind);
        if (!kvMap.containsKey(ind))
            throw new NoReportDataError();
        String value = kvMap.get(ind);
        if (value == null)
            return "";
        else
            return value;
    }


    private void setString(String ind, String value) {
        ind = getIndicator(ind);
        kvMap.put(ind, value);
    }

    @Override
    public double getNumber(String ind) throws NoReportDataError {
        String value = getString(ind);
        if (value != null)
            return Convert.parseDouble(value);
        return 0;
    }

    @Override
    public void set(String ind, Object value, String dt) {
        int dataType = DataType.shortNameToDataType(dt);
        String strValue;
        if (value == null)
            strValue = "";
        else {
            switch (dataType) {
                case DataType.DOUBLE:
                    double dblValue = DataType.toDouble(value);
                    try {
                        strValue = Convert.getNumWithMaskFromStr(String.valueOf(dblValue), dt);
                    } catch (Exception e) {
                        throw new IllegalStateException(e.getMessage());
                    }
                    break;
                case DataType.LONG:
                    long lngValue = DataType.toLong(value);
                    try {
                        strValue = Convert.getNumWithMaskFromStr(String.valueOf(lngValue), dt);
                    } catch (Exception e) {
                        throw new IllegalStateException(e.getMessage());
                    }
                    break;
                case DataType.DATE:
                    Date dteValue = DataType.toDate(value);
                    strValue = Convert.getDateStringFromDate(dteValue);
                    break;
                case DataType.STRING:
                    strValue = DataType.toString(value);
                    break;
                case DataType.BOOLEAN:
                    boolean b = DataType.toBoolean(value);
                    strValue = String.valueOf(b);
                    break;
                default:
                    throw new IllegalStateException();// never should happen
            }
        }
        setString(ind, strValue);
    }

    @Override
    public Date getDate(String ind) throws NoReportDataError {
        String value = getString(ind);
        if (value != null)
            try {
                return Convert.dateTimeFormatRus.parse(value);
            } catch (ParseException e) {
                throw new IllegalStateException(e.getMessage());
            }
        return null;
    }

    @Override
    public boolean getBoolean(String ind) throws NoReportDataError {
        String value = getString(ind);
        return DataType.toBoolean(value);
    }

    @Override
    public double sumDynRow(DoubleCallback cb, String templ, String container) throws Exception {
        List<String> rowIds = this.getRowIds(container, templ);
        double res = 0;
        for (String rowId : rowIds) {
            this.setRowId(rowId);
            res += cb.call();
        }
        return res;
    }

    @Override
    public int count(String templ, String container) {
        String prefix = templ.substring(0, templ.toLowerCase().lastIndexOf(".n") + 1).toLowerCase();
        Set<String> rows = new HashSet<String>();
        for (String key : kvMap.keySet()) {
            String cont = key.substring(0, key.indexOf("*"));
            String rowId = key.substring(key.lastIndexOf(":") + 1);
            if (cont.equalsIgnoreCase(container) && rowId.toLowerCase().startsWith(prefix)) {
                rows.add(rowId);
            }
        }
        return rows.size();
    }

    @Override
    public double sumRange(DoubleCallback cb, String orientation, String start, String end, String container) throws Exception {
        if (orientation.equalsIgnoreCase("ROW")) {
            List<String> rowIds = this.getRangeRowIds(container, start, end);
            double res = 0;
            for (String rowId : rowIds) {
                this.setRowId(rowId);
                res += cb.call();
            }
            return res;
        } else if (orientation.equalsIgnoreCase("FIELD")) {
            List<String> fields = this.getRangeFields(container, start, end);
            double res = 0;
            for (String field : fields) {
                this.setField(field);
                res += cb.call();
            }
            return res;
        }

        throw new IllegalStateException();// never shoul happen
    }

    @Override
    public void eachRow(String ind, ObjectCallback cb, String templ, String container, String dt) throws Exception {
        List<String> rowIds = this.getRowIds(container, templ);
        for (String rowId : rowIds) {
            this.setRowId(rowId);
            set(ind, cb.call(), dt);
        }
    }

    @Override
    public List<String> getRowIds(String container, String templ) {
        String prefix = templ.substring(0, templ.toLowerCase().lastIndexOf(".n") + 1).toLowerCase();
        List<String> res = new ArrayList<String>();
        for (String key : kvMap.keySet()) {
            String cont = key.substring(0, key.indexOf("*"));
            String rowId = key.substring(key.lastIndexOf(":") + 1);
            if (cont.equalsIgnoreCase(container) && rowId.toLowerCase().startsWith(prefix)) {
                if (!containsCaseInsensitive(rowId, res))
                    res.add(rowId);
            }
        }
        return res;
    }

    public boolean containsCaseInsensitive(String s, List<String> l) {
        for (String string : l) {
            if (string.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public int indexOfCaseInsensitive(String s, List<String> l) {
        int i = 0;
        for (String string : l) {
            if (string.equalsIgnoreCase(s)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public String getRowId() {
        return rowId;
    }

    @Override
    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    @Override
    public String getField() {
        return field;
    }

    @Override
    public void setField(String field) {
        this.field = field;
    }

    @Override
    public void setFields(Map<String, List<String>> fieldMap) {
        this.fieldMap = fieldMap;
    }

    @Override
    public List<String> getRangeRowIds(String container, String start, String end) {
        List<String> rowIds = new ArrayList<String>();
        String templ = null, s1, s2;
        if (start.contains(".")) {
            templ = start.substring(0, start.lastIndexOf(".") + 1).toLowerCase();
            s1 = start.substring(start.lastIndexOf(".") + 1);
            s2 = end.substring(end.lastIndexOf(".") + 1);
        } else {
            s1 = start;
            s2 = end;
        }
        int n1 = Integer.parseInt(s1);
        int n2 = Integer.parseInt(s2);
        for (String key : kvMap.keySet()) {
            String cont = key.substring(0, key.indexOf("*"));
            if (!cont.equalsIgnoreCase(container)) continue;
            String rowId = key.substring(key.lastIndexOf(':') + 1);
            String s;
            if (templ != null) {
                if (!rowId.toLowerCase().startsWith(templ)) continue;
                s = rowId.substring(rowId.lastIndexOf(".") + 1);
            } else {
                s = rowId;
            }
            int n;
            try {
                n = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                continue;
            }
            if (n >= n1 && n <= n2 && rowIds.indexOf(rowId) == -1)
                rowIds.add(rowId);
        }
        return rowIds;
    }

    @Override
    public List<String> getRangeFields(String container, String start, String end) {
        List<String> fields = this.fieldMap.get(container);
        if (fields == null) {
            throw new IllegalStateException(); //should never happen
        }
        int i1 = indexOfCaseInsensitive(start, fields);
        int i2 = indexOfCaseInsensitive(end, fields);
        List<String> arr = new ArrayList<String>();
        if (i1 == -1 || i2 == -1) {
            throw new IllegalStateException(); // never should happen
        }
        for (int i = i1; i <= i2; i++) {
            arr.add(fields.get(i));
        }
        return arr;
    }

    private String getIndicator(String ind) {
        String rowId = ind.substring(ind.lastIndexOf(":") + 1);
        String field = ind.substring(ind.indexOf("*") + 1, ind.indexOf(":"));
        if (rowId.equalsIgnoreCase("$dr")) {
            ind = ind.substring(0, ind.lastIndexOf(":") + 1) + this.rowId;
        }
        if (field.equalsIgnoreCase("$dc"))
            ind = ind.substring(0, ind.indexOf("*") + 1) + this.field + ind.substring(ind.indexOf(":"));

        return ind;
    }
}
