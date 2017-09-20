package entities;

/**
 * Created by nuriddin on 1/16/17.
 */
public class RefExtIndicatorParam extends AbstractReference {
    public static final String REF_CODE = "ref_extparam";

    private Long ref_extind_id;
    private String name;
    private String valueType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public Long getRef_extind_id() {
        return ref_extind_id;
    }

    public void setRef_extind_id(Long ref_extind_id) {
        this.ref_extind_id = ref_extind_id;
    }

    /* @Override
    public String toString() {
        return "RefOutIndicatorParam{" +
                "name='" + name + '\'' +
                ", valueType=" + valueType +
                '}';
    }  */
}
