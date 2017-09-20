package entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nuriddin on 6/2/16.
 */
public class InputSelectRecord implements Serializable {
    private long recId;
    private Map<String, Variant> data = new HashMap<String, Variant>();

    public InputSelectRecord(long recId) {
        this.recId = recId;
    }

    public long getRecId() {
        return recId;
    }

    public void put(String columnName, Variant value) {
        data.put(columnName, value);
    }

    public Variant get(String columnName) {
        return data.get(columnName);
    }

    public Map<String, Variant> getData() {
        return new HashMap<String, Variant>(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InputSelectRecord record = (InputSelectRecord) o;

        return recId == record.recId;

    }

    @Override
    public int hashCode() {
        return (int) (recId ^ (recId >>> 32));
    }

    @Override
    public String toString() {
        return "InputSelectRecord{" +
                "recId=" + recId +
                ", data=" + data +
                '}';
    }
}
