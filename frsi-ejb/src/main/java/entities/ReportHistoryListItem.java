package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity
 *
 * @author Nuriddin Baideuov
 */
public class ReportHistoryListItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long reportId;
    private Date saveDate;
    private String status;
    private String statusName;
    private Date statusDate;
    private long userId;
    private String userInfo;
    private String deliveryWay;
    private Date completeDate;
    private boolean haveAttachedFile;
    private boolean haveAttachedLetter;
    private boolean canAttachedFile;
    private String controlResultCode;
    private String controlResultName;
    private boolean current;
    private String comment;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getReportId() {
        return reportId;
    }

    public void setReportId(long reportId) {
        this.reportId = reportId;
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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
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

    public Date getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(Date completeDate) {
        this.completeDate = completeDate;
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

    public boolean isCanAttachedFile() {
        return canAttachedFile;
    }

    public void setCanAttachedFile(boolean canAttachedFile) {
        this.canAttachedFile = canAttachedFile;
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

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportHistoryListItem that = (ReportHistoryListItem) o;
        if (id != that.id) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
