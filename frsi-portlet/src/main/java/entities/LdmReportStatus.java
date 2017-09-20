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
public class LdmReportStatus extends LazyDataModel<ReportStatus> {

    private List<ReportStatus> srcCollection;
    private List<ReportStatus> dstCollection = new ArrayList<ReportStatus>();
    private Boolean draftStatusesHidden;

    public LdmReportStatus() {
    }

    public LdmReportStatus(List<ReportStatus> srcCollection) {
        this.srcCollection = srcCollection;
    }

    public void setSrcCollection(List<ReportStatus> srcCollection) {
        this.srcCollection = srcCollection;
    }

    public void setDraftStatusesHidden(Boolean draftStatusesHidden) {
        this.draftStatusesHidden = draftStatusesHidden;
    }

    @Override
    public Object getRowKey(ReportStatus object) {
        return super.getRowKey(object);
    }

    @Override
    public ReportStatus getRowData(String rowKey) {
        return super.getRowData(rowKey);
    }

    @Override
    public List<ReportStatus> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        dstCollection.clear();

        // Filter
        if (srcCollection != null) {
            for (ReportStatus item : srcCollection) {
                boolean match = true;

                if (draftStatusesHidden != null && draftStatusesHidden.booleanValue()) {
                    try {
                        Field field = item.getClass().getDeclaredField("statusCode");
                        field.setAccessible(true);
                        Object fieldValue = field.get(item);
                        String strFieldValue = String.valueOf(fieldValue);
                        if (strFieldValue.equals("DRAFT")) match = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (filters != null) {
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
        if(sortField != null) Collections.sort(dstCollection, new CmpReportStatus(sortField, sortOrder));

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
