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

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by Ayupov.Bakhtiyar on 25.05.2016.
 */
@ManagedBean
@SessionScoped
public class TemplateBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    private List<Template> templateList;
    private Template selectedTemplate;
    private Long typeTemplate;

    @PostConstruct
    public void init() { // preRenderView event listener
        Date dateStart = new Date();

        try {
            if (sessionBean.isEjbNull()) sessionBean.init();
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

    public void refreshList() {
        templateList = sessionBean.getPersistence().getTemplateList(typeTemplate);
    }

    public void onUploadXlsOut(FileUploadEvent event) {
        if (selectedTemplate != null) {
            UploadedFile uploadedFile = event.getFile();
            byte[] contents = uploadedFile.getContents();
            if (contents == null) return;

            String fileName = uploadedFile.getFileName();

            if(!fileName.equals(selectedTemplate.getCodeTemplate() + ".xlsx") && !fileName.equals(selectedTemplate.getCodeTemplate() + ".xlsm")
                    && !fileName.equals(selectedTemplate.getCodeTemplate() + ".pdf") ){
                RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
                RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Ошибка", "Имя файла не совподает!"));
                return;
            }

            selectedTemplate.setXlsOut(contents);
            sessionBean.getPersistence().updateTemplateXlsOut(selectedTemplate);

            refreshList();
            FacesMessage message = new FacesMessage(sessionBean.res.getString("success"), MessageFormat.format(sessionBean.res.getString("uploaded"), uploadedFile.getFileName()));
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void onDownloadXlsOut() {
        if(selectedTemplate!= null){
            Template template = sessionBean.getReference().getTemplateData(selectedTemplate);
            applicationBean.putFileContentToResponseOutputStream(template.getXlsOut(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    template.getCodeTemplate() + (template.getCodeTemplate().equalsIgnoreCase("tml_balance_accounts_xlsm") ? ".xlsm" : ".xlsx"));
        }
    }

    // Getters and Setters

    public ApplicationBean getApplicationBean() {
        return applicationBean;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public UserBean getUserBean() {
        return userBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public List<Template> getTemplateList() {
        return templateList;
    }

    public void setTemplateList(List<Template> templateList) {
        this.templateList = templateList;
    }

    public Template getSelectedTemplate() {
        return selectedTemplate;
    }

    public void setSelectedTemplate(Template selectedTemplate) {
        this.selectedTemplate = selectedTemplate;
    }

    public Long getTypeTemplate() {
        return typeTemplate;
    }

    public void setTypeTemplate(Long typeTemplate) {
        this.typeTemplate = typeTemplate;
    }
}
