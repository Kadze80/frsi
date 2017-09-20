package entities;

import java.io.Serializable;

/**
 * Created by Baurzhan.Baisholakov on 15.05.2015.
 */
public class ExcelTable implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String key;
    private String total;
    private int position;
    private int positionKey;
    private int positionRn;
    private int levelRn;
    private ExcelCell[] columns;
    private ExcelCell[] headers;
    private int startTable;
    private int endTable;


    // region Getter and Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ExcelCell[] getColumns() {
        return columns;
    }

    public ExcelCell getColumn(int position) {
        return columns[position];
    }

    public void setColumns(ExcelCell[] columns) {
        this.columns = columns;
    }

    public ExcelCell[] getHeaders() {
        return headers;
    }

    public void setHeaders(ExcelCell[] headers) {
        this.headers = headers;
    }

    public int getPositionKey() {
        return positionKey;
    }

    public void setPositionKey(int positionKey) {
        this.positionKey = positionKey;
    }

    public int getPositionRn() {
        return positionRn;
    }

    public void setPositionRn(int positionRn) {
        this.positionRn = positionRn;
    }

    public int getLevelRn() {
        return levelRn;
    }

    public void setLevelRn(int levelRn) {
        this.levelRn = levelRn;
    }

    public int getStartTable() {
        return startTable;
    }

    public void setStartTable(int startTable) {
        this.startTable = startTable;
    }

    public int getEndTable() {
        return endTable;
    }

    public void setEndTable(int endTable) {
        this.endTable = endTable;
    }

    // endregion
}
