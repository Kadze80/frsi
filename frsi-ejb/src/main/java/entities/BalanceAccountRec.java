package entities;

import java.io.Serializable;

/**
 * Created by Nuriddin.Baideuov on 23.09.2015.
 */
public class BalanceAccountRec implements Serializable {
    private Long rowNum;
    private Integer level;
    private String code;
    private String parentCode;

    public Long getRowNum() {
        return rowNum;
    }

    public void setRowNum(Long rowNum) {
        this.rowNum = rowNum;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }
}
