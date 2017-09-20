package entities;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserWarrant implements Serializable {
    private Long id;
    private String code;
    private Long principal;
    private Long attorney;
    private Date beginDate;
    private Date endDate;
    private boolean readonly;
    private boolean canceled;
    private List<AttachedFile> files = new ArrayList<AttachedFile>();
    private boolean filesChanged;
    private String principalName;
    private String attorneyName;
    private Boolean haveFile;

    public boolean isSame(UserWarrant o) {
        if (this.code != null && o.code != null) {
            if (!this.code.equals(o.code)) {
                return false;
            }
        }
        if ((this.code == null && o.code != null) ||
                this.code != null && o.code == null) {
            return false;
        }
        if (this.principal.longValue() != o.principal.longValue()) {
            return false;
        }
        if (this.attorney.longValue() != o.attorney.longValue()) {
            return false;
        }
        if (this.principal.longValue() != o.principal.longValue()) {
            return false;
        }
        if (!this.beginDate.equals(o.beginDate)) {
            return false;
        }
        if (this.endDate != null && o.endDate != null) {
            if (!this.endDate.equals(o.endDate)) {
                return false;
            }
        }
        if ((this.endDate == null && o.endDate != null) ||
                (this.endDate != null && o.endDate == null)) {
            return false;
        }
        if (this.canceled != o.canceled) {
            return false;
        }
        if (this.readonly != o.readonly) {
            return false;
        }
        return true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getPrincipal() {
        return principal;
    }

    public void setPrincipal(Long principal) {
        this.principal = principal;
    }

    public Long getAttorney() {
        return attorney;
    }

    public void setAttorney(Long attorney) {
        this.attorney = attorney;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getAttorneyName() {
        return attorneyName;
    }

    public void setAttorneyName(String attorneyName) {
        this.attorneyName = attorneyName;
    }

    public boolean isFilesChanged() {
        return filesChanged;
    }

    public void setFilesChanged(boolean filesChanged) {
        this.filesChanged = filesChanged;
    }

    public List<AttachedFile> getFiles() {
        return files;
    }

    public void setFiles(List<AttachedFile> files) {
        this.files = files;
    }

    public Boolean getHaveFile() {
        return haveFile;
    }

    public void setHaveFile(Boolean haveFile) {
        this.haveFile = haveFile;
    }
}
