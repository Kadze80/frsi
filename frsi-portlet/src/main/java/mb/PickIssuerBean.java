package mb;

import entities.RefIssuersItem;
import org.apache.log4j.Logger;
import util.Convert;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@ManagedBean
@SessionScoped
public class PickIssuerBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger("fileLogger");

	@ManagedProperty(value = "#{applicationBean}")
	private ApplicationBean applicationBean;
	@ManagedProperty(value = "#{sessionBean}")
	private SessionBean sessionBean;

	private List<RefIssuersItem> issuers;
	private RefIssuersItem selectedIssuer;
	private boolean useIssuerDateFilter;
	private Date issuerDateFilter;
	private String issuerNameFilter;

	@PostConstruct
	public void init() {
		try {
            if (sessionBean.isEjbNull()) sessionBean.init();
            issuerDateFilter = sessionBean.getIntegration().getNewDateFromBackEndServer();
            useIssuerDateFilter = true;
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

	public void setApplicationBean(ApplicationBean applicationBean) {
		this.applicationBean = applicationBean;
	}

	public void setSessionBean(SessionBean sessionBean) {
		this.sessionBean = sessionBean;
	}

	public String getIssuerNameFilter() {
		return issuerNameFilter;
	}

	public void setIssuerNameFilter(String issuerNameFilter) {
		this.issuerNameFilter = issuerNameFilter;
	}

	public void refreshIssuers() {
		issuers = sessionBean.getReference().getIssuersByFilters(useIssuerDateFilter ? issuerDateFilter : null, issuerNameFilter);
		if (issuers.isEmpty()) selectedIssuer = null;
	}

	public List<RefIssuersItem> getIssuers() {
		return issuers;
	}

	public void setIssuers(List<RefIssuersItem> issuers) {
		this.issuers = issuers;
	}

	public RefIssuersItem getSelectedIssuer() {
		return selectedIssuer;
	}

	public void setSelectedIssuer(RefIssuersItem selectedIssuer) {
		this.selectedIssuer = selectedIssuer;
	}

	public String getPickedValue() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		sb.append("\"recId\": \"").append(selectedIssuer == null ? "" : selectedIssuer.getRecId().longValue()).append("\", ");
		sb.append("\"caption\": \"").append(selectedIssuer == null ? "" : escape(selectedIssuer.getNameRu())).append("\"");
		sb.append(" }");
		return Convert.encodeURIComponent(sb.toString());
	}

	private String escape(String s) {
		return s == null ? null : s.replaceAll("\"", "\\\\\"");
	}

	public void setPickedValue(String pickedValue) {}

	public Date getIssuerDateFilter() {
		return issuerDateFilter;
	}

	public void setIssuerDateFilter(Date issuerDateFilter) {
		this.issuerDateFilter = issuerDateFilter;
	}

	public boolean isUseIssuerDateFilter() {
		return useIssuerDateFilter;
	}

	public void setUseIssuerDateFilter(boolean useIssuerDateFilter) {
		this.useIssuerDateFilter = useIssuerDateFilter;
	}
}
