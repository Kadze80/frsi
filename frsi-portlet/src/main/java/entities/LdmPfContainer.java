package entities;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import java.lang.reflect.Field;
import java.util.*;

public class LdmPfContainer extends LazyDataModel<PermissionFormContainer> {

    private List<PermissionFormContainer> srcCollection;
    private List<PermissionFormContainer> dstCollection = new ArrayList<PermissionFormContainer>();

    public LdmPfContainer() {
    }

    public LdmPfContainer(List<PermissionFormContainer> srcCollection) {
        this.srcCollection = srcCollection;
    }

    public List<PermissionFormContainer> getSrcCollection() {
        return srcCollection;
    }

    public void setSrcCollection(List<PermissionFormContainer> srcCollection) {
        this.srcCollection = srcCollection;
    }

    @Override
    public Object getRowKey(PermissionFormContainer object) {
        return object.getId();
    }

    @Override
    public PermissionFormContainer getRowData(String rowKey) {
        Long id = Long.valueOf(rowKey);
        for (PermissionFormContainer item : srcCollection)
            if (id.equals(item.getId())) return item;
        return null;
    }

    @Override
    public List<PermissionFormContainer> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        dstCollection.clear();

        // Filter
        if (srcCollection != null) {
            for (PermissionFormContainer item : srcCollection) {
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

                            // String filters
                            boolean matchFormCode = filterProperty.equals("formCode") && strFieldValue.toLowerCase().contains(filterValueStr.toLowerCase());
                            boolean matchFormName = filterProperty.equals("formName") && strFieldValue.toLowerCase().contains(filterValueStr.toLowerCase());

                            if (filterValue == null || matchFormCode || matchFormName ||
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
        if(sortField != null) Collections.sort(dstCollection, new CmpPfContainer(sortField, sortOrder));

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
