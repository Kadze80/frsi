package mb;

import entities.*;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import util.OracleException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by Ayupov.Bakhtiyar on 10.02.2017.
 */
@ManagedBean
@SessionScoped
public class NoticeAdmSetBean {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    private List<Notice> noticeList;
    private Notice selectedNotice;
    private String noticeMessage;
    private String noticeSubjectMsg;
    private boolean changed;
    private Long noticeId;
    private Long msgNotice = 57L; // Предназначено для отправки простого сообщения
    private List<RefElements> variableList;

    // Фильтры
    private String filterNameNotice;
    private List<RefElements> filterEventNames = new ArrayList<RefElements>();
    private List<RefElements> eventNames;
    private String filterEventNamesText;

    // Таблицы
    private List<NoticeSettings> subjectTypeList;
    private List<NoticeSettings> roleList;
    private List<NoticeSettings> respondentList;
    private List<NoticeSettings> userList;
    private List<NoticeSettings> groupList;


    @PostConstruct
    public void init() {
        try {
            if (sessionBean.isEjbNull())
                sessionBean.init();

            resetFilterEventNames();
            refreshNotice();

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
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('statusDialog').show()");
    }

    public void refreshNotice(){
        List<Long> EventNameList = new ArrayList<Long>();
        for (RefElements eventName : filterEventNames)
            EventNameList.add(eventName.getId());

        noticeList = sessionBean.getPersistence().getNoticeList(EventNameList, filterNameNotice);

        selectedNotice = null;
    }

    public void refreshTables(){
        if (changed){
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('wDlgConfirmSave').show()");
        }else {
            noticeId = selectedNotice.getId();

            roleList = sessionBean.getPersistence().getNoticeSettings("notice_role", selectedNotice.getId());
            groupList = sessionBean.getPersistence().getNoticeSettings("notice_group", selectedNotice.getId());
            subjectTypeList = sessionBean.getPersistence().getNoticeSettings("notice_subjecttype", selectedNotice.getId());
            respondentList = sessionBean.getPersistence().getNoticeSettings("notice_respondent", selectedNotice.getId());
            userList = sessionBean.getPersistence().getNoticeSettings("notice_user", selectedNotice.getId());
        }
    }

    public void saveSettings(){
        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject("NOTICE_SETTINGS");
        auditEvent.setNameObject(selectedNotice.getName());
        auditEvent.setIdKindEvent(125L);
        auditEvent.setDateEvent(date);
        auditEvent.setIdRefRespondent(null);
        auditEvent.setDateIn(date);
        auditEvent.setRecId(null);
        auditEvent.setUserId(sessionBean.portalUser.getUserId());
        auditEvent.setUserLocation(sessionBean.user.getLoginIP());

        sessionBean.getPersistence().updateNoticeSettings(noticeId, subjectTypeList, roleList, respondentList, userList, groupList, auditEvent);

        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Выполнено", "Настройка уведомления сохранена успешно"));
        changed = false;
    }

    public void clearSettings(){
        changed = true;
        clearNoticeInList(roleList);
        clearNoticeInList(groupList);
        clearNoticeInList(subjectTypeList);
        clearNoticeInList(respondentList);
        clearNoticeInList(userList);
    }

    private List<NoticeSettings> clearNoticeInList(List<NoticeSettings> list){
        for(NoticeSettings item : list){
            item.setNotice(null);
        }
        return list;
    }

    public void onConfirmSaveHide(boolean save){
        if(save){
            saveSettings();
        }else{
            changed = false;
        }
        refreshTables();
    }

    public void onDlgMessageShow(){
        noticeMessage = selectedNotice.getMessage();
        noticeSubjectMsg = selectedNotice.getSubjectMsg();
    }

    public void onDlgMessageHide(){
        if(noticeSubjectMsg == null || noticeSubjectMsg.trim().isEmpty()){
            FacesMessage message = new FacesMessage();
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            message.setSummary("Укажите тему сообщения!");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject("NOTICE_TEMP");
        auditEvent.setNameObject(selectedNotice.getName());
        auditEvent.setIdKindEvent(124L);
        auditEvent.setDateEvent(date);
        auditEvent.setIdRefRespondent(null);
        auditEvent.setDateIn(date);
        auditEvent.setRecId(null);
        auditEvent.setUserId(sessionBean.portalUser.getUserId());
        auditEvent.setUserLocation(sessionBean.user.getLoginIP());

        sessionBean.getPersistence().updateNoticeMessage(selectedNotice.getId(), noticeSubjectMsg, noticeMessage, auditEvent);
        refreshNotice();
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('wDlgMessage').hide()");
    }

    public String getNoticeRowStyle(String message) {
        String result = "";

        if(message == null || message.isEmpty())
            result = "font-weight: bold";

        return result;
    }

    private void resetFilterEventNames() {
        eventNames = sessionBean.getReference().getRefElements("AE_NAME_EVENT", false);

        filterEventNames = new ArrayList<RefElements>();
        filterEventNames.addAll(eventNames);

        updateFilterEventNamesText();
    }

    public void clearFilters() {
        resetFilterEventNames();
    }

    public void onFiltersToggle() {
    }

    public void onFilterEventNameShow() {
    }

    public void onFilterEventKindShow() {
    }

    public void onFilterEventNamesHide() {
        updateFilterEventNamesText();
    }

    private void updateFilterEventNamesText() {
        int size = filterEventNames.size();
        if (size == 0) filterEventNamesText = "Нет ни одного аудируемого события!";
        else if (size == 1) filterEventNamesText = filterEventNames.get(0).getName();
        else if (size == eventNames.size()) filterEventNamesText = "Все";
        else filterEventNamesText = "Несколько аудируемых событий (" + size + ")";
    }

    public void refreshVariables(){
        variableList = sessionBean.getReference().getRefElements("variable", true);
    }

    public void onDlgVariableShow(){
        refreshVariables();
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

    public List<RefElements> getFilterEventNames() {
        return filterEventNames;
    }

    public void setFilterEventNames(List<RefElements> filterEventNames) {
        this.filterEventNames = filterEventNames;
    }

    public List<RefElements> getEventNames() {
        return eventNames;
    }

    public void setEventNames(List<RefElements> eventNames) {
        this.eventNames = eventNames;
    }

    public String getFilterEventNamesText() {
        return filterEventNamesText;
    }

    public void setFilterEventNamesText(String filterEventNamesText) {
        this.filterEventNamesText = filterEventNamesText;
    }

    public List<Notice> getNoticeList() {
        return noticeList;
    }

    public void setNoticeList(List<Notice> noticeList) {
        this.noticeList = noticeList;
    }

    public Notice getSelectedNotice() {
        return selectedNotice;
    }

    public void setSelectedNotice(Notice selectedNotice) {
        this.selectedNotice = selectedNotice;
    }

    public String getFilterNameNotice() {
        return filterNameNotice;
    }

    public void setFilterNameNotice(String filterNameNotice) {
        this.filterNameNotice = filterNameNotice;
    }

    public String getNoticeMessage() {
        return noticeMessage;
    }

    public void setNoticeMessage(String noticeMessage) {
        this.noticeMessage = noticeMessage;
    }

    public List<NoticeSettings> getSubjectTypeList() {
        return subjectTypeList;
    }

    public void setSubjectTypeList(List<NoticeSettings> subjectTypeList) {
        this.subjectTypeList = subjectTypeList;
    }

    public List<NoticeSettings> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<NoticeSettings> roleList) {
        this.roleList = roleList;
    }

    public List<NoticeSettings> getRespondentList() {
        return respondentList;
    }

    public void setRespondentList(List<NoticeSettings> respondentList) {
        this.respondentList = respondentList;
    }

    public List<NoticeSettings> getUserList() {
        return userList;
    }

    public void setUserList(List<NoticeSettings> userList) {
        this.userList = userList;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public Long getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(Long noticeId) {
        this.noticeId = noticeId;
    }

    public List<NoticeSettings> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<NoticeSettings> groupList) {
        this.groupList = groupList;
    }

    public Long getMsgNotice() {
        return msgNotice;
    }

    public List<RefElements> getVariableList() {
        return variableList;
    }

    public void setVariableList(List<RefElements> variableList) {
        this.variableList = variableList;
    }

    public String getNoticeSubjectMsg() {
        return noticeSubjectMsg;
    }

    public void setNoticeSubjectMsg(String noticeSubjectMsg) {
        this.noticeSubjectMsg = noticeSubjectMsg;
    }

    // endregion
}
