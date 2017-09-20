package entities;

import java.io.Serializable;
import java.util.Date;

public class OutDataItem implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;
	private Long userId;
	private Date reportDate;
	private Date receivedDate;
	private Long isApproved;
	private String formName;
	private String title;
	private String note;
    private Long couchbase_id;
    private Date approvalDate;
    private String[] respondentsName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
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

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

    public Long getCouchbase_id() {
        return couchbase_id;
    }

    public void setCouchbase_id(Long couchbase_id) {
        this.couchbase_id = couchbase_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }

	public String[] getRespondentsName() {
		return respondentsName;
	}

	public void setRespondentsName(String[] respondentsName) {
		this.respondentsName = respondentsName;
	}

	@Override
	public String toString() {
		return "OutDataItem{" +
				"id=" + id +
				", userId=" + userId +
				", reportDate=" + reportDate +
				", receivedDate=" + receivedDate +
				", isApproved=" + isApproved +
				", formName='" + formName + '\'' +
				", title='" + title + '\'' +
				", note='" + note + '\'' +
				", couchbase_id=" + couchbase_id +
                ", approvalDate=" + approvalDate +
				'}';
	}
}
