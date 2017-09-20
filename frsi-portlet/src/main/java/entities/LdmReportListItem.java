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
public class LdmReportListItem extends LazyDataModel<ReportListItem> {

    private List<ReportListItem> srcCollection;
    private List<ReportListItem> dstCollection = new ArrayList<ReportListItem>();

    public LdmReportListItem() {
    }

    public LdmReportListItem(List<ReportListItem> srcCollection) {
        this.srcCollection = srcCollection;
    }

    public List<ReportListItem> getSrcCollection() {
        return srcCollection;
    }

    public void setSrcCollection(List<ReportListItem> srcCollection) {
        this.srcCollection = srcCollection;
    }

    @Override
    public Object getRowKey(ReportListItem object) {
        return object.getId();
    }

    @Override
    public ReportListItem getRowData(String rowKey) {
        Long id = Long.valueOf(rowKey);
        if (srcCollection != null)
            for (ReportListItem item : srcCollection)
                if (id.equals(item.getId())) return item;
        return null;
    }

    @Override
    public List<ReportListItem> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        dstCollection.clear();

        // Filter
        if (srcCollection != null) {
            for (ReportListItem item : srcCollection) {
                boolean match = true;

                if (filters != null) { // Currently, there are no filters used in the dataTable. Instead, search parameters used to obtain source collection.
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
                            boolean matchDate = false;
                            if (filterValue instanceof Date) {
                                matchDate = filterValue.equals(fieldValue);
                            }

                            // String filters
                            boolean matchFormTitle = filterProperty.equals("formName") && strFieldValue.toLowerCase().contains(filterValueStr.toLowerCase());
                            boolean matchUserInfo = filterProperty.equals("userInfo") && strFieldValue.toLowerCase().contains(filterValueStr.toLowerCase());

                            if (filterValue == null || matchDate || matchFormTitle || matchUserInfo ||
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
        if(sortField != null) Collections.sort(dstCollection, new CmpReportListItem(sortField, sortOrder));

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
