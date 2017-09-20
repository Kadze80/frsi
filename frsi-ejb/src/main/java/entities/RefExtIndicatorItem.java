package entities;

import oracle.jdbc.OracleTypes;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class RefExtIndicatorItem extends AbstractReference {
    public static final String REF_CODE = "ref_extind";

    private Long extSysId;
    private String extSysNameRu;
    private String algorithm;
    private String valueType;
    private Long valueTypeId;

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_EXTIND_LIST(?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_EXTIND_LIST_BY_PARAMS(?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "PKG_FRSI_REF.REF_READ_EXTIND_HST_LIST(?, ?, ?, ?)";
    public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_EXTIND(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_EXTIND(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_EXTIND(?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefExtIndicatorItem item, Date date) throws SQLException {
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

   // Insert
   public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefExtIndicatorItem item) throws SQLException{
       int num = 0;

       if (item.getRecId() == null) {
           ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
       } else
           ocsNumMap.getOcs().setLong(++num, item.getRecId());

       ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

       return ocsNumMap;
   }

   // Update
   public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefExtIndicatorItem item) throws SQLException {
       int num = 0;

       ocsNumMap.getOcs().setLong(++num, item.getId());
       ocsNumMap.getOcs().setLong(++num, item.getRecId());

       ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

       return ocsNumMap;
   }

   // Edit for Insert and Update
   public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefExtIndicatorItem item, int num, String mode) throws SQLException{
       ocsNumMap.getOcs().setString(++num, item.getCode());
       ocsNumMap.getOcs().setString(++num, item.getNameKz());
       ocsNumMap.getOcs().setString(++num, item.getNameRu());
       ocsNumMap.getOcs().setString(++num, item.getNameEn());
       ocsNumMap.getOcs().setLong(++num, item.getExtSysId());
       ocsNumMap.getOcs().setString(++num, item.getAlgorithm());
       ocsNumMap.getOcs().setString(++num, item.getValueType());
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
   public static RefExtIndicatorItem setItemFromCursor(ResultSet cursor) throws SQLException{
       RefExtIndicatorItem item = new RefExtIndicatorItem();
       item = (RefExtIndicatorItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
       item.setExtSysId(cursor.getLong("EXTSYS_ID"));
       item.setExtSysNameRu(cursor.getString("EXTSYSNAMERU"));
       Clob alg = cursor.getClob("ALG");
       if (alg != null) {
          item.setAlgorithm(alg.getSubString(1, (int) alg.length()));
          alg.free();
       }
      // item.setValueTypeId(cursor.getLong("VALUE_TYPE"));
       item.setValueType(cursor.getString("VALUE_TYPENAME"));

       return item;
   }

   // for History Cursor
   public static RefExtIndicatorItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
       RefExtIndicatorItem item = setItemFromCursor(cursor);
       item = (RefExtIndicatorItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);
       return item;
   }

    public Long getExtSysId() {
        return extSysId;
    }

    public void setExtSysId(Long extSysId) {
        this.extSysId = extSysId;
    }

    public String getExtSysNameRu() {
        return extSysNameRu;
    }

    public void setExtSysNameRu(String extSysNameRu) {
        this.extSysNameRu = extSysNameRu;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

   /* @Override
    public String toString() {
        return "RefExtIndicatorItem{" +
                "extSysId=" + extSysId +
                ", extSysNameRu='" + extSysNameRu + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", valueType=" + valueType +
                '}';
    }       */

    public Long getValueTypeId() {
        return valueTypeId;
    }

    public void setValueTypeId(Long valueTypeId) {
        this.valueTypeId = valueTypeId;
    }
}
