package entities;

import java.io.Serializable;

public class PermissionSubjectType implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private RefSubjectTypeItem subjectType;
    private boolean active;
    private boolean inhActive; // унаследован от группы (для пользователя)
    private boolean forGroup;

    public PermissionSubjectType() {
    }

    public PermissionSubjectType(RefSubjectTypeItem subjectType) {
        this.subjectType = subjectType;
    }

    public RefSubjectTypeItem getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(RefSubjectTypeItem subjectType) {
        this.subjectType = subjectType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
