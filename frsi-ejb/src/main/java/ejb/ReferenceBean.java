package ejb;

import entities.*;
import excelreport.EntityMapConverter;
import excelreport.ExcelReport;
import excelreport.MapKeyHandler;
import excelreport.ReportSheet;
import excelreport.impl.ExcelReportImpl;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;
import org.apache.log4j.Logger;
import parser.DataType;
import util.Convert;
import util.DbUtil;
import util.OracleException;
import util.SqlParamParser;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.*;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by Ayupov.Bakhtiyar on 14.04.2016.
 */
@Stateless
public class ReferenceBean implements ReferenceLocal, ReferenceRemote {
    private static final Logger logger = Logger.getLogger("fileLogger");
    private static final String JDBC_POOL_NAME = "jdbc/FrsiPool";

    //    @EJB private CoreLocal core;
    private DataSource dataSource;

    private static final Map<Class, String> refTableNames = new HashMap<Class, String>(); // not used
    private static final Map<String, Class> refClassMap = new HashMap<String, Class>(); // not used

    static {
        refClassMap.put("ref_country", RefCountryItem.class);
        refTableNames.put(RefDeliveryWay.class, "REF_DELIVERY_WAY");
        refTableNames.put(RefFormStatus.class, "REF_FORM_STATUS");
    }

    @PostConstruct
    @Override
    public void init() {
        Date dateStart = new Date();

        try {
            Context context = new InitialContext();
            dataSource = (DataSource) context.lookup(JDBC_POOL_NAME);
            logger.info("Connected to " + JDBC_POOL_NAME);
        } catch (NamingException e) {
            logger.error("Could not connect to " + JDBC_POOL_NAME);
            throw new EJBException(e);
        }

        Date dateEnd = new Date();
        long duration = dateEnd.getTime() - dateStart.getTime();
        logger.debug(MessageFormat.format("t: {0}, d: {1} ms", dateStart, duration));
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public Template getTemplateData(Template template) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Template result = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT code_template, xls_out FROM project_template WHERE (? is null or id = ?) and (? is null or code_template = ?) and (? is null or code = ?)" +
                    "and (? is null or begin_date <= ?) and (? is null or end_date >= ?)");
            if (template.getId() != 0) {
                ps.setLong(1, template.getId());
                ps.setLong(2, template.getId());
            } else {
                ps.setNull(1, OracleTypes.NULL);
                ps.setNull(2, OracleTypes.NULL);
            }

            if (template.getCodeTemplate() != null) {
                ps.setString(3, template.getCodeTemplate());
                ps.setString(4, template.getCodeTemplate());
            } else {
                ps.setNull(3, OracleTypes.NULL);
                ps.setNull(4, OracleTypes.NULL);
            }

            if (template.getCode() != null) {
                ps.setString(5, template.getCode());
                ps.setString(6, template.getCode());
            } else {
                ps.setNull(5, OracleTypes.NULL);
                ps.setNull(6, OracleTypes.NULL);
            }

            if (template.getBeginDate() != null) {
                ps.setDate(7, new java.sql.Date(template.getBeginDate().getTime()));
                ps.setDate(8, new java.sql.Date(template.getBeginDate().getTime()));
            } else {
                ps.setNull(7, OracleTypes.NULL);
                ps.setNull(8, OracleTypes.NULL);
            }

