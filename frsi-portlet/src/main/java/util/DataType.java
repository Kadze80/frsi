package util;

import entities.ValueType;
import entities.Variant;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.text.MessageFormat;

public class DataType {
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
