package mb;

import entities.*;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.UploadedFile;
import util.Convert;
import util.SettingsValueConverter;
import util.Util;

import javax.annotation.PostConstruct;
import javax.el.ELContext;
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
 * Managed bean
 *
 * @author Ardak Saduakassov
 */
@ManagedBean
@SessionScoped
public class ReportsBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");
    List<String> testUsers = Arrays.asList(new String[]{"biruza", "biruzaapk", "biruzakp", "biruzaot", "biruzacd", "biruzamfo", "biruzamfo1", "biruzamfo2", "test", "testresp1", "testresp2", "testresp3"});
    private boolean error = false;
    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{reportBean}")
    private ReportBean reportBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;
    @ManagedProperty(value = "#{kiscBean}")
    private KiscBean kiscBean;
    private Date filterReportDateBegin;
    private Date filterReportDateEnd;
    private String filterFormName;
    private List<String> filterStatuses;
    private List<SelectItem> allFilterStatuses;
    private String filterStatusesText;
    private List<Form> filterForms = new ArrayList<Form>();
    private List<Form> forms;
    private String filterFormsText;
    private List<RefNpaItem> filterNPA = new ArrayList<RefNpaItem>();
    private List<RefNpaItem> npaList;
    private String filterNPAText;
    private List<RefRespondentItem> filterResp = new ArrayList<RefRespondentItem>();
    private List<RefRespondentItem> respList;
    private String filterRespText;
    private LazyDataModel<ReportListItem> ldmReportListItem;
    private LazyDataModel<ReportStatus> ldmReportStatus;
    private ReportStatus selectedStatus;
    private List<ReportListItem> reportList;
    private List<ReportListItem> filteredReportList;
    private List<ReportListItem> selectedReports = new ArrayList<ReportListItem>();
    private List<ReportStatus> statuses;
    private List<ReportStatus> filteredStatuses;
    private Boolean draftStatusesHidden;
    private String signature;
    private List<String> submitResultMessages = new ArrayList<String>();
    private String openResultMessage;
    private Date newReportDate;
    private List<ControlResultItem> controlResults = new ArrayList<ControlResultItem>();
    private Map<Long, ControlResult.ResultType> controlResultTypes;
    private boolean approved;
    private boolean pollEnabled;
    private String pdfFilePath;
    private Long reportId;
    private boolean haveWarrant;
