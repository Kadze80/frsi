package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 29.04.2015.
 */
public class RefLegalPersonItem extends AbstractReference {
    public static final String REF_CODE = "ref_legal_person";

    private String idn;
    private Long refOrgType;
    private Long refOrgTypeRecId;
    private String orgTypeName;
    private Long refTypeBusEntity;
    private Long refTypeBusEntityRecId;
    private String typeBeName;
    private Long refCountry;
    private Long refCountryRecId;
    private String countryName;
    private Long refRegion;
    private Long refRegionRecId;
    private String regionName;
    private String postalIndex;
    private String addressStreet;
    private String addressNumHouse;
    /*private Long refManagers;
    private Long refManagersRecId;
    private String managersName;*/
    private String manager;
    private String legalAddress;
    private String factAddress;
    private Boolean isNonRezident;
    private Boolean isInvFund;
    private String invIdn;
    private Boolean isAkimat;
    private String bic_bn;
    private String bicHead_bn;
    private String bicNbrk_bn;
    private String postAddress_bn;
    private String phoneNum_bn;
    private Boolean isLoad;
    private Boolean userConfirm;
    private Boolean currentRec;
    private String codeInsur;
    private Long ref_branch_insur;
    private Long ref_ownership_insur;
    private String vat;
    private boolean isTax;


    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_LEGAL_PERSON_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_LP_LIST_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "PKG_FRSI_REF.REF_READ_LEGAL_PERSON_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_LEGAL_PERSON (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_LEGAL_PERSON (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_LEGAL_PERSON (?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefLegalPersonItem item, Date date) throws SQLException {
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

        if (item == null || item.getRefOrgType() == null || item.getRefOrgType() == 0)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setLong(++num, item.getRefOrgType());

        if (item == null || item.getUserConfirm() == null)
          ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
          ocsNumMap.getOcs().setBoolean(++num, item.getUserConfirm());

        if (item == null || item.getCurrentRec() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setBoolean(++num, item.getCurrentRec());

        if (item == null || item.getIsLoad() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setBoolean(++num, item.getIsLoad());

        if (item == null || item.getIsNonRezident() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setBoolean(++num, item.getIsNonRezident());


        ocsNumMap = AbstractReference.setDefaultOcsNumMapForCursor(ocsNumMap, num);

        return ocsNumMap;
    }

    // Insert
    public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefLegalPersonItem item) throws SQLException{
        int num = 0;

        if (item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        } else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

        return ocsNumMap;
    }

    // Update
    public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefLegalPersonItem item) throws SQLException {
        int num = 0;

        ocsNumMap.getOcs().setLong(++num, item.getId());
        ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

        return ocsNumMap;
    }

