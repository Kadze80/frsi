package entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Entity
 *
 * @author Ardak Saduakassov
 */
public class Report implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String idn;
    private String idnChild;
    private Date reportDate;
    private String formCode;

    private List<ReportHistory> reportHistory;
    private List<ReportStatus> reportStatusHistory;

    public Report() {

    }

    public Report(String idn, Date reportDate, String formCode) {
        this.idn = idn;
        this.reportDate = reportDate;
        this.formCode = formCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        if (id != null ? !id.equals(report.id) : report.id != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // region Getter and Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public List<ReportHistory> getReportHistory() {
        return reportHistory;
    }

    public void setReportHistory(List<ReportHistory> reportHistory) {
        this.reportHistory = reportHistory;
    }

    public List<ReportStatus> getReportStatusHistory() {
        return reportStatusHistory;
    }

    public void setReportStatusHistory(List<ReportStatus> reportStatusHistory) {
        this.reportStatusHistory = reportStatusHistory;
    }

    public String getIdnChild() {
        return idnChild;
    }

    public void setIdnChild(String idnChild) {
        this.idnChild = idnChild;
    }
    // endregion
}
