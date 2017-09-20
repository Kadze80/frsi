package entities;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;

/**
 * Created by Nuriddin.Baideuov on 30.03.2015.
 */
public class PermissionForm implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long itemId;
    private String formCode;
    private String formName;
    private String permissionName;
    private boolean isActive;
    private boolean forGroup;
    private boolean inhActive;
    private boolean initActive;
    private Long refRespondentRecId;
    private String idn;

    public Struct toStruct(Connection connection) throws SQLException {
        final Object[] attributes = new Object[10];
        attributes[0] = id;
        attributes[1] = permissionName;
        attributes[2] = formCode;
        attributes[3] = formName;
        attributes[4] = refRespondentRecId;
        attributes[5] = idn;
        attributes[6] = isActive ? 1 : 0;
        attributes[7] = initActive ? 1 : 0;
        attributes[8] = inhActive ? 1 : 0;
        attributes[9] = forGroup ? 1 : 0;
        return connection.createStruct("PERMISSION_FORM_ROW", attributes);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isForGroup() {
        return forGroup;
    }

    public void setForGroup(boolean forGroup) {
        this.forGroup = forGroup;
    }

    public boolean isInhActive() {
        return inhActive;
    }

    public void setInhActive(boolean inhActive) {
        this.inhActive = inhActive;
    }

    public boolean isInitActive() {
        return initActive;
    }

    public void setInitActive(boolean initActive) {
        this.initActive = initActive;
    }

    public Long getRefRespondentRecId() {
        return refRespondentRecId;
    }

    public void setRefRespondentRecId(Long refRespondentRecId) {
        this.refRespondentRecId = refRespondentRecId;
    }

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    @Override
    public String toString() {
        return "PermissionForm{" +
                "id=" + id +
                ", itemId=" + itemId +
                ", formCode='" + formCode + '\'' +
                ", formName='" + formName + '\'' +
                ", permissionName='" + permissionName + '\'' +
                ", isActive=" + isActive +
                ", forGroup=" + forGroup +
                ", inhActive=" + inhActive +
                ", initActive=" + initActive +
                ", refRespondentRecId=" + refRespondentRecId +
                '}';
    }
}
