package entities;

import java.io.Serializable;

/**
 * Created by Ayupov.Bakhtiyar on 12.08.2016.
 */
public class Image implements Serializable {
    private static final long serialVersionUID = 1L;

    private String path;
    private String title;
    private String titleRight;
    private String description;
    private int index;

    // region Getter and Setter

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTitleRight() {
        return titleRight;
    }

    public void setTitleRight(String titleRight) {
        this.titleRight = titleRight;
    }

    // endregion
}
