package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 05.05.2015.
 */
public class RefSecuritiesItem extends AbstractReference {
    public static final String REF_CODE = "ref_securities";
    private Long sIssuer;
    private String issuerName;
    private Long sgIssuerSign;
    private String signCode;
    private String signName;
    private Boolean isResident;
    private Boolean isState;
    private Long nominalValue;
    private String nin;
    private Date circulDate;
    private Date maturityDate;
    private Long securityCnt;
    private Long sgSecurityVariety;
    private String varietyCode;
    private String varietyName;
    private Long sgSecurityType;
    private String typeCode;
    private String typeName;
    private Long nominalCurrency;
    private String currencyCode;
    private String currencyName;
    private Long secVarRecId;
    private Long currencyRecId;
    private Long issueVolume;
    private Long circulPeriod;
    private String listingEstimation;
    private String ratingEstimation;
    private Boolean isBondProgram;
    private Long bondProgramVolume;
    private Long bondPrgCnt;
    private Boolean isGarant;
    private String garant;
    private Boolean isPermit;
    private Boolean isFromKase;

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_SECURITIES_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_SEC_LIST_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefSecuritiesItem item, Date date) throws SQLException {
        int num = 0;

        if (item == null || item.getId() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setLong(++num, item.getId());

        if (date == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setDate(++num, new java.sql.Date(date.getTime()));

        if (item == null || item.getIssuerName() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getIssuerName());

        if (item == null || item.getNin() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getNin());

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
    public static RefSecuritiesItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefSecuritiesItem item = new RefSecuritiesItem();
        item = (RefSecuritiesItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        item.setsIssuer(cursor.getLong("S_ISSUER"));
        item.setIssuerName(cursor.getString("ISSUER_NAME"));
        item.setSgIssuerSign(cursor.getLong("S_G_ISSUER_SIGN"));
        item.setSignCode(cursor.getString("SIGN_CODE"));
        item.setSignName(cursor.getString("SIGN_NAME"));
        item.setIsState(cursor.getInt("IS_STATE") > 0);
        item.setIsResident(cursor.getInt("IS_RESIDENT") > 0);
        item.setNominalValue(cursor.getLong("NOMINAL_VALUE"));
        item.setNin(cursor.getString("NIN"));
        item.setCirculDate(cursor.getDate("CIRCUL_DATE"));
        item.setMaturityDate(cursor.getDate("MATURITY_DATE"));
        item.setSecurityCnt(cursor.getLong("SECURITY_CNT"));
        item.setSgSecurityVariety(cursor.getLong("S_G_SECURITY_VARIETY"));
        item.setVarietyCode(cursor.getString("VARIETY_CODE"));
        item.setVarietyName(cursor.getString("VARIETY_NAME"));
        item.setSgSecurityType(cursor.getLong("S_G_SECURITY_TYPE"));
        item.setTypeCode(cursor.getString("TYPE_CODE"));
        item.setTypeName(cursor.getString("TYPE_NAME"));
        item.setNominalCurrency(cursor.getLong("NOMINAL_CURRENCY"));
        item.setCurrencyCode(cursor.getString("CURRENCY_CODE"));
        item.setCurrencyName(cursor.getString("CURRENCY_NAME"));
        item.setSecVarRecId(cursor.getLong("SEC_VAR_REC_ID"));
        item.setCurrencyRecId(cursor.getLong("CURRENCY_REC_ID"));
        item.setIssueVolume(cursor.getLong("ISSUE_VOLUME"));
        item.setCirculPeriod(cursor.getLong("CIRCUL_PERIOD"));
        item.setListingEstimation(cursor.getString("LISTING_ESTIMATION"));
        item.setRatingEstimation(cursor.getString("RATING_ESTIMATION"));
        item.setIsBondProgram(cursor.getInt("IS_BOND_PROGRAM") > 0);
        item.setBondProgramVolume(cursor.getLong("BOND_PROGRAM_VOLUME"));
        item.setBondPrgCnt(cursor.getLong("BOND_PRG_CNT"));
        item.setIsGarant(cursor.getInt("IS_GARANT") > 0);
        item.setGarant(cursor.getString("GARANT"));
        item.setIsPermit(cursor.getInt("IS_PERMIT") > 0);
        item.setIsFromKase(cursor.getInt("IS_FROM_KASE") > 0);
        return item;
    }

    // region Getter and Setter

    public String getNin() {
        return nin;
    }

    public void setNin(String nin) {
        this.nin = nin;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getVarietyCode() {
        return varietyCode;
    }

    public void setVarietyCode(String varietyCode) {
        this.varietyCode = varietyCode;
    }

    public String getVarietyName() {
        return varietyName;
    }

    public void setVarietyName(String varietyName) {
        this.varietyName = varietyName;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public Boolean getIsState() {
        return isState;
    }

    public void setIsState(Boolean isState) {
        this.isState = isState;
    }

    public Boolean getIsResident() {
        return isResident;
    }

    public void setIsResident(Boolean isResident) {
        this.isResident = isResident;
    }

    public String getSignCode() {
        return signCode;
    }

    public void setSignCode(String signCode) {
        this.signCode = signCode;
    }

    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }

    public Date getCirculDate() {
        return circulDate;
    }

    public void setCirculDate(Date circulDate) {
        this.circulDate = circulDate;
    }

    public Date getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(Date maturityDate) {
        this.maturityDate = maturityDate;
    }

    public Long getNominalValue() {
        return nominalValue;
    }

    public void setNominalValue(Long nominalValue) {
        this.nominalValue = nominalValue;
    }

    public Long getsIssuer() {
        return sIssuer;
    }

    public void setsIssuer(Long sIssuer) {
        this.sIssuer = sIssuer;
    }

    public Long getSgIssuerSign() {
        return sgIssuerSign;
    }

    public void setSgIssuerSign(Long sgIssuerSign) {
        this.sgIssuerSign = sgIssuerSign;
    }

    public Long getSecurityCnt() {
        return securityCnt;
    }

    public void setSecurityCnt(Long securityCnt) {
        this.securityCnt = securityCnt;
    }

    public Long getSgSecurityVariety() {
        return sgSecurityVariety;
    }

    public void setSgSecurityVariety(Long sgSecurityVariety) {
        this.sgSecurityVariety = sgSecurityVariety;
    }

    public Long getSgSecurityType() {
        return sgSecurityType;
    }

    public void setSgSecurityType(Long sgSecurityType) {
        this.sgSecurityType = sgSecurityType;
    }

    public Long getNominalCurrency() {
        return nominalCurrency;
    }

    public void setNominalCurrency(Long nominalCurrency) {
        this.nominalCurrency = nominalCurrency;
    }

    public Long getSecVarRecId() {
        return secVarRecId;
    }

    public void setSecVarRecId(Long secVarRecId) {
        this.secVarRecId = secVarRecId;
    }

    public Long getCurrencyRecId() {
        return currencyRecId;
    }

    public void setCurrencyRecId(Long currencyRecId) {
        this.currencyRecId = currencyRecId;
    }

    public Long getIssueVolume() {
        return issueVolume;
    }

    public void setIssueVolume(Long issueVolume) {
        this.issueVolume = issueVolume;
    }

    public Long getCirculPeriod() {
        return circulPeriod;
    }

    public void setCirculPeriod(Long circulPeriod) {
        this.circulPeriod = circulPeriod;
    }

    public String getListingEstimation() {
        return listingEstimation;
    }

    public void setListingEstimation(String listingEstimation) {
        this.listingEstimation = listingEstimation;
    }

    public String getRatingEstimation() {
        return ratingEstimation;
    }

    public void setRatingEstimation(String ratingEstimation) {
        this.ratingEstimation = ratingEstimation;
    }

    public Boolean getIsBondProgram() {
        return isBondProgram;
    }

    public void setIsBondProgram(Boolean isBondProgram) {
        this.isBondProgram = isBondProgram;
    }

    public Long getBondProgramVolume() {
        return bondProgramVolume;
    }

    public void setBondProgramVolume(Long bondProgramVolume) {
        this.bondProgramVolume = bondProgramVolume;
    }

    public Long getBondPrgCnt() {
        return bondPrgCnt;
    }

    public void setBondPrgCnt(Long bondPrgCnt) {
        this.bondPrgCnt = bondPrgCnt;
    }

    public Boolean getIsGarant() {
        return isGarant;
    }

    public void setIsGarant(Boolean isGarant) {
        this.isGarant = isGarant;
    }

    public String getGarant() {
        return garant;
    }

    public void setGarant(String garant) {
        this.garant = garant;
    }

    public Boolean getIsPermit() {
        return isPermit;
    }

    public void setIsPermit(Boolean isPermit) {
        this.isPermit = isPermit;
    }

    public Boolean getIsFromKase() {
        return isFromKase;
    }

    public void setIsFromKase(Boolean isFromKase) {
        this.isFromKase = isFromKase;
    }

    // endregion
}
