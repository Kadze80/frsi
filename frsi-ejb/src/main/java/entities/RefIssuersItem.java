package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 05.05.2015.
 */
public class RefIssuersItem extends AbstractReference {
    public static final String REF_CODE = "ref_issuers";
    private String signName;
    private Boolean isState;
    private Boolean isResident;
    private String listingEstimation;
    private String ratingEstimation;
    private Boolean isFromKase;

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_ISSUERS_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_ISSUERS_L_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefIssuersItem item, Date date) throws SQLException {
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
    public static RefIssuersItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefIssuersItem item = new RefIssuersItem();
        item = (RefIssuersItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        item.setSignName(cursor.getString("SIGN_NAME"));
        item.setIsState(cursor.getInt("IS_STATE") > 0);
        item.setIsResident(cursor.getInt("IS_RESIDENT") > 0);
        item.setListingEstimation(cursor.getString("LISTING_ESTIMATION"));
        item.setRatingEstimation(cursor.getString("RATING_ESTIMATION"));
        item.setIsFromKase(cursor.getInt("IS_FROM_KASE") > 0);
        return item;
    }
    
    // region Getter and Setter
    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }

    public Boolean getIsState() {
        return isState;
    }

    public void setIsState(Boolean isState) {
        this.isState = isState;
    }

    public Boolean getIsResident() {
        return isResident;
    }

    public void setIsResident(Boolean isResident) {
        this.isResident = isResident;
    }

    public String getListingEstimation() {
        return listingEstimation;
    }

    public void setListingEstimation(String listingEstimation) {
        this.listingEstimation = listingEstimation;
    }

    public String getRatingEstimation() {
        return ratingEstimation;
    }

    public void setRatingEstimation(String ratingEstimation) {
        this.ratingEstimation = ratingEstimation;
    }

    public Boolean getIsFromKase() {
        return isFromKase;
    }

    public void setIsFromKase(Boolean isFromKase) {
        this.isFromKase = isFromKase;
    }
    // endregion
}
