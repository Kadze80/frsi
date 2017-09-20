package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 04.05.2015.
 */
public class RefCurrencyRateItem extends AbstractReference {
    public static final String REF_CODE = "ref_currency_rate";
    private Long refCurrency;
    private String rateAgency;
    private Long refRateAgency;
    private Long refRateAgencyRecId;

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_CURRENCY_RATE_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_CURR_RAT_L_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefCurrencyRateItem item, Date date) throws SQLException {
        int num = 0;

        if (item == null || item.getId() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setLong(++num, item.getId());

        if (date == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setDate(++num, new java.sql.Date(date.getTime()));

        if (item == null || item.getRateAgency() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getRateAgency());

        if (item == null || item.getNameRu() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setString(++num, item.getNameRu());

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
    public static RefCurrencyRateItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefCurrencyRateItem item = new RefCurrencyRateItem();
        item = (RefCurrencyRateItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        item.setRefRateAgency(cursor.getLong("REF_RATE_AGENCY"));
        item.setRefRateAgencyRecId(cursor.getLong("REF_RATE_AGENCY_REC_ID"));
        item.setRateAgency(cursor.getString("RATE_AGENCY"));
        return item;
    }

    //region Getter and Setter
    public Long getRefCurrency() {
        return refCurrency;
    }

    public void setRefCurrency(Long refCurrency) {
        this.refCurrency = refCurrency;
    }

    public String getRateAgency() {
        return rateAgency;
    }

    public void setRateAgency(String rateAgency) {
        this.rateAgency = rateAgency;
    }

    public Long getRefRateAgency() {
        return refRateAgency;
    }

    public void setRefRateAgency(Long refRateAgency) {
        this.refRateAgency = refRateAgency;
    }

    public Long getRefRateAgencyRecId() {
        return refRateAgencyRecId;
    }

    public void setRefRateAgencyRecId(Long refRateAgencyRecId) {
        this.refRateAgencyRecId = refRateAgencyRecId;
    }
    // endregion
}
