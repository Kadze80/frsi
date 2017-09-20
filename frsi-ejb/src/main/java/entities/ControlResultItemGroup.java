package entities;

import java.io.Serializable;
import java.util.List;

/**
 * Created by nuriddin on 8/4/16.
 */
public class ControlResultItemGroup implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title;
    private String idn;
    private List<ControlResultItem> items;

    public ControlResultItemGroup(String title, String idn, List<ControlResultItem> items) {
        this.title = title;
        this.idn = idn;
        this.items = items;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ControlResultItem> getItems() {
        return items;
    }

    public void setItems(List<ControlResultItem> items) {
        this.items = items;
    }

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }
}
