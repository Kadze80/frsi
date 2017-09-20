package form.html;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.io.*;
import java.util.*;

public class HtmlParser {

    private String html;
    private Document document;
    private StringBuilder stringBuilder;

    private Workbook workbook;
    private Sheet sheet;
    private Row row;
    private Cell cell;
    private int rowNum, cellNum;
    private Map<Integer, Integer> lineCountInRow = new HashMap<Integer, Integer>();
    private Set<Integer> wideRowNums = new HashSet<Integer>();
    private Set<Integer> containerWideRowNums = new HashSet<Integer>();
    private short firstCellNum, lastCellNum;
    private int level, levelSpan;
    private boolean isDataTableContainer, isDataTable, isDataTableHeader, isDataTableGroup;
    private int dataTableFirstRowNum, dataTableFirstCellNum;
    private String tableName;
    private int trNum, thNum, tdNum;

    private CellStyle cellStyle;

    private CellStyle csDefault;
    private CellStyle csTextRegularLeft, csTextRegularCenter, csTextRegularRight;
    private CellStyle csTextBoldLeft, csTextBoldCenter, csTextBoldRight;
    private CellStyle csTableHeader;
    private CellStyle csTableLeft, csTableCenter, csTableRight;
    private CellStyle csTableGroupLeft, csTableGroupCenter, csTableGroupRight;
    private CellStyle csTableNumeric0, csTableNumeric1, csTableNumeric2, csTableNumeric3, csTableNumeric4, csTableNumeric5, csTableNumeric6;
    private CellStyle csTableGroupNumeric0, csTableGroupNumeric1, csTableGroupNumeric2, csTableGroupNumeric3, csTableGroupNumeric4, csTableGroupNumeric5, csTableGroupNumeric6;

    private Font fontRegular, fontBold;
    private static final String FONT_NAME = "Times New Roman";
    private static final short FONT_HEIGHT = 10; // points

    private short colorBgTableHeader;
    private short colorBgTableGroup;
    private short colorBorderTable;

    private DataFormat dataFormat;

    public HtmlParser(String html) {
        this.html = html;
        document = Jsoup.parse(html);
    }

    public HtmlParser(Document document) {
        this.html = null;
        this.document = document;
    }

    public void createExcelDocument() {
        workbook = new HSSFWorkbook();
        if (document != null) {
            sheet = workbook.createSheet("Лист1");
            createStyles();
            level = levelSpan = rowNum = cellNum = 0;
            row = sheet.createRow(rowNum);
            cell = row.createCell(cellNum);
            cellStyle = csDefault;
            cell.setCellStyle(cellStyle);

            stringBuilder = new StringBuilder();
            wideRowNums.clear();
            dataTableFirstRowNum = dataTableFirstCellNum = -1;
            isDataTableContainer = isDataTable = isDataTableHeader = isDataTableGroup = false;
            tableName = null;
            trNum = thNum = tdNum = 0;

            parseNode(document.body()); // Recursive method
            autoSize();
        }
    }

