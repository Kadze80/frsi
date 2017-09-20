package entities;

import java.io.Serializable;

/**
 * Created by nuriddin on 10/21/16.
 */
public class DataColumn implements Serializable, Comparable<DataColumn> {
    private static final long serialVersionUID = 1L;
    private int index;
    private String name;
    private Class colClass;

    public DataColumn(int index, String name, Class colClass) {
        this.index = index;
        this.name = name;
        this.colClass = colClass;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getColClass() {
        return colClass;
    }

    public void setColClass(Class colClass) {
        this.colClass = colClass;
    }

    @Override
    public String toString() {
        return "DataColumn{" +
                "index='" + index + '\'' +
                ", name='" + name + '\'' +
                ", colClass=" + colClass +
                '}';
    }

    @Override
    public int compareTo(DataColumn o) {
        if (index > o.getIndex())
            return 1;
        else
            return -1;
    }
}
