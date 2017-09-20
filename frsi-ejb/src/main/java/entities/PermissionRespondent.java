package entities;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;

/**
 * Created by Nuriddin.Baideuov on 10.04.2015.
 */
public class PermissionRespondent implements Serializable{
    private static final long serialVersionUID = 1L;

    private Long id;
    private RefRespondentItem respondent;
    private boolean active;
    private boolean inhActive; // унаследован от группы (для пользователя)
    private boolean forGroup;

    public PermissionRespondent() {
    }

    public Struct toStruct(Connection connection) throws SQLException {
        final Object[] attributes = new Object[6];
        attributes[0] = id;
        attributes[1] = respondent.getRecId();
        attributes[2] = respondent.getNameRu();
        attributes[3] = active ? 1 : 0;
        attributes[4] = inhActive ? 1 : 0;
        attributes[5] = forGroup ? 1 : 0;
        return connection.createStruct("PERMISSION_RESP_ROW", attributes);
    }

    public PermissionRespondent(RefRespondentItem respondent) {
        this.respondent = respondent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RefRespondentItem getRespondent() {
        return respondent;
    }

    public void setRespondent(RefRespondentItem respondent) {
        this.respondent = respondent;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isInhActive() {
        return inhActive;
    }

    public void setInhActive(boolean inhActive) {
        this.inhActive = inhActive;
    }

    public boolean isForGroup() {
        return forGroup;
    }

    public void setForGroup(boolean forGroup) {
        this.forGroup = forGroup;
    }

    @Override
    public String toString() {
        return "PermissionRespondent{" +
                "id=" + id +
                ", respondent='" + respondent + '\'' +
                ", active=" + active +
                ", inhActive=" + inhActive +
                ", forGroup=" + forGroup +
                '}';
    }
}
