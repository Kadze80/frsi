package entities;

import java.io.Serializable;

/**
 * Created by Marat.Madybayev on 03.11.2014.
 */
public class PerformControlItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String control;
    private String result;

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
