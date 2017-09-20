package entities;

import java.io.Serializable;

/**
 * Entity
 *
 * @author Ardak Saduakassov
 */
public class SerializedObjectWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    public String className;
    public String classSimpleName;
    public String tag;

    public byte[] bytes;
}
