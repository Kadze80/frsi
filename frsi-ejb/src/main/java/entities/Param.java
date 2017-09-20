package entities;

import java.io.Serializable;

/**
 * Created by nuriddin on 1/16/17.
 */
public class Param implements Serializable{
    private String name;
    private Variant value;

    public Param(String name, Variant value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Variant getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Param{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
