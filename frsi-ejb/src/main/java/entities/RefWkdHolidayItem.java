package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
/**
 * Created by Zhanar.Sanaupova on 24.01.2017.
 */
public class RefWkdHolidayItem extends AbstractReference {
    public static final String REF_CODE = "ref_wkd_holidays";
    private String nameDate;
    private Date dateValue;
    private String signWkdHoliday;

     // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_WKN_HOLIDAY_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_WKD_HLDAY_L_BY_PARAMS (?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "PKG_FRSI_REF.REF_READ_WKDHOLID_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_WKDHOLIDAYS (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_WKDHOLIDAYS (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_WKDHOLIDAYS (?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefWkdHolidayItem item, Date date) throws SQLException {
      int num = 0;


      if (item == null || item.getId() == null)
          ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
      else
          ocsNumMap.getOcs().setLong(++num, item.getId());

      if (date == null)
         ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
      else
         ocsNumMap.getOcs().setDate(++num, new java.sql.Date(date.getTime()));

      if (item == null || item.getNameDate() == null)
         ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
      else
         ocsNumMap.getOcs().setString(++num, item.getNameDate());

      if (item == null || item.getRecId() == null)
         ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
      else
         ocsNumMap.getOcs().setLong(++num, item.getRecId());


        ocsNumMap = AbstractReference.setDefaultOcsNumMapForCursor(ocsNumMap, num);

        return ocsNumMap;
    }

    // Insert
    public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefWkdHolidayItem item) throws SQLException{
        int num = 0;

        if (item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        } else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

        return ocsNumMap;
    }

    // Update
    public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefWkdHolidayItem item) throws SQLException {
        int num = 0;

        ocsNumMap.getOcs().setLong(++num, item.getId());
        ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

        return ocsNumMap;
    }

    // Edit for Insert and Update
    public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefWkdHolidayItem item, int num, String mode) throws SQLException{
        ocsNumMap.getOcs().setString(++num, item.getNameDate());
        ocsNumMap.getOcs().setDate(++num, new java.sql.Date(item.getDateValue().getTime()));
        ocsNumMap.getOcs().setString(++num, item.getSignWkdHoliday());
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
    public static RefWkdHolidayItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefWkdHolidayItem item = new RefWkdHolidayItem();
        item.setId(cursor.getLong("ID"));
        item.setRecId(cursor.getLong("REC_ID"));
        item.setNameDate(cursor.getString("NAME_DATE"));
        item.setDateValue(cursor.getDate("DATE_VALUE"));
        item.setSignWkdHoliday(cursor.getString("SIGN_WKD_HOLIDAY"));
        item.setBeginDate(cursor.getDate("BEGIN_DATE"));
        item.setEndDate(cursor.getDate("END_DATE"));
        item.setDatlast(cursor.getTimestamp("DATLAST"));
        item.setUserName(cursor.getString("USER_NAME"));
        item.setUserLocation(cursor.getString("USER_LOCATION"));
        item.setSentKnd(cursor.getString("SENT_KND"));

        return item;
    }

    // for History Cursor
    public static RefWkdHolidayItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
        RefWkdHolidayItem item = setItemFromCursor(cursor);
        item = (RefWkdHolidayItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);

        return item;
    }

    public String getNameDate() {
        return nameDate;
    }

    public void setNameDate(String nameDate) {
        this.nameDate = nameDate;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public String getSignWkdHoliday() {
        return signWkdHoliday;
    }

    public void setSignWkdHoliday(String signWkdHoliday) {
        this.signWkdHoliday = signWkdHoliday;
    }
}
