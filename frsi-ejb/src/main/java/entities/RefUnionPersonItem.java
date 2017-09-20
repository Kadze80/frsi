package entities;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RefUnionPersonItem extends AbstractReference {
    public static final String REF_CODE = "ref_unionpersons";

    private String idn;
    private Integer type;
    private String typeName;
    private boolean isTax;
    private String orgTypeName;
    private Boolean isNonRezident;
    private Long refCountry;
    private Long refCountryRecId;
    private String countryName;
    private String legalAddress;


    // for Cursor
    public static RefUnionPersonItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefUnionPersonItem item = new RefUnionPersonItem();
        item.setId(cursor.getLong("ID"));
        item.setRecId(cursor.getLong("REC_ID"));
        item.setNameRu(cursor.getString("NAME_RU"));
        item.setBeginDate(cursor.getDate("BEGIN_DATE"));
        item.setEndDate(cursor.getDate("END_DATE"));
        item.setIdn(cursor.getString("IDN"));
        item.setType(cursor.getInt("TYPE"));

        return item;
    }

    // region Getter and Setter
    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public boolean isTax() {
        return isTax;
    }

    public void setTax(boolean tax) {
        isTax = tax;
    }

    public String getOrgTypeName() {
        return orgTypeName;
    }

    public void setOrgTypeName(String orgTypeName) {
        this.orgTypeName = orgTypeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
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

    // endregion
}
