package excelreport;

import dataform.IKeyHandler;

/**
 * Created by Nuriddin.Baideuov on 23.11.2015.
 */
public abstract class ReportSheet {
    public abstract void setSheetTitle(String title);
    public abstract void out(String nameBand, IKeyHandler keyHandler);
    public abstract boolean hasBand(String nameBand);
}
