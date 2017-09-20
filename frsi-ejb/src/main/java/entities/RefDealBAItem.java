package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 12.05.2016.
 */
public class RefDealBAItem extends AbstractReference {
    public static final String REF_CODE = "ref_deal_balance_acc";
    private String numAcc;

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_DEAL_BALANCE_ACC_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_DEAL_BA_L_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "PKG_FRSI_REF.REF_READ_DEAL_BA_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_DEAL_BALANCE_ACC (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_DEAL_BALANCE_ACC (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_DEAL_BALANCE_ACC (?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefDealBAItem item, Date date) throws SQLException {
        int num = 0;

        if (item == null || item.getId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setLong(++num, item.getId());

        if (date == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
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

    // Insert
    public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefDealBAItem item) throws SQLException{
        int num = 0;

        if (item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        } else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

        return ocsNumMap;
    }

    // Update
    public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefDealBAItem item) throws SQLException {
        int num = 0;

        ocsNumMap.getOcs().setLong(++num, item.getId());
        ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

        return ocsNumMap;
    }

    // Edit for Insert and Update
    public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefDealBAItem item, int num, String mode) throws SQLException{
        ocsNumMap.getOcs().setString(++num, item.getCode());
        ocsNumMap.getOcs().setString(++num, item.getNameKz());
        ocsNumMap.getOcs().setString(++num, item.getNameRu());
        ocsNumMap.getOcs().setString(++num, item.getNameEn());
        ocsNumMap.getOcs().setString(++num, item.getShortNameKz());
        ocsNumMap.getOcs().setString(++num, item.getShortNameRu());
        ocsNumMap.getOcs().setString(++num, item.getShortNameEn());
        ocsNumMap.getOcs().setString(++num, item.getNumAcc());
        ocsNumMap.getOcs().setDate(++num, new java.sql.Date(item.getBeginDate().getTime()));
        ocsNumMap.getOcs().setDate(++num, item.getEndDate() == null ? null : new java.sql.Date(item.getEndDate().getTime()));
        ocsNumMap.getOcs().setLong(++num, item.getUserId());
        ocsNumMap.getOcs().setString(++num, item.getUserLocation());
        ocsNumMap.getOcs().setDate(++num, item.getDatlast() == null ? null : new java.sql.Date(item.getDatlast().getTime()));
        ocsNumMap.getOcs().setInt(++num, 1);

        if(mode.equals("ins"))
            ocsNumMap = AbstractReference.setDefaultOcsNumMapForIns(ocsNumMap, num);
        else if (mode.equals("upd"))
            ocsNumMap = AbstractReference.setDefaultOcsNumMap(ocsNumMap, num);

        return ocsNumMap;
    }

    // for Cursor
    public static RefDealBAItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefDealBAItem item = new RefDealBAItem();
        item = (RefDealBAItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        item.setShortNameKz(cursor.getString("SHORT_NAME_KZ"));
        item.setShortNameRu(cursor.getString("SHORT_NAME_RU"));
        item.setShortNameEn(cursor.getString("SHORT_NAME_EN"));
        item.setNumAcc(cursor.getString("NUM_ACC"));

        return item;
    }

    // for History Cursor
    public static RefDealBAItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
        RefDealBAItem item = setItemFromCursor(cursor);
        item = (RefDealBAItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);

        return item;
    }

    // region Getter and Setter
    public String getNumAcc() {
        return numAcc;
    }

    public void setNumAcc(String numAcc) {
        this.numAcc = numAcc;
    }

    // endregion
}
