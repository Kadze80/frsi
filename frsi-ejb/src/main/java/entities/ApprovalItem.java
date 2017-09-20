package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity
 *
 * @author Ardak Saduakassov
 */
public class ApprovalItem implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;
	private Long batchId;
	private Long userId;
    private Long respondentId;
    private String respName;
    private String bin;
	private Long entityId;
	private Date reportDate;
	private Date receivedDate;
	private Long isApproved;
	private String status;
	private String formName;
	private String title;
    private Date approvalDate;
	private String sReportDate;
	private String sReceivedDate;
	private String sApprovalDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getBatchId() {
		return batchId;
	}

	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getRespondentId() {
		return respondentId;
	}

	public void setRespondentId(Long respondentId) {
		this.respondentId = respondentId;
	}

	public String getRespName() {
		return respName;
	}

	public void setRespName(String respName) {
		this.respName = respName;
	}

	public String getBin() {
		return bin;
	}

	public void setBin(String bin) {
		this.bin = bin;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public Date getReportDate() {
		return reportDate;
	}

	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	public Date getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	public Long getIsApproved() {
		return isApproved;
	}

	public void setIsApproved(Long isApproved) {
		this.isApproved = isApproved;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getApprovalDate() {
		return approvalDate;
	}

	public void setApprovalDate(Date approvalDate) {
		this.approvalDate = approvalDate;
	}

	public String getsReportDate() {
		return sReportDate;
	}

	public void setsReportDate(String sReportDate) {
		this.sReportDate = sReportDate;
	}

	public String getsReceivedDate() {
		return sReceivedDate;
	}

	public void setsReceivedDate(String sReceivedDate) {
		this.sReceivedDate = sReceivedDate;
	}

	public String getsApprovalDate() {
		return sApprovalDate;
	}

	public void setsApprovalDate(String sApprovalDate) {
		this.sApprovalDate = sApprovalDate;
	}

	@Override
	public String toString() {
		return "ApprovalItem{" +
				"id=" + id +
				", batchId=" + batchId +
				", userId=" + userId +
				", respondentId=" + respondentId +
				", respName='" + respName + '\'' +
				", bin='" + bin + '\'' +
				", entityId=" + entityId +
				", reportDate=" + reportDate +
				", receivedDate=" + receivedDate +
				", isApproved=" + isApproved +
				", status='" + status + '\'' +
				", formName='" + formName + '\'' +
				", title='" + title + '\'' +
				", approvalDate=" + approvalDate +
				", sReportDate='" + sReportDate + '\'' +
				", sReceivedDate='" + sReceivedDate + '\'' +
				", sApprovalDate='" + sApprovalDate + '\'' +
				'}';
	}
}
