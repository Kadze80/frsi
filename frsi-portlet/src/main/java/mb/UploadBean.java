package mb;

import entities.*;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import util.Convert;
import util.Util;
import util.Validators;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@ManagedBean
@SessionScoped
public class UploadBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;
    @ManagedProperty(value = "#{reportsBean}")
    private ReportsBean reportsBean;

    private Date reportDate;
    private String resultMessage;

    private Boolean haveWarrant;

    private Long selectedRespId;
    private List<RefRespondentItem> respList;

    private enum FileType { UNKNOWN, XML, XLS, XLSX, ZIP };

    private Map<Long, Map<String, String>> cacheRefValues;

    @PostConstruct
    public void init() {
        try {
            if (sessionBean.isEjbNull()) sessionBean.init();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
            reportDate = cal.getTime();
            haveWarrant = sessionBean.getReference().respondentHaveWarrant(sessionBean.respondent.getRecId(), new Date());
            selectedRespId = sessionBean.respondent.getId();
            respList = sessionBean.getReference().getRespondentsWithWarrants(sessionBean.respondent.getRecId(),  new Date(), true);

        } catch (Exception e) { applicationBean.redirectToErrorPage(e); }
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

    public void onDateSelect() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Date Selected", format.format(reportDate)));
    }

    public void onRespSelect(){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Respondent Selected", reportsBean.getRespItem(selectedRespId, respList).getNameRu()));
    }

    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile uploadedFile = event.getFile();
        String fileMime = uploadedFile.getContentType();
        byte[] contents = uploadedFile.getContents();
        if (contents == null) return;

        cacheRefValues = new HashMap<Long, Map<String, String>>();

        RefRespondentItem respondentItem;

        if(haveWarrant) {
            respondentItem = reportsBean.getRespItem(selectedRespId, respList);
        }else{
            respondentItem = sessionBean.respondent;
        }

        String msg = "Success";
        FileType fileType = getFileTypeByMime(fileMime);
        try {
            switch (fileType) {
                case XML:
                    handleXmlFile(contents, respondentItem);
                    break;
                case XLS:
                case XLSX:
                    handleExcelFile(contents, respondentItem);
                    break;
                case ZIP:
                    handleZipFile(contents, respondentItem);
                    break;
            }
        } catch (IOException e) {
            msg = "Ошибка загрузки файла";
            e.printStackTrace();
        } catch (Exception e) {
            msg = e.getMessage();
            e.printStackTrace();
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (msg.equals("Success")) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Файл успешно загружен", msg));
        } else {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Загрузка файла", msg));
        }
    }

    private FileType getFileTypeByMime(String mime) {
        if (mime.equals("text/xml"))
            return FileType.XML;
        if (mime.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            return FileType.XLSX;
        if (mime.startsWith("application/") && (mime.contains("xls") || mime.contains("excel") || mime.contains("ms-office")))
            return FileType.XLS;
        if (mime.startsWith("application/") && (mime.contains("zip") || mime.contains("octet-stream")))
            return FileType.ZIP;

        return FileType.UNKNOWN;
    }

    private FileType getFileTypeByExt(String ext) {
        String lext = ext.toLowerCase();
        if (lext.equals("xml"))
            return FileType.XML;
        if (lext.equals("xls"))
            return FileType.XLS;
        if (lext.equals("xlsx") || lext.equals("xlsm"))
            return FileType.XLSX;
        if (lext.equals("zip"))
            return FileType.ZIP;

        return FileType.UNKNOWN;
    }

    private void handleXmlFile(byte[] bytes, RefRespondentItem respondentItem) throws Exception {

        if (reportDate == null) {
            throw new Exception("Не указана дата отчета");
        }

        if ((respondentItem.getIdn() == null) || (respondentItem.getIdn().equals(""))) {
            if(sessionBean.respondent.getId().equals(respondentItem.getId())) {
                throw new Exception("Не указан БИН организации");
            }else {
                throw new Exception("Не указан БИН доверенной организации");
            }
        }

        Report report = null;
        try {
            report = sessionBean.getPersistence().getReportFromXml(bytes, sessionBean.languageCode, reportDate);
        } catch (Exception e) {
            throw new Exception("Ошибка обработки XML-файла " + e.getMessage());
        }

        userBean.checkFormAccess(report.getFormCode(), "F:EDIT", respondentItem.getIdn());

        if (sessionBean.getPersistence().getFormId(report.getFormCode(), reportDate) == null){
            throw new Exception("Форма с кодом " + report.getFormCode() + " не найдена");
        }

        Form form = sessionBean.getPersistence().getFormsByCodeLanguageReportDate(report.getFormCode(), "ru", reportDate, null).get(0);
        if (!form.getTypeCode().equals(Form.Type.INPUT.name())) {
            throw new Exception("Загружаемый отчет должен быть входным отчетом");
        }

        if(!sessionBean.getPersistence().checkPeriod(reportDate,report.getFormCode(),respondentItem.getRefSubjectTypeRecId())) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            String formName = sessionBean.getPersistence().getFormNameByFormCodeLanguageCodeReportDate(report.getFormCode(),sessionBean.languageCode,reportDate);
            throw new Exception(MessageFormat.format("Невозможно создать отчет {0} за {1}", formName, dateFormat.format(reportDate)));
        }

        report.setIdn(respondentItem.getIdn());
        if(!sessionBean.respondent.getId().equals(respondentItem.getId())) {
            report.setIdnChild(respondentItem.getIdn());
        }
        report.setReportDate(reportDate);
        report.setId(sessionBean.getPersistence().getReportId(respondentItem.getIdn(), reportDate, report.getFormCode()));

        if (report.getId() != null) {
            if (sessionBean.getPersistence().getLastReportStatusByReportId(report.getId(), false, null).getStatusCode().equals("APPROVED"))
                throw new Exception("Отчет уже утвержден, загрузка невозможна!");
        }

        Map<String,String> inputValues = report.getReportHistory().get(report.getReportHistory().size() - 1).getKvMap();

        fillAutoFillFieldsData(report.getFormCode(), inputValues);

        inputValues = sessionBean.getPersistence().executeValueFunctions(inputValues, report.getFormCode(), reportDate);

        sessionBean.getPersistence().saveAndGetId("XML", report, respondentItem.getRecId(), form.getId(), sessionBean.abstractUser, sessionBean.getIntegration().getNewDateFromBackEndServer(), inputValues, null, false);
    }

    private void handleExcelFile(byte[] bytes, RefRespondentItem refRespondentItem) throws Exception {

        if (reportDate == null) {
            throw new Exception("Не указана дата отчета");
        }

        if ((refRespondentItem.getIdn() == null) || (refRespondentItem.getIdn().equals(""))) {
            if(sessionBean.respondent.getId().equals(refRespondentItem.getId())) {
                throw new Exception("Не указан БИН организации");
            }else {
                throw new Exception("Не указан БИН доверенной организации");
            }
        }

        ExcelData ee = sessionBean.getPersistence().extractExcelData(bytes, reportDate);

        userBean.checkFormAccess(ee.getFormName(), "F:EDIT", refRespondentItem.getIdn());

        if (sessionBean.getPersistence().getFormId(ee.getFormName(), reportDate) == null) {
            throw new Exception("Форма с кодом " + ee.getFormName() + " не найдена");
        }

        Form form = sessionBean.getPersistence().getFormsByCodeLanguageReportDate(ee.getFormName(), "ru", reportDate, null).get(0);
        if (!form.getTypeCode().equals(Form.Type.INPUT.name()))
            throw new Exception("Загружаемый отчет должен быть входным отчетом");

        /*if (((ee.getExcelForm().getXlsVersion() == null || ee.getExcelForm().getXlsVersion() == 0)
                && form.getFormHistory().getXlsVersion() > 1)
                || (ee.getExcelForm().getXlsVersion() != null && ee.getExcelForm().getXlsVersion() != 0
                && !ee.getExcelForm().getXlsVersion().equals(form.getFormHistory().getXlsVersion()))) {
            throw new Exception(MessageFormat.format("Не совпадает версия шаблона. Текущая версия - {0}", form.getFormHistory().getXlsVersion().toString()));
        }*/

        if (!Validators.isValidXlsVersion(ee.getExcelForm().getXlsVersion(),form.getFormHistory().getXlsVersion(), ee.getExcelForm().getBeginDate() == null ? null : Convert.getDateFromString(ee.getExcelForm().getBeginDate()), form.getFormHistory().getBeginDate())) {
            throw new Exception(MessageFormat.format("Не совпадает версия и/или дата шаблона. Текущая версия - {0}, дата - {1}", form.getFormHistory().getXlsVersion().toString(), Convert.getDateStringFromDate(form.getFormHistory().getBeginDate())));
        }

        Report report = new Report();
        report.setIdn(sessionBean.respondent == null ? null : sessionBean.respondent.getIdn());
        if(!sessionBean.respondent.getId().equals(refRespondentItem.getId())) {
            report.setIdnChild(refRespondentItem.getIdn());
        }
        report.setReportDate(reportDate);
        report.setFormCode(ee.getFormName());
        report.setId(sessionBean.getPersistence().getReportId(refRespondentItem.getIdn(), reportDate, ee.getFormName()));

        if (!sessionBean.getPersistence().checkPeriod(reportDate, report.getFormCode(), refRespondentItem.getRefSubjectTypeRecId())) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            String formName = sessionBean.getPersistence().getFormNameByFormCodeLanguageCodeReportDate(report.getFormCode(), sessionBean.languageCode, reportDate);
            throw new Exception(MessageFormat.format("Невозможно создать отчет {0} за {1}", formName, dateFormat.format(reportDate)));
        }

        if (report.getId() != null) {
            // TODO нужно проверить на статусы
            if (sessionBean.getPersistence().getLastReportStatusByReportId(report.getId(), false, null).getStatusCode().equals("APPROVED"))
                throw new Exception("Отчет уже утвержден, загрузка невозможна!");
        }

        Map<String,String> inputValues = ee.getInputValues();

        // TODO нужно в будущем перенести в метод saveAndGetId
        fillAutoFillFieldsData(ee.getFormName(), inputValues);

        inputValues = sessionBean.getPersistence().executeValueFunctions(inputValues, ee.getFormName(), reportDate);

        sessionBean.getPersistence().saveAndGetId("EXCEL", report, refRespondentItem.getRecId(), form.getId(), sessionBean.abstractUser, sessionBean.getIntegration().getNewDateFromBackEndServer(), inputValues, null, false);
    }

    private void handleZipFile(byte[] bytes, RefRespondentItem respondentItem) throws Exception {
        if (bytes == null) return;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            ZipInputStream zis = new ZipInputStream(bis);

            ZipEntry ze;
            ByteArrayOutputStream bos;
            byte[] buffer = new byte[1024];

            ze = zis.getNextEntry();
            while (ze != null) {
                bos = new ByteArrayOutputStream();
                int len = 0;
                while ((len = zis.read(buffer)) > 0) bos.write(buffer, 0, len);
                bos.close();

                String zeFileName = ze.getName();
                String zeFileExt = Util.getFileExtension(zeFileName);
                byte[] zeBytes = bos.toByteArray();

                FileType fileType = getFileTypeByExt(zeFileExt);
                switch (fileType) {
                    case XML:
                        handleXmlFile(zeBytes, respondentItem);
                        break;
                    case XLS:
                    case XLSX:
                        handleExcelFile(zeBytes, respondentItem);
                        break;
                }

                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillAutoFillFieldsData(String formCode, Map<String, String> keyValueMap){
        List<RefInfo> refInfos = getFormRefInfos(formCode);
        Map<String, String> newData = new HashMap<String, String>();
        for (RefInfo refInfo : refInfos) {
            Iterator<Map.Entry<String, String>> it = keyValueMap.entrySet().iterator();
            String key = refInfo.containerName + "*" + refInfo.pickFieldName + ":" + refInfo.keyFieldName + ":";
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                if (entry.getKey().startsWith(key)) {
                    String num = entry.getKey().substring(key.length());
                    String searchValue = entry.getValue();
                    if (searchValue != null) {
                        Long recId;
                        try {
                             recId = Long.parseLong(searchValue);
                        } catch (NumberFormatException e){
                            recId = -1L;
                        }
                        Map<String, String> rec = getRec(recId);

                        for (Map.Entry<String, String> dfEntry : refInfo.dependentFields.entrySet()) {
                            String dfKey = refInfo.containerName + "*" + dfEntry.getKey() + ":" + refInfo.keyFieldName + ":" + num;
                            String dfValue = rec.get(dfEntry.getValue());

                            if (dfValue != null) {
                                newData.put(dfKey, dfValue);
                            }
                        }
                    }
                } else {
                    for (Map.Entry<String, String> dfEntry : refInfo.dependentFields.entrySet()) {
                        String dfKey = refInfo.containerName + "*" + dfEntry.getKey() + ":" + refInfo.keyFieldName + ":";
                        if (entry.getKey().startsWith(dfKey)) {
                            it.remove();
                            break;
                        }
                    }
                }
            }
        }
        keyValueMap.putAll(newData);
    }

    private List<RefInfo> getFormRefInfos(String formCode){
        List<RefInfo> refInfos = new ArrayList<RefInfo>();

        if(formCode.equals("fs_sscb") || formCode.equals("fs_sscb_apk_kp")) {
            RefInfo refInfo = new RefInfo();
            refInfo.containerName = formCode+"_array";
            refInfo.keyFieldName = "num";
            refInfo.pickFieldName = "nin_iin";
            refInfo.pickerName = "securityinfo";

            refInfo.dependentFields.put("name_emitter", "issuer_name");
            refInfo.dependentFields.put("vid_cb", "variety_code");
            refInfo.dependentFields.put("date_maturity", "maturity_date");

            refInfos.add(refInfo);
        } else if(formCode.equals("fs_repo") || formCode.equals("fs_repo_apk_kp")){
            RefInfo refInfo = new RefInfo();
            refInfo.containerName = formCode + "_array";
            refInfo.keyFieldName = "num";
            refInfo.pickFieldName = "nin_iin";
            refInfo.pickerName = "securityinfo";

            refInfo.dependentFields.put("name_emitter", "issuer_name");
            refInfo.dependentFields.put("vid_cb", "variety_code");
            refInfos.add(refInfo);
        }

        return refInfos;
    }

    private Map<String, String> getRec(Long recId){
        if(!cacheRefValues.containsKey(recId)) {
            Map<String, String> rec = new HashMap<String, String>();
            List<RefSecuritiesItem> securities = sessionBean.getReference().getSecuritiesByRecId(sessionBean.getIntegration().getNewDateFromBackEndServer(), recId);
            if(securities.size()>0) {
                RefSecuritiesItem security = securities.get(0);
                rec.put("nin", security.getNin());
                rec.put("variety_code", security.getSgSecurityVariety() == null ? "" : String.valueOf(security.getSgSecurityVariety()));
                rec.put("variety_name", security.getVarietyName());
                rec.put("type_code", security.getSgSecurityType() == null ? "" : String.valueOf(security.getSgSecurityType()));
                rec.put("type_name", security.getTypeName());
                rec.put("nominal_value", String.valueOf(security.getNominalValue()));
                rec.put("currency_code", security.getCurrencyRecId() == null ? "" : String.valueOf(security.getCurrencyRecId()));
                rec.put("currency_name", security.getCurrencyName());
                rec.put("begin_date", security.getBeginDate() == null ? "" : Convert.getDateStringFromDate(security.getBeginDate()));
                rec.put("end_date", security.getEndDate() == null ? "" : Convert.getDateStringFromDate(security.getEndDate()));
                rec.put("maturity_date", security.getMaturityDate() == null ? "" : Convert.getDateStringFromDate(security.getMaturityDate()));

                rec.put("issuer_name", security.getsIssuer().toString());
                rec.put("issuer_sign_code", security.getSignCode());
                rec.put("issuer_sign_name", security.getSignName());
                rec.put("issuer_state", security.getIsState().toString());
                rec.put("issuer_resident", security.getIsResident().toString());
                rec.put("issuer_country_code", "");
                rec.put("issuer_country_name", "");
            }
            cacheRefValues.put(recId, rec);
        }
        return cacheRefValues.get(recId);
    }

    private class RefInfo {
        String pickerName;
        String containerName;
        String keyFieldName;
        String pickFieldName;
        Map<String, String> dependentFields = new HashMap<String, String>();
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

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    private String genInputName(String containerName, String name){
        return containerName + "*" + name + "::";
    }

    /**
     * Возвращает ФИО пользователя в сокращенном виде
     * @param user
     * @return
     */
    private String getUserFIO(PortalUser user){
        StringBuilder b = new StringBuilder();

        if (user.getLastName() != null)
            b.append(user.getLastName());
        else
            return b.toString();

        if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty())
            b.append(" " + user.getFirstName().trim().toUpperCase().substring(0, 1) + ".");
        else
            return b.toString();

        if (user.getMiddleName() != null && !user.getMiddleName().trim().isEmpty())
            b.append(" " + user.getMiddleName().trim().toUpperCase().substring(0, 1) + ".");

        return b.toString();
    }

    private void fillUserData(String containerName, Map<String,String> inputValues, List<InputValueCheck> inputValueChecks){
        RefRespondentItem respondentItem = reportsBean.getRespItem(selectedRespId, respList);
        Date curDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
        String ceoInputName = genInputName(containerName, "ceo");
        if (inputValues.get(ceoInputName) == null || inputValues.get(ceoInputName).trim().isEmpty())
            if (!inputValues.containsKey(ceoInputName)) {
                PortalUser user = sessionBean.getPersistence().getRespondentCeo(respondentItem.getRecId(), curDate);
                if (user != null)
                    inputValues.put(ceoInputName, getUserFIO(user));
            }

        String chiefAccountantInputName = genInputName(containerName, "chief_accountant");
        if (inputValues.get(chiefAccountantInputName) == null || inputValues.get(chiefAccountantInputName).trim().isEmpty()) {
            PortalUser user = sessionBean.getPersistence().getRespondentChiefAccountant(respondentItem.getRecId(), curDate);
            if (user != null)
                inputValues.put(chiefAccountantInputName, getUserFIO(user));
        }

        String draftedByInputName = genInputName(containerName, "drafted_by");
        if (inputValues.get(draftedByInputName) == null || inputValues.get(draftedByInputName).trim().isEmpty()) {
            PortalUser user = sessionBean.getPersistence().getRespondentDraftedBy(respondentItem.getRecId(), curDate);
            if (user != null)
                inputValues.put(draftedByInputName, getUserFIO(user));
        }
    }

    // region Getter and Setter


    public void setReportsBean(ReportsBean reportsBean) {
        this.reportsBean = reportsBean;
    }

    public Long getSelectedRespId() {
        return selectedRespId;
    }

    public void setSelectedRespId(Long selectedRespId) {
        this.selectedRespId = selectedRespId;
    }

    public List<RefRespondentItem> getRespList() {
        return respList;
    }

    public void setRespList(List<RefRespondentItem> respList) {
        this.respList = respList;
    }

    public Boolean getHaveWarrant() {
        return haveWarrant;
    }

    public void setHaveWarrant(Boolean haveWarrant) {
        this.haveWarrant = haveWarrant;
    }

    // endregion
}