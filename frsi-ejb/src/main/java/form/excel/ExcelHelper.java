package form.excel;

import org.apache.poi.ss.usermodel.*;

public class ExcelHelper {

    public static void cloneStyle(Cell sourceCell, Cell targetCell) {
        targetCell.setCellStyle(sourceCell.getCellStyle());
        Sheet sheet = sourceCell.getSheet();
        sheet.setColumnWidth(targetCell.getColumnIndex(), sheet.getColumnWidth(sourceCell.getColumnIndex()));
    }

    public static void insertNewColumnBefore(Sheet sheet, int columnIndex) {
        int nrRows = getNumberOfRows(sheet);

        int maxCell = 0;
        for (int row = 0; row < nrRows; row++) {
            Row r = sheet.getRow(row);

            if (r == null) {
                continue;
            }

            // shift to right
            int lastCell =  r.getLastCellNum();
            if(lastCell>maxCell){
                maxCell = lastCell;
            }
            for (int col = lastCell; col > columnIndex; col--) {
                Cell rightCell = r.getCell(col);
                if (rightCell != null) {
                    r.removeCell(rightCell);
                }

                Cell leftCell = r.getCell(col - 1);

                if (leftCell != null) {
                    Cell newCell = r.createCell(col, leftCell.getCellType());
                    cloneCell(newCell, leftCell);
                }
            }

            // delete old column
            int cellType = Cell.CELL_TYPE_BLANK;

            Cell currentEmptyWeekCell = r.getCell(columnIndex);
            if (currentEmptyWeekCell != null) {
//				cellType = currentEmptyWeekCell.getCellType();
                r.removeCell(currentEmptyWeekCell);
            }

            // create new column
            r.createCell(columnIndex, cellType);
        }

        // Adjust the column widths
        for (int col = maxCell; col > columnIndex; col--) {
            sheet.setColumnWidth(col, sheet.getColumnWidth(col - 1));
        }
    }

    /*
	 * Takes an existing Cell and merges all the styles and forumla into the new
	 * one
	 */
    private static void cloneCell(Cell cNew, Cell cOld) {
        cNew.setCellComment(cOld.getCellComment());
        cNew.setCellStyle(cOld.getCellStyle());

        switch (cOld.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN: {
                cNew.setCellValue(cOld.getBooleanCellValue());
                break;
            }
            case Cell.CELL_TYPE_NUMERIC: {
                cNew.setCellValue(cOld.getNumericCellValue());
                break;
            }
            case Cell.CELL_TYPE_STRING: {
                cNew.setCellValue(cOld.getStringCellValue());
                break;
            }
            case Cell.CELL_TYPE_ERROR: {
                cNew.setCellValue(cOld.getErrorCellValue());
                break;
            }
            case Cell.CELL_TYPE_FORMULA: {
                cNew.setCellFormula(cOld.getCellFormula());
                break;
            }
        }
    }

    public static int getNumberOfRows(Sheet sheet) {
        return sheet.getLastRowNum() + 1;
    }
}
