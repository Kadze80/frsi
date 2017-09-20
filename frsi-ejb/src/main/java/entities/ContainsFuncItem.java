package entities;

import ejb.KeyValue;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nuriddin on 8/7/16.
 */
public class ContainsFuncItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String[] sourceColumns;
    private String[] targetColumns;
    private String message;

    private Map<String, Map<String, KeyValue>> sourceRows = new HashMap<String, Map<String, KeyValue>>();
    private Map<String, Map<String, KeyValue>> targetRows = new HashMap<String, Map<String, KeyValue>>();

    public void addSourceValue(String rowId, String column, String key, String value) {
        addValue(rowId, column, value, sourceRows, key);
    }

    public void addTargetValue(String rowId, String column, String key, String value) {
        addValue(rowId, column, value, targetRows, key);
    }

    private void addValue(String rowId, String column, String value, Map<String, Map<String, KeyValue>> rows, String key){
        if (!rows.containsKey(rowId)) {
            rows.put(rowId, new HashMap<String, KeyValue>());
        }
        Map<String, KeyValue> cells = rows.get(rowId);
        if (cells.containsKey(column)) {
            throw new IllegalStateException(MessageFormat.format("Дублирующие данные: запись {0}, колонка {1}", rowId, column));
        }
        cells.put(column, new KeyValue(key, value));
    }

    public String[] getSourceColumns() {
        return sourceColumns;
    }

    public void setSourceColumns(String[] sourceColumns) {
        this.sourceColumns = sourceColumns;
    }

    public String[] getTargetColumns() {
        return targetColumns;
    }

    public void setTargetColumns(String[] targetColumns) {
        this.targetColumns = targetColumns;
    }

    public Map<String, Map<String, KeyValue>> getSourceRows() {
        return new HashMap<String, Map<String, KeyValue>>(sourceRows);
    }

    public Map<String, Map<String, KeyValue>> getTargetRows() {
        return new HashMap<String, Map<String, KeyValue>>(targetRows);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
