package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 27.07.2015.
 */
public class RefRatingEstimationItem extends AbstractReference {
    public static final String REF_CODE = "ref_rating_estimation";

    private String priority;
    private Long refRatingCategory;
    private Long refRatingCategoryRecId;
    private String ratingCategoryName;

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_RATING_EST_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_RAT_EST_L_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefRatingEstimationItem item, Date date) throws SQLException {
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

        if (item == null || item.getRatingCategoryName() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getRatingCategoryName());

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
    public static RefRatingEstimationItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefRatingEstimationItem item = new RefRatingEstimationItem();
        item = (RefRatingEstimationItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        item.setPriority(cursor.getString("PRIORITY"));
        item.setRefRatingCategory(cursor.getLong("REF_RATING_CATEGORY"));
        item.setRefRatingCategoryRecId(cursor.getLong("REF_RATING_CATEGORY_REC_ID"));
        item.setRatingCategoryName(cursor.getString("RATING_CATEGORY_NAME"));
        return item;
    }

    // region Getter and Setter
    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getRefRatingCategory() {
        return refRatingCategory;
    }

    public void setRefRatingCategory(Long refRatingCategory) {
        this.refRatingCategory = refRatingCategory;
    }

    public String getRatingCategoryName() {
        return ratingCategoryName;
    }

    public void setRatingCategoryName(String ratingCategoryName) {
        this.ratingCategoryName = ratingCategoryName;
    }

    public Long getRefRatingCategoryRecId() {
        return refRatingCategoryRecId;
    }

    public void setRefRatingCategoryRecId(Long refRatingCategoryRecId) {
        this.refRatingCategoryRecId = refRatingCategoryRecId;
    }
    // endregion
}
