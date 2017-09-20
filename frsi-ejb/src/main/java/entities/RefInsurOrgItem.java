package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Zhanar.Sanaupova on 07.06.2017.
 */
public class RefInsurOrgItem extends AbstractReference {
       public static final String REF_CODE = "ref_insur_org";

       private String idn;
       private Long refCountry;
       private Long refCountryRecId;
       private String countryName;
       private String legalAddress;
       private String factAddress;
       private Boolean isNonRezident;
       private Boolean isInvFund;
       private String invIdn;
       private Boolean isLoad;
       private String code_insur;
       private Long ref_branch_insur;
       private Long ref_ownership_insur;

    public static final String READ = "PKG_FRSI_REF.REF_READ_INSURORG_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_INSURORG_L_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?)";


    // read by filter
       public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefInsurOrgItem item, Date date) throws SQLException {
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

           if (item  == null || item .getIdn() == null) {
               ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
           }else
               ocsNumMap.getOcs().setString(++num, item .getIdn());

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

    // for Cursor
    public static RefInsurOrgItem setItemFromCursor(ResultSet cursor) throws SQLException{
         RefInsurOrgItem item = new RefInsurOrgItem();
         item = (RefInsurOrgItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
         item.setShortNameKz(cursor.getString("SHORT_NAME_KZ"));
         item.setShortNameRu(cursor.getString("SHORT_NAME_RU"));
         item.setShortNameEn(cursor.getString("SHORT_NAME_EN"));
         item.setIdn(cursor.getString("IDN"));
         item.setIsNonRezident(cursor.getInt("IS_NON_REZIDENT") > 0);
         item.setIsInvFund(cursor.getInt("IS_INV_FUND") > 0);
         item.setInvIdn(cursor.getString("INV_IDN"));
         item.setRefCountry(cursor.getLong("REF_COUNTRY"));
         item.setRefCountryRecId(cursor.getLong("REF_COUNTRY_REC_ID"));
         item.setCountryName(cursor.getString("COUNTRY_NAME"));
         item.setIsLoad(cursor.getInt("IS_LOAD") > 0);
         item.setCode_insur(cursor.getString("CODE_INSUR"));                 //поле спр-ка страх.орг
         item.setRef_branch_insur(cursor.getLong("REF_BRANCH_INSUR"));       //поле спр-ка страх.орг
         item.setRef_ownership_insur(cursor.getLong("REF_OWNERSHIP_INSUR")); //поле спр-ка страх.орг
         item.setLegalAddress(cursor.getString("LEGAL_ADDRESS"));
         item.setFactAddress(cursor.getString("FACT_ADDRESS"));

         return item;
}

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
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

    public Boolean getIsInvFund() {
        return isInvFund;
    }

    public void setIsInvFund(Boolean isInvFund) {
        this.isInvFund = isInvFund;
    }

    public String getInvIdn() {
        return invIdn;
    }

    public void setInvIdn(String invIdn) {
        this.invIdn = invIdn;
    }

    public Boolean getIsLoad() {
        return isLoad;
    }

    public void setIsLoad(Boolean isLoad) {
        this.isLoad = isLoad;
    }

    public String getCode_insur() {
        return code_insur;
    }

    public void setCode_insur(String code_insur) {
        this.code_insur = code_insur;
    }

    public Long getRef_branch_insur() {
        return ref_branch_insur;
    }

    public void setRef_branch_insur(Long ref_branch_insur) {
        this.ref_branch_insur = ref_branch_insur;
    }

    public Long getRef_ownership_insur() {
        return ref_ownership_insur;
    }

    public void setRef_ownership_insur(Long ref_ownership_insur) {
        this.ref_ownership_insur = ref_ownership_insur;
    }
}
