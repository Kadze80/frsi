package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 01.08.2016.
 */
public class Template implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String code;
    private String name;
    private String codeTemplate;
    private byte[] xlsOut;
    private long typeTemplate;
    private boolean haveTemplate;
    private Date beginDate;
    private Date endDate;

    public Template(){

    }

    public Template(String codeTemplate) {
        this.codeTemplate = codeTemplate;
    }

    // region Getter and Setter
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCodeTemplate() {
        return codeTemplate;
    }

    public void setCodeTemplate(String codeTemplate) {
        this.codeTemplate = codeTemplate;
    }

    public byte[] getXlsOut() {
        return xlsOut;
    }

    public void setXlsOut(byte[] xlsOut) {
        this.xlsOut = xlsOut;
    }

    public long getTypeTemplate() {
        return typeTemplate;
    }

    public void setTypeTemplate(long typeTemplate) {
        this.typeTemplate = typeTemplate;
    }

    public boolean isHaveTemplate() {
        return haveTemplate;
    }

    public void setHaveTemplate(boolean haveTemplate) {
        this.haveTemplate = haveTemplate;
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

    // endregion
}
