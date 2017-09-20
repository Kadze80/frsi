package form.excel;

import com.google.gson.Gson;
import ejb.Reference;
import entities.*;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import util.Validators;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Ref;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Nuriddin.Baideuov on 14.09.2015.
 */
public class ExtractExcelData implements Serializable {
    private static final Logger logger = Logger.getLogger("fileLogger");

    private DecimalFormat n0f;
    private DecimalFormat n1f;
    private DecimalFormat n2f;
    private DecimalFormat n3f;
    private DecimalFormat n4f;
    private DecimalFormat n5f;
    private DecimalFormat n6f;
    private DecimalFormat n7f;
    private DecimalFormat n8f;
    private SimpleDateFormat sdf;
    private SimpleDateFormat stf;


    private String formName;
    private Date reportDate;
    private Sheet sheet;
    private ExcelForm excelForm;
    private Map<String, String> inputValues;
    private Boolean haveParams;

    private Reference reference;

    private Gson gson = new Gson();

    public ExtractExcelData(Reference reference) {
        this.reference = reference;
    }

    public ExtractExcelData(byte[] xlsFile, Date reportDate, Reference reference) throws Exception {
        this.reportDate = reportDate;
        this.reference = reference;

        n0f = new DecimalFormat("#");
        n1f = new DecimalFormat("#.#");
        n2f = new DecimalFormat("#.##");
        n3f = new DecimalFormat("#.###");
        n4f = new DecimalFormat("#.####");
        n5f = new DecimalFormat("#.#####");
        n6f = new DecimalFormat("#.######");
        n7f = new DecimalFormat("#.#######");
        n8f = new DecimalFormat("#.########");
        sdf = new SimpleDateFormat("dd.MM.yyyy");
        stf = new SimpleDateFormat("HH:mm:ss");

        InputStream inputStream = new ByteArrayInputStream(xlsFile);

        XSSFWorkbook xb = new XSSFWorkbook(inputStream);
        sheet = xb.getSheetAt(0);

        try {
            excelForm = new Gson().fromJson(sheet.getRow(0).getCell(0).getStringCellValue(), ExcelForm.class);
        } catch (Exception e) {
            throw new Exception("Ошибка разбора структуры excel файла! \n" + e.getMessage());
        }

        if (sheet.getRow(1) == null || sheet.getRow(1).getCell(0) == null || sheet.getRow(1).getCell(0).getStringCellValue() == null
                || sheet.getRow(1).getCell(0).getStringCellValue().trim().length() == 0)
            haveParams = false;
        else if(sheet.getRow(1).getCell(0).getStringCellValue().trim().equals("begin"))
            haveParams = true;

        formName = excelForm.getForm();
    }

