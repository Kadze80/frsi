package entities;

import ejb.Reference;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class LdmRefSecuritiesItem extends LazyDataModel<RefSecuritiesItem> {

    private List<RefSecuritiesItem> securitiesItems;
    private Reference reference;
    private Date currentDate;
    private String filter;

    public LdmRefSecuritiesItem(Reference reference) {
        this.reference = reference;
    }

    @Override
    public Object getRowKey(RefSecuritiesItem object) {
        return object.getId();
    }

    @Override
    public RefSecuritiesItem getRowData(String rowKey) {
        Long id = Long.valueOf(rowKey);
        if (securitiesItems != null)
            for (RefSecuritiesItem item : securitiesItems)
                if (id.equals(item.getId())) return item;
        return null;
    }

    @Override
    public List<RefSecuritiesItem> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        securitiesItems = null;

        // RowCount
        int dataSize = reference.getSecuritiesCount(currentDate, filter);
        this.setRowCount(dataSize);

        // Paginate
        securitiesItems = reference.getPagingSecuritiesByNin(currentDate, filter, first, pageSize);
        return securitiesItems;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
