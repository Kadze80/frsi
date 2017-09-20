package entities;

import oracle.jdbc.OracleTypes;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class RefPeriodItem extends AbstractReference {
    public static final String REF_CODE = "ref_period";

    private Long refPeriodAlgId;
    private String periodAlgName;
    private String alg;
    private Boolean autoApprove;

    // prepareCall
    public static final String READ = "TMP_PKG_FRSI_REF.REF_READ_PERIOD_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "TMP_PKG_FRSI_REF.REF_READ_PERIOD_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "TMP_PKG_FRSI_REF.REF_READ_PERIOD_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "TMP_PKG_FRSI_REF.REF_INSERT_PERIOD (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "TMP_PKG_FRSI_REF.REF_UPDATE_PERIOD (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "TMP_PKG_FRSI_REF.REF_DELETE_PERIOD (?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefPeriodItem item, Date date) throws SQLException {
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

        if (item == null || item.getRefPeriodAlgId() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setLong(++num, item.getRefPeriodAlgId());

        if (item == null || item.getSearchAllVer() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setBoolean(++num, item.getSearchAllVer());

        ocsNumMap = AbstractReference.setDefaultOcsNumMapForCursor(ocsNumMap, num);

        return ocsNumMap;
    }

    // Insert
    public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefPeriodItem item) throws SQLException{
        int num = 0;

        if (item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        } else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

        return ocsNumMap;
    }

    // Update
    public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefPeriodItem item) throws SQLException {
        int num = 0;

        ocsNumMap.getOcs().setLong(++num, item.getId());
        ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

        return ocsNumMap;
    }

    // Edit for Insert and Update
    public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefPeriodItem item, int num, String mode) throws SQLException{
        ocsNumMap.getOcs().setString(++num, item.getCode());
        ocsNumMap.getOcs().setString(++num, item.getNameKz());
        ocsNumMap.getOcs().setString(++num, item.getNameRu());
        ocsNumMap.getOcs().setString(++num, item.getNameEn());
        ocsNumMap.getOcs().setBoolean(++num, item.getAutoApprove());
        ocsNumMap.getOcs().setLong(++num, item.getRefPeriodAlgId());
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
    public static RefPeriodItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefPeriodItem item = new RefPeriodItem();
        item = (RefPeriodItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        Clob alg = cursor.getClob("ALG");
        if (alg != null) {
            item.setAlg(alg.getSubString(1, (int) alg.length()));
            alg.free();
        }
        item.setPeriodAlgName(cursor.getString("PERIOD_ALG_NAME"));
        item.setRefPeriodAlgId(cursor.getLong("REF_PERIOD_ALG"));
        item.setAutoApprove(cursor.getInt("AUTO_APPROVE") > 0);
        return item;
    }

    // for History Cursor
    public static RefPeriodItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
        RefPeriodItem item = setItemFromCursor(cursor);
        item = (RefPeriodItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);
        return item;
    }


    // region Getter and Setter

    public Long getRefPeriodAlgId() {
        return refPeriodAlgId;
    }

    public void setRefPeriodAlgId(Long refPeriodAlgId) {
        this.refPeriodAlgId = refPeriodAlgId;
    }

    public Boolean getAutoApprove() {
        return autoApprove;
    }

    public void setAutoApprove(Boolean autoApprove) {
        this.autoApprove = autoApprove;
    }

    public String getPeriodAlgName() {
        return periodAlgName;
    }

    public void setPeriodAlgName(String periodAlgName) {
        this.periodAlgName = periodAlgName;
    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    // endregion
}
