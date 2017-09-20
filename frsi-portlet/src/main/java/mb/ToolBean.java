package mb;

import entities.*;
import org.primefaces.context.RequestContext;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Managed bean
 *
 * @author Ardak Saduakassov
 */
@ManagedBean
@ViewScoped
public class ToolBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    private Date fromReportDate;
    private Date toReportDate;
    private String respondentBin;

    private List<RefRespondentItem> respondents;
    private RefRespondentItem selectedRespondent;

    private int testNumber;
    private List<Integer> testNumbers;

    private long reportId;
    private Date reportDate;
    private int copiedHistories;

    private long reportHistoryId;

    @PostConstruct
    public void init() {
        respondents = (List<RefRespondentItem>)sessionBean.getReference().getRefAbstractList(RefRespondentItem.REF_CODE, new Date());
    }

    public void onDialogRespondentsShow() {}

    public void onDialogRespondentsHide() {
        respondentBin = selectedRespondent == null ? null : selectedRespondent.getIdn();
    }

    public void setTimer(){
        sessionBean.getSchedule().setTimerService(null);
        RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_INFO, "Результат", "Время успешно установлено!"));
    }

    public void insertIntoReportRefLinkAll(){
        sessionBean.getPersistence().insertIntoReportRefLink();
    }

    public void insertPdfFile(){
        List<Report> allReports = sessionBean.getPersistence().getAllReports();

        for(Report item : allReports){
            reportId = item.getId();
            insertPdfFile(item.getId());
        }
    }

    public void insertPdfFile(long id){
        if(id != 0) {
            List<ReportHistory> reportHistoryList = sessionBean.getPersistence().getReportHistoryByReportIdNoLobs(reportId, null);
            for(ReportHistory reportHistory : reportHistoryList) {
                insPdfFile(sessionBean.getPersistence().getFileListByLinkId(reportHistory.getId(), 1, null));
            }
        }
    }

    private void insPdfFile(List<AttachedFile> items){
        if(items != null && items.size() > 0) {
            for (AttachedFile item : items) {
                if (item.getPdfFile() == null) {
                    String format = item.getFileName().substring(item.getFileName().lastIndexOf(".") + 1);
                    if (!format.equalsIgnoreCase("pdf")) {
                        byte[] file = sessionBean.getPersistence().getDataFile(item.getId(), false).getFile();
                        if(file != null) {
                            String fileName = item.getFileName().substring(0, item.getFileName().lastIndexOf("."));
                            FileWrapper pdfFile = new FileWrapper();
                            pdfFile.setBytes(file);
                            pdfFile.setFileFormat(format);
                            boolean havError = false;
                            try {
                                pdfFile = sessionBean.getPersistence().convertFileToPdf(pdfFile, fileName);
                            } catch (Exception e) {
                                havError = true;
                                RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
                                RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage() + " id_report_file = " + item.getId()));
                            }
                            item.setPdfFile(pdfFile.getBytes());
                            if (!havError) {
                                sessionBean.getPersistence().updatePdfFile(item);
                            }
                        }
                    }
                }
            }
        }
    }

    public void copyHistories(){
        copiedHistories = sessionBean.getPersistence().copyReportHistory();
    }

    public void searchNotice(){
        sessionBean.getSchedule().searchNotice();
        RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_INFO, "Результат", "Отправка завершена!"));
    }

    public void sendNotice(){
        //sessionBean.getSchedule().sendNotice();
    }

    // region Getters and setters
    public void saveToReportHistoryList(){
        sessionBean.getSchedule().saveToReportHistoryListAll();
    }

    // Getters and setters

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public Date getFromReportDate() {
        return fromReportDate;
    }

    public void setFromReportDate(Date fromReportDate) {
        this.fromReportDate = fromReportDate;
    }

    public Date getToReportDate() {
        return toReportDate;
    }

    public void setToReportDate(Date toReportDate) {
        this.toReportDate = toReportDate;
    }

    public String getRespondentBin() {
        return respondentBin;
    }

    public void setRespondentBin(String respondentBin) {
        this.respondentBin = respondentBin;
    }

    public List<RefRespondentItem> getRespondents() {
        return respondents;
    }

    public void setRespondents(List<RefRespondentItem> respondents) {
        this.respondents = respondents;
    }

    public RefRespondentItem getSelectedRespondent() {
        return selectedRespondent;
    }

    public void setSelectedRespondent(RefRespondentItem selectedRespondent) {
        this.selectedRespondent = selectedRespondent;
    }

    public void insertTestNumber(){
        sessionBean.getPersistence().testInsert(testNumber);
    }

    public void refreshTestNumbers(){
        testNumbers = sessionBean.getPersistence().getTestNumbers();
    }

    public int getTestNumber() {
        return testNumber;
    }

    public void setTestNumber(int testNumber) {
        this.testNumber = testNumber;
    }

    public List<Integer> getTestNumbers() {
        return testNumbers;
    }

    public void setTestNumbers(List<Integer> testNumbers) {
        this.testNumbers = testNumbers;
    }

    public long getReportId() {
        return reportId;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportId(long reportId) {
        this.reportId = reportId;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public int getCopiedHostories() {
        return copiedHistories;
    }

    // endregion

    public long getReportHistoryId() {
        return reportHistoryId;
    }

    public void setReportHistoryId(long reportHistoryId) {
        this.reportHistoryId = reportHistoryId;
    }
}
