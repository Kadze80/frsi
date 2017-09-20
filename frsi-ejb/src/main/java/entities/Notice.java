package entities;

import java.io.Serializable;

/**
 * Created by Ayupov.Bakhtiyar on 10.02.2017.
 */
public class Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String message;
    private String subjectMsg;
    private Long idNameEvent;
    private String nameEvent;
    private Boolean roleRender;
    private Boolean groupRender;
    private Boolean stRender;
    private Boolean respondentRender;
    private Boolean userRender;

    // region Getter and Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getIdNameEvent() {
        return idNameEvent;
    }

    public void setIdNameEvent(Long idNameEvent) {
        this.idNameEvent = idNameEvent;
    }

    public String getNameEvent() {
        return nameEvent;
    }

    public void setNameEvent(String nameEvent) {
        this.nameEvent = nameEvent;
    }

    public String getSubjectMsg() {
        return subjectMsg;
    }

    public void setSubjectMsg(String subjectMsg) {
        this.subjectMsg = subjectMsg;
    }

    public Boolean getRoleRender() {
        return roleRender;
    }

    public void setRoleRender(Boolean roleRender) {
        this.roleRender = roleRender;
    }

    public Boolean getGroupRender() {
        return groupRender;
    }

    public void setGroupRender(Boolean groupRender) {
        this.groupRender = groupRender;
    }

    public Boolean getStRender() {
        return stRender;
    }

    public void setStRender(Boolean stRender) {
        this.stRender = stRender;
    }

    public Boolean getRespondentRender() {
        return respondentRender;
    }

    public void setRespondentRender(Boolean respondentRender) {
        this.respondentRender = respondentRender;
    }

    public Boolean getUserRender() {
        return userRender;
    }

    public void setUserRender(Boolean userRender) {
        this.userRender = userRender;
    }

    // endregion
}
