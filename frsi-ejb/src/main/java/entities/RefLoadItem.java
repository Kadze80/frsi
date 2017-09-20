package entities;

import java.io.Serializable;

/**
 * Created by Marat.Madybayev on 27.02.2015.
 */
public class RefLoadItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String refFrsiName;         //code
    private String nsiTableName;        //table_name
    private String name;                //name
    private String type;
    private String MatName;
    private Integer Id;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMatName() {
        return MatName;
    }

    public void setMatName(String matName) {
        MatName = matName;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getRefFrsiName() {
        return refFrsiName;
    }

    public void setRefFrsiName(String refFrsiName) {
        this.refFrsiName = refFrsiName;
    }

    public String getNsiTableName() {
        return nsiTableName;
    }

    public void setNsiTableName(String nsiTableName) {
        this.nsiTableName = nsiTableName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
