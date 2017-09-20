package mb;

import entities.RefBankItem;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import util.Convert;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@ManagedBean
@SessionScoped
public class PickBankBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger("fileLogger");

	@ManagedProperty(value = "#{applicationBean}")
	private ApplicationBean applicationBean;
	@ManagedProperty(value = "#{sessionBean}")
	private SessionBean sessionBean;

	private List<RefBankItem> banks;
	private RefBankItem selectedBank;
	private boolean useDateFilter;
	private Date dateFilter;
	private String nameFilter;

	@PostConstruct
	public void init() {
		try {
            if (sessionBean.isEjbNull()) sessionBean.init();
            dateFilter = sessionBean.getIntegration().getNewDateFromBackEndServer();
            useDateFilter = true;
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

	public void clearFilters() {
		nameFilter = null;
	}

	public void searchBanks() {
		boolean noNameFilter = nameFilter == null || nameFilter.trim().isEmpty();
		if (noNameFilter) {
			String msgText = "Результат поиска может содержать слишком большое количество элементов.<br/>Пожалуйста, укажите фрагмент наименования.";
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Внимание!", msgText);
			RequestContext.getCurrentInstance().showMessageInDialog(message);
		} else refreshBanks();
	}

	public void refreshBanks() {
		banks = sessionBean.getReference().getBanksByFilters(useDateFilter ? dateFilter : null, nameFilter);
		if (banks.isEmpty()) selectedBank = null;
	}

	public void setApplicationBean(ApplicationBean applicationBean) {
		this.applicationBean = applicationBean;
	}

	public void setSessionBean(SessionBean sessionBean) {
		this.sessionBean = sessionBean;
	}

	public String getPickedValue() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		sb.append("\"recId\": \"").append(selectedBank == null ? "" : selectedBank.getRecId().longValue()).append("\", ");
		sb.append("\"caption\": \"").append(selectedBank == null ? "" : escape(selectedBank.getNameRu())).append("\"");
		sb.append(" }");
		return Convert.encodeURIComponent(sb.toString());
	}

	private String escape(String s) {
		return s == null ? null : s.replaceAll("\"", "\\\\\"");
	}

	public void setPickedValue(String pickedValue) {}

	public List<RefBankItem> getBanks() {
		return banks;
	}

	public void setBanks(List<RefBankItem> banks) {
		this.banks = banks;
	}

	public RefBankItem getSelectedBank() {
		return selectedBank;
	}

	public void setSelectedBank(RefBankItem selectedBank) {
		this.selectedBank = selectedBank;
	}

	public boolean isUseDateFilter() {
		return useDateFilter;
	}

	public void setUseDateFilter(boolean useDateFilter) {
		this.useDateFilter = useDateFilter;
	}

	public Date getDateFilter() {
		return dateFilter;
	}

	public void setDateFilter(Date dateFilter) {
		this.dateFilter = dateFilter;
	}

	public String getNameFilter() {
		return nameFilter;
	}

	public void setNameFilter(String nameFilter) {
		this.nameFilter = nameFilter;
	}
}
