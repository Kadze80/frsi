package mb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import entities.*;
import org.apache.log4j.Logger;
import util.DataType;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.*;

@SessionScoped
@ManagedBean
public class UnionPickerBean {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    private List<InputSelectColumn> columns;
    private List<InputSelectRecord> records;
    private String filterName;
    private String filterIdn;
    private int filterType;
    private Boolean searchInTax;
    private RefUnionPersonItem filter = new RefUnionPersonItem();
    private int offset = 0;
    private int limit = 20;
    private int count;
    private InputSelectViewModel viewModel;
    private Date reportDate;
    private String selectedValue;

    private String errorMessage;

    @PostConstruct
    public void init() {
        /*records = new ArrayList<InputSelectRecord>();
        columns = new ArrayList<InputSelectColumn>();
        columns.add(new InputSelectColumn("code", "Код"));
        columns.add(new InputSelectColumn("name_ru", "Наименование"));*/
        /*String vm = "{\n" +
                "    \"reportDate\":\"\",\n" +
                "    \"viewModel\":{\n" +
                "\t\t\t\t\"name\":\"ref_vid_oper_vm\",\n" +
                "\t\t\t\t\t\"columns\":[\n" +
                "\t\t\t\t\t{\"name\":\"rec_id\", \"key\":true, \"hidden\":true},\n" +
                "\t\t\t\t\t{\"name\":\"code\", \"title\":\"Код\"},\n" +
                "\t\t\t\t\t{\"name\":\"name_ru\", \"title\":\"Наименование\"}\n" +
                "\t\t\t\t],\n" +
                "\t\t\t\t\"sortFields\":[{\"name\":\"code\"}, {\"name\":\"name_ru\"}]\n" +
                "\t\t\t}\n" +
                "}";*/
    }

    // At least dummy preRender event listener required to properly redirect to error pages when exceptions occur in PostConstruct methods.
    public void preRender() {
        boolean isPostBack = FacesContext.getCurrentInstance().isPostback();
        boolean isAjax = FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest();
        if (isPostBack || isAjax) return;
        try {
        } catch (Exception e) {
            applicationBean.redirectToErrorPage(e);
        }
    }

    public void onShow() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String param = params.get("pickParam");
        show(param);

