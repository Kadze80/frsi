package mb;

import entities.*;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Ayupov.Bakhtiyar on 25.05.2016.
 */
@ManagedBean
@SessionScoped
public class RefProtocolBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    private List<RefItem> RefList;
    private RefItem selectedRef;
    private RefItem filterRefMainItem = new RefItem();

    private Long userId;
    private Date date;

    @PostConstruct
    public void init() { // preRenderView event listener
        try {
            if (sessionBean.isEjbNull()) sessionBean.init();
            date = sessionBean.getIntegration().getNewDateFromBackEndServer();
            userId = (long) sessionBean.user.getUserId();
            refreshList();
        } catch (Exception e) { applicationBean.redirectToErrorPage(e); }
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
        selectedRef = null;
        filterRefMainItem.setRefKnd(2L);
        RefList = sessionBean.getReference().getRefList(userId, filterRefMainItem);
    }

    public void setTimer(){
        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject("REFERENCE");
        auditEvent.setNameObject("Справочники");
        auditEvent.setIdKindEvent(116L);
        auditEvent.setDateEvent(date);
        auditEvent.setIdRefRespondent(null);
        auditEvent.setDateIn(date);
        auditEvent.setRecId(null);
        auditEvent.setUserId(sessionBean.portalUser.getUserId());
        auditEvent.setUserLocation(sessionBean.user.getLoginIP());

        sessionBean.getSchedule().setTimerService(auditEvent);
        RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_INFO, "Результат", "Время успешно установлено!"));
    }

    public String getRowStyleClass(RefItem item) {
        if(item == null)
            return null;
        String color = item.getStsLoad();
        if(color == null || color.isEmpty()) {
            return null;
        }else {
            if (item.getStsLoad().equals("Загружен успешно!")) {
                return "customRowGreen ";
            } else {
                return "customRowRed ";
            }
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

    public List<RefItem> getRefList() {
        return RefList;
    }

    public void setRefList(List<RefItem> refList) {
        RefList = refList;
    }

    public RefItem getSelectedRef() {
        return selectedRef;
    }

    public void setSelectedRef(RefItem selectedRef) {
        this.selectedRef = selectedRef;
    }

    public RefItem getFilterRefMainItem() {
        return filterRefMainItem;
    }

    public void setFilterRefMainItem(RefItem filterRefMainItem) {
        this.filterRefMainItem = filterRefMainItem;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
