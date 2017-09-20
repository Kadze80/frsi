package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DataBase utility methods
 *
 * @author Ardak Saduakassov
 */
public class DbUtil {

    public static void closeStatement(Statement stmt) {
        closeConnection(null, stmt, null, null);
    }

    public static void closeConnection(Connection con) {
        closeConnection(con, null, null, null);
    }

    public static void closeConnection(Connection con, Statement stmt) {
        closeConnection(con, stmt, null, null);
    }

    public static void closeConnection(Connection con, Statement stmt, ResultSet rs){
        closeConnection(con, stmt, rs, null);
    }

    public static void closeConnection(Connection con, Statement stmt, ResultSet rs, Statement ocs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (ocs != null) {
            try {
                ocs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
