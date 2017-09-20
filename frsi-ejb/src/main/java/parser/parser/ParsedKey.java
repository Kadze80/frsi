package parser.parser;

import java.util.*;

/**
 * Created by nuriddin on 9/4/16.
 */
public class ParsedKey extends ParsedItem{
    private int id;
    private String key;
    private String formCode;
    private String fieldName;
    private String rowId;
    private String container;
    private int dataType;
    private boolean required;
    private String datePart;
    private boolean dynamicRow;
    private Map<Date, Date> dates = new HashMap<Date, Date>();
    private boolean meta; //метаданные (rec_id респондента)

    private boolean rowRange;
    private boolean fieldRange;
    private String startRange;
    private String endRange;

    public ParsedKey(int id, String originText, int startIndex, int endIndex) {
        super(originText, startIndex, endIndex);
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isDynamicRow() {
        return dynamicRow;
    }

    public void setDynamicRow(boolean dynamicRow) {
        this.dynamicRow = dynamicRow;
    }

    public Map<Date, Date> getDates() {
        return dates;
    }

    public void setDates(Map<Date, Date> dates) {
        this.dates = dates;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public boolean isMeta() {
        return meta;
    }

    public void setMeta(boolean meta) {
        this.meta = meta;
    }

    public int getId() {
        return id;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getDatePart() {
        return datePart;
    }

    public void setDatePart(String datePart) {
        this.datePart = datePart;
    }

    public boolean isRowRange() {
        return rowRange;
    }

    public void setRowRange(boolean rowRange) {
        this.rowRange = rowRange;
    }

    public boolean isFieldRange() {
        return fieldRange;
    }

    public void setFieldRange(boolean fieldRange) {
        this.fieldRange = fieldRange;
    }

    public String getStartRange() {
        return startRange;
    }

    public void setStartRange(String startRange) {
        this.startRange = startRange;
    }

    public String getEndRange() {
        return endRange;
    }

    public void setEndRange(String endRange) {
        this.endRange = endRange;
    }

    @Override
    public String toString() {
        return "ParsedKey{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", formCode='" + formCode + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", rowId='" + rowId + '\'' +
                ", container='" + container + '\'' +
                ", dataType=" + dataType +
                ", required=" + required +
                ", datePart='" + datePart + '\'' +
                ", dynamicRow=" + dynamicRow +
                ", dates=" + dates +
                ", meta=" + meta +
                ", rowRange=" + rowRange +
                ", fieldRange=" + fieldRange +
                ", startRange='" + startRange + '\'' +
                ", endRange='" + endRange + '\'' +
                '}';
    }
}
