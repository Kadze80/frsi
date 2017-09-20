package entities;

import java.io.Serializable;
import java.util.*;

/**
 * Entity
 *
 * @author Ardak Saduakassov
 */
public class ReportStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status { DRAFT, SIGNED, ERROR, COMPLETED, APPROVED, DISAPPROVED }

    private Long id;
    private ReportHistory reportHistory;
    private String statusCode;
    private Date statusDate;
    private String message;
    private Long userId;
    private String userInfo;
    private String userLocation;
    private Long userWarrantId;
    private Long respWarrantId;
    private boolean haveUserWarrant;
    private boolean haveRespWarrant;

    public static List<Status> statuses = new ArrayList<Status>(Arrays.asList(Status.values()));
    public static final Map<String, Status> statusesMap;
    public static Map<String, String> resMap = new HashMap<String, String>();

    public static List<Status> suUserStatuses = new ArrayList<Status>(Arrays.asList(new Status[]{Status.COMPLETED, Status.APPROVED, Status.DISAPPROVED}));

    static {
        resMap.put("en_DRAFT", "Draft");
        resMap.put("en_SIGNED", "Signed");
        resMap.put("en_SENT", "Sent");
        resMap.put("en_WAITING", "Waiting");
        resMap.put("en_PROCESSING", "Processing");
        resMap.put("en_ERROR", "Error");
        resMap.put("en_COMPLETED", "Completed");
        resMap.put("en_APPROVED", "Approved");
        resMap.put("en_DISAPPROVED", "Disapproved");

        resMap.put("kz_DRAFT", "Алғашқы түрі");
        resMap.put("kz_SIGNED", "Қол қойылған");
        resMap.put("kz_SENT", "Жіберілді");
        resMap.put("kz_WAITING", "Кезекке түсті");
        resMap.put("kz_PROCESSING", "Өңдеу басталды");
        resMap.put("kz_ERROR", "Қате");
        resMap.put("kz_COMPLETED", "Өңдеу аяқталды");
        resMap.put("kz_APPROVED", "Бекітілді");
        resMap.put("kz_DISAPPROVED", "Бекіту жойылды");

        resMap.put("ru_DRAFT", "Черновик");
        resMap.put("ru_SIGNED", "Подписан");
        resMap.put("ru_SENT", "Отправлен");
        resMap.put("ru_WAITING", "Принят в очередь");
        resMap.put("ru_PROCESSING", "Начата обработка");
        resMap.put("ru_ERROR", "Ошибка");
        resMap.put("ru_COMPLETED", "Не утвержден");
        resMap.put("ru_APPROVED", "Утвержден");
        resMap.put("ru_DISAPPROVED", "Разутвержден");

        statusesMap = new HashMap<String, Status>();
        for(Status status:Status.values()){
            statusesMap.put(status.name(), status);
        }
    }

    public String getStatusName(String languageCode) {
        String result = resMap.get(languageCode + "_" + statusCode);
        return result == null ? "" : result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportStatus that = (ReportStatus) o;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReportHistory getReportHistory() {
        return reportHistory;
    }

    public void setReportHistory(ReportHistory reportHistory) {
        this.reportHistory = reportHistory;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public Date getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(Date statusDate) {
        this.statusDate = statusDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public Long getUserWarrantId() {
        return userWarrantId;
    }

    public void setUserWarrantId(Long userWarrantId) {
        this.userWarrantId = userWarrantId;
    }

    public Long getRespWarrantId() {
        return respWarrantId;
    }

    public void setRespWarrantId(Long respWarrantId) {
        this.respWarrantId = respWarrantId;
    }

    public boolean getHaveUserWarrant() {
        return haveUserWarrant;
    }

    public void setHaveUserWarrant(boolean haveUserWarrant) {
        this.haveUserWarrant = haveUserWarrant;
    }

    public boolean getHaveRespWarrant() {
        return haveRespWarrant;
    }

    public void setHaveRespWarrant(boolean haveRespWarrant) {
        this.haveRespWarrant = haveRespWarrant;
    }

    public static void main(String[] args){
        String str = "-01";
        try {
            double d = Double.parseDouble(str);
            System.out.println("FINE: "+Double.toString(d));
        } catch (NumberFormatException e){
            e.printStackTrace();
        }
    }
}