    public void ExtractExcelTable() throws Exception {
        inputValues = new HashMap<String, String>();

        Iterator<Row> rows;
        Row row;
        Cell cell;
        Cell cellParam;
        String positionValue;
        boolean isTableFound;
        String value;
        String valueKey;
        Integer iLevel = null;
        String valueKeyGroup;
        ExcelTable excelTable;
        ExcelCell excelCellKey = new ExcelCell();
        excelCellKey.setType("s");
        ExcelCells replaceExcelCells = null;

        for (int i = 0; i < excelForm.getTables().length; i++) {

            excelTable = excelForm.getTable(i);
            isTableFound = false;
            rows = sheet.iterator();
            valueKeyGroup = null;

            while (rows.hasNext()) {
                row = rows.next();

                positionValue = null;

                if(haveParams && row.getRowNum() > 1) {
                    cellParam = row.getCell(0);
                    if (cellParam != null) {
                        positionValue = cellParam.getStringCellValue().trim();
                        if ((!positionValue.equals("")) && (positionValue.substring(0, 1).equals("{"))) {
                            excelCellsParsing(row, positionValue);
                            continue;
                        }else if ((positionValue != null) && (!positionValue.equals("")) && (positionValue.length() >= 2) && (positionValue.substring(0, 2).equals("//")))
                            continue;
                        else if (positionValue.equals("end") && !isTableFound)
                            break;
                    } else
                        positionValue = null;
                }

                cell = row.getCell(excelTable.getPosition());
                if (cell != null) {
                    positionValue = cell.getStringCellValue().trim();
                    if ((positionValue == null) || (positionValue.equals("")))
                        positionValue = null;
                    else if (positionValue.equals(excelTable.getName()))
                        isTableFound = true;
                    else if ((positionValue.length() >= 2) && (positionValue.substring(0, 2).equals("//")))
                        continue;
                    else if (positionValue.equals("$D.") || ((positionValue.length() > 3) && (positionValue.substring(0,4).equals("$D.{")))) {
                        valueKeyGroup = getCellValue(excelCellKey,row.getCell(excelTable.getPositionKey()));
                        if (valueKeyGroup == null)
                            throw new Exception("Ошибка определения ключа в строке " + row.getCell(excelTable.getPositionKey()).getRow() +
                                    ", в столбце " + row.getCell(excelTable.getPositionKey()).getColumnIndex() + "!");

                        if(positionValue.length() > 3 && positionValue.substring(0,4).equals("$D.{")) {
                            try {
                                replaceExcelCells = new Gson().fromJson(positionValue.substring(3, positionValue.length()), ExcelCells.class);
                            } catch (Exception e) {
                                throw new Exception("Ошибка разбора структуры excel ячеек! \n" + e.getMessage());
                            }
                            valueKeyGroup = positionValue.substring(0, 3) + valueKeyGroup + ".";
                        } else {
                            valueKeyGroup = positionValue + valueKeyGroup + ".";
                        }
                        iLevel = -1;
                    } /*else if (positionValue.equals("$D.group.")) {
                        valueKeyGroup = positionValue;
                        iLevel = -1;
                    }*/ else if (positionValue.equals("code")) {
                        valueKeyGroup = positionValue;
                        iLevel = -1;
                    } else if (positionValue.equals("level_end"))
                        valueKeyGroup = null;
                    else if (positionValue.equals("end"))
                        isTableFound = false;
                } else
                    positionValue = null;

                if (isTableFound) {
                    if ((positionValue != null) && (positionValue.substring(0,1).equals("v"))) {
                        if(positionValue.length() > 1 && positionValue.substring(0,2).equals("v{")){
                            try {
                                replaceExcelCells = new Gson().fromJson(positionValue.substring(1, positionValue.length()), ExcelCells.class);
                            } catch (Exception e) {
                                throw new Exception("Ошибка разбора структуры excel ячеек! \n" + e.getMessage());
                            }
                        }else
                            replaceExcelCells = null;

                        for (ExcelCell excelCell : excelTable.getColumns()){
                            ExcelCell v_excelCell = new ExcelCell(excelCell);
                            v_excelCell = ExcelUtil.replacementValues(v_excelCell, replaceExcelCells);

                            if(v_excelCell.getType().equals("null"))
                                continue;

                            cell = row.getCell(v_excelCell.getPosition());

                            if (cell != null) {
                                value = getCellValue(v_excelCell, cell);
                                if ((value != null) && (!value.isEmpty())) {
                                    valueKey = getCellValue(excelCellKey,row.getCell(excelTable.getPositionKey()));
                                    if (valueKey == null)
                                        throw new Exception("Ошибка определения ключа в строке " + cell.getRow() + ", в столбце " + cell.getColumnIndex() + "!");
                                    inputValues.put(excelForm.getForm() + "_" + excelTable.getName() + "*" + v_excelCell.getName() + ":" + excelTable.getKey() + ":" + valueKey, value);
                                }
                            }
                        }
                        replaceExcelCells = null;
                    } else if (valueKeyGroup != null) {
                        if ((valueKeyGroup.length() > 2) && (valueKeyGroup.substring(0,3).equals("$D."))) {
                            iLevel++;
                            if (iLevel > 0) {
                                valueKey = valueKeyGroup + iLevel.toString();

                                boolean isExistNotNumberValue = false;
                                boolean isExistNotZeroValue = false;
                                Map<String, String> inputValueByDynRows = new HashMap<String, String>();
                                for(ExcelCell excelCell : excelTable.getColumns()){
                                    ExcelCell v_excelCell = new ExcelCell(excelCell);
                                    v_excelCell = ExcelUtil.replacementValues(v_excelCell, replaceExcelCells);

                                    if(v_excelCell.getType().equals("null"))
                                        continue;

                                    cell = row.getCell(v_excelCell.getPosition());

                                    if (cell != null) {
                                        value = getCellValue(v_excelCell, cell);
                                        if ((value != null) && (!value.isEmpty())) {
                                            if (!v_excelCell.getType().substring(0, 1).equals("n"))
                                                isExistNotNumberValue = true;
                                            else if (Double.parseDouble(value) != 0)
                                                isExistNotZeroValue = true;
                                            inputValueByDynRows.put(excelForm.getForm() + "_" + excelTable.getName() + "*" + v_excelCell.getName() + ":" + excelTable.getKey() + ":" + valueKey, value);
                                            // inputValues.put(excelForm.getForm() + "_" + excelTable.getName() + "*" + excelCell.getName() + ":" + excelTable.getKey() + ":" + valueKey, value);
                                        }
                                    }
                                }
                                // Пустая числовая ячейка при загрузке заменяется на ноль. В связис этим проверяем есть ли значения в дургих не числовых ячейках, если есть добаляем строку
                                if (isExistNotNumberValue || isExistNotZeroValue)
                                    inputValues.putAll(inputValueByDynRows);
                                else
                                    iLevel--;
                                replaceExcelCells = null;
                            }
                        } else if (valueKeyGroup.equals("code")) {
                            iLevel++;
                            if (iLevel > 0) {
                                valueKey = null;

                                for(ExcelCell excelCell : excelTable.getColumns()){

                                    cell = row.getCell(excelCell.getPosition());

                                    if (cell != null) {
                                        value = getCellValue(excelCell, cell);
                                        if (excelCell.getName().equals(excelTable.getKey()))
                                            valueKey = value;
                                        else if ((value != null) && (!value.isEmpty()))
                                            inputValues.put(excelForm.getForm() + "_" + excelTable.getName() + "*" + excelCell.getName() + ":" + excelTable.getKey() + ":" + valueKey, value);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void excelCellsParsing(Row row, String positionValue) throws Exception{
        ExcelCells excelCells;
        Cell cell;
        String value;
        try {
            excelCells = new Gson().fromJson(positionValue, ExcelCells.class);
        } catch (Exception e) {
            throw new Exception("Ошибка разбора структуры excel ячеек! \n" + e.getMessage());
        }
        for(ExcelCell excelCell : excelCells.getExcelCells()){
            cell = row.getCell(excelCell.getPosition());
            if (cell != null) {
                value = getCellValue(excelCell, cell);
                inputValues.put(excelForm.getForm() + "*" + excelCell.getName() + "::", value);
            }
        }
    }

    private String getCellValue(ExcelCell excelCell, Cell cell) throws Exception {
        String result = null;
        try {
            if (excelCell.getType().equals("s")) {
                try {
                    result = cell.getStringCellValue();
                } catch (Exception e) {
                    result = n6f.format(cell.getNumericCellValue());
                }
                if ((excelCell.getRefName() != null) && (!result.isEmpty())) {
                    if(excelCell.getMultiValue()!=null && excelCell.getMultiValue()){
                        String[] codes = result.split(",");
                        MultiSelectValue multiValue = new MultiSelectValue();
                        multiValue.setValues(new ArrayList<String>());
                        for (String code : codes) {
                            code = code.trim();
                            Long recId = reference.getRefRecId(excelCell.getRefName(), excelCell.getRefColumn(), code, reportDate);
                            multiValue.getValues().add(recId.toString());
                        }
                        result = gson.toJson(multiValue, MultiSelectValue.class);
                    } else {
                        Long recId = reference.getRefRecId(excelCell.getRefName(), excelCell.getRefColumn(), result, reportDate);
                        /*if (excelCell.getFilterType().equals("pick")) {
                            if (recId == -1)
                                result = "не найден в справочнике";
                            else if(excelCell.getType().equalsIgnoreCase("string")){
                                result = persistence.getRefOriginalValue(excelCell.getRefName(), excelCell.getRefColumn(), result, reportDate);
                            }
                        } else*/
                            result = recId.toString();
                    }
                }
            }
            else if (excelCell.getType().equals("d")) {
                if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    String val = cell.getStringCellValue();
                    if (val != null) {
                        val = val.trim();
                    }
                    if(val!=null && !val.isEmpty()){
                        Date d = sdf.parse(val);
                        result = sdf.format(d);
                    } else {
                        result = "";
                    }
                } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    result = (cell.getDateCellValue() != null) ? sdf.format(cell.getDateCellValue()) : "";
                } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                    result = "";
                } else {
                    logger.error("Формат даты не верный: " + cell.getCellType());
                    throw new Exception("Формат даты не верный");
                }
            }else if (excelCell.getType().equals("t"))
                result = (cell.getDateCellValue() != null)?stf.format(cell.getDateCellValue()):"";
            else if (excelCell.getType().substring(0, 1).equals("n")) {
                double value;
                if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    String strValue = cell.getStringCellValue();
                    if (strValue != null) {
                        strValue = strValue.trim();
                        strValue = strValue.replace(",", ".").replace(" ", "");
                    }
                    value = Double.parseDouble(strValue);
                } else {
                    value = cell.getNumericCellValue();
                }
                /*if (value == 0)
                    // Для пустой ячейки возвращается значение 0
                    result = "";
                else {*/
                    if (excelCell.getType().equals("n0")) {
                        result = n0f.format(value);
                    } else if (excelCell.getType().equals("n1")) {
                        result = n1f.format(value);
                    }
                    else if (excelCell.getType().equals("n2")){
                        result = n2f.format(value);
                    }
                    else if (excelCell.getType().equals("n3")){
                        result = n3f.format(value);
                    }
                    else if (excelCell.getType().equals("n4")){
                        result = n4f.format(value);
                    }
                    else if (excelCell.getType().equals("n5")){
                        result = n5f.format(value);
                    }
                    else if (excelCell.getType().equals("n6")){
                        result = n6f.format(value);
                    }
                    else if (excelCell.getType().equals("n7")){
                        result = n7f.format(value);
                    }
                    else if (excelCell.getType().equals("n8")){
                        result = n8f.format(value);
                    }
                    result = result.replace(',', '.');

                    if (!Validators.isValidMask(result, excelCell.getType()))
                        throw new Exception();
                // }
            }
        } catch (Exception e) {
            int rowIndex = cell.getRowIndex() + 1;
            int columnIndex = cell.getColumnIndex() + 1;
            throw new Exception("Ошибка разбора значения в строке " + rowIndex + ", в столбце " + columnIndex + "!");
        }

        return result;
    }

    public String getFormName() {
        return formName;
    }

    public Map<String, String> getInputValues() {
        return inputValues;
    }

    public ExcelForm getExcelForm() {
        return excelForm;
    }
}
