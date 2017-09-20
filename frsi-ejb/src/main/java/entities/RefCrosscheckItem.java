package entities;

/**
 * Created by Ayupov.Bakhtiyar on 06.05.2015.
 */
public class RefCrosscheckItem extends AbstractReference {

    private String formula;
    private String formulaL; // release
    private String formulaR; // release
    private String formulaSymbol;
    private Long crosscheckType;
    private String crossTypeName;
    private String descrRus;
    private String descrRuL;
    private String descrRuR;
    private Long num;
    private Boolean isAvailable;
    private String condition;
    private String conditionL; // release
    private String conditionR; // release
    private String conditionSymbol;

    public String getFormula(){
        return formulaL + " " + formulaSymbol + " " + formulaR;
    }

    public String getDescrRus(){
        return descrRuL + " " + formulaSymbol + " " + descrRuR;
    }

    public String getCondition(){
        return conditionL + " " + conditionSymbol + " " + conditionR;
    }

    public String getFormulaL() {
        return formulaL;
    }

    public void setFormulaL(String formulaL) {
        this.formulaL = formulaL;
    }

    public String getFormulaR() {
        return formulaR;
    }

    public void setFormulaR(String formulaR) {
        this.formulaR = formulaR;
    }

    public String getFormulaSymbol() {
        return formulaSymbol;
    }

    public void setFormulaSymbol(String formulaSymbol) {
        this.formulaSymbol = formulaSymbol;
    }

    public String getCrossTypeName() {
        return crossTypeName;
    }

    public void setCrossTypeName(String crossTypeName) {
        this.crossTypeName = crossTypeName;
    }

    public String getDescrRuL() {
        return descrRuL;
    }

    public void setDescrRuL(String descrRuL) {
        this.descrRuL = descrRuL;
    }

    public String getDescrRuR() {
        return descrRuR;
    }

    public void setDescrRuR(String descrRuR) {
        this.descrRuR = descrRuR;
    }

    public Long getCrosscheckType() {
        return crosscheckType;
    }

    public void setCrosscheckType(Long crosscheckType) {
        this.crosscheckType = crosscheckType;
    }

    public Long getNum() {
        return num;
    }

    public void setNum(Long num) {
        this.num = num;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public String getConditionL() {
        return conditionL;
    }

    public void setConditionL(String conditionL) {
        this.conditionL = conditionL;
    }

    public String getConditionR() {
        return conditionR;
    }

    public void setConditionR(String conditionR) {
        this.conditionR = conditionR;
    }

    public String getConditionSymbol() {
        return conditionSymbol;
    }

    public void setConditionSymbol(String conditionSymbol) {
        this.conditionSymbol = conditionSymbol;
    }


}
