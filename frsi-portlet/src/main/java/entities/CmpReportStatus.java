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
public class CmpReportStatus implements Comparator<ReportStatus> {

    private String sortField;
    private SortOrder sortOrder;

    public CmpReportStatus(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(ReportStatus o1, ReportStatus o2) {
        int result = 0;
        try {
            Field sortField = ReportStatus.class.getDeclaredField(this.sortField);
            sortField.setAccessible(true);

            Object value1 = sortField.get(o1);
            Object value2 = sortField.get(o2);

            if (value1 == null) {
                result = value2 == null ? 0 : -1;
            } else {
                if (value2 == null) result = 1;
                else result = ((Comparable) value1).compareTo(value2);
            }
            return sortOrder.equals(SortOrder.ASCENDING) ? result : -result;
        }
        catch(Exception e) {
            return result;
        }
    }
}
