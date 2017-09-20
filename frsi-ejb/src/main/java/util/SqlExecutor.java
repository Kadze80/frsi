package util;

import entities.DataColumn;
import entities.DataRecord;
import entities.DataSet;
import entities.QueryType;
import oracle.jdbc.OracleTypes;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.Date;
import java.util.HashMap;

/**
 * @author Aidar.Myrzahanov
 */
public class SqlExecutor {

    private static final String FETCH_EXPLAIN_PLAN_QUERY = "select plan_table_output from table(dbms_xplan.display())";

    private static final String LAST_ERROR_POSITION_QUERY = "DECLARE\n"
            + "  c        INTEGER := DBMS_SQL.open_cursor();\n"
            + "  errorpos integer := -1;\n"
            + "BEGIN\n"
            + "  BEGIN\n"
            + "    DBMS_SQL.parse(c, :sqltext, DBMS_SQL.native);\n"
            + "  EXCEPTION\n"
            + "    WHEN OTHERS THEN\n"
            + "      errorpos := DBMS_SQL.LAST_ERROR_POSITION();\n"
            + "  END;\n"
            + "  :errorpos := errorpos;\n"
            + "  DBMS_SQL.close_cursor(c);\n"
            + "END;";

    private QueryType queryType;

    private final int limit = 1000;
    private final int timeout = 300;

    private DataSet dataSet;

    public final Logger logger = Logger.getLogger(SqlExecutor.class);

    private void saveException(Exception ex) {
        logger.error(null, ex);
        dataSet.setExceptionMessage(ex.getMessage());
    }

    public void runQuery(String query, Connection conn) {
        dataSet = new DataSet();
        if (!isQuerySafe(query)) {
            return;
        }
        Statement statement = null;
        ResultSet resultSet = null;
        dataSet.setExceptionMessage(null);
        dataSet.setSqlErrorPosition(-1);
        try {
            if (conn == null) {
                return;
            }
            statement = conn.createStatement();
            statement.setMaxRows(limit);
            statement.setQueryTimeout(timeout);

            long timeBeforeQueryMillis = System.currentTimeMillis();
            if (queryType == QueryType.SELECT) {
                resultSet = statement.executeQuery(query);
                parseResultSet(resultSet);
            } else if (queryType == QueryType.INSERT_OR_UPDATE) {
                int affectedRowsCount = statement.executeUpdate(query);
                dataSet.setAffectedRowsCount(affectedRowsCount);
            } else if (queryType == QueryType.EXPLAIN_PLAN) {
                statement.execute("EXPLAIN PLAN FOR " + query);
                resultSet = statement.executeQuery(FETCH_EXPLAIN_PLAN_QUERY);
                parseResultSet(resultSet);
            }
            long timeAfterQueryMillis = System.currentTimeMillis();
            long executionTimeMillis = timeAfterQueryMillis - timeBeforeQueryMillis;
            dataSet.setExecutionTimeMillis(executionTimeMillis);
        } catch (SQLException ex) {
            int sqlErrorPosition = retrieveErrorPosition(conn, query);
            dataSet.setSqlErrorPosition(sqlErrorPosition);
            saveException(ex);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException sqle) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sqle) {
                }
            }
        }
    }

    private int retrieveErrorPosition(Connection connection, String query) {
        CallableStatement callStatement = null;
        try {
            callStatement = connection.prepareCall(LAST_ERROR_POSITION_QUERY);
            callStatement.setString(1, query);
            callStatement.registerOutParameter(2, OracleTypes.INTEGER);
            callStatement.execute();
            return callStatement.getInt(2);
        } catch (SQLException ex) {
            logger.error(null, ex);
        } finally {
            if (callStatement != null) {
                try {
                    callStatement.close();
                } catch (SQLException sqle) {
                }
            }
        }
        return -1;
    }

    private boolean isQuerySafe(String query) {
        String queryText = query.toLowerCase();
        if (queryText.contains("where")) {
            return true;
        }
        if (queryText.contains("update")) {
            dataSet.setExceptionMessage("UPDATE without WHERE");
            return false;
        }
        if (queryText.contains("delete")) {
            dataSet.setExceptionMessage("DELETE without WHERE");
            return false;
        }
        return true;
    }

    private void parseResultSet(ResultSet resultSet) throws SQLException, UnsupportedOperationException {
        StringBuilder textBuilder = new StringBuilder();
        ResultSetMetaData metaData = resultSet.getMetaData();

        int columnCount = metaData.getColumnCount();
        HashMap<String, Integer> duplicatedColumnNames = new HashMap<String, Integer>();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            Class columnClass = String.class;
            int columnType = metaData.getColumnType(columnIndex);
            if (columnType == OracleTypes.NUMBER) {
                columnClass = Double.class;
            } else if (columnType == OracleTypes.TIMESTAMP) {
                columnClass = Date.class;
            }
            String columnName = metaData.getColumnName(columnIndex);
            textBuilder.append(columnName).append("\t");
            Integer nameIndex = duplicatedColumnNames.get(columnName);
            String columnPropertyName;
            if (nameIndex == null) {
                nameIndex = 1;
                columnPropertyName = columnName;
            } else {
                columnPropertyName = columnName + nameIndex;
                nameIndex++;
            }
            duplicatedColumnNames.put(columnName, nameIndex);
            dataSet.addColumn(new DataColumn(columnIndex, columnName, columnClass));
        }
        textBuilder.append("\n");
        int rowNumber = 1;
        while (resultSet.next()) {
            DataRecord record = new DataRecord();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                if (resultSet.getObject(columnIndex) == null) {
                    record.put(columnIndex, null);
                    continue;
                }
                int columnType = metaData.getColumnType(columnIndex);
                if (columnType == OracleTypes.NUMBER) {
                    record.put(columnIndex, resultSet.getDouble(columnIndex));
                } else if (columnType == OracleTypes.TIMESTAMP) {
                    record.put(columnIndex, resultSet.getTimestamp(columnIndex));
                } else {
                    record.put(columnIndex, resultSet.getString(columnIndex));
                }
                textBuilder.append(record.get(columnIndex)).append("\t");
            }
            dataSet.addRecord(record);
            textBuilder.append("\n");
            rowNumber++;
        }
        dataSet.setRowsCount(rowNumber - 1);
        dataSet.setTextResults(textBuilder.toString());
    }

    /**
     * @param queryType the queryType to set
     */
    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public DataSet getDataSet() {
        return dataSet;
    }
}
