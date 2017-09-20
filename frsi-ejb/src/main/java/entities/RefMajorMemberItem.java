package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Zhanar.Sanaupova on 22.06.2017.
 */
public class RefMajorMemberItem extends AbstractReference {

    public static final String REF_CODE = "ref_major_member";

    private Long refUnionPersons;
    private String idn;
    private Boolean isNonRezident;
    private Long refCountry;
    private Long refCountryRecId;
    private String countryName;
    private String legalAddress;


    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_MAJOR_MEMBER_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_MM_LIST_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "PKG_FRSI_REF.REF_READ_MAJOR_MEMBER_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_MAJOR_MEMBER(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_MAJOR_MEMBER(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_MAJOR_MEMBER(?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefMajorMemberItem item, Date date) throws SQLException {
        int num = 0;

        if (item == null || item.getId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setLong(++num, item.getId());

        if (date == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setDate(++num, new java.sql.Date(date.getTime()));

        if (item == null || item.getNameRu() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getNameRu());

        if (item == null || item.getIdn() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getIdn());

        if (item == null || item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        if (item == null || item.getSearchAllVer() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setBoolean(++num, item.getSearchAllVer());

        if (item == null || item.getRefUnionPersons() == null || item.getRefUnionPersons() == 0)
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
           ocsNumMap.getOcs().setLong(++num, item.getRefUnionPersons());

        ocsNumMap = AbstractReference.setDefaultOcsNumMapForCursor(ocsNumMap, num);

        return ocsNumMap;
    }

    // Insert
    public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefMajorMemberItem item) throws SQLException{
        int num = 0;

        if (item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        } else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

        return ocsNumMap;
    }

    // Update
    public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefMajorMemberItem item) throws SQLException {
        int num = 0;

        ocsNumMap.getOcs().setLong(++num, item.getId());
        ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

        return ocsNumMap;
    }

    // Edit for Insert and Update
    public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefMajorMemberItem item, int num, String mode) throws SQLException{

        ocsNumMap.getOcs().setLong(++num, item.getRefUnionPersons());
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
    public static RefMajorMemberItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefMajorMemberItem item = new RefMajorMemberItem();
        item = (RefMajorMemberItem) AbstractReference.setDefaultItemFromCursor(item, cursor);

        item.setIdn(cursor.getString("IDN"));
        item.setIsNonRezident(cursor.getInt("IS_NON_REZIDENT") > 0);
        item.setRefCountry(cursor.getLong("REF_COUNTRY"));
        item.setRefCountryRecId(cursor.getLong("REF_COUNTRY_REC_ID"));
        item.setCountryName(cursor.getString("COUNTRY_NAME"));
        item.setLegalAddress(cursor.getString("LEGAL_ADDRESS"));

        return item;
    }

    // for History Cursor
    public static RefMajorMemberItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
        RefMajorMemberItem item = setItemFromCursor(cursor);
        item = (RefMajorMemberItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);
        return item;
    }


    public Long getRefUnionPersons() {
        return refUnionPersons;
    }

    public void setRefUnionPersons(Long refUnionPersons) {
        this.refUnionPersons = refUnionPersons;
    }

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    public Boolean getIsNonRezident() {
        return isNonRezident;
    }

    public void setIsNonRezident(Boolean isNonRezident) {
        this.isNonRezident = isNonRezident;
    }

    public Long getRefCountry() {
        return refCountry;
    }

    public void setRefCountry(Long refCountry) {
        this.refCountry = refCountry;
    }

    public Long getRefCountryRecId() {
        return refCountryRecId;
    }

    public void setRefCountryRecId(Long refCountryRecId) {
        this.refCountryRecId = refCountryRecId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getLegalAddress() {
        return legalAddress;
    }

    public void setLegalAddress(String legalAddress) {
        this.legalAddress = legalAddress;
    }
}
