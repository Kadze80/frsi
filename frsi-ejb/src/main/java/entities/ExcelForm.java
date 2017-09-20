package entities;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Baurzhan.Baisholakov on 16.05.2015.
 */
public class ExcelForm implements Serializable {
    private static final long serialVersionUID = 1L;
    private String form;
    private int columnCount;
    private ExcelTable[] tables;
    @SerializedName("xls_version")
    private Integer xlsVersion;
    @SerializedName("begin_date")
    private String beginDate;

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public ExcelTable[] getTables() {
        return tables;
    }

    public ExcelTable getTable(int position) {
        return tables[position];
    }

    public void setTables(ExcelTable[] tables) {
        this.tables = tables;
    }

    public Integer getXlsVersion() {
        return xlsVersion;
    }

    public void setXlsVersion(Integer xlsVersion) {
        this.xlsVersion = xlsVersion;
    }

    public String getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(String beginDate) {
        this.beginDate = beginDate;
    }
}
