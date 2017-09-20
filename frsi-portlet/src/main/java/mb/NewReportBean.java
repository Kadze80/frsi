package mb;

import com.liferay.portal.model.User;
import entities.*;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import util.Util;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Managed bean
 *
 * @author Ardak Saduakassov
 */
@ManagedBean
@SessionScoped
public class NewReportBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger("fileLogger");

	@ManagedProperty(value = "#{applicationBean}")
	private ApplicationBean applicationBean;
	@ManagedProperty(value = "#{sessionBean}")
	private SessionBean sessionBean;
	@ManagedProperty(value = "#{reportsBean}")
	private ReportsBean reportsBean;
	@ManagedProperty(value = "#{reportBean}")
	private ReportBean reportBean;
	@ManagedProperty(value = "#{userBean}")
	private UserBean userBean;

	private Date reportDate;
	private List<Form> forms;
	private Form selectedForm;
	private ReportListItem existingReportListItem;

	private boolean hasReportCreateErrors;

	private Long selectedRespId;
	private List<RefRespondentItem> respList;

	@PostConstruct
	public void init() {
		Date dateStart = new Date();

		try {
            if (sessionBean.isEjbNull()) sessionBean.init();
            reportDate = Util.getFirstDayOfCurrentMonth();
			selectedRespId = sessionBean.respondent.getId();
        } catch (Exception e) { applicationBean.redirectToErrorPage(e); }

		Date dateEnd = new Date();
		long duration = dateEnd.getTime() - dateStart.getTime();
		logger.debug(MessageFormat.format("t: {0}, d: {1} ms", dateStart, duration));
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

	public void updateForms() {
		if(selectedRespId==null) {
			forms = null;
			selectedForm = null;
		}else {
			forms = sessionBean.getPersistence().getFormsByUserIdDate(sessionBean.user.getUserId(), reportDate, reportsBean.getRespItem(selectedRespId, respList), true);
		}
	}

	public void createNewReport() {
		RefRespondentItem respondentItem = reportsBean.getRespItem(selectedRespId, respList);
		hasReportCreateErrors = false;
		reportBean.clearErrors();

		if (selectedForm == null) {
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", "Выберите форму"));
			hasReportCreateErrors = true;
			return;
		}

		if (reportDate == null) {
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", "Выберите дату отчета"));
			hasReportCreateErrors = true;
			return;
		}

		userBean.checkFormAccess(selectedForm.getCode(), "F:EDIT", getSelectedRespIdn());

		if(!sessionBean.getPersistence().checkPeriod(reportDate,selectedForm.getCode(),respondentItem.getRefSubjectTypeRecId())) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
			String formName = sessionBean.getPersistence().getFormNameByFormCodeLanguageCodeReportDate(selectedForm.getCode(),sessionBean.languageCode,reportDate);
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, sessionBean.res.getString("error"), MessageFormat.format("Невозможно создать отчет {0} за {1}", formName, dateFormat.format(reportDate))));
			hasReportCreateErrors = true;
			return;
		}

		checkReportExist(null, null);
		if (existingReportListItem != null) {
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('wDlgReportExists').show();");
			return;
		}

		Report report = new Report();
		report.setIdn(sessionBean.respondent == null ? null : sessionBean.respondent.getIdn());
		if(!sessionBean.respondent.getId().equals(selectedRespId))
			report.setIdnChild(respondentItem.getIdn());
		report.setReportDate(reportDate);
		report.setFormCode(selectedForm.getCode());
		report.setId(null);
		Map<String,String> inputValues = new HashMap<String, String>();
		Long genReportId = sessionBean.getPersistence().saveAndGetId("WEB", report, sessionBean.respondent.getRecId(), selectedForm.getId(), sessionBean.abstractUser, sessionBean.getIntegration().getNewDateFromBackEndServer(), inputValues, null, false);

		ReportListItem item = new ReportListItem();
		item.setId(genReportId);
		User user = sessionBean.user;
		item.setUserInfo(user.getFullName());
		item.setFormCode(selectedForm.getCode());
		item.setFormName(selectedForm.getFormHistory().getName());
		item.setFormTypeCode(selectedForm.getTypeCode());
		item.setReportDate(reportDate);
		item.setSaveDate(sessionBean.getIntegration().getNewDateFromBackEndServer());
		item.setDeliveryWay("WEB_FORM");
		item.setStatus("DRAFT");
		item.setCanAttachedFile(selectedForm.getFormHistory().getFormTag() == null ? false : selectedForm.getFormHistory().getFormTag().canAttachedFile);
