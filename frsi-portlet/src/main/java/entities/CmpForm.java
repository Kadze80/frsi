package entities;

import org.primefaces.model.SortOrder;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Entity comparator
 *
 * @author Ardak Saduakassov
 */
public class CmpForm implements Comparator<Form> {

    private static final DateFormat dateFormatDMY = new SimpleDateFormat("dd.MM.yyyy");
    private static final DateFormat dateFormatDMYT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private String sortField;
    private SortOrder sortOrder;

    public CmpForm(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(Form o1, Form o2) {
        int result = 0;
        try {
            Field sortField = Form.class.getDeclaredField(this.sortField);
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
