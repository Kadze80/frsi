package mb;

import com.liferay.portal.model.UserGroup;
import entities.*;
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
import java.util.Date;
import java.util.List;

/**
 * Created by Ayupov.Bakhtiyar on 16.02.2017.
 */
@ManagedBean
@SessionScoped
public class NoticeMailBean {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    private List<NoticeMail> mailList;
    private List<NoticeMail> selectedMail;
    private Long userId;
    private String message;
    private String subjectMsg;
    private String userLocation;

    private Long roleId;
    private boolean sendTabVisible;
    private String newMessage;
    private String newSubjectMsg;
    private Long msgNotice = 57L; // Предназначено для отправки простого сообщения
    private List<RefElements> variableList;

    private List<PortalUser> userList;
    private List<PortalUser> selectedUser = new ArrayList<PortalUser>();

    private String filterRoleText;
    private List<Role> roleItems;
    private List<Role> filterRole = new ArrayList<Role>();
    private boolean filterRoleVisible;

    private String filterGroupText;
    private List<PortalUserGroup> groupItems;
    private List<PortalUserGroup> filterGroup = new ArrayList<PortalUserGroup>();
    private boolean filterGroupVisible;

    private String filterSubjectTypesText;
    private List<RefSubjectTypeItem> subjectTypeItems;
    private List<RefSubjectTypeItem> filterSubjectTypes = new ArrayList<RefSubjectTypeItem>();
    private boolean filterSubjectTypeVisible;

    private List<RefRespondentItem> filterRespondents = new ArrayList<RefRespondentItem>();
    private List<RefRespondentItem> respondents;
    private String filterRespondentsText;
    private boolean filterRespondentsVisible;

    private List<Long> depRecIdList = new ArrayList<Long>();


