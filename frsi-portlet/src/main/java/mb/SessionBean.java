package mb;

import com.liferay.portal.model.User;
import ejb.*;
import entities.AbstractUser;
import entities.PortalUser;
import entities.PortalUserGroup;
import entities.RefRespondentItem;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Managed bean
 *
 * @author Ardak Saduakassov
 */
@ManagedBean
@SessionScoped
public class SessionBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;

    private CoreRemote core;
    private FormDao formDao;
    private PersistenceRemote persistence;
    private ScheduleRemote schedule;
    private IntegrationRemote integration;
    private PerformControlRemote performControl;
    private OutputReportsRemote outputReports;
    private ReferenceRemote reference;

    public ResourceBundle res;
    public Date sessionStartDate;
    public User user;
    public AbstractUser abstractUser;
    public RefRespondentItem respondent;
    public String languageCode;
    public PortalUserGroup portalUserGroup;
    public PortalUser portalUser;
    private final int POLL_INTERVAL = 15 * 60; // seconds (15 min)
    private int pollCount;
    private boolean pollStop;

    @PostConstruct
    public void init() {
        Date dateStart = new Date();

        try {
            checkConnection(true);
            core = applicationBean.getCoreRemote();
            formDao = applicationBean.getFormDaoRemote();
            persistence = applicationBean.getPersistenceEjb();
            schedule = applicationBean.getScheduleEjb();
            integration = applicationBean.getIntegrationEjb();
            performControl = applicationBean.getPerformControlEjb();
            outputReports = applicationBean.getOutputReportsEjb();
            reference = applicationBean.getReferenceEjb();

            res = applicationBean.getResourceBundle();
            user = applicationBean.getUser();
            languageCode = applicationBean.getLocale().getLanguage();

            abstractUser = new AbstractUser();
            abstractUser.setId(user.getUserId());
            abstractUser.setLogin(user.getScreenName());
            abstractUser.setMail(user.getEmailAddress());
            abstractUser.setFirstName(user.getFirstName());
            abstractUser.setLastName(user.getLastName());
            abstractUser.setDisplayName(user.getFullName());
            abstractUser.setLocation(user.getLastLoginIP());

            pollCount = 0;
            pollStop = false;

            if (isEjbNull()) return;

            /*sessionStartDate = integration.getNewDateFromBackEndServer();
            List<RefRespondentItem> allRespondents = persistence.getRefRespondentList(sessionStartDate);
            portalUser = persistence.getUserByUserId(user.getUserId());
            if (portalUser != null) {
                Long respondentId = portalUser.getRespondentId();
                if (respondentId != null && respondentId != 0) {
                    for (RefRespondentItem respondent : allRespondents) {
                        if (respondent.getRecId() != null && respondent.getRecId().longValue() == respondentId.longValue()) {
                            this.respondent = respondent;
                            break;
                        }
                    }
                }
            }*/
            sessionStartDate = integration.getNewDateFromBackEndServer();
            portalUser = persistence.getUserByUserId(user.getUserId(), null);
            Long respondentId = portalUser.getRespondentId();
            if (respondentId != null && respondentId != 0) {
                RefRespondentItem respondentItem = new RefRespondentItem();
                respondentItem.setRecId(respondentId);
                this.respondent = (RefRespondentItem) reference.getRefAbstractByFilterList(RefRespondentItem.REF_CODE, respondentItem, sessionStartDate).get(0);
            }
            portalUserGroup = persistence.getUserGroupByUser(portalUser.getUserId(), null);
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

    public boolean isNewSession() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        PortletRequest request = (PortletRequest) facesContext.getExternalContext().getRequest();
        PortletSession session = request.getPortletSession(true);
        return session.isNew();
    }

    public void onPoll() {
        pollCount++;
        if (pollCount >= 2 * 60 * 60 / POLL_INTERVAL) { // 2 hours of inactivity
            pollStop = true;
        }
    }

    public void resetPoll() {
        pollCount = 0;
        pollStop = false;
    }

    public boolean isEjbNull() {
        return persistence == null || integration == null || performControl == null || outputReports == null || schedule == null || reference == null;
    }



    public int getPageReloadDuration(){
        return integration.getPageReloadDuration();
    }

    public int getAutosaveDuration(){
        return integration.getAutosaveDuration();
    }

    // Getters and Setters

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public int getPollInterval() {
        return POLL_INTERVAL;
    }

    public int getPollCount() {
        return pollCount;
    }

    public boolean isPollStop() {
        return pollStop;
    }

    public boolean checkConnection(boolean throwException) {
        // TODO надо разобраться, когда свыше 5 запросов подсистема зависает
        /*try {
            URL url = new URL("http://" + applicationBean.getOrbHost() + ":" + applicationBean.getTestPort() + "/frsi-web/test_connection");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.getResponseCode();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            if (throwException)
                throw new ConnectionLostException();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            if(throwException)
                throw new ConnectionLostException();
            return false;
        }*/
        return true;
    }

    public IntegrationRemote getIntegration() {
        checkConnection(true);
        return integration;
    }

    public PersistenceRemote getPersistence() {
        checkConnection(true);
        return persistence;
    }

    public PerformControlRemote getPerformControl() {
        checkConnection(true);
        return performControl;
    }

    public OutputReportsRemote getOutputReports(){
        checkConnection(true);
        return outputReports;
    }

    public ScheduleRemote getSchedule() {
        checkConnection(true);
        return schedule;
    }

    public ReferenceRemote getReference() {
        checkConnection(true);
        return reference;
    }

    public CoreRemote getCore() {
        checkConnection(true);
        return core;
    }

    public FormDao getFormDao() {
        checkConnection(true);
        return formDao;
    }

    public void logout(){
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        try {
            ec.redirect("/c/portal/logout");
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
}