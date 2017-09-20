package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;




public class RefInsurGroupsItem extends AbstractReference {
    public static final String REF_CODE = "ref_insur_groups";

    private String parentCode;
    private String levelCode;
    private Long refLegalPerson;
    private String idn;
    private String share_particip;
    private Long refOrgType;
    private Long refOrgTypeRecId;
    private String orgTypeName;
    private Long refCountry;
    private Long refCountryRecId;
    private String countryName;
    private String legalAddress;
    private String factAddress;
    private Boolean isNonRezident;
    private Long ref_branch_insur;
    private String branch_name;

    // prepareCall
   public static final String READ = "PKG_FRSI_REF.REF_READ_INSUR_GROUPS_LIST (?, ?, ?, ?)";
   public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_INSUR_GR_L_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
   public static final String READ_HST = "PKG_FRSI_REF.REF_READ_INSUR_GROUPS_HST_LIST (?, ?, ?, ?)";
   public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_INSUR_GROUPS(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
   public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_INSUR_GROUPS(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
   public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_INSUR_GROUPS(?, ?, ?, ?)";

   // read by filter
   public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefInsurGroupsItem item, Date date) throws SQLException {
       int num = 0;

       if (item == null || item.getId() == null) {
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       }else
           ocsNumMap.getOcs().setLong(++num, item.getId());

       if (date == null) {
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       }else
           ocsNumMap.getOcs().setDate(++num, new java.sql.Date(date.getTime()));

       if (item == null || item.getRefLegalPerson() == null)
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       else
           ocsNumMap.getOcs().setLong(++num, item.getRefLegalPerson());

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
   public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefInsurGroupsItem item) throws SQLException{
       int num = 0;

       if (item.getRecId() == null) {
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       } else
           ocsNumMap.getOcs().setLong(++num, item.getRecId());

       ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

       return ocsNumMap;
   }

   // Update
   public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefInsurGroupsItem item) throws SQLException {
       int num = 0;

       ocsNumMap.getOcs().setLong(++num, item.getId());
       ocsNumMap.getOcs().setLong(++num, item.getRecId());

       ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

       return ocsNumMap;
   }

   // Edit for Insert and Update
   public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefInsurGroupsItem item, int num, String mode) throws SQLException{
       ocsNumMap.getOcs().setString(++num, item.getCode());
       ocsNumMap.getOcs().setString(++num, item.getParentCode());
       ocsNumMap.getOcs().setString(++num, item.getLevelCode());
       ocsNumMap.getOcs().setLong(++num, item.getRefLegalPerson());
       ocsNumMap.getOcs().setString(++num, item.getShare_particip());
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
   public static RefInsurGroupsItem setItemFromCursor(ResultSet cursor) throws SQLException{
       RefInsurGroupsItem item = new RefInsurGroupsItem();
       item = (RefInsurGroupsItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
       item.setParentCode(cursor.getString("PARENT_CODE"));
       item.setLevelCode(cursor.getString("LEVEL_CODE"));
       item.setRefLegalPerson(cursor.getLong("REFLEGALPERSON"));
       item.setIdn(cursor.getString("IDN"));
       item.setRefOrgType(cursor.getLong("REF_ORG_TYPE"));
       item.setRefOrgTypeRecId(cursor.getLong("REF_ORG_TYPE_REC_ID"));
       item.setOrgTypeName(cursor.getString("ORG_TYPE_NAME"));
       item.setRefCountry(cursor.getLong("REF_COUNTRY"));
       item.setRefCountryRecId(cursor.getLong("REF_COUNTRY_REC_ID"));
       item.setCountryName(cursor.getString("COUNTRY_NAME"));
       item.setLegalAddress(cursor.getString("LEGAL_ADDRESS"));
       item.setFactAddress(cursor.getString("FACT_ADDRESS"));
       item.setIsNonRezident(cursor.getInt("IS_NON_REZIDENT") > 0);
       item.setRef_branch_insur(cursor.getLong("REF_BRANCH_INSUR"));
       item.setBranch_name(cursor.getString("BRANCH_NAME"));
       item.setShare_particip(cursor.getString("SHARE_PARTICIP"));

       return item;
   }

   // for History Cursor
   public static RefInsurGroupsItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
       RefInsurGroupsItem item = setItemFromCursor(cursor);
       item = (RefInsurGroupsItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);
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

    public Long getRefLegalPerson() {
        return refLegalPerson;
    }

    public void setRefLegalPerson(Long refLegalPerson) {
        this.refLegalPerson = refLegalPerson;
    }

    public String getShare_particip() {
        return share_particip;
    }

    public void setShare_particip(String share_particip) {
        this.share_particip = share_particip;
    }

    public String getBranch_name() {
        return branch_name;
    }

    public void setBranch_name(String branch_name) {
        this.branch_name = branch_name;
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

    public String getFactAddress() {
        return factAddress;
    }

    public void setFactAddress(String factAddress) {
        this.factAddress = factAddress;
    }

    public Boolean getIsNonRezident() {
        return isNonRezident;
    }

    public void setIsNonRezident(Boolean isNonRezident) {
        this.isNonRezident = isNonRezident;
    }

    public Long getRef_branch_insur() {
        return ref_branch_insur;
    }

    public void setRef_branch_insur(Long ref_branch_insur) {
        this.ref_branch_insur = ref_branch_insur;
    }

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    //endregion

}