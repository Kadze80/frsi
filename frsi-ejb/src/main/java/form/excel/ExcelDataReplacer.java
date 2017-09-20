package form.excel;

import com.google.gson.Gson;
import entities.ExcelCell;
import entities.ExcelCells;
import entities.ExcelForm;
import entities.ExcelTable;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Iterator;

/**
 * Created by Nuriddin.Baideuov on 22.06.2015.
 */
public class ExcelDataReplacer implements Serializable {

    private String formName;
    XSSFWorkbook workbook;
    private Sheet sheet;
    private ExcelForm excelForm;
    private DecimalFormat n6f;
    private String[] REF_BALANCE_ACCOUNT_EXAMPLES = {"1001131", "1001232", "1001233", "1002131", "1002232"};

    public ExcelDataReplacer(byte[] xlsFile) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(xlsFile);

        workbook = new XSSFWorkbook(inputStream);
        sheet = workbook.getSheetAt(0);

        try {
            excelForm = new Gson().fromJson(sheet.getRow(0).getCell(0).getStringCellValue(), ExcelForm.class);
        } catch (Exception e) {
            throw new Exception("Ошибка разбора структуры excel файла! \n" + e.getMessage());
        }

        formName = excelForm.getForm();

        n6f = new DecimalFormat("#.######");
    }

    public void process() throws Exception {
        Iterator<Row> rows;
        Row row;
        Cell cell;
        String positionValue;
        boolean isTableFound;
        String value;
        String valueKey;
        Integer iLevel = null;
        String valueKeyGroup;
        ExcelTable excelTable;
        ExcelCells excelCells;
        ExcelCell excelCell;

        for (int i = 0; i < excelForm.getTables().length; i++) {
            excelTable = excelForm.getTable(i);
            isTableFound = false;
            rows = sheet.iterator();
            positionValue = null;
            valueKeyGroup = null;
            while (rows.hasNext()) {
                row = rows.next();

                cell = row.getCell(excelTable.getPosition());
                if (cell != null) {
                    positionValue = cell.getStringCellValue().trim();
                    if (positionValue.equals(excelTable.getName()))
                        isTableFound = true;
                    else if ((!positionValue.equals("")) && (positionValue.substring(0, 1).equals("{")))
                        valueKeyGroup = positionValue.substring(0, 1);
                    else if ((positionValue != null) && (!positionValue.equals("")) && (positionValue.length() >= 2) && (positionValue.substring(0, 2).equals("//")))
                        continue;
                    else if (positionValue.equals("$D.")) {
                        for (int j = 0; j < excelTable.getColumns().length; j++) {
                            excelCell = excelTable.getColumn(j);
                            if (excelCell.getName().equals(excelTable.getKey())) {
                                valueKeyGroup = getCellStringValue(row.getCell(excelCell.getPosition()));
                                if (valueKeyGroup == null)
                                    throw new Exception("Ошибка определения ключа в строке " + row.getCell(excelCell.getPosition()).getRow() +
                                            ", в столбце " + row.getCell(excelCell.getPosition()).getColumnIndex() + "!");
                                break;
                            }
                        }
                        valueKeyGroup = positionValue + valueKeyGroup + ".";
                        iLevel = -1;
                    } else if (positionValue.equals("$D.group.")) {
                        valueKeyGroup = positionValue;
                        iLevel = -1;
                    } else if (positionValue.equals("code")) {
                        valueKeyGroup = positionValue;
                        iLevel = -1;
                    } else if (positionValue.equals("end"))
                        break;
                } else
                    positionValue = null;

                if (isTableFound) {
                    if ((positionValue != null) && (positionValue.equals("v"))) {
                        valueKey = null;
                        for (int j = 0; j < excelTable.getColumns().length; j++) {
                            excelCell = excelTable.getColumn(j);

                            cell = row.getCell(excelCell.getPosition());

                            if (excelCell.getName().equals(excelTable.getKey())) {
                                if (cell != null)
                                    valueKey = getCellStringValue(cell);
                            } else {
                                String path = excelForm.getForm() + "_" + excelTable.getName() + "*" + excelCell.getName() + ":" + excelTable.getKey() + ":" + valueKey;
                                if (cell == null)
                                    cell = row.createCell(excelCell.getPosition(), Cell.CELL_TYPE_STRING);
                                cell.setCellValue(path);
                            }
                        }
                    } else if (valueKeyGroup != null) {
                        if ((valueKeyGroup.length() > 2) && (valueKeyGroup.substring(0, 3).equals("$D."))) {
                            iLevel++;
                            if (iLevel > 0) {
                                if (positionValue != null && positionValue.equals("level_end")) {
                                    valueKey = valueKeyGroup + "n";
                                    valueKeyGroup = null;
                                } else {
                                    valueKey = valueKeyGroup + iLevel.toString();
                                }
                                for (int j = 0; j < excelTable.getColumns().length; j++) {
                                    excelCell = excelTable.getColumn(j);

                                    cell = row.getCell(excelCell.getPosition());

                                    String path;
                                    if (excelCell.getName().equals(excelTable.getKey()))
                                        //path = valueKey.substring("$D.".length());
                                        continue;
                                    else
                                        path = excelForm.getForm() + "_" + excelTable.getName() + "*" + excelCell.getName() + ":" + excelTable.getKey() + ":" + valueKey;

                                    if (cell == null)
                                        cell = row.createCell(excelCell.getPosition(), Cell.CELL_TYPE_STRING);
                                    cell.setCellValue(path);
                                }
                            }
                        } else if (valueKeyGroup.equals("{")) {
                            try {
                                excelCells = new Gson().fromJson(positionValue, ExcelCells.class);
                            } catch (Exception e) {
                                throw new Exception("Ошибка разбора структуры excel ячеек! \n" + e.getMessage());
                            }
                            for (int j = 0; j < excelCells.getExcelCells().length; j++) {
                                excelCell = excelCells.getCell(j);

                                String path = excelForm.getForm() + "*" + excelCell.getName() + "::";

                                cell = row.getCell(excelCell.getPosition());
                                if (cell == null)
                                    cell = row.createCell(excelCell.getPosition(), Cell.CELL_TYPE_STRING);
                                cell.setCellValue(path);
                            }
                            valueKeyGroup = null;
                        } else if (valueKeyGroup.equals("code")) {
                            iLevel++;
                            if (iLevel > 0) {
                                valueKey = null;
                                for (int j = 0; j < excelTable.getColumns().length; j++) {
                                    excelCell = excelTable.getColumn(j);

                                    cell = row.getCell(excelCell.getPosition());

                                    if (excelCell.getName().equals(excelTable.getKey())) {
                                        valueKey = REF_BALANCE_ACCOUNT_EXAMPLES[iLevel - 1];
                                        if (cell == null)
                                            cell = row.createCell(excelCell.getPosition(), Cell.CELL_TYPE_STRING);
                                        cell.setCellValue(valueKey);
                                    } else {
                                        if (cell != null) {
                                            String path = excelForm.getForm() + "_" + excelTable.getName() + "*" + excelCell.getName() + ":" + excelTable.getKey() + ":" + valueKey;
                                            if (cell == null)
                                                cell = row.createCell(excelCell.getPosition(), Cell.CELL_TYPE_STRING);
                                            cell.setCellValue(path);
                                        }
                                    }
                                }

                                if (iLevel > (REF_BALANCE_ACCOUNT_EXAMPLES.length - 1))
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    private String getCellStringValue(Cell cell) throws Exception {
        String result = null;
        try {
            try {
                result = cell.getStringCellValue();
            } catch (Exception e) {
                result = n6f.format(cell.getNumericCellValue());
            }
        } catch (Exception e) {
            int rowIndex = cell.getRowIndex() + 1;
            int columnIndex = cell.getColumnIndex() + 1;
            throw new Exception("Ошибка разбора значения в строке " + rowIndex + ", в столбце " + columnIndex + "!");
        }
        return result;
    }

    public XSSFWorkbook getWorkbook() {
        return workbook;
    }

    public String getFormName() {
        return formName;
    }

    public static void main(String[] args) {
        try {

            Path path = Paths.get("c://xls/balance_accounts.xlsm");
            byte[] data = Files.readAllBytes(path);
            ExcelDataReplacer replacer = new ExcelDataReplacer(data);
            replacer.process();

            FileOutputStream fileOut = new FileOutputStream("c://xls/" + replacer.getFormName() + "_out.xlsm");
            replacer.getWorkbook().write(fileOut);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
