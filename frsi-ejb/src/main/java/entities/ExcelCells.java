package entities;

import java.io.Serializable;

/**
 * Created by Baurzhan.Baisholakov on 26.05.2015.
 */
public class ExcelCells implements Serializable {
    private static final long serialVersionUID = 1L;
    private ExcelCell[] excelCells;

    public ExcelCell[] getExcelCells() {
        return excelCells;
    }

    public ExcelCell getCell(int position) {
        return excelCells[position];
    }

    public void setExcelCells(ExcelCell[] excelCells) {
        this.excelCells = excelCells;
    }
}
