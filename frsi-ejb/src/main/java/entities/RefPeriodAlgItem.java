package entities;

import oracle.jdbc.OracleTypes;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class RefPeriodAlgItem extends AbstractReference {
    public static final String REF_CODE = "ref_period_alg";

    private String alg;
    private boolean onlyWorkingDays;


    // prepareCall
    public static final String READ = "TMP_PKG_FRSI_REF.REF_READ_PERIOD_ALG_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "TMP_PKG_FRSI_REF.REF_READ_PERIOD_ALG_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "TMP_PKG_FRSI_REF.REF_READ_PERIOD_ALG_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "TMP_PKG_FRSI_REF.REF_INSERT_PERIOD_ALG (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "TMP_PKG_FRSI_REF.REF_UPDATE_PERIOD_ALG (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "TMP_PKG_FRSI_REF.REF_DELETE_PERIOD_ALG (?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefPeriodAlgItem item, Date date) throws SQLException {
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
    public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefPeriodAlgItem item) throws SQLException{
        int num = 0;

        if (item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        } else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

        return ocsNumMap;
    }

    // Update
    public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefPeriodAlgItem item) throws SQLException {
        int num = 0;

        ocsNumMap.getOcs().setLong(++num, item.getId());
        ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

        return ocsNumMap;
    }

    // Edit for Insert and Update
    public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefPeriodAlgItem item, int num, String mode) throws SQLException{
        ocsNumMap.getOcs().setString(++num, item.getCode());
        ocsNumMap.getOcs().setString(++num, item.getNameKz());
        ocsNumMap.getOcs().setString(++num, item.getNameRu());
        ocsNumMap.getOcs().setString(++num, item.getNameEn());
        ocsNumMap.getOcs().setString(++num, item.getAlg());
        ocsNumMap.getOcs().setDate(++num, new java.sql.Date(item.getBeginDate().getTime()));
        ocsNumMap.getOcs().setDate(++num, item.getEndDate() == null ? null : new java.sql.Date(item.getEndDate().getTime()));
        ocsNumMap.getOcs().setLong(++num, item.getUserId());
        ocsNumMap.getOcs().setString(++num, item.getUserLocation());
        ocsNumMap.getOcs().setDate(++num, item.getDatlast() == null ? null : new java.sql.Date(item.getDatlast().getTime()));
        ocsNumMap.getOcs().setInt(++num, item.isOnlyWorkingDays() ? 1 : 0);
        ocsNumMap.getOcs().setInt(++num, 1);

        if(mode.equals("ins"))
            ocsNumMap = AbstractReference.setDefaultOcsNumMapForIns(ocsNumMap, num);
        else if (mode.equals("upd"))
            ocsNumMap = AbstractReference.setDefaultOcsNumMap(ocsNumMap, num);

        return ocsNumMap;
    }

    // for Cursor
    public static RefPeriodAlgItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefPeriodAlgItem item = new RefPeriodAlgItem();
        item = (RefPeriodAlgItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        Clob alg = cursor.getClob("ALG");
        if (alg != null) {
            item.setAlg(alg.getSubString(1, (int) alg.length()));
            alg.free();
        }
        item.setOnlyWorkingDays(cursor.getInt("ONLY_WORKING_DAYS") == 1);
        return item;
    }

    // for History Cursor
    public static RefPeriodAlgItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
        RefPeriodAlgItem item = setItemFromCursor(cursor);
        item = (RefPeriodAlgItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);
        return item;
    }


    // region Getter and Setter
    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public boolean isOnlyWorkingDays() {
        return onlyWorkingDays;
    }

    public void setOnlyWorkingDays(boolean onlyWorkingDays) {
        this.onlyWorkingDays = onlyWorkingDays;
    }

    // endregion
}
