package entities;

import util.Convert;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;

/**
 * Entity
 *
 * @author Ardak Saduakassov
 */
public class ReportHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Report report;
    private Date saveDate;
    private String data;
    private Long dataSize;
    private String comments;
    private byte[] attachment;
    private Long attachmentSize;
    private String attachmentFileName;
    private String hashAlgorithm;
    private String hash;
    private String deliveryWayCode;
    private Long userId;
    private String userInfo;
    private Long suUserId;
    private String suUserInfo;
    private String suComments;
    private String note;
    private String controlResultCode;
    private String controlResultCode2;
    private Long isExistList;

    public ReportHistory () {
        hashAlgorithm = "MD5";
    }

    // non-persistable fields
    public Map<String,String> kvMap;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public Date getSaveDate() {
        return saveDate;
    }

    public void setSaveDate(Date saveDate) {
        this.saveDate = saveDate;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Long getDataSize() {
        return dataSize;
    }

    public void setDataSize(Long dataSize) {
        this.dataSize = dataSize;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public byte[] getAttachment() {
        return attachment;
    }

    public void setAttachment(byte[] attachment) {
        this.attachment = attachment;
    }

    public Long getAttachmentSize() {
        return attachmentSize;
    }

    public void setAttachmentSize(Long attachmentSize) {
        this.attachmentSize = attachmentSize;
    }

    public String getAttachmentFileName() {
        return attachmentFileName;
    }

    public void setAttachmentFileName(String attachmentFileName) {
        this.attachmentFileName = attachmentFileName;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void updateHash(String hashFiles) {
        updateHash(hashAlgorithm, hashFiles);
    }

    public void updateHash(String hashAlgorithm, String hashFiles) {
        StringBuilder sb = new StringBuilder();
        sb.append(report.getIdn()).append(Convert.getDateStringFromDate(report.getReportDate())).append(data);

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(hashAlgorithm);
            byte[] byteHash = messageDigest.digest(bytes);
            hash = (new HexBinaryAdapter()).marshal(byteHash);
            if(hashFiles != null)
                hash = hash + hashFiles;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            hash = null;
        }
    }

    public String getDeliveryWayCode() {
        return deliveryWayCode;
    }

    public void setDeliveryWayCode(String deliveryWayCode) {
        this.deliveryWayCode = deliveryWayCode;
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

    public Long getSuUserId() {
        return suUserId;
    }

    public void setSuUserId(Long suUserId) {
        this.suUserId = suUserId;
    }

    public String getSuUserInfo() {
        return suUserInfo;
    }

    public void setSuUserInfo(String suUserInfo) {
        this.suUserInfo = suUserInfo;
    }

    public String getSuComments() {
        return suComments;
    }

    public void setSuComments(String suComments) {
        this.suComments = suComments;
    }

    public Map<String, String> getKvMap() {
        return kvMap;
    }

    public void setKvMap(Map<String, String> kvMap) {
        this.kvMap = kvMap;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getControlResultCode() {
        return controlResultCode;
    }

    public void setControlResultCode(String controlResultCode) {
        this.controlResultCode = controlResultCode;
    }

    public String getControlResultCode2() {
        return controlResultCode2;
    }

    public void setControlResultCode2(String controlResultCode2) {
        this.controlResultCode2 = controlResultCode2;
    }

    public Long getIsExistList() {
        return isExistList;
    }

    public void setIsExistList(Long isExistList) {
        this.isExistList = isExistList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportHistory that = (ReportHistory) o;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
