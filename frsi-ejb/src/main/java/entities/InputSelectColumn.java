package entities;

import java.io.Serializable;

/**
 * Created by nuriddin on 6/1/16.
 */
public class InputSelectColumn implements Serializable {
    private String name;
    private String title;
    private boolean key;
    private boolean hidden;
    private String targetColumnName;
    private ValueType valueType;

    public InputSelectColumn() {
    }

    public InputSelectColumn(String name, ValueType valueType) {
        this(name, name, valueType);
    }

    public InputSelectColumn(String name, String title, ValueType valueType) {
        this(name, title, false, false, null, valueType);
    }

    public InputSelectColumn(String name, String title, boolean key, boolean hidden, String targetColumnName, ValueType valueType) {
        this.name = name;
        this.title = title;
        this.key = key;
        this.hidden = hidden;
        this.targetColumnName = targetColumnName;
        this.valueType = valueType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isKey() {
        return key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }

    public String getTargetColumnName() {
        return targetColumnName;
    }

    public void setTargetColumnName(String targetColumnName) {
        this.targetColumnName = targetColumnName;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }
}
