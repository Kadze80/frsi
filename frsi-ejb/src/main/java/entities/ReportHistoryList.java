package entities;

/**
 * Created by Baurzhan.Baisholakov on 07.03.2017.
 */
public class ReportHistoryList {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long reportHisoryId;
    private String key;
    private String value;
    private String valueType;
    private String ref;
    private Long multiValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReportHisoryId() {
        return reportHisoryId;
    }

    public void setReportHisoryId(Long reportHisoryId) {
        this.reportHisoryId = reportHisoryId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Long getMultiValue() {
        return multiValue;
    }

    public void setMultiValue(Long multiValue) {
        this.multiValue = multiValue;
    }
}
