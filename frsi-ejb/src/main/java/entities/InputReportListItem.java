package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Nuriddin.Baideuov on 24.05.2015.
 */
public class InputReportListItem implements Serializable{
    private static final long serialVersionUID = 1L;

    private long id;
    private Date reportDate;
    private String formCode;
    private String formName;
    private Date saveDate;
    private String idn;
    private String respondentNameKz;
    private String respondentNameRu;
    private String respondentNameEn;
    private String respondentShortNameRu;
    private String subjectTypeNameRu;

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

    public Date getSaveDate() {
        return saveDate;
    }

    public void setSaveDate(Date saveDate) {
        this.saveDate = saveDate;
    }

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    public String getRespondentNameKz() {
        return respondentNameKz;
    }

    public void setRespondentNameKz(String respondentNameKz) {
        this.respondentNameKz = respondentNameKz;
    }

    public String getRespondentNameRu() {
        return respondentNameRu;
    }

    public void setRespondentNameRu(String respondentNameRu) {
        this.respondentNameRu = respondentNameRu;
    }

    public String getRespondentNameEn() {
        return respondentNameEn;
    }

    public void setRespondentNameEn(String respondentNameEn) {
        this.respondentNameEn = respondentNameEn;
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
}
