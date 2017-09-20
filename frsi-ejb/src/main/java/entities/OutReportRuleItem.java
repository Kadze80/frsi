package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Nuriddin.Baideuov on 06.05.2015.
 */
public class OutReportRuleItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String formCode;
    private String tableName;
    private String fieldName;
    private String formula;
    private Integer priority;
    private String keyValue;
    private boolean grouping;
    private String dataType;

    public OutReportRuleItem() {
    }

    public OutReportRuleItem(OutReportRuleItem item) {
        id = item.id;
        formCode = item.formCode;
        tableName = item.tableName;
        fieldName = item.fieldName;
        formula = item.formula;
        priority = item.priority;
        keyValue = item.keyValue;
        grouping = item.grouping;
        dataType = item.dataType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public boolean isGrouping() {
        return grouping;
    }

    public void setGrouping(boolean grouping) {
        this.grouping = grouping;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
