package entities;

import java.io.Serializable;

public class SignInfo implements Serializable {
    private RefPostItem refPostItem;
    private String info;

    public SignInfo(RefPostItem refPostItem, String info) {
        this.refPostItem = refPostItem;
        this.info = info;
    }

    public RefPostItem getRefPostItem() {
        return refPostItem;
    }

    public String getInfo() {
        return info;
    }
}
