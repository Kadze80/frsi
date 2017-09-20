package entities;

import java.io.Serializable;

/**
 * Created by Baurzhan.Baisholakov on 15.05.2015.
 */
public class ExcelCell implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String keyValue;
    private String type;
    private int position;
    private String refName;
    private String refColumn;
    private String filterType;
    private Boolean multiValue;



    public ExcelCell () {
        this.filterType = "list";
    }

    public ExcelCell (ExcelCell o) {
        this.name = o.name;
        this.keyValue = o.keyValue;
        this.type = o.type;
        this.position = o.position;
        this.refName = o.refName;
        this.refColumn = o.refColumn;
        this.filterType = o.filterType;
        this.multiValue = o.multiValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

    public String getRefColumn() {
        return refColumn;
    }

    public void setRefColumn(String refColumn) {
        this.refColumn = refColumn;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public Boolean getMultiValue() {
        return multiValue;
    }

    public void setMultiValue(Boolean multiValue) {
        this.multiValue = multiValue;
    }
}
