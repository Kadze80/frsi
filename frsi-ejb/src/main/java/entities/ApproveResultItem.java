package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by nuriddin on 6/14/16.
 */
public class ApproveResultItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private long reportId;
    private String formName;
    private String respondentName;
    private Date reportDate;
    private String message;
    private ResultType resultType;

    public ApproveResultItem(long reportId, String formName, String respondentName, Date reportDate, String message) {
        this.reportId = reportId;
        this.formName = formName;
        this.respondentName = respondentName;
        this.reportDate = reportDate;
        this.message = message;
        resultType = ResultType.FAIL;
    }

    public ApproveResultItem(long reportId, String formName, String respondentName, Date reportDate, String message, ResultType resultType) {
        this.reportId = reportId;
        this.formName = formName;
        this.respondentName = respondentName;
        this.reportDate = reportDate;
        this.message = message;
        this.resultType = resultType;
    }

    public long getReportId() {
        return reportId;
    }

    public void setReportId(long reportId) {
        this.reportId = reportId;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getRespondentName() {
        return respondentName;
    }

    public void setRespondentName(String respondentName) {
        this.respondentName = respondentName;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }

    public enum ResultType {
        SUCCESS(1L, "Выполнено", "Орындалды", "Success"),
        FAIL(2L, "Не выполнено", "Орындалмады", "Fail");

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

        public String getNameKz() {
            return nameKz;
        }

        public String getNameRu() {
            return nameRu;
        }

        public String getNameEn() {
            return nameEn;
        }

        public String getName(String languageCode) {
            if (languageCode.equals("kz"))
                return nameKz;
            else if (languageCode.equals("en"))
                return nameEn;
            else
                return nameRu;
        }
    }
}
