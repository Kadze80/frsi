package entities;

import java.io.Serializable;
import java.util.Date;


/**
 * Created by Ayupov.Bakhtiyar on 16.04.2015.
 */
public class RefItem implements Serializable  {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String code;
    private Long refKnd;
    private String refKndName;
    private Date dateLoad;
    private String stsLoad;
    private Long cntNotSent;
    private Long cntWait;
    private boolean selected;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRefKnd() {
        return refKnd;
    }

    public void setRefKnd(Long refKnd) {
        this.refKnd = refKnd;
    }

    public Date getDateLoad() {
        return dateLoad;
    }

    public void setDateLoad(Date dateLoad) {
        this.dateLoad = dateLoad;
    }

    public Long getCntNotSent() {
        return cntNotSent;
    }

    public void setCntNotSent(Long cntNotSent) {
        this.cntNotSent = cntNotSent;
    }

    public Long getCntWait() {
        return cntWait;
    }

    public void setCntWait(Long cntWait) {
        this.cntWait = cntWait;
    }

    public String getStsLoad() {
        return stsLoad;
    }

    public void setStsLoad(String stsLoad) {
        this.stsLoad = stsLoad;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getRefKndName() {
        return refKndName;
    }

    public void setRefKndName(String refKndName) {
        this.refKndName = refKndName;
    }
}
