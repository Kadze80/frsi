package entities;

import java.io.Serializable;

/**
 * Created by Marat.Madybayev on 09.01.2015.
 */
public class SubjectType_Form implements Serializable{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long subjectTypeRecId;
    private String formCode;
    private String formName;
    private String formTypeCode;
    private String formTypeName;
    private Long periodId;
    private String periodName;
    private String periodCode;
    private Long refPeriodRecId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubjectTypeRecId() {
        return subjectTypeRecId;
    }

    public void setSubjectTypeRecId(Long subjectTypeRecId) {
        this.subjectTypeRecId = subjectTypeRecId;
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

    public Long getPeriodId() {
        return periodId;
    }

    public void setPeriodId(Long periodId) {
        this.periodId = periodId;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public String getPeriodCode() {
        return periodCode;
    }

    public void setPeriodCode(String periodCode) {
        this.periodCode = periodCode;
    }

    public Long getRefPeriodRecId() {
        return refPeriodRecId;
    }

    public void setRefPeriodRecId(Long refPeriodRecId) {
        this.refPeriodRecId = refPeriodRecId;
    }

    @Override
    public String toString() {
        return "SubjectType_Form{" +
                "id=" + id +
                ", subjectTypeRecId=" + subjectTypeRecId +
                ", formCode='" + formCode + '\'' +
                ", formName='" + formName + '\'' +
                ", formTypeCode='" + formTypeCode + '\'' +
                ", formTypeName='" + formTypeName + '\'' +
                ", periodId=" + periodId +
                ", periodName='" + periodName + '\'' +
                ", periodCode='" + periodCode + '\'' +
                ", refPeriodrecId='" + refPeriodRecId + '\'' +
                '}';
    }
}
