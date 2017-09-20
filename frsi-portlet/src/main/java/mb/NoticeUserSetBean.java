package mb;

import entities.NoticeSettings;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ToggleSelectEvent;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Ayupov.Bakhtiyar on 27.02.2017.
 */
@ManagedBean
@SessionScoped
public class NoticeUserSetBean {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    private List<NoticeSettings> settingsList;
    private NoticeSettings selectedSettings;
    private Long userId;
    private boolean btnSaveDisable;


    @PostConstruct
    public void init() {
        try {
            if (sessionBean.isEjbNull()) {
                sessionBean.init();
            }

            btnSaveDisable = false;
            userId = sessionBean.user.getUserId();
            refreshSettings();

        } catch (Exception e) {
            applicationBean.redirectToErrorPage(e);
        }
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
        /*RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('statusDialog').show()");*/
    }

    public void refreshSettings(){
        settingsList = sessionBean.getPersistence().getNoticeForUser(userId);
        Iterator<NoticeSettings> it = settingsList.iterator();
			while (it.hasNext()) {
				NoticeSettings n = it.next();
				if (n.getId() == 117) {
					it.remove(); // Не показываем здесь. Он виден в настройках пользователя
					break;
				}
			}
        btnSaveDisable = true;
    }

    public void saveSettings(){
        sessionBean.getPersistence().insertNoticeUserOff(settingsList, userId, sessionBean.user.getLoginIP(), null);
        refreshSettings();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Выполнено", "Настройка уведомления сохранена успешно"));
    }

    public void onChangeFlag(){
        btnSaveDisable = userBean.disabled(settingsList.size() == 0, "NOTICE:USER_SETTING:SAVE");
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

    public List<NoticeSettings> getSettingsList() { return settingsList; }

    public void setSettingsList(List<NoticeSettings> settingsList) {
        this.settingsList = settingsList;
    }

    public NoticeSettings getSelectedSettings() {
        return selectedSettings;
    }

    public void setSelectedSettings(NoticeSettings selectedSettings) {
        this.selectedSettings = selectedSettings;
    }

    public boolean isBtnSaveDisable() {
        return btnSaveDisable;
    }

    public void setBtnSaveDisable(boolean btnSaveDisable) {
        this.btnSaveDisable = btnSaveDisable;
    }

    // endregion
}
