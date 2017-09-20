package entities.Reference;

import entities.RefSecuritiesItem;
import org.primefaces.model.SortOrder;

import java.lang.reflect.Field;
import java.util.Comparator;

/**
 * Entity comparator
 *
 * @author Ayupov Bakhtiyar
 */
public class CmpRefSecurities implements Comparator<RefSecuritiesItem> {
    private String sortField;
    private SortOrder sortOrder;

    public CmpRefSecurities(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(RefSecuritiesItem o1, RefSecuritiesItem o2) {
        int result = 0;
        try {
            Field sortField = RefSecuritiesItem.class.getDeclaredField(this.sortField);
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
