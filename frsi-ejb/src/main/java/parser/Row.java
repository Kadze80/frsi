package parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by nuriddin on 9/13/16.
 */
public class Row {
    private Map<String, Cell> cellMap = new HashMap<String, Cell>();

    public void addCell(Cell cell) {
        cellMap.put(cell.fieldName, cell);
    }

    public Cell getCell(String f) {
        return cellMap.get(f);
    }

    public Set<Cell> getCells() {
        return new HashSet<Cell>(cellMap.values());
    }

    @Override
    public String toString() {
        return "Row{" +
                "cellMap=" + cellMap +
                '}';
    }
}
