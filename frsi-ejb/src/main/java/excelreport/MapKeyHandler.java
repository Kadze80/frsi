package excelreport;

import dataform.FormulaSyntaxError;
import dataform.IKeyHandler;
import dataform.NoReportDataError;

import java.util.Map;

/**
 * Created by Nuriddin.Baideuov on 23.11.2015.
 */
public class MapKeyHandler<T> implements IKeyHandler {
    private Map<String, String> data;
    private EntityMapConverter<T> converter;

    public MapKeyHandler(EntityMapConverter<T> converter) {
        this.converter = converter;
    }

    public void setData(T entity){
        data = converter.convert(entity);
    }

    @Override
    public String onKey(String key, int startIndex, int endIndex) throws FormulaSyntaxError, NoReportDataError {
        if (data.containsKey(key))
            return data.get(key);
        else
            return "";
    }
}
