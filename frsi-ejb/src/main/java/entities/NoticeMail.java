package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 16.02.2017.
 */
public class NoticeMail implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userIdFrom;
    private String userNameFrom;
    private String emailFrom;
    private Long userIdTo;
    private String userNameTo;
    private String emailTo;
    private String message;
    private String subjectMsg;
    private int noticeSts;
    private String noticeStsName;
    private boolean isRead;
    private Date datlast;
    private Long kindEventId;
    private String kindEventName;

    // region Getter and Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserIdFrom() {
        return userIdFrom;
    }

    public void setUserIdFrom(Long userIdFrom) {
        this.userIdFrom = userIdFrom;
    }

    public String getUserNameFrom() {
        return userNameFrom;
    }

    public void setUserNameFrom(String userNameFrom) {
        this.userNameFrom = userNameFrom;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public Long getUserIdTo() {
        return userIdTo;
    }

    public void setUserIdTo(Long userIdTo) {
        this.userIdTo = userIdTo;
    }

    public String getUserNameTo() {
        return userNameTo;
    }

    public void setUserNameTo(String userNameTo) {
        this.userNameTo = userNameTo;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getNoticeSts() {
        return noticeSts;
    }

    public void setNoticeSts(int noticeSts) {
        this.noticeSts = noticeSts;
    }

    public String getNoticeStsName() {
        return noticeStsName;
    }

    public void setNoticeStsName(String noticeStsName) {
        this.noticeStsName = noticeStsName;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public Date getDatlast() {
        return datlast;
    }

    public void setDatlast(Date datlast) {
        this.datlast = datlast;
    }

    public Long getKindEventId() {
        return kindEventId;
    }

    public void setKindEventId(Long kindEventId) {
        this.kindEventId = kindEventId;
    }

    public String getKindEventName() {
        return kindEventName;
    }

    public void setKindEventName(String kindEventName) {
        this.kindEventName = kindEventName;
    }

    public String getSubjectMsg() {
        return subjectMsg;
    }

    public void setSubjectMsg(String subjectMsg) {
        this.subjectMsg = subjectMsg;
    }

    // endregion;
}
