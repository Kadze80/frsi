package mb;

import com.liferay.portal.model.User;
import entities.Constants;
import entities.PermissionForm;
import entities.PermissionFormIndex;
import entities.PermissionRespForm;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import util.AccessDeniedException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

@ManagedBean
@SessionScoped
public class UserBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    protected static final String NB_BIN = "941240001151";

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    private User user;

    private Set<String> permissionNames;

    // private List<String> notSetAdminPermissions = new ArrayList<String>();

    @PostConstruct
    public void init() {
        Date dateStart = new Date();

        try {
            if (sessionBean.isEjbNull()) sessionBean.init();
            user = applicationBean.getUser();
            permissionNames = new HashSet<String>(sessionBean.getPersistence().getUserPermissionNames(user.getUserId()));
            // refreshAdminPermissions();
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
    }

    /*public void refreshAdminPermissions() {
        if (!user.getScreenName().equalsIgnoreCase(FRSI_ADMIN_NAME))
            return;
        notSetAdminPermissions.clear();
        for (String permissionName : ADMIN_PERMISSIONS) {
            if (!sessionBean.getPersistence().hasAnyUserPermission(permissionName))
                notSetAdminPermissions.add(permissionName);
        }
    }*/

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public boolean hasPermission(String target) {
        String screenName = user.getScreenName();
        if (screenName.equalsIgnoreCase(Constants.FRSI_ADMIN_NAME))
        // if (notSetAdminPermissions.contains(target) && screenName.equalsIgnoreCase(FRSI_ADMIN_NAME))
            return true;
        else
            return permissionNames.contains(target);
    }

    public void checkAccess(String target) {
        if (!hasPermission(target)) {
            /*FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", "Нет доступа"));*/
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", "Нет доступа"));
            throw new AccessDeniedException(target);
        }
    }

    public boolean hasPermissionForm(String formCode, String permissionName, String idn) {
        return sessionBean.getPersistence().hasPermissionRespForm(user.getUserId(), formCode, permissionName, idn);
    }

    public boolean hasOutputPermissionForm(String formCode, String permissionName) {
        return sessionBean.getPersistence().hasOutputPermissionForm(user.getUserId(), formCode, permissionName);
    }

    public void checkFormAccess(String formName, String permissionName, String idn) {
        if (!hasPermissionForm(formName, permissionName, idn)) {
            /*FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", "Нет доступа"));*/
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", "Нет доступа"));
            throw new AccessDeniedException(permissionName);
        }
    }

    public boolean disabled(boolean initialFlag, String permissionName) {
        if (initialFlag)
            return true;
        return !hasPermission(permissionName);
    }

    /**
     * !!!Only for respondents
     * @param initialFlag
     * @param formCode
     * @param permissionName
     * @return
     */
    public boolean disabled(boolean initialFlag, String formCode, String permissionName) {
        return disabled(initialFlag, formCode, permissionName, sessionBean.respondent.getIdn());
    }

    public boolean disabled(boolean initialFlag, String formCode, String permissionName, String idn) {
        if (initialFlag)
            return true;
        return !hasPermissionForm(formCode, permissionName, idn);
    }

    public boolean disabledOutputForm(boolean initialFlag, String formCode, String permissionName) {
        if (initialFlag)
            return true;
        return !hasOutputPermissionForm(formCode, permissionName);
    }
}
