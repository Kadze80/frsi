package entities;

import java.io.Serializable;

public class ColumnModel implements Comparable<ColumnModel>, Serializable {
    private final String header;
    private final String name;

    public ColumnModel(String header, String name) {
        this.header = header;
        this.name = name;
    }

    public String getHeader() {
        return header;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColumnModel that = (ColumnModel) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(ColumnModel o) {
        return this.header.compareTo(o.header);
    }

    @Override
    public String toString() {
        return "ColumnModel{" +
                "header='" + header + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
