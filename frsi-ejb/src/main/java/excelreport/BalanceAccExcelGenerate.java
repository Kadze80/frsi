package excelreport;

import com.google.gson.Gson;
import entities.ExcelForm;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.helpers.XSSFRowShifter;

import javax.ejb.EJBException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 06.06.2017.
 */
public class BalanceAccExcelGenerate implements Serializable {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private int positionKey;
    private int finishRn;
    private int startTableRn;
    private int endTableRn;
    private int codeNum;
    private int nameNum;
    private int sumNum;
    private XSSFCellStyle codeStyle;
    private XSSFCellStyle nameStyle;
    private XSSFCellStyle sumStyle;

    public BalanceAccExcelGenerate(byte[] xlsFile, Date reportdate, Connection connection) throws Exception {

        InputStream inputStream = new ByteArrayInputStream(xlsFile);

        this.workbook = new XSSFWorkbook(inputStream);
        this.sheet = workbook.getSheetAt(0);

        ExcelForm excelForm;
        try {
            excelForm = new Gson().fromJson(sheet.getRow(0).getCell(0).getStringCellValue(), ExcelForm.class);
        } catch (Exception e) {
            throw new Exception("Ошибка разбора структуры excel файла! \n" + e.getMessage());
        }

        startTableRn = excelForm.getTable(0).getStartTable();
        endTableRn = excelForm.getTable(0).getEndTable();
        positionKey = excelForm.getTable(0).getPosition();
        codeNum = positionKey + 1;
        nameNum = positionKey + 2;
        sumNum = positionKey + 3;

        codeStyle = sheet.getRow(startTableRn + 1).getCell(codeNum).getCellStyle();
        nameStyle = sheet.getRow(startTableRn + 1).getCell(nameNum).getCellStyle();
        sumStyle = sheet.getRow(startTableRn + 1).getCell(sumNum).getCellStyle();

        finishRn = startTableRn + 1;

        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;

        try {
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_READ_BALANCE_ACC_LIST (?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            if (reportdate == null) {
                ocs.setNull(1, OracleTypes.NULL);
            }else{
                ocs.setDate(1, new java.sql.Date(reportdate.getTime()));
            }
            ocs.registerOutParameter(2, OracleTypes.CURSOR);
            ocs.registerOutParameter(3, OracleTypes.INTEGER);
            ocs.registerOutParameter(4, OracleTypes.VARCHAR);
            ocs.execute();

            cursor = ocs.getCursor(2);

            while (cursor.next()) {
                insertRow(cursor.getString("CODE"), cursor.getString("NAME_RU"), cursor.getInt("LEVEL"));
            }

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            try { cursor.close(); } catch (Exception e) {}
            try { ocs.close(); } catch (Exception e) {}
            try { stmt.close(); } catch (Exception e) {}
        }
    }

    private void insertRow(String code, String name, int level)
    {
        XSSFRow row;
        XSSFCell cellCode;
        XSSFCell cellName;
        if(finishRn >= endTableRn){
            sheet.shiftRows(finishRn, sheet.getLastRowNum(), 1);

            row = sheet.createRow(finishRn);

            XSSFCell vCell = row.createCell(positionKey);
            vCell.setCellValue("v");

            cellCode = row.createCell(codeNum);
            cellCode.setCellStyle(codeStyle);

            cellName = row.createCell(nameNum);
            cellName.setCellStyle(nameStyle);

            XSSFCell sumCell = row.createCell(sumNum);
            sumCell.setCellStyle(sumStyle);
        }else {
            row = sheet.getRow(finishRn);
            cellCode = row.getCell(codeNum);
            cellName = row.getCell(nameNum);
        }
        cellCode.setCellValue(code);
        cellName.setCellValue(level == 4 ? "      " + name : name);
        finishRn++;
    }

    public XSSFWorkbook getWorkbook() {
        return workbook;
    }
}
