package util;

/**
 * Created by nuriddin on 3/29/16.
 */
public class ExceptionUtil {
    public static Throwable getRootCauseRecursive(Throwable cause){
        if (cause == cause.getCause() || cause.getCause() == null)
            return cause;
        else
            return getRootCauseRecursive(cause.getCause());
    }
}
