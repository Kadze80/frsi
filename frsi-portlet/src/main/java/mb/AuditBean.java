package mb;

import entities.*;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import util.OracleException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by Ayupov.Bakhtiyar on 24.11.2015.
 */
@ManagedBean
@SessionScoped
public class AuditBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    private Date filterEventDateBegin;
    private Date filterEventDateEnd;

    private List<AuditEvent> auditList;
    private AuditEvent selectedAudit;
    private TreeNode auditTree;
    private TreeNode selectedAuditTree;

    // Filters
    private List<RefRespondentItem> filterRespondents = new ArrayList<RefRespondentItem>();
    private List<RefRespondentItem> respondents = new ArrayList<RefRespondentItem>();
    private String filterRespondentsText;

    private List<RefElements> filterEventNames = new ArrayList<RefElements>();
    private List<RefElements> eventNames;
    private String filterEventNamesText;

    private List<RefElements> filterEventKinds = new ArrayList<RefElements>();
    private List<RefElements> eventKinds;
    private String filterEventKindsText;

    private String filterUserCode;
    private String filterCodeObject;
    private String filterNameObject;

    private Boolean isArchive;
    private boolean showDetails;

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

    @PostConstruct
    public void init() {
        Date dateStart = new Date();

        try {
            if (sessionBean.isEjbNull())
                sessionBean.init();

            filterEventDateBegin = sessionBean.getIntegration().getNewDateFromBackEndServer();
            filterEventDateEnd = sessionBean.getIntegration().getNewDateFromBackEndServer();
            filterEventDateBegin.setTime(filterEventDateBegin.getTime() - 3600 * 1000);
            filterEventDateEnd.setTime(filterEventDateEnd.getTime() + 3600 * 1000);

            resetFilterRespondents();
            resetFilterEventNames();
            resetFilterEventKinds();
            refreshAuditEvents();

        } catch (Exception e) {
            applicationBean.redirectToErrorPage(e);
        }

        Date dateEnd = new Date();
        long duration = dateEnd.getTime() - dateStart.getTime();
        logger.debug(MessageFormat.format("t: {0}, d: {1} ms", dateStart, duration));
    }

    public void refreshAuditEvents() {
        userBean.checkAccess("AUDIT:REFRESH");

        List<Long> RespondentList = new ArrayList<Long>();
        for (RefRespondentItem respondent : filterRespondents)
            RespondentList.add(respondent.getId());

        List<Long> EventNameList = new ArrayList<Long>();
        for (RefElements eventName : filterEventNames)
            EventNameList.add(eventName.getId());

        List<Long> EventKindList = new ArrayList<Long>();
        for (RefElements eventKind : filterEventKinds)
            EventKindList.add(eventKind.getId());

        auditList = sessionBean.getPersistence().getAuditEventList(filterEventDateBegin,filterEventDateEnd,RespondentList,
                filterUserCode,EventNameList, EventKindList,filterCodeObject,filterNameObject, false, sessionBean.portalUser.getUserId());

        auditTree = new DefaultTreeNode("Root", null);

        if(auditList.size() > 3000){
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_INFO, "Внимание",
                    "Результат поиска превысил 500 записей, укажите более точные данные для фильтра!"));
            return;
        }

        Map<Long, TreeNode> nodes = new HashMap<Long, TreeNode>();
        for (AuditEvent item : auditList) {
            if(nodes.get(item.getId()) != null)
                continue;
            TreeNode parentNode;
            if (item.getParentId() == null || item.getParentId() == 0)
                parentNode = auditTree;
            else
                parentNode = nodes.get(item.getParentId());
            TreeNode node;
            node = new DefaultTreeNode(item, parentNode);
            nodes.put(item.getId(), node);
        }
    }

    public void refreshSelectItem(){
        selectedAudit = (AuditEvent) selectedAuditTree.getData();
    }

    public void moveToArchive(Boolean isArchive) throws OracleException {
        List<Long> eventList = new ArrayList<Long>();
        for(AuditEvent auditEvent : auditList)
            eventList.add(auditEvent.getId());
        sessionBean.getPersistence().moveToFromArchive(eventList, isArchive);
    }

    private void resetFilterRespondents() {
        PortalUserGroup userGroup = sessionBean.portalUserGroup;

        respondents.clear();
        if (userGroup != null && userGroup.getRoleId() != null) {
            Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
            List<RefRespondentItem> respondentList = (List<RefRespondentItem>)sessionBean.getReference().getRefAbstractList(RefRespondentItem.REF_CODE, date);

            if (userGroup.getRoleId().equals(Constants.FRSI_ADMIN_ROLE)) {
                for (RefRespondentItem item : respondentList) {
                    respondents.add(item);
                }
            }
            else {
                if (userGroup.getRefSubjectTypeRecId() != null && userGroup.getRefSubjectTypeRecId() != 0L) {
                    RefRespondentItem NBRefRespondentItem = sessionBean.getReference().getRespondentByIdn(userBean.NB_BIN, date);
                    respondents.add(NBRefRespondentItem);
                }

                for (RefRespondentItem item : respondentList) {
                    if ((userGroup.getRefSubjectTypeRecId() == null || userGroup.getRefSubjectTypeRecId() == 0L)
                            || item.getRefSubjectTypeRecId().equals(userGroup.getRefSubjectTypeRecId())) {
                        if ((userGroup.getRefDepartmentRecId() == null || userGroup.getRefDepartmentRecId() == 0L)
                                || item.getRefDepartmentRecId().equals(userGroup.getRefDepartmentRecId())) {
                            respondents.add(item);
                        }
                    }
                }
            }
        }

        filterRespondents.clear();
        filterRespondents.addAll(respondents);

        updateFilterRespondentsText();
    }

    private void resetFilterEventNames() {
        eventNames = sessionBean.getReference().getRefElements("AE_NAME_EVENT", false);

        filterEventNames = new ArrayList<RefElements>();
        filterEventNames.addAll(eventNames);

        updateFilterEventNamesText();
    }

    private void resetFilterEventKinds() {
        eventKinds = sessionBean.getReference().getRefElements("AE_KIND_EVENT", false);

        filterEventKinds = new ArrayList<RefElements>();
        filterEventKinds.addAll(eventKinds);

        updateFilterEventKindsText();
    }

    public void clearFilters() {
        filterEventDateBegin = filterEventDateEnd = null;
        filterUserCode = null;
        filterCodeObject = null;
        filterNameObject = null;
        isArchive = false;
        resetFilterRespondents();
        resetFilterEventNames();
        resetFilterEventKinds();
    }

    public void onFiltersToggle() {
    }

    public void onDateSelect() {
        if (filterEventDateBegin == null && filterEventDateEnd != null) filterEventDateBegin = filterEventDateEnd;
        if (filterEventDateBegin != null && filterEventDateEnd == null) filterEventDateEnd = filterEventDateBegin;
    }

    public void onFilterRespondentsShow() {
    }

    public void onFilterEventNameShow() {
    }

    public void onFilterEventKindShow() {
    }

    public void onFilterRespondentsHide() {
        updateFilterRespondentsText();
    }

    public void onFilterEventNamesHide() {
        updateFilterEventNamesText();
    }

    public void onFilterEventKindsHide() {
        updateFilterEventKindsText();
    }

    private void updateFilterRespondentsText() {
        int size = filterRespondents.size();
        if (size == 0) filterRespondentsText = "Нет ни одного респондента!";
        else if (size == 1) filterRespondentsText = filterRespondents.get(0).getName(sessionBean.languageCode);
        else if (size == respondents.size()) filterRespondentsText = "Все";
        else filterRespondentsText = "Несколько респондентов (" + size + ")";
    }

    private void updateFilterEventNamesText() {
        int size = filterEventNames.size();
        if (size == 0) filterEventNamesText = "Нет ни одного аудируемого события!";
        else if (size == 1) filterEventNamesText = filterEventNames.get(0).getName();
        else if (size == eventNames.size()) filterEventNamesText = "Все";
        else filterEventNamesText = "Несколько аудируемых событий (" + size + ")";
    }

    private void updateFilterEventKindsText() {
        int size = filterEventKinds.size();
        if (size == 0) filterEventKindsText = "Нет ни одного вида аудируемого события!";
        else if (size == 1) filterEventKindsText = filterEventKinds.get(0).getName();
        else if (size == eventKinds.size()) filterEventKindsText = "Все";
        else filterEventKindsText = "Несколько виодов аудируемых событий (" + size + ")";
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

    public Date getFilterEventDateBegin() {
        return filterEventDateBegin;
    }

    public void setFilterEventDateBegin(Date filterEventDateBegin) {
        this.filterEventDateBegin = filterEventDateBegin;
    }

    public Date getFilterEventDateEnd() {
        return filterEventDateEnd;
    }

    public void setFilterEventDateEnd(Date filterEventDateEnd) {
        this.filterEventDateEnd = filterEventDateEnd;
    }

    public List<AuditEvent> getAuditList() {
        return auditList;
    }

    public void setAuditList(List<AuditEvent> auditList) {
        this.auditList = auditList;
    }

    public AuditEvent getSelectedAudit() {
        return selectedAudit;
    }

    public void setSelectedAudit(AuditEvent selectedAudit) {
        this.selectedAudit = selectedAudit;
    }

    public String getFilterRespondentsText() {
        return filterRespondentsText;
    }

    public void setFilterRespondentsText(String filterRespondentsText) {
        this.filterRespondentsText = filterRespondentsText;
    }

    public List<RefRespondentItem> getRespondents() {
        return respondents;
    }

    public void setRespondents(List<RefRespondentItem> respondents) {
        this.respondents = respondents;
    }

    public List<RefRespondentItem> getFilterRespondents() {
        return filterRespondents;
    }

    public void setFilterRespondents(List<RefRespondentItem> filterRespondents) {
        this.filterRespondents = filterRespondents;
    }

    public String getFilterUserCode() {
        return filterUserCode;
    }

    public void setFilterUserCode(String filterUserCode) {
        this.filterUserCode = filterUserCode;
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

    public List<RefElements> getFilterEventKinds() {
        return filterEventKinds;
    }

    public void setFilterEventKinds(List<RefElements> filterEventKinds) {
        this.filterEventKinds = filterEventKinds;
    }

    public List<RefElements> getEventKinds() {
        return eventKinds;
    }

    public void setEventKinds(List<RefElements> eventKinds) {
        this.eventKinds = eventKinds;
    }

    public String getFilterEventKindsText() {
        return filterEventKindsText;
    }

    public void setFilterEventKindsText(String filterEventKindsText) {
        this.filterEventKindsText = filterEventKindsText;
    }

    public String getFilterCodeObject() {
        return filterCodeObject;
    }

    public void setFilterCodeObject(String filterCodeObject) {
        this.filterCodeObject = filterCodeObject;
    }

    public String getFilterNameObject() {
        return filterNameObject;
    }

    public void setFilterNameObject(String filterNameObject) {
        this.filterNameObject = filterNameObject;
    }

    public Boolean getIsArchive() {
        return isArchive;
    }

    public void setIsArchive(Boolean isArchive) {
        this.isArchive = isArchive;
    }

    public TreeNode getAuditTree() {
        return auditTree;
    }

    public void setAuditTree(TreeNode auditTree) {
        this.auditTree = auditTree;
    }

    public TreeNode getSelectedAuditTree() {
        return selectedAuditTree;
    }

    public void setSelectedAuditTree(TreeNode selectedAuditTree) {
        this.selectedAuditTree = selectedAuditTree;
    }

    public boolean isShowDetails() {
        return showDetails;
    }

    public void setShowDetails(boolean showDetails) {
        this.showDetails = showDetails;
    }

    // endregion
}
