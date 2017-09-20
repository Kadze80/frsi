package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Zhanar.Sanaupova on 10.07.2017.
 */
public class RefMajorMemDetailsItem extends AbstractReference {
    public static final String REF_CODE = "ref_major_memdetails";

    private Long  ref_major_memorgs;
    private String  num_pp_status_agree;
    private Date  date_pp_status_agree;
    private String  num_pp_status_recall;
    private Date  date_pp_status_recall;

    public static final String READ = "PKG_FRSI_REF.REF_READ_MAJOR_MEMDETAILS_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_MMDETAILS_L_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "PKG_FRSI_REF.REF_READ_MMDETAILS_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_MAJOR_MEMDETAILS(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_MAJOR_MEMDETAILS(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_MAJOR_MEMDETAILS(?, ?, ?, ?)";

    // read by filter
   public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefMajorMemDetailsItem item, Date date) throws SQLException {
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

       if (item == null || item.getRef_major_memorgs() == null || item.getRef_major_memorgs() == 0)
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       else
           ocsNumMap.getOcs().setLong(++num, item.getRef_major_memorgs());

       if (item == null || item.getSearchAllVer() == null)
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       else
           ocsNumMap.getOcs().setBoolean(++num, item.getSearchAllVer());

       ocsNumMap = AbstractReference.setDefaultOcsNumMapForCursor(ocsNumMap, num);

       return ocsNumMap;
   }

   // Insert
   public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefMajorMemDetailsItem item) throws SQLException{
       int num = 0;

       if (item.getRecId() == null) {
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       } else
           ocsNumMap.getOcs().setLong(++num, item.getRecId());

       ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

       return ocsNumMap;
   }

   // Update
   public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefMajorMemDetailsItem item) throws SQLException {
       int num = 0;

       ocsNumMap.getOcs().setLong(++num, item.getId());
       ocsNumMap.getOcs().setLong(++num, item.getRecId());

       ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

       return ocsNumMap;
   }

   // Edit for Insert and Update
   public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefMajorMemDetailsItem item, int num, String mode) throws SQLException{

       ocsNumMap.getOcs().setLong(++num, item.getRef_major_memorgs());
       ocsNumMap.getOcs().setString(++num, item.getNum_pp_status_agree());
       ocsNumMap.getOcs().setDate(++num, item.getDate_pp_status_agree() == null ? null :new java.sql.Date(item.getDate_pp_status_agree().getTime()));
       if (item.getNum_pp_status_recall() == null || item.getNum_pp_status_recall().isEmpty()) {
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       }else
           ocsNumMap.getOcs().setString(++num, item.getNum_pp_status_recall());
       ocsNumMap.getOcs().setDate(++num, item.getDate_pp_status_recall() == null ? null :new  java.sql.Date(item.getDate_pp_status_recall().getTime()));
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
   public static RefMajorMemDetailsItem setItemFromCursor(ResultSet cursor) throws SQLException{
       RefMajorMemDetailsItem item = new RefMajorMemDetailsItem();
       item = (RefMajorMemDetailsItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
       item.setRef_major_memorgs(cursor.getLong("REF_MAJOR_MEMORGS"));
       item.setNum_pp_status_agree(cursor.getString("NUM_PP_STATUS_AGREE"));
       item.setDate_pp_status_agree(cursor.getDate("DATE_PP_STATUS_AGREE"));
       item.setNum_pp_status_recall(cursor.getString("NUM_PP_STATUS_RECALL"));
       item.setDate_pp_status_recall(cursor.getDate("DATE_PP_STATUS_RECALL"));

       return item;
   }

   // for History Cursor
   public static RefMajorMemDetailsItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
       RefMajorMemDetailsItem item = setItemFromCursor(cursor);
       item = (RefMajorMemDetailsItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);
       return item;
   }


    public Long getRef_major_memorgs() {
        return ref_major_memorgs;
    }

    public void setRef_major_memorgs(Long ref_major_memorgs) {
        this.ref_major_memorgs = ref_major_memorgs;
    }

    public String getNum_pp_status_agree() {
        return num_pp_status_agree;
    }

    public void setNum_pp_status_agree(String num_pp_status_agree) {
        this.num_pp_status_agree = num_pp_status_agree;
    }

    public Date getDate_pp_status_agree() {
        return date_pp_status_agree;
    }

    public void setDate_pp_status_agree(Date date_pp_status_agree) {
        this.date_pp_status_agree = date_pp_status_agree;
    }

    public String getNum_pp_status_recall() {
        return num_pp_status_recall;
    }

    public void setNum_pp_status_recall(String num_pp_status_recall) {
        this.num_pp_status_recall = num_pp_status_recall;
    }

    public Date getDate_pp_status_recall() {
        return date_pp_status_recall;
    }

    public void setDate_pp_status_recall(Date date_pp_status_recall) {
        this.date_pp_status_recall = date_pp_status_recall;
    }
}
