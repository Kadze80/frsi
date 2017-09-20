package parser;

import entities.ValueType;
import entities.Variant;
import jaxb.Data;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import util.Convert;

import java.text.MessageFormat;
import java.util.Date;

/**
 * Created by nuriddin on 9/1/16.
 */
public class DataType {

    public static final int NONE = 0;
    public static final int LONG = 1;
    public static final int DOUBLE = 2;
    public static final int DATE = 3;
    public static final int STRING = 4;
    public static final int BOOLEAN = 5;

    private static final String[] dataTypeNames = new String[]{"number", "string", "date", "boolean"};

    /**
     * Получить тип по его имени
     */
    public static int nameToDataType(String name) {
        String n = name.toLowerCase();
        if (n.startsWith("number")) {
            if (n.equals("number")) n += "0";

            String sfx = n.substring("number".length());
            int i;
            try {
                i = Integer.parseInt(sfx);
            } catch (NumberFormatException e) {
                return NONE;
            }
            if (i == 0)
                return LONG;
            else
                return DOUBLE;
        } else if (n.equals("string"))
            return STRING;
        else if (n.equals("date"))
            return DATE;
        else if (n.equals("boolean"))
            return BOOLEAN;
        else
            return NONE;
    }

    public static int shortNameToDataType(String shortName) {
        String name = null;
        for (String n : dataTypeNames) {
            if (shortName.charAt(0) == n.charAt(0)) {
                name = n;
                if (shortName.length() > 1)
                    name += shortName.substring(1);
                break;
            }
        }
        if (name == null)
            throw new IllegalStateException();
        return nameToDataType(name);
    }

    public static Long toLong(Object value) throws NumberFormatException {
        if (value == null)
            return null;
        if (value instanceof Long)
            return (Long) value;
        if (value instanceof Integer)
            return ((Integer) value).longValue();
        if (value instanceof Double)
            return Math.round((Double) value);
        if (value instanceof Float)
            return ((Float) value).longValue();

        throw new NumberFormatException();
    }

    public static Double toDouble(Object value) throws NumberFormatException {
        if (value == null)
            return null;
        if (value instanceof Double)
            return (Double) value;
        if (value instanceof Float)
            return ((Float) value).doubleValue();
        if (value instanceof Integer)
            return ((Integer) value).doubleValue();
        if (value instanceof Long)
            return ((Long) value).doubleValue();

        throw new NumberFormatException();
    }

    public static String toString(Object value) throws NumberFormatException {
        if (value == null)
            return null;
        if (value instanceof String)
            return (String) value;

        throw new NumberFormatException();
    }

    public static Date toDate(Object value) throws NumberFormatException {
        if (value == null)
            return null;
        if (value instanceof Date)
            return (Date) value;

        throw new NumberFormatException();
    }

    public static Boolean toBoolean(Object value) throws NumberFormatException {
        if (value == null)
            return null;
        if (value instanceof Boolean)
            return (Boolean) value;
        if (value instanceof Integer) {
            int i = (Integer) value;
            if (i == 0)
                return false;
            if (i == 1)
                return true;
        }
        if (value instanceof Long) {
            long i = (Long) value;
            if (i == 0)
                return false;
            if (i == 1)
                return true;
        }
        if (value instanceof String) {
            String s = (String) value;
            if (s.equalsIgnoreCase("true"))
                return true;
            if (s.equalsIgnoreCase("false"))
                return false;
        }
        throw new NumberFormatException();
    }

