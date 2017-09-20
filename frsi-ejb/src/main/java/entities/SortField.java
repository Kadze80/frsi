package entities;

import java.io.Serializable;

/**
 * Created by Nuriddin.Baideuov on 04.09.2015.
 */
public class SortField implements Serializable {
    public String name;
    public Boolean desc;
    public String refName; //название справочника, если сортируется значение связонное со справочником
    public String captionField;//название поля для отображения справочника

    public SortField() {
    }

    public SortField(String name, Boolean desc) {
        this.name = name;
        this.desc = desc;
    }

    public SortField(String name, Boolean desc, String refName, String captionField) {
        this.name = name;
        this.desc = desc;
        this.refName = refName;
        this.captionField = captionField;
    }
}
