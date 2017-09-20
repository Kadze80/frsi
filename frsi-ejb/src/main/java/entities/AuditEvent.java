package entities;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 24.11.2015.
 */
public class AuditEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private Long idNameEvent;
    private String nameEvent;
    private String nameObject;
    private String codeObject;
    private Long idKindEvent;
    private String kindEvent;
    private Date dateEvent;
    private Long idRefRespondent;
    private String respondentName;
    private Date dateIn;
    private Long recId;
    private Long userId;
    private String userName;
    private String userLocation;
    private Date datlast;
    private Boolean isArchive;
    private String screenName;
    private String addMessage;
    private String subjectMsg;
    private String idn;


    public static OracleCallableStatement setOCSParamsForIns(AuditEvent auditEvent, OracleCallableStatement ocs) throws SQLException{
        ocs.setString(1, auditEvent.getCodeObject());
        if(auditEvent.getNameObject() == null || auditEvent.getNameObject().isEmpty()) {
            ocs.setNull(2, OracleTypes.NULL);
        }else{
            ocs.setString(2, auditEvent.getNameObject());
        }
        ocs.setLong(3, auditEvent.getIdKindEvent());
        ocs.setDate(4, new java.sql.Date(auditEvent.getDateEvent().getTime()));
        if(auditEvent.getIdRefRespondent() == null || auditEvent.getIdRefRespondent() == 0)
            ocs.setNull(5, OracleTypes.NULL);
        else
            ocs.setLong(5, auditEvent.getIdRefRespondent());
        ocs.setDate(6, new java.sql.Date(auditEvent.getDateIn().getTime()));
        if (auditEvent.getRecId() != null && auditEvent.getRecId() != 0) {
            ocs.setLong(7, auditEvent.getRecId());
        }else {
            ocs.setNull(7, OracleTypes.NULL);
        }
        ocs.setLong(8, auditEvent.getUserId());
        ocs.setString(9, auditEvent.getUserLocation());
        ocs.setString(10, auditEvent.getAddMessage());
        if(auditEvent.getParentId() == null || auditEvent.getParentId() == 0)
            ocs.setNull(11, OracleTypes.NULL);
        else
            ocs.setLong(11, auditEvent.getParentId());
        ocs.setInt(12,0);
        ocs.registerOutParameter(13, OracleTypes.INTEGER);
        ocs.registerOutParameter(14, OracleTypes.INTEGER);
        ocs.registerOutParameter(15, OracleTypes.VARCHAR);
        return ocs;
    }

    // region Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdNameEvent() {
        return idNameEvent;
    }

    public void setIdNameEvent(Long idNameEvent) {
        this.idNameEvent = idNameEvent;
    }

    public String getNameObject() {
        return nameObject;
    }

    public void setNameObject(String nameObject) {
        this.nameObject = nameObject;
    }

    public String getCodeObject() {
        return codeObject;
    }

    public void setCodeObject(String codeObject) {
        this.codeObject = codeObject;
    }

    public String getNameEvent() {
        return nameEvent;
    }

    public void setNameEvent(String nameEvent) {
        this.nameEvent = nameEvent;
    }

    public Long getIdKindEvent() {
        return idKindEvent;
    }

    public void setIdKindEvent(Long idKindEvent) {
        this.idKindEvent = idKindEvent;
    }

    public String getKindEvent() {
        return kindEvent;
    }

    public void setKindEvent(String kindEvent) {
        this.kindEvent = kindEvent;
    }

    public Date getDateEvent() {
        return dateEvent;
    }

    public void setDateEvent(Date dateEvent) {
        this.dateEvent = dateEvent;
    }

    public Long getIdRefRespondent() {
        return idRefRespondent;
    }

    public void setIdRefRespondent(Long idRefRespondent) {
        this.idRefRespondent = idRefRespondent;
    }

    public String getRespondentName() {
        return respondentName;
    }

    public void setRespondentName(String respondentName) {
        this.respondentName = respondentName;
    }

    public Date getDateIn() {
        return dateIn;
    }

    public void setDateIn(Date dateIn) {
        this.dateIn = dateIn;
    }

    public Long getRecId() {
        return recId;
    }

    public void setRecId(Long recId) {
        this.recId = recId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public Date getDatlast() {
        return datlast;
    }

    public void setDatlast(Date datlast) {
        this.datlast = datlast;
    }

    public Boolean getIsArchive() {
        return isArchive;
    }

    public void setIsArchive(Boolean isArchive) {
        this.isArchive = isArchive;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getAddMessage() {
        return addMessage;
    }

    public void setAddMessage(String addMessage) {
        this.addMessage = addMessage;
    }

    public String getSubjectMsg() {
        return subjectMsg;
    }

    public void setSubjectMsg(String subjectMsg) {
        this.subjectMsg = subjectMsg;
    }

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
// endregion
}
