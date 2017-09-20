package entities;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Marat.Madybayev on 06.12.2014.
 */
public class ReportsFormula implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private Date reportDate;
    private String formName;
    private String fieldName;
    private String formula;
    private int is_calc_other_field;
    private Double coeff;
    private String condition;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public int getIs_calc_other_field() {
        return is_calc_other_field;
    }

    public void setIs_calc_other_field(int is_calc_other_field) {
        this.is_calc_other_field = is_calc_other_field;
    }

    public Double getCoeff() {
        return coeff;
    }

    public void setCoeff(Double coeff) {
        this.coeff = coeff;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
