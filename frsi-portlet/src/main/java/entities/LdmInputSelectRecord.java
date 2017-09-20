package entities;

import ejb.Reference;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class LdmInputSelectRecord extends LazyDataModel<InputSelectRecord> {

    private List<InputSelectRecord> records;
    private Reference reference;
    private Date currentDate;
    private Filters filters;
    private InputSelectViewModel viewModel;

    public LdmInputSelectRecord(Reference reference) {
        this.reference = reference;
    }

    @Override
    public Object getRowKey(InputSelectRecord object) {
        return object.getRecId();
    }

    @Override
    public InputSelectRecord getRowData(String rowKey) {
        Long id = Long.valueOf(rowKey);
        if (records != null)
            for (InputSelectRecord item : records)
                if (id.equals(item.getRecId())) return item;
        return null;
    }

    @Override
    public List<InputSelectRecord> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        records = null;

        // RowCount
        int dataSize = reference.getReferenceItemsCount(viewModel.getName(), currentDate, this.filters);
        this.setRowCount(dataSize);

        // Paginate
        records = reference.getReferenceItemsByNameViewModelPage(viewModel.getName(), currentDate, viewModel, this.filters, first, pageSize);
        return records;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public Filters getFilters() {
        return filters;
    }

    public void setFilters(Filters filters) {
        this.filters = filters;
    }

    public InputSelectViewModel getViewModel() {
        return viewModel;
    }

    public void setViewModel(InputSelectViewModel viewModel) {
        this.viewModel = viewModel;
    }
}
