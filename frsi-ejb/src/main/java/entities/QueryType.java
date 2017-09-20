package entities;

import java.io.Serializable;

/**
 * @author Aidar.Myrzahanov
 */
public enum QueryType implements Serializable {
    INSERT_OR_UPDATE, SELECT, EXPLAIN_PLAN;
    private static final long serialVersionUID = 1L;
}
