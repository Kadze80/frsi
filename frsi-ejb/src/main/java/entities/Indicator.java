package entities;

/**
 * Created by nuriddin on 10/10/16.
 */
public class Indicator {
    private String origin;
    private String indicator;
    private String formCode;
    private String container;
    private String fieldName;
    private String keyFieldName;
    private String rowId;
    private String dateOffset;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getIndicator() {
        return indicator;
    }

    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getKeyFieldName() {
        return keyFieldName;
    }

    public void setKeyFieldName(String keyFieldName) {
        this.keyFieldName = keyFieldName;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public String getDateOffset() {
        return dateOffset;
    }

    public void setDateOffset(String dateOffset) {
        this.dateOffset = dateOffset;
    }

    @Override
    public String toString() {
        return "Indicator{" +
                "origin='" + origin + '\'' +
                ", formCode='" + formCode + '\'' +
                ", container='" + container + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", keyFieldName='" + keyFieldName + '\'' +
                ", rowId='" + rowId + '\'' +
                ", dateOffset='" + dateOffset + '\'' +
                '}';
    }
}
