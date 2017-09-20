package mb;

import entities.AuditEvent;
import entities.FileWrapper;
import entities.Form;
import entities.RefRespondentItem;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ToggleSelectEvent;
import util.Convert;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Managed bean
 *
 * @author Ardak Saduakassov
 */
@ManagedBean
@SessionScoped
public class DownloadBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;
    @ManagedProperty(value = "#{reportsBean}")
    private ReportsBean reportsBean;

    private Date reportDate;
    private List<Form> forms;
    private List<Form> selectedForms = new ArrayList<Form>();

    private Long selectedRespId;
    private List<RefRespondentItem> respList;

    @PostConstruct
    public void init() {
        Date dateStart = new Date();

        try {
            if (sessionBean.isEjbNull()) sessionBean.init();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
            reportDate = cal.getTime();
            selectedRespId = sessionBean.respondent.getId();
            respList = sessionBean.getReference().getRespondentsWithWarrants(sessionBean.respondent.getRecId(), reportDate, true);
            refreshForms();
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
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('statusDialog').show()");
    }

    public void refreshForms() {
        if(sessionBean.res==null) {
            selectedForms.clear();
            forms = null;
        } else {
            forms = sessionBean.getPersistence().getFormsByUserIdDate(sessionBean.user.getUserId(), reportDate, reportsBean.getRespItem(selectedRespId, respList), false);
        }
    }

    public void downloadFormXls() {
        if (selectedForms.size() == 1) {
            downloadSingleFormXls(selectedForms.get(0));
        } else if (selectedForms.size() > 1) {
            downloadMultipleFormsXls();
        }
    }

    public void downloadSingleFormXls(Form selectedForm) {
        userBean.checkAccess("RESP:DOWNLOAD:EXCEL");

        if (selectedForm != null && reportDate != null) {
            Form form = sessionBean.getPersistence().getFormWithActualXls(selectedForm.getId(), reportDate, true);
            byte[] bytes = null;
            if (form != null && form.getFormHistory().getXls() != null) bytes = form.getFormHistory().getXls();
            DateFormat dfReportDate = new SimpleDateFormat("yyyy-MM-dd");
            String sReportDate = dfReportDate.format(reportDate);
            String fileName = sessionBean.respondent.getIdn() + "_" + selectedForm.getCode() + "_" + sReportDate + ".xlsm";
            applicationBean.putFileContentToResponseOutputStream(bytes, "application/vnd.ms-excel", fileName);

            try {
                AuditEvent auditEvent = new AuditEvent();
                auditEvent.setCodeObject(selectedForm.getCode());
                auditEvent.setNameObject(null);
                auditEvent.setIdKindEvent(14L);
                auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
                auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
                auditEvent.setDateIn(reportDate);
                auditEvent.setRecId(selectedForm.getId());
                auditEvent.setUserId(sessionBean.abstractUser.getId());
                auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());
                sessionBean.getPersistence().insertAuditEvent(auditEvent);
            } catch (Exception e) {
                RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
                RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
            }
        }
    }

    public void downloadMultipleFormsXls(){
        boolean hasError = false;
        if (!userBean.hasPermission("RESP:DOWNLOAD:EXCEL")) {
            hasError = true;
        }
        List<FileWrapper> fileWrappers = new ArrayList<FileWrapper>();
        List<AuditEvent> auditEvents = new ArrayList<AuditEvent>();

        if(!hasError) {
            for (Form selectedForm : selectedForms) {
                try {
                    Form form = sessionBean.getPersistence().getFormWithActualXls(selectedForm.getId(), reportDate, true);
                    byte[] bytes = null;
                    if (form != null && form.getFormHistory().getXls() != null) bytes = form.getFormHistory().getXls();

                    if (bytes == null) continue;

                    DateFormat dfReportDate = new SimpleDateFormat("yyyy-MM-dd");
                    String sReportDate = dfReportDate.format(reportDate);
                    String fileName = sessionBean.respondent.getIdn() + "_" + selectedForm.getCode() + "_" + sReportDate + ".xlsm";
                    FileWrapper fileWrapper = new FileWrapper(bytes, ".xlsm");
                    fileWrapper.setFileName(fileName);
                    fileWrappers.add(fileWrapper);
                } catch (Exception e) {
                    hasError = true;
                    break;
                }

                AuditEvent auditEvent = new AuditEvent();
                auditEvent.setCodeObject(selectedForm.getCode());
                auditEvent.setNameObject(null);
                auditEvent.setIdKindEvent(14L);
                auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
                auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
                auditEvent.setDateIn(reportDate);
                auditEvent.setRecId(selectedForm.getId());
                auditEvent.setUserId(sessionBean.abstractUser.getId());
                auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());
                auditEvents.add(auditEvent);
            }

            byte[] zipContent;
            String fileName;
            if (!hasError) {
                fileName = "forms_" + Convert.dateTimeFormatCompact_.format(sessionBean.getIntegration().getNewDateFromBackEndServer()) + ".zip";
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
    }

    public void downloadFormIdXls(){
        if (selectedForms.size() == 1) {
            downloadSingleFormIdXls(selectedForms.get(0));
        } else if (selectedForms.size() > 1) {
            downloadMultipleFormIdsXls();
        }
    }

    public void downloadSingleFormIdXls(Form selectedForm){
        try {
            userBean.checkAccess("RESP:DOWNLOAD:EXCEL_ID");

            if (selectedForm != null && reportDate != null) {
                Form form = sessionBean.getPersistence().getFormWithActualXls(selectedForm.getId(), reportDate, false);
                byte[] bytes = null;
                if (form != null && form.getFormHistory().getXls() != null) bytes = form.getFormHistory().getXls();

                if (bytes == null) throw new Exception("xls файл не залит в форму");

                DateFormat dfReportDate = new SimpleDateFormat("yyyy-MM-dd");
                String sReportDate = dfReportDate.format(reportDate);
                String fileName = sessionBean.respondent.getIdn() + "_" + selectedForm.getCode() + "_" + sReportDate + ".xlsm";

                byte[] result = sessionBean.getPersistence().replaceExcelData(bytes);

                applicationBean.putFileContentToResponseOutputStream(result, "application/vnd.ms-excel", fileName);

                try {
                    AuditEvent auditEvent = new AuditEvent();
                    auditEvent.setCodeObject(selectedForm.getCode());
                    auditEvent.setNameObject(null);
                    auditEvent.setIdKindEvent(15L);
                    auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
                    auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
                    auditEvent.setDateIn(reportDate);
                    auditEvent.setRecId(selectedForm.getId());
                    auditEvent.setUserId(sessionBean.abstractUser.getId());
                    auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());
                    sessionBean.getPersistence().insertAuditEvent(auditEvent);
                } catch (Exception e) {
                    RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
                    RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            applicationBean.putFileContentToResponseOutputStream(null, "application/vnd.ms-excel", "");

        }
    }

    public void downloadMultipleFormIdsXls(){
        boolean hasError = false;
        if (!userBean.hasPermission("RESP:DOWNLOAD:EXCEL_ID")) {
            hasError = true;
        }
        List<FileWrapper> fileWrappers = new ArrayList<FileWrapper>();
        List<AuditEvent> auditEvents = new ArrayList<AuditEvent>();

        if(!hasError) {
            for (Form selectedForm : selectedForms) {
                try {
                    Form form = sessionBean.getPersistence().getFormWithActualXls(selectedForm.getId(), reportDate, false);
                    byte[] bytes = null;
                    if (form != null && form.getFormHistory().getXls() != null) bytes = form.getFormHistory().getXls();

                    if (bytes == null) continue;

                    DateFormat dfReportDate = new SimpleDateFormat("yyyy-MM-dd");
                    String sReportDate = dfReportDate.format(reportDate);
                    String fileName = sessionBean.respondent.getIdn() + "_" + selectedForm.getCode() + "_" + sReportDate + ".xlsm";

                    byte[] result = sessionBean.getPersistence().replaceExcelData(bytes);

                    if (result == null) continue;

                    FileWrapper fileWrapper = new FileWrapper(result, ".xlsm");
                    fileWrapper.setFileName(fileName);
                    fileWrappers.add(fileWrapper);
                } catch (Exception e){
                    hasError = true;
                    break;
                }

                AuditEvent auditEvent = new AuditEvent();
                auditEvent.setCodeObject(selectedForm.getCode());
                auditEvent.setNameObject(null);
                auditEvent.setIdKindEvent(15L);
                auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
                auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
                auditEvent.setDateIn(reportDate);
                auditEvent.setRecId(selectedForm.getId());
                auditEvent.setUserId(sessionBean.abstractUser.getId());
                auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());
                auditEvents.add(auditEvent);
            }

            byte[] zipContent;
            String fileName;
            if (!hasError) {
                fileName = "formids_" + Convert.dateTimeFormatCompact_.format(sessionBean.getIntegration().getNewDateFromBackEndServer()) + ".zip";
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
    }

    public void onSelectAll(ToggleSelectEvent event) {
        if (event.isSelected()) {
            selectedForms.clear();
            selectedForms.addAll(forms);
        } else {
            selectedForms.clear();
        }
    }

    // region Getter and Setter
    public void setReportsBean(ReportsBean reportsBean) {
        this.reportsBean = reportsBean;
    }

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

    public List<Form> getForms() {
        return forms;
    }

    public void setForms(List<Form> forms) {
        this.forms = forms;
    }

    public Form getSelectedForm() {
        if (selectedForms.size() == 1) {
            return selectedForms.get(0);
        } else {
            return null;
        }
    }

    public void setSelectedForm(Form selectedForm) {
        selectedForms.clear();
        selectedForms.add(selectedForm);
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public List<Form> getSelectedForms() {
        return selectedForms;
    }

    public void setSelectedForms(List<Form> selectedForms) {
        this.selectedForms = selectedForms;
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
