package parser;

import entities.OutReportRuleItem;
import jaxb.Data;
import util.Convert;

import java.text.MessageFormat;
import java.util.Date;

/**
 * Created by nuriddin on 9/13/16.
 */
public class Cell implements Comparable<Cell> {
    String key;
    String fieldName;
    String value;
    String strValue;
    double dblValue;
    long lngValue;
    Date dteValue;
    int dataType;
    boolean dynamicRow;
    boolean dynamicCell;

    public Cell(OutReportRuleItem i, Object data, Date contextDate, String keyField) {
        key = i.getFormCode() + "_" + i.getTableName() + "*";
        if (i.getFieldName().toLowerCase().endsWith("@dynamiccellid")) {
            fieldName = i.getFieldName().substring(0, i.getFieldName().indexOf("@") + 1) + Convert.dateFormatCompact.format(contextDate);
            dynamicCell = true;
        } else
            fieldName = i.getFieldName();
        key += fieldName;
        key += ":" + keyField + ":";
        key += i.getKeyValue();
        dynamicRow = i.getKeyValue().toLowerCase().startsWith("$d.");

        dataType = DataType.shortNameToDataType(i.getDataType());
        value = "";
        if (data == null) {
            value = null;
        } else {
            switch (dataType) {
                case DataType.DOUBLE:
                    dblValue = DataType.toDouble(data);
                    if (isNaN(dblValue)) {
                        value = "NaN";
                        break;
                    }
                    try {
                        value = Convert.getNumWithMaskFromStr(String.valueOf(dblValue), i.getDataType());
                    } catch (Exception e) {
                        throw new IllegalStateException(e.getMessage());
                    }
                    break;
                case DataType.LONG:
                    lngValue = DataType.toLong(data);
                    if (data instanceof Double) {
                        if (isNaN((Double) data)) {
                            value = "NaN";
                            break;
                        }
                    } else if (isNaN(lngValue)) {
                        value = "NaN";
                        break;
                    }
                    try {
                        value = Convert.getNumWithMaskFromStr(String.valueOf(lngValue), i.getDataType());
                    } catch (Exception e) {
                        throw new IllegalStateException(e.getMessage());
                    }
                    break;
                case DataType.DATE:
                    dteValue = DataType.toDate(data);
                    value = Convert.getDateStringFromDate(dteValue);
                    break;
                case DataType.STRING:
                    strValue = value = DataType.toString(data);
                    break;
            }
        }
    }

    private boolean isNaN(double value) {
        return Double.isNaN(value) || Double.isInfinite(value);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    public double getDblValue() {
        return dblValue;
    }

    public void setDblValue(double dblValue) {
        this.dblValue = dblValue;
    }

    public long getLngValue() {
        return lngValue;
    }

    public void setLngValue(long lngValue) {
        this.lngValue = lngValue;
    }

    public Date getDteValue() {
        return dteValue;
    }

    public void setDteValue(Date dteValue) {
        this.dteValue = dteValue;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public boolean isDynamicRow() {
        return dynamicRow;
    }

    public void setDynamicRow(boolean dynamicRow) {
        this.dynamicRow = dynamicRow;
    }

    public boolean isDynamicCell() {
        return dynamicCell;
    }

    public void setDynamicCell(boolean dynamicCell) {
        this.dynamicCell = dynamicCell;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "key='" + key + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", value='" + value + '\'' +
                ", strValue='" + strValue + '\'' +
                ", dblValue=" + dblValue +
                ", lngValue=" + lngValue +
                ", dteValue=" + dteValue +
                '}';
    }

    @Override
    public int compareTo(Cell c) {
        if (!this.fieldName.equalsIgnoreCase(c.fieldName))
            throw new IllegalStateException("Нельзя сравнить значения разных полей");
        switch (dataType) {
            case DataType.DOUBLE:
                if (this.dblValue > c.dblValue)
                    return 1;
                else if (this.dblValue < c.dblValue)
                    return -1;
                break;
            case DataType.LONG:
                if (this.lngValue > c.lngValue)
                    return 1;
                else if (this.lngValue < c.lngValue)
                    return -1;
                break;
            case DataType.DATE:
                if (dteValue == null)
                    return -1;
                else if (c.dteValue == null)
                    return 1;
                return dteValue.compareTo(c.dteValue);
            case DataType.STRING:
                if (strValue == null)
                    return -1;
                else if (c.strValue == null)
                    return 1;
                return strValue.compareTo(c.strValue);
        }
        return 0;
    }
}
