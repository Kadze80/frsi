package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 04.05.2015.
 */
public class RefDepartmentItem extends AbstractReference {
    public static final String REF_CODE = "ref_department";
    private Long refDeptTypeId;
    private String refDeptTypeName;

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_DEPARTMENT_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_DEP_LIST_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefDepartmentItem item, Date date) throws SQLException {
        int num = 0;

        if (item == null || item.getId() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setLong(++num, item.getId());

        if (date == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setDate(++num, new java.sql.Date(date.getTime()));

        if (item == null || item.getNameRu() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setString(++num, item.getNameRu());

        if (item == null || item.getRecId() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        if (item == null || item.getRefDeptTypeId() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setLong(++num, item.getRefDeptTypeId());

        if (item == null || item.getSearchAllVer() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setBoolean(++num, item.getSearchAllVer());

        ocsNumMap = AbstractReference.setDefaultOcsNumMapForCursor(ocsNumMap, num);

        return ocsNumMap;
    }

    // for Cursor
    public static RefDepartmentItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefDepartmentItem item = new RefDepartmentItem();
        item = (RefDepartmentItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        item.setRefDeptTypeId(cursor.getLong("REF_DEPARTMENT_TYPE"));
        item.setRefDeptTypeName(cursor.getString("DEP_TYPE_NAME"));
        return item;
    }


    // region Constructor
    public RefDepartmentItem() {
    }

    public RefDepartmentItem(Long refDeptTypeId) {
        this.refDeptTypeId = refDeptTypeId;
    }

    // endregion

    // region Getter and Setter

    public Long getRefDeptTypeId() {
        return refDeptTypeId;
    }

    public void setRefDeptTypeId(Long refDeptTypeId) {
        this.refDeptTypeId = refDeptTypeId;
    }

    public String getRefDeptTypeName() {
        return refDeptTypeName;
    }

    public void setRefDeptTypeName(String refDeptTypeName) {
        this.refDeptTypeName = refDeptTypeName;
    }

    // endregion
}
