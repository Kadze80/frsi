package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Nuriddin.Baideuov on 15.06.2015.
 */
public class ControlResultKey implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long crosscheckItemRecId;
    private Date reportDate;
    private String idn;
    public ControlResultKey() {
    }

    public ControlResultKey(ControlResult controlResult) {
        crosscheckItemRecId = controlResult.getCrosscheckItemRecId();
        reportDate = controlResult.getReportDate();
        idn = controlResult.getIdn();
    }

    public ControlResultKey(Long crosscheckItemRecId, Date reportDate, String idn) {
        this.crosscheckItemRecId = crosscheckItemRecId;
        this.reportDate = reportDate;
        this.idn = idn;
    }

    public Long getCrosscheckItemRecId() {
        return crosscheckItemRecId;
    }

    public void setCrosscheckItemRecId(Long crosscheckItemRecId) {
        this.crosscheckItemRecId = crosscheckItemRecId;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ControlResultKey that = (ControlResultKey) o;

        if (!crosscheckItemRecId.equals(that.crosscheckItemRecId)) return false;
        if (!idn.equals(that.idn)) return false;
        if (!reportDate.equals(that.reportDate)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = crosscheckItemRecId.hashCode();
        result = 31 * result + reportDate.hashCode();
        result = 31 * result + idn.hashCode();
        return result;
    }
}
