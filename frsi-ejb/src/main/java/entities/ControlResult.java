package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Nuriddin.Baideuov on 11.06.2015.
 */
public class ControlResult implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum ResultType{
        SUCCESS(1L, "Выполнено", "Орындалды", "Success"),
        FAIL(2L, "Не выполнено", "Орындалмады", "Fail"),
        ERROR(3L, "Синт. ошибка", "Синт. қате", "Synt. error"),
        NO_DATA(4L,"Нет данных","Мәлімет жоқ","No data");

        private Long id;
        private String nameKz;
        private String nameRu;
        private String nameEn;

        ResultType(Long id, String nameRu, String nameKz, String nameEn){
            this.id=id;
            this.nameRu = nameRu;
            this.nameKz = nameKz;
            this.nameEn = nameEn;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        public String getNameEn() {
            return nameEn;
        }

        public void setNameEn(String nameEn) {
            this.nameEn = nameEn;
        }

        public String getName(String languageCode) {
            if (languageCode.equals("kz")) return nameKz;
            else if (languageCode.equals("ru")) return nameRu;
            else return nameEn;
        }

    }

    private Long id;
    private Long crosscheckItemRecId;
    private Date reportDate;
    private String idn;
    private String descriptionRu;
    private Long resultType;
    private String errorMessage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCrosscheckItemRecId() {
        return crosscheckItemRecId;
    }

    public void setCrosscheckItemRecId(Long crosscheckItemRecId) {
        this.crosscheckItemRecId = crosscheckItemRecId;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    public String getDescriptionRu() {
        return descriptionRu;
    }

    public void setDescriptionRu(String descriptionRu) {
        this.descriptionRu = descriptionRu;
    }

    public Long getResultType() {
        return resultType;
    }

    public void setResultType(Long resultType) {
        this.resultType = resultType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "ControlResult{" +
                "id=" + id +
                ", crosscheckItemRecId=" + crosscheckItemRecId +
                ", reportDate=" + reportDate +
                ", idn='" + idn + '\'' +
                ", descriptionRu='" + descriptionRu + '\'' +
                ", resultType=" + resultType +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
