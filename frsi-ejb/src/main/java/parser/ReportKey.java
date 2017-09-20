package parser;

import util.Convert;

import java.util.Date;

/**
 * Created by nuriddin on 9/1/16.
 */
public class ReportKey {
    private String formCode;
    private String bin;
    private Date reportDate;
    private String reportDateString;

    public ReportKey() {
    }

    public ReportKey(String formCode, String bin, Date reportDate) {
        this.formCode = formCode;
        this.bin = bin;
        this.reportDate = reportDate;
        setReportDate(reportDate);
    }

    public ReportKey(ReportKey k) {
        this.formCode = k.getFormCode();
        this.bin = k.getBin();
        setReportDate(k.getReportDate());
    }

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
        this.reportDateString = Convert.dateFormatRus.format(reportDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportKey reportKey = (ReportKey) o;

        if (!formCode.equals(reportKey.formCode)) return false;
        if (!bin.equals(reportKey.bin)) return false;
        return reportDateString.equals(reportKey.reportDateString);

    }

    @Override
    public int hashCode() {
        int result = formCode.hashCode();
        result = 31 * result + bin.hashCode();
        result = 31 * result + reportDateString.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ReportKey{" +
                "formCode='" + formCode + '\'' +
                ", bin='" + bin + '\'' +
                ", reportDate=" + reportDate +
                '}';
    }
}
