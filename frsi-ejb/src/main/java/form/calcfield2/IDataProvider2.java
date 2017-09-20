package form.calcfield2;

import dataform.NoReportDataError;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by nuriddin on 10/6/16.
 */
public interface IDataProvider2 {

    String getString(String ind) throws NoReportDataError;

    double getNumber(String ind) throws NoReportDataError;

    void set(String ind, Object value, String dt);

    Date getDate(String ind) throws NoReportDataError;

    boolean getBoolean(String ind) throws NoReportDataError;

    double sumDynRow(DoubleCallback cb, String templ, String container) throws Exception;

    int count(String templ, String container);

    double sumRange(DoubleCallback cb, String orientation, String start, String end, String container) throws Exception;

    void eachRow(String ind, ObjectCallback cb, String templ, String container, String dt) throws Exception;

    List<String> getRowIds(String container, String templ);

    List<String> getRangeRowIds(String container, String start, String end);

    List<String> getRangeFields(String container, String start, String end);

    String getRowId(); // get current row id of dynamic rows or range

    void setRowId(String rowId); // set current rowId of dynamic rows or range

    String getField(); // get current field of range

    void setField(String field); // set current field of range of fields

    void setFields(Map<String, List<String>> fieldMap);
}
