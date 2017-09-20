package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Baurzhan.Baisholakov on 09.09.2016.
 */
// TODO Есть еще другой класс для то жей цели что и данный класс, нужно их объединить
public class CompoundReportKey  implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idn;
    private Date reportDate;
    private String formCode;

    public CompoundReportKey(String idn, Date reportDate, String formCode) {
        this.idn = idn;
        this.reportDate = reportDate;
        this.formCode = formCode;
    }

    public String getIdn() {
        return idn;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public String getFormCode() {
        return formCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompoundReportKey that = (CompoundReportKey) o;

        if (!idn.equals(that.idn)) return false;
        if (!reportDate.equals(that.reportDate)) return false;
        return formCode.equals(that.formCode);

    }

    @Override
    public int hashCode() {
        int result = idn.hashCode();
        result = 31 * result + reportDate.hashCode();
        result = 31 * result + formCode.hashCode();
        return result;
    }
}
