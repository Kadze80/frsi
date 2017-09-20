package entities;

import java.io.Serializable;
import java.util.Date;

public class ReportPeriod implements Serializable{
    private Long reportId;
    private String formCode;
    private String idn;
    private Date reportDate;
    private Integer leftDays;
    private String periodAlgError;
    private Long refPeriodRecId;
    private String formName;
    private String statusName;
    private String status;
    private Long respondentId;
    private Long subjectTypeRecId;

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public Integer getLeftDays() {
        return leftDays;
    }

    public void setLeftDays(Integer leftDays) {
        this.leftDays = leftDays;
    }

    public String getPeriodAlgError() {
        return periodAlgError;
    }

    public void setPeriodAlgError(String periodAlgError) {
        this.periodAlgError = periodAlgError;
    }

    public Long getRefPeriodRecId() {
        return refPeriodRecId;
    }

    public void setRefPeriodRecId(Long refPeriodRecId) {
        this.refPeriodRecId = refPeriodRecId;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getRespondentId() {
        return respondentId;
    }

    public void setRespondentId(Long respondentId) {
        this.respondentId = respondentId;
    }

    public Long getSubjectTypeRecId() {
        return subjectTypeRecId;
    }

    public void setSubjectTypeRecId(Long subjectTypeRecId) {
        this.subjectTypeRecId = subjectTypeRecId;
    }

    @Override
    public String toString() {
        return "ReportPeriod{" +
                "reportId=" + reportId +
                ", formCode='" + formCode + '\'' +
                ", idn='" + idn + '\'' +
                ", reportDate=" + reportDate +
                ", leftDays=" + leftDays +
                ", periodAlgError='" + periodAlgError + '\'' +
                ", refPeriodRecId=" + refPeriodRecId +
                ", formName='" + formName + '\'' +
                ", statusName='" + statusName + '\'' +
                ", status='" + status + '\'' +
                ", respondentId=" + respondentId +
                ", subjectTypeRecId=" + subjectTypeRecId +
                '}';
    }
}