//		item.setCanAttachedFile(selectedForm.getFormHistory().getFormTag().canAttachedFile);

		reportsBean.getReportList().add(item);
		reportsBean.setSelectedReportListItem(item);
		if(selectedForm.getTypeCode().equals(Form.Type.INPUT_RAW.name())) {
			reportBean.refreshFileList(genReportId, null, "RESP", 3);
		}else {
			String html = sessionBean.getPersistence().getHtmlWithReportData(genReportId, false, false);
			reportBean.setHtml(html);
		}
		reportBean.setReportId(item.getId());
		reportBean.setApproved(false);
		reportBean.setStatusName(ReportStatus.resMap.get(sessionBean.languageCode + "_" + item.getStatus()));
		reportBean.setStatus(item.getStatus());
		reportBean.setCanAttachedFile(item.getCanAttachedFile());
		reportBean.setHaveAttachedFile(false);
		reportBean.setHaveAttachedLetter(false);
		reportBean.setRightSave(userBean.hasPermissionForm(item.getFormCode(), "F:EDIT", sessionBean.respondent == null ? "" : sessionBean.respondent.getIdn()));
		reportBean.setFixed(false);
		reportBean.setPollEnabled(true);
		//reportsBean.refreshReportList();
	}

	public String createNewReportAction() {
		if(hasReportCreateErrors)
			return null;
		if (reportDate == null) return null;
		reportBean.setReportDate(reportDate);
		reportsBean.setApproved(false);
		reportBean.setUpdateCalculatedFieldsManually(selectedForm != null &&selectedForm.getFormHistory().getFormTag() != null && selectedForm.getFormHistory().getFormTag().updateCalculatedFieldsManually);
		if(existingReportListItem == null){
			if(selectedForm.getTypeCode().equals(Form.Type.INPUT_RAW.name()))
				return "/views/reports/form_raw?faces-redirect=true";
			else
				return "/views/reports/form?faces-redirect=true";
		} else
			return null;
		//return existingReportListItem == null ? "/views/reports/form?faces-redirect=true" : null;
	}

	public void checkReportExist(Date newReportDate, ReportListItem selectedReportListItem) {
		existingReportListItem = null;
		RefRespondentItem respondent = reportsBean.getRespItem(selectedRespId, respList);
		if (respondent == null) {
			return;
		}

		List<ReportListItem> reportList = null;
		reportList = newReportDate == null && selectedReportListItem == null ?
				sessionBean.getPersistence().getReportListByIdnReportDateFormCode(respondent.getIdn(), !respondent.getIdn().equals(sessionBean.respondent.getIdn()),  reportDate, selectedForm.getCode(), sessionBean.languageCode) :
				sessionBean.getPersistence().getReportListByIdnReportDateFormCode(respondent.getIdn(), !respondent.getIdn().equals(sessionBean.respondent.getIdn()), newReportDate, selectedReportListItem.getFormCode(), sessionBean.languageCode);
		if (!reportList.isEmpty()) {
			existingReportListItem = reportList.get(0);
		}
	}

	public String prepareExistingReport() {
		String result;

		if (!reportsBean.getReportList().contains(existingReportListItem)) {
			reportsBean.getReportList().add(existingReportListItem);
		}
		//reportsBean.setSelectedReportListItem(existingReportListItem);
		reportsBean.setApproved(existingReportListItem.getStatus().equals("APPROVED"));
		/*
		При открытии не меняю дату подписания

		if(existingReportListItem.getStatus().equals("DRAFT")){
			Long formId = sessionBean.getPersistence().getFormIdByCode(existingReportListItem.getFormCode());
			Form form = sessionBean.getPersistence().getForm(formId, existingReportListItem.getReportDate());
			ReportHistory reportHistory  = sessionBean.getPersistence().getLastReportHistoryByReportId(existingReportListItem.getId(), true, false);
			FormHistory formHistory = sessionBean.getPersistence().getFormHistoryWithInputValueChecks(form.getFormHistory().getId());
			if(reportBean.fillDefaultValueByFormWithValues(reportHistory.getId(), formHistory.getInputValueChecks(), reportHistory.getData(), sessionBean.getIntegration().getNewDateFromBackEndServer(), true, false)){
				RequestContext context = RequestContext.getCurrentInstance();
				context.execute("PF('wDlgSignDate').show();");
				result = null;
			}
		}*/

		reportBean.setReportId(existingReportListItem.getId());
		reportBean.setApproved(existingReportListItem.getStatus().equals("APPROVED"));
		reportBean.setStatusName(ReportStatus.resMap.get(sessionBean.languageCode + "_" + existingReportListItem.getStatus()));
		reportBean.setStatus(existingReportListItem.getStatus());
		reportBean.setCanAttachedFile(existingReportListItem.getCanAttachedFile());
		reportBean.setHaveAttachedFile(existingReportListItem.getHaveAttachedFile());
		reportBean.setHaveAttachedLetter(existingReportListItem.getHaveAttachedLetter());
		reportBean.setRightSave(userBean.hasPermissionForm(existingReportListItem.getFormCode(), "F:EDIT", existingReportListItem.getIdn()));
		reportBean.setFixed(false);

		if(selectedForm.getTypeCode().equals(Form.Type.INPUT_RAW.name())) {
			result = "/views/reports/form_raw?faces-redirect=true";
			reportBean.refreshFileList(existingReportListItem.getId(), null, "RESP", 3);
		}else{
			result = "/views/reports/form?faces-redirect=true";
			String html = sessionBean.getPersistence().getHtmlWithReportData(existingReportListItem.getId(),
					existingReportListItem.getStatus().equals(ReportStatus.Status.APPROVED.name()), false);
			reportBean.setHtml(html);
		}
		return result;
	}

	public String getHtml() {
		if (selectedForm == null) return null;
		String htmlWithInitialData = sessionBean.getPersistence().getHtmlWithInitialData(selectedForm, sessionBean.respondent, reportDate, false);
		String htmlWithoutDynamicRowTemplates = sessionBean.getPersistence().getHtmlWithoutDynamicRowTemplates(htmlWithInitialData);
		return htmlWithoutDynamicRowTemplates;
	}

    public void refreshRespList(Date date, Boolean withParent){
        respList = reportsBean.getRespWarrants(date, withParent);
    }

    public void prepareOpenDlg(){
        refreshRespList(reportDate, true);
        updateForms();

    }

	public String getSelectedRespIdn() {
		if (selectedRespId == null || selectedRespId == 0) return "";

		if(respList==null) return "";

		RefRespondentItem r = reportsBean.getRespItem(selectedRespId, respList);
		if (r == null) return "";
		return r.getIdn();
	}

	// region Getters and setters

	public void setApplicationBean(ApplicationBean applicationBean) {
		this.applicationBean = applicationBean;
	}

	public void setSessionBean(SessionBean sessionBean) {
		this.sessionBean = sessionBean;
	}

	public void setReportsBean(ReportsBean reportsBean) {
		this.reportsBean = reportsBean;
	}

	public void setReportBean(ReportBean reportBean) {
		this.reportBean = reportBean;
	}

	public Date getReportDate() {
		return reportDate;
	}

	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	public List<Form> getForms() {
		return forms;
	}

	public void setForms(List<Form> forms) {
		this.forms = forms;
	}

	public Form getSelectedForm() {
		return selectedForm;
	}

	public void setSelectedForm(Form selectedForm) {
		this.selectedForm = selectedForm;
	}

	public ReportListItem getExistingReportListItem() {
		return existingReportListItem;
	}

	public void setExistingReportListItem(ReportListItem existingReportListItem) {
		this.existingReportListItem = existingReportListItem;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	public Long getSelectedRespId() {
		return selectedRespId;
	}

	public void setSelectedRespId(Long selectedRespId) {
		this.selectedRespId = selectedRespId;
	}

	public List<RefRespondentItem> getRespList() {
		return respList;
	}

	public void setRespList(List<RefRespondentItem> respList) {
		this.respList = respList;
	}

	// endregion
}
