package entities;

import java.io.Serializable;

/**
 * Created by nuriddin on 3/10/17.
 */
public class EjbResponse<T> implements Serializable{
    private T payload;
    private Exception exception;

    public EjbResponse() {
    }

    public EjbResponse(T payload) {
        this.payload = payload;
    }

    public EjbResponse(Exception exception) {
        this.exception = exception;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "EjbResponse{" +
                "payload=" + payload +
                ", exception=" + exception +
                '}';
    }
}
