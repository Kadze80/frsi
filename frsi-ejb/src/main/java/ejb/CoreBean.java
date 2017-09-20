package ejb;

import com.google.gson.Gson;
import entities.Report;
import entities.ReportStatus;
import org.apache.log4j.Logger;
import util.DbUtil;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Core EJB
 *
 * Do not inject anything into this EJB in order to avoid cyclic dependencies!
 *
 * @author Ardak Saduakassov
 */
@Stateless
public class CoreBean implements CoreLocal, CoreRemote {
    private static final Logger logger = Logger.getLogger("fileLogger");
    private static final String JDBC_POOL_NAME = "jdbc/FrsiPool";
    private static final String SCHEMA = "FRSI";

    private DataSource dataSource;
    private Gson gson = new Gson();

    @PostConstruct
    @Override
    public void init() {
        Date dateStart = new Date();

        // Connect to JDBC pool
        try {
            Context context = new InitialContext();
            dataSource = (DataSource) context.lookup(JDBC_POOL_NAME);
            logger.info("Connected to " + JDBC_POOL_NAME);
        } catch (NamingException e) {
            logger.error("Could not connect to " + JDBC_POOL_NAME);
            throw new EJBException(e);
        }

        Date dateEnd = new Date();
        long duration = dateEnd.getTime() - dateStart.getTime();
        logger.debug(MessageFormat.format("t: {0}, d: {1} ms", dateStart, duration));
    }

    // COMMON METHODS

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public Long getOracleSequenceNextValue(String sequenceName) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Long result = 0L;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT " +sequenceName+".NEXTVAL AS val FROM dual");
            rs = ps.executeQuery();
            while (rs.next()) {
                result = rs.getLong("val");
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public String getOracleDate(Date date) {
        if (date == null) {
            return "TO_DATE(NULL)";
        } else {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strDate = dateFormat.format(date);
            return "TO_DATE('" + strDate + "','YYYY-MM-DD HH24:MI:SS')";
        }
    }

    @Override
    public String getOracleTimestamp(Date date) {
        if (date == null) {
            return "TO_TIMESTAMP(NULL)";
        } else {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String strDate = dateFormat.format(date);
            return "TO_TIMESTAMP('" + strDate + "','YYYY-MM-DD HH24:MI:SS.FF3')";
        }
    }

    // REPORTS

    /*@Override
    public List<Report> getReportsByFilters(String idn, Date fromReportDate, Date toReportDate, String statusCode) {
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        List<Report> result = new ArrayList<Report>();
        try {
            connection = getConnection();
            StringBuilder sbQuery = new StringBuilder();

            sbQuery.append("SELECT r.id, r.idn, r.report_date, r.form_code, f.type_code, s.status_code, s.status_date").append("\n");
            sbQuery.append("FROM reports r, forms f, report_status_history s,").append("\n");
            sbQuery.append("     (SELECT report_id, MAX(id) id FROM report_status_history GROUP BY report_id) gr").append("\n");
            sbQuery.append("WHERE 0=0").append("\n");
            sbQuery.append("  AND r.form_code = f.code").append("\n");
            sbQuery.append("  AND f.type_code = 'INPUT'").append("\n");
            sbQuery.append("  AND r.id = gr.report_id").append("\n");
            sbQuery.append("  AND gr.id = s.id").append("\n");

            if (idn != null && !idn.trim().isEmpty())
                sbQuery.append("  AND r.idn = '" + idn.trim() + "'").append("\n");

            String oracleFromReportDate = getOracleDate(fromReportDate);
            String oracleToReportDate = getOracleDate(toReportDate);
            sbQuery.append("  AND r.report_date BETWEEN NVL(TRUNC(").append(oracleFromReportDate).append("), TRUNC(r.report_date)) AND NVL(TRUNC(").append(oracleToReportDate).append("), TRUNC(r.report_date))").append("\n");

            if (statusCode != null && !statusCode.trim().isEmpty())
                sbQuery.append("  AND s.status_code = '" + statusCode.trim() + "'").append("\n");

            sbQuery.append("ORDER BY r.report_date");
            statement = connection.createStatement();
            rs = statement.executeQuery(sbQuery.toString());
            while (rs.next()) {
                Report report = new Report();
                report.setId(rs.getLong("id"));
                report.setIdn(rs.getString("idn"));
                report.setReportDate(rs.getDate("report_date"));
                report.setFormCode(rs.getString("form_code"));

                List<ReportStatus> statuses = new ArrayList<ReportStatus>();
                ReportStatus status = new ReportStatus();
                status.setStatusCode(rs.getString("status_code"));
                status.setStatusDate(rs.getDate("status_date"));
                statuses.add(status);
                report.setReportStatusHistory(statuses);

                result.add(report);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            try { rs.close(); } catch (Exception e) {}
            try { statement.close(); } catch (Exception e) {}
            try { connection.close();
            } catch (Exception e) {
            }
        }
        return result;
    }*/

    // TEST

    @Override
    public String getTestMessage() {
        return "Test message from Core EJB";
    }

    @Override
    public void throwTestException() {
        try {
            Long l = Long.parseLong("Wrong number format");
        } catch (NumberFormatException e) {
            throw new EJBException(e);
        }
    }
}