//    private boolean updateCalculatedFieldsManually;
    private int sendCount;
    private boolean saveError;
    private List<ApproveResultItem> deleteMessages = new ArrayList<ApproveResultItem>();

    private List<ApproveResultItem> submitMessages = new ArrayList<ApproveResultItem>();
    private List<ApproveResultItem> copyMessages = new ArrayList<ApproveResultItem>();

    private List<ApproveResultItem> signMessages = new ArrayList<ApproveResultItem>();
    private List<ReportListItem> reportsForSign = new ArrayList<ReportListItem>();

    private List<ReportPeriod> overdueReports;
    private boolean showOverdueReportsOnLoad = true;

    private List<UserWarrant> userWarrants;
    private UserWarrant selectedUserWarrant;

    private UserWarrant userWarrant;

    @PostConstruct
    public void init() {
        Date dateStart = new Date();
        try {
            if (sessionBean.isEjbNull()) {
                sessionBean.init();
            }

            filterReportDateBegin = filterReportDateEnd = Util.getFirstDayOfCurrentMonth();
            allFilterStatuses = new ArrayList<SelectItem>();
            for (ReportStatus.Status status : ReportStatus.statuses)
                allFilterStatuses.add(new SelectItem(status.name(), ReportStatus.resMap.get("ru_" + status.name())));
            filterStatuses = new ArrayList<String>();

            resetFilterStatuses();
            readRefNpa();
            resetFilterForms();
            refreshRespList(dateStart, true);
            resetFilterResp();

            haveWarrant = sessionBean.getReference().respondentHaveWarrant(sessionBean.respondent.getRecId(), dateStart);

            ldmReportListItem = new LdmReportListItem();
            ldmReportStatus = new LdmReportStatus();

            // move to method preRender
            // refreshReportList();

            controlResultTypes = new HashMap<Long, ControlResult.ResultType>();
            controlResultTypes.put(ControlResult.ResultType.SUCCESS.getId(), ControlResult.ResultType.SUCCESS);
            controlResultTypes.put(ControlResult.ResultType.FAIL.getId(), ControlResult.ResultType.FAIL);
            controlResultTypes.put(ControlResult.ResultType.ERROR.getId(), ControlResult.ResultType.ERROR);
            controlResultTypes.put(ControlResult.ResultType.NO_DATA.getId(), ControlResult.ResultType.NO_DATA);
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

            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('statusDialog').show()");
            SettingsItem settingsItem = sessionBean.getPersistence().getSettingsItemByTypeUserId(SettingsItem.Type.SHOW_OVERDUE_REPORTS, sessionBean.user.getUserId());
            if (settingsItem != null && SettingsValueConverter.fromRaw(settingsItem.getRawValue(), false, Boolean.class)) {
                if (showOverdueReportsOnLoad) {
                    refreshOverdueReports();
                    if (overdueReports.size() > 0) {
                        context.execute("PF('wDlgOverdueReports').show()");
                    }
                    showOverdueReportsOnLoad = false;
                }
            }
        } catch (Exception e) {
            applicationBean.redirectToErrorPage(e);
        }
    }

    public void refreshReportList() {
        RefRespondentItem respondent = sessionBean.respondent;
        if (respondent == null) {
            ldmReportListItem = null;
            reportList = null;
            selectedReports = new ArrayList<ReportListItem>();
            return;
        }
        List<String> formCodes = new ArrayList<String>();
        for (Form form : filterForms)
            formCodes.add(form.getCode());

        List<String> idnList = new ArrayList<String>();
        for (RefRespondentItem resp : filterResp){
            idnList.add(resp.getIdn());
        }

        reportList = sessionBean.getPersistence().getReportListByUserIdnFormCodesReportDateRange(sessionBean.user.getUserId(), respondent.getIdn(), idnList, haveWarrant, formCodes,
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
            boolean matchFormName = filterFormName == null || filterFormName.isEmpty() || item.getFormName().toLowerCase().contains(filterFormName.toLowerCase());
            if (!(matchStatus && matchFormName)) it.remove();
        }

        for (ReportListItem item : reportList) {
            item.setStatusName(ReportStatus.resMap.get(sessionBean.languageCode + "_" + item.getStatus()) + " " + item.getSignInfo());
            if (item.getControlResultCode() != null && !item.getControlResultCode().isEmpty())
                item.setControlResultName(ControlResultType.resMap.get(sessionBean.languageCode + "_" + item.getControlResultCode()));
        }

        ((LdmReportListItem) ldmReportListItem).setSrcCollection(reportList);

        // Selection
        /*if (selectedReports.size() > 0) {
            boolean hasSelectedItem = false;
            for (ReportListItem item : reportList)
                if (item.equals(selectedReportListItem)) {
                    hasSelectedItem = true;
                    break;
                }
            if (!hasSelectedItem) selectedReportListItem = null;
        }*/
    }

    public void refreshStatuses() {
        userBean.checkAccess("RESP:FORM:HISTORY");
        ReportListItem selectedReportListItem = null;
        if (selectedReports.size() == 1) {
            selectedReportListItem = selectedReports.get(0);
        } else {
            statuses = new ArrayList<ReportStatus>();
            return;
        }
        statuses = sessionBean.getPersistence().getReportStatusHistoryByReportId(selectedReportListItem.getId(), false, null);

        ((LdmReportStatus) ldmReportStatus).setSrcCollection(statuses);
        ((LdmReportStatus) ldmReportStatus).setDraftStatusesHidden(draftStatusesHidden);

        sendCount = 0;
        for (ReportStatus item : statuses)
            if (item.getStatusCode().equals("COMPLETED")) sendCount++;
        setDraftStatusesHidden(true);
    }

    public void refreshOverdueReports() {
        RefRespondentItem respondent = sessionBean.respondent;
        if (respondent == null)
            overdueReports = new ArrayList<ReportPeriod>();
        else
            overdueReports = sessionBean.getPersistence().getRespondentOverdueReports(sessionBean.user.getUserId(), respondent.getIdn(), 1);
        for (ReportPeriod item : overdueReports) {
            if (item.getStatus() != null) {
                item.setStatusName(ReportStatus.resMap.get(sessionBean.languageCode + "_" + item.getStatus()));
            }
        }
    }

    public int getLeftDaysForSort(ReportPeriod item) {
        if (item == null) {
            return 0;
        } else if (item.getPeriodAlgError() == null) {
            return -1;
        } else {
            return item.getLeftDays() != null ? item.getLeftDays() : 0;
        }
    }

    public String getLeftDays(ReportPeriod item) {
        if (item == null) {
            return "";
        } else if (item.getPeriodAlgError() != null) {
            return "Ошибка";
        } else {
            return item.getLeftDays() != null ? item.getLeftDays().toString() : "";
        }
    }

    public void onFiltersToggle() {
    }

    public void onFilterNPAShow() {
        //resetFilterForms();
    }

    public void clearFilters() {
        filterReportDateBegin = filterReportDateEnd = null;
        filterFormName = null;
        resetFilterStatuses();
        resetFilterNPA();
        resetFilterForms();
        resetFilterResp();
    }

    public void onDateSelect() {
        if (filterReportDateBegin == null && filterReportDateEnd != null) filterReportDateBegin = filterReportDateEnd;
        if (filterReportDateBegin != null && filterReportDateEnd == null) filterReportDateEnd = filterReportDateBegin;
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

    public void onFilterFormsHide() {
        updateFilterFormsText();
    }

    public void onFilterNPAHide() {
        updateFilterNPAText();
        resetFilterForms();
    }

    private void updateFilterFormsText() {
        int size = filterForms.size();
        if (size == 0) filterFormsText = "Нет ни одной формы!";
        else if (size == 1) filterFormsText = filterForms.get(0).getFormHistory().getName();
        else if (size == forms.size()) filterFormsText = "Все";
        else filterFormsText = "Несколько форм (" + size + ")";
    }

    public void resetFilterForms() {
        if (filterNPA.size() != 0) {
            forms = sessionBean.getPersistence().getReportFormsByUserIdNoDate(sessionBean.user.getUserId(), filterNPA);
        }else {
            forms = new ArrayList<Form>();
        }
        filterForms = new ArrayList<Form>();
        filterForms.addAll(forms);

        updateFilterFormsText();
    }

    public void onFilterRespHide(){
        updateFilterRespText();
    }

    private void updateFilterRespText() {
        int size = filterResp.size();
        if (size == 0) filterRespText = "Нет ни одной организации!";
        else if (size == 1) filterRespText = filterResp.get(0).getNameRu();
        else if (size == respList.size()) filterRespText = "Все";
        else filterRespText = "Несколько организаций (" + size + ")";
    }

    public void resetFilterResp() {
        filterResp = new ArrayList<RefRespondentItem>();
        filterResp.addAll(respList);

        updateFilterRespText();
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

    public void readRefNpa(){
        npaList = (List<RefNpaItem>)sessionBean.getReference().getRefAbstractList(RefNpaItem.REF_CODE,  sessionBean.getIntegration().getNewDateFromBackEndServer());
        resetFilterNPA();
    }

    /*public void validateSubmitted() {
        sessionBean.resetPoll();
        Date reportDate = selectedReportListItem.getReportDate();
        List<Form> forms = sessionBean.getPersistence().getFormsByCodeLanguageReportDate(selectedReportListItem.getFormCode(), sessionBean.languageCode, reportDate);
        if (forms.isEmpty()) {
            reportBean.validationFailed();
            return;
        }
        reportBean.validateSubmitted(forms.get(0));
    }*/

    public void onShowErrors() {
        userBean.checkAccess("RESP:FORM:ERRORS");
    }

    /*public void save() {
        sessionBean.resetPoll();
        userBean.checkFormAccess(selectedReportListItem.getFormCode(), "F:EDIT");

        ReportStatus status = sessionBean.getPersistence().getLastReportStatusByReportId(selectedReportListItem.getId());
        saveError = !sessionBean.getPersistence().isStatusCompatible(status.getStatusCode(), ReportStatus.Status.DRAFT.name());
        if (saveError) {
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", MessageFormat.format("Невозможно сохранить отчет. Статус отчета - {0}", status.getStatusName("ru"))));
            return;
        }

        Date reportDate = selectedReportListItem.getReportDate();
        List<Form> forms = sessionBean.getPersistence().getFormsByCodeLanguageReportDate(selectedReportListItem.getFormCode(), sessionBean.languageCode, reportDate);
        if (forms.isEmpty()) return;
        reportBean.saveAndGetId(selectedReportListItem.getId(), forms.get(0), reportDate);
    }*/

    /*public String saveAndQuitAction(){
        if(saveError)
            return null;
        else
            return "/views/reports/view?faces-redirect=true";
    }*/

    public String getHash() {
        if (selectedReports.size() != 1) return null;
        ReportListItem selectedReportListItem = selectedReports.get(0);
        ReportHistory lastReportHistory = sessionBean.getPersistence().getLastReportHistoryByReportId(selectedReportListItem.getId(), false, false, false, null);
        return lastReportHistory.getHash();
    }

    public void setHash(String hash) { // Dummy setter, required for inputHidden page component
    }

    public Boolean haveAttachedFile(ReportListItem reportListItem) {
        Boolean result = false;
        if (reportListItem.getCanAttachedFile()) {
            long lastReportHistoryId = sessionBean.getPersistence().getLastReportHistoryIdByReportId(reportListItem.getId(), false, null);
            List<AttachedFile> reportFiles = sessionBean.getPersistence().getFileListByLinkId(lastReportHistoryId, 1, null);
            if (reportFiles.isEmpty()) {
                result = false;
            } else
                result = true;
        }
        return result;
    }

    public Boolean haveAttachedLetter(ReportListItem reportListItem) {
        Boolean result = false;
        long lastReportHistoryId = sessionBean.getPersistence().getLastReportHistoryIdByReportId(reportListItem.getId(), false, null);
        List<AttachedFile> reportFiles = sessionBean.getPersistence().getFileListByLinkId(lastReportHistoryId, 2, null);
        if (reportFiles.isEmpty()) {
            result = false;
        } else
            result = true;

        return result;
    }

    public String gotoSignaturePage() {
        userBean.checkAccess("RESP:FORM:SIGN");

        this.userWarrant = userWarrant;
        signMessages.clear();
        reportsForSign.clear();
        Date curDate = sessionBean.getIntegration().getNewDateFromBackEndServer();

        if (selectedReports.size() == 0) {
            return null;
        }

        for (ReportListItem selectedReportListItem : selectedReports) {
            Date reportDate = selectedReportListItem.getReportDate();

            ReportStatus status = sessionBean.getPersistence().getLastReportStatusByReportId(selectedReportListItem.getId(), false, null);
            if (!sessionBean.getPersistence().isStatusCompatible(status.getStatusCode(), ReportStatus.Status.SIGNED.name())) {
                signMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                        selectedReportListItem.getFormName(),
                        selectedReportListItem.getRespondentNameRu(),
                        selectedReportListItem.getReportDate(),
                        MessageFormat.format("Невозможно подписать отчет. Статус отчета - {0}", status.getStatusName("ru"))));
                continue;
            }

            List<Form> forms = sessionBean.getPersistence().getFormsByCodeLanguageReportDate(selectedReportListItem.getFormCode(), sessionBean.languageCode, reportDate, null);
            if (forms.isEmpty()) {
                signMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                        selectedReportListItem.getFormName(),
                        selectedReportListItem.getRespondentNameRu(),
                        selectedReportListItem.getReportDate(),
                        "Не найден шаблон формы"));
                continue;
            }

            if (selectedReportListItem.getIdnChild() != null && !sessionBean.getReference().respondentHaveWarrantByIdn(sessionBean.respondent.getRecId(), curDate, selectedReportListItem.getIdnChild())) {
                signMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                        selectedReportListItem.getFormName(),
                        selectedReportListItem.getRespondentNameRu(),
                        selectedReportListItem.getReportDate(),
                        "Отсутствует доверенность"));
                continue;
            }

            if (selectedReportListItem.getCanAttachedFile()) {
                if (!haveAttachedFile(selectedReportListItem)) {
                    signMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                            selectedReportListItem.getFormName(),
                            selectedReportListItem.getRespondentNameRu(),
                            selectedReportListItem.getReportDate(),
                            "Необходимо загрузить пояснительную записку!"));
                    continue;
                }
            }

            Form form = forms.get(0);
            Map<String, String> kvMap = sessionBean.getPersistence().getKvMap(selectedReportListItem.getId());
            reportBean.validateKvMap(kvMap, form.getFormHistory().getId());
            if (reportBean.getValidationResult() != ReportBean.ValidationResult.OK) {
                signMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                        selectedReportListItem.getFormName(),
                        selectedReportListItem.getRespondentNameRu(),
                        selectedReportListItem.getReportDate(),
                        "Отчёт не прошёл предварительную проверку. Для просмотра ошибок откройте отчёт и нажмите кнопку \"Проверить\""));
                continue;
            }
            /*Long formId = sessionBean.getPersistence().getFormIdByCode(selectedReportListItem.getFormCode());
            Form formWithInputValueChecks = sessionBean.getPersistence().getForm(formId, newReportDate);
            ReportHistory lastReportHistory = sessionBean.getPersistence().getLastReportHistoryByReportId(selectedReportListItem.getId(), true, false, false);

            if (sessionBean.getPersistence().fillDefaultValueByFormWithValues(lastReportHistory.getId(), formWithInputValueChecks.getFormHistory().getInputValueChecks(), lastReportHistory.getData(), curDate, true, false)) {

                sessionBean.getPersistence().clearSignatures(selectedReportListItem.getId());

                signMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                        selectedReportListItem.getFormName(),
                        selectedReportListItem.getRespondentNameRu(),
                        selectedReportListItem.getReportDate(),
                        "Дата подписания изменена на текущую дату!", ApproveResultItem.ResultType.SUCCESS));
            }
            selectedReportListItem.setHash(lastReportHistory.getHash());*/
            reportsForSign.add(selectedReportListItem);
        }
        return "/views/reports/sign?faces-redirect=true";
    }

    public void refreshUserWarrants(){
        userWarrants = sessionBean.getPersistence().getActiveWarrantsByAttorney(sessionBean.user.getUserId(), sessionBean.getIntegration().getNewDateFromBackEndServer());
        selectedUserWarrant = null;
    }

    public void onSign(UserWarrant userWarrant) {
        userBean.checkAccess("RESP:FORM:SIGN");
        this.userWarrant = userWarrant;
        Date curDate = sessionBean.getIntegration().getNewDateFromBackEndServer();

        List<ReportListItem> deleted = new ArrayList<ReportListItem>();
        for (ReportListItem selectedReportListItem : reportsForSign) {
            ReportStatus status = sessionBean.getPersistence().getLastReportStatusByReportId(selectedReportListItem.getId(), false, null);
            saveError = !sessionBean.getPersistence().isStatusCompatible(status.getStatusCode(), ReportStatus.Status.SIGNED.name());
            if (saveError) {
                String message = MessageFormat.format("Невозможно подписать отчет. Статус отчета - {0}", status.getStatusName("ru"));
                addSignResultItem(selectedReportListItem, ApproveResultItem.ResultType.FAIL, message);
                deleted.add(selectedReportListItem);
                continue;
            }else if (selectedReportListItem.getIdnChild() != null && !sessionBean.getReference().respondentHaveWarrantByIdn(sessionBean.respondent.getRecId(), curDate, selectedReportListItem.getIdnChild())) {
                String message = "Отсутствует доверенность";
                addSignResultItem(selectedReportListItem, ApproveResultItem.ResultType.FAIL, message);
                deleted.add(selectedReportListItem);
                continue;
            }

            /*long reportHistoryId = sessionBean.getPersistence().getLastReportHistoryIdByReportId(selectedReportListItem.getId(), false, null);

            if (sessionBean.getPersistence().updateCurrentDate(reportHistoryId, sessionBean.respondent.getRecId(), sessionBean.abstractUser)) {
                signMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                        selectedReportListItem.getFormName(),
                        selectedReportListItem.getRespondentNameRu(),
                        selectedReportListItem.getReportDate(),
                        "Дата подписания изменена на текущую дату!", ApproveResultItem.ResultType.SUCCESS));
            }
            ReportHistory lastReportHistory = sessionBean.getPersistence().getLastReportHistoryByReportIdNoLobs(selectedReportListItem.getId(), false, null);
            selectedReportListItem.setHash(lastReportHistory.getHash());*/
        }
        reportsForSign.removeAll(deleted);
    }

    public void updateSignatures() {
        userBean.checkAccess("RESP:FORM:SIGN");

        Date signDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
        for (ReportListItem selectedReportListItem : reportsForSign) {

            // todo Для тестирование адрес киск betaca.kisc.kz:62255, при промышленном возвратить адрес http://ca.kisc.kz:62255 и взять на нее доступ
//            String signatureInfo = kiscBean.getSignatureInfo("http://ca.kisc.kz:62255", selectedReportListItem.getSignature(), sessionBean.respondent.getIdn(), sessionBean.portalUser.getIdn(), applicationBean.getLocale().getLanguage());
//        String signatureInfo = kiscBean.getSignatureInfo("http://betaca.kisc.kz:62255", signature, sessionBean.respondent.getIdn(), sessionBean.portalUser.getIdn(), applicationBean.getLocale().getLanguage());
            String signatureInfo;
            if (isTestUser()) {
                signatureInfo = "";
            } else {
                signatureInfo = kiscBean.getSignatureInfo("http://ca.kisc.kz:62255", selectedReportListItem.getSignature(), sessionBean.respondent.getIdn(), sessionBean.portalUser.getIdn(), applicationBean.getLocale().getLanguage());
//                signatureInfo = kiscBean.getSignatureInfo("http://betaca.kisc.kz:62255", selectedReportListItem.getSignature(), sessionBean.respondent.getIdn(), sessionBean.portalUser.getIdn(), applicationBean.getLocale().getLanguage());
            }

            if (signatureInfo.startsWith("ERROR:")) {
                String errMessage = signatureInfo.substring(7);
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", errMessage));
                addSignResultItem(selectedReportListItem, ApproveResultItem.ResultType.FAIL, errMessage);
            } else {
                try {
                    AuditEvent auditEvent = new AuditEvent();
                    auditEvent.setCodeObject(selectedReportListItem.getFormCode());
                    auditEvent.setNameObject(null);
                    auditEvent.setIdKindEvent(7L);
                    auditEvent.setDateEvent(signDate);
                    auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
                    auditEvent.setDateIn(selectedReportListItem.getReportDate());
                    auditEvent.setRecId(selectedReportListItem.getId());
                    auditEvent.setUserId(sessionBean.abstractUser.getId());
                    auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());

                    reportBean.updateSignature(selectedReportListItem.getId(), selectedReportListItem.getSignature(), signDate, auditEvent, userWarrant != null ? userWarrant.getId() : 0);
                    addSignResultItem(selectedReportListItem, ApproveResultItem.ResultType.SUCCESS, "");
                } catch (Exception e) {
                    addSignResultItem(selectedReportListItem, ApproveResultItem.ResultType.FAIL, e.getMessage());
                }
            }
        }

        Collections.sort(signMessages, new OperationResultComparator());

        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('wDlgSignResults').show()");
    }

    private void addSignResultItem(ReportListItem selectedReportListItem, ApproveResultItem.ResultType resultType, String message) {
        ApproveResultItem resultItem = null;
        for (ApproveResultItem item : signMessages) {
            if (item.getReportId() == selectedReportListItem.getId()) {
                resultItem = item;
                break;
            }
        }
        if (resultItem == null) {
            resultItem = new ApproveResultItem(selectedReportListItem.getId(),
                    selectedReportListItem.getFormName(),
                    selectedReportListItem.getRespondentNameRu(),
                    selectedReportListItem.getReportDate(),
                    message,
                    resultType
            );
            signMessages.add(resultItem);
        } else {
            resultItem.setResultType(resultType);
            if (message != null && !message.isEmpty()) {
                resultItem.setMessage(message + "\n" + resultItem.getMessage());
            }
        }
    }

    public boolean canUpdateSignature() {
        if (selectedReports.size() == 0) {
            return false;
        }
        if (!userBean.hasPermission("RESP:FORM:SIGN")) {
            return false;
        }
        if (!testUsers.contains(sessionBean.user.getScreenName())) {
            if (selectedReports.size() == 1) {
                ReportListItem selectedReportListItem = selectedReports.get(0);
                if (!sessionBean.getPersistence().isStatusCompatible(selectedReportListItem.getStatus(), ReportStatus.Status.SIGNED.name())) {
                    return false;
                }
            }
        }
        return true;
    }

    public String open() {
        String result;
        sessionBean.resetPoll();
        if (selectedReports.size() != 1) {
            return null;
        }
        ReportListItem selectedReportListItem = selectedReports.get(0);
        userBean.checkFormAccess(selectedReportListItem.getFormCode(), "F:SHOW", selectedReportListItem.getIdn());

        openResultMessage = "";
        Date reportDate = selectedReportListItem.getReportDate();
        //Date curDate = sessionBean.getIntegration().getNewDateFromBackEndServer();

        // Check whether the data form is approved
        ReportStatus lastStatus = sessionBean.getPersistence().getLastReportStatusByReportId(selectedReportListItem.getId(), false, null);
        approved = lastStatus.getStatusCode().equals(ReportStatus.Status.APPROVED.name());

        List<Form> forms = sessionBean.getPersistence().getFormsByCodeLanguageReportDate(selectedReportListItem.getFormCode(), sessionBean.languageCode, reportDate, null);
        Form form = forms.isEmpty() ? null : forms.get(0);

//        updateCalculatedFieldsManually = form != null && form.getFormHistory().getFormTag() != null && form.getFormHistory().getFormTag().updateCalculatedFieldsManually;
        String statusName = selectedReportListItem == null ? null : selectedReportListItem.getStatusName();
        String status = selectedReportListItem == null ? null : selectedReportListItem.getStatus();
        reportBean.setReportDate(reportDate);
        reportBean.setApproved(approved);
        reportBean.setStatusName(statusName);
        reportBean.setStatus(status);
        reportBean.setCanAttachedFile(selectedReportListItem.getCanAttachedFile());
        reportBean.setHaveAttachedFile(selectedReportListItem.getHaveAttachedFile());
        reportBean.setHaveAttachedLetter(selectedReportListItem.getHaveAttachedLetter());
        reportBean.setReportId(selectedReportListItem.getId());
        reportBean.setFixed(false);
        reportBean.setPollEnabled(false);
        reportBean.setRightSave(userBean.hasPermissionForm(selectedReportListItem.getFormCode(), "F:EDIT", selectedReportListItem.getIdn()));
        reportBean.setFormTypeCode(form.getTypeCode());
        if (approved) {
            openResultMessage = "Отчёт за эту дату уже утвержден НБРК.<br/>Редактирование и сохранение будет не возможно.";
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('wDlgOpen').show();");
            result = null;
        }else{
            reportBean.clearErrors();
            reportBean.setUpdateCalculatedFieldsManually(form != null && form.getFormHistory().getFormTag() != null && form.getFormHistory().getFormTag().updateCalculatedFieldsManually);
            /*

            При открытии не меняю дату подписания !

            if(lastStatus.getStatusCode().equals("DRAFT")){
                ReportHistory reportHistory  = sessionBean.getPersistence().getLastReportHistoryByReportId(selectedReportListItem.getId(), true, false);
                FormHistory formHistory = sessionBean.getPersistence().getFormHistoryWithInputValueChecks(form.getFormHistory().getId());
                if(reportBean.fillDefaultValueByFormWithValues(reportHistory.getId(), formHistory.getInputValueChecks(), reportHistory.getData(), curDate, true,  false)){
                    openResultMessage = "Дата подписания не равна текущей дате.<br/>Дата подписания изменена на текущую дату!";
                    RequestContext context = RequestContext.getCurrentInstance();
                    context.execute("PF('wDlgOpen').show();");
                    result = null;
                }
            }*/
            if(form.getTypeCode().equals(Form.Type.INPUT_RAW.name())) {
                result = "/views/reports/form_raw?faces-redirect=true";
                reportBean.refreshFileList(selectedReportListItem.getId(), null, "RESP", 3);
            }else {
                result = "/views/reports/form?faces-redirect=true";
                String html = selectedReportListItem == null ? null : sessionBean.getPersistence().getHtmlWithReportData(selectedReportListItem.getId(),
                        lastStatus.getStatusCode().equals(ReportStatus.Status.APPROVED.name()), false);
                reportBean.setHtml(html);
            }
        }
        return result;
    }

    public String closeOpen() {
        String result;
        reportBean.clearErrors();
        if(reportBean.getFormTypeCode().equals(Form.Type.INPUT_RAW.name())) {
            result = "/views/reports/form_raw?faces-redirect=true";
            reportBean.refreshFileList(reportBean.getReportId(), null, "RESP", 3);
        }else {
            result = "/views/reports/form?faces-redirect=true";
            String html = sessionBean.getPersistence().getHtmlWithReportData(reportBean.getReportId(), true, false);
            reportBean.setHtml(html);
        }
        return result;
    }

    public void submit() {
        RequestContext.getCurrentInstance().execute("PF('wDlgConfirmSubmit').hide();");
        userBean.checkAccess("RESP:FORM:SEND");
        submitMessages.clear();
        controlResults.clear();
        Date curDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
        if (selectedReports.size() == 0) {
            return;
        }
        for (ReportListItem selectedReportListItem : selectedReports) {
            Date reportDate = selectedReportListItem.getReportDate();

            Long formId = sessionBean.getPersistence().getFormIdByCode(selectedReportListItem.getFormCode());
            Form formWithInputValueChecks = sessionBean.getPersistence().getForm(formId, newReportDate);
            ReportHistory lastReportHistory = sessionBean.getPersistence().getLastReportHistoryByReportId(selectedReportListItem.getId(), true, false, false, null);

            ReportStatus lastStatus = sessionBean.getPersistence().getLastReportStatusByReportId(selectedReportListItem.getId(), false, null);

            if (ReportStatus.suUserStatuses.contains(ReportStatus.Status.valueOf(lastStatus.getStatusCode()))) {
                submitMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                        selectedReportListItem.getFormName(),
                        selectedReportListItem.getRespondentNameRu(),
                        selectedReportListItem.getReportDate(),
                        MessageFormat.format("Отчет уже отправлен в НБРК и имеет статус \"{0}\".", ReportStatus.resMap.get("ru_" + lastStatus.getStatusCode()))));
                continue;
            } else if (!sessionBean.getPersistence().isStatusCompatible(lastStatus.getStatusCode(), ReportStatus.Status.COMPLETED.name())) {
                submitMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                        selectedReportListItem.getFormName(),
                        selectedReportListItem.getRespondentNameRu(),
                        selectedReportListItem.getReportDate(),
                        MessageFormat.format("Отчет должен иметь статус \"{0}\".", ReportStatus.resMap.get("ru_" + ReportStatus.Status.SIGNED.name()))));
                continue;
            } else if (selectedReportListItem.getIdnChild() != null) {
                RespondentWarrant respondentWarrant = sessionBean.getReference().getRespondentWarrant(sessionBean.respondent.getRecId(), selectedReportListItem.getIdnChild(), curDate);
                if(respondentWarrant == null || !respondentWarrant.getHaveFile()) {
                    submitMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                            selectedReportListItem.getFormName(),
                            selectedReportListItem.getRespondentNameRu(),
                            selectedReportListItem.getReportDate(),
                            "Отсутствует доверенность"));
                    continue;
                }
            }

            /*if (sessionBean.getPersistence().fillDefaultValueByFormWithValues(lastReportHistory.getId(), formWithInputValueChecks.getFormHistory().getInputValueChecks(), lastReportHistory.getData(), curDate, false, false)) {
                submitMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                        selectedReportListItem.getFormName(),
                        selectedReportListItem.getRespondentNameRu(),
                        selectedReportListItem.getReportDate(),
                        "Дата(ы) подписи в отчете не равна текущей дате. Необходимо переподписать отчет посредством ЭЦП!"));
                continue;
            }*/

            if (selectedReportListItem.getCanAttachedFile()) {
                if (!haveAttachedFile(selectedReportListItem)) {
                    submitMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                            selectedReportListItem.getFormName(),
                            selectedReportListItem.getRespondentNameRu(),
                            selectedReportListItem.getReportDate(),
                            "Необходимо загрузить пояснительную записку!"));
                    continue;
                }
            }

            List<Form> forms = sessionBean.getPersistence().getFormsByCodeLanguageReportDate(selectedReportListItem.getFormCode(), sessionBean.languageCode, reportDate, null);
            if (forms.isEmpty()) {
                submitMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                        selectedReportListItem.getFormName(),
                        selectedReportListItem.getRespondentNameRu(),
                        selectedReportListItem.getReportDate(),
                        "Не найден шаблон формы."));
                continue;
            }
            Form form = forms.get(0);
            Long respondentId = Long.valueOf(sessionBean.respondent.getRecId());

            // Check whether the data form is approved
            approved = false;
            ReportStatus lastSuperUserStatus = sessionBean.getPersistence().getLastReportStatusByReportId(selectedReportListItem.getId(), true, null);
            if (lastSuperUserStatus != null && lastSuperUserStatus.getStatusCode().equals(ReportStatus.Status.APPROVED.name()))
                approved = true;

            if (approved) {
                submitMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                        selectedReportListItem.getFormName(),
                        selectedReportListItem.getRespondentNameRu(),
                        selectedReportListItem.getReportDate(),
                        "Отчёт за эту дату уже утвержден НБРК. Для снятия утверждения необходимо обратиться в НБРК."));
                continue;
            }

            // TODO нужно убрать как будет включена проверка на подпись
            Map<String, String> kvMap = sessionBean.getPersistence().getKvMap(selectedReportListItem.getId());

            reportBean.validateKvMap(kvMap, form.getFormHistory().getId());
            if (reportBean.getValidationResult() != ReportBean.ValidationResult.OK) {
                submitMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                        selectedReportListItem.getFormName(),
                        selectedReportListItem.getRespondentNameRu(),
                        selectedReportListItem.getReportDate(),
                        "Не прошёл предварительную проверку. Для просмотра ошибок откройте отчёт и нажмите кнопку \"Проверить\"."));
                continue;
            }

            if (!selectedReportListItem.getStatus().equalsIgnoreCase(ReportStatus.Status.SIGNED.toString())) {
                submitMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                        selectedReportListItem.getFormName(),
                        selectedReportListItem.getRespondentNameRu(),
                        selectedReportListItem.getReportDate(),
                        "Отчет не подписан."));
                continue;
            }

            if (!isTestUser()) {
                List<PortalUser> unsignedUsers = getUnsignedUsers(selectedReportListItem.getId(), respondentId);
                if (unsignedUsers.size() > 0) {
                    StringBuilder b = new StringBuilder("Следующие пользователи должны подписать отчет: ");
                    for (int i = 0; i < unsignedUsers.size(); i++) {
                        PortalUser user = unsignedUsers.get(i);
                        if (i > 0)
                            b.append(", ");
                        b.append(user.getFullName());
                    }
                    b.append(".");
                    submitMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                            selectedReportListItem.getFormName(),
                            selectedReportListItem.getRespondentNameRu(),
                            selectedReportListItem.getReportDate(),
                            b.toString()));
                    continue;
                }

                List<Long> unsignedPostRecIds = getUnsignedPostRecIds(selectedReportListItem.getId());
                if (unsignedPostRecIds.size() > 0) {
                    StringBuilder b = new StringBuilder("Требуются подписи следующих должностей: ");
                    Date serverDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
                    for (int i = 0; i < unsignedPostRecIds.size(); i++) {
                        RefPostItem filter = new RefPostItem();
                        filter.setRecId(unsignedPostRecIds.get(i));
                        List<RefPostItem> refPostItems = (List<RefPostItem>) sessionBean.getReference().getRefAbstractByFilterList(RefPostItem.REF_CODE,filter, serverDate);
                        if (refPostItems.size() == 0) {
                            throw new IllegalStateException(MessageFormat.format("Должность по REC_ID {0} на дату {1} не найдена", filter.getRecId(), serverDate));
                        }
                        if (i > 0)
                            b.append(", ");
                        b.append(refPostItems.get(0).getNameRu());
                    }
                    b.append(".");
                    submitMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                            selectedReportListItem.getFormName(),
                            selectedReportListItem.getRespondentNameRu(),
                            selectedReportListItem.getReportDate(),
                            b.toString()));
                    continue;
                }
            }

            ReportStatus status = new ReportStatus();
            status.setReportHistory(sessionBean.getPersistence().getLastReportHistoryByReportIdNoLobs(selectedReportListItem.getId(), false, null));
            status.setStatusCode(ReportStatus.Status.COMPLETED.name());
            status.setStatusDate(curDate);
            status.setUserId(sessionBean.abstractUser.getId());
            status.setUserInfo(sessionBean.abstractUser.getDisplayName());
            status.setUserLocation(sessionBean.abstractUser.getLocation());
            String msg;
            if(haveWarrant && selectedReportListItem.getIdnChild() != null){
                RespondentWarrant respondentWarrant = sessionBean.getReference().getRespondentWarrant(sessionBean.respondent.getRecId(), selectedReportListItem.getIdnChild(), curDate);
                status.setRespWarrantId(respondentWarrant.getId());
                RefRespondentItem filter = new RefRespondentItem();
                filter.setIdn(selectedReportListItem.getIdnChild());
                filter.setBeginDate(curDate);
                RefRespondentItem warrantResp = (RefRespondentItem) sessionBean.getReference().getRefAbstractItem(RefRespondentItem.REF_CODE, filter);
                msg = "Отправил(а): " + sessionBean.abstractUser.getDisplayName() + " [" + sessionBean.abstractUser.getLocation() + "]" + ", от организации: " + warrantResp.getPersonName() +
                "по доверенности №" + respondentWarrant.getNum() + " на дату " + Convert.getDateStringFromDate(respondentWarrant.getbDate());
            }else{
                msg = "Отправил(а): " + sessionBean.abstractUser.getDisplayName() + " [" + sessionBean.abstractUser.getLocation() + "]";
            }
            status.setMessage(msg);

            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(selectedReportListItem.getFormCode());
            auditEvent.setNameObject(null);
            auditEvent.setIdKindEvent((long) (lastSuperUserStatus == null ? 8 : 58));
            auditEvent.setDateEvent(curDate);
            auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
            auditEvent.setDateIn(reportDate);
            auditEvent.setRecId(selectedReportListItem.getId());
            auditEvent.setUserId(sessionBean.abstractUser.getId());
            auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());

            sessionBean.getPersistence().insertReportStatusHistory(status, auditEvent);

            submitMessages.add(new ApproveResultItem(selectedReportListItem.getId(),
                    selectedReportListItem.getFormName(),
                    selectedReportListItem.getRespondentNameRu(),
                    selectedReportListItem.getReportDate(),
                    "", ApproveResultItem.ResultType.SUCCESS));

            // doPerformControl(selectedReportListItem, true, true);
            // doPerformControl(selectedReportListItem, false, false);

            ReportListItem newReportListItem = sessionBean.getPersistence().getReportListByReportIdAdvanced(selectedReportListItem.getId(), "ru", false);
            selectedReportListItem.setFirstCompletedDate(newReportListItem.getFirstCompletedDate());
            selectedReportListItem.setLastCompletedDate(newReportListItem.getLastCompletedDate());
            selectedReportListItem.setCompleteCount(newReportListItem.getCompleteCount());
            selectedReportListItem.setControlResultCode(newReportListItem.getControlResultCode());
            selectedReportListItem.setControlResultName(ControlResultType.resMap.get(sessionBean.languageCode + "_" + selectedReportListItem.getControlResultCode()));

            selectedReportListItem.setStatus(status.getStatusCode());
            selectedReportListItem.setStatusDate(status.getStatusDate());
            selectedReportListItem.setStatusName(ReportStatus.resMap.get(sessionBean.languageCode + "_" + selectedReportListItem.getStatus()));
        }

        performControlAll(true, true, false, false);
        performControlAll(false, true, false, false);

        refreshReportList();

        Collections.sort(submitMessages, new OperationResultComparator());

        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('wDlgSubmitResults').show()");
    }

    public boolean isTestUser() {
        return testUsers.contains(sessionBean.user.getScreenName().toLowerCase());
    }

    /**
     * Список пользователей, которые должны были подписать отчет, но не подписавшие
     *
     * @param reportId
     * @param respondentId
     * @return
     */
    private List<PortalUser> getUnsignedUsers(long reportId, long respondentId) {
        List<PortalUser> users = new ArrayList<PortalUser>();
        List<ReportHistory> historyItems = sessionBean.getPersistence().getReportHistoryByReportIdNoLobs(reportId, null);

        if (historyItems.size() == 0)
            return users;

        List<Long> signedUserIds = new ArrayList<Long>();
        for (ReportHistorySignature signature : sessionBean.getPersistence().getSignaturesByReportHistory(historyItems.get(historyItems.size() - 1).getId())) {
            signedUserIds.add(signature.getUserId());
        }
        for (PortalUser user : sessionBean.getPersistence().getUsersByRespondentId(respondentId)) {
            if (user.isMustSign() && !signedUserIds.contains(user.getUserId()))
                users.add(user);
        }
        return users;
    }

    private List<Long> getUnsignedPostRecIds(long reportId) {
        List<Long> mustSignPostRecIds = sessionBean.getReference().getActiveSubjectTypePosts(sessionBean.respondent.getRefSubjectTypeRecId());
        long lastReportHistoryId = sessionBean.getPersistence().getLastReportHistoryIdByReportId(reportId, false, null);
        List<ReportHistorySignature> signatures = sessionBean.getPersistence().getSignaturesByReportHistory(lastReportHistoryId);
        for (ReportHistorySignature signature : signatures) {
            PortalUser user = sessionBean.getPersistence().getUserByUserId(signature.getUserId(), null);
            if (user.isMustSign() && user.getRefPostId() != null) {
                mustSignPostRecIds.remove(user.getRefPostId());
            }
        }
        return mustSignPostRecIds;
    }

    public void copy() {

        copyMessages.clear();
        userBean.checkAccess("RESP:FORM:COPY");

        for (ReportListItem reportListItem : selectedReports) {

            userBean.checkFormAccess(reportListItem.getFormCode(), "F:EDIT", reportListItem.getIdn());
            Date curDate = sessionBean.getIntegration().getNewDateFromBackEndServer();


            // из-за ошибки "Managed bean reportBean contains cyclic references" используем след.код (2 строки)
            ELContext elContext = FacesContext.getCurrentInstance().getELContext();
            NewReportBean newReportBean = (NewReportBean) FacesContext.getCurrentInstance().getApplication().getELResolver().getValue(elContext, null, "newReportBean");

            if (!sessionBean.getPersistence().checkPeriod(newReportDate, reportListItem.getFormCode(), sessionBean.respondent.getRefSubjectTypeRecId())) {
                copyMessages.add(new ApproveResultItem(reportListItem.getId(),
                        reportListItem.getFormName(),
                        reportListItem.getRespondentNameRu(),
                        reportListItem.getReportDate(),
                        MessageFormat.format("Невозможно создать отчет на {0}", Convert.dateFormatRus.format(newReportDate))));
                continue;
            } else if (reportListItem.getIdnChild() != null && !sessionBean.getReference().respondentHaveWarrantByIdn(sessionBean.respondent.getRecId(), curDate, reportListItem.getIdnChild())) {
                copyMessages.add(new ApproveResultItem(reportListItem.getId(),
                        reportListItem.getFormName(),
                        reportListItem.getRespondentNameRu(),
                        reportListItem.getReportDate(),
                        "Отсутствует доверенность"));
                continue;
            }

            List<Form> forms = sessionBean.getPersistence().getFormsByCodeLanguageReportDate(reportListItem.getFormCode(), "ru", reportListItem.getReportDate(), null);
            if (forms.size() == 0) {
                throw new IllegalStateException(MessageFormat.format("Не найден шаблон отчета {0}", reportListItem.getFormCode()));
            }
            Date beginDate = forms.get(0).getFormHistory().getBeginDate();
            Date endDate = forms.get(0).getFormHistory().getEndDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            if (beginDate.compareTo(newReportDate) > 0 || (endDate != null && endDate.compareTo(newReportDate) < 0)) {
                String message = MessageFormat.format("Заданная дата отчета ({2}) не входит в диапазон шаблона ({0}-{1})", dateFormat.format(beginDate), endDate == null ? "..." : dateFormat.format(endDate), dateFormat.format(newReportDate));
                copyMessages.add(new ApproveResultItem(reportListItem.getId(),
                        reportListItem.getFormName(),
                        reportListItem.getRespondentNameRu(),
                        reportListItem.getReportDate(),
                        message));
                continue;
            }

            newReportBean.checkReportExist(newReportDate, reportListItem);
            if (newReportBean.getExistingReportListItem() != null) {
                copyMessages.add(new ApproveResultItem(reportListItem.getId(),
                        reportListItem.getFormName(),
                        reportListItem.getRespondentNameRu(),
                        reportListItem.getReportDate(),
                        MessageFormat.format("Отчет на {0} уже существует", Convert.dateFormatRus.format(newReportDate))));
                continue;
            }

            Report report = sessionBean.getPersistence().getReport(reportListItem.getId(), null);
            if (report == null) return;
            report.setId(null);
            report.setReportDate(newReportDate);
            Map<String, String> inputValues = sessionBean.getPersistence().getKvMap(reportListItem.getId());
            sessionBean.getPersistence().saveAndGetId("WEB_COPY", report, sessionBean.respondent.getRecId(), forms.get(0).getId(), sessionBean.abstractUser, sessionBean.getIntegration().getNewDateFromBackEndServer(), inputValues, reportListItem.getReportDate(), false);

            copyMessages.add(new ApproveResultItem(reportListItem.getId(),
                    reportListItem.getFormName(),
                    reportListItem.getRespondentNameRu(),
                    reportListItem.getReportDate(),
                    "", ApproveResultItem.ResultType.SUCCESS));
        }
        selectedReports.clear();
        refreshReportList();

        Collections.sort(copyMessages, new OperationResultComparator());

        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('wDlgCopyResults').show()");
    }

    public void download() {
        if (selectedReports.size() == 1) {
            downloadSingleFile(selectedReports.get(0), null);
        } else if (selectedReports.size() > 1) {
            downloadMultipleFilesAsZip();
        }
    }

    public void downloadUnion(){
        downloadUnionFile(selectedReports, "RESP:FORM:DOWNLOAD_EXCEL", false);
    }

    public void saveNDownloadSingleFile(long reportId) {
        reportBean.save(reportId, false);
        downloadSingleFile(null, reportId);
    }

    public void downloadSingleFile(ReportListItem reportListItem, Long reportId) {
        ReportListItem selectedReportListItem;
        if (reportListItem == null)
            selectedReportListItem = reportBean.getCurrentReportListItem(reportId);
        else
            selectedReportListItem = reportListItem;
        byte[] bytes = null;
        String fileName = "";

        try {
            userBean.checkAccess("RESP:FORM:DOWNLOAD_EXCEL");

            FileWrapper fileWrapper = sessionBean.getPersistence().generateExcelFile(selectedReportListItem, false);
            bytes = fileWrapper.getBytes();

            DateFormat dfReportDate = new SimpleDateFormat("dd.MM.yyyy");
            fileName = dfReportDate.format(selectedReportListItem.getReportDate()) + "_" + selectedReportListItem.getFormCode() + "_draft." + fileWrapper.getFileFormat();

        } catch (Exception e) {
            bytes = null;
            fileName = "";
        } finally {
            applicationBean.putFileContentToResponseOutputStream(bytes, "application/vnd.ms-excel", fileName);
        }
        try {
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
            sessionBean.getPersistence().insertAuditEvent(auditEvent);
        } catch (Exception e) {
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
        }
    }

    public void downloadUnionFile(List<ReportListItem> reportListItemList, String permissionName, boolean forSuperUser){
        if (userBean.hasPermission(permissionName)) {
            try {
                List<FileWrapper> fileWrappers = sessionBean.getPersistence().generateExcelFiles(reportListItemList, forSuperUser, sessionBean.respondent.getId(), sessionBean.user.getUserId(), sessionBean.user.getLastLoginIP());
                String fileName = "reports_" + Convert.dateTimeFormatCompact_.format(sessionBean.getIntegration().getNewDateFromBackEndServer());
                FileWrapper fileWrapper = sessionBean.getPersistence().unionExcel(fileWrappers, fileName);

                applicationBean.putFileContentToResponseOutputStream(fileWrapper.getBytes(), "application/vnd.ms-excel", fileName + ".xlsx");

            } catch (Exception e) {
                RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
                RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
            }
        }
    }

    private void downloadMultipleFilesAsZip() {
        if (userBean.hasPermission("RESP:FORM:DOWNLOAD_EXCEL")) {
            try {
                List<FileWrapper> fileWrappers = sessionBean.getPersistence().generateExcelFiles(selectedReports, false, sessionBean.respondent.getId(), sessionBean.user.getUserId(), sessionBean.user.getLastLoginIP());

                byte[] zipContent;
                String fileName;

                fileName = "reports_" + Convert.dateTimeFormatCompact_.format(sessionBean.getIntegration().getNewDateFromBackEndServer()) + ".zip";
                zipContent = applicationBean.createExcelFilesZipContent(fileWrappers);

                applicationBean.putFileContentToResponseOutputStream(zipContent, "application/zip", fileName);
            }catch (Exception e) {
                RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
                RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
            }
        }
    }

    public void saveNPreparePdfFile(Long reportId) {
        reportBean.save(reportId, false);
        preparePdfFile(null, reportId, true);
    }

    public void preparePdfFile(ReportListItem repListItem, Long repId, boolean isReportOpen) {
        ReportListItem selectedReportListItem;
        if (repListItem == null)
            selectedReportListItem = reportBean.getCurrentReportListItem(repId);
        else
            selectedReportListItem = repListItem;
        try {
            userBean.checkAccess("RESP:FORM:PRINT");

            FileWrapper fileWrapper = sessionBean.getPersistence().generatePdfFile(selectedReportListItem, false);

            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            PortletContext portletContext = (PortletContext) externalContext.getContext();
            String dir = portletContext.getRealPath("/resources/reports/");
            File file = File.createTempFile(String.valueOf(selectedReportListItem.getId()) + "_", ".pdf", new File(dir));

            if (!file.exists()) {
                boolean created = file.createNewFile();
                if (!created)
                    throw new Exception("Ошибка при создании pdf-файла");
            }

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(fileWrapper.getBytes());
            outputStream.flush();
            outputStream.close();

            if (isReportOpen)
                reportBean.setPdfFilePath("/frsi-portlet/resources/reports/" + file.getName());
            else
                pdfFilePath = "/frsi-portlet/resources/reports/" + file.getName();

        } catch (Exception e) {
            if (isReportOpen)
                reportBean.setPdfFilePath("");
            else
                pdfFilePath = "";
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
        }

        try {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(selectedReportListItem.getFormCode());
            auditEvent.setNameObject(null);
            auditEvent.setIdKindEvent(11L);
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

    public boolean signDateFilled(ReportListItem item) {
        if (item == null)
            return false;
        Long lastReportHistoryId = sessionBean.getPersistence().getLastReportHistoryIdByReportId(item.getId(), false, null);
        if (lastReportHistoryId == null)
            return false;
        String signedDate = sessionBean.getPersistence().getReportValueByKeyReportHistoryId(item.getFormCode() + "*signature_date::", lastReportHistoryId);
        return signedDate != null && !signedDate.trim().isEmpty();
    }

    /*public void downloadBackup() {
        RefRespondentItem respondent = sessionBean.respondent;
        if (respondent == null) return;
        List<SerializedObjectWrapper> serializedReports = new ArrayList<SerializedObjectWrapper>();
        List<ReportListItem> reportList = sessionBean.getPersistence().getReportListByIdn(respondent.getIdn(), "ru");
        for (ReportListItem reportListItem : reportList) {
            Report report = sessionBean.getPersistence().getReport(reportListItem.getId());
            if (report != null) {
                // Report history
                List<ReportHistory> reportHistoryNoLobs = sessionBean.getPersistence().getReportHistoryByReportNoLobs(report);
                List<ReportHistory> reportHistory = new ArrayList<ReportHistory>();
                for (ReportHistory reportHistoryItemNoLobs : reportHistoryNoLobs) {
                    ReportHistory reportHistoryItem = sessionBean.getPersistence().getReportHistory(reportHistoryItemNoLobs.getId(), true, true);
                    if (reportHistoryItem != null) reportHistory.add(reportHistoryItem);
                }
                report.setReportHistory(reportHistory);
                // Status history
                List<ReportStatus> statusHistory = sessionBean.getPersistence().getReportStatusHistoryByReportId(report.getId(), false);
                report.setReportStatusHistory(statusHistory);

                // Archive
                byte[] bytes = Convert.getSerializedObject(report);
                SerializedObjectWrapper sow = new Se
                rializedObjectWrapper();
                sow.className = Report.class.getName();
                sow.classSimpleName = Report.class.getSimpleName();
                sow.tag = "REPORT_" + respondent.getIdn() + "_" + Convert.dateFormatCompact.format(report.getReportDate()) + "_" + report.getFormCode() + ".ser";
                sow.bytes = bytes;
                serializedReports.add(sow);
            }
        }
        byte[] backupZipContent = applicationBean.createBackupZipContent(serializedReports);
        String fileName = "Backup_" + respondent.getIdn() + "_" + Convert.dateTimeFormatCompact_.format(sessionBean.getIntegration().getNewDateFromBackEndServer()) + ".zip";
        applicationBean.putFileContentToResponseOutputStream(backupZipContent, "application/zip", fileName);
    }*/

    public boolean isSubmitEnabled() {
        if (selectedReports.size() == 0) {
            return false;
        }
        if (!userBean.hasPermission("RESP:FORM:SEND")) {
            return false;
        }
        if (!testUsers.contains(sessionBean.user.getScreenName())) {
            if (selectedReports.size() == 1) {
                ReportListItem selectedReportListItem = selectedReports.get(0);
                if (!sessionBean.getPersistence().isStatusCompatible(selectedReportListItem.getStatus(), ReportStatus.Status.COMPLETED.name())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isBtnEnabled(String permission, List<ReportListItem> reportListItemList) {
        if (reportListItemList.size() == 0) {
            return false;
        }else if (!userBean.hasPermission(permission)) {
            return false;
        }
        return true;
    }

    public boolean haveRaw(List<ReportListItem> reportListItemList){
        if(reportListItemList.size() == 0)
            return false;

        for (ReportListItem item : reportListItemList) {
            if (item.getFormTypeCode().equals(Form.Type.INPUT_RAW.name()))
                return true;
        }
        return false;
    }

    /*public boolean isSaveEnabled() {
        return !approved; // && selectedReportListItem.getStatus().equals(ReportStatus.Status.SIGNED.name());
    }*/

    public boolean isDeleteDisable() {
        if (selectedReports.size() == 0) {
            return true;
        }
        if (!testUsers.contains(sessionBean.user.getScreenName())) {
            if (selectedReports.size() == 1) {
                ReportListItem selectedReportListItem = selectedReports.get(0);
                String status = selectedReportListItem.getStatus();
                if (status.equals(ReportStatus.Status.COMPLETED.name()) || status.equals(ReportStatus.Status.APPROVED.name()) || status.equals(ReportStatus.Status.DISAPPROVED.name())) {
                    return false;
                }
                if (!userBean.hasPermissionForm(selectedReportListItem.getFormCode(), "F:DELETE", selectedReportListItem.getIdn())) {
                    return false;
                }
            }
        }
        return false;
    }

    public void deleteReport() {
        Date delDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
        deleteMessages.clear();
        Iterator<ReportListItem> itemIterator = selectedReports.iterator();
        while (itemIterator.hasNext()) {
            ReportListItem reportListItem = itemIterator.next();
            if (!userBean.hasPermissionForm(reportListItem.getFormCode(), "F:DELETE", reportListItem.getIdn())) {
                deleteMessages.add(new ApproveResultItem(reportListItem.getId(), reportListItem.getFormName(),
                        reportListItem.getRespondentNameRu(), reportListItem.getReportDate(), "Не достаточно прав"));
                continue;
            }
            List<ReportListItem> outputReportList = sessionBean.getPersistence().getOutputReportListByInputReportId(reportListItem.getId(), sessionBean.languageCode);
            if (outputReportList.size() > 0) {
                deleteMessages.add(new ApproveResultItem(reportListItem.getId(), reportListItem.getFormName(),
                        reportListItem.getRespondentNameRu(), reportListItem.getReportDate(), "Используется в выходных/сводных отчетах"));
                continue;
            }
            ReportStatus reportStatus = sessionBean.getPersistence().getLastReportStatusByReportId(reportListItem.getId(), false, null);
            if (reportStatus == null) {
                continue;
            }
            String statusCode = reportStatus.getStatusCode();
            if (statusCode.equals(ReportStatus.Status.COMPLETED.name()) || statusCode.equals(ReportStatus.Status.APPROVED.name()) || statusCode.equals(ReportStatus.Status.DISAPPROVED.name())) {
                deleteMessages.add(new ApproveResultItem(reportListItem.getId(), reportListItem.getFormName(),
                        reportListItem.getRespondentNameRu(), reportListItem.getReportDate(), MessageFormat.format("Статус \"{0}\"", reportStatus.getStatusName("ru"))));
                continue;
            } else if (reportListItem.getIdnChild() != null && !sessionBean.getReference().respondentHaveWarrantByIdn(sessionBean.respondent.getRecId(), delDate, reportListItem.getIdnChild())) {
                deleteMessages.add(new ApproveResultItem(reportListItem.getId(),
                        reportListItem.getFormName(),
                        reportListItem.getRespondentNameRu(),
                        reportListItem.getReportDate(),
                        "Отсутствует доверенность"));
                continue;
            }

            List<ReportHistory> reportHistories = sessionBean.getPersistence().getReportHistoryByReportIdNoLobs(reportListItem.getId(), null);
            if (reportHistories.size() > 1) {
                deleteMessages.add(new ApproveResultItem(reportListItem.getId(), reportListItem.getFormName(),
                        reportListItem.getRespondentNameRu(), reportListItem.getReportDate(), MessageFormat.format("Уже отправленный в НБРК отчет нельзя удалить.", reportStatus.getStatusName("ru"))));
                continue;
            }



            if (sessionBean.getPersistence().getReport(reportListItem.getId(), null) != null) {

                ReportStatus status = new ReportStatus();
                status.setReportHistory(sessionBean.getPersistence().getLastReportHistoryByReportIdNoLobs(reportListItem.getId(), false, null));
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
                auditEvent.setRecId(reportId);
                auditEvent.setUserId(sessionBean.abstractUser.getId());
                auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());

                sessionBean.getPersistence().deleteReportTransactional(reportListItem.getId(), status, auditEvent); // ReportHistory and StatusHistory are cascade-deleted

                deleteMessages.add(new ApproveResultItem(reportListItem.getId(), reportListItem.getFormName(),
                        reportListItem.getRespondentNameRu(), reportListItem.getReportDate(), "", ApproveResultItem.ResultType.SUCCESS));

                reportList.remove(reportListItem);
                itemIterator.remove();
            }
        }

        Collections.sort(deleteMessages, new OperationResultComparator());

        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('wDlgDeleteResults').show()");

    }

    public void performControlAll(boolean forSuperUser, boolean cascade, boolean refreshReportList, boolean extSysControls) {
        userBean.checkAccess("RESP:FORM:CONTROL");

        List<Report> reports = new ArrayList<Report>();
        for (ReportListItem reportListItem : selectedReports) {
            reports.add(sessionBean.getPersistence().getReport(reportListItem.getId(), null));
        }

        controlResults.clear();
        controlResults = sessionBean.getPerformControl().runTaskAll(reports, forSuperUser, cascade, sessionBean.abstractUser.getId(),
                sessionBean.abstractUser.getLocation(), sessionBean.respondent.getId(), 12L, extSysControls);

        if (refreshReportList)
            refreshReportList();
    }

    /*public void performControl() {
        userBean.checkAccess("RESP:FORM:CONTROL");
        controlResults.clear();
        for(ReportListItem selectedReportListItem:selectedReports) {
            doPerformControl(selectedReportListItem, false, false);
        }
        refreshReportList();
    }*/

    /*private void doPerformControl(ReportListItem selectedReportListItem, boolean forSuperUser, boolean cascade) {
        Report report = sessionBean.getPersistence().getReport(selectedReportListItem.getId());
        controlResults.addAll(sessionBean.getPerformControl().runTask(selectedReportListItem.getReportDate(), selectedReportListItem.getFormCode(), report.getIdn(), forSuperUser, sessionBean.respondent.getRefSubjectTypeRecId(), cascade));

        try {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(selectedReportListItem.getFormCode());
            auditEvent.setNameObject(null);
            auditEvent.setIdKindEvent(forSuperUser ? (long) 38 : 12);
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
    }*/

    public void downloadControlResults() {
        if (selectedReports.size() == 1) {
            downloadControlResultsOfSingleReport();
        } else if (selectedReports.size() > 1) {
            downloadControlResultsAll();
        }
    }

    public void downloadControlResultsOfSingleReport() {
        ReportListItem selectedReportListItem = null;
        if (selectedReports.size() == 1) {
            selectedReportListItem = selectedReports.get(0);
        }
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

    public void downloadControlResultsAll() {
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

    public void downloadExtControlResultsAll() {
        byte[] bytes = null;
        String fileName = "";

        try {

            FileWrapper fileWrapper = sessionBean.getPersistence().extControlResultsAllToExcelFile(controlResults, sessionBean.respondent);
            bytes = fileWrapper.getBytes();

            fileName = "control_result_" + sessionBean.respondent.getIdn() + "." + fileWrapper.getFileFormat();
        } catch (Exception e) {
            bytes = null;
            fileName = "";
        } finally {
            applicationBean.putFileContentToResponseOutputStream(bytes, "application/vnd.ms-excel", fileName);
        }
    }

    public void prepareControlResultsPdfFile() {
        if (selectedReports.size() == 1) {
            prepareControlResultsPdfFileOfSingleReport();
        } else if (selectedReports.size() > 1) {
            prepareControlResultsAllPdfFile();
        }
    }

    public void prepareControlResultsPdfFileOfSingleReport() {
        ReportListItem selectedReportListItem = null;
        if (selectedReports.size() == 1) {
            selectedReportListItem = selectedReports.get(0);
        }
        try {
            FileWrapper fileWrapper = sessionBean.getPersistence().generateControlResultsPdfFile(controlResults, selectedReportListItem);

            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            PortletContext portletContext = (PortletContext) externalContext.getContext();
            String dir = portletContext.getRealPath("/resources/reports/");
            File file = File.createTempFile("control_result_" + String.valueOf(selectedReportListItem.getId()) + "_", ".pdf", new File(dir));

            if (!file.exists()) {
                boolean created = file.createNewFile();
                if (!created)
                    throw new Exception("Ошибка при создании pdf-файла");
            }

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
    }

    public void prepareControlResultsAllPdfFile() {
        try {
            FileWrapper fileWrapper = sessionBean.getPersistence().generateControlResultsAllPdfFile(controlResults, sessionBean.respondent);

            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            PortletContext portletContext = (PortletContext) externalContext.getContext();
            String dir = portletContext.getRealPath("/resources/reports/");
            File file = File.createTempFile("control_result_all_", ".pdf", new File(dir));

            if (!file.exists()) {
                boolean created = file.createNewFile();
                if (!created)
                    throw new Exception("Ошибка при создании pdf-файла");
            }

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
    }

    public String getControlResultTypeName(Long controlResultType) {
        ControlResult.ResultType resultType = controlResultTypes.get(controlResultType);
        if (resultType == null)
            return "";
        else
            return resultType.getName(sessionBean.languageCode);
    }

    public void onPollSwitch() {
        FacesMessage msg = new FacesMessage(pollEnabled ? "Автообновление включено" : "Автообновление выключено");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public int getSuccessOperationCount(List<ApproveResultItem> items) {
        int result = 0;
        for (ApproveResultItem item : items) {
            if (item.getResultType() != null && item.getResultType().equals(ApproveResultItem.ResultType.SUCCESS)) {
                result++;
            }
        }
        return result;
    }

    public int getFailOperationCount(List<ApproveResultItem> items) {
        int result = 0;
        for (ApproveResultItem item : items) {
            if (item.getResultType() != null && item.getResultType().equals(ApproveResultItem.ResultType.FAIL)) {
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

    public void onSelectAll(ToggleSelectEvent event) {
        if (event.isSelected()) {
            selectedReports.clear();
            selectedReports.addAll(reportList);
        } else {
            selectedReports.clear();
        }
    }

    public void refreshRespList(Date date, Boolean withParent){
        respList = getRespWarrants(date == null ? sessionBean.getIntegration().getNewDateFromBackEndServer() : date, withParent);
    }

    public List<RefRespondentItem> getRespWarrants(Date reportDate, Boolean withParent){
        return sessionBean.getReference().getRespondentsWithWarrants(sessionBean.respondent.getRecId(), reportDate, withParent);
    }

    public RefRespondentItem getRespItem(Long id, List<RefRespondentItem> respList){
        for (RefRespondentItem item : respList){
            if(item.getId().equals(id))
                return item;
        }
        return sessionBean.respondent;
    }

    public void onUploadFileFromView(FileUploadEvent event){
        long lastReportHistoryId = sessionBean.getPersistence().getLastReportHistoryIdByReportId(getSelectedReportListItem().getId(), false, null);
        List<AttachedFile> reportRawListItem = sessionBean.getPersistence().getFileListByLinkId(lastReportHistoryId, 3, null);

        UploadedFile uploadedFile = event.getFile();
        String fileNameWithFormat = uploadedFile.getFileName();

        if(!reportBean.checkUplFileName(reportRawListItem, fileNameWithFormat)){
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Ошибка", "Файл \"" + fileNameWithFormat + "\" уже существует!"));
            return;
        }

        reportBean.onUploadFile(event);
        refreshReportList();
        FacesMessage msg = new FacesMessage("Файл успешно загружен");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }


    // region Getters and setters

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public void setReportBean(ReportBean reportBean) {
        this.reportBean = reportBean;
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

    public String getFilterFormName() {
        return filterFormName;
    }

    public void setFilterFormName(String filterFormName) {
        this.filterFormName = filterFormName;
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

    public List<ReportListItem> getReportList() {
        return reportList;
    }

    public void setReportList(List<ReportListItem> reportList) {
        this.reportList = reportList;
    }

    public List<ReportListItem> getFilteredReportList() {
        return filteredReportList;
    }

    public void setFilteredReportList(List<ReportListItem> filteredReportList) {
        this.filteredReportList = filteredReportList;
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
        ((LdmReportStatus) ldmReportStatus).setDraftStatusesHidden(draftStatusesHidden);
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

    public int getSendCount() {
        return sendCount;
    }

    public void setSendCount(int sendCount) {
        this.sendCount = sendCount;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List<String> getSubmitResultMessages() {
        return submitResultMessages;
    }

    public Date getNewReportDate() {
        return newReportDate;
    }

    public void setNewReportDate(Date newReportDate) {
        this.newReportDate = newReportDate;
    }

    public List<ControlResultItem> getControlResults() {
        return controlResults;
    }

    public void setControlResults(List<ControlResultItem> controlResults) {
        this.controlResults = controlResults;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
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

    public List<Form> getFilterForms() {
        return filterForms;
    }

    public void setFilterForms(List<Form> filterForms) {
        this.filterForms = filterForms;
    }

    public Map<Long, ControlResult.ResultType> getControlResultTypes() {
        return controlResultTypes;
    }

    public void setControlResultTypes(Map<Long, ControlResult.ResultType> controlResultTypes) {
        this.controlResultTypes = controlResultTypes;
    }

    /*public boolean isUpdateCalculatedFieldsManually() {
        return updateCalculatedFieldsManually;
    }*/

    public void setKiscBean(KiscBean kiscBean) {
        this.kiscBean = kiscBean;
    }

    public String getPdfFilePath() {
        return pdfFilePath;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
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

    public String getOpenResultMessage() {
        return openResultMessage;
    }

    public List<ApproveResultItem> getDeleteMessages() {
        return deleteMessages;
    }

    public List<ApproveResultItem> getSubmitMessages() {
        return submitMessages;
    }

    public List<ReportListItem> getReportsForSign() {
        return reportsForSign;
    }

    public void setReportsForSign(List<ReportListItem> reportsForSign) {
        this.reportsForSign = reportsForSign;
    }

    public List<ApproveResultItem> getSignMessages() {
        return signMessages;
    }

    public void setSignMessages(List<ApproveResultItem> signMessages) {
        this.signMessages = signMessages;
    }

    public List<ApproveResultItem> getCopyMessages() {
        return copyMessages;
    }

    public List<ReportPeriod> getOverdueReports() {
        return overdueReports;
    }

    public List<UserWarrant> getUserWarrants() {
        return userWarrants;
    }

    public UserWarrant getSelectedUserWarrant() {
        return selectedUserWarrant;
    }

    public void setSelectedUserWarrant(UserWarrant selectedUserWarrant) {
        this.selectedUserWarrant = selectedUserWarrant;
    }

    public List<RefRespondentItem> getFilterResp() {
        return filterResp;
    }

    public void setFilterResp(List<RefRespondentItem> filterResp) {
        this.filterResp = filterResp;
    }

    public List<RefRespondentItem> getRespList() {
        return respList;
    }

    public void setRespList(List<RefRespondentItem> respList) {
        this.respList = respList;
    }

    public String getFilterRespText() {
        return filterRespText;
    }

    public void setFilterRespText(String filterRespText) {
        this.filterRespText = filterRespText;
    }

    public boolean isHaveWarrant() {
        return haveWarrant;
    }

    public ReportStatus getSelectedStatus() {
        return selectedStatus;
    }

    public void setSelectedStatus(ReportStatus selectedStatus) {
        this.selectedStatus = selectedStatus;
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
