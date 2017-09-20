package mb;

import com.google.gson.reflect.TypeToken;
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
import javax.faces.component.UIOutput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.portlet.PortletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Nuriddin.Baideuov on 13.05.2015.
 */
@ManagedBean
@SessionScoped
public class OutputDataBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    private Long reportId;
    private Long openedReportId;
    private ReportListItem openedReportListItem;
    private boolean approved;
    private boolean draft;
    private boolean rightApprove;
    private boolean rightDisapprove;
    private boolean rightInputReports;
    private boolean rightEditSign;
    private boolean rightDownload;

    private boolean fixed;


    private Date filterReportDateBegin;
    private Date filterReportDateEnd;

    private String filterSubjectTypesText;
    private List<RefSubjectTypeItem> subjectTypeItems;
    private List<RefSubjectTypeItem> filterSubjectTypes = new ArrayList<RefSubjectTypeItem>();

    private List<RefDepartmentItem> filterDeps = new ArrayList<RefDepartmentItem>();
    private List<RefDepartmentItem> deps;
    private String filterDepsText;

    private List<Form> filterForms = new ArrayList<Form>();
    private List<Form> forms;
    private String filterFormsText;

    private List<RefNpaItem> filterNPA = new ArrayList<RefNpaItem>();
    private List<RefNpaItem> npaList;
    private String filterNPAText;

    private List<String> filterStatuses;
    private List<SelectItem> allFilterStatuses;
    private String filterStatusesText;

    private LazyDataModel<ReportListItem> ldmReportListItem;
    private LazyDataModel<ReportStatus> ldmReportStatus;

    private List<ReportListItem> reportList;
    private List<ReportListItem> filteredReportList;
    private List<ReportListItem> selectedReports = new ArrayList<ReportListItem>();

    private String html;
    private String statusName;

    private List<InputReportListItem> inputReports;
    private List<RefRespondentItem> respondents;

    private List<RefRespondentItem> respondentsIn;
    private List<RefRespondentItem> filterRespondents = new ArrayList<RefRespondentItem>();
    private String filterRespondentsText;

    private List<ControlResultItem> controlResults;
    private Map<Long, ControlResult.ResultType> controlResultTypes;

    private List<ReportStatus> statuses;
    private List<ReportStatus> filteredStatuses;
    private Boolean draftStatusesHidden;

    // Подпись
    private Long managerId;
    private List<RefManagersItem> managersList;
    private String postName;
    private Long executorId;
    private String executorPhone;
    private List<RefManagersItem> executorList;

    private String pdfFilePath;
    private Integer kindEvent;

    private boolean pollEnabled;

    private List<ApproveResultItem> approveResultMessages = new ArrayList<ApproveResultItem>();

    private List<ApproveResultItem> deleteMessages = new ArrayList<ApproveResultItem>();

    private List<ApproveResultItem> noteMessages = new ArrayList<ApproveResultItem>();

    private List<ApproveResultItem> signMessages = new ArrayList<ApproveResultItem>();


    private String reportNote = new String();
    private boolean append;
    private boolean newLine;
    private boolean disableNewLine;
    private boolean haveReportNote;
    private boolean canEditReportNote;

    private static SortedMap<String, String> getDataFormInputValues(Form form) {
        Map<String, String[]> originalParams = ApplicationBean.getPortalRequest().getParameterMap();
        SortedMap<String, String> result = new TreeMap<String, String>();
        if (form != null) {
            for (Map.Entry<String, String[]> entry : originalParams.entrySet()) {
                if (entry.getKey().startsWith(form.getCode())) {
                    String[] values = entry.getValue();
                    if (values != null && values.length > 0) result.put(entry.getKey(), values[0]);
                }
            }
        }
        return result;
    }

    @PostConstruct
    public void init() {
        Date dateStart = new Date();

        try {
            if (sessionBean.isEjbNull()) sessionBean.init();

            filterReportDateBegin = filterReportDateEnd = Util.getFirstDayOfCurrentMonth();
            subjectTypeItems = sessionBean.getReference().getRefSubjectTypeListAdvanced(sessionBean.getIntegration().getNewDateFromBackEndServer(), true);
            resetFilterSubjectTypes();

            refreshRefNpa();
            resetFilterForms();
            resetFilterDeps();
            resetFilterRespondents();

            allFilterStatuses = new ArrayList<SelectItem>();
            List<ReportStatus.Status> statuses = new ArrayList<ReportStatus.Status>(Arrays.asList(new ReportStatus.Status[]{ReportStatus.Status.COMPLETED, ReportStatus.Status.APPROVED}));
            for (ReportStatus.Status status : statuses)
                allFilterStatuses.add(new SelectItem(status.name(), ReportStatus.resMap.get("ru_" + status.name())));
            filterStatuses = new ArrayList<String>();
            resetFilterStatuses();

            ldmReportListItem = new LdmReportListItem();
            ldmReportStatus = new LdmReportStatus();

            // move to method preRender
            // refreshReportList();

            controlResultTypes = new HashMap<Long, ControlResult.ResultType>();
            controlResultTypes.put(ControlResult.ResultType.SUCCESS.getId(), ControlResult.ResultType.SUCCESS);
            controlResultTypes.put(ControlResult.ResultType.FAIL.getId(), ControlResult.ResultType.FAIL);
            controlResultTypes.put(ControlResult.ResultType.ERROR.getId(), ControlResult.ResultType.ERROR);
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

    private void updateFilterFormsText() {
        int size = filterForms.size();
        if (size == 0) filterFormsText = "Нет ни одной формы!";
        else if (size == 1) filterFormsText = filterForms.get(0).getFormHistory().getName();
        else if (size == forms.size()) filterFormsText = "Все";
        else filterFormsText = "Несколько форм (" + size + ")";
    }

    public void onFilterNPAHide() {
        updateFilterNPAText();
        resetFilterForms();
    }

    public void resetFilterForms() {
        List<Long> stRecIds = new ArrayList<Long>();
        for (RefSubjectTypeItem stItem : filterSubjectTypes)
            stRecIds.add(stItem.getRecId());
        if (stRecIds.size() != 0 && filterNPA.size() != 0)
            forms = sessionBean.getPersistence().getAllOutFormsByUserSubjectTypeRecIds(sessionBean.user.getUserId(), stRecIds, filterNPA);
        else
            forms = new ArrayList<Form>();

        filterForms = new ArrayList<Form>();
        filterForms.addAll(forms);

        updateFilterFormsText();
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
        else if (size == respondentsIn.size()) filterRespondentsText = "Все";
        else filterRespondentsText = "Несколько респондентов (" + size + ")";
    }

    public void resetFilterRespondents() {
        List<Long> stRecIds = new ArrayList<Long>();
        for (RefSubjectTypeItem stItem : filterSubjectTypes)
            stRecIds.add(stItem.getRecId());

        if (stRecIds.size() != 0 && filterDeps.size() != 0)
            respondentsIn = sessionBean.getReference().getUserRespsBySTRDepRecIds(sessionBean.user.getUserId(), sessionBean.getIntegration().getNewDateFromBackEndServer(), stRecIds, filterDeps);
        else
            respondentsIn = new ArrayList<RefRespondentItem>();

        filterRespondents = new ArrayList<RefRespondentItem>();
        filterRespondents.addAll(respondentsIn);

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
        resetFilterNPA();
        resetFilterDeps();
        resetFilterRespondents();
        resetFilterStatuses();
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

        reportList = sessionBean.getPersistence().getFilteredOutputReportList(sessionBean.user.getUserId(), formCodes, idnList,
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
            item.setStatusName(ReportStatus.resMap.get(sessionBean.languageCode + "_" + item.getStatus()));
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

    public void refreshInputReportList(Long id) {
        userBean.checkAccess("SU:OUT:INPUT_REPORTS");

        if (id != null) {
            inputReports = sessionBean.getPersistence().getInputReportsByOutputReportId(id, sessionBean.languageCode);
            respondents = sessionBean.getPersistence().getInputReportRespondentsByOutputReportId(id);
        } else {
            inputReports = null;
            respondents = null;
        }
    }

    public String open(ReportListItem repListItem, Long repId) {
        ReportListItem reportListItem;
        if (repListItem == null)
            reportListItem = getCurrentReportListItem(repId);
        else
            reportListItem = repListItem;
        html = reportListItem == null ? null : sessionBean.getPersistence().getHtmlWithOutputReportData(reportListItem.getId(), true);
        statusName = reportListItem == null ? null : reportListItem.getStatusName();
        reportId = reportListItem.getId();
        approved = reportListItem.getStatus().equals(ReportStatus.Status.APPROVED.name());
        draft = reportListItem.getStatus().equals(ReportStatus.Status.DRAFT.name());
        rightApprove = userBean.hasOutputPermissionForm(reportListItem.getFormCode(), "F:APPROVE");
        rightDisapprove = userBean.hasOutputPermissionForm(reportListItem.getFormCode(), "F:DISAPPROVE");
        rightInputReports = userBean.hasPermission("SU:OUT:INPUT_REPORTS");
        rightEditSign = userBean.hasPermission("SU:OUT:SIGN:EDIT");
        rightDownload = userBean.hasPermission("SU:OUT:DOWNLOAD_EXCEL");
        haveReportNote = reportListItem.getHaveNote();
        canEditReportNote = !isEditReportNoteDisable(null,reportListItem.getId());
        setFixed(false);

        return "/views/su/outputdata/form?faces-redirect=true";
    }

    public void approveReports(boolean approve) {
        approveResultMessages.clear();
        Date approvalDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
        for (ReportListItem reportListItem : selectedReports) {
            String msg;
            if(approve) {
                msg = doApprove(reportListItem, approvalDate);
            } else {
                msg = doDisapprove(reportListItem, approvalDate);
            }
            if(msg.isEmpty()){
                approveResultMessages.add(new ApproveResultItem(reportListItem.getId(),
                        reportListItem.getFormName(), reportListItem.getRespondentNameRu(),
                        reportListItem.getReportDate(), "", ApproveResultItem.ResultType.SUCCESS));
            } else {
                approveResultMessages.add(new ApproveResultItem(reportListItem.getId(),
                        reportListItem.getFormName(), reportListItem.getRespondentNameRu(),
                        reportListItem.getReportDate(), msg));
            }
        }

        refreshReportList();

        Collections.sort(approveResultMessages, new OperationResultComparator());

        RequestContext context = RequestContext.getCurrentInstance();
        if(approve) {
            context.execute("PF('wDlgApproveResults').show()");
        } else {
            context.execute("PF('wDlgDisApproveResults').show()");
        }
    }

    public void approve(ReportListItem reportListItem) {
        Date approvalDate = sessionBean.getIntegration().getNewDateFromBackEndServer();

        String msg = doApprove(reportListItem, approvalDate);

        refreshReportList();

        if (!msg.isEmpty()) {
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", msg));
        }
    }

    private String doApprove(ReportListItem reportListItem, Date approvalDate){
        if (!userBean.hasOutputPermissionForm(reportListItem.getFormCode(), "F:APPROVE")) {
            return "Не достаточно прав";
        }

        /*if (!sessionBean.getPersistence().isStatusCompatible(reportListItem.getStatus(), ReportStatus.Status.APPROVED.name())) {
            return MessageFormat.format("Статус отчета \"{0}\"", reportListItem.getStatusName());
        }*/
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject(reportListItem.getFormCode());
        auditEvent.setNameObject(null);
        auditEvent.setIdKindEvent(55L);
        auditEvent.setDateEvent(approvalDate);
        auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
        auditEvent.setDateIn(reportListItem.getReportDate());
        auditEvent.setRecId(reportListItem.getId());
        auditEvent.setUserId(sessionBean.abstractUser.getId());
        auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());
        try {
            sessionBean.getPersistence().updateOutputReport(reportListItem.getId(), true, sessionBean.abstractUser, approvalDate, auditEvent);
        }catch (Exception e){
            return e.getMessage();
        }
        return "";
    }

    public void disapprove(ReportListItem reportListItem) {
        Date disapproveDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
        String msg = doDisapprove(reportListItem, disapproveDate);

        refreshReportList();

        if (!msg.isEmpty()) {
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", msg));
        }
    }

    /*public void save(Long repId){
        ReportListItem reportListItem = getCurrentReportListItem(repId);
        Date reportDate = reportListItem.getReportDate();
        List<Form> forms = sessionBean.getPersistence().getFormsByCodeLanguageReportDate(reportListItem.getFormCode(), sessionBean.languageCode, reportDate);
        if (forms.isEmpty()) return;

        Form form = forms.get(0);
        Map<String,String> inputValues = getDataFormInputValues(form);
        List<ReportHistory> reportHistory = sessionBean.getPersistence().getReportHistoryByReportIdNoLobs(reportListItem.getId());
        if (!reportHistory.isEmpty()) {
            Long lastReportHistoryId = reportHistory.get(reportHistory.size() - 1).getId();
            String jsonData = applicationBean.gson.toJson(inputValues);
            if(jsonData!=null){
                sessionBean.getPersistence().updateReportHistoryData(lastReportHistoryId, jsonData);
            }
        }
    }*/

    private String doDisapprove(ReportListItem reportListItem, Date disapproveDate){
        if(!userBean.hasOutputPermissionForm(reportListItem.getFormCode(), "F:DISAPPROVE")){
            return "Не достаточно прав";
        }

        /*if(!sessionBean.getPersistence().isStatusCompatible(reportListItem.getStatus(), ReportStatus.Status.DISAPPROVED.name())){
            return MessageFormat.format("Статус отчета - \"{0}\"", reportListItem.getStatusName());
        }*/

        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject(reportListItem.getFormCode());
        auditEvent.setNameObject(null);
        auditEvent.setIdKindEvent(56L);
        auditEvent.setDateEvent(disapproveDate);
        auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
        auditEvent.setDateIn(reportListItem.getReportDate());
        auditEvent.setRecId(reportListItem.getId());
        auditEvent.setUserId(sessionBean.abstractUser.getId());
        auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());

        try {
            sessionBean.getPersistence().updateOutputReport(reportListItem.getId(), false, sessionBean.abstractUser, disapproveDate, auditEvent);
        } catch (Exception e) {
            return e.getMessage();
        }

        return "";
    }

    public void approveAndUpdate(Long reportId) {
        ReportListItem reportListItem = getCurrentReportListItem(reportId);
        approve(reportListItem);
        updateStatus(reportListItem);
    }

    public void disapproveAndUpdate(Long reportId) {
        ReportListItem reportListItem = getCurrentReportListItem(reportId);
        disapprove(reportListItem);
        updateStatus(reportListItem);
    }

    private void updateStatus(ReportListItem reportListItem) {
        if (reportListItem != null) {
            ReportStatus status = sessionBean.getPersistence().getLastReportStatusByReportId(reportListItem.getId(), false, null);
            statusName = ReportStatus.resMap.get(sessionBean.languageCode + "_" + status.getStatusCode());
            approved = status.getStatusCode().equals(ReportStatus.Status.APPROVED.name());
        }
    }

    public boolean approveButtonEnabled() {
        if (selectedReports.size() == 0) {
            return false;
        }
        if (selectedReports.size() == 1) {
            ReportListItem reportListItem = selectedReports.get(0);
            if (reportListItem.getStatus().equals(ReportStatus.Status.APPROVED.toString()) || reportListItem.getStatus().equals(ReportStatus.Status.DRAFT.toString())) {
                return false;
            }
            if (!userBean.hasOutputPermissionForm(reportListItem.getFormCode(), "F:APPROVE")) {
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
            if (!userBean.hasOutputPermissionForm(reportListItem.getFormCode(), "F:DISAPPROVE")) {
                return false;
            }
        }
        return true;
    }

    public void deleteReport() {
        deleteMessages.clear();
        Iterator<ReportListItem> itemIterator = selectedReports.iterator();
        while (itemIterator.hasNext()) {
            ReportListItem reportListItem  = itemIterator.next();

            if (!userBean.hasOutputPermissionForm(reportListItem.getFormCode(), "F:DELETE")) {
                deleteMessages.add(new ApproveResultItem(reportListItem.getId(), reportListItem.getFormName(),
                        reportListItem.getRespondentNameRu(), reportListItem.getReportDate(), "Не достаточно прав"));
                continue;
            }

            ReportStatus reportStatus = sessionBean.getPersistence().getLastReportStatusByReportId(reportListItem.getId(), false, null);
            if (reportStatus == null) {
                continue;
            }
            String statusCode = reportStatus.getStatusCode();
            if (statusCode.equals(ReportStatus.Status.APPROVED.name())) {
                deleteMessages.add(new ApproveResultItem(reportListItem.getId(), reportListItem.getFormName(),
                        reportListItem.getRespondentNameRu(), reportListItem.getReportDate(), MessageFormat.format("Статус \"{0}\"", reportStatus.getStatusName("ru"))));
                continue;
            }

            Date delDate = sessionBean.getIntegration().getNewDateFromBackEndServer();

            if (sessionBean.getPersistence().getReport(reportListItem.getId(), null) != null) {

                ReportStatus status = new ReportStatus();
                Report report = new Report();
                report.setId(reportListItem.getId());
                status.setReportHistory(sessionBean.getPersistence().getLastReportHistoryByReportIdNoLobs(reportListItem.getId(), true, null));
                status.setStatusCode("DELETE");
                status.setStatusDate(delDate);
                status.setUserId(sessionBean.abstractUser.getId());
                status.setUserInfo(sessionBean.abstractUser.getDisplayName());
                status.setUserLocation(sessionBean.abstractUser.getLocation());
                status.setMessage("Удалил(а): " + status.getUserInfo() + " [" + sessionBean.abstractUser.getLocation() + "]");

                AuditEvent auditEvent = new AuditEvent();
                auditEvent.setCodeObject(reportListItem.getFormCode());
                auditEvent.setNameObject(null);
                auditEvent.setIdKindEvent(2L);
                auditEvent.setDateEvent(delDate);
                auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
                auditEvent.setDateIn(reportListItem.getReportDate());
                auditEvent.setRecId(reportListItem.getId());
                auditEvent.setUserId(sessionBean.abstractUser.getId());
                auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());

                sessionBean.getPersistence().deleteReportTransactional(reportListItem.getId(), status, auditEvent);

                deleteMessages.add(new ApproveResultItem(reportListItem.getId(),
                        reportListItem.getFormName(),
                        reportListItem.getRespondentNameRu(),
                        reportListItem.getReportDate(),
                        "",
                        ApproveResultItem.ResultType.SUCCESS));
            }
            reportList.remove(reportListItem);
        }

        Collections.sort(deleteMessages, new OperationResultComparator());

        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('wDlgDeleteResults').show()");
    }

    public void deleteDraft(Long reportId) {
        ReportListItem reportListItem = getCurrentReportListItem(reportId);
        if (reportListItem != null) {
            if (reportListItem.getStatus().equals(ReportStatus.Status.DRAFT.toString())) {
                sessionBean.getPersistence().deleteReport(reportId, null);
                refreshReportList();
            }
        }
    }

    public boolean isDeleteDisable() {
        if (selectedReports.size() == 0) {
            return true;
        }
        if(selectedReports.size()==1){
            ReportListItem selectedReportListItem = selectedReports.get(0);
            String status = selectedReportListItem.getStatus();
            if (status.equals(ReportStatus.Status.APPROVED.name())) {
                return true;
            }
            if (!userBean.hasOutputPermissionForm(selectedReportListItem.getFormCode(), "F:DELETE")) {
                return true;
            }
        }
        return false;
    }

    public boolean controlButtonEnabled() {
        if (selectedReports.size() == 0) {
            return false;
        }
        if (!userBean.hasPermission("SU:OUT:CONTROL")) {
            return false;
        }
        return true;
    }

    public void performControl(ReportListItem repListItem, Long repId, boolean cascade) {
        userBean.checkAccess("SU:OUT:CONTROL");

        ReportListItem reportListItem;
        if (repListItem == null)
            reportListItem = getCurrentReportListItem(repId);
        else
            reportListItem = repListItem;

        Report report = sessionBean.getPersistence().getReport(reportListItem.getId(), null);
        RefRespondentItem respondent = sessionBean.getReference().getRespondentByIdn(report.getIdn(), report.getReportDate());
        if (respondent != null)
            controlResults = sessionBean.getPerformControl().runTask(reportListItem.getReportDate(), reportListItem.getFormCode(), report.getIdn(), true, respondent.getRefSubjectTypeRecId(), cascade, false);

        try {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(reportListItem.getFormCode());
            auditEvent.setNameObject(null);
            auditEvent.setIdKindEvent(54L);
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
        refreshReportList();
    }

    public void performControlAll() {
        userBean.checkAccess("SU:OUT:CONTROL");

        List<Report> reports = new ArrayList<Report>();
        for (ReportListItem reportListItem : selectedReports) {
            reports.add(sessionBean.getPersistence().getReport(reportListItem.getId(), null));
        }

        controlResults = sessionBean.getPerformControl().runTaskAll(reports, true, true, sessionBean.abstractUser.getId(),
                sessionBean.abstractUser.getLocation(), sessionBean.respondent.getId(), 54L, false);

        refreshReportList();
    }

    public void downloadControlResults(ReportListItem repListItem, Long repId){
        ReportListItem reportListItem;
        if (repListItem == null)
            reportListItem = getCurrentReportListItem(repId);
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

            FileWrapper fileWrapper = sessionBean.getPersistence().controlResultsAllToExcelFile(controlResults, sessionBean.respondent);
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
        if (repListItem == null)
            reportListItem = getCurrentReportListItem(repId);
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

    public void prepareControlResultsAllPdfFile() {
        try {
            FileWrapper fileWrapper = sessionBean.getPersistence().generateControlResultsAllPdfFile(controlResults, sessionBean.respondent);

            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            PortletContext portletContext = (PortletContext) externalContext.getContext();
            String dir = portletContext.getRealPath("/resources/reports/");
            File file = File.createTempFile("control_result_all_", ".pdf", new File(dir));

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

    public void refreshStatuses() {
        userBean.checkAccess("SU:OUT:HISTORY");
        ReportListItem selectedReportListItem = getSelectedReportListItem();
        if (selectedReportListItem == null) {
            statuses = new ArrayList<ReportStatus>();
            return;
        }
        statuses = sessionBean.getPersistence().getReportStatusHistoryByReportId(selectedReportListItem.getId(), true, null);

        ((LdmReportStatus) ldmReportStatus).setSrcCollection(statuses);
        ((LdmReportStatus) ldmReportStatus).setDraftStatusesHidden(draftStatusesHidden);
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
        if (repListItem == null)
            reportListItem = getCurrentReportListItem(repId);
        else
            reportListItem = repListItem;

        byte[] bytes = null;
        String fileName = "";

        try {
            userBean.checkAccess("SU:OUT:DOWNLOAD_EXCEL");

            FileWrapper fileWrapper = sessionBean.getPersistence().generateExcelFile(reportListItem, false);
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
            auditEvent.setIdKindEvent(52L);
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
        if (!userBean.hasPermission("SU:OUT:DOWNLOAD_EXCEL")) {
            hasError = true;
        }
        if (!hasError) {
            for (ReportListItem selectedReportListItem : selectedReports) {
                try {
                    FileWrapper fileWrapper = sessionBean.getPersistence().generateExcelFile(selectedReportListItem, false);
                    String fileName = dfReportDate.format(selectedReportListItem.getReportDate()) + "_" + selectedReportListItem.getFormCode() + "_draft." + fileWrapper.getFileFormat();
                    fileWrapper.setFileName(fileName);
                    fileWrappers.add(fileWrapper);
                } catch (Exception e) {
                    hasError = true;
                    break;
                }

                AuditEvent auditEvent = new AuditEvent();
                auditEvent.setCodeObject(selectedReportListItem.getFormCode());
                auditEvent.setNameObject(null);
                auditEvent.setIdKindEvent(52L);
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

    public void preparePdfFile(ReportListItem repListItem, Long repId) {
        ReportListItem reportListItem;
        if (repListItem == null)
            reportListItem = getCurrentReportListItem(repId);
        else
            reportListItem = repListItem;
        try {
            userBean.checkAccess("SU:OUT:PRINT");

            FileWrapper fileWrapper = sessionBean.getPersistence().generatePdfFile(reportListItem, false);

            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            PortletContext portletContext = (PortletContext) externalContext.getContext();
            String dir = portletContext.getRealPath("/resources/reports/");
            File file = File.createTempFile(String.valueOf(reportListItem.getId()) + "_", ".pdf", new File(dir));
            if (!file.exists())
                file.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(file);
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
            auditEvent.setIdKindEvent(53L);
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

    public String getControlResultTypeName(Long controlResultType) {
        ControlResult.ResultType resultType = controlResultTypes.get(controlResultType);
        if (resultType == null)
            return "";
        else
            return resultType.getName(sessionBean.languageCode);
    }

    public void getList(Long reportId) {
        userBean.checkAccess("SU:OUT:SIGN:EDIT");
        Date date;
        if(reportId != null && reportId != 0) {
            ReportListItem reportListItem = getCurrentReportListItem(reportId);
            date = reportListItem.getReportDate();
        }else {
            date = sessionBean.getIntegration().getNewDateFromBackEndServer();
        }

        RefManagersItem managersItem = new RefManagersItem();
        managersItem.setExecutor(false);
        managersList = (List<RefManagersItem>)sessionBean.getReference().getRefAbstractByFilterList(RefManagersItem.REF_CODE, managersItem, date);

        managersItem.setExecutor(true);
        executorList = (List<RefManagersItem>)sessionBean.getReference().getRefAbstractByFilterList(RefManagersItem.REF_CODE, managersItem, date);
    }

    public void insertSign(List<ReportListItem> selectedRepListItem,Long repId) {
        userBean.checkAccess("SU:OUT:SIGN:EDIT");
        signMessages.clear();

        List<ReportListItem> items = new ArrayList<ReportListItem>();
        if(selectedRepListItem == null){
            items.add(0, getCurrentReportListItem(repId));
        }else{
            items = selectedRepListItem;
        }

        String managerName = null;
        String executorName = null;

        for (RefManagersItem item : managersList){
            if(item.getId().equals(managerId)) {
                if(item.getNm() != null && !item.getNm().isEmpty()){
                    managerName = item.getNm().substring(0,1).toUpperCase() + ".";
                }
                if(item.getFt() != null && !item.getFt().isEmpty()){
                    managerName = managerName + item.getFt().substring(0,1).toUpperCase() + ".";
                }
                if(item.getFm() != null && !item.getFm().isEmpty()){
                    managerName = managerName + " " + item.getFm();
                }
                break;
            }
        }

        for (RefManagersItem item : executorList){
            if(item.getId().equals(executorId)) {
                if(item.getFm() != null && !item.getFm().isEmpty()){
                    executorName = item.getFm();
                }
                if(item.getNm() != null && !item.getNm().isEmpty()) {
                    executorName = executorName + " " + item.getNm().substring(0,1).toUpperCase() + ".";
                }
                if(item.getFt() != null && !item.getFt().isEmpty()){
                    executorName = executorName + item.getFt().substring(0,1).toUpperCase() + ".";
                }
                executorName = "Исп. " + executorName;
                break;
            }
        }
        kindEvent = 28;
        Iterator<ReportListItem> itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            ReportListItem reportListItem = itemIterator.next();
            updateSign(reportListItem, postName, managerName, executorName, executorPhone);
        }
        if(selectedRepListItem != null) {
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('wDlgEditSignResults').show()");
        }
    }

    public void deleteSign(List<ReportListItem> selectedRepListItem,Long repId) {
        signMessages.clear();
        userBean.checkAccess("SU:OUT:SIGN:EDIT");
        List<ReportListItem> items = new ArrayList<ReportListItem>();
        if(selectedRepListItem == null){
            items.add(0, getCurrentReportListItem(repId));
        }else{
            items = selectedRepListItem;
        }

        kindEvent = 29;

        Iterator<ReportListItem> itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            ReportListItem reportListItem = itemIterator.next();
            updateSign(reportListItem, null, null, null, null);
        }
        if(selectedRepListItem != null) {
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('wDlgEditSignResults').show()");
        }
    }

    private void updateSign(ReportListItem reportListItem, String postName, String managerName, String executorName, String executorPhone) {
        Long reportHistoryId = sessionBean.getPersistence().getLastReportHistoryIdByReportId(reportListItem.getId(), false, null);
        ReportHistory reportHistory = sessionBean.getPersistence().getReportHistory(reportHistoryId, true, false);

        String jsonData = reportHistory.getData();

        Type typeMapStringString = new TypeToken<Map<String, String>>() {
        }.getType();

        Map<String, String> kvMap = applicationBean.gson.fromJson(jsonData, typeMapStringString);

        kvMap.put(reportListItem.getFormCode() + "*" + "post::", postName);
        kvMap.put(reportListItem.getFormCode() + "*" + "manager::", managerName);
        kvMap.put(reportListItem.getFormCode() + "*" + "executorName::", executorName);
        kvMap.put(reportListItem.getFormCode() + "*" + "executorPhone::", executorPhone);
        if (postName != null && managerName != null)
            kvMap.put(reportListItem.getFormCode() + "*" + "sign::", postName +
                    "                                                           "
                    + managerName);
        else
            kvMap.put(reportListItem.getFormCode() + "*" + "sign::", null);


        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject(reportListItem.getFormCode());
        auditEvent.setNameObject(null);
        auditEvent.setIdKindEvent((long) kindEvent);
        auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
        auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
        auditEvent.setDateIn(reportListItem.getReportDate());
        auditEvent.setRecId(reportListItem.getId());
        auditEvent.setUserId(sessionBean.abstractUser.getId());
        auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());

        String data = applicationBean.gson.toJson(kvMap);
        sessionBean.getPersistence().updateReportHistoryData(reportHistoryId, data, auditEvent);

        signMessages.add(new ApproveResultItem(reportListItem.getId(),
                reportListItem.getFormName(),
                reportListItem.getRespondentNameRu(),
                reportListItem.getReportDate(),
                "",
                ApproveResultItem.ResultType.SUCCESS));
    }

    public void onExecutorListChange(AjaxBehaviorEvent vce){
        Long id = (Long) ((UIOutput) vce.getSource()).getValue();
        if(id == 0 || id == null) {
            executorPhone = null;
        } else {
            for (RefManagersItem item : executorList) {
                if (item.getId().equals(id)) {
                    executorPhone = item.getPhone();
                    break;
                }
            }
        }
    }

    public void onManagerListChange(AjaxBehaviorEvent vce){
        Long id = (Long) ((UIOutput) vce.getSource()).getValue();
        if(id == 0 || id == null) {
            postName = null;
        } else {
            for (RefManagersItem item : managersList) {
                if (item.getId().equals(id)) {
                    postName = item.getPostNameRu();
                    break;
                }
            }
        }
    }

    public ReportListItem getCurrentReportListItem(Long reportId) {
        if (reportId.equals(openedReportId) && openedReportListItem != null)
            return openedReportListItem;

        openedReportListItem = sessionBean.getPersistence().getReportListByReportId(reportId, sessionBean.languageCode, false);
        openedReportId = reportId;

        openedReportListItem.setStatusName(ReportStatus.resMap.get(sessionBean.languageCode + "_" + openedReportListItem.getStatus()));

        return openedReportListItem;
    }

    public void onPollSwitch() {
        FacesMessage msg = new FacesMessage(pollEnabled ? "Автообновление включено" : "Автообновление выключено");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void fixTable() {
        RequestContext.getCurrentInstance().execute("fixTable(" + fixed + ",0)");
    }

    public void getReportNoteFromBD(Long reportId){
        if(reportId == null || reportId == 0) {
            reportNote = null;
            append = true;
        }else {
            Long reportHistoryId = sessionBean.getPersistence().getLastReportHistoryByReportId(reportId, false, false, true, null).getId();
            reportNote = sessionBean.getPersistence().getReportNote(reportHistoryId, null);
            append = false;
        }
        onAppendCheckBoxChange();
    }

    public void editReportNote(List<ReportListItem> selectedRepListItem, Long repId) {
        noteMessages.clear();
        List<ReportListItem> items = new ArrayList<ReportListItem>();
        if(selectedRepListItem == null){
            items.add(0, getCurrentReportListItem(repId));
            if(reportNote != null && !reportNote.trim().isEmpty()) {
                haveReportNote = true;
            }else{
                haveReportNote = false;
            }
        }else{
            items = selectedRepListItem;
        }
        Iterator<ReportListItem> itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            ReportListItem reportListItem = itemIterator.next();
            if (!userBean.hasOutputPermissionForm(reportListItem.getFormCode(), "F:EDIT")) {
                noteMessages.add(new ApproveResultItem(reportListItem.getId(), reportListItem.getFormName(),
                        reportListItem.getRespondentNameRu(), reportListItem.getReportDate(), "Не достаточно прав"));
                continue;
            }

            Long reportHistoryId = sessionBean.getPersistence().getLastReportHistoryByReportId(reportListItem.getId(), false, false, false, null).getId();
            userBean.hasOutputPermissionForm(reportListItem.getFormCode(), "F:EDIT");
            sessionBean.getPersistence().editReportNote(reportHistoryId, reportNote == null ? null : reportNote.trim(), reportNote == null ? false : append, reportNote == null ? false : newLine);
            noteMessages.add(new ApproveResultItem(reportListItem.getId(),
                    reportListItem.getFormName(),
                    reportListItem.getRespondentNameRu(),
                    reportListItem.getReportDate(),
                    "",
                    ApproveResultItem.ResultType.SUCCESS));
        }

        if(selectedRepListItem != null) {
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('wDlgEditNoteResults').show()");
        }
    }

    public boolean isEditReportNoteDisable(List<ReportListItem> selectedRepListItem, Long repId){
        if ((selectedRepListItem == null || selectedRepListItem.size() == 0) && (repId == 0 || repId == null)) {
            return true;
        }
        List<ReportListItem> items = new ArrayList<ReportListItem>();
        if(selectedRepListItem == null){
            items.add(0, getCurrentReportListItem(repId));
        }else if (selectedRepListItem != null){
            items = selectedRepListItem;
        }
        Iterator<ReportListItem> itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            ReportListItem reportListItem  = itemIterator.next();
            if (!userBean.hasOutputPermissionForm(reportListItem.getFormCode(), "F:EDIT")) {
                return true;
            }
        }
        return false;
    }

    public void onAppendCheckBoxChange() {
        disableNewLine = !append;
        if(disableNewLine) newLine = false;
    }



    // Getters and setters

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

    public List<RefSubjectTypeItem> getFilterSubjectTypes() {
        return filterSubjectTypes;
    }

    public void setFilterSubjectTypes(List<RefSubjectTypeItem> filterSubjectTypes) {
        this.filterSubjectTypes = filterSubjectTypes;
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

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
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

    public List<String> getFilterStatuses() {
        return filterStatuses;
    }

    public void setFilterStatuses(List<String> filterStatuses) {
        this.filterStatuses = filterStatuses;
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



    public List<ReportListItem> getReportList() {
        return reportList;
    }

    public void setReportList(List<ReportListItem> reportList) {
        this.reportList = reportList;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public List<InputReportListItem> getInputReports() {
        return inputReports;
    }

    public void setInputReports(List<InputReportListItem> inputReports) {
        this.inputReports = inputReports;
    }

    public List<RefRespondentItem> getRespondents() {
        return respondents;
    }

    public void setRespondents(List<RefRespondentItem> respondents) {
        this.respondents = respondents;
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
    }

    public List<ControlResultItem> getControlResults() {
        return controlResults;
    }

    public void setControlResults(List<ControlResultItem> controlResults) {
        this.controlResults = controlResults;
    }


    public List<RefManagersItem> getManagersList() {
        return managersList;
    }

    public void setManagersList(List<RefManagersItem> managersList) {
        this.managersList = managersList;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
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

    public boolean isRightInputReports() {
        return rightInputReports;
    }

    public void setRightInputReports(boolean rightInputReports) {
        this.rightInputReports = rightInputReports;
    }

    public boolean isRightEditSign() {
        return rightEditSign;
    }

    public void setRightEditSign(boolean rightEditSign) {
        this.rightEditSign = rightEditSign;
    }

    public boolean isRightDownload() {
        return rightDownload;
    }

    public void setRightDownload(boolean rightDownload) {
        this.rightDownload = rightDownload;
    }

    public String getPdfFilePath() {
        return pdfFilePath;
    }

    public void setPdfFilePath(String pdfFilePath) {
        this.pdfFilePath = pdfFilePath;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
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

    public List<ApproveResultItem> getApproveResultMessages() {
        return approveResultMessages;
    }

    /*public String getApproveConfirmMessage() {
        if (selectedReports.size() > 1) {
            return MessageFormat.format("Вы уверены, что хотите утвердить {0} выходных/сводных форм?", selectedReports.size());
        } else {
            return "Вы уверены, что хотите утвердить выходную/сводную форму?";
        }
    }*/

    /*public String getDisapproveConfirmMessage() {
        if (selectedReports.size() > 1) {
            return MessageFormat.format("Вы уверены, что хотите разутвердить {0} выходных/сводных форм?", selectedReports.size());
        } else {
            return "Вы уверены, что хотите разутвердить выходную/сводную форму?";
        }
    }*/

    public void onFilterDepsHide(){
        updateFilterDepsText();
        resetFilterRespondents();
    }

    private void updateFilterDepsText() {
        int size = filterDeps.size();
        if (size == 0) filterDepsText = "Нет ни одного филиала!";
        else if (size == 1) filterDepsText = filterDeps.get(0).getNameRu();
        else if (size == deps.size()) filterDepsText = "Все";
        else filterDepsText = "Несколько филиалов (" + size + ")";
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
        else filterNPAText = "Выделено(" + size + ")";
    }
    public void refreshRefNpa(){
        npaList = (List<RefNpaItem>)sessionBean.getReference().getRefAbstractList(RefNpaItem.REF_CODE,  sessionBean.getIntegration().getNewDateFromBackEndServer());
        resetFilterNPA();
    }
    // region Getter and Setter
    public List<ApproveResultItem> getDeleteMessages() {
        return deleteMessages;
    }

    public String getReportNote() {
        return reportNote;
    }

    public void setReportNote(String reportNote) {
        this.reportNote = reportNote;
    }

    public List<ApproveResultItem> getNoteMessages() {
        return noteMessages;
    }

    public void setNoteMessages(List<ApproveResultItem> noteMessages) {
        this.noteMessages = noteMessages;
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public boolean isHaveReportNote() {
        return haveReportNote;
    }

    public void setHaveReportNote(boolean haveReportNote) {
        this.haveReportNote = haveReportNote;
    }

    public boolean isCanEditReportNote() {
        return canEditReportNote;
    }

    public void setCanEditReportNote(boolean canEditReportNote) {
        this.canEditReportNote = canEditReportNote;
    }

    public Long getExecutorId() {
        return executorId;
    }

    public void setExecutorId(Long executorId) {
        this.executorId = executorId;
    }

    public String getExecutorPhone() {
        return executorPhone;
    }

    public void setExecutorPhone(String executorPhone) {
        this.executorPhone = executorPhone;
    }

    public List<RefManagersItem> getExecutorList() {
        return executorList;
    }

    public void setExecutorList(List<RefManagersItem> executorList) {
        this.executorList = executorList;
    }

    public boolean isNewLine() {
        return newLine;
    }

    public void setNewLine(boolean newLine) {
        this.newLine = newLine;
    }

    public boolean isDisableNewLine() {
        return disableNewLine;
    }

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    public List<ApproveResultItem> getSignMessages() {
        return signMessages;
    }

    public void setSignMessages(List<ApproveResultItem> signMessages) {
        this.signMessages = signMessages;
    }

    public List<RefRespondentItem> getFilterRespondents() {
        return filterRespondents;
    }

    public void setFilterRespondents(List<RefRespondentItem> filterRespondents) {
        this.filterRespondents = filterRespondents;
    }

    public String getFilterRespondentsText() {
        return filterRespondentsText;
    }

    public List<RefRespondentItem> getRespondentsIn() {
        return respondentsIn;
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

    // endregion
}
