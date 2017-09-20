package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 16.04.2015.
 */
public class RefPostItem extends AbstractReference {
    public static final String REF_CODE = "ref_post";

    private Long typePostId;
    private String typePostName;
    private Boolean isActivity;
    private Boolean isMainRuk;

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_POST_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_POST_LIST_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "PKG_FRSI_REF.REF_READ_POST_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_POST (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_POST (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_POST (?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefPostItem item, Date date) throws SQLException{
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
    public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefPostItem item) throws SQLException{
        int num = 0;

        if (item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        } else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

        return ocsNumMap;
    }

    // Update
    public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefPostItem item) throws SQLException {
        int num = 0;

        ocsNumMap.getOcs().setLong(++num, item.getId());
        ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

        return ocsNumMap;
    }

    // Edit for Insert and Update
    public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefPostItem item, int num, String mode) throws SQLException{
        ocsNumMap.getOcs().setString(++num, item.getCode());
        ocsNumMap.getOcs().setString(++num, item.getNameKz());
        ocsNumMap.getOcs().setString(++num, item.getNameRu());
        ocsNumMap.getOcs().setString(++num, item.getNameEn());
        ocsNumMap.getOcs().setLong(++num, item.getTypePostId());
        ocsNumMap.getOcs().setBoolean(++num, item.getIsActivity());
        ocsNumMap.getOcs().setBoolean(++num, item.getIsMainRuk());
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
    public static RefPostItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefPostItem item = new RefPostItem();
        item = (RefPostItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        item.setTypePostId(cursor.getLong("TYPE_POST_ID"));
        item.setTypePostName(cursor.getString("TYPE_POST_NAME"));
        item.setIsActivity(cursor.getInt("IS_ACTIVITY") > 0);
        item.setIsMainRuk(cursor.getInt("IS_MAIN_RUK") > 0);

        return item;
    }

    // for History Cursor
    public static RefPostItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
        RefPostItem item = setItemFromCursor(cursor);
        item = (RefPostItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);

        return item;
    }


    // region Getters and Setters

    public String getTypePostName() {
        return typePostName;
    }

    public void setTypePostName(String typePostName) {
        this.typePostName = typePostName;
    }

    public Boolean getIsActivity() {
        return isActivity;
    }

    public void setIsActivity(Boolean isActivity) {
        this.isActivity = isActivity;
    }

    public Boolean getIsMainRuk() {
        return isMainRuk;
    }

    public void setIsMainRuk(Boolean isMainRuk) {
        this.isMainRuk = isMainRuk;
    }

    public Long getTypePostId() {
        return typePostId;
    }

    public void setTypePostId(Long typePostId) {
        this.typePostId = typePostId;
    }
    // endregion

}

