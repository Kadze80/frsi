package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Nuriddin.Baideuov on 24.04.2015.
 */
public class ReportHistorySignature implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long reportHistoryId;
    private Long userId;
    private Long refPostId;
    private String signature;
    private Date signDate;
    private Long userWarrantId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReportHistoryId() {
        return reportHistoryId;
    }

    public void setReportHistoryId(Long reportHistoryId) {
        this.reportHistoryId = reportHistoryId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRefPostId() {
        return refPostId;
    }

    public void setRefPostId(Long refPostId) {
        this.refPostId = refPostId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Date getSignDate() {
        return signDate;
    }

    public void setSignDate(Date signDate) {
        this.signDate = signDate;
    }

    public Long getUserWarrantId() {
        return userWarrantId;
    }

    public void setUserWarrantId(Long userWarrantId) {
        this.userWarrantId = userWarrantId;
    }

    @Override
    public String toString() {
        return "ReportHistorySignature{" +
                "id=" + id +
                ", reportHistoryId=" + reportHistoryId +
                ", userId=" + userId +
                ", refPostId=" + refPostId +
                ", signature='" + signature + '\'' +
                '}';
    }
}
