package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 04.05.2015.
 */
public class RefTransTypeItem extends AbstractReference {
    public static final String REF_CODE = "ref_trans_types";

    private String kindOfActivity;

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_TRANS_TYPES_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_T_T_LIST_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "PKG_FRSI_REF.REF_READ_TRANS_TYPES_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_TRANS_TYPES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_TRANS_TYPES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_TRANS_TYPES (?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefTransTypeItem item, Date date) throws SQLException {
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

        if (item == null || item.getKindOfActivity() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getKindOfActivity());

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
    public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefTransTypeItem item) throws SQLException{
        int num = 0;

        if (item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        } else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

        return ocsNumMap;
    }

    // Update
    public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefTransTypeItem item) throws SQLException {
        int num = 0;

        ocsNumMap.getOcs().setLong(++num, item.getId());
        ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

        return ocsNumMap;
    }

    // Edit for Insert and Update
    public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefTransTypeItem item, int num, String mode) throws SQLException{
        ocsNumMap.getOcs().setString(++num, item.getCode());
        ocsNumMap.getOcs().setString(++num, item.getNameKz());
        ocsNumMap.getOcs().setString(++num, item.getNameRu());
        ocsNumMap.getOcs().setString(++num, item.getNameEn());
        ocsNumMap.getOcs().setString(++num, item.getKindOfActivity());
        ocsNumMap.getOcs().setString(++num, item.getShortNameRu());
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
    public static RefTransTypeItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefTransTypeItem item = new RefTransTypeItem();
        item = (RefTransTypeItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        item.setKindOfActivity(cursor.getString("KIND_OF_ACTIVITY"));
        item.setShortNameRu(cursor.getString("SHORT_NAME"));
        return item;
    }

    // for History Cursor
    public static RefTransTypeItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
        RefTransTypeItem item = setItemFromCursor(cursor);
        item = (RefTransTypeItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);
        return item;
    }

    // region Getter and Setter
    public String getKindOfActivity() {
        return kindOfActivity;
    }

    public void setKindOfActivity(String kindOfActivity) {
        this.kindOfActivity = kindOfActivity;
    }

    // endregion

}
