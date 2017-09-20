package entities;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Nuriddin.Baideuov on 14.09.2015.
 */
public class ExcelData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String formName;
    private Map<String, String> inputValues;
    private final ExcelForm excelForm;

    public ExcelData(String formName, Map<String, String> inputValues, ExcelForm excelForm) {
        this.formName = formName;
        this.inputValues = inputValues;
        this.excelForm = excelForm;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public Map<String, String> getInputValues() {
        return inputValues;
    }

    public void setInputValues(Map<String, String> inputValues) {
        this.inputValues = inputValues;
    }

    public ExcelForm getExcelForm() {
        return excelForm;
    }
}
