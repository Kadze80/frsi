package entities;

import oracle.jdbc.OracleTypes;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Entity
 *
 * @author Ardak Saduakassov
 */
public abstract class AbstractReference implements Serializable {
    private static final long serialVersionUID = 1L;
    private String refCode;
	private Long id;
    private Long idHst;
    private Long recId;
	private String code;
	private String nameEn;
	private String nameKz;
	private String nameRu;
    private String shortNameKz;
    private String shortNameRu;
    private String shortNameEn;
    private Date beginDate;
    private Date endDate;
    private Long userId;
    private String userName;
    private String userLocation;
    private Long delfl;
    private Date datlast;
    private Long typeChange;
    private String typeChangeName;
    private String sentKnd;
    private String note;
    private String tag;
    private Long ErrCode;
    private String ErrMsg;
    private Long value;
    private Boolean searchAllVer;

    // region Universal methods
    public static AbstractReference setDefaultItemFromCursor(AbstractReference item, ResultSet cursor) throws SQLException {
        item.setId(cursor.getLong("ID"));
        item.setRecId(cursor.getLong("REC_ID"));
        item.setCode(cursor.getString("CODE"));
        item.setNameKz(cursor.getString("NAME_KZ"));
        item.setNameRu(cursor.getString("NAME_RU"));
        item.setNameEn(cursor.getString("NAME_EN"));
        item.setBeginDate(cursor.getDate("BEGIN_DATE"));
        item.setEndDate(cursor.getDate("END_DATE"));
        item.setDatlast(cursor.getTimestamp("DATLAST"));
        item.setUserName(cursor.getString("USER_NAME"));
        item.setUserLocation(cursor.getString("USER_LOCATION"));
        item.setSentKnd(cursor.getString("SENT_KND"));

        return item;
    }

    public static AbstractReference setDefaultHstItemFromCursor(AbstractReference item, ResultSet cursor) throws SQLException {
        item.setIdHst(cursor.getLong("ID_HST"));
        item.setTypeChange(cursor.getLong("TYPE_CHANGE"));
        item.setTypeChangeName(cursor.getString("TYPE_CHANGE_NAME"));

        return item;
    }

    public static OcsNumMap setOcsNumMapByDateForList(OcsNumMap ocsNumMap, Date date) throws SQLException{
        int num = 0;

        if (date == null) {
            ocsNumMap.getOcs().setNull(++num, OracleTypes.NULL);
        }else
            ocsNumMap.getOcs().setDate(++num, new java.sql.Date(date.getTime()));

        ocsNumMap = setDefaultOcsNumMapForCursor(ocsNumMap, num);

        return ocsNumMap;
    }

    public static OcsNumMap setOcsNumMapByIdForList(OcsNumMap ocsNumMap, Long id) throws SQLException{
        int num = 0;

        ocsNumMap.getOcs().setLong(++num, id);
        ocsNumMap = setDefaultOcsNumMapForCursor(ocsNumMap, num);

        return ocsNumMap;
    }

    public static OcsNumMap setDefaultOcsNumMapForCursor(OcsNumMap ocsNumMap, int num) throws SQLException{
        ocsNumMap.getOcs().registerOutParameter(++num, OracleTypes.CURSOR);
        ocsNumMap.getNumMap().put("cursor", num);
        ocsNumMap = setDefaultOcsNumMap(ocsNumMap, num);

        return ocsNumMap;
    }

    public static OcsNumMap setDefaultOcsNumMapForIns(OcsNumMap ocsNumMap, int num) throws SQLException{
        ocsNumMap.getOcs().registerOutParameter(++num, OracleTypes.FLOAT);
        ocsNumMap.getNumMap().put("id", num);
        ocsNumMap = setDefaultOcsNumMap(ocsNumMap, num);

        return ocsNumMap;
    }

