package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 28.04.2015.
 */
public class RefManagersItem extends AbstractReference {
    public static final String REF_CODE = "ref_managers";

    private String fm;
    private String nm;
    private String ft;
    private String fioRu;
    private String fioKz;
    private String fioEn;
    private Long refPostId;
    private String postNameRu;
    private String phone;
    private Boolean executor;

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_MANAGERS_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_MANAGERS_L_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "PKG_FRSI_REF.REF_READ_MANAGERS_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_MANAGERS (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_MANAGERS (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_MANAGERS (?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefManagersItem item, Date date) throws SQLException {
        int num = 0;

        if (item == null || item.getId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setLong(++num, item.getId());

        if (date == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setDate(++num, new java.sql.Date(date.getTime()));

        if (item == null || item.getFm() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getFm());

        if (item == null || item.getNm() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getNm());

        if (item == null || item.getFt() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getFt());

        if (item == null || item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        if (item == null || item.getRefPostId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setLong(++num, item.getRefPostId());

        if (item == null || item.getExecutor() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setBoolean(++num, item.getExecutor());

        if (item == null || item.getSearchAllVer() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setBoolean(++num, item.getSearchAllVer());

        ocsNumMap = AbstractReference.setDefaultOcsNumMapForCursor(ocsNumMap, num);

        return ocsNumMap;
    }

    // Insert
    public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefManagersItem item) throws SQLException{
        int num = 0;

        if (item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        } else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

        return ocsNumMap;
    }

    // Update
    public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefManagersItem item) throws SQLException {
        int num = 0;

        ocsNumMap.getOcs().setLong(++num, item.getId());
        ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

        return ocsNumMap;
    }

    // Edit for Insert and Update
    public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefManagersItem item, int num, String mode) throws SQLException{
        ocsNumMap.getOcs().setString(++num, item.getCode());
        ocsNumMap.getOcs().setString(++num, item.getFm());
        ocsNumMap.getOcs().setString(++num, item.getNm());
        ocsNumMap.getOcs().setString(++num, item.getFt());
        ocsNumMap.getOcs().setString(++num, item.getFioKz());
        ocsNumMap.getOcs().setString(++num, item.getFioEn());
        ocsNumMap.getOcs().setLong(++num, item.getRefPostId());
        ocsNumMap.getOcs().setString(++num, item.getPhone());
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
    public static RefManagersItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefManagersItem item = new RefManagersItem();
        item.setId(cursor.getLong("ID"));
        item.setRecId(cursor.getLong("REC_ID"));
        item.setCode(cursor.getString("CODE"));
        item.setFm(cursor.getString("FM"));
        item.setNm(cursor.getString("NM"));
        item.setFt(cursor.getString("FT"));
        item.setFioRu(cursor.getString("FIO_RU"));
        item.setFioKz(cursor.getString("FIO_KZ"));
        item.setFioEn(cursor.getString("FIO_EN"));
        item.setRefPostId(cursor.getLong("REF_POST"));
        item.setPostNameRu(cursor.getString("POST_NAME_RU"));
        item.setPhone(cursor.getString("PHONE"));
        item.setExecutor(cursor.getInt("IS_EXECUTOR") > 0);
        item.setBeginDate(cursor.getDate("BEGIN_DATE"));
        item.setEndDate(cursor.getDate("END_DATE"));
        item.setDatlast(cursor.getTimestamp("DATLAST"));
        item.setUserName(cursor.getString("USER_NAME"));
        item.setUserLocation(cursor.getString("USER_LOCATION"));
        item.setSentKnd(cursor.getString("SENT_KND"));
        return item;
    }

    // for History Cursor
    public static RefManagersItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
        RefManagersItem item = setItemFromCursor(cursor);
        item = (RefManagersItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);
        return item;
    }

    // region Getter and Setter
    public String getFm() {
        return fm;
    }

    public void setFm(String fm) {
        this.fm = fm;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public String getFt() {
        return ft;
    }

    public void setFt(String ft) {
        this.ft = ft;
    }

    public String getFioKz() {
        return fioKz;
    }

    public void setFioKz(String fioKz) {
        this.fioKz = fioKz;
    }

    public String getFioEn() {
        return fioEn;
    }

    public void setFioEn(String fioEn) {
        this.fioEn = fioEn;
    }

    public String getFioRu() {
        return fioRu;
    }

    public void setFioRu(String fioRu) {
        this.fioRu = fioRu;
    }

    public Long getRefPostId() {
        return refPostId;
    }

    public void setRefPostId(Long refPostId) {
        this.refPostId = refPostId;
    }

    public String getPostNameRu() {
        return postNameRu;
    }

    public void setPostNameRu(String postNameRu) {
        this.postNameRu = postNameRu;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getExecutor() {
        return executor;
    }

    public void setExecutor(Boolean executor) {
        this.executor = executor;
    }

    // endregion
}
