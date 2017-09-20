package util;

/**
 * Created by nuriddin on 3/25/16.
 */
public class DivisionByZeroException extends RuntimeException {
    public DivisionByZeroException() {
        super("Деление на ноль");
    }

    public DivisionByZeroException(String message) {
        super(message);
    }
}
