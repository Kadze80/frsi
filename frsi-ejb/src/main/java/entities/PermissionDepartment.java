package entities;

import java.io.Serializable;

/**
 * Created by Baurzhan.Baisholakov on 26.09.2016.
 */
public class PermissionDepartment implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private RefDepartmentItem department;
    private boolean active;
    private boolean inhActive; // унаследован от группы (для пользователя)
    private boolean forGroup;

    public PermissionDepartment() {
    }

    public PermissionDepartment(RefDepartmentItem department) {
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RefDepartmentItem getDepartment() {
        return department;
    }

    public void setDepartment(RefDepartmentItem department) {
        this.department = department;
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
}