    @PostConstruct
    public void init() {
        try {
            if (sessionBean.isEjbNull())
                sessionBean.init();

            userId = sessionBean.user.getUserId();
            roleId = sessionBean.getPersistence().getRoleByUserId(userId).getId();
            userLocation = sessionBean.user.getLoginIP();
            setVisibleElements();
            refreshNoticeMail();
            if(sendTabVisible)
                refreshUserList();
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

    // region Входные сообщения
    public void refreshNoticeMail(){
        mailList = sessionBean.getPersistence().getNoticeMailList(userId);
        selectedMail = null;
    }

    public void onDlgMessageShow(){
        NoticeMail noticeMail = selectedMail.get(0);
        message = noticeMail.getMessage();
        subjectMsg = noticeMail.getSubjectMsg();

        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject("NOTICE_MAIL");
        auditEvent.setNameObject(noticeMail.getKindEventName());
        auditEvent.setIdKindEvent(126L);
        auditEvent.setDateEvent(date);
        auditEvent.setIdRefRespondent(null);
        auditEvent.setDateIn(date);
        auditEvent.setRecId(noticeMail.getId());
        auditEvent.setUserId(userId);
        auditEvent.setUserLocation(userLocation);

        sessionBean.getPersistence().setNoticeMailHowRead(noticeMail.getId(), 1, auditEvent);
    }

    public void deleteMail(){
        sessionBean.getPersistence().deleteNoticeMail(selectedMail,userId,userLocation);
        refreshNoticeMail();
    }

    public String getMailRowStyle(NoticeMail item) {
        String result = "";

        if(!item.isRead())
            result = "bold";

        return result;
    }

    public void onSelectAllMail(ToggleSelectEvent event) {
        selectedMail.clear();
        if (event.isSelected()) {
            selectedMail.addAll(mailList);
        }
    }
    // endregion

    // region Исходящие сообщения
    public void refreshUserList(){
        List<Long> roleIdList = new ArrayList<Long>();
        for (Role item : filterRole)
            roleIdList.add(item.getId());

        List<Long> groupIdList = new ArrayList<Long>();
        for (PortalUserGroup item : filterGroup)
            groupIdList.add(item.getUserGroupId());

        List<Long> subjectTypeIdList = new ArrayList<Long>();
        for (RefSubjectTypeItem item : filterSubjectTypes)
            subjectTypeIdList.add(item.getRecId());

        List<Long> respondentIdList = new ArrayList<Long>();
        for (RefRespondentItem item : filterRespondents)
            respondentIdList.add(item.getRecId());

        userList = sessionBean.getPersistence().getUsers(roleIdList, groupIdList, subjectTypeIdList, respondentIdList);
        selectedUser = null;
    }

    private void setVisibleElements(){
        getDepRecIdList();
        if(roleId.equals(Constants.FRSI_ADMIN_ROLE) || roleId.equals(Constants.FRSI_ADMIN_DEPARTMENT_ROLE)){ // Администраторы
            sendTabVisible = true;
            filterRoleVisible = true;
            filterGroupVisible = true;
            filterSubjectTypeVisible = false;
            filterRespondentsVisible = false;
            resetFilterRole();
            resetFilterGroup();
        }else if (roleId.equals(Constants.FRSI_USER_NB_MAIN_ROLE) || roleId.equals(Constants.FRSI_USER_NB_DEPARTMENT_ROLE)){ // Пользователи
            sendTabVisible = true;
            filterRoleVisible = false;
            filterGroupVisible = false;
            filterSubjectTypeVisible = true;
            filterRespondentsVisible = true;
            resetFilterSubjectTypes();
            resetFilterRespondents();
        }else if (roleId.equals(Constants.FRSI_RESPONDENT_ROLE)) { // Респонденты
            sendTabVisible = false;
            filterRoleVisible = false;
            filterGroupVisible = false;
            filterSubjectTypeVisible = false;
            filterRespondentsVisible = false;
        }
    }

    private void getDepRecIdList(){
        if(roleId.equals(Constants.FRSI_ADMIN_DEPARTMENT_ROLE))
            depRecIdList = sessionBean.getPersistence().getDepRecIdListByUser(userId);
        else {
            Long depRecId = sessionBean.getPersistence().getDepRecIdByUser(userId);
            if(depRecId != null)
                depRecIdList.add(depRecId);
        }
    }

    public void onSelectAllUsers(ToggleSelectEvent event) {
        selectedUser.clear();
        if (event.isSelected()) {
            selectedUser.addAll(userList);
        }
    }

    // region filters

    public void clearFilters() {
        resetFilterRole();
        resetFilterGroup();
        resetFilterSubjectTypes();
        resetFilterRespondents();
    }

    private void resetFilterRole(){
        if(depRecIdList != null && roleId.equals(Constants.FRSI_ADMIN_DEPARTMENT_ROLE))
            roleItems = sessionBean.getPersistence().getRolesByDepRecId(depRecIdList);
        else
            roleItems = sessionBean.getPersistence().getRoles(null);

        filterRole.clear();
        filterRole.addAll(roleItems);
        updateFilterRoleText();
    }

    public void onFilterRoleHide (){
        updateFilterRoleText();
        resetFilterGroup();
    }

    private void updateFilterRoleText(){
        int size = filterRole.size();
        if (size == 0)
            filterRoleText = "Нет ни одной роли!";
        else if (size == 1)
            filterRoleText = filterRole.get(0).getName();
        else if (size == roleItems.size())
            filterRoleText = "Все";
        else
            filterRoleText = "Несколько ролей (" + size + ")";
    }

    private void resetFilterGroup(){
        List<Long> roleIdList = new ArrayList<Long>();
        for (Role item : filterRole)
            roleIdList.add(item.getId());
        if (roleIdList.size() != 0)
            groupItems = sessionBean.getPersistence().getUserGroupByRoleIdList(roleIdList, depRecIdList);
        else
            groupItems = new ArrayList<PortalUserGroup>();

        filterGroup.clear();
        filterGroup.addAll(groupItems);
        updateFilterGroupText();
    }

    public void onFilterGroupHide(){
        updateFilterGroupText();
    }

    private void updateFilterGroupText(){
        int size = filterGroup.size();
        if (size == 0)
            filterGroupText = "Нет ни одной группы!";
        else if (size == 1)
            filterGroupText = filterGroup.get(0).getName();
        else if (size == groupItems.size())
            filterGroupText = "Все";
        else
            filterGroupText = "Несколько групп(" + size + ")";
    }

    private void resetFilterSubjectTypes() {
        subjectTypeItems = sessionBean.getReference().getSTListByUser(userId);

        filterSubjectTypes.clear();
        filterSubjectTypes.addAll(subjectTypeItems);
        updateFilterSubjectTypesText();
    }

    public void onFilterSubjectTypesHide() {
        updateFilterSubjectTypesText();
        resetFilterRespondents();
    }

    private void updateFilterSubjectTypesText(){
        int size = filterSubjectTypes.size();
        if (size == 0)
            filterSubjectTypesText = "Нет ни одного типа субъекта!";
        else if (size == 1)
            filterSubjectTypesText = filterSubjectTypes.get(0).getName(sessionBean.languageCode);
        else if (size == subjectTypeItems.size())
            filterSubjectTypesText = "Все";
        else
            filterSubjectTypesText = "Несколько типов субъектов (" + size + ")";
    }

    private void resetFilterRespondents() {
        List<Long> stRecIds = new ArrayList<Long>();
        for (RefSubjectTypeItem stItem : filterSubjectTypes)
            stRecIds.add(stItem.getRecId());
        if (stRecIds.size() != 0)
            respondents = sessionBean.getReference().getRespondentsByUserSTRecIdList(userId, stRecIds);
        else
            respondents = new ArrayList<RefRespondentItem>();

        filterRespondents = new ArrayList<RefRespondentItem>();
        filterRespondents.addAll(respondents);

        updateFilterRespondentsText();
    }

    public void onFilterRespondentsHide() {
        updateFilterRespondentsText();
    }

    private void updateFilterRespondentsText() {
        int size = filterRespondents.size();
        if (size == 0)
            filterRespondentsText = "Нет ни одного респондента!";
        else if (size == 1)
            filterRespondentsText = filterRespondents.get(0).getPersonShortName();
        else if (size == respondents.size())
            filterRespondentsText = "Все";
        else
            filterRespondentsText = "Несколько респондентов (" + size + ")";
    }

    // endregion

    public boolean disableBtnWriteMsg(){
        return userBean.disabled(selectedUser == null || selectedUser.size() == 0, "NOTICE:SEND:WRITE");
    }

    public void sendMessage(){
        try{
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if(newMessage == null || newMessage.trim().isEmpty()) {
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Ошибка", "Необходимо ввести текст сообщения!"));
            }
            else if  (newSubjectMsg == null || newSubjectMsg.trim().isEmpty()){
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Ошибка", "Необходимо ввести тему сообщения!"));
            }
            else {
                PortalUser portalUser = sessionBean.getPersistence().getUserByUserId(userId, null);
                sessionBean.getPersistence().insertNoticeMail(selectedUser, portalUser, userLocation, newSubjectMsg, newMessage);

                newMessage = null;
                newSubjectMsg = null;

                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Выполнено", "Сообщение отправлено"));

                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('wDlgNewMessage').hide()");
            }
        }catch (Exception e){
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
        }
    }

    public void onDlgNewMessageShow(){
        newMessage = sessionBean.getPersistence().getNoticeMessageById(msgNotice);
        newSubjectMsg = sessionBean.getPersistence().getNoticeSubjectMessageById(msgNotice);
    }

    public void onDlgVariableShow(){
        refreshVariables();
    }

    public void refreshVariables(){
        variableList = sessionBean.getReference().getRefElements("variable", true);
    }
    // endregion

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

    public List<NoticeMail> getMailList() {
        return mailList;
    }

    public void setMailList(List<NoticeMail> mailList) {
        this.mailList = mailList;
    }

    public List<NoticeMail> getSelectedMail() {
        return selectedMail;
    }

    public void setSelectedMail(List<NoticeMail> selectedMail) {
        this.selectedMail = selectedMail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }

    public String getNewSubjectMsg() {
        return newSubjectMsg;
    }

    public void setNewSubjectMsg(String newSubjectMsg) {
        this.newSubjectMsg = newSubjectMsg;
    }

    public List<PortalUser> getUserList() {
        return userList;
    }

    public void setUserList(List<PortalUser> userList) {
        this.userList = userList;
    }

    public List<PortalUser> getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(List<PortalUser> selectedUser) {
        this.selectedUser = selectedUser;
    }

    public String getFilterRoleText() {
        return filterRoleText;
    }

    public void setFilterRoleText(String filterRoleText) {
        this.filterRoleText = filterRoleText;
    }

    public List<Role> getRoleItems() {
        return roleItems;
    }

    public void setRoleItems(List<Role> roleItems) {
        this.roleItems = roleItems;
    }

    public List<Role> getFilterRole() {
        return filterRole;
    }

    public void setFilterRole(List<Role> filterRole) {
        this.filterRole = filterRole;
    }

    public String getFilterGroupText() {
        return filterGroupText;
    }

    public void setFilterGroupText(String filterGroupText) {
        this.filterGroupText = filterGroupText;
    }

    public List<PortalUserGroup> getGroupItems() {
        return groupItems;
    }

    public void setGroupItems(List<PortalUserGroup> groupItems) {
        this.groupItems = groupItems;
    }

    public List<PortalUserGroup> getFilterGroup() {
        return filterGroup;
    }

    public void setFilterGroup(List<PortalUserGroup> filterGroup) {
        this.filterGroup = filterGroup;
    }

    public String getFilterSubjectTypesText() {
        return filterSubjectTypesText;
    }

    public void setFilterSubjectTypesText(String filterSubjectTypesText) {
        this.filterSubjectTypesText = filterSubjectTypesText;
    }

    public List<RefSubjectTypeItem> getSubjectTypeItems() {
        return subjectTypeItems;
    }

    public void setSubjectTypeItems(List<RefSubjectTypeItem> subjectTypeItems) {
        this.subjectTypeItems = subjectTypeItems;
    }

    public List<RefSubjectTypeItem> getFilterSubjectTypes() {
        return filterSubjectTypes;
    }

    public void setFilterSubjectTypes(List<RefSubjectTypeItem> filterSubjectTypes) {
        this.filterSubjectTypes = filterSubjectTypes;
    }

    public List<RefRespondentItem> getFilterRespondents() {
        return filterRespondents;
    }

    public void setFilterRespondents(List<RefRespondentItem> filterRespondents) {
        this.filterRespondents = filterRespondents;
    }

    public List<RefRespondentItem> getRespondents() {
        return respondents;
    }

    public void setRespondents(List<RefRespondentItem> respondents) {
        this.respondents = respondents;
    }

    public String getFilterRespondentsText() {
        return filterRespondentsText;
    }

    public void setFilterRespondentsText(String filterRespondentsText) {
        this.filterRespondentsText = filterRespondentsText;
    }

    public Long getRoleId() {
        return roleId;
    }

    public boolean isFilterRoleVisible() {
        return filterRoleVisible;
    }

    public boolean isFilterGroupVisible() {
        return filterGroupVisible;
    }

    public boolean isFilterSubjectTypeVisible() {
        return filterSubjectTypeVisible;
    }

    public boolean isFilterRespondentsVisible() {
        return filterRespondentsVisible;
    }

    public boolean isSendTabVisible() {
        return sendTabVisible;
    }

    public String getSubjectMsg() {
        return subjectMsg;
    }

    public void setSubjectMsg(String subjectMsg) {
        this.subjectMsg = subjectMsg;
    }

    public List<RefElements> getVariableList() {
        return variableList;
    }

    public void setVariableList(List<RefElements> variableList) {
        this.variableList = variableList;
    }

    // endregion
}
