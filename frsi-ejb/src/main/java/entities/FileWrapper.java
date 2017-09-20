package entities;

import java.io.Serializable;
import java.util.zip.ZipEntry;

/**
 * Created by Nuriddin.Baideuov on 11.09.2015.
 */
public class FileWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    private byte[] bytes;

    private String fileFormat;
    private String fileName;

    public FileWrapper() {
    }

    public FileWrapper(byte[] bytes, String fileFormat) {
        this.bytes = bytes;
        this.fileFormat = fileFormat;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
