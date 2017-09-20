package entities;

import java.io.Serializable;

public class PermissionRespForm implements Serializable {
    private String formCode;
    private String permissionName;
    private String idn;

    public PermissionRespForm() {
    }

    public PermissionRespForm(String formCode, String permissionName, String idn) {
        this.formCode = formCode;
        this.permissionName = permissionName;
        this.idn = idn;
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

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PermissionRespForm that = (PermissionRespForm) o;

        if (!formCode.equals(that.formCode)) return false;
        if (!permissionName.equals(that.permissionName)) return false;
        return idn.equals(that.idn);
    }

    @Override
    public int hashCode() {
        int result = formCode.hashCode();
        result = 31 * result + permissionName.hashCode();
        result = 31 * result + idn.hashCode();
        return result;
    }
}
