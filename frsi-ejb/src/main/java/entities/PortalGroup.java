package entities;

import java.io.Serializable;

/**
 * Created by Baurzhan.Baisholakov on 24.09.2016.
 */
public class PortalGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    private long groupId;
    private String name;

    public PortalGroup() {
    }

    public PortalGroup(long groupId, String name) {
        this.groupId = groupId;
        this.name = name;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
