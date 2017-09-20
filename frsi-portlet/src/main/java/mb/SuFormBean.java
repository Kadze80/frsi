package mb;

import com.google.gson.Gson;
import entities.*;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import util.Convert;
import util.ExceptionUtil;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ManagedBean
@SessionScoped
public class SuFormBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger("fileLogger");

	@ManagedProperty(value = "#{applicationBean}")
	private ApplicationBean applicationBean;
	@ManagedProperty(value = "#{sessionBean}")
	private SessionBean sessionBean;
	@ManagedProperty(value = "#{userBean}")
	private UserBean userBean;

	private Date filterReportDateBegin;
	private Date filterReportDateEnd;

	private String filterFormsText;
	private List<FormListItem> filterForms = new ArrayList<FormListItem>();

	private String filterCodeText;
	private String filterLanguageText;


	private List<FormListItem> formsList;
	private List<FormListItem> forms;
	private FormListItem selectedForm;
	private boolean isUpdateExistingForm;
	private boolean isUpdateOnlyXml;
	private boolean isFormValidationOk;
	private String formValidationMessage;
	private String formValidationErrorMessage;

	private Integer kindEvent;

	private boolean isFillList;

	@PostConstruct
	public void init() {
		Date dateStart = new Date();

		try {
			if (sessionBean.isEjbNull()) sessionBean.init();
			resetFilterForms();
			refreshTemplates();
		} catch (Exception e) {
			applicationBean.redirectToErrorPage(e);
		}

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
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('statusDialog').show()");
	}

	public void refreshTemplates() {
		selectedForm = null;
		List<String> formCodes = new ArrayList<String>();
		for (FormListItem formListItem : filterForms) {
			formCodes.add(formListItem.getCode());
		}
		formsList = sessionBean.getPersistence().getFormListItemsNoLob(filterReportDateBegin, filterReportDateEnd, formCodes, filterCodeText, filterLanguageText);
	}

	public void updateOnlyXml(){
		kindEvent = 3;
		this.isUpdateOnlyXml = true;
	}

	public void updateExistingFormFlag(boolean updateExistingFormFlag) {
		kindEvent = updateExistingFormFlag ? 3 : 1;
		this.isUpdateExistingForm = updateExistingFormFlag;
	}

	public void updateIsFillList() {
		sessionBean.getPersistence().updateFormHistoryIsFillList(selectedForm.getFhId(), !selectedForm.isFillList());
		refreshTemplates();
	}

	public void onUploadXml(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		byte[] contents = uploadedFile.getContents();
		if (contents == null) return;

		FacesMessage message;
		String xml = new String(contents);

		if(isUpdateOnlyXml) {
			this.isUpdateOnlyXml = false;
			sessionBean.getPersistence().updateXmlForm(xml, selectedForm.getFhId());
			message = new FacesMessage(sessionBean.res.getString("success"), MessageFormat.format(sessionBean.res.getString("uploaded"), uploadedFile.getFileName()));
		}else {
			Form form = sessionBean.getPersistence().newForm(xml, "ru");
			if (form.getFormHistory().isValid()) {
				form = isValidForm(form);
			}

			isFormValidationOk = form.getFormHistory().isValid();
			formValidationMessage = form.getFormHistory().getValidationMessage();
			formValidationErrorMessage = form.getFormHistory().getErrorMessage();

			if (isFormValidationOk) {
				Date curDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
				form.getFormHistory().setLastUpdateXml(curDate);

				AuditEvent auditEvent = new AuditEvent();
				auditEvent.setCodeObject(form.getCode());
				auditEvent.setNameObject(null);
				auditEvent.setIdKindEvent((long) kindEvent);
				auditEvent.setDateEvent(curDate);
				auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
				auditEvent.setDateIn(form.getFormHistory().getBeginDate());
				auditEvent.setRecId(form.getId());
				auditEvent.setUserId(sessionBean.abstractUser.getId());
				auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());

				if (isUpdateExistingForm) {
					form.setId(selectedForm.getFormId());
					form.getFormHistory().setId(selectedForm.getFhId());
					sessionBean.getPersistence().updateForm(form, auditEvent);
				} else {
					Long oldFormId = sessionBean.getPersistence().getFormIdByCode(form.getCode());
					if (oldFormId == null) {
						Long newFormId = sessionBean.getPersistence().insertForm(form, auditEvent);
					} else {
						form.getFormHistory().setFormId(oldFormId);
						Long newFormHistoryId = sessionBean.getPersistence().insertFormHistory(form.getFormHistory(), form.getTypeCode().equals(Form.Type.INPUT.name()), auditEvent, null);
					}
				}
				resetFilterForms();
				refreshTemplates();
				message = new FacesMessage(sessionBean.res.getString("success"), MessageFormat.format(sessionBean.res.getString("uploaded"), uploadedFile.getFileName()));
			} else {
				RequestContext.getCurrentInstance().execute("PF('wValidationDialog').show()");
				message = new FacesMessage(sessionBean.res.getString("error"), formValidationErrorMessage);
			}
		}
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void onUploadXls(FileUploadEvent event) {
		kindEvent = 30;
		UploadedFile uploadedFile = event.getFile();
		byte[] contents = uploadedFile.getContents();
		if (contents == null) return;

		userBean.checkAccess("SU:TEMPL:EDIT_XLS");

		FacesMessage message = null;
		if (selectedForm != null) {

			if (!selectedForm.getTypeCode().equals(Form.Type.INPUT.name())) {
				formValidationMessage = "Ошибка";
				formValidationErrorMessage = "xls шаблоны можно загружать только к входным отчетам";
				RequestContext.getCurrentInstance().execute("PF('wValidationDialog').show()");
				return;
			}

			int xlsVersion;
			try {
				xlsVersion = validateXlsFileAndGetVersion(contents, selectedForm);
			} catch (Exception e) {
				formValidationMessage = "Ошибка";
				formValidationErrorMessage = e.getMessage();
				RequestContext.getCurrentInstance().execute("PF('wValidationDialog').show()");
				return;
			}
			Date curDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
			selectedForm.setLastUpdateXls(curDate);
			selectedForm.setXls(contents);
			selectedForm.setXlsVersion(xlsVersion);

			AuditEvent auditEvent = new AuditEvent();
			auditEvent.setCodeObject(selectedForm.getCode());
			auditEvent.setNameObject(null);
			auditEvent.setIdKindEvent((long) kindEvent);
			auditEvent.setDateEvent(curDate);
			auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
			auditEvent.setDateIn(selectedForm.getBeginDate());
			auditEvent.setRecId(selectedForm.getFormId());
			auditEvent.setUserId(sessionBean.abstractUser.getId());
			auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());

			sessionBean.getPersistence().updateFormHistoryWithXls(selectedForm, auditEvent);
			selectedForm.setXls(null);

			refreshTemplates();
			message = new FacesMessage(sessionBean.res.getString("success"), MessageFormat.format(sessionBean.res.getString("uploaded"), uploadedFile.getFileName()));
		}
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	private int validateXlsFileAndGetVersion(byte[] contents, FormListItem selectedForm) throws Exception{
		InputStream inputStream = new ByteArrayInputStream(contents);

		XSSFWorkbook workbook;
		try {
			workbook = new XSSFWorkbook(inputStream);
		} catch (IOException e) {
			throw new Exception("Ошибка при открытии файла");
		}

		Sheet sheet = workbook.getSheetAt(0);
		ExcelForm excelForm;
		try {
			excelForm = new Gson().fromJson(sheet.getRow(0).getCell(0).getStringCellValue(), ExcelForm.class);
		} catch (Exception e) {
			throw new Exception("Ошибка разбора структуры excel файла! \n" + e.getMessage());
		}

		if (excelForm.getForm() == null) {
			throw new Exception("Не указан код формы");
		}

		if (!excelForm.getForm().equals(selectedForm.getCode())) {
			throw new Exception("Код формы не совпадает");
		}

		if (excelForm.getXlsVersion() == null || excelForm.getXlsVersion() == 0) {
			throw new Exception("Нет версии Xls");
		}

		if (excelForm.getBeginDate() == null) {
			throw new Exception("Нет даты начала действия");
		}

		int maxXlsVersion = sessionBean.getPersistence().getFormMaxXlsVersion(selectedForm.getCode(), Convert.getDateFromString(excelForm.getBeginDate()));

		if(maxXlsVersion == 0){
			throw new Exception("Не найдена новая версия");
		}

		int xlsVersion = excelForm.getXlsVersion();

		if ((maxXlsVersion == selectedForm.getXlsVersion() && maxXlsVersion > xlsVersion)
				|| (maxXlsVersion != selectedForm.getXlsVersion() && maxXlsVersion >= xlsVersion)) {
			throw new Exception(MessageFormat.format("Более новая версия - {0} файла уже загружена", maxXlsVersion));
		}

		return xlsVersion;
	}

	public void onUploadXlsOut(FileUploadEvent event) {
		kindEvent = 31;
		UploadedFile uploadedFile = event.getFile();
		byte[] contents = uploadedFile.getContents();
		if (contents == null) return;

		userBean.checkAccess("SU:TEMPL:EDIT_XLS_OUT");

		FacesMessage message = null;
		if (selectedForm != null) {

			try {
				InputStream inputStream = new ByteArrayInputStream(contents);

				XSSFWorkbook workbook;
				try {
					workbook = new XSSFWorkbook(inputStream);
				} catch (IOException e) {
					throw new Exception("Ошибка при открытии файла");
				}

				Sheet sheet = workbook.getSheetAt(0);
				ExcelForm excelForm;
				try {
					excelForm = new Gson().fromJson(sheet.getRow(0).getCell(0).getStringCellValue(), ExcelForm.class);
				} catch (Exception e) {
					throw new Exception("Ошибка разбора структуры excel файла! \n" + e.getMessage());
				}

				if (excelForm.getForm() == null) {
					throw new Exception("Не указан код формы");
				}

				if (!excelForm.getForm().equals(selectedForm.getCode())) {
					throw new Exception("Код формы не совпадает");
				}
			} catch (Exception e) {
				formValidationMessage = "Ошибка";
				formValidationErrorMessage = e.getMessage();
				RequestContext.getCurrentInstance().execute("PF('wValidationDialog').show()");
				return;
			}

			Date curDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
			selectedForm.setLastUpdateXlsOut(curDate);
			selectedForm.setXlsOut(contents);

			AuditEvent auditEvent = new AuditEvent();
			auditEvent.setCodeObject(selectedForm.getCode());
			auditEvent.setNameObject(null);
			auditEvent.setIdKindEvent((long) kindEvent);
			auditEvent.setDateEvent(curDate);
			auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
			auditEvent.setDateIn(selectedForm.getBeginDate());
			auditEvent.setRecId(selectedForm.getFormId());
			auditEvent.setUserId(sessionBean.abstractUser.getId());
			auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());

			sessionBean.getPersistence().updateFormHistoryWithXlsOut(selectedForm, auditEvent);
			selectedForm.setXlsOut(null);

			refreshTemplates();
			message = new FacesMessage(sessionBean.res.getString("success"), MessageFormat.format(sessionBean.res.getString("uploaded"), uploadedFile.getFileName()));
		}
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void validate() {
		if (selectedForm == null) {
			isFormValidationOk = false;
			formValidationMessage = sessionBean.res.getString("noSelectedForm");
			formValidationErrorMessage = null;
		} else {
			Form form = sessionBean.getPersistence().newForm(sessionBean.getPersistence().getForm(selectedForm.getFormId(),selectedForm.getBeginDate()).getFormHistory().getXml(), "ru");
			isFormValidationOk = form.getFormHistory().isValid();
			formValidationMessage = form.getFormHistory().getValidationMessage();
			formValidationErrorMessage = form.getFormHistory().getErrorMessage();
		}
	}

	public String getHtml() {
		return selectedForm == null ? null : sessionBean.getPersistence().getForm(selectedForm.getFormId(), selectedForm.getBeginDate()).getFormHistory().getHtml();
	}

	public void deleteSelectedForm() {
		if (selectedForm != null) {
			userBean.checkAccess("SU:TEMPL:DELETE");

			Long fhId = sessionBean.getPersistence().getCountHistoryIdByFormId(selectedForm.getFormId());
			AuditEvent auditEvent = new AuditEvent();
			auditEvent.setCodeObject(selectedForm.getCode());
			auditEvent.setNameObject(selectedForm.getName());
			auditEvent.setIdKindEvent(2L);
			auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
			auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
			auditEvent.setDateIn(selectedForm.getBeginDate());
			auditEvent.setRecId(selectedForm.getFormId());
			auditEvent.setUserId(sessionBean.abstractUser.getId());
			auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());
			if (fhId > 1)
				sessionBean.getPersistence().deleteFormHistory(selectedForm.getFhId(), auditEvent);
			else
				sessionBean.getPersistence().deleteForm(selectedForm.getFormId(), auditEvent);

			resetFilterForms();
			refreshTemplates();
		}
	}

	public void updateBalanceAccountsTemplate() {
		try {
			sessionBean.getSchedule().updateBalanceAccTemplate(sessionBean.respondent.getId(), sessionBean.abstractUser.getId(), sessionBean.abstractUser.getLocation());

			refreshTemplates();
			resetFilterForms();

			FacesMessage message = new FacesMessage(sessionBean.res.getString("success"), MessageFormat.format(sessionBean.res.getString("uploaded"), "\"Балансовые счета\""));
			FacesContext.getCurrentInstance().addMessage(null, message);
		}catch (Exception e){
			RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка обновления шаблонов!", e.getMessage()));
		}
	}

	public void resetFilterForms() {
		forms = sessionBean.getPersistence().getFormListItemsNoLobNoDate();
		filterForms = new ArrayList<FormListItem>();
		filterForms.addAll(forms);

		updateFilterFormsText();
	}

	public void onDateSelect() {
		if (filterReportDateBegin == null && filterReportDateEnd != null) filterReportDateBegin = filterReportDateEnd;
		if (filterReportDateBegin != null && filterReportDateEnd == null) filterReportDateEnd = filterReportDateBegin;
	}

	public void onFiltersToggle() {
	}

	public void clearFilters() {
		filterReportDateBegin = filterReportDateEnd = null;
		filterForms = new ArrayList<FormListItem>();
		filterCodeText = null;
		filterLanguageText = null;
		updateFilterFormsText();
	}

	public void onFilterFormsHide() {
		updateFilterFormsText();
	}

	private void updateFilterFormsText() {
		int size = filterForms.size();
		if (size == 0) filterFormsText = "Нет ни одной формы!";
		else if (size == 1) filterFormsText = filterForms.get(0).getName();
		else if (size == forms.size()) filterFormsText = "Все";
		else filterFormsText = "Несколько форм (" + size + ")";
	}

	public void onFilterFormsShow() {
	}

	public Form isValidForm(Form form){
		Date beginDate = Convert.getDateFromString("01.01.1900");
		Date endDate = Convert.getDateFromString("01.01.3333");

		if(form.getFormHistory().getBeginDate() == null) {
			form.getFormHistory().setIsValid(false);
			form.getFormHistory().setValidationMessage("Ошибка!");
			form.getFormHistory().setErrorMessage("Дата начала не может быть пустой!");
			return form;
		}else{
			beginDate = form.getFormHistory().getBeginDate();
		}

		if(form.getFormHistory().getEndDate() != null){
			endDate = form.getFormHistory().getEndDate();
		}

		if(beginDate.after(endDate)){
			form.getFormHistory().setIsValid(false);
			form.getFormHistory().setValidationMessage("Ошибка!");
			form.getFormHistory().setErrorMessage("Дата начала не может быть больше даты окончания!");
			return form;
		}
		List<Form> formsValidList = sessionBean.getPersistence().getFormsNoLob();
		for(Form formValid : formsValidList){
			if(formValid.getCode().equals(form.getCode())){
				if((!isUpdateExistingForm) || (isUpdateExistingForm && !selectedForm.getFhId().equals(formValid.getFormHistory().getId()))){
					if(formValid.getFormHistory().getEndDate() == null)
						formValid.getFormHistory().setEndDate(endDate);
					if(beginDate.before(formValid.getFormHistory().getEndDate()) && endDate.after(formValid.getFormHistory().getBeginDate())){
						form.getFormHistory().setIsValid(false);
						form.getFormHistory().setValidationMessage("Ошибка!");
						form.getFormHistory().setErrorMessage("За этот период уже имеется шаблон формы!");
						break;
					}
				}
			}
		}
		if(isUpdateExistingForm){
			if(selectedForm.getXmlVersion()>form.getFormHistory().getXmlVersion()){
				form.getFormHistory().setIsValid(false);
				form.getFormHistory().setValidationMessage("Ошибка!");
				form.getFormHistory().setErrorMessage(MessageFormat.format("Более новая версия - {0} файла уже загружена", selectedForm.getXmlVersion()));
			}
		}
		return form;
	}

	public void generateConsFormExpr() {
		if (selectedForm != null) {
			try {
				sessionBean.getPersistence().makeConsolidatedReport(selectedForm.getCode(), selectedForm.getBeginDate());
			} catch (Exception e){
				Throwable t  = ExceptionUtil.getRootCauseRecursive(e);
				String message = "Unknown error";
				if(t.getMessage()!=null) {
					message = t.getMessage().trim();
				}
				RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
				RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
				return;
			}
		}
		RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_INFO, "Done", "Generated"));
	}

	public void onDownloadXlsOut() {
		if(selectedForm!= null){
			applicationBean.putFileContentToResponseOutputStream(sessionBean.getPersistence().downloadTemplateExcel(selectedForm.getFhId(), false), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", selectedForm.getCode() + ".xlsx");
		}
	}

	// region Getters and setters

	public void setApplicationBean(ApplicationBean applicationBean) {
		this.applicationBean = applicationBean;
	}

	public void setSessionBean(SessionBean sessionBean) {
		this.sessionBean = sessionBean;
	}

	public List<FormListItem> getForms() {
		return forms;
	}

	public void setForms(List<FormListItem> forms) {
		this.forms = forms;
	}

	public FormListItem getSelectedForm() {
		return selectedForm;
	}

	public void setSelectedForm(FormListItem selectedForm) {
		this.selectedForm = selectedForm;
	}

	public boolean isFormValidationOk() {
		return isFormValidationOk;
	}

	public String getFormValidationMessage() {
		return formValidationMessage;
	}

	public String getFormValidationErrorMessage() {
		return formValidationErrorMessage;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	public Date getFilterReportDateBegin() {
		return filterReportDateBegin;
	}

	public void setFilterReportDateBegin(Date filterReportDateBegin) {
		this.filterReportDateBegin = filterReportDateBegin;
	}

	public Date getFilterReportDateEnd() {
		return filterReportDateEnd;
	}

	public void setFilterReportDateEnd(Date filterReportDateEnd) {
		this.filterReportDateEnd = filterReportDateEnd;
	}

	public String getFilterFormsText() {
		return filterFormsText;
	}

	public void setFilterFormsText(String filterFormsText) {
		this.filterFormsText = filterFormsText;
	}

	public List<FormListItem> getFilterForms() {
		return filterForms;
	}

	public void setFilterForms(List<FormListItem> filterForms) {
		this.filterForms = filterForms;
	}

	public List<FormListItem> getFormsList() {
		return formsList;
	}

	public void setFormsList(List<FormListItem> formsList) {
		this.formsList = formsList;
	}

	public String getFilterCodeText() {
		return filterCodeText;
	}

	public void setFilterCodeText(String filterCodeText) {
		this.filterCodeText = filterCodeText;
	}

	public String getFilterLanguageText() {
		return filterLanguageText;
	}

	public void setFilterLanguageText(String filterLanguageText) {
		this.filterLanguageText = filterLanguageText;
	}

	public Boolean getFillList() {
		return isFillList;
	}

	public void setFillList(Boolean fillList) {
		isFillList = fillList;
	}

	// endregion
}
