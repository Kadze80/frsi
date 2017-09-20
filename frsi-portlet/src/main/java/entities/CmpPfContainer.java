package entities;

import org.primefaces.model.SortOrder;

import java.lang.reflect.Field;
import java.util.Comparator;

/**
 * Entity comparator
 *
 * @author Nuriddin Baideuov
 */
public class CmpPfContainer implements Comparator<PermissionFormContainer> {

    private String sortField;
    private SortOrder sortOrder;

    public CmpPfContainer(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(PermissionFormContainer o1, PermissionFormContainer o2) {
        int result = 0;
        try {
            Field sortField = PermissionFormContainer.class.getDeclaredField(this.sortField);
            sortField.setAccessible(true);

            Object value1 = sortField.get(o1);
            Object value2 = sortField.get(o2);

            if (value1 == null) {
                result = value2 == null ? 0 : -1;
            } else {
                if (value2 == null) result = 1;
                else {
                    if(value1 instanceof String)
                        value1 = ((String) value1).toLowerCase();
                    if(value2 instanceof String)
                        value2 = ((String) value2).toLowerCase();
                    result = ((Comparable) value1).compareTo(value2);
                }
            }
            return sortOrder.equals(SortOrder.ASCENDING) ? result : -result;
        }
        catch(Exception e) {
            return result;
        }
    }
}
