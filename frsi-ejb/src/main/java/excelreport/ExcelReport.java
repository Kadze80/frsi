package excelreport;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;

/**
 * Created by Nuriddin.Baideuov on 23.11.2015.
 */
public abstract class ExcelReport {
    public abstract ReportSheet addSheet(int templateSheetIndex);
    public abstract void saveResult(String fn) throws Exception;
    public abstract void saveResult(OutputStream st) throws Exception;
    public abstract void closeWorkBooks() throws Exception;
}
