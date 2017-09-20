package entities;

import java.io.Serializable;

/**
 * Created by Nuriddin.Baideuov on 08.02.2016.
 */
public class PostType implements Serializable {
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
