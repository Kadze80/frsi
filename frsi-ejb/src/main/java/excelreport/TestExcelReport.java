package excelreport;

import dataform.FormulaSyntaxError;
import dataform.IKeyHandler;
import dataform.NoReportDataError;
import excelreport.impl.ExcelReportImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nuriddin.Baideuov on 23.11.2015.
 */
public class TestExcelReport {
    public static void main(String[] args) throws Exception {
        System.out.println("start");

        String f_tml = "D:\\work\\temp\\tml_3.xlsx";
        String f_res = "D:\\work\\temp\\res_1.xlsx";

        ExcelReport report = new ExcelReportImpl(f_tml);
        ReportSheet sheet = report.addSheet(0);
        sheet.setSheetTitle("List0");
        MapKeyHandler<String> handler = new MapKeyHandler<String>(new EntityMapConverter<String>() {
            @Override
            public Map<String, String> convert(String entity) {
                Map<String, String> data = new HashMap<String, String>();
                data.put("report_name", "Report 1");
                data.put("respondent_name","respondent 1");
                data.put("report_date","01.11.2015");
                data.put("validationMessage","Message 1");
                data.put("description","Error 1 data.put(\"validationMessage\",\"Message 1\"); data.put(\"validationMessage\",\"Message 1\"); data.put(\"validationMessage\",\"Message 1\");");
                return data;
            }
        });
        handler.setData("Nuriddin");
        sheet.out("header", handler);
        sheet.out("errorsHeader",null);
        sheet.out("errorsBody", handler);
        sheet.out("errorsBody", handler);

        report.saveResult(f_res);

        System.out.println("end");
    }

    /*public static void main(String[] args) throws Exception {
        System.out.println("start");

        String f_tml = "D:\\work\\temp\\tml_3.xlsx";
        String f_res = "D:\\work\\temp\\res_1.xlsx";

        ExcelReport report = new ExcelReportImpl(f_tml);
        report.addSheet(0);

        report.saveResult(f_res);

        System.out.println("end");
    }*/
}
