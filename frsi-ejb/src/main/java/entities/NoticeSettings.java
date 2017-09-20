package entities;

import java.io.Serializable;

/**
 * Created by Ayupov.Bakhtiyar on 13.02.2017.
 */
public class NoticeSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Boolean isNotice;
    private Boolean noticeMail;
    private Boolean noticeSystem;

    // region Getter and Setter;
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

    public Boolean getNotice() {
        return isNotice;
    }

    public void setNotice(Boolean notice) {
        isNotice = notice;
    }

    public Boolean getNoticeMail() {
        return noticeMail;
    }

    public void setNoticeMail(Boolean noticeMail) {
        this.noticeMail = noticeMail;
    }

    public Boolean getNoticeSystem() {
        return noticeSystem;
    }

    public void setNoticeSystem(Boolean noticeSystem) {
        this.noticeSystem = noticeSystem;
    }

    // endregion
}
