package mb;

import entities.*;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.model.LazyDataModel;
import util.Convert;
import util.Util;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.portlet.PortletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Nuriddin.Baideuov on 29.05.2015.
 */
@ManagedBean
@SessionScoped
public class FormDataBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{reportBean}")
    private ReportBean reportBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    private Date filterReportDateBegin;
    private Date filterReportDateEnd;

    private String filterSubjectTypesText;
    private List<RefSubjectTypeItem> subjectTypeItems;
    private List<RefSubjectTypeItem> filterSubjectTypes = new ArrayList<RefSubjectTypeItem>();

    private List<Form> filterForms = new ArrayList<Form>();
    private List<Form> forms;
    private String filterFormsText;

    private List<RefNpaItem> filterNPA = new ArrayList<RefNpaItem>();
    private List<RefNpaItem> npaList;
    private String filterNPAText;

    private List<RefRespondentItem> filterRespondents = new ArrayList<RefRespondentItem>();
    private List<RefRespondentItem> respondents;
    private String filterRespondentsText;

    private List<String> filterStatuses;
    private List<SelectItem> allFilterStatuses;
    private String filterStatusesText;

    private List<RefDepartmentItem> filterDeps = new ArrayList<RefDepartmentItem>();
    private List<RefDepartmentItem> deps;
    private String filterDepsText;

    private LazyDataModel<ReportListItem> ldmReportListItem;
    private LazyDataModel<ReportStatus> ldmReportStatus;

    private List<ReportListItem> reportList;
    private List<ReportListItem> filteredReportList;
    private List<ReportListItem> selectedReports = new ArrayList<ReportListItem>();

    private List<ApproveResultItem> approveResultMessages = new ArrayList<ApproveResultItem>();

    private String html;
