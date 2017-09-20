package entities;

import java.io.Serializable;

/**
 * Created by Marat.Madybayev on 26.11.2014.
 */
public class SubjectType implements Serializable {
    private static final long serialVersionUID = 1L;

    private String Code;
    private String Name_Ru;

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public String getName_Ru() {
        return Name_Ru;
    }

    public void setName_Ru(String name_Ru) {
        Name_Ru = name_Ru;
    }
}
