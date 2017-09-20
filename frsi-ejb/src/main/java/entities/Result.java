package entities;

import java.io.Serializable;

/**
 * Created by Ayupov.Bakhtiyar on 22.06.2016.
 */
public class Result implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String refName;
    private String msg;

    public Result(boolean success, String refName, String msg) {
        this.success = success;
        this.refName = refName;
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