    private void parseNode(Node parentNode) {
        for (Node node : parentNode.childNodes()) {

            String nodeName = node.nodeName();
            boolean nodeIsTextNode = node instanceof TextNode;
            boolean nodeBreaksLine = getBreaksLine(node);

            Node prevNode = getPreviousNonEmptySiblingNode(node);
            boolean prevIsTextNode = prevNode instanceof TextNode;
            boolean prevBreaksLine = prevNode != null && getBreaksLine(prevNode);
            boolean prevBreaksColumn = prevNode != null && getBreaksColumn(prevNode);

            if (nodeIsTextNode) {
                String nodeText = ((TextNode) node).text().replaceAll("^\\s+", "");
                String nodeTextNoWhiteSpace = nodeText.replaceAll("[\\s\\u00A0\\u2003]+", "");
                String value = nodeTextNoWhiteSpace.isEmpty() ? "" : nodeText;

                if (value.isEmpty()) {
                    cell.setCellStyle(cellStyle);
                    continue;
                } else {
                    setCell();
                    String cellValue = getCellValueAsString(cell) + value;
                    cell.setCellValue(cellValue);
                    autoStyle(cellValue);
                    cell.setCellStyle(cellStyle);
                }
            }
            else if (nodeName.equals("br")) {
                if (level == 0) {
                    nextRow();
                } else {
                    String cellValue = getCellValueAsString(cell);
                    cell.setCellValue(cellValue + "\n");
                    int lineCount = getLineCount(cellValue) + 1;
                    lineCountInRow.put(rowNum, lineCount);
                }
            }
            else if (nodeName.equals("h1") || nodeName.equals("h2") || nodeName.equals("h3") || nodeName.equals("h4") || nodeName.equals("h5") || nodeName.equals("h6")) {
                cellStyle = csTextBoldLeft;
                String style = node.attr("style");
                CssParser cssParser = new CssParser(style);
                String textAlign = cssParser.getPropertyValue("text-align");
                if (textAlign != null) {
                    if (textAlign.equals("center")) {
                        cellStyle = csTextBoldCenter;
                    } else if (textAlign.equals("right")) {
                        cellStyle = csTextBoldRight;
                    }
                }
                if (prevNode != null && !prevBreaksLine) nextRow();
                if (level == 0) wideRowNums.add(rowNum);
                else containerWideRowNums.add(rowNum);
                cell.setCellStyle(cellStyle);
                level++;
                parseNode(node);
                level--;
                nextRow();
            }
            else if (nodeName.equals("p")) {
                cellStyle = csTextRegularLeft;
                String style = node.attr("style");
                CssParser cssParser = new CssParser(style);
                String textAlign = cssParser.getPropertyValue("text-align");
                if (textAlign != null) {
                    if (textAlign.equals("center")) {
                        cellStyle = csTextRegularCenter;
                    } else if (textAlign.equals("right")) {
                        cellStyle = csTextRegularRight;
                    }
                }
                if (prevNode != null && !prevBreaksLine) nextRow();
                if (level == 0) wideRowNums.add(rowNum);
                else containerWideRowNums.add(rowNum);
                cell.setCellStyle(cellStyle);
                level++;
                parseNode(node);
                level--;
                nextRow();
            }
            else if (nodeName.equals("span")) {
                if (levelSpan == 0) {
                    if (prevNode != null && !prevBreaksLine) nextRow();
                    String style = node.attr("style");
                    if (style != null) {
                        CssParser cssParser = new CssParser(style);
                        String textAlign = cssParser.getPropertyValue("text-align");
                        if (textAlign != null) {
                            if (textAlign.equals("center"))
                                cellStyle = csTextRegularCenter;
                            else if (textAlign.equals("right"))
                                cellStyle = csTextRegularRight;
                            else
                                cellStyle = csTextRegularLeft;
                        }
                    }
                    if (level == 0) wideRowNums.add(rowNum);
                    else if (isDataTableContainer) containerWideRowNums.add(rowNum);

                    cell.setCellStyle(cellStyle);
                }
                levelSpan++;
                level++;
                parseNode(node);
                level--;
                levelSpan--;
            }
            else if (nodeName.equals("table")) {
                if (!prevBreaksLine) nextRow();
                String clazz = node.attr("class");
                if (clazz.contains("dataTable")) {
                    isDataTable = true;
                    tableName = node.attr("id");
                }
                trNum = thNum = tdNum = 0;
                level++;
                parseNode(node);
                level--;
                cellStyle = csDefault;
                isDataTable = false;
                tableName = null;
            }
            else if (nodeName.equals("tbody")) {
                level++;
                parseNode(node);
                level--;
            }
            else if (nodeName.equals("tr")) {
                String clazz = node.attr("class");
                if (clazz.contains("groupHeader")) {
                    isDataTableGroup = true;
                    cellStyle = csTableGroupLeft;
                }
                else {
                    isDataTableGroup = false;
                    cellStyle = csTableLeft;
                }
                trNum++;
                thNum = tdNum = 0;
                level++;
                parseNode(node);
                level--;
                nextRow();
            }
            else if (nodeName.equals("th")) {
                thNum++;
                if (!(tableName != null && tableName.equals("balance_accounts_array") && (thNum == 1 || thNum == 3 || thNum == 6))) {
                    newNonMergedCell();
                    cellStyle = csTableHeader;

                    String sColSpan = node.attr("colspan");
                    String sRowSpan = node.attr("rowspan");
                    mergeCells(sheet, rowNum, cellNum, sRowSpan, sColSpan);

                    isDataTableHeader = true;
                    level++;
                    parseNode(node);
                    level--;
                    isDataTableHeader = false;
                    cellNum++;
                }
            }
            else if (nodeName.equals("td")) {
                tdNum++;
                if (!(tableName != null && tableName.equals("balance_accounts_array") && (tdNum == 1 || tdNum == 3 || tdNum == 6))) {
                    boolean containsChildTable = containsChildTable(node);
                    if (containsChildTable) {
                        isDataTableContainer = true;
                        containerWideRowNums.clear();
                        if (dataTableFirstRowNum < 0) {
                            dataTableFirstRowNum = rowNum;
                            dataTableFirstCellNum = 0;
                        } else {
                            rowNum = dataTableFirstRowNum;
                            updateMinMaxCellNums();
                            dataTableFirstCellNum = lastCellNum + 1;
                            cellNum = dataTableFirstCellNum;
                        }
                        cellStyle = csDefault;
                    } else {
                        newNonMergedCell();
                        cellStyle = isDataTableGroup ? csTableGroupLeft : csTableLeft;
                        String sColSpan = node.attr("colspan");
                        String sRowSpan = node.attr("rowspan");
                        mergeCells(sheet, rowNum, cellNum, sRowSpan, sColSpan);
                    }
                    setCell();
                    level++;
                    parseNode(node);
                    level--;
                    cellNum++;

                    if (containsChildTable) {
                        updateMinMaxCellNums();
                        for (Integer rowNum : containerWideRowNums)
                            mergeCells(sheet, rowNum, dataTableFirstCellNum, 1, lastCellNum - dataTableFirstCellNum);
                        isDataTableContainer = false;
                        rowNum = sheet.getLastRowNum() - 1; // will be increased before exiting layout table's <tr> tag, i.e. will be set to LastRowNum
                        cellStyle = csDefault;
                    }
                }
            }
            else if (nodeName.equals("input")) {
                String type = node.attr("type");
                String clazz = node.attr("class");
                String value = node.attr("value");
                if (type.equals("text")) {
                    if (levelSpan > 0) {
                        String cellValue = getCellValueAsString(cell);
                        cell.setCellValue(cellValue + value + " ");
                    } else {
                        if (clazz.contains("maskMoney")) {
                            try {
                                Double numValue = Double.valueOf(value);
                                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                                cell.setCellValue(numValue);
                                if (clazz.contains("maskMoney0")) cellStyle = isDataTableGroup ? csTableGroupNumeric0 : csTableNumeric0;
                                else if (clazz.contains("maskMoney1")) cellStyle = isDataTableGroup ? csTableGroupNumeric1 : csTableNumeric1;
                                else if (clazz.contains("maskMoney2")) cellStyle = isDataTableGroup ? csTableGroupNumeric2 : csTableNumeric2;
                                else if (clazz.contains("maskMoney3")) cellStyle = isDataTableGroup ? csTableGroupNumeric3 : csTableNumeric3;
                                else if (clazz.contains("maskMoney4")) cellStyle = isDataTableGroup ? csTableGroupNumeric4 : csTableNumeric4;
                                else if (clazz.contains("maskMoney5")) cellStyle = isDataTableGroup ? csTableGroupNumeric5 : csTableNumeric5;
                                else if (clazz.contains("maskMoney6")) cellStyle = isDataTableGroup ? csTableGroupNumeric6 : csTableNumeric6;
                                else cellStyle = isDataTableGroup ? csTableGroupNumeric2 : csTableNumeric2;
                            } catch (Exception e) {
                                cell.setCellValue(value);
                            }
                        } else {
                            cell.setCellValue(value);
                            autoStyle(value);
                        }
                    }
                    cell.setCellStyle(cellStyle);
                }
            }
            else if (nodeName.equals("select")) {
                List<Node> options = node.childNodes();
                String code = null;
                String caption = null;
                for (Node option : options) {
                    String selected = option.attr("selected");
                    if (selected.equals("selected")) {
                        code = option.attr("value");
                        if (option.childNodes().size() > 0) {
                            TextNode textNode = (TextNode) option.childNode(0);
                            caption = textNode.text();
                        }
                    }
                }
                if (caption != null) cell.setCellValue(caption);
                if (isDataTableGroup) cellStyle = csTableGroupCenter;
                else if (isDataTable) cellStyle = csTableCenter;
                else cellStyle = csDefault;
                cell.setCellStyle(cellStyle);
            }
            else {
                level++;
                parseNode(node);
                level--;
            }
        }
    }

