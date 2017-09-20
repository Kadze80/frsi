package entities;

/**
 * Created by Marat.Madybayev on 12.02.2015.
 */
public class SvodDinamicBean {
    String num;
    String key;
    String value;
    boolean numericValue;

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isNumericValue() {
        return numericValue;
    }

    public void setNumericValue(boolean numericValue) {
        this.numericValue = numericValue;
    }
}
