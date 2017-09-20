package entities;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Timestamp;
import java.util.Date;

public class PortalUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String ScreenName;
    private String emailAddress;
    private String firstName;
    private String lastName;
    private String middleName;
    private String fio;
    private Date modifiedDate;
    private boolean blocked;
    private Long respondentId;
    private String idn;
    private String passport;
    private Long refPostId;
    private boolean mustSign;
    private String designUserName;
    private String roleName;
    private String groupName;
    private String subjectTypeName;
    private String respondentName;
    private String postName;


    public PortalUser() {
    }

    public Struct toStruct(Connection connection) throws SQLException {
        final Object[] attributes = new Object[9];
        attributes[0] = id;
        attributes[1] = userId;
        attributes[2] = ScreenName;
        attributes[3] = emailAddress;
        attributes[4] = firstName;
        attributes[5] = lastName;
        attributes[6] = middleName;
        attributes[7] = modifiedDate!=null? new Timestamp(modifiedDate.getTime()):null;
        attributes[8] = blocked ? 1 : 0;
        return connection.createStruct("USER_ROW", attributes);
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the userId
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @return the ScreenName
     */
    public String getScreenName() {
        return ScreenName;
    }

    /**
     * @param ScreenName the ScreenName to set
     */
    public void setScreenName(String ScreenName) {
        this.ScreenName = ScreenName;
    }

    /**
     * @return the emailAddress
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * @param emailAddress the emailAddress to set
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the middleName
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * @param middleName the middleName to set
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * @return the modifiedDate
     */
    public Date getModifiedDate() {
        return modifiedDate;
    }

    /**
     * @param modifiedDate the modifiedDate to set
     */
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public Long getRespondentId() {
        return respondentId;
    }

    public void setRespondentId(Long respondentId) {
        this.respondentId = respondentId;
    }

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    public Long getRefPostId() {
        return refPostId;
    }

    public void setRefPostId(Long refPostId) {
        this.refPostId = refPostId;
    }

    public boolean isMustSign() {
        return mustSign;
    }

    public void setMustSign(boolean mustSign) {
        this.mustSign = mustSign;
    }

    public String getDesignUserName() {
        return designUserName;
    }

    public void setDesignUserName(String designUserName) {
        this.designUserName = designUserName;
    }

    public String getFullName() {
        return ((lastName != null && !lastName.trim().isEmpty()) ? lastName + " " : "") + firstName + ((middleName != null && !middleName.trim().isEmpty()) ? " " + middleName : "");
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getSubjectTypeName() {
        return subjectTypeName;
    }

    public void setSubjectTypeName(String subjectTypeName) {
        this.subjectTypeName = subjectTypeName;
    }

    public String getRespondentName() {
        return respondentName;
    }

    public void setRespondentName(String respondentName) {
        this.respondentName = respondentName;
    }

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = true;
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PortalUser that = (PortalUser) o;

        if (blocked != that.blocked) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;

        if ((ScreenName == null && that.ScreenName == null)
                || ((ScreenName == null && that.ScreenName != null && that.ScreenName.isEmpty())
                ||
                (that.ScreenName == null && ScreenName != null && ScreenName.isEmpty()))) result = true;
        else if (ScreenName != null ? !ScreenName.equals(that.ScreenName) : that.ScreenName != null) return false;
        if ((emailAddress == null && that.emailAddress == null)
                || ((emailAddress == null && that.emailAddress != null && that.emailAddress.isEmpty())
                ||
                (that.emailAddress == null && emailAddress != null && emailAddress.isEmpty()))) result = true;
        else if (emailAddress != null ? !emailAddress.equals(that.emailAddress) : that.emailAddress != null) return false;
        if ((firstName == null && that.firstName == null)
                || ((firstName == null && that.firstName != null && that.firstName.isEmpty())
                ||
                (that.firstName == null && firstName != null && firstName.isEmpty()))) result = true;
        else if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null) return false;
        if ((lastName == null && that.lastName == null)
                || ((lastName == null && that.lastName != null && that.lastName.isEmpty())
                ||
                (that.lastName == null && lastName != null && lastName.isEmpty()))) result = true;
        else if (lastName != null ? !lastName.equals(that.lastName) : that.lastName != null) return false;
        if ((middleName == null && that.middleName == null)
                || ((middleName == null && that.middleName != null && that.middleName.isEmpty())
                ||
                (that.middleName == null && middleName != null && middleName.isEmpty()))) result = true;
        else if (middleName != null ? !middleName.equals(that.middleName) : that.middleName != null) return false;
        if (modifiedDate != null ? !modifiedDate.toString().equals(that.modifiedDate.toString()) : that.modifiedDate != null) return false;
        return result;
    }

    public boolean equalsFull(Object o) {
        boolean result = true;
        if (!equals(o)) return false;

        PortalUser that = (PortalUser) o;

        if (respondentId != null ? !respondentId.equals(that.respondentId) : that.respondentId != null) return false;
        if (idn != null ? !idn.equals(that.idn) : that.idn != null) return false;
        if (refPostId != null ? !refPostId.equals(that.refPostId) : that.refPostId != null) return false;
        if (mustSign != that.mustSign) return false;
        if (designUserName != null ? !designUserName.equals(that.designUserName) : that.designUserName != null) return false;
        return result;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (ScreenName != null ? ScreenName.hashCode() : 0);
        result = 31 * result + (emailAddress != null ? emailAddress.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (middleName != null ? middleName.hashCode() : 0);
        result = 31 * result + (modifiedDate != null ? modifiedDate.hashCode() : 0);
        result = 31 * result + (blocked ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PortalUser{" +
                "id=" + id +
                ", userId=" + userId +
                ", ScreenName='" + ScreenName + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", idn='" + idn + '\'' +
                ", respondentId='" + respondentId + '\'' +
                ", refPostId='" + refPostId + '\'' +
                ", modifiedDate=" + modifiedDate +
                ", blocked=" + blocked +
                '}';
    }
}

