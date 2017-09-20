package entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nuriddin on 10/21/16.
 */
public class DataRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<Integer, Object> data = new HashMap<Integer, Object>();

    public void put(int colIndex, Object value) {
        data.put(colIndex, value);
    }

    public Object get(int colIndex) {
        return data.get(colIndex);
    }

    @Override
    public String toString() {
        return "DataRecord{" +
                "data=" + data +
                '}';
    }
}
