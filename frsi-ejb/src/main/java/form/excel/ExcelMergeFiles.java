package form.excel;

import entities.FileWrapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

/**
 * Created by Ayupov.Bakhtiyar on 10.04.2017.
 */
public class ExcelMergeFiles implements Serializable {

    private XSSFWorkbook unionWb;

    public ExcelMergeFiles(List<FileWrapper> files) throws Exception {
        unionWb = new XSSFWorkbook();
        for(FileWrapper fileWrapper : files){
            InputStream inputStream = new ByteArrayInputStream(fileWrapper.getBytes());
            XSSFWorkbook sourceWb = new XSSFWorkbook(inputStream);
            unionWb = mergeWb(unionWb, sourceWb);
        }
    }

    private XSSFWorkbook mergeWb(XSSFWorkbook unionWb, XSSFWorkbook sourceWb){
        for(int i = 0; i < sourceWb.getNumberOfSheets(); i ++){
            XSSFSheet sheet = unionWb.createSheet(sourceWb.getSheetName(i));
            XSSFSheet sourceSheet = sourceWb.getSheetAt(i);

            copySheetSettings(sourceSheet, sheet);
            copySheet(sourceSheet, sheet);
        }
        return unionWb;
    }

    private static void copySheet(XSSFSheet source, XSSFSheet destination) {
        int maxColumnNum = 0;
        List<CellStyle> styleMap2 = new ArrayList<CellStyle>();
        for (int i = source.getFirstRowNum(); i <= source.getLastRowNum(); i++) {
            XSSFRow srcRow = source.getRow(i);
            XSSFRow destRow = destination.createRow(i);
            if (srcRow != null) {
                copyRow(srcRow, destRow, styleMap2);
                if (srcRow.getLastCellNum() > maxColumnNum) {
                    maxColumnNum = srcRow.getLastCellNum();
                }
            }
        }
        for (int i = 0; i <= maxColumnNum; i++) {
            destination.setColumnWidth(i, source.getColumnWidth(i));
            destination.setColumnHidden(i, source.isColumnHidden(i));
        }

        List<CellRangeAddress> regionMergeList = source.getMergedRegions();
        for(CellRangeAddress cellRangeAddress : regionMergeList) {
            destination.addMergedRegion(cellRangeAddress);
        }
    }

    private static void copyRow(XSSFRow srcRow, XSSFRow destRow, List<CellStyle> styleMap) {
        destRow.setHeight(srcRow.getHeight());
        destRow.getCTRow().setHidden(srcRow.getCTRow().getHidden());

        int j = srcRow.getFirstCellNum();
        if (j < 0) {
            j = 0;
        }
        for (; j <= srcRow.getLastCellNum(); j++) {
            XSSFCell oldCell = srcRow.getCell(j); // ancienne cell
            XSSFCell newCell = destRow.getCell(j); // new cell
            if (oldCell != null) {
                if (newCell == null) {
                    newCell = destRow.createCell(j);
                }

                copyCell(oldCell, newCell, styleMap);
            }
        }

    }

