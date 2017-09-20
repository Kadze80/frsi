package entities;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 02.03.2016.
 */
public class AttachedFile implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long linkId;
    private byte[] file;
    private byte[] pdfFile;
    private String fileType;
    private String fileName;
    private Date fileDate;
    private Long idUsr;
    private String userName;
    private int fileKind;
    private String hash;

    public void updateHash(){
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            byte[] byteHash = messageDigest.digest(file);
            hash = (new HexBinaryAdapter()).marshal(byteHash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            hash = null;
        }
    }

    // region Getter and Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getFileDate() {
        return fileDate;
    }

    public void setFileDate(Date fileDate) {
        this.fileDate = fileDate;
    }

    public Long getIdUsr() {
        return idUsr;
    }

    public void setIdUsr(Long idUsr) {
        this.idUsr = idUsr;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public byte[] getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(byte[] pdfFile) {
        this.pdfFile = pdfFile;
    }

    public int getFileKind() {
        return fileKind;
    }

    public void setFileKind(int fileKind) {
        this.fileKind = fileKind;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    // endregion
}
