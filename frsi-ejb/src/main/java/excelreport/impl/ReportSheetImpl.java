package excelreport.impl;

import dataform.FormulaParser;
import dataform.FormulaSyntaxError;
import dataform.IKeyHandler;
import excelreport.ReportSheet;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Nuriddin.Baideuov on 23.11.2015.
 */
public class ReportSheetImpl extends ReportSheet {

    protected XSSFSheet shTemplate;
    protected XSSFSheet shResult;
    protected int shResultIdx;
    protected XSSFWorkbook wbResult;
    protected XSSFWorkbook wbTemplate;

    // текущие позиции для вывода
    protected int curR;
    protected int curC;

    protected LinkedHashMap<String, BandInfo> bands;
    protected int maxCol;
    protected IKeyHandler keyHandler;

    // стили в формате  [стиль-шаблона]->стиль-результата
    protected Map<CellStyle, StyleInfo> cellStyles;

    private int maxRowNum;

    class BandInfo {
        String name;
        int c1, c2, r1, r2;
        boolean wholeCols; // все колонки
        List<Cell> cells = new ArrayList<Cell>();
        List<Row> rows = new ArrayList<Row>();
        public List<CellRangeAddress> merges = new ArrayList<CellRangeAddress>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    class StyleInfo {
        XSSFCellStyle tmlStyle;
        XSSFCellStyle resStyle;
        boolean isDate;
        boolean isNumber;
    }

    public ReportSheetImpl(XSSFSheet shTemplate, XSSFSheet shResult) {
        this.shTemplate = shTemplate;
        this.shResult = shResult;
        this.wbTemplate = this.shTemplate.getWorkbook();
        this.wbResult = this.shResult.getWorkbook();
        this.shResultIdx = this.wbResult.getSheetIndex(this.shResult);
        //
        parseSheet();
        maxRowNum = 0;
    }

    protected void parseSheet() {
        shResult.setDefaultColumnWidth(shTemplate.getDefaultColumnWidth());
        //shResult.setDefaultRowHeight(shTemplate.getDefaultRowHeight());
        //
        ArrayList<Cell> cells = new ArrayList<Cell>();
        // all cells
        for (Row row : shTemplate) {
            for (Cell cell : row) {
                cells.add(cell);
                maxCol = Math.max(maxCol, cell.getColumnIndex());
            }
        }
        //
        bands = new LinkedHashMap<String, BandInfo>();
        int numNames = shTemplate.getWorkbook().getNumberOfNames();
        for (int i = 0; i < numNames; i++) {
            Name nm = shTemplate.getWorkbook().getNameAt(i);

            AreaReference aref = new AreaReference(nm.getRefersToFormula(), SpreadsheetVersion.EXCEL2007);

            BandInfo band = new BandInfo();
            band.setName(nm.getNameName());
            band.c1 = aref.getFirstCell().getCol();
            band.c2 = aref.getLastCell().getCol();
            band.r1 = aref.getFirstCell().getRow();
            band.r2 = aref.getLastCell().getRow();
            band.wholeCols = aref.isWholeColumnReference();
            //
            for (int j = band.r1; j <= band.r2; j++) {
                band.rows.add(shTemplate.getRow(j));
            }
            // cells for band
            for (Cell cell : cells) {
                int ri = cell.getRowIndex();
                int ci = cell.getColumnIndex();

                if (ri >= band.r1 && ri <= band.r2 && ci >= band.c1 && ci <= band.c2) {
                    band.cells.add(cell);
                }
            }
            // merges for band
            int mergCnt = shTemplate.getNumMergedRegions();
            for (int k = 0; k < mergCnt; k++) {
                CellRangeAddress mr = shTemplate.getMergedRegion(k);
                int ri = mr.getFirstRow();
                int ci = mr.getFirstColumn();

                if (ri >= band.r1 && ri <= band.r2 && ci >= band.c1 && ci <= band.c2) {
                    band.merges.add(mr);
                }
            }
            //
            bands.put(band.name, band);
        }

        // стили
        cellStyles = new HashMap<CellStyle, StyleInfo>();
        for (short i = 0; i < wbTemplate.getNumCellStyles(); i++) {

            XSSFCellStyle tmlStyle = wbTemplate.getCellStyleAt(i);
            XSSFCellStyle resStyle = wbResult.createCellStyle();
            resStyle.cloneStyleFrom(tmlStyle);
            StyleInfo styleInfo = new StyleInfo();
            styleInfo.tmlStyle = tmlStyle;
            styleInfo.resStyle = resStyle;
            styleInfo.isDate = isStyleDate(resStyle);
            styleInfo.isNumber = isStyleNumber(resStyle);
            cellStyles.put(tmlStyle, styleInfo);
        }

        StylesTable newStylesSource = wbResult.getStylesSource();
        StylesTable oldStylesSource = wbTemplate.getStylesSource();
        for (XSSFCellFill fill : oldStylesSource.getFills()) {
            XSSFCellFill fillNew = new XSSFCellFill(fill.getCTFill());
            newStylesSource.putFill(fillNew);
        }
        for (XSSFCellBorder border : oldStylesSource.getBorders()) {
            XSSFCellBorder borderNew = new XSSFCellBorder(border.getCTBorder());
            newStylesSource.putBorder(borderNew);
        }

        // columns widths & styles
        for (int i = 0; i <= maxCol; i++) {
            CellStyle st = shTemplate.getColumnStyle(i);
            if (st != null) {
                shResult.setDefaultColumnStyle(i, getResultCellStyle(st).resStyle);
            }
            shResult.setColumnWidth(i, shTemplate.getColumnWidth(i));
            if (shTemplate.isColumnHidden(i)) {
                shResult.setColumnHidden(i, true);
            }
        }

    }

