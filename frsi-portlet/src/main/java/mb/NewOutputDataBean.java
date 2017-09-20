package mb;

import com.liferay.portal.model.User;
import entities.*;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;
import util.ExceptionUtil;
import util.Util;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by Nuriddin.Baideuov on 19.05.2015.
 */
@ManagedBean
@ViewScoped
public class NewOutputDataBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{outputDataBean}")
    private OutputDataBean outputDataBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    private Date reportDate;

    private List<RefSubjectTypeItem> subjectTypes;
    private RefSubjectTypeItem selectedSubjectType;
    private Map<Long, Boolean> subjectTypeCheckBoxes = new HashMap<Long, Boolean>();

    private List<RefRespondentItem> respondents;
    private List<RefRespondentItem> selectedRespondents;
    private Map<Long, Set<Long>> unselectedRespondentRecIds = new HashMap<Long, Set<Long>>();

    private List<Form> forms;
    private Form selectedForm;

    private List<RefDepartmentItem> filterDeps = new ArrayList<RefDepartmentItem>();
    private List<RefDepartmentItem> deps;
    private String filterDepsText;

    private Date fromReportDate;
    private Date toReportDate;

    private ReportListItem existingReportListItem;

    private boolean hasGenerateErrors;
    private boolean periodsDisabled;

    private int activeTabIndex;

    Integer kindEvent;

    String currentTabId = "tabInputReports";

    private List<RequiredReport> requiredReports;

    @PostConstruct
    public void init() {
        Date dateStart = new Date();

        try {
            if (sessionBean.isEjbNull()) sessionBean.init();
            reportDate = Util.getFirstDayOfCurrentMonth();
//            refreshData();
        } catch (Exception e) { applicationBean.redirectToErrorPage(e); }

        Date dateEnd = new Date();
        long duration = dateEnd.getTime() - dateStart.getTime();
        logger.debug(MessageFormat.format("t: {1}, d: {2} ms", dateStart, duration));
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

    public void onOpenCreateDialog(){
        currentTabId = "tabInputReports";
        activeTabIndex = 0;
        outputDataBean.setReportNote(null);
        resetFilterDeps();
        refreshData();
    }

    public boolean isGenerateButtonDisabled(){
        if (currentTabId.equals("tabInputReports") && selectedForm == null) {
            return true;
        }
        if (currentTabId.equals("tabReportNote")) {
            return true;
        }
        if(!userBean.hasPermission("SU:OUT:NEW")){
            return true;
        }
        return false;
    }

    public boolean isGenerateDraftButtonDisabled(){
        if (currentTabId.equals("tabInputReports") && selectedForm == null) {
            return true;
        }
        if (currentTabId.equals("tabReportNote")) {
            return true;
        }
        if(!userBean.hasPermission("SU:OUT:NEW:DRAFT")){
            return true;
        }
        return false;
    }

    public void refreshData() {
        subjectTypeCheckBoxes.clear();
        selectedSubjectType = null;
        selectedRespondents = null;
        unselectedRespondentRecIds.clear();
        selectedForm = null;

        updateSubjectTypes();
        respondents = null;
        forms = null;
    }

    private void updateSubjectTypes() {
        if (reportDate != null) {
            subjectTypes = sessionBean.getReference().getRefSubjectTypeListAdvanced(reportDate, false);
        } else {
            subjectTypes = null;
        }
        fromReportDate = toReportDate = reportDate;
        periodsDisabled = isPeriodsDisable1();
    }

    public void updateRespondents() {
        if (reportDate != null && selectedSubjectType != null && subjectTypeCheckBoxes.get(selectedSubjectType.getRecId()))
            respondents = sessionBean.getReference().getUserRespondentsBySubjectType(sessionBean.user.getUserId(), selectedSubjectType.getRecId(), reportDate, filterDeps);
        else
            respondents = new ArrayList<RefRespondentItem>();

        updateRespondentsSelection();
    }

    private void updateRespondentsSelection(){
        selectedRespondents = new ArrayList<RefRespondentItem>();
        if (respondents != null)
            for (RefRespondentItem respondent : respondents) {
                if (!unselected(respondent.getRecId()))
                    selectedRespondents.add(respondent);
            }
    }

    public void onRespondentRowSelect(SelectEvent event) {
        Long respondentId = ((RefRespondentItem) event.getObject()).getRecId();
        removeFromUnselected(respondentId);
    }

    public void onRespondentRowUnselect(UnselectEvent event) {
        Long respondentId = ((RefRespondentItem) event.getObject()).getRecId();
        addToUnselected(respondentId);
    }

    public void onRespondentToggleSelect(ToggleSelectEvent event) {
        if (event.isSelected()) {
            for (RefRespondentItem respondent : respondents)
                removeFromUnselected(respondent.getRecId());
        } else {
            for (RefRespondentItem respondent : respondents)
                addToUnselected(respondent.getRecId());
        }
    }

    public void onSubjectTypeChange(RefSubjectTypeItem subjectType) {
        boolean checked = subjectTypeCheckBoxes.get(subjectType.getRecId());
        if (!checked) {
            if (unselectedRespondentRecIds.containsKey(subjectType.getRecId()))
                unselectedRespondentRecIds.remove(subjectType.getRecId());
        }

        updateForms();
    }

    private boolean unselected(Long respondentRecId) {
        if (selectedSubjectType == null)
            return false;
        return unselected(selectedSubjectType.getRecId(), respondentRecId);
    }

    private boolean unselected(Long subjectTypeRecId, Long respondentRecId) {
        if (!unselectedRespondentRecIds.containsKey(subjectTypeRecId))
            return false;
        return unselectedRespondentRecIds.get(subjectTypeRecId).contains(respondentRecId);
    }

    private void addToUnselected(Long respondentId) {
        addToUnselected(respondentId, selectedSubjectType != null ? selectedSubjectType.getRecId() : null);
    }

    private void addToUnselected(Long respondentId, Long subjectTypeRecId) {
        if (subjectTypeRecId != null) {
            if (!unselectedRespondentRecIds.containsKey(subjectTypeRecId))
                unselectedRespondentRecIds.put(subjectTypeRecId, new HashSet<Long>());
            unselectedRespondentRecIds.get(subjectTypeRecId).add(respondentId);
        }
    }

    private void removeFromUnselected(Long respondentId) {
        if (selectedSubjectType != null) {
            if (unselectedRespondentRecIds.containsKey(selectedSubjectType.getRecId())) {
                Set<Long> ids = unselectedRespondentRecIds.get(selectedSubjectType.getRecId());
                if (ids.contains(respondentId))
                    ids.remove(respondentId);
            }
        }
    }

    private void updateForms() {
        if (reportDate != null) {
            List<Long> subjectTypeRecIds = new ArrayList<Long>();
            for (Map.Entry<Long, Boolean> e : subjectTypeCheckBoxes.entrySet()) {
                if (e.getValue())
                    subjectTypeRecIds.add(e.getKey());
            }
            if (subjectTypeRecIds.size() > 0)
                forms = sessionBean.getPersistence().getOutFormsByUserSubjectTypeRecIds(sessionBean.user.getUserId(), reportDate, subjectTypeRecIds, applicationBean.getLocale().getLanguage());
            else {
                forms = null;
//                selectedForm = null;
            }
        } else {
            forms = null;
//            selectedForm = null;
        }
        fromReportDate = toReportDate = reportDate;
        periodsDisabled = isPeriodsDisable1();
        selectedForm = null;
    }

    public void onSelectForm(){
        periodsDisabled = isPeriodsDisable1();
    }

    private boolean isPeriodsDisable1() {
        return !(selectedForm != null && selectedForm.getFormHistory().getPeriodCount() > 0);
    }

    public void generateReport(boolean isDraft) {
        Form selectedForm;
        List<RefRespondentItem> selectedRespondents;
        Date reportDate;
        selectedForm = this.selectedForm;
        selectedRespondents = getAllSelectedRespondents();
        reportDate = this.reportDate;
        if (isDraft){
            userBean.checkAccess("SU:OUT:NEW:DRAFT");
            kindEvent = 26;
        }
        else{
            userBean.checkAccess("SU:OUT:NEW");
            kindEvent = 25;
        }

        hasGenerateErrors = false;

        Long genReportId = null;
        if (selectedForm == null) {
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Формирование выходной формы", "Для формирования выходной формы выберите шаблон"));
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            hasGenerateErrors = true;
            return;
        }

        if (!isDraft) {
            checkReportExist(reportDate, null, selectedForm);
            if (existingReportListItem != null) {
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('wDlgReportExists').show();");
                return;
            }
        }

        String initStatus = isDraft ? ReportStatus.Status.DRAFT.toString() : ReportStatus.Status.COMPLETED.toString();
        String idn = sessionBean.respondent == null ? null : sessionBean.respondent.getIdn();
        Date curDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
        Date toDate = null, fromDate = null;
        if (selectedForm.getFormHistory().getPeriodCount() > 0) {
            toDate = toReportDate;
            fromDate = fromReportDate;
        }

        EjbResponse<Long> response;
        try {
            response = sessionBean.getOutputReports().generateAndGetId(selectedForm, reportDate, selectedRespondents, sessionBean.portalUser, idn, initStatus, curDate, fromDate, toDate);
        } catch (Exception ex) {
            Throwable t  = ExceptionUtil.getRootCauseRecursive(ex);
            String message = "Неизвестная ошибка";
            if(t.getMessage()!=null) {
                message = t.getMessage().trim();
            }
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Формирование выходной формы", message));
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            hasGenerateErrors = true;
            return;
        }

        if (response.getException() == null) {
            genReportId = response.getPayload();
        } else {
            requiredReports = ((RequiredReportsException) response.getException()).getReports();
            RequestContext context = RequestContext.getCurrentInstance();
            context.addCallbackParam("hasErrors", true);
            context.execute("PF('wDlgRequiredReports').show();");
            hasGenerateErrors = true;
            return;
        }

        ReportListItem item = new ReportListItem();
        item.setId(genReportId);
        User user = sessionBean.user;
        item.setIdn(idn);
        item.setUserInfo(user.getFullName());
        item.setFormCode(selectedForm.getCode());
        item.setFormName(selectedForm.getFormHistory().getName());
        item.setReportDate(reportDate);
        item.setSaveDate(sessionBean.getIntegration().getNewDateFromBackEndServer());
        item.setDeliveryWay("WEB_FORM");
        item.setStatus(initStatus);
        item.setStatusName(ReportStatus.resMap.get(sessionBean.languageCode + "_" + initStatus));
        List<Form> forms = sessionBean.getPersistence().getFormsByCodeLanguageReportDate(item.getFormCode(), "ru", item.getReportDate(), null);
        item.setFormTypeCode(forms.get(0).getTypeCode());
        item.setFormTypeName(forms.get(0).getTypeName("ru"));

        outputDataBean.editReportNote(null, genReportId);

        outputDataBean.getReportList().add(item);
        outputDataBean.setSelectedReportListItem(item);
        outputDataBean.setReportId(item.getId());
        outputDataBean.setApproved(item.getStatus().equals(ReportStatus.Status.APPROVED.name()));
        outputDataBean.setHtml(sessionBean.getPersistence().getHtmlWithOutputReportData(genReportId, true));
        outputDataBean.setDraft(isDraft);
        outputDataBean.setRightApprove(userBean.hasOutputPermissionForm(item.getFormCode(), "F:APPROVE"));
        outputDataBean.setRightDisapprove(userBean.hasOutputPermissionForm(item.getFormCode(), "F:DISAPPROVE"));
        outputDataBean.setRightInputReports(userBean.hasPermission("SU:OUT:INPUT_REPORTS"));
        outputDataBean.setRightEditSign(userBean.hasPermission("SU:OUT:SIGN:EDIT"));
        outputDataBean.setRightDownload(userBean.hasPermission("SU:OUT:DOWNLOAD_EXCEL"));
        outputDataBean.setFixed(false);
        outputDataBean.setCanEditReportNote(!outputDataBean.isEditReportNoteDisable(null,item.getId()));
        /*if (!isDraft)
            outputDataBean.refreshReportList();*/
        outputDataBean.setStatusName(item.getStatusName());

        try {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(selectedForm.getCode());
            auditEvent.setNameObject(null);
            auditEvent.setIdKindEvent((long) kindEvent);
            auditEvent.setDateEvent(curDate);
            auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
            auditEvent.setDateIn(reportDate);
            auditEvent.setRecId(genReportId);
            auditEvent.setUserId(sessionBean.abstractUser.getId());
            auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());
            sessionBean.getPersistence().insertAuditEvent(auditEvent);
        } catch (Exception e) {
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
        }

        if (!isDraft)
            outputDataBean.performControl(outputDataBean.getSelectedReportListItem(), outputDataBean.getSelectedReportListItem().getId(), true);
    }

    public void unselectRespondents() {
        for (RequiredReport r : requiredReports) {
            addToUnselected(r.getRespondentRecId(), r.getSubjectTypeRecId());
        }
        updateRespondentsSelection();
    }

    private RefRespondentItem getNBRefRespondentItem() {
        RefRespondentItem filter = new RefRespondentItem();
        filter.setIdn(userBean.NB_BIN); //БИН Нац. банка
        List<RefRespondentItem> respondentItems = (List<RefRespondentItem>)sessionBean.getReference().getRefAbstractByFilterList(RefRespondentItem.REF_CODE, filter, reportDate);
        if (respondentItems.size() > 0) {
            return respondentItems.get(0);
        } else {
            return null;
        }
    }

    public String generateReportAction() {
        if (reportDate == null)
            return null;
        if(hasGenerateErrors)
            return null;
        return existingReportListItem == null ? "/views/su/outputdata/form?faces-redirect=true" : null;
    }

    public void regenerateReport() {
        List<RefRespondentItem> respondentItems;
        respondentItems = getAllSelectedRespondents();
        if (existingReportListItem != null) {
            if(existingReportListItem.getStatus().equals(ReportStatus.Status.APPROVED.name())){
                RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Формирование выходной формы", "Нельзя переформировать утвержденный отчет"));
                RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
                hasGenerateErrors = true;
                return;
            }
        }
        Date curDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
        List<Form> forms = sessionBean.getPersistence().getFormsByCodeLanguageReportDate(existingReportListItem.getFormCode(), "ru", existingReportListItem.getReportDate(), null);
        if(forms.size()==0){
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Формирование выходной формы",
                    MessageFormat.format("Нету формы с кодом {0} на {1}", existingReportListItem.getFormCode(), existingReportListItem.getReportDate())));
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            hasGenerateErrors = true;
            return;
        }
        Date toDate = null, fromDate = null;
        if (forms.get(0).getFormHistory().getPeriodCount() > 0) {
            toDate = toReportDate;
            fromDate = fromReportDate;
        }
        try {
            sessionBean.getOutputReports().regenerate(existingReportListItem, respondentItems, sessionBean.portalUser, curDate, fromDate, toDate);
        } catch (Exception ex) {
            Throwable t  = ExceptionUtil.getRootCauseRecursive(ex);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Формирование выходной формы", t.getMessage()));
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            hasGenerateErrors = true;
        }

        prepareExistingReport();

        outputDataBean.editReportNote(null,existingReportListItem.getId());

        kindEvent = 27;

        try {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(existingReportListItem.getFormCode());
            auditEvent.setNameObject(null);
            auditEvent.setIdKindEvent((long) kindEvent);
            auditEvent.setDateEvent(curDate);
            auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
            auditEvent.setDateIn(existingReportListItem.getReportDate());
            auditEvent.setRecId(existingReportListItem.getId());
            auditEvent.setUserId(sessionBean.abstractUser.getId());
            auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());
            sessionBean.getPersistence().insertAuditEvent(auditEvent);
        } catch (Exception e) {
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
        }

        outputDataBean.performControl(outputDataBean.getSelectedReportListItem(), outputDataBean.getSelectedReportListItem().getId(), true);
    }

    public String regenerateReportAction() {
        if (reportDate == null)
            return null;
        if(hasGenerateErrors)
            return null;
        return "/views/su/outputdata/form?faces-redirect=true";
    }

    public void checkReportExist(Date newReportDate, ReportListItem selectedReportListItem, Form selectedForm) {
        existingReportListItem = null;

        List<ReportListItem> reportList = null;
        String idn = sessionBean.respondent == null ? null : sessionBean.respondent.getIdn();
        if (idn == null || idn.trim().isEmpty())
            return;
        reportList = selectedReportListItem == null ?
                sessionBean.getPersistence().getReportListByIdnReportDateFormCode(idn, false, newReportDate, selectedForm.getCode(), sessionBean.languageCode) :
                sessionBean.getPersistence().getReportListByIdnReportDateFormCode(idn, false, newReportDate, selectedReportListItem.getFormCode(), sessionBean.languageCode);
        if (!reportList.isEmpty()) existingReportListItem = reportList.get(0);
    }

    public void prepareExistingReport() {
        if (!outputDataBean.getReportList().contains(existingReportListItem))
            outputDataBean.getReportList().add(existingReportListItem);
        outputDataBean.setSelectedReportListItem(existingReportListItem);
        outputDataBean.setHtml(sessionBean.getPersistence().getHtmlWithOutputReportData(existingReportListItem.getId(), true));
        outputDataBean.setReportId(existingReportListItem.getId());
        outputDataBean.setStatusName(existingReportListItem.getStatusName());
        outputDataBean.setApproved(existingReportListItem.getStatus().equals(ReportStatus.Status.APPROVED.name()));
        outputDataBean.setRightApprove(userBean.hasOutputPermissionForm(existingReportListItem.getFormCode(), "F:APPROVE"));
        outputDataBean.setRightDisapprove(userBean.hasOutputPermissionForm(existingReportListItem.getFormCode(), "F:DISAPPROVE"));
        outputDataBean.setRightInputReports(userBean.hasPermission("SU:OUT:INPUT_REPORTS"));
        outputDataBean.setRightEditSign(userBean.hasPermission("SU:OUT:SIGN:EDIT"));
        outputDataBean.setRightDownload(userBean.hasPermission("SU:OUT:DOWNLOAD_EXCEL"));
        outputDataBean.setFixed(false);
        outputDataBean.setHaveReportNote(existingReportListItem.getHaveNote());
        outputDataBean.setCanEditReportNote(!outputDataBean.isEditReportNoteDisable(null,existingReportListItem.getId()));
    }

    public void onFilterDepsHide(){
        updateFilterDepsText();
        updateRespondents();
    }

    private void updateFilterDepsText() {
        int size = filterDeps.size();
        if (size == 0) filterDepsText = "Нет ни одного филиала!";
        else if (size == 1) filterDepsText = filterDeps.get(0).getNameRu();
        else if (size == deps.size()) filterDepsText = "Все";
        else filterDepsText = "Несколько филиалов (" + size + ")";
    }

    public void resetFilterDeps(){
        deps = (List<RefDepartmentItem>)sessionBean.getReference().getRefAbstractByFilterList(RefDepartmentItem.REF_CODE, new RefDepartmentItem(2L),sessionBean.getIntegration().getNewDateFromBackEndServer());
        RefDepartmentItem item = new RefDepartmentItem();
        item.setId(0L);
        item.setNameRu("Без подвязки филиала");
        item.setCode("0");
        deps.add(item);
        filterDeps = new ArrayList<RefDepartmentItem>();
        filterDeps.addAll(deps);

        updateFilterDepsText();
    }

    /**
     * Формирует список респондентов по выбранным типам субъектав, учитывая при этом исключенные респонденты
     *
     * @return
     */
    private List<RefRespondentItem> getAllSelectedRespondents() {
        List<RefRespondentItem> result = new ArrayList<RefRespondentItem>();
        for (Map.Entry<Long, Boolean> e : subjectTypeCheckBoxes.entrySet()) {
            if (e.getValue()) {
                Long stRecId = e.getKey();
                List<RefRespondentItem> respondentList = sessionBean.getReference().getUserRespondentsBySubjectType(sessionBean.user.getUserId(), stRecId, reportDate, filterDeps);
                for (RefRespondentItem r : respondentList) {
                    if (!unselected(stRecId, r.getRecId()))
                        result.add(r);
                }
            }
        }
        return result;
    }

    public void onTabChange(TabChangeEvent event){
        currentTabId = event.getTab().getId();
    }

    // region Getter and Setter

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public List<RefSubjectTypeItem> getSubjectTypes() {
        return subjectTypes;
    }

    public void setSubjectTypes(List<RefSubjectTypeItem> subjectTypes) {
        this.subjectTypes = subjectTypes;
    }

    public List<RefRespondentItem> getRespondents() {
        return respondents;
    }

    public void setRespondents(List<RefRespondentItem> respondents) {
        this.respondents = respondents;
    }

    public List<RefRespondentItem> getSelectedRespondents() {
        return selectedRespondents;
    }

    public void setSelectedRespondents(List<RefRespondentItem> selectedRespondents) {
        this.selectedRespondents = selectedRespondents;
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

    public RefSubjectTypeItem getSelectedSubjectType() {
        return selectedSubjectType;
    }

    public void setSelectedSubjectType(RefSubjectTypeItem selectedSubjectType) {
        this.selectedSubjectType = selectedSubjectType;
    }

    public Map<Long, Boolean> getSubjectTypeCheckBoxes() {
        return subjectTypeCheckBoxes;
    }

    public void setSubjectTypeCheckBoxes(Map<Long, Boolean> subjectTypeCheckBoxes) {
        this.subjectTypeCheckBoxes = subjectTypeCheckBoxes;
    }

    public void setOutputDataBean(OutputDataBean outputDataBean) {
        this.outputDataBean = outputDataBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public Date getFromReportDate() {
        return fromReportDate;
    }

    public void setFromReportDate(Date fromReportDate) {
        this.fromReportDate = fromReportDate;
    }

    public Date getToReportDate() {
        return toReportDate;
    }

    public void setToReportDate(Date toReportDate) {
        this.toReportDate = toReportDate;
    }

    public boolean isPeriodsDisabled() {
        return periodsDisabled;
    }

    public String getCurrentTabId() {
        return currentTabId;
    }

    public int getActiveTabIndex() {
        return activeTabIndex;
    }

    public void setActiveTabIndex(int activeTabIndex) {
        this.activeTabIndex = activeTabIndex;
    }

    public List<RequiredReport> getRequiredReports() {
        return requiredReports;
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

    // endregion
}
