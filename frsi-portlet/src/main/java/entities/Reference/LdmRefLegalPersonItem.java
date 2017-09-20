package entities.Reference;

import ejb.Reference;
import entities.RefLegalPersonItem;
import entities.RefSecuritiesItem;
import entities.RefUnionPersonItem;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class LdmRefLegalPersonItem extends LazyDataModel<RefLegalPersonItem> {

    private List<RefLegalPersonItem> legalPersonItems;
    private Reference reference;
    private Date currentDate;
    private String filterName;
    private String filterIdn;
    private Long refOrgTypeRecId;
    private boolean searchInTax;

    public LdmRefLegalPersonItem(Reference reference) {
        this.reference = reference;
    }

    @Override
    public Object getRowKey(RefLegalPersonItem object) {
        return object.getId();
    }

    @Override
    public RefLegalPersonItem getRowData(String rowKey) {
        Long id = Long.valueOf(rowKey);
        if (legalPersonItems != null)
            for (RefLegalPersonItem item : legalPersonItems)
                if (id.equals(item.getId())) return item;
        return null;
    }

    @Override
    public List<RefLegalPersonItem> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        legalPersonItems = null;

        // RowCount
        int dataSize = 0;
        if(searchInTax){
            RefUnionPersonItem filterU = new RefUnionPersonItem();
            filterU.setNameRu(filterName);
            filterU.setIdn(filterIdn);
            filterU.setType(1);
            dataSize = reference.getRefLPTaxItemsCount(currentDate, filterU);
            legalPersonItems = reference.getRefLPTaxByFilterPage(currentDate, filterU, first, pageSize);
        }else{
            RefLegalPersonItem filter = new RefLegalPersonItem();
            filter.setNameRu(filterName);
            filter.setIdn(filterIdn);
            filter.setRefOrgTypeRecId(refOrgTypeRecId);
            dataSize = reference.getRefLegalPersonsCount(currentDate, filter);
            // Paginate
            legalPersonItems = reference.getRefLegalPersonsByFilterPage(currentDate, filter, first, pageSize);
        }
        this.setRowCount(dataSize);

        return legalPersonItems;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public void setFilterIdn(String filterIdn) {
        this.filterIdn = filterIdn;
    }

    public void setRefOrgTypeRecId(Long refOrgTypeRecId) {
        this.refOrgTypeRecId = refOrgTypeRecId;
    }

    public void setSearchInTax(boolean searchInTax) {
        this.searchInTax = searchInTax;
    }
}
