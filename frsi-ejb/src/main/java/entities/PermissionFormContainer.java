package entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nuriddin.Baideuov on 03.04.2015.
 */
public class PermissionFormContainer implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String formCode;
    private String formName;
    private String formTypeCode;
    private String formTypeName;
    private Map<String, Map<Long, PermissionForm>> items = new HashMap<String, Map<Long, PermissionForm>>();
    private Map<Long, String> refRespondents = new HashMap<Long, String>();

    private boolean show;
    private boolean edit;
    private boolean delete;
    private boolean approve;
    private boolean disapprove;
    private boolean sign;

    private Map<String, Integer> activeItemCounts = new HashMap<String, Integer>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void addItem(PermissionForm permissionForm) {
        put(permissionForm);
    }

    public void addItemValue(String permissionName, Long refRespondentRecId, boolean value) {
        PermissionForm permissionForm;
        if (!contains(permissionName, refRespondentRecId)) {
            permissionForm = new PermissionForm();
            permissionForm.setFormName(formName);
            permissionForm.setFormCode(formCode);
            permissionForm.setPermissionName(permissionName);
            permissionForm.setRefRespondentRecId(refRespondentRecId);
            permissionForm.setIdn(refRespondents.get(refRespondentRecId));
            put(permissionForm);
        } else {
            permissionForm = get(permissionName, refRespondentRecId);
        }
        if(permissionForm != null) {
            permissionForm.setActive(value);
        }
    }

    private void put(PermissionForm permissionForm) {
        if (!items.containsKey(permissionForm.getPermissionName())) {
            items.put(permissionForm.getPermissionName(), new HashMap<Long, PermissionForm>());
        }
        items.get(permissionForm.getPermissionName()).put(permissionForm.getRefRespondentRecId(), permissionForm);
    }

    private PermissionForm get(String permissionName, Long refRespondentRecId) {
        if (!items.containsKey(permissionName))
            return null;
        return items.get(permissionName).get(refRespondentRecId);
    }

    private boolean contains(String permissionName, Long refRespondentRecId) {
        return items.containsKey(permissionName) && items.get(permissionName).containsKey(refRespondentRecId);
    }

    public void updateState(String permissionName) {
        int count = 0;
        if (items.containsKey(permissionName)) {
            for (PermissionForm permissionForm : items.get(permissionName).values()) {
                if (permissionForm.isActive())
                    count++;
            }
        }
        activeItemCounts.put(permissionName, count);
        setValue(permissionName, count > 0);
    }

    public void updateAllStates() {
        updateState("F:SHOW");
        updateState("F:EDIT");
        updateState("F:SIGN");
        updateState("F:DELETE");
        updateState("F:APPROVE");
        updateState("F:DISAPPROVE");
    }


    public void updateItems(String permissionName) {
        for (Long refRespondentRecId : refRespondents.keySet()) {
            addItemValue(permissionName, refRespondentRecId, getValue(permissionName));
        }
        boolean value = getValue(permissionName);
        if (value)
            activeItemCounts.put(permissionName, refRespondents.size());
        else
            activeItemCounts.put(permissionName, 0);
    }

    public int getActiveCount(String permissionName) {
        if (!activeItemCounts.containsKey(permissionName))
            return 0;
        else
            return activeItemCounts.get(permissionName);
    }

    public boolean isAllActive(String permissionName) {
        boolean active = getValue(permissionName);
        if (!active)
            return false;
        return getRefRespondents().size() == getActiveCount(permissionName);
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public String getFormTypeCode() {
        return formTypeCode;
    }

    public void setFormTypeCode(String formTypeCode) {
        this.formTypeCode = formTypeCode;
    }

    public String getFormTypeName() {
        return formTypeName;
    }

    public void setFormTypeName(String formTypeName) {
        this.formTypeName = formTypeName;
    }

    public Map<String, Map<Long, PermissionForm>> getItems() {
        return items;
    }

    public void setItems(Map<String, Map<Long, PermissionForm>> items) {
        this.items = items;
    }

    public boolean getValue(String permissionName) {
        if (permissionName.equalsIgnoreCase("F:SHOW"))
            return show;
        else if (permissionName.equalsIgnoreCase("F:EDIT"))
            return edit;
        else if (permissionName.equalsIgnoreCase("F:DELETE"))
            return delete;
        else if (permissionName.equalsIgnoreCase("F:APPROVE"))
            return approve;
        else if (permissionName.equalsIgnoreCase("F:DISAPPROVE"))
            return disapprove;
        else if (permissionName.equalsIgnoreCase("F:SIGN"))
            return sign;
        throw new IllegalStateException("Unknown permission name " + permissionName);
    }

    public void setValue(String permissionName, boolean value) {
        if (permissionName.equalsIgnoreCase("F:SHOW"))
            show = value;
        else if (permissionName.equalsIgnoreCase("F:EDIT"))
            edit = value;
        else if (permissionName.equalsIgnoreCase("F:DELETE"))
            delete = value;
        else if (permissionName.equalsIgnoreCase("F:APPROVE"))
            approve = value;
        else if (permissionName.equalsIgnoreCase("F:DISAPPROVE"))
            disapprove = value;
        else if (permissionName.equalsIgnoreCase("F:SIGN"))
            sign = value;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
        updateItems("F:SHOW");
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
        updateItems("F:EDIT");
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
        updateItems("F:DELETE");
    }

    public boolean isApprove() {
        return approve;
    }

    public void setApprove(boolean approve) {
        this.approve = approve;
        updateItems("F:APPROVE");
    }

    public boolean isDisapprove() {
        return disapprove;
    }

    public void setDisapprove(boolean disapprove) {
        this.disapprove = disapprove;
        updateItems("F:DISAPPROVE");
    }

    public boolean isSign() {
        return sign;
    }

    public void setSign(boolean sign) {
        this.sign = sign;
        updateItems("F:SIGN");
    }

    public Map<Long, String> getRefRespondents() {
        return refRespondents;
    }

    public void setRefRespondents(Map<Long, String> refRespondents) {
        this.refRespondents = refRespondents;
    }

    @Override
    public String toString() {
        return "PermissionFormContainer{" +
                "formCode='" + formCode + '\'' +
                ", formName='" + formName + '\'' +
                ", items=" + items +
                '}';
    }
}
