package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity
 *
 * @author Ardak Saduakassov
 */
public class ReportListItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private Date reportDate;
    private String formCode;
    private String formName;
    private String formTypeCode;
    private String formTypeName;
    private Date saveDate;
    private String status;
    private String statusName;
    private Date statusDate;
    private Date firstCompletedDate;
    private Date lastCompletedDate;
    private int completeCount;
    private String userInfo;
    private String deliveryWay;
    private String respondentNameRu;
    private String respondentShortNameRu;
    private String childRespondentNameRu;
    private String childRespondentShortNameRu;
    private String subjectTypeNameRu;
    private String subjectTypeShortNameRu;
    private String subjectTypeName;
    private String departmentNameRu;
    private String controlResultCode;
    private String controlResultName;
    private String signInfo;
    private boolean haveAttachedFile;
    private boolean haveAttachedLetter;
    private boolean canAttachedFile;
    private boolean haveNote;
    private boolean submitReport;
    private String submitReportText;
    private String hash;
    private String signature;
    private int rowNum;
    private String periodCode;
    private String idn;
    private String idnChild;
    private Integer overdueDays;
    private String periodAlgError;
    private String nameNPA;
    private boolean haveFiles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportListItem that = (ReportListItem) o;
        if (id != that.id) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    // region Getter and Setter
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
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

    public Date getSaveDate() {
        return saveDate;
    }

    public void setSaveDate(Date saveDate) {
        this.saveDate = saveDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public Date getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(Date statusDate) {
        this.statusDate = statusDate;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getDeliveryWay() {
        return deliveryWay;
    }

    public void setDeliveryWay(String deliveryWay) {
        this.deliveryWay = deliveryWay;
    }

    public Date getFirstCompletedDate() {
        return firstCompletedDate;
    }

    public void setFirstCompletedDate(Date firstCompletedDate) {
        this.firstCompletedDate = firstCompletedDate;
    }

    public String getRespondentNameRu() {
        return respondentNameRu;
    }

    public String getRespondentName(){
        if(respondentShortNameRu == null || respondentShortNameRu.trim().isEmpty())
            return respondentNameRu;
        else
            return respondentShortNameRu;
    }

    public void setRespondentNameRu(String respondentNameRu) {
        this.respondentNameRu = respondentNameRu;
    }

    public String getRespondentShortNameRu() {
        return respondentShortNameRu;
    }

    public void setRespondentShortNameRu(String respondentShortNameRu) {
        this.respondentShortNameRu = respondentShortNameRu;
    }

    public String getSubjectTypeNameRu() {
        return subjectTypeNameRu;
    }

    public void setSubjectTypeNameRu(String subjectTypeNameRu) {
        this.subjectTypeNameRu = subjectTypeNameRu;
    }

    public int getCompleteCount() {
        return completeCount;
    }

    public void setCompleteCount(int completeCount) {
        this.completeCount = completeCount;
    }

    public Date getLastCompletedDate() {
        return lastCompletedDate;
    }

    public void setLastCompletedDate(Date lastCompletedDate) {
        this.lastCompletedDate = lastCompletedDate;
    }

    public String getControlResultCode() {
        return controlResultCode;
    }

    public void setControlResultCode(String controlResultCode) {
        this.controlResultCode = controlResultCode;
    }

    public String getControlResultName() {
        return controlResultName;
    }

    public void setControlResultName(String controlResultName) {
        this.controlResultName = controlResultName;
    }

    public String getSignInfo() {
        return signInfo;
    }

    public void setSignInfo(String signInfo) {
        this.signInfo = signInfo;
    }

    public boolean getHaveAttachedFile() {
        return haveAttachedFile;
    }

    public void setHaveAttachedFile(boolean haveAttachedFile) {
        this.haveAttachedFile = haveAttachedFile;
    }

    public boolean getHaveAttachedLetter() {
        return haveAttachedLetter;
    }

    public void setHaveAttachedLetter(boolean haveAttachedLetter) {
        this.haveAttachedLetter = haveAttachedLetter;
    }

    public boolean getCanAttachedFile() {
        return canAttachedFile;
    }

    public void setCanAttachedFile(boolean canAttachedFile) {
        this.canAttachedFile = canAttachedFile;
    }

    public boolean getHaveNote() {
        return haveNote;
    }

    public void setHaveNote(boolean haveNote) {
        this.haveNote= haveNote;
    }

    public boolean isSubmitReport() {
        return submitReport;
    }

    public void setSubmitReport(boolean submitReport) {
        this.submitReport = submitReport;
    }

    public String getSubmitReportText() {
        return submitReportText;
    }

    public void setSubmitReportText(String submitReportText) {
        this.submitReportText = submitReportText;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public String getSubjectTypeShortNameRu() {
        return subjectTypeShortNameRu;
    }

    public void setSubjectTypeShortNameRu(String subjectTypeShortNameRu) {
        this.subjectTypeShortNameRu = subjectTypeShortNameRu;
    }

    public String getSubjectTypeName() {
        if(subjectTypeShortNameRu == null || subjectTypeShortNameRu.trim().isEmpty())
            return subjectTypeNameRu;
        else
            return subjectTypeShortNameRu;
    }

    public void setSubjectTypeName(String subjectTypeName) {
        this.subjectTypeName = subjectTypeName;
    }

    public String getPeriodCode() {
        return periodCode;
    }

    public void setPeriodCode(String periodCode) {
        this.periodCode = periodCode;
    }

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    public Integer getOverdueDays() {return overdueDays;}

    public void setOverdueDays(Integer overdueDays) {
        this.overdueDays = overdueDays;
    }

    public String getPeriodAlgError() {
        return periodAlgError;
    }

    public void setPeriodAlgError(String periodAlgError) {
        this.periodAlgError = periodAlgError;
    }

    public String getDepartmentNameRu() {
        return departmentNameRu;
    }

    public void setDepartmentNameRu(String departmentNameRu) {
        this.departmentNameRu = departmentNameRu;
    }

    public String getChildRespondentNameRu() {
        return childRespondentNameRu;
    }

    public void setChildRespondentNameRu(String childRespondentNameRu) {
        this.childRespondentNameRu = childRespondentNameRu;
    }

    public String getChildRespondentShortNameRu() {
        return childRespondentShortNameRu;
    }

    public void setChildRespondentShortNameRu(String childRespondentShortNameRu) {
        this.childRespondentShortNameRu = childRespondentShortNameRu;
    }

    public String getIdnChild() {
        return idnChild;
    }

    public void setIdnChild(String idnChild) {
        this.idnChild = idnChild;
    }

    public String getNameNPA() {
        return nameNPA;
    }

    public void setNameNPA(String nameNPA) {
        this.nameNPA = nameNPA;
    }

    public boolean isHaveFiles() {
        return haveFiles;
    }

    public void setHaveFiles(boolean haveFiles) {
        this.haveFiles = haveFiles;
    }

// endregion

}