    @Override
    public void setSheetTitle(String title) {
        shResult.getWorkbook().setSheetName(shResultIdx, title);
    }

    @Override
    public void out(String nameBand, IKeyHandler keyHandler) {
        this.keyHandler = keyHandler;
        //
        BandInfo band = bands.get(nameBand);
        int baseRow = curR;
        for (Row row : band.rows) {
            if (row == null) {
                Row emptyRow = shResult.createRow(curR);
                Cell emptyCell = emptyRow.createCell(0);
                emptyRow.setHeight(shTemplate.getDefaultRowHeight());
                curR++;   // тут не заполненная строка
                continue;
            }
            Row newRow = shResult.createRow(curR);
            XSSFRow _row = (XSSFRow) row;
            if (_row.getCTRow().isSetHt())
                newRow.setHeight(row.getHeight());
            //
            curR++;
            for (Cell cell : band.cells) {
                if (cell.getRowIndex() == row.getRowNum()) {
                    Cell newCell = newRow.createCell(cell.getColumnIndex());
                    copyCell(cell, newCell);
                }
            }
        }
        // merges
        if (!band.merges.isEmpty()) {
            for (CellRangeAddress mr : band.merges) {
                CellRangeAddress mrNew = new CellRangeAddress(
                        mr.getFirstRow() + baseRow - band.r1,
                        mr.getLastRow() + baseRow - band.r1,
                        mr.getFirstColumn(),
                        mr.getLastColumn()
                );

                shResult.addMergedRegion(mrNew);
            }
        }
    }

    protected void copyCell(Cell oldCell, Cell newCell) {
        // Copy style from old cell and apply to new cell
        StyleInfo styleInfo = getResultCellStyle(oldCell.getCellStyle());
        newCell.setCellStyle(styleInfo.resStyle);

        // If there is a cell comment, copy
        if (newCell.getCellComment() != null) {
            newCell.setCellComment(oldCell.getCellComment());
        }

        // If there is a cell hyperlink, copy
        if (oldCell.getHyperlink() != null) {
            newCell.setHyperlink(oldCell.getHyperlink());
        }

        String s, s1;
        // Set the cell data value
        switch (oldCell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
                s = oldCell.getRichStringCellValue().getString();
                if (s.startsWith("=")) {
                    // формула
                    s = s.substring(1);
                    s1 = parseString(s);
                    if (s1 != null && !s1.isEmpty()) {
                        try {
                            newCell.setCellType(Cell.CELL_TYPE_FORMULA);
                            newCell.setCellFormula(s1);
                        } catch (FormulaParseException e) {
                            newCell.setCellValue("!ERROR");
                        }
                    }
                } else {
                    s1 = parseString(s);
                    if (styleInfo.isDate) {
                        Date dt = toDate(s1, null);
                        if (dt != null) {
                            newCell.setCellValue(dt);
                        } else {
                            newCell.setCellValue(s1);
                        }
                    } else if (styleInfo.isNumber) {
                        double v = toDouble(s1, Double.MIN_VALUE);
                        if (v != Double.MIN_VALUE) {
                            newCell.setCellValue(v);
                        } else {
                            newCell.setCellValue(s1);
                        }
                    } else {
                        newCell.setCellValue(s1);
                    }
                }
                break;
        }
    }

    @Override
    public boolean hasBand(String nameBand) {
        return bands.containsKey(nameBand);
    }

    protected StyleInfo getResultCellStyle(CellStyle tmlStyle) {
        StyleInfo st = cellStyles.get(tmlStyle);
        if (st != null) {
            return st;
        }
        throw new RuntimeException("Style not found!");
    }

    protected boolean isStyleNumber(CellStyle style) {
        String s = style.getDataFormatString();
        if (s == null) {
            return false;
        }
        return s.indexOf('0') != -1 || s.indexOf('#') != -1;
    }

    protected boolean isStyleDate(CellStyle style) {
        String s = style.getDataFormatString();
        if (s == null) {
            return false;
        }
        return DateUtil.isADateFormat(style.getFontIndex(), style.getDataFormatString());
    }

    protected double toDouble(String str, double defValue) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    protected Date toDate(String str, Date defValue) {
        try {
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            return df.parse(str);
        } catch (ParseException e) {
            return defValue;
        }
    }

    protected String parseString(String s) {
        if (keyHandler != null) {
            try {
                FormulaParser parser = new FormulaParser(s, keyHandler);
                return parser.getResult();
            } catch (FormulaSyntaxError e) {
                return s;
            }
        } else {
            return s;
        }
    }
}
