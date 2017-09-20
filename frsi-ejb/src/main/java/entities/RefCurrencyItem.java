package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 04.05.2015.
 */
public class RefCurrencyItem extends AbstractReference {
    public static final String REF_CODE = "ref_currency";
    private Long minorUnits;
    private Long rate;
    private String rateAgency;
    private Long refCurrencyRate;
    private Long refCurrencyRateRecId;
    private String curRateName;

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_CURRENCY_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_CURR_LIST_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefCurrencyItem item, Date date) throws SQLException {
        int num = 0;

        if (item == null || item.getId() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setLong(++num, item.getId());

        if (date == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setDate(++num, new java.sql.Date(date.getTime()));

        if (item == null || item.getCode() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getCode());

        if (item == null || item.getCurRateName() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getCurRateName());

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
    public static RefCurrencyItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefCurrencyItem item = new RefCurrencyItem();
        item = (RefCurrencyItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        item.setMinorUnits(cursor.getLong("MINOR_UNITS"));
        item.setRate(cursor.getLong("RATE"));
        item.setRefCurrencyRate(cursor.getLong("REF_CURRENCY_RATE"));
        item.setRefCurrencyRateRecId(cursor.getLong("REF_CURRENCY_RATE_REC_ID"));
        item.setCurRateName(cursor.getString("CUR_RATE_NAME"));
        item.setRateAgency(cursor.getString("RATE_AGENCY"));
        return item;
    }


    // region Getter and Setter
    public Long getMinorUnits() {
        return minorUnits;
    }

    public void setMinorUnits(Long minorUnits) {
        this.minorUnits = minorUnits;
    }

    public Long getRate() {
        return rate;
    }

    public void setRate(Long rate) {
        this.rate = rate;
    }

    public String getCurRateName() {
        return curRateName;
    }

    public void setCurRateName(String curRateName) {
        this.curRateName = curRateName;
    }

    public String getRateAgency() {
        return rateAgency;
    }

    public void setRateAgency(String rateAgency) {
        this.rateAgency = rateAgency;
    }

    public Long getRefCurrencyRate() {
        return refCurrencyRate;
    }

    public void setRefCurrencyRate(Long refCurrencyRate) {
        this.refCurrencyRate = refCurrencyRate;
    }

    public Long getRefCurrencyRateRecId() {
        return refCurrencyRateRecId;
    }

    public void setRefCurrencyRateRecId(Long refCurrencyRateRecId) {
        this.refCurrencyRateRecId = refCurrencyRateRecId;
    }

    // endregion
}
