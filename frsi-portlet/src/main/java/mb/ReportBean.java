package mb;

import entities.*;
import entities.Error;
import org.apache.commons.fileupload.FileUpload;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.ResizeEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.UploadedFile;
import util.Convert;
import util.ReportHelper;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
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
public class ReportBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger("fileLogger");

	@ManagedProperty(value = "#{applicationBean}")
	private ApplicationBean applicationBean;
	@ManagedProperty(value = "#{sessionBean}")
	private SessionBean sessionBean;
	@ManagedProperty(value = "#{userBean}")
	private UserBean userBean;

	private Long reportId;
	private Long reportHistoryId;
	private Long openedReportId;
	private Date reportDate;
	private String hiddenValue;
	private boolean rightSave;
	private boolean rightApprove;
	private boolean rightDisapprove;
	private boolean rightDonwload;
	private Date completeDate;

	private boolean fixed;

	private ReportListItem openedReportListItem;
	private boolean updateCalculatedFieldsManually;
	private boolean approved;
	String html;
	private boolean saveError;
	private String statusName;
	private String status;

	private List<AttachedFile> reportFileListItem;
	private List<AttachedFile> reportLetterListItem;
	private AttachedFile selectedReportFileItem;
	private AttachedFile selectedReportLetterItem;
	private boolean canAttachedFile;
	private boolean haveAttachedFile;
	private boolean haveAttachedLetter;
	private Long openedReportHistoryId;
	private ReportHistoryListItem openedReportHistoryListItem;
	private String formName;
	private String formCode;
	private String formTypeCode;
	private Date saveDate;
	private Date statusDate;
	private List<SignInfo> signeInfos;
	private String respondentName;
	private String respondentChildName;

	public String getFormName() {
		return formName;
	}

	public String getFormCode() {
		return formCode;
	}

	public enum ValidationResult { UNKNOWN, FAILED, OK, ERRORS }

	// Validation
	private boolean validationVisible;
	private ValidationResult validationResult;
	private String validationMessage;
	private List<Error> errors = new ArrayList<Error>();

	private String pdfFilePath;

	private Image curImage;
	private List<Image> imageList;

	private Image curImageLetter;
	private List<Image> imageLetterList;

	private Image curImageWarrant;
	private List<Image> imageWarrantList;

	private List<ControlResultItem> controlResults;

	private List<ReportHistoryListItem> reportHistoryListItems;
	private ReportHistoryListItem selectedReportHistoryItem;
	private String currentHistoryComment;

	private List<ReportStatus> statuses;
	private List<ReportStatus> filteredStatuses;
	private LazyDataModel<ReportStatus> ldmReportStatus;
	private Boolean draftStatusesHidden;
	private int sendCount;

	private boolean pollEnabled;

	// RAW report
	private Image curImageRaw;
	private List<Image> imageRawList;
	private List<AttachedFile> reportRawListItem;
	private AttachedFile selectedReportRawItem;

	@PostConstruct
	public void init() {
		Date dateStart = new Date();

		try {
			if (sessionBean.isEjbNull()) sessionBean.init();
			ldmReportStatus = new LdmReportStatus();
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

	public void onResizeMainLayout(ResizeEvent event){
		if(event.getComponent().getId().equals("luForm")) {
			RequestContext.getCurrentInstance().execute("onResizeFormLayout(" + event.getHeight() + ")");
		}
	}

	public void validationFailed() {
		errors.clear();
		validationVisible = true;
		validationResult = ValidationResult.FAILED;
		validationMessage = "Произошла ошибка при проверке.";
	}

	public void validateKvMap(Map<String, String> kvMap, Long id) {
		errors.addAll(sessionBean.getPersistence().validateKvMap(kvMap, id, reportDate));
		setValidationParams();
	}

	private void setValidationParams(){
		validationVisible = true;
		validationResult = ValidationResult.OK;
		for (Error error : errors) {
			if (error.getLevel() == Error.Level.ERROR) {
				validationResult = ValidationResult.ERRORS;
				break;
			}
		}
		if (validationResult == ValidationResult.OK) {
			validationMessage = "Предварительная проверка прошла успешно.";
		} else {
			validationMessage = "Отчёт содержит ошибки.";
		}
	}

	public void validateSubmittedByForm(Form form) {
		Map<String,String> kvMap = getDataFormInputValues(form);
		validateKvMap(kvMap, form.getFormHistory().getId());
	}

	public void clearErrors() {
		errors.clear();
		validationVisible = false;
		validationResult = ValidationResult.UNKNOWN;
		validationMessage = "Отчёт не проверен";
	}

	public String getErrorLevel(Error error) {
		String result;
		switch (error.getLevel()) {
			case ERROR:
				result = "Ошибка:";
				break;
			case WARNING:
				result = "Предупреждение:";
				break;
			case INFO:
				result = "Информация:";
				break;
			case DEBUG:
				result = "Отладка:";
				break;
			default:
				result = "Сообщение:";

		}
		return result;
	}

	public String getValidationMessageStyle(ValidationResult validationResult) {
		String result;
		switch (validationResult) {
			case ERRORS:
				result = "color: red;";
				break;
			case FAILED:
				result = "color: #808000;";
				break;
			case OK:
				result = "color: green;";
				break;
			default:
				result = "color: gray;";
		}
		return result;
	}

	public String getErrorStyle(Error error) {
		String result;
		switch (error.getLevel()) {
			case ERROR:
				result = "color: red;";
				break;
			case WARNING:
				result = "color: #808000;";
				break;
			case INFO:
				result = "color: blue;";
				break;
			case DEBUG:
				result = "color: green;";
				break;
			default:
				result = "color: black;";
		}
		return result;
	}

	public String getHistoryRowStyleClass(ReportHistoryListItem item) {
		String result = null;
		String controlResultStyleClass = "";
		if (item.getControlResultCode() != null) {
			Long controlResultType = 3L;
			try {
				ControlResult.ResultType rt = ControlResult.ResultType.valueOf(item.getControlResultCode());
				controlResultType = rt.getId();
			} catch (IllegalArgumentException e) {
			}
			controlResultStyleClass = ReportHelper.getControlRowStyleClass(controlResultType);
		}

		return controlResultStyleClass;
	}

	public void updateSignature(long id, String signature, Date signDate, AuditEvent auditEvent, long userWarrantId) {
		sessionBean.getPersistence().updateSignature(id, signature, sessionBean.user.getUserId(), sessionBean.user.getLoginIP(), signDate, userWarrantId, auditEvent);
	}

	public static SortedMap<String,String> getDataFormInputValues(Form form) {
		Map<String,String[]> originalParams = ApplicationBean.getPortalRequest().getParameterMap();
		SortedMap<String,String> result = new TreeMap<String,String>();
		if (form != null) {
			for (Map.Entry<String,String[]> entry : originalParams.entrySet()) {
				if (entry.getKey().startsWith(form.getCode())) {
					String[] values = entry.getValue();
					if (values != null && values.length > 0) result.put(entry.getKey(), values[0]);
				}
			}
		}
		return result;
	}

	public void hiddenListener() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String tag = params.get("tag");
		if (tag == null || tag.trim().isEmpty()) return;
		String value = null;

		if (tag.equals("test")) {
			value = "Ok";
		}
		else if (tag.equals("balanceAccountDetails")) {
			String tableId = params.get("tableId");
			String rowId = params.get("rowId");
			List<RefBalanceAccItem> items = sessionBean.getReference().getRefBalanceAccountsByParentCode(reportDate, rowId);
			StringBuilder sb = new StringBuilder();
			sb.append("{ \"tag\": \"" + tag + "\", \"tableId\": \"" + tableId + "\", \"rowId\": \"" + rowId + "\", \"data\": [");
			int i = 0;
			for (RefBalanceAccItem item : items) {
				if (i > 0) sb.append(", ");
				String name = Convert.htmlFormatWithTabsAndLineBreaks(item.getNameRu());
				sb.append("{ \"parentcode\": \"").append(item.getParentCode()).append("\", \"code\": \"").append(item.getCode()).append("\", \"name\": \"").append(name).append("\" }");
				i++;
			}
			sb.append("]}");
			value = sb.toString();
		}

		hiddenValue = value;
	}

	public void validateSubmittedByReportId(Long id, boolean saveData, boolean saveControlResult) {
		clearErrors();
		if (saveData) {
			save(id, false);
		}

		ReportListItem reportListItem = getCurrentReportListItem(id);
		sessionBean.resetPoll();
		Date reportDate = reportListItem.getReportDate();
		List<Form> forms = sessionBean.getPersistence().getFormsByCodeLanguageReportDate(reportListItem.getFormCode(), sessionBean.languageCode, reportDate, null);
		if (forms.isEmpty()) {
			validationFailed();
			return;
		}
		validateRefValues(reportListItem);
		validateSubmittedByForm(forms.get(0));

		if (validationResult == ValidationResult.OK) {
			performControl(reportListItem, saveControlResult);
		}
	}

	private void validateRefValues(ReportListItem reportListItem){
		errors.addAll(sessionBean.getPersistence().validateRefValues(reportListItem.getId(), reportListItem.getReportDate()));
		setValidationParams();
	}

	public void downloadValidationResults(Long id){
		ReportListItem reportListItem = getCurrentReportListItem(id);
		byte[] bytes = null;
		String fileName = "";
		try {
			FileWrapper fileWrapper = sessionBean.getPersistence().validationResultsToExcelFile(validationMessage, errors, reportListItem);
			bytes = fileWrapper.getBytes();

			DateFormat dfReportDate = new SimpleDateFormat("dd.MM.yyyy");
			fileName = "validation_result_" + dfReportDate.format(reportListItem.getReportDate()) + "_" + reportListItem.getFormCode() + sessionBean.respondent.getIdn() + "." + fileWrapper.getFileFormat();
		} catch (Exception e) {
			bytes = null;
			fileName = "";
		} finally {
			applicationBean.putFileContentToResponseOutputStream(bytes, "application/vnd.ms-excel", fileName);
		}
	}

	public void prepareValidationResultsPdfFile(Long id) {
		ReportListItem reportListItem = getCurrentReportListItem(id);
		try {
			FileWrapper fileWrapper = sessionBean.getPersistence().generateValidationResultsPdfFile(validationMessage, errors, reportListItem);

			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			PortletContext portletContext = (PortletContext) externalContext.getContext();
			String dir = portletContext.getRealPath("/resources/reports/");
			File file = File.createTempFile("validation_result_"+String.valueOf(reportListItem.getId())+"_", ".pdf", new File(dir));

			if(!file.exists()) {
				boolean created = file.createNewFile();
				if(!created)
					throw new Exception("Ошибка при создании pdf-файла");
			}

			FileOutputStream outputStream  = new FileOutputStream(file);
			outputStream.write(fileWrapper.getBytes());
			outputStream.flush();
			outputStream.close();

			pdfFilePath = "/frsi-portlet/resources/reports/" + file.getName();

		} catch (Exception e) {
			pdfFilePath = "";
			RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
		}
	}

	public boolean isAutoSaveRendered(long id) {
		ReportListItem reportListItem = getCurrentReportListItem(id);
		return reportListItem.getStatus().equals(ReportStatus.Status.DRAFT.name());
	}

	public boolean isSaveEnabled() {
		return !approved;
	}

	public void save(Long id, boolean isAutoSave) {
		Date curDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
		ReportListItem reportListItem = getCurrentReportListItem(id);
		sessionBean.resetPoll();
		userBean.checkFormAccess(reportListItem.getFormCode(), "F:EDIT", reportListItem.getIdn());

		ReportStatus status = sessionBean.getPersistence().getLastReportStatusByReportId(reportListItem.getId(), false, null);
		if (isAutoSave && !status.getStatusCode().equals(ReportStatus.Status.DRAFT.name())) {
			return;
		}

		boolean redirectPage = !status.getStatusCode().equals(ReportStatus.Status.DRAFT.name());

		saveError = !sessionBean.getPersistence().isStatusCompatible(status.getStatusCode(), ReportStatus.Status.DRAFT.name());
		if (saveError) {
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", MessageFormat.format("Невозможно сохранить отчет. Статус отчета - {0}", status.getStatusName("ru"))));
			return;
		} else if (reportListItem.getIdnChild() != null && !sessionBean.getReference().respondentHaveWarrantByIdn(sessionBean.respondent.getRecId(), curDate, reportListItem.getIdnChild())) {
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", "Отсутствует доверенность"));
			return;
		}

		Date reportDate = reportListItem.getReportDate();
		List<Form> forms = sessionBean.getPersistence().getFormsByCodeLanguageReportDate(reportListItem.getFormCode(), sessionBean.languageCode, reportDate, null);
		if (forms.isEmpty()) return;

		Map<String,String> inputValues = getDataFormInputValues(forms.get(0));

		FormHistory formHistory = sessionBean.getPersistence().getFormHistoryWithInputValueChecks(forms.get(0).getFormHistory().getId());
		//boolean redirectPage = sessionBean.getPersistence().getUpdatedCurrentDate(formHistory.getInputValueChecks(), inputValues, curDate).size() > 0;

		Report report = new Report();
		report.setIdn(sessionBean.respondent == null ? null : sessionBean.respondent.getIdn());
		report.setReportDate(reportDate);
		report.setFormCode(reportListItem.getFormCode());
		report.setId(reportListItem.getId());
		sessionBean.getPersistence().saveAndGetId("WEB", report, sessionBean.respondent.getRecId(), forms.get(0).getId(), sessionBean.abstractUser, curDate, inputValues, null, isAutoSave);

		html = reportListItem == null ? null : sessionBean.getPersistence().getHtmlWithReportData(reportListItem.getId(),
				status.getStatusCode().equals(ReportStatus.Status.APPROVED.name()), false);

		status = sessionBean.getPersistence().getLastReportStatusByReportId(reportListItem.getId(), false, null);
		this.status = status.getStatusCode();
		statusName = ReportStatus.resMap.get(sessionBean.languageCode + "_" + status.getStatusCode());

		if(redirectPage){
			try {
				FacesContext ctx = FacesContext.getCurrentInstance();

				ExternalContext extContext = ctx.getExternalContext();
				String url = extContext.encodeActionURL(ctx.getApplication().
						getViewHandler().getActionURL(ctx, "/views/reports/form"));

				extContext.redirect(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/*if(redirectPage) {
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('wDlgSignDate').show()");
			try {
				FacesContext ctx = FacesContext.getCurrentInstance();

				ExternalContext extContext = ctx.getExternalContext();
				String url = extContext.encodeActionURL(ctx.getApplication().
						getViewHandler().getActionURL(ctx, "/views/reports/form"));

				extContext.redirect(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
	}

	public String saveAndQuitAction(){
		if(saveError)
			return null;
		else
			return "/views/reports/view?faces-redirect=true";
	}

	public ReportListItem getCurrentReportListItem(Long id){
		/*if(reportId.equals(openedReportId) && openedReportListItem != null)
			return openedReportListItem;*/

		openedReportListItem = sessionBean.getPersistence().getReportListByReportId(id, sessionBean.languageCode, false);
		openedReportId = id;

		return openedReportListItem;
	}

	public ReportHistoryListItem getCurrentReportHistoryListItem(Long reportHistoryId){
		if(reportId.equals(openedReportHistoryId) && openedReportHistoryListItem != null)
			return openedReportHistoryListItem;

		openedReportHistoryListItem = sessionBean.getPersistence().getReportHistoryListItemById(reportHistoryId, null);
		openedReportHistoryId = reportHistoryId;

		return openedReportHistoryListItem;
	}

	public void performControl(ReportListItem selectedReportListItem, boolean saveControlResult) {
		userBean.checkAccess("RESP:FORM:CONTROL");

		controlResults = new ArrayList<ControlResultItem>();

		List<Form> forms = sessionBean.getPersistence().getFormsByCodeLanguageReportDate(selectedReportListItem.getFormCode(), sessionBean.languageCode, selectedReportListItem.getReportDate(), null);
		if (forms.size() == 0) {
			return;
		}
		Map<String, String> inputValues = getDataFormInputValues(forms.get(0));

		Report report = sessionBean.getPersistence().getReport(selectedReportListItem.getId(), null);
		inputValues = sessionBean.getPersistence().updateCalculatedFields(report.getFormCode(), report.getReportDate(), inputValues, "ru", false);

		if (saveControlResult) {
			controlResults = sessionBean.getPerformControl().runTask(selectedReportListItem.getReportDate(), selectedReportListItem.getFormCode(), report.getIdn(), false, sessionBean.respondent.getRefSubjectTypeRecId(), false);
		} else {
			controlResults = sessionBean.getPerformControl().runTaskUnsaved(selectedReportListItem.getReportDate(), selectedReportListItem.getFormCode(), report.getIdn(), sessionBean.respondent.getRefSubjectTypeRecId(), inputValues, false);
		}

		try {
			AuditEvent auditEvent = new AuditEvent();
			auditEvent.setCodeObject(selectedReportListItem.getFormCode());
			auditEvent.setNameObject(null);
			auditEvent.setIdKindEvent(12L);
			auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
			auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
			auditEvent.setDateIn(selectedReportListItem.getReportDate());
			auditEvent.setRecId(selectedReportListItem.getId());
			auditEvent.setUserId(sessionBean.abstractUser.getId());
			auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());
			sessionBean.getPersistence().insertAuditEvent(auditEvent);
		} catch (Exception e) {
			RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
		}
	}

	public void downloadControlResults(Long id){
		ReportListItem selectedReportListItem = getCurrentReportListItem(id);
		byte[] bytes = null;
		String fileName = "";

		try {

			FileWrapper fileWrapper = sessionBean.getPersistence().controlResultsToExcelFile(controlResults, selectedReportListItem);
			bytes = fileWrapper.getBytes();

			DateFormat dfReportDate = new SimpleDateFormat("dd.MM.yyyy");
			fileName = "control_result_" + dfReportDate.format(selectedReportListItem.getReportDate()) + "_" + selectedReportListItem.getFormCode() + "_" + sessionBean.respondent.getIdn() + "." + fileWrapper.getFileFormat();

		} catch (Exception e) {
			bytes = null;
			fileName = "";
		} finally {
			applicationBean.putFileContentToResponseOutputStream(bytes, "application/vnd.ms-excel", fileName);
		}
	}

	public void prepareControlResultsPdfFile(Long id) {
		ReportListItem selectedReportListItem = getCurrentReportListItem(id);
		try {
			FileWrapper fileWrapper = sessionBean.getPersistence().generateControlResultsPdfFile(controlResults, selectedReportListItem);

			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			PortletContext portletContext = (PortletContext) externalContext.getContext();
			String dir = portletContext.getRealPath("/resources/reports/");
			File file = File.createTempFile("control_result_"+String.valueOf(selectedReportListItem.getId())+"_", ".pdf", new File(dir));

			if(!file.exists()) {
				boolean created = file.createNewFile();
				if(!created)
					throw new Exception("Ошибка при создании pdf-файла");
			}

			FileOutputStream outputStream  = new FileOutputStream(file);
			outputStream.write(fileWrapper.getBytes());
			outputStream.flush();
			outputStream.close();

			pdfFilePath = "/frsi-portlet/resources/reports/" + file.getName();

		} catch (Exception e) {
			pdfFilePath = "";
			RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
		}
	}

	public void refreshFileList(Long id, Long reportHistoryId, String userKind, int fileKind){
		boolean forSuperUser = false;
		long lastReportHistoryId;

		if(userKind.equals("SU"))
			forSuperUser = true;

		if(reportHistoryId == null || reportHistoryId == 0) {
			lastReportHistoryId = sessionBean.getPersistence().getLastReportHistoryIdByReportId(id, forSuperUser, null);
		}else {
			lastReportHistoryId = reportHistoryId;
		}

		if(fileKind == 1) {
			if(userKind.equals("RESP"))
				userBean.checkAccess("RESP:FORM:ATTACH_FILE");
			else if(userKind.equals("SU")) {
				userBean.checkAccess("SU:FORMS:ATTACH_FILE");
			}
			reportFileListItem = sessionBean.getPersistence().getFileListByLinkId(lastReportHistoryId, fileKind, null);
			selectedReportFileItem = null;
		}else if (fileKind == 2) {
			if(userKind.equals("RESP"))
				userBean.checkAccess("RESP:FORM:ATTACH_LETTER");
			else if(userKind.equals("SU")) {
				userBean.checkAccess("SU:FORMS:ATTACH_LETTER");
			}
			reportLetterListItem = sessionBean.getPersistence().getFileListByLinkId(lastReportHistoryId, fileKind, null);
			selectedReportLetterItem = null;
		}else if (fileKind == 3){
			reportRawListItem = sessionBean.getPersistence().getFileListByLinkId(lastReportHistoryId, fileKind, null);
			if(reportRawListItem != null && reportRawListItem.size() > 0) {
				selectedReportRawItem = reportRawListItem.get(0);
			}
			prepareGalleria(id, reportRawListItem, selectedReportRawItem, 3);
		}
	}

	public void getCurObject(String kind, Image curImageIn, List<Image> imageListIn, int fileKind){
		int index = curImageIn.getIndex();
		if(kind.equals("NEXT")) {
			index ++;
			if(index > imageListIn.size() - 1)
				index = 0;
		}else if(kind.equals("PREV")){
			index--;
			if(index < 0)
				index = imageListIn.size() - 1;
		}else if(kind.equals("FIRST")){
			index = 0;
		}else if (kind.equals("LAST")){
			index = imageListIn.size() - 1;
		}
		if(fileKind == 1)
			curImage = imageListIn.get(index);
		else if (fileKind == 2)
			curImageLetter = imageListIn.get(index);
		else if (fileKind == 3) {
			curImageRaw = imageListIn.get(index);
			selectedReportRawItem = reportRawListItem.get(index);
		}else if (fileKind == 4 || fileKind == 5){
			curImageWarrant = imageListIn.get(index);
		}
	}

	public void prepareWarrantGalleriaByStatus(Long id, ReportStatus reportStatus){
		try {
			Long linkId = null;
			int fileKind = 0;
			if(reportStatus.getHaveUserWarrant()){
				linkId = reportStatus.getUserWarrantId();
				fileKind = 4;
			}else if (reportStatus.getHaveRespWarrant()){
				linkId = reportStatus.getRespWarrantId();
				fileKind = 5;
			}
			if(linkId != null) {
				List<AttachedFile> items = sessionBean.getPersistence().getFileListByLinkId(linkId, fileKind, null);
				prepareGalleria(id, items, null, fileKind);
			}
		}catch (Exception e){
			RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка открытия прикрепленного файла", e.getMessage()));
		}
	}

	public void prepareWarrantGalleria(Long id, long warrantId){
		try {
			List<AttachedFile> items = sessionBean.getPersistence().getFileListByLinkId(warrantId, 4, null);
			prepareGalleria(id, items, null, 4);
		}catch (Exception e){
			RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка открытия прикрепленного файла", e.getMessage()));
		}
	}

	public void prepareGalleria(Long id, List<AttachedFile> items, AttachedFile selectedItem, int fileKind){
		List<Image> imageListIn = new ArrayList<Image>();
		Image curImageIn = null;
		try {
			int index = 0;
			int selectIndex = 0;

			ReportListItem item = getCurrentReportListItem(id);

			for(AttachedFile reportFile : items){
				if(fileKind == 3){
					imageListIn.add(createImage(sessionBean.getPersistence().getDataFile(reportFile.getId(), true).getPdfFile(), reportFile.getId(), getTitleForRaw(item), getTitleRightForRaw(item), index));
				}else{
					imageListIn.add(createImage(sessionBean.getPersistence().getDataFile(reportFile.getId(), true).getPdfFile(), reportFile.getId(), reportFile.getFileName(), null, index));
				}
				if(selectedItem != null && reportFile.getId().equals(selectedItem.getId()))
					selectIndex = index;
				index++;
			}

			if(imageListIn.size() > 0) {
				curImageIn = imageListIn.get(selectIndex);
			}

			if(fileKind == 1) {
				imageList = imageListIn;
				curImage = curImageIn;
			}else if (fileKind == 2) {
				imageLetterList = imageListIn;
				curImageLetter = curImageIn;
			}else if (fileKind == 3){
				if(curImageIn == null){
					curImageIn = createImage(sessionBean.getReference().getTemplateData(new Template("tml_raw_template")).getXlsOut(), 1000L, "Неструктурированный отчет", null, 0);
					imageListIn.add(curImageIn);
				}
				imageRawList = imageListIn;
				curImageRaw = curImageIn;
			}else if (fileKind == 4 || fileKind == 5){
				imageWarrantList = imageListIn;
				curImageWarrant = curImageIn;
			}
		} catch (Exception e) {
			RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
		}
	}

	private String getTitleForRaw(ReportListItem item){
		return item.getFormCode() + "-" + item.getFormName() + ", организация: " + item.getRespondentName();
	}

	private String getTitleRightForRaw(ReportListItem item){
		return "дата отчета: " + Convert.getDateStringFromDate(item.getReportDate());
	}

	private Image createImage(byte[] fileIn, Long fileId, String title, String titleRight, int index){
		Image image = new Image();
		try {
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			PortletContext portletContext = (PortletContext) externalContext.getContext();
			String dir = portletContext.getRealPath("/resources/reports/");
			File file = File.createTempFile(String.valueOf(fileId) + "_",
					".pdf", new File(dir));

			if (!file.exists()) {
				boolean created = file.createNewFile();
				if (!created)
					throw new Exception("Ошибка при создании pdf-файла");
			}

			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(fileIn);
			outputStream.flush();
			outputStream.close();

			image.setPath("/frsi-portlet/resources/reports/" + file.getName());
			image.setTitle(title);
			image.setTitleRight(titleRight);
			image.setIndex(index);

		}catch (Exception e) {
			RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
		}

		return image;
	}

	public void onUploadFile(FileUploadEvent event) {
		Date curDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
		String userKind = (String) event.getComponent().getAttributes().get("userKind");
		int fileKind = Integer.parseInt((String) event.getComponent().getAttributes().get("fileKind"));
		List<AttachedFile> items = new ArrayList<AttachedFile>();

		if (fileKind == 1) {
			if(userKind.equals("RESP"))
				userBean.checkAccess("RESP:FORM:ATTACH_FILE");
			else if(userKind.equals("SU"))
				userBean.checkAccess("SU:FORMS:ATTACH_FILE");
			items = reportFileListItem;
		} else if (fileKind == 2){
			if(userKind.equals("RESP"))
				userBean.checkAccess("RESP:FORM:ATTACH_LETTER");
			else if(userKind.equals("SU"))
				userBean.checkAccess("SU:FORMS:ATTACH_LETTER");
			items = reportLetterListItem;
		} else if (fileKind == 3){
			items = reportRawListItem;
		}

		Long id = (Long) event.getComponent().getAttributes().get("reportId");

		ReportListItem reportListItem = sessionBean.getPersistence().getReportListByReportId(id, sessionBean.languageCode, false);

		if(!reportListItem.getStatus().equals("DRAFT") && fileKind != 3){
			RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", "Прикреплять пояснительную записку можно только для статуса Черновик"));
			return;
		} else if (reportListItem.getIdnChild() != null && !sessionBean.getReference().respondentHaveWarrantByIdn(sessionBean.respondent.getRecId(), curDate, reportListItem.getIdnChild())) {
			RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", "Отсутствует доверенность"));
			return;
		}

		UploadedFile uploadedFile = event.getFile();
		byte[] contents = uploadedFile.getContents();
		if (contents == null) return;

		String fileNameWithFormat = uploadedFile.getFileName();
		String fileName = fileNameWithFormat.substring(0, fileNameWithFormat.lastIndexOf("."));
		String contentType = uploadedFile.getContentType();
		String format = fileNameWithFormat.substring(fileNameWithFormat.lastIndexOf(".") + 1);


		if(!checkUplFileName(items, fileNameWithFormat)){
			RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Ошибка", "Файл \"" + fileNameWithFormat + "\" уже существует!"));
			return;
		}

		if(fileName.length() > 100){
			RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", "Слишком длинное наименование файла! Наименование не должно превышать 100 символов."));
			return;
		}

		ReportHistory reportHistory = sessionBean.getPersistence().getLastReportHistoryByReportIdNoLobs(id, false, null);
		AttachedFile reportFile = new AttachedFile();
		reportFile.setLinkId(reportHistory.getId());
		reportFile.setFileDate(curDate);
		reportFile.setFileType(contentType);
		reportFile.setFileName(fileNameWithFormat);
		reportFile.setIdUsr(sessionBean.abstractUser.getId());
		reportFile.setFileKind(fileKind);
		reportFile.setFile(contents);
		reportFile.updateHash();
		if(!format.equalsIgnoreCase("pdf")){
			FileWrapper pdfFile = new FileWrapper();
			pdfFile.setBytes(contents);
			pdfFile.setFileFormat(format);
			try {
				pdfFile = sessionBean.getPersistence().convertFileToPdf(pdfFile, fileName);
			} catch (Exception e) {
				RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
				RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
				return;
			}
			reportFile.setPdfFile(pdfFile.getBytes());
		}
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject(reportListItem.getFormCode());
        auditEvent.setNameObject(reportFile.getFileName());
        auditEvent.setIdKindEvent(34L);
        auditEvent.setDateEvent(curDate);
        auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
        auditEvent.setDateIn(reportListItem.getReportDate());
        auditEvent.setRecId(reportListItem.getId());
        auditEvent.setUserId(sessionBean.abstractUser.getId());
        auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());

		sessionBean.getPersistence().uploadFile(reportFile, auditEvent, null);

        // Set DRAFT status with Report File
        ReportStatus status = new ReportStatus();
        status.setReportHistory(reportHistory);
        status.setStatusCode("DRAFT");
        status.setStatusDate(curDate);
        status.setUserId(sessionBean.abstractUser.getId());
        status.setUserInfo(sessionBean.abstractUser.getDisplayName());
        status.setUserLocation(sessionBean.abstractUser.getLocation());
        String message = getStsMessage(reportFile, status, fileKind, "Добавил(а)");
        status.setMessage(message);
        Long genStatusId = sessionBean.getPersistence().insertReportStatusHistory(status, null);

        if(fileKind == 3){
            status = sessionBean.getPersistence().getLastReportStatusByReportId(reportListItem.getId(), false, null);
            this.status = status.getStatusCode();
            statusName = ReportStatus.resMap.get(sessionBean.languageCode + "_" + status.getStatusCode());
        }

		refreshFileList(reportListItem.getId(), null, userKind, fileKind);
	}

	public boolean checkUplFileName(List<AttachedFile> fileList, String fileNameWithFormat){
		if(fileList != null) {
			for (AttachedFile file : fileList) {
				if (file.getFileName().toUpperCase().equals(fileNameWithFormat.toUpperCase())) {
					return false;
				}
			}
		}
		return true;
	}

	public void onDownloadFile(Long id, String userKind, AttachedFile item, int fileKind) {
		if (fileKind == 1) {
			if (userKind.equals("RESP"))
				userBean.checkAccess("RESP:FORM:ATTACH_FILE");
			else if (userKind.equals("SU"))
				userBean.checkAccess("SU:FORMS:ATTACH_FILE");
		}else if (fileKind == 2) {
			if (userKind.equals("RESP"))
				userBean.checkAccess("RESP:FORM:ATTACH_LETTER");
			else if (userKind.equals("SU"))
				userBean.checkAccess("SU:FORMS:ATTACH_LETTER");
		}

		ReportListItem reportListItem = sessionBean.getPersistence().getReportListByReportId(id, sessionBean.languageCode, false);

		if (item != null){
			AttachedFile reportFile;
			reportFile = sessionBean.getPersistence().getDataFile(item.getId(), false);
			String fileName = reportFile.getFileName();
			byte[] file;
			file = reportFile.getFile();

			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			fileName = Convert.getContentDespositionFilename(fileName, externalContext.getRequestHeaderMap());

			applicationBean.putFileContentToResponseOutputStream(file, reportFile.getFileType(), fileName);
			try {
				AuditEvent auditEvent = new AuditEvent();
				auditEvent.setCodeObject(reportListItem.getFormCode());
				auditEvent.setNameObject(reportFile.getFileName());
				auditEvent.setIdKindEvent(35L);
				auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
				auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
				auditEvent.setDateIn(reportListItem.getReportDate());
				auditEvent.setRecId(reportListItem.getId());
				auditEvent.setUserId(sessionBean.abstractUser.getId());
				auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());
				sessionBean.getPersistence().insertAuditEvent(auditEvent);
			} catch (Exception e) {
				RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
				RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
			}
		}
	}

	public void onDownloadFileZip(Long id){
		if(id != null && id != 0){
			Long reportHistoryId = sessionBean.getPersistence().getLastReportHistoryIdByReportId(id, false, null);
			List<AttachedFile> reportFileList = sessionBean.getPersistence().getFileListWithDataByLinkId(reportHistoryId, 3);
			byte[] zipContent;
			String fileName;
			fileName = "files_" + Convert.dateTimeFormatCompact_.format(sessionBean.getIntegration().getNewDateFromBackEndServer()) + ".zip";
			zipContent = applicationBean.createExcelFilesZipContentByFileList(reportFileList);
			applicationBean.putFileContentToResponseOutputStream(zipContent, "application/zip", fileName);
		}
	}

	public void onDownloadFileByReportHistory(Long reportHistoryId, AttachedFile item, int fileKind) {
		long id = sessionBean.getPersistence().getReportIdByReportHistoryId(reportHistoryId, null);
		onDownloadFile(id, "SU", item, fileKind);
	}

	public void deleteReportFile(Long id, String userKind, AttachedFile item, int fileKind){
		Date curDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
		if(fileKind == 1) {
			if (userKind.equals("RESP"))
				userBean.checkAccess("RESP:FORM:ATTACH_FILE");
			else if (userKind.equals("SU"))
				userBean.checkAccess("SU:FORMS:ATTACH_FILE");
		}else if (fileKind == 2) {
			if (userKind.equals("RESP"))
				userBean.checkAccess("RESP:FORM:ATTACH_LETTER");
			else if (userKind.equals("SU"))
				userBean.checkAccess("SU:FORMS:ATTACH_LETTER");
		}

		ReportListItem reportListItem = sessionBean.getPersistence().getReportListByReportId(id, sessionBean.languageCode, false);

		if(!reportListItem.getStatus().equals("DRAFT") && fileKind != 3){
			RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", "Удалять пояснительную записку можно только для статуса Черновик"));
			return;
		} else if (reportListItem.getIdnChild() != null && !sessionBean.getReference().respondentHaveWarrantByIdn(sessionBean.respondent.getRecId(), curDate, reportListItem.getIdnChild())) {
			RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
			RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", "Отсутствует доверенность"));
			return;
		}

		if(item != null){


            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(reportListItem.getFormCode());
            auditEvent.setNameObject(item.getFileName());
            auditEvent.setIdKindEvent(37L);
            auditEvent.setDateEvent(curDate);
            auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
            auditEvent.setDateIn(reportListItem.getReportDate());
            auditEvent.setRecId(reportListItem.getId());
            auditEvent.setUserId(sessionBean.abstractUser.getId());
            auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());

			sessionBean.getPersistence().deleteFile(item.getId(), auditEvent, null);

            // Set DRAFT status with Report File
            ReportHistory reportHistory = sessionBean.getPersistence().getLastReportHistoryByReportIdNoLobs(id, false, null);
            ReportStatus status = new ReportStatus();
            status.setReportHistory(reportHistory);
            status.setStatusCode("DRAFT");
            status.setStatusDate(curDate);
            status.setUserId(sessionBean.abstractUser.getId());
            status.setUserInfo(sessionBean.abstractUser.getDisplayName());
            status.setUserLocation(sessionBean.abstractUser.getLocation());
            String message = getStsMessage(item, status, fileKind, "Удалил(а)");
            status.setMessage(message);
            Long genStatusId = sessionBean.getPersistence().insertReportStatusHistory(status, null);

            if(fileKind == 3){
                status = sessionBean.getPersistence().getLastReportStatusByReportId(reportListItem.getId(), false, null);
                this.status = status.getStatusCode();
                statusName = ReportStatus.resMap.get(sessionBean.languageCode + "_" + status.getStatusCode());
            }
			refreshFileList(reportListItem.getId(), null, userKind, fileKind);
		}
	}

	private String getStsMessage(AttachedFile reportFile, ReportStatus reportStatus, int fileKind, String text){
        if(fileKind == 1)
            return text + " пояснительную записку: " + reportStatus.getUserInfo() + " [" + sessionBean.abstractUser.getLocation() + "], файл \"" + reportFile.getFileName() + "\"";
        else if (fileKind == 2)
            return  text + " сопроводительное письмо: " + reportStatus.getUserInfo() + " [" + sessionBean.abstractUser.getLocation() + "], файл \"" + reportFile.getFileName() + "\"";
        else if (fileKind == 3)
            return text + " файл: " + reportStatus.getUserInfo() + " [" + sessionBean.abstractUser.getLocation() + "], файл \"" + reportFile.getFileName() + "\"";
        else
            return "";
    }

	public void fixTable(){
		RequestContext.getCurrentInstance().execute("fixTable(" + fixed + ",0)");
	}

	public void onPollSwitch() {
		FacesMessage msg = new FacesMessage(pollEnabled ? MessageFormat.format("Автосохранение включено. Интервал австосохраненния равен {0} мин.", (int) sessionBean.getAutosaveDuration() / 60) : "Автосохранение выключено");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public void refreshReportHistoryItemList(Long id) {
		reportHistoryListItems = null;
		selectedReportHistoryItem = null;
		if (id == null) {
			return;
		}
		reportHistoryListItems = sessionBean.getPersistence().getReportHistoryListByReportId(id, true);
	}

	public String openHistory(){
		ReportStatus reportStatus = sessionBean.getPersistence().getLastReportStatusByReportHistoryId(selectedReportHistoryItem.getId(), null);
		boolean approved = reportStatus.getStatusCode().equals(ReportStatus.Status.APPROVED.name());

		html = selectedReportHistoryItem == null ? null : sessionBean.getPersistence().getHtmlWithReportHistoryData(selectedReportHistoryItem.getId(), true);
		String statusName = selectedReportHistoryItem == null ? null : selectedReportHistoryItem.getStatusName();
		String status = selectedReportHistoryItem == null ? null : selectedReportHistoryItem.getStatus();
		setStatusName(statusName);
		setReportHistoryId(selectedReportHistoryItem.getId());
		setHtml(html);
		setApproved(approved);
		setRightDonwload(userBean.hasPermission("SU:FORMS:DOWNLOAD_EXCEL"));
		setFixed(false);
		setStatus(status);
		setHaveAttachedFile(selectedReportHistoryItem.getHaveAttachedFile());
		setHaveAttachedLetter(selectedReportHistoryItem.getHaveAttachedLetter());
		setCompleteDate(selectedReportHistoryItem == null ? null : selectedReportHistoryItem.getCompleteDate());

		return "/views/su/formdata/historyForm?faces-redirect=true";
	}

	public void refreshStatusesByReportId(Long id, boolean forSuperUser){
		long lastReporthistoryId = sessionBean.getPersistence().getLastReportHistoryIdByReportId(id, forSuperUser, null);
		refreshStatuses(lastReporthistoryId, forSuperUser);
	}

	public void refreshStatuses(Long reportHistoryId, boolean forSuperUser) {
		if (!forSuperUser)
			userBean.checkAccess("RESP:FORM:HISTORY");
		else
			userBean.checkAccess("SU:FORMS:HISTORY");

		if (reportHistoryId == null) {
			statuses = new ArrayList<ReportStatus>();
			return;
		}
		statuses = sessionBean.getPersistence().getReportStatusHistoryByReportHistoryId(reportHistoryId, null);

		((LdmReportStatus) ldmReportStatus).setSrcCollection(statuses);
		((LdmReportStatus) ldmReportStatus).setDraftStatusesHidden(draftStatusesHidden);

		sendCount = 0;
		for (ReportStatus item : statuses)
			if (item.getStatusCode().equals("COMPLETED")) sendCount++;
		setDraftStatusesHidden(draftStatusesHidden);
	}

	public void refreshProps(Long id, boolean forSuperUser){
		ReportListItem reportListItem = sessionBean.getPersistence().getReportListByReportId(id, "ru", forSuperUser);
		formCode = reportListItem.getFormCode();
		formName = reportListItem.getFormName();
		saveDate = reportListItem.getSaveDate();
		statusName = reportListItem.getStatusName();
		statusDate = reportListItem.getStatusDate();
		respondentName = reportListItem.getRespondentNameRu();
		respondentChildName = reportListItem.getChildRespondentNameRu();
		reportDate = reportListItem.getReportDate();

		Date serverDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
		signeInfos = new ArrayList<SignInfo>();
		RefRespondentItem refRespondentItem = sessionBean.getReference().getRespondentByIdn(reportListItem.getIdn(), serverDate);
		List<Long> mustSignRefPostRecIds = sessionBean.getReference().getActiveSubjectTypePosts(refRespondentItem.getRefSubjectTypeRecId());
		RefPostItem filter = new RefPostItem();
		for (Long refPostRecId : mustSignRefPostRecIds) {
			filter.setRecId(refPostRecId);
			List<RefPostItem> refPosts = (List<RefPostItem>) sessionBean.getReference().getRefAbstractByFilterList(RefPostItem.REF_CODE, filter, serverDate);
			if (refPosts.size() > 0) {
				signeInfos.add(new SignInfo(refPosts.get(0), getSignerInfoByReportId(id, false, refPostRecId)));
			}
		}
	}

	public void refreshHistoryProps(Long reportHistoryId){
		saveDate = selectedReportHistoryItem.getSaveDate();
		statusName = selectedReportHistoryItem.getStatusName();
		statusDate = selectedReportHistoryItem.getStatusDate();

		Date serverDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
		signeInfos = new ArrayList<SignInfo>();
		Long id = sessionBean.getPersistence().getReportIdByReportHistoryId(reportHistoryId, null);
		Report report = sessionBean.getPersistence().getReport(id, null);
		RefRespondentItem refRespondentItem = sessionBean.getReference().getRespondentByIdn(report.getIdn(), report.getReportDate());
		List<Long> mustSignRefPostRecIds = sessionBean.getReference().getActiveSubjectTypePosts(refRespondentItem.getRefSubjectTypeRecId());
		RefPostItem filter = new RefPostItem();
		for (Long refPostRecId : mustSignRefPostRecIds) {
			filter.setRecId(refPostRecId);
			List<RefPostItem> refPosts = (List<RefPostItem>) sessionBean.getReference().getRefAbstractByFilterList(RefPostItem.REF_CODE, filter, serverDate);
			if (refPosts.size() > 0) {
				signeInfos.add(new SignInfo(refPosts.get(0), getSignerInfo(reportHistoryId, refPostRecId)));
			}
		}
	}

	private String getSignerInfoByReportId(Long id, boolean forSuperUser, Long refPostRecId) {
		long lastReporthistoryId = sessionBean.getPersistence().getLastReportHistoryIdByReportId(id, forSuperUser, null);
		return getSignerInfo(lastReporthistoryId, refPostRecId);
	}

	private String getSignerInfo(Long reportHistoryId, Long refPostRecId) {
		if (reportHistoryId == null) {
			return "";
		}
		return sessionBean.getPersistence().getSignerInfoByReportIdRefPostRecId(reportHistoryId, refPostRecId);
	}

	public void onOpenHistoryComment(Long reportHistoryId) {
		ReportHistoryListItem selectedReportHistoryItem = sessionBean.getPersistence().getReportHistoryListItemById(reportHistoryId, null);
		if (selectedReportHistoryItem == null) {
			return;
		}
		currentHistoryComment = selectedReportHistoryItem.getComment();
	}

	public void editHistoryComment(long reportHistoryId) {
		ReportHistoryListItem reportHistoryListItem = sessionBean.getPersistence().getReportHistoryListItemById(reportHistoryId, null);
		if (reportHistoryListItem == null) {
			return;
		}
		sessionBean.getPersistence().updateReportHistoryComment(reportHistoryId, currentHistoryComment);
		if (selectedReportHistoryItem != null && selectedReportHistoryItem.getId() == reportHistoryId) {
			selectedReportHistoryItem.setComment(currentHistoryComment);
		}
	}

	public void clearHistoryComment(){
		currentHistoryComment = "";
	}

	// region Getters and setters

	public void setApplicationBean(ApplicationBean applicationBean) {
		this.applicationBean = applicationBean;
	}

	public void setSessionBean(SessionBean sessionBean) {
		this.sessionBean = sessionBean;
	}

	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	public String getHiddenValue() {
		return hiddenValue;
	}

	public void setHiddenValue(String hiddenValue) {
		this.hiddenValue = hiddenValue;
	}

	public boolean isValidationVisible() {
		return validationVisible;
	}

	public void setValidationVisible(boolean validationVisible) {
		this.validationVisible = validationVisible;
	}

	public ValidationResult getValidationResult() {
		return validationResult;
	}

	public String getValidationMessage() {
		return validationMessage;
	}

	public List<Error> getErrors() {
		return errors;
	}

	public boolean isUpdateCalculatedFieldsManually() {
		return updateCalculatedFieldsManually;
	}

	public void setUpdateCalculatedFieldsManually(boolean updateCalculatedFieldsManually) {
		this.updateCalculatedFieldsManually = updateCalculatedFieldsManually;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public Long getReportId() {
		return reportId;
	}

	public void setReportId(Long reportId) {
		this.reportId = reportId;
	}

	public boolean isRightSave() {
		return rightSave;
	}

	public void setRightSave(boolean rightSave) {
		this.rightSave = rightSave;
	}

	public boolean isRightApprove() {
		return rightApprove;
	}

	public void setRightApprove(boolean rightApprove) {
		this.rightApprove = rightApprove;
	}

	public boolean isRightDisapprove() {
		return rightDisapprove;
	}

	public void setRightDisapprove(boolean rightDisapprove) {
		this.rightDisapprove = rightDisapprove;
	}

	public boolean isRightDonwload() {
		return rightDonwload;
	}

	public void setRightDonwload(boolean rightDonwload) {
		this.rightDonwload = rightDonwload;
	}

	public boolean isFixed() {
		return fixed;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	public String getPdfFilePath() {
		return pdfFilePath;
	}

	public void setPdfFilePath(String pdfFilePath) {
		this.pdfFilePath = pdfFilePath;
	}

	public List<ControlResultItem> getControlResults() {
		return controlResults;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isCanAttachedFile() {
		return canAttachedFile;
	}

	public void setCanAttachedFile(boolean canAttachedFile) {
		this.canAttachedFile = canAttachedFile;
	}

	public boolean isHaveAttachedFile() {
		return haveAttachedFile;
	}

	public void setHaveAttachedFile(boolean haveAttachedFile) {
		this.haveAttachedFile = haveAttachedFile;
	}

	public List<AttachedFile> getReportFileListItem() {
		return reportFileListItem;
	}

	public void setReportFileListItem(List<AttachedFile> reportFileListItem) {
		this.reportFileListItem = reportFileListItem;
	}

	public AttachedFile getSelectedReportFileItem() {
		return selectedReportFileItem;
	}

	public void setSelectedReportFileItem(AttachedFile selectedReportFileItem) {
		this.selectedReportFileItem = selectedReportFileItem;
	}

	public boolean isPollEnabled() {
		return pollEnabled;
	}

	public void setPollEnabled(boolean pollEnabled) {
		this.pollEnabled = pollEnabled;
	}

	public boolean isPollDisabled() {
		return !pollEnabled;
	}

	public Long getReportHistoryId() {
		return reportHistoryId;
	}

	public void setReportHistoryId(Long reportHistoryId) {
		this.reportHistoryId = reportHistoryId;
	}

	public Date getCompleteDate() {
		return completeDate;
	}

	public void setCompleteDate(Date completeDate) {
		this.completeDate = completeDate;
	}

	public List<ReportHistoryListItem> getReportHistoryListItems() {
		return reportHistoryListItems;
	}

	public void setReportHistoryListItems(List<ReportHistoryListItem> reportHistoryListItems) {
		this.reportHistoryListItems = reportHistoryListItems;
	}

	public ReportHistoryListItem getSelectedReportHistoryItem() {
		return selectedReportHistoryItem;
	}

	public void setSelectedReportHistoryItem(ReportHistoryListItem selectedReportHistoryItem) {
		this.selectedReportHistoryItem = selectedReportHistoryItem;
	}

	public int getSendCount() {
		return sendCount;
	}

	public void setSendCount(int sendCount) {
		this.sendCount = sendCount;
	}

	public Boolean getDraftStatusesHidden() {
		return draftStatusesHidden;
	}

	public void setDraftStatusesHidden(Boolean draftStatusesHidden) {
		this.draftStatusesHidden = draftStatusesHidden;
		((LdmReportStatus) ldmReportStatus).setDraftStatusesHidden(draftStatusesHidden);
	}

	public LazyDataModel<ReportStatus> getLdmReportStatus() {
		return ldmReportStatus;
	}

	public void setLdmReportStatus(LazyDataModel<ReportStatus> ldmReportStatus) {
		this.ldmReportStatus = ldmReportStatus;
	}

	public List<ReportStatus> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<ReportStatus> statuses) {
		this.statuses = statuses;
	}

	public List<ReportStatus> getFilteredStatuses() {
		return filteredStatuses;
	}

	public void setFilteredStatuses(List<ReportStatus> filteredStatuses) {
		this.filteredStatuses = filteredStatuses;
	}

	public void setFormCode(String formCode) {
		this.formCode = formCode;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public Date getReportDate() {
		return reportDate;
	}

	public Date getSaveDate() {
		return saveDate;
	}

	public Date getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(Date statusDate) {
		this.statusDate = statusDate;
	}

	public List<Image> getImageList() {
		return imageList;
	}

	public Image getCurImage() {
		return curImage;
	}

	public String getCurrentHistoryComment() {
		return currentHistoryComment;
	}

	public void setCurrentHistoryComment(String currentHistoryComment) {
		this.currentHistoryComment = currentHistoryComment;
	}

	public List<AttachedFile> getReportLetterListItem() {
		return reportLetterListItem;
	}

	public AttachedFile getSelectedReportLetterItem() {
		return selectedReportLetterItem;
	}

	public void setSelectedReportLetterItem(AttachedFile selectedReportLetterItem) {
		this.selectedReportLetterItem = selectedReportLetterItem;
	}

	public List<Image> getImageLetterList() {
		return imageLetterList;
	}

	public Image getCurImageLetter() {
		return curImageLetter;
	}

	public boolean isHaveAttachedLetter() {
		return haveAttachedLetter;
	}

	public void setHaveAttachedLetter(boolean haveAttachedLetter) {
		this.haveAttachedLetter = haveAttachedLetter;
	}

	public void setReportLetterListItem(List<AttachedFile> reportLetterListItem) {
		this.reportLetterListItem = reportLetterListItem;
	}

	public Image getCurImageRaw() {
		return curImageRaw;
	}

	public List<Image> getImageRawList() {
		return imageRawList;
	}

	public List<AttachedFile> getReportRawListItem() {
		return reportRawListItem;
	}

	public void setReportRawListItem(List<AttachedFile> reportRawListItem) {
		this.reportRawListItem = reportRawListItem;
	}

	public AttachedFile getSelectedReportRawItem() {
		return selectedReportRawItem;
	}

	public void setSelectedReportRawItem(AttachedFile selectedReportRawItem) {
		this.selectedReportRawItem = selectedReportRawItem;
	}

	public String getFormTypeCode() {
		return formTypeCode;
	}

	public void setFormTypeCode(String formTypeCode) {
		this.formTypeCode = formTypeCode;
	}

	public List<SignInfo> getSigneInfos() {
		return signeInfos;
	}

	public Image getCurImageWarrant() {
		return curImageWarrant;
	}

	public void setCurImageWarrant(Image curImageWarrant) {
		this.curImageWarrant = curImageWarrant;
	}

	public List<Image> getImageWarrantList() {
		return imageWarrantList;
	}

	public void setImageWarrantList(List<Image> imageWarrantList) {
		this.imageWarrantList = imageWarrantList;
	}

	public String getRespondentName() {
		return respondentName;
	}

	public void setRespondentName(String respondentName) {
		this.respondentName = respondentName;
	}

	public String getRespondentChildName() {
		return respondentChildName;
	}

	public void setRespondentChildName(String respondentChildName) {
		this.respondentChildName = respondentChildName;
	}

	// endregion

}
