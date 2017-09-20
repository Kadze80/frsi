package entities;

import java.io.Serializable;
import java.util.List;

/**
 * Created by nuriddin on 6/1/16.
 */
public class InputSelectViewModel implements Serializable {

    private String name;

    private List<InputSelectColumn> columns;

    private List<SortField> sortFields;

    private String filterColumn;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<InputSelectColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<InputSelectColumn> columns) {
        this.columns = columns;
    }

    public List<SortField> getSortFields() {
        return sortFields;
    }

    public void setSortFields(List<SortField> sortFields) {
        this.sortFields = sortFields;
    }

    public String getFilterColumn() {
        return filterColumn;
    }

    public void setFilterColumn(String filterColumn) {
        this.filterColumn = filterColumn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InputSelectViewModel that = (InputSelectViewModel) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
