package mb;

import entities.*;
import org.apache.log4j.Logger;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.tabview.Tab;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.TreeNode;
import util.Convert;
import util.Helper;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by Nuriddin.Baideuov on 03.04.2015.
 */
@ManagedBean
@SessionScoped
public class AdmPermissionBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");
    boolean isGroup;
    PortalUserGroup userGroup;
    PortalUser user;
    boolean isShowDepartmentTab;
    boolean isShowSubjectTypesTab;
    boolean isShowRespondentsTab;
    boolean isShowFormsTab;
    boolean isShowPermissionEdit;
    boolean isShowPermissionDelete;
    boolean isShowPermissionApprove;
    boolean isShowPermissionDisapprove;
    boolean showRespondentsFilter;
    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{admBean}")
    private AdmBean admBean;
    private long selectedUserId;
    private long selectedUserGroupId;
    private Long selectedSubjectTypeRecId;
    private String errorMessage;
    private TreeNode permissionTree;
    private List<Permission> permissions;
    private TreeNode[] selectedPermissions;
    private Set<Permission> changedPermissions = new HashSet<Permission>();
    private List<PermissionDepartment> departments = new ArrayList<PermissionDepartment>();
    private PermissionDepartment selectedDepartment;
    private Set<PermissionDepartment> changedDepartments = new HashSet<PermissionDepartment>();
    private List<PermissionSubjectType> permissionSubjectTypes = new ArrayList<PermissionSubjectType>();
    private PermissionSubjectType selectedPermissionSubjectType;
    private Set<PermissionSubjectType> changedpermissionSubjectTypes = new HashSet<PermissionSubjectType>();
    private List<RefSubjectTypeItem> subjectTypes;
    private List<PermissionRespondent> respondents;
    private TreeNode respondentTree;
    private TreeNode[] selectedRespondents;
    private Set<PermissionRespondent> changedRespondents = new HashSet<PermissionRespondent>();
    private List<PermissionFormContainer> pfContainers = new ArrayList<PermissionFormContainer>();
    private LazyDataModel<PermissionFormContainer> ldmPfContainer;
    private List<PermissionFormContainer> filteredPfContainers;
    private Set<PermissionFormContainer> changedPfContainers = new HashSet<PermissionFormContainer>();
    private PermissionFormContainer selectedPfContainer;
    private DataTable pfContainerDataTable;
    private Map<String, String> formTypeCodesMap;
    private String selectedFormTypeCode;
    private boolean allShowPermissionValue;
    private RefRespondentItem selectedRespondent;
    private int activeTabIndex;
    private boolean showPermission;
    private boolean editPermission;
    private boolean deletePermission;
    private boolean approvePermission;
    private boolean disapprovePermission;
    private boolean signPermission;
    private Map<String, Long> subjectTypesInvariantMap;
    private Long userId;
    private String userLocation;

    private List<RefRespondentItem> formRespondents;
    private String filterFormRespondentsText;
    private List<RefRespondentItem> filterFormRespondents = new ArrayList<RefRespondentItem>();

    private boolean reloadForms; // indicates that forms need to be reloaded
    private boolean reloadFormRespondents; // indicates that respondents in form tab need to be reloaded
    private String currentTabId; // Keeps current tab id (tabGeneral, tabDepartments, tabSubjectTypes, tabRespondents, tabForms)

    private List<RefNpaItem> filterNPA = new ArrayList<RefNpaItem>();
    private List<RefNpaItem> npaList;
    private String filterNPAText;

    @PostConstruct
    public void init() {
        Date dateStart = new Date();

        try {
            if (sessionBean.isEjbNull()) sessionBean.init();

            permissionTree = new DefaultTreeNode("Root", null);
            respondentTree = new DefaultTreeNode("Root", null);
            ldmPfContainer = new LdmPfContainer();

            userId = sessionBean.portalUser.getUserId();
            userLocation = sessionBean.user.getLoginIP();

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
    }

    /**
     * перестраивает дерево прав по списку прав
     */
    private void updatePermissionTree() {
        if (selectedUserGroupId != 0)
            permissions = sessionBean.getPersistence().getAllPermissionByUserGroup(selectedUserGroupId, userGroup.getRoleId());
        else {
            permissions = sessionBean.getPersistence().getAllPermissionByUser(selectedUserId, userGroup.getRoleId());
        }

        permissionTree = new DefaultTreeNode("Root", null);
        Map<Long, TreeNode> nodes = new HashMap<Long, TreeNode>();
        for (Permission permission : permissions) {
            TreeNode parentNode;
            if (permission.getParentId() == null || permission.getParentId() == 0)
                parentNode = permissionTree;
            else
                parentNode = nodes.get(permission.getParentId());
            TreeNode node;
            if (permission.isLeaf())
                node = new DefaultTreeNode("leaf", permission, parentNode);
            else
                node = new DefaultTreeNode("node", permission, parentNode);

            node.setSelected(permission.isActive());
            parentNode.setSelectable(false);
            nodes.put(permission.getId(), node);
        }
    }

    private void updateDepartments() {
        if (selectedUserGroupId != 0)
            departments = sessionBean.getPersistence().getAllDepartmentByUserGroup(selectedUserGroupId);
        else {
            departments = sessionBean.getPersistence().getAllPermissionDepartmentByUser(selectedUserId);
        }
        Collections.sort(departments, new Comparator<PermissionDepartment>() {
            @Override
            public int compare(PermissionDepartment o1, PermissionDepartment o2) {
                return o1.getDepartment().getNameRu().compareTo(o2.getDepartment().getNameRu());
            }
        });
    }

    private void updateSubjectTypes(){
        if (selectedUserGroupId != 0) {
            permissionSubjectTypes = sessionBean.getPersistence().getAllPermissionSubjectTypesByUserGroup(selectedUserGroupId);
        } else {
            permissionSubjectTypes = sessionBean.getPersistence().getAllPermissionSubjectTypesByUser(selectedUserId);
        }
    }

    private void updateRespondentTree(boolean isGroup) {
        boolean isRespondentUser = !isGroup && userGroup.getRoleId() != null && userGroup.getRoleId().longValue() == Constants.FRSI_RESPONDENT_ROLE;

        if (isRespondentUser) {
            subjectTypes = sessionBean.getReference().getRefSubjectTypeByWarrant(admBean.getSelectedUser().getRespondentId());
        } else {
            if (userGroup.getRefSubjectTypeRecId() != null && !userGroup.getRefSubjectTypeRecId().equals(0L)) {
                RefSubjectTypeItem subjectType = new RefSubjectTypeItem();
                subjectType.setRecId(userGroup.getRefSubjectTypeRecId());
                subjectTypes = (List<RefSubjectTypeItem>) sessionBean.getReference().getRefAbstractByFilterList(RefSubjectTypeItem.REF_CODE, subjectType, sessionBean.getIntegration().getNewDateFromBackEndServer());
            } else {
                subjectTypes = sessionBean.getReference().getRefSubjectTypeListAdvanced(sessionBean.getIntegration().getNewDateFromBackEndServer(), true);
            }
        }
        Collections.sort(subjectTypes, new Comparator<RefSubjectTypeItem>() {
            @Override
            public int compare(RefSubjectTypeItem o1, RefSubjectTypeItem o2) {
                return o1.getNameRu().compareTo(o2.getNameRu());
            }
        });
        if (isRespondentUser) {
            respondents = sessionBean.getPersistence().getRespondentsByWarrant(selectedUserId, admBean.getSelectedUser().getRespondentId());
        } else {
            if (selectedUserGroupId != 0)
                respondents = sessionBean.getPersistence().getAllRespondentByUserGroup(selectedUserGroupId, userGroup.getRefDepartmentRecId(), userGroup.getRefSubjectTypeRecId(), userGroup.getRefRespondentRecId());
            else
                respondents = sessionBean.getPersistence().getAllRespondentByUser(selectedUserId, userGroup.getRefDepartmentRecId(), userGroup.getRefSubjectTypeRecId(), userGroup.getRefRespondentRecId());
        }

        Collections.sort(respondents, new Comparator<PermissionRespondent>() {
            @Override
            public int compare(PermissionRespondent o1, PermissionRespondent o2) {
                return o1.getRespondent().getNameRu().compareTo(o2.getRespondent().getNameRu());
            }
        });

        respondentTree = new DefaultTreeNode("Root", null);
        Map<Long, TreeNode> parents = new HashMap<Long, TreeNode>();
        RefRespondentItem resp = null;
        if (!isRespondentUser && userGroup.getRefRespondentRecId() != null && userGroup.getRefRespondentRecId() != 0) {
            if (respondents.size() > 0) {
                resp = respondents.get(0).getRespondent();
            }
            if (resp != null) {
                for (RefSubjectTypeItem subjectType : subjectTypes) {
                    if (resp.getRefSubjectTypeRecId().longValue() == subjectType.getRecId()) {
                        TreeNode node = new DefaultTreeNode("subject_type", subjectType, respondentTree);
                        parents.put(subjectType.getRecId(), node);
                        break;
                    }
                }
            }
        } else {
            for (RefSubjectTypeItem subjectType : subjectTypes) {
                TreeNode node = new DefaultTreeNode("subject_type", subjectType, respondentTree);
                parents.put(subjectType.getRecId(), node);

            }
        }
        for (PermissionRespondent respondent : respondents) {
            TreeNode parentNode = parents.get(respondent.getRespondent().getRefSubjectTypeRecId());
            if (parentNode != null) {
                TreeNode node = new DefaultTreeNode("respondent", respondent, parentNode);
                node.setSelected(respondent.isActive());
            }
        }

        for (TreeNode subjectTypeNode : respondentTree.getChildren()) {
            boolean allChildrenSelected = true;
            for (TreeNode respondentNode : subjectTypeNode.getChildren()) {
                if (!respondentNode.isSelected())
                    allChildrenSelected = false;
            }
            subjectTypeNode.setSelected(subjectTypeNode.getChildCount() > 0 && allChildrenSelected);
        }

        reloadFormRespondents = true;
    }

    public String getRespondentWarrantInfo(RefRespondentItem respondentItem) {
        if (respondentItem.getWarrantDate() == null && respondentItem.getWarrantNum() == null) {
            return "";
        } else {
            return "№" + (respondentItem.getWarrantNum() == null ? "" : respondentItem.getWarrantNum())
                    + " от " + (respondentItem.getWarrantDate() == null ? "" : Convert.dateFormatRus.format(respondentItem.getWarrantDate()));
        }
    }

    private void updateSubjectTypesMap() {
        /*boolean isRespondentUser = !isGroup && userGroup.getRoleId() != null && userGroup.getRoleId().longValue() == UserBean.FRSI_RESPONDENT_ROLE;
        List<RefSubjectTypeItem> subjectTypeList;
        if(isRespondentUser){
            subjectTypeList = sessionBean.getReference().getRefSubjectTypeByWarrant(admBean.getSelectedUser().getRespondentId());
        } else {
            subjectTypeList = sessionBean.getReference().getRefSubjectTypeListAdvanced(sessionBean.getIntegration().getNewDateFromBackEndServer(), true);
        }
        subjectTypesInvariantMap = new TreeMap<String, Long>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        for (RefSubjectTypeItem item : subjectTypeList) {
            if (isRespondentUser  || userGroup.getRefSubjectTypeRecId() == null || userGroup.getRefSubjectTypeRecId().equals(0L) || item.getRecId().equals(userGroup.getRefSubjectTypeRecId()))
                subjectTypesInvariantMap.put(item.getNameRu(), item.getRecId());
        }*/
        boolean selectedOutputForm = selectedFormTypeCode != null && (selectedFormTypeCode.equalsIgnoreCase(Form.Type.OUTPUT.name()) || selectedFormTypeCode.equalsIgnoreCase(Form.Type.CONSOLIDATED.name()));
        subjectTypesInvariantMap = new TreeMap<String, Long>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        for (TreeNode stNode : respondentTree.getChildren()) {
            RefSubjectTypeItem st = (RefSubjectTypeItem) stNode.getData();
            if (selectedOutputForm) {
                subjectTypesInvariantMap.put(st.getNameRu(), st.getRecId());
            } else {
                boolean isSelected = false;
                for (TreeNode respondentNode : stNode.getChildren()) {
                    if (respondentNode.isSelected()) {
                        isSelected = true;
                    }
                }
                if (isSelected) {
                    subjectTypesInvariantMap.put(st.getNameRu(), st.getRecId());
                }
            }
        }
        selectedSubjectTypeRecId = null;
    }

    private void updateFormTypeCodesMap() {
        formTypeCodesMap = new HashMap<String, String>();
        for (Form.Type formType : Form.Type.values()) {
            // Для ролей пользователь НБРК филиал и респондент скроем выходные и сводные формы
            if ((!userGroup.getRoleId().equals(Constants.FRSI_USER_NB_DEPARTMENT_ROLE) && (!userGroup.getRoleId().equals(Constants.FRSI_RESPONDENT_ROLE))) || formType.equals(Form.Type.INPUT) || formType.equals((Form.Type.INPUT_RAW)))
                formTypeCodesMap.put(Form.resMap.get(sessionBean.languageCode + "_" + formType.name()), formType.name());
        }
        selectedFormTypeCode = formTypeCodesMap.size() > 0 ? formTypeCodesMap.values().iterator().next() : null;
    }

    public void onPermissions(boolean isGroup) {
        this.isGroup = isGroup;
        user = admBean.getSelectedUser();
        userGroup = admBean.getSelectedUserGroup();

        isShowDepartmentTab = userGroup.getRoleId().equals(Constants.FRSI_ADMIN_DEPARTMENT_ROLE);
        isShowSubjectTypesTab = userGroup.getRoleId().equals(Constants.FRSI_ADMIN_SUBJECT_TYPE_ROLE);
        isShowRespondentsTab = userGroup.getRoleId().equals(Constants.FRSI_ADMIN_ROLE) || userGroup.getRoleId().equals(Constants.FRSI_USER_NB_MAIN_ROLE)
                || userGroup.getRoleId().equals(Constants.FRSI_USER_NB_DEPARTMENT_ROLE) || userGroup.getRoleId().equals(Constants.FRSI_ADMIN_RESPONDENT_ROLE);
        isShowFormsTab = userGroup.getRoleId().equals(Constants.FRSI_ADMIN_ROLE) || userGroup.getRoleId().equals(Constants.FRSI_USER_NB_MAIN_ROLE)
                || userGroup.getRoleId().equals(Constants.FRSI_USER_NB_DEPARTMENT_ROLE) || userGroup.getRoleId().equals(Constants.FRSI_RESPONDENT_ROLE);
        isShowPermissionEdit = userGroup.getRoleId().equals(Constants.FRSI_ADMIN_ROLE) || userGroup.getRoleId().equals(Constants.FRSI_USER_NB_MAIN_ROLE)
                || userGroup.getRoleId().equals(Constants.FRSI_RESPONDENT_ROLE);
        isShowPermissionDelete = userGroup.getRoleId().equals(Constants.FRSI_ADMIN_ROLE) || userGroup.getRoleId().equals(Constants.FRSI_USER_NB_MAIN_ROLE)
                || userGroup.getRoleId().equals(Constants.FRSI_RESPONDENT_ROLE);
        isShowPermissionApprove = userGroup.getRoleId().equals(Constants.FRSI_ADMIN_ROLE) || userGroup.getRoleId().equals(Constants.FRSI_USER_NB_MAIN_ROLE)
                || userGroup.getRoleId().equals(Constants.FRSI_USER_NB_DEPARTMENT_ROLE);
        isShowPermissionDisapprove = userGroup.getRoleId().equals(Constants.FRSI_ADMIN_ROLE) || userGroup.getRoleId().equals(Constants.FRSI_USER_NB_MAIN_ROLE)
                || userGroup.getRoleId().equals(Constants.FRSI_USER_NB_DEPARTMENT_ROLE);
        showRespondentsFilter = isShowRespondentsTab || userGroup.getRoleId().equals(Constants.FRSI_RESPONDENT_ROLE);

        if (isGroup) {
            selectedUserGroupId = userGroup.getUserGroupId();
            selectedUserId = 0L;
        }
        else {
            selectedUserGroupId = 0L;
            selectedUserId = user.getUserId();
        }

        selectedFormTypeCode = null;
        activeTabIndex = 0;
        allShowPermissionValue = false;

        changedPfContainers.clear();
        changedPermissions.clear();
        changedDepartments.clear();
        changedpermissionSubjectTypes.clear();
        changedRespondents.clear();

        updatePermissionTree();
        if (isShowDepartmentTab)
            updateDepartments();
        if(isShowSubjectTypesTab)
            updateSubjectTypes();
        if (isShowRespondentsTab) {
            updateRespondentTree(isGroup);
        }
        if (isShowFormsTab && !isShowRespondentsTab && showRespondentsFilter) {
            updateRespondentTree(isGroup);
            Helper.scanTreeNode(respondentTree, new TreeNodeVisitor() {
                @Override
                public boolean visit(TreeNode node) {
                    node.setSelected(true);
                    return false;
                }
            });
        }
        if (isShowFormsTab) {
//            selectedSubjectTypeRecId = userGroup.getRefSubjectTypeRecId();
            updateFormTypeCodesMap();
            updateSubjectTypesMap();
            resetFilterFormRespondents();
            refreshRefNpa();
            pfContainerDataTable.setFirst(1);
            pfContainerDataTable.getFilters().clear();
            updatePfContainers();
        }
    }

    public void onCheckboxChange(PermissionFormContainer pfContainer) {
        changedPfContainers.add(pfContainer);
    }

    public void onChangePermissionForm(ActionEvent event){
        String permissionName = (String) event.getComponent().getAttributes().get("permissionName");
        String updateIds = (String) event.getComponent().getAttributes().get("updateIds");
        PermissionFormContainer pfContainer = (PermissionFormContainer) event.getComponent().getAttributes().get("pfContainer");
        pfContainer.setValue(permissionName, !pfContainer.getValue(permissionName));
        pfContainer.updateItems(permissionName);
        changedPfContainers.add(pfContainer);

        CommandButton cb = (CommandButton) event.getSource();
        String[] arr = updateIds.split(",");
        for (String id : arr) {
            String update = cb.getClientId().substring(0, cb.getClientId().lastIndexOf(":") + 1) + id;
            RequestContext.getCurrentInstance().update(update);
        }

    }

    public void onPermissionChange(Permission p, boolean selected) {
        p.setActive(selected);
        changedPermissions.add(p);
    }


    public void onDepartmentCheckboxChange(PermissionDepartment department) {
        changedDepartments.add(department);
    }

    public void changeAllPermissionDepartments(boolean value) {
        for (PermissionDepartment department : departments) {
            if (department.isActive() != value) {
                department.setActive(value);
                changedDepartments.add(department);
            }
        }
    }

    public void onSubjectTypeCheckboxChange(PermissionSubjectType subjectType) {
        changedpermissionSubjectTypes.add(subjectType);
    }

    public void changeAllPermissionSubjectTypes(boolean value) {
        for (PermissionSubjectType subjectType : permissionSubjectTypes) {
            if (subjectType.isActive() != value) {
                subjectType.setActive(value);
                changedpermissionSubjectTypes.add(subjectType);
            }
        }
    }

    public void onRespondentSelect(NodeSelectEvent event) {
        onRespondentChange(event.getTreeNode(), true);
    }

    public void onRespondentUnSelect(NodeUnselectEvent event) {
        onRespondentChange(event.getTreeNode(), false);
    }

    private void onRespondentChange(TreeNode node, boolean select) {
        List<Long> unselectedRespondentRecIds = new ArrayList<Long>();

        if (node.getData() instanceof PermissionRespondent) {
            PermissionRespondent respondent = (PermissionRespondent) node.getData();
            respondent.setActive(select);
            changedRespondents.add(respondent);

            if (!select) unselectedRespondentRecIds.add(respondent.getRespondent().getRecId());
        } else {
            for (TreeNode childNode : node.getChildren()) {
                PermissionRespondent respondent = (PermissionRespondent) childNode.getData();
                respondent.setActive(select);
                changedRespondents.add(respondent);

                if (!select) unselectedRespondentRecIds.add(respondent.getRespondent().getRecId());
            }
        }

        if (unselectedRespondentRecIds.size() > 0) {
            deleteCachedRespondentForms(unselectedRespondentRecIds);
        }

        reloadFormRespondents = true;
        reloadForms = true;
    }

    /**
     * Удаляет измененные права форм по rec_id респондента
     * @param refRespondentRecIds
     */
    private void deleteCachedRespondentForms(List<Long> refRespondentRecIds) {
        if (changedPfContainers != null) {
            for (Iterator<PermissionFormContainer> it = changedPfContainers.iterator(); it.hasNext(); ) {
                PermissionFormContainer c = it.next();

                if (c.getFormTypeCode().equalsIgnoreCase(Form.Type.INPUT.name()) || c.getFormTypeCode().equalsIgnoreCase(Form.Type.INPUT_RAW.name())) {

                    for (Iterator<Map.Entry<Long, String>> it2 = c.getRefRespondents().entrySet().iterator(); it2.hasNext(); ) {
                        Map.Entry<Long, String> e = it2.next();
                        if (refRespondentRecIds.contains(e.getKey())) {
                            it2.remove();
                        }
                    }
                    for (Map.Entry<String, Map<Long, PermissionForm>> e2 : c.getItems().entrySet()) {
                        for (Iterator<Map.Entry<Long, PermissionForm>> it3 = e2.getValue().entrySet().iterator(); it3.hasNext(); ) {
                            Map.Entry<Long, PermissionForm> e3 = it3.next();
                            if (refRespondentRecIds.contains(e3.getKey())) {
                                it3.remove();
                            }
                        }
                    }
                    if (c.getRefRespondents().size() == 0) {
                        it.remove();
                    }
                }
            }
        }
    }

    public void onTabChange(TabChangeEvent event) {
        Tab tab = event.getTab();
        currentTabId = tab.getId();
        if (currentTabId.equalsIgnoreCase("tabForms")) {
            if (reloadFormRespondents) {
                updateSubjectTypesMap();
                resetFilterFormRespondents();
                reloadFormRespondents = false;
            }
            if (reloadForms) {
                updatePfContainers();
                reloadForms = false;
            }
        }
    }

    public void onSavePermissions() {
        if (isGroup)
            userBean.checkAccess("PERMIS:GROUP_PERMIS");
        else
            userBean.checkAccess("PERMIS:USER_PERMIS");

        savePermissions();

        changedPfContainers.clear();
        changedPermissions.clear();
        changedDepartments.clear();
        changedpermissionSubjectTypes.clear();
        changedRespondents.clear();
    }

    private void savePermissions(){
        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();

        AuditEvent auditEvent = new AuditEvent();
        if(selectedUserGroupId != 0) {
            auditEvent.setCodeObject("PERMISSIONS_GROUP");
            auditEvent.setNameObject("Права группы");
            auditEvent.setIdKindEvent(110L);
        }else{
            auditEvent.setCodeObject("PERMISSIONS_USER");
            auditEvent.setNameObject("Права пользователя");
            auditEvent.setIdKindEvent(111L);
        }

        auditEvent.setDateEvent(date);
        auditEvent.setIdRefRespondent(null);
        auditEvent.setDateIn(date);
        auditEvent.setRecId(null);
        auditEvent.setUserId(userId);
        auditEvent.setUserLocation(userLocation);

        sessionBean.getPersistence().savePermissions(changedPermissions, changedPfContainers, changedDepartments, changedpermissionSubjectTypes, changedRespondents,
                selectedUserGroupId, selectedUserId, userId, userLocation, auditEvent, date);
    }

    public void changeAllShowPermissions(boolean value) {
        List<PermissionFormContainer> list;
        if (filteredPfContainers != null && filteredPfContainers.size() > 0)
            list = filteredPfContainers;
        else
            list = pfContainers;
        for (PermissionFormContainer pfContainer : list) {
            if (value && !pfContainer.isAllActive("F:SHOW") || pfContainer.isShow() != value) {
                pfContainer.setShow(value);
                changedPfContainers.add(pfContainer);
            }
        }
    }

    public void changeAllEditPermissions(boolean value) {
        List<PermissionFormContainer> list;
        if (filteredPfContainers != null && filteredPfContainers.size() > 0)
            list = filteredPfContainers;
        else
            list = pfContainers;
        for (PermissionFormContainer pfContainer : list) {
            if (value && !pfContainer.isAllActive("F:EDIT") || pfContainer.isEdit() != value) {
                pfContainer.setEdit(value);
                changedPfContainers.add(pfContainer);
            }
        }
    }

    public void changeAllDeletePermissions(boolean value) {
        List<PermissionFormContainer> list;
        if (filteredPfContainers != null && filteredPfContainers.size() > 0)
            list = filteredPfContainers;
        else
            list = pfContainers;
        for (PermissionFormContainer pfContainer : list) {
            if (value && !pfContainer.isAllActive("F:DELETE") ||pfContainer.isDelete() != value) {
                pfContainer.setDelete(value);
                changedPfContainers.add(pfContainer);
            }
        }
    }

    public void changeAllApprovePermissions(boolean value) {
        List<PermissionFormContainer> list;
        if (filteredPfContainers != null && filteredPfContainers.size() > 0)
            list = filteredPfContainers;
        else
            list = pfContainers;
        for (PermissionFormContainer pfContainer : list) {
            if (value && !pfContainer.isAllActive("F:APPROVE") ||pfContainer.isApprove() != value) {
                pfContainer.setApprove(value);
                changedPfContainers.add(pfContainer);
            }
        }
    }

    public void changeAllDisapprovePermissions(boolean value) {
        List<PermissionFormContainer> list;
        if (filteredPfContainers != null && filteredPfContainers.size() > 0)
            list = filteredPfContainers;
        else
            list = pfContainers;
        for (PermissionFormContainer pfContainer : list) {
            if (value && !pfContainer.isAllActive("F:DISAPPROVE") ||pfContainer.isDisapprove() != value) {
                pfContainer.setDisapprove(value);
                changedPfContainers.add(pfContainer);
            }
        }
    }

    public void changeAllSignPermissions(boolean value) {
        List<PermissionFormContainer> list;
        if (filteredPfContainers != null && filteredPfContainers.size() > 0)
            list = filteredPfContainers;
        else
            list = pfContainers;
        for (PermissionFormContainer pfContainer : list) {
            if (pfContainer.isSign() != value) {
                pfContainer.setSign(value);
                changedPfContainers.add(pfContainer);
            }
        }
    }


    public void onSubjectTypeChange() {
        resetFilterFormRespondents();
        updatePfContainers();
    }

    public void onFormTypeCodeChange() {
        updateSubjectTypesMap();
        resetFilterFormRespondents();
        updatePfContainers();
    }

    private void updatePfContainers() {
        String formTypeCode = selectedFormTypeCode;

        if (formTypeCode == null) {
            pfContainers = Collections.emptyList();
            return;
        }

        Set<Long> stRecIds = new HashSet<Long>();
        Map<Long, String> refRespondents = new HashMap<Long, String>();
        if (formTypeCode.equals(Form.Type.OUTPUT.name()) || formTypeCode.equals(Form.Type.CONSOLIDATED.name())) {
            refRespondents.put(0l,""); // dump respondent
            if (selectedSubjectTypeRecId == null) {
                stRecIds.addAll(subjectTypesInvariantMap.values());
            } else {
                stRecIds.add(selectedSubjectTypeRecId);
            }
        } else {
            for (RefRespondentItem respondentItem : filterFormRespondents) {
                refRespondents.put(respondentItem.getRecId(), respondentItem.getIdn());
                stRecIds.add(respondentItem.getRefSubjectTypeRecId());
            }
        }

        if (selectedUserGroupId != 0)
            pfContainers = sessionBean.getPersistence().getAllPfContainerByUserGroupNSubjectTypeFormTypeCode(selectedUserGroupId, new ArrayList<Long>(stRecIds), formTypeCode, filterNPA,  "ru", refRespondents);
        else
            pfContainers = sessionBean.getPersistence().getAllPfContainerByUserNSubjectTypeFormTypeCode(selectedUserId, new ArrayList<Long>(stRecIds), formTypeCode, filterNPA,  "ru", refRespondents);

        applyChangedPfContainers(pfContainers);

        ((LdmPfContainer) ldmPfContainer).setSrcCollection(pfContainers);

        pfContainerDataTable.setFirst(0);
    }

    /**
     * Заменяем элементы ранее измененными элементами
     *
     * @param containers
     */
    private void applyChangedPfContainers(List<PermissionFormContainer> containers) {
        if (!changedPfContainers.isEmpty()) {

            Map<String, PermissionFormContainer> index = new HashMap<String, PermissionFormContainer>();
            for (PermissionFormContainer container : containers)
                index.put(container.getFormCode(), container);

            for (PermissionFormContainer changed : changedPfContainers) {
                if (index.containsKey(changed.getFormCode())) {
                    /*int i = containers.indexOf(index.get(changed.getFormCode()));
                    containers.remove(i);
                    containers.add(i, changed);*/
                    PermissionFormContainer pfContainer = index.get(changed.getFormCode());
                    for (Map<Long, PermissionForm> map : changed.getItems().values()) {
                        for (PermissionForm permissionForm : map.values()) {
                            if (pfContainer.getRefRespondents().keySet().contains(permissionForm.getRefRespondentRecId())) {
                                pfContainer.addItem(permissionForm);
                            }
                        }
                    }
                    pfContainer.updateAllStates();
                }
            }
        }
    }

    public void onFilterFormRespondentsShow() {
    }

    public void refreshRefNpa(){
        npaList = (List<RefNpaItem>)sessionBean.getReference().getRefAbstractList(RefNpaItem.REF_CODE,  sessionBean.getIntegration().getNewDateFromBackEndServer());
        resetFilterNPA();
    }

    public void onFilterFormRespondentsHide() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filterFormRespondents.size(); i++) {
            sb.append(formRespondents.get(i).getName(sessionBean.languageCode));
            if (i < filterFormRespondents.size() - 1) sb.append(", ");
        }
        int size = filterFormRespondents.size();
        if (size == 0) filterFormRespondentsText = "Нет ни одной организации!";
        else if (size == 1)
            filterFormRespondentsText = filterFormRespondents.get(0).getName(sessionBean.languageCode);
        else if (size == formRespondents.size()) filterFormRespondentsText = "Все";
        else filterFormRespondentsText = "Несколько организации (" + size + ")";

        updatePfContainers();
    }

    public void onFilterNPAHide() {
        updateFilterNPAText();
        updatePfContainers();
    }

    private void updateFilterNPAText() {
        int size = filterNPA.size();
        if (size == 0) filterNPAText = "Нет ни одной НПА!";
        else if (size == 1) filterNPAText = filterNPA.get(0).getNameRu();
        else if (size == npaList.size()) filterNPAText = "Все";
        else filterNPAText = "Выделено (" + size + ")";
    }

    public void resetFilterNPA() {
        filterNPA = new ArrayList<RefNpaItem>();
        RefNpaItem item = new RefNpaItem();
        item.setId(0L);
        item.setRecId(0L);
        item.setNameRu("Без подвязки НПА");
        item.setCode("0");
        npaList.add(item);
        filterNPA.addAll(npaList);

        updateFilterNPAText();
    }

    public void resetFilterFormRespondents() {
        formRespondents = new ArrayList<RefRespondentItem>();
        for (TreeNode stNode : respondentTree.getChildren()) {
            RefSubjectTypeItem st = (RefSubjectTypeItem) stNode.getData();
            if (selectedSubjectTypeRecId == null || selectedSubjectTypeRecId == 0 || selectedSubjectTypeRecId.longValue() == st.getRecId()) {
                for (TreeNode respNode : stNode.getChildren()) {
                    if (respNode.isSelected()) {
                        PermissionRespondent permissionRespondent = (PermissionRespondent) respNode.getData();
                        formRespondents.add(permissionRespondent.getRespondent());
                    }
                }
            }
        }

        filterFormRespondents.clear();
        filterFormRespondents.addAll(formRespondents);
        filterFormRespondentsText = "Все";
    }

    public String getActiveItemsCount(PermissionFormContainer pfContainer, String permissionName) {
        if (!isInputForms()) return "";
        boolean active = pfContainer.getValue(permissionName);
        if (!active) return "";
        if (filterFormRespondents.size() > pfContainer.getActiveCount(permissionName)) {
            return pfContainer.getActiveCount(permissionName) + "/" + filterFormRespondents.size();
        }
        return "";
    }

    public boolean isInputForms(){
        return selectedFormTypeCode != null
                && (selectedFormTypeCode.equals(Form.Type.INPUT.name()) || selectedFormTypeCode.equals(Form.Type.INPUT_RAW.name()));
    }

    public boolean showRespondentsFilter() {
        return showRespondentsFilter && isInputForms();
    }

    // region Getter and Setter

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setAdmBean(AdmBean admBean) {
        this.admBean = admBean;
    }

    public long getSelectedUserId() {
        return selectedUserId;
    }

    public void setSelectedUserId(long selectedUserId) {
        this.selectedUserId = selectedUserId;
    }

    public long getSelectedUserGroupId() {
        return selectedUserGroupId;
    }

    public void setSelectedUserGroupId(long selectedUserGroupId) {
        this.selectedUserGroupId = selectedUserGroupId;
    }

    public TreeNode getPermissionTree() {
        return permissionTree;
    }

    public void setPermissionTree(TreeNode permissionTree) {
        this.permissionTree = permissionTree;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public TreeNode[] getSelectedPermissions() {
        return selectedPermissions;
    }

    public void setSelectedPermissions(TreeNode[] selectedPermissions) {
        this.selectedPermissions = selectedPermissions;
    }

    public List<RefSubjectTypeItem> getSubjectTypes() {
        return subjectTypes;
    }

    public void setSubjectTypes(List<RefSubjectTypeItem> subjectTypes) {
        this.subjectTypes = subjectTypes;
    }

    public List<PermissionDepartment> getDepartments() {
        return departments;
    }

    public void setDepartments(List<PermissionDepartment> departments) {
        this.departments = departments;
    }

    public PermissionDepartment getSelectedDepartment() {
        return selectedDepartment;
    }

    public void setSelectedDepartment(PermissionDepartment selectedDepartment) {
        this.selectedDepartment = selectedDepartment;
    }

    public List<PermissionSubjectType> getPermissionSubjectTypes() {
        return permissionSubjectTypes;
    }

    public void setPermissionSubjectTypes(List<PermissionSubjectType> permissionSubjectTypes) {
        this.permissionSubjectTypes = permissionSubjectTypes;
    }

    public PermissionSubjectType getSelectedPermissionSubjectType() {
        return selectedPermissionSubjectType;
    }

    public void setSelectedPermissionSubjectType(PermissionSubjectType selectedPermissionSubjectType) {
        this.selectedPermissionSubjectType = selectedPermissionSubjectType;
    }

    public TreeNode getRespondentTree() {
        return respondentTree;
    }

    public void setRespondentTree(TreeNode respondentTree) {
        this.respondentTree = respondentTree;
    }

    public List<PermissionRespondent> getRespondents() {
        return respondents;
    }

    public void setRespondents(List<PermissionRespondent> respondents) {
        this.respondents = respondents;
    }

    public TreeNode[] getSelectedRespondents() {
        return selectedRespondents;
    }

    public void setSelectedRespondents(TreeNode[] selectedRespondents) {
        this.selectedRespondents = selectedRespondents;
    }

    public List<PermissionFormContainer> getPfContainers() {
        return pfContainers;
    }

    public void setPfContainers(List<PermissionFormContainer> pfContainers) {
        this.pfContainers = pfContainers;
    }

    public List<PermissionFormContainer> getFilteredPfContainers() {
        return filteredPfContainers;
    }

    public void setFilteredPfContainers(List<PermissionFormContainer> filteredPfContainers) {
        this.filteredPfContainers = filteredPfContainers;
    }

    public RefRespondentItem getSelectedRespondent() {
        return selectedRespondent;
    }

    public void setSelectedRespondent(RefRespondentItem selectedRespondent) {
        this.selectedRespondent = selectedRespondent;
    }

    public boolean isAllShowPermissionValue() {
        return allShowPermissionValue;
    }

    public void setAllShowPermissionValue(boolean allShowPermissionValue) {
        this.allShowPermissionValue = allShowPermissionValue;
    }

    public PermissionFormContainer getSelectedPfContainer() {
        return selectedPfContainer;
    }

    public void setSelectedPfContainer(PermissionFormContainer selectedPfContainer) {
        this.selectedPfContainer = selectedPfContainer;
    }

    public int getActiveTabIndex() {
        return activeTabIndex;
    }

    public void setActiveTabIndex(int activeTabIndex) {
        this.activeTabIndex = activeTabIndex;
    }

    public boolean isFormTab(){
        return activeTabIndex == 4;
    }

    public Long getSelectedSubjectTypeRecId() {
        return selectedSubjectTypeRecId;
    }

    public void setSelectedSubjectTypeRecId(Long selectedSubjectTypeRecId) {
        this.selectedSubjectTypeRecId = selectedSubjectTypeRecId;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public boolean isShowPermission() {
        return showPermission;
    }

    public void setShowPermission(boolean showPermission) {
        this.showPermission = showPermission;
    }

    public boolean isSignPermission() {
        return signPermission;
    }

    public void setSignPermission(boolean signPermission) {
        this.signPermission = signPermission;
    }

    public boolean isDisapprovePermission() {
        return disapprovePermission;
    }

    public void setDisapprovePermission(boolean disapprovePermission) {
        this.disapprovePermission = disapprovePermission;
    }

    public boolean isApprovePermission() {
        return approvePermission;
    }

    public void setApprovePermission(boolean approvePermission) {
        this.approvePermission = approvePermission;
    }

    public boolean isDeletePermission() {
        return deletePermission;
    }

    public void setDeletePermission(boolean deletePermission) {
        this.deletePermission = deletePermission;
    }

    public boolean isEditPermission() {
        return editPermission;
    }

    public void setEditPermission(boolean editPermission) {
        this.editPermission = editPermission;
    }

    public LazyDataModel<PermissionFormContainer> getLdmPfContainer() {
        return ldmPfContainer;
    }

    public DataTable getPfContainerDataTable() {
        return pfContainerDataTable;
    }

    public void setPfContainerDataTable(DataTable pfContainerDataTable) {
        this.pfContainerDataTable = pfContainerDataTable;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public Map<String, String> getFormTypeCodesMap() {
        return formTypeCodesMap;
    }

    public void setFormTypeCodesMap(Map<String, String> formTypeCodesMap) {
        this.formTypeCodesMap = formTypeCodesMap;
    }

    public String getSelectedFormTypeCode() {
        return selectedFormTypeCode;
    }

    public void setSelectedFormTypeCode(String selectedFormTypeCode) {
        this.selectedFormTypeCode = selectedFormTypeCode;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public Map<String, Long> getSubjectTypesInvariantMap() {
        return subjectTypesInvariantMap;
    }

    public boolean isShowDepartmentTab() {
        return isShowDepartmentTab;
    }

    public boolean isShowSubjectTypesTab() {
        return isShowSubjectTypesTab;
    }

    public boolean isShowRespondentsTab() {
        return isShowRespondentsTab;
    }

    public boolean isShowFormsTab() {
        return isShowFormsTab;
    }

    public boolean isShowPermissionEdit() {
        return isShowPermissionEdit;
    }

    public boolean isShowPermissionDelete() {
        return isShowPermissionDelete;
    }

    public boolean isShowPermissionApprove() {
        return isShowPermissionApprove;
    }

    public boolean isShowPermissionDisapprove() {
        return isShowPermissionDisapprove;
    }

    public List<RefRespondentItem> getFormRespondents() {
        return formRespondents;
    }

    public String getFilterFormRespondentsText() {
        return filterFormRespondentsText;
    }

    public List<RefRespondentItem> getFilterFormRespondents() {
        return filterFormRespondents;
    }

    public void setFilterFormRespondents(List<RefRespondentItem> filterFormRespondents) {
        this.filterFormRespondents = filterFormRespondents;
    }

    public String getCurrentTabId() {
        return currentTabId;
    }

    public void setCurrentTabId(String currentTabId) {
        this.currentTabId = currentTabId;
    }

    public boolean isShowRespondentsFilter() {
        return showRespondentsFilter;
    }

    public List<RefNpaItem> getFilterNPA() {
        return filterNPA;
    }

    public void setFilterNPA(List<RefNpaItem> filterNPA) {
        this.filterNPA = filterNPA;
    }

    public List<RefNpaItem> getNpaList() {
        return npaList;
    }

    public void setNpaList(List<RefNpaItem> npaList) {
        this.npaList = npaList;
    }

    public String getFilterNPAText() {
        return filterNPAText;
    }

    public void setFilterNPAText(String filterNPAText) {
        this.filterNPAText = filterNPAText;
    }

    // endregion
}
