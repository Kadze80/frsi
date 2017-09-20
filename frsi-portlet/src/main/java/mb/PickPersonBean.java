package mb;

import entities.RefCountryItem;
import entities.RefLegalPersonItem;
import org.primefaces.context.RequestContext;
import util.Convert;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Managed bean.
 * Supports combined picker dialog for legal and individual persons.
 *
 * @author Ardak Saduakassov
 */
@ManagedBean
@SessionScoped
public class PickPersonBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    private Integer activeTabIndex;

    private List<RefLegalPersonItem> legalPersons;
    private RefLegalPersonItem selectedLegalPerson;
    private String lpNameFilter;
    private String lpBinFilter;

    private String npIin;
    private String npFirstName;
    private String npMiddleName;
    private String npLastName;
    private String npFullName;

    private String nrIdn;
    private String nrName;
    private String nrCountryCode;
    private List<SelectItem> countries;

    private boolean disableNpIdn;
    private boolean disableNrIdn;
    private boolean nonresident;
    private boolean minor;

    @PostConstruct
    public void init() {
        try {
            if (sessionBean.isEjbNull()) sessionBean.init();

            activeTabIndex = 0;
            List<RefCountryItem> refCountryItems = (List<RefCountryItem>) sessionBean.getReference().getRefAbstractList(RefCountryItem.REF_CODE, sessionBean.getIntegration().getNewDateFromBackEndServer());
            countries = new ArrayList<SelectItem>();
            for (RefCountryItem item : refCountryItems)
                countries.add(new SelectItem(item.getRecId(), item.getNameRu()));

            disableNpIdn = disableNrIdn = nonresident = minor = false;
        } catch (Exception e) { applicationBean.redirectToErrorPage(e); }
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

    public boolean isPickButtonDisabled() {
        return activeTabIndex == 0 && selectedLegalPerson == null;
    }

    public void onNpCheckBoxChange() {
        npIin = minor ? "несовершеннолетний" : null;
        disableNpIdn = minor;
    }

    public void onNrCheckBoxChange() {
        nrIdn = nonresident ? "нерезидент РК" : null;
        disableNrIdn = nonresident;
    }

    public void clearLpFilters() {
        lpBinFilter = lpNameFilter = null;
    }

    public void searchLegalPersons() {
        boolean noLpBinFilter = lpBinFilter == null || lpBinFilter.trim().isEmpty();
        boolean noLpNameFilter = lpNameFilter == null || lpNameFilter.trim().isEmpty();
        if (noLpBinFilter && noLpNameFilter) {
            String msgText = "Результат поиска может содержать слишком большое количество элементов.<br/>Пожалуйста, укажите БИН или фрагмент наименования.";
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Внимание!", msgText);
            RequestContext.getCurrentInstance().showMessageInDialog(message);
        } else refreshLegalPersons();
    }

    public void refreshLegalPersons() {
        legalPersons = sessionBean.getReference().getLegalPersonsByFilters(sessionBean.getIntegration().getNewDateFromBackEndServer(), lpBinFilter, lpNameFilter);
        if (legalPersons.isEmpty()) selectedLegalPerson = null;
    }

    public String getPickedValue() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        String name = null;
        switch (activeTabIndex) {
            case 0: // Legal person
                sb.append("\"category\": \"LEGAL_PERSON\", ");
                sb.append("\"idn\": \"").append(selectedLegalPerson == null || selectedLegalPerson.getIdn() == null ? "" : selectedLegalPerson.getIdn()).append("\", ");
                name = selectedLegalPerson == null || selectedLegalPerson.getNameRu() == null ? "" : escape(selectedLegalPerson.getNameRu());
                sb.append("\"name\": \"").append(name).append("\", ");
                sb.append("\"country_id\": \"").append(selectedLegalPerson == null || selectedLegalPerson.getRefCountry() == null ? "-1" : selectedLegalPerson.getRefCountry().longValue()).append("\"");
                break;
            case 1: // Natural person
                sb.append("\"category\": \"NATURAL_PERSON\", ");
                sb.append("\"idn\": \"").append(npIin == null ? "" : npIin).append("\", ");
                name = npFullName == null ? "" : escape(npFullName);
                sb.append("\"name\": \"").append(name == null ? "" : name.trim()).append("\", ");
                sb.append("\"country_id\": \"").append("1347").append("\""); // KZ
                break;
            case 2: // Non-resident
                sb.append("\"category\": \"NON_RESIDENT\", ");
                sb.append("\"idn\": \"").append(nrIdn == null ? "" : nrIdn).append("\", ");
                sb.append("\"name\": \"").append(nrName == null ? "" : escape(nrName.trim())).append("\", ");
                sb.append("\"country_id\": \"").append(nrCountryCode == null ? "-1" : nrCountryCode).append("\"");
                break;
            default:
                sb.append("\"category\": \"UNKNOWN\", ");
        }

        sb.append(" }");
        return Convert.encodeURIComponent(sb.toString());
    }

    public void setPickedValue(String pickedValue) {}

    private String escape(String s) {
        return s == null ? null : s.replaceAll("\"", "\\\\\"");
    }

    // Getters and Setters

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public Integer getActiveTabIndex() {
        return activeTabIndex;
    }

    public void setActiveTabIndex(Integer activeTabIndex) {
        this.activeTabIndex = activeTabIndex;
    }

    public List<RefLegalPersonItem> getLegalPersons() {
        return legalPersons;
    }

    public void setLegalPersons(List<RefLegalPersonItem> legalPersons) {
        this.legalPersons = legalPersons;
    }

    public RefLegalPersonItem getSelectedLegalPerson() {
        return selectedLegalPerson;
    }

    public void setSelectedLegalPerson(RefLegalPersonItem selectedLegalPerson) {
        this.selectedLegalPerson = selectedLegalPerson;
    }

    public String getLpNameFilter() {
        return lpNameFilter;
    }

    public void setLpNameFilter(String lpNameFilter) {
        this.lpNameFilter = lpNameFilter;
    }

    public String getLpBinFilter() {
        return lpBinFilter;
    }

    public void setLpBinFilter(String lpBinFilter) {
        this.lpBinFilter = lpBinFilter;
    }

    public String getNpIin() {
        return npIin;
    }

    public void setNpIin(String npIin) {
        this.npIin = npIin;
    }

    public String getNpFirstName() {
        return npFirstName;
    }

    public void setNpFirstName(String npFirstName) {
        this.npFirstName = npFirstName;
    }

    public String getNpMiddleName() {
        return npMiddleName;
    }

    public void setNpMiddleName(String npMiddleName) {
        this.npMiddleName = npMiddleName;
    }

    public String getNpLastName() {
        return npLastName;
    }

    public void setNpLastName(String npLastName) {
        this.npLastName = npLastName;
    }

    public String getNpFullName() {
        return npFullName;
    }

    public void setNpFullName(String npFullName) {
        this.npFullName = npFullName;
    }

    public String getNrIdn() {
        return nrIdn;
    }

    public void setNrIdn(String nrIdn) {
        this.nrIdn = nrIdn;
    }

    public String getNrName() {
        return nrName;
    }

    public void setNrName(String nrName) {
        this.nrName = nrName;
    }

    public String getNrCountryCode() {
        return nrCountryCode;
    }

    public void setNrCountryCode(String nrCountryCode) {
        this.nrCountryCode = nrCountryCode;
    }

    public List<SelectItem> getCountries() {
        return countries;
    }

    public void setCountries(List<SelectItem> countries) {
        this.countries = countries;
    }

    public boolean isDisableNpIdn() {
        return disableNpIdn;
    }

    public void setDisableNpIdn(boolean disableNpIdn) {
        this.disableNpIdn = disableNpIdn;
    }

    public boolean isDisableNrIdn() {
        return disableNrIdn;
    }

    public void setDisableNrIdn(boolean disableNrIdn) {
        this.disableNrIdn = disableNrIdn;
    }

    public boolean isNonresident() {
        return nonresident;
    }

    public void setNonresident(boolean nonresident) {
        this.nonresident = nonresident;
    }

    public boolean isMinor() {
        return minor;
    }

    public void setMinor(boolean minor) {
        this.minor = minor;
    }
}
