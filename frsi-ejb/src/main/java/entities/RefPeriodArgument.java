package entities;

import util.Convert;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;

public class RefPeriodArgument implements Serializable {

    private Long refPeriodId;
    private String name;
    private ValueType valueType;
    private Variant value;
    private String strValue;

    public static String[] noArgArray = {"reportDate", "idn", "formCode", "backendDate", "result"};

    public static Boolean equalsArg(String argName){
        for(String noArgs : noArgArray){
            if(argName.equalsIgnoreCase(noArgs))
                return true;
        }
        return false;
    }

    public static String getColumn(ValueType vt){
        switch (vt){
            case STRING:
                return "string_value";
            case BOOLEAN:
                return "boolean_value";
            case DATE:
                return "date_value";
            case NUMBER_0:
                return "integer_value";
            case NUMBER_1:
                return "real_value";
            case NUMBER_2:
                return "real_value";
            case NUMBER_3:
                return "real_value";
            case NUMBER_4:
                return "real_value";
            case NUMBER_5:
                return "real_value";
            case NUMBER_6:
                return "real_value";
            case NUMBER_7:
                return "real_value";
            case NUMBER_8:
                return "real_value";
            default:
                throw new IllegalStateException(MessageFormat.format("Unknown ValueType {0}", vt.name()));
        }
    }

    public static String getValue(ValueType vt, String strValue){
        switch (vt){
            case STRING:
                return strValue;
            case BOOLEAN:
                return strValue.trim().equalsIgnoreCase("true") ? "1" : "0";
            case DATE:
                 return strValue;
            case NUMBER_0:
                return strValue;
            case NUMBER_1:
                return strValue;
            case NUMBER_2:
                return strValue;
            case NUMBER_3:
                return strValue;
            case NUMBER_4:
                return strValue;
            case NUMBER_5:
                return strValue;
            case NUMBER_6:
                return strValue;
            case NUMBER_7:
                return strValue;
            case NUMBER_8:
                return strValue;
            default:
                throw new IllegalStateException(MessageFormat.format("Unknown ValueType {0}", vt.name()));
        }
    }

    // region Getter and Setter
    public Long getRefPeriodId() {
        return refPeriodId;
    }

    public void setRefPeriodId(Long refPeriodId) {
        this.refPeriodId = refPeriodId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public Variant getValue() {
        return value;
    }

    public void setValue(Variant value) {
        this.value = value;
    }

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    // endregion
}