    // Sets cell style depending on whether the value represents any typed value or not
    private void autoStyle(String value) {
        if (isDataTable && !isDataTableHeader) {
            boolean isFormattedValue = value.matches("[\\d\\.,_\\+\\-%\\s]+");
            if (isFormattedValue) {
                cellStyle = isDataTableGroup ? csTableGroupCenter : csTableCenter;
            } else {
                cellStyle = isDataTableGroup ? csTableGroupLeft : csTableLeft;
            }
        }
    }

    private boolean containsChildTable(Node parentNode) {
        boolean result = false;
        for (Node node : parentNode.childNodes()) {
            if (node.nodeName().equals("table")) {
                result = true;
                break;
            } else {
                result = containsChildTable(node);
            }
        }
        return result;
    }

    private boolean isMergedCell(int rowNum, int colNum) {
        boolean result = false;
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.isInRange(rowNum, colNum)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private CellStyle getRegionStyle(Sheet sheet, CellRangeAddress region) { // Returns top-left cell's style
        CellStyle result = null;
        int firstRowNum = region.getFirstRow();
        Row firstRow = sheet.getRow(firstRowNum);
        if (firstRow != null) {
            int firstColNum = region.getFirstColumn();
            Cell firstCell = firstRow.getCell(firstColNum);
            if (firstCell != null) result = firstCell.getCellStyle();
        }
        return result;
    }

    private void copyRegionStyle(Sheet sheet, CellRangeAddress region) {
        CellStyle style = getRegionStyle(sheet, region);
        for (int rowNum = region.getFirstRow(); rowNum <= region.getLastRow(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) row = sheet.createRow(rowNum);
            for (int colNum = region.getFirstColumn(); colNum <= region.getLastColumn(); colNum++)
                if (!(rowNum == region.getFirstRow() && colNum == region.getFirstColumn())) {
                    Cell cell = row.getCell(colNum);
                    if (cell == null) cell = row.createCell(colNum);
                    if (style != null) cell.setCellStyle(style);
                }
        }
    }

    private void mergeCells(Sheet sheet, int rowNum, int colNum, int rowSpan, int colSpan) {
        if (colSpan > 1 || rowSpan > 1) {
            CellRangeAddress region = new CellRangeAddress(rowNum, rowNum + rowSpan -1, colNum, colNum + colSpan - 1);
            copyRegionStyle(sheet, region);
            sheet.addMergedRegion(region);
        }
    }

    private void mergeCells(Sheet sheet, int rowNum, int colNum, String sRowSpan, String sColSpan) {
        int colSpan = 1;
        int rowSpan = 1;
        try { colSpan = Integer.parseInt(sColSpan); } catch (NumberFormatException e) {}
        try { rowSpan = Integer.parseInt(sRowSpan); } catch (NumberFormatException e) {}

        if (colSpan > 1 || rowSpan > 1) {
            CellRangeAddress region = new CellRangeAddress(rowNum, rowNum + rowSpan -1, colNum, colNum + colSpan - 1);
            copyRegionStyle(sheet, region);
            sheet.addMergedRegion(region);
        }
    }

    private int getLineCount(String s) {
        String[] lines = s.split("\\n");
        return lines.length;
    }

    private Node getPreviousNonEmptySiblingNode(Node node) {
        Node prevNode = node;
        boolean isTextNode;
        String prevText;
        do {
            prevNode = prevNode.previousSibling();
            isTextNode = prevNode instanceof TextNode;
            prevText = isTextNode ? ((TextNode) prevNode).text().trim() : "";
        } while (prevNode != null && isTextNode && prevText.isEmpty());
        return prevNode;
    }

    private boolean getBreaksLine(Node node) {
        String nodeName = node.nodeName();
        return nodeName.equals("h1") || nodeName.equals("h2") || nodeName.equals("h3") || nodeName.equals("h4") || nodeName.equals("h5") || nodeName.equals("h6") ||
               nodeName.equals("br") || nodeName.equals("p") || nodeName.equals("table") || nodeName.equals("tr");
    }

    private boolean getBreaksColumn(Node node) {
        String nodeName = node.nodeName();
        return nodeName.equals("th") || nodeName.equals("td");
    }

    private String getCellValueAsString(Cell cell) {
        String result;
        int cellType = cell.getCellType();
        switch (cellType) {
            case Cell.CELL_TYPE_BLANK:
                result = "";
                break;
            case Cell.CELL_TYPE_STRING:
                result = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                double doubleValue = cell.getNumericCellValue();
                result = Double.toString(doubleValue);
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                boolean booleanValue = cell.getBooleanCellValue();
                result = Boolean.toString(booleanValue);
                break;
            case Cell.CELL_TYPE_FORMULA:
                result = cell.getCellFormula();
                break;
            case Cell.CELL_TYPE_ERROR:
                byte byteValue = cell.getErrorCellValue();
                result = "ERROR";
                break;
            default:
                result = "";
        }
        return result;
    }

    private void setCell() {
        row = sheet.getRow(rowNum);
        if (row == null) row = sheet.createRow(rowNum);
        cell = row.getCell(cellNum, Row.CREATE_NULL_AS_BLANK);
        cell.setCellStyle(cellStyle);
    }

    private void nextRow() {
        rowNum++;
        row = sheet.getRow(rowNum);
        if (row == null) row = sheet.createRow(rowNum);
        if (lineCountInRow.get(rowNum) == null) lineCountInRow.put(rowNum, 1);
        cellNum = isDataTableContainer ? dataTableFirstCellNum : 0;
        cell = row.getCell(cellNum, Row.CREATE_NULL_AS_BLANK);
        cell.setCellStyle(cellStyle);
    }

    private void newNonMergedCell() {
        while (isMergedCell(rowNum, cellNum)) cellNum++;
        cell = row.getCell(cellNum, Row.CREATE_NULL_AS_BLANK);
        cell.setCellStyle(cellStyle);
    }

    private void updateMinMaxCellNums() {
        firstCellNum = Short.MAX_VALUE;
        lastCellNum = Short.MIN_VALUE;
        for (int rn = sheet.getFirstRowNum(); rn <= sheet.getLastRowNum(); rn++) {
            Row r = sheet.getRow(rn);
            short first = r.getFirstCellNum();
            short last = r.getLastCellNum();
            if (first < firstCellNum) firstCellNum = first;
            if (last > lastCellNum) lastCellNum = last;
        }
    }

    private void autoSize() {
        final int MAX_COLUMN_WIDTH = 150 * 256; // in units of 1/256th of a character width
        final int NEW_COLUMN_WIDTH = 100 * 256;

        updateMinMaxCellNums();
        // Merge wide spans
        for (Integer rowNum : wideRowNums) {
            mergeCells(sheet, rowNum, firstCellNum, 1, lastCellNum - firstCellNum);
        }
        // Cell width
        for (int cn = firstCellNum; cn < lastCellNum; cn++) {
            sheet.autoSizeColumn(cn, true);
            int colWidth = sheet.getColumnWidth(cn);
            if (colWidth > MAX_COLUMN_WIDTH) sheet.setColumnWidth(cn, NEW_COLUMN_WIDTH);
        }
        // Row height
        float defaultRowHeight = 12.96f; // in points
        sheet.setDefaultRowHeightInPoints(defaultRowHeight);
        for (Integer rn : lineCountInRow.keySet()) {
            int lineCount = lineCountInRow.get(rn);
            float newRowHeight = lineCount > 1 ? lineCount * FONT_HEIGHT + defaultRowHeight : defaultRowHeight;
            sheet.getRow(rn.intValue()).setHeightInPoints(newRowHeight);
        }
        // Custom sizes
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

    public boolean saveExcelDocumentToFile(String fileName) {
        boolean result = false;
        if (workbook == null) return result;

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(fileName));
            workbook.write(out);
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }
    }

