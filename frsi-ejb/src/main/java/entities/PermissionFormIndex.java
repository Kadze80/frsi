package entities;

import java.io.Serializable;

public class PermissionFormIndex implements Serializable {
    private String formCode;
    private String permissionName;
    private Long refRespondentRecId;

    public PermissionFormIndex() {
    }

    public PermissionFormIndex(String formCode, String permissionName, Long refRespondentRecId) {
        this.formCode = formCode;
        this.permissionName = permissionName;
        this.refRespondentRecId = refRespondentRecId;
    }

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public Long getRefRespondentRecId() {
        return refRespondentRecId;
    }

    public void setRefRespondentRecId(Long refRespondentRecId) {
        this.refRespondentRecId = refRespondentRecId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PermissionFormIndex that = (PermissionFormIndex) o;

        if (!formCode.equals(that.formCode)) return false;
        if (!permissionName.equals(that.permissionName)) return false;
        return refRespondentRecId.equals(that.refRespondentRecId);
    }

    @Override
    public int hashCode() {
        int result = formCode.hashCode();
        result = 31 * result + permissionName.hashCode();
        result = 31 * result + refRespondentRecId.hashCode();
        return result;
    }
}
