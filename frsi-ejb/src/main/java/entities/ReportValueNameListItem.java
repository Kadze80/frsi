package entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ayupov.Bakhtiyar on 28.07.2016.
 */
public class ReportValueNameListItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String infoName;
    private String valueName;
    private long cnt;
    private long submitCnt;
    private long notSubmitCnt;
    private List<ReportListItem> reportListItems;
    private int rowNum;
    private int overdueReportsCount;
    private Map<String, ReportListItem> reportListItemMap = new HashMap<String, ReportListItem>();

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    public long getCnt() {
        return cnt;
    }

    public void setCnt(long cnt) {
        this.cnt = cnt;
    }

    public long getSubmitCnt() {
        return submitCnt;
    }

    public void setSubmitCnt(long submitCnt) {
        this.submitCnt = submitCnt;
    }

    public long getNotSubmitCnt() {
        return notSubmitCnt;
    }

    public void setNotSubmitCnt(long notSubmitCnt) {
        this.notSubmitCnt = notSubmitCnt;
    }

    public List<ReportListItem> getReportListItems() {
        return reportListItems;
    }

    public void setReportListItems(List<ReportListItem> reportListItems) {
        this.reportListItems = reportListItems;
    }

    public String getInfoName() {
        return infoName;
    }

    public void setInfoName(String infoName) {
        this.infoName = infoName;
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public int getOverdueReportsCount() {
        return overdueReportsCount;
    }

    public void setOverdueReportsCount(int overdueReportsCount) {
        this.overdueReportsCount = overdueReportsCount;
    }

    public void addReportListItem(ReportListItem item){
        reportListItemMap.put(item.getFormCode(), item);
    }

    public String getSubmitReportTextByFormCode(String formCode){
        if(reportListItemMap.containsKey(formCode)){
            return reportListItemMap.get(formCode).getSubmitReportText();
        } else {
            return "";
        }
    }
}
