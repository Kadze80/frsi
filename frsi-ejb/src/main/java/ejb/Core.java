package ejb;

import entities.Report;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Core interface
 *
 * @author Ardak Saduakassov
 */
public interface Core {

    void init();

    // COMMON METHODS
    Connection getConnection() throws SQLException;
    Long getOracleSequenceNextValue(String sequenceName);
    String getOracleDate(Date date);
    String getOracleTimestamp(Date date);

    // REPORTS
    // List<Report> getReportsByFilters(String idn, Date fromReportDate, Date toReportDate, String statusCode);

    // TEST
    String getTestMessage();
    void throwTestException();
}
