package entities;

import java.io.Serializable;

/**
 * Created by Nuriddin.Baideuov on 29.03.2015.
 */
public class Permission implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String name;
    private String titleKz;
    private String titleRu;
    private String titleEn;
    private long itemId;
    private boolean active;
    private boolean inhActive; // унаследован от группы (для пользователя)
    private boolean forGroup;
    private boolean leaf;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitleKz() {
        return titleKz;
    }

    public void setTitleKz(String titleKz) {
        this.titleKz = titleKz;
    }

    public String getTitleRu() {
        return titleRu;
    }

    public void setTitleRu(String titleRu) {
        this.titleRu = titleRu;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isInhActive() {
        return inhActive;
    }

    public void setInhActive(boolean inhActive) {
        this.inhActive = inhActive;
    }

    public boolean isForGroup() {
        return forGroup;
    }

    public void setForGroup(boolean forGroup) {
        this.forGroup = forGroup;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    // todo нужно доработать если значение не задано для текущего языка
    public String getTitle(String languageCode) {
        String title = null;
        if (languageCode.equals("kz")) title = titleKz;
        else if (languageCode.equals("ru")) title = titleRu;
        else if (languageCode.equals("en")) title = titleEn;

        if (title != null && !title.trim().isEmpty())
            return title;

        String[] titles = {titleRu, titleKz, titleEn};
        for (String n : titles) {
            if (n != null) {
                if (!n.trim().isEmpty()) {
                    return n;
                }
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", name='" + name + '\'' +
                ", titleKz='" + titleKz + '\'' +
                ", titleRu='" + titleRu + '\'' +
                ", titleEn='" + titleEn + '\'' +
                ", leaf='" + leaf + '\'' +
                '}';
    }
}
