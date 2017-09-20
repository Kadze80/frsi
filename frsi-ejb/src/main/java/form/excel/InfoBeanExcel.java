package form.excel;

import com.google.gson.Gson;
import entities.ColumnModel;
import entities.ExcelForm;
import entities.ReportListItem;
import entities.ReportValueNameListItem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

public class InfoBeanExcel {

    private XSSFWorkbook workbook;
    private Sheet sheet;
    private Info info;


    public InfoBeanExcel(byte[] xlsFile, List<ReportValueNameListItem> items, List<ColumnModel> columns, String headerText) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(xlsFile);

        workbook = new XSSFWorkbook(inputStream);
        sheet = workbook.getSheetAt(0);

        try {
            info = new Gson().fromJson(sheet.getRow(0).getCell(0).getStringCellValue(), Info.class);
        } catch (Exception e) {
            throw new Exception("Ошибка разбора структуры excel файла! \n" + e.getMessage());
        }

        Row row = sheet.getRow(info.headerRow);
        if (row == null) {
            row = sheet.createRow(info.headerRow);
        }
        setCellValue(info.headerCol, headerText, row);

        row = sheet.getRow(info.row);
        Cell tmplCell = row.getCell(info.dynCol);
        if (tmplCell == null) {
            tmplCell = row.createCell(info.dynCol);
        }
        int curCell = info.dynCol;
        for (ColumnModel column : columns) {
            if (curCell != info.dynCol) {
                ExcelHelper.insertNewColumnBefore(sheet, curCell);
            }
            Cell cell = setCellValue(curCell, column.getHeader(), row);
            if (curCell != info.dynCol) {
                ExcelHelper.cloneStyle(tmplCell, cell);
            }
            curCell++;
        }

        int curRow = info.row + 1;
        Row templRow = sheet.getRow(curRow);
        if (templRow == null) {
            templRow = sheet.createRow(curRow);
        }
        XSSFRow _row = (XSSFRow) templRow;

        Cell tmplRowNumCell = getTemplCell(templRow, info.no);
        Cell tmplNameCell = getTemplCell(templRow, info.name);
        Cell tmplDynCell = getTemplCell(templRow, info.dynCol);

        for (ReportValueNameListItem item : items) {
            row = sheet.getRow(curRow);
            if (row == null) {
                row = sheet.createRow(curRow);
            }
            if (_row.getCTRow().isSetHt())
                row.setHeight(_row.getHeight());

            Cell rowNumCell = setCellValue(info.no, String.valueOf(item.getRowNum()), row);
            rowNumCell.setCellStyle(tmplRowNumCell.getCellStyle());

            Cell nameCell = setCellValue(info.name, item.getValueName(), row);
            nameCell.setCellStyle(tmplNameCell.getCellStyle());

            curCell = info.dynCol;
            for (ColumnModel column : columns) {
                Cell cell = setCellValue(curCell, item.getSubmitReportTextByFormCode(column.getName()), row);
                if (curCell != info.dynCol) {
                    ExcelHelper.cloneStyle(tmplDynCell, cell);
                }
                curCell++;
            }

            curRow++;
        }
    }

    private Cell setCellValue(int cellIndex, String value, Row row) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            cell = row.createCell(cellIndex);
        }
        cell.setCellValue(value);
        return cell;
    }

    private Cell getTemplCell(Row templRow, int cellIndex){
        Cell tmplCell = templRow.getCell(cellIndex);
        if (tmplCell == null) {
            tmplCell = templRow.createCell(cellIndex);
        }
        return tmplCell;
    }

    public XSSFWorkbook getWorkbook() {
        return workbook;
    }

    private static class Info {
        private int no; // позиция клонки rowNum
        private int name; // позиция колонки Наименование
        private int dynCol; // позиция начало динамических коолонок
        private int row; // номер записи заголовки таблицы
        private int headerRow; // номер записи заголовки
        private int headerCol; // номер колонки заголовки
    }
}
