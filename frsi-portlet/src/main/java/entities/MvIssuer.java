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
public class MvIssuer {

    public byte[] GenerateExcel(List<RefIssuersItem> refIssuersItems) throws SQLException, IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("MV_ISSUER");

        Map<String, Object[]> data = new TreeMap<String, Object[]>();
        int i = 0;
        for (RefIssuersItem rs : refIssuersItems) {
        //while (rs.next()) {
            String i2 = String.valueOf(++i);
            String NAME = rs.getNameRu();
//            String ISSUER_SIGN = rs.getSignNameRu();
            //Integer S_G_ISSUER_SIGN = rs.getInt("S_G_ISSUER_SIGN");
            //String AMD_REASON = rs.getString("AMD_REASON");
            



            //data.put(i2, new Object[]{NAME, ISSUER_SIGN, AMD_REASON});
            //data.put(i2, new Object[]{NAME, ISSUER_SIGN});
            data.put(i2, new Object[]{/*i, */NAME});
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
        cell.setCellValue("Справочник эмитентов");
        //sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 0));

        Row row2 = sheet.createRow(1);
        //Cell cell2 = row2.createCell(0);
        //cell2.setCellValue("№ п/п");
        //cell2.setCellStyle(style2);
        Cell cell2 = row2.createCell(0);
        cell2.setCellValue("Наименование");
        cell2.setCellStyle(style2);
        /*
        cell2 = row2.createCell(1);
        cell2.setCellValue("S_G_ISSUER_SIGN");
        cell2.setCellStyle(style2);
        */
        //cell2 = row2.createCell(2);
        //cell2.setCellValue("AMD_REASON");
        //cell2.setCellStyle(style2);
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        //sheet.autoSizeColumn(2);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        byte[] xls = baos.toByteArray();
        /*
        FileOutputStream out = new FileOutputStream(new File("C:\\TEMP\\MV_ISSUER.xls"));
        workbook.write(out);
        out.close();
        System.out.println("Excel written successfully..");
        */
        return xls;
    }
}
