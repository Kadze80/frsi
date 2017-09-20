package entities.Reference;

import entities.RefIssuersItem;
import org.primefaces.model.SortOrder;

import java.lang.reflect.Field;
import java.util.Comparator;

/**
 * Entity comparator
 *
 * @author Ayupov Bakhtiyar
 */
public class CmpRefIssuers implements Comparator<RefIssuersItem> {
    private String sortField;
    private SortOrder sortOrder;

    public CmpRefIssuers(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(RefIssuersItem o1, RefIssuersItem o2) {
        int result = 0;
        try {
            Field sortField = RefIssuersItem.class.getDeclaredField(this.sortField);
            sortField.setAccessible(true);

            Object value1 = sortField.get(o1);
            Object value2 = sortField.get(o2);

            result = value1 == null ? (value2 == null ? 0 : -1) : (value2 == null ? 1 : ((Comparable)value1).compareTo(value2));
            return sortOrder.equals(SortOrder.ASCENDING) ? result : -result;
        }
        catch(Exception e) {
            return result;
        }
    }
}
