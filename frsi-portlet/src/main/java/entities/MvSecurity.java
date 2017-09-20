/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 *
 * @author nurzhan.trimov
 */
public class MvSecurity {

    public byte[] GenerateExcel(List<RefSecuritiesItem> refSecuritiesItems) throws SQLException, IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("mv_securities");

        Map<String, Object[]> data = new TreeMap<String, Object[]>();
        int i = 0;
        for (RefSecuritiesItem rs : refSecuritiesItems) {
        //while (rs.next()) {
            String i2 = String.valueOf(++i);
            String nin = rs.getNin();
            //Long code = rs.getId();
            Long nominal_value = rs.getNominalValue();
            String currencyName = rs.getCurrencyName();
            String typeName = rs.getTypeName();
            String varietyName = rs.getVarietyName();
            String issuerName = rs.getIssuerName();
            String signName = rs.getSignName();



            data.put(i2, new Object[]{nin, nominal_value, currencyName, typeName, varietyName, issuerName, signName});
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
        cell.setCellValue("Справочник ценных бумаг");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
        
        Row row2 = sheet.createRow(1);
        Cell cell2 = row2.createCell(0);
        cell2.setCellValue("НИН");
        cell2.setCellStyle(style2);
        cell2 = row2.createCell(1);
        cell2.setCellValue("Номинальная стоимость");
        cell2.setCellStyle(style2);
        cell2 = row2.createCell(2);
        cell2.setCellValue("Валюта");
        cell2.setCellStyle(style2);
        cell2 = row2.createCell(3);
        cell2.setCellValue("Тип ЦБ");
        cell2.setCellStyle(style2);
        cell2 = row2.createCell(4);
        cell2.setCellValue("Вид ЦБ");
        cell2.setCellStyle(style2);
        cell2 = row2.createCell(5);
        cell2.setCellValue("Эмитент");
        cell2.setCellStyle(style2);
        cell2 = row2.createCell(6);
        cell2.setCellValue("Признак эмитента");
        cell2.setCellStyle(style2);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        sheet.autoSizeColumn(4);
        sheet.autoSizeColumn(5);
        sheet.autoSizeColumn(6);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        byte[] xls = baos.toByteArray();
        return xls;
    }
}
