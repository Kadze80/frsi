package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Zhanar.Sanaupova on 18.07.2017.
 */
public class RefBankConglomeratesItem extends AbstractReference {
    public static final String REF_CODE = "ref_bank_conglomerates";

    private String parentCode;
    private String levelCode;
    private Long refUnionPersons;
    private String idn;
    private String share_particip;
    private Long refOrgType;
    private Long refOrgTypeRecId;
    private String orgTypeName;
    private Long refCountry;
    private Long refCountryRecId;
    private String countryName;
    private String legalAddress;
    private Boolean isNonRezident;
    private Long ref_status;
    private String statusName;
    private Long refTypeActivity;
    private String typeActivityName;
    private Long refLegalPerson;
    private String refLPName;


    // prepareCall
  public static final String READ = "PKG_FRSI_REF.REF_READ_BANK_CONGL_LIST (?, ?, ?, ?)";
  public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_BANKCONGL_L_BY_PARAMS(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  public static final String READ_HST = "PKG_FRSI_REF.REF_READ_BANK_CONGL_HST_LIST (?, ?, ?, ?)";
  public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_BANK_CONGL(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_BANK_CONGL(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_BANK_CONGL(?, ?, ?, ?)";

  // read by filter
  public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefBankConglomeratesItem item, Date date) throws SQLException {
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
  public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefBankConglomeratesItem item) throws SQLException{
      int num = 0;

      if (item.getRecId() == null) {
          ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
      } else
          ocsNumMap.getOcs().setLong(++num, item.getRecId());

      ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

      return ocsNumMap;
  }

  // Update
  public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefBankConglomeratesItem item) throws SQLException {
      int num = 0;

      ocsNumMap.getOcs().setLong(++num, item.getId());
      ocsNumMap.getOcs().setLong(++num, item.getRecId());

      ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

      return ocsNumMap;
  }

  // Edit for Insert and Update
  public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefBankConglomeratesItem item, int num, String mode) throws SQLException{
      ocsNumMap.getOcs().setString(++num, item.getCode());
      ocsNumMap.getOcs().setString(++num, item.getParentCode());
      ocsNumMap.getOcs().setString(++num, item.getLevelCode());
      //ocsNumMap.getOcs().setLong(++num, item.getRefUnionPersons());
      if (item.getRefUnionPersons() == null || item.getRefUnionPersons() == 0) {
          ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
      }else
          ocsNumMap.getOcs().setLong(++num, item.getRefUnionPersons());
      //ocsNumMap.getOcs().setLong(++num, item.getRefTypeActivity());
      if (item.getRefTypeActivity() == null || item.getRefTypeActivity() == 0) {
         ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
      }else
        ocsNumMap.getOcs().setLong(++num, item.getRefTypeActivity());
      //ocsNumMap.getOcs().setLong(++num, item.getRef_status());
      if (item.getRef_status() == null || item.getRef_status() == 0) {
          ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
      }else
          ocsNumMap.getOcs().setLong(++num, item.getRef_status());
       if (item.getRefLegalPerson() == null || item.getRefLegalPerson() == 0) {
          ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
      }else
          ocsNumMap.getOcs().setLong(++num, item.getRefLegalPerson());
     // ocsNumMap.getOcs().setLong(++num, item.getRefLegalPerson());
      if (item.getShare_particip() == null || item.getShare_particip().trim().isEmpty()) {
          ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
      }else
          ocsNumMap.getOcs().setString(++num, item.getShare_particip());
      //ocsNumMap.getOcs().setString(++num, item.getShare_particip());
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
  public static RefBankConglomeratesItem setItemFromCursor(ResultSet cursor) throws SQLException{
      RefBankConglomeratesItem item = new RefBankConglomeratesItem();
      item = (RefBankConglomeratesItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
      item.setParentCode(cursor.getString("PARENT_CODE"));
      item.setLevelCode(cursor.getString("LEVEL_CODE"));
      item.setRefUnionPersons(cursor.getLong("REF_UNIONPERSONS"));
      item.setIdn(cursor.getString("IDN"));
      item.setRefCountry(cursor.getLong("REF_COUNTRY"));
      item.setRefCountryRecId(cursor.getLong("REF_COUNTRY_REC_ID"));
      item.setCountryName(cursor.getString("COUNTRY_NAME"));
      item.setLegalAddress(cursor.getString("LEGAL_ADDRESS"));
      item.setIsNonRezident(cursor.getInt("IS_NON_REZIDENT") > 0);
      item.setRefTypeActivity(cursor.getLong("REF_TYPE_ACTIVITY"));
      item.setTypeActivityName(cursor.getString("TYPEACTIVITYNAME"));
      item.setRef_status(cursor.getLong("REF_STATUS"));
      item.setStatusName(cursor.getString("STATUSNAME"));
      item.setRefLegalPerson(cursor.getLong("REF_LEGAL_PERSON"));
      item.setRefLPName(cursor.getString("REFLPNAME"));
      item.setRefOrgType(cursor.getLong("REF_ORG_TYPE"));
      item.setRefOrgTypeRecId(cursor.getLong("REF_ORG_TYPE_REC_ID"));
      item.setOrgTypeName(cursor.getString("ORG_TYPE_NAME"));
      item.setShare_particip(cursor.getString("SHARE_PARTICIP"));

      return item;
  }

  // for History Cursor
  public static RefBankConglomeratesItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
      RefBankConglomeratesItem item = setItemFromCursor(cursor);
      item = (RefBankConglomeratesItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);
      return item;
  }



   // region Getter and Setter
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

    public String getShare_particip() {
        return share_particip;
    }

    public void setShare_particip(String share_particip) {
        this.share_particip = share_particip;
    }

    public Long getRefOrgType() {
        return refOrgType;
    }

    public void setRefOrgType(Long refOrgType) {
        this.refOrgType = refOrgType;
    }

    public Long getRefOrgTypeRecId() {
        return refOrgTypeRecId;
    }

    public void setRefOrgTypeRecId(Long refOrgTypeRecId) {
        this.refOrgTypeRecId = refOrgTypeRecId;
    }

    public String getOrgTypeName() {
        return orgTypeName;
    }

    public void setOrgTypeName(String orgTypeName) {
        this.orgTypeName = orgTypeName;
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

    public Boolean getIsNonRezident() {
        return isNonRezident;
    }

    public void setIsNonRezident(Boolean isNonRezident) {
        this.isNonRezident = isNonRezident;
    }

    public Long getRef_status() {
        return ref_status;
    }

    public void setRef_status(Long ref_status) {
        this.ref_status = ref_status;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public Long getRefTypeActivity() {
        return refTypeActivity;
    }

    public void setRefTypeActivity(Long refTypeActivity) {
        this.refTypeActivity = refTypeActivity;
    }

    public String getTypeActivityName() {
        return typeActivityName;
    }

    public void setTypeActivityName(String typeActivityName) {
        this.typeActivityName = typeActivityName;
    }

    public Long getRefLegalPerson() {
        return refLegalPerson;
    }

    public void setRefLegalPerson(Long refLegalPerson) {
        this.refLegalPerson = refLegalPerson;
    }

    public String getRefLPName() {
        return refLPName;
    }

    public void setRefLPName(String refLPName) {
        this.refLPName = refLPName;
    }
    // endregion
}
