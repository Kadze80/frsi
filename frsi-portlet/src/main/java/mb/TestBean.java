package mb;

import com.google.gson.reflect.TypeToken;
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
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;

/**
 * Тестовый бин. Делайте с ним всё, что хотите :)
 */
@ManagedBean
@SessionScoped
public class TestBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger("fileLogger");

	@ManagedProperty(value = "#{applicationBean}")
	private ApplicationBean applicationBean;
	@ManagedProperty(value = "#{sessionBean}")
	private SessionBean sessionBean;

    private String msg;
	private String idn;
	private Date reportDate;
	private String code;
	private String cntMsg;

	private String idnPick;
	private Date reportDatePick;
	private String codePick;
	private String tableNamePick;
	private String columnNamePick;

	private String selectedFormTypeCode;
	private Map<String, String> formTypeCodesMap;

	private String idn2;
	private Date reportDate2;
	private String code2;
	private String selectedFormTypeCode2;


	@PostConstruct
	public void init() {
		Date dateStart = new Date();

		try {
            if (sessionBean.isEjbNull()) sessionBean.init();
            msg = "initial value";
			updateFormTypeCodesMap();
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

	public void doSomething() {
        msg = sessionBean.getCore().getTestMessage(); // "Some message";
        sessionBean.getCore().throwTestException();
		//List<Form> forms = sessionBean.getPersistence().getFormsNoLob();
		//msg = "Forms count = " + (forms == null ? "NULL" : forms.size());
    }

	private void updateFormTypeCodesMap(){
		formTypeCodesMap = new HashMap<String, String>();
		for(Form.Type formType: Form.Type.values()){
			formTypeCodesMap.put(Form.resMap.get(sessionBean.languageCode + "_" + formType.name()), formType.name());
		}
	}

//	public void exportStatuses(){
//		sessionBean.getPersistence().exportCouchBaseStatuses();
//	}

	public String getApplet() {
		return "<applet code=\"com.example.TestApplet\" codebase=\"/frsi-portlet/lib\" archive=\"TestApplet.jar\" width=\"256\" height=\"256\" />";
	}

	public void doControlCalc() {
		cntMsg = " ";
		int i = 0;
		int n;
		List<ReportListItem> reportList = sessionBean.getPersistence().getReportListByIdnNoLobsV2(idn, reportDate, code, selectedFormTypeCode);

		n = reportList.size();

		for (ReportListItem reportListItem : reportList) {
			i++;
			Report report = sessionBean.getPersistence().getReport(reportListItem.getId(), null);
			if (report != null) {
				List<ReportHistory> reportHistory = sessionBean.getPersistence().getReportHistoryByReportNoLobs(report, null);
				if (reportHistory.isEmpty()) return;
				Long lastReportHistoryItemId = reportHistory.get(reportHistory.size() - 1).getId();
				ReportHistory lastReportHistoryItem = sessionBean.getPersistence().getReportHistory(lastReportHistoryItemId, true, false);

				Type typeMapStringString = new TypeToken<Map<String, String>>() {
				}.getType();
				Map<String, String> kvMap = applicationBean.gson.fromJson(lastReportHistoryItem.getData(), typeMapStringString);
				Map<String, String> kvMap_v2 = applicationBean.gson.fromJson(lastReportHistoryItem.getData(), typeMapStringString);

				kvMap_v2 = sessionBean.getPersistence().updateCalculatedFields(report.getFormCode(), report.getReportDate(), kvMap_v2, "ru", true);

				Set<String> updatedKeys = new HashSet<String>(kvMap.keySet());
				updatedKeys.retainAll(kvMap_v2.keySet());

				for (String key : updatedKeys) {
					String value1 = kvMap.get(key);
					String value2 = kvMap_v2.get(key);
					if ((value1 == null && value2 == null)
							|| (value1 != null && value2 != null && value1.equals(value2))) {
						kvMap.remove(key);
						kvMap_v2.remove(key);
					}
				}

				sessionBean.getPersistence().deleteTmpReportV2(report.getId());
				sessionBean.getPersistence().insertTmpReportV2(report.getId(), lastReportHistoryItem.getId());

				sessionBean.getPersistence().insertGrpTmpReportV2Dtl(report.getId(), 0L, kvMap);
				sessionBean.getPersistence().insertGrpTmpReportV2Dtl(report.getId(), 1L, kvMap_v2);
			}
		}

		cntMsg = "Количество обработанных форм " + i + " из " + n;

		/*FacesContext facesContext = FacesContext.getCurrentInstance();
		facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Fill result", cntMsg));*/
		RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_INFO, "Fill result", cntMsg));
	}

	public void recalculate(){
		cntMsg = " ";
		int i = 0;
		int n;
		List<ReportListItem> reportList = sessionBean.getPersistence().getReportListByIdnNoLobsV2(idn, reportDate, code, selectedFormTypeCode);

		n = reportList.size();

		for (ReportListItem reportListItem : reportList) {
			i++;
			Report report = sessionBean.getPersistence().getReport(reportListItem.getId(), null);
			if (report != null) {
				List<ReportHistory> reportHistory = sessionBean.getPersistence().getReportHistoryByReportNoLobs(report, null);
				if (reportHistory.isEmpty()) return;
				Long lastReportHistoryItemId = reportHistory.get(reportHistory.size() - 1).getId();
				ReportHistory lastReportHistoryItem = sessionBean.getPersistence().getReportHistory(lastReportHistoryItemId, true, false);

				Type typeMapStringString = new TypeToken<Map<String, String>>() {
				}.getType();
				Map<String, String> kvMap_v2 = applicationBean.gson.fromJson(lastReportHistoryItem.getData(), typeMapStringString);

				kvMap_v2 = sessionBean.getPersistence().updateCalculatedFields(report.getFormCode(), report.getReportDate(), kvMap_v2, "ru", true);

				String jsonData = applicationBean.gson.toJson(kvMap_v2);
				sessionBean.getPersistence().updateReportHistoryData(lastReportHistoryItemId, jsonData, null);
			}
		}

		cntMsg = "Количество обработанных форм " + i + " из " + n;

		FacesContext facesContext = FacesContext.getCurrentInstance();
		facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Fill result", cntMsg));
	}

	public void validateNumberFormat() {
		cntMsg = " ";
		int i = 0;
		int n;
		List<ReportListItem> reportList = sessionBean.getPersistence().getReportListByIdnNoLobsV2(idn2, reportDate2, code2, selectedFormTypeCode2);

		n = reportList.size();

		Map<Long, List<InputValueCheck>> inputValueCheckIndex = new HashMap<Long, List<InputValueCheck>>();

		for (ReportListItem reportListItem : reportList) {
			i++;
			Report report = sessionBean.getPersistence().getReport(reportListItem.getId(), null);
			if (report != null) {
				List<ReportHistory> reportHistory = sessionBean.getPersistence().getReportHistoryByReportNoLobs(report, null);
				if (reportHistory.isEmpty()) return;
				Long lastReportHistoryItemId = reportHistory.get(reportHistory.size() - 1).getId();
				ReportHistory lastReportHistoryItem = sessionBean.getPersistence().getReportHistory(lastReportHistoryItemId, true, false);

				Type typeMapStringString = new TypeToken<Map<String, String>>() {
				}.getType();
				Map<String, String> kvMap = applicationBean.gson.fromJson(lastReportHistoryItem.getData(), typeMapStringString);

				List<Form> forms = sessionBean.getPersistence().getFormsByCodeLanguageReportDate(report.getFormCode(), "ru", report.getReportDate(), null);
				Long formHistoryId = forms.get(0).getFormHistory().getId();
				List<InputValueCheck> inputValueChecks;
				if (!inputValueCheckIndex.containsKey(formHistoryId)) {
					FormHistory formHistory2 = sessionBean.getPersistence().getFormHistoryWithInputValueChecks(forms.get(0).getFormHistory().getId());
					Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {
					}.getType();
					inputValueChecks = applicationBean.gson.fromJson(formHistory2.getInputValueChecks(), typeListInputValueCheck);
					inputValueCheckIndex.put(formHistoryId, inputValueChecks);
				} else {
					inputValueChecks = inputValueCheckIndex.get(formHistoryId);
				}

				Map<String, String> kvMap_v2 = sessionBean.getPersistence().validateAndNormalizeDataFormat(kvMap, inputValueChecks, true);

				Set<String> updatedKeys = new HashSet<String>(kvMap.keySet());
				updatedKeys.retainAll(kvMap_v2.keySet());

				sessionBean.getPersistence().deleteTmpReportV2(report.getId());
				sessionBean.getPersistence().insertTmpReportV2(report.getId(), lastReportHistoryItem.getId());

				sessionBean.getPersistence().insertGrpTmpReportV2Dtl(report.getId(), 1L, kvMap_v2);
			}
		}

		cntMsg = "Количество обработанных форм " + i + " из " + n;

		RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_INFO, "Fill result", cntMsg));
	}

	public void checkTransformation() {
		cntMsg = " ";
		int i = 0;
		int n;
		List<ReportListItem> reportList = sessionBean.getPersistence().getReportListByIdnNoLobsV2(idnPick, reportDatePick, codePick, null);

		n = reportList.size();

		for (ReportListItem reportListItem : reportList) {
			i++;
			Report report = sessionBean.getPersistence().getReport(reportListItem.getId(), null);
			if (report != null) {
				List<ReportHistory> reportHistory = sessionBean.getPersistence().getReportHistoryByReportNoLobs(report, null);
				if (reportHistory.isEmpty()) return;
				Long lastReportHistoryItemId = reportHistory.get(reportHistory.size() - 1).getId();
				ReportHistory lastReportHistoryItem = sessionBean.getPersistence().getReportHistory(lastReportHistoryItemId, true, false);

				Type typeMapStringString = new TypeToken<Map<String, String>>() {
				}.getType();
				Map<String, String> kvMap = applicationBean.gson.fromJson(lastReportHistoryItem.getData(), typeMapStringString);
				String searchKey = report.getFormCode() + "_" + tableNamePick + "*" + columnNamePick + ":";
				Map<String, String> kvMapSerach = new HashMap<String, String>();
				for(Map.Entry<String, String> entry:kvMap.entrySet()){
					if(entry.getKey().startsWith(searchKey))
						kvMapSerach.put(entry.getKey(), entry.getValue());
				}


				Map<String, String> kvMapTransformed = sessionBean.getPersistence().transformRefCaptionToRecId(report.getFormCode(), report.getReportDate(), "ru", kvMapSerach, report.getIdn());

				kvMapSerach.keySet().retainAll(kvMapTransformed.keySet());

				sessionBean.getPersistence().deleteTmpReportV2(report.getId());
				sessionBean.getPersistence().insertTmpReportV2(report.getId(), lastReportHistoryItem.getId());

				sessionBean.getPersistence().insertGrpTmpReportV2Dtl(report.getId(), 0L, kvMapSerach);
				sessionBean.getPersistence().insertGrpTmpReportV2Dtl(report.getId(), 1L, kvMapTransformed);
			}
		}

		cntMsg = "Количество обработанных форм " + i + " из " + n;

		FacesContext facesContext = FacesContext.getCurrentInstance();
		facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Fill result", cntMsg));
	}

	public void doTransformation(){
		cntMsg = " ";
		int i = 0;
		int n;
		List<ReportListItem> reportList = sessionBean.getPersistence().getReportListByIdnNoLobsV2(idnPick, reportDatePick, codePick, null);

		n = reportList.size();

		for (ReportListItem reportListItem : reportList) {
			i++;
			Report report = sessionBean.getPersistence().getReport(reportListItem.getId(), null);
			if (report != null) {
				List<ReportHistory> reportHistory = sessionBean.getPersistence().getReportHistoryByReportNoLobs(report, null);
				if (reportHistory.isEmpty()) return;
				Long lastReportHistoryItemId = reportHistory.get(reportHistory.size() - 1).getId();
				ReportHistory lastReportHistoryItem = sessionBean.getPersistence().getReportHistory(lastReportHistoryItemId, true, false);

				Type typeMapStringString = new TypeToken<Map<String, String>>() {
				}.getType();
				Map<String, String> kvMap = applicationBean.gson.fromJson(lastReportHistoryItem.getData(), typeMapStringString);
				String searchKey = report.getFormCode() + "_" + tableNamePick + "*" + columnNamePick + ":";
				Map<String, String> kvMapSerach = new HashMap<String, String>();
				for(Map.Entry<String, String> entry:kvMap.entrySet()){
					if(entry.getKey().startsWith(searchKey))
						kvMapSerach.put(entry.getKey(), entry.getValue());
				}


				Map<String, String> kvMapTransformed = sessionBean.getPersistence().transformRefCaptionToRecId(report.getFormCode(), report.getReportDate(), "ru", kvMapSerach, report.getIdn());

				kvMap.putAll(kvMapTransformed);

				String jsonData = applicationBean.gson.toJson(kvMap);
				sessionBean.getPersistence().updateReportHistoryData(lastReportHistoryItemId, jsonData, null);
			}
		}

		cntMsg = "Количество обработанных форм " + i + " из " + n;

		FacesContext facesContext = FacesContext.getCurrentInstance();
		facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Fill result", cntMsg));
	}


	// region Getter and Setter

	public void setApplicationBean(ApplicationBean applicationBean) {
		this.applicationBean = applicationBean;
	}

	public void setSessionBean(SessionBean sessionBean) {
		this.sessionBean = sessionBean;
	}

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

	public String getIdn() {
		return this.idn;
	}

	public void setIdn(String idn) {
		this.idn = idn;
	}

	public Date getReportDate() {
		return reportDate;
	}

	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCntMsg() {
		return cntMsg;
	}

	public String getIdnPick() {
		return idnPick;
	}

	public void setIdnPick(String idnPick) {
		this.idnPick = idnPick;
	}

	public Date getReportDatePick() {
		return reportDatePick;
	}

	public void setReportDatePick(Date reportDatePick) {
		this.reportDatePick = reportDatePick;
	}

	public String getCodePick() {
		return codePick;
	}

	public void setCodePick(String codePick) {
		this.codePick = codePick;
	}

	public String getTableNamePick() {
		return tableNamePick;
	}

	public void setTableNamePick(String tableNamePick) {
		this.tableNamePick = tableNamePick;
	}

	public String getColumnNamePick() {
		return columnNamePick;
	}

	public void setColumnNamePick(String columnNamePick) {
		this.columnNamePick = columnNamePick;
	}

	public String getSelectedFormTypeCode() {
		return selectedFormTypeCode;
	}

	public void setSelectedFormTypeCode(String selectedFormTypeCode) {
		this.selectedFormTypeCode = selectedFormTypeCode;
	}

	public Map<String, String> getFormTypeCodesMap() {
		return formTypeCodesMap;
	}

	public String getIdn2() {
		return idn2;
	}

	public void setIdn2(String idn2) {
		this.idn2 = idn2;
	}

	public Date getReportDate2() {
		return reportDate2;
	}

	public void setReportDate2(Date reportDate2) {
		this.reportDate2 = reportDate2;
	}

	public String getCode2() {
		return code2;
	}

	public void setCode2(String code2) {
		this.code2 = code2;
	}

	public String getSelectedFormTypeCode2() {
		return selectedFormTypeCode2;
	}

	public void setSelectedFormTypeCode2(String selectedFormTypeCode2) {
		this.selectedFormTypeCode2 = selectedFormTypeCode2;
	}

	// endregion
}
