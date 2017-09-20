package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Zhanar.Sanaupova on 05.07.2017.
 */
public class RefMajorMemberOrgItem extends  AbstractReference {
    public static final String REF_CODE = "ref_major_memorgs";

    private Long refLegalPerson;
    private Long refStatus;
    private String refStatusName;
    private Long refMajorMember;

    public static final String READ = "PKG_FRSI_REF.REF_READ_MAJOR_MEMBERORG_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_MMORG_LIST_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "PKG_FRSI_REF.REF_READ_MMORG_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_MAJOR_MEMBERORG(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_MAJOR_MEMBERORG(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_MAJOR_MEMBERORG(?, ?, ?, ?)";

    // read by filter
   public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefMajorMemberOrgItem item, Date date) throws SQLException {
       int num = 0;

       if (item == null || item.getId() == null) {
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       }else
           ocsNumMap.getOcs().setLong(++num, item.getId());

       if (date == null) {
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       }else
           ocsNumMap.getOcs().setDate(++num, new java.sql.Date(date.getTime()));


       if (item == null || item.getRecId() == null) {
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       }else
           ocsNumMap.getOcs().setLong(++num, item.getRecId());

       if (item == null || item.getRefMajorMember() == null || item.getRefMajorMember() == 0)
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       else
           ocsNumMap.getOcs().setLong(++num, item.getRefMajorMember());

       if (item == null || item.getSearchAllVer() == null)
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       else
           ocsNumMap.getOcs().setBoolean(++num, item.getSearchAllVer());

       if (item == null || item.getRefLegalPerson() == null || item.getRefLegalPerson() == 0)
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       else
           ocsNumMap.getOcs().setLong(++num, item.getRefLegalPerson());

       ocsNumMap = AbstractReference.setDefaultOcsNumMapForCursor(ocsNumMap, num);

       return ocsNumMap;
   }

   // Insert
   public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefMajorMemberOrgItem item) throws SQLException{
       int num = 0;

       if (item.getRecId() == null) {
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       } else
           ocsNumMap.getOcs().setLong(++num, item.getRecId());

       ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

       return ocsNumMap;
   }

   // Update
   public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefMajorMemberOrgItem item) throws SQLException {
       int num = 0;

       ocsNumMap.getOcs().setLong(++num, item.getId());
       ocsNumMap.getOcs().setLong(++num, item.getRecId());

       ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

       return ocsNumMap;
   }

   // Edit for Insert and Update
   public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefMajorMemberOrgItem item, int num, String mode) throws SQLException{

       if (item.getRefLegalPerson() == null || item.getRefLegalPerson() == 0) {
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       }else
           ocsNumMap.getOcs().setLong(++num, item.getRefLegalPerson());

       if (item.getRefMajorMember() == null || item.getRefMajorMember() == 0) {
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       }else
           ocsNumMap.getOcs().setLong(++num, item.getRefMajorMember());

       if (item.getRefStatus() == null || item.getRefStatus() == 0) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       }else
            ocsNumMap.getOcs().setLong(++num, item.getRefStatus());
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
   public static RefMajorMemberOrgItem setItemFromCursor(ResultSet cursor) throws SQLException{
       RefMajorMemberOrgItem item = new RefMajorMemberOrgItem();
       item = (RefMajorMemberOrgItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
       item.setRefLegalPerson(cursor.getLong("REF_LEGAL_PERSON"));
       item.setRefStatus(cursor.getLong("REF_STATUS"));
       item.setRefStatusName(cursor.getString("STATUSNAME"));
       item.setRefMajorMember(cursor.getLong("REF_MAJOR_MEMBER"));

       return item;
   }

   // for History Cursor
   public static RefMajorMemberOrgItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
       RefMajorMemberOrgItem item = setItemFromCursor(cursor);
       item = (RefMajorMemberOrgItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);
       return item;
   }


   // region Getter and Setter

   public Long getRefLegalPerson() {
      return refLegalPerson;
   }

   public void setRefLegalPerson(Long refLegalPerson) {
      this.refLegalPerson = refLegalPerson;
   }

   public Long getRefStatus() {
      return refStatus;
   }

   public void setRefStatus(Long refStatus) {
      this.refStatus = refStatus;
   }

   public Long getRefMajorMember() {
      return refMajorMember;
   }

   public void setRefMajorMember(Long refMajorMember) {
      this.refMajorMember = refMajorMember;
   }

    public String getRefStatusName() {
        return refStatusName;
    }

    public void setRefStatusName(String refStatusName) {
        this.refStatusName = refStatusName;
    }

    // endregion
}
