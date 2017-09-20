package entities;

import java.io.Serializable;

/**
 * Entity
 *
 * @author Ardak Saduakassov
 */
public class NsiListItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long ddGuideId;

    private String code;
    private String nameEn;
    private String nameKz;
    private String nameRu;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDdGuideId() {
        return ddGuideId;
    }

    public void setDdGuideId(Long ddGuideId) {
        this.ddGuideId = ddGuideId;
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
