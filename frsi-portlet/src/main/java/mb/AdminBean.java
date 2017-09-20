package mb;

import entities.*;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Updated by Nuriddin.Baideuov on 22.05.2015.
 */
@ManagedBean
@SessionScoped
public class AdminBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger("fileLogger");

	@ManagedProperty(value = "#{applicationBean}")
	private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    private Date reportDate;

    private List<Form> forms;
    private Form selectedForm;

    private List<SubjectType_Form> subjectType_forms;
    private SubjectType_Form selectedSubjForm;

    private List<RefSubjectTypeItem> subjectTypes;
    private RefSubjectTypeItem selectedSubjectType;
    private List<Period> periods;
    private List<RefPeriodItem> refPeriodItems;

	@PostConstruct
	public void init() {
        Date dateStart = new Date();

        try {
            if (sessionBean.isEjbNull()) sessionBean.init();

            // System.out.println("admin_init");
            reportDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            //String dateInString = "09.11.2014";
            String repDate = sdf.format(reportDate);
            // System.out.println("repDate:" + repDate);
            subjectTypes = sessionBean.getReference().getRefSubjectTypeListAdvanced(reportDate, true);
            //updateForms();

            periods = sessionBean.getPersistence().getPeriods();
            refPeriodItems = (List<RefPeriodItem>)sessionBean.getReference().getRefAbstractList(RefPeriodItem.REF_CODE, sessionBean.getIntegration().getNewDateFromBackEndServer());
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

    public void updateForms() {
        if (selectedSubjectType != null) {
            forms = sessionBean.getPersistence().getFormsNotInSubjForm(selectedSubjectType.getRecId());
            subjectType_forms = sessionBean.getPersistence().getSubjTypeForms(selectedSubjectType.getRecId());
        }
    }


    public void onSubjectTypeFormsChange() {
    }

    public void onRowSelect(SelectEvent event) {
        Long subjectTypeRecId = ((RefSubjectTypeItem) event.getObject()).getRecId();
        if (subjectTypeRecId != null) {
            subjectType_forms = sessionBean.getPersistence().getSubjTypeForms(subjectTypeRecId);
            forms = sessionBean.getPersistence().getFormsNotInSubjForm(subjectTypeRecId);
        }
    }

    public void onRowUnselect(UnselectEvent event) {
        //((SubjectType) event.getObject()).getId();
        //System.out.println("onRowUnselect");
    }

    public void addForm() {
        if (selectedSubjectType != null & selectedForm != null) {
            userBean.checkAccess("ADM_FORMS:ADD");

            sessionBean.getPersistence().addSubjTypeForms(selectedSubjectType.getRecId(), selectedForm.getCode(),
                    prepareAuditEvent(selectedForm.getCode(),"Отчет: " + selectedForm.getFormHistory().getName() + ", ТС: " + selectedSubjectType.getNameRu(), 112L));
            subjectType_forms = sessionBean.getPersistence().getSubjTypeForms(selectedSubjectType.getRecId());
            forms = sessionBean.getPersistence().getFormsNotInSubjForm(selectedSubjectType.getRecId());
            selectedForm = null;
        }
    }

    public void delForm() {
        if (selectedSubjForm != null) {
            userBean.checkAccess("ADM_FORMS:DELETE");

            sessionBean.getPersistence().delSubjTypeForms(selectedSubjForm.getId(),
                    prepareAuditEvent(selectedSubjForm.getFormCode(),"Отчет: " + selectedSubjForm.getFormName() + ", ТС: " + selectedSubjectType.getNameRu(), 113L));
            subjectType_forms = sessionBean.getPersistence().getSubjTypeForms(selectedSubjectType.getRecId());
            forms = sessionBean.getPersistence().getFormsNotInSubjForm(selectedSubjectType.getRecId());
            selectedSubjForm = null;
        }
    }

    public void onDateSelect(SelectEvent event) {
        //SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        reportDate = (Date)event.getObject();
        updateForms();
        /*
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Date Selected", format.format(event.getObject())));
        */
    }

    public void onChangePeriod(SubjectType_Form subjectType_form){
        sessionBean.getPersistence().updateSubjTypeForm(subjectType_form, prepareAuditEvent(subjectType_form.getFormCode(),
                "Отчет: " + subjectType_form.getFormName() + ", ТП: " + getPeriodName(subjectType_form.getPeriodId()), 114L));
    }

    public void onChangeRefPeriod(SubjectType_Form subjectType_form){
        sessionBean.getPersistence().updateSubjTypeForm(subjectType_form, prepareAuditEvent(subjectType_form.getFormCode(),
                "Отчет: " + subjectType_form.getFormName() + ", ПП: " + getRefPeriodName(subjectType_form.getRefPeriodRecId()), 115L));
    }

    private String getPeriodName(Long id){
        return sessionBean.getPersistence().getPeriod(id).getName();
    }

    private String getRefPeriodName(Long id){
        if(id == null)
            return "Выберите значение";
        RefPeriodItem refPeriodItem = new RefPeriodItem();
        refPeriodItem.setId(id);
        List<RefPeriodItem> refPeriodItems = (List<RefPeriodItem>)sessionBean.getReference().getRefAbstractByFilterList(RefPeriodItem.REF_CODE, refPeriodItem, sessionBean.getIntegration().getNewDateFromBackEndServer());
        return refPeriodItems.get(0).getNameRu();
    }

    private AuditEvent prepareAuditEvent(String codeObject, String nameObject, Long kindEvent){
        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject(codeObject);
        auditEvent.setNameObject(nameObject);
        auditEvent.setIdKindEvent(kindEvent);
        auditEvent.setDateEvent(date);
        auditEvent.setIdRefRespondent(null);
        auditEvent.setDateIn(date);
        auditEvent.setRecId(selectedSubjectType.getRecId());
        auditEvent.setUserId(sessionBean.portalUser.getUserId());
        auditEvent.setUserLocation(sessionBean.user.getLoginIP());

        return auditEvent;
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

    public List<Form> getForms() {
        return forms;
    }

    public void setForms(List<Form> forms) {
        this.forms = forms;
    }

    public List<SubjectType_Form> getSubjectType_forms() {
        return subjectType_forms;
    }

    public void setSubjectType_forms(List<SubjectType_Form> subjectType_forms) {
        this.subjectType_forms = subjectType_forms;
    }

    public SubjectType_Form getSelectedSubjForm() {
        return selectedSubjForm;
    }

    public void setSelectedSubjForm(SubjectType_Form selectedSubjForm) {
        this.selectedSubjForm = selectedSubjForm;
    }

    public Form getSelectedForm() {
        return selectedForm;
    }

    public void setSelectedForm(Form selectedForm) {
        this.selectedForm = selectedForm;
    }

    public List<RefSubjectTypeItem> getSubjectTypes() {
        return subjectTypes;
    }

    public void setSubjectTypes(List<RefSubjectTypeItem> subjectTypes) {
        this.subjectTypes = subjectTypes;
    }

    public RefSubjectTypeItem getSelectedSubjectType() {
        return selectedSubjectType;
    }

    public void setSelectedSubjectType(RefSubjectTypeItem selectedSubjectType) {
        this.selectedSubjectType = selectedSubjectType;
    }

    public List<Period> getPeriods() {
        return periods;
    }

    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public List<RefPeriodItem> getRefPeriodItems() {
        return refPeriodItems;
    }

    // endregion
}
