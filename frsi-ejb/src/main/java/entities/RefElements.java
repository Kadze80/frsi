package entities;

import java.io.Serializable;

/**
 * Created by Ayupov.Bakhtiyar on 29.06.2015.
 */
public class RefElements implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long recId;
    private String name;
    private String code;

    // region Getter and Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRecId() {
        return recId;
    }

    public void setRecId(Long recId) {
        this.recId = recId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    // endregion
}
