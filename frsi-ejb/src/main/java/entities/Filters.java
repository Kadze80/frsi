package entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Filters implements Serializable {
    private List<String> columns = new ArrayList<String>();
    private String filterValue;

    public Filters() {
    }

    public Filters(String column, String filterValue) {
        this(new String[]{column}, filterValue);
    }

    public Filters(String[] columns, String filterValue) {
        this.columns.addAll(new ArrayList<String>(Arrays.asList(columns)));
        this.filterValue = filterValue;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    @Override
    public String toString() {
        return "Filters{" +
                "columns=" + columns +
                ", filterValue='" + filterValue + '\'' +
                '}';
    }
}