    // Getters and setters

    public String getHtml() {
        return html;
    }

    public Document getDocument() {
        return document;
    }

    public Workbook getExcelDocument() {
        return workbook;
    }

    // Initializers

    private void createStyles() {

        // Colors
        colorBgTableHeader = HSSFColor.GREY_40_PERCENT.index; // new XSSFColor(new Color(192, 192, 192));
        colorBgTableGroup = HSSFColor.GREY_25_PERCENT.index;  // new XSSFColor(new Color(224, 224, 224));
        colorBorderTable = HSSFColor.BLACK.index; // new Color(new XSSFColor(0, 0, 0));

        // Fonts

        fontRegular = workbook.createFont();
        fontRegular.setBold(false);
        fontRegular.setFontHeightInPoints(FONT_HEIGHT);
        fontRegular.setFontName(FONT_NAME);

        fontBold = workbook.createFont();
        fontBold.setBold(true);
        fontBold.setFontHeightInPoints(FONT_HEIGHT);
        fontBold.setFontName(FONT_NAME);

        // Styles

        csDefault = workbook.getCellStyleAt((short) 0);
        csDefault.setFont(fontRegular);
        csDefault.setWrapText(true);

        csTextRegularLeft = workbook.createCellStyle();
        csTextRegularLeft.setAlignment(CellStyle.ALIGN_LEFT);
        csTextRegularLeft.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        csTextRegularLeft.setFont(fontRegular);
        csTextRegularLeft.setWrapText(true);

        csTextRegularCenter = workbook.createCellStyle();
        csTextRegularCenter.setAlignment(CellStyle.ALIGN_CENTER);
        csTextRegularCenter.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        csTextRegularCenter.setFont(fontRegular);
        csTextRegularCenter.setWrapText(true);

        csTextRegularRight = workbook.createCellStyle();
        csTextRegularRight.setAlignment(CellStyle.ALIGN_RIGHT);
        csTextRegularRight.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        csTextRegularRight.setFont(fontRegular);
        csTextRegularRight.setWrapText(true);

        csTextBoldLeft = workbook.createCellStyle();
        csTextBoldLeft.setAlignment(CellStyle.ALIGN_LEFT);
        csTextBoldLeft.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        csTextBoldLeft.setFont(fontBold);
        csTextBoldLeft.setWrapText(true);

        csTextBoldCenter = workbook.createCellStyle();
        csTextBoldCenter.setAlignment(CellStyle.ALIGN_CENTER);
        csTextBoldCenter.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        csTextBoldCenter.setFont(fontBold);
        csTextBoldCenter.setWrapText(true);

        csTextBoldRight = workbook.createCellStyle();
        csTextBoldRight.setAlignment(CellStyle.ALIGN_RIGHT);
        csTextBoldRight.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        csTextBoldRight.setFont(fontBold);
        csTextBoldRight.setWrapText(true);

        csTableHeader = workbook.createCellStyle();
        csTableHeader.setFillPattern(CellStyle.SOLID_FOREGROUND);
        csTableHeader.setFillForegroundColor(colorBgTableHeader);
        csTableHeader.setBorderTop(CellStyle.BORDER_THIN);
        csTableHeader.setBorderBottom(CellStyle.BORDER_THIN);
        csTableHeader.setBorderLeft(CellStyle.BORDER_THIN);
        csTableHeader.setBorderRight(CellStyle.BORDER_THIN);
        csTableHeader.setTopBorderColor(colorBorderTable);
        csTableHeader.setBottomBorderColor(colorBorderTable);
        csTableHeader.setLeftBorderColor(colorBorderTable);
        csTableHeader.setRightBorderColor(colorBorderTable);
        csTableHeader.setAlignment(CellStyle.ALIGN_CENTER);
        csTableHeader.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        csTableHeader.setFont(fontBold);
        csTableHeader.setWrapText(true);

        csTableLeft = workbook.createCellStyle();
        csTableLeft.setBorderTop(CellStyle.BORDER_THIN);
        csTableLeft.setBorderBottom(CellStyle.BORDER_THIN);
        csTableLeft.setBorderLeft(CellStyle.BORDER_THIN);
        csTableLeft.setBorderRight(CellStyle.BORDER_THIN);
        csTableLeft.setTopBorderColor(colorBorderTable);
        csTableLeft.setBottomBorderColor(colorBorderTable);
        csTableLeft.setLeftBorderColor(colorBorderTable);
        csTableLeft.setRightBorderColor(colorBorderTable);
        csTableLeft.setAlignment(CellStyle.ALIGN_LEFT);
        csTableLeft.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        csTableLeft.setFont(fontRegular);
        csTableLeft.setWrapText(true);

        csTableCenter = workbook.createCellStyle();
        csTableCenter.cloneStyleFrom(csTableLeft);
        csTableCenter.setAlignment(CellStyle.ALIGN_CENTER);

        csTableRight = workbook.createCellStyle();
        csTableRight.cloneStyleFrom(csTableLeft);
        csTableRight.setAlignment(CellStyle.ALIGN_RIGHT);

        csTableGroupLeft = workbook.createCellStyle();
        csTableGroupLeft.setFillPattern(CellStyle.SOLID_FOREGROUND);
        csTableGroupLeft.setFillForegroundColor(colorBgTableGroup);
        csTableGroupLeft.setBorderTop(CellStyle.BORDER_THIN);
        csTableGroupLeft.setBorderBottom(CellStyle.BORDER_THIN);
        csTableGroupLeft.setBorderLeft(CellStyle.BORDER_THIN);
        csTableGroupLeft.setBorderRight(CellStyle.BORDER_THIN);
        csTableGroupLeft.setTopBorderColor(colorBorderTable);
        csTableGroupLeft.setBottomBorderColor(colorBorderTable);
        csTableGroupLeft.setLeftBorderColor(colorBorderTable);
        csTableGroupLeft.setRightBorderColor(colorBorderTable);
        csTableGroupLeft.setAlignment(CellStyle.ALIGN_LEFT);
        csTableGroupLeft.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        csTableGroupLeft.setFont(fontBold);
        csTableGroupLeft.setWrapText(true);

        csTableGroupCenter = workbook.createCellStyle();
        csTableGroupCenter.cloneStyleFrom(csTableGroupLeft);
        csTableGroupCenter.setAlignment(CellStyle.ALIGN_CENTER);

        csTableGroupRight = workbook.createCellStyle();
        csTableGroupRight.cloneStyleFrom(csTableGroupLeft);
        csTableGroupRight.setAlignment(CellStyle.ALIGN_RIGHT);

        dataFormat = workbook.createDataFormat();

        csTableNumeric0 = workbook.createCellStyle();
        csTableNumeric0.cloneStyleFrom(csTableRight);
        csTableNumeric0.setDataFormat(dataFormat.getFormat("# ### ### ### ##0"));

        csTableNumeric1 = workbook.createCellStyle();
        csTableNumeric1.cloneStyleFrom(csTableRight);
        csTableNumeric1.setDataFormat(dataFormat.getFormat("# ### ### ### ##0.0"));

        csTableNumeric2 = workbook.createCellStyle();
        csTableNumeric2.cloneStyleFrom(csTableRight);
        csTableNumeric2.setDataFormat(dataFormat.getFormat("# ### ### ### ##0.00"));

        csTableNumeric3 = workbook.createCellStyle();
        csTableNumeric3.cloneStyleFrom(csTableRight);
        csTableNumeric3.setDataFormat(dataFormat.getFormat("# ### ### ### ##0.000"));

        csTableNumeric4 = workbook.createCellStyle();
        csTableNumeric4.cloneStyleFrom(csTableRight);
        csTableNumeric4.setDataFormat(dataFormat.getFormat("# ### ### ### ##0.0000"));

        csTableNumeric5 = workbook.createCellStyle();
        csTableNumeric5.cloneStyleFrom(csTableRight);
        csTableNumeric5.setDataFormat(dataFormat.getFormat("# ### ### ### ##0.00000"));

        csTableNumeric6 = workbook.createCellStyle();
        csTableNumeric6.cloneStyleFrom(csTableRight);
        csTableNumeric6.setDataFormat(dataFormat.getFormat("# ### ### ### ##0.000000"));

        csTableGroupNumeric0 = workbook.createCellStyle();
        csTableGroupNumeric0.cloneStyleFrom(csTableGroupRight);
        csTableGroupNumeric0.setAlignment(CellStyle.ALIGN_RIGHT);
        csTableGroupNumeric0.setDataFormat(dataFormat.getFormat("# ### ### ### ##0"));

        csTableGroupNumeric1 = workbook.createCellStyle();
        csTableGroupNumeric1.cloneStyleFrom(csTableGroupRight);
        csTableGroupNumeric1.setAlignment(CellStyle.ALIGN_RIGHT);
        csTableGroupNumeric1.setDataFormat(dataFormat.getFormat("# ### ### ### ##0.0"));

        csTableGroupNumeric2 = workbook.createCellStyle();
        csTableGroupNumeric2.cloneStyleFrom(csTableGroupRight);
        csTableGroupNumeric2.setAlignment(CellStyle.ALIGN_RIGHT);
        csTableGroupNumeric2.setDataFormat(dataFormat.getFormat("# ### ### ### ##0.00"));

        csTableGroupNumeric3 = workbook.createCellStyle();
        csTableGroupNumeric3.cloneStyleFrom(csTableGroupRight);
        csTableGroupNumeric3.setAlignment(CellStyle.ALIGN_RIGHT);
        csTableGroupNumeric3.setDataFormat(dataFormat.getFormat("# ### ### ### ##0.000"));

        csTableGroupNumeric4 = workbook.createCellStyle();
        csTableGroupNumeric4.cloneStyleFrom(csTableGroupRight);
        csTableGroupNumeric4.setAlignment(CellStyle.ALIGN_RIGHT);
        csTableGroupNumeric4.setDataFormat(dataFormat.getFormat("# ### ### ### ##0.0000"));

        csTableGroupNumeric5 = workbook.createCellStyle();
        csTableGroupNumeric5.cloneStyleFrom(csTableGroupRight);
        csTableGroupNumeric5.setAlignment(CellStyle.ALIGN_RIGHT);
        csTableGroupNumeric5.setDataFormat(dataFormat.getFormat("# ### ### ### ##0.00000"));

        csTableGroupNumeric6 = workbook.createCellStyle();
        csTableGroupNumeric6.cloneStyleFrom(csTableGroupRight);
        csTableGroupNumeric6.setAlignment(CellStyle.ALIGN_RIGHT);
        csTableGroupNumeric6.setDataFormat(dataFormat.getFormat("# ### ### ### ##0.000000"));
    }
}
