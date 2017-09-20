package entities;

import java.util.Date;

/**
 * Created by Marat.Madybayev on 15.10.2014.
 */
public class Guide_CrossCheckBean {
	private int id;
	private Date report_date;
	private String internal_formula;
	private String external_formula;
	private String condition;
	private String expression;
	private String descr_rus;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getReport_date() {
		return report_date;
	}

	public void setReport_date(Date report_date) {
		this.report_date = report_date;
	}

	public String getInternal_formula() {
		return internal_formula;
	}

	public void setInternal_formula(String internal_formula) {
		this.internal_formula = internal_formula;
	}

	public String getExternal_formula() {
		return external_formula;
	}

	public void setExternal_formula(String external_formula) {
		this.external_formula = external_formula;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

    public String getDescr_rus() {
        return descr_rus;
    }

    public void setDescr_rus(String descr_rus) {
        this.descr_rus = descr_rus;
    }
}
