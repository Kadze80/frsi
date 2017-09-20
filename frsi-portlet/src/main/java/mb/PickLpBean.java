package mb;

import entities.RefLegalPersonItem;
import org.primefaces.context.RequestContext;
import util.Convert;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

/**
 * Managed bean.
 * Supports picker dialog for legal persons.
 *
 * @author Ardak Saduakassov
 */
@ManagedBean
@SessionScoped
public class PickLpBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    private List<RefLegalPersonItem> legalPersons;
    private RefLegalPersonItem selectedLegalPerson;
    private String lpNameFilter;
    private String lpBinFilter;

    @PostConstruct
    public void init() {
        try {
            if (sessionBean.isEjbNull()) sessionBean.init();
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
        return selectedLegalPerson == null;
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
        sb.append("\"recId\": \"").append(selectedLegalPerson == null ? "" : selectedLegalPerson.getRecId().longValue()).append("\", ");
        sb.append("\"caption\": \"").append(selectedLegalPerson == null ? "" : escape(selectedLegalPerson.getNameRu())).append("\" ");
        sb.append(" }");
        return Convert.encodeURIComponent(sb.toString());
    }

    private String escape(String s) {
        return s == null ? null : s.replaceAll("\"", "\\\\\"");
    }

    public void setPickedValue(String pickedValue) {}

    // Getters and Setters

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
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
}
