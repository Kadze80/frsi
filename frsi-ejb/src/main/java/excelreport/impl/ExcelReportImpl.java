package excelreport.impl;

import excelreport.ExcelReport;
import excelreport.ReportSheet;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

/**
 * Created by Nuriddin.Baideuov on 23.11.2015.
 */
public class ExcelReportImpl extends ExcelReport {
    protected XSSFWorkbook wbTemplate;
    protected XSSFWorkbook wbResult;

    public ExcelReportImpl(String fn) throws Exception{
        openTemplate(fn);
    }

    public ExcelReportImpl(InputStream byteInputStream) throws Exception{
        openTemplate(byteInputStream);
    }

    protected void openTemplate(String fn) throws Exception {
        InputStream fs = new FileInputStream(fn);
        try {
            wbTemplate = new XSSFWorkbook(fs);
        } finally {
            fs.close();
        }

        wbResult = new XSSFWorkbook();
    }

    protected void openTemplate(InputStream byteInputStream) throws Exception {
        wbTemplate = new XSSFWorkbook(byteInputStream);
        wbResult = new XSSFWorkbook();
    }

    @Override
    public ReportSheet addSheet(int templateSheetIndex) {
        XSSFSheet shRes = wbResult.createSheet();
        XSSFSheet shTml = wbTemplate.getSheetAt(templateSheetIndex);
        copyPrintSetup(shTml, shRes);
        return new ReportSheetImpl(shTml, shRes);
    }

    protected void copyPrintSetup(Sheet sheetToCopy, Sheet newSheet){
        PrintSetup sheetToCopyPrintSetup = sheetToCopy.getPrintSetup();
        PrintSetup newSheetPrintSetup = newSheet.getPrintSetup();

        newSheetPrintSetup.setPaperSize(sheetToCopyPrintSetup.getPaperSize());
        newSheetPrintSetup.setScale(sheetToCopyPrintSetup.getScale());
        newSheetPrintSetup.setPageStart(sheetToCopyPrintSetup.getPageStart());
        newSheetPrintSetup.setFitWidth(sheetToCopyPrintSetup.getFitWidth());
        newSheetPrintSetup.setFitHeight(sheetToCopyPrintSetup.getFitHeight());
        newSheetPrintSetup.setLeftToRight(sheetToCopyPrintSetup.getLeftToRight());
        newSheetPrintSetup.setLandscape(sheetToCopyPrintSetup.getLandscape());
        newSheetPrintSetup.setValidSettings(sheetToCopyPrintSetup.getValidSettings());
        newSheetPrintSetup.setNoColor(sheetToCopyPrintSetup.getNoColor());
        newSheetPrintSetup.setDraft(sheetToCopyPrintSetup.getDraft());
        newSheetPrintSetup.setNotes(sheetToCopyPrintSetup.getNotes());
        newSheetPrintSetup.setNoOrientation(sheetToCopyPrintSetup.getNoOrientation());
        newSheetPrintSetup.setUsePage(sheetToCopyPrintSetup.getUsePage());
        newSheetPrintSetup.setHResolution(sheetToCopyPrintSetup.getHResolution());
        newSheetPrintSetup.setVResolution(sheetToCopyPrintSetup.getVResolution());
        newSheetPrintSetup.setHeaderMargin(sheetToCopyPrintSetup.getHeaderMargin());
        newSheetPrintSetup.setFooterMargin(sheetToCopyPrintSetup.getFooterMargin());
        newSheetPrintSetup.setCopies(sheetToCopyPrintSetup.getCopies());
    }

    @Override
    public void saveResult(String fn) throws Exception {
        OutputStream fs = new FileOutputStream(fn);
        try {
            wbResult.write(fs);
        } finally {
            fs.close();
        }
    }

    @Override
    public void saveResult(OutputStream st) throws Exception {
        wbResult.write(st);
    }

    @Override
    public void closeWorkBooks() throws Exception{
        wbTemplate.close();
        wbResult.close();

    }
}
