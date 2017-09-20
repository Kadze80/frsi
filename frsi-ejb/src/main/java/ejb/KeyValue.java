package ejb;

import java.io.Serializable;

/**
 * Created by nuriddin on 8/7/16.
 */
public class KeyValue implements Serializable {
    private static final long serialVersionUID = 1L;
    private String key;
    private String value;

    public KeyValue(String key, String value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalStateException("Ключ не может быть пустым");
        }
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyValue keyValue = (KeyValue) o;

        return key.equals(keyValue.key);

    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
