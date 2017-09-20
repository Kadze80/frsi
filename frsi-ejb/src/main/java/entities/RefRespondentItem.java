package entities;

import oracle.jdbc.OracleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;


/**
 * Created by Ayupov.Bakhtiyar on 05.05.2015.
 */
public class RefRespondentItem extends AbstractReference {
    public static final String REF_CODE = "ref_respondent";
    private String nokbdbCode;
    private Long unionPersonsId;
    private Long person;
    private Long personRecId;
    private String personName;
    private String personShortName;
    private String mainBuh;
    private Date dateBeginLic;
    private Date dateEndLic;
    private String stopLic;
    private String vidActivity;
    private String idn;
    private String vat;
    private Long refSubjectType;
    private Long refSubjectTypeRecId;
    private String refSubjectTypeName;
    private Long refDepartment;
    private Long refDepartmentRecId;
    private String refDepartmentName;
    private Boolean nonResident;
    private Date warrantDate; //дата доверенности
    private String warrantNum; //номер доверенности

    // prepareCall
    public static final String READ = "PKG_FRSI_REF.REF_READ_RESPONDENT_LIST (?, ?, ?, ?)";
    public static final String READ_BY_FILTER = "PKG_FRSI_REF.REF_READ_RESP_LIST_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String READ_HST = "PKG_FRSI_REF.REF_READ_RESPONDENT_HST_LIST (?, ?, ?, ?)";
    public static final String INSERT = "PKG_FRSI_REF.REF_INSERT_RESPONDENT (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String UPDATE = "PKG_FRSI_REF.REF_UPDATE_RESPONDENT (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE = "PKG_FRSI_REF.REF_DELETE_RESPONDENT (?, ?, ?, ?)";

    // read by filter
    public static OcsNumMap setOcsNumMapByFilters(OcsNumMap ocsNumMap, RefRespondentItem item, Date date) throws SQLException {
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

        if (item == null || item.getIdn() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setString(++num, item.getIdn());

        if (item == null || item.getRefDepartment() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setLong(++num, item.getRefDepartment());

        if (item == null || item.getRefSubjectType() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setLong(++num, item.getRefSubjectType());

        if (item == null || item.getSearchAllVer() == null)
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        else
            ocsNumMap.getOcs().setBoolean(++num, item.getSearchAllVer());

        ocsNumMap = AbstractReference.setDefaultOcsNumMapForCursor(ocsNumMap, num);

        return ocsNumMap;
    }

    // Insert
    public static OcsNumMap setOcsNumMapForIns(OcsNumMap ocsNumMap, RefRespondentItem item) throws SQLException{
        int num = 0;

        if (item.getRecId() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        } else
            ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "ins");

        return ocsNumMap;
    }

    // Update
    public static OcsNumMap setOcsNumMapForUpd(OcsNumMap ocsNumMap, RefRespondentItem item) throws SQLException {
        int num = 0;

        ocsNumMap.getOcs().setLong(++num, item.getId());
        ocsNumMap.getOcs().setLong(++num, item.getRecId());

        ocsNumMap = setOcsNumMapForEdit(ocsNumMap, item, num, "upd");

        return ocsNumMap;
    }

    // Edit for Insert and Update
    public static OcsNumMap setOcsNumMapForEdit(OcsNumMap ocsNumMap, RefRespondentItem item, int num, String mode) throws SQLException{
        ocsNumMap.getOcs().setString(++num, item.getCode());
        ocsNumMap.getOcs().setString(++num, item.getNokbdbCode());
        ocsNumMap.getOcs().setString(++num, item.getMainBuh());
        if (item.getDateBeginLic() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setDate(++num, new java.sql.Date(item.getDateBeginLic().getTime()));
        if (item.getDateEndLic() == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setDate(++num, new java.sql.Date(item.getDateEndLic().getTime()));
        ocsNumMap.getOcs().setString(++num, item.getStopLic());
        ocsNumMap.getOcs().setString(++num, item.getVidActivity());
        if(item.getRefDepartment() == 0){
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setLong(++num, item.getRefDepartment());
        if(item.getRefSubjectType() == 0){
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setLong(++num, item.getRefSubjectType());
        ocsNumMap.getOcs().setDate(++num, new java.sql.Date(item.getBeginDate().getTime()));
        ocsNumMap.getOcs().setDate(++num, item.getEndDate() == null ? null : new java.sql.Date(item.getEndDate().getTime()));
        ocsNumMap.getOcs().setLong(++num, item.getUserId());
        ocsNumMap.getOcs().setString(++num, item.getUserLocation());
        ocsNumMap.getOcs().setDate(++num, item.getDatlast() == null ? null : new java.sql.Date(item.getDatlast().getTime()));
        ocsNumMap.getOcs().setLong(++num, item.getUnionPersonsId());
        ocsNumMap.getOcs().setInt(++num, 1);

        if(mode.equals("ins"))
            ocsNumMap = AbstractReference.setDefaultOcsNumMapForIns(ocsNumMap, num);
        else if (mode.equals("upd"))
            ocsNumMap = AbstractReference.setDefaultOcsNumMap(ocsNumMap, num);

        return ocsNumMap;
    }

    // for Cursor
    public static RefRespondentItem setItemFromCursor(ResultSet cursor) throws SQLException{
        RefRespondentItem item = new RefRespondentItem();
        item.setId(cursor.getLong("ID"));
        item.setRecId(cursor.getLong("REC_ID"));
        item.setUnionPersonsId(cursor.getLong("REF_UNIONPERSONS"));
        item.setPerson(cursor.getLong("PERSON_ID"));
        item.setPersonRecId(cursor.getLong("PERSON_REC_ID"));
        item.setPersonName(cursor.getString("PERSON_NAME"));
        item.setNameRu(cursor.getString("PERSON_NAME"));
        item.setShortNameRu(cursor.getString("PERSON_SHORT_NAME"));
        item.setCode(cursor.getString("CODE"));
        item.setNokbdbCode(cursor.getString("NOKBDB_CODE"));
        item.setMainBuh(cursor.getString("MAIN_BUH"));
        item.setDateBeginLic(cursor.getDate("DATE_BEGIN_LIC"));
        item.setDateEndLic(cursor.getDate("DATE_END_LIC"));
        item.setStopLic(cursor.getString("STOP_LIC"));
        item.setVidActivity(cursor.getString("VID_ACTIVITY"));
        item.setBeginDate(cursor.getDate("BEGIN_DATE"));
        item.setEndDate(cursor.getDate("END_DATE"));
        item.setDatlast(cursor.getTimestamp("DATLAST"));
        item.setUserName(cursor.getString("USER_NAME"));
        item.setUserLocation(cursor.getString("USER_LOCATION"));
        item.setSentKnd(cursor.getString("SENT_KND"));
        item.setIdn(cursor.getString("IDN"));
        item.setRefSubjectType(cursor.getLong("REF_SUBJECT_TYPE"));
        item.setRefSubjectTypeRecId(cursor.getLong("REF_SUBJECT_TYPE_REC_ID"));
        item.setRefSubjectTypeName(cursor.getString("REF_SUBJECT_TYPE_NAME"));
        item.setRefDepartment(cursor.getLong("REF_DEPARTMENT"));
        item.setRefDepartmentRecId(cursor.getLong("REF_DEPARTMENT_REC_ID"));
        item.setRefDepartmentName(cursor.getString("REF_DEPARTMENT_NAME"));
        item.setNonResident(cursor.getInt("IS_NON_REZIDENT") > 0);

        return item;
    }

    // for History Cursor
    public static RefRespondentItem setItemHstFromCursor(ResultSet cursor) throws SQLException{
        RefRespondentItem item = setItemFromCursor(cursor);
        item = (RefRespondentItem) AbstractReference.setDefaultHstItemFromCursor(item, cursor);

        return item;
    }

    // region Getter and Setter
    public String getNokbdbCode() {
        return nokbdbCode;
    }

    public void setNokbdbCode(String nokbdbCode) {
        this.nokbdbCode = nokbdbCode;
    }

    public String getMainBuh() {
        return mainBuh;
    }

    public void setMainBuh(String mainBuh) {
        this.mainBuh = mainBuh;
    }

    public Date getDateBeginLic() {
        return dateBeginLic;
    }

    public void setDateBeginLic(Date dateBeginLic) {
        this.dateBeginLic = dateBeginLic;
    }

    public Date getDateEndLic() {
        return dateEndLic;
    }

    public void setDateEndLic(Date dateEndLic) {
        this.dateEndLic = dateEndLic;
    }

    public String getStopLic() {
        return stopLic;
    }

    public void setStopLic(String stopLic) {
        this.stopLic = stopLic;
    }

    public String getVidActivity() {
        return vidActivity;
    }

    public void setVidActivity(String vidActivity) {
        this.vidActivity = vidActivity;
    }

    public Long getPerson() {
        return person;
    }

    public void setPerson(Long person) {
        this.person = person;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonShortName() {
        return personShortName == null ? personName : personShortName;
    }

    public void setPersonShortName(String personShortName) {
        this.personShortName = personShortName;
    }

    public String getIdn() {
        return idn;
    }

    public void setIdn(String idn) {
        this.idn = idn;
    }

    public Long getRefSubjectType() {
        return refSubjectType;
    }

    public void setRefSubjectType(Long refSubjectType) {
        this.refSubjectType = refSubjectType;
    }

    public Long getRefSubjectTypeRecId() {
        return refSubjectTypeRecId;
    }

    public void setRefSubjectTypeRecId(Long refSubjectTypeRecId) {
        this.refSubjectTypeRecId = refSubjectTypeRecId;
    }

    public Long getPersonRecId() {
        return personRecId;
    }

    public void setPersonRecId(Long personRecId) {
        this.personRecId = personRecId;
    }

    public String getRefSubjectTypeName() {
        return refSubjectTypeName;
    }

    public void setRefSubjectTypeName(String refSubjectTypeName) {
        this.refSubjectTypeName = refSubjectTypeName;
    }

    public Long getRefDepartment() {
        return refDepartment;
    }

    public void setRefDepartment(Long refDepartment) {
        this.refDepartment = refDepartment;
    }

    public Long getRefDepartmentRecId() {
        return refDepartmentRecId;
    }

    public void setRefDepartmentRecId(Long refDepartmentRecId) {
        this.refDepartmentRecId = refDepartmentRecId;
    }

    public String getRefDepartmentName() {
        return refDepartmentName;
    }

    public void setRefDepartmentName(String refDepartmentName) {
        this.refDepartmentName = refDepartmentName;
    }

    public Boolean getNonResident() {
        return nonResident;
    }

    public void setNonResident(Boolean nonResident) {
        this.nonResident = nonResident;
    }

    public Long getUnionPersonsId() {
        return unionPersonsId;
    }

    public void setUnionPersonsId(Long unionPersonsId) {
        this.unionPersonsId = unionPersonsId;
    }

    public Date getWarrantDate() {
        return warrantDate;
    }

    public void setWarrantDate(Date warrantDate) {
        this.warrantDate = warrantDate;
    }

    public String getWarrantNum() {
        return warrantNum;
    }

    public void setWarrantNum(String warrantNum) {
        this.warrantNum = warrantNum;
    }

    // endregion
}
