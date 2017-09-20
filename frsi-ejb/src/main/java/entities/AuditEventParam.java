package entities;

import java.io.Serializable;

public class AuditEventParam implements Serializable{
    private Long id;
    private Long auditEventId;
    private String code;
    private ValueType valueType;
    private Variant value;

    public AuditEventParam() {
    }

    public AuditEventParam(String code, ValueType valueType, Variant value) {
        this.code = code;
        this.valueType = valueType;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuditEventId() {
        return auditEventId;
    }

    public void setAuditEventId(Long auditEventId) {
        this.auditEventId = auditEventId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    @Override
    public String toString() {
        return "AuditEventParam{" +
                "id=" + id +
                ", auditEventId=" + auditEventId +
                ", code='" + code + '\'' +
                ", valueType=" + valueType +
                ", value=" + value +
                '}';
    }
}
