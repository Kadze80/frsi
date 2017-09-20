package entities;

import java.io.Serializable;

public class RefLoadListItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String sourceCode;
    private String sourceNameEn;
    private String sourceNameKz;
    private String sourceNameRu;
    private String code;
    private String nameEn;
    private String nameKz;
    private String nameRu;

    public RefLoadListItem(long id, String sourceCode, String sourceNameEn, String sourceNameKz, String sourceNameRu, String code, String nameEn, String nameKz, String nameRu) {
        this.id = id;
        this.sourceCode = sourceCode;
        this.sourceNameEn = sourceNameEn;
        this.sourceNameKz = sourceNameKz;
        this.sourceNameRu = sourceNameRu;
        this.code = code;
        this.nameEn = nameEn;
        this.nameKz = nameKz;
        this.nameRu = nameRu;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getSourceNameEn() {
        return sourceNameEn;
    }

    public void setSourceNameEn(String sourceNameEn) {
        this.sourceNameEn = sourceNameEn;
    }

    public String getSourceNameKz() {
        return sourceNameKz;
    }

    public void setSourceNameKz(String sourceNameKz) {
        this.sourceNameKz = sourceNameKz;
    }

    public String getSourceNameRu() {
        return sourceNameRu;
    }

    public void setSourceNameRu(String sourceNameRu) {
        this.sourceNameRu = sourceNameRu;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameKz() {
        return nameKz;
    }

    public void setNameKz(String nameKz) {
        this.nameKz = nameKz;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }
}
