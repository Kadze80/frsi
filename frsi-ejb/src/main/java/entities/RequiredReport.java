package entities;

import java.io.Serializable;
import java.util.Date;

public class RequiredReport implements Serializable {
    private Date reportDate;
    private String formCode;
    private Long respondentRecId;
    private Long subjectTypeRecId;
    private String respondentNameRu;

    public RequiredReport() {
    }

    public RequiredReport(Date reportDate, String formCode, Long respondentRecId, Long subjectTypeRecId, String respondentNameRu) {
        this.reportDate = reportDate;
        this.formCode = formCode;
        this.respondentRecId = respondentRecId;
        this.subjectTypeRecId = subjectTypeRecId;
        this.respondentNameRu = respondentNameRu;
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

    public Long getRespondentRecId() {
        return respondentRecId;
    }

    public void setRespondentRecId(Long respondentRecId) {
        this.respondentRecId = respondentRecId;
    }

    public Long getSubjectTypeRecId() {
        return subjectTypeRecId;
    }

    public void setSubjectTypeRecId(Long subjectTypeRecId) {
        this.subjectTypeRecId = subjectTypeRecId;
    }

    public String getRespondentNameRu() {
        return respondentNameRu;
    }

    public void setRespondentNameRu(String respondentNameRu) {
        this.respondentNameRu = respondentNameRu;
    }

    @Override
    public String toString() {
        return "RequiredReport{" +
                "reportDate=" + reportDate +
                ", formCode='" + formCode + '\'' +
                ", respondentRecId=" + respondentRecId +
                ", subjectTypeRecId=" + subjectTypeRecId +
                ", respondentNameRu='" + respondentNameRu + '\'' +
                '}';
    }
}
