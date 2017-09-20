package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 06.05.2015.
 */
public class RefVidOperItem extends AbstractReference {
    public static final String REF_CODE = "ref_vid_oper";

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_VID_OPER_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_VID_OPER_L_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "PKG_FRSI_REF.REF_READ_VID_OPER_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_VID_OPER (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_VID_OPER (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_VID_OPER (?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefVidOperItem item, Date date) throws SQLException {
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
    public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefVidOperItem item) throws SQLException{
        int num = 0;

        if (item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        } else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

        return ocsNumMap;
    }

    // Update
    public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefVidOperItem item) throws SQLException {
        int num = 0;

        ocsNumMap.getOcs().setLong(++num, item.getId());
        ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

        return ocsNumMap;
    }

    // Edit for Insert and Update
    public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefVidOperItem item, int num, String mode) throws SQLException{
        ocsNumMap.getOcs().setString(++num, item.getCode());
        ocsNumMap.getOcs().setString(++num, item.getNameKz());
        ocsNumMap.getOcs().setString(++num, item.getNameRu());
        ocsNumMap.getOcs().setString(++num, item.getNameEn());
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
    public static RefVidOperItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefVidOperItem item = new RefVidOperItem();
        item = (RefVidOperItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        return item;
    }

    // for History Cursor
    public static RefVidOperItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
        RefVidOperItem item = setItemFromCursor(cursor);
        item = (RefVidOperItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);

        return item;
    }    


}
