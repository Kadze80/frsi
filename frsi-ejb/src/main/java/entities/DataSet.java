package entities;

import java.io.Serializable;
import java.util.*;

/**
 * Created by nuriddin on 10/21/16.
 */
public class DataSet implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<Integer, DataColumn> columns = new HashMap<Integer, DataColumn>();
    private List<DataRecord> records = new ArrayList<DataRecord>();

    private String textResults;
    private long executionTimeMillis;
    private int affectedRowsCount;
    private int rowsCount;
    private String exceptionMessage;
    private int sqlErrorPosition = -1;

    public void addColumn(DataColumn column) {
        columns.put(column.getIndex(), column);
    }

    public SortedSet<DataColumn> getColumns() {
        return new TreeSet<DataColumn>(columns.values());
    }

    public void addRecord(DataRecord rec) {
        records.add(rec);
    }

    public List<DataRecord> getRecords() {
        return new ArrayList<DataRecord>(records);
    }

    public String getTextResults() {
        return textResults;
    }

    public void setTextResults(String textResults) {
        this.textResults = textResults;
    }

    public long getExecutionTimeMillis() {
        return executionTimeMillis;
    }

    public void setExecutionTimeMillis(long executionTimeMillis) {
        this.executionTimeMillis = executionTimeMillis;
    }

    public int getAffectedRowsCount() {
        return affectedRowsCount;
    }

    public void setAffectedRowsCount(int affectedRowsCount) {
        this.affectedRowsCount = affectedRowsCount;
    }

    public int getRowsCount() {
        return rowsCount;
    }

    public void setRowsCount(int rowsCount) {
        this.rowsCount = rowsCount;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public int getSqlErrorPosition() {
        return sqlErrorPosition;
    }

    public void setSqlErrorPosition(int sqlErrorPosition) {
        this.sqlErrorPosition = sqlErrorPosition;
    }
}
