package entities;

/**
 * Created by Ayupov.Bakhtiyar on 06.05.2015.
 */
public class RefReportsRulesItem extends AbstractReference {

    private String formname;
    private String fieldname;
    private String formula;
    private Double coeff;
    private String condition;
    private Integer priority;
    private Long reportType;
    private String reportTypeName;
    private Long reportKind;
    private String reportKindName;
    private Long repPerDurMonths;
    private String durName;
    private String durCode;
    private String keyValue;
    private String tableName;

    private boolean grouping;
    private String type; // DYNAMIC|STATIC
    private boolean nullable;
    private String dataType;



    public String getFormname() {
        return formname;
    }

    public void setFormname(String formname) {
        this.formname = formname;
    }

    public String getFieldname() {
        return fieldname;
    }

    public void setFieldname(String fieldname) {
        this.fieldname = fieldname;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public Double getCoeff() {
        return coeff;
    }

    public void setCoeff(Double coeff) {
        this.coeff = coeff;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Long getReportType() {
        return reportType;
    }

    public void setReportType(Long reportType) {
        this.reportType = reportType;
    }

    public String getReportTypeName() {
        return reportTypeName;
    }

    public void setReportTypeName(String reportTypeName) {
        this.reportTypeName = reportTypeName;
    }

    public Long getReportKind() {
        return reportKind;
    }

    public void setReportKind(Long reportKind) {
        this.reportKind = reportKind;
    }

    public String getReportKindName() {
        return reportKindName;
    }

    public void setReportKindName(String reportKindName) {
        this.reportKindName = reportKindName;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public Long getRepPerDurMonths() {
        return repPerDurMonths;
    }

    public void setRepPerDurMonths(Long repPerDurMonths) {
        this.repPerDurMonths = repPerDurMonths;
    }

    public String getDurName() {
        return durName;
    }

    public void setDurName(String durName) {
        this.durName = durName;
    }

    public String getDurCode() {
        return durCode;
    }

    public void setDurCode(String durCode) {
        this.durCode = durCode;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isGrouping() {
        return grouping;
    }

    public void setGrouping(boolean grouping) {
        this.grouping = grouping;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
