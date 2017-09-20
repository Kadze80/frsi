package form.excel;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataform.DateEval;
import dataform.FormulaSyntaxError;
import ejb.PeriodType;
import ejb.Reference;
import entities.*;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import util.Convert;
import util.PeriodUtil;

import javax.ejb.EJBException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.*;
import java.util.*;

/**
 * Created by Nuriddin.Baideuov on 22.06.2015.
 */
public class ExcelDataFiller implements Serializable {

    private String formName;
    XSSFWorkbook workbook;
    private Sheet sheet;
    private ExcelForm excelForm;
    private DecimalFormat n6f;
    private IDataProvider dataProvider;
    private String respondentName;
    private Date reportDate;
    private Reference reference;
    private static final String[] emptyFooterItemValues = {"", "«_____» __________ 20_____", ""};
    private int bodyDataLastRow;
    private int dynamicRowNum;
    private int currRowNum;
    private int maxRowNum;
    private SortedSet<Integer> deletingRowNumbers;
    private int maxColumnNum;
    private Date startDate;
    private Date endDate;
    private PeriodType periodType;
    private SortedSet<Integer> cellKeys = new TreeSet<Integer>();
    private SimpleDateFormat dfShow = new SimpleDateFormat("dd.MM.yyyy");
    private SimpleDateFormat dfParse = new SimpleDateFormat("yyyyMMdd");
    private DateEval dateEval = new DateEval();
    private Boolean haveParams;

    public ExcelDataFiller(byte[] xlsFile, String respondentName, Date reportDate, IDataProvider dataProvider, Reference reference, Date startDate, Date endDate, PeriodType periodType) throws Exception {
        this.dataProvider = dataProvider;
        this.reportDate = reportDate;
        this.respondentName = respondentName;
        this.reference = reference;

        this.startDate = startDate;
        this.endDate = endDate;
        this.periodType = periodType;

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

        if (startDate != null && endDate != null && periodType != null) {
            DateTimeZone dtZone = DateTimeZone.forTimeZone(TimeZone.getDefault());
            LocalDate localeStartDate = PeriodUtil.floor(new LocalDate(this.startDate, dtZone), periodType);
            LocalDate localEndDate = PeriodUtil.floor(new LocalDate(this.endDate, dtZone), periodType);
            LocalDate interDate = localeStartDate;
            while (interDate.compareTo(localEndDate) <= 0) {
                cellKeys.add(Integer.parseInt(interDate.toString("yyyyMMdd")));
                interDate = PeriodUtil.plusPeriod(interDate, periodType, 1);
            }
        }

        if(sheet.getRow(1).getCell(0).getStringCellValue().trim().equals("begin"))
            haveParams = true;
    }

