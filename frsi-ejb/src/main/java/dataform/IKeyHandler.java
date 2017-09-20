package dataform;

/**
 * Created by Nuriddin.Baideuov on 11.06.2015.
 */
public interface IKeyHandler {
    String onKey(String key, int startIndex, int endIndex) throws FormulaSyntaxError, NoReportDataError;
}