    private static void copyCell(Cell oldCell, Cell newCell, List<CellStyle> styleList) {
        if (styleList != null) {
            if (oldCell.getSheet().getWorkbook() == newCell.getSheet().getWorkbook()) {
                newCell.setCellStyle(oldCell.getCellStyle());
            } else {
                DataFormat newDataFormat = newCell.getSheet().getWorkbook().createDataFormat();

                CellStyle newCellStyle = getSameCellStyle(oldCell, newCell, styleList);
                if (newCellStyle == null) {
                    // Create a new cell style
                    Font oldFont = oldCell.getSheet().getWorkbook().getFontAt(oldCell.getCellStyle().getFontIndex());
                    // Find a existing font corresponding to avoid to create a
                    // new one
                    Font newFont = newCell
                            .getSheet()
                            .getWorkbook()
                            .findFont(oldFont.getBoldweight(), oldFont.getColor(), oldFont.getFontHeight(),
                                    oldFont.getFontName(), oldFont.getItalic(), oldFont.getStrikeout(),
                                    oldFont.getTypeOffset(), oldFont.getUnderline());
                    if (newFont == null) {
                        newFont = newCell.getSheet().getWorkbook().createFont();
                        newFont.setBoldweight(oldFont.getBoldweight());
                        newFont.setColor(oldFont.getColor());
                        newFont.setFontHeight(oldFont.getFontHeight());
                        newFont.setFontName(oldFont.getFontName());
                        newFont.setItalic(oldFont.getItalic());
                        newFont.setStrikeout(oldFont.getStrikeout());
                        newFont.setTypeOffset(oldFont.getTypeOffset());
                        newFont.setUnderline(oldFont.getUnderline());
                        newFont.setCharSet(oldFont.getCharSet());
                    }

                    short newFormat = newDataFormat.getFormat(oldCell.getCellStyle().getDataFormatString());
                    newCellStyle = newCell.getSheet().getWorkbook().createCellStyle();
                    newCellStyle.setFont(newFont);
                    newCellStyle.setDataFormat(newFormat);

                    newCellStyle.setAlignment(oldCell.getCellStyle().getAlignment());
                    newCellStyle.setHidden(oldCell.getCellStyle().getHidden());
                    newCellStyle.setLocked(oldCell.getCellStyle().getLocked());
                    newCellStyle.setWrapText(oldCell.getCellStyle().getWrapText());
                    newCellStyle.setBorderBottom(oldCell.getCellStyle().getBorderBottom());
                    newCellStyle.setBorderLeft(oldCell.getCellStyle().getBorderLeft());
                    newCellStyle.setBorderRight(oldCell.getCellStyle().getBorderRight());
                    newCellStyle.setBorderTop(oldCell.getCellStyle().getBorderTop());
                    newCellStyle.setBottomBorderColor(oldCell.getCellStyle().getBottomBorderColor());
                    newCellStyle.setFillBackgroundColor(oldCell.getCellStyle().getFillBackgroundColor());
                    newCellStyle.setFillForegroundColor(oldCell.getCellStyle().getFillForegroundColor());
                    newCellStyle.setFillPattern(oldCell.getCellStyle().getFillPattern());
                    newCellStyle.setIndention(oldCell.getCellStyle().getIndention());
                    newCellStyle.setLeftBorderColor(oldCell.getCellStyle().getLeftBorderColor());
                    newCellStyle.setRightBorderColor(oldCell.getCellStyle().getRightBorderColor());
                    newCellStyle.setRotation(oldCell.getCellStyle().getRotation());
                    newCellStyle.setTopBorderColor(oldCell.getCellStyle().getTopBorderColor());
                    newCellStyle.setVerticalAlignment(oldCell.getCellStyle().getVerticalAlignment());

                    styleList.add(newCellStyle);
                }
                newCell.setCellStyle(newCellStyle);
            }
        }
        switch (oldCell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                newCell.setCellValue(oldCell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_BLANK:
                newCell.setCellType(Cell.CELL_TYPE_BLANK);
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
                break;
            /*case Cell.CELL_TYPE_FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                formulaInfoList.add(new Test.FormulaInfo(oldCell.getSheet().getSheetName(), oldCell.getRowIndex(), oldCell
                        .getColumnIndex(), oldCell.getCellFormula()));
                break;*/
            default:
                break;
        }
    }

    private static CellStyle getSameCellStyle(Cell oldCell, Cell newCell, List<CellStyle> styleList) {
        CellStyle styleToFind = oldCell.getCellStyle();
        CellStyle currentCellStyle = null;
        CellStyle returnCellStyle = null;
        Iterator<CellStyle> iterator = styleList.iterator();
        Font oldFont = null;
        Font newFont = null;
        while (iterator.hasNext() && returnCellStyle == null) {
            currentCellStyle = iterator.next();

            if (currentCellStyle.getAlignment() != styleToFind.getAlignment()) {
                continue;
            }
            if (currentCellStyle.getHidden() != styleToFind.getHidden()) {
                continue;
            }
            if (currentCellStyle.getLocked() != styleToFind.getLocked()) {
                continue;
            }
            if (currentCellStyle.getWrapText() != styleToFind.getWrapText()) {
                continue;
            }
            if (currentCellStyle.getBorderBottom() != styleToFind.getBorderBottom()) {
                continue;
            }
            if (currentCellStyle.getBorderLeft() != styleToFind.getBorderLeft()) {
                continue;
            }
            if (currentCellStyle.getBorderRight() != styleToFind.getBorderRight()) {
                continue;
            }
            if (currentCellStyle.getBorderTop() != styleToFind.getBorderTop()) {
                continue;
            }
            if (currentCellStyle.getBottomBorderColor() != styleToFind.getBottomBorderColor()) {
                continue;
            }
            if (currentCellStyle.getFillBackgroundColor() != styleToFind.getFillBackgroundColor()) {
                continue;
            }
            if (currentCellStyle.getFillForegroundColor() != styleToFind.getFillForegroundColor()) {
                continue;
            }
            if (currentCellStyle.getFillPattern() != styleToFind.getFillPattern()) {
                continue;
            }
            if (currentCellStyle.getIndention() != styleToFind.getIndention()) {
                continue;
            }
            if (currentCellStyle.getLeftBorderColor() != styleToFind.getLeftBorderColor()) {
                continue;
            }
            if (currentCellStyle.getRightBorderColor() != styleToFind.getRightBorderColor()) {
                continue;
            }
            if (currentCellStyle.getRotation() != styleToFind.getRotation()) {
                continue;
            }
            if (currentCellStyle.getTopBorderColor() != styleToFind.getTopBorderColor()) {
                continue;
            }
            if (currentCellStyle.getVerticalAlignment() != styleToFind.getVerticalAlignment()) {
                continue;
            }

            oldFont = oldCell.getSheet().getWorkbook().getFontAt(oldCell.getCellStyle().getFontIndex());
            newFont = newCell.getSheet().getWorkbook().getFontAt(currentCellStyle.getFontIndex());

            if (newFont.getBoldweight() == oldFont.getBoldweight()) {
                continue;
            }
            if (newFont.getColor() == oldFont.getColor()) {
                continue;
            }
            if (newFont.getFontHeight() == oldFont.getFontHeight()) {
                continue;
            }
            if (newFont.getFontName() == oldFont.getFontName()) {
                continue;
            }
            if (newFont.getItalic() == oldFont.getItalic()) {
                continue;
            }
            if (newFont.getStrikeout() == oldFont.getStrikeout()) {
                continue;
            }
            if (newFont.getTypeOffset() == oldFont.getTypeOffset()) {
                continue;
            }
            if (newFont.getUnderline() == oldFont.getUnderline()) {
                continue;
            }
            if (newFont.getCharSet() == oldFont.getCharSet()) {
                continue;
            }
            if (oldCell.getCellStyle().getDataFormatString().equals(currentCellStyle.getDataFormatString())) {
                continue;
            }

            returnCellStyle = currentCellStyle;
        }
        return returnCellStyle;
    }

    private static void copySheetSettings(Sheet newSheet, Sheet sheetToCopy) {

        newSheet.setAutobreaks(sheetToCopy.getAutobreaks());
        newSheet.setDefaultColumnWidth(sheetToCopy.getDefaultColumnWidth());
        newSheet.setDefaultRowHeight(sheetToCopy.getDefaultRowHeight());
        newSheet.setDefaultRowHeightInPoints(sheetToCopy.getDefaultRowHeightInPoints());
        newSheet.setDisplayGuts(sheetToCopy.getDisplayGuts());
        newSheet.setFitToPage(sheetToCopy.getFitToPage());

        newSheet.setForceFormulaRecalculation(sheetToCopy.getForceFormulaRecalculation());

        PrintSetup sheetToCopyPrintSetup = sheetToCopy.getPrintSetup();
        PrintSetup newSheetPrintSetup = newSheet.getPrintSetup();

        newSheetPrintSetup.setPaperSize(sheetToCopyPrintSetup.getPaperSize());
        newSheetPrintSetup.setScale(sheetToCopyPrintSetup.getScale());
        newSheetPrintSetup.setPageStart(sheetToCopyPrintSetup.getPageStart());
        newSheetPrintSetup.setFitWidth(sheetToCopyPrintSetup.getFitWidth());
        newSheetPrintSetup.setFitHeight(sheetToCopyPrintSetup.getFitHeight());
        newSheetPrintSetup.setLeftToRight(sheetToCopyPrintSetup.getLeftToRight());
        newSheetPrintSetup.setLandscape(sheetToCopyPrintSetup.getLandscape());
        newSheetPrintSetup.setValidSettings(sheetToCopyPrintSetup.getValidSettings());
        newSheetPrintSetup.setNoColor(sheetToCopyPrintSetup.getNoColor());
        newSheetPrintSetup.setDraft(sheetToCopyPrintSetup.getDraft());
        newSheetPrintSetup.setNotes(sheetToCopyPrintSetup.getNotes());
        newSheetPrintSetup.setNoOrientation(sheetToCopyPrintSetup.getNoOrientation());
        newSheetPrintSetup.setUsePage(sheetToCopyPrintSetup.getUsePage());
        newSheetPrintSetup.setHResolution(sheetToCopyPrintSetup.getHResolution());
        newSheetPrintSetup.setVResolution(sheetToCopyPrintSetup.getVResolution());
        newSheetPrintSetup.setHeaderMargin(sheetToCopyPrintSetup.getHeaderMargin());
        newSheetPrintSetup.setFooterMargin(sheetToCopyPrintSetup.getFooterMargin());
        newSheetPrintSetup.setCopies(sheetToCopyPrintSetup.getCopies());

        Header sheetToCopyHeader = sheetToCopy.getHeader();
        Header newSheetHeader = newSheet.getHeader();
        newSheetHeader.setCenter(sheetToCopyHeader.getCenter());
        newSheetHeader.setLeft(sheetToCopyHeader.getLeft());
        newSheetHeader.setRight(sheetToCopyHeader.getRight());

        Footer sheetToCopyFooter = sheetToCopy.getFooter();
        Footer newSheetFooter = newSheet.getFooter();
        newSheetFooter.setCenter(sheetToCopyFooter.getCenter());
        newSheetFooter.setLeft(sheetToCopyFooter.getLeft());
        newSheetFooter.setRight(sheetToCopyFooter.getRight());

        newSheet.setHorizontallyCenter(sheetToCopy.getHorizontallyCenter());
        newSheet.setMargin(Sheet.LeftMargin, sheetToCopy.getMargin(Sheet.LeftMargin));
        newSheet.setMargin(Sheet.RightMargin, sheetToCopy.getMargin(Sheet.RightMargin));
        newSheet.setMargin(Sheet.TopMargin, sheetToCopy.getMargin(Sheet.TopMargin));
        newSheet.setMargin(Sheet.BottomMargin, sheetToCopy.getMargin(Sheet.BottomMargin));

        newSheet.setPrintGridlines(sheetToCopy.isPrintGridlines());
        newSheet.setRowSumsBelow(sheetToCopy.getRowSumsBelow());
        newSheet.setRowSumsRight(sheetToCopy.getRowSumsRight());
        newSheet.setVerticallyCenter(sheetToCopy.getVerticallyCenter());
        newSheet.setDisplayFormulas(sheetToCopy.isDisplayFormulas());
        newSheet.setDisplayGridlines(sheetToCopy.isDisplayGridlines());
        newSheet.setDisplayRowColHeadings(sheetToCopy.isDisplayRowColHeadings());
        newSheet.setDisplayZeros(sheetToCopy.isDisplayZeros());
        newSheet.setPrintGridlines(sheetToCopy.isPrintGridlines());
        newSheet.setRightToLeft(sheetToCopy.isRightToLeft());
        newSheet.setZoom(1, 1);
        copyPrintTitle(newSheet, sheetToCopy);
    }

    private static void copyPrintTitle(Sheet newSheet, Sheet sheetToCopy) {
        int nbNames = sheetToCopy.getWorkbook().getNumberOfNames();
        Name name = null;
        String formula = null;

        String part1S = null;
        String part2S = null;
        String formS = null;
        String formF = null;
        String part1F = null;
        String part2F = null;
        int rowB = -1;
        int rowE = -1;
        int colB = -1;
        int colE = -1;

        for (int i = 0; i < nbNames; i++) {
            name = sheetToCopy.getWorkbook().getNameAt(i);
            if (name.getSheetIndex() == sheetToCopy.getWorkbook().getSheetIndex(sheetToCopy)) {
                if (name.getNameName().equals("Print_Titles")
                        || name.getNameName().equals(XSSFName.BUILTIN_PRINT_TITLE)) {
                    formula = name.getRefersToFormula();
                    int indexComma = formula.indexOf(",");
                    if (indexComma == -1) {
                        indexComma = formula.indexOf(";");
                    }
                    String firstPart = null;
                    ;
                    String secondPart = null;
                    if (indexComma == -1) {
                        firstPart = formula;
                    } else {
                        firstPart = formula.substring(0, indexComma);
                        secondPart = formula.substring(indexComma + 1);
                    }

                    formF = firstPart.substring(firstPart.indexOf("!") + 1);
                    part1F = formF.substring(0, formF.indexOf(":"));
                    part2F = formF.substring(formF.indexOf(":") + 1);

                    if (secondPart != null) {
                        formS = secondPart.substring(secondPart.indexOf("!") + 1);
                        part1S = formS.substring(0, formS.indexOf(":"));
                        part2S = formS.substring(formS.indexOf(":") + 1);
                    }

                    rowB = -1;
                    rowE = -1;
                    colB = -1;
                    colE = -1;
                    String rowBs, rowEs, colBs, colEs;
                    if (part1F.lastIndexOf("$") != part1F.indexOf("$")) {
                        rowBs = part1F.substring(part1F.lastIndexOf("$") + 1, part1F.length());
                        rowEs = part2F.substring(part2F.lastIndexOf("$") + 1, part2F.length());
                        rowB = Integer.parseInt(rowBs);
                        rowE = Integer.parseInt(rowEs);
                        if (secondPart != null) {
                            colBs = part1S.substring(part1S.lastIndexOf("$") + 1, part1S.length());
                            colEs = part2S.substring(part2S.lastIndexOf("$") + 1, part2S.length());
                            colB = CellReference.convertColStringToIndex(colBs);// CExportExcelHelperPoi.convertColumnLetterToInt(colBs);
                            colE = CellReference.convertColStringToIndex(colEs);// CExportExcelHelperPoi.convertColumnLetterToInt(colEs);
                        }
                    } else {
                        colBs = part1F.substring(part1F.lastIndexOf("$") + 1, part1F.length());
                        colEs = part2F.substring(part2F.lastIndexOf("$") + 1, part2F.length());
                        colB = CellReference.convertColStringToIndex(colBs);// CExportExcelHelperPoi.convertColumnLetterToInt(colBs);
                        colE = CellReference.convertColStringToIndex(colEs);// CExportExcelHelperPoi.convertColumnLetterToInt(colEs);

                        if (secondPart != null) {
                            rowBs = part1S.substring(part1S.lastIndexOf("$") + 1, part1S.length());
                            rowEs = part2S.substring(part2S.lastIndexOf("$") + 1, part2S.length());
                            rowB = Integer.parseInt(rowBs);
                            rowE = Integer.parseInt(rowEs);
                        }
                    }

                    newSheet.getWorkbook().setRepeatingRowsAndColumns(newSheet.getWorkbook().getSheetIndex(newSheet),
                            colB, colE, rowB - 1, rowE - 1);
                }
            }
        }
    }

    // region Getter and Setter
    public XSSFWorkbook getUnionWb() {
        return unionWb;
    }
    // endregion
}