    public void process() throws Exception {
        deletingRowNumbers = new TreeSet<Integer>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });

        Row row;
        String positionValue;
        boolean isTableFound;
        Integer iLevel = null;
        String valueKeyGroup;
        ExcelTable excelTable;
        maxRowNum = sheet.getLastRowNum();
        ExcelCells replaceExcelCells = null;

        for (int i = 0; i < excelForm.getTables().length; i++) {
            dynamicRowNum = 0;
            excelTable = excelForm.getTable(i);
            maxColumnNum = -1;
            isTableFound = false;
            valueKeyGroup = null;
            currRowNum = 0;
            while (currRowNum <= maxRowNum) {

                row = sheet.getRow(currRowNum);
                if (row == null) {
                    currRowNum++;
                    continue;
                }

                if(haveParams && row.getRowNum() > 1)
                    searchParams(row);

                Cell cell = row.getCell(excelTable.getPosition());
                if (cell != null) {
                    positionValue = cell.getStringCellValue().trim();
                    if (positionValue.equals(excelTable.getName())) {
                        isTableFound = true;
                        dataProvider.setCurrentContainer(formName + "_" + excelTable.getName());
                    }
                    else if ((positionValue != null) && (!positionValue.equals("")) && (positionValue.length() >= 2) && (positionValue.substring(0, 2).equals("//")))
                        continue;
                    else if (positionValue.equals("$D.") || ((positionValue.length() > 3) && (positionValue.substring(0,4).equals("$D.{")))) {
                        valueKeyGroup = getCellStringValue(row.getCell(excelTable.getPositionKey()));
                        if (valueKeyGroup == null){
                            throw new Exception("Ошибка определения ключа в строке " + row.getCell(excelTable.getPositionKey()).getRow() +
                                    ", в столбце " + row.getCell(excelTable.getPositionKey()).getColumnIndex() + "!");
                        }

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
                    }
                    /*else if (positionValue.equals("$D.group.")) {
                        valueKeyGroup = positionValue;
                        iLevel = -1;
                    }*/
                    else if (positionValue.equals("code")) {
                        valueKeyGroup = positionValue;
                        iLevel = -1;
                    }
                    else if (positionValue.equals("end")) {
                        if (row.getRowNum() > bodyDataLastRow)
                            bodyDataLastRow = row.getRowNum();
                        isTableFound = false;
                    }
                } else
                    positionValue = null;

                if (isTableFound) {
                    if (positionValue != null && !positionValue.equals("") && positionValue.substring(0,1).equals("v")) {
                        processStaticRow(positionValue, excelTable, row);
                    } else if ((positionValue != null) && (positionValue.equals("h"))) {
                        processTableHeaderRow(excelTable, row);
                    } else if (valueKeyGroup != null) {
                        if ((valueKeyGroup.length() > 2) && (valueKeyGroup.substring(0, 3).equals("$D."))) {
                            iLevel++;
                            if (iLevel > 0) {
                                processDynamicRow(excelTable, valueKeyGroup, row, replaceExcelCells);
                                valueKeyGroup = null;
                            } else {
                                processStaticRow(positionValue, excelTable, row);
                            }
                        } else if (valueKeyGroup.equals("code")) {
                            iLevel++;
                            if (iLevel > 0) {
                                processBalanceAccountRow(excelTable, row);
                            }
                        }
                    } else if ((positionValue != null) && (positionValue.equals("t"))) {
                        processTotalRow(excelTable, row);
                    }
                }
                currRowNum++;
            }
        }

        for(Integer rowNum: deletingRowNumbers){
            removeRow(rowNum, 1);
        }
    }

    private void processTableHeaderRow(ExcelTable excelTable, Row row) throws Exception{
        ExcelCell excelCell;
        for (int j = 0; j < excelTable.getHeaders().length; j++) {

            excelCell = excelTable.getHeaders()[j];

            if (excelCell.getName().contains("@DynamicCellDate")) {
                int exp = 0;
                for (Integer cellKey : cellKeys) {
                    Date date;
                    try {
                        date = dfParse.parse(Integer.toString(cellKey));
                    } catch (ParseException e) {
                        continue;
                    }
                    String cellValue = excelCell.getName().replace("@DynamicCellDate", dfShow.format(date));
                    setCellValue(row, excelCell.getPosition() + exp, excelCell.getType(), cellValue);
                    if (exp > 0) {
                        Cell sourceCell = row.getCell(excelCell.getPosition());
                        Cell targetCell = row.getCell(excelCell.getPosition() + exp);
                        if (targetCell == null) {
                            targetCell = row.createCell(excelCell.getPosition() + exp);
                        }
                        ExcelHelper.cloneStyle(sourceCell, targetCell);
                    }
                    exp++;
                }
            }
        }
    }

    private void hideRow(Row row) {
        deletingRowNumbers.add(row.getRowNum());
    }

    private void removeRow(int rowIndex, int rowCount) {

        /*for (int r = 0; r < sheet.getNumMergedRegions(); r++) {
            CellRangeAddress address = sheet.getMergedRegion(r);
            if (address.getFirstRow() <= rowIndex && address.getLastRow() >= rowIndex) {
                sheet.removeMergedRegion(r);
            }
        }*/

        int lastRowNum = sheet.getLastRowNum();
        if (rowIndex >= 0 && rowIndex < lastRowNum) {
            sheet.shiftRows(rowIndex + 1, lastRowNum, -1 * rowCount);
        }
        if (rowIndex == lastRowNum) {
            Row removingRow = sheet.getRow(rowIndex);
            if (removingRow != null) {
                sheet.removeRow(removingRow);
            }
        }
    }

    private Row addRow(int rowIndex, int columnCount){
        Row newRow = null;
        int lastRowNum = sheet.getLastRowNum();
        if (rowIndex >= 0 && rowIndex < lastRowNum) {
            sheet.shiftRows(rowIndex, lastRowNum, 1);
            sheet.createRow(rowIndex);
            maxRowNum++;
        }

        Row currRow = sheet.getRow(rowIndex - 1);
        if (currRow != null) {
            newRow = sheet.getRow(rowIndex);

            for (int colIndex = 0; colIndex <= columnCount; colIndex++) {
                Cell sourceCell = currRow.getCell(colIndex);
                if (sourceCell != null) {
                    CellStyle style = sourceCell.getCellStyle();
                    Cell targetCell = newRow.getCell(colIndex);
                    if (targetCell == null)
                        targetCell = newRow.createCell(colIndex);
                    targetCell.setCellStyle(style);
                }
            }
        }
        return newRow;
    }

    private void processStaticRow(String positionValue, ExcelTable excelTable, Row row) throws Exception {
        String valueKey;
        ExcelCells replaceExcelCells;

        valueKey = getCellStringValue(row.getCell(excelTable.getPositionKey()));

        if(positionValue.length() > 1 && positionValue.substring(0,2).equals("v{")){
            try {
                replaceExcelCells = new Gson().fromJson(positionValue.substring(1, positionValue.length()), ExcelCells.class);
            } catch (Exception e) {
                throw new Exception("Ошибка разбора структуры excel ячеек! \n" + e.getMessage());
            }
        }else
            replaceExcelCells = null;


        for (ExcelCell excelCell : excelTable.getColumns()) {
            ExcelCell v_excelCell = new ExcelCell(excelCell);
            v_excelCell = ExcelUtil.replacementValues(v_excelCell, replaceExcelCells);

            if(v_excelCell.getType().equals("null"))
                continue;

            if (v_excelCell.getName().contains("@DynamicCellId")) {
                int exp = 0;
                for (Integer cellKey : cellKeys) {
                    String fieldName = v_excelCell.getName().replace("@DynamicCellId", "@" + Integer.toString(cellKey));
                    String path = excelForm.getForm() + "_" + excelTable.getName() + "*" + fieldName + ":" + excelTable.getKey() + ":" + valueKey;
                    setCellValue(row, v_excelCell.getPosition() + exp, v_excelCell.getType(), adaptValue(dataProvider.getAllData().get(path), v_excelCell));
                    if (exp > 0) {
                        Cell sourceCell = row.getCell(v_excelCell.getPosition());
                        Cell targetCell = row.getCell(v_excelCell.getPosition() + exp);
                        if (targetCell == null) {
                            targetCell = row.createCell(v_excelCell.getPosition() + exp);
                        }
                        ExcelHelper.cloneStyle(sourceCell, targetCell);
                    }
                    exp++;
                }
            } else {
                String path = excelForm.getForm() + "_" + excelTable.getName() + "*" + v_excelCell.getName() + ":" + excelTable.getKey() + ":" + valueKey;
                String value = adaptValue(dataProvider.getAllData().get(path), v_excelCell);
                if(value != null)
                    setCellValue(row, v_excelCell.getPosition(), v_excelCell.getType(), adaptValue(dataProvider.getAllData().get(path), v_excelCell));
            }
        }
    }

    private void processTotalRow(ExcelTable excelTable, Row row) throws Exception {
        // Cell cell;
        String valueKey = excelTable.getTotal();
        ExcelCell excelCell;
        for (int j = 0; j < excelTable.getColumns().length; j++) {
            excelCell = excelTable.getColumn(j);

            //cell = row.getCell(excelCell.getPosition());

            String path = excelForm.getForm() + "_" + excelTable.getName() + "*" + excelCell.getName() + ":" + excelTable.getKey() + ":" + valueKey;
            /*if (cell == null)
                cell = row.createCell(excelCell.getPosition(), Cell.CELL_TYPE_STRING);
            setCellValue(cell, dataProvider.getAllData().get(path));*/
            setCellValue(row, excelCell.getPosition(), excelCell.getType(), adaptValue(dataProvider.getAllData().get(path), excelCell));
        }
    }

    private void processDynamicRow(ExcelTable excelTable, String valueKeyGroup, Row row, ExcelCells replaceExcelCells) {
        String valueKey;
        int count = 0;
        while (dataProvider.hasNextRec(valueKeyGroup)){
            Map<String, String> kvMap = dataProvider.nextRecData(valueKeyGroup);
            if (kvMap.size() > 0) {
                String key = kvMap.keySet().iterator().next();
                valueKey = key.substring(key.lastIndexOf(":") + 1);
            } else {
                continue;
            }
            if (count > 0) {
                row = addRow(currRowNum + count, getMaxColumnNum(excelTable));
            }

            String cellValue;
            if (excelTable.getLevelRn() != 0) {
                int n = 0;
                cellValue = null;
                for (int i = 0; i < valueKey.length(); i++) {
                    if (valueKey.charAt(i) == '.') {
                        n++;
                        if (n == excelTable.getLevelRn()) {
                            cellValue = valueKey.substring(i + 1);
                            break;
                        }
                    }
                }
             }
            else if (valueKey.startsWith("$D.group."))
                cellValue = Integer.toString(++dynamicRowNum);
            else
                cellValue = valueKey.substring("$D.".length());

            setCellValue(row, excelTable.getPositionRn() != 0? excelTable.getPositionRn() : excelTable.getPositionKey(), "s", adaptValue(cellValue, new ExcelCell()));

            for (ExcelCell excelCell : excelTable.getColumns()) {
                ExcelCell v_excelCell = new ExcelCell(excelCell);
                v_excelCell = ExcelUtil.replacementValues(v_excelCell, replaceExcelCells);

                if(v_excelCell.getType().equals("null"))
                    continue;

                String path = excelForm.getForm() + "_" + excelTable.getName() + "*" + v_excelCell.getName() + ":" + excelTable.getKey() + ":" + valueKey;
                cellValue = kvMap.get(path);

                setCellValue(row, v_excelCell.getPosition(), v_excelCell.getType(), adaptValue(cellValue, v_excelCell));
            }
            replaceExcelCells = null;
            count++;
        }
        if (count == 0) {
            hideRow(row);
        } else
            currRowNum += count - 1;
    }

    private int getMaxColumnNum(ExcelTable excelTable){
        if (maxColumnNum == -1) {
            for (ExcelCell excelCell : excelTable.getColumns()) {
                if (maxColumnNum < excelCell.getPosition())
                    maxColumnNum = excelCell.getPosition();
            }
        }
        return maxColumnNum;
    }

    private void processBalanceAccountRow(ExcelTable excelTable, Row row) {
        Cell cell;
        String valueKey;
        if (!dataProvider.hasNextRec(null)) {
            hideRow(row);
        } else {
            Map<String, String> kvMap = dataProvider.nextRecData(null);
            if (kvMap.size() > 0) {
                String key = kvMap.keySet().iterator().next();
                valueKey = key.substring(key.lastIndexOf(":") + 1);
            } else
                return;

            setCellValue(row, excelTable.getPositionKey(), "s", valueKey);

            for (ExcelCell excelCell : excelTable.getColumns()) {
                cell = row.getCell(excelCell.getPosition());
                if (cell != null) {
                    String path = excelForm.getForm() + "_" + excelTable.getName() + "*" + excelCell.getName() + ":" + excelTable.getKey() + ":" + valueKey;
                    /*if (cell == null)
                        cell = row.createCell(excelCell.getPosition(), Cell.CELL_TYPE_STRING);
                    setCellValue(cell, adaptValue(kvMap.get(path), excelCell));*/
                    setCellValue(row, excelCell.getPosition(), excelCell.getType(), adaptValue(kvMap.get(path), excelCell));
                }
            }
        }
    }

    /*private void processTableRow(Row row, String positionValue, String tableName, String key) throws Exception {
        ExcelCells excelCells;
        Cell cell;
        try {
            excelCells = new Gson().fromJson(positionValue, ExcelCells.class);
        } catch (Exception e) {
            throw new Exception("Ошибка разбора структуры excel ячеек! \n" + e.getMessage());
        }
        for (ExcelCell excelCell : excelCells.getExcelCells()) {
            String path;
            if (excelCell.getKeyValue() != null && !excelCell.getKeyValue().isEmpty())
                path = excelForm.getForm() + "_" + tableName + "*" + excelCell.getName() + ":" + key + ":" + excelCell.getKeyValue();
            else
                path = excelForm.getForm() + "*" + excelCell.getName() + "::";

            setCellValue(row, excelCell.getPosition(), excelCell.getType(), adaptValue(dataProvider.getAllData().get(path), excelCell));
        }
    }*/

    private Boolean isSignParam(String paramValue){
        if(paramValue.equalsIgnoreCase("signature_date") ||
           paramValue.equalsIgnoreCase("ceo") ||
           paramValue.equalsIgnoreCase("chief_accountant") ||
           paramValue.equalsIgnoreCase("drafted_by"))
            return true;
        else
            return false;
    }

    private void searchParams(Row row) throws Exception {
        Cell cell = row.getCell(0);
        if (cell != null) {
            String positionValue = cell.getStringCellValue().trim();
            ExcelCells excelCells = null;

            if(!positionValue.equals("begin") && !positionValue.equals("end")) {
                try {
                    excelCells = new Gson().fromJson(positionValue, ExcelCells.class);
                } catch (Exception e) {
                    throw new Exception("Ошибка разбора структуры excel ячеек! \n" + e.getMessage());
                }
            }

            if (excelCells != null) {
                for (ExcelCell excelCell : excelCells.getExcelCells()) {
                    String cellName = excelCell.getName();

                    String value = null;

                    if (cellName.equalsIgnoreCase("respondentname") || cellName.equalsIgnoreCase("reportdate") || cellName.equalsIgnoreCase("respondentinfo")) {
                        if (cellName.equalsIgnoreCase("respondentname") || cellName.equalsIgnoreCase("respondentinfo")) {
                            value = respondentName;
                        } else if (cellName.equalsIgnoreCase("reportdate")) {
                            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                            value = dateFormat.format(reportDate);
                        }
                        if (value == null)
                            value = "_____________________________";
                    } else if (cellName.toLowerCase().startsWith("reportdate(")) {
                        if (!cellName.endsWith(")"))
                            throw new Exception(MessageFormat.format("Ошибка при разборе выражения {0}", cellName));
                        else {
                            try {
                                Date d = dateEval.eval(cellName.substring(cellName.indexOf("(") + 1, cellName.lastIndexOf(")")), reportDate);
                                value = Convert.dateFormatRus.format(d);
                            } catch (FormulaSyntaxError ex) {
                                throw new EJBException(MessageFormat.format("Ошибка при разборе выражения {0}", cellName));
                            }
                        }
                    } else {
                        String path = excelForm.getForm() + "*" + excelCell.getName() + "::";
                        value = dataProvider.getAllData().get(path);

                        if (cellName.equalsIgnoreCase("signature_date") && (value == null || value.isEmpty()))
                            value = emptyFooterItemValues[1];
                        else if (isSignParam(cellName) && (value == null || value.isEmpty() || value.equals("-") || value.equalsIgnoreCase("Не предусмотрено"))) {
                            value = emptyFooterItemValues[0];
                        }
                    }

                    if (value == null)
                        value = emptyFooterItemValues[0];

                    value = formatValue(value, excelCell.getType());

                    cell = row.getCell(excelCell.getPosition());
                    if (cell != null) {
                        short fontIndex = cell.getCellStyle().getFontIndex();
                        Font font = workbook.getFontAt(fontIndex);

                        if (isSignParam(cellName)) {
                            Font underlineFont = workbook.createFont();
                            if (!excelCell.getName().toLowerCase().equals("sign"))
                                underlineFont.setUnderline(XSSFFont.U_SINGLE);

                            underlineFont.setFontHeight(font.getFontHeight());
                            underlineFont.setFontName(font.getFontName());
                            underlineFont.setBold(font.getBold());
                            underlineFont.setItalic(font.getItalic());
                        }

                        String initText = cell.getStringCellValue();
                        String searchText = "[" + excelCell.getName() + "]";

                        List<Integer> indexes = new ArrayList<Integer>();
                        String newText;

                        if (excelCell.getName().toLowerCase().equals("sign") && value.trim().isEmpty())
                            newText = " ";
                        else {
                            StringBuilder sb = new StringBuilder(initText);
                            int index = sb.indexOf(searchText);
                            while (index > -1) {
                                sb.replace(index, index + searchText.length(), value);
                                indexes.add(index);
                                index = sb.indexOf(searchText);
                            }
                            newText = sb.toString();
                        }
                        RichTextString richString = new XSSFRichTextString(newText);
                        richString.applyFont(font);

                        cell.setCellValue(richString);
                    }
                }
            }
        }
    }

    private String formatValue(String value, String cellType){
        String formattedValue = value;
        if (cellType.toLowerCase().startsWith("number")) {
            String d = cellType.substring("number".length());
            DecimalFormatSymbols unusualSymbols = new DecimalFormatSymbols();
            unusualSymbols.setGroupingSeparator(' ');
            DecimalFormat formatter = new DecimalFormat();
            formatter.setDecimalFormatSymbols(unusualSymbols);
            try {
                int decimalNumb = Integer.parseInt(d);
                if (decimalNumb == 0) {
                    int intValue = Integer.parseInt(value);
                    formattedValue = formatter.format(intValue);
                } else {
                    double doubleValue = Double.parseDouble(value);
                    formattedValue = formatter.format(doubleValue);
                }
            } catch (NumberFormatException e) {
            }
        }
        return formattedValue;
    }

    private void processFooter() throws Exception {
        boolean footerFound = false;
        Iterator<Row> rows = sheet.rowIterator();
        while (rows.hasNext()) {
            Row row = rows.next();
            Cell cell = row.getCell(0);
            if (cell != null) {
                String positionValue = cell.getStringCellValue().trim();
                if (positionValue.equals("footer"))
                    footerFound = true;
                else if (positionValue.equals("footer_end"))
                    break;

                if (footerFound && positionValue.startsWith("{")) {
                    ExcelCells excelCells;
                    try {
                        excelCells = new Gson().fromJson(positionValue, ExcelCells.class);
                    } catch (Exception e) {
                        throw new Exception("Ошибка разбора структуры excel ячеек! \n" + e.getMessage());
                    }
                    for (ExcelCell excelCell : excelCells.getExcelCells()) {
                        String path = excelForm.getForm() + "*" + excelCell.getName() + "::";
                        String value = dataProvider.getAllData().get(path);

                        String cellName = excelCell.getName();
                        if (cellName.equalsIgnoreCase("signature_date") && (value == null || value.isEmpty()))
                            value = emptyFooterItemValues[1];
                        else if ((cellName.equalsIgnoreCase("ceo") || cellName.equalsIgnoreCase("chief_accountant") || cellName.equalsIgnoreCase("drafted_by") || cellName.equalsIgnoreCase("ceo"))
                                && (value == null || value.isEmpty() || value.equals("-") || value.equalsIgnoreCase("Не предусмотрено"))) {
                            value = emptyFooterItemValues[0];
                        }
                        if (value == null)
                            value = "";

                        cell = row.getCell(excelCell.getPosition());
                        if (cell != null) {
                            short fontIndex = cell.getCellStyle().getFontIndex();
                            Font font = workbook.getFontAt(fontIndex);

                            Font underlineFont = workbook.createFont();
                            if (!excelCell.getName().toLowerCase().equals("sign"))
                                underlineFont.setUnderline(XSSFFont.U_SINGLE);

                            underlineFont.setFontHeight(font.getFontHeight());
                            underlineFont.setFontName(font.getFontName());
                            underlineFont.setBold(font.getBold());
                            underlineFont.setItalic(font.getItalic());

                            String initText = cell.getStringCellValue();
                            String searchText = "[" + excelCell.getName() + "]";

                            List<Integer> indexes = new ArrayList<Integer>();
                            String newText;

                            if(excelCell.getName().toLowerCase().equals("sign") && value.trim().isEmpty())
                                newText = " ";
                            else {
                                StringBuilder sb = new StringBuilder(initText);
                                int index = sb.indexOf(searchText);
                                while (index > -1) {
                                    sb.replace(index, index + searchText.length(), value);


                                    indexes.add(index);
                                    index = sb.indexOf(searchText);
                                }
                                newText = sb.toString();
                            }
                            RichTextString richString = new XSSFRichTextString(newText);
                            richString.applyFont(font);

                            cell.setCellValue(richString);
                        }
                    }
                }
            }
        }
    }

    private String adaptValue(String value, ExcelCell cell) {
        if (value == null)
            return value;

        if (cell.getRefName() != null && cell.getRefColumn() != null
                /*&& cell.getFilterType() != null && cell.getFilterType().toLowerCase().equals("list")*/) {
            if (cell.getMultiValue() != null && cell.getMultiValue()) {
                try {
                    MultiSelectValue multiSelectValue = gson.fromJson(value, MultiSelectValue.class);
                    if(multiSelectValue!=null) {
                        value = "";
                        for (int i = 0; i < multiSelectValue.getValues().size(); i++) {
                            String v = multiSelectValue.getValues().get(i);
                            try {
                                Long recId = Long.parseLong(v);
                                if (recId == -1)
                                    v = "не найден в справочнике";
                                else
                                    v = reference.getRefItemNameByRecId(cell.getRefName(), cell.getRefColumn(), recId, reportDate);
                            } catch (NumberFormatException e) {
                            }
                            if (!value.isEmpty())
                                value += ", ";
                            value += v;
                        }
                    }
                } catch (JsonSyntaxException e) {
                }
            } else {
                try {
                    Long recId = Long.parseLong(value);
                    value = reference.getRefItemNameByRecId(cell.getRefName(), cell.getRefColumn(), recId, reportDate);
                } catch (NumberFormatException e) {
                    return value;
                }
            }
        }
        return value;
    }

    private void setCellValue(Cell cell, String value) {
        if (value != null)
            cell.setCellValue(value);
    }

    private void setCellValue(Row row, int cellNum, String cellType, String value) {
        if (value == null)
            return;

        if (cellType == null)
            cellType = "s";

        Cell cell = row.getCell(cellNum);
        if (cell == null) {
            cell = row.createCell(cellNum);
        }

        if (cellType.toLowerCase().equals("s")) {
            cell.setCellValue(value);
        } else if (cellType.toLowerCase().startsWith("n")) {
            String d = cellType.substring("n".length());
            try {
                int decimalNumb = Integer.parseInt(d);
                if (decimalNumb == 0) {
                    Double doubleValue = Double.parseDouble(value);
                    long longValue = doubleValue.longValue();
                    cell.setCellValue(longValue);
                } else {
                    double doubleValue = Double.parseDouble(value);
                    cell.setCellValue(doubleValue);
                }
            } catch (NumberFormatException e) {
                cell.setCellValue(value);
            }
        } else if (cellType.toLowerCase().equals("d")) {
            DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            try {
                Date date = format.parse(value);
                cell.setCellValue(date);
            } catch (ParseException e) {
                cell.setCellValue(value);
            }
        } else {
            cell.setCellValue(value);
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

    private static Gson gson = new Gson();
    private static String strData = "{\"fs_repo*ceo::\":\"Не предусмотрено\",\"fs_repo*chief_accountant::\":\"Не предусмотрено\",\"fs_repo*drafted_by::\":\"Не предусмотрено\",\"fs_repo*signature_date::\":\"01.06.2015\",\"fs_repo_array*countcb_repo:num:$D.1.1.1.1\":\"10\",\"fs_repo_array*countcb_repo:num:$D.1.1.2.1\":\"70\",\"fs_repo_array*countcb_repo:num:$D.1.1.2.2\":\"130\",\"fs_repo_array*countcb_repo:num:1\":\"210\",\"fs_repo_array*countcb_repo:num:1.1\":\"210\",\"fs_repo_array*countcb_repo:num:1.1.1\":\"10\",\"fs_repo_array*countcb_repo:num:1.1.2\":\"200\",\"fs_repo_array*countcb_repo:num:1.2\":\"0\",\"fs_repo_array*countcb_repo:num:2\":\"0\",\"fs_repo_array*countcb_repo:num:2.1\":\"0\",\"fs_repo_array*countcb_repo:num:2.1.1\":\"0\",\"fs_repo_array*countcb_repo:num:2.1.2\":\"0\",\"fs_repo_array*countcb_repo:num:2.2\":\"0\",\"fs_repo_array*date_closerepo:num:$D.1.1.1.1\":\"\",\"fs_repo_array*date_closerepo:num:$D.1.1.2.1\":\"\",\"fs_repo_array*date_closerepo:num:$D.1.1.2.2\":\"\",\"fs_repo_array*date_openrepo:num:$D.1.1.1.1\":\"\",\"fs_repo_array*date_openrepo:num:$D.1.1.2.1\":\"\",\"fs_repo_array*date_openrepo:num:$D.1.1.2.2\":\"\",\"fs_repo_array*date_prolong:num:$D.1.1.1.1\":\"\",\"fs_repo_array*date_prolong:num:$D.1.1.2.1\":\"\",\"fs_repo_array*date_prolong:num:$D.1.1.2.2\":\"\",\"fs_repo_array*datesattl_repo:num:$D.1.1.1.1\":\"\",\"fs_repo_array*datesattl_repo:num:$D.1.1.2.1\":\"\",\"fs_repo_array*datesattl_repo:num:$D.1.1.2.2\":\"\",\"fs_repo_array*dcf:num:$D.1.1.1.1\":\"50\",\"fs_repo_array*dcf:num:$D.1.1.2.1\":\"110\",\"fs_repo_array*dcf:num:$D.1.1.2.2\":\"170\",\"fs_repo_array*dcf:num:1\":\"330\",\"fs_repo_array*dcf:num:1.1\":\"330\",\"fs_repo_array*dcf:num:1.1.1\":\"50\",\"fs_repo_array*dcf:num:1.1.2\":\"280\",\"fs_repo_array*dcf:num:1.2\":\"0\",\"fs_repo_array*dcf:num:2\":\"0\",\"fs_repo_array*dcf:num:2.1\":\"0\",\"fs_repo_array*dcf:num:2.1.1\":\"0\",\"fs_repo_array*dcf:num:2.1.2\":\"0\",\"fs_repo_array*dcf:num:2.2\":\"0\",\"fs_repo_array*deadlineoper_repo:num:$D.1.1.1.1\":\"0\",\"fs_repo_array*deadlineoper_repo:num:$D.1.1.2.1\":\"0\",\"fs_repo_array*deadlineoper_repo:num:$D.1.1.2.2\":\"0\",\"fs_repo_array*fee_rate:num:$D.1.1.1.1\":\"0\",\"fs_repo_array*fee_rate:num:$D.1.1.2.1\":\"0\",\"fs_repo_array*fee_rate:num:$D.1.1.2.2\":\"0\",\"fs_repo_array*name_contr:num:$D.1.1.1.1\":\"\",\"fs_repo_array*name_contr:num:$D.1.1.2.1\":\"\",\"fs_repo_array*name_contr:num:$D.1.1.2.2\":\"\",\"fs_repo_array*nin_iin:num:$D.1.1.1.1\":\"\",\"fs_repo_array*nin_iin:num:$D.1.1.2.1\":\"\",\"fs_repo_array*nin_iin:num:$D.1.1.2.2\":\"\",\"fs_repo_array*note:num:$D.1.1.1.1\":\"\",\"fs_repo_array*note:num:$D.1.1.2.1\":\"\",\"fs_repo_array*note:num:$D.1.1.2.2\":\"\",\"fs_repo_array*num_doc:num:$D.1.1.1.1\":\"\",\"fs_repo_array*num_doc:num:$D.1.1.2.1\":\"\",\"fs_repo_array*num_doc:num:$D.1.1.2.2\":\"\",\"fs_repo_array*prov_intstand:num:$D.1.1.1.1\":\"60\",\"fs_repo_array*prov_intstand:num:$D.1.1.2.1\":\"120\",\"fs_repo_array*prov_intstand:num:$D.1.1.2.2\":\"180\",\"fs_repo_array*prov_intstand:num:1\":\"360\",\"fs_repo_array*prov_intstand:num:1.1\":\"360\",\"fs_repo_array*prov_intstand:num:1.1.1\":\"60\",\"fs_repo_array*prov_intstand:num:1.1.2\":\"300\",\"fs_repo_array*prov_intstand:num:1.2\":\"0\",\"fs_repo_array*prov_intstand:num:2\":\"0\",\"fs_repo_array*prov_intstand:num:2.1\":\"0\",\"fs_repo_array*prov_intstand:num:2.1.1\":\"0\",\"fs_repo_array*prov_intstand:num:2.1.2\":\"0\",\"fs_repo_array*prov_intstand:num:2.2\":\"0\",\"fs_repo_array*reward:num:$D.1.1.1.1\":\"40\",\"fs_repo_array*reward:num:$D.1.1.2.1\":\"100\",\"fs_repo_array*reward:num:$D.1.1.2.2\":\"160\",\"fs_repo_array*reward:num:1\":\"300\",\"fs_repo_array*reward:num:1.1\":\"300\",\"fs_repo_array*reward:num:1.1.1\":\"40\",\"fs_repo_array*reward:num:1.1.2\":\"260\",\"fs_repo_array*reward:num:1.2\":\"0\",\"fs_repo_array*reward:num:2\":\"0\",\"fs_repo_array*reward:num:2.1\":\"0\",\"fs_repo_array*reward:num:2.1.1\":\"0\",\"fs_repo_array*reward:num:2.1.2\":\"0\",\"fs_repo_array*reward:num:2.2\":\"0\",\"fs_repo_array*sumrepo_thouscurr:num:$D.1.1.1.1\":\"30\",\"fs_repo_array*sumrepo_thouscurr:num:$D.1.1.2.1\":\"90\",\"fs_repo_array*sumrepo_thouscurr:num:$D.1.1.2.2\":\"150\",\"fs_repo_array*sumrepo_thouscurr:num:1\":\"270\",\"fs_repo_array*sumrepo_thouscurr:num:1.1\":\"270\",\"fs_repo_array*sumrepo_thouscurr:num:1.1.1\":\"30\",\"fs_repo_array*sumrepo_thouscurr:num:1.1.2\":\"240\",\"fs_repo_array*sumrepo_thouscurr:num:1.2\":\"0\",\"fs_repo_array*sumrepo_thouscurr:num:2\":\"0\",\"fs_repo_array*sumrepo_thouscurr:num:2.1\":\"0\",\"fs_repo_array*sumrepo_thouscurr:num:2.1.1\":\"0\",\"fs_repo_array*sumrepo_thouscurr:num:2.1.2\":\"0\",\"fs_repo_array*sumrepo_thouscurr:num:2.2\":\"0\",\"fs_repo_array*sumrepo_thoustenge:num:$D.1.1.1.1\":\"20\",\"fs_repo_array*sumrepo_thoustenge:num:$D.1.1.2.1\":\"80\",\"fs_repo_array*sumrepo_thoustenge:num:$D.1.1.2.2\":\"140\",\"fs_repo_array*sumrepo_thoustenge:num:1\":\"240\",\"fs_repo_array*sumrepo_thoustenge:num:1.1\":\"240\",\"fs_repo_array*sumrepo_thoustenge:num:1.1.1\":\"20\",\"fs_repo_array*sumrepo_thoustenge:num:1.1.2\":\"220\",\"fs_repo_array*sumrepo_thoustenge:num:1.2\":\"0\",\"fs_repo_array*sumrepo_thoustenge:num:2\":\"0\",\"fs_repo_array*sumrepo_thoustenge:num:2.1\":\"0\",\"fs_repo_array*sumrepo_thoustenge:num:2.1.1\":\"0\",\"fs_repo_array*sumrepo_thoustenge:num:2.1.2\":\"0\",\"fs_repo_array*sumrepo_thoustenge:num:2.2\":\"0\",\"fs_repo_array*vid_cb:num:$D.1.1.1.1\":\"2\",\"fs_repo_array*vid_cb:num:$D.1.1.2.1\":\"8\",\"fs_repo_array*vid_cb:num:$D.1.1.2.2\":\"13\"}\n";

    public static void main(String[] args){
        SortedSet<Integer> sortedSet =  new TreeSet<Integer>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        sortedSet.add(10);
        sortedSet.add(5);
        sortedSet.add(20);
        System.out.println(sortedSet);

    }
}
