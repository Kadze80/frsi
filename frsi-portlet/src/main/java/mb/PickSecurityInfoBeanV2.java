package mb;

import entities.LdmRefSecuritiesItem;
import entities.RefSecuritiesItem;
import entities.ReportListItem;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import util.Convert;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

@ManagedBean
@SessionScoped
public class PickSecurityInfoBeanV2 implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    private LazyDataModel<RefSecuritiesItem> ldmRefSecuritiesItem;

    private String nin;
    private List<RefSecuritiesItem> refSecuritiesItems;
    private RefSecuritiesItem security;
    private String pickedValue;

    @PostConstruct
    public void init() {
        try {
            if (sessionBean.isEjbNull()) sessionBean.init();
        } catch (Exception e) {
            applicationBean.redirectToErrorPage(e);
        }
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

    public void search() {
        if (ldmRefSecuritiesItem == null) {
            ldmRefSecuritiesItem = new LdmRefSecuritiesItem(sessionBean.getReference());
            ((LdmRefSecuritiesItem) ldmRefSecuritiesItem).setCurrentDate(sessionBean.getIntegration().getNewDateFromBackEndServer());
        }
        ((LdmRefSecuritiesItem) ldmRefSecuritiesItem).setFilter(nin);
        security = null;
    }

    public void passSecurityInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");

        sb.append("\"nin\": \"").append(security == null ? "" : security.getNin()).append("\", ");
        sb.append("\"variety_code\": \"").append(security == null || security.getSgSecurityVariety() == null ? "" : security.getSgSecurityVariety().longValue()).append("\", ");
        sb.append("\"variety_name\": \"").append(security == null ? "" : escape(security.getVarietyName())).append("\", ");
        sb.append("\"type_code\": \"").append(security == null || security.getSgSecurityType() == null ? "" : security.getSgSecurityType().longValue()).append("\", ");
        sb.append("\"type_name\": \"").append(security == null ? "" : escape(security.getTypeName())).append("\", ");
        sb.append("\"nominal_value\": \"").append(security == null ? "" : security.getNominalValue()).append("\", ");
        sb.append("\"currency_code\": \"").append(security == null || security.getCurrencyRecId() == null ? "" : security.getCurrencyRecId().longValue()).append("\", ");
        sb.append("\"currency_name\": \"").append(security == null ? "" : escape(security.getCurrencyName())).append("\", ");
        sb.append("\"begin_date\": \"").append(security == null || security.getBeginDate() == null ? "" : Convert.getDateStringFromDate(security.getBeginDate())).append("\", ");
        sb.append("\"end_date\": \"").append(security == null || security.getEndDate() == null ? "" : Convert.getDateStringFromDate(security.getEndDate())).append("\", ");
        sb.append("\"maturity_date\": \"").append(security == null || security.getMaturityDate() == null ? "" : Convert.getDateStringFromDate(security.getMaturityDate())).append("\", ");

        sb.append("\"issuer_name\": \"").append(security == null ? "" : escape(security.getIssuerName())).append("\", ");
        sb.append("\"issuer_rec_id\": \"").append(security == null ? "" : security.getsIssuer().longValue()).append("\", ");
        sb.append("\"rec_id\": \"").append(security == null ? "" : security.getRecId().longValue()).append("\", ");
        sb.append("\"issuer_sign_code\": \"").append(security == null ? "" : security.getSignCode()).append("\", ");
        sb.append("\"issuer_sign_name\": \"").append(security == null ? "" : escape(security.getSignName())).append("\", ");
        sb.append("\"issuer_state\": ").append(security == null ? "false" : security.getIsState()).append(", ");
        sb.append("\"issuer_resident\": ").append(security == null ? "false" : security.getIsResident()).append(", ");
        sb.append("\"issuer_country_code\": \"").append(security == null ? "" : "").append("\", ");
        sb.append("\"issuer_country_name\": \"").append(security == null ? "" : "").append("\"");

        sb.append(" }");
        pickedValue = Convert.encodeURIComponent(sb.toString());
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

    public String getPickedValue() {
        return pickedValue;
    }

    public void setPickedValue(String pickedValue) {
    }

    public String getNin() {
        return nin;
    }

    public void setNin(String nin) {
        this.nin = nin;
    }

    public RefSecuritiesItem getSecurity() {
        return security;
    }

    public void setSecurity(RefSecuritiesItem security) {
        this.security = security;
    }

    public LazyDataModel<RefSecuritiesItem> getLdmRefSecuritiesItem() {
        return ldmRefSecuritiesItem;
    }

    public void setLdmRefSecuritiesItem(LazyDataModel<RefSecuritiesItem> ldmRefSecuritiesItem) {
        this.ldmRefSecuritiesItem = ldmRefSecuritiesItem;
    }
}
