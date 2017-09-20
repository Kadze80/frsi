package dataform;

import java.util.Date;

/**
 * Created by Nuriddin.Baideuov on 04.08.2015.
 */
public class NoReportDataError extends Exception {
    private Date reportDate;
    private String formCode;
    private String respondentIdn;

    public NoReportDataError() {
    }

    public NoReportDataError(String message) {
        super(message);
    }

    public NoReportDataError(Date reportDate, String formCode, String respondentIdn) {
        this.reportDate = reportDate;
        this.formCode = formCode;
        this.respondentIdn = respondentIdn;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public String getFormCode() {
        return formCode;
    }

    public String getRespondentIdn() {
        return respondentIdn;
    }
}
