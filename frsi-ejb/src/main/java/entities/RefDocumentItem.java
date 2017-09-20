package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 05.05.2015.
 */
public class RefDocumentItem extends AbstractReference {
    public static final String REF_CODE = "ref_document";
    private Long refDocType;
    private Long refDocTypeRecId;
    private String docTypeName;
    private Long refRespondent;
    private Long refRespondentRecId;
    private String respondentName;

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_DOCUMENT_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_DOC_LIST_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "PKG_FRSI_REF.REF_READ_DOCUMENT_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_DOCUMENT (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_DOCUMENT (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_DOCUMENT (?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefDocumentItem item, Date date) throws SQLException {
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
    public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefDocumentItem item) throws SQLException{
        int num = 0;

        if (item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        } else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

        return ocsNumMap;
    }

    // Update
    public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefDocumentItem item) throws SQLException {
        int num = 0;

        ocsNumMap.getOcs().setLong(++num, item.getId());
        ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

        return ocsNumMap;
    }

    // Edit for Insert and Update
    public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefDocumentItem item, int num, String mode) throws SQLException{
        ocsNumMap.getOcs().setString(++num, item.getCode());
        ocsNumMap.getOcs().setString(++num, item.getNameKz());
        ocsNumMap.getOcs().setString(++num, item.getNameRu());
        ocsNumMap.getOcs().setString(++num, item.getNameEn());
        ocsNumMap.getOcs().setLong(++num, item.getRefDocType());
        ocsNumMap.getOcs().setLong(++num, item.getRefRespondent());
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
    public static RefDocumentItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefDocumentItem item = new RefDocumentItem();
        item = (RefDocumentItem) AbstractReference.setDefaultItemFromCursor(item, cursor);
        item.setRefDocType(cursor.getLong("REF_DOC_TYPE"));
        item.setRefDocTypeRecId(cursor.getLong("REF_DOC_TYPE_REC_ID"));
        item.setDocTypeName(cursor.getString("DOC_TYPE_NAME"));
        item.setRefRespondent(cursor.getLong("REF_RESPONDENT"));
        item.setRefRespondentRecId(cursor.getLong("REF_RESPONDENT_REC_ID"));
        item.setRespondentName(cursor.getString("RESPONDENT_NAME"));

        return item;
    }

    // for History Cursor
    public static RefDocumentItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
        RefDocumentItem item = setItemFromCursor(cursor);
        item = (RefDocumentItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);

        return item;
    }

    // region Getter and Setter
    public String getDocTypeName() {
        return docTypeName;
    }

    public void setDocTypeName(String docTypeName) {
        this.docTypeName = docTypeName;
    }

    public Long getRefDocType() {
        return refDocType;
    }

    public void setRefDocType(Long refDocType) {
        this.refDocType = refDocType;
    }

    public Long getRefRespondent() {
        return refRespondent;
    }

    public void setRefRespondent(Long refRespondent) {
        this.refRespondent = refRespondent;
    }

    public String getRespondentName() {
        return respondentName;
    }

    public void setRespondentName(String respondentName) {
        this.respondentName = respondentName;
    }

    public Long getRefDocTypeRecId() {
        return refDocTypeRecId;
    }

    public void setRefDocTypeRecId(Long refDocTypeRecId) {
        this.refDocTypeRecId = refDocTypeRecId;
    }

    public Long getRefRespondentRecId() {
        return refRespondentRecId;
    }

    public void setRefRespondentRecId(Long refRespondentRecId) {
        this.refRespondentRecId = refRespondentRecId;
    }

    // endregion
}
