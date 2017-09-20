package entities;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Ayupov.Bakhtiyar on 28.07.2015.
 */
public class MvCrosscheck {

    public byte[] GenerateExcel(List<RefCrosscheckItem> refCrosscheckItems) throws SQLException, IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("mv_crosscheck");

        Map<String, Object[]> data = new TreeMap<String, Object[]>();
        int i = 0;
        for (RefCrosscheckItem rs : refCrosscheckItems) {
            String i2 = String.valueOf(++i);
            Long id = rs.getId();
            Long recId = rs.getRecId();
            String formula = rs.getFormula();
            String descrRus = rs.getDescrRus();
            String crosscheckTypeName = rs.getCrossTypeName();
            String condition = rs.getCondition();
            data.put(i2, new Object[]{id, recId, formula, descrRus, crosscheckTypeName, condition});
        }

        HSSFCellStyle style = workbook.createCellStyle();
        style = workbook.createCellStyle();
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        DataFormat format = workbook.createDataFormat();

        Set<String> keyset = data.keySet();
        int rownum = 2;
        for (String key : keyset) {
            Row row = sheet.createRow(rownum++);
            Object[] objArr = data.get(key);
            int cellnum = 0;
            for (Object obj : objArr) {
                Cell cell = row.createCell(cellnum++);
                if (obj instanceof Date) {
                    cell.setCellValue((Date) obj);
                    cell.setCellStyle(style);
                } else if (obj instanceof Boolean) {
                    cell.setCellValue((Boolean) obj);
                    cell.setCellStyle(style);
                } else if (obj instanceof String) {
                    cell.setCellValue((String) obj);
                    cell.setCellStyle(style);
                } else if (obj instanceof Integer) {
                    cell.setCellValue((Integer) obj);
                    //style.setDataFormat(format.getFormat("#,##0"));
                    cell.setCellStyle(style);
                } else if (obj instanceof Long) {
                    cell.setCellValue((Long) obj);
                    style.setDataFormat(format.getFormat("#,##0"));
                    cell.setCellStyle(style);
                } else {
                    cell.setCellStyle(style);
                }
            }

        }

        HSSFCellStyle style2 = workbook.createCellStyle();
        style2.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
        style2.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        style2.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        style2.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
        HSSFFont font = workbook.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        style2.setFont(font);

        HSSFCellStyle HeaderStyle = workbook.createCellStyle();
        HeaderStyle.setFont(font);
        HeaderStyle.setAlignment(HeaderStyle.ALIGN_CENTER);

        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        // cell.setCellStyle(style2);
        cell.setCellStyle(HeaderStyle);
        cell.setCellValue("Справочник межформенных и внутриформенных контролей");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

        Row row2 = sheet.createRow(1);
        Cell cell2 = row2.createCell(0);
        cell2.setCellValue("ID");
        cell2.setCellStyle(style2);
        cell2 = row2.createCell(1);
        cell2.setCellValue("Rec_id");
        cell2.setCellStyle(style2);
        cell2 = row2.createCell(2);
        cell2.setCellValue("Формула");
        cell2.setCellStyle(style2);
        cell2 = row2.createCell(3);
        cell2.setCellValue("Описание на русском");
        cell2.setCellStyle(style2);
        cell2 = row2.createCell(4);
        cell2.setCellValue("Тип контроля");
        cell2.setCellStyle(style2);
        cell2 = row2.createCell(5);
        cell2.setCellValue("Условие");
        cell2.setCellStyle(style2);


        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        sheet.autoSizeColumn(4);
        sheet.autoSizeColumn(5);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        byte[] xls = baos.toByteArray();
        return xls;
    }
}
