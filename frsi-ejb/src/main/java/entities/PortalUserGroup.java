package entities;


import java.io.Serializable;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.HashSet;
import java.util.Set;

public class PortalUserGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userGroupId;
    private String name;
    private String description;
    private Long roleId;
    private Long refDepartmentRecId;
    private Long refSubjectTypeRecId;
    private Set<Long> userIds = new HashSet<Long>();
    private Long refRespondentRecId;

    public Struct toStruct(Connection connection) throws SQLException {
        Clob desc = connection.createClob();
        desc.setString(1, description);

        final Object[] attributes = new Object[4];
        attributes[0] = id;
        attributes[1] = userGroupId;
        attributes[2] = name;
        attributes[3] = desc;
        return connection.createStruct("GROUP_ROW", attributes);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(Long userGroupId) {
        this.userGroupId = userGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(Set<Long> userIds) {
        this.userIds = userIds;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getRefDepartmentRecId() {
        return refDepartmentRecId;
    }

    public void setRefDepartmentRecId(Long refDepartmentRecId) {
        this.refDepartmentRecId = refDepartmentRecId;
    }

    public Long getRefSubjectTypeRecId() {
        return refSubjectTypeRecId;
    }

    public void setRefSubjectTypeRecId(Long refSubjectTypeRecId) {
        this.refSubjectTypeRecId = refSubjectTypeRecId;
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

        PortalUserGroup that = (PortalUserGroup) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (userGroupId != null ? !userGroupId.equals(that.userGroupId) : that.userGroupId != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (roleId != null ? !roleId.equals(that.roleId) : that.roleId != null) return false;
        if (refDepartmentRecId != null ? !refDepartmentRecId.equals(that.refDepartmentRecId) : that.refDepartmentRecId != null)
            return false;
        if (refSubjectTypeRecId != null ? !refSubjectTypeRecId.equals(that.refSubjectTypeRecId) : that.refSubjectTypeRecId != null)
            return false;
        return userIds != null ? userIds.equals(that.userIds) : that.userIds == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (userGroupId != null ? userGroupId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (roleId != null ? roleId.hashCode() : 0);
        result = 31 * result + (refDepartmentRecId != null ? refDepartmentRecId.hashCode() : 0);
        result = 31 * result + (refSubjectTypeRecId != null ? refSubjectTypeRecId.hashCode() : 0);
        result = 31 * result + (userIds != null ? userIds.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PortalUserGroup{" +
                "id=" + id +
                ", userGroupId=" + userGroupId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", userIds=" + userIds +
                '}';
    }
}
