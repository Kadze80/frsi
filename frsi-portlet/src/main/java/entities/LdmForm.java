package entities;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Lazy data loading is used as a walkaround to Primefaces DataTable's bug to force sorting/filtering data after ajax update.
 * Additionally, it implements custom filters.
 *
 * @author Ardak Saduakassov
 */
public class LdmForm extends LazyDataModel<Form> {

    private List<Form> srcCollection;
    private List<Form> dstCollection = new ArrayList<Form>();
    private Date beginDate;

    public LdmForm() {
    }

    public LdmForm(List<Form> srcCollection) {
        this.srcCollection = srcCollection;
    }

    public void setSrcCollection(List<Form> srcCollection) {
        this.srcCollection = srcCollection;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public Object getRowKey(Form object) {
        return object.getId();
    }

    @Override
    public Form getRowData(String rowKey) {
        Long id = Long.valueOf(rowKey);
        for (Form item : srcCollection)
            if (id.equals(item.getId())) return item;
        return null;
    }

    @Override
    public List<Form> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        dstCollection.clear();

        // Filter
        if (srcCollection != null) {
            for (Form item : srcCollection) {
                boolean match = true;
                if (filters != null) {
                    for (Iterator<String> it = filters.keySet().iterator(); it.hasNext(); ) {
                        try {
                            String filterProperty = it.next();
                            Object filterValue = filters.get(filterProperty);
                            String filterValueStr = filterValue.toString();

                            Field field = item.getClass().getDeclaredField(filterProperty);
                            field.setAccessible(true);
                            Object fieldValue = field.get(item);
                            String strFieldValue = String.valueOf(fieldValue);

                            // date filters
                            boolean matchBeginDate = filterProperty.equals("beginDate") && ((Date) fieldValue).equals(beginDate);

                            // case insensitive filterMatchMode="contains"
                            boolean matchContainsString = (filterProperty.equals("code") || filterProperty.equals("languageCode") || filterProperty.equals("name"))
                                    && strFieldValue.toLowerCase().contains(filterValueStr.toLowerCase());

                            if (filterValue == null || matchBeginDate || matchContainsString ||
                                    strFieldValue.startsWith(filterValueStr)) {
                                match = true;
                            } else {
                                match = false;
                                break;
                            }
                        } catch (Exception e) {
                            match = false;
                        }
                    }
                }
                if (match) dstCollection.add(item);
            }
        }
        // Sort
        if(sortField != null) Collections.sort(dstCollection, new CmpForm(sortField, sortOrder));

        // RowCount
        int dataSize = dstCollection.size();
        this.setRowCount(dataSize);

        // Paginate
        if(0 < pageSize && pageSize < dataSize) {
            try {
                return dstCollection.subList(first, first + pageSize);
            }
            catch(IndexOutOfBoundsException e) {
                return dstCollection.subList(first, first + (dataSize % pageSize));
            }
        }
        else {
            return dstCollection;
        }
    }
}
