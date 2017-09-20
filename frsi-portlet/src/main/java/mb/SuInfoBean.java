package mb;

import entities.*;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;
import util.Convert;
import util.Util;

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
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

@ManagedBean
@SessionScoped
public class SuInfoBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    private Date reportDate;

    private List<RefSubjectTypeItem> subjectTypes;
    private RefSubjectTypeItem selectedSubjectType;
    private Map<Long, Boolean> subjectTypeCheckBoxes = new HashMap<Long, Boolean>();

    private List<RefRespondentItem> respondents;
    private List<RefRespondentItem> selectedRespondents;
    private Set<RefRespondentItem> selectedResp = new HashSet<RefRespondentItem>();
    private Map<Long, Set<Long>> unselectedRespondentRecIds = new HashMap<Long, Set<Long>>();

    private List<Form> forms;
    private List<Form> selectedForm;

    private boolean btnReportsClicked;
    private boolean btnRespondentsClicked;
    private boolean btnSummaryClicked;
    private boolean btnMatrixClicked;

    private Boolean stateSender;

    private String infoName;
    private String pdfFilePath;
    private String typeInfo;

    private List<ReportListItem> reportListItems;
    private List<ReportValueNameListItem> reportValueNameListItems;

    private List<ColumnModel> columns;

	@PostConstruct
	public void init() {
        Date dateStart = new Date();

        try {
            if (sessionBean.isEjbNull()) sessionBean.init();
        } catch (Exception e) { applicationBean.redirectToErrorPage(e); }

        Date dateEnd = new Date();
        long duration = dateEnd.getTime() - dateStart.getTime();
        logger.debug(MessageFormat.format("t: {0}, d: {1} ms", dateStart, duration));

        if(reportDate == null){
            reportDate = Util.getFirstDayOfCurrentMonth();
        }
        refreshData();
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

    private void updateBtns(){
        btnReportsClicked = false;
        btnRespondentsClicked = false;
        btnSummaryClicked = false;
    }

    public String getReports(String kindGroup, boolean summary){
        updateBtns();
        Boolean vStateSender = stateSender;
        if (kindGroup.equals("BY_RESPONDENT")) {
            btnMatrixClicked = false;
            if (summary) {
                vStateSender = null;
                btnSummaryClicked = true;
                typeInfo = "SUMMARY";
                infoName = "Состояние отчетов респондентов - ";
            } else {
                btnReportsClicked = true;
                typeInfo = "REPORTS";
                infoName = "Список отчетов в разрезе респондентов - ";
            }
        } else if (kindGroup.equals("BY_FORM_NAME")) {
            btnMatrixClicked = false;
            if (summary) {
                vStateSender = null;
                btnSummaryClicked = true;
                typeInfo = "SUMMARY";
                infoName = "Состояние отчетов касательно респондентов - ";
            } else {
                btnRespondentsClicked = true;
                typeInfo = "RESPONDENTS";
                infoName = "Список респондентов в разрезе отчетов - ";
            }
        } else if (kindGroup.equals("BY_RESPONDENT_FORMS")) {
            infoName = "Справка в разрезе респондентов и отчетов - ";
            btnMatrixClicked = true;
            typeInfo = "RESPONDENT_FORMS";
        }

        infoName = infoName + " на " + Convert.getDateStringFromDate(reportDate);

        List<Long> subjTypeRecIdList = new ArrayList<Long>();
        for (Map.Entry<Long, Boolean> e : subjectTypeCheckBoxes.entrySet()) {
            if (e.getValue())
                subjTypeRecIdList.add(e.getKey());
        }

        List<Long> respRecIdList = new ArrayList<Long>();
        for (RefRespondentItem respItem : selectedResp) {
            respRecIdList.add(respItem.getRecId());
        }

        List<String> formCodes = new ArrayList<String>();
        for (Form form : selectedForm) {
            formCodes.add(form.getCode());
        }

        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject(typeInfo);
        auditEvent.setNameObject(infoName);
        auditEvent.setIdKindEvent(40L);
        auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
        auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
        auditEvent.setDateIn(sessionBean.getIntegration().getNewDateFromBackEndServer());
        auditEvent.setRecId(null);
        auditEvent.setUserId((long) sessionBean.user.getUserId());
        auditEvent.setUserLocation(sessionBean.user.getLoginIP());

        reportListItems = sessionBean.getPersistence().getAllReportsForInfo(sessionBean.user.getUserId(), subjTypeRecIdList, respRecIdList, formCodes, reportDate, sessionBean.languageCode, vStateSender, auditEvent);

        reportValueNameListItems = new ArrayList<ReportValueNameListItem>();

        SortedSet<String> valueNames = new TreeSet<String>();
        columns = new ArrayList<ColumnModel>();
        for (ReportListItem repItem : reportListItems) {
            if (kindGroup.equals("BY_RESPONDENT")) {
                valueNames.add(repItem.getRespondentNameRu());
            } else if (kindGroup.equals("BY_FORM_NAME")) {
                valueNames.add(repItem.getFormName());
            } else if(kindGroup.equals("BY_RESPONDENT_FORMS")){
                valueNames.add(repItem.getRespondentNameRu());
                ColumnModel column = new ColumnModel(repItem.getFormName(), repItem.getFormCode());
                if (!columns.contains(column)) {
                    columns.add(column);
                }
            }
        }
        if (columns.size() > 0) {
            Collections.sort(columns);
        }

        int sumRownNum = 1;

        for (String valueName : valueNames) {
            ReportValueNameListItem reportValueNameListItem = new ReportValueNameListItem();
            reportValueNameListItem.setValueName(valueName);

            List<ReportListItem> reportsList = new ArrayList<ReportListItem>();
            long cnt = 0;
            long submitCnt = 0;
            int rowNum = 1;
            int overdueReportsCount = 0;

            for (ReportListItem repItem : reportListItems) {
                if (kindGroup.equals("BY_RESPONDENT")) {
                    if (valueName.equals(repItem.getRespondentNameRu())) {
                        if(summary){
                            cnt ++;
                            if(repItem.isSubmitReport()){
                                submitCnt ++;
                            }
                            if (repItem.getOverdueDays() != null && repItem.getOverdueDays() > 0) {
                                overdueReportsCount++;
                            }
                        }else {
                            repItem.setRowNum(rowNum++);
                            reportsList.add(repItem);
                        }
                    }
                } else if (kindGroup.equals("BY_FORM_NAME")) {
                    if (valueName.equals(repItem.getFormName())) {
                        if(summary){
                            cnt ++;
                            if(repItem.isSubmitReport()){
                                submitCnt ++;
                            }
                            if (repItem.getOverdueDays() != null && repItem.getOverdueDays() > 0) {
                                overdueReportsCount++;
                            }
                        } else {
                            repItem.setRowNum(rowNum++);
                            reportsList.add(repItem);
                        }
                    }
                } else if (kindGroup.equals("BY_RESPONDENT_FORMS")) {
                    if (valueName.equals(repItem.getRespondentNameRu())) {
                        reportValueNameListItem.addReportListItem(repItem);
                    }
                }
            }
            if(summary){
                reportValueNameListItem.setCnt(cnt);
                reportValueNameListItem.setSubmitCnt(submitCnt);
                reportValueNameListItem.setNotSubmitCnt(cnt - submitCnt);
                reportValueNameListItem.setRowNum(sumRownNum++);
                reportValueNameListItem.setOverdueReportsCount(overdueReportsCount);
            }else{
                reportValueNameListItem.setReportListItems(reportsList);
            }
            reportValueNameListItems.add(reportValueNameListItem);
        }

        return "/views/su/info/infoView?faces-redirect=true";
    }

    public void downloadExcel(){
        byte[] bytes = null;
        String fileName = "";
        try {
            FileWrapper fileWrapper = sessionBean.getPersistence().suInfoToExcelFile(typeInfo, infoName, reportValueNameListItems, columns);

            bytes = fileWrapper.getBytes();

            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            fileName = Convert.getContentDespositionFilename(infoName + "." + fileWrapper.getFileFormat(), externalContext.getRequestHeaderMap());
        } catch (Exception e) {
            bytes = null;
            fileName = "";
        } finally {
            applicationBean.putFileContentToResponseOutputStream(bytes, "application/vnd.ms-excel", fileName);
        }

        try {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(typeInfo);
            auditEvent.setNameObject(infoName);
            auditEvent.setIdKindEvent(41L);
            auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
            auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
            auditEvent.setDateIn(sessionBean.getIntegration().getNewDateFromBackEndServer());
            auditEvent.setRecId(null);
            auditEvent.setUserId((long) sessionBean.user.getUserId());
            auditEvent.setUserLocation(sessionBean.user.getLoginIP());
            sessionBean.getPersistence().insertAuditEvent(auditEvent);
        } catch (Exception e) {
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
        }
    }

    public void prepareSuInfoPdfFile() {
        try {
            FileWrapper fileWrapper = sessionBean.getPersistence().generateSuInfoPdfFile(typeInfo, infoName, reportValueNameListItems, columns);

            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            PortletContext portletContext = (PortletContext) externalContext.getContext();
            String dir = portletContext.getRealPath("/resources/reports/");
            File file = File.createTempFile("suInfo_" + typeInfo + "_", ".pdf", new File(dir));

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
        try {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(typeInfo);
            auditEvent.setNameObject(infoName);
            auditEvent.setIdKindEvent(42L);
            auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
            auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
            auditEvent.setDateIn(sessionBean.getIntegration().getNewDateFromBackEndServer());
            auditEvent.setRecId(null);
            auditEvent.setUserId((long) sessionBean.user.getUserId());
            auditEvent.setUserLocation(sessionBean.user.getLoginIP());
            sessionBean.getPersistence().insertAuditEvent(auditEvent);
        } catch (Exception e) {
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
        }
    }

    public String getSubmitRowStyle(boolean submitReport) {
        return submitReport ? "color: green;" : "color: red;";
    }

    public String getRowStyleForSts(String statusIn) {
        String result = null;
        ReportStatus.Status status = ReportStatus.Status.ERROR;
        try { status = ReportStatus.Status.valueOf(statusIn); } catch (IllegalArgumentException e) {};
        switch (status) {
            case SIGNED:
                result = "color:blue";
                break;
            case ERROR:
                result = "color:red;";
                break;
            case COMPLETED:
            case DISAPPROVED:
                result = "color:#008080;";
                break;
            case APPROVED:
                result = "color:green";
                break;
        }
        return result;
    }

    public String getControlRowStyle(String controlResult) {
        Long controlResultType = 3L;
        try {
            ControlResult.ResultType rt = ControlResult.ResultType.valueOf(controlResult);
            controlResultType = rt.getId();
        } catch (IllegalArgumentException e) {
        }

        if (controlResultType == null) return "";
        String result = "";
        switch (controlResultType.intValue()) {
            case 1:
                result = "color: green;";
                break;
            case 2:
            case 3:
                result = "color: red;";
                break;
        }
        return result;
    }

    public void refreshData() {
        subjectTypeCheckBoxes.clear();
        selectedSubjectType = null;
        selectedRespondents = null;
        selectedResp.clear();
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
    }

    public void updateRespondents() {
        if (reportDate != null && selectedSubjectType != null && subjectTypeCheckBoxes.get(selectedSubjectType.getRecId()))
            respondents = sessionBean.getReference().getUserRespondentsBySubjectType(sessionBean.user.getUserId(), selectedSubjectType.getRecId(), reportDate, null);
        else
            respondents = new ArrayList<RefRespondentItem>();

        if(selectedRespondents != null)
            selectedResp.addAll(selectedRespondents);

        selectedRespondents = new ArrayList<RefRespondentItem>();
        for (RefRespondentItem respondent : respondents) {
            if (!unselected(respondent.getRecId()))
                selectedRespondents.add(respondent);
        }
        selectedResp.addAll(selectedRespondents);
    }

    private boolean unselected(Long subjectTypeRecId, Long respondentRecId) {
        if (!unselectedRespondentRecIds.containsKey(subjectTypeRecId))
            return false;
        return unselectedRespondentRecIds.get(subjectTypeRecId).contains(respondentRecId);
    }

    private boolean unselected(Long respondentRecId) {
        if (selectedSubjectType == null)
            return false;
        return unselected(selectedSubjectType.getRecId(), respondentRecId);
    }

    public void onSubjectTypeChange(RefSubjectTypeItem subjectType) {
        boolean checked = subjectTypeCheckBoxes.get(subjectType.getRecId());
        List<RefRespondentItem> respList = sessionBean.getReference().getUserRespondentsBySubjectType(sessionBean.user.getUserId(), subjectType.getRecId(), reportDate, null);
        if (!checked) {
            if (unselectedRespondentRecIds.containsKey(subjectType.getRecId()))
                unselectedRespondentRecIds.remove(subjectType.getRecId());
            selectedResp.removeAll(respList);
        }else{
            selectedResp.addAll(respList);
        }
        updateForms();

        if(selectedForm == null)
            selectedForm = new ArrayList<Form>();
        else
            selectedForm.clear();
        selectedForm.addAll(forms);
    }

    private void updateForms() {
        if (reportDate != null) {
            List<Long> subjectTypeRecIds = new ArrayList<Long>();
            for (Map.Entry<Long, Boolean> e : subjectTypeCheckBoxes.entrySet()) {
                if (e.getValue())
                    subjectTypeRecIds.add(e.getKey());
            }
            if (subjectTypeRecIds.size() > 0)
                forms = sessionBean.getPersistence().getFormsByUserIdDateSTList(sessionBean.user.getUserId(), reportDate, subjectTypeRecIds);
            else {
                forms = new ArrayList<Form>();
            }
        } else {
            forms = new ArrayList<Form>();
        }
        selectedForm = null;
    }

    public void onRespondentRowSelect(SelectEvent event) {
        Long respondentId = ((RefRespondentItem) event.getObject()).getRecId();
        removeFromUnselected(respondentId);
        selectedResp.add((RefRespondentItem) event.getObject());
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

    public void onRespondentRowUnselect(UnselectEvent event) {
        Long respondentId = ((RefRespondentItem) event.getObject()).getRecId();
        addToUnselected(respondentId);
        selectedResp.remove((RefRespondentItem) event.getObject());
    }

    public void onRespondentToggleSelect(ToggleSelectEvent event) {
        if (event.isSelected()) {
            for (RefRespondentItem respondent : respondents)
                removeFromUnselected(respondent.getRecId());
            selectedResp.addAll(respondents);
        } else {
            for (RefRespondentItem respondent : respondents)
                addToUnselected(respondent.getRecId());
            selectedResp.removeAll(respondents);
        }
    }

    private void addToUnselected(Long respondentId) {
        if (selectedSubjectType != null) {
            if (!unselectedRespondentRecIds.containsKey(selectedSubjectType.getRecId()))
                unselectedRespondentRecIds.put(selectedSubjectType.getRecId(), new HashSet<Long>());
            unselectedRespondentRecIds.get(selectedSubjectType.getRecId()).add(respondentId);
        }
    }

    public void onSelectFormAll(ToggleSelectEvent event) {
        selectedForm.clear();
        if (event.isSelected()) {
            selectedForm.addAll(forms);
        }
    }

    public int getOverdueDaysForSort(ReportListItem item) {
        if (item == null) {
            return 0;
        } else if (item.getPeriodAlgError() == null) {
            return -1;
        } else {
            return item.getOverdueDays() != null ? item.getOverdueDays() : 0;
        }
    }

    public String getOverdueDays(ReportListItem item) {
        if (item == null) {
            return "";
        } else if (item.getPeriodAlgError() != null) {
            return "Ошибка";
        } else {
            return item.getOverdueDays() != null ? item.getOverdueDays().toString() : "";
        }
    }


    // Getters & Setters

	public void setApplicationBean(ApplicationBean applicationBean) {
		this.applicationBean = applicationBean;
	}

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public boolean isBtnReportsClicked() {
        return btnReportsClicked;
    }

    public void setBtnReportsClicked(boolean btnReportsClicked) {
        this.btnReportsClicked = btnReportsClicked;
    }

    public String getInfoName() {
        return infoName;
    }

    public List<ReportValueNameListItem> getReportValueNameListItems() {
        return reportValueNameListItems;
    }

    public boolean isBtnRespondentsClicked() {
        return btnRespondentsClicked;
    }

    public boolean isBtnSummaryClicked() {
        return btnSummaryClicked;
    }

    public String getPdfFilePath() {
        return pdfFilePath;
    }

    public List<RefSubjectTypeItem> getSubjectTypes() {
        return subjectTypes;
    }

    public void setSubjectTypes(List<RefSubjectTypeItem> subjectTypes) {
        this.subjectTypes = subjectTypes;
    }

    public Map<Long, Boolean> getSubjectTypeCheckBoxes() {
        return subjectTypeCheckBoxes;
    }

    public void setSubjectTypeCheckBoxes(Map<Long, Boolean> subjectTypeCheckBoxes) {
        this.subjectTypeCheckBoxes = subjectTypeCheckBoxes;
    }

    public RefSubjectTypeItem getSelectedSubjectType() {
        return selectedSubjectType;
    }

    public void setSelectedSubjectType(RefSubjectTypeItem selectedSubjectType) {
        this.selectedSubjectType = selectedSubjectType;
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

    public List<Form> getSelectedForm() {
        return selectedForm;
    }

    public void setSelectedForm(List<Form> selectedForm) {
        this.selectedForm = selectedForm;
    }

    public Boolean getStateSender() {
        return stateSender;
    }

    public void setStateSender(Boolean stateSender) {
        this.stateSender = stateSender;
    }

    public List<ColumnModel> getColumns() {
        return columns;
    }

    public boolean isBtnMatrixClicked() {
        return btnMatrixClicked;
    }


}
