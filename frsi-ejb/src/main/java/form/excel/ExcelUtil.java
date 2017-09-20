package form.excel;

import ejb.PersistenceLocal;
import ejb.ReferenceLocal;
import entities.AbstractReference;
import entities.CurrencyWrapper;
import entities.ExcelCell;
import entities.ExcelCells;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ExcelUtil {

    private ReferenceLocal reference;
    private Date reportDate;
    private Workbook workbook;

    public ExcelUtil(ReferenceLocal reference) {
        this.reference = reference;
    }

    public void createExcelDocumentFromBytes(byte[] bytes) {
        InputStream is = new ByteArrayInputStream(bytes);
        try {
            this.workbook = new XSSFWorkbook(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateReferences(Date reportDate) {
        this.reportDate = reportDate;
        if (workbook == null) return;
        String refColumn;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            String sheetName = workbook.getSheetAt(i).getSheetName();
            // if (sheetName.equals("ref_bank") || sheetName.equals("ref_balance_account") || sheetName.equals("ref_legal_person") || sheetName.equals("ref_issuer") || sheetName.equals("ref_security")) continue;
            // if (sheetName.equals("ref_currency")) updateRefCurrency(i);
            if (sheetName.startsWith("ref_")) {
                String ref = "";
                if (sheetName.endsWith("_rec_id")) {
                    ref = sheetName.substring(0, sheetName.length() - 6);
                    refColumn = "rec_id";
                } else if (sheetName.endsWith("_code")) {
                    ref = sheetName.substring(0, sheetName.length() - 5);
                    refColumn = "code";
                } else {
                    refColumn = "name_ru";
                    ref = sheetName;
                }
                updateReference(i, ref, refColumn);
            }
        }
    }

    private void updateRefCurrency(int sheetIndex) {
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        List<CurrencyWrapper> currencies = reference.getCurrencyWrappers(reportDate);
        int rowNum = -1;
        for (CurrencyWrapper currency : currencies) {
            rowNum++;
            Row row = sheet.getRow(rowNum);
            if (row == null) row = sheet.createRow(rowNum);
            Cell cellA = row.getCell(0, Row.CREATE_NULL_AS_BLANK);
            cellA.setCellValue(currency.getCode());
            Cell cellB = row.getCell(1, Row.CREATE_NULL_AS_BLANK);
            cellB.setCellValue(currency.getNameRu());
            Cell cellC = row.getCell(2, Row.CREATE_NULL_AS_BLANK);
            cellC.setCellValue(currency.getCreditRate());
        }
        if (currencies.size() > 0) {
            sheet.autoSizeColumn(0, true);
            sheet.autoSizeColumn(1, true);
        }
    }

    private void updateReference(int sheetIndex, String refName, String refColumn) {
        List<? extends AbstractReference> refs = reference.getReferenceItemByName(refName, refColumn, reportDate);
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        int rowNum = -1;
        for (AbstractReference reference : refs) {
            rowNum++;
            Row row = sheet.getRow(rowNum);
            if (row == null) row = sheet.createRow(rowNum);
            if (refColumn.equals("code")) {
                Cell cellA = row.getCell(0, Row.CREATE_NULL_AS_BLANK);
                cellA.setCellValue(reference.getCode());
            }
            else if (refColumn.equals("name_ru")) {
                Cell cellB = row.getCell(1, Row.CREATE_NULL_AS_BLANK);
                cellB.setCellValue(reference.getNameRu());
            }
            else if (refColumn.equals("rec_id")) {
                Cell cellC = row.getCell(2, Row.CREATE_NULL_AS_BLANK);
                cellC.setCellValue(reference.getRecId());
            }
        }
        if (refs.size() > 0) {
            sheet.autoSizeColumn(0, true);
            sheet.autoSizeColumn(1, true);
        }
    }

    private void updateRefVidOper(int sheetIndex) {
    }

    public byte[] getExcelDocumentBytes() {
        byte[] result = null;
        if (workbook == null) return null;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            result = out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { out.close(); }
            catch (IOException e) { e.printStackTrace(); }
            return result;
        }
    }

    public static synchronized ExcelCell replacementValues(ExcelCell excelCell, ExcelCells replaceExcelCells){
        if(replaceExcelCells != null){
            for(ExcelCell item : replaceExcelCells.getExcelCells()){
                if(excelCell.getName().equals(item.getName())){
                    if(item.getKeyValue() != null)
                        excelCell.setKeyValue(item.getKeyValue());
                    if (item.getType() != null)
                        excelCell.setType(item.getType());
                    if (item.getPosition() != 0)
                        excelCell.setPosition(item.getPosition());
                    if (item.getRefName() != null)
                        excelCell.setRefName(item.getRefName());
                    if (item.getRefColumn() != null)
                        excelCell.setRefColumn(item.getRefColumn());
                    if(item.getFilterType() != null)
                        excelCell.setFilterType(item.getFilterType());
                    if(item.getMultiValue() != null)
                        excelCell.setMultiValue(item.getMultiValue());
                    if(excelCell.getRefName() != null && item.getRefName() == null){
                        excelCell.setRefName(null);
                        excelCell.setRefColumn(null);
                        excelCell.setFilterType(null);
                    }
                    break;
                }
            }
        }
        return excelCell;
    }

}
