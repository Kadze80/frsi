package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 18.08.2015.
 */
public class FormHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long formId;
    private String languageCode;
    private Date beginDate;
    private Date endDate;
    private Date lastUpdateXml;
    private Date lastUpdateXls;
    private Date lastUpdateXlsOut;
    private String xml;
    private String html;
    private String htmlView;
    private String inputValueChecks;
    private byte[] xls;
    private byte[] xlsOut;
    private String tag;
    private String periodCode;
    private String jsCode;
    private Integer periodCount;
    private Integer xmlVersion;
    private Integer xlsVersion;
    private String name;
    private String shortName;

    // Non-persistable fields
    private FormTag formTag;
    private boolean isValid;
    private String validationMessage;
    private String errorMessage;




    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFormId() {
        return formId;
    }

    public void setFormId(Long formId) {
        this.formId = formId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getLastUpdateXml() {
        return lastUpdateXml;
    }

    public void setLastUpdateXml(Date lastUpdateXml) {
        this.lastUpdateXml = lastUpdateXml;
    }

    public Date getLastUpdateXls() {
        return lastUpdateXls;
    }

    public void setLastUpdateXls(Date lastUpdateXls) {
        this.lastUpdateXls = lastUpdateXls;
    }

    public Date getLastUpdateXlsOut() {
        return lastUpdateXlsOut;
    }

    public void setLastUpdateXlsOut(Date lastUpdateXlsOut) {
        this.lastUpdateXlsOut = lastUpdateXlsOut;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getInputValueChecks() {
        return inputValueChecks;
    }

    public void setInputValueChecks(String inputValueChecks) {
        this.inputValueChecks = inputValueChecks;
    }

    public byte[] getXls() {
        return xls;
    }

    public void setXls(byte[] xls) {
        this.xls = xls;
    }

    public byte[] getXlsOut() {
        return xlsOut;
    }

    public void setXlsOut(byte[] xlsOut) {
        this.xlsOut = xlsOut;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getPeriodCode() {
        return periodCode;
    }

    public void setPeriodCode(String periodCode) {
        this.periodCode = periodCode;
    }

    public FormTag getFormTag() {
        return formTag;
    }

    public void setFormTag(FormTag formTag) {
        this.formTag = formTag;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getJsCode() {
        return jsCode;
    }

    public void setJsCode(String jsCode) {
        this.jsCode = jsCode;
    }

    public Integer getPeriodCount() {
        return periodCount;
    }

    public void setPeriodCount(Integer periodCount) {
        this.periodCount = periodCount;
    }

    public String getHtmlView() {
        return htmlView;
    }

    public void setHtmlView(String htmlView) {
        this.htmlView = htmlView;
    }

    public Integer getXmlVersion() {
        return xmlVersion;
    }

    public void setXmlVersion(Integer xmlVersion) {
        this.xmlVersion = xmlVersion;
    }

    public Integer getXlsVersion() {
        return xlsVersion;
    }

    public void setXlsVersion(Integer xlsVersion) {
        this.xlsVersion = xlsVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FormHistory that = (FormHistory) o;

        if (!id.equals(that.id)) return false;
        if (languageCode != null ? !languageCode.equals(that.languageCode) : that.languageCode != null) return false;
        if (beginDate != null ? !beginDate.equals(that.beginDate) : that.beginDate != null) return false;
        if (endDate != null ? !endDate.equals(that.endDate) : that.endDate != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = languageCode != null ? languageCode.hashCode() : 0;
        result = 31 * result + (beginDate != null ? beginDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
