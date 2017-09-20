package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 19.08.2015.
 */
public class FormListItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long formId;
    private Long fhId;
    private String code;
    private String languageCode;
    private String name;
    private Date beginDate;
    private Date endDate;
    private Date lastUpdateXml;
    private Date lastUpdateXls;
    private Date lastUpdateXlsOut;
    private String tag;
    private byte[] xls;
    private byte[] xlsOut;
    private Integer xmlVersion;
    private Integer xlsVersion;
    private String typeCode;
    private boolean isFillList;

    public Long getFormId() {
        return formId;
    }

    public void setFormId(Long formId) {
        this.formId = formId;
    }

    public Long getFhId() {
        return fhId;
    }

    public void setFhId(Long fhId) {
        this.fhId = fhId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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

    public int getXmlVersion() {
        return xmlVersion;
    }

    public void setXmlVersion(int xmlVersion) {
        this.xmlVersion = xmlVersion;
    }

    public Integer getXlsVersion() {
        return xlsVersion;
    }

    public void setXlsVersion(int xlsVersion) {
        this.xlsVersion = xlsVersion;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public boolean isFillList() {
        return isFillList;
    }

    public void setFillList(boolean fillList) {
        isFillList = fillList;
    }
}