    public static String variantToString(Variant value, ValueType vt) throws Exception {
        switch (vt) {
            case STRING:
                if (value == null)
                    return "";
                else
                    return value.getStrValue() == null ? "" : value.getStrValue();
            case BOOLEAN:
                if (value == null)
                    return "FALSE";
                else
                    return value.getBoolValue() ? "TRUE" : "FALSE";
            case DATE:
                if (value == null)
                    return "";
                else
                    return value.getDateValue() == null ? "" : Convert.dateFormatRus.format(value.getDateValue());
            case NUMBER_0:
                if (value == null)
                    return "0";
                else
                    return Convert.getNumWithMaskFromStr(String.valueOf(value.getLngValue()), "n0");
            case NUMBER_1:
                if (value == null)
                    return "0.0";
                else
                    return Convert.getNumWithMaskFromStr(String.valueOf(value.getDblValue()), "n1");
            case NUMBER_2:
                if (value == null)
                    return "0.00";
                else
                    return Convert.getNumWithMaskFromStr(String.valueOf(value.getDblValue()), "n2");
            case NUMBER_3:
                if (value == null)
                    return "0.000";
                else
                    return Convert.getNumWithMaskFromStr(String.valueOf(value.getDblValue()), "n3");
            case NUMBER_4:
                if (value == null)
                    return "0.0000";
                else
                    return Convert.getNumWithMaskFromStr(String.valueOf(value.getDblValue()), "n4");
            case NUMBER_5:
                if (value == null)
                    return "0.00000";
                else
                    return Convert.getNumWithMaskFromStr(String.valueOf(value.getDblValue()), "n5");
            case NUMBER_6:
                if (value == null)
                    return "0.000000";
                else
                    return Convert.getNumWithMaskFromStr(String.valueOf(value.getDblValue()), "n6");
            case NUMBER_7:
                if (value == null)
                    return "0.0000000";
                else
                    return Convert.getNumWithMaskFromStr(String.valueOf(value.getDblValue()), "n7");
            case NUMBER_8:
                if (value == null)
                    return "0.00000000";
                else
                    return Convert.getNumWithMaskFromStr(String.valueOf(value.getDblValue()), "n8");
            default:
                throw new IllegalStateException(MessageFormat.format("Unknown ValueType {0}", value.getValueType().name()));
        }
    }

    public static Variant stringToVariant(String value, ValueType vt) {
        switch (vt) {
            case STRING:
                return Variant.createString(value == null ? "" : value);
            case BOOLEAN:
                return Variant.createBoolean(Boolean.valueOf(value));
            case DATE:
                if (value == null)
                    return Variant.createDate(null);
                else {
                    LocalDate ldate = LocalDate.parse(value, DateTimeFormat.forPattern("dd.MM.yyyy"));
                    return Variant.createDate(ldate.toDate());
                }
            case NUMBER_0:
                if (value == null)
                    return Variant.createNumber0(0);
                else
                    return Variant.createNumber0(Long.valueOf(value));
            case NUMBER_1:
                if (value == null)
                    return Variant.createNumber1(0);
                else
                    return Variant.createNumber1(Double.valueOf(value));
            case NUMBER_2:
                if (value == null)
                    return Variant.createNumber2(0);
                else
                    return Variant.createNumber2(Double.valueOf(value));
            case NUMBER_3:
                if (value == null)
                    return Variant.createNumber3(0);
                else
                    return Variant.createNumber3(Double.valueOf(value));
            case NUMBER_4:
                if (value == null)
                    return Variant.createNumber4(0);
                else
                    return Variant.createNumber4(Double.valueOf(value));
            case NUMBER_5:
                if (value == null)
                    return Variant.createNumber5(0);
                else
                    return Variant.createNumber5(Double.valueOf(value));
            case NUMBER_6:
                if (value == null)
                    return Variant.createNumber6(0);
                else
                    return Variant.createNumber6(Double.valueOf(value));
            case NUMBER_7:
                if (value == null)
                    return Variant.createNumber7(0);
                else
                    return Variant.createNumber7(Double.valueOf(value));
            case NUMBER_8:
                if (value == null)
                    return Variant.createNumber8(0);
                else
                    return Variant.createNumber8(Double.valueOf(value));
            default:
                throw new IllegalStateException(MessageFormat.format("Unknown ValueType {0}", vt.name()));
        }
    }

    public static ValueType toValueType(String t) {
        if (t.equalsIgnoreCase("s"))
            return ValueType.STRING;
        if (t.equalsIgnoreCase("d"))
            return ValueType.DATE;
        if (t.equalsIgnoreCase("b"))
            return ValueType.BOOLEAN;
        if (t.equalsIgnoreCase("n0"))
            return ValueType.NUMBER_0;
        if (t.equalsIgnoreCase("n1"))
            return ValueType.NUMBER_1;
        if (t.equalsIgnoreCase("n2"))
            return ValueType.NUMBER_2;
        if (t.equalsIgnoreCase("n3"))
            return ValueType.NUMBER_3;
        if (t.equalsIgnoreCase("n4"))
            return ValueType.NUMBER_4;
        if (t.equalsIgnoreCase("n5"))
            return ValueType.NUMBER_5;
        if (t.equalsIgnoreCase("n6"))
            return ValueType.NUMBER_6;
        if (t.equalsIgnoreCase("n7"))
            return ValueType.NUMBER_7;
        if (t.equalsIgnoreCase("n8"))
            return ValueType.NUMBER_8;

        throw new IllegalStateException();
    }

}
