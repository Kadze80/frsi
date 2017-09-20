package entities;

import java.io.Serializable;

/**
 * Created by Baurzhan.Baisholakov on 20.09.2016.
 */
public class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;

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
}