    public static OcsNumMap setDefaultOcsNumMap(OcsNumMap ocsNumMap, int num) throws SQLException{
        ocsNumMap.getOcs().registerOutParameter(++num, OracleTypes.INTEGER);
        ocsNumMap.getNumMap().put("err_code", num);
        ocsNumMap.getOcs().registerOutParameter(++num, OracleTypes.VARCHAR);
        ocsNumMap.getNumMap().put("err_msg", num);

        return ocsNumMap;
    }

    public static OcsNumMap setOcsNumMapForDel(OcsNumMap ocsNumMap, Long id) throws SQLException {
        int num = 0;
        ocsNumMap.getOcs().setLong(++num, id);
        ocsNumMap.getOcs().setInt(++num, 1);
        ocsNumMap = setDefaultOcsNumMap(ocsNumMap, num);
        return ocsNumMap;
    }

    // endregion

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractReference that = (AbstractReference) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // region Getters and Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getNameEn() {
		return nameEn;
	}

	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}

	public String getNameKz() {
		return  nameKz;
	}

	public void setNameKz(String nameKz) {
		this.nameKz = nameKz;
	}

	public String getNameRu() {
		return nameRu;
	}

	public void setNameRu(String nameRu) {
		this.nameRu = nameRu;
	}

	public String getName(String languageCode) {
        String name = null;
        if (languageCode.equals("kz")){
            name = nameKz;
        }else if (languageCode.equals("ru")) {
            name = nameRu;
        }else if (languageCode.equals("en")) {
            name = nameEn;
        }

        if (name != null && !name.trim().isEmpty())
            return name;

        String[] names = {nameRu, nameKz, nameEn};
        for (String n : names) {
            if (n != null) {
                if (!n.trim().isEmpty()) {
                    return n;
                }
            }
        }
        return "";
    }

    public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getDatlast() {
        return datlast;
    }

    public void setDatlast(Date datlast) {
        this.datlast = datlast;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDelfl() {
        return delfl;
    }

    public void setDelfl(Long delfl) {
        this.delfl = delfl;
    }

    public Long getTypeChange() {
        return typeChange;
    }

    public void setTypeChange(Long typeChange) {
        this.typeChange = typeChange;
    }

    public String getTypeChangeName() {
        return typeChangeName;
    }

    public void setTypeChangeName(String typeChangeName) {
        this.typeChangeName = typeChangeName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getShortNameRu() {
        return shortNameRu;
    }

    public void setShortNameRu(String shortNameRu) {
        this.shortNameRu = shortNameRu;
    }

    public Long getRecId() {
        return recId;
    }

    public void setRecId(Long recId) {
        this.recId = recId;
    }

    public String getSentKnd() {
        return sentKnd;
    }

    public void setSentKnd(String sentKnd) {
        this.sentKnd = sentKnd;
    }

    public Long getIdHst() {
        return idHst;
    }

    public void setIdHst(Long idHst) {
        this.idHst = idHst;
    }

    public String getShortNameKz() {
        return shortNameKz;
    }

    public void setShortNameKz(String shortNameKz) {
        this.shortNameKz = shortNameKz;
    }

    public String getShortNameEn() {
        return shortNameEn;
    }

    public void setShortNameEn(String shortNameEn) {
        this.shortNameEn = shortNameEn;
    }

    public String getRefCode() {
        return refCode;
    }

    public void setRefCode(String refCode) {
        this.refCode = refCode;
    }

    public Long getErrCode() {
        return ErrCode;
    }

    public void setErrCode(Long errCode) {
        ErrCode = errCode;
    }

    public String getErrMsg() {
        return ErrMsg;
    }

    public void setErrMsg(String errMsg) {
        ErrMsg = errMsg;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Boolean getSearchAllVer() {
        return searchAllVer;
    }

    public void setSearchAllVer(Boolean searchAllVer) {
        this.searchAllVer = searchAllVer;
    }

    public String getShortName(){
        if(shortNameRu != null && !shortNameRu.trim().isEmpty()){
            return shortNameRu;
        }else{
            return nameRu;
        }
    }

    // endregion
}
