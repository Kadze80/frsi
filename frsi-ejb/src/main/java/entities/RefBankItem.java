package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 04.05.2015.
 */
public class RefBankItem extends AbstractReference {
    public static final String REF_CODE = "ref_bank";
    private String bic;
    private String bicHead;
    private String bicNbrk;
    private String idn;
    private String postAddress;
    private String phoneNum;
    private Boolean isLoad;
    private Boolean isNonRezident;
    private Long refCountryId;
    private Long refCountryRecId;
    private String refCountryName;

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_BANK_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_BANK_LIST_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "PKG_FRSI_REF.REF_READ_BANK_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_BANK (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_BANK (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_BANK (?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefBankItem item, Date date) throws SQLException {
        int num = 0;

        if (item == null || item.getId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setLong(++num, item.getId());

        if (date == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setDate(++num, new java.sql.Date(date.getTime()));

        if (item  == null || item .getIdn() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item .getIdn());

        if (item == null || item.getNameRu() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setString(++num, item.getNameRu());

        if (item == null || item.getRecId() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        if (item  == null || item .getIsLoad() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setBoolean(++num, item .getIsLoad());

        if (item  == null || item .getIsNonRezident() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setBoolean(++num, item .getIsNonRezident());

        if (item == null || item.getSearchAllVer() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setBoolean(++num, item.getSearchAllVer());

        ocsNumMap = AbstractReference.setDefaultOcsNumMapForCursor(ocsNumMap, num);

        return ocsNumMap;
    }

    // Insert
    public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefBankItem item) throws SQLException{
        int num = 0;

        if (item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        } else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

        return ocsNumMap;
    }

    // Update
    public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefBankItem item) throws SQLException {
        int num = 0;

        ocsNumMap.getOcs().setLong(++num, item.getId());
        ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

        return ocsNumMap;
    }

    // Edit for Insert and Update
    public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefBankItem item, int num, String mode) throws SQLException{
        ocsNumMap.getOcs().setString(++num, item.getCode());
        ocsNumMap.getOcs().setString(++num, item.getNameKz());
        ocsNumMap.getOcs().setString(++num, item.getNameRu());
        ocsNumMap.getOcs().setString(++num, item.getNameEn());
        ocsNumMap.getOcs().setString(++num, item.getIdn());
        ocsNumMap.getOcs().setString(++num, item.getPostAddress());
        ocsNumMap.getOcs().setString(++num, item.getPhoneNum());
        ocsNumMap.getOcs().setLong(++num, item.getRefCountryId());
        ocsNumMap.getOcs().setBoolean(++num, item.getIsNonRezident());
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
    public static RefBankItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefBankItem item = new RefBankItem();
        item = (RefBankItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        item.setBic(cursor.getString("BIC"));
        item.setBicHead(cursor.getString("BIC_HEAD"));
        item.setBicNbrk(cursor.getString("BIC_NBRK"));
        item.setIdn(cursor.getString("IDN"));
        item.setPostAddress(cursor.getString("POST_ADDRESS"));
        item.setPhoneNum(cursor.getString("PHONE_NUM"));
        item.setIsLoad(cursor.getInt("IS_LOAD") > 0);
        item.setIsNonRezident(cursor.getInt("IS_NON_REZIDENT") > 0);
        item.setRefCountryId(cursor.getLong("REF_COUNTRY"));
        item.setRefCountryRecId(cursor.getLong("COUNTRY_REC_ID"));
        item.setRefCountryName(cursor.getString("COUNTRY_NAME"));
        return item;
    }

    // for History Cursor
    public static RefBankItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
        RefBankItem item = setItemFromCursor(cursor);
        item = (RefBankItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);
        return item;
    }


    // region Getter and Setter
    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getBicHead() {
        return bicHead;
    }

    public void setBicHead(String bicHead) {
        this.bicHead = bicHead;
    }

    public String getBicNbrk() {
        return bicNbrk;
    }

    public void setBicNbrk(String bicNbrk) {
        this.bicNbrk = bicNbrk;
    }

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    public String getPostAddress() {
        return postAddress;
    }

    public void setPostAddress(String postAddress) {
        this.postAddress = postAddress;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public Boolean getIsLoad() {
        return isLoad;
    }

    public void setIsLoad(Boolean isLoad) {
        this.isLoad = isLoad;
    }

    public Boolean getIsNonRezident() {
        return isNonRezident;
    }

    public void setIsNonRezident(Boolean isNonRezident) {
        this.isNonRezident = isNonRezident;
    }

    public Long getRefCountryId() {
        return refCountryId;
    }

    public void setRefCountryId(Long refCountryId) {
        this.refCountryId = refCountryId;
    }

    public Long getRefCountryRecId() {
        return refCountryRecId;
    }

    public void setRefCountryRecId(Long refCountryRecId) {
        this.refCountryRecId = refCountryRecId;
    }

    public String getRefCountryName() {
        return refCountryName;
    }

    public void setRefCountryName(String refCountryName) {
        this.refCountryName = refCountryName;
    }

    // endregion
}
