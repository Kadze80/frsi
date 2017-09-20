package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by nuriddin on 1/17/17.
 */
public class Variant implements Serializable {
    private ValueType valueType;
    private String strValue;
    private long lngValue;
    private double dblValue;
    private Date dateValue;
    private boolean boolValue;

    public static Variant createString(String value) {
        Variant v = new Variant();
        v.setStrValue(value);
        v.setValueType(ValueType.STRING);
        return v;
    }

    public static Variant createBoolean(boolean value) {
        Variant v = new Variant();
        v.setBoolValue(value);
        v.setValueType(ValueType.BOOLEAN);
        return v;
    }

    public static Variant createDate(Date value) {
        Variant v = new Variant();
        v.setDateValue(value);
        v.setValueType(ValueType.DATE);
        return v;
    }

    public static Variant createNumber0(long value) {
        Variant v = new Variant();
        v.setLngValue(value);
        v.setValueType(ValueType.NUMBER_0);
        return v;
    }

    public static Variant createNumber1(double value) {
        Variant v = new Variant();
        v.setDblValue(value);
        v.setValueType(ValueType.NUMBER_1);
        return v;
    }

    public static Variant createNumber2(double value) {
        Variant v = new Variant();
        v.setDblValue(value);
        v.setValueType(ValueType.NUMBER_2);
        return v;
    }

    public static Variant createNumber3(double value) {
        Variant v = new Variant();
        v.setDblValue(value);
        v.setValueType(ValueType.NUMBER_3);
        return v;
    }

    public static Variant createNumber4(double value) {
        Variant v = new Variant();
        v.setDblValue(value);
        v.setValueType(ValueType.NUMBER_4);
        return v;
    }

    public static Variant createNumber5(double value) {
        Variant v = new Variant();
        v.setDblValue(value);
        v.setValueType(ValueType.NUMBER_5);
        return v;
    }

    public static Variant createNumber6(double value) {
        Variant v = new Variant();
        v.setDblValue(value);
        v.setValueType(ValueType.NUMBER_6);
        return v;
    }

    public static Variant createNumber7(double value) {
        Variant v = new Variant();
        v.setDblValue(value);
        v.setValueType(ValueType.NUMBER_7);
        return v;
    }

    public static Variant createNumber8(double value) {
        Variant v = new Variant();
        v.setDblValue(value);
        v.setValueType(ValueType.NUMBER_8);
        return v;
    }

    public boolean equal(Variant v) {
        if (valueType != v.valueType) {
            if ((valueType == ValueType.STRING) || valueType == ValueType.DATE || valueType == ValueType.BOOLEAN
                    || v.valueType == ValueType.STRING || v.valueType == ValueType.DATE || v.valueType == ValueType.BOOLEAN) {
                return false;
            }
        }
        switch (valueType) {
            case STRING:
                if (strValue == null && v.strValue == null)
                    return true;
                if (strValue == null || v.strValue == null)
                    return false;
                return strValue.equalsIgnoreCase(v.strValue);
            case DATE:
                if (dateValue == null && v.dateValue == null)
                    return true;
                if (dateValue == null || v.dateValue == null)
                    return false;
                return dateValue == v.dateValue;
            case BOOLEAN:
                return boolValue == v.boolValue;
            case NUMBER_0:
                return lngValue == v.lngValue;
            case NUMBER_1:
            case NUMBER_2:
            case NUMBER_3:
            case NUMBER_4:
            case NUMBER_5:
            case NUMBER_6:
            case NUMBER_7:
            case NUMBER_8:
                return dblValue == v.dblValue;
        }
        return false;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    public long getLngValue() {
        return lngValue;
    }

    public void setLngValue(long lngValue) {
        this.lngValue = lngValue;
    }

    public double getDblValue() {
        return dblValue;
    }

    public void setDblValue(double dblValue) {
        this.dblValue = dblValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public boolean getBoolValue() {
        return boolValue;
    }

    public void setBoolValue(boolean boolValue) {
        this.boolValue = boolValue;
    }

    @Override
    public String toString() {
        return "Variant{" +
                "valueType=" + valueType +
                ", strValue='" + strValue + '\'' +
                ", lngValue=" + lngValue +
                ", dblValue=" + dblValue +
                ", dateValue=" + dateValue +
                ", boolValue=" + boolValue +
                '}';
    }
}
