package mb;

import entities.SettingsItem;
import org.apache.log4j.Logger;
import util.SettingsValueConverter;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
@SessionScoped
public class SettingsBean {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    private boolean showOverdueReportsSettingValue;

    private boolean enableNoticeOverDueDateSetting;
    private boolean noticeMailOverdueDateSettingValue;
    private boolean noticeSysOverdueDateSettingValue;
    private Integer daysBeforeOverdueDateSettingValue;

    @PostConstruct
    public void init() {
        try {
            if (sessionBean.isEjbNull())
                sessionBean.init();

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
            load();
        } catch (Exception e) {
            applicationBean.redirectToErrorPage(e);
        }
        /*RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('statusDialog').show()");*/
    }

    private void load() {
        SettingsItem showOverdueReportsSetting = sessionBean.getPersistence().getSettingsItemByTypeUserId(SettingsItem.Type.SHOW_OVERDUE_REPORTS, sessionBean.user.getUserId());
        if (showOverdueReportsSetting == null) {
            showOverdueReportsSettingValue = false;
        } else {
            showOverdueReportsSettingValue = SettingsValueConverter.fromRaw(showOverdueReportsSetting.getRawValue(), false, Boolean.class);
        }

        SettingsItem noticeMailOverdueDateSetting = sessionBean.getPersistence().getSettingsItemByTypeUserId(SettingsItem.Type.NOTICE_MAIL_OVERDUE_DATE, sessionBean.user.getUserId());
        if (noticeMailOverdueDateSetting == null) {
            noticeMailOverdueDateSettingValue = false;
        } else {
            noticeMailOverdueDateSettingValue = SettingsValueConverter.fromRaw(noticeMailOverdueDateSetting.getRawValue(), false, Boolean.class);
        }

        SettingsItem noticeSysOverdueDateSetting = sessionBean.getPersistence().getSettingsItemByTypeUserId(SettingsItem.Type.NOTICE_SYS_OVERDUE_DATE, sessionBean.user.getUserId());
        if (noticeSysOverdueDateSetting == null) {
            noticeSysOverdueDateSettingValue = false;
        } else {
            noticeSysOverdueDateSettingValue = SettingsValueConverter.fromRaw(noticeSysOverdueDateSetting.getRawValue(), false, Boolean.class);
        }

        enableNoticeOverDueDateSetting = noticeMailOverdueDateSetting != null || noticeSysOverdueDateSetting != null;

        SettingsItem daysBeforeOverdueDateSetting = sessionBean.getPersistence().getSettingsItemByTypeUserId(SettingsItem.Type.DAYS_BEFORE_OVERDUE_DATE, sessionBean.user.getUserId());
        if (daysBeforeOverdueDateSetting == null) {
            daysBeforeOverdueDateSettingValue = 0;
        } else {
            daysBeforeOverdueDateSettingValue = SettingsValueConverter.fromRaw(daysBeforeOverdueDateSetting.getRawValue(), sessionBean.getPersistence().getDaysBeforeOverdueDateSettingDefaultValue(), Integer.class);
        }
    }

    public void save() {
        List<SettingsItem> settingsItems = new ArrayList<SettingsItem>();
        SettingsItem showOverdueReportsSetting = new SettingsItem(SettingsItem.Type.SHOW_OVERDUE_REPORTS, sessionBean.user.getUserId());
        showOverdueReportsSetting.setRawValue(SettingsValueConverter.toRaw(showOverdueReportsSettingValue, false, Boolean.class));
        settingsItems.add(showOverdueReportsSetting);

        if (enableNoticeOverDueDateSetting) {
            SettingsItem noticeMailOverdueDateSetting = new SettingsItem(SettingsItem.Type.NOTICE_MAIL_OVERDUE_DATE, sessionBean.user.getUserId());
            noticeMailOverdueDateSetting.setRawValue(SettingsValueConverter.toRaw(noticeMailOverdueDateSettingValue, false, Boolean.class));
            settingsItems.add(noticeMailOverdueDateSetting);

            SettingsItem noticeSysOverdueDateSetting = new SettingsItem(SettingsItem.Type.NOTICE_SYS_OVERDUE_DATE, sessionBean.user.getUserId());
            noticeSysOverdueDateSetting.setRawValue(SettingsValueConverter.toRaw(noticeSysOverdueDateSettingValue, false, Boolean.class));
            settingsItems.add(noticeSysOverdueDateSetting);

            SettingsItem daysBeforeOverdueDateSetting = new SettingsItem(SettingsItem.Type.DAYS_BEFORE_OVERDUE_DATE, sessionBean.user.getUserId());
            daysBeforeOverdueDateSetting.setRawValue(SettingsValueConverter.toRaw(daysBeforeOverdueDateSettingValue, 0, Integer.class));
            settingsItems.add(daysBeforeOverdueDateSetting);
        }

        sessionBean.getPersistence().saveSettingsItems(settingsItems.toArray(new SettingsItem[settingsItems.size()]), sessionBean.user.getLoginIP());

        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Выполнено", "Системные настройки сохранены успешно"));
    }

    public void cancel() {
        load();
    }

    public void onChangeNoticeOverdueSettingSys() {
        if (!noticeMailOverdueDateSettingValue)
            daysBeforeOverdueDateSettingValue = 5;
    }

    public void onChangeNoticeOverdueSettingMail() {
        if (!noticeSysOverdueDateSettingValue)
            daysBeforeOverdueDateSettingValue = 5;
    }

    public boolean isDaysBeforeOverdueDateDisabled(){
        return !noticeMailOverdueDateSettingValue && !noticeSysOverdueDateSettingValue;
    }

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public boolean isShowOverdueReportsSettingValue() {
        return showOverdueReportsSettingValue;
    }

    public void setShowOverdueReportsSettingValue(boolean showOverdueReportsSettingValue) {
        this.showOverdueReportsSettingValue = showOverdueReportsSettingValue;
    }

    public boolean isNoticeSysOverdueDateSettingValue() {
        return noticeSysOverdueDateSettingValue;
    }

    public void setNoticeSysOverdueDateSettingValue(boolean noticeSysOverdueDateSettingValue) {
        this.noticeSysOverdueDateSettingValue = noticeSysOverdueDateSettingValue;
    }

    public boolean isNoticeMailOverdueDateSettingValue() {
        return noticeMailOverdueDateSettingValue;
    }

    public void setNoticeMailOverdueDateSettingValue(boolean noticeMailOverdueDateSettingValue) {
        this.noticeMailOverdueDateSettingValue = noticeMailOverdueDateSettingValue;
    }

    public Integer getDaysBeforeOverdueDateSettingValue() {
        return daysBeforeOverdueDateSettingValue;
    }

    public void setDaysBeforeOverdueDateSettingValue(Integer daysBeforeOverdueDateSettingValue) {
        this.daysBeforeOverdueDateSettingValue = daysBeforeOverdueDateSettingValue;
    }

    public boolean isEnableNoticeOverDueDateSetting() {
        return enableNoticeOverDueDateSetting;
    }
}