        /*AjaxBody ajaxBody = new AjaxBody();
        ajaxBody.setReportDate(new Date());
        InputSelectViewModel vm = new InputSelectViewModel();
        vm.setColumns(new ArrayList<InputSelectColumn>());
        vm.setSortFields(new ArrayList<SortField>());
        vm.setName("REF_SECURITIES");
        vm.getColumns().add(new InputSelectColumn("rec_id", "rec_id", true, true, null));
        vm.getColumns().add(new InputSelectColumn("code", "Код"));
        vm.getColumns().add(new InputSelectColumn("name_ru", "Наименование"));
        vm.getSortFields().add(new SortField("code", false));
        vm.getSortFields().add(new SortField("name_ru", false));
        ajaxBody.setViewModel(vm);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
        String str = gson.toJson(ajaxBody, AjaxBody.class);
        show(str);*/
    }

    public void show(String vmStr) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
        AjaxBody ajaxBody;
        try {
            ajaxBody = gson.fromJson(vmStr, AjaxBody.class);
        } catch (JsonSyntaxException e) {
            errorMessage = e.getMessage();
            return;
        }

        viewModel = ajaxBody.viewModel;
        reportDate = ajaxBody.reportDate;
        filterName = null;
        filterIdn = null;
        filterType = 0;
        filter = new RefUnionPersonItem();

        offset = 0;

        columns = viewModel.getColumns();

        load();
    }

    private void load() {
        List<RefUnionPersonItem> personItems;
        if(searchInTax){
            count = sessionBean.getReference().getRefLPTaxItemsCount(reportDate, filter);
            personItems = sessionBean.getReference().getRefLPTaxItemsByFilterPage(reportDate, filter, offset, limit);
        }else {
            count = sessionBean.getReference().getRefUnionPersonItemsCount(reportDate, filter);
            personItems = sessionBean.getReference().getRefUnionPersonItemsByFilterPage(reportDate, filter, offset, limit);
        }
        records = toRecords(personItems);
    }

    private List<InputSelectRecord> toRecords(List<RefUnionPersonItem> personItems) {
        List<InputSelectRecord> records = new ArrayList<InputSelectRecord>();
        for (RefUnionPersonItem p : personItems) {
            InputSelectRecord rec = new InputSelectRecord(p.getId());
            rec.put("rec_id", Variant.createNumber0(p.getId()));
            rec.put("name_ru", Variant.createString(p.getNameRu()));
            rec.put("idn", Variant.createString(p.getIdn()));
            rec.put("type_name", Variant.createString(p.getType() == 1 ? "Юр. лицо" : "Физ. лицо"));
            rec.put("is_tax", Variant.createBoolean(p.isTax()));
            records.add(rec);
        }
        return records;
    }

    public void filter() {
        if (filterIdn != null && !filterIdn.trim().isEmpty()) {
            filter.setIdn(filterIdn.trim());
        } else {
            filter.setIdn(null);
        }
        if (filterName != null && !filterName.trim().isEmpty()) {
            filter.setNameRu(filterName);
        } else {
            filter.setNameRu(null);
        }
        if (filterType == 0) {
            filter.setType(null);
        } else {
            filter.setType(filterType);
        }
        offset = 0;
        load();
    }

    public void prevPage() {
        if(searchInTax) {
            count = sessionBean.getReference().getRefLPTaxItemsCount(reportDate, filter);
        }else {
            count = sessionBean.getReference().getRefUnionPersonItemsCount(reportDate, filter);
        }
        offset -= limit;
        if (offset < 0)
            offset = 0;

        List<RefUnionPersonItem> personItems;

        if(searchInTax){
            personItems = sessionBean.getReference().getRefLPTaxItemsByFilterPage(reportDate, filter, offset, limit);
        }else {
            personItems = sessionBean.getReference().getRefUnionPersonItemsByFilterPage(reportDate, filter, offset, limit);
        }
        records = toRecords(personItems);
    }

    public void nextPage() {
        if(searchInTax){
            count = sessionBean.getReference().getRefLPTaxItemsCount(reportDate, filter);
        }else {
            count = sessionBean.getReference().getRefUnionPersonItemsCount(reportDate, filter);
        }

        if ((offset + limit) < count) {
            offset += limit;
        }

        List<RefUnionPersonItem> personItems;
        if(searchInTax){
            personItems = sessionBean.getReference().getRefLPTaxItemsByFilterPage(reportDate, filter, offset, limit);
        }else {
            personItems = sessionBean.getReference().getRefUnionPersonItemsByFilterPage(reportDate, filter, offset, limit);
        }

        records = toRecords(personItems);
    }

    public void passValue() {
        selectedValue = "";
        InputSelectRecord foundRec = null;
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String recIdStr = params.get("selectedRecId");
        if (recIdStr != null && !recIdStr.isEmpty()) {
            long recId;
            try {
                recId = Long.parseLong(recIdStr);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Can't parse Long from " + recIdStr);
            }
            for (InputSelectRecord record : records) {
                if (recId == record.getRecId()) {
                    foundRec = record;
                    break;
                }
            }
        } else {
            foundRec = new InputSelectRecord(-1);
        }
        if (foundRec != null) {
            if(foundRec.get("is_tax").getBoolValue()) {
                Long newRecId = createNewPersonFromTax(foundRec.get("rec_id").getLngValue(), sessionBean.abstractUser.getId(), sessionBean.abstractUser.getLocation());
                foundRec.put("rec_id", Variant.createNumber0(newRecId));
            }
            selectedValue = recordToJson(foundRec);
        }
    }

    public Long createNewPersonFromTax(Long recId, Long userId, String userLocation){
        return sessionBean.getReference().createNewPersonFromTax(recId, userId, userLocation);
    }

    public String recordToJson(InputSelectRecord record) {
        Gson gson = new Gson();
        Map<String, String> map = new HashMap<String, String>();
        for (InputSelectColumn col : viewModel.getColumns()) {
            Variant cellValue = record == null ? null : record.getData().get(col.getName());
            String key;
            if (col.getTargetColumnName() != null && !col.getTargetColumnName().isEmpty()) {
                key = col.getTargetColumnName();
            } else {
                key = col.getName();
            }
            String cellStrValue;
            if (cellValue == null) {
                cellStrValue = "";
            } else {
                try {
                    cellStrValue = DataType.variantToString(cellValue, col.getValueType());
                } catch (Exception e) {
                    throw new IllegalStateException(e.getMessage());
                }
            }
            map.put(key, cellStrValue);
        }
        return gson.toJson(map);
    }

    public int getFrom() {
        return offset + 1;
    }

    public int getTo() {
        int to = offset + limit;
        if (to > count) to = count;
        return to;
    }

    public String format(InputSelectColumn column, InputSelectRecord record) {
        Variant variant = record.get(column.getName());
        try {
            return DataType.variantToString(variant, column.getValueType());
        } catch (Exception e) {
            return "ОШИБКА " + e.getMessage();
        }
    }

    public List<InputSelectColumn> getColumns() {
        return columns;
    }

    public List<InputSelectRecord> getRecords() {
        return records;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterIdn() {
        return filterIdn;
    }

    public void setFilterIdn(String filterIdn) {
        this.filterIdn = filterIdn;
    }

    public int getFilterType() {
        return filterType;
    }

    public void setFilterType(int filterType) {
        this.filterType = filterType;
    }

    public String getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    static public class AjaxBody implements Serializable {
        private InputSelectViewModel viewModel;
        private Date reportDate;
        private Filters filters;

        public InputSelectViewModel getViewModel() {
            return viewModel;
        }

        public void setViewModel(InputSelectViewModel viewModel) {
            this.viewModel = viewModel;
        }

        public Date getReportDate() {
            return reportDate;
        }

        public void setReportDate(Date reportDate) {
            this.reportDate = reportDate;
        }

        public Filters getFilters() {
            return filters;
        }

        public void setFilters(Filters filters) {
            this.filters = filters;
        }
    }

    public Boolean getSearchInTax() {
        return searchInTax;
    }

    public void setSearchInTax(Boolean searchInTax) {
        this.searchInTax = searchInTax;
    }
}
