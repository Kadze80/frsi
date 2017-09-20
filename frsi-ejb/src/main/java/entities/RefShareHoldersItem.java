package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Zhanar.Sanaupova on 20.07.2017.
 */
public class RefShareHoldersItem extends AbstractReference {
    public static final String REF_CODE = "ref_share_holders";

    private String parentCode;
    private String levelCode;
    private Long   refIssuers;
    private Long refUnionPersons;
    private String refUPName;
    private String idn;
    private String type_holders;
    private String share_value;
    private Long refCountry;
    private Long refCountryRecId;
    private String countryName;
    private String note;

    // prepareCall
   public static final String READ = "PKG_FRSI_REF.REF_READ_SHARE_HOLDERS_LIST (?, ?, ?, ?)";
   public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_SHARE_HOLD_BY_PARAMS(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
   public static final String READ_HST = "PKG_FRSI_REF.REF_READ_SHARE_HOLD_HST_LIST (?, ?, ?, ?)";
   public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_SHARE_HOLD(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
   public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_SHARE_HOLD(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
   public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_SHARE_HOLD(?, ?, ?, ?)";

     // read by filter
   public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefShareHoldersItem item, Date date) throws SQLException {
     int num = 0;

     if (item == null || item.getId() == null) {
         ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
     }else
         ocsNumMap.getOcs().setLong(++num, item.getId());

     if (date == null) {
         ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
     }else
         ocsNumMap.getOcs().setDate(++num, new java.sql.Date(date.getTime()));

     if (item == null || item.getRefUnionPersons() == null)
         ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
     else
         ocsNumMap.getOcs().setLong(++num, item.getRefUnionPersons());

     if (item == null || item.getCode() == null) {
         ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
     }else
         ocsNumMap.getOcs().setString(++num, item.getCode());

     if (item == null || item.getParentCode() == null) {
         ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
     }else
         ocsNumMap.getOcs().setString(++num, item.getParentCode());

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
 public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefShareHoldersItem item) throws SQLException{
     int num = 0;

     if (item.getRecId() == null) {
         ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
     } else
         ocsNumMap.getOcs().setLong(++num, item.getRecId());

     ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

     return ocsNumMap;
 }

 // Update
 public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefShareHoldersItem item) throws SQLException {
     int num = 0;

     ocsNumMap.getOcs().setLong(++num, item.getId());
     ocsNumMap.getOcs().setLong(++num, item.getRecId());

     ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

     return ocsNumMap;
 }

 // Edit for Insert and Update
 public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefShareHoldersItem item, int num, String mode) throws SQLException{
     ocsNumMap.getOcs().setString(++num, item.getCode());
     ocsNumMap.getOcs().setString(++num, item.getParentCode());
     ocsNumMap.getOcs().setString(++num, item.getLevelCode());
     ocsNumMap.getOcs().setLong(++num, item.getRefIssuers());
     if (item.getRefUnionPersons() == null || item.getRefUnionPersons() == 0) {
         ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
     }else
         ocsNumMap.getOcs().setLong(++num, item.getRefUnionPersons());
     ocsNumMap.getOcs().setString(++num, item.getType_holders());
     ocsNumMap.getOcs().setLong(++num, item.getRefCountry());
     ocsNumMap.getOcs().setString(++num, item.getShare_value());
     ocsNumMap.getOcs().setString(++num, item.getNote());
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
 public static RefShareHoldersItem setItemFromCursor(ResultSet cursor) throws SQLException{
     RefShareHoldersItem item = new RefShareHoldersItem();
     item = (RefShareHoldersItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
     item.setParentCode(cursor.getString("PARENT_CODE"));
     item.setLevelCode(cursor.getString("LEVEL_CODE"));
     item.setRefIssuers(cursor.getLong("REF_ISSUERS"));
     item.setRefUnionPersons(cursor.getLong("REF_UNIONPERSONS"));
     item.setRefUPName(cursor.getString("REFUPNAME"));
     item.setIdn(cursor.getString("IDN"));
     item.setType_holders(cursor.getString("TYPE_HOLDERS"));
     item.setRefCountry(cursor.getLong("REF_COUNTRY"));
     item.setRefCountryRecId(cursor.getLong("REF_COUNTRY_REC_ID"));
     item.setCountryName(cursor.getString("COUNTRY_NAME"));
     item.setShare_value(cursor.getString("SHARE_VALUE"));
     item.setNote(cursor.getString("NOTE"));

     return item;
 }

 // for History Cursor
 public static RefShareHoldersItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
     RefShareHoldersItem item = setItemFromCursor(cursor);
     item = (RefShareHoldersItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);
     return item;
 }


    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public String getLevelCode() {
        return levelCode;
    }

    public void setLevelCode(String levelCode) {
        this.levelCode = levelCode;
    }

    public Long getRefIssuers() {
        return refIssuers;
    }

    public void setRefIssuers(Long refIssuers) {
        this.refIssuers = refIssuers;
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

    public String getType_holders() {
        return type_holders;
    }

    public void setType_holders(String type_holders) {
        this.type_holders = type_holders;
    }

    public String getShare_value() {
        return share_value;
    }

    public void setShare_value(String share_value) {
        this.share_value = share_value;
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

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public void setNote(String note) {
        this.note = note;
    }

    public String getRefUPName() {
        return refUPName;
    }

    public void setRefUPName(String refUPName) {
        this.refUPName = refUPName;
    }
}
