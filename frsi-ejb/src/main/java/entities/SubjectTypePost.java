package entities;

import java.io.Serializable;

/**
 * Created by ayupov.bakhtiyar on 03.05.2017.
 */
public class SubjectTypePost implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long postRecId;
    private String name;
    private Boolean isActive;


    // region Getter and Setter

    public Long getPostRecId() {
        return postRecId;
    }

    public void setPostRecId(Long postRecId) {
        this.postRecId = postRecId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }


    // endregion
}
