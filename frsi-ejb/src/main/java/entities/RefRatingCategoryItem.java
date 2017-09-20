package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 27.07.2015.
 */
public class RefRatingCategoryItem extends AbstractReference {
    public static final String REF_CODE = "ref_rating_category";

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_RATING_CATEGORY_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_RAT_CAT_L_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefRatingCategoryItem item, Date date) throws SQLException {
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

        if (item == null || item.getCode() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getCode());

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
    public static RefRatingCategoryItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefRatingCategoryItem item = new RefRatingCategoryItem();
        item = (RefRatingCategoryItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        return item;
    }


}