//    private String statusName;

    private List<ControlResultItem> controlResults;
    private List<ControlResultItemGroup> controlResultGroups;

    private List<ReportStatus> statuses;
    private List<ReportStatus> filteredStatuses;
    private Boolean draftStatusesHidden;
    private int sendCount;

    private List<ReportListItem> outputReportList;

    private boolean pollEnabled;

    private String pdfFilePath;

    private String messageType = "";
    private String messagePanelTitle="";

    private List<ReportHistoryListItem> reportHistoryListItems;
    private ReportHistoryListItem selectedReportHistoryItem;
    private boolean validationVisible;
    private String currentHistoryComment;

    private boolean editNoticeText;
    private String noticeMessage;
    private List<RefElements> variableList;

    private Long approveKindEventId = 22L;
    private Long disApproveKindEventId = 23L;

    private boolean approve;
    private Long reportId;

    private ReportStatus selectedStatus;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public boolean testEnabled(Long reportId){
        Long id = reportId;
        return true;
    }

    @PostConstruct
    public void init() {
        Date dateStart = new Date();

        try {
            if (sessionBean.isEjbNull()) sessionBean.init();

            filterReportDateBegin = filterReportDateEnd = Util.getFirstDayOfCurrentMonth();
            subjectTypeItems = sessionBean.getReference().getRefSubjectTypeListAdvanced(sessionBean.getIntegration().getNewDateFromBackEndServer(), false);
            resetFilterSubjectTypes();

            refreshRefNpa();
            resetFilterForms();
            resetFilterDeps();
            resetFilterRespondents();

            allFilterStatuses = new ArrayList<SelectItem>();
            List<ReportStatus.Status> statuses = ReportStatus.suUserStatuses;
            for (ReportStatus.Status status : statuses)
                allFilterStatuses.add(new SelectItem(status.name(), ReportStatus.resMap.get("ru_" + status.name())));
            filterStatuses = new ArrayList<String>();
            resetFilterStatuses();

            ldmReportListItem = new LdmReportListItem();
            ldmReportStatus = new LdmReportStatus();

            editNoticeText = false;

            // move to method preRender
            // refreshReportList();
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
            refreshReportList();
        } catch (Exception e) {
            applicationBean.redirectToErrorPage(e);
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('statusDialog').show()");
    }

    public void onDateSelect() {
        if (filterReportDateBegin == null && filterReportDateEnd != null) filterReportDateBegin = filterReportDateEnd;
        if (filterReportDateBegin != null && filterReportDateEnd == null) filterReportDateEnd = filterReportDateBegin;
    }

    public void onFilterSubjectTypesShow() {
    }

    public void onFilterSubjectTypesHide() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filterSubjectTypes.size(); i++) {
            sb.append(subjectTypeItems.get(i).getName(sessionBean.languageCode));
            if (i < filterSubjectTypes.size() - 1) sb.append(", ");
        }
        int size = filterSubjectTypes.size();
        if (size == 0) filterSubjectTypesText = "Нет ни одного типа субъекта!";
        else if (size == 1)
            filterSubjectTypesText = filterSubjectTypes.get(0).getName(sessionBean.languageCode);
        else if (size == subjectTypeItems.size()) filterSubjectTypesText = "Все";
        else filterSubjectTypesText = "Несколько типов субъектов (" + size + ")";

        resetFilterForms();
        resetFilterRespondents();
    }

    public void resetFilterSubjectTypes() {
        filterSubjectTypes.clear();
        filterSubjectTypes.addAll(subjectTypeItems);
        filterSubjectTypesText = "Все";
    }

    public void onFilterNPAShow() {
        //resetFilterForms();
    }

    public void onFilterFormsHide() {
        updateFilterFormsText();
    }

    public void onFilterNPAHide() {
        updateFilterNPAText();
        resetFilterForms();
    }

    public void refreshRefNpa(){
        npaList = (List<RefNpaItem>)sessionBean.getReference().getRefAbstractList(RefNpaItem.REF_CODE,  sessionBean.getIntegration().getNewDateFromBackEndServer());
        resetFilterNPA();
    }

    public void onFilterDepsHide(){
        updateFilterDepsText();
        resetFilterRespondents();
    }

    private void updateFilterFormsText() {
        int size = filterForms.size();
        if (size == 0) filterFormsText = "Нет ни одной формы!";
        else if (size == 1) filterFormsText = filterForms.get(0).getFormHistory().getName();
        else if (size == forms.size()) filterFormsText = "Все";
        else filterFormsText = "Несколько форм (" + size + ")";
    }

    private void updateFilterDepsText() {
        int size = filterDeps.size();
        if (size == 0) filterDepsText = "Нет ни одного филиала!";
        else if (size == 1) filterDepsText = filterDeps.get(0).getNameRu();
        else if (size == deps.size()) filterDepsText = "Все";
        else filterDepsText = "Несколько филиалов (" + size + ")";
    }

    public void resetFilterForms() {
        List<Long> stRecIds = new ArrayList<Long>();
        for (RefSubjectTypeItem stItem : filterSubjectTypes)
            stRecIds.add(stItem.getRecId());

        if (stRecIds.size() != 0 && filterNPA.size() != 0)
            forms = sessionBean.getPersistence().getFormsByUserSubjectTypeRecIds(sessionBean.user.getUserId(), stRecIds, filterNPA);
        else
            forms = new ArrayList<Form>();

        filterForms = new ArrayList<Form>();
        filterForms.addAll(forms);

        updateFilterFormsText();
    }

    public void resetFilterNPA() {
        filterNPA = new ArrayList<RefNpaItem>();
        RefNpaItem item = new RefNpaItem();
        item.setId(0L);
        item.setRecId(0L);
        item.setNameRu("Без подвязки НПА");
        item.setCode("0");
        npaList.add(item);
        filterNPA.addAll(npaList);

        updateFilterNPAText();
        resetFilterForms();
    }

    private void updateFilterNPAText() {
        int size = filterNPA.size();
        if (size == 0) filterNPAText = "Нет ни одной НПА!";
        else if (size == 1) filterNPAText = filterNPA.get(0).getNameRu();
        else if (size == npaList.size()) filterNPAText = "Все";
        else filterNPAText = "Выделено (" + size + ")";
    }

    public void resetFilterDeps(){
        deps = (List<RefDepartmentItem>)sessionBean.getReference().getRefAbstractByFilterList(RefDepartmentItem.REF_CODE, new RefDepartmentItem(2L), sessionBean.getIntegration().getNewDateFromBackEndServer());
        RefDepartmentItem item = new RefDepartmentItem();
        item.setId(0L);
        item.setNameRu("Без подвязки филиала");
        item.setCode("0");
        deps.add(item);
        filterDeps = new ArrayList<RefDepartmentItem>();
        filterDeps.addAll(deps);

        updateFilterDepsText();
    }

    public void onFilterRespondentsShow() {
    }

    public void onFilterRespondentsHide() {
        updateFilterRespondentsText();
    }

    private void updateFilterRespondentsText() {
        int size = filterRespondents.size();
        if (size == 0) filterRespondentsText = "Нет ни одного респондента!";
        else if (size == 1) filterRespondentsText = filterRespondents.get(0).getName(sessionBean.languageCode);
        else if (size == respondents.size()) filterRespondentsText = "Все";
        else filterRespondentsText = "Несколько респондентов (" + size + ")";
    }

    public void resetFilterRespondents() {
        List<Long> stRecIds = new ArrayList<Long>();
        for (RefSubjectTypeItem stItem : filterSubjectTypes)
            stRecIds.add(stItem.getRecId());

        if (stRecIds.size() != 0 && filterDeps.size() != 0)
            respondents = sessionBean.getReference().getUserRespsBySTRDepRecIds(sessionBean.user.getUserId(), sessionBean.getIntegration().getNewDateFromBackEndServer(), stRecIds, filterDeps);
        else
            respondents = new ArrayList<RefRespondentItem>();

        filterRespondents = new ArrayList<RefRespondentItem>();
        filterRespondents.addAll(respondents);

        updateFilterRespondentsText();
    }

    public void onFilterStatusesShow() {
    }

    public void onFilterStatusesHide() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filterStatuses.size(); i++) {
            String status = filterStatuses.get(i);
            sb.append(ReportStatus.resMap.get("ru_" + status));
            if (i < filterStatuses.size() - 1) sb.append(", ");
        }
        int size = filterStatuses.size();
        if (size == 0) filterStatusesText = "Нет ни одного статуса!";
        else if (size == 1) filterStatusesText = ReportStatus.resMap.get("ru_" + filterStatuses.get(0));
        else if (size == allFilterStatuses.size()) filterStatusesText = "Все";
        else filterStatusesText = "Несколько статусов (" + size + ")";
    }

    public void resetFilterStatuses() {
        filterStatuses.clear();
        for (SelectItem item : allFilterStatuses) filterStatuses.add(item.getValue().toString());
        filterStatusesText = "Все";
    }

    public void emptyFilterStatuses() {
        filterStatuses.clear();
        filterStatusesText = "Нет ни одного статуса!";
    }

    public void onFiltersToggle() {
    }

    public void clearFilters() {
        filterReportDateBegin = filterReportDateEnd = null;
        filterForms = null;
        resetFilterSubjectTypes();
        resetFilterStatuses();
        resetFilterForms();
        resetFilterNPA();
        resetFilterDeps();
        resetFilterRespondents();
    }

    public void onSelectAll(ToggleSelectEvent event) {
        if (event.isSelected()) {
            selectedReports.clear();
            selectedReports.addAll(reportList);
        } else {
            selectedReports.clear();
        }
    }

    public void refreshReportList() {
        List<String> formCodes = new ArrayList<String>();
        for (Form form : filterForms) {
            formCodes.add(form.getCode());
        }

        List<String> idnList = new ArrayList<String>();
        for (RefRespondentItem respondent : filterRespondents)
            idnList.add(respondent.getIdn());

        reportList = sessionBean.getPersistence().getReportListByUserIdnListFormCodesReportDateRange(sessionBean.user.getUserId(), idnList, formCodes,
                filterNPA, filterReportDateBegin, filterReportDateEnd, sessionBean.languageCode);

        // Apply additional filters
        Iterator<ReportListItem> it = reportList.iterator();
        while (it.hasNext()) {
            ReportListItem item = it.next();

            boolean matchStatus = false;
            for (String filterStatus : filterStatuses)
                if (item.getStatus().equals(filterStatus)) {
                    matchStatus = true;
                    break;
                }
            if (!matchStatus) it.remove();
        }

        for (ReportListItem item : reportList) {
            item.setControlResultName(ControlResultType.resMap.get(sessionBean.languageCode + "_" + item.getControlResultCode()));
        }
        ((LdmReportListItem) ldmReportListItem).setSrcCollection(reportList);

        // Selection
        for (ReportListItem reportListItem : reportList) {
            int index = selectedReports.indexOf(reportListItem);
            if (index > -1) {
                selectedReports.remove(index);
                selectedReports.add(reportListItem);
            }
        }
    }

    public void refreshReportHistoryItemList() {
        if (selectedReports.size() == 0) {
            return;
        }
        ReportListItem selectedReportListItem = selectedReports.get(0);
        reportHistoryListItems = null;
        selectedReportHistoryItem = null;
        reportHistoryListItems = sessionBean.getPersistence().getReportHistoryListByReportId(selectedReportListItem.getId(), true);
    }

    public String open() {
        String result;
        if (selectedReports.size() != 1) {
            return null;
        }
        ReportListItem selectedReportListItem = selectedReports.get(0);
        ReportStatus reportStatus = sessionBean.getPersistence().getLastReportStatusByReportId(selectedReportListItem.getId(), true, null);
        if (!sessionBean.getPersistence().isStatusCompatible(reportStatus.getStatusCode(), "SU_OPEN")) {
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", MessageFormat.format("Невозможно открыть отчет. Статус отчета - {0}", reportStatus.getStatusName("ru"))));
            return null;
        }

        boolean approved = reportStatus.getStatusCode().equals(ReportStatus.Status.APPROVED.name());
        String statusName = selectedReportListItem == null ? null : selectedReportListItem.getStatusName();
        String status = selectedReportListItem == null ? null : selectedReportListItem.getStatus();
        clearErrors();
        reportBean.setStatusName(statusName);
        reportBean.setReportId(selectedReportListItem.getId());
        reportBean.setApproved(approved);
        reportBean.setRightApprove(userBean.hasPermissionForm(selectedReportListItem.getFormCode(), "F:APPROVE", selectedReportListItem.getIdn()));
        reportBean.setRightDisapprove(userBean.hasPermissionForm(selectedReportListItem.getFormCode(), "F:DISAPPROVE", selectedReportListItem.getIdn()));
        reportBean.setRightDonwload(userBean.hasPermission("SU:FORMS:DOWNLOAD_EXCEL"));
        reportBean.setFixed(false);
        reportBean.setStatus(status);
        reportBean.setCanAttachedFile(selectedReportListItem.getCanAttachedFile());
        reportBean.setHaveAttachedFile(selectedReportListItem.getHaveAttachedFile());
        reportBean.setHaveAttachedLetter(selectedReportListItem.getHaveAttachedLetter());
        reportBean.setFormCode(selectedReportListItem.getFormCode());

        if(selectedReports.get(0).getFormTypeCode().equals(Form.Type.INPUT_RAW.name())){
            reportBean.refreshFileList(selectedReportListItem.getId(), null, "RESP", 3);
            result = "/views/su/formdata/form_raw?faces-redirect=true";
        }else{
            //html = selectedReportListItem == null ? null : sessionBean.getPersistence().getHtmlWithDataByReportListItem(selectedReportListItem, sessionBean.languageCode);
            html = selectedReportListItem == null ? null : sessionBean.getPersistence().getHtmlWithReportData(selectedReportListItem.getId(), true, true);
            reportBean.setHtml(html);
            result = "/views/su/formdata/form?faces-redirect=true";
        }
//        refreshErrors();
        return result;
    }

    public String openHistory(Long reportHistoryId){
        ReportHistoryListItem selectedReportHistoryItem = sessionBean.getPersistence().getReportHistoryListItemById(reportHistoryId, null);
        long reportId = sessionBean.getPersistence().getReportIdByReportHistoryId(reportHistoryId, null);
        long lastReportHistoryId = sessionBean.getPersistence().getLastReportHistoryIdByReportId(reportId, true, null);
        selectedReportHistoryItem.setCurrent(lastReportHistoryId == reportHistoryId);

        if (selectedReportHistoryItem.isCurrent()) {
            return open();
        }

        ReportListItem selectedReportListItem = sessionBean.getPersistence().getReportListByReportId(reportId, "ru", true);
        reportBean.setReportId(selectedReportListItem.getId());
        reportBean.setCanAttachedFile(selectedReportListItem.getCanAttachedFile());
        reportBean.setSelectedReportHistoryItem(selectedReportHistoryItem);
        reportBean.setFormName(selectedReportListItem != null ? selectedReportListItem.getFormName() : "");
        reportBean.setFormCode(selectedReportListItem != null ? selectedReportListItem.getFormCode() : "");
        reportBean.setReportDate(selectedReportListItem.getReportDate());
        reportBean.setFormCode(selectedReportListItem.getFormCode());
        return reportBean.openHistory();
    }

    public String openCurrentVersion(Long reportId){
        return "/views/su/formdata/form?faces-redirect=true";
    }

    public void onShowErrors() {
        userBean.checkAccess("SU:FORMS:ERRORS");
//        refreshErrors();
    }

    public void approveReports(boolean approve) {
        this.approve = approve;
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();
        if(editNoticeText){
            noticeMessage = sessionBean.getPersistence().getNoticeMessageById(approve ? approveKindEventId : disApproveKindEventId);
            context.execute("PF('wDlgMessage').show()");
            context.update(liferayNameSpace + ":frmMessage");
        }else {
            approveResultMessages.clear();
            Date approvalDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
            for (ReportListItem reportListItem : selectedReports) {
                String msg;
                if (approve) {
                    msg = doApprove(reportListItem, approvalDate);
                } else {
                    msg = doDisapprove(reportListItem, approvalDate);
                }
                if (msg.isEmpty()) {
                    approveResultMessages.add(new ApproveResultItem(reportListItem.getId(),
                            reportListItem.getFormName(), reportListItem.getRespondentNameRu(),
                            reportListItem.getReportDate(), "", ApproveResultItem.ResultType.SUCCESS));
                } else {
                    approveResultMessages.add(new ApproveResultItem(reportListItem.getId(),
                            reportListItem.getFormName(), reportListItem.getRespondentNameRu(),
                            reportListItem.getReportDate(), msg));
                }
            }
            Collections.sort(approveResultMessages, new OperationResultComparator());
            context.update(liferayNameSpace + ":frmMain");
            if (approve) {
                context.execute("PF('wDlgConfirmApprove').hide()");
                context.update(liferayNameSpace + ":frmApproveResults");
                context.execute("PF('wDlgApproveResults').show()");
            } else {
                context.execute("PF('wDlgConfirmDisapprove').hide()");
                context.update(liferayNameSpace + ":frmDisApproveResults");
                context.execute("PF('wDlgDisApproveResults').show()");
            }
            refreshReportList();
        }
    }

    public boolean approve(ReportListItem repListItem, Long id) {
        ReportListItem reportListItem;
        if (repListItem == null)
            reportListItem = reportBean.getCurrentReportListItem(id);
        else
            reportListItem = repListItem;

        Date approvalDate = sessionBean.getIntegration().getNewDateFromBackEndServer();

        String msg = doApprove(reportListItem, approvalDate);

        refreshReportList();

        if (msg.isEmpty()) {
            return true;
        } else {
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", msg));
            return false;
        }
    }

    private String doApprove(ReportListItem reportListItem, Date approvalDate){
        if (!userBean.hasPermissionForm(reportListItem.getFormCode(), "F:APPROVE", reportListItem.getIdn())) {
            return "Не достаточно прав";
        }

        ReportStatus status = sessionBean.getPersistence().getLastReportStatusByReportId(reportListItem.getId(), true, null);
        if (!sessionBean.getPersistence().isStatusCompatible(status.getStatusCode(), ReportStatus.Status.APPROVED.name())) {
            return MessageFormat.format("Статус отчета уже \"{0}\"", reportListItem.getStatusName());
        }

        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject(reportListItem.getFormCode());
        auditEvent.setNameObject(null);
        auditEvent.setIdKindEvent(approveKindEventId);
        auditEvent.setDateEvent(approvalDate);
        auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
        auditEvent.setDateIn(reportListItem.getReportDate());
        auditEvent.setRecId(reportListItem.getId());
        auditEvent.setUserId(sessionBean.abstractUser.getId());
        auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());
        auditEvent.setAddMessage(noticeMessage);

        sessionBean.getPersistence().setReportApproved(reportListItem.getId(), true, sessionBean.abstractUser, approvalDate, auditEvent);

        return "";
    }

    public boolean disapprove(ReportListItem repListItem, Long id) {
        ReportListItem reportListItem;
        if(repListItem == null)
            reportListItem = reportBean.getCurrentReportListItem(id);
        else
            reportListItem = repListItem;

        Date disapproveDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
        String msg = doDisapprove(reportListItem, disapproveDate);

        refreshReportList();

        if (msg.isEmpty()) {
            return true;
        } else {
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", msg));
            return false;
        }
    }

    private String doDisapprove(ReportListItem reportListItem, Date disapproveDate){
        if(!userBean.hasPermissionForm(reportListItem.getFormCode(), "F:DISAPPROVE", reportListItem.getIdn())){
            return "Не достаточно прав";
        }

        ReportStatus status = sessionBean.getPersistence().getLastReportStatusByReportId(reportListItem.getId(), true, null);
        if(!sessionBean.getPersistence().isStatusCompatible(status.getStatusCode(), ReportStatus.Status.DISAPPROVED.name())){
            return MessageFormat.format("Статус отчета уже - \"{0}\"", reportListItem.getStatusName());
        }

        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject(reportListItem.getFormCode());
        auditEvent.setNameObject(null);
        auditEvent.setIdKindEvent(disApproveKindEventId);
        auditEvent.setDateEvent(disapproveDate);
        auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
        auditEvent.setDateIn(reportListItem.getReportDate());
        auditEvent.setRecId(reportListItem.getId());
        auditEvent.setUserId(sessionBean.abstractUser.getId());
        auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());
        auditEvent.setAddMessage(noticeMessage);

        sessionBean.getPersistence().setReportApproved(reportListItem.getId(), false, sessionBean.abstractUser, disapproveDate, auditEvent);

        return "";
    }

    private void refreshOutputReportList(Long id) {
        outputReportList = sessionBean.getPersistence().getOutputReportListByInputReportId(id, sessionBean.languageCode);
        for (ReportListItem item : outputReportList)
            item.setStatusName(ReportStatus.resMap.get(sessionBean.languageCode + "_" + item.getStatus()));
    }

    public void approveAndUpdate(ReportListItem repListItem,Long id) {
        this.approve = true;
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();
        reportId = id;
        if(editNoticeText){
            noticeMessage = sessionBean.getPersistence().getNoticeMessageById(approve ? approveKindEventId : disApproveKindEventId);
            context.execute("PF('wDlgMessage').show()");
            context.update(liferayNameSpace + ":frmMessage");
        }else {
            if (approve(repListItem, id)) {
                updateStatus(repListItem, id);
            }
            context.update(liferayNameSpace + ":dynamic-form:toolbar");
            context.execute("PF('wDlgConfirmApprove').hide()");
        }
    }

    public void disapproveAndUpdate(ReportListItem repListItem,Long id) {
        this.approve = false;
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();
        reportId = id;
        if(editNoticeText){
            noticeMessage = sessionBean.getPersistence().getNoticeMessageById(approve ? approveKindEventId : disApproveKindEventId);
            context.execute("PF('wDlgMessage').show()");
            context.update(liferayNameSpace + ":frmMessage");
        }else {
            if (disapprove(repListItem, id)) {
                updateStatus(repListItem, id);
            }
            context.update(liferayNameSpace + ":dynamic-form:toolbar");
            context.execute("PF('wDlgConfirmDisapprove').hide()");
        }
    }

    private void updateStatus(ReportListItem repListItem,Long id){
        ReportListItem reportListItem;
        if (repListItem == null)
            reportListItem = reportBean.getCurrentReportListItem(id);
        else
            reportListItem = repListItem;
        if (reportListItem != null) {
            ReportStatus status = sessionBean.getPersistence().getLastReportStatusByReportId(reportListItem.getId(), true, null);
            String statusName = ReportStatus.resMap.get(sessionBean.languageCode + "_" + status.getStatusCode());
            reportBean.setStatusName(statusName);
            reportBean.setApproved(status.getStatusCode().equalsIgnoreCase(ReportStatus.Status.APPROVED.name()));
        }
    }

    public boolean approveButtonEnabled() {
        if (selectedReports.size() == 0) {
            return false;
        }
        if(selectedReports.size()==1) {
            ReportListItem reportListItem = selectedReports.get(0);
            if (reportListItem.getStatus().equals(ReportStatus.Status.APPROVED.toString()) || reportListItem.getStatus().equals(ReportStatus.Status.DRAFT.toString())) {
                return false;
            }
            if (!userBean.hasPermissionForm(reportListItem.getFormCode(), "F:APPROVE", reportListItem.getIdn())) {
                return false;
            }
        }
        return true;
    }

    public boolean disapproveButtonEnabled() {
        if (selectedReports.size() == 0) {
            return false;
        }
        if(selectedReports.size()==1) {
            ReportListItem reportListItem = selectedReports.get(0);
            if (!reportListItem.getStatus().equals(ReportStatus.Status.APPROVED.toString())) {
                return false;
            }
            if (!userBean.hasPermissionForm(reportListItem.getFormCode(), "F:DISAPPROVE", reportListItem.getIdn())) {
                return false;
            }
        }
        return true;
    }

    public boolean buttonEnabled(String permission) {
        if (selectedReports.size() == 0)
            return false;
        else if (!userBean.hasPermission(permission))
            return false;
        else {
            for (ReportListItem item : selectedReports) {
                if (item.getFormTypeCode().equals(Form.Type.INPUT_RAW.name()))
                    return false;
            }
        }
        return true;
    }

    public void performControlAll(boolean extSysControls) {
        userBean.checkAccess("SU:FORMS:CONTROL");

        List<Report> reports = new ArrayList<Report>();
        for (ReportListItem reportListItem : selectedReports) {
            reports.add(sessionBean.getPersistence().getReport(reportListItem.getId(), null));
        }

        controlResultGroups = sessionBean.getPerformControl().runTaskNGetGrouped(reports, true, sessionBean.abstractUser.getId(),
                sessionBean.abstractUser.getLocation(), sessionBean.respondent.getId(), extSysControls);

        refreshReportList();
    }

    public void performControl(Long repId) {
        userBean.checkAccess("SU:FORMS:CONTROL");

        validationVisible = true;

        ReportListItem reportListItem = reportBean.getCurrentReportListItem(repId);
        Report report = sessionBean.getPersistence().getReport(repId, null);
        RefRespondentItem respondent = sessionBean.getReference().getRespondentByIdn(report.getIdn(), report.getReportDate());
        controlResults = sessionBean.getPerformControl().runTask(reportListItem.getReportDate(), reportListItem.getFormCode(), report.getIdn(), true, respondent.getRefSubjectTypeRecId(), false);

        try {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(reportListItem.getFormCode());
            auditEvent.setNameObject(null);
            auditEvent.setIdKindEvent(38L);
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

    public void clearErrors() {
        validationVisible = false;
    }

    public void downloadControlResults(ReportListItem repListItem, Long repId){
        ReportListItem reportListItem;
        if(repListItem == null)
            reportListItem = reportBean.getCurrentReportListItem(repId);
        else
            reportListItem = repListItem;

        byte[] bytes = null;
        String fileName = "";

        try {

            FileWrapper fileWrapper = sessionBean.getPersistence().controlResultsToExcelFile(controlResults, reportListItem);
            bytes = fileWrapper.getBytes();

            DateFormat dfReportDate = new SimpleDateFormat("dd.MM.yyyy");
            fileName = "control_result_" + dfReportDate.format(reportListItem.getReportDate()) + "_" + reportListItem.getFormCode() + "_" + sessionBean.respondent.getIdn() + "." + fileWrapper.getFileFormat();

        } catch (Exception e) {
            bytes = null;
            fileName = "";
        } finally {
            applicationBean.putFileContentToResponseOutputStream(bytes, "application/vnd.ms-excel", fileName);
        }
    }

    public void downloadControlResultsAll(){
        byte[] bytes = null;
        String fileName = "";

        try {

            FileWrapper fileWrapper = sessionBean.getPersistence().controlResultGroupsToExcelFile(controlResultGroups);
            bytes = fileWrapper.getBytes();

            fileName = "control_result_" + sessionBean.respondent.getIdn() + "." + fileWrapper.getFileFormat();
        } catch (Exception e) {
            bytes = null;
            fileName = "";
        } finally {
            applicationBean.putFileContentToResponseOutputStream(bytes, "application/vnd.ms-excel", fileName);
        }
    }

    public void downloadExtControlResultsAll(){
        byte[] bytes = null;
        String fileName = "";

        try {

            FileWrapper fileWrapper = sessionBean.getPersistence().extControlResultGroupsToExcelFile(controlResultGroups);
            bytes = fileWrapper.getBytes();

            fileName = "control_result_" + sessionBean.respondent.getIdn() + "." + fileWrapper.getFileFormat();
        } catch (Exception e) {
            bytes = null;
            fileName = "";
        } finally {
            applicationBean.putFileContentToResponseOutputStream(bytes, "application/vnd.ms-excel", fileName);
        }
    }

    public void prepareControlResultsPdfFile(ReportListItem repListItem, Long repId) {
        ReportListItem reportListItem;
        if(repListItem == null)
            reportListItem = reportBean.getCurrentReportListItem(repId);
        else
            reportListItem = repListItem;
        try {
            FileWrapper fileWrapper = sessionBean.getPersistence().generateControlResultsPdfFile(controlResults, reportListItem);

            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            PortletContext portletContext = (PortletContext) externalContext.getContext();
            String dir = portletContext.getRealPath("/resources/reports/");
            File file = File.createTempFile("control_result_"+String.valueOf(reportListItem.getId())+"_", ".pdf", new File(dir));

            if(!file.exists()) {
                boolean created = file.createNewFile();
                if (!created)
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

    // TODO: 6/13/16 неизвестный метод
    /*public StreamedContent export() {
        byte[] bytes = sessionBean.getPersistence().getExcelFileContentForReport(getHtml());
        //DateFormat dfReportDate = new SimpleDateFormat("yyyy-MM-dd");
        String fileName = selectedReportListItem.getReportDate() + "_" + selectedReportListItem.getFormCode() + ".xls";
        applicationBean.putFileContentToResponseOutputStream(bytes, "application/vnd.ms-excel", fileName);
        return null;
    }*/

    public void prepareControlResultsAllPdfFile() {
        try {
            FileWrapper fileWrapper = sessionBean.getPersistence().generateControlResultGroupsPdfFile(controlResultGroups);

            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            PortletContext portletContext = (PortletContext) externalContext.getContext();
            String dir = portletContext.getRealPath("/resources/reports/");
            File file = File.createTempFile("control_result_all_", ".pdf", new File(dir));

            if(!file.exists()) {
                boolean created = file.createNewFile();
                if (!created)
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

    public void refreshStatuses() {
        userBean.checkAccess("SU:FORMS:HISTORY");
        if (selectedReports.size() == 0) {
            return;
        }
        ReportListItem selectedReportListItem = selectedReports.get(0);
        if (selectedReportListItem == null) {
            statuses = new ArrayList<ReportStatus>();
            return;
        }
        statuses = sessionBean.getPersistence().getReportStatusHistoryByReportId(selectedReportListItem.getId(), true, null);

        ((LdmReportStatus) ldmReportStatus).setSrcCollection(statuses);
        ((LdmReportStatus) ldmReportStatus).setDraftStatusesHidden(draftStatusesHidden);

        sendCount = 0;
        for (ReportStatus item : statuses)
            if (item.getStatusCode().equals("COMPLETED")) sendCount++;
        setDraftStatusesHidden(true);
    }

    public void download(){
        if(selectedReports.size()==1){
            downloadSingleFile(selectedReports.get(0), null);
        } else {
            downloadMultipleFilesAsZip();
        }
    }

    public void downloadSingleFile(ReportListItem repListItem, Long repId) {
        ReportListItem reportListItem;
        if(repListItem == null)
            reportListItem = reportBean.getCurrentReportListItem(repId);
        else
            reportListItem = repListItem;

        byte[] bytes = null;
        String fileName = "";

        try {
            userBean.checkAccess("SU:FORMS:DOWNLOAD_EXCEL");

            FileWrapper fileWrapper = sessionBean.getPersistence().generateExcelFile(reportListItem, true);
            bytes = fileWrapper.getBytes();

            DateFormat dfReportDate = new SimpleDateFormat("dd.MM.yyyy");
            fileName = dfReportDate.format(reportListItem.getReportDate()) + "_" + reportListItem.getFormCode() + "_draft." + fileWrapper.getFileFormat();

        } catch (Exception e) {
            bytes = null;
            fileName = "";
        } finally {
            applicationBean.putFileContentToResponseOutputStream(bytes, "application/vnd.ms-excel", fileName);
        }
        try {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(reportListItem.getFormCode());
            auditEvent.setNameObject(null);
            auditEvent.setIdKindEvent(10L);
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

    public void downloadMultipleFilesAsZip() {
        DateFormat dfReportDate = new SimpleDateFormat("dd.MM.yyyy");
        List<FileWrapper> fileWrappers = new ArrayList<FileWrapper>();
        List<AuditEvent> auditEvents = new ArrayList<AuditEvent>();
        boolean hasError = false;
        if (!userBean.hasPermission("SU:FORMS:DOWNLOAD_EXCEL")) {
            hasError = true;
        }
        if (!hasError) {
            for (ReportListItem selectedReportListItem : selectedReports) {
                try {
                    FileWrapper fileWrapper = sessionBean.getPersistence().generateExcelFile(selectedReportListItem, true);
                    String fileName = dfReportDate.format(selectedReportListItem.getReportDate()) + "_" + fileWrapper.getFileName() + "_" + selectedReportListItem.getFormCode() + "_draft." + fileWrapper.getFileFormat();
                    fileWrapper.setFileName(fileName);
                    fileWrappers.add(fileWrapper);
                } catch (Exception e) {
                    hasError = true;
                    break;
                }

                AuditEvent auditEvent = new AuditEvent();
                auditEvent.setCodeObject(selectedReportListItem.getFormCode());
                auditEvent.setNameObject(null);
                auditEvent.setIdKindEvent(10L);
                auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
                auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
                auditEvent.setDateIn(selectedReportListItem.getReportDate());
                auditEvent.setRecId(selectedReportListItem.getId());
                auditEvent.setUserId(sessionBean.abstractUser.getId());
                auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());
                auditEvents.add(auditEvent);
            }
        }
        byte[] zipContent;
        String fileName;
        if (!hasError) {
            fileName = "reports_" + Convert.dateTimeFormatCompact_.format(sessionBean.getIntegration().getNewDateFromBackEndServer()) + ".zip";
            zipContent = applicationBean.createExcelFilesZipContent(fileWrappers);
        } else {
            fileName = "";
            zipContent = null;
        }
        applicationBean.putFileContentToResponseOutputStream(zipContent, "application/zip", fileName);
        if (!hasError) {
            for (AuditEvent auditEvent : auditEvents) {
                try {
                    sessionBean.getPersistence().insertAuditEvent(auditEvent);
                } catch (Exception e) {
                    RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
                    RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
                }
            }
        }
    }

    public void downloadByReportHistoryId(Long reportHistoryId) {
        byte[] bytes = null;
        String fileName = "";
        long reportId = sessionBean.getPersistence().getReportIdByReportHistoryId(reportHistoryId, null);
        if (reportId == 0) {
            return;
        }
        Report report = sessionBean.getPersistence().getReport(reportId, null);
        try {
            userBean.checkAccess("SU:FORMS:DOWNLOAD_EXCEL");

            FileWrapper fileWrapper = sessionBean.getPersistence().generateExcelFileFromReportHistory(reportHistoryId);
            bytes = fileWrapper.getBytes();

            DateFormat dfReportDate = new SimpleDateFormat("dd.MM.yyyy");
            fileName = dfReportDate.format(report.getReportDate()) + "_" + report.getFormCode() + "_draft." + fileWrapper.getFileFormat();

        } catch (Exception e) {
            bytes = null;
            fileName = "";
        } finally {
            applicationBean.putFileContentToResponseOutputStream(bytes, "application/vnd.ms-excel", fileName);
        }
        try {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(report.getFormCode());
            auditEvent.setNameObject(null);
            auditEvent.setIdKindEvent(10L);
            auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
            auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
            auditEvent.setDateIn(report.getReportDate());
            auditEvent.setRecId(report.getId());
            auditEvent.setUserId(sessionBean.abstractUser.getId());
            auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());
            sessionBean.getPersistence().insertAuditEvent(auditEvent);
        } catch (Exception e) {
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
        }
    }

    public void preparePdfFile(ReportListItem repListItem, Long repId) {
        ReportListItem reportListItem;
        if(repListItem == null)
            reportListItem = reportBean.getCurrentReportListItem(repId);
        else
            reportListItem = repListItem;
        try {
            userBean.checkAccess("SU:FORMS:PRINT");

            FileWrapper fileWrapper = sessionBean.getPersistence().generatePdfFile(reportListItem, true);

            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            PortletContext portletContext = (PortletContext) externalContext.getContext();
            String dir = portletContext.getRealPath("/resources/reports/");
            File file = File.createTempFile(String.valueOf(reportListItem.getId())+"_", ".pdf", new File(dir));
            if(!file.exists())
                file.createNewFile();

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
        try {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(reportListItem.getFormCode());
            auditEvent.setNameObject(null);
            auditEvent.setIdKindEvent(11L);
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

    public void preparePdfFileForReportHistory(Long reportHistoryId) {
        if (reportHistoryId == null) {
            return;
        }
        long reportId = sessionBean.getPersistence().getReportIdByReportHistoryId(reportHistoryId, null);
        if (reportId == 0) {
            return;
        }
        Report report = sessionBean.getPersistence().getReport(reportId, null);
        try {
            userBean.checkAccess("SU:FORMS:PRINT");

            FileWrapper fileWrapper = sessionBean.getPersistence().generatePdfFileFromReportHistory(reportHistoryId);

            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            PortletContext portletContext = (PortletContext) externalContext.getContext();
            String dir = portletContext.getRealPath("/resources/reports/");
            File file = File.createTempFile(String.valueOf(reportHistoryId)+"_", ".pdf", new File(dir));
            if(!file.exists())
                file.createNewFile();

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
        try {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(report.getFormCode());
            auditEvent.setNameObject(null);
            auditEvent.setIdKindEvent(11L);
            auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
            auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
            auditEvent.setDateIn(report.getReportDate());
            auditEvent.setRecId(report.getId());
            auditEvent.setUserId(sessionBean.abstractUser.getId());
            auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());
            sessionBean.getPersistence().insertAuditEvent(auditEvent);
        } catch (Exception e) {
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
        }
    }

    public void onPollSwitch() {
        FacesMessage msg = new FacesMessage(pollEnabled ? "Автообновление включено" : "Автообновление выключено");
        FacesContext.getCurrentInstance().addMessage(null, msg);
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

    public int getSuccessOperationCount(List<ApproveResultItem> items){
        int result = 0;
        for(ApproveResultItem item:items){
            if(item.getResultType()!=null && item.getResultType().equals(ApproveResultItem.ResultType.SUCCESS)){
                result++;
            }
        }
        return result;
    }

    public int getFailOperationCount(List<ApproveResultItem> items){
        int result = 0;
        for(ApproveResultItem item:items){
            if(item.getResultType()!=null && item.getResultType().equals(ApproveResultItem.ResultType.FAIL)){
                result++;
            }
        }
        return result;
    }

    public String getRowStyleClass(ReportListItem item) {
        String result = null;
        ReportStatus.Status status = ReportStatus.Status.ERROR;
        try {
            status = ReportStatus.Status.valueOf(item.getStatus());
        } catch (IllegalArgumentException e) {
        }
        ;
        switch (status) {
            case SIGNED:
                result = "customRowBlue";
                break;
            case ERROR:
                result = "customRowRed";
                break;
            case COMPLETED:
            case DISAPPROVED:
                result = "customRowTeal";
                break;
            case APPROVED:
                result = "customRowGreen";
                break;
        }

        String controlResultStyleClass = "";
        if (item.getControlResultCode() != null) {
            Long controlResultType = 3L;
            try {
                ControlResult.ResultType rt = ControlResult.ResultType.valueOf(item.getControlResultCode());
                controlResultType = rt.getId();
            } catch (IllegalArgumentException e) {
            }
            controlResultStyleClass = getControlRowStyleClass(controlResultType);
        }

        return result + " " + controlResultStyleClass;
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
            controlResultStyleClass = getControlRowStyleClass(controlResultType);
        }

        return controlResultStyleClass;
    }

    public String getControlRowStyleClass(Long controlResultType) {
        if (controlResultType == null) return "";
        String result = "";
        switch (controlResultType.intValue()) {
            case 1:
                result = "controlSuccess";
                break;
            case 2:
            case 3:
                result = "controlFail";
                break;
        }
        return result;
    }

    public String getControlColumnStyleClass(Long controlResultType) {
        if (controlResultType == null) return "";
        String result = "";
        switch (controlResultType.intValue()) {
            case 1:
                result = "controlSuccessColumn";
                break;
            case 2:
            case 3:
                result = "controlFailColumn";
                break;
        }
        return result;
    }

    public String getOperationRowStyleClass(ApproveResultItem.ResultType resultType) {
        if (resultType == null) return "";
        String result = "";
        switch (resultType) {
            case SUCCESS:
                result = "operationSuccess";
                break;
            case FAIL:
                result = "operationFail";
                break;
        }
        return result;
    }

    public void onDlgMessageHide(){
        editNoticeText = false;
        approveReports(approve);
    }

    public void onDlgMessageHideFromForm(){
        editNoticeText = false;
        if(approve){
            approveAndUpdate(null,reportId);
        }else{
            disapproveAndUpdate(null, reportId);
        }
    }

    public void refreshVariables(){
        variableList = sessionBean.getReference().getRefElements("variable", true);
    }

    // region Getter and Setter

    public void onDlgVariableShow(){
        refreshVariables();
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

    public String getFilterSubjectTypesText() {
        return filterSubjectTypesText;
    }

    public void setFilterSubjectTypesText(String filterSubjectTypesText) {
        this.filterSubjectTypesText = filterSubjectTypesText;
    }

    public List<RefSubjectTypeItem> getSubjectTypeItems() {
        return subjectTypeItems;
    }

    public void setSubjectTypeItems(List<RefSubjectTypeItem> subjectTypeItems) {
        this.subjectTypeItems = subjectTypeItems;
    }

    public List<RefSubjectTypeItem> getFilterSubjectTypes() {
        return filterSubjectTypes;
    }

    public void setFilterSubjectTypes(List<RefSubjectTypeItem> filterSubjectTypes) {
        this.filterSubjectTypes = filterSubjectTypes;
    }

    public List<Form> getFilterForms() {
        return filterForms;
    }

    public void setFilterForms(List<Form> filterForms) {
        this.filterForms = filterForms;
    }

    public List<Form> getForms() {
        return forms;
    }

    public void setForms(List<Form> forms) {
        this.forms = forms;
    }

    public String getFilterFormsText() {
        return filterFormsText;
    }

    public void setFilterFormsText(String filterFormsText) {
        this.filterFormsText = filterFormsText;
    }

    public List<String> getFilterStatuses() {
        return filterStatuses;
    }

    public void setFilterStatuses(List<String> filterStatuses) {
        this.filterStatuses = filterStatuses;
    }

    public List<SelectItem> getAllFilterStatuses() {
        return allFilterStatuses;
    }

    public void setAllFilterStatuses(List<SelectItem> allFilterStatuses) {
        this.allFilterStatuses = allFilterStatuses;
    }

    public String getFilterStatusesText() {
        return filterStatusesText;
    }

    public void setFilterStatusesText(String filterStatusesText) {
        this.filterStatusesText = filterStatusesText;
    }

    public LazyDataModel<ReportListItem> getLdmReportListItem() {
        return ldmReportListItem;
    }

    public void setLdmReportListItem(LazyDataModel<ReportListItem> ldmReportListItem) {
        this.ldmReportListItem = ldmReportListItem;
    }

    public LazyDataModel<ReportStatus> getLdmReportStatus() {
        return ldmReportStatus;
    }

    public void setLdmReportStatus(LazyDataModel<ReportStatus> ldmReportStatus) {
        this.ldmReportStatus = ldmReportStatus;
    }

    public List<ReportListItem> getFilteredReportList() {
        return filteredReportList;
    }

    public void setFilteredReportList(List<ReportListItem> filteredReportList) {
        this.filteredReportList = filteredReportList;
    }

    public List<ReportListItem> getSelectedReports() {
        return selectedReports;
    }

    public void setSelectedReports(List<ReportListItem> selectedReports) {
        this.selectedReports = selectedReports;
    }

    public ReportListItem getSelectedReportListItem() {
        if (selectedReports.size() == 1) {
            return selectedReports.get(0);
        } else {
            return null;
        }
    }

    public void setSelectedReportListItem(ReportListItem item) {
        selectedReports.clear();
        selectedReports.add(item);
    }

    public String getHtml() {
        return html;
    }

    /*public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }*/

    public void setHtml(String html) {
        this.html = html;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public List<RefRespondentItem> getFilterRespondents() {
        return filterRespondents;
    }

    public void setFilterRespondents(List<RefRespondentItem> filterRespondents) {
        this.filterRespondents = filterRespondents;
    }

    public List<RefRespondentItem> getRespondents() {
        return respondents;
    }

    public void setRespondents(List<RefRespondentItem> respondents) {
        this.respondents = respondents;
    }

    public String getFilterRespondentsText() {
        return filterRespondentsText;
    }

    public void setFilterRespondentsText(String filterRespondentsText) {
        this.filterRespondentsText = filterRespondentsText;
    }

    public void setReportBean(ReportBean reportBean) {
        this.reportBean = reportBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public List<ReportStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<ReportStatus> statuses) {
        this.statuses = statuses;
    }

    public int getSendCount() {
        return sendCount;
    }

    public void setSendCount(int sendCount) {
        this.sendCount = sendCount;
    }

    public List<ReportStatus> getFilteredStatuses() {
        return filteredStatuses;
    }

    public void setFilteredStatuses(List<ReportStatus> filteredStatuses) {
        this.filteredStatuses = filteredStatuses;
    }

    public Boolean getDraftStatusesHidden() {
        return draftStatusesHidden;
    }

    public void setDraftStatusesHidden(Boolean draftStatusesHidden) {
        this.draftStatusesHidden = draftStatusesHidden;
        ((LdmReportStatus) ldmReportStatus).setDraftStatusesHidden(draftStatusesHidden);
    }

    public List<ReportListItem> getOutputReportList() {
        return outputReportList;
    }

    public void setOutputReportList(List<ReportListItem> outputReportList) {
        this.outputReportList = outputReportList;
    }

    public List<ControlResultItem> getControlResults() {
        return controlResults;
    }

    public void setControlResults(List<ControlResultItem> controlResults) {
        this.controlResults = controlResults;
    }

    public String getPdfFilePath() {
        return pdfFilePath;
    }

    public void setPdfFilePath(String pdfFilePath) {
        this.pdfFilePath = pdfFilePath;
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

    public void setPollDisabled(boolean pollDisabled) {
        this.pollEnabled = !pollDisabled;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getMessagePanelTitle() {
        return messagePanelTitle;
    }

    public List<ApproveResultItem> getApproveResultMessages() {
        return approveResultMessages;
    }

    /*public String getDisApproveConfirmMessage() {
        if(selectedReports.size()>1){
            return MessageFormat.format("Вы уверены, что хотите разутвердить {0} входных форм?", selectedReports.size());
        } else {
            return "Вы уверены, что хотите разутвердить входную форму?";
        }
    }*/

    public String getApproveConfirmMessage() {
        if (selectedReports.size() > 1) {
            return MessageFormat.format("Вы уверены, что хотите утвердить {0} входных форм?", selectedReports.size());
        } else {
            return "Вы уверены, что хотите утвердить входную форму?";
        }
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

    public List<ControlResultItemGroup> getControlResultGroups() {
        return controlResultGroups;
    }

    public boolean isValidationVisible() {
        return validationVisible;
    }

    public String getCurrentHistoryComment() {
        return currentHistoryComment;
    }

    public void setCurrentHistoryComment(String currentHistoryComment) {
        this.currentHistoryComment = currentHistoryComment;
    }

    public boolean isEditNoticeText() {
        return editNoticeText;
    }

    public void setEditNoticeText(boolean editNoticeText) {
        this.editNoticeText = editNoticeText;
    }

    public String getNoticeMessage() {
        return noticeMessage;
    }

    public void setNoticeMessage(String noticeMessage) {
        this.noticeMessage = noticeMessage;
    }

    public List<RefElements> getVariableList() {
        return variableList;
    }

    public void setVariableList(List<RefElements> variableList) {
        this.variableList = variableList;
    }

    public boolean isApprove() {
        return approve;
    }

    public void setApprove(boolean approve) {
        this.approve = approve;
    }

    public List<RefDepartmentItem> getFilterDeps() {
        return filterDeps;
    }

    public void setFilterDeps(List<RefDepartmentItem> filterDeps) {
        this.filterDeps = filterDeps;
    }

    public List<RefDepartmentItem> getDeps() {
        return deps;
    }

    public void setDeps(List<RefDepartmentItem> deps) {
        this.deps = deps;
    }

    public String getFilterDepsText() {
        return filterDepsText;
    }

    public void setFilterDepsText(String filterDepsText) {
        this.filterDepsText = filterDepsText;
    }

    public List<RefNpaItem> getFilterNPA() {
        return filterNPA;
    }

    public void setFilterNPA(List<RefNpaItem> filterNPA) {
        this.filterNPA = filterNPA;
    }

    public List<RefNpaItem> getNpaList() {
        return npaList;
    }

    public void setNpaList(List<RefNpaItem> npaList) {
        this.npaList = npaList;
    }

    public String getFilterNPAText() {
        return filterNPAText;
    }

    public void setFilterNPAText(String filterNPAText) {
        this.filterNPAText = filterNPAText;
    }

    public ReportStatus getSelectedStatus() {
        return selectedStatus;
    }

    public void setSelectedStatus(ReportStatus selectedStatus) {
        this.selectedStatus = selectedStatus;
    }

    // endregion
}
