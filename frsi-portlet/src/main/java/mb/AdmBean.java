package mb;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.*;
import com.liferay.portal.service.*;
import entities.*;
import entities.Image;
import entities.Role;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;
import util.Convert;
import util.Validators;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

@ManagedBean
@SessionScoped
public class AdmBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    private String groupPrefix;
    private long companyId;
    private List<RefRespondentItem> respondentList;
    private RefRespondentItem NBRefRespondentItem;
    private String languageCode;

    private List<PortalUserGroup> userGroups;
    private PortalUserGroup selectedUserGroup;

    private Map<Long, String> rolesMap;
    private Map<String, Long> rolesInvariantMap;
    private Map<Long, String> subjectTypesMap;
    private Map<String, Long> subjectTypesInvariantMap;
    private Map<Long, String> departmentsMap;
    private Map<String, Long> departmentsInvariantMap;
    private Map<Long, String> postItemsMap;
    private Map<String, Long> postItemsInvariantMap;
    private Map<Long, String> allRespondentsMap = new HashMap<Long, String>();

    private List<PortalUser> users;
    private PortalUser selectedUser;

    private boolean isGroup;
    private boolean isAdd;
    private PortalUserGroup editingUserGroup;
    private PortalUser editingUser;

    private String deleteUserGroupMessage;

    private PortalUser searchUser;
    private List<PortalUser> foundUsers;
    private PortalUser selectedFoundUser;

    private List<PortalGroup> groups = new ArrayList<PortalGroup>();
    private PortalGroup selectedGroup;

    private List<PortalGroup> foundGroups;
    private PortalGroup selectedFoundGroup;

    private int activeUserGroupTabIndex;

    private List<RefRespondentItem> respondents = new ArrayList<RefRespondentItem>();
    private Long selectedRespondentId;
    private Map<Long, RefRespondentItem> respondentsMap = new HashMap<Long, RefRespondentItem>();
    private Map<Long, RefRespondentItem> editingRespondentsMap = new HashMap<Long, RefRespondentItem>();
    private List<String> designUsers;

    private boolean isShowRefPost;
    private boolean isShowMustSign;
    private boolean isShowDesignUserName;

    private PortalUserGroup currentUsersGroup;
    private List<Long> currentUserRespondentRecIds;

    private List<Role> roles;
    private List<Role> filterRoles = new ArrayList<Role>();
    private String filterRolesText;

    private String filterSubjectTypesText;
    private List<RefSubjectTypeItem> subjectTypeItems;
    private List<RefSubjectTypeItem> filterSubjectTypes = new ArrayList<RefSubjectTypeItem>();

    private String filterDepartmentsText;
    private List<RefDepartmentItem> departmentItems;
    private List<RefDepartmentItem> filterDepartments = new ArrayList<RefDepartmentItem>();

    private String filterGroupName;

    private String filterUser;

    private String filterRespondentsText;
    private List<RefRespondentItem> filterRespondents = new ArrayList<RefRespondentItem>();

    private List<UserWarrant> userWarrants;
    private UserWarrant selectedUserWarrant;
    private UserWarrant editingUserWarrant = new UserWarrant();
    private boolean userWarrantEditMode;
    private List<PortalUser> respondentUsers;
    private boolean showUserWarrantTab;
    private AttachedFile selectedWarrantFile;
    private UserWarrant fileEditingUserWarrant;

    private List<Image> imageList;
    private Image curImage;

    private int activeUserTabIndex;

    private List<RefRespondentItem> groupComboboxRespondents = new ArrayList<RefRespondentItem>(); //used when adding/editing user group

    private boolean editingNaturalPerson;
    private String editingUserIdn;
    private String editingUserPassport;

    @PostConstruct
    public void init() {
        Date dateStart = new Date();

        try {
            if (sessionBean.isEjbNull()) sessionBean.init();

            groupPrefix = sessionBean.getPersistence().getGroupPrefix();
            companyId = getCompanyId();
            languageCode = applicationBean.getLocale().getLanguage();

            currentUsersGroup = sessionBean.getPersistence().getUserGroupByUser(sessionBean.user.getUserId(), null);
            if (currentUsersGroup != null) {
                currentUserRespondentRecIds = sessionBean.getPersistence().getUserRespondents(sessionBean.user.getUserId());
            } else {
                currentUserRespondentRecIds = new ArrayList<Long>();
            }

            roles = sessionBean.getPersistence().getRoles(null);
            resetFilterRoles();

            List<RefSubjectTypeItem> lSubjectTypeItems = sessionBean.getReference().getRefSubjectTypeListAdvanced(sessionBean.getIntegration().getNewDateFromBackEndServer(), true);
            subjectTypeItems = new ArrayList<RefSubjectTypeItem>();
            RefSubjectTypeItem st = new RefSubjectTypeItem();
            st.setId(-1L);
            st.setRecId(-1L);
            st.setNameRu("Без типа субъекта");
            subjectTypeItems.add(st);

            Collections.sort(lSubjectTypeItems, new RefSubjectTypeItemComparator());
            subjectTypeItems.addAll(lSubjectTypeItems);

            resetFilterSubjectTypes();

            List<RefDepartmentItem> lDepartmentItems = (List<RefDepartmentItem>) sessionBean.getReference().getRefAbstractByFilterList(RefDepartmentItem.REF_CODE, new RefDepartmentItem(2L), sessionBean.getIntegration().getNewDateFromBackEndServer());
            departmentItems = new ArrayList<RefDepartmentItem>();
            RefDepartmentItem d = new RefDepartmentItem();
            d.setId(-1L);
            d.setRecId(-1L);
            d.setNameRu("Без филиала");
            departmentItems.add(d);

            Collections.sort(lDepartmentItems, new RefDepartmentItemComparator());
            departmentItems.addAll(lDepartmentItems);

            resetFilterDepartments();


            updateRolesMap();
            updateSubjectTypesMap();
            updateDepartmentsMap();
            updateRefPostsMap();
            updateRespondents();

            designUsers = sessionBean.getPersistence().getAllOracleUsers();

            refresh();
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

    public void onFiltersToggle() {
    }

    public void refresh() {
        synchronize();
        Long userGroupId = selectedUserGroup != null ? selectedUserGroup.getUserGroupId() : null;
        refreshUserGroups(userGroupId);
        refreshUsers();
    }

    public void synchronize() {
        List<PortalUser> users = new ArrayList<PortalUser>();
        List<PortalUserGroup> userGroups = new ArrayList<PortalUserGroup>();
        try {
            for (User user : UserLocalServiceUtil.getUsers(0, UserLocalServiceUtil.getUsersCount())) {
                PortalUser portalUser = new PortalUser();
                portalUser.setUserId(user.getUserId());
                portalUser.setEmailAddress(user.getEmailAddress());
                portalUser.setModifiedDate(user.getModifiedDate());
                portalUser.setFirstName(user.getFirstName());
                portalUser.setLastName(user.getLastName());
                portalUser.setMiddleName(user.getMiddleName());
                portalUser.setScreenName(user.getScreenName());
                portalUser.setBlocked(!user.isActive());

                users.add(portalUser);
            }

            for (UserGroup userGroup : UserGroupLocalServiceUtil.getUserGroups(0, UserGroupLocalServiceUtil.getUserGroupsCount())) {
                if(userGroup.getName().toLowerCase().startsWith(groupPrefix.toLowerCase())) {
                    PortalUserGroup portalUserGroup = new PortalUserGroup();
                    portalUserGroup.setUserGroupId(userGroup.getUserGroupId());
                    portalUserGroup.setName(userGroup.getName());
                    portalUserGroup.setDescription(userGroup.getDescription());
                    for (User user : UserLocalServiceUtil.getUserGroupUsers(userGroup.getUserGroupId())) {
                        portalUserGroup.getUserIds().add(user.getUserId());
                    }
                    userGroups.add(portalUserGroup);
                }
            }
        } catch (SystemException e) {
            throw new EJBException(e);
        }
        sessionBean.getPersistence().synchronizeUsers(users);
        sessionBean.getPersistence().synchronizeUserGroups(userGroups);

        Set<Long> duplicatedUsers = sessionBean.getPersistence().getDuplicatedUserInUserGroups(groupPrefix, null);
        for (Long duplicatedUserId : duplicatedUsers) {
            Set<Long> groupIds = sessionBean.getPersistence().getUserGroupsByUser(duplicatedUserId, null);
            int i = 0;
            int groupIdsCnt = groupIds.size();
            for (Long groupId : groupIds) {
                if (++i < groupIdsCnt) {
                    deletePortalUserFromUserGroup(groupId, duplicatedUserId);
                    sessionBean.getPersistence().deleteUserFromUserGroup(groupId, duplicatedUserId, null, null);
                }
                else
                    break;
            }
        }
    }

    private void refreshUserGroups(Long userGroupId) {
        Long[] roleIds = new Long[filterRoles.size()];
        int index = 0;
        for (Role role : filterRoles) {
            roleIds[index++] = role.getId();
        }

        Long[] subjectTypeIds = new Long[filterSubjectTypes.size()];
        index = 0;
        for(RefSubjectTypeItem st:filterSubjectTypes){
            subjectTypeIds[index++] = st.getRecId();
        }

        Long[] departmentIds = new Long[filterDepartments.size()];
        index = 0;
        for(RefDepartmentItem d:filterDepartments){
            departmentIds[index++] = d.getRecId();
        }

        long userId = sessionBean.portalUser.getUserId();
        if (sessionBean.portalUser.getScreenName().equalsIgnoreCase(Constants.FRSI_ADMIN_NAME)) {
            userId = 0L;
        } else {
            if (currentUsersGroup.getRoleId() != null && currentUsersGroup.getRoleId().equals(Constants.FRSI_ADMIN_ROLE))
                userId = 0L;
        }
        userGroups = sessionBean.getPersistence().getUserGroupsByFilter(userId, roleIds, subjectTypeIds, departmentIds, filterGroupName, filterUser);
        selectedUserGroup = null;
        if (userGroupId != null && userGroups != null)
            for (PortalUserGroup userGroup : userGroups)
                if (userGroup.getUserGroupId().equals(userGroupId)) {
                    selectedUserGroup = userGroup;
                    break;
                }
        updateFilterRespondents(selectedUserGroup, 1);
    }

    public void refreshUsers() {
        if (selectedUserGroup != null) {
            if (selectedRespondentId == null || selectedRespondentId == 0)
                users = sessionBean.getPersistence().getUsersByFilter(selectedUserGroup.getUserGroupId(), null, filterUser);
            else
                users = sessionBean.getPersistence().getUsersByFilter(selectedUserGroup.getUserGroupId(), new Long[]{selectedRespondentId}, filterUser);
        } else
            clearUsers();

        selectedUser = null;
    }

    public void refreshGroups() {
        refreshGroups(0L);
    }

    private void refreshGroups(long groupId) {
        List<Group> groups = getPortalUserGroupGroups(companyId, selectedUserGroup.getName());
        this.groups = new ArrayList<PortalGroup>();
        for (Group group : groups) {
            PortalGroup portalGroup = new PortalGroup(group.getGroupId(), group.getName());
            this.groups.add(portalGroup);
            if (portalGroup.getGroupId() == groupId)
                selectedGroup = portalGroup;
        }
    }

    private void clearUsers() {
        users = null;
        selectedUser = null;
    }

    private void updateRolesMap() {
        List<Role> roleList = sessionBean.getPersistence().getRoles(null);
        rolesMap = new HashMap<Long, String>();
        rolesInvariantMap = new TreeMap<String, Long>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        for (Role item : roleList) {
            rolesMap.put(item.getId(), item.getName());
            rolesInvariantMap.put(item.getName(), item.getId());
        }
    }

    private void updateSubjectTypesMap() {
        List<RefSubjectTypeItem> subjectTypeList = sessionBean.getReference().getRefSubjectTypeListAdvanced(sessionBean.getIntegration().getNewDateFromBackEndServer(), true);
        subjectTypesMap = new HashMap<Long, String>();
        subjectTypesInvariantMap = new TreeMap<String, Long>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        for (RefSubjectTypeItem item : subjectTypeList) {
            subjectTypesMap.put(item.getRecId(), item.getNameRu());
            subjectTypesInvariantMap.put(item.getNameRu(), item.getRecId());
        }
    }

    private void updateDepartmentsMap() {
        List<RefDepartmentItem> departmentList = (List<RefDepartmentItem>) sessionBean.getReference().getRefAbstractByFilterList(RefDepartmentItem.REF_CODE, new RefDepartmentItem(2L), sessionBean.getIntegration().getNewDateFromBackEndServer());
        departmentsMap = new HashMap<Long, String>();
        departmentsInvariantMap = new TreeMap<String, Long>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        for (RefDepartmentItem item : departmentList) {
            departmentsMap.put(item.getRecId(), item.getNameRu());
            departmentsInvariantMap.put(item.getNameRu(), item.getRecId());
        }
    }

    private void updateRefPostsMap() {
        List<RefPostItem> postList = sessionBean.getReference().getRefPosts(null, null);
        postItemsMap = new HashMap<Long, String>();
        postItemsInvariantMap = new TreeMap<String, Long>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        for (RefPostItem item : postList) {
            if (item.getTypePostId().equals(3L) || item.getTypePostId().equals(4L) || item.getTypePostId().equals(5L)) {
                postItemsMap.put(item.getRecId(), item.getNameRu());
                postItemsInvariantMap.put(item.getNameRu(), item.getRecId());
            }
        }
    }

    private void updateRespondents() {
        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
        respondentList = (List<RefRespondentItem>)sessionBean.getReference().getRefAbstractList(RefRespondentItem.REF_CODE, date);
        NBRefRespondentItem = sessionBean.getReference().getRespondentByIdn(userBean.NB_BIN, date);
        allRespondentsMap.clear();
        for (RefRespondentItem r : respondentList) {
            allRespondentsMap.put(r.getRecId(), (r.getShortNameRu() != null && !r.getShortNameRu().trim().isEmpty()) ? r.getShortNameRu() : r.getNameRu());
        }
    }

    public void refreshGroupComboboxRespondents() {
        groupComboboxRespondents.clear();
        boolean found = false;
        for (RefRespondentItem item : respondentList) {
            if ((editingUserGroup.getRefSubjectTypeRecId() == null || editingUserGroup.getRefSubjectTypeRecId() == 0L)
                    || item.getRefSubjectTypeRecId().equals(editingUserGroup.getRefSubjectTypeRecId())) {
                if ((editingUserGroup.getRefDepartmentRecId() == null || editingUserGroup.getRefDepartmentRecId() == 0L)
                        || item.getRefDepartmentRecId().equals(editingUserGroup.getRefDepartmentRecId())) {
                    groupComboboxRespondents.add(item);
                    if (editingUserGroup.getRefRespondentRecId() != null && item.getRecId() == editingUserGroup.getRefRespondentRecId().longValue()) {
                        found = true;
                    }
                }
            }
        }
        if (editingUserGroup.getRefRespondentRecId() != null && !found) {
            editingUserGroup.setRefRespondentRecId(null);
        }
    }

    private void updateFilterRespondents(PortalUserGroup userGroup, int type) {
        if (type == 1) {
            selectedRespondentId = null;
            respondents.clear();
            respondentsMap.clear();
        }
        else
            editingRespondentsMap.clear();
        if (userGroup != null && userGroup.getRoleId() != null) {
            if (!userGroup.getRoleId().equals(Constants.FRSI_RESPONDENT_ROLE) && !userGroup.getRoleId().equals(Constants.FRSI_ADMIN_RESPONDENT_ROLE)) {
                if (type == 1) {
                    respondents.add(NBRefRespondentItem);
                    respondentsMap.put(NBRefRespondentItem.getRecId(), NBRefRespondentItem);
                } else
                    editingRespondentsMap.put(NBRefRespondentItem.getRecId(), NBRefRespondentItem);
            } else {
                for (RefRespondentItem item : respondentList) {
                    if ((userGroup.getRefSubjectTypeRecId() == null || userGroup.getRefSubjectTypeRecId() == 0L)
                            || item.getRefSubjectTypeRecId().equals(userGroup.getRefSubjectTypeRecId())) {

                        if ((userGroup.getRefDepartmentRecId() == null || userGroup.getRefDepartmentRecId() == 0L)
                                || item.getRefDepartmentRecId().equals(userGroup.getRefDepartmentRecId())) {

                            if (userGroup.getRefRespondentRecId() == null || userGroup.getRefRespondentRecId() == 0l || userGroup.getRefRespondentRecId().longValue() == item.getRecId()) {
                                if (type == 1) {
                                    respondents.add(item);
                                    respondentsMap.put(item.getRecId(), item);
                                } else
                                    editingRespondentsMap.put(item.getRecId(), item);
                            }
                        }
                    }
                }
            }
        }

        resetFilterRespondents();
    }

    private boolean checkData(boolean isGroup) {
        boolean result = false;
        if (isGroup) {
            if (editingUserGroup.getName() == null || editingUserGroup.getName().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage("userGroupDialogErr", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", "Введите наименование группы"));
                result = true;
            } else {
                if (!editingUserGroup.getName().startsWith(groupPrefix)) {
                    FacesContext.getCurrentInstance().addMessage("userGroupDialogErr", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", "Наименование группы должно начинаться на " + groupPrefix));
                    result = true;
                }
                if (editingUserGroup.getRoleId() == null || editingUserGroup.getRoleId().equals(0L)) {
                    FacesContext.getCurrentInstance().addMessage("userGroupDialogErr", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", "Роль обязательное поле"));
                    result = true;
                }
                if (isAdd) {
                    UserGroup userGroup = getPortalUserGroup(companyId, editingUserGroup.getName());
                    if (userGroup != null) {
                        FacesContext.getCurrentInstance().addMessage("userGroupDialogErr", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", "Наименование группы уже имеется на Портале"));
                        result = true;
                    }
                }
                else {
                    if (!editingUserGroup.getRefSubjectTypeRecId().equals(selectedUserGroup.getRefSubjectTypeRecId())) {
                        updateFilterRespondents(editingUserGroup, 2);
                        for (PortalUser user : users)
                            if (user.getRespondentId() != 0L && !editingRespondentsMap.containsKey(user.getRespondentId())) {
                                FacesContext.getCurrentInstance().addMessage("userGroupDialogErr", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", "У некторых пользователей организаций подвязаны к другому типу субъекта"));
                                result = true;
                            }
                    }
                }
            }
        }
        else {
            if (editingUser.getScreenName() == null && editingUser.getScreenName().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage("userDialogErr", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", "Экранное имя обязательное поле"));
                result = true;
            }
            if (editingUser.getIdn() != null && !editingUser.getIdn().isEmpty()) {
                if (!Validators.validateIDN(editingUser.getIdn())) {
                    FacesContext.getCurrentInstance().addMessage("userDialogErr", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", "Неверный ИИН"));
                    result = true;
                }
            }
            if (editingUser.isMustSign() && (!editingNaturalPerson && (editingUser.getIdn() == null || editingUser.getIdn().isEmpty()))) {
                FacesContext.getCurrentInstance().addMessage("userDialogErr", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", "ИИН обязателен для подписи ЭЦП"));
                result = true;
            }
            if(sessionBean.getPersistence().checkUniqueDesignUserName(editingUser.getId(),editingUser.getDesignUserName())){
                FacesContext.getCurrentInstance().addMessage("userDialogErr", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", editingUser.getDesignUserName() + " пользователь для дизайнера уже занят, необходимо выбрать другого!"));
                result = true;
            }
            if (editingUser.getRespondentId() == null || editingUser.getRespondentId() == 0L) {
                FacesContext.getCurrentInstance().addMessage("userDialogErr", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", "Организация обязателен для заполнения"));
                result = true;
            }
            if (editingUser.getRespondentId() != null && editingUser.getRespondentId() != 0L && !respondentsMap.containsKey(editingUser.getRespondentId())) {
                FacesContext.getCurrentInstance().addMessage("userDialogErr", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", "У пользователя организация относиться к типу субъекта которого нет в группе"));
                result = true;
            }
        }

        if (result)
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);

        return result;
    }

    public void onUserGroupSelect(SelectEvent event) {
        updateFilterRespondents(selectedUserGroup, 1);
        refreshUsers();
    }

    public String getRespondentNameByUser(PortalUser user) {
        if (user == null)
            return "";
        if (user.getRespondentId() == null || user.getRespondentId() == 0)
            return "";
        if (!respondentsMap.containsKey(user.getRespondentId()))
            return "";
        return respondentsMap.get(user.getRespondentId()).getNameRu();
    }

    public void onSetIsGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    public void onSetUserGroup(boolean isAdd) {
        this.isGroup = true;
        this.isAdd = isAdd;
        activeUserGroupTabIndex = 0;
        editingUserGroup = isAdd ? new PortalUserGroup() : sessionBean.getPersistence().getUserGroupByGroupId(selectedUserGroup.getUserGroupId(), null);
        selectedGroup = new PortalGroup();
        if (!this.isAdd) {
            refreshGroups();
        }
        refreshGroupComboboxRespondents();
    }

    public void onSetUser(boolean isAdd) {
        this.isGroup = false;
        this.isAdd = isAdd;
        editingUser = isAdd ? new PortalUser() : sessionBean.getPersistence().getUserByUserId(selectedUser.getUserId(), null);
        showHideIdnVat();
        isShowRefPost = selectedUserGroup.getRoleId().equals(Constants.FRSI_RESPONDENT_ROLE);
        isShowMustSign = selectedUserGroup.getRoleId().equals(Constants.FRSI_RESPONDENT_ROLE);
        isShowDesignUserName = selectedUserGroup.getRoleId().equals(Constants.FRSI_ADMIN_ROLE) || selectedUserGroup.getRoleId().equals(Constants.FRSI_USER_NB_MAIN_ROLE);

        if (isAdd
                || selectedUser.getRespondentId() == null
                || !selectedUserGroup.getRoleId().equals(Constants.FRSI_RESPONDENT_ROLE)) {
            showUserWarrantTab = false;
        } else {
            showUserWarrantTab = true;
        }
        refreshUserWarrant();
        activeUserGroupTabIndex = 0;
    }

    public void showHideIdnVat() {
        editingNaturalPerson = false;
        editingUserIdn = editingUser.getIdn();
        editingUserPassport = editingUser.getPassport();

        if (selectedUserGroup.getRoleId().equals(Constants.FRSI_RESPONDENT_ROLE)) {
            if (editingUser.getRespondentId() != null) {
                RefRespondentItem resp = respondentsMap.get(editingUser.getRespondentId());
                if (resp != null) {
                    RefUnionPersonItem up = sessionBean.getReference().getUnionPersonItemById(resp.getUnionPersonsId());
                    if (up.getType() == 2) {
                        editingNaturalPerson = true;
                        if(resp.getNonResident()){
                            editingUserPassport = resp.getIdn();
                            editingUserIdn = null;
                        } else {
                            editingUserPassport = null;
                            editingUserIdn = resp.getIdn();
                        }
                    }
                }
            }
        }
    }

    public void onSaveUserGroup() {
        if (isAdd ? addUserGroup() : updateUserGroup()) {
            refreshUserGroups(editingUserGroup.getUserGroupId());
            if (isAdd) clearUsers();
        }
    }

    public void onSaveUser() {
        if (editingNaturalPerson) {
            editingUser.setIdn(null);
            editingUser.setPassport(null);
        } else {
            editingUser.setIdn(editingUserIdn);
            editingUser.setPassport(editingUserPassport);
        }
        if (isNBUserGroup()) {
            RefRespondentItem nbResp = sessionBean.getReference().getRespondentByIdn(Constants.NB_BIN, new Date());
            editingUser.setRespondentId(nbResp.getRecId());
        }
        if (!checkData(false)) {
            boolean isUpdate = updateUser();
            boolean isAddUserToUserGroup = isAdd ? addUserToUserGroup(selectedUserGroup.getUserGroupId(), editingUser.getUserId()) : false;
            if (isUpdate || isAddUserToUserGroup) {
                refreshUsers();
                for (PortalUser user : users)
                    if (user.getUserId().equals(editingUser.getUserId())) {
                        selectedUser = user;
                        break;
                    }
            }
        }
    }

    public void onDelete() {
        if (isGroup) {
            deleteUserGroup(selectedUserGroup.getUserGroupId());
            refreshUserGroups(null);
            clearUsers();
        } else {
            deleteUserFromUserGroup(selectedUserGroup.getUserGroupId(), selectedUser.getUserId());
            refreshUsers();
        }
    }

    public void onDeleteGroup() {
        userBean.checkAccess("PERMIS:GROUP:DELETE");

        deletePortalGroup(selectedGroup.getGroupId(), selectedUserGroup.getUserGroupId());

        selectedGroup.setGroupId(0L);
        refreshGroups(0L);
    }

    public void onSearchedUsers() {
        foundUsers = sessionBean.getPersistence().getUsers(searchUser.getScreenName(), searchUser.getLastName(), searchUser.getFirstName(), searchUser.getMiddleName());
    }

    public void onAddFoundUser() {
        //editingUser = selectedFoundUser;
        editingUser.setId(selectedFoundUser.getId());
        editingUser.setUserId(selectedFoundUser.getUserId());
        editingUser.setScreenName(selectedFoundUser.getScreenName());
        editingUser.setEmailAddress(selectedFoundUser.getEmailAddress());
        editingUser.setFirstName(selectedFoundUser.getFirstName());
        editingUser.setLastName(selectedFoundUser.getLastName());
        editingUser.setMiddleName(selectedFoundUser.getMiddleName());
    }

    private void refreshUserWarrant() {
        if (!showUserWarrantTab) {
            userWarrants = new ArrayList<UserWarrant>();
        } else {
            userWarrants = sessionBean.getPersistence().getUserWarrantsByAttorney(editingUser.getUserId());
        }
    }

    public boolean renderWarrantIcon(UserWarrant warrant) {
        return warrant != null && warrant.getFiles() != null && warrant.getFiles().size() > 0;
    }

    public void onAddUserWarrant(){
        userWarrantEditMode = false;
        editingUserWarrant = new UserWarrant();
        editingUserWarrant.setAttorney(editingUser.getUserId());

        refreshRespondentUsers();
    }

    public void onEditWarrant(){
        userWarrantEditMode = true;
        editingUserWarrant = new UserWarrant();
        editingUserWarrant.setId(selectedUserWarrant.getId());
        editingUserWarrant.setPrincipal(selectedUserWarrant.getPrincipal());
        editingUserWarrant.setAttorney(selectedUserWarrant.getAttorney());
        editingUserWarrant.setCode(selectedUserWarrant.getCode());
        editingUserWarrant.setBeginDate(selectedUserWarrant.getBeginDate());
        editingUserWarrant.setEndDate(selectedUserWarrant.getEndDate());
        editingUserWarrant.setReadonly(selectedUserWarrant.isReadonly());
        editingUserWarrant.setCanceled(selectedUserWarrant.isCanceled());
        editingUserWarrant.setFiles(selectedUserWarrant.getFiles());
        editingUserWarrant.setFilesChanged(selectedUserWarrant.isFilesChanged());

        refreshRespondentUsers();
    }

    private void refreshRespondentUsers(){
        respondentUsers = sessionBean.getPersistence().getRespondentUsersByRespondentId(editingUser.getRespondentId());
        Iterator<PortalUser> iterator = respondentUsers.iterator();
        while (iterator.hasNext()) {
            PortalUser next = iterator.next();
            if (next.getUserId().longValue() == editingUser.getUserId().longValue()) {
                iterator.remove();
                break;
            }
        }
    }

    public void deleteWarrant(){
        userWarrants.remove(selectedUserWarrant);
    }

    public void cancelWarrant(){
        selectedUserWarrant.setCanceled(true);
    }

    public void onSaveUserWarrant() {
        if(checkUserWarrant()){
            return;
        }
        UserWarrant warrant;
        if (!userWarrantEditMode) {
            warrant = new UserWarrant();
            userWarrants.add(warrant);
        } else {
            warrant = selectedUserWarrant;
        }
        if (warrant != null) {
            warrant.setId(editingUserWarrant.getId());
            warrant.setCode(editingUserWarrant.getCode());
            warrant.setPrincipal(editingUserWarrant.getPrincipal());
            warrant.setAttorney(editingUserWarrant.getAttorney());
            warrant.setBeginDate(editingUserWarrant.getBeginDate());
            warrant.setEndDate(editingUserWarrant.getEndDate());
            warrant.setReadonly(editingUserWarrant.isReadonly());
            warrant.setCanceled(editingUserWarrant.isCanceled());
            warrant.setFilesChanged(editingUserWarrant.isFilesChanged());
            warrant.setFiles(editingUserWarrant.getFiles());

            PortalUser pu = sessionBean.getPersistence().getUserByUserId(warrant.getPrincipal(), null);
            warrant.setPrincipalName(pu.getFullName());
        }
    }

    private boolean checkUserWarrant() {
        boolean result = false;
        if (editingUserWarrant.getCode() == null || editingUserWarrant.getCode().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("userWarrantDialogError", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", "Номер договора не указан"));
            result = true;
        }
        if (editingUserWarrant.getPrincipal() == 0) {
            FacesContext.getCurrentInstance().addMessage("userWarrantDialogError", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", "Выберите доверяющего"));
            result = true;
        }
        if (editingUserWarrant.getAttorney() == 0) {
            FacesContext.getCurrentInstance().addMessage("userWarrantDialogError", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", "Выберите доверенного"));
            result = true;
        }
        if (editingUserWarrant.getBeginDate() == null) {
            FacesContext.getCurrentInstance().addMessage("userWarrantDialogError", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка!", "Введите дату начала"));
            result = true;
        }
        if (result)
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
        return result;
    }

    public void onShowWarrantFiles() {
        fileEditingUserWarrant = new UserWarrant();
        fileEditingUserWarrant.setFilesChanged(selectedUserWarrant.isFilesChanged());
        fileEditingUserWarrant.setFiles(new ArrayList<AttachedFile>(selectedUserWarrant.getFiles()));
        fileEditingUserWarrant.setReadonly(selectedUserWarrant.isReadonly());
        fileEditingUserWarrant.setCanceled(selectedUserWarrant.isCanceled());
        selectedWarrantFile = null;
    }

    public void onSaveWarrantFiles(){
        selectedUserWarrant.setFilesChanged(fileEditingUserWarrant.isFilesChanged());
        selectedUserWarrant.setFiles(new ArrayList<AttachedFile>(fileEditingUserWarrant.getFiles()));
    }

    public void onUploadWarrantFile(FileUploadEvent event) {
        UploadedFile uploadedFile = event.getFile();
        byte[] contents = uploadedFile.getContents();
        String fileNameWithFormat = uploadedFile.getFileName();
        String contentType = uploadedFile.getContentType();
        String format = fileNameWithFormat.substring(fileNameWithFormat.lastIndexOf(".") + 1);
        String fileName = fileNameWithFormat.substring(0, fileNameWithFormat.lastIndexOf("."));

        AttachedFile file = new AttachedFile();
        file.setLinkId(fileEditingUserWarrant.getId());
        file.setFileKind(4);
        file.setFile(contents);
        file.setFileName(fileNameWithFormat);
        file.setFileType(contentType);
        file.setIdUsr(sessionBean.user.getUserId());

        if (!format.equalsIgnoreCase("pdf")) {
            FileWrapper pdfFile = new FileWrapper();
            pdfFile.setBytes(contents);
            pdfFile.setFileFormat(format);
            try {
                pdfFile = sessionBean.getPersistence().convertFileToPdf(pdfFile, fileName);
            } catch (Exception e) {
                RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
                RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
                return;
            }
            file.setPdfFile(pdfFile.getBytes());
        }

        fileEditingUserWarrant.getFiles().add(file);
        fileEditingUserWarrant.setFilesChanged(true);
    }

    public void onDownloadWarrantFile() {
        AttachedFile warrantFile;
        if (selectedWarrantFile.getId() == null) {
            warrantFile = selectedWarrantFile;
        } else {
            warrantFile = sessionBean.getPersistence().getDataFile(selectedWarrantFile.getId(), false);
        }
        String fileName = warrantFile.getFileName();
        byte[] file = warrantFile.getFile();

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        fileName = Convert.getContentDespositionFilename(fileName, externalContext.getRequestHeaderMap());

        applicationBean.putFileContentToResponseOutputStream(file, warrantFile.getFileType(), fileName);
    }

    public void deleteWarrantFile(){
        fileEditingUserWarrant.getFiles().remove(selectedWarrantFile);
        fileEditingUserWarrant.setFilesChanged(true);
        selectedWarrantFile = null;
    }

    public void getCurObject(String kind, Image curImageIn, List<Image> imageListIn) {
        int index = curImageIn.getIndex();
        if (kind.equals("NEXT")) {
            index++;
            if (index > imageListIn.size() - 1)
                index = 0;
        } else if (kind.equals("PREV")) {
            index--;
            if (index < 0)
                index = imageListIn.size() - 1;
        } else if (kind.equals("FIRST")) {
            index = 0;
        } else if (kind.equals("LAST")) {
            index = imageListIn.size() - 1;
        }
        curImage = imageListIn.get(index);
    }

    public void prepareGalleria(List<AttachedFile> items, AttachedFile selectedItem){
        List<Image> imageListIn = new ArrayList<Image>();
        Image curImageIn = null;
        try {
            int index = 0;
            int selectIndex = 0;

            for(AttachedFile reportFile : items){
                byte[] pdfFile;
                if (reportFile.getId() == null) {
                    if (reportFile.getFileName().endsWith(".pdf")) {
                        pdfFile = reportFile.getFile();
                    } else {
                        pdfFile = reportFile.getPdfFile();
                    }
                } else {
                    pdfFile = sessionBean.getPersistence().getDataFile(reportFile.getId(), true).getPdfFile();
                }
                ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
                PortletContext portletContext = (PortletContext) externalContext.getContext();
                String dir = portletContext.getRealPath("/resources/reports/");
                File file = File.createTempFile("wf_",".pdf", new File(dir));

                if (!file.exists()) {
                    boolean created = file.createNewFile();
                    if (!created)
                        throw new Exception("Ошибка при создании pdf-файла");
                }

                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(pdfFile);
                outputStream.flush();
                outputStream.close();

                Image image = new Image();
                image.setPath("/frsi-portlet/resources/reports/" + file.getName());
                image.setTitle(reportFile.getFileName());
                image.setIndex(index);

                imageListIn.add(image);
                if(reportFile == selectedItem)
                    selectIndex = index;
                index++;
            }
            if(imageListIn.size() > 0) {
                curImageIn = imageListIn.get(selectIndex);
            }
        } catch (Exception e) {
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
        }
        imageList = imageListIn;
        curImage = curImageIn;
    }

    private long getCompanyId() {
        long companyId;
        try {
            Company company = CompanyLocalServiceUtil.getCompanyByMx(PropsUtil.get(PropsKeys.COMPANY_DEFAULT_WEB_ID));
            companyId = company.getCompanyId();
        } catch(PortalException e) {
            throw new EJBException(e);
        } catch(SystemException e) {
            throw new EJBException(e);
        }
        return companyId;
    }

    private UserGroup getPortalUserGroup(long companyId, String name) {
        UserGroup userGroup = null;
        try {
            userGroup = UserGroupLocalServiceUtil.getUserGroup(companyId, name);
        } catch(PortalException e) {
            if (!e.getMessage().startsWith("No UserGroup exists with the key"))
                throw new EJBException(e);
        } catch(SystemException e) {
            throw new EJBException(e);
        }
        return userGroup;
    }

    private List<User> getPortalUserGroupUsers(long userGroupId) {
        List<User> users = null;
        try {
            users = UserLocalServiceUtil.getUserGroupUsers(userGroupId);
        } catch(SystemException e) {
            throw new EJBException(e);
        }
        return users;
    }

    private User getPortalUser(long userId) {
        User user;
        try {
            user = UserLocalServiceUtil.getUser(userId);
        } catch (SystemException e) {
            throw new IllegalStateException(e);
        } catch (PortalException e) {
            throw new IllegalStateException(e);
        }
        return user;
    }

    private UserGroup addPortalUserGroup(long userId, long companyId, String name, String description) {
        UserGroup userGroup = null;
        try {
            userGroup = UserGroupLocalServiceUtil.addUserGroup(userId, companyId, name, description);
        } catch(PortalException e) {
            throw new EJBException(e);
        } catch(SystemException e) {
            throw new EJBException(e);
        }
        return userGroup;
    }

    private UserGroup updatePortalUserGroup(long companyId, long userGroupId, String name, String description) {
        UserGroup userGroup = null;
        try {
            userGroup = UserGroupLocalServiceUtil.updateUserGroup(companyId, userGroupId, name, description);
        } catch(PortalException e) {
            throw new EJBException(e);
        } catch(SystemException e) {
            throw new EJBException(e);
        }
        return userGroup;
    }

    private UserGroup deletePortalUserGroup(long userGroupId) {
        List<User> users = getPortalUserGroupUsers(userGroupId);
        for (User user: users)
            deletePortalUserFromUserGroup(userGroupId, user.getUserId());
        UserGroup userGroup = null;
        try {
            userGroup = UserGroupLocalServiceUtil.deleteUserGroup(userGroupId);
        } catch(PortalException e) {
            throw new EJBException(e);
        } catch(SystemException e) {
            throw new EJBException(e);
        }
        return userGroup;
    }

    private void addPortalUserToUserGroup(long userGroupId, long userId) {
        try {
            long[] userIds = {userId};
            UserLocalServiceUtil.addUserGroupUsers(userGroupId, userIds);
        } catch(PortalException e) {
            throw new EJBException(e);
        } catch(SystemException e) {
            throw new EJBException(e);
        }
    }

    private void deletePortalUserFromUserGroup(long userGroupId, long userId) {
        try {
            UserLocalServiceUtil.deleteUserGroupUser(userGroupId, userId);
        } catch(PortalException e) {
            throw new EJBException(e);
        } catch(SystemException e) {
            throw new EJBException(e);
        }
    }

    private void updatePortalUserStatus(long userId, int status) {
        try {
            UserLocalServiceUtil.updateStatus(userId, status);
        } catch(PortalException e) {
            throw new EJBException(e);
        } catch(SystemException e) {
            throw new EJBException(e);
        }
    }

    private List<Group> getPortalUserGroupGroups(long companyId, String userGroupName) {
        List<Group> groups = new ArrayList<Group>();
        try {
            List<UserGroup> userGroups = new ArrayList<UserGroup>();
            userGroups.add(getPortalUserGroup(companyId, userGroupName));
            groups = GroupLocalServiceUtil.getUserGroupsRelatedGroups(userGroups);
        } catch(SystemException e) {
            throw new EJBException(e);
        }

        return groups;
    }

    private List<PortalGroup> getPortalGroups(long companyId, String groupPrefix) {
        List<Group> groups = new ArrayList<Group>();
        try {
            groups = GroupLocalServiceUtil.getCompanyGroups(companyId, 0, 1000);
        } catch(SystemException e) {
            throw new EJBException(e);
        }

        List<PortalGroup> findGroups = new ArrayList<PortalGroup>();
        for (Group group : groups) {
            if (group.isSite() && group.getName().toLowerCase().contains(groupPrefix.toLowerCase())) {
                boolean isFound = false;
                if (this.groups != null) {
                    for (PortalGroup item : this.groups) {
                        if (item.getName().equals(group.getName())) {
                            isFound = true;
                            break;
                        }
                    }
                }
                if (!isFound) {
                    PortalGroup findGroup = new PortalGroup(group.getGroupId(), group.getName());
                    findGroups.add(findGroup);
                }
            }
        }

        return findGroups;
    }

    private void addPortaGroupToGroupUser(long groupId, long userGroupId) {
        try {
            long[] userGroupIds = {userGroupId};
            UserGroupLocalServiceUtil.addGroupUserGroups(groupId, userGroupIds);
        } catch(SystemException e) {
            throw new EJBException(e);
        }
    }

    private void deletePortalGroup(long groupId, long userGroupId) {
        try {
            long[] userGroupsId = {userGroupId};
            UserGroupLocalServiceUtil.unsetGroupUserGroups(groupId, userGroupsId);
        } catch(SystemException e) {
            throw new EJBException(e);
        }
    }

    public boolean addUserGroup() {
        userBean.checkAccess("PERMIS:USER_GROUP:ADD");

        if (checkData(true)) return false;

        UserGroup userGroup = addPortalUserGroup(sessionBean.portalUser.getUserId(), companyId, editingUserGroup.getName(), editingUserGroup.getDescription());

        if (userGroup != null) {
            try {
                editingUserGroup.setUserGroupId(userGroup.getUserGroupId());

                Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();

                AuditEvent auditEvent = new AuditEvent();
                auditEvent.setCodeObject("USER_GROUP");
                auditEvent.setNameObject("Группа");
                auditEvent.setIdKindEvent(60L);
                auditEvent.setDateEvent(date);
                auditEvent.setIdRefRespondent(null);
                auditEvent.setDateIn(date);
                auditEvent.setRecId(null);
                auditEvent.setUserId(sessionBean.portalUser.getUserId());
                auditEvent.setUserLocation(sessionBean.user.getLoginIP());

                sessionBean.getPersistence().insertPortalUserGroup(editingUserGroup, true, null, auditEvent);
            } catch(EJBException e) {
                deleteUserGroup(userGroup.getUserGroupId());
                throw new EJBException(e);
            }
        }

        return true;
    }

    public boolean updateUserGroup() {
        PortalUserGroup portalUserGroup = sessionBean.getPersistence().getUserGroupByGroupId(selectedUserGroup.getUserGroupId(), null);
        if(editingUserGroup.equals(portalUserGroup))
            return false;

        userBean.checkAccess("PERMIS:USER_GROUP:EDIT");

        if (checkData(true)) return false;

        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();

        UserGroup userGroup = updatePortalUserGroup(companyId, editingUserGroup.getUserGroupId(), editingUserGroup.getName(), editingUserGroup.getDescription());

        if (userGroup != null) {
            Map<String, Boolean> deletePermissions = new HashMap<String, Boolean>();
            deletePermissions.put("Role", editingUserGroup.getRoleId().equals(0L) ? false : !editingUserGroup.getRoleId().equals(selectedUserGroup.getRoleId()));
            deletePermissions.put("RefSubjectType", editingUserGroup.getRefSubjectTypeRecId().equals(0L) ? false : !editingUserGroup.getRefSubjectTypeRecId().equals(selectedUserGroup.getRefSubjectTypeRecId()));
            deletePermissions.put("RefDepartment", editingUserGroup.getRefDepartmentRecId().equals(0L) ? false : !editingUserGroup.getRefDepartmentRecId().equals(selectedUserGroup.getRefDepartmentRecId()));
            deletePermissions.put("RefRespondent", editingUserGroup.getRefRespondentRecId().equals(0L) ? false : !editingUserGroup.getRefRespondentRecId().equals(selectedUserGroup.getRefRespondentRecId()));

            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject("USER_GROUP");
            auditEvent.setNameObject("Группа");
            auditEvent.setIdKindEvent(61L);
            auditEvent.setDateEvent(date);
            auditEvent.setIdRefRespondent(null);
            auditEvent.setDateIn(date);
            auditEvent.setRecId(null);
            auditEvent.setUserId(sessionBean.portalUser.getUserId());
            auditEvent.setUserLocation(sessionBean.user.getLoginIP());
            sessionBean.getPersistence().updatePortalUserGroup(editingUserGroup, true, deletePermissions, null, auditEvent);
        }

        return true;
    }

    private void deleteUserGroup(long userGroupId) {
        userBean.checkAccess("PERMIS:USER_GROUP:DELETE");

        UserGroup userGroup = deletePortalUserGroup(userGroupId);

        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();

        if (userGroup != null) {

            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject("USER_GROUP");
            auditEvent.setNameObject("Группа");
            auditEvent.setIdKindEvent(62L);
            auditEvent.setDateEvent(date);
            auditEvent.setIdRefRespondent(null);
            auditEvent.setDateIn(date);
            auditEvent.setRecId(null);
            auditEvent.setUserId(sessionBean.portalUser.getUserId());
            auditEvent.setUserLocation(sessionBean.user.getLoginIP());

            sessionBean.getPersistence().deletePortalUserGroup(userGroupId, null, auditEvent);
        }
    }

    public boolean addUserToUserGroup(long userGroupId, long userId) {
        if (!sessionBean.getPersistence().isExistUserInUserGroup(userGroupId, userId, null)) {
            userBean.checkAccess("PERMIS:USER:ADD_TO_GROUP");

            addPortalUserToUserGroup(userGroupId, userId);

            Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();

            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject("USER_GROUP");
            auditEvent.setNameObject("Группа");
            auditEvent.setIdKindEvent(63L);
            auditEvent.setDateEvent(date);
            auditEvent.setIdRefRespondent(null);
            auditEvent.setDateIn(date);
            auditEvent.setRecId(null);
            auditEvent.setUserId(sessionBean.portalUser.getUserId());
            auditEvent.setUserLocation(sessionBean.user.getLoginIP());

            try {
                sessionBean.getPersistence().addUserToUserGroup(userGroupId, userId, null, auditEvent);
            } catch (EJBException e) {
                throw new EJBException(e);
            }
            List<UserGroup> userGroups;
            try {
                userGroups = UserGroupLocalServiceUtil.getUserUserGroups(userId);
            } catch (SystemException e) {
                throw new IllegalStateException(e);
            }
            for (UserGroup group : userGroups) {
                if (group.getUserGroupId() != userGroupId) {
                    deletePortalUserFromUserGroup(group.getUserGroupId(), userId);
                }
            }

            return true;
        }

        return false;
    }

    public boolean updateUser() {
        PortalUser user = sessionBean.getPersistence().getUserByUserId(editingUser.getUserId(), null);
        /*if (editingUser.equalsFull(user))
            return false;*/

        userBean.checkAccess("PERMIS:USER:EDIT");

        if (!editingUser.isBlocked() && user.isBlocked())
            updatePortalUserStatus(editingUser.getUserId(), WorkflowConstants.STATUS_APPROVED);
        else if (editingUser.isBlocked() && !user.isBlocked())
            updatePortalUserStatus(editingUser.getUserId(), WorkflowConstants.STATUS_INACTIVE);

        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();

        boolean fillUserPermissions = false;
        if (user.getRespondentId() != null && editingUser.getRespondentId() != null) {
            fillUserPermissions = user.getRespondentId().longValue() != editingUser.getRespondentId().longValue();
        } else {
            fillUserPermissions = user.getRespondentId() != null || editingUser.getRespondentId() != null;
        }


        AuditEvent auditEvent = null;
        if(!isAdd) {
            auditEvent = new AuditEvent();
            auditEvent.setCodeObject("USER_GROUP");
            auditEvent.setNameObject("Группа");
            auditEvent.setIdKindEvent(64L);
            auditEvent.setDateEvent(date);
            auditEvent.setIdRefRespondent(null);
            auditEvent.setDateIn(date);
            auditEvent.setRecId(null);
            auditEvent.setUserId(sessionBean.portalUser.getUserId());
            auditEvent.setUserLocation(sessionBean.user.getLoginIP());
        }
        sessionBean.getPersistence().updatePortalUser(editingUser, true, isAdd ? null : userWarrants, null, auditEvent, fillUserPermissions);

        return true;
    }

    private void deleteUserFromUserGroup(long userGroupId, long userId) {
        userBean.checkAccess("PERMIS:USER:DELETE_FROM_GROUP");

        deletePortalUserFromUserGroup(userGroupId, userId);

        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();


        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject("USER_GROUP");
        auditEvent.setNameObject("Группа");
        auditEvent.setIdKindEvent(65L);
        auditEvent.setDateEvent(date);
        auditEvent.setIdRefRespondent(null);
        auditEvent.setDateIn(date);
        auditEvent.setRecId(null);
        auditEvent.setUserId(sessionBean.portalUser.getUserId());
        auditEvent.setUserLocation(sessionBean.user.getLoginIP());

        sessionBean.getPersistence().deleteUserFromUserGroup(userGroupId, userId, null, auditEvent);
    }

    public void onAddFoundGroup() {
        userBean.checkAccess("PERMIS:GROUP:ADD");

        addPortaGroupToGroupUser(selectedFoundGroup.getGroupId(), selectedUserGroup.getUserGroupId());

        refreshGroups(selectedFoundGroup.getGroupId());
    }

    public String getRoleName(Long roleId) {
        if (roleId == null || roleId.equals(0L)) {
            return "";
        }
        if (!rolesMap.containsKey(roleId))
            return "";
        return rolesMap.get(roleId);
    }

    public String getSubjectTypeName(Long subjectTypeRecId) {
        if (subjectTypeRecId == null || subjectTypeRecId.equals(0L)) {
            return "";
        }
        if (!subjectTypesMap.containsKey(subjectTypeRecId))
            return "";
        return subjectTypesMap.get(subjectTypeRecId);
    }

    public String getDepartmentName(Long departmentRecId) {
        if (departmentRecId == null || departmentRecId.equals(0L)) {
            return "";
        }
        if (!departmentsMap.containsKey(departmentRecId))
            return "";
        return departmentsMap.get(departmentRecId);
    }

    public String getRespondentName(Long respondentRecId) {
        if (respondentRecId == null || respondentRecId.equals(0L)) {
            return "";
        }
        if (!allRespondentsMap.containsKey(respondentRecId))
            return "";
        return allRespondentsMap.get(respondentRecId);
    }

    public String getPostItemName(Long postItemRecId) {
        if (postItemRecId == null || postItemRecId.equals(0L)) {
            return "";
        }
        if (!postItemsMap.containsKey(postItemRecId))
            return "";
        return postItemsMap.get(postItemRecId);
    }

    public void onSetSearchUser() {
        searchUser = new PortalUser();
        selectedFoundUser = null;
        foundUsers = null;
    }

    public void onSetSearchGroup() {
        selectedFoundGroup = new PortalGroup();
        foundGroups = getPortalGroups(companyId, groupPrefix);
    }

    public void onFilterRolesShow() {
    }

    public void onFilterRolesHide() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filterRoles.size(); i++) {
            sb.append(roles.get(i).getName());
            if (i < filterRoles.size() - 1) sb.append(", ");
        }
        int size = filterRoles.size();
        if (size == 0) filterRolesText = "Нет ни одной роли!";
        else if (size == 1)
            filterRolesText = filterRoles.get(0).getName();
        else if (size == roles.size()) filterRolesText = "Все";
        else filterRolesText = "Несколько ролей (" + size + ")";
    }

    public void resetFilterRoles() {
        filterRoles.clear();
        filterRoles.addAll(roles);
        filterRolesText = "Все";
    }

    public void onFilterSubjectTypesShow() {
    }

    public void onFilterSubjectTypesHide() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filterSubjectTypes.size(); i++) {
            sb.append(subjectTypeItems.get(i).getName(sessionBean.languageCode));
            if (i < filterSubjectTypes.size() - 1) sb.append(", ");
        }
        int size = filterSubjectTypes.size();
        if (size == 0) filterSubjectTypesText = "Нет ни одного типа субъекта!";
        else if (size == 1)
            filterSubjectTypesText = (filterSubjectTypes.get(0).getShortNameRu() == null || filterSubjectTypes.get(0).getShortNameRu().isEmpty()) ? filterSubjectTypes.get(0).getNameRu() : filterSubjectTypes.get(0).getShortNameRu();
        else if (size == subjectTypeItems.size()) filterSubjectTypesText = "Все";
        else filterSubjectTypesText = "Несколько типов субъектов (" + size + ")";
    }

    public void resetFilterSubjectTypes() {
        filterSubjectTypes.clear();
        filterSubjectTypes.addAll(subjectTypeItems);
        filterSubjectTypesText = "Все";
    }

    public void onFilterDepartmentsShow() {
    }

    public void onFilterDepartmentsHide() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filterDepartments.size(); i++) {
            sb.append(departmentItems.get(i).getName(sessionBean.languageCode));
            if (i < filterDepartments.size() - 1) sb.append(", ");
        }
        int size = filterDepartments.size();
        if (size == 0) filterDepartmentsText = "Нет ни одного филиала!";
        else if (size == 1)
            filterDepartmentsText = filterDepartments.get(0).getName(sessionBean.languageCode);
        else if (size == departmentItems.size()) filterDepartmentsText = "Все";
        else filterDepartmentsText = "Несколько филиалов (" + size + ")";
    }

    public void resetFilterDepartments() {
        filterDepartments.clear();
        filterDepartments.addAll(departmentItems);
        filterDepartmentsText = "Все";
    }

    public void clearFilters() {
        resetFilterRoles();
        resetFilterSubjectTypes();
        resetFilterDepartments();
        filterUser = null;
        filterGroupName = null;
    }

    public void onFilterRespondentsShow() {
    }

    public void onFilterRespondentsHide() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filterRespondents.size(); i++) {
            sb.append(respondents.get(i).getName(sessionBean.languageCode));
            if (i < filterRespondents.size() - 1) sb.append(", ");
        }
        int size = filterRespondents.size();
        if (size == 0) filterRespondentsText = "Нет ни одной организации!";
        else if (size == 1)
            filterRespondentsText = filterRespondents.get(0).getName(sessionBean.languageCode);
        else if (size == respondents.size()) filterRespondentsText = "Все";
        else filterRespondentsText = "Несколько организации (" + size + ")";
    }

    public void resetFilterRespondents() {
        filterRespondents.clear();
        filterRespondents.addAll(respondents);
        filterRespondentsText = "Все";
    }

    public boolean renderUserPassportIdn() {
        return selectedUserGroup != null && selectedUserGroup.getRoleId() != null && selectedUserGroup.getRoleId().equals(Constants.FRSI_RESPONDENT_ROLE);
    }

    public boolean isNBUserGroup() {
        return selectedUserGroup != null
                && selectedUserGroup.getRoleId() != null
                && (selectedUserGroup.getRoleId().equals(Constants.FRSI_USER_NB_MAIN_ROLE)
                || selectedUserGroup.getRoleId().equals(Constants.FRSI_USER_NB_MAIN_ROLE)
                || selectedUserGroup.getRoleId().equals(Constants.FRSI_USER_NB_DEPARTMENT_ROLE));
    }

    /*public String getGroupPrefix() {
        return groupPrefix;
    }

    public List<RefRespondentItem> getRespondentList() {
        return respondentList;
    }

    public void setRespondentList(List<RefRespondentItem> respondentList) {
        this.respondentList = respondentList;
    }*/

    public String getLanguageCode() {
        return languageCode;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public boolean isAdd() {
        return isAdd;
    }

    public String getDeleteUserGroupMessage() {
        deleteUserGroupMessage = (users != null && users.size() > 0) ? "Группа содержит пользователей. Удалить группу" : "Удалить группу";
        return deleteUserGroupMessage;
    }

    public List<PortalUserGroup> getUserGroups() {
        return userGroups;
    }

    public PortalUserGroup getSelectedUserGroup() {
        return selectedUserGroup;
    }

    public void setSelectedUserGroup(PortalUserGroup selectedUserGroup) {
        this.selectedUserGroup = selectedUserGroup;
    }

    public List<PortalUser> getUsers() {
        return users;
    }

    public PortalUser getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(PortalUser selectedUser) {
        this.selectedUser = selectedUser;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public Locale getLocale() {
        return applicationBean.getLocale();
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public PortalUserGroup getEditingUserGroup() {
        return editingUserGroup;
    }

    public void setEditingUserGroup(PortalUserGroup editingUserGroup) {
        this.editingUserGroup = editingUserGroup;
    }

    public PortalUser getEditingUser() {
        return editingUser;
    }

    public void setEditingUser(PortalUser editingUser) {
        this.editingUser = editingUser;
    }

    public List<RefRespondentItem> getRespondents() {
        return respondents;
    }

    public void setRespondents(List<RefRespondentItem> respondents) {
        this.respondents = respondents;
    }

    public Long getSelectedRespondentId() {
        return selectedRespondentId;
    }

    public void setSelectedRespondentId(Long selectedRespondentId) {
        this.selectedRespondentId = selectedRespondentId;
    }

    public PortalUser getSearchUser() {
        return searchUser;
    }

    public void setSearchUser(PortalUser searchUser) {
        this.searchUser = searchUser;
    }

    public List<PortalUser> getFoundUsers() {
        return foundUsers;
    }

    public void setFoundUsers(List<PortalUser> foundUsers) {
        this.foundUsers = foundUsers;
    }

    public PortalUser getSelectedFoundUser() {
        return selectedFoundUser;
    }

    public void setSelectedFoundUser(PortalUser selectedFoundUser) {
        this.selectedFoundUser = selectedFoundUser;
    }

    public Map<String, Long> getRolesInvariantMap() {
        return rolesInvariantMap;
    }

    public Map<String, Long> getSubjectTypesInvariantMap() {
        return subjectTypesInvariantMap;
    }

    public Map<String, Long> getDepartmentsInvariantMap() {
        return departmentsInvariantMap;
    }

    public Map<String, Long> getPostItemsInvariantMap() {
        return postItemsInvariantMap;
    }

    public List<String> getDesignUsers() {
        return designUsers;
    }

    public int getActiveUserGroupTabIndex() {
        return activeUserGroupTabIndex;
    }

    public void setActiveUserGroupTabIndex(int activeUserGroupTabIndex) {
        this.activeUserGroupTabIndex = activeUserGroupTabIndex;
    }

    public PortalGroup getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(PortalGroup selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public List<PortalGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<PortalGroup> groups) {
        this.groups = groups;
    }

    public List<PortalGroup> getFoundGroups() {
        return foundGroups;
    }

    public PortalGroup getSelectedFoundGroup() {
        return selectedFoundGroup;
    }

    public void setSelectedFoundGroup(PortalGroup selectedFoundGroup) {
        this.selectedFoundGroup = selectedFoundGroup;
    }

    public boolean isShowRefPost() {
        return isShowRefPost;
    }

    public boolean isShowMustSign() {
        return isShowMustSign;
    }

    public boolean isShowDesignUserName() {
        return isShowDesignUserName;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public List<Role> getFilterRoles() {
        return filterRoles;
    }

    public void setFilterRoles(List<Role> filterRoles) {
        this.filterRoles = filterRoles;
    }

    public String getFilterRolesText() {
        return filterRolesText;
    }

    public String getFilterSubjectTypesText() {
        return filterSubjectTypesText;
    }

    public List<RefSubjectTypeItem> getSubjectTypeItems() {
        return subjectTypeItems;
    }

    public List<RefSubjectTypeItem> getFilterSubjectTypes() {
        return filterSubjectTypes;
    }

    public void setFilterSubjectTypes(List<RefSubjectTypeItem> filterSubjectTypes) {
        this.filterSubjectTypes = filterSubjectTypes;
    }

    public String getFilterDepartmentsText() {
        return filterDepartmentsText;
    }

    public List<RefDepartmentItem> getDepartmentItems() {
        return departmentItems;
    }

    public List<RefDepartmentItem> getFilterDepartments() {
        return filterDepartments;
    }

    public void setFilterDepartments(List<RefDepartmentItem> filterDepartments) {
        this.filterDepartments = filterDepartments;
    }

    public String getFilterGroupName() {
        return filterGroupName;
    }

    public void setFilterGroupName(String filterGroupName) {
        this.filterGroupName = filterGroupName;
    }

    public String getFilterUser() {
        return filterUser;
    }

    public void setFilterUser(String filterUser) {
        this.filterUser = filterUser;
    }

    public String getFilterRespondentsText() {
        return filterRespondentsText;
    }

    public List<RefRespondentItem> getFilterRespondents() {
        return filterRespondents;
    }

    public void setFilterRespondents(List<RefRespondentItem> filterRespondents) {
        this.filterRespondents = filterRespondents;
    }

    public List<UserWarrant> getUserWarrants() {
        return userWarrants;
    }

    public UserWarrant getSelectedUserWarrant() {
        return selectedUserWarrant;
    }

    public void setSelectedUserWarrant(UserWarrant selectedUserWarrant) {
        this.selectedUserWarrant = selectedUserWarrant;
    }

    public UserWarrant getEditingUserWarrant() {
        return editingUserWarrant;
    }

    public void setEditingUserWarrant(UserWarrant editingUserWarrant) {
        this.editingUserWarrant = editingUserWarrant;
    }

    public List<PortalUser> getRespondentUsers() {
        return respondentUsers;
    }

    public boolean isShowUserWarrantTab() {
        return showUserWarrantTab;
    }

    public int getActiveUserTabIndex() {
        return activeUserTabIndex;
    }

    public void setActiveUserTabIndex(int activeUserTabIndex) {
        this.activeUserTabIndex = activeUserTabIndex;
    }

    public AttachedFile getSelectedWarrantFile() {
        return selectedWarrantFile;
    }

    public void setSelectedWarrantFile(AttachedFile selectedWarrantFile) {
        this.selectedWarrantFile = selectedWarrantFile;
    }

    public UserWarrant getFileEditingUserWarrant() {
        return fileEditingUserWarrant;
    }

    public List<Image> getImageList() {
        return imageList;
    }

    public Image getCurImage() {
        return curImage;
    }

    public void setCurImage(Image curImage) {
        this.curImage = curImage;
    }

    public List<RefRespondentItem> getGroupComboboxRespondents() {
        return groupComboboxRespondents;
    }

    public boolean isEditingNaturalPerson() {
        return editingNaturalPerson;
    }

    public String getEditingUserIdn() {
        return editingUserIdn;
    }

    public void setEditingUserIdn(String editingUserIdn) {
        this.editingUserIdn = editingUserIdn;
    }

    public String getEditingUserPassport() {
        return editingUserPassport;
    }

    public void setEditingUserPassport(String editingUserPassport) {
        this.editingUserPassport = editingUserPassport;
    }

    private static class RefDepartmentItemComparator implements Comparator<RefDepartmentItem>{

        @Override
        public int compare(RefDepartmentItem o1, RefDepartmentItem o2) {
            return o1.getNameRu().toLowerCase().compareTo(o2.getNameRu().toLowerCase());
        }
    }
    private static class RefSubjectTypeItemComparator implements Comparator<RefSubjectTypeItem>{

        @Override
        public int compare(RefSubjectTypeItem o1, RefSubjectTypeItem o2) {
            return o1.getNameRu().toLowerCase().compareTo(o2.getNameRu().toLowerCase());
        }
    }
}
