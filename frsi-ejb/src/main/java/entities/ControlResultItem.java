package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Nuriddin.Baideuov on 12.06.2015.
 */
public class ControlResultItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String descriptionRu;
    private Long resultType;
    private Long crosscheckType;
    private String crosscheckTypeNameRu;
    private Long sortNum;
    private String dynamicRowId;
    private Date reportDate;
    private String errorMessage;
    private long externalSystemId;
    private String externalSystemNameRu;
    private String resultL;
    private String resultR;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescriptionRu() {
        return descriptionRu;
    }

    public void setDescriptionRu(String descriptionRu) {
        this.descriptionRu = descriptionRu;
    }

    public Long getResultType() {
        return resultType;
    }

    public void setResultType(Long resultType) {
        this.resultType = resultType;
    }

    public String getCrosscheckTypeNameRu() {
        return crosscheckTypeNameRu;
    }

    public void setCrosscheckTypeNameRu(String crosscheckTypeNameRu) {
        this.crosscheckTypeNameRu = crosscheckTypeNameRu;
    }

    public Long getCrosscheckType() {
        return crosscheckType;
    }

    public void setCrosscheckType(Long crosscheckType) {
        this.crosscheckType = crosscheckType;
    }

    public Long getSortNum() {
        return sortNum;
    }

    public void setSortNum(Long sortNum) {
        this.sortNum = sortNum;
    }

    public String getDynamicRowId() {
        return dynamicRowId;
    }

    public void setDynamicRowId(String dynamicRowId) {
        this.dynamicRowId = dynamicRowId;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getExternalSystemId() {
        return externalSystemId;
    }

    public void setExternalSystemId(long externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    public String getExternalSystemNameRu() {
        return externalSystemNameRu;
    }

    public void setExternalSystemNameRu(String externalSystemNameRu) {
        this.externalSystemNameRu = externalSystemNameRu;
    }

    public String getResultL() {
        return resultL;
    }

    public void setResultL(String resultL) {
        this.resultL = resultL;
    }

    public String getResultR() {
        return resultR;
    }

    public void setResultR(String resultR) {
        this.resultR = resultR;
    }
}