    // Edit for Insert and Update
    public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefLegalPersonItem item, int num, String mode) throws SQLException{
        ocsNumMap.getOcs().setString(++num, item.getCode());
        ocsNumMap.getOcs().setString(++num, item.getNameKz());
        ocsNumMap.getOcs().setString(++num, item.getNameRu());
        ocsNumMap.getOcs().setString(++num, item.getNameEn());
        ocsNumMap.getOcs().setString(++num, item.getShortNameKz());
        ocsNumMap.getOcs().setString(++num, item.getShortNameRu());
        ocsNumMap.getOcs().setString(++num, item.getShortNameEn());
        ocsNumMap.getOcs().setBoolean(++num, item.getIsNonRezident());
        ocsNumMap.getOcs().setString(++num, item.getIdn());
        ocsNumMap.getOcs().setLong(++num, item.getRefOrgType());
        ocsNumMap.getOcs().setLong(++num, item.getRefTypeBusEntity());
        ocsNumMap.getOcs().setLong(++num, item.getRefCountry());
        if (item.getRefRegion() == null || item.getRefRegion() == 0) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setLong(++num, item.getRefRegion());
        ocsNumMap.getOcs().setString(++num, item.getPostalIndex());
        ocsNumMap.getOcs().setString(++num, item.getAddressStreet());
        ocsNumMap.getOcs().setString(++num, item.getAddressNumHouse());
        ocsNumMap.getOcs().setString(++num, item.getManager());
        ocsNumMap.getOcs().setString(++num, item.getLegalAddress());
        ocsNumMap.getOcs().setString(++num, item.getFactAddress());
        ocsNumMap.getOcs().setString(++num, item.getPostAddress_bn());     //поле спр-ка банков
        ocsNumMap.getOcs().setString(++num, item.getPhoneNum_bn());        //поле спр-ка банков
       // ocsNumMap.getOcs().setString(++num, item.getBic_bn());           //поле спр-ка банков
       // ocsNumMap.getOcs().setString(++num, item.getBicHead_bn());       //поле спр-ка банков
       // ocsNumMap.getOcs().setString(++num, item.getBicNbrk_bn());       //поле спр-ка банков
        ocsNumMap.getOcs().setString(++num, item.getNote());
        ocsNumMap.getOcs().setDate(++num, new java.sql.Date(item.getBeginDate().getTime()));
        ocsNumMap.getOcs().setDate(++num, item.getEndDate() == null ? null : new java.sql.Date(item.getEndDate().getTime()));
        ocsNumMap.getOcs().setBoolean(++num, item.getIsInvFund());
        ocsNumMap.getOcs().setString(++num, item.getInvIdn());
        if (item.getIsAkimat() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        } else
            ocsNumMap.getOcs().setBoolean(++num, item.getIsAkimat());
      //  ocsNumMap.getOcs().setBoolean(++num, item.getIsAkimat());
      //  ocsNumMap.getOcs().setBoolean(++num, item.getIsLoad());         //поле спр-ка банков
        if (item.getCodeInsur() == null || item.getCodeInsur().trim().isEmpty()) { //поле справочника страх.орг
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getCodeInsur());
        if (item.getRef_branch_insur() == null || item.getRef_branch_insur() == 0) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setLong(++num, item.getRef_branch_insur());
        if (item.getRef_ownership_insur() == null || item.getRef_ownership_insur() == 0) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setLong(++num, item.getRef_ownership_insur());
        //ocsNumMap.getOcs().setString(++num, item.getCodeInsur());
       // ocsNumMap.getOcs().setLong(++num, item.getRef_branch_insur());    //поле справочника страх.орг
       // ocsNumMap.getOcs().setLong(++num, item.getRef_ownership_insur()); //поле справочника страх.орг
        if (item.getVat() == null || item.getVat().trim().isEmpty()) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getVat());
        //ocsNumMap.getOcs().setString(++num, item.getVat());
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
    public static RefLegalPersonItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefLegalPersonItem item = new RefLegalPersonItem();
        item = (RefLegalPersonItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        item.setShortNameKz(cursor.getString("SHORT_NAME_KZ"));
        item.setShortNameRu(cursor.getString("SHORT_NAME_RU"));
        item.setShortNameEn(cursor.getString("SHORT_NAME_EN"));
        item.setIdn(cursor.getString("IDN"));
        item.setBic_bn(cursor.getString("BIC_BN"));            //поле спр-ка банков
        item.setBicHead_bn(cursor.getString("BIC_HEAD_BN"));    //поле спр-ка банков
        item.setBicNbrk_bn(cursor.getString("BIC_NBRK_BN"));    //поле спр-ка банков
        item.setIsNonRezident(cursor.getInt("IS_NON_REZIDENT") > 0);
        item.setIsInvFund(cursor.getInt("IS_INV_FUND") > 0);
        item.setInvIdn(cursor.getString("INV_IDN"));
        item.setIsAkimat(cursor.getInt("IS_AKIMAT") > 0);
        item.setRefOrgType(cursor.getLong("REF_ORG_TYPE"));
        item.setRefOrgTypeRecId(cursor.getLong("REF_ORG_TYPE_REC_ID"));
        item.setOrgTypeName(cursor.getString("ORG_TYPE_NAME"));
        item.setRefTypeBusEntity(cursor.getLong("REF_TYPE_BUS_ENTITY"));
        item.setRefTypeBusEntityRecId(cursor.getLong("REF_TYPE_BUS_ENTITY_REC_ID"));
        item.setTypeBeName(cursor.getString("TYPE_BE_NAME"));
        item.setRefCountry(cursor.getLong("REF_COUNTRY"));
        item.setRefCountryRecId(cursor.getLong("REF_COUNTRY_REC_ID"));
        item.setCountryName(cursor.getString("COUNTRY_NAME"));
        item.setRefRegion(cursor.getLong("REF_REGION"));
        item.setRefRegionRecId(cursor.getLong("REF_REGION_REC_ID"));
        item.setRegionName(cursor.getString("REGION_NAME"));
        item.setPostalIndex(cursor.getString("POSTAL_INDEX"));
        item.setAddressStreet(cursor.getString("ADDRESS_STREET"));
        item.setAddressNumHouse(cursor.getString("ADDRESS_NUM_HOUSE"));
        item.setPostAddress_bn(cursor.getString("POST_ADDRESS_BN"));        //поле спр-ка банков
        item.setPhoneNum_bn(cursor.getString("PHONE_NUM_BN"));              //поле спр-ка банков
        item.setIsLoad(cursor.getInt("IS_LOAD") > 0);                       //поле спр-ка банков
        item.setCodeInsur(cursor.getString("CODE_INSUR"));                 //поле спр-ка страх.орг
        item.setRef_branch_insur(cursor.getLong("REF_BRANCH_INSUR"));       //поле спр-ка страх.орг
        item.setRef_ownership_insur(cursor.getLong("REF_OWNERSHIP_INSUR")); //поле спр-ка страх.орг
        item.setManager(cursor.getString("MANAGER"));
        item.setLegalAddress(cursor.getString("LEGAL_ADDRESS"));
        item.setFactAddress(cursor.getString("FACT_ADDRESS"));
        item.setNote(cursor.getString("NOTE"));
        item.setUserConfirm(cursor.getInt("USER_CONFIRM") > 0);
        item.setVat(cursor.getString("VAT"));

        return item;
    }

    // for History Cursor
    public static RefLegalPersonItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
        RefLegalPersonItem item = setItemFromCursor(cursor);
        item = (RefLegalPersonItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);
        return item;
    }


    // region Getter and Setter
    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    public Long getRefOrgType() {
        return refOrgType;
    }

    public void setRefOrgType(Long refOrgType) {
        this.refOrgType = refOrgType;
    }

    public String getOrgTypeName() {
        return orgTypeName;
    }

    public void setOrgTypeName(String orgTypeName) {
        this.orgTypeName = orgTypeName;
    }

    public Long getRefTypeBusEntity() {
        return refTypeBusEntity;
    }

    public void setRefTypeBusEntity(Long refTypeBusEntity) {
        this.refTypeBusEntity = refTypeBusEntity;
    }

    public String getTypeBeName() {
        return typeBeName;
    }

    public void setTypeBeName(String typeBeName) {
        this.typeBeName = typeBeName;
    }

    public Long getRefCountry() {
        return refCountry;
    }

    public void setRefCountry(Long refCountry) {
        this.refCountry = refCountry;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public Long getRefRegion() {
        return refRegion;
    }

    public void setRefRegion(Long refRegion) {
        this.refRegion = refRegion;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getPostalIndex() {
        return postalIndex;
    }

    public void setPostalIndex(String postalIndex) {
        this.postalIndex = postalIndex;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public String getAddressNumHouse() {
        return addressNumHouse;
    }

    public void setAddressNumHouse(String addressNumHouse) {
        this.addressNumHouse = addressNumHouse;
    }

/*    public Long getRefManagers() {
        return refManagers;
    }

    public void setRefManagers(Long refManagers) {
        this.refManagers = refManagers;
    }

    public String getManagersName() {
        return managersName;
    }

    public void setManagersName(String managersName) {
        this.managersName = managersName;
    }*/

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

    public Long getRefOrgTypeRecId() {
        return refOrgTypeRecId;
    }

    public void setRefOrgTypeRecId(Long refOrgTypeRecId) {
        this.refOrgTypeRecId = refOrgTypeRecId;
    }

    public Long getRefTypeBusEntityRecId() {
        return refTypeBusEntityRecId;
    }

    public void setRefTypeBusEntityRecId(Long refTypeBusEntityRecId) {
        this.refTypeBusEntityRecId = refTypeBusEntityRecId;
    }

    public Long getRefCountryRecId() {
        return refCountryRecId;
    }

    public void setRefCountryRecId(Long refCountryRecId) {
        this.refCountryRecId = refCountryRecId;
    }

    public Long getRefRegionRecId() {
        return refRegionRecId;
    }

    public void setRefRegionRecId(Long refRegionRecId) {
        this.refRegionRecId = refRegionRecId;
    }

/*    public Long getRefManagersRecId() {
        return refManagersRecId;
    }

    public void setRefManagersRecId(Long refManagersRecId) {
        this.refManagersRecId = refManagersRecId;
    }*/

    public Boolean getIsNonRezident() {
        return isNonRezident;
    }

    public void setIsNonRezident(Boolean isNonRezident) {
        this.isNonRezident = isNonRezident;
    }

    public Boolean getIsAkimat() {
        return isAkimat;
    }

    public void setIsAkimat(Boolean isAkimat) {
        this.isAkimat = isAkimat;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
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

    public String getBic_bn() {
        return bic_bn;
    }

    public void setBic_bn(String bic_bn) {
        this.bic_bn = bic_bn;
    }

    public String getBicHead_bn() {
        return bicHead_bn;
    }

    public void setBicHead_bn(String bicHead_bn) {
        this.bicHead_bn = bicHead_bn;
    }

    public String getBicNbrk_bn() {
        return bicNbrk_bn;
    }

    public void setBicNbrk_bn(String bicNbrk_bn) {
        this.bicNbrk_bn = bicNbrk_bn;
    }

    public String getPostAddress_bn() {
        return postAddress_bn;
    }

    public void setPostAddress_bn(String postAddress_bn) {
        this.postAddress_bn = postAddress_bn;
    }

    public String getPhoneNum_bn() {
        return phoneNum_bn;
    }

    public void setPhoneNum_bn(String phoneNum_bn) {
        this.phoneNum_bn = phoneNum_bn;
    }

    public String getCodeInsur() {
        return codeInsur;
    }

    public void setCodeInsur(String codeInsur) {
        this.codeInsur = codeInsur;
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

    public Boolean getUserConfirm() {
        return userConfirm;
    }

    public void setUserConfirm(Boolean userConfirm) {
        this.userConfirm = userConfirm;
    }

    public Boolean getCurrentRec() {
        return currentRec;
    }

    public void setCurrentRec(Boolean currentRec) {
        this.currentRec = currentRec;
    }

    public String getVat() {
        return vat;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }

    public boolean isTax() {
        return isTax;
    }

    public void setTax(boolean tax) {
        isTax = tax;
    }


    // endregion
}
