package entities;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Nuriddin.Baideuov on 15.07.2015.
 */
public class MultiSelectValue implements Serializable{
    private static final long serialVersionUID = 1L;

    List<String> values;

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
