package entities.Reference;

import entities.RefIssuersItem;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import java.util.*;

/**
 * Lazy data loading is used as a walkaround to Primefaces DataTable's bug to force sorting/filtering data after ajax update.
 * Additionally, it implements custom filters.
 *
 * @author Ayupov Bakhtiyar
 */
public class LdmRefIssuers extends LazyDataModel<RefIssuersItem> {

    private List<RefIssuersItem> srcCollection;
    private List<RefIssuersItem> dstCollection = new ArrayList<RefIssuersItem>();

    public LdmRefIssuers() {
    }

    public LdmRefIssuers(List<RefIssuersItem> srcCollection) {
        this.srcCollection = srcCollection;
    }

    public void setSrcCollection(List<RefIssuersItem> srcCollection) {
        this.srcCollection = srcCollection;
    }

    @Override
    public Object getRowKey(RefIssuersItem object) {
        return object.getId();
    }

    @Override
    public RefIssuersItem getRowData(String rowKey) {
        Long id = Long.valueOf(rowKey);
        for (RefIssuersItem item : srcCollection)
            if (id.equals(item.getId())) return item;
        return null;
    }

    @Override
    public List<RefIssuersItem> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        dstCollection.clear();

        // Filter
        if (srcCollection != null) {
            for (RefIssuersItem item : srcCollection) {
                boolean match = true;

                if (filters != null) { // Currently, there are no filters used in the dataTable. Instead, search parameters used to obtain source collection.
                    for (Iterator<String> it = filters.keySet().iterator(); it.hasNext(); ) {
                        try {
                            String filterProperty = it.next();
                            Object filterValue = filters.get(filterProperty);
                            String fieldValue = String.valueOf(item.getClass().getField(filterProperty).get(item));

                            if (filterValue == null || fieldValue.startsWith(filterValue.toString())) {
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
        if(sortField != null) Collections.sort(dstCollection, new CmpRefIssuers(sortField, sortOrder));

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
