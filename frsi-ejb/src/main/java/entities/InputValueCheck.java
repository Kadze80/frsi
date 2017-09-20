package entities;

import java.io.Serializable;

/**
 * Created by Baurzhan.Baisholakov on 31.05.2015.
 */
public class InputValueCheck implements Serializable {
    private static final long serialVersionUID = 1L;

    private String inputType;
    private String key;
    private String valueType;
    private Boolean required;
    private Boolean readonly;
    private String valueCheckFunc;
    private String ref;
    private String refCode;
    private String refCaption;
    private Boolean multiValue;
    private String mask;
    private Boolean auto;
    private String groupId;
    private Boolean unique;
    private String uniqueArea; /*report - отчет; table - таблица; column - поле  */
    private String valueFunc;
    private String defaultValue;
    private String filterField; //название поля по которой фильтруется (используется для legal_person)
    private String filterValue; //Значение фильтра (используется для legal_person)
    private String filterDataType; //Тип значения фильтра (используется для legal_person)

    public InputValueCheck() {
        required = false;
    }

    public InputValueCheck(InputValueCheck inputValueCheck) {
        this.inputType = inputValueCheck.getInputType();
        this.key = inputValueCheck.getKey();
        this.valueType = inputValueCheck.getValueType();
        this.required = inputValueCheck.getRequired();
        this.readonly = inputValueCheck.getReadonly();
        this.valueCheckFunc = inputValueCheck.getValueCheckFunc();
        this.ref = inputValueCheck.getRef();
        this.refCode = inputValueCheck.getRefCode();
        this.refCaption = inputValueCheck.getRefCaption();
        this.multiValue = inputValueCheck.getMultiValue();
        this.mask = inputValueCheck.getMask();
        this.auto = inputValueCheck.getAuto();
        this.groupId = inputValueCheck.getGroupId();
        this.unique = inputValueCheck.getUnique();
        this.uniqueArea = inputValueCheck.getUniqueArea();
        this.valueFunc = inputValueCheck.getValueFunc();
        this.defaultValue = inputValueCheck.getDefaultValue();
        this.filterField = inputValueCheck.getFilterField();
        this.filterDataType = inputValueCheck.getFilterDataType();
        this.filterValue = inputValueCheck.getFilterValue();
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    public String getValueCheckFunc() {
        return valueCheckFunc;
    }

    public void setValueCheckFunc(String valueCheckFunc) {
        this.valueCheckFunc = valueCheckFunc;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getRefCode() {
        return refCode;
    }

    public void setRefCode(String refCode) {
        this.refCode = refCode;
    }

    public String getRefCaption() {
        return refCaption;
    }

    public void setRefCaption(String refCaption) {
        this.refCaption = refCaption;
    }

    public Boolean getMultiValue() {
        return multiValue;
    }

    public void setMultiValue(Boolean multiValue) {
        this.multiValue = multiValue;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public Boolean getAuto() {
        return auto;
    }

    public void setAuto(Boolean auto) {
        this.auto = auto;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Boolean getUnique() {
        return unique;
    }

    public void setUnique(Boolean unique) {
        this.unique = unique;
    }

    public String getUniqueArea() {
        return uniqueArea;
    }

    public void setUniqueArea(String uniqueArea) {
        this.uniqueArea = uniqueArea;
    }

    public String getValueFunc() {
        return valueFunc;
    }

    public void setValueFunc(String valueFunc) {
        this.valueFunc = valueFunc;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFilterField() {
        return filterField;
    }

    public void setFilterField(String filterField) {
        this.filterField = filterField;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public String getFilterDataType() {
        return filterDataType;
    }

    public void setFilterDataType(String filterDataType) {
        this.filterDataType = filterDataType;
    }
}
