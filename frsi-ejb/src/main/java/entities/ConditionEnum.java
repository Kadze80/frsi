package entities;

import java.io.Serializable;

/**
 * Created by nuriddin on 11/3/16.
 */
public enum ConditionEnum implements Serializable {
    EQ, NE, LT, LE, GT, GE;

    public String nameToSign() {
        if (this == EQ) return "=";
        if (this == NE) return "!=";
        if (this == LT) return "<";
        if (this == GT) return ">";
        if (this == LE) return "<=";
        if (this == GE) return ">=";
        throw new IllegalStateException();
    }
}
