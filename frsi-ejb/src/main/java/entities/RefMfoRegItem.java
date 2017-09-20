package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 26.08.2016.
 */
public class RefMfoRegItem extends AbstractReference {
    public static final String REF_CODE = "ref_mfo_reg";

    private long refDepartmentId;
    private String depName;
    private long refLpId;
    private String lpName;
    private String base;
    private String numReg;
    private String FioManager;
    private String address;
    private String contactDetails;

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_MFO_REG_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_MFO_REG_LIST_BY_P (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "PKG_FRSI_REF.REF_READ_MFO_REG_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_MFO_REG (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_MFO_REG (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_MFO_REG (?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefMfoRegItem item, Date date) throws SQLException {
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

        if (item == null || item.getRefDepartmentId() == 0)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setLong(++num, item.getRefDepartmentId());

        if (item == null || item.getSearchAllVer() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setBoolean(++num, item.getSearchAllVer());

        ocsNumMap = AbstractReference.setDefaultOcsNumMapForCursor(ocsNumMap, num);

        return ocsNumMap;
    }

    // Insert
    public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefMfoRegItem item) throws SQLException{
        int num = 0;

        if (item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        } else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

        return ocsNumMap;
    }

    // Update
    public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefMfoRegItem item) throws SQLException {
        int num = 0;

        ocsNumMap.getOcs().setLong(++num, item.getId());
        ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

        return ocsNumMap;
    }

    // Edit for Insert and Update
    public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefMfoRegItem item, int num, String mode) throws SQLException{
        ocsNumMap.getOcs().setString(++num, item.getCode());
        ocsNumMap.getOcs().setString(++num, item.getNameKz());
        ocsNumMap.getOcs().setString(++num, item.getNameRu());
        ocsNumMap.getOcs().setString(++num, item.getNameEn());
        ocsNumMap.getOcs().setLong(++num, item.getRefDepartmentId());
        ocsNumMap.getOcs().setLong(++num, item.getRefLpId());
        ocsNumMap.getOcs().setString(++num, item.getBase());
        ocsNumMap.getOcs().setString(++num, item.getNumReg());
        ocsNumMap.getOcs().setString(++num, item.getFioManager());
        ocsNumMap.getOcs().setString(++num, item.getAddress());
        ocsNumMap.getOcs().setString(++num, item.getContactDetails());
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
    public static RefMfoRegItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefMfoRegItem item = new RefMfoRegItem();
        item = (RefMfoRegItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        item.setRefDepartmentId(cursor.getLong("REF_DEPARTMENT"));
        item.setDepName(cursor.getString("DEP_NAME_RU"));
        item.setRefLpId(cursor.getLong("REF_LEGAL_PERSON"));
        item.setLpName(cursor.getString("LP_NAME_RU"));
        item.setBase(cursor.getString("BASE"));
        item.setNumReg(cursor.getString("NUM_REG"));
        item.setFioManager(cursor.getString("FIO_MANAGER"));
        item.setAddress(cursor.getString("ADDRESS"));
        item.setContactDetails(cursor.getString("CONTACT_DETAILS"));

        return item;
    }

    // for History Cursor
    public static RefMfoRegItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
        RefMfoRegItem item = setItemFromCursor(cursor);
        item = (RefMfoRegItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);

        return item;
    }
    
    // region Getter and Setter
    public long getRefDepartmentId() {
        return refDepartmentId;
    }

    public void setRefDepartmentId(long refDepartmentId) {
        this.refDepartmentId = refDepartmentId;
    }

    public String getDepName() {
        return depName;
    }

    public void setDepName(String depName) {
        this.depName = depName;
    }

    public long getRefLpId() {
        return refLpId;
    }

    public void setRefLpId(long refLpId) {
        this.refLpId = refLpId;
    }

    public String getLpName() {
        return lpName;
    }

    public void setLpName(String lpName) {
        this.lpName = lpName;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getNumReg() {
        return numReg;
    }

    public void setNumReg(String numReg) {
        this.numReg = numReg;
    }

    public String getFioManager() {
        return FioManager;
    }

    public void setFioManager(String fioManager) {
        FioManager = fioManager;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(String contactDetails) {
        this.contactDetails = contactDetails;
    }
    
    // endregion
}
