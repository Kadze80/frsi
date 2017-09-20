package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by ayupov.bakhtiyar on 03.05.2017.
 */
public class RespondentWarrant implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long recIdParent;
    private Long recId;
    private String childName;
    private Date bDate;
    private Date eDate;
    private String num;
    private byte[] fileWarrant;
    private Boolean haveFile;
    private Boolean submitReport;

    // region Getter and Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecIdParent() {
        return recIdParent;
    }

    public void setRecIdParent(Long recIdParent) {
        this.recIdParent = recIdParent;
    }

    public Long getRecId() {
        return recId;
    }

    public void setRecId(Long recId) {
        this.recId = recId;
    }

    public Date getbDate() {
        return bDate;
    }

    public void setbDate(Date bDate) {
        this.bDate = bDate;
    }

    public Date geteDate() {
        return eDate;
    }

    public void seteDate(Date eDate) {
        this.eDate = eDate;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public byte[] getFileWarrant() {
        return fileWarrant;
    }

    public void setFileWarrant(byte[] fileWarrant) {
        this.fileWarrant = fileWarrant;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public Boolean getHaveFile() {
        return haveFile;
    }

    public void setHaveFile(Boolean haveFile) {
        this.haveFile = haveFile;
    }

    public Boolean getSubmitReport() {
        return submitReport;
    }

    public void setSubmitReport(Boolean submitReport) {
        this.submitReport = submitReport;
    }

    // endregion
}
