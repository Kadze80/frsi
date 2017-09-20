package entities;

import java.io.Serializable;

/**
 * Entity
 *
 * @author Ardak Saduakassov
 * @deprecated Use ReportListItem instead.
 */
public class LogListItem implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	private long userId;
	private String userScreenName;
	private String userFullName;
	private String formLanguageCode;
	private String formName;
	private String formTitle;
	private String reportDate;
	private String saveDate;
	private String deliveryWay;
	private String status;
    private String statusName;
	private String statusDate;
    private String hash;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUserScreenName() {
		return userScreenName;
	}

	public void setUserScreenName(String userScreenName) {
		this.userScreenName = userScreenName;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

	public String getFormLanguageCode() {
		return formLanguageCode;
	}

	public void setFormLanguageCode(String formLanguageCode) {
		this.formLanguageCode = formLanguageCode;
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public String getFormTitle() {
		return formTitle;
	}

	public void setFormTitle(String formTitle) {
		this.formTitle = formTitle;
	}

	public String getReportDate() {
		return reportDate;
	}

	public void setReportDate(String reportDate) {
		this.reportDate = reportDate;
	}

	public String getSaveDate() {
		return saveDate;
	}

	public void setSaveDate(String saveDate) {
		this.saveDate = saveDate;
	}

	public String getDeliveryWay() {
		return deliveryWay;
	}

	public void setDeliveryWay(String deliveryWay) {
		this.deliveryWay = deliveryWay;
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

	public String getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(String statusDate) {
		this.statusDate = statusDate;
	}

	public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LogListItem that = (LogListItem) o;

		if (id != that.id) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (int) (id ^ (id >>> 32));
	}
}
