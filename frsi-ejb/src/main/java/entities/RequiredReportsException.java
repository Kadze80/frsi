package entities;

import parser.ReportKey;

import java.util.List;
import java.util.Map;

public class RequiredReportsException extends Exception {
    private List<RequiredReport> reports;

    public RequiredReportsException(List<RequiredReport> reports) {
        this.reports = reports;
    }

    public List<RequiredReport> getReports() {
        return reports;
    }
}
