package parser;

import entities.SortField;

import java.util.Comparator;
import java.util.List;

/**
 * Created by nuriddin on 9/14/16.
 */
public class RowComparator implements Comparator<Row> {
    private List<SortField> sortFields;
    private AbstractDataProvider dataProvider;

    @Override
    public int compare(Row r1, Row r2) {
        for (SortField sortField : sortFields) {
            String f = sortField.name.substring(sortField.name.indexOf("*") + 1);
            Cell c1 = r1.getCell(f);
            Cell c2 = r2.getCell(f);
            if (c1 == null || c2 == null)
                continue;

            int r;
            if (sortField.refName != null && !sortField.refName.isEmpty()) {
                String s1 = dataProvider.getRefCaption(sortField.refName, sortField.captionField, c1.lngValue);
                String s2 = dataProvider.getRefCaption(sortField.refName, sortField.captionField, c2.lngValue);
                if (s1 == null || s2 == null)
                    continue;
                r = s1.compareTo(s2);
            } else {
                r = c1.compareTo(c2);
            }

            if (r == 0) continue;
            if (sortField.desc != null && sortField.desc)
                return -1 * r;
            else
                return r;
        }
        return 0;
    }

    public void setSortFields(List<SortField> sortFields) {
        this.sortFields = sortFields;
    }

    public void setDataProvider(AbstractDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }
}
