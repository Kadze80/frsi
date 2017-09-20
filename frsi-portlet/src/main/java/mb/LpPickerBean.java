package mb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import entities.*;
import entities.Reference.LdmRefLegalPersonItem;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.LazyDataModel;
import util.Convert;
import util.DataType;
import util.Helper;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;

@SessionScoped
@ManagedBean
public class LpPickerBean {
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    private Date reportDate;
    private String filterName;
    private String filterIdn;
    private Long filterOrgTypeRecId;
    private RefLegalPersonItem legalPersonItem;
    private String errorMessage;

    private LazyDataModel<RefLegalPersonItem> ldmReflegalPersonItem;
    private String selectedValue;
    private DataTable dataTable;

    private Boolean searchInTax;

    @PostConstruct
    public void init() {
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
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
        LpPickerBean.AjaxBody ajaxBody;
        try {
            ajaxBody = gson.fromJson(param, LpPickerBean.AjaxBody.class);
            errorMessage = "";
        } catch (JsonSyntaxException e) {
            errorMessage = e.getMessage();
            return;
        }
        reportDate = ajaxBody.reportDate;

        String filterParam = params.get("pickFilter");
        Filter filter = null;
        if (filterParam != null) {
            gson = new GsonBuilder().registerTypeAdapter(Filter.class, new FilterAdapter()).create();
            try {
                filter = gson.fromJson(filterParam, Filter.class);
                errorMessage = "";
            } catch (JsonSyntaxException e) {
                errorMessage = e.getMessage();
                return;
            }
        }
        filterIdn = null;
        filterName = null;
        filterOrgTypeRecId = null;
        if (filter != null) {
            if (filter.fieldName.equalsIgnoreCase("ref_org_type_rec_id")
                    && filter.value != null && filter.value.getLngValue() != 0) {
                filterOrgTypeRecId = filter.value.getLngValue();
            } else {
                filterOrgTypeRecId = null;
            }
            if (filterOrgTypeRecId == null) {
                ldmReflegalPersonItem = null;
                return;
            }
        }
        load();
    }

    private void load() {
        if (ldmReflegalPersonItem == null) {
            ldmReflegalPersonItem = new LdmRefLegalPersonItem(sessionBean.getReference());
            ((LdmRefLegalPersonItem) ldmReflegalPersonItem).setCurrentDate(reportDate);
        }
        if (filterIdn != null && filterIdn.trim().isEmpty())
            ((LdmRefLegalPersonItem) ldmReflegalPersonItem).setFilterIdn(null);
        else
            ((LdmRefLegalPersonItem) ldmReflegalPersonItem).setFilterIdn(filterIdn);
        if (filterName != null && filterName.trim().isEmpty())
            ((LdmRefLegalPersonItem) ldmReflegalPersonItem).setFilterName(filterName);
        else
            ((LdmRefLegalPersonItem) ldmReflegalPersonItem).setFilterName(filterName);

        ((LdmRefLegalPersonItem) ldmReflegalPersonItem).setRefOrgTypeRecId(filterOrgTypeRecId);
        ((LdmRefLegalPersonItem) ldmReflegalPersonItem).setSearchInTax(searchInTax);

        dataTable.setFirst(0);
        legalPersonItem = null;
    }

    public void filter() {
        load();
    }

    public void passValue() {
        if(searchInTax){
            Long newRecId = sessionBean.getReference().createNewPersonFromTax(legalPersonItem.getRecId(), sessionBean.abstractUser.getId(), sessionBean.abstractUser.getLocation());
            legalPersonItem.setRecId(newRecId);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        sb.append("\"name_ru\": \"").append(escape(legalPersonItem.getNameRu())).append("\", ");
        sb.append("\"rec_id\": \"").append(legalPersonItem.getRecId().longValue()).append("\" ");
        sb.append(" }");
        selectedValue = Convert.encodeURIComponent(sb.toString());
    }

    private String escape(String s) {
        return s == null ? null : s.replaceAll("\"", "\\\\\"");
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public RefLegalPersonItem getLegalPersonItem() {
        return legalPersonItem;
    }

    public void setLegalPersonItem(RefLegalPersonItem legalPersonItem) {
        this.legalPersonItem = legalPersonItem;
    }

    public LazyDataModel<RefLegalPersonItem> getLdmReflegalPersonItem() {
        return ldmReflegalPersonItem;
    }

    public void setLdmReflegalPersonItem(LazyDataModel<RefLegalPersonItem> ldmReflegalPersonItem) {
        this.ldmReflegalPersonItem = ldmReflegalPersonItem;
    }

    public String getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public Boolean getSearchInTax() {
        return searchInTax;
    }

    public void setSearchInTax(Boolean searchInTax) {
        this.searchInTax = searchInTax;
    }

    static private class AjaxBody implements Serializable {
        private Date reportDate;
    }

    static private class Filter {
        private final String fieldName;
        private final ValueType valueType;
        private final Variant value;

        public Filter(String fieldName, ValueType valueType, Variant value) {
            this.fieldName = fieldName;
            this.valueType = valueType;
            this.value = value;
        }
    }

    private static class FilterAdapter extends TypeAdapter<Filter> {

        private static DateTimeFormatter formatter = ISODateTimeFormat.basicDate();

        @Override
        public void write(JsonWriter out, Filter value) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Filter read(JsonReader in) throws IOException {
            String fieldName = "";
            ValueType vt = null;
            Variant value = new Variant();

            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equalsIgnoreCase("fieldName")) {
                    fieldName = in.nextString();
                } else if (name.equalsIgnoreCase("dataType")) {
                    vt = DataType.toValueType(in.nextString());
                } else if (name.equalsIgnoreCase("strValue")) {
                    value.setStrValue(in.nextString());
                } else if (name.equalsIgnoreCase("boolValue")) {
                    value.setBoolValue(in.nextBoolean());
                } else if (name.equalsIgnoreCase("dateValue")) {
                    String dateStr = in.nextString();
                    if (dateStr != null) {
                        try {
                            LocalDate date = LocalDate.parse(dateStr, formatter);
                            value.setDateValue(date.toDate());
                        } catch (Exception e) {
                            throw new IllegalStateException(MessageFormat.format("Can't parse string {0} to date", dateStr));
                        }
                    }
                } else if (name.equalsIgnoreCase("dblValue")) {
                    value.setDblValue(in.nextDouble());
                } else if (name.equalsIgnoreCase("lngValue")) {
                    value.setLngValue(in.nextLong());
                } else {
                    throw new IllegalStateException(MessageFormat.format("Unknown json field {0}", name));
                }
            }
            in.endObject();
            value.setValueType(vt);
            return new Filter(fieldName, vt, value);
        }
    }
}


