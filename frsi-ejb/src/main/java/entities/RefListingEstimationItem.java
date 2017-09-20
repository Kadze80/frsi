package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 27.07.2015.
 */
public class RefListingEstimationItem extends AbstractReference {
    public static final String REF_CODE = "ref_listing_estimation";
    private String priority;

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_LISTING_EST_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_LIS_EST_L_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefListingEstimationItem item, Date date) throws SQLException {
        int num = 0;

        if (item == null || item.getId() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setLong(++num, item.getId());

        if (date == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setDate(++num, new java.sql.Date(date.getTime()));

        if (item == null || item.getNameRu() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setString(++num, item.getNameRu());

        if (item == null || item.getRecId() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        if (item == null || item.getSearchAllVer() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setBoolean(++num, item.getSearchAllVer());

        ocsNumMap = AbstractReference.setDefaultOcsNumMapForCursor(ocsNumMap, num);

        return ocsNumMap;
    }

    // for Cursor
    public static RefListingEstimationItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefListingEstimationItem item = new RefListingEstimationItem();
        item = (RefListingEstimationItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        item.setPriority(cursor.getString("PRIORITY"));
        return item;
    }

    // region Getter and Setter
    public String getPriority() {
        return priority;
    }
    public void setPriority(String priority) {
        this.priority = priority;
    }
    // endregion
}
