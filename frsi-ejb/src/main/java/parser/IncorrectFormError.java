package parser;

import java.util.Date;

/**
 * Created by nuriddin on 12/7/16.
 */
public class IncorrectFormError extends Exception {
    private String formCode;
    private Date reportDate;

    public IncorrectFormError(String formCode, Date reportDate) {
        this.formCode = formCode;
        this.reportDate = reportDate;
    }

    public IncorrectFormError(String message, String formCode, Date reportDate) {
        super(message);
        this.formCode = formCode;
        this.reportDate = reportDate;
    }

    public String getFormCode() {
        return formCode;
    }

    public Date getReportDate() {
        return reportDate;
    }
}
