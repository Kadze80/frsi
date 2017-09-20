package entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Entity
 *
 * @author Ardak Saduakassov
 */
public class Organization implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String bin;

    private String nameEn;
    private String nameKz;
    private String nameRu;
    private String shortNameEn;
    private String shortNameKz;
    private String shortNameRu;

    private Date beginDate;
    private Date endDate;

    private Organization parent;
    private List<Organization> affiliates;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
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

    public String getShortNameEn() {
        return shortNameEn;
    }

    public void setShortNameEn(String shortNameEn) {
        this.shortNameEn = shortNameEn;
    }

    public String getShortNameKz() {
        return shortNameKz;
    }

    public void setShortNameKz(String shortNameKz) {
        this.shortNameKz = shortNameKz;
    }

    public String getShortNameRu() {
        return shortNameRu;
    }

    public void setShortNameRu(String shortNameRu) {
        this.shortNameRu = shortNameRu;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Organization getParent() {
        return parent;
    }

    public void setParent(Organization parent) {
        this.parent = parent;
    }

    public List<Organization> getAffiliates() {
        return affiliates;
    }

    public void setAffiliates(List<Organization> affiliates) {
        this.affiliates = affiliates;
    }
}
