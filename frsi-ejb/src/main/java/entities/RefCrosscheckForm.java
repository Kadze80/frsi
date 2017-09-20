package entities;

import java.io.Serializable;

/**
 * Created by Nuriddin.Baideuov on 16.06.2015.
 */
public class RefCrosscheckForm implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long refCrosscheckItemId;
    private String formCode;
    private String formTypeCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRefCrosscheckItemId() {
        return refCrosscheckItemId;
    }

    public void setRefCrosscheckItemId(Long refCrosscheckItemId) {
        this.refCrosscheckItemId = refCrosscheckItemId;
    }

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public String getFormTypeCode() {
        return formTypeCode;
    }

    public void setFormTypeCode(String formTypeCode) {
        this.formTypeCode = formTypeCode;
    }

    @Override
    public String toString() {
        return "RefCrosscheckForm{" +
                "id=" + id +
                ", refCrosscheckItemId=" + refCrosscheckItemId +
                ", formCode='" + formCode + '\'' +
                '}';
    }
}
