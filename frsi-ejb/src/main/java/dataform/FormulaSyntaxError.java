package dataform;

/**
 * Created by Nuriddin.Baideuov on 11.06.2015.
 */
public class FormulaSyntaxError extends Exception {

    public FormulaSyntaxError() {
        super("Ошибка в синтаксисе формулы");
    }

    public FormulaSyntaxError(String message) {
        super(message);
    }

    public FormulaSyntaxError(Throwable cause) {
        super(cause);
    }
}
