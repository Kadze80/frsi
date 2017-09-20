package parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuriddin on 12/21/16.
 */
public class Helper {

    public static List<String> getRangeRowIds(String start, String end) {
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
        for (int i = n1; i <= n2; i++) {
            String s = templ != null ? templ : "";
            s += Integer.toString(i);
            rowIds.add(s);
        }
        return rowIds;
    }

    public static List<String> getRangeFields(List<String> fields, String start, String end) {
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

    private static int indexOfCaseInsensitive(String s, List<String> l) {
        int i = 0;
        for (String string : l) {
            if (string.equalsIgnoreCase(s)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static String changeIndicatorField(String ind, String field) {
        return ind.substring(0, ind.indexOf("*") + 1) + field + ind.substring(ind.indexOf(":"));
    }

    public static String changeIndicatorRow(String ind, String row) {
        return ind.substring(0, ind.lastIndexOf(":") + 1) + row + (ind.endsWith("]") ? "]" : "");
    }
}
