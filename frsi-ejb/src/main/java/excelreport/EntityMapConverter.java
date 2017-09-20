package excelreport;

import java.util.Map;

/**
 * Created by Nuriddin.Baideuov on 23.11.2015.
 */
public interface EntityMapConverter<T> {
    Map<String, String> convert(T entity);
}
