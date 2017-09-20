package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 20.04.2015.
 */
public class RefPersonItem extends AbstractReference {
    public static final String REF_CODE = "ref_person";

    private String  fm;
    private String  nm;
    private String  ft;
    private String  fioRu;
    private String  fioKz;
    private String  fioEn;
    private Long    refCountry;
    private Long    refCountryRecId;
    private String  countryName;
    private String  phoneWork;
    private String  fax;
    private String  addressWork;
    private String  idn;
    private Boolean userConfirm;
    private String  passport;

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_PERSON_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_PERSON_LIST_BY_PARAMS(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "PKG_FRSI_REF.REF_READ_PERSON_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_PERSON (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_PERSON (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_PERSON (?, ?, ?, ?)";


    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefPersonItem item, Date date) throws SQLException {
        int num = 0;

        if (item == null || item.getId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setLong(++num, item.getId());

        if (date == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setDate(++num, new java.sql.Date(date.getTime()));

        if (item == null || item.getIdn() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getIdn());

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

        if (item == null || item.getUserConfirm() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setBoolean(++num, item.getUserConfirm());

        if (item == null || item.getSearchAllVer() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setBoolean(++num, item.getSearchAllVer());

        ocsNumMap = AbstractReference.setDefaultOcsNumMapForCursor(ocsNumMap, num);

        return ocsNumMap;
    }

    // Insert
    public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefPersonItem item) throws SQLException{
        int num = 0;

        if (item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        } else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

        return ocsNumMap;
    }

    // Update
    public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefPersonItem item) throws SQLException {
        int num = 0;

        ocsNumMap.getOcs().setLong(++num, item.getId());
        ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

        return ocsNumMap;
    }

    // Edit for Insert and Update
    public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefPersonItem item, int num, String mode) throws SQLException{
        ocsNumMap.getOcs().setString(++num, item.getCode());
        ocsNumMap.getOcs().setString(++num, item.getIdn());
        ocsNumMap.getOcs().setString(++num, item.getFm());
        ocsNumMap.getOcs().setString(++num, item.getNm());
        ocsNumMap.getOcs().setString(++num, item.getFt());
        ocsNumMap.getOcs().setString(++num, item.getFioKz());
        ocsNumMap.getOcs().setString(++num, item.getFioEn());
        if (item.getRefCountry() == 0) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }
        ocsNumMap.getOcs().setLong(++num, item.getRefCountry());
        ocsNumMap.getOcs().setString(++num, item.getPhoneWork());
        ocsNumMap.getOcs().setString(++num, item.getFax());
        ocsNumMap.getOcs().setString(++num, item.getAddressWork());
        ocsNumMap.getOcs().setString(++num, item.getNote());
        ocsNumMap.getOcs().setString(++num, item.getPassport());
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
    public static RefPersonItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefPersonItem item = new RefPersonItem();
        item.setId(cursor.getLong("ID"));
        item.setRecId(cursor.getLong("REC_ID"));
        item.setCode(cursor.getString("CODE"));
        item.setIdn(cursor.getString("IDN"));
        item.setFm(cursor.getString("FM"));
        item.setNm(cursor.getString("NM"));
        item.setFt(cursor.getString("FT"));
        item.setFioRu(cursor.getString("FM") + " " + cursor.getString("NM") + " " + cursor.getString("FT"));
        item.setFioKz(cursor.getString("FIO_KZ"));
        item.setFioEn(cursor.getString("FIO_EN"));
        item.setRefCountry(cursor.getLong("REF_COUNTRY"));
        item.setRefCountryRecId(cursor.getLong("REF_COUNTRY_REC_ID"));
        item.setCountryName(cursor.getString("COUNTRY_NAME"));
        item.setPhoneWork(cursor.getString("PHONE_WORK"));
        item.setFax(cursor.getString("FAX"));
        item.setAddressWork(cursor.getString("ADDRESS_WORK"));
        item.setNote(cursor.getString("NOTE"));
        item.setPassport(cursor.getString("PASSPORT"));
        item.setBeginDate(cursor.getDate("BEGIN_DATE"));
        item.setEndDate(cursor.getDate("END_DATE"));
        item.setDatlast(cursor.getTimestamp("DATLAST"));
        item.setUserName(cursor.getString("USER_NAME"));
        item.setUserLocation(cursor.getString("USER_LOCATION"));
        item.setSentKnd(cursor.getString("SENT_KND"));
        item.setUserConfirm(cursor.getInt("USER_CONFIRM") > 0);
        return item;
    }

    // for History Cursor
    public static RefPersonItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
        RefPersonItem item = setItemFromCursor(cursor);
        item = (RefPersonItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);

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

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getPhoneWork() {
        return phoneWork;
    }

    public void setPhoneWork(String phoneWork) {
        this.phoneWork = phoneWork;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getAddressWork() {
        return addressWork;
    }

    public void setAddressWork(String addressWork) {
        this.addressWork = addressWork;
    }

    public Long getRefCountry() {
        return refCountry;
    }

    public void setRefCountry(Long refCountry) {
        this.refCountry = refCountry;
    }

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    public Long getRefCountryRecId() {
        return refCountryRecId;
    }

    public void setRefCountryRecId(Long refCountryRecId) {
        this.refCountryRecId = refCountryRecId;
    }

    public String getFioRu() {
        return fioRu;
    }

    public void setFioRu(String fioRu) {
        this.fioRu = fioRu;
    }

    public Boolean getUserConfirm() {
        return userConfirm;
    }

    public void setUserConfirm(Boolean userConfirm) {
        this.userConfirm = userConfirm;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    // endregion
}
