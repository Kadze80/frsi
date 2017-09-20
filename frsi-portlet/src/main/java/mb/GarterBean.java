package mb;

import entities.*;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Ayupov.Bakhtiyar on 23.05.2017.
 */
@ManagedBean
@SessionScoped
public class GarterBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    private Date reportDate;

    private List<RefNpaItem> npaList;
    private RefNpaItem selectedNpa;

    private List<Form> forms;
    private Form selectedForm;

    private List<Form> npaFormsList;
    private Form selectedNpaForm;

    @PostConstruct
    public void init() {
        Date dateStart = new Date();
        try {
            if (sessionBean.isEjbNull()) sessionBean.init();
            reportDate = sessionBean.getIntegration().getNewDateFromBackEndServer();

            npaList = (List<RefNpaItem>)sessionBean.getReference().getRefAbstractList(RefNpaItem.REF_CODE, reportDate);
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

    public void onRowSelect(SelectEvent event) {
        Long npaRecId = ((RefNpaItem) event.getObject()).getRecId();
        if (npaRecId != null) {
            forms = sessionBean.getPersistence().getNpaForms(npaRecId, false);
            npaFormsList = sessionBean.getPersistence().getNpaForms(npaRecId, true);
        }
    }

    public void updateForms() {
        if (selectedNpa != null) {
            forms = sessionBean.getPersistence().getNpaForms(selectedNpa.getRecId(), false);
            npaFormsList = sessionBean.getPersistence().getNpaForms(selectedNpa.getRecId(), true);
        }
    }

    public void addForm() {
        if (selectedNpa != null & selectedForm != null) {
            userBean.checkAccess("NPA_FORMS:ADD");

            sessionBean.getPersistence().addNpaForms(selectedNpa.getRecId(), selectedForm.getFormHistory().getId(),
                    prepareAuditEvent(selectedForm.getCode(),"Отчет: " + selectedForm.getFormHistory().getName() + ", НПА: " + selectedNpa.getNameRu(), 135L));
            updateForms();
            selectedForm = null;
        }
    }

    public void delForm() {
        if (selectedNpa != null) {
            userBean.checkAccess("NPA_FORMS:DELETE");

            sessionBean.getPersistence().delNpaForms(selectedNpa.getRecId(), selectedForm.getFormHistory().getId(),
                    prepareAuditEvent(selectedNpaForm.getCode(),"Отчет: " + selectedNpaForm.getFormHistory().getName() + ", НПА: " + selectedNpa.getNameRu(), 136L));
            updateForms();
            selectedNpaForm = null;
        }
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
        auditEvent.setRecId(selectedNpa.getRecId());
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

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public List<RefNpaItem> getNpaList() {
        return npaList;
    }

    public void setNpaList(List<RefNpaItem> npaList) {
        this.npaList = npaList;
    }

    public RefNpaItem getSelectedNpa() {
        return selectedNpa;
    }

    public void setSelectedNpa(RefNpaItem selectedNpa) {
        this.selectedNpa = selectedNpa;
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

    public List<Form> getNpaFormsList() {
        return npaFormsList;
    }

    public void setNpaFormsList(List<Form> npaFormsList) {
        this.npaFormsList = npaFormsList;
    }

    public Form getSelectedNpaForm() {
        return selectedNpaForm;
    }

    public void setSelectedNpaForm(Form selectedNpaForm) {
        this.selectedNpaForm = selectedNpaForm;
    }

    // endregion

}