            if (template.getEndDate() != null) {
                ps.setDate(9, new java.sql.Date(template.getEndDate().getTime()));
                ps.setDate(10, new java.sql.Date(template.getEndDate().getTime()));
            } else {
                ps.setNull(9, OracleTypes.NULL);
                ps.setNull(10, OracleTypes.NULL);
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                Template item = new Template();
                item.setCodeTemplate(rs.getString("CODE_TEMPLATE"));
                Blob blobXls = rs.getBlob("XLS_OUT");
                if (blobXls != null) {
                    item.setXlsOut(blobXls.getBytes(1, (int) blobXls.length()));
                    blobXls.free();
                }
                result = item;
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    // REF_MAIN
    @Override
    public List<RefItem> getRefList(Long userId, RefItem filterRefMainItem) {
        int Err_Code = 0;
        String Err_Msg = "";
        List<RefItem> result = new ArrayList<RefItem>();

        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;

        try {
            connection = getConnection();
            // вызов процедуры
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.READ_REF_LIST (?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            ocs.setLong(1, userId);
            if (filterRefMainItem == null || filterRefMainItem.getRefKnd() == null || filterRefMainItem.getRefKnd() == 0)
                ocs.setNull(2, OracleTypes.NULL);
            else
                ocs.setLong(2, filterRefMainItem.getRefKnd());
            if (filterRefMainItem == null || filterRefMainItem.getName() == null)
                ocs.setNull(3, OracleTypes.NULL);
            else
                ocs.setString(3, filterRefMainItem.getName());
            if (filterRefMainItem == null || filterRefMainItem.getCode() == null)
                ocs.setNull(4, OracleTypes.NULL);
            else
                ocs.setString(4, filterRefMainItem.getCode());
            ocs.registerOutParameter(5, OracleTypes.CURSOR);
            ocs.registerOutParameter(6, OracleTypes.INTEGER);
            ocs.registerOutParameter(7, OracleTypes.VARCHAR);
            ocs.execute();
            Err_Code = ocs.getInt(6);
        } catch (SQLException e) {
            throw new EJBException(e);
        }

        // получение курсора
        try {
            cursor = ocs.getCursor(5);
        } catch (SQLException e) {
            throw new EJBException(e);
        }
        try {
            // записываем в Bean, а затем добавляем в ArrayList
            while (cursor.next()) {
                RefItem ref = new RefItem();
                ref.setId(cursor.getLong("ID"));
                ref.setName(cursor.getString("NAME"));
                ref.setCode(cursor.getString("CODE"));
                ref.setRefKnd(cursor.getLong("REF_KND"));
                ref.setRefKndName(cursor.getString("REF_KND_NAME"));
                ref.setDateLoad(cursor.getTimestamp("DATE_LOAD"));
                ref.setStsLoad(cursor.getString("STS_LOAD"));
                ref.setCntNotSent(cursor.getLong("CNT_NOT_SENT"));
                ref.setCntWait(cursor.getLong("CNT_WAIT"));
                result.add(ref);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, cursor, ocs);
        }

        return result;
    }

    @Override
    public RefItem getRefByCode(String code) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        RefItem result = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT * FROM ref_main WHERE code = ?");
            ps.setString(1, code);
            rs = ps.executeQuery();
            while (rs.next()) {
                result = new RefItem();
                result.setId(rs.getLong("id"));
                result.setName(rs.getString("name"));
                result.setCode(rs.getString("code"));
                result.setRefKnd(rs.getLong("ref_knd"));
                result.setDateLoad(rs.getTimestamp("date_load"));
                result.setStsLoad(rs.getString("sts_load"));
//                result.setCntNotSent(rs.getLong("cnt_not_sent"));
//                result.setCntWait(rs.getLong("cnt_wait"));
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    // Получить элементы справочника
    public List<RefElements> getRefElements(String name, Boolean withCode) {
        List<RefElements> result = new ArrayList<RefElements>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT * FROM " + name);
            rs = ps.executeQuery();
            while (rs.next()) {
                RefElements ref = new RefElements();
                ref.setId(rs.getLong("ID"));
                ref.setName(rs.getString("NAME"));
                if (withCode)
                    ref.setCode(rs.getString("CODE"));
                result.add(ref);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }


    @Override
    public boolean refItemExists(String refName, Long rec_id, Date date) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("select * from " + refName + " where delfl = 0 and rec_id = ? and begin_date <= trunc(?)  and (end_date is null or end_date > trunc(?))");
            ps.setLong(1, rec_id);
            ps.setDate(2, new java.sql.Date(date.getTime()));
            ps.setDate(3, new java.sql.Date(date.getTime()));

            rs = ps.executeQuery();
            result = rs.next();
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public Long getRefRecId(String refName, String refColumn, String refColumnValue, Date date) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Long result = -1L;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("select rec_id as rec_id from " + refName + " where delfl = 0 and lower(" + refColumn + ") = lower(?) and begin_date <= trunc(?)  and (end_date is null or end_date > trunc(?)) group by rec_id");
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            ps.setString(1, refColumnValue);
            ps.setDate(2, sqlDate);
            ps.setDate(3, sqlDate);

            rs = ps.executeQuery();
            if (rs.next())
                result = rs.getLong("REC_ID");
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public Long getRefRecIdById(String refName, Long id) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Long result = -1L;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("select rec_id from " + refName + " where id = ? ");
            ps.setLong(1, id);

            rs = ps.executeQuery();
            if (rs.next())
                result = rs.getLong("REC_ID");
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public Long getRefIdByRecIdBeginDate(String refName, Date date, Long recId) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Long result = -1L;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("select t.id " +
                    "	from " + refName + " t " +
                    " where t.delfl = 0 " +
                    "   and t.rec_id = ? " +
                    "   and t.begin_date = (select max(t1.begin_date) " +
                    "						 from " + refName + " t1 " +
                    "						where  t1.rec_id = t.rec_id " +
                    "						  and  t1.begin_date <= trunc(?)) " +
                    "	 and (t.end_date is null or t.end_date > trunc(?))");
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            ps.setLong(1, recId);
            ps.setDate(2, sqlDate);
            ps.setDate(3, sqlDate);
            rs = ps.executeQuery();
            if (rs.next())
                result = rs.getLong("ID");
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public String getRefOriginalValue(String refName, String refColumn, String refColumnValue, Date date) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String result = refColumnValue;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("select " + refColumn + " as col from " + refName + " where delfl = 0 and lower(" + refColumn + ") = lower(?) and begin_date <= trunc(?)  and (end_date is null or end_date > trunc(?)) group by " + refColumn);
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            ps.setString(1, refColumnValue);
            ps.setDate(2, sqlDate);
            ps.setDate(3, sqlDate);

            rs = ps.executeQuery();
            if (rs.next())
                result = rs.getString("COL");
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public String getRefItemNameByRecId(String refName, String refColumn, Long recId, Date date) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String result = "не найден в справочнике";
        try {
            connection = getConnection();
            ps = connection.prepareStatement("select " + refColumn + " as name " +
                    " from " + refName + " r " +
                    " where r.rec_id=? " +
                    " and r.begin_date=(select max(r2.begin_date) from " + refName + " r2 " +
                    " where r2.rec_id=r.rec_id and r2.begin_date<=?)" +
                    "   and (? < r.end_date or r.end_date is null)" +
                    " order by r.delfl");
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            ps.setLong(1, recId);
            ps.setDate(2, sqlDate);
            ps.setDate(3, sqlDate);

            rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getString("NAME");
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<? extends AbstractReference> getReferenceItemsByName(String refName, String refColumn, Date date) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<SimpleReference> result = new ArrayList<SimpleReference>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("select rec_id, code, name_ru from " + refName + " t where delfl = 0 and begin_date = (select max(begin_date) from " + refName +
                    " where delfl = 0 and rec_id = t.rec_id and begin_date <= trunc(?)) and (trunc(?) < end_date or end_date is null) order by " + refColumn);
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            ps.setDate(1, sqlDate);
            ps.setDate(2, sqlDate);
            rs = ps.executeQuery();
            while (rs.next()) {
                SimpleReference ref = new SimpleReference();
                ref.setRecId(rs.getLong("REC_ID"));
                ref.setCode(rs.getString("CODE"));
                ref.setNameRu(rs.getString("NAME_RU"));
                result.add(ref);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<? extends AbstractReference> getReferenceItemByName(String refName, String refColumn, Date date) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<SimpleReference> result = new ArrayList<SimpleReference>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("select " + refColumn + " from " + refName + " t where delfl = 0 and begin_date = (select max(begin_date) from " + refName +
                    " where delfl = 0 and rec_id = t.rec_id and begin_date <= trunc(?)) and (trunc(?) < end_date or end_date is null) order by " + refColumn);
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            ps.setDate(1, sqlDate);
            ps.setDate(2, sqlDate);
            rs = ps.executeQuery();
            while (rs.next()) {
                SimpleReference ref = new SimpleReference();
                if (refColumn.equalsIgnoreCase("rec_id"))
                    ref.setRecId(rs.getLong("REC_ID"));
                if (refColumn.equalsIgnoreCase("code"))
                    ref.setCode(rs.getString("CODE"));
                if (refColumn.equalsIgnoreCase("name_ru"))
                    ref.setNameRu(rs.getString("NAME_RU"));
                result.add(ref);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<InputSelectRecord> getReferenceItemsByNameViewModel(String refName, Date date, String refColumn, InputSelectViewModel vm) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<InputSelectRecord> result = new ArrayList<InputSelectRecord>();
        Map<String, InputSelectColumn> columns = new HashMap<String, InputSelectColumn>();
        List<String> selectFieldList = new ArrayList<String>();

        boolean recIdFieldFound = false;
        boolean refColumnFound = false;
        for (InputSelectColumn col : vm.getColumns()) {
            if (!selectFieldList.contains(col.getName())) {
                selectFieldList.add(col.getName());
            }
            columns.put(col.getName(), col);
            if (col.getName().equalsIgnoreCase("rec_id")) {
                recIdFieldFound = true;
            }
            if (col.getName().equalsIgnoreCase(refColumn)) {
                refColumnFound = true;
            }
        }
        if (!recIdFieldFound) {
            columns.put("rec_id", new InputSelectColumn("rec_id", ValueType.NUMBER_0));
            selectFieldList.add("rec_id");
        }
        if (!refColumnFound) {
            columns.put(refColumn, new InputSelectColumn(refColumn, ValueType.STRING));
            selectFieldList.add(refColumn);
        }

        String selectFields = "";
        for (String col : selectFieldList) {
            if (!selectFields.isEmpty()) {
                selectFields += ", ";
            }
            selectFields += col;
        }

        List<String> sortFields = new ArrayList<String>();
        String orderBy = "";
        if (vm.getSortFields() != null) {
            for (SortField f : vm.getSortFields()) {
                if (!sortFields.contains(f.name)) {
                    sortFields.add(f.name);
                    if (!orderBy.isEmpty()) {
                        orderBy += ", ";
                    }
                    orderBy += f.name;
                    if (f.desc != null && f.desc.booleanValue()) {
                        orderBy += " desc";
                    }
                }
            }
        }
        if (sortFields.size() == 0) {
            orderBy += "rec_id";
        }
        try {
            connection = getConnection();
            ps = connection.prepareStatement("select " + selectFields + " from " + refName + " t where delfl = 0 and begin_date = (select max(begin_date) from " + refName +
                    " where delfl = 0 and rec_id = t.rec_id and begin_date <= trunc(?)) and (trunc(?) < end_date or end_date is null) " +
                    " order by " + orderBy);
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            ps.setDate(1, sqlDate);
            ps.setDate(2, sqlDate);
            rs = ps.executeQuery();
            while (rs.next()) {
                InputSelectRecord record = new InputSelectRecord(rs.getLong("rec_id"));
                for (String fieldName : selectFieldList) {
                    ValueType vt = columns.get(fieldName).getValueType();
                    if (vt == null) { // для старых шаблонов
                        vt = ValueType.STRING;
                    }
                    Variant value = new Variant();
                    value.setValueType(vt);
                    switch (vt) {
                        case STRING:
                            value.setStrValue(rs.getString(fieldName));
                            break;
                        case BOOLEAN:
                            value.setBoolValue(rs.getBoolean(fieldName));
                            break;
                        case DATE:
                            value.setDateValue(rs.getDate(fieldName));
                            break;
                        case NUMBER_0:
                            value.setLngValue(rs.getLong(fieldName));
                            break;
                        case NUMBER_1:
                        case NUMBER_2:
                        case NUMBER_3:
                        case NUMBER_4:
                        case NUMBER_5:
                        case NUMBER_6:
                        case NUMBER_7:
                        case NUMBER_8:
                            value.setDblValue(rs.getDouble(fieldName));
                            break;
                        default:
                            throw new IllegalStateException(MessageFormat.format("Unknown ValueType {0}", vt.name()));
                    }
                    if (rs.wasNull()) {
                        value = null;
                    }
                    record.put(fieldName, value);
                }
                result.add(record);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<InputSelectRecord> getReferenceItemsByNameViewModelPage(String refName, Date date, InputSelectViewModel vm, Filters filters, int offset, int limit) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<InputSelectRecord> result = new ArrayList<InputSelectRecord>();
        List<String> selectFieldList = new ArrayList<String>();
        selectFieldList.add("rec_id");

        Map<String, InputSelectColumn> columns = new HashMap<String, InputSelectColumn>();

        for (InputSelectColumn col : vm.getColumns()) {
            if (!selectFieldList.contains(col.getName())) {
                selectFieldList.add(col.getName());
            }
            columns.put(col.getName(), col);
        }
        String selectFields = "";
        for (String col : selectFieldList) {
            if (!selectFields.isEmpty()) {
                selectFields += ", ";
            }
            selectFields += col;
        }

        List<String> sortFields = new ArrayList<String>();
        String orderBy = "";
        if (vm.getSortFields() != null) {
            for (SortField f : vm.getSortFields()) {
                if (!sortFields.contains(f.name)) {
                    sortFields.add(f.name);
                    if (!orderBy.isEmpty()) {
                        orderBy += ", ";
                    }
                    orderBy += f.name;
                    if (f.desc != null && f.desc.booleanValue()) {
                        orderBy += " desc";
                    }
                }
            }
        }
        if (sortFields.size() == 0) {
            orderBy += "rec_id";
        }
        String filterClause = "";
        if (filters != null
                && filters.getFilterValue() != null && !filters.getFilterValue().trim().isEmpty()
                && filters.getColumns() != null && filters.getColumns().size() > 0) {
            filterClause += "and (";
            String f = " like '%" + filters.getFilterValue().trim() + "%' ";
            for (int i = 0; i < filters.getColumns().size(); i++) {
                if (i > 0) filterClause += " OR";
                filterClause += " " + filters.getColumns().get(i) + f;
            }
            filterClause += ")";
        }

        try {
            connection = getConnection();
            ps = connection.prepareStatement("select " + selectFields + " from " + refName + " t where delfl = 0 and begin_date = (select max(begin_date) from " + refName +
                    " where delfl = 0 and rec_id = t.rec_id and begin_date <= trunc(?)) and (trunc(?) < end_date or end_date is null) " + filterClause + "\n" +
                    " order by " + orderBy + "\n" +
                    " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY"
            );
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            ps.setDate(1, sqlDate);
            ps.setDate(2, sqlDate);
            ps.setInt(3, offset);
            ps.setInt(4, limit);
            rs = ps.executeQuery();
            while (rs.next()) {
                InputSelectRecord record = new InputSelectRecord(rs.getLong("rec_id"));
                for (String fieldName : selectFieldList) {
                    ValueType vt = columns.get(fieldName).getValueType();
                    Variant value = new Variant();
                    value.setValueType(vt);
                    switch (vt) {
                        case STRING:
                            value.setStrValue(rs.getString(fieldName));
                            break;
                        case BOOLEAN:
                            value.setBoolValue(rs.getBoolean(fieldName));
                            break;
                        case DATE:
                            value.setDateValue(rs.getDate(fieldName));
                            break;
                        case NUMBER_0:
                            value.setLngValue(rs.getLong(fieldName));
                            break;
                        case NUMBER_1:
                        case NUMBER_2:
                        case NUMBER_3:
                        case NUMBER_4:
                        case NUMBER_5:
                        case NUMBER_6:
                        case NUMBER_7:
                        case NUMBER_8:
                            value.setDblValue(rs.getDouble(fieldName));
                            break;
                        default:
                            throw new IllegalStateException(MessageFormat.format("Unknown ValueType {0}", vt.name()));
                    }
                    if (rs.wasNull()) {
                        value = null;
                    }
                    record.put(fieldName, value);
                }
                result.add(record);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public int getReferenceItemsCount(String refName, Date date, Filters filters) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int result = 0;
        try {
            connection = getConnection();
            String filterClause = "";
            if (filters != null
                    && filters.getFilterValue() != null && !filters.getFilterValue().trim().isEmpty()
                    && filters.getColumns() != null && filters.getColumns().size() > 0) {
                filterClause += "and (";
                String f = " like '%" + filters.getFilterValue().trim() + "%' ";
                for (int i = 0; i < filters.getColumns().size(); i++) {
                    if (i > 0) filterClause += " OR";
                    filterClause += " " + filters.getColumns().get(i) + f;
                }
                filterClause += ")";
            }
            String sql = "select count(*) cnt from " + refName + " t where delfl = 0 and begin_date = (select max(begin_date) from " + refName +
                    " where delfl = 0 and rec_id = t.rec_id and begin_date <= trunc(?)) and (trunc(?) < end_date or end_date is null) " + filterClause;
            ps = connection.prepareStatement(sql);
            ps.setTimestamp(1, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
            rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getInt("cnt");
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }


    @Override
    public List<RefElements> getRefKndList() {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefElements> result = new ArrayList<RefElements>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT * from ref_knd where id not in (3,4)");
            rs = ps.executeQuery();
            while (rs.next()) {
                RefElements item = new RefElements();
                item.setId(rs.getLong("ID"));
                item.setName(rs.getString("NAME"));
                result.add(item);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    // region Универсальные методы для справочников

    private OcsNumMap getOcsNumMapByMethodName(Connection connection, String methodName) {
        OcsNumMap result = new OcsNumMap();
        try {
            result.setOcs(connection.prepareCall("{ call " + methodName + "}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).unwrap(oracle.jdbc.OracleCallableStatement.class));
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
        return result;
    }

    private RefHelperOut getRefHelperOut(RefHelperIn refHelperIn, RefHelperOut refHelperOut) {
        try {
            if (refHelperOut == null)
                refHelperOut = new RefHelperOut();
            // region ref_post
            if (refHelperIn.getRefCode().equalsIgnoreCase(RefPostItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPostItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefPostItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefPostItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPostItem.READ_BY_FILTER), (RefPostItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefPostItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPostItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefPostItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefPostItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPostItem.INSERT), (RefPostItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefPostItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPostItem.UPDATE), (RefPostItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPostItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            // region ref_type_activity
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefTypeActivityItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeActivityItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefTypeActivityItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefTypeActivityItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeActivityItem.READ_BY_FILTER), (RefTypeActivityItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefTypeActivityItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeActivityItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefTypeActivityItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefTypeActivityItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeActivityItem.INSERT), (RefTypeActivityItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefTypeActivityItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeActivityItem.UPDATE), (RefTypeActivityItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeActivityItem.DELETE), refHelperIn.getId()));
                }
            } // endregion
            // region ref_npa
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefNpaItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefNpaItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefNpaItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefNpaItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefNpaItem.READ_BY_FILTER), (RefNpaItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefNpaItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefNpaItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefNpaItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefNpaItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefNpaItem.INSERT), (RefNpaItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefNpaItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefNpaItem.UPDATE), (RefNpaItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefNpaItem.DELETE), refHelperIn.getId()));
                }
            } // endregion
            // region ref_wkd_holidays
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefWkdHolidayItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefWkdHolidayItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefWkdHolidayItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefWkdHolidayItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefWkdHolidayItem.READ_BY_FILTER), (RefWkdHolidayItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefWkdHolidayItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefWkdHolidayItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefWkdHolidayItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefWkdHolidayItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefWkdHolidayItem.INSERT), (RefWkdHolidayItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefWkdHolidayItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefWkdHolidayItem.UPDATE), (RefWkdHolidayItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefWkdHolidayItem.DELETE), refHelperIn.getId()));
                }
            } // endregion
            // region ref_basisofcontrol
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefBasisofControlItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBasisofControlItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefBasisofControlItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefBasisofControlItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBasisofControlItem.READ_BY_FILTER), (RefBasisofControlItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefBasisofControlItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBasisofControlItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefBasisofControlItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefBasisofControlItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBasisofControlItem.INSERT), (RefBasisofControlItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefBasisofControlItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBasisofControlItem.UPDATE), (RefBasisofControlItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBasisofControlItem.DELETE), refHelperIn.getId()));
                }
            } // endregion
            // region ref_person
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefPersonItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPersonItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefPersonItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefPersonItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPersonItem.READ_BY_FILTER), (RefPersonItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefPersonItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPersonItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefPersonItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefPersonItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPersonItem.INSERT), (RefPersonItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefPersonItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPersonItem.UPDATE), (RefPersonItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPersonItem.DELETE), refHelperIn.getId()));
                }
            } // endregion
            // region ref_legal_person
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefLegalPersonItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefLegalPersonItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefLegalPersonItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefLegalPersonItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefLegalPersonItem.READ_BY_FILTER), (RefLegalPersonItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefLegalPersonItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefLegalPersonItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefLegalPersonItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefLegalPersonItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefLegalPersonItem.INSERT), (RefLegalPersonItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefLegalPersonItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefLegalPersonItem.UPDATE), (RefLegalPersonItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefLegalPersonItem.DELETE), refHelperIn.getId()));
                }
            } // endregion
            // region ref_country
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefCountryItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefCountryItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefCountryItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefCountryItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefCountryItem.READ_BY_FILTER), (RefCountryItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefCountryItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                }
            } // endregion
            // region ref_managers
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefManagersItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefManagersItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefManagersItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefManagersItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefManagersItem.READ_BY_FILTER), (RefManagersItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefManagersItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefManagersItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefManagersItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefManagersItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefManagersItem.INSERT), (RefManagersItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefManagersItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefManagersItem.UPDATE), (RefManagersItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefManagersItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            // region ref_type_bus_entity
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefTypeBusEntityItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeBusEntityItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefTypeBusEntityItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefTypeBusEntityItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeBusEntityItem.READ_BY_FILTER), (RefTypeBusEntityItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefTypeBusEntityItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeBusEntityItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefTypeBusEntityItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefTypeBusEntityItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeBusEntityItem.INSERT), (RefTypeBusEntityItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefTypeBusEntityItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeBusEntityItem.UPDATE), (RefTypeBusEntityItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeBusEntityItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            // region ref_region
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefRegionItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRegionItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefRegionItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefRegionItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRegionItem.READ_BY_FILTER), (RefRegionItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefRegionItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                }
            } // endregion
            // region ref_requirement
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefRequirementItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRequirementItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefRequirementItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefRequirementItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRequirementItem.READ_BY_FILTER), (RefRequirementItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefRequirementItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRequirementItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefRequirementItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefRequirementItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRequirementItem.INSERT), (RefRequirementItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefRequirementItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRequirementItem.UPDATE), (RefRequirementItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRequirementItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            // region ref_type_provide
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefTypeProvideItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeProvideItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefTypeProvideItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefTypeProvideItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeProvideItem.READ_BY_FILTER), (RefTypeProvideItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefTypeProvideItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeProvideItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefTypeProvideItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefTypeProvideItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeProvideItem.INSERT), (RefTypeProvideItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefTypeProvideItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeProvideItem.UPDATE), (RefTypeProvideItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTypeProvideItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            // region ref_trans_types
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefTransTypeItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTransTypeItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefTransTypeItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefTransTypeItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTransTypeItem.READ_BY_FILTER), (RefTransTypeItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefTransTypeItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTransTypeItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefTransTypeItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefTransTypeItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTransTypeItem.INSERT), (RefTransTypeItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefTransTypeItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTransTypeItem.UPDATE), (RefTransTypeItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefTransTypeItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            // region ref_balance_account
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefBalanceAccItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBalanceAccItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefBalanceAccItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefBalanceAccItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBalanceAccItem.READ_BY_FILTER), (RefBalanceAccItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefBalanceAccItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBalanceAccItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefBalanceAccItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefBalanceAccItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBalanceAccItem.INSERT), (RefBalanceAccItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefBalanceAccItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBalanceAccItem.UPDATE), (RefBalanceAccItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBalanceAccItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            // region ref_conn_org
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefConnOrgItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefConnOrgItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefConnOrgItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefConnOrgItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefConnOrgItem.READ_BY_FILTER), (RefConnOrgItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefConnOrgItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefConnOrgItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefConnOrgItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefConnOrgItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefConnOrgItem.INSERT), (RefConnOrgItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefConnOrgItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefConnOrgItem.UPDATE), (RefConnOrgItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefConnOrgItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            // region ref_department
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefDepartmentItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDepartmentItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefDepartmentItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefDepartmentItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDepartmentItem.READ_BY_FILTER), (RefDepartmentItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefDepartmentItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                }
            } //endregion
            // region ref_bank
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefBankItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBankItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefBankItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefBankItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBankItem.READ_BY_FILTER), (RefBankItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefBankItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBankItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefBankItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefBankItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBankItem.INSERT), (RefBankItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefBankItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBankItem.UPDATE), (RefBankItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBankItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            // region ref_rate_agency
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefRateAgencyItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRateAgencyItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefRateAgencyItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefRateAgencyItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRateAgencyItem.READ_BY_FILTER), (RefRateAgencyItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefRateAgencyItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                }
            } // endregion
            // region ref_currency
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefCurrencyItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefCurrencyItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefCurrencyItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefCurrencyItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefCurrencyItem.READ_BY_FILTER), (RefCurrencyItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefCurrencyItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                }
            } // endregion
            // region ref_currency_rate
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefCurrencyRateItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefCurrencyRateItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefCurrencyRateItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefCurrencyRateItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefCurrencyRateItem.READ_BY_FILTER), (RefCurrencyRateItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefCurrencyRateItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                }
            } // endregion
            // region ref_subject_type
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefSubjectTypeItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefSubjectTypeItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefSubjectTypeItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefSubjectTypeItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefSubjectTypeItem.READ_BY_FILTER), (RefSubjectTypeItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefSubjectTypeItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefSubjectTypeItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefSubjectTypeItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefSubjectTypeItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefSubjectTypeItem.INSERT), (RefSubjectTypeItem) refHelperIn.getItem()));

                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefSubjectTypeItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefSubjectTypeItem.UPDATE), (RefSubjectTypeItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefSubjectTypeItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            // region ref_respondent
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefRespondentItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRespondentItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefRespondentItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefRespondentItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRespondentItem.READ_BY_FILTER), (RefRespondentItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefRespondentItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRespondentItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefRespondentItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefRespondentItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRespondentItem.INSERT), (RefRespondentItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefRespondentItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRespondentItem.UPDATE), (RefRespondentItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRespondentItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            // region ref_doc_type
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefDocTypeItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDocTypeItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefDocTypeItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefDocTypeItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDocTypeItem.READ_BY_FILTER), (RefDocTypeItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefDocTypeItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDocTypeItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefDocTypeItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefDocTypeItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDocTypeItem.INSERT), (RefDocTypeItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefDocTypeItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDocTypeItem.UPDATE), (RefDocTypeItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDocTypeItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            // region ref_document
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefDocumentItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDocumentItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefDocumentItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefDocumentItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDocumentItem.READ_BY_FILTER), (RefDocumentItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefDocumentItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDocumentItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefDocumentItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefDocumentItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDocumentItem.INSERT), (RefDocumentItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefDocumentItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDocumentItem.UPDATE), (RefDocumentItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDocumentItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            // region ref_issuers
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefIssuersItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefIssuersItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefIssuersItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefIssuersItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefIssuersItem.READ_BY_FILTER), (RefIssuersItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefIssuersItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                }
            } // endregion
            // region ref_securities
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefSecuritiesItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefSecuritiesItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefSecuritiesItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefSecuritiesItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefSecuritiesItem.READ_BY_FILTER), (RefSecuritiesItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefSecuritiesItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                }
            } // endregion
            // region ref_vid_oper
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefVidOperItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefVidOperItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefVidOperItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefVidOperItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefVidOperItem.READ_BY_FILTER), (RefVidOperItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefVidOperItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefVidOperItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefVidOperItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefVidOperItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefVidOperItem.INSERT), (RefVidOperItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefVidOperItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefVidOperItem.UPDATE), (RefVidOperItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefVidOperItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            // region ref_branch
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefBranchItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBranchItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefBranchItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefBranchItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBranchItem.READ_BY_FILTER), (RefBranchItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefBranchItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                }
            } //endregion
            // region ref_listing_estimation
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefListingEstimationItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefListingEstimationItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefListingEstimationItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefListingEstimationItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefListingEstimationItem.READ_BY_FILTER), (RefListingEstimationItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefListingEstimationItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                }
            } // endregion
            // region ref_rating_estimation
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefRatingEstimationItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRatingEstimationItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefRatingEstimationItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefRatingEstimationItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRatingEstimationItem.READ_BY_FILTER), (RefRatingEstimationItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefRatingEstimationItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                }
            } // endregion
            // region ref_rating_category
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefRatingCategoryItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRatingCategoryItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefRatingCategoryItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefRatingCategoryItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefRatingCategoryItem.READ_BY_FILTER), (RefRatingCategoryItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefRatingCategoryItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                }
            } // endregion
            // region ref_mrp
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefMrpItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefMrpItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMrpItem.READ_BY_FILTER), (RefMrpItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefMrpItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMrpItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefMrpItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefMrpItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMrpItem.INSERT), (RefMrpItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefMrpItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMrpItem.UPDATE), (RefMrpItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMrpItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            // region ref_mfo_reg
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefMfoRegItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMfoRegItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefMfoRegItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefMfoRegItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMfoRegItem.READ_BY_FILTER), (RefMfoRegItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefMfoRegItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMfoRegItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefMfoRegItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefMfoRegItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMfoRegItem.INSERT), (RefMfoRegItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefMfoRegItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMfoRegItem.UPDATE), (RefMfoRegItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMfoRegItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            // region ref_deal_balance_acc
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefDealBAItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDealBAItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefDealBAItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefDealBAItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDealBAItem.READ_BY_FILTER), (RefDealBAItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefDealBAItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDealBAItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefDealBAItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefDealBAItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDealBAItem.INSERT), (RefDealBAItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefDealBAItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDealBAItem.UPDATE), (RefDealBAItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefDealBAItem.DELETE), refHelperIn.getId()));
                }
            } //endregion
            //region ref_insur_org
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefInsurOrgItem.REF_CODE)) {
                  if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                      if (refHelperIn.getCursor() == null) {
                          refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefInsurOrgItem.READ), refHelperIn.getDate()));
                      } else {
                          refHelperOut.setItem(RefInsurOrgItem.setItemFromCursor(refHelperIn.getCursor()));
                      }
                  } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                      if (refHelperIn.getCursor() == null) {
                          refHelperOut.setOcsNumMap(RefInsurOrgItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefInsurOrgItem.READ_BY_FILTER), (RefInsurOrgItem) refHelperIn.getItem(), refHelperIn.getDate()));
                      } else {
                          refHelperOut.setItem(RefInsurOrgItem.setItemFromCursor(refHelperIn.getCursor()));
                      }
                  }
              }
            //endregion
            // region ref_extind
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefExtIndicatorItem.REF_CODE)) {
                  if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                      if (refHelperIn.getCursor() == null) {
                          refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefExtIndicatorItem.READ), refHelperIn.getDate()));
                      } else {
                          refHelperOut.setItem(RefExtIndicatorItem.setItemFromCursor(refHelperIn.getCursor()));
                      }
                  } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                      if (refHelperIn.getCursor() == null) {
                          refHelperOut.setOcsNumMap(RefExtIndicatorItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefExtIndicatorItem.READ_BY_FILTER), (RefExtIndicatorItem) refHelperIn.getItem(), refHelperIn.getDate()));
                      } else {
                          refHelperOut.setItem(RefExtIndicatorItem.setItemFromCursor(refHelperIn.getCursor()));
                      }
                  } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                      if (refHelperIn.getCursor() == null) {
                          refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefExtIndicatorItem.READ_HST), refHelperIn.getId()));
                      } else {
                          refHelperOut.setItem(RefExtIndicatorItem.setItemHstFromCursor(refHelperIn.getCursor()));
                      }
                  } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                      refHelperOut.setOcsNumMap(RefExtIndicatorItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefExtIndicatorItem.INSERT), (RefExtIndicatorItem) refHelperIn.getItem()));
                  } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                      refHelperOut.setOcsNumMap(RefExtIndicatorItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefExtIndicatorItem.UPDATE), (RefExtIndicatorItem) refHelperIn.getItem()));
                  } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                      refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefExtIndicatorItem.DELETE), refHelperIn.getId()));
                  }
            }
            //endregion
            // region ref_major_member
            else  if (refHelperIn.getRefCode().equalsIgnoreCase(RefMajorMemberItem.REF_CODE)) {
               if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                   if (refHelperIn.getCursor() == null) {
                       refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemberItem.READ), refHelperIn.getDate()));
                   } else {
                       refHelperOut.setItem(RefMajorMemberItem.setItemFromCursor(refHelperIn.getCursor()));
                   }
               } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                   if (refHelperIn.getCursor() == null) {
                       refHelperOut.setOcsNumMap(RefMajorMemberItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemberItem.READ_BY_FILTER), (RefMajorMemberItem) refHelperIn.getItem(), refHelperIn.getDate()));
                   } else {
                       refHelperOut.setItem(RefMajorMemberItem.setItemFromCursor(refHelperIn.getCursor()));
                   }
               } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                   if (refHelperIn.getCursor() == null) {
                       refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemberItem.READ_HST), refHelperIn.getId()));
                   } else {
                       refHelperOut.setItem(RefMajorMemberItem.setItemHstFromCursor(refHelperIn.getCursor()));
                   }
               } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                   refHelperOut.setOcsNumMap(RefMajorMemberItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemberItem.INSERT), (RefMajorMemberItem) refHelperIn.getItem()));
               } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                   refHelperOut.setOcsNumMap(RefMajorMemberItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemberItem.UPDATE), (RefMajorMemberItem) refHelperIn.getItem()));
               } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                   refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemberItem.DELETE), refHelperIn.getId()));
               }
           } // endregion
            // region ref_major_memberorgs
           else  if (refHelperIn.getRefCode().equalsIgnoreCase(RefMajorMemberOrgItem.REF_CODE)) {
              if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                  if (refHelperIn.getCursor() == null) {
                      refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemberOrgItem.READ), refHelperIn.getDate()));
                  } else {
                      refHelperOut.setItem(RefMajorMemberOrgItem.setItemFromCursor(refHelperIn.getCursor()));
                  }
              } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                  if (refHelperIn.getCursor() == null) {
                      refHelperOut.setOcsNumMap(RefMajorMemberOrgItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemberOrgItem.READ_BY_FILTER), (RefMajorMemberOrgItem) refHelperIn.getItem(), refHelperIn.getDate()));
                  } else {
                      refHelperOut.setItem(RefMajorMemberOrgItem.setItemFromCursor(refHelperIn.getCursor()));
                  }
              } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                  if (refHelperIn.getCursor() == null) {
                      refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemberOrgItem.READ_HST), refHelperIn.getId()));
                  } else {
                      refHelperOut.setItem(RefMajorMemberOrgItem.setItemHstFromCursor(refHelperIn.getCursor()));
                  }
              } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                  refHelperOut.setOcsNumMap(RefMajorMemberOrgItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemberOrgItem.INSERT), (RefMajorMemberOrgItem) refHelperIn.getItem()));
              } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                  refHelperOut.setOcsNumMap(RefMajorMemberOrgItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemberOrgItem.UPDATE), (RefMajorMemberOrgItem) refHelperIn.getItem()));
              } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                  refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemberOrgItem.DELETE), refHelperIn.getId()));
              }
          } // endregion
            // region ref_major_memdetails
           else  if (refHelperIn.getRefCode().equalsIgnoreCase(RefMajorMemDetailsItem.REF_CODE)) {
              if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                  if (refHelperIn.getCursor() == null) {
                      refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemDetailsItem.READ), refHelperIn.getDate()));
                  } else {
                      refHelperOut.setItem(RefMajorMemDetailsItem.setItemFromCursor(refHelperIn.getCursor()));
                  }
              } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                  if (refHelperIn.getCursor() == null) {
                      refHelperOut.setOcsNumMap(RefMajorMemDetailsItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemDetailsItem.READ_BY_FILTER), (RefMajorMemDetailsItem) refHelperIn.getItem(), refHelperIn.getDate()));
                  } else {
                      refHelperOut.setItem(RefMajorMemDetailsItem.setItemFromCursor(refHelperIn.getCursor()));
                  }
              } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                  if (refHelperIn.getCursor() == null) {
                      refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemDetailsItem.READ_HST), refHelperIn.getId()));
                  } else {
                      refHelperOut.setItem(RefMajorMemDetailsItem.setItemHstFromCursor(refHelperIn.getCursor()));
                  }
              } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                  refHelperOut.setOcsNumMap(RefMajorMemDetailsItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemDetailsItem.INSERT), (RefMajorMemDetailsItem) refHelperIn.getItem()));
              } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                  refHelperOut.setOcsNumMap(RefMajorMemDetailsItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemDetailsItem.UPDATE), (RefMajorMemDetailsItem) refHelperIn.getItem()));
              } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                  refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefMajorMemDetailsItem.DELETE), refHelperIn.getId()));
              }
          } // endregion

            // region ref_period_alg
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefPeriodAlgItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPeriodAlgItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefPeriodAlgItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefPeriodAlgItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPeriodAlgItem.READ_BY_FILTER), (RefPeriodAlgItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefPeriodAlgItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPeriodAlgItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefPeriodAlgItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefPeriodAlgItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPeriodAlgItem.INSERT), (RefPeriodAlgItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefPeriodAlgItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPeriodAlgItem.UPDATE), (RefPeriodAlgItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPeriodAlgItem.DELETE), refHelperIn.getId()));
                }
            }
            // endregion
            // region ref_period_alg
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefPeriodItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPeriodItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefPeriodItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefPeriodItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPeriodItem.READ_BY_FILTER), (RefPeriodItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefPeriodItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPeriodItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefPeriodItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefPeriodItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPeriodItem.INSERT), (RefPeriodItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefPeriodItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPeriodItem.UPDATE), (RefPeriodItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefPeriodItem.DELETE), refHelperIn.getId()));
                }
            }
            // endregion
            // region ref_insur_groups
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefInsurGroupsItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefInsurGroupsItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefInsurGroupsItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefInsurGroupsItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefInsurGroupsItem.READ_BY_FILTER), (RefInsurGroupsItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefInsurGroupsItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefInsurGroupsItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefInsurGroupsItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefInsurGroupsItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefInsurGroupsItem.INSERT), (RefInsurGroupsItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefInsurGroupsItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefInsurGroupsItem.UPDATE), (RefInsurGroupsItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefInsurGroupsItem.DELETE), refHelperIn.getId()));
                }
            }
            // endregion
            // region ref_bank_conglomerates
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefBankConglomeratesItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBankConglomeratesItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefBankConglomeratesItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefBankConglomeratesItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBankConglomeratesItem.READ_BY_FILTER), (RefBankConglomeratesItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefBankConglomeratesItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBankConglomeratesItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefBankConglomeratesItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefBankConglomeratesItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBankConglomeratesItem.INSERT), (RefBankConglomeratesItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefBankConglomeratesItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBankConglomeratesItem.UPDATE), (RefBankConglomeratesItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefBankConglomeratesItem.DELETE), refHelperIn.getId()));
                }
            }
            // endregion
            // region ref_share_holders
            else if (refHelperIn.getRefCode().equalsIgnoreCase(RefShareHoldersItem.REF_CODE)) {
                if (refHelperIn.getTypeAction().equalsIgnoreCase("read")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByDateForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefShareHoldersItem.READ), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefShareHoldersItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_by_filter")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(RefShareHoldersItem.setOcsNumMapByFilters(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefShareHoldersItem.READ_BY_FILTER), (RefShareHoldersItem) refHelperIn.getItem(), refHelperIn.getDate()));
                    } else {
                        refHelperOut.setItem(RefShareHoldersItem.setItemFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("read_hst")) {
                    if (refHelperIn.getCursor() == null) {
                        refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapByIdForList(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefShareHoldersItem.READ_HST), refHelperIn.getId()));
                    } else {
                        refHelperOut.setItem(RefShareHoldersItem.setItemHstFromCursor(refHelperIn.getCursor()));
                    }
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("insert")) {
                    refHelperOut.setOcsNumMap(RefShareHoldersItem.setOcsNumMapForIns(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefShareHoldersItem.INSERT), (RefShareHoldersItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("update")) {
                    refHelperOut.setOcsNumMap(RefShareHoldersItem.setOcsNumMapForUpd(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefShareHoldersItem.UPDATE), (RefShareHoldersItem) refHelperIn.getItem()));
                } else if (refHelperIn.getTypeAction().equalsIgnoreCase("delete")) {
                    refHelperOut.setOcsNumMap(AbstractReference.setOcsNumMapForDel(getOcsNumMapByMethodName(refHelperIn.getConnection(), RefShareHoldersItem.DELETE), refHelperIn.getId()));
                }
            }
            // endregion
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
        return refHelperOut;
    }

    // Универсальные методы для справочника
    @Override
    public List<? extends AbstractReference> getRefAbstractList(String refCode, Date date) {
        int ErrCode = 0;
        String ErrMsg = "";
        List<AbstractReference> result = new ArrayList<AbstractReference>();

        Connection connection = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;

        RefHelperIn refHelperIn = new RefHelperIn();
        refHelperIn.setRefCode(refCode);
        refHelperIn.setTypeAction("read");
        refHelperIn.setDate(date);

        try {
            connection = getConnection();
            refHelperIn.setConnection(connection);

            RefHelperOut refHelperOut = getRefHelperOut(refHelperIn, null);

            ocs = refHelperOut.getOcsNumMap().getOcs();
            ocs.execute();

            ErrCode = ocs.getInt(refHelperOut.getOcsNumMap().getNumMap().get("err_code"));
            ErrMsg = ocs.getString(refHelperOut.getOcsNumMap().getNumMap().get("err_msg"));
            if (ErrCode != 0) throw new SQLException(ErrMsg);

            cursor = ocs.getCursor(refHelperOut.getOcsNumMap().getNumMap().get("cursor"));
            refHelperIn.setCursor(cursor);

            while (cursor.next()) {
                refHelperOut = getRefHelperOut(refHelperIn, refHelperOut);
                result.add(refHelperOut.getItem());
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, null, cursor, ocs);
        }
        return result;
    }

    @Override
    public List<? extends AbstractReference> getRefAbstractByFilterList(String refCode, AbstractReference filter, Date date) {
        int ErrCode = 0;
        String ErrMsg = "";
        List<AbstractReference> result = new ArrayList<AbstractReference>();

        Connection connection = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;

        RefHelperIn refHelperIn = new RefHelperIn();
        refHelperIn.setRefCode(refCode);
        refHelperIn.setTypeAction("read_by_filter");
        refHelperIn.setDate(date);
        refHelperIn.setItem(filter);

        try {
            connection = getConnection();
            refHelperIn.setConnection(connection);

            RefHelperOut refHelperOut = getRefHelperOut(refHelperIn, null);

            ocs = refHelperOut.getOcsNumMap().getOcs();
            ocs.execute();

            ErrCode = ocs.getInt(refHelperOut.getOcsNumMap().getNumMap().get("err_code"));
            ErrMsg = ocs.getString(refHelperOut.getOcsNumMap().getNumMap().get("err_msg"));
            if (ErrCode != 0) throw new SQLException(ErrMsg);

            cursor = ocs.getCursor(refHelperOut.getOcsNumMap().getNumMap().get("cursor"));
            refHelperIn.setCursor(cursor);

            while (cursor.next()) {
                refHelperOut = getRefHelperOut(refHelperIn, refHelperOut);
                result.add(refHelperOut.getItem());
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, null, cursor, ocs);
        }
        return result;
    }

    @Override
    public List<? extends AbstractReference> getRefAbstractHstList(String refCode, Long id) {
        int ErrCode = 0;
        String ErrMsg = "";
        List<AbstractReference> result = new ArrayList<AbstractReference>();

        Connection connection = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;

        RefHelperIn refHelperIn = new RefHelperIn();
        refHelperIn.setRefCode(refCode);
        refHelperIn.setTypeAction("read_hst");
        refHelperIn.setId(id);

        try {
            connection = getConnection();
            refHelperIn.setConnection(connection);

            RefHelperOut refHelperOut = getRefHelperOut(refHelperIn, null);

            ocs = refHelperOut.getOcsNumMap().getOcs();
            ocs.execute();

            ErrCode = ocs.getInt(refHelperOut.getOcsNumMap().getNumMap().get("err_code"));
            ErrMsg = ocs.getString(refHelperOut.getOcsNumMap().getNumMap().get("err_msg"));
            if (ErrCode != 0) throw new SQLException(ErrMsg);

            cursor = ocs.getCursor(refHelperOut.getOcsNumMap().getNumMap().get("cursor"));
            refHelperIn.setCursor(cursor);

            while (cursor.next()) {
                refHelperOut = getRefHelperOut(refHelperIn, refHelperOut);
                result.add(refHelperOut.getItem());
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, null, cursor, ocs);
        }
        return result;
    }

    @Override
    public AbstractReference getRefAbstractItem(String refCode, AbstractReference filter) {
        return getRefAbstractByFilterList(refCode, filter, null).get(0);
    }

    @Override
    public Long insertRefAbstractItem(String refCode, AbstractReference item, AuditEvent auditEvent) throws OracleException {
        int ErrCode = 0;
        String ErrMsg = " ";
        Long result = null;

        Connection connection = null;
        OracleCallableStatement ocs = null;

        RefHelperIn refHelperIn = new RefHelperIn();
        refHelperIn.setRefCode(refCode);
        refHelperIn.setTypeAction("insert");
        refHelperIn.setItem(item);

        try {
            connection = getConnection();
            refHelperIn.setConnection(connection);

            RefHelperOut refHelperOut = getRefHelperOut(refHelperIn, null);

            ocs = refHelperOut.getOcsNumMap().getOcs();
            ocs.execute();

            ErrCode = ocs.getInt(refHelperOut.getOcsNumMap().getNumMap().get("err_code"));
            ErrMsg = ocs.getString(refHelperOut.getOcsNumMap().getNumMap().get("err_msg"));
            if (ErrCode != 0) throw new OracleException(ErrMsg);

            result = ocs.getLong(refHelperOut.getOcsNumMap().getNumMap().get("id"));

            auditEvent.setDateIn(item.getBeginDate());
            auditEvent.setRecId(getRefRecIdById(refCode, result));
            insertAuditEvent(auditEvent, connection);

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, null, null, ocs);
        }
        return result;
    }

    @Override
    public void updateRefAbstractItem(String refCode, AbstractReference item, AuditEvent auditEvent) throws OracleException {
        int ErrCode = 0;
        String ErrMsg = " ";

        Connection connection = null;
        OracleCallableStatement ocs = null;

        RefHelperIn refHelperIn = new RefHelperIn();
        refHelperIn.setRefCode(refCode);
        refHelperIn.setTypeAction("update");
        refHelperIn.setItem(item);

        try {
            connection = getConnection();
            refHelperIn.setConnection(connection);

            RefHelperOut refHelperOut = getRefHelperOut(refHelperIn, null);

            ocs = refHelperOut.getOcsNumMap().getOcs();
            ocs.execute();

            ErrCode = ocs.getInt(refHelperOut.getOcsNumMap().getNumMap().get("err_code"));
            ErrMsg = ocs.getString(refHelperOut.getOcsNumMap().getNumMap().get("err_msg"));
            if (ErrCode != 0) throw new OracleException(ErrMsg);

            auditEvent.setDateIn(item.getBeginDate());
            auditEvent.setRecId(item.getRecId());
            insertAuditEvent(auditEvent, connection);

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, null, null, ocs);
        }
    }

    @Override
    public void deleteRefAbstractItem(String refCode, Long id, AuditEvent auditEvent) throws OracleException {
        int ErrCode = 0;
        String ErrMsg = " ";

        Connection connection = null;
        OracleCallableStatement ocs = null;

        RefHelperIn refHelperIn = new RefHelperIn();
        refHelperIn.setRefCode(refCode);
        refHelperIn.setTypeAction("delete");
        refHelperIn.setId(id);

        try {
            connection = getConnection();
            refHelperIn.setConnection(connection);

            RefHelperOut refHelperOut = getRefHelperOut(refHelperIn, null);

            ocs = refHelperOut.getOcsNumMap().getOcs();
            ocs.execute();

            ErrCode = ocs.getInt(refHelperOut.getOcsNumMap().getNumMap().get("err_code"));
            ErrMsg = ocs.getString(refHelperOut.getOcsNumMap().getNumMap().get("err_msg"));
            if (ErrCode != 0) throw new OracleException(ErrMsg);

            auditEvent.setRecId(getRefRecIdById(refCode, id));
            insertAuditEvent(auditEvent, connection);

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, null, null, ocs);
        }
    }

    // Справочник должностей
    @Override
    public PostType getPostType(Long id) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        PostType result = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT * from type_post t where t.id = :id");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                result = new PostType();
                result.setId(rs.getLong("ID"));
                result.setName(rs.getString("NAME"));
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefPostItem> getRefPosts(Long typePost, Date date) {
        Connection connection = null;
        PreparedStatement ps = null;
        List<RefPostItem> result = new ArrayList<RefPostItem>();

        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_READ_SIMPLE_POST_LIS (?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            if (typePost == null)
                ocs.setNull(1, OracleTypes.NULL);
            else
                ocs.setLong(1, typePost);
            if (date == null) {
                ocs.setNull(2, OracleTypes.NULL);
            } else
                ocs.setDate(2, new java.sql.Date(date.getTime()));

            ocs.registerOutParameter(3, OracleTypes.CURSOR);
            ocs.registerOutParameter(4, OracleTypes.INTEGER);
            ocs.registerOutParameter(5, OracleTypes.VARCHAR);
            ocs.execute();

            cursor = ocs.getCursor(3);

            while (cursor.next()) {
                result.add(RefPostItem.setItemFromCursor(cursor));
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, cursor, ocs);
        }
        return result;
    }

    // справочник юридических лиц
    @Override
    public List<RefLegalPersonItem> getLegalPersons(Date date) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefLegalPersonItem> result = new ArrayList<RefLegalPersonItem>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT * FROM v_ref_legal_person t WHERE begin_date = (SELECT MAX(begin_date) FROM v_ref_legal_person WHERE rec_id = t.rec_id AND begin_date <= ?) AND (? < end_date or end_date is null) ORDER BY name_ru");
            ps.setTimestamp(1, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
            rs = ps.executeQuery();
            while (rs.next()) {
                RefLegalPersonItem legalPerson = getLegalPersonFromResultSet(rs);
                result.add(legalPerson);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefLegalPersonItem> getLegalPersonsByFilters(Date date, String bin, String name) {
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        List<RefLegalPersonItem> result = new ArrayList<RefLegalPersonItem>();
        try {
            connection = getConnection();
            StringBuilder sbQuery = new StringBuilder();
            sbQuery.append("SELECT * FROM v_ref_legal_person t WHERE 1=1");
            if (date != null) {
//                String toDate = core.getOracleDate(date);
                String toDate = getOracleDate(date);

                sbQuery.append(" AND begin_date = (SELECT MAX(begin_date) FROM v_ref_legal_person WHERE rec_id = t.rec_id AND begin_date <= " + toDate + ")");
                sbQuery.append(" AND (" + toDate + " < end_date or end_date is null)");
            }
            if (bin != null && !bin.trim().isEmpty()) sbQuery.append(" AND idn LIKE '%" + bin + "%'");
            if (name != null && !name.trim().isEmpty())
                sbQuery.append(" AND LOWER(name_ru) LIKE LOWER('%" + name + "%')");
            sbQuery.append(" ORDER BY name_ru");
            String query = sbQuery.toString();
            statement = connection.createStatement();
            rs = statement.executeQuery(query);
            while (rs.next()) {
                RefLegalPersonItem legalPerson = getLegalPersonFromResultSet(rs);
                result.add(legalPerson);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, statement, rs);
        }
        return result;
    }

    @Override
    public List<RefLegalPersonItem> getLegalPersonsByBin(Date date, String bin) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefLegalPersonItem> result = new ArrayList<RefLegalPersonItem>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT * FROM v_ref_legal_person t WHERE begin_date = (SELECT MAX(begin_date) FROM v_ref_legal_person WHERE rec_id = t.rec_id AND begin_date <= ?) AND (? < end_date or end_date is null) AND idn = ?");
            ps.setTimestamp(1, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setString(3, bin);
            rs = ps.executeQuery();
            while (rs.next()) {
                RefLegalPersonItem legalPerson = getLegalPersonFromResultSet(rs);
                result.add(legalPerson);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefLegalPersonItem> getRefLegalPersonsByFilterPage(Date date_, RefLegalPersonItem filter, int offset, int limit) {
        int Err_Code = 0;
        String Err_Msg = "";

        List<RefLegalPersonItem> resultList = new ArrayList<RefLegalPersonItem>();
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet rs = null;
        Connection connection = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call TMP_PKG_FRSI_REF.ref_read_lp_list_by_page(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);

            ocs.setDate(1, new java.sql.Date(date_.getTime()));

            if (filter == null || filter.getNameRu() == null) {
                ocs.setNull(2, OracleTypes.NULL);
            } else
                ocs.setString(2, filter.getNameRu());

            if (filter == null || filter.getIdn() == null) {
                ocs.setNull(3, OracleTypes.NULL);
            } else
                ocs.setString(3, filter.getIdn());

            if (filter == null || filter.getRefOrgType() == null || filter.getRefOrgType() == 0)
                ocs.setNull(4, OracleTypes.NULL);
            else
                ocs.setLong(4, filter.getRefOrgType());
            if (filter == null || filter.getRefOrgTypeRecId() == null || filter.getRefOrgTypeRecId() == 0)
                ocs.setNull(5, OracleTypes.NULL);
            else
                ocs.setLong(5, filter.getRefOrgTypeRecId());
            ocs.setInt(6, offset);
            ocs.setInt(7, limit);
            ocs.registerOutParameter(8, OracleTypes.CURSOR);
            ocs.registerOutParameter(9, OracleTypes.INTEGER);
            ocs.registerOutParameter(10, OracleTypes.VARCHAR);
            ocs.execute();

            // получение курсора
            rs = ocs.getCursor(8);

            while (rs.next()) {
                RefLegalPersonItem item = RefLegalPersonItem.setItemFromCursor(rs);
                resultList.add(item);
            }

            Err_Code = ocs.getInt(9);
            Err_Msg = ocs.getString(10);

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, rs);
        }
        return resultList;
    }

    @Override
    public int getRefLegalPersonsCount(Date date_, RefLegalPersonItem filter) {
        int Err_Code = 0;
        String Err_Msg = "";

        int count;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        Connection connection = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call TMP_PKG_FRSI_REF.get_lp_list_count(?, ?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);

            ocs.setDate(1, new java.sql.Date(date_.getTime()));

            if (filter == null || filter.getNameRu() == null) {
                ocs.setNull(2, OracleTypes.NULL);
            } else
                ocs.setString(2, filter.getNameRu());

            if (filter == null || filter.getIdn() == null) {
                ocs.setNull(3, OracleTypes.NULL);
            } else
                ocs.setString(3, filter.getIdn());

            if (filter == null || filter.getRefOrgType() == null || filter.getRefOrgType() == 0)
                ocs.setNull(4, OracleTypes.NULL);
            else
                ocs.setLong(4, filter.getRefOrgType());
            if (filter == null || filter.getRefOrgTypeRecId() == null || filter.getRefOrgTypeRecId() == 0)
                ocs.setNull(5, OracleTypes.NULL);
            else
                ocs.setLong(5, filter.getRefOrgTypeRecId());
            ocs.registerOutParameter(6, OracleTypes.INTEGER);
            ocs.registerOutParameter(7, OracleTypes.INTEGER);
            ocs.registerOutParameter(8, OracleTypes.VARCHAR);
            ocs.execute();

            count = ocs.getInt(6);

            Err_Code = ocs.getInt(7);
            Err_Msg = ocs.getString(8);

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt);
        }
        return count;
    }

    @Override
    public List<RefLegalPersonItem> getRefLPTaxByFilterPage(Date date_, RefUnionPersonItem filter, int offset, int limit){
        int ErrCode = 0;
        String ErrMsg = "";

        List<RefLegalPersonItem> resultList = new ArrayList<RefLegalPersonItem>();
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet rs = null;
        Connection connection = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_READ_LP_IN_TAX (?, ?, ?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);

            ocs.setDate(1, new java.sql.Date(date_.getTime()));
            if (filter.getNameRu() == null) {
                ocs.setNull(2, OracleTypes.NULL);
            } else {
                ocs.setString(2, filter.getNameRu());
            }
            if (filter.getIdn() == null) {
                ocs.setNull(3, OracleTypes.NULL);
            } else {
                ocs.setString(3, filter.getIdn());
            }
            if (filter.getType() == null) {
                ocs.setNull(4, OracleTypes.NULL);
            } else {
                switch (filter.getType()){
                    case 0:
                        ocs.setNull(4, OracleTypes.NULL);
                        break;
                    case 1:
                        ocs.setInt(4, 1);
                        break;
                    case 2:
                        ocs.setInt(4, 0);
                        break;
                }
            }
            ocs.setInt(5, offset);
            ocs.setInt(6, limit);
            ocs.registerOutParameter(7, OracleTypes.CURSOR);
            ocs.registerOutParameter(8, OracleTypes.INTEGER);
            ocs.registerOutParameter(9, OracleTypes.VARCHAR);
            ocs.execute();

            // получение курсора
            rs = ocs.getCursor(7);

            while (rs.next()) {
                RefLegalPersonItem item = new RefLegalPersonItem();
                item.setId(rs.getLong("ID"));
                item.setRecId(rs.getLong("REC_ID"));
                item.setIdn(rs.getString("IDN"));
                item.setNameRu(rs.getString("NAME_RU"));
                item.setTax(true);
                resultList.add(item);
            }

            ErrCode = ocs.getInt(8);
            ErrMsg = ocs.getString(9);

            if (ErrCode != 0) throw new SQLException(ErrMsg);

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, rs, ocs);
        }
        return resultList;
    }

    private RefLegalPersonItem getLegalPersonFromResultSet(ResultSet rs) throws SQLException {
        RefLegalPersonItem legalPerson = new RefLegalPersonItem();

        legalPerson.setId(rs.getLong("id"));
        legalPerson.setIdHst(null);
        legalPerson.setRecId(rs.getLong("rec_id"));
        legalPerson.setCode(rs.getString("code"));
        legalPerson.setNameEn(rs.getString("name_en"));
        legalPerson.setNameKz(rs.getString("NAME_KZ"));
        legalPerson.setNameRu(rs.getString("NAME_RU"));
       // legalPerson.setBic_bn(rs.getString("BIC_BN"));
       // legalPerson.setBicNbrk_bn(rs.getString("BIC_NBRK_BN"));
       // legalPerson.setBicHead_bn(rs.getString("BIC_HEAD_BN"));
       // legalPerson.setPostAddress_bn(rs.getString("POST_ADDRESS_BN"));
       // legalPerson.setPhoneNum_bn(rs.getString("PHONE_NUM_BN"));
       // legalPerson.setIsLoad(rs.getBoolean("IS_LOAD"));
        legalPerson.setShortNameRu(null);
        legalPerson.setBeginDate(rs.getDate("begin_date"));
        legalPerson.setEndDate(rs.getDate("end_date"));
        legalPerson.setUserId(rs.getLong("id_usr"));
        legalPerson.setUserName(null);
        legalPerson.setUserLocation(rs.getString("user_location"));
        legalPerson.setDelfl(rs.getLong("delfl"));
        legalPerson.setDatlast(rs.getDate("datlast"));
        legalPerson.setTypeChange(null);
        legalPerson.setTypeChangeName(null);
        legalPerson.setSentKnd(String.valueOf(rs.getLong("sent_knd")));
        legalPerson.setNote(null);
        legalPerson.setTag(null);

        legalPerson.setIdn(rs.getString("idn"));
        legalPerson.setShortNameRu(null);
//		legalPerson.setSubsidiary(rs.getLong("is_subsidiary") > 0L);
//		legalPerson.setParentId(rs.getLong("parent_id"));
        legalPerson.setLegalAddress(rs.getString("legal_address"));
        legalPerson.setFactAddress(rs.getString("fact_address"));

        legalPerson.setRefCountry(rs.getLong("ref_country"));

        return legalPerson;
    }

    // Справочник балансовых счетов для отчетов о сделках
    @Override
    public List<RefBalanceAccItem> getRefBalanceAccLastRecord(Date date_) {

        int Err_Code = 0;
        String Err_Msg = "";
        List<RefBalanceAccItem> result = new ArrayList<RefBalanceAccItem>();

        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;

        try {
            connection = getConnection();
            // вызов процедуры
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_READ_BAL_ACC_LAST_REC_LIST (?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            if (date_ == null) {
                ocs.setNull(1, OracleTypes.NULL);
            } else
                ocs.setDate(1, new java.sql.Date(date_.getTime()));
            ocs.registerOutParameter(2, OracleTypes.CURSOR);
            ocs.registerOutParameter(3, OracleTypes.INTEGER);
            ocs.registerOutParameter(4, OracleTypes.VARCHAR);
            ocs.execute();
            Err_Code = ocs.getInt(3);
        } catch (SQLException e) {
            throw new EJBException(e);
        }

        // получение курсора
        try {
            cursor = ocs.getCursor(2);
        } catch (SQLException e) {
            throw new EJBException(e);
        }
        try {
            // записываем в Bean, а затем добавляем в ArrayList
            while (cursor.next()) {
                RefBalanceAccItem ref_balance_account = new RefBalanceAccItem();
                ref_balance_account.setId(cursor.getLong("ID"));
                ref_balance_account.setRecId(cursor.getLong("REC_ID"));
                ref_balance_account.setCode(cursor.getString("CODE"));
                ref_balance_account.setParentCode(cursor.getString("PARENT_CODE"));
                ref_balance_account.setLevelCode(cursor.getString("LEVEL_CODE"));
                ref_balance_account.setNameKz(cursor.getString("NAME_KZ"));
                ref_balance_account.setNameRu(cursor.getString("NAME_RU"));
                ref_balance_account.setNameEn(cursor.getString("NAME_EN"));
                ref_balance_account.setShortNameKz(cursor.getString("SHORT_NAME_KZ"));
                ref_balance_account.setShortNameRu(cursor.getString("SHORT_NAME_RU"));
                ref_balance_account.setShortNameEn(cursor.getString("SHORT_NAME_EN"));
                ref_balance_account.setBeginDate(cursor.getDate("BEGIN_DATE"));
                ref_balance_account.setEndDate(cursor.getDate("END_DATE"));
                ref_balance_account.setDatlast(cursor.getTimestamp("DATLAST"));
                ref_balance_account.setUserName(cursor.getString("USER_NAME"));
                ref_balance_account.setUserLocation(cursor.getString("USER_LOCATION"));
                ref_balance_account.setSentKnd(cursor.getString("SENT_KND"));
                result.add(ref_balance_account);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, cursor, ocs);
        }
        return result;
    }


    // Справочник балансовых счетов для отчетов о сделках
    @Override
    public List<BalanceAccountRec> getSortedBalanceAccounts(Date reportDate) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<BalanceAccountRec> result = new ArrayList<BalanceAccountRec>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("select rownum, \n" +
                    "       level-1 as lev,\n" +
                    "       ba.code,\n" +
                    "       ba.parent_code\n" +
                    "from v_ref_balance_account ba\n" +
                    "where ba.begin_date = (select max(t.begin_date)\n" +
                    "                          from v_ref_balance_account t\n" +
                    "                          where t.rec_id = ba.rec_id\n" +
                    "                                and t.begin_date <= ?)\n" +
                    "      and (? < ba.end_date or ba.end_date is null)\n" +
                    "start with ba.parent_code is null\n" +
                    "connect by prior ba.code = ba.parent_code\n" +
                    "order siblings by ba.code");
            ps.setDate(1, new java.sql.Date(reportDate.getTime()));
            ps.setDate(2, new java.sql.Date(reportDate.getTime()));
            rs = ps.executeQuery();
            while (rs.next()) {
                BalanceAccountRec rec = new BalanceAccountRec();
                rec.setRowNum(rs.getLong("rownum"));
                rec.setLevel(rs.getInt("lev"));
                rec.setCode(rs.getString("code"));
                rec.setParentCode(rs.getString("parent_code"));
                result.add(rec);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefBalanceAccItem> getRefBalanceAccountsByCode(Date date, String code) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefBalanceAccItem> result = new ArrayList<RefBalanceAccItem>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT * FROM v_ref_balance_account t WHERE code = ? AND begin_date = (SELECT MAX(begin_date) FROM v_ref_balance_account WHERE rec_id = t.rec_id AND begin_date <= ?) AND (? < end_date or end_date is null) ORDER BY code");
            ps.setString(1, code);
            ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setTimestamp(3, date == null ? null : new java.sql.Timestamp(date.getTime()));
            rs = ps.executeQuery();
            while (rs.next()) {
                RefBalanceAccItem item = getRefBalanceAccountFromResultSet(rs);
                result.add(item);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefBalanceAccItem> getRefBalanceAccountsByParentCode(Date date, String parentCode) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefBalanceAccItem> result = new ArrayList<RefBalanceAccItem>();
        try {
            connection = getConnection();
            if (parentCode == null) {
                ps = connection.prepareStatement("SELECT t.* FROM v_ref_balance_account t WHERE t.parent_code IS NULL AND t.begin_date = (SELECT MAX(d.begin_date) FROM v_ref_balance_account d WHERE d.rec_id = t.rec_id AND d.begin_date <= nvl(?,sysdate)) AND (nvl(?,sysdate) < end_date or end_date is null) ORDER BY t.code");
                ps.setTimestamp(1, date == null ? null : new java.sql.Timestamp(date.getTime()));
                ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
            } else {
                ps = connection.prepareStatement("SELECT t.* FROM v_ref_balance_account t WHERE t.parent_code = ? AND t.begin_date = (SELECT MAX(d.begin_date) FROM v_ref_balance_account d WHERE d.rec_id = t.rec_id AND d.begin_date <= nvl(?,sysdate)) AND (nvl(?,sysdate) < end_date or end_date is null) ORDER BY t.code");
                ps.setString(1, parentCode);
                ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
                ps.setTimestamp(3, date == null ? null : new java.sql.Timestamp(date.getTime()));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                RefBalanceAccItem item = getRefBalanceAccountFromResultSet(rs);
                result.add(item);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefBalanceAccItem> getRefBalanceAccountsByLevelCode(Date date, String levelCode) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefBalanceAccItem> result = new ArrayList<RefBalanceAccItem>();
        try {
            connection = getConnection();
            if (levelCode == null) {
                ps = connection.prepareStatement("SELECT * FROM v_ref_balance_account t WHERE level_code IS NULL AND begin_date = (SELECT MAX(begin_date) FROM v_ref_balance_account WHERE rec_id = t.rec_id AND begin_date <= ?) AND (? < end_date or end_date is null) ORDER BY code");
                ps.setTimestamp(1, date == null ? null : new java.sql.Timestamp(date.getTime()));
                ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
            } else {
                ps = connection.prepareStatement("SELECT * FROM v_ref_balance_account t WHERE level_code = ? AND begin_date = (SELECT MAX(begin_date) FROM v_ref_balance_account WHERE rec_id = t.rec_id AND begin_date <= ?) AND (? < end_date or end_date is null) ORDER BY code");
                ps.setString(1, levelCode);
                ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
                ps.setTimestamp(3, date == null ? null : new java.sql.Timestamp(date.getTime()));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                RefBalanceAccItem item = getRefBalanceAccountFromResultSet(rs);
                result.add(item);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    private RefBalanceAccItem getRefBalanceAccountFromResultSet(ResultSet rs) throws SQLException {
        RefBalanceAccItem item = new RefBalanceAccItem();

        item.setId(rs.getLong("id"));
        item.setIdHst(null);
        item.setRecId(rs.getLong("rec_id"));
        item.setCode(rs.getString("code"));
        item.setNameEn(rs.getString("name_en"));
        item.setNameKz(rs.getString("NAME_KZ"));
        item.setNameRu(rs.getString("NAME_RU"));
        item.setShortNameRu(null);
        item.setBeginDate(rs.getDate("begin_date"));
        item.setEndDate(rs.getDate("end_date"));
        item.setUserId(rs.getLong("id_usr"));
        item.setUserName(null);
        item.setUserLocation(rs.getString("user_location"));
        item.setDelfl(rs.getLong("delfl"));
        item.setDatlast(rs.getDate("datlast"));
        item.setTypeChange(null);
        item.setTypeChangeName(null);
        item.setSentKnd(String.valueOf(rs.getLong("sent_knd")));
        item.setNote(null);
        item.setTag(null);

        item.setParentCode(rs.getString("parent_code"));
        item.setLevelCode(rs.getString("level_code"));

        return item;
    }


    @Override
    @Deprecated
    public Long getRefDepartmentId(Long RecId, Date date_) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Long result = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("select id from v_ref_department n where n.rec_id = ? " +
                    "and n.begin_date = (select max(t.begin_date) from v_ref_department t where t.rec_id = n.rec_id and t.begin_date <= ?)");

            ps.setLong(1, RecId);
            ps.setDate(2, new java.sql.Date(date_.getTime()));
            rs = ps.executeQuery();
            while (rs.next()) {
                result = rs.getLong("ID");
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }

        return result;
    }

    @Override
    public List<SimpleReference> getRefDeptType(Date date_) {
        int Err_Code = 0;
        String Err_Msg = " ";
        List<SimpleReference> result = new ArrayList<SimpleReference>();

        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;

        try {
            connection = getConnection();
            // вызов процедуры
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_READ_DEP_TYPE (?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            if (date_ == null) {
                ocs.setNull(1, OracleTypes.NULL);
            } else
                ocs.setDate(1, new java.sql.Date(date_.getTime()));
            ocs.registerOutParameter(2, OracleTypes.CURSOR);
            ocs.registerOutParameter(3, OracleTypes.INTEGER);
            ocs.registerOutParameter(4, OracleTypes.VARCHAR);
            ocs.execute();
            Err_Code = ocs.getInt(3);
        } catch (SQLException e) {
            throw new EJBException(e);
        }

        // получение курсора
        try {
            cursor = ocs.getCursor(2);
        } catch (SQLException e) {
            throw new EJBException(e);
        }
        try {
            // записываем в Bean, а затем добавляем в ArrayList
            while (cursor.next()) {
                SimpleReference item = new SimpleReference();
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
                result.add(item);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, cursor, ocs);
        }

        return result;
    }

    // Справочник банков второго уровня
    @Override
    public List<RefBankItem> getBanks(Date date) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefBankItem> result = new ArrayList<RefBankItem>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT * FROM v_ref_bank t WHERE begin_date = (SELECT MAX(begin_date) FROM v_ref_bank WHERE rec_id = t.rec_id AND begin_date <= ?) AND (? < end_date or end_date is null) ORDER BY name_ru");
            ps.setTimestamp(1, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
            rs = ps.executeQuery();
            while (rs.next()) {
                RefBankItem bank = getBankFromResultSet(rs);
                result.add(bank);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefBankItem> getBanksByFilters(Date date, String name) {
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        List<RefBankItem> result = new ArrayList<RefBankItem>();
        try {
            connection = getConnection();
            StringBuilder sbQuery = new StringBuilder();
            sbQuery.append("SELECT * FROM v_ref_bank t WHERE 1=1");
            if (date != null) {
                String toDate = getOracleDate(date);
                sbQuery.append(" AND begin_date = (SELECT MAX(begin_date) FROM v_ref_bank WHERE rec_id = t.rec_id AND begin_date <= " + toDate + ")");
                sbQuery.append(" AND (" + toDate + " < end_date or end_date is null)");
            }
            if (name != null && !name.trim().isEmpty())
                sbQuery.append(" AND LOWER(name_ru) LIKE LOWER('%" + name + "%')");
            sbQuery.append(" ORDER BY name_ru");
            String query = sbQuery.toString();
            statement = connection.createStatement();
            rs = statement.executeQuery(query);
            while (rs.next()) {
                RefBankItem bank = getBankFromResultSet(rs);
                result.add(bank);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, statement, rs);
        }
        return result;
    }

    private RefBankItem getBankFromResultSet(ResultSet rs) throws SQLException {
        RefBankItem item = new RefBankItem();

        item.setId(rs.getLong("id"));
        item.setIdHst(null);
        item.setRecId(rs.getLong("rec_id"));
        item.setCode(rs.getString("code"));
        item.setNameEn(rs.getString("name_en"));
        item.setNameKz(rs.getString("NAME_KZ"));
        item.setNameRu(rs.getString("NAME_RU"));
        item.setShortNameRu(null);
        item.setBeginDate(rs.getDate("begin_date"));
        item.setEndDate(rs.getDate("end_date"));
        item.setUserId(rs.getLong("id_usr"));
        item.setUserName(null);
        item.setUserLocation(rs.getString("user_location"));
        item.setDelfl(rs.getLong("delfl"));
        item.setDatlast(rs.getDate("datlast"));
        item.setTypeChange(null);
        item.setTypeChangeName(null);
        item.setSentKnd(String.valueOf(rs.getLong("sent_knd")));
        item.setNote(null);
        item.setTag(null);

        item.setBic(rs.getString("bic"));
        item.setBicHead(rs.getString("bic_head"));
        item.setBicNbrk(rs.getString("bic_nbrk"));
        item.setIdn(rs.getString("idn"));
        item.setPostAddress(rs.getString("post_address"));
        item.setPhoneNum(rs.getString("phone_num"));

        return item;
    }


    // Справочник валют
    @Override
    public List<CurrencyWrapper> getCurrencyWrappers(Date date) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CurrencyWrapper> result = new ArrayList<CurrencyWrapper>();
        try {
            connection = getConnection();
            String stmt =
                    "SELECT c.*, r.code credit_rate, a.code rating_agency_code, a.name_ru rating_agency_name " +
                            "FROM v_ref_currency c " +
                            "LEFT OUTER JOIN v_ref_currency_rate r ON c.ref_currency_rate = r.id " +
                            "LEFT OUTER JOIN v_ref_rate_agency a ON r.ref_rate_agency = a.id " +
                            "WHERE c.begin_date = (SELECT MAX(begin_date) FROM v_ref_currency WHERE rec_id = c.rec_id AND begin_date <= ?) AND (? < c.end_date or c.end_date is null) " +
                            "ORDER BY c.name_ru";
            ps = connection.prepareStatement(stmt);
            ps.setTimestamp(1, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
            rs = ps.executeQuery();
            while (rs.next()) {
                CurrencyWrapper cw = getCurrencyWrapperFromResultSet(rs);
                result.add(cw);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<String> getRateACurrencyRecIds(Date date) {
        List<String> result = new ArrayList<String>();
        List<CurrencyWrapper> currencies = getCurrencyWrappers(date);
        for (CurrencyWrapper currency : currencies)
            if (currency.getCreditRate() != null && currency.getCreditRate().trim().toUpperCase().startsWith("A"))
                result.add(String.valueOf(currency.getRecId()));
        return result;
    }

    private CurrencyWrapper getCurrencyWrapperFromResultSet(ResultSet rs) throws SQLException {
        CurrencyWrapper cw = new CurrencyWrapper();

        cw.setId(rs.getLong("id"));
        cw.setIdHst(null);
        cw.setRecId(rs.getLong("rec_id"));
        cw.setCode(rs.getString("code"));
        cw.setNameEn(rs.getString("name_en"));
        cw.setNameKz(rs.getString("NAME_KZ"));
        cw.setNameRu(rs.getString("NAME_RU"));
        cw.setShortNameRu(null);
        cw.setBeginDate(rs.getDate("begin_date"));
        cw.setEndDate(rs.getDate("end_date"));
        cw.setUserId(rs.getLong("id_usr"));
        cw.setUserName(null);
        cw.setUserLocation(rs.getString("user_location"));
        cw.setDelfl(rs.getLong("delfl"));
        cw.setDatlast(rs.getDate("datlast"));
        cw.setTypeChange(null);
        cw.setTypeChangeName(null);
        cw.setSentKnd(String.valueOf(rs.getLong("sent_knd")));
        cw.setNote(null);
        cw.setTag(null);

        cw.setMinorUnits(rs.getLong("minor_units"));
        cw.setCreditRate(rs.getString("credit_rate"));
        cw.setRatingAgencyCode(rs.getString("rating_agency_code"));
        cw.setRatingAgencyName(rs.getString("rating_agency_name"));

        return cw;
    }

    // Справочник типов организаций
    @Override
    public List<RefSubjectTypeItem> getRefSubjectTypeListAdvanced(Date date_, boolean includeFirstLevelBank) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefSubjectTypeItem> result = new ArrayList<RefSubjectTypeItem>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("select st.id,\n" +
                    "             st.rec_id,\n" +
                    "             st.code,\n" +
                    "             st.name_kz,\n" +
                    "             st.name_ru,\n" +
                    "             st.name_en,\n" +
                    "             st.short_name_kz,\n" +
                    "             st.short_name_ru,\n" +
                    "             st.short_name_en,\n" +
                    "             st.kind_id,\n" +
                    "             st.rep_per_dur_months,\n" +
                    "             st.is_advance,\n" +
                    "             dm.name as du_name,\n" +
                    "             st.begin_date,             \n" +
                    "             st.end_date,             \n" +
                    "             st.datlast,\n" +
                    "             st.id_usr,\n" +
                    "             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          \n" +
                    "             st.user_location,\n" +
                    "             sk.name as sent_knd\n" +
                    "        from v_ref_subject_type st,\n" +
                    "             rep_per_dur_months dm,\n" +
                    "             f_users u,\n" +
                    "             sent_knd sk\n" +
                    "       where st.rec_id != ? and st.begin_date = (select max(sf2.begin_date) from v_ref_subject_type sf2 where st.rec_id=sf2.rec_id and sf2.is_advance=1 and sf2.begin_date<=?)\n" +
                    "         and (? < st.end_date or st.end_date is null)\n" +
                    "         and st.id_usr = u.user_id\n" +
                    "         and st.rep_per_dur_months = dm.id\n" +
                    "         and st.sent_knd = sk.sent_knd");
            ps.setLong(1, includeFirstLevelBank ? 0 : Constants.FIRST_LEVEL_BANK_REC_ID);
            ps.setDate(2, new java.sql.Date(date_.getTime()));
            ps.setDate(3, new java.sql.Date(date_.getTime()));
            rs = ps.executeQuery();
            while (rs.next()) {
                RefSubjectTypeItem ref_subject_type = new RefSubjectTypeItem();
                ref_subject_type.setId(rs.getLong("ID"));
                ref_subject_type.setRecId(rs.getLong("REC_ID"));
                ref_subject_type.setCode(rs.getString("CODE"));
                ref_subject_type.setNameKz(rs.getString("NAME_KZ"));
                ref_subject_type.setNameRu(rs.getString("NAME_RU"));
                ref_subject_type.setNameEn(rs.getString("NAME_EN"));
                ref_subject_type.setShortNameKz(rs.getString("SHORT_NAME_KZ"));
                ref_subject_type.setShortNameRu(rs.getString("SHORT_NAME_RU"));
                ref_subject_type.setShortNameEn(rs.getString("SHORT_NAME_EN"));
                ref_subject_type.setNameRu(ref_subject_type.getShortNameRu(), ref_subject_type.getNameRu());
                ref_subject_type.setRepPerDurMonths(rs.getLong("REP_PER_DUR_MONTHS"));
                ref_subject_type.setIsAdvance(rs.getInt("IS_ADVANCE") > 0);
                ref_subject_type.setBeginDate(rs.getDate("BEGIN_DATE"));
                ref_subject_type.setEndDate(rs.getDate("END_DATE"));
                ref_subject_type.setDatlast(rs.getTimestamp("DATLAST"));
                ref_subject_type.setUserName(rs.getString("USER_NAME"));
                ref_subject_type.setUserLocation(rs.getString("USER_LOCATION"));
                ref_subject_type.setSentKnd(rs.getString("SENT_KND"));
                result.add(ref_subject_type);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefSubjectTypeItem> getSTListByUser(Long userId) {
        List<RefSubjectTypeItem> result = new ArrayList<RefSubjectTypeItem>();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet cursor = null;

        try {
            connection = getConnection();

            ps = connection.prepareStatement(
                    "select st.id as st_id,\n" +
                            "       st.rec_id as st_rec_id,\n" +
                            "       st.name_ru as st_name\n" +
                            "  from v_ref_subject_type st\n" +
                            " where st.is_advance = 1\n" +
                            "   and st.rec_id != ?\n" +
                            "   and st.begin_date = (select max(st1.begin_date)\n" +
                            "                          from v_ref_subject_type st1\n" +
                            "                         where st1.rec_id = st.rec_id\n" +
                            "                           and st1.is_advance = 1\n" +
                            "                           and st1.begin_date <= sysdate)\n" +
                            "   and (st.end_date is null or st.end_date > sysdate)\n" +
                            "   and exists (select *\n" +
                            "                 from v_ref_respondent r,\n" +
                            "                      f_session_creditors cr\n" +
                            "                where r.ref_subject_type = st.id\n" +
                            "                  and r.begin_date = (select max(r1.begin_date)\n" +
                            "                                        from v_ref_respondent r1\n" +
                            "                                       where r1.rec_id = r.rec_id\n" +
                            "                                         and r1.begin_date <= sysdate)\n" +
                            "                  and (r.end_date is null or r.end_date > sysdate)\n" +
                            "                  and r.rec_id = cr.creditor_id\n" +
                            "                  and cr.user_id = ?\n" +
                            "               )");

            ps.setLong(1, Constants.FIRST_LEVEL_BANK_REC_ID);
            ps.setLong(2, userId);
            cursor = ps.executeQuery();

            while (cursor.next()) {
                RefSubjectTypeItem item = new RefSubjectTypeItem();
                item.setId(cursor.getLong("ST_ID"));
                item.setRecId(cursor.getLong("ST_REC_ID"));
                item.setNameRu(cursor.getString("ST_NAME"));
                result.add(item);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, cursor);
        }
        return result;
    }

    @Override
    @Deprecated
    public Long getRefSubjectTypeId(Long RecId, Date date_) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Long result = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("select id from v_ref_subject_type n where n.rec_id = ? " +
                    "and n.begin_date = (select max(t.begin_date) from v_ref_subject_type t where t.rec_id = n.rec_id and t.begin_date <= ?)");

            ps.setLong(1, RecId);
            ps.setDate(2, new java.sql.Date(date_.getTime()));
            rs = ps.executeQuery();
            while (rs.next()) {
                result = rs.getLong("ID");
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }

        return result;
    }

    @Override
    public List<SubjectTypePost> getSubjectTypePostList(long stRecId, Date date){
        List<SubjectTypePost> result = new ArrayList<SubjectTypePost>();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet cursor = null;

        try {
            connection = getConnection();
            String sqlText = "select decode(st.ref_post_rec_id, null, 0, 1) as is_active,\n" +
                    "       p.rec_id as post_rec_id,\n" +
                    "       p.name_ru\n" +
                    "  from v_ref_post p,\n" +
                    "       type_post tp,\n" +
                    "       (select *\n" +
                    "         from ref_subject_type_post st\n" +
                    "        where st.ref_subject_type_rec_id = ?) st\n" +
                    " where st.ref_post_rec_id(+) = p.REC_ID\n" +
                    "   and p.begin_date = (select max(p1.begin_date)\n" +
                    "                         from v_ref_post p1\n" +
                    "                        where p1.rec_id = p.rec_id\n" +
                    "                          and p1.begin_date <= ?)" +
                    "   and p.type_post = tp.id\n" +
                    "   and tp.respondet_available = 1";

            ps = connection.prepareStatement(sqlText);
            ps.setLong(1, stRecId);
            ps.setDate(2, new java.sql.Date(date.getTime()));

            cursor = ps.executeQuery();

            while (cursor.next()) {
                SubjectTypePost item = new SubjectTypePost();
                item.setPostRecId(cursor.getLong("POST_REC_ID"));
                item.setName(cursor.getString("NAME_RU"));
                item.setActive(cursor.getInt("IS_ACTIVE") > 0);
                result.add(item);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, cursor);
        }
        return result;
    }

    @Override
    public void updateSubjectTypePost(List<SubjectTypePost> subjectTypePostList, long stRecId){
        PreparedStatement ps = null;
        AbstractReference result = null;
        Connection connection = null;
        try {

            connection = getConnection();
            connection.setAutoCommit(false);

            if (subjectTypePostList != null && subjectTypePostList.size() > 0) {
                ps = connection.prepareStatement("delete ref_subject_type_post where REF_SUBJECT_TYPE_REC_ID = ?");
                ps.setLong(1, stRecId);
                ps.execute();

                Date date = new Date();

                for (SubjectTypePost item : subjectTypePostList) {
                    if (item.getActive()) {
                        ps = connection.prepareStatement(
                                "insert into ref_subject_type_post\n" +
                                        "  (REF_SUBJECT_TYPE_REC_ID, REF_POST_REC_ID)\n" +
                                        "values\n" +
                                        "  (?, ?)");
                        ps.setLong(1, stRecId);
                        ps.setLong(2, item.getPostRecId());
                        ps.execute();
                    }
                }
            }
            connection.commit();
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps);
        }
    }

    @Override
    public List<Long> getActiveSubjectTypePosts(Long refSubjectTypeRecId) {
        List<Long> result = new ArrayList<Long>();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet cursor = null;

        try {
            connection = getConnection();
            String sql = "SELECT t.REF_POST_REC_ID\n" +
                    "FROM REF_SUBJECT_TYPE_POST t\n" +
                    "WHERE t.REF_SUBJECT_TYPE_REC_ID = ?\n" +
                    "      AND t.REF_POST_REC_ID IN (SELECT p.REC_ID\n" +
                    "                                FROM V_REF_POST p)";

            ps = connection.prepareStatement(sql);
            ps.setLong(1, refSubjectTypeRecId);

            cursor = ps.executeQuery();

            while (cursor.next()) {
                result.add(cursor.getLong("REF_POST_REC_ID"));
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, cursor);
        }
        return result;
    }

    @Override
    public List<RefSubjectTypeItem> getRefSubjectTypeByWarrant(Long refRespondentRecId) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefSubjectTypeItem> result = new ArrayList<RefSubjectTypeItem>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT *\n" +
                    "FROM V_REF_SUBJECT_TYPE st\n" +
                    "WHERE st.ID IN (\n" +
                    "  SELECT r.REF_SUBJECT_TYPE\n" +
                    "  FROM V_REF_RESPONDENT r\n" +
                    "  WHERE r.REC_ID IN (\n" +
                    "    SELECT ?\n" +
                    "    FROM dual\n" +
                    "    UNION ALL\n" +
                    "    SELECT w.REC_ID\n" +
                    "    FROM REF_RESPONDENT_WARRANT w\n" +
                    "    WHERE w.REC_ID_PARENT = ?\n" +
                    "  )\n" +
                    ")");
            ps.setLong(1, refRespondentRecId);
            ps.setLong(2, refRespondentRecId);
            rs = ps.executeQuery();
            while (rs.next()) {
                RefSubjectTypeItem ref_subject_type = new RefSubjectTypeItem();
                ref_subject_type.setId(rs.getLong("ID"));
                ref_subject_type.setRecId(rs.getLong("REC_ID"));
                ref_subject_type.setCode(rs.getString("CODE"));
                ref_subject_type.setNameKz(rs.getString("NAME_KZ"));
                ref_subject_type.setNameRu(rs.getString("NAME_RU"));
                ref_subject_type.setNameEn(rs.getString("NAME_EN"));
                ref_subject_type.setShortNameKz(rs.getString("SHORT_NAME_KZ"));
                ref_subject_type.setShortNameRu(rs.getString("SHORT_NAME_RU"));
                ref_subject_type.setShortNameEn(rs.getString("SHORT_NAME_EN"));
                result.add(ref_subject_type);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    // Справочник респондентов(подотчетных организаций)
    @Override
    public RefRespondentItem getRespondentByRecId(Long recId, Date reportDate) {
        int Err_Code = 0;
        String Err_Msg = " ";
        RefRespondentItem result = null;

        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;

        try {
            connection = getConnection();
            // вызов процедуры
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_READ_RESPONDENT_BY_REC_ID (?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            ocs.setLong(1, recId);
            if (reportDate == null) {
                ocs.setNull(2, OracleTypes.NULL);
            } else
                ocs.setDate(2, new java.sql.Date(reportDate.getTime()));
            ocs.registerOutParameter(3, OracleTypes.CURSOR);
            ocs.registerOutParameter(4, OracleTypes.INTEGER);
            ocs.registerOutParameter(5, OracleTypes.VARCHAR);
            ocs.execute();
            Err_Code = ocs.getInt(4);
            cursor = ocs.getCursor(3);
            while (cursor.next()) {
                result = RefRespondentItem.setItemFromCursor(cursor);
                break;
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, cursor, ocs);
        }

        if (result == null)
            throw new EJBException(MessageFormat.format("Респондент по rec_id {0} не найден", recId));

        return result;
    }

    @Override
    public RefRespondentItem getRespondentByIdn(String idn, Date reportDate) {
        RefRespondentItem result = null;
        if (idn == null || idn.trim().isEmpty()) return result;

        RefRespondentItem respondentItem = new RefRespondentItem();
        respondentItem.setIdn(idn);

        List<RefRespondentItem> respondentItems = (List<RefRespondentItem>) getRefAbstractByFilterList(RefRespondentItem.REF_CODE, respondentItem, reportDate);
        if (respondentItems != null && respondentItems.size() > 0)
            result = respondentItems.get(0);

        return result;
    }

    @Override
    public Map<String, Long> getRespondentBinEntityIdMap() {
        Connection connection = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        Map<String, Long> result = new HashMap<String, Long>();
        try {
            connection = getConnection();
            stmt = connection.prepareCall("BEGIN ? := pkg_frsi_util.get_respondents(?); end;");
            stmt.registerOutParameter(1, OracleTypes.CURSOR); //REF CURSOR
            java.sql.Date repDate = new java.sql.Date((new Date()).getTime());
            stmt.setDate(2, repDate);
            stmt.execute();
            rs = (ResultSet) stmt.getObject(1);
            while (rs.next()) {
                String bin = rs.getString("BIN");
                Long entityId = rs.getLong("ID");
                result.put(bin, entityId);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, rs);
        }
        return result;
    }

    @Override
    public List<RefRespondentItem> getUserRespsBySTRDepRecIds(Long userId, Date reportDate, List<Long> subjectTypeRecIds,  List<RefDepartmentItem> depList) {
        //todo переместить в процедуру
        Connection connection = null;
        List<RefRespondentItem> result = new ArrayList<RefRespondentItem>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        StringBuilder sb = new StringBuilder("0");
        for (Long stRecId : subjectTypeRecIds)
            sb.append(",").append(stRecId);

        try {
            connection = getConnection();

            String sqlText =
                    "      select r.id,\n" +
                    "             r.rec_id,\n" +
                    "             r.code,\n" +
                    "             r.REF_UNIONPERSONS,\n" +
                    "             up.REC_ID person_rec_id,\n" +
                    "             up.PERSON_ID,\n" +
                    "             up.name_ru as PERSON_NAME,\n" +
                    "             nvl(up.short_name_ru, up.name_ru) as PERSON_SHORT_NAME,\n" +
                    "             up.idn," +
                    "             r.ref_subject_type,\n" +
                    "             st.rec_id as ref_subject_type_rec_id,\n" +
                    "             st.name_ru as ref_subject_type_name,\n" +
                    "             r.ref_department,\n" +
                    "             d.rec_id as ref_department_rec_id,\n" +
                    "             d.name_ru as ref_department_name,\n" +
                    "             r.nokbdb_code,\n" +
                    "             r.main_buh,\n" +
                    "             r.date_begin_lic,\n" +
                    "             r.date_end_lic,\n" +
                    "             r.stop_lic,\n" +
                    "             r.vid_activity,              \n" +
                    "             r.begin_date,             \n" +
                    "             r.end_date,             \n" +
                    "             r.datlast,\n" +
                    "             r.id_usr,\n" +
                    "             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          \n" +
                    "             r.user_location,\n" +
                    "             sk.name as sent_knd,\n" +
                    "             up.IS_NON_REZIDENT\n" +
                    "        from v_ref_respondent r,             \n" +
                    "             V_REF_UNIONPERSONS up,\n" +
                    "             v_ref_subject_type st,\n" +
                    "             v_ref_department d,\n" +
                    "             f_users u,\n" +
                    "             sent_knd sk,\n" +
                    "             f_session_creditors s\n" +
                    "       where r.id_usr = u.user_id\n" +
                    "         and r.rec_id = s.creditor_id\n" +
                    "         and ( " + userId + " is null or s.user_id = " + userId + ")\n" +
                    "         and r.REF_UNIONPERSONS = up.id\n" +
                    "         and r.ref_department = d.id(+)\n" +
                    "         and r.ref_subject_type = st.id\n" +
                    "         and r.sent_knd = sk.sent_knd\n" +
                    "		  and st.rec_id in (" + sb.toString() + ") " +
                    "         and r.begin_date = (select max(t.begin_date)\n" +
                    "                                                 from v_ref_respondent t\n" +
                    "                                                where t.rec_id = r.rec_id\n" +
                    "                                                  and t.begin_date <= ?)" +
                    "		  and (? < r.end_date or r.end_date is null)\n"+
                    "         and up.begin_date = (select max(t.begin_date)\n" +
                    "                                                 from V_REF_UNIONPERSONS t\n" +
                    "                                                where t.rec_id = up.rec_id AND t.TYPE = up.TYPE\n" +
                    "                                                  and t.begin_date <= ?)" +
                    "		  and (? < up.end_date or up.end_date is null)";

            if(depList != null && depList.size() > 0) {
                StringBuilder sbD = new StringBuilder();
                Boolean noDep = false;
                for (int i = 0; i < depList.size(); i++) {
                    if(depList.get(i).getId() == 0)
                        noDep = true;
                    if (i > 0)
                        sbD.append(",");
                    sbD.append(depList.get(i).getId());
                }
                if(noDep) {
                    sqlText = sqlText + " and (r.ref_department is null or r.ref_department in (" + sbD.toString() + "))";
                }else {
                    sqlText = sqlText + " and r.ref_department in (" + sbD.toString() + ")";
                }
            }

            ps = connection.prepareStatement(sqlText);

            ps.setDate(1, new java.sql.Date(reportDate.getTime()));
            ps.setDate(2, new java.sql.Date(reportDate.getTime()));
            ps.setDate(3, new java.sql.Date(reportDate.getTime()));
            ps.setDate(4, new java.sql.Date(reportDate.getTime()));
            rs = ps.executeQuery();
            while (rs.next()) {
                RefRespondentItem item = RefRespondentItem.setItemFromCursor(rs);
                result.add(item);
            }

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefRespondentItem> getUserRespondentsBySubjectType(long userId, Long stRecId, Date reportDate, List<RefDepartmentItem> deps) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefRespondentItem> result = new ArrayList<RefRespondentItem>();
        try {
            connection = getConnection();
            String sqlText =
                    "    select r.id,\n" +
                    "           r.rec_id,\n" +
                    "           r.code,\n" +
                    "           r.REF_UNIONPERSONS,\n" +
                    "           up.person_id,\n" +
                    "           up.rec_id person_rec_id,\n" +
                    "           up.name_ru as PERSON_NAME,\n" +
                    "           nvl(up.short_name_ru, up.name_ru) as PERSON_SHORT_NAME,\n" +
                    "           up.idn,\n" +
                    "           r.ref_subject_type,\n" +
                    "           st.rec_id as ref_subject_type_rec_id,\n" +
                    "           st.name_ru as ref_subject_type_name,\n" +
                    "           r.REF_DEPARTMENT,\n" +
                    "           d.rec_id as REF_DEPARTMENT_REC_ID,\n" +
                    "           d.name_ru as REF_DEPARTMENT_NAME,\n" +
                    "           r.nokbdb_code,\n" +
                    "           r.main_buh,\n" +
                    "           r.date_begin_lic,\n" +
                    "           r.date_end_lic,\n" +
                    "           r.stop_lic,\n" +
                    "           r.vid_activity,              \n" +
                    "           r.begin_date,\n" +
                    "           r.end_date,\n" +
                    "           r.datlast,\n" +
                    "           r.id_usr,\n" +
                    "           u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          \n" +
                    "           r.user_location,\n" +
                    "           sk.name as sent_knd,\n" +
                    "           up.IS_NON_REZIDENT\n" +
                    "      from v_ref_respondent r,             \n" +
                    "           V_REF_UNIONPERSONS up,\n" +
                    "           v_ref_subject_type st,\n" +
                    "           v_ref_department d,\n" +
                    "           f_users u,\n" +
                    "           sent_knd sk,\n" +
                    "           f_session_creditors s\n" +
                    "     where r.id_usr = u.user_id\n" +
                    "       and r.rec_id = s.creditor_id\n" +
                    "       and s.user_id = ?\n" +
                    "       and r.REF_DEPARTMENT = d.id(+)\n" +
                    "       and r.REF_UNIONPERSONS = up.id\n" +
                    "       and r.ref_subject_type = st.id\n" +
                    "       and st.rec_id = ?\n" +
                    "       and r.sent_knd = sk.sent_knd         \n" +
                    "       and (? is null or r.begin_date = (select max(t.begin_date)\n" +
                    "                                                       from v_ref_respondent t\n" +
                    "                                                      where t.rec_id = r.rec_id\n" +
                    "                                                        and t.begin_date <= ?))\n" +
                    "       and (r.end_date is null or r.end_date > ?)\n"+
                    "       and (up.begin_date = (select max(t.begin_date)\n" +
                    "                                                       from V_REF_UNIONPERSONS t\n" +
                    "                                                      where t.rec_id = up.rec_id AND t.TYPE=up.TYPE\n" +
                    "                                                        and t.begin_date <= ?))\n" +
                    "       and (up.end_date is null or up.end_date > ?)";

            if(deps != null && deps.size() > 0) {
                StringBuilder sb = new StringBuilder();
                Boolean noDep = false;
                for (int i = 0; i < deps.size(); i++) {
                    if(deps.get(i).getId() == 0)
                        noDep = true;
                    if (i > 0)
                        sb.append(",");
                    sb.append(deps.get(i).getId());
                }
                if(noDep) {
                    sqlText = sqlText + " and (r.ref_department is null or r.ref_department in (" + sb.toString() + "))";
                }else {
                    sqlText = sqlText + " and r.ref_department in (" + sb.toString() + ")";
                }
            }
            ps = connection.prepareStatement(sqlText);
            java.sql.Date repDate = null;
            if (reportDate != null)
                repDate = new java.sql.Date(reportDate.getTime());
            ps.setLong(1, userId);
            ps.setLong(2, stRecId);
            ps.setDate(3, repDate);
            ps.setDate(4, repDate);
            ps.setDate(5, repDate);
            ps.setDate(6, repDate);
            ps.setDate(7, repDate);
            ps.execute();
            rs = ps.executeQuery();
            while (rs.next()) {
                RefRespondentItem item = RefRespondentItem.setItemFromCursor(rs);
                result.add(item);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefRespondentItem> getRespondentsByUserSTRecIdList(Long userId, List<Long> stRecIdList) {
        List<RefRespondentItem> result = new ArrayList<RefRespondentItem>();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet cursor = null;

        try {
            connection = getConnection();

            StringBuilder sqlText = new StringBuilder();
            sqlText.append("select r.id as resp_id,\n" +
                    "       r.rec_id as resp_rec_id,\n" +
                    "       nvl(up.short_name_ru, up.name_ru) as resp_short_name\n" +
                    "  from v_ref_respondent r,\n" +
                    "       v_ref_unionpersons up,\n" +
                    "       v_ref_subject_type st,\n" +
                    "       f_session_creditors cr       \n" +
                    " where r.begin_date = (select max(r1.begin_date)\n" +
                    "                         from v_ref_respondent r1\n" +
                    "                        where r1.rec_id = r.rec_id\n" +
                    "                          and r1.begin_date <= sysdate)\n" +
                    "   and (r.end_date is null or r.end_date > sysdate)\n" +
                    "   and r.ref_unionpersons = up.id\n" +
                    "   and up.begin_date = (select max(lp1.begin_date)\n" +
                    "                         from v_ref_unionpersons lp1\n" +
                    "                        where lp1.rec_id = up.rec_id and lp1.type = up.type\n" +
                    "                          and lp1.begin_date <= sysdate)\n" +
                    "   and (up.end_date is null or up.end_date > sysdate)\n" +
                    "   and r.ref_subject_type = st.id\n" +
                    "   and st.begin_date = (select max(st1.begin_date)\n" +
                    "                          from v_ref_subject_type st1\n" +
                    "                         where st1.rec_id = st.rec_id\n" +
                    "                           and st1.begin_date <= sysdate)\n" +
                    "   and (st.end_date is null or st.end_date > sysdate)\n" +
                    "   and st.rec_id != ? \n" +
                    "   and r.rec_id = cr.creditor_id\n" +
                    "   and cr.user_id = ?\n");

            if (stRecIdList != null && stRecIdList.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < stRecIdList.size(); i++) {
                    if (i > 0)
                        sb.append(",");
                    sb.append(stRecIdList.get(i));
                }

                sqlText.append("and st.rec_id in (" + sb.toString() + ")");
            }

            ps = connection.prepareStatement(sqlText.toString());

            ps.setLong(1, Constants.FIRST_LEVEL_BANK_REC_ID);
            ps.setLong(2, userId);
            cursor = ps.executeQuery();

            while (cursor.next()) {
                RefRespondentItem item = new RefRespondentItem();
                item.setId(cursor.getLong("RESP_ID"));
                item.setRecId(cursor.getLong("RESP_REC_ID"));
                item.setPersonShortName(cursor.getString("RESP_SHORT_NAME"));
                result.add(item);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, cursor);
        }
        return result;
    }

    @Override
    public List<RefRespondentItem> getRespondentsWithWarrants(Long parentRespRecId, Date reportDate, Boolean withParent){
        List<RefRespondentItem> result = new ArrayList<RefRespondentItem>();

        if(withParent) {
            RefRespondentItem parentFilter = new RefRespondentItem();
            parentFilter.setRecId(parentRespRecId);
            RefRespondentItem parentResp = (RefRespondentItem) getRefAbstractByFilterList(RefRespondentItem.REF_CODE, parentFilter, reportDate).get(0);
            result.add(parentResp);
        }

        List<RespondentWarrant> respWarrantList = getRespondentWarrantList(parentRespRecId, reportDate);
        for(RespondentWarrant item : respWarrantList){
            RefRespondentItem filter = new RefRespondentItem();
            filter.setRecId(item.getRecId());
            RefRespondentItem childResp = (RefRespondentItem) getRefAbstractByFilterList(RefRespondentItem.REF_CODE, filter, reportDate).get(0);
            result.add(childResp);
        }
        return result;
    }

    @Override
    public List<RespondentWarrant> getRespondentWarrantList(Long respRecId, Date date) {
        List<RespondentWarrant> result = new ArrayList<RespondentWarrant>();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet cursor = null;

        try {
            connection = getConnection();
            ps = connection.prepareStatement(
                    "select rw.id,\n" +
                    "       rw.rec_id_parent,\n" +
                    "       rw.rec_id,\n" +
                    "       up.name_ru as child_name,\n" +
                    "       rw.begin_date,\n" +
                    "       rw.end_date,\n" +
                    "       rw.num, \n" +
                    "      (select decode(sum(dbms_lob.getlength(af.file_data)),null,0,1)   \n" +
                    "         from attached_file af\n" +
                    "        where af.link_id = rw.id " +
                    "          and af.file_kind = 5) as have_file \n" +
                    "  from ref_respondent_warrant rw,\n" +
                    "       v_ref_respondent r,\n" +
                    "       V_REF_UNIONPERSONS up\n" +
                    " where rw.rec_id = r.rec_id\n" +
                    "   and r.begin_date = (select max(r1.begin_date)\n" +
                    "                         from v_ref_respondent r1\n" +
                    "                        where r1.rec_id = r.rec_id\n" +
                    "                          and r1.begin_date <= ?)\n" +
                    "   and (r.end_date is null or r.end_date > ?)\n" +
                    "   and r.REF_UNIONPERSONS = up.id " +
                    "   and up.begin_date = (select max(up1.begin_date)\n" +
                    "                         from V_REF_UNIONPERSONS up1\n" +
                    "                        where up1.rec_id = up.rec_id AND up1.TYPE=up.TYPE\n" +
                    "                          and up1.begin_date <= ?)\n" +
                    "   and (up.end_date is null or up.end_date > ?)\n" +
                    "   and rw.rec_id_parent = ? ");
            ps.setDate(1, new java.sql.Date(date.getTime()));
            ps.setDate(2, new java.sql.Date(date.getTime()));
            ps.setDate(3, new java.sql.Date(date.getTime()));
            ps.setDate(4, new java.sql.Date(date.getTime()));
            ps.setLong(5, respRecId);
            cursor = ps.executeQuery();

            while (cursor.next()) {
                RespondentWarrant item = new RespondentWarrant();
                item.setId(cursor.getLong("ID"));
                item.setRecIdParent(cursor.getLong("REC_ID_PARENT"));
                item.setRecId(cursor.getLong("REC_ID"));
                item.setChildName(cursor.getString("CHILD_NAME"));
                item.setbDate(cursor.getDate("BEGIN_DATE"));
                item.seteDate(cursor.getDate("END_DATE"));
                item.setNum(cursor.getString("NUM"));
                item.setHaveFile(cursor.getInt("HAVE_FILE") > 0);
                item.setSubmitReport(submitReportsByWarrant(item.getRecIdParent(), item.getRecId()));
                result.add(item);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, cursor);
        }
        return result;
    }

    @Override
    public RespondentWarrant getRespondentWarrant(Long respRecId, String idnChild, Date date){
        RespondentWarrant result = null;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet cursor = null;

        try {
            connection = getConnection();
            ps = connection.prepareStatement(
                    "select rw.id,\n" +
                    "       rw.rec_id_parent,\n" +
                    "       rw.rec_id,\n" +
                    "       up.name_ru as child_name,\n" +
                    "       rw.begin_date,\n" +
                    "       rw.end_date,\n" +
                    "       rw.num, \n" +
                    "      (select decode(sum(dbms_lob.getlength(af.file_data)),null,0,1)   \n" +
                    "         from attached_file af\n" +
                    "        where af.link_id = rw.id " +
                    "          and af.file_kind = 5) as have_file \n" +
                    "  from ref_respondent_warrant rw,\n" +
                    "       v_ref_respondent r,\n" +
                    "       V_REF_UNIONPERSONS up\n" +
                    " where rw.rec_id = r.rec_id\n" +
                    "   and r.begin_date = (select max(r1.begin_date)\n" +
                    "                         from v_ref_respondent r1\n" +
                    "                        where r1.rec_id = r.rec_id\n" +
                    "                          and r1.begin_date <= ?)\n" +
                    "   and (r.end_date is null or r.end_date > ?)\n" +
                    "   and r.REF_UNIONPERSONS = up.id " +
                    "   and up.begin_date = (select max(up1.begin_date)\n" +
                    "                         from V_REF_UNIONPERSONS up1\n" +
                    "                        where up1.rec_id = up.rec_id AND up1.TYPE = up.TYPE\n" +
                    "                          and up1.begin_date <= ?)\n" +
                    "   and (up.end_date is null or up.end_date > ?)\n" +
                    "   and rw.rec_id_parent = ? " +
                    "   and up.idn = ? " +
                    "   and rw.begin_date <= ? " +
                    "   and (rw.end_date is null or rw.end_date > ?)");
            ps.setDate(1, new java.sql.Date(date.getTime()));
            ps.setDate(2, new java.sql.Date(date.getTime()));
            ps.setDate(3, new java.sql.Date(date.getTime()));
            ps.setDate(4, new java.sql.Date(date.getTime()));
            ps.setLong(5, respRecId);
            ps.setString(6, idnChild);
            ps.setDate(7, new java.sql.Date(date.getTime()));
            ps.setDate(8, new java.sql.Date(date.getTime()));
            cursor = ps.executeQuery();

            while (cursor.next()) {
                result = new RespondentWarrant();
                result.setId(cursor.getLong("ID"));
                result.setRecIdParent(cursor.getLong("REC_ID_PARENT"));
                result.setRecId(cursor.getLong("REC_ID"));
                result.setChildName(cursor.getString("CHILD_NAME"));
                result.setbDate(cursor.getDate("BEGIN_DATE"));
                result.seteDate(cursor.getDate("END_DATE"));
                result.setNum(cursor.getString("NUM"));
                result.setHaveFile(cursor.getInt("HAVE_FILE") > 0);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, cursor);
        }
        return result;
    }

    @Override
    public Boolean respondentHaveWarrant(Long recId, Date date){
        Boolean result = null;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet cursor = null;

        try {
            connection = getConnection();
            ps = connection.prepareStatement(
                    "select count(*) as cnt \n" +
                    "  from REF_RESPONDENT_WARRANT t\n" +
                    " where t.rec_id_parent = ?\n" +
                    "   and t.begin_date <= ?\n" +
                    "   and (t.end_date is null or t.end_date > ?)");
            ps.setLong(1, recId);
            ps.setDate(2, new java.sql.Date(date.getTime()));
            ps.setDate(3, new java.sql.Date(date.getTime()));
            cursor = ps.executeQuery();

            while (cursor.next()) {
                result = cursor.getInt("CNT") > 0;
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, cursor);
        }
        return result;
    }

    @Override
    public Boolean respondentHaveWarrantByIdn(Long recId, Date date, String idnChild){
        Boolean result = null;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet cursor = null;

        try {
            connection = getConnection();
            ps = connection.prepareStatement(
                    "select count(*) as cnt \n" +
                    "  from ref_respondent_warrant rw, \n" +
                    "       v_ref_respondent r, \n" +
                    "       V_REF_UNIONPERSONS up \n" +
                    " where rw.rec_id = r.rec_id \n" +
                    "   and r.begin_date = (select max(r1.begin_date) \n" +
                    "                         from v_ref_respondent r1 \n" +
                    "                        where r1.rec_id = r.rec_id \n" +
                    "                          and r1.begin_date <= ?) \n" +
                    "   and (r.end_date is null or r.end_date > ?) \n" +
                    "   and r.REF_UNIONPERSONS = up.id  \n" +
                    "   and rw.rec_id_parent = ?\n" +
                    "   and rw.begin_date <= ?" +
                    "   and (rw.end_date is null or rw.end_date > ?)" +
                    "   and up.idn = ?\n" +
                    "   and up.begin_date = (select max(up1.begin_date) \n" +
                    "                         from V_REF_UNIONPERSONS up1 \n" +
                    "                        where up1.rec_id = up.rec_id AND up1.TYPE=up.TYPE\n" +
                    "                          and up1.begin_date <= ?) \n" +
                    "   and (up.end_date is null or up.end_date > ?) \n");
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            ps.setDate(1, sqlDate);
            ps.setDate(2, sqlDate);
            ps.setLong(3, recId);
            ps.setDate(4, sqlDate);
            ps.setDate(5, sqlDate);
            ps.setString(6, idnChild);
            ps.setDate(7, sqlDate);
            ps.setDate(8, sqlDate);
            cursor = ps.executeQuery();

            while (cursor.next()) {
                result = cursor.getInt("CNT") > 0;
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, cursor);
        }
        return result;
    }

    @Override
    public void insertRespWarrant(RespondentWarrant respondentWarrant){
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement(
                    "insert into ref_respondent_warrant\n" +
                    "  (id, rec_id_parent, rec_id, begin_date, end_date, num)\n" +
                    "values\n" +
                    "  (seq_ref_respondent_warrant_id.nextval, ?, ?, ?, ?, ?)");
            ps.setLong(1, respondentWarrant.getRecIdParent());
            ps.setLong(2, respondentWarrant.getRecId());
            ps.setDate(3, new java.sql.Date(respondentWarrant.getbDate().getTime()));
            if(respondentWarrant.geteDate() != null) {
                ps.setDate(4, new java.sql.Date(respondentWarrant.geteDate().getTime()));
            }else{
                ps.setNull(4, OracleTypes.NULL);
            }
            ps.setString(5, respondentWarrant.getNum());
            ps.execute();
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps);
        }
    }

    @Override
    public void updateRespWarrant(RespondentWarrant respondentWarrant){
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement(
                    "update ref_respondent_warrant \n" +
                    "   set begin_date = ?,\n" +
                    "       end_date = ?,\n" +
                    "       num = ?,\n" +
                    "       rec_id = ?\n" +
                    " where id = ?");
            ps.setDate(1, new java.sql.Date(respondentWarrant.getbDate().getTime()));
            if(respondentWarrant.geteDate() != null) {
                ps.setDate(2, new java.sql.Date(respondentWarrant.geteDate().getTime()));
            }else{
                ps.setNull(2, OracleTypes.NULL);
            }
            ps.setString(3, respondentWarrant.getNum());
            ps.setLong(4, respondentWarrant.getRecId());
            ps.setLong(5, respondentWarrant.getId());
            ps.execute();
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps);
        }
    }

    @Override
    public void deleteWarrant(Long warrantId){
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement(
                    "delete ref_respondent_warrant where id = ?");
            ps.setLong(1, warrantId);
            ps.execute();
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps);
        }
    }

    @Override
    public Boolean submitReportsByWarrant(Long recIdParent, Long recIdChild){
        Boolean result = null;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet cursor = null;

        try {
            connection = getConnection();
            ps = connection.prepareStatement(
                    "select count(rhs.id) cnt\n" +
                    "  from reports r,\n" +
                    "       report_history rh,\n" +
                    "       report_history_statuses rhs,\n" +
                    "       v_ref_respondent resp,\n" +
                    "       v_ref_unionpersons u,\n" +
                    "       v_ref_respondent respch,\n" +
                    "       v_ref_unionpersons uch\n" +
                    " where r.id = rh.report_id\n" +
                    "   and rh.id = rhs.report_history_id\n" +
                    "   and rhs.status_code = 'COMPLETED'\n" +
                    "   and resp.begin_date = (select max(resp1.begin_date)\n" +
                    "                         \t  from v_ref_respondent resp1\n" +
                    "                           where resp1.rec_id = resp.rec_id\n" +
                    "                             and resp1.begin_date <= sysdate)\n" +
                    "   and u.begin_date = (select max(u1.begin_date)\n" +
                    "                        from v_ref_unionpersons u1\n" +
                    "                       where u1.rec_id = u.rec_id\n" +
                    "                         and u1.type = u.type\n" +
                    "                         and u1.begin_date <= sysdate)\n" +
                    "                         \n" +
                    "   and resp.ref_unionpersons = u.id\n" +
                    "   and resp.rec_id = ?\n" +
                    "   and r.idn = u.idn   \n" +
                    "   and respch.begin_date = (select max(respch1.begin_date)\n" +
                    "                         \t  from v_ref_respondent respch1\n" +
                    "                           where respch1.rec_id = respch.rec_id\n" +
                    "                             and respch1.begin_date <= sysdate)\n" +
                    "   and uch.begin_date = (select max(uch1.begin_date)\n" +
                    "                        from v_ref_unionpersons uch1\n" +
                    "                       where uch1.rec_id = uch.rec_id\n" +
                    "                         and uch1.type = uch.type\n" +
                    "                         and uch1.begin_date <= sysdate)\n" +
                    "\n" +
                    "   and respch.REF_UNIONPERSONS = uch.id\n" +
                    "   and respch.rec_id = ?\n" +
                    "   and r.idn_child = uch.idn\n" +
                    "   \n" +
                    "       ");
            ps.setLong(1, recIdParent);
            ps.setLong(2, recIdChild);
            cursor = ps.executeQuery();

            while (cursor.next()) {
                result = cursor.getInt("CNT") > 0;
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, cursor);
        }
        return result;
    }

    // Справочник Эмитентов
    @Override
    public List<RefIssuersItem> getIssuers(Date date) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefIssuersItem> result = new ArrayList<RefIssuersItem>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT * FROM v_ref_issuers t WHERE begin_date = (SELECT MAX(begin_date) FROM v_ref_issuers WHERE rec_id = t.rec_id AND begin_date <= ?) AND (? < end_date or end_date is null) ORDER BY name_ru");
            ps.setTimestamp(1, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
            rs = ps.executeQuery();
            while (rs.next()) {
                RefIssuersItem issuer = getIssuerFromResultSet(rs);
                result.add(issuer);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefIssuersItem> getIssuersByFilters(Date date, String name) {
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        List<RefIssuersItem> result = new ArrayList<RefIssuersItem>();
        try {
            connection = getConnection();
            StringBuilder sbQuery = new StringBuilder();
            sbQuery.append("SELECT * FROM v_ref_issuers t WHERE 1=1");
            if (date != null) {
                String toDate = getOracleDate(date);
                sbQuery.append(" AND begin_date = (SELECT MAX(begin_date) FROM v_ref_issuers WHERE rec_id = t.rec_id AND begin_date <= " + toDate + ")");
                sbQuery.append(" AND (" + toDate + " < end_date or end_date is null)");
            }
            if (name != null && !name.trim().isEmpty())
                sbQuery.append(" AND LOWER(name_ru) LIKE LOWER('%" + name + "%')");
            sbQuery.append(" ORDER BY name_ru");
            String query = sbQuery.toString();
            statement = connection.createStatement();
            rs = statement.executeQuery(query);
            while (rs.next()) {
                RefIssuersItem issuer = getIssuerFromResultSet(rs);
                result.add(issuer);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, statement, rs);
        }
        return result;
    }

    private RefIssuersItem getIssuerFromResultSet(ResultSet rs) throws SQLException {
        RefIssuersItem item = new RefIssuersItem();

        item.setId(rs.getLong("id"));
        item.setIdHst(null);
        item.setRecId(rs.getLong("rec_id"));
        item.setCode(rs.getString("code"));
        item.setNameEn(rs.getString("name_en"));
        item.setNameKz(rs.getString("NAME_KZ"));
        item.setNameRu(rs.getString("NAME_RU"));
        item.setShortNameRu(null);
        item.setBeginDate(rs.getDate("begin_date"));
        item.setEndDate(rs.getDate("end_date"));
        item.setUserId(rs.getLong("id_usr"));
        item.setUserName(null);
        item.setUserLocation(rs.getString("user_location"));
        item.setDelfl(rs.getLong("delfl"));
        item.setDatlast(rs.getDate("datlast"));
        item.setTypeChange(null);
        item.setTypeChangeName(null);
        item.setSentKnd(String.valueOf(rs.getLong("sent_knd")));
        item.setNote(null);
        item.setTag(null);

//		issuer.setState(rs.getLong("is_state") > 0L);
//		issuer.setResident(rs.getLong("is_resident") > 0L);
//		issuer.setSignCode(null);
//		issuer.setSignNameRu(null);

        return item;
    }

    // Справочник Ценных бумаг
    @Override
    public List<RefSecuritiesItem> getSecurities(Date date) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefSecuritiesItem> result = new ArrayList<RefSecuritiesItem>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT * FROM v_ref_securities t WHERE begin_date = (SELECT MAX(begin_date) FROM v_ref_securities WHERE rec_id = t.rec_id AND begin_date <= ?) AND (? < end_date or end_date is null) ORDER BY issuer_name, nin");
            ps.setTimestamp(1, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
            rs = ps.executeQuery();
            while (rs.next()) {
                RefSecuritiesItem security = getSecurityFromResultSet(rs);
                result.add(security);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefSecuritiesItem> getSecuritiesByIssuerId(Date date, Long issuerId) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefSecuritiesItem> result = new ArrayList<RefSecuritiesItem>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT * FROM v_ref_securities t WHERE begin_date = (SELECT MAX(begin_date) FROM v_ref_securities WHERE rec_id = t.rec_id AND begin_date <= ?) AND (? < end_date or end_date is null) AND s_issuer = ? AND ORDER BY nin");
            ps.setTimestamp(1, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setLong(3, issuerId);
            rs = ps.executeQuery();
            while (rs.next()) {
                RefSecuritiesItem security = getSecurityFromResultSet(rs);
                result.add(security);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefSecuritiesItem> getSecuritiesByNin(Date date, String nin) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefSecuritiesItem> result = new ArrayList<RefSecuritiesItem>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT * FROM v_ref_securities t WHERE begin_date = (SELECT MAX(begin_date) FROM v_ref_securities WHERE rec_id = t.rec_id AND begin_date <= ?) AND (? < end_date or end_date is null) AND nin = ?");
            ps.setTimestamp(1, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setString(3, nin);
            rs = ps.executeQuery();
            while (rs.next()) {
                RefSecuritiesItem security = getSecurityFromResultSet(rs);
                result.add(security);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefSecuritiesItem> getSecuritiesByRecId(Date date, Long recId) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefSecuritiesItem> result = new ArrayList<RefSecuritiesItem>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT * FROM v_ref_securities t WHERE begin_date = (SELECT MAX(begin_date) FROM v_ref_securities WHERE rec_id = t.rec_id AND begin_date <= ?) AND (? < end_date or end_date is null) AND rec_id = ?");
            ps.setTimestamp(1, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setLong(3, recId);
            rs = ps.executeQuery();
            while (rs.next()) {
                RefSecuritiesItem security = getSecurityFromResultSet(rs);
                result.add(security);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefSecuritiesItem> getPagingSecuritiesByNin(Date date, String nin, int offset, int limit) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefSecuritiesItem> result = new ArrayList<RefSecuritiesItem>();
        try {
            connection = getConnection();
            String filter = (nin != null && !nin.trim().isEmpty()) ? "AND nin like  '" + nin.trim() + "%'" : "";
            String sql = "SELECT *\n" +
                    "FROM v_ref_securities t\n" +
                    "WHERE begin_date = (SELECT MAX(begin_date)\n" +
                    "                    FROM v_ref_securities\n" +
                    "                    WHERE rec_id = t.rec_id AND begin_date <= ?) AND (? < end_date OR end_date IS NULL) " + filter + "\n" +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            ps = connection.prepareStatement(sql);
            ps.setTimestamp(1, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setInt(3, offset);
            ps.setInt(4, limit);
            rs = ps.executeQuery();
            while (rs.next()) {
                RefSecuritiesItem security = getSecurityFromResultSet(rs);
                result.add(security);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public int getSecuritiesCount(Date date, String nin) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int result = 0;
        try {
            connection = getConnection();
            String filter = (nin != null && !nin.trim().isEmpty()) ? "AND nin like  '" + nin.trim() + "%'" : "";
            String sql = "SELECT count(*) cnt\n" +
                    "FROM v_ref_securities t\n" +
                    "WHERE begin_date = (SELECT MAX(begin_date)\n" +
                    "                    FROM v_ref_securities\n" +
                    "                    WHERE rec_id = t.rec_id AND begin_date <= ?) AND (? < end_date OR end_date IS NULL) " + filter;
            ps = connection.prepareStatement(sql);
            ps.setTimestamp(1, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
            rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getInt("cnt");
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    private RefSecuritiesItem getSecurityFromResultSet(ResultSet rs) throws SQLException {
        RefSecuritiesItem security = new RefSecuritiesItem();

        security.setId(rs.getLong("id"));
        security.setIdHst(null);
        security.setRecId(rs.getLong("rec_id"));
        security.setCode(rs.getString("code"));
        security.setNameEn(rs.getString("name_en"));
        security.setNameKz(rs.getString("NAME_KZ"));
        security.setNameRu(rs.getString("NAME_RU"));
        security.setShortNameRu(null);
        security.setBeginDate(rs.getDate("begin_date"));
        security.setEndDate(rs.getDate("end_date"));
        security.setUserId(rs.getLong("id_usr"));
        security.setUserName(null);
        security.setUserLocation(rs.getString("user_location"));
        security.setDelfl(rs.getLong("delfl"));
        security.setDatlast(rs.getDate("datlast"));
        security.setTypeChange(null);
        security.setTypeChangeName(null);
        security.setSentKnd(String.valueOf(rs.getLong("sent_knd")));
        security.setNote(null);
        security.setTag(null);

        security.setNin(rs.getString("nin"));
        security.setNominalValue(rs.getLong("nominal_value"));
        security.setNominalCurrency(rs.getLong("nominal_currency"));
        security.setCurrencyRecId(rs.getLong("currency_rec_id"));
        security.setCurrencyCode(rs.getString("currency_code"));
        security.setCurrencyName(rs.getString("currency_name"));
        security.setSgSecurityVariety(rs.getLong("s_g_security_variety"));
        security.setSecVarRecId(rs.getLong("sec_var_rec_id"));
        security.setVarietyCode(rs.getString("variety_code"));
        security.setVarietyName(rs.getString("variety_name"));
        security.setSgSecurityType(rs.getLong("s_g_security_type"));
        security.setTypeCode(rs.getString("type_code"));
        security.setTypeName(rs.getString("type_name"));
        security.setCirculDate(rs.getDate("circul_date"));
        security.setMaturityDate(rs.getDate("maturity_date"));

        security.setIssuerName(rs.getString("issuer_name"));
        security.setsIssuer(rs.getLong("s_issuer"));
        security.setIsState(rs.getLong("is_state") > 0L);
        security.setIsResident(rs.getLong("is_resident") > 0L);
//		issuer.setCountryCode(rs.getString("country_code"));
//		issuer.setCountryNameRu(rs.getString("country_name"));
        security.setSignCode(rs.getString("sign_code"));
        security.setSignName(rs.getString("sign_name"));

        return security;
    }


    // Справочник межформенных контролей
    private RefCrosscheckItem getRefCrosscheckFromRS(ResultSet cursor) throws SQLException {
        RefCrosscheckItem ref_crosscheck = new RefCrosscheckItem();

        ref_crosscheck.setId(cursor.getLong("ID"));
        ref_crosscheck.setRecId(cursor.getLong("REC_ID"));
        ref_crosscheck.setFormulaL(cursor.getString("L_REL_FORMULA"));
        ref_crosscheck.setFormulaR(cursor.getString("R_REL_FORMULA"));
        ref_crosscheck.setDescrRuL(cursor.getString("L_DESC"));
        ref_crosscheck.setDescrRuR(cursor.getString("R_DESC"));
        ref_crosscheck.setConditionL(cursor.getString("L_REL_COND"));
        ref_crosscheck.setConditionR(cursor.getString("R_REL_COND"));
        ref_crosscheck.setFormulaSymbol(cursor.getString("FORMULA_SYMBOL"));
        ref_crosscheck.setConditionSymbol(cursor.getString("COND_SYMBOL"));
        ref_crosscheck.setCrosscheckType(cursor.getLong("CROSSCHECK_TYPE"));
        ref_crosscheck.setCrossTypeName(cursor.getString("CROSS_TYPE_NAME"));
        ref_crosscheck.setNum(cursor.getLong("NUM"));
        ref_crosscheck.setIsAvailable(cursor.getLong("IS_AVAILABLE") > 0);
        ref_crosscheck.setBeginDate(cursor.getDate("BEGIN_DATE"));
        ref_crosscheck.setEndDate(cursor.getDate("END_DATE"));
        ref_crosscheck.setDatlast(cursor.getTimestamp("DATLAST"));
        ref_crosscheck.setUserName(cursor.getString("USER_NAME"));
        ref_crosscheck.setUserLocation(cursor.getString("USER_LOCATION"));

        return ref_crosscheck;
    }

    //todo поменять реализацию после того как создастся дочерняя таблица
    @Override
    public List<RefCrosscheckItem> getRefCrosscheckListByFormCodeDateSubjectType(String formCode, Date date_, boolean forSuperUser, Long subjectTypeRecId, boolean extSysControls) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefCrosscheckItem> result = new ArrayList<RefCrosscheckItem>();
        try {
            connection = getConnection();
            StringBuilder sql = new StringBuilder("select c.id,\n" +
                    "             c.rec_id,             \n" +
                    "             c.l_rel_formula,\n" +
                    "             c.r_rel_formula,\n" +
                    "             c.crosscheck_type,\n" +
                    "             t.name cross_type_name,\n" +
                    "             c.l_desc,             \n" +
                    "             c.r_desc,             \n" +
                    "             c.L_REL_COND,             \n" +
                    "             c.R_REL_COND,             \n" +
                    "             c.FORMULA_SYMBOL,             \n" +
                    "             c.COND_SYMBOL,             \n" +
                    "             c.num,\n" +
                    "             c.is_available,\n" +
                    "             c.begin_date,\n" +
                    "             c.end_date,\n" +
                    "             c.datlast,\n" +
                    "             c.id_usr,\n" +
                    "             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,    \n" +
                    "             c.user_location\n" +
                    "        from v_ref_crosscheck c,\n" +
                    "             crosscheck_type t,\n" +
                    "             f_users u " +
                    "       where c.id_usr = u.user_id         \n" +
                    "         and c.crosscheck_type = t.id " +
                    "		  and c.begin_date = (select max(c1.begin_date) from v_ref_crosscheck c1 where c1.rec_id = c.rec_id and c1.begin_date <= ?) " +
                    "		  and (c.end_date is null or c.end_date > ?) " +
                    "         and (case when ? = 1 then case when t.id = 3 then 1 else 0 end else 1 end) = 1");
            sql.append("         and exists (\n" +
                    " select c2.ref_crosscheck_id from ref_crosscheck_forms c2 left join Subjecttype_Forms sf on lower(c2.form_code)=lower(sf.form_code) and sf.ref_subject_type_rec_id=?" +
                    " and pkg_frsi_util.check_period((select p.code from rep_per_dur_months p where p.id=sf.period_id), ?)=1 \n" +
                    " where c2.ref_crosscheck_id=c.id  and exists (select * from ref_crosscheck_forms f where lower(f.form_code)=lower(?) and f.ref_crosscheck_id=c2.ref_crosscheck_id) \n" +
                    " and not EXISTS ( SELECT * FROM ref_crosscheck_forms f, forms fr WHERE f.FORM_CODE=fr.CODE and fr.TYPE_CODE in ('OUTPUT', 'CONSOLIDATED') AND f.ref_crosscheck_id=c2.ref_crosscheck_id) \n" +
                    " group by c2.ref_crosscheck_id\n" +
                    " having min(case when sf.id is null then 0 else 1 end)>0\n" +
                    " ) ");
            if (!forSuperUser) {
                sql.append(" and is_available = 1");
            } else {
                sql.append("         and exists (select 'x' from ref_crosscheck_forms cf where lower(cf.form_code)=lower(?) and cf.ref_crosscheck_id=c.id) ");
            }
            ps = connection.prepareStatement(sql.toString());
            int paramIndex = 0;
            ps.setDate(++paramIndex, new java.sql.Date(date_.getTime()));
            ps.setDate(++paramIndex, new java.sql.Date(date_.getTime()));
            ps.setInt(++paramIndex, extSysControls ? 1 : 0);
            ps.setLong(++paramIndex, subjectTypeRecId);
            ps.setDate(++paramIndex, new java.sql.Date(date_.getTime()));
            ps.setString(++paramIndex, formCode);
            if (forSuperUser)
                ps.setString(++paramIndex, formCode);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(getRefCrosscheckFromRS(rs));
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefCrosscheckItem> getRefCrosscheckListByFormCodeDate(String formCode, Date date_, boolean forSuperUser, boolean extSysControls) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefCrosscheckItem> result = new ArrayList<RefCrosscheckItem>();
        try {
            connection = getConnection();
            StringBuilder sql = new StringBuilder("select c.id,\n" +
                    "             c.rec_id,             \n" +
                    "             c.l_rel_formula,\n" +
                    "             c.r_rel_formula,\n" +
                    "             c.crosscheck_type,\n" +
                    "             t.name cross_type_name,\n" +
                    "             c.l_desc,             \n" +
                    "             c.r_desc,             \n" +
                    "             c.L_REL_COND,             \n" +
                    "             c.R_REL_COND,             \n" +
                    "             c.FORMULA_SYMBOL,             \n" +
                    "             c.COND_SYMBOL,             \n" +
                    "             c.num,\n" +
                    "             c.is_available,\n" +
                    "             c.begin_date,\n" +
                    "             c.end_date,\n" +
                    "             c.datlast,\n" +
                    "             c.id_usr,\n" +
                    "             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,    \n" +
                    "             c.user_location\n" +
                    "        from v_ref_crosscheck c,\n" +
                    "             crosscheck_type t,\n" +
                    "             f_users u " +
                    "       where c.id_usr = u.user_id         \n" +
                    "         and c.crosscheck_type = t.id " +
                    "		  and c.begin_date = (select max(c1.begin_date) from v_ref_crosscheck c1 where c1.rec_id = c.rec_id and c1.begin_date <= ?) " +
                    "	      and (c.end_date is null or c.end_date > ?) " +
                    "         and (case when ? = 1 then case when t.id = 3 then 1 else 0 end else 1 end) = 1 ");
            sql.append("         and exists (\n" +
                    " select c2.ref_crosscheck_id from ref_crosscheck_forms c2 left join Subjecttype_Forms sf on c2.form_code=sf.form_code " +
                    " and pkg_frsi_util.check_period((select p.code from rep_per_dur_months p where p.id=sf.period_id), ?)=1 \n" +
                    " where c2.ref_crosscheck_id=c.id  and exists (select * from ref_crosscheck_forms f where f.form_code=? and f.ref_crosscheck_id=c2.ref_crosscheck_id) \n" +
                    " group by c2.ref_crosscheck_id\n" +
                    " having min(case when sf.id is null then 0 else 1 end)>0\n" +
                    " ) ");
            if (!forSuperUser) {
                sql.append(" and is_available = 1");
            } else {
                sql.append("         and exists (select 'x' from ref_crosscheck_forms cf where cf.form_code=? and cf.ref_crosscheck_id=c.id) ");
            }
            ps = connection.prepareStatement(sql.toString());
            int paramIndex = 0;
            ps.setDate(++paramIndex, new java.sql.Date(date_.getTime()));
            ps.setDate(++paramIndex, new java.sql.Date(date_.getTime()));
            ps.setInt(++paramIndex, extSysControls ? 1 : 0);
            ps.setDate(++paramIndex, new java.sql.Date(date_.getTime()));
            ps.setString(++paramIndex, formCode);
            if (forSuperUser)
                ps.setString(++paramIndex, formCode);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(getRefCrosscheckFromRS(rs));
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefCrosscheckForm> getRefCrosscheckForms(Long refCrosscheckId) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefCrosscheckForm> result = new ArrayList<RefCrosscheckForm>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT f.*, (SELECT fr.TYPE_CODE\n" +
                    "             FROM FORMS fr WHERE fr.CODE=f.FORM_CODE) type_code\n" +
                    "FROM ref_crosscheck_forms f \n" +
                    "WHERE f.ref_crosscheck_id = ?");
            ps.setLong(1, refCrosscheckId);
            rs = ps.executeQuery();
            while (rs.next()) {
                RefCrosscheckForm form = new RefCrosscheckForm();
                form.setId(rs.getLong("ID"));
                form.setRefCrosscheckItemId(rs.getLong("REF_CROSSCHECK_ID"));
                form.setFormCode(rs.getString("FORM_CODE"));
                form.setFormTypeCode(rs.getString("TYPE_CODE"));
                result.add(form);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    // Справочник правил выходных форм
    @Override
    public List<RefReportsRulesItem> getRefReportsRulesListByFormCodeDate(String formCode, Date date_) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefReportsRulesItem> result = new ArrayList<RefReportsRulesItem>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("select rr.id, " +
                    "             rr.rec_id, " +
                    "             rr.code, " +
                    "             rr.name_kz, " +
                    "             rr.name_ru, " +
                    "             rr.name_en, " +
                    "             rr.formname, " +
                    "             rr.fieldname, " +
                    "             rr.formula, " +
                    "             rr.coeff, " +
                    "             rr.condition, " +
                    "             rr.priority, " +
                    "             rr.begin_date, " +
                    "             rr.end_date, " +
                    "             rr.datlast, " +
                    "             rr.id_usr, " +
                    "             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME, " +
                    "             rr.user_location, " +
                    "             sk.name as sent_knd," +
                    "			  rr.report_type," +
                    " 			  rt.name as report_type_name," +
                    "			  rr.report_kind," +
                    " 			  rk.name as report_kind_name," +
                    "			  rr.rep_per_dur_months," +
                    "		      dur.name as dur_name," +
                    "             dur.code as dur_code," +
                    "             rr.keyvalue," +
                    "			  rr.table_name" +
                    "        from v_ref_reports_rules rr," +
                    "			  report_type rt," +
                    "			  report_kind rk," +
                    "			  rep_per_dur_months dur, " +
                    "             f_users u, " +
                    "             sent_knd sk " +
                    "       where rr.id_usr = u.user_id " +
                    "		  and rr.report_type = rt.id" +
                    "         and rr.report_kind = rk.id" +
                    "         and rr.rep_per_dur_months = dur.id(+)" +
                    "         and rr.sent_knd = sk.sent_knd " +
                    "         and rr.formname=? " +
                    "         and (rr.begin_date = (select max(t.begin_date) " +
                    "                                                 from v_ref_reports_rules t" +
                    "                                                where t.rec_id = rr.rec_id " +
                    "                                                  and t.begin_date <= ?))" +
                    "         and (? < rr.end_date or rr.end_date is null)");
            ps.setString(1, formCode);
            ps.setDate(2, new java.sql.Date(date_.getTime()));
            ps.setDate(3, new java.sql.Date(date_.getTime()));
            rs = ps.executeQuery();
            while (rs.next()) {
                RefReportsRulesItem ref_reports_rules = new RefReportsRulesItem();
                ref_reports_rules.setId(rs.getLong("ID"));
                ref_reports_rules.setRecId(rs.getLong("REC_ID"));
                ref_reports_rules.setCode(rs.getString("CODE"));
                ref_reports_rules.setNameKz(rs.getString("NAME_KZ"));
                ref_reports_rules.setNameRu(rs.getString("NAME_RU"));
                ref_reports_rules.setNameEn(rs.getString("NAME_EN"));
                ref_reports_rules.setFormname(rs.getString("FORMNAME"));
                ref_reports_rules.setFieldname(rs.getString("FIELDNAME"));
                ref_reports_rules.setFormula(rs.getString("FORMULA"));
                ref_reports_rules.setCoeff(rs.getDouble("COEFF"));
                ref_reports_rules.setCondition(rs.getString("CONDITION"));
                ref_reports_rules.setPriority(rs.getInt("PRIORITY"));
                ref_reports_rules.setReportType(rs.getLong("REPORT_TYPE"));
                ref_reports_rules.setReportTypeName(rs.getString("REPORT_TYPE_NAME"));
                ref_reports_rules.setKeyValue(rs.getString("KEYVALUE"));
                ref_reports_rules.setReportKind(rs.getLong("REPORT_KIND"));
                ref_reports_rules.setReportKindName(rs.getString("REPORT_KIND_NAME"));
                ref_reports_rules.setRepPerDurMonths(rs.getLong("REP_PER_DUR_MONTHS"));
                ref_reports_rules.setDurName(rs.getString("DUR_NAME"));
                ref_reports_rules.setDurCode(rs.getString("DUR_CODE"));
                ref_reports_rules.setTableName(rs.getString("TABLE_NAME"));
                ref_reports_rules.setBeginDate(rs.getDate("BEGIN_DATE"));
                ref_reports_rules.setEndDate(rs.getDate("END_DATE"));
                ref_reports_rules.setDatlast(rs.getTimestamp("DATLAST"));
                ref_reports_rules.setUserName(rs.getString("USER_NAME"));
                ref_reports_rules.setUserLocation(rs.getString("USER_LOCATION"));
                ref_reports_rules.setSentKnd(rs.getString("SENT_KND"));
                result.add(ref_reports_rules);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefExtIndicatorItem> getExtIndicatorItemsByFilters(Date vDate, RefExtIndicatorItem filter) {
        List<RefExtIndicatorItem> resultList = new ArrayList<RefExtIndicatorItem>();
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet rs = null;
        Connection connection = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call frsi.TMP_PKG_FRSI_REF.REF_READ_EXTIND_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);

            if (filter == null || filter.getId() == null) {
                ocs.setNull(1, OracleTypes.NULL);
            } else
                ocs.setLong(1, filter.getId());

            if (vDate == null) {
                ocs.setNull(2, OracleTypes.NULL);
            } else
                ocs.setDate(2, new java.sql.Date(vDate.getTime()));

            if (filter == null || filter.getCode() == null) {
                ocs.setNull(3, OracleTypes.NULL);
            } else {
                ocs.setString(3, filter.getCode());
            }

            if (filter == null || filter.getExtSysId() == null) {
                ocs.setNull(4, OracleTypes.NULL);
            } else {
                ocs.setLong(4, filter.getExtSysId());
            }

            if (filter == null || filter.getId() == null) {
                ocs.setNull(5, OracleTypes.NULL);
            } else
                ocs.setLong(5, filter.getRecId());

            ocs.setNull(6, OracleTypes.NULL);

            ocs.registerOutParameter(7, OracleTypes.CURSOR);
            ocs.registerOutParameter(8, OracleTypes.INTEGER);
            ocs.registerOutParameter(9, OracleTypes.VARCHAR);
            ocs.execute();

            // получение курсора
            rs = ocs.getCursor(7);

            while (rs.next()) {
                RefExtIndicatorItem item = getRefRefExtIndicatorFromRS(rs);
                resultList.add(item);
            }

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, rs);
        }
        return resultList;
    }

    private RefExtIndicatorItem getRefRefExtIndicatorFromRS(ResultSet rs) throws SQLException {
        RefExtIndicatorItem item = new RefExtIndicatorItem();
        item.setId(rs.getLong("ID"));
        item.setRecId(rs.getLong("REC_ID"));
        Date beginDate = rs.getDate("BEGIN_DATE");
        if (beginDate != null)
            item.setBeginDate(beginDate);
        Date endDate = rs.getDate("END_DATE");
        if (endDate != null)
            item.setEndDate(endDate);
        item.setCode(rs.getString("CODE"));
        item.setExtSysId(rs.getLong("EXTSYS_ID"));
        item.setExtSysNameRu(rs.getString("extsys_name_ru"));
        item.setAlgorithm(rs.getString("ALG"));
        Clob alg = rs.getClob("ALG");
        if (alg != null) {
            item.setAlgorithm(alg.getSubString(1, (int) alg.length()));
            alg.free();
        }
        item.setValueType(rs.getString("VALUE_TYPE"));
        Timestamp datlast = rs.getTimestamp("DATLAST");
        if (datlast != null)
            item.setDatlast(datlast);
        item.setUserName(rs.getString("USER_NAME"));
        item.setUserLocation(rs.getString("USER_LOCATION"));
        return item;
    }

    @Override
    public List<RefPeriodArgument> getRefPeriodArguments(Long refPeriodId) {
        int Err_Code = 0;
        String Err_Msg = "";

        List<RefPeriodArgument> resultList = new ArrayList<RefPeriodArgument>();
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet rs = null;
        Connection connection = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call TMP_PKG_FRSI_REF.REF_READ_PERIOD_ARGS (?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);

            ocs.setLong(1, refPeriodId);

            ocs.registerOutParameter(2, OracleTypes.CURSOR);
            ocs.registerOutParameter(3, OracleTypes.INTEGER);
            ocs.registerOutParameter(4, OracleTypes.VARCHAR);
            ocs.execute();

            // получение курсора
            rs = ocs.getCursor(2);

            while (rs.next()) {
                RefPeriodArgument item = getRefPeriodArgumentFromRS(rs);
                resultList.add(item);
            }

            Err_Code = ocs.getInt(3);
            Err_Msg = ocs.getString(4);

        } catch (SQLException e) {
            throw new EJBException(e);
        } catch (Exception e){
            throw new EJBException(e);
        }
        finally {
            DbUtil.closeConnection(connection, stmt, rs);
        }
        return resultList;
    }

    private RefPeriodArgument getRefPeriodArgumentFromRS(ResultSet rs) throws SQLException, Exception {
        RefPeriodArgument item = new RefPeriodArgument();
        item.setRefPeriodId(rs.getLong("REF_PERIOD_ID"));
        item.setName(rs.getString("NAME"));
        String vt= rs.getString("VALUE_TYPE");
        if(vt != null) {
            item.setValueType(ValueType.valueOf(rs.getString("VALUE_TYPE")));

            Variant value = null;
            switch (item.getValueType()) {
                case STRING:
                    value = Variant.createString(rs.getString("STRING_VALUE"));
                    break;
                case BOOLEAN:
                    value = Variant.createBoolean(rs.getLong("BOOLEAN_VALUE") > 0);
                    break;
                case DATE:
                    value = Variant.createDate(rs.getDate("DATE_VALUE"));
                    break;
                case NUMBER_0:
                    value = Variant.createNumber0(rs.getLong("INTEGER_VALUE"));
                    break;
                case NUMBER_1:
                    value = Variant.createNumber1(rs.getDouble("REAL_VALUE"));
                    break;
                case NUMBER_2:
                    value = Variant.createNumber2(rs.getDouble("REAL_VALUE"));
                    break;
                case NUMBER_3:
                    value = Variant.createNumber3(rs.getDouble("REAL_VALUE"));
                    break;
                case NUMBER_4:
                    value = Variant.createNumber4(rs.getDouble("REAL_VALUE"));
                    break;
                case NUMBER_5:
                    value = Variant.createNumber5(rs.getDouble("REAL_VALUE"));
                    break;
                case NUMBER_6:
                    value = Variant.createNumber6(rs.getDouble("REAL_VALUE"));
                    break;
                case NUMBER_7:
                    value = Variant.createNumber7(rs.getDouble("REAL_VALUE"));
                    break;
                case NUMBER_8:
                    value = Variant.createNumber8(rs.getDouble("REAL_VALUE"));
                    break;
                default:
                    throw new IllegalStateException("Cant't cast value type: " + item.getValueType().name());
            }
            if (rs.wasNull()) {
                item.setValue(null);
            } else {
                item.setValue(value);
            }

            item.setStrValue(DataType.variantToString(item.getValue(), item.getValueType()));
        }

        return item;
    }

    @Override
    public void insertArguments(List<RefPeriodArgument> itemList, Long refPeriodId, Connection connection){
        boolean localCon = false;
        PreparedStatement ps = null;
        try {
            if (connection == null) { localCon = true; connection = getConnection(); }

            connection.setAutoCommit(false);

            deleteArguments(refPeriodId, connection);

            for(RefPeriodArgument item : itemList){
                if(item.getValueType() == null){
                    throw new SQLException("Выберите тип данных!");
                }
                insertArguments(item, refPeriodId, connection);
            }

            connection.commit();
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(localCon ? connection : null, ps);
        }
    }

    private void deleteArguments(Long refPeriodId, Connection connection){
        PreparedStatement ps = null;
        try{
            ps = connection.prepareStatement("delete ref_period_args where ref_period_id = " + refPeriodId);
            ps.execute();
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(null, ps);
        }
    }

    private void insertArguments(RefPeriodArgument item, Long refPeriodId, Connection connection){
        PreparedStatement ps = null;
        try{
            ps = connection.prepareStatement(
                    "insert into ref_period_args\n" +
                    "  (ref_period_id, name, value_type, " +
                    RefPeriodArgument.getColumn(item.getValueType()) + ")" +
                    " values (" + refPeriodId + "," + "'" + item.getName() + "'" + "," + "'" + item.getValueType().toString() + "'" + "," + "'" +
                    RefPeriodArgument.getValue(item.getValueType(), item.getStrValue()) + "'" + ")");
            ps.execute();
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(null, ps);
        }
    }

    @Override
    public void updateArgument(RefPeriodArgument item){
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = getConnection();

            ps = connection.prepareStatement(
                    "update ref_period_args" +
                    "   set value_type = ?, integer_value = ?, real_value = ?, boolean_value = ?, string_value = ?, date_value = ? " +
                    " where ref_period_id = ? and name = ? ");

            ps.setString(1, item.getValueType().toString());
            ps.setNull(2, OracleTypes.NULL);
            ps.setNull(3, OracleTypes.NULL);
            ps.setNull(4, OracleTypes.NULL);
            ps.setNull(5, OracleTypes.NULL);
            ps.setNull(6, OracleTypes.NULL);

            switch (item.getValueType()){
                case STRING:
                    ps.setString(5, item. getStrValue());
                    break;
                case BOOLEAN:
                    ps.setBoolean(4, item.getStrValue().trim().equalsIgnoreCase("true"));
                    break;
                case DATE:
                    ps.setDate(6, new java.sql.Date(Convert.getDateFromString(item.getStrValue()).getTime()));
                    break;
                case NUMBER_0:
                    ps.setInt(2, Integer.parseInt(item.getStrValue()));
                    break;
                case NUMBER_1:
                    ps.setDouble(3, Double.parseDouble(item.getStrValue()));
                    break;
                case NUMBER_2:
                    ps.setDouble(3, Double.parseDouble(item.getStrValue()));
                    break;
                case NUMBER_3:
                    ps.setDouble(3, Double.parseDouble(item.getStrValue()));
                    break;
                case NUMBER_4:
                    ps.setDouble(3, Double.parseDouble(item.getStrValue()));
                    break;
                case NUMBER_5:
                    ps.setDouble(3, Double.parseDouble(item.getStrValue()));
                    break;
                case NUMBER_6:
                    ps.setDouble(3, Double.parseDouble(item.getStrValue()));
                    break;
                case NUMBER_7:
                    ps.setDouble(3, Double.parseDouble(item.getStrValue()));
                    break;
                case NUMBER_8:
                    ps.setDouble(3, Double.parseDouble(item.getStrValue()));
                    break;
                default:
                    throw new IllegalStateException(MessageFormat.format("Unknown ValueType {0}", item.getValueType().name()));
            }
            ps.setLong(7, item.getRefPeriodId());
            ps.setString(8, item.getName());
            ps.execute();
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps);
        }
    }

    @Override
    public List<RefPeriodArgument> searchArgumentsFromAlg(Long refPeriodId, List<RefPeriodArgument> argumentList, String alg){
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            SqlParamParser parser = new SqlParamParser(alg);
            List<SqlParamParser.Param> sqlParams = parser.getParams();
            if(sqlParams.size() == 0){
                return argumentList;
            }

            Iterator<RefPeriodArgument> iterator = argumentList.iterator();
            while (iterator.hasNext()){
                RefPeriodArgument item = iterator.next();
                boolean found = false;
                for(SqlParamParser.Param sqlParam : sqlParams){
                    if (item.getName().equalsIgnoreCase(sqlParam.getName())){
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    iterator.remove();
                }
            }

            for (SqlParamParser.Param sqlParam : sqlParams) {
                boolean found = false;
                for(RefPeriodArgument item : argumentList) {
                    if (sqlParam.getName().equalsIgnoreCase(item.getName())){
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    RefPeriodArgument item = new RefPeriodArgument();
                    item.setName(sqlParam.getName());
                    item.setRefPeriodId(refPeriodId);
                    if(!RefPeriodArgument.equalsArg(item.getName())) {
                        argumentList.add(item);
                    }
                }
            }
            connection.commit();
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection);
        }
        return argumentList;
    }

    @Override
    public List<RefUnionPersonItem> getRefUnionPersonItemsByFilterPage(Date date_, RefUnionPersonItem filter, int offset, int limit) {
        int Err_Code = 0;
        String Err_Msg = "";

        List<RefUnionPersonItem> resultList = new ArrayList<RefUnionPersonItem>();
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet rs = null;
        Connection connection = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call TMP_PKG_FRSI_REF.REF_READ_UNIONPERSONS_BY_PAGE (?, ?, ?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);

            ocs.setDate(1, new java.sql.Date(date_.getTime()));
            if (filter.getNameRu() == null) {
                ocs.setNull(2, OracleTypes.NULL);
            } else {
                ocs.setString(2, filter.getNameRu());
            }
            if (filter.getIdn() == null) {
                ocs.setNull(3, OracleTypes.NULL);
            } else {
                ocs.setString(3, filter.getIdn());
            }
            if (filter.getType() == null) {
                ocs.setNull(4, OracleTypes.NULL);
            } else {
                ocs.setInt(4, filter.getType());
            }
            ocs.setInt(5, offset);
            ocs.setInt(6, limit);
            ocs.registerOutParameter(7, OracleTypes.CURSOR);
            ocs.registerOutParameter(8, OracleTypes.INTEGER);
            ocs.registerOutParameter(9, OracleTypes.VARCHAR);
            ocs.execute();

            // получение курсора
            rs = ocs.getCursor(7);

            while (rs.next()) {
                RefUnionPersonItem item = RefUnionPersonItem.setItemFromCursor(rs);
                resultList.add(item);
            }

            Err_Code = ocs.getInt(8);
            Err_Msg = ocs.getString(9);

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, rs);
        }
        return resultList;
    }

    @Override
    public int getRefUnionPersonItemsCount(Date date_, RefUnionPersonItem filter) {
        int Err_Code = 0;
        String Err_Msg = "";

        int count;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        Connection connection = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call TMP_PKG_FRSI_REF.get_unionpersons_count (?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);

            ocs.setDate(1, new java.sql.Date(date_.getTime()));
            if (filter.getNameRu() == null) {
                ocs.setNull(2, OracleTypes.NULL);
            } else {
                ocs.setString(2, filter.getNameRu());
            }
            if (filter.getIdn() == null) {
                ocs.setNull(3, OracleTypes.NULL);
            } else {
                ocs.setString(3, filter.getIdn());
            }
            if (filter.getType() == null) {
                ocs.setNull(4, OracleTypes.NULL);
            } else {
                ocs.setInt(4, filter.getType());
            }
            ocs.registerOutParameter(5, OracleTypes.INTEGER);
            ocs.registerOutParameter(6, OracleTypes.INTEGER);
            ocs.registerOutParameter(7, OracleTypes.VARCHAR);
            ocs.execute();

            count = ocs.getInt(5);

            Err_Code = ocs.getInt(6);
            Err_Msg = ocs.getString(7);

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt);
        }
        return count;
    }

    public List<RefUnionPersonItem> getRefLPTaxItemsByFilterPage(Date date_, RefUnionPersonItem filter, int offset, int limit) {
        int ErrCode = 0;
        String ErrMsg = "";

        List<RefUnionPersonItem> resultList = new ArrayList<RefUnionPersonItem>();
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet rs = null;
        Connection connection = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_READ_LP_IN_TAX (?, ?, ?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);

            ocs.setDate(1, new java.sql.Date(date_.getTime()));
            if (filter.getNameRu() == null) {
                ocs.setNull(2, OracleTypes.NULL);
            } else {
                ocs.setString(2, filter.getNameRu());
            }
            if (filter.getIdn() == null) {
                ocs.setNull(3, OracleTypes.NULL);
            } else {
                ocs.setString(3, filter.getIdn());
            }
            if (filter.getType() == null) {
                ocs.setNull(4, OracleTypes.NULL);
            } else {
                switch (filter.getType()){
                    case 0:
                        ocs.setNull(4, OracleTypes.NULL);
                        break;
                    case 1:
                        ocs.setInt(4, 1);
                        break;
                    case 2:
                        ocs.setInt(4, 0);
                        break;
                }
            }
            ocs.setInt(5, offset);
            ocs.setInt(6, limit);
            ocs.registerOutParameter(7, OracleTypes.CURSOR);
            ocs.registerOutParameter(8, OracleTypes.INTEGER);
            ocs.registerOutParameter(9, OracleTypes.VARCHAR);
            ocs.execute();

            // получение курсора
            rs = ocs.getCursor(7);

            while (rs.next()) {
                RefUnionPersonItem item = RefUnionPersonItem.setItemFromCursor(rs);
                item.setTax(true);
                resultList.add(item);
            }

            ErrCode = ocs.getInt(8);
            ErrMsg = ocs.getString(9);

            if (ErrCode != 0) throw new SQLException(ErrMsg);

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, rs, ocs);
        }
        return resultList;
    }

    @Override
    public int getRefLPTaxItemsCount(Date date_, RefUnionPersonItem filter) {
        int ErrCode = 0;
        String ErrMsg = "";

        int count;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        Connection connection = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.GET_LP_TAX_COUNT (?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);

            ocs.setDate(1, new java.sql.Date(date_.getTime()));
            if (filter.getNameRu() == null) {
                ocs.setNull(2, OracleTypes.NULL);
            } else {
                ocs.setString(2, filter.getNameRu());
            }
            if (filter.getIdn() == null) {
                ocs.setNull(3, OracleTypes.NULL);
            } else {
                ocs.setString(3, filter.getIdn());
            }
            if (filter.getType() == null) {
                ocs.setNull(4, OracleTypes.NULL);
            } else {
                switch (filter.getType()){
                    case 0:
                        ocs.setNull(4, OracleTypes.NULL);
                        break;
                    case 1:
                        ocs.setInt(4, 1);
                        break;
                    case 2:
                        ocs.setInt(4, 0);
                        break;
                }
            }
            ocs.registerOutParameter(5, OracleTypes.INTEGER);
            ocs.registerOutParameter(6, OracleTypes.INTEGER);
            ocs.registerOutParameter(7, OracleTypes.VARCHAR);
            ocs.execute();

            count = ocs.getInt(5);

            ErrCode = ocs.getInt(6);
            ErrMsg = ocs.getString(7);

            if (ErrCode != 0) throw new SQLException(ErrMsg);

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, null, ocs);
        }
        return count;
    }

    @Override
    public Long createNewPersonFromTax(Long recId, Long userId, String userLocation){
        int ErrCode = 0;
        String ErrMsg = "";

        Long result;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        Connection connection = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.CREATE_PERSON_FROM_TAX (?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);

            ocs.setLong(1,recId);
            ocs.setLong(2, userId);
            ocs.setString(3, userLocation);
            ocs.registerOutParameter(4, OracleTypes.INTEGER);
            ocs.registerOutParameter(5, OracleTypes.INTEGER);
            ocs.registerOutParameter(6, OracleTypes.VARCHAR);
            ocs.execute();

            result = ocs.getLong(4);

            ErrCode = ocs.getInt(5);
            ErrMsg = ocs.getString(6);

            if (ErrCode != 0) throw new SQLException(ErrMsg);

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, null, ocs);
        }
        return result;
    }

    @Override
    public RefUnionPersonItem getUnionPersonItemById(Long unionId) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        RefUnionPersonItem result = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("select * from REF_UNIONPERSONS where id = ?" );
            ps.setLong(1, unionId);

            rs = ps.executeQuery();
            while (rs.next()) {
                RefUnionPersonItem item = new RefUnionPersonItem();
                item.setId(unionId);
                item.setRecId(rs.getLong("REC_ID"));
                item.setType(rs.getInt("TYPE"));
                result = item;
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    @Override
    public List<RefUnionPersonItem> getUnionPersonItemList(Date date, String idn, String nameRu) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RefUnionPersonItem> result = new ArrayList<RefUnionPersonItem>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement(
                    "select u.* " +
                    "from V_REF_UNIONPERSONS u " +
                    "where u.begin_date <= ? " +
                    "  and (u.end_date is null or u.end_date > ?)" +
                    "  and (? is null or upper(u.name_ru) like upper('%" + nameRu + "%'))" +
                    "  and (? is null or u.idn = ?)");
            ps.setDate(1, new java.sql.Date(date.getTime()));
            ps.setDate(2, new java.sql.Date(date.getTime()));
            ps.setString(3, nameRu);
            ps.setString(4, idn);
            ps.setString(5, idn);
            rs = ps.executeQuery();
            while (rs.next()) {
                RefUnionPersonItem item = new RefUnionPersonItem();
                item.setId(rs.getLong("ID"));
                item.setRecId(rs.getLong("REC_ID"));
                item.setType(rs.getInt("TYPE"));
                item.setTypeName(item.getType() == 1 ? "Юр.лицо" : "Физ.лицо");
                item.setNameRu(rs.getString("NAME_RU"));
                item.setIdn(rs.getString("IDN"));
                item.setNameEn(rs.getString("NAME_EN"));
                item.setNameKz(rs.getString("NAME_KZ"));
                item.setIsNonRezident(rs.getBoolean("IS_NON_REZIDENT"));
                item.setRefCountry(rs.getLong("REFCOUNTRY"));
                item.setRefCountryRecId(rs.getLong("REFCOUNTRYRECID"));
                item.setCountryName(rs.getString("COUNTRY_NAME"));
                item.setLegalAddress(rs.getString("LEGAL_ADDRESS"));
                result.add(item);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }


    // праздничные дни
    @Override
    public List<HolidayItem> getRefHolidayList(Date date_) {
        int Err_Code = 0;
        String Err_Msg = "";
        List<HolidayItem> result = new ArrayList<HolidayItem>();

        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;

        try {
            connection = getConnection();
            // вызов процедуры
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_READ_HOLIDAY_LIST (?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            if (date_ == null) {
                ocs.setNull(1, OracleTypes.NULL);
            } else
                ocs.setDate(1, new java.sql.Date(date_.getTime()));
            ocs.registerOutParameter(2, OracleTypes.CURSOR);
            ocs.registerOutParameter(3, OracleTypes.INTEGER);
            ocs.registerOutParameter(4, OracleTypes.VARCHAR);
            ocs.execute();
            Err_Code = ocs.getInt(3);
        } catch (SQLException e) {
            throw new EJBException(e);
        }

        // получение курсора
        try {
            cursor = ocs.getCursor(2);
        } catch (SQLException e) {
            throw new EJBException(e);
        }
        try {
            // записываем в Bean, а затем добавляем в ArrayList
            while (cursor.next()) {
                HolidayItem ref_holiday = new HolidayItem();
                ref_holiday.setId(cursor.getLong("ID"));
                ref_holiday.setRecId(cursor.getLong("REC_ID"));
                ref_holiday.setNameDate(cursor.getString("NAME_DATE"));
                ref_holiday.setDayValue(cursor.getString("DAY_VALUE"));
                ref_holiday.setMesValue(cursor.getString("MES_VALUE"));
                ref_holiday.setStatus(cursor.getInt("STATUS"));
                ref_holiday.setTransferDays(cursor.getInt("TRANSFER_DAYS"));
                ref_holiday.setBeginDate(cursor.getDate("BEGIN_DATE"));
                ref_holiday.setEndDate(cursor.getDate("END_DATE"));
                ref_holiday.setDatlast(cursor.getTimestamp("DATLAST"));
                ref_holiday.setUserName(cursor.getString("USER_NAME"));
                ref_holiday.setUserLocation(cursor.getString("USER_LOCATION"));
                ref_holiday.setSentKnd(cursor.getString("SENT_KND"));
                ref_holiday.setIsChecked(true);
                result.add(ref_holiday);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, cursor, ocs);
        }
        return result;
    }


    @Override
    public List<HolidayItem> getRefHolidayListByParams(Date date, HolidayItem filterRefHolidayItem) {
        int Err_Code = 0;
        String Err_Msg = "";
        List<HolidayItem> result = new ArrayList<HolidayItem>();

        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;

        try {
            connection = getConnection();
            // вызов процедуры
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_READ_HLDAY_L_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            if (filterRefHolidayItem == null || filterRefHolidayItem.getId() == null) {
                ocs.setNull(1, OracleTypes.NULL);
            } else
                ocs.setLong(1, filterRefHolidayItem.getId());

            if (date == null) {
                ocs.setNull(2, OracleTypes.NULL);
            } else
                ocs.setDate(2, new java.sql.Date(date.getTime()));

            if (filterRefHolidayItem == null || filterRefHolidayItem.getNameDate() == null)
                ocs.setNull(3, OracleTypes.NULL);
            else
                ocs.setString(3, filterRefHolidayItem.getNameDate());

            if (filterRefHolidayItem == null || filterRefHolidayItem.getRecId() == null)
                ocs.setNull(4, OracleTypes.NULL);
            else
                ocs.setLong(4, filterRefHolidayItem.getRecId());

            if (filterRefHolidayItem == null || filterRefHolidayItem.getSearchAllVer() == null)
                ocs.setNull(5, OracleTypes.NULL);
            else
                ocs.setBoolean(5, filterRefHolidayItem.getSearchAllVer());

            ocs.registerOutParameter(6, OracleTypes.CURSOR);
            ocs.registerOutParameter(7, OracleTypes.INTEGER);
            ocs.registerOutParameter(8, OracleTypes.VARCHAR);
            ocs.execute();
            Err_Code = ocs.getInt(7);
        } catch (SQLException e) {
            throw new EJBException(e);
        }

        // получение курсора
        try {
            cursor = ocs.getCursor(6);
        } catch (SQLException e) {
            throw new EJBException(e);
        }
        try {
            // записываем в Bean, а затем добавляем в ArrayList
            while (cursor.next()) {
                HolidayItem ref_holiday = new HolidayItem();
                ref_holiday.setId(cursor.getLong("ID"));
                ref_holiday.setRecId(cursor.getLong("REC_ID"));
                ref_holiday.setNameDate(cursor.getString("NAME_DATE"));
                ref_holiday.setDayValue(cursor.getString("DAY_VALUE"));
                ref_holiday.setMesValue(cursor.getString("MES_VALUE"));
                ref_holiday.setStatus(cursor.getInt("STATUS"));
                ref_holiday.setTransferDays(cursor.getInt("TRANSFER_DAYS"));
                ref_holiday.setBeginDate(cursor.getDate("BEGIN_DATE"));
                ref_holiday.setEndDate(cursor.getDate("END_DATE"));
                ref_holiday.setDatlast(cursor.getTimestamp("DATLAST"));
                ref_holiday.setUserName(cursor.getString("USER_NAME"));
                ref_holiday.setUserLocation(cursor.getString("USER_LOCATION"));
                ref_holiday.setSentKnd(cursor.getString("SENT_KND"));
                ref_holiday.setIsChecked(true);
                result.add(ref_holiday);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, cursor, ocs);
        }

        return result;
    }

    @Override
    public HolidayItem getRefHolidayItem(Long id) {
        HolidayItem item = new HolidayItem();
        item.setId(id);
        return getRefHolidayListByParams(null, item).get(0);
    }

    public void updateRefHolidayItem(HolidayItem refHolidayItem) throws OracleException {
        int Err_Code = 0;
        String Err_Msg = " ";

        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;

        try {
            connection = getConnection();
            // вызов процедуры

            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_UPDATE_HOLIDAY (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            ocs.setLong(1, refHolidayItem.getId());
            ocs.setLong(2, refHolidayItem.getRecId());
            ocs.setString(3, refHolidayItem.getNameDate());
            ocs.setString(4, refHolidayItem.getDayValue());
            ocs.setString(5, refHolidayItem.getMesValue());
            ocs.setInt(6, refHolidayItem.getStatus());
            ocs.setInt(7, refHolidayItem.getTransferDays());
            // ocs.setDate(8, new java.sql.Date(refHolidayItem.getBeginDate().getTime()));
            //  ocs.setDate(9, refHolidayItem.getEndDate() == null ? null : new java.sql.Date(refHolidayItem.getEndDate().getTime()));
            ocs.setLong(8, refHolidayItem.getUserId() == null ? 0 : refHolidayItem.getUserId());
            ocs.setString(9, refHolidayItem.getUserLocation());
            ocs.setDate(10, new java.sql.Date(refHolidayItem.getDatlast().getTime()));
            ocs.setInt(11, 1);
            ocs.registerOutParameter(12, OracleTypes.INTEGER);
            ocs.registerOutParameter(13, OracleTypes.VARCHAR);
            ocs.execute();
            Err_Code = ocs.getInt(12);
            Err_Msg = ocs.getString(13);
            if (Err_Code != 0) throw new OracleException(Err_Msg);

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, null, ocs);
        }
    }

    //Заполнение справочника выходных и праздничных дней
    public void fillRefWkdHolidays(HolidayItem refHolidayItem, Date dateYear) throws OracleException {
        int Err_Code = 0;
        String Err_Msg = " ";

        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;

        try {
            connection = getConnection();
            // вызов процедуры

            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_FILL_WKD_HOLIDAY (?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            ocs.setLong(1, refHolidayItem.getUserId() == null ? 0 : refHolidayItem.getUserId());
            ocs.setString(2, refHolidayItem.getUserLocation());
            ocs.setDate(3, new java.sql.Date(refHolidayItem.getDatlast().getTime()));
            ocs.setInt(4, 1);
            ocs.setDate(5, new java.sql.Date(dateYear.getTime()));
            ocs.registerOutParameter(6, OracleTypes.INTEGER);
            ocs.registerOutParameter(7, OracleTypes.VARCHAR);
            ocs.execute();
            Err_Code = ocs.getInt(6);
            Err_Msg = ocs.getString(7);
            if (Err_Code != 0) throw new OracleException(Err_Msg);

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, null, ocs);
        }
    }

    @Override
    public List<RefExtIndicatorParam> getRefExtParamListByParams (Date date_, String algorithm, Long idRefExtInd) throws OracleException{
        int Err_Code = 0;
        String Err_Msg = " ";
        List<RefExtIndicatorParam>  extParamList = new ArrayList<RefExtIndicatorParam>();
        List<RefExtIndicatorParam>  extParamListDB = new ArrayList<RefExtIndicatorParam>();
        List<RefExtIndicatorParam>  extParamListRes = new ArrayList<RefExtIndicatorParam>();
        List<RefExtIndicatorParam>  extParamListItog = new ArrayList<RefExtIndicatorParam>();
        SqlParamParser parser = new SqlParamParser(algorithm);
        List<SqlParamParser.Param> sqlParams = parser.getParams();

        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;

        if (extParamList!=null && extParamList.size()!=0)
            extParamList.clear();
        if (extParamListDB!=null && extParamListDB.size()!=0)
            extParamListDB.clear();
        if (extParamListRes!=null && extParamListRes.size()!=0)
            extParamListRes.clear();
        if (extParamListItog!=null && extParamListItog.size()!=0)
            extParamListItog.clear();

        try {
            connection = getConnection();
            // вызов процедуры

            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_READ_EXTINDPARAM_LIST (?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);

            ocs.setDate(1, new java.sql.Date(date_.getTime()));
            ocs.setLong(2, idRefExtInd);
            ocs.registerOutParameter(3, OracleTypes.CURSOR);
            ocs.registerOutParameter(4, OracleTypes.INTEGER);
            ocs.registerOutParameter(5, OracleTypes.VARCHAR);
            ocs.execute();
            Err_Code = ocs.getInt(4);
            Err_Msg = ocs.getString(5);
            if (Err_Code != 0) throw new OracleException(Err_Msg);

        } catch (SQLException e) {
            throw new EJBException(e);
        }

        // получение курсора
        try {
           cursor = ocs.getCursor(3);
        } catch (SQLException e) {
           throw new EJBException(e);
        }
        try {
           // записываем в Bean, а затем добавляем в ArrayList
           while (cursor.next()) {
               RefExtIndicatorParam refExtParamItem = new  RefExtIndicatorParam();
               refExtParamItem.setId(cursor.getLong("ID"));
               refExtParamItem.setRef_extind_id(cursor.getLong("REF_EXTIND_ID"));
               refExtParamItem.setName(cursor.getString("NAME"));
               refExtParamItem.setValueType(cursor.getString("VALUE_TYPE"));
               extParamListDB.add(refExtParamItem);
           }
        } catch (SQLException e) {
           throw new EJBException(e);
        } finally {
           DbUtil.closeConnection(connection, stmt, cursor, ocs);
        }

        for (SqlParamParser.Param sqlParam : sqlParams) {
             RefExtIndicatorParam refExtParamItem = new  RefExtIndicatorParam();
             if (!sqlParam.getName().equalsIgnoreCase("result")) {  //check not editable params

               refExtParamItem.setRef_extind_id(idRefExtInd);
               refExtParamItem.setName(sqlParam.getName());
                 if (sqlParam.getName().equalsIgnoreCase("idn"))
                    refExtParamItem.setValueType("STRING");
                 else if (sqlParam.getName().equalsIgnoreCase("reportdate"))
                     refExtParamItem.setValueType("DATE");
                 else  refExtParamItem.setValueType("");
               refExtParamItem.setId(Long.parseLong(String.valueOf(sqlParam.getIndex())));
               extParamList.add(refExtParamItem);
             }
       }
       boolean mask[] = new boolean[extParamList.size()];

       for (int i = 0; i < extParamList.size(); i++) {
           if (!mask[i]) {
               String tmp = extParamList.get(i).getName();
               for (int j = i + 1; j < extParamList.size(); j++) {
                   if (tmp.equals(extParamList.get(j).getName())) {
                       mask[j] = true;
                   }
               }
           }
       }

       for (int i = 0, j = 0; i < extParamList.size(); i++) {
           if (!mask[i]) {
               extParamListRes.add(extParamList.get(i));
           }
       }
       //сортировка списков
       /* Collections.sort(extParamListDB, new Comparator<RefExtIndicatorParam>() {
               public int compare(RefExtIndicatorParam o1, RefExtIndicatorParam o2) {
                       return o1.getName().compareTo(o2.getName());
               }
        });

         */
        int countM = 0;
        boolean maskDel[] = new boolean[10];
        boolean maskIns[] = new boolean[10];

       if (extParamListDB.size() == 0)
           extParamListItog.addAll(extParamListRes);
       else if(extParamListDB!=null && extParamListDB.size()!=0) {
           for (int i = 0; i < extParamListRes.size(); i++) {
               countM=0;
               for (int j = 0; j < extParamListDB.size(); j++) {
                   if (extParamListDB.get(j).getName().equals(extParamListRes.get(i).getName()))  {
                       maskDel[j]=true;
                       countM++;
                   }
                   else {
                       maskIns [i]=true;
                   }
                   if (countM>0) maskIns[i] = false;
               }
           }

           for (int j = 0; j < extParamListDB.size(); j++) {
              if (!maskDel[j] && extParamListDB.size()>=extParamListRes.size())  {
                  extParamListDB.remove(j);
              }
           }
           for (int n = 0; n < extParamListRes.size(); n++) {
                 if (maskIns[n])  {
                     extParamListDB.add(extParamListRes.get(n));
                 }
            }

           extParamListItog.addAll(extParamListDB);
       }

       return extParamListItog;
    }

    @Override
    public Long insertRefExtIndicatorWithParam(RefExtIndicatorItem item, List<RefExtIndicatorParam> refExtIndList, AuditEvent auditEvent) throws OracleException {
            int ErrCode = 0;
            Long id = 0L;
            String ErrMsg = " ";
            Long result = null;
            Connection connection = null;
            CallableStatement stmt = null;
            OracleCallableStatement ocs = null;

            try {
             id = insertRefAbstractItem("ref_extind", item, auditEvent);

             if (id != 0L && id != null && refExtIndList!=null) {

                 connection = getConnection();

                 for (RefExtIndicatorParam param : refExtIndList) {
                     stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_INSERT_EXTINDPARAM (?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                     ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);

                     ocs.setLong(1, id);
                     ocs.setString(2, param.getName());
                     ocs.setString(3, param.getValueType());

                     ocs.setInt(4, 1);
                     ocs.registerOutParameter(5, OracleTypes.FLOAT);
                     ocs.registerOutParameter(6, OracleTypes.INTEGER);
                     ocs.registerOutParameter(7, OracleTypes.VARCHAR);
                     ocs.execute();
                     result = ocs.getLong(5);
                     ErrCode = ocs.getInt(6);
                     ErrMsg = ocs.getString(7);

                     if (ErrCode != 0) throw new OracleException(ErrMsg);

                 }
             }
            } catch (SQLException ex) {
                throw new EJBException(ex);
            } finally {
                DbUtil.closeConnection(connection, null, null, ocs);
            }

            return result;
    }

    @Override
    public void updateRefExtIndicatorWithParam(RefExtIndicatorItem item, List<RefExtIndicatorParam> refExtIndList, AuditEvent auditEvent) throws OracleException {
        int ErrCode = 0;
        Long id = 0L;
        String ErrMsg = " ";
        Long result = null;
        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;

        try {
         updateRefAbstractItem("ref_extind", item, auditEvent);

         connection = getConnection();

         for (RefExtIndicatorParam param : refExtIndList) {
             stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_INSERT_EXTINDPARAM (?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);

             ocs.setLong(1, param.getRef_extind_id());
             ocs.setString(2, param.getName());
             ocs.setString(3, param.getValueType());

             ocs.setInt(4, 1);
             ocs.registerOutParameter(5, OracleTypes.FLOAT);
             ocs.registerOutParameter(6, OracleTypes.INTEGER);
             ocs.registerOutParameter(7, OracleTypes.VARCHAR);
             ocs.execute();
             result = ocs.getLong(5);
             ErrCode = ocs.getInt(6);
             ErrMsg = ocs.getString(7);

             if (ErrCode != 0) throw new OracleException(ErrMsg);
         }

        } catch (SQLException ex) {
            throw new EJBException(ex);
        } finally {
            DbUtil.closeConnection(connection, null, null, ocs);
        }


    }

    //Методы для подсправочника крупных участников
    @Override
    public List<RefMajorMemberOrgItem> getRefMajorMemOrgListByParam(RefMajorMemberOrgItem item) {
        List<RefMajorMemberOrgItem> result = new ArrayList<RefMajorMemberOrgItem>();
        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_READ_MMORG_LIST_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            if (item == null) {
                throw new SQLException("Пустые данные - не допустимы !");
            }
            ocs.setString(1, item.getRefCode());
           /* if (date != null)
                ocs.setDate(2, new java.sql.Date(date.getTime()));
            else   */
                ocs.setNull(2, OracleTypes.NULL);
            if (item.getId() != null)
                ocs.setLong(3, item.getId());
            else
                ocs.setNull(3, OracleTypes.NULL);
            if (item.getRecId() != null)
                ocs.setLong(4, item.getRecId());
            else
                ocs.setNull(4, OracleTypes.NULL);
            if (item.getNameRu() != null)
                ocs.setString(5, item.getNameRu());
            else
                ocs.setNull(5, OracleTypes.NULL);
            if (item.getCode() != null)
                ocs.setString(6, item.getCode());
            else
                ocs.setNull(6, OracleTypes.NULL);
            if (item.getSearchAllVer() != null)
                ocs.setBoolean(7, item.getSearchAllVer());
            else
                ocs.setNull(7, OracleTypes.NULL);

            ocs.registerOutParameter(8, OracleTypes.CURSOR);
            ocs.registerOutParameter(9, OracleTypes.INTEGER);
            ocs.registerOutParameter(10, OracleTypes.VARCHAR);
            ocs.execute();
            item.setErrCode(ocs.getLong(9));
            item.setErrMsg(ocs.getString(10));
        } catch (SQLException e) {
            throw new EJBException(e);
        }
        try {
            cursor = ocs.getCursor(8);
        } catch (SQLException e) {
            throw new EJBException(e);
        }
        try {
            while (cursor.next()) {
                RefMajorMemberOrgItem refMMOrgitem = new RefMajorMemberOrgItem();
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
                result.add(refMMOrgitem);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, cursor, ocs);
        }
        return result;


    }

    // Методы для обычных справочников
    @Override
    public List<SimpleReference> getRefSimpleListByParams(Date date, SimpleReference refSimpleItem) {
        List<SimpleReference> result = new ArrayList<SimpleReference>();
        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_READ_SIMPLE_LIST_BY_PARAMS (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            if (refSimpleItem == null) {
                throw new SQLException("Пустые данные - не допустимы !");
            }
            ocs.setString(1, refSimpleItem.getRefCode());
            if (date != null)
                ocs.setDate(2, new java.sql.Date(date.getTime()));
            else
                ocs.setNull(2, OracleTypes.NULL);
            if (refSimpleItem.getId() != null)
                ocs.setLong(3, refSimpleItem.getId());
            else
                ocs.setNull(3, OracleTypes.NULL);
            if (refSimpleItem.getRecId() != null)
                ocs.setLong(4, refSimpleItem.getRecId());
            else
                ocs.setNull(4, OracleTypes.NULL);
            if (refSimpleItem.getNameRu() != null)
                ocs.setString(5, refSimpleItem.getNameRu());
            else
                ocs.setNull(5, OracleTypes.NULL);
            if (refSimpleItem.getCode() != null)
                ocs.setString(6, refSimpleItem.getCode());
            else
                ocs.setNull(6, OracleTypes.NULL);
            if (refSimpleItem.getSearchAllVer() != null)
                ocs.setBoolean(7, refSimpleItem.getSearchAllVer());
            else
                ocs.setNull(7, OracleTypes.NULL);

            ocs.registerOutParameter(8, OracleTypes.CURSOR);
            ocs.registerOutParameter(9, OracleTypes.INTEGER);
            ocs.registerOutParameter(10, OracleTypes.VARCHAR);
            ocs.execute();
            refSimpleItem.setErrCode(ocs.getLong(9));
            refSimpleItem.setErrMsg(ocs.getString(10));
        } catch (SQLException e) {
            throw new EJBException(e);
        }
        try {
            cursor = ocs.getCursor(8);
        } catch (SQLException e) {
            throw new EJBException(e);
        }
        try {
            while (cursor.next()) {
                SimpleReference item = new SimpleReference();
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
                result.add(item);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, cursor, ocs);
        }
        return result;
    }

    @Override
    public List<SimpleReference> getRefSimpleHstList(Long id, String refCode) {
        List<SimpleReference> result = new ArrayList<SimpleReference>();
        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_READ_SIMPLE_HST_LIST (?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            ocs.setString(1, refCode);
            ocs.setLong(2, id);
            ocs.registerOutParameter(3, OracleTypes.CURSOR);
            ocs.registerOutParameter(4, OracleTypes.INTEGER);
            ocs.registerOutParameter(5, OracleTypes.VARCHAR);
            ocs.execute();
        } catch (SQLException e) {
            throw new EJBException(e);
        }
        try {
            cursor = ocs.getCursor(3);
        } catch (SQLException e) {
            throw new EJBException(e);
        }
        try {
            while (cursor.next()) {
                SimpleReference item = new SimpleReference();
                item.setIdHst(cursor.getLong("ID_HST"));
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
                item.setTypeChange(cursor.getLong("TYPE_CHANGE"));
                item.setTypeChangeName(cursor.getString("TYPE_CHANGE_NAME"));
                item.setUserLocation(cursor.getString("USER_LOCATION"));
                item.setSentKnd(cursor.getString("SENT_KND"));
                result.add(item);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, cursor, ocs);
        }
        return result;
    }

    @Override
    public SimpleReference getRefSimpleItem(Long id, String refCode) {
        SimpleReference item = new SimpleReference();
        item.setId(id);
        item.setRefCode(refCode);
        return getRefSimpleListByParams(null, item).get(0);
    }

    @Override
    public Long insertRefSimpleItem(SimpleReference refSimpleItem) throws OracleException {
        int Err_Code = 0;
        String Err_Msg = " ";
        Long result = null;
        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_INSERT_SIMPLE (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            ocs.setString(1, refSimpleItem.getRefCode());
            if (refSimpleItem.getRecId() != null) {
                ocs.setLong(2, refSimpleItem.getRecId());
            } else
                ocs.setNull(2, OracleTypes.NULL);
            ocs.setString(3, refSimpleItem.getCode());
            ocs.setString(4, refSimpleItem.getNameKz());
            ocs.setString(5, refSimpleItem.getNameRu());
            ocs.setString(6, refSimpleItem.getNameEn());
            ocs.setDate(7, new java.sql.Date(refSimpleItem.getBeginDate().getTime()));
            ocs.setDate(8, refSimpleItem.getEndDate() == null ? null : new java.sql.Date(refSimpleItem.getEndDate().getTime()));
            ocs.setLong(9, refSimpleItem.getUserId());
            ocs.setString(10, refSimpleItem.getUserLocation());
            ocs.setDate(11, refSimpleItem.getDatlast() == null ? null : new java.sql.Date(refSimpleItem.getDatlast().getTime()));
            ocs.setInt(12, 1);
            ocs.registerOutParameter(13, OracleTypes.FLOAT);
            ocs.registerOutParameter(14, OracleTypes.INTEGER);
            ocs.registerOutParameter(15, OracleTypes.VARCHAR);
            ocs.execute();
            result = ocs.getLong(13);
            Err_Code = ocs.getInt(14);
            Err_Msg = ocs.getString(15);
            if (Err_Code != 0) throw new OracleException(Err_Msg);
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, null, ocs);
        }
        return result;
    }

    @Override
    public void updateRefSimpleItem(SimpleReference refSimpleItem) throws OracleException {
        int Err_Code = 0;
        String Err_Msg = "";
        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_UPDATE_SIMPLE (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            ocs.setString(1, refSimpleItem.getRefCode());
            ocs.setLong(2, refSimpleItem.getId());
            ocs.setLong(3, refSimpleItem.getRecId());
            ocs.setString(4, refSimpleItem.getCode());
            ocs.setString(5, refSimpleItem.getNameKz());
            ocs.setString(6, refSimpleItem.getNameRu());
            ocs.setString(7, refSimpleItem.getNameEn());
            ocs.setDate(8, new java.sql.Date(refSimpleItem.getBeginDate().getTime()));
            ocs.setDate(9, refSimpleItem.getEndDate() == null ? null : new java.sql.Date(refSimpleItem.getEndDate().getTime()));
            ocs.setLong(10, refSimpleItem.getUserId());
            ocs.setString(11, refSimpleItem.getUserLocation());
            ocs.setDate(12, refSimpleItem.getDatlast() == null ? null : new java.sql.Date(refSimpleItem.getDatlast().getTime()));
            ocs.setInt(13, 1);
            ocs.registerOutParameter(14, OracleTypes.INTEGER);
            ocs.registerOutParameter(15, OracleTypes.VARCHAR);
            ocs.execute();
            Err_Code = ocs.getInt(14);
            Err_Msg = ocs.getString(15);
            if (Err_Code != 0) throw new OracleException(Err_Msg);
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, null, ocs);
        }
    }

    @Override
    public void deleteRefSimpleItem(Long id, String refCode) throws OracleException {
        int Err_Code = 0;
        String Err_Msg = "";
        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("{ call PKG_FRSI_REF.REF_DELETE_SIMPLE (?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            ocs.setString(1, refCode);
            ocs.setLong(2, id);
            ocs.setInt(3, 1);
            ocs.registerOutParameter(4, OracleTypes.INTEGER);
            ocs.registerOutParameter(5, OracleTypes.VARCHAR);
            ocs.execute();
            Err_Code = ocs.getInt(4);
            Err_Msg = ocs.getString(5);
            if (Err_Code != 0) throw new OracleException(Err_Msg);
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, null, ocs);
        }
    }

    @Override
    public FileWrapper referenceToExcelFile(RefItem refItem, boolean allRecords, Date date, Boolean loadBank) throws Exception {
        FileWrapper result = new FileWrapper();

        if (refItem.getCode().equalsIgnoreCase(RefPostItem.REF_CODE)) {
            List<RefPostItem> items;
            if (allRecords) {
                items = (List<RefPostItem>) getRefAbstractByFilterList(refItem.getCode(), null, date);
            } else {
                items = (List<RefPostItem>) getRefAbstractList(refItem.getCode(), date);
            }
            result = refPostToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefPersonItem.REF_CODE)) {
            List<RefPersonItem> items;
            if (allRecords) {
                items = (List<RefPersonItem>) getRefAbstractByFilterList(refItem.getCode(), null, date);
            } else {
                items = (List<RefPersonItem>) getRefAbstractList(refItem.getCode(), date);
            }
            result = refPersonToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefLegalPersonItem.REF_CODE)) {
            List<RefLegalPersonItem> items;
            if (allRecords) {
                items = (List<RefLegalPersonItem>) getRefAbstractByFilterList(refItem.getCode(), null, date);
            } else {
                items = (List<RefLegalPersonItem>) getRefAbstractList(refItem.getCode(), date);
            }
            result = refLegalPersonToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefCountryItem.REF_CODE)) {
            List<RefCountryItem> items;
            if (allRecords) {
                items = (List<RefCountryItem>) getRefAbstractByFilterList(refItem.getCode(), null, date);
                ;
            } else {
                items = (List<RefCountryItem>) getRefAbstractList(refItem.getCode(), date);
            }
            result = refCountryToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefManagersItem.REF_CODE)) {
            List<RefManagersItem> items;
            if (allRecords) {
                items = (List<RefManagersItem>) getRefAbstractByFilterList(refItem.getCode(), null, date);
            } else {
                items = (List<RefManagersItem>) getRefAbstractList(refItem.getCode(), date);
            }
            result = refManagersToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefTypeBusEntityItem.REF_CODE)) {
            List<RefTypeBusEntityItem> items;
            if (allRecords) {
                items = (List<RefTypeBusEntityItem>) getRefAbstractByFilterList(RefTypeBusEntityItem.REF_CODE, null, date);
            } else {
                items = (List<RefTypeBusEntityItem>) getRefAbstractList(RefTypeBusEntityItem.REF_CODE, date);
            }
            result = refTypeBusEntityToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefRegionItem.REF_CODE)) {
            List<RefRegionItem> items;
            if (allRecords) {
                items = (List<RefRegionItem>) getRefAbstractByFilterList(RefRegionItem.REF_CODE, null, date);
            } else {
                items = (List<RefRegionItem>) getRefAbstractList(RefRegionItem.REF_CODE, date);
            }
            result = refRegionToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefRequirementItem.REF_CODE)) {
            List<RefRequirementItem> items;
            if (allRecords) {
                items = (List<RefRequirementItem>) getRefAbstractByFilterList(RefRequirementItem.REF_CODE, null, date);
            } else {
                items = (List<RefRequirementItem>) getRefAbstractList(RefRequirementItem.REF_CODE, date);
            }
            result = refRequirementToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefTypeProvideItem.REF_CODE)) {
            List<RefTypeProvideItem> items;
            if (allRecords) {
                items = (List<RefTypeProvideItem>) getRefAbstractByFilterList(RefTypeProvideItem.REF_CODE, null, date);
            } else {
                items = (List<RefTypeProvideItem>) getRefAbstractList(RefTypeProvideItem.REF_CODE, date);
            }
            result = refTypeProvideToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefTransTypeItem.REF_CODE)) {
            List<RefTransTypeItem> items;
            if (allRecords) {
                items = (List<RefTransTypeItem>) getRefAbstractByFilterList(RefTransTypeItem.REF_CODE, null, date);
            } else {
                items = (List<RefTransTypeItem>) getRefAbstractList(RefTransTypeItem.REF_CODE, date);
            }
            result = refTransTypesToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefBalanceAccItem.REF_CODE)) {
            List<RefBalanceAccItem> items;
            if (allRecords) {
                items = (List<RefBalanceAccItem>) getRefAbstractByFilterList(RefBalanceAccItem.REF_CODE, null, date);
            } else {
                items = (List<RefBalanceAccItem>) getRefAbstractList(RefBalanceAccItem.REF_CODE, date);
            }
            result = refBalanceAccountToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefConnOrgItem.REF_CODE)) {
            List<RefConnOrgItem> items;
            if (allRecords) {
                items = (List<RefConnOrgItem>) getRefAbstractByFilterList(RefConnOrgItem.REF_CODE, null, date);
            } else {
                items = (List<RefConnOrgItem>) getRefAbstractList(RefConnOrgItem.REF_CODE, date);
            }
            result = refConnOrgToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefDepartmentItem.REF_CODE)) {
            List<RefDepartmentItem> items;
            if (allRecords) {
                items = (List<RefDepartmentItem>) getRefAbstractByFilterList(RefDepartmentItem.REF_CODE, null, date);
            } else {
                items = (List<RefDepartmentItem>) getRefAbstractList(RefDepartmentItem.REF_CODE, date);
            }
            result = refDepartmentToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefBankItem.REF_CODE)) {
            List<RefBankItem> items;
            RefBankItem item = new RefBankItem();
            item.setIsLoad(loadBank);
            items = (List<RefBankItem>) getRefAbstractByFilterList(RefBankItem.REF_CODE, item, date);
            result = refBankToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefRateAgencyItem.REF_CODE)) {
            List<RefRateAgencyItem> items;
            if (allRecords) {
                items = (List<RefRateAgencyItem>) getRefAbstractByFilterList(RefRateAgencyItem.REF_CODE, null, date);
            } else {
                items = (List<RefRateAgencyItem>) getRefAbstractList(RefRateAgencyItem.REF_CODE, date);
            }
            result = refRateAgencyToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefCurrencyItem.REF_CODE)) {
            List<RefCurrencyItem> items;
            if (allRecords) {
                items = (List<RefCurrencyItem>) getRefAbstractByFilterList(RefCurrencyItem.REF_CODE, null, date);
            } else {
                items = (List<RefCurrencyItem>) getRefAbstractList(RefCurrencyItem.REF_CODE, date);
            }
            result = refCurrencyToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefCurrencyRateItem.REF_CODE)) {
            List<RefCurrencyRateItem> items;
            if (allRecords) {
                items = (List<RefCurrencyRateItem>) getRefAbstractByFilterList(RefCurrencyRateItem.REF_CODE, null, date);
            } else {
                items = (List<RefCurrencyRateItem>) getRefAbstractList(RefCurrencyRateItem.REF_CODE, date);
            }
            result = refCurrencyRateToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefSubjectTypeItem.REF_CODE)) {
            List<RefSubjectTypeItem> items;
            if (allRecords) {
                items = (List<RefSubjectTypeItem>) getRefAbstractByFilterList(RefSubjectTypeItem.REF_CODE, null, date);
            } else {
                items = (List<RefSubjectTypeItem>) getRefAbstractList(RefSubjectTypeItem.REF_CODE, date);
            }
            result = refSubjectTypeToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase("ref_org_type")) {
            SimpleReference simpleReference = new SimpleReference();
            simpleReference.setRefCode(refItem.getCode());
            List<SimpleReference> items = getRefSimpleListByParams(date, simpleReference);
            result = simpleReferenceToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefRespondentItem.REF_CODE)) {
            List<RefRespondentItem> items;
            if (allRecords) {
                items = (List<RefRespondentItem>) getRefAbstractByFilterList(RefRespondentItem.REF_CODE, null, date);
            } else {
                items = (List<RefRespondentItem>) getRefAbstractList(RefRespondentItem.REF_CODE, date);
            }
            result = refRespondentToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefDocTypeItem.REF_CODE)) {
            List<RefDocTypeItem> items;
            if (allRecords) {
                items = (List<RefDocTypeItem>) getRefAbstractByFilterList(RefDocTypeItem.REF_CODE, null, date);
            } else {
                items = (List<RefDocTypeItem>) getRefAbstractList(RefDocTypeItem.REF_CODE, date);
            }
            result = refDocTypeToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefDocumentItem.REF_CODE)) {
            List<RefDocumentItem> items;
            if (allRecords) {
                items = (List<RefDocumentItem>) getRefAbstractByFilterList(RefDocumentItem.REF_CODE, null, date);
            } else {
                items = (List<RefDocumentItem>) getRefAbstractList(RefDocumentItem.REF_CODE, date);
            }
            result = refDocumentToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefIssuersItem.REF_CODE)) {
            List<RefIssuersItem> items;
            if (allRecords) {
                items = (List<RefIssuersItem>) getRefAbstractByFilterList(RefIssuersItem.REF_CODE, null, date);
            } else {
                items = (List<RefIssuersItem>) getRefAbstractList(RefIssuersItem.REF_CODE, date);
            }
            result = refIssuersToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefSecuritiesItem.REF_CODE)) {
            List<RefSecuritiesItem> items;
            if (allRecords) {
                items = (List<RefSecuritiesItem>) getRefAbstractByFilterList(RefSecuritiesItem.REF_CODE, null, date);
            } else {
                items = (List<RefSecuritiesItem>) getRefAbstractList(RefSecuritiesItem.REF_CODE, date);
            }
            result = refSecuritiesToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefVidOperItem.REF_CODE)) {
            List<RefVidOperItem> items;
            if (allRecords) {
                items = (List<RefVidOperItem>) getRefAbstractByFilterList(RefVidOperItem.REF_CODE, null, date);
            } else {
                items = (List<RefVidOperItem>) getRefAbstractList(RefVidOperItem.REF_CODE, date);
            }
            result = refVidOperToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefBranchItem.REF_CODE)) {
            List<RefBranchItem> items;
            if (allRecords) {
                items = (List<RefBranchItem>) getRefAbstractByFilterList(RefBranchItem.REF_CODE, null, date);
            } else {
                items = (List<RefBranchItem>) getRefAbstractList(RefBranchItem.REF_CODE, date);
            }
            result = refBranchToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefListingEstimationItem.REF_CODE)) {
            List<RefListingEstimationItem> items;
            if (allRecords) {
                items = (List<RefListingEstimationItem>) getRefAbstractByFilterList(RefListingEstimationItem.REF_CODE, null, date);
            } else {
                items = (List<RefListingEstimationItem>) getRefAbstractList(RefListingEstimationItem.REF_CODE, date);
            }
            result = refListingEstimationToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefRatingEstimationItem.REF_CODE)) {
            List<RefRatingEstimationItem> items;
            if (allRecords) {
                items = (List<RefRatingEstimationItem>) getRefAbstractByFilterList(RefRatingEstimationItem.REF_CODE, null, date);
            } else {
                items = (List<RefRatingEstimationItem>) getRefAbstractList(RefRatingEstimationItem.REF_CODE, date);
            }
            result = refRatingEstimationToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefRatingCategoryItem.REF_CODE)) {
            List<RefRatingCategoryItem> items;
            if (allRecords) {
                items = (List<RefRatingCategoryItem>) getRefAbstractByFilterList(RefRatingCategoryItem.REF_CODE, null, date);
            } else {
                items = (List<RefRatingCategoryItem>) getRefAbstractList(RefRatingCategoryItem.REF_CODE, date);
            }
            result = refRatingCategoryToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase("ref_request_type")) {
            SimpleReference simpleReference = new SimpleReference();
            simpleReference.setRefCode(refItem.getCode());
            List<SimpleReference> items = getRefSimpleListByParams(date, simpleReference);
            result = simpleReferenceToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase("ref_request_way")) {
            SimpleReference simpleReference = new SimpleReference();
            simpleReference.setRefCode(refItem.getCode());
            List<SimpleReference> items = getRefSimpleListByParams(date, simpleReference);
            result = simpleReferenceToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase("ref_market_kind")) {
            SimpleReference simpleReference = new SimpleReference();
            simpleReference.setRefCode(refItem.getCode());
            List<SimpleReference> items = getRefSimpleListByParams(date, simpleReference);
            result = simpleReferenceToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase("ref_category")) {
            SimpleReference simpleReference = new SimpleReference();
            simpleReference.setRefCode(refItem.getCode());
            List<SimpleReference> items = getRefSimpleListByParams(date, simpleReference);
            result = simpleReferenceToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase("ref_subcategory")) {
            SimpleReference simpleReference = new SimpleReference();
            simpleReference.setRefCode(refItem.getCode());
            List<SimpleReference> items = getRefSimpleListByParams(date, simpleReference);
            result = simpleReferenceToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase("ref_account_type")) {
            SimpleReference simpleReference = new SimpleReference();
            simpleReference.setRefCode(refItem.getCode());
            List<SimpleReference> items = getRefSimpleListByParams(date, simpleReference);
            result = simpleReferenceToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase("ref_subaccount_type")) {
            SimpleReference simpleReference = new SimpleReference();
            simpleReference.setRefCode(refItem.getCode());
            List<SimpleReference> items = getRefSimpleListByParams(date, simpleReference);
            result = simpleReferenceToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase("ref_type_holder_acc")) {
            SimpleReference simpleReference = new SimpleReference();
            simpleReference.setRefCode(refItem.getCode());
            List<SimpleReference> items = getRefSimpleListByParams(date, simpleReference);
            result = simpleReferenceToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase("ref_request_feature")) {
            SimpleReference simpleReference = new SimpleReference();
            simpleReference.setRefCode(refItem.getCode());
            List<SimpleReference> items = getRefSimpleListByParams(date, simpleReference);
            result = simpleReferenceToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase("ref_request_sts")) {
            SimpleReference simpleReference = new SimpleReference();
            simpleReference.setRefCode(refItem.getCode());
            List<SimpleReference> items = getRefSimpleListByParams(date, simpleReference);
            result = simpleReferenceToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase("ref_repo_kind")) {
            SimpleReference simpleReference = new SimpleReference();
            simpleReference.setRefCode(refItem.getCode());
            List<SimpleReference> items = getRefSimpleListByParams(date, simpleReference);
            result = simpleReferenceToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase("ref_market_type")) {
            SimpleReference simpleReference = new SimpleReference();
            simpleReference.setRefCode(refItem.getCode());
            List<SimpleReference> items = getRefSimpleListByParams(date, simpleReference);
            result = simpleReferenceToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase("ref_trad_method")) {
            SimpleReference simpleReference = new SimpleReference();
            simpleReference.setRefCode(refItem.getCode());
            List<SimpleReference> items = getRefSimpleListByParams(date, simpleReference);
            result = simpleReferenceToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase("ref_oper_type")) {
            SimpleReference simpleReference = new SimpleReference();
            simpleReference.setRefCode(refItem.getCode());
            List<SimpleReference> items = getRefSimpleListByParams(date, simpleReference);
            result = simpleReferenceToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase("ref_deal_sts")) {
            SimpleReference simpleReference = new SimpleReference();
            simpleReference.setRefCode(refItem.getCode());
            List<SimpleReference> items = getRefSimpleListByParams(date, simpleReference);
            result = simpleReferenceToExcelFile(refItem, items);
        } else if (refItem.getCode().equalsIgnoreCase(RefMrpItem.REF_CODE)) {
            List<RefMrpItem> items = (List<RefMrpItem>) getRefAbstractList(RefMrpItem.REF_CODE, date);
            result = refMrpToExcelFile(refItem, items);
        }
        return result;
    }

    @Override
    public FileWrapper simpleReferenceToExcelFile(final RefItem refItem, List<SimpleReference> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCodeTemplate("tml_simple_ref");

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<SimpleReference> parentDataMapKeyHandler = new MapKeyHandler<SimpleReference>(new EntityMapConverter<SimpleReference>() {
                    @Override
                    public Map<String, String> convert(SimpleReference entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("code", entity.getCode() == null ? "" : entity.getCode());
                        data.put("name_ru", entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (SimpleReference item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refPostToExcelFile(final RefItem refItem, List<RefPostItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefPostItem> parentDataMapKeyHandler = new MapKeyHandler<RefPostItem>(new EntityMapConverter<RefPostItem>() {
                    @Override
                    public Map<String, String> convert(RefPostItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("type_post_name", entity.getTypePostName() == null ? "" : String.valueOf(entity.getTypePostName()));
                        if (entity.getIsActivity() == null) {
                            data.put("is_activity", "");
                        } else {
                            data.put("is_activity", entity.getIsActivity() ? "Да" : "Нет");
                        }
                        if (entity.getIsMainRuk() == null) {
                            data.put("is_main_ruk", "");
                        } else {
                            data.put("is_main_ruk", entity.getIsMainRuk() ? "Да" : "Нет");
                        }
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefPostItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refPersonToExcelFile(final RefItem refItem, List<RefPersonItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefPersonItem> parentDataMapKeyHandler = new MapKeyHandler<RefPersonItem>(new EntityMapConverter<RefPersonItem>() {
                    @Override
                    public Map<String, String> convert(RefPersonItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("fm", entity.getFm() == null ? "" : entity.getFm());
                        data.put("nm", entity.getNm() == null ? "" : entity.getNm());
                        data.put("ft", entity.getFt() == null ? "" : entity.getFt());
                        data.put("fio_ru", entity.getFioRu() == null ? "" : entity.getFioRu());
                        data.put("fio_kz", entity.getFioKz() == null ? "" : entity.getFioKz());
                        data.put("fio_en", entity.getFioEn() == null ? "" : entity.getFioEn());
                        data.put("country_name", entity.getCountryName() == null ? "" : entity.getCountryName());
                        data.put("phone_work", entity.getPhoneWork() == null ? "" : entity.getPhoneWork());
                        data.put("fax", entity.getFax() == null ? "" : entity.getFax());
                        data.put("address_work", entity.getAddressWork() == null ? "" : entity.getAddressWork());
                        data.put("note", entity.getNote() == null ? "" : entity.getNote());
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefPersonItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refLegalPersonToExcelFile(final RefItem refItem, List<RefLegalPersonItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefLegalPersonItem> parentDataMapKeyHandler = new MapKeyHandler<RefLegalPersonItem>(new EntityMapConverter<RefLegalPersonItem>() {
                    @Override
                    public Map<String, String> convert(RefLegalPersonItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("short_name_ru", entity.getShortNameRu() == null ? "" : entity.getShortNameRu());
                        data.put("short_name_kz", entity.getShortNameKz() == null ? "" : entity.getShortNameKz());
                        data.put("short_name_en", entity.getShortNameEn() == null ? "" : entity.getShortNameEn());
                        if (entity.getIsNonRezident() == null) {
                            data.put("is_non_rezident", "");
                        } else {
                            data.put("is_non_rezident", entity.getIsNonRezident() ? "Да" : "Нет");
                        }
                        data.put("idn", entity.getIdn() == null ? "" : entity.getIdn());
                        if (entity.getIsInvFund() == null) {
                            data.put("is_ivn_fund", "");
                        } else {
                            data.put("is_ivn_fund", entity.getIsInvFund() ? "Да" : "Нет");
                        }
                        data.put("inv_idn", entity.getInvIdn() == null ? "" : entity.getInvIdn());
                        data.put("manager", entity.getManager() == null ? "" : entity.getManager());
                        data.put("org_type_name", entity.getOrgTypeName() == null ? "" : entity.getOrgTypeName());
                        data.put("type_be_name", entity.getTypeBeName() == null ? "" : entity.getTypeBeName());
                        data.put("country_name", entity.getCountryName() == null ? "" : entity.getCountryName());
                        /*data.put("region_name", entity.getRegionName() == null ? "" : entity.getRegionName());
                        data.put("postal_index", entity.getPostalIndex() == null ? "" : entity.getPostalIndex());*/
                        data.put("legal_address", entity.getLegalAddress() == null ? "" : entity.getLegalAddress());
                        data.put("fact_address", entity.getFactAddress() == null ? "" : entity.getFactAddress());
                        data.put("note", entity.getNote() == null ? "" : entity.getNote());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefLegalPersonItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refCountryToExcelFile(final RefItem refItem, List<RefCountryItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefCountryItem> parentDataMapKeyHandler = new MapKeyHandler<RefCountryItem>(new EntityMapConverter<RefCountryItem>() {
                    @Override
                    public Map<String, String> convert(RefCountryItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefCountryItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refManagersToExcelFile(final RefItem refItem, List<RefManagersItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefManagersItem> parentDataMapKeyHandler = new MapKeyHandler<RefManagersItem>(new EntityMapConverter<RefManagersItem>() {
                    @Override
                    public Map<String, String> convert(RefManagersItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("fm", entity.getFm() == null ? "" : entity.getFm());
                        data.put("nm", entity.getNm() == null ? "" : entity.getNm());
                        data.put("ft", entity.getFt() == null ? "" : entity.getFt());
                        data.put("fio_ru", entity.getFioRu() == null ? "" : entity.getFioRu());
                        data.put("fio_kz", entity.getFioKz() == null ? "" : entity.getFioKz());
                        data.put("fio_en", entity.getFioEn() == null ? "" : entity.getFioEn());
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefManagersItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refTypeBusEntityToExcelFile(final RefItem refItem, List<RefTypeBusEntityItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefTypeBusEntityItem> parentDataMapKeyHandler = new MapKeyHandler<RefTypeBusEntityItem>(new EntityMapConverter<RefTypeBusEntityItem>() {
                    @Override
                    public Map<String, String> convert(RefTypeBusEntityItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefTypeBusEntityItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refRegionToExcelFile(final RefItem refItem, List<RefRegionItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefRegionItem> parentDataMapKeyHandler = new MapKeyHandler<RefRegionItem>(new EntityMapConverter<RefRegionItem>() {
                    @Override
                    public Map<String, String> convert(RefRegionItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefRegionItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refRequirementToExcelFile(final RefItem refItem, List<RefRequirementItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefRequirementItem> parentDataMapKeyHandler = new MapKeyHandler<RefRequirementItem>(new EntityMapConverter<RefRequirementItem>() {
                    @Override
                    public Map<String, String> convert(RefRequirementItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefRequirementItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refTypeProvideToExcelFile(final RefItem refItem, List<RefTypeProvideItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefTypeProvideItem> parentDataMapKeyHandler = new MapKeyHandler<RefTypeProvideItem>(new EntityMapConverter<RefTypeProvideItem>() {
                    @Override
                    public Map<String, String> convert(RefTypeProvideItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefTypeProvideItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refTransTypesToExcelFile(final RefItem refItem, List<RefTransTypeItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefTransTypeItem> parentDataMapKeyHandler = new MapKeyHandler<RefTransTypeItem>(new EntityMapConverter<RefTransTypeItem>() {
                    @Override
                    public Map<String, String> convert(RefTransTypeItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("short_name_ru", entity.getShortNameRu() == null ? "" : entity.getShortNameRu());
                        data.put("kind_of_activity", entity.getKindOfActivity() == null ? "" : entity.getKindOfActivity());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefTransTypeItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refBalanceAccountToExcelFile(final RefItem refItem, List<RefBalanceAccItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefBalanceAccItem> parentDataMapKeyHandler = new MapKeyHandler<RefBalanceAccItem>(new EntityMapConverter<RefBalanceAccItem>() {
                    @Override
                    public Map<String, String> convert(RefBalanceAccItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("parent_code", entity.getParentCode() == null ? "" : String.valueOf(entity.getParentCode()));
                        data.put("code", String.valueOf(entity.getCode()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("short_name_ru", entity.getShortNameRu() == null ? "" : entity.getShortNameRu());
                        data.put("short_name_kz", entity.getShortNameKz() == null ? "" : entity.getShortNameKz());
                        data.put("short_name_en", entity.getShortNameEn() == null ? "" : entity.getShortNameEn());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefBalanceAccItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refConnOrgToExcelFile(final RefItem refItem, List<RefConnOrgItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefConnOrgItem> parentDataMapKeyHandler = new MapKeyHandler<RefConnOrgItem>(new EntityMapConverter<RefConnOrgItem>() {
                    @Override
                    public Map<String, String> convert(RefConnOrgItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefConnOrgItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refDepartmentToExcelFile(final RefItem refItem, List<RefDepartmentItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefDepartmentItem> parentDataMapKeyHandler = new MapKeyHandler<RefDepartmentItem>(new EntityMapConverter<RefDepartmentItem>() {
                    @Override
                    public Map<String, String> convert(RefDepartmentItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefDepartmentItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refBankToExcelFile(final RefItem refItem, List<RefBankItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefBankItem> parentDataMapKeyHandler = new MapKeyHandler<RefBankItem>(new EntityMapConverter<RefBankItem>() {
                    @Override
                    public Map<String, String> convert(RefBankItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("idn", entity.getIdn() == null ? "" : entity.getIdn());
                        if (entity.getIsLoad() == null) {
                            data.put("is_load", "");
                        } else {
                            data.put("is_load", entity.getIsLoad() ? "Да" : "Нет");
                        }
                        if (entity.getIsNonRezident() == null) {
                            data.put("is_non_resident", "");
                        } else {
                            data.put("is_non_resident", entity.getIsNonRezident() ? "Да" : "Нет");
                        }
                        data.put("country_name", entity.getRefCountryName() == null ? "" : entity.getRefCountryName());
                        data.put("post_address", entity.getPostAddress() == null ? "" : entity.getPostAddress());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefBankItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refRateAgencyToExcelFile(final RefItem refItem, List<RefRateAgencyItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefRateAgencyItem> parentDataMapKeyHandler = new MapKeyHandler<RefRateAgencyItem>(new EntityMapConverter<RefRateAgencyItem>() {
                    @Override
                    public Map<String, String> convert(RefRateAgencyItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefRateAgencyItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refCurrencyToExcelFile(final RefItem refItem, List<RefCurrencyItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefCurrencyItem> parentDataMapKeyHandler = new MapKeyHandler<RefCurrencyItem>(new EntityMapConverter<RefCurrencyItem>() {
                    @Override
                    public Map<String, String> convert(RefCurrencyItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("code", entity.getCode() == null ? "" : entity.getCode());
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("minor_units", String.valueOf(entity.getMinorUnits() == null ? "" : entity.getMinorUnits()));
                        data.put("rate", String.valueOf(entity.getRate() == null ? "" : entity.getRate()));
                        data.put("cur_rate_name", entity.getCurRateName() == null ? "" : entity.getCurRateName());
                        data.put("rate_agency", entity.getRateAgency() == null ? "" : entity.getRateAgency());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefCurrencyItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refCurrencyRateToExcelFile(final RefItem refItem, List<RefCurrencyRateItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefCurrencyRateItem> parentDataMapKeyHandler = new MapKeyHandler<RefCurrencyRateItem>(new EntityMapConverter<RefCurrencyRateItem>() {
                    @Override
                    public Map<String, String> convert(RefCurrencyRateItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("code", entity.getCode() == null ? "" : entity.getCode());
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("rate_agency", entity.getRateAgency() == null ? "" : entity.getRateAgency());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefCurrencyRateItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refSubjectTypeToExcelFile(final RefItem refItem, List<RefSubjectTypeItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefSubjectTypeItem> parentDataMapKeyHandler = new MapKeyHandler<RefSubjectTypeItem>(new EntityMapConverter<RefSubjectTypeItem>() {
                    @Override
                    public Map<String, String> convert(RefSubjectTypeItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("short_name_ru", entity.getShortNameRu() == null ? "" : entity.getShortNameRu());
                        data.put("short_name_kz", entity.getShortNameKz() == null ? "" : entity.getShortNameKz());
                        data.put("short_name_en", entity.getShortNameEn() == null ? "" : entity.getShortNameEn());
                        if (entity.getIsAdvance() == null) {
                            data.put("is_advance", "");
                        } else {
                            data.put("is_advance", entity.getIsAdvance() ? "Да" : "Нет");
                        }
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefSubjectTypeItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refRespondentToExcelFile(final RefItem refItem, List<RefRespondentItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefRespondentItem> parentDataMapKeyHandler = new MapKeyHandler<RefRespondentItem>(new EntityMapConverter<RefRespondentItem>() {
                    @Override
                    public Map<String, String> convert(RefRespondentItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("lp_name", entity.getPersonName() == null ? "" : entity.getPersonName());
                        /*data.put("nokdbd_code", entity.getNokbdbCode() == null ? "" : entity.getNokbdbCode());
                        data.put("main_buh", entity.getMainBuh() == null ? "" : entity.getMainBuh());
                        data.put("bdate_lic", entity.getDateBeginLic() == null ? "" : Convert.getDateStringFromDate(entity.getDateBeginLic()));
                        data.put("edate_lic", entity.getDateEndLic() == null ? "" : Convert.getDateStringFromDate(entity.getDateEndLic()));
                        data.put("stop_lic", entity.getStopLic() == null ? "" : entity.getStopLic());
                        data.put("vid_activity", entity.getVidActivity() == null ? "" : entity.getVidActivity());*/
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefRespondentItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refDocTypeToExcelFile(final RefItem refItem, List<RefDocTypeItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefDocTypeItem> parentDataMapKeyHandler = new MapKeyHandler<RefDocTypeItem>(new EntityMapConverter<RefDocTypeItem>() {
                    @Override
                    public Map<String, String> convert(RefDocTypeItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        if (entity.getIsIdentification() == null) {
                            data.put("is_identification", "");
                        } else {
                            data.put("is_identification", entity.getIsIdentification() ? "Да" : "Нет");
                        }
                        if (entity.getIsOrganizationDoc() == null) {
                            data.put("is_organization_doc", "");
                        } else {
                            data.put("is_organization_doc", entity.getIsOrganizationDoc() ? "Да" : "Нет");
                        }
                        if (entity.getIsPersonDoc() == null) {
                            data.put("is_person_doc", "");
                        } else {
                            data.put("is_person_doc", entity.getIsPersonDoc() ? "Да" : "Нет");
                        }
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefDocTypeItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refDocumentToExcelFile(final RefItem refItem, List<RefDocumentItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefDocumentItem> parentDataMapKeyHandler = new MapKeyHandler<RefDocumentItem>(new EntityMapConverter<RefDocumentItem>() {
                    @Override
                    public Map<String, String> convert(RefDocumentItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("code", entity.getCode());
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("doc_type_name", entity.getDocTypeName() == null ? "" : entity.getDocTypeName());
                        data.put("respondent_name", entity.getRespondentName() == null ? "" : entity.getRespondentName());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefDocumentItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refIssuersToExcelFile(final RefItem refItem, List<RefIssuersItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefIssuersItem> parentDataMapKeyHandler = new MapKeyHandler<RefIssuersItem>(new EntityMapConverter<RefIssuersItem>() {
                    @Override
                    public Map<String, String> convert(RefIssuersItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("code", entity.getCode() == null ? "" : entity.getCode());
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("sign_name", entity.getSignName() == null ? "" : entity.getSignName());
                        if (entity.getIsState() == null) {
                            data.put("is_state", "");
                        } else {
                            data.put("is_state", entity.getIsState() ? "Да" : "Нет");
                        }
                        if (entity.getIsResident() == null) {
                            data.put("is_rezident", "");
                        } else {
                            data.put("is_rezident", entity.getIsResident() ? "Да" : "Нет");
                        }
                        data.put("listing_estimation", entity.getListingEstimation() == null ? "" : entity.getListingEstimation());
                        data.put("rating_estimation", entity.getRatingEstimation() == null ? "" : entity.getRatingEstimation());
                        if (entity.getIsFromKase() == null) {
                            data.put("is_from_kase", "");
                        } else {
                            data.put("is_from_kase", entity.getIsFromKase() ? "Да" : "Нет");
                        }
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefIssuersItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refSecuritiesToExcelFile(final RefItem refItem, List<RefSecuritiesItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefSecuritiesItem> parentDataMapKeyHandler = new MapKeyHandler<RefSecuritiesItem>(new EntityMapConverter<RefSecuritiesItem>() {
                    @Override
                    public Map<String, String> convert(RefSecuritiesItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("code", entity.getCode());
                        data.put("issuer_name", entity.getIssuerName() == null ? "" : entity.getIssuerName());
                        data.put("sign_code", entity.getSignCode() == null ? "" : entity.getSignCode());
                        data.put("sign_name", entity.getSignName() == null ? "" : entity.getSignName());
                        if (entity.getIsResident() == null) {
                            data.put("is_rezident", "");
                        } else {
                            data.put("is_rezident", entity.getIsResident() ? "Да" : "Нет");
                        }
                        if (entity.getIsState() == null) {
                            data.put("is_state", "");
                        } else {
                            data.put("is_state", entity.getIsState() ? "Да" : "Нет");
                        }
                        data.put("nominal_value", entity.getNominalValue() == null ? "" : String.valueOf(entity.getNominalValue()));
                        data.put("nin", entity.getNin() == null ? "" : entity.getNin());
                        data.put("circul_date", entity.getCirculDate() == null ? "" : Convert.getDateStringFromDate(entity.getCirculDate()));
                        data.put("maturity_date", entity.getMaturityDate() == null ? "" : Convert.getDateStringFromDate(entity.getMaturityDate()));
                        data.put("security_cnt", entity.getSecurityCnt() == null ? "" : String.valueOf(entity.getSecurityCnt()));
                        data.put("variety_code", entity.getVarietyCode() == null ? "" : entity.getVarietyCode());
                        data.put("variety_name", entity.getVarietyName() == null ? "" : entity.getVarietyName());
                        data.put("type_code", entity.getTypeCode() == null ? "" : entity.getTypeCode());
                        data.put("type_name", entity.getTypeName() == null ? "" : entity.getTypeName());
                        data.put("currency_code", entity.getCurrencyCode() == null ? "" : entity.getCurrencyCode());
                        data.put("currency_name", entity.getCurrencyName() == null ? "" : entity.getCurrencyName());
                        data.put("issue_volume", entity.getIssueVolume() == null ? "" : String.valueOf(entity.getIssueVolume()));
                        data.put("circul_period", entity.getCirculPeriod() == null ? "" : String.valueOf(entity.getCirculPeriod()));
                        data.put("listing_estimation", entity.getListingEstimation() == null ? "" : entity.getListingEstimation());
                        data.put("rating_estimation", entity.getRatingEstimation() == null ? "" : entity.getRatingEstimation());
                        if (entity.getIsBondProgram() == null) {
                            data.put("is_bond_program", "");
                        } else {
                            data.put("is_bond_program", entity.getIsBondProgram() ? "Да" : "Нет");
                        }
                        data.put("bond_prog_vol", entity.getBondProgramVolume() == null ? "" : String.valueOf(entity.getBondProgramVolume()));
                        data.put("bond_prog_cnt", entity.getBondPrgCnt() == null ? "" : String.valueOf(entity.getBondPrgCnt()));
                        if (entity.getIsGarant() == null) {
                            data.put("is_garant", "");
                        } else {
                            data.put("is_garant", entity.getIsGarant() ? "Да" : "Нет");
                        }
                        data.put("garant", entity.getGarant() == null ? "" : entity.getGarant());
                        if (entity.getIsPermit() == null) {
                            data.put("is_permit", "");
                        } else {
                            data.put("is_permit", entity.getIsPermit() ? "Да" : "Нет");
                        }
                        if (entity.getIsFromKase() == null) {
                            data.put("is_from_kase", "");
                        } else {
                            data.put("is_from_kase", entity.getIsFromKase() ? "Да" : "Нет");
                        }
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefSecuritiesItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refVidOperToExcelFile(final RefItem refItem, List<RefVidOperItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefVidOperItem> parentDataMapKeyHandler = new MapKeyHandler<RefVidOperItem>(new EntityMapConverter<RefVidOperItem>() {
                    @Override
                    public Map<String, String> convert(RefVidOperItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("code", entity.getCode());
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefVidOperItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refBranchToExcelFile(final RefItem refItem, List<RefBranchItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefBranchItem> parentDataMapKeyHandler = new MapKeyHandler<RefBranchItem>(new EntityMapConverter<RefBranchItem>() {
                    @Override
                    public Map<String, String> convert(RefBranchItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("code", entity.getCode());
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefBranchItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refCrosscheckToExcelFile(final RefItem refItem, List<RefCrosscheckItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefCrosscheckItem> parentDataMapKeyHandler = new MapKeyHandler<RefCrosscheckItem>(new EntityMapConverter<RefCrosscheckItem>() {
                    @Override
                    public Map<String, String> convert(RefCrosscheckItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        if (entity.getFormula() == null)
                            data.put("formula", "");
                        else
                            data.put("formula", entity.getFormula());
                        if (entity.getDescrRuL() == null || entity.getDescrRuR() == null)
                            data.put("descr_rus", "");
                        else
                            data.put("descr_rus", entity.getDescrRuL() + " " + entity.getFormulaSymbol() + " " + entity.getDescrRuR());
                        data.put("cross_type_name", entity.getCrossTypeName() == null ? "" : entity.getCrossTypeName());
                        if (entity.getConditionL() == null || entity.getConditionR() == null)
                            data.put("condition", "");
                        else
                            data.put("condition", entity.getConditionL() + " " + entity.getConditionSymbol() + " " + entity.getConditionR());
                        data.put("num", String.valueOf(entity.getNum() == null ? "" : entity.getNum()));
                        if (entity.getIsAvailable() == null) {
                            data.put("is_available", "");
                        } else {
                            data.put("is_available", entity.getIsAvailable() ? "Да" : "Нет");
                        }
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefCrosscheckItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refReportsRulesToExcelFile(final RefItem refItem, List<RefReportsRulesItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefReportsRulesItem> parentDataMapKeyHandler = new MapKeyHandler<RefReportsRulesItem>(new EntityMapConverter<RefReportsRulesItem>() {
                    @Override
                    public Map<String, String> convert(RefReportsRulesItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("form_name", entity.getFormname() == null ? "" : entity.getFormname());
                        data.put("table_name", entity.getTableName() == null ? "" : entity.getTableName());
                        data.put("field_name", entity.getFieldname() == null ? "" : entity.getFieldname());
                        data.put("key_value", entity.getKeyValue() == null ? "" : entity.getKeyValue());
                        data.put("formula", entity.getFormula() == null ? "" : entity.getFormula());
                        data.put("priority", String.valueOf(entity.getPriority() == null ? "" : entity.getPriority()));
                        data.put("coeff", String.valueOf(entity.getCoeff() == null ? "" : entity.getCoeff()));
                        data.put("condition", entity.getCondition() == null ? "" : entity.getCondition());
                        data.put("report_kind_name", entity.getReportKindName() == null ? "" : entity.getReportKindName());
                        data.put("dur_name", entity.getDurName() == null ? "" : entity.getDurName());
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefReportsRulesItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refListingEstimationToExcelFile(final RefItem refItem, List<RefListingEstimationItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefListingEstimationItem> parentDataMapKeyHandler = new MapKeyHandler<RefListingEstimationItem>(new EntityMapConverter<RefListingEstimationItem>() {
                    @Override
                    public Map<String, String> convert(RefListingEstimationItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("priority", entity.getPriority() == null ? "" : entity.getPriority());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefListingEstimationItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refRatingEstimationToExcelFile(final RefItem refItem, List<RefRatingEstimationItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefRatingEstimationItem> parentDataMapKeyHandler = new MapKeyHandler<RefRatingEstimationItem>(new EntityMapConverter<RefRatingEstimationItem>() {
                    @Override
                    public Map<String, String> convert(RefRatingEstimationItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("priority", entity.getPriority() == null ? "" : entity.getPriority());
                        data.put("rating_category_name", entity.getRatingCategoryName() == null ? "" : entity.getRatingCategoryName());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefRatingEstimationItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refRatingCategoryToExcelFile(final RefItem refItem, List<RefRatingCategoryItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefRatingCategoryItem> parentDataMapKeyHandler = new MapKeyHandler<RefRatingCategoryItem>(new EntityMapConverter<RefRatingCategoryItem>() {
                    @Override
                    public Map<String, String> convert(RefRatingCategoryItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("code", entity.getCode() == null ? "" : entity.getCode());
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefRatingCategoryItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public FileWrapper refMrpToExcelFile(final RefItem refItem, List<RefMrpItem> refList) throws Exception {
        FileWrapper fileWrapper = new FileWrapper();

        Template template = new Template();
        template.setCode(refItem.getCode());

        byte[] xlsOut = getTemplateData(template).getXlsOut();

        if (xlsOut == null)
            throw new Exception("Файл шаблона не найден");

        InputStream myInputStream = new ByteArrayInputStream(xlsOut);

        try {
            ExcelReport excelReport = new ExcelReportImpl(myInputStream);
            ReportSheet sheet = excelReport.addSheet(0);
            sheet.setSheetTitle("Лист 1");

            MapKeyHandler<RefItem> headerMapKeyHandler = new MapKeyHandler<RefItem>(new EntityMapConverter<RefItem>() {
                @Override
                public Map<String, String> convert(RefItem entity) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("ref_name", refItem.getName());
                    return data;
                }
            });

            headerMapKeyHandler.setData(refItem);

            sheet.out("header", headerMapKeyHandler);

            if (refList.size() > 0) {
                MapKeyHandler<RefMrpItem> parentDataMapKeyHandler = new MapKeyHandler<RefMrpItem>(new EntityMapConverter<RefMrpItem>() {
                    @Override
                    public Map<String, String> convert(RefMrpItem entity) {
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("id", String.valueOf(entity.getId()));
                        data.put("rec_id", String.valueOf(entity.getRecId()));
                        data.put("code", entity.getCode() == null ? "" : entity.getCode());
                        data.put("name_ru", entity.getNameRu() == null ? "" : entity.getNameRu());
                        data.put("name_kz", entity.getNameKz() == null ? "" : entity.getNameKz());
                        data.put("name_en", entity.getNameEn() == null ? "" : entity.getNameEn());
                        data.put("value", String.valueOf(entity.getValue() == null ? "" : entity.getValue()));
                        data.put("begin_date", Convert.getDateStringFromDate(entity.getBeginDate()));
                        data.put("end_date", entity.getEndDate() == null ? "" : Convert.getDateStringFromDate(entity.getEndDate()));
                        data.put("user_name", entity.getUserName() == null ? "" : entity.getUserName());
                        data.put("user_location", entity.getUserLocation() == null ? "" : entity.getUserLocation());
                        data.put("datlast", entity.getDatlast() == null ? "" : Convert.getDateTimeStringFromDate(entity.getDatlast()));
                        return data;
                    }
                });
                for (RefMrpItem item : refList) {
                    parentDataMapKeyHandler.setData(item);
                    sheet.out("data", parentDataMapKeyHandler);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                excelReport.saveResult(outputStream);
                excelReport.closeWorkBooks();

                fileWrapper.setFileFormat("xlsx");
                fileWrapper.setBytes(outputStream.toByteArray());
            } finally {
                outputStream.close();
            }
        } finally {
            myInputStream.close();
        }
        return fileWrapper;
    }

    @Override
    public String getOracleDate(Date date) {
        if (date == null) {
            return "TO_DATE(NULL)";
        } else {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strDate = dateFormat.format(date);
            return "TO_DATE('" + strDate + "','YYYY-MM-DD HH24:MI:SS')";
        }
    }

    @Override
    public long getFirstLevelBankRecId() {
        return Constants.FIRST_LEVEL_BANK_REC_ID;
    }

    private Long insertAuditEvent(AuditEvent auditEvent, Connection connection) throws SQLException {
        int Err_Code = 0;
        String Err_Msg = " ";
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        Long id;
        try {
            // вызов процедуры
            stmt = connection.prepareCall("{ call PKG_FRSI_AE.AE_INSERT_MAIN (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
            ocs = auditEvent.setOCSParamsForIns(auditEvent, ocs);
            ocs.execute();
            id = ocs.getLong(13);
            Err_Code = ocs.getInt(14);
            Err_Msg = ocs.getString(15);
            if (Err_Code != 0) throw new SQLException(Err_Msg);
        } finally {
            DbUtil.closeConnection(connection, stmt, null, ocs);
        }
        return id;
    }

}
