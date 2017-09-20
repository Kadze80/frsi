package ejb;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dataform.BalanceAccountsFormHtml;
import dataform.BalanceAccountsOutFormHtml;
import entities.*;
import excelreport.BalanceAccExcelGenerate;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;
import util.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.ejb.Schedule;
import javax.mail.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sound.sampled.Port;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.sql.*;
// import java.sql.Date;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;
import java.util.Date;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

/**
 * Created by Ayupov.Bakhtiyar on 25.06.2015.
 */
@Startup
@Singleton
public class ScheduleBean implements ScheduleLocal, ScheduleRemote {
    private static final Logger logger = Logger.getLogger("fileLogger");
    private static final String JDBC_POOL_NAME = "jdbc/FrsiPool";

    @Resource
    private TimerService timerService;

    private DataSource dataSource;
    private Long systemUserId;
    private String systemUserEmail;
    private String userLocation;
    private Long respondentId;

    private static final Long FRSI_ADMIN_ROLE = 1L;
    private static final Long FRSI_ADMIN_DEPARTMENT_ROLE = 2L;
    private static final Long FRSI_USER_NB_MAIN_ROLE = 3L;
    private static final Long FRSI_USER_NB_DEPARTMENT_ROLE = 4L;
    private static final Long FRSI_RESPONDENT_ROLE = 5L;

    private String groupPrefix = "ФРСП."; // Set JVM option -Dfrsi.group.prefix in GlassFish to override this default value.

    @EJB
    private PerformControlLocal performControl;
    @EJB
    private PersistenceLocal persistence;
    @EJB
    private ReferenceLocal reference;

    @PostConstruct
    @Override
    public void init() {
        Date dateStart = new Date();

        // JDBC connection pool
        try {
            Context context = new InitialContext();
            dataSource = (DataSource) context.lookup(JDBC_POOL_NAME);
            logger.info("Connected to " + JDBC_POOL_NAME);
        } catch (NamingException e) {
            logger.error("Could not connect to " + JDBC_POOL_NAME);
            throw new EJBException(e);
        }

        systemUserId = 98150L; // Id пользователя (система) 98150(frsiadmin)
        systemUserEmail = "frsp@nationalbank.kz";
        userLocation = "127.0.0.1"; // Местоположение
        respondentId = 14L; // 14 - Национальный банк РК

        String propGroupPrefix = System.getProperty("frsi.group.prefix");
        if (propGroupPrefix != null) groupPrefix = propGroupPrefix;

        setTimerService(null);

        Date dateEnd = new Date();
        long duration = dateEnd.getTime() - dateStart.getTime();
        logger.debug(MessageFormat.format("t: {0}, d: {1} ms", dateStart, duration));
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void setTimerService(AuditEvent auditEvent) {
        String hour = "19";
        String minute = "10";
        String second = "00";

        String timer = System.getProperty("frsi.reference.timer"); // Формат таймера : hh:mm:ss
        // logger.info("Timer:" + timer);

        if (timer != null) {
            hour = timer.substring(0, 2);
            minute = timer.substring(3, 5);
            second = timer.substring(6, 8);
        }
        ScheduleExpression schedule = new ScheduleExpression();
        schedule.hour(hour).minute(minute).second(second);
        timerService.createCalendarTimer(schedule);

        try {
            if (auditEvent != null)
                persistence.insertAuditEvent(auditEvent);
        } catch (OracleException e) {
            throw new EJBException(e);
        }
    }

    @Timeout
    public void timeOutExecute() {
        List<String> loadReferenceList = getLoadReference();
        for (String str : loadReferenceList) {
            loadReferences(str, systemUserId == null ? Long.valueOf(98150) : systemUserId, userLocation == null ? "127.0.0.1" : userLocation, new Date(), respondentId == null ? Long.valueOf(14) : respondentId);
            logger.info("Reference: " + str + " load successful");
        }
    }

    // Загрузка справочников из НСИ и ПУРЦБ
    @Override
    public synchronized String loadReferences(String ref_code_, Long id_usr_, String User_Location_, Date loadDate, Long respondentId) {
        String result = "";
        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        try {
            connection = getConnection();
            // вызов процедуры
            stmt = connection.prepareCall("{ call pkg_frsi_ref_load.ref_load(?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(OracleCallableStatement.class);
            ocs.setString(1, ref_code_);
            ocs.setLong(2, id_usr_);
            ocs.setString(3, User_Location_);
            ocs.setDate(4, loadDate == null ? null : new java.sql.Date(loadDate.getTime()));
            ocs.registerOutParameter(5, OracleTypes.INTEGER);
            ocs.registerOutParameter(6, OracleTypes.VARCHAR);
            ocs.execute();
            if (ocs.getInt(5) == 0)
                result = "SUCCESS";
            else
                result = ocs.getString(6);

            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(ref_code_);
            auditEvent.setNameObject(null);
            auditEvent.setIdKindEvent(4L);
            auditEvent.setDateEvent(loadDate);
            auditEvent.setIdRefRespondent(respondentId);
            auditEvent.setDateIn(loadDate);
            auditEvent.setRecId(null);
            auditEvent.setUserId(id_usr_);
            auditEvent.setUserLocation(User_Location_);
            persistence.insertAuditEvent(auditEvent);

        } catch (SQLException e) {
            throw new EJBException(e);
        } catch (OracleException oe) {
            throw new EJBException(oe);
        } finally {
            DbUtil.closeConnection(connection, stmt, null, ocs);
        }
        return result;
    }

    ;

    @Override
    public synchronized List<String> getLoadReference() {
        List<String> result = new ArrayList<String>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT CODE FROM REF_MAIN WHERE REF_KND in (2,4) ORDER BY NAME ");
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("CODE"));
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    // Уведомления todo Включить после тестирования
    @Schedule(hour = "*", minute = "*", second = "*/30")
    @Override
    public void searchNotice() {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet cursor = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            // Находим список уведмолений
            List<AuditEvent> auditEventList = new ArrayList<AuditEvent>();
            ps = connection.prepareStatement(
                    "select m.id as ae_main,\n" +
                    "       m.REC_ID,\n" +
                    "       m.ae_kind_event,\n" +
                    "       ke.name as kind_event_name,\n" +
                    "       nvl(decode(mm.message, null, ke.message, mm.message), ' ') as message,\n" +
                    "       ke.subjectmsg,\n" +
                    "       m.name_object,\n" +
                    "       m.code_object,\n" +
                    "       m.date_in,\n" +
                    "       m.ref_respondent,\n" +
                    "       m.datlast\n" +
                    "  from ae_main m,\n" +
                    "       ae_kind_event ke,\n" +
                    "       ae_main_message mm\n" +
                    " where m.notice_sts = 0\n" +
                    "   and m.ae_kind_event = ke.id\n" +
                    "   and ke.is_notice = 1\n" +
                    "   and ke.is_active = 1\n" +
                    "   and m.id = mm.ae_main(+)");

            cursor = ps.executeQuery();
            Set<Long> kindEventSet = new HashSet<Long>();
            while (cursor.next()) {
                AuditEvent auditEvent = new AuditEvent();
                auditEvent.setId(cursor.getLong("AE_MAIN"));
                auditEvent.setRecId(cursor.getLong("REC_ID"));
                auditEvent.setIdKindEvent(cursor.getLong("AE_KIND_EVENT"));
                auditEvent.setKindEvent(cursor.getString("KIND_EVENT_NAME"));
                auditEvent.setAddMessage(cursor.getString("MESSAGE"));
                auditEvent.setSubjectMsg(cursor.getString("SUBJECTMSG"));
                auditEvent.setNameObject(cursor.getString("NAME_OBJECT"));
                auditEvent.setCodeObject(cursor.getString("CODE_OBJECT"));
                auditEvent.setDateIn(cursor.getDate("DATE_IN"));
                auditEvent.setIdRefRespondent(cursor.getLong("REF_RESPONDENT"));
                auditEvent.setDatlast(cursor.getDate("DATLAST"));
                auditEventList.add(auditEvent);
                kindEventSet.add(auditEvent.getIdKindEvent());
            }
            DbUtil.closeConnection(null, ps, cursor);

            Map<Long, Set<Long>> kindEventUsersMap = new HashMap<Long, Set<Long>>();

            for (Long idKindEvent : kindEventSet) {
                // Находим список пользователей
                ps = connection.prepareStatement(
                        "select u.user_id\n" +
                        "  from F_USERS u,\n" +
                        "       group_users gu,\n" +
                        "       groups g\n" +
                        " where u.blockfl = 0\n" +
                        "   and gu.user_id = u.user_id\n" +
                        "   and gu.group_id = g.group_id\n" +
                        "   and upper(g.name) like upper('" + groupPrefix + "%')\n" +
                        "   and not exists (select *\n" +
                        "                     from NOTICE_USER_OFF m,\n" +
                        "                          notice_user_off_sys s\n" +
                        "                    where m.ae_kind_event = s.ae_kind_event\n" +
                        "                      and m.user_id = s.user_id\n" +
                        "                      and m.user_id = u.user_id\n" +
                        "                      and m.ae_kind_event = ?)");
                ps.setLong(1, idKindEvent);
                cursor = ps.executeQuery();
                Set<Long> userToList = new HashSet<Long>();
                while (cursor.next()) {
                    userToList.add(cursor.getLong("USER_ID"));
                }
                DbUtil.closeConnection(null, ps, cursor);

                // Убираем лишних пользователей
                Set<Long> userList = new HashSet<Long>();
                for (Long userToId : userToList) {
                    Long roleId = getRoleByUserId(userToId).getId();

                    Boolean noticeUser;
                    noticeUser = isNoticeUser("notice_user", userToId, idKindEvent, connection);
                    if (noticeUser == null) {
                        if (roleId.equals(FRSI_ADMIN_ROLE) || roleId.equals(FRSI_ADMIN_DEPARTMENT_ROLE) || roleId.equals(FRSI_USER_NB_MAIN_ROLE) || roleId.equals(FRSI_USER_NB_DEPARTMENT_ROLE)) { // Администраторы и Пользователи
                            userList = checkGroupRoleUser(userToId, idKindEvent, userList, connection);
                        } else if (roleId.equals(FRSI_RESPONDENT_ROLE)) { // Респонденты
                            Boolean noticeRespondent = isNoticeUser("notice_respondent", userToId, idKindEvent, connection);
                            if (noticeRespondent == null) {
                                Boolean noticeSubjectType = isNoticeUser("notice_subjecttype", userToId, idKindEvent, connection);
                                if (noticeSubjectType == null) {
                                    userList = checkGroupRoleUser(userToId, idKindEvent, userList, connection);
                                } else if (noticeSubjectType)
                                    userList.add(userToId);
                            } else if (noticeRespondent)
                                userList.add(userToId);
                        }
                    } else if (noticeUser)
                        userList.add(userToId);
                }
                kindEventUsersMap.put(idKindEvent, userList);
            }

            // Отправляем на почту сообщения и записываем в таблицу
            for (AuditEvent auditEvent : auditEventList) {
                Set<Long> userList = kindEventUsersMap.get(auditEvent.getIdKindEvent());
                Long idKindEvent = auditEvent.getIdKindEvent();

                if (idKindEvent == 22 || idKindEvent == 23 || idKindEvent == 59 || idKindEvent == 117) {
                    String permissionName = "";
                    if (idKindEvent == 22) permissionName = "F:APPROVE";
                    if (idKindEvent == 23) permissionName = "F:DISAPPROVE";
                    if (idKindEvent == 59 || idKindEvent == 117) permissionName = "F:SHOW";
                    userList = persistence.filterUsersByFormPermission(userList, auditEvent.getRecId(), auditEvent.getCodeObject(), permissionName);
                }

                if (idKindEvent == 117) {
                    AuditEventParam param = persistence.getAuditEventParam(auditEvent.getId(), "left_days");
                    long leftDays = param.getValue().getLngValue();
                    Iterator<Long> userIdIterator = userList.iterator();
                    while (userIdIterator.hasNext()) {
                        Long userId = userIdIterator.next();
                        SettingsItem settingsItem = persistence.getSettingsItemByTypeUserId(SettingsItem.Type.DAYS_BEFORE_OVERDUE_DATE, userId);
                        if (settingsItem == null) {
                            userIdIterator.remove();
                        } else {
                            int days = SettingsValueConverter.fromRaw(settingsItem.getRawValue(), persistence.getDaysBeforeOverdueDateSettingDefaultValue(), Integer.class);
                            if (days == 0 && days < leftDays) {
                                userIdIterator.remove();
                            }
                        }
                    }
                }

                for (Long userIdTo : userList) {
                    if(!persistence.checkUserNoticeOff(userIdTo, auditEvent.getIdKindEvent(), 2)) {
                        // Пишем в таблицу
                        PortalUser portalUser = getUserByUserId(userIdTo, connection);
                        ps = connection.prepareStatement(
                                "insert into notice_mail\n" +
                                        "  (id, user_id_from, email_from, user_id_to, email_to, message, ae_main, is_read, datlast, ae_kind_event, delfl, subjectmsg)\n" +
                                        "values\n" +
                                        "  (seq_notice_mail_id.nextval, ?, ?, ?, ?, ?, ?, 0, ?, ?, 0, ?)");
                        ps.setLong(1, systemUserId);
                        ps.setString(2, systemUserEmail);
                        ps.setLong(3, userIdTo);
                        ps.setString(4, portalUser.getEmailAddress());
                        ps.setString(5, persistence.changeOfVariables(auditEvent.getAddMessage(), auditEvent.getId(),systemUserId,userIdTo,connection));
                        ps.setLong(6, auditEvent.getId());
                        ps.setTimestamp(7, new Timestamp(new Date().getTime()));
                        ps.setLong(8, idKindEvent);
                        ps.setString(9, persistence.changeOfVariables(auditEvent.getSubjectMsg(), auditEvent.getId(),systemUserId,userIdTo,connection));
                        ps.execute();
                        DbUtil.closeConnection(null, ps);
                    }
                }

                List<PortalUser> portalUserList = new ArrayList<PortalUser>();
                for (Long userId : userList) {
                    if(!persistence.checkUserNoticeOff(userId, auditEvent.getIdKindEvent(), 1)) {
                        portalUserList.add(getUserByUserId(userId, connection));
                    }
                }

                if(portalUserList.size() > 0) {
                    if (!persistence.sendNotice(auditEvent.getId(), systemUserId, portalUserList, auditEvent.getSubjectMsg(), auditEvent.getAddMessage(), connection))
                        updateAEMainSts(auditEvent.getId(), 2, connection);
                }

                updateAEMainSts(auditEvent.getId(), 1, connection);
            }
            connection.commit();
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection);
        }
    }

    private Set<Long> checkGroupRoleUser(Long userId, Long idKindEvent, Set<Long> userList, Connection connection) {
        Boolean noticeGroup = isNoticeUser("notice_group", userId, idKindEvent, connection);
        if (noticeGroup == null) {
            Boolean noticeRole = isNoticeUser("notice_role", userId, idKindEvent, connection);
            if (noticeRole != null) {
                if (noticeRole)
                    userList.add(userId);
            }
        } else if (noticeGroup)
            userList.add(userId);

        return userList;
    }

    @Override
    public Boolean isNoticeUser(String tableName, Long userId, Long kindEvent, Connection connection) {
        Boolean result = null;
        boolean localCon = false;
        PreparedStatement ps = null;
        ResultSet cursor = null;

        try {
            if (connection == null) {
                localCon = true;
                connection = getConnection();
            }

            StringBuilder sqlText = new StringBuilder();
            if (tableName.equals("notice_role")) {
                sqlText.append(
                        "select decode(nr.is_notice, null, 3, nr.is_notice) isNotice\n" +
                                "  from notice_role nr,\n" +
                                "       groups g,\n" +
                                "       group_users gu\n" +
                                " where nr.ae_kind_event = " + kindEvent + "\n" +
                                "   and nr.role = g.role_id\n" +
                                "   and g.group_id = gu.group_id\n" +
                                "   and gu.user_id = " + userId);
            } else if (tableName.equals("notice_group")) {
                sqlText.append(
                        "select decode(ng.is_notice, null, 3, ng.is_notice) isNotice\n" +
                                "  from notice_group ng,\n" +
                                "       group_users gu\n" +
                                " where ng.ae_kind_event = " + kindEvent + "\n" +
                                "   and ng.group_id = gu.group_id\n" +
                                "   and gu.user_id = " + userId);
            } else if (tableName.equals("notice_subjecttype")) {
                sqlText.append(
                        "select decode(ns.is_notice, null, 3, ns.is_notice) isNotice\n" +
                                "  from notice_subjecttype ns,\n" +
                                "       v_ref_subject_type st,\n" +
                                "       v_ref_respondent r,\n" +
                                "       f_users u\n" +
                                " where ns.st_rec_id = st.rec_id\n" +
                                "   and st.id = r.ref_subject_type\n" +
                                "   and st.begin_date = (select max(st1.begin_date)\n" +
                                "                          from v_ref_subject_type st1\n" +
                                "                         where st1.rec_id = st.rec_id\n" +
                                "                           and st1.begin_date <= sysdate)\n" +
                                "   and r.begin_date = (select max(r1.begin_date)\n" +
                                "                         from v_ref_respondent r1\n" +
                                "                        where r1.rec_id = r.rec_id\n" +
                                "                          and r1.begin_date <= sysdate)\n" +
                                "   and u.ref_respondent_rec_id = r.REC_ID\n" +
                                "   and u.blockfl = 0\n" +
                                "	and ns.ae_kind_event = " + kindEvent + "\n" +
                                "   and u.user_id = " + userId);
            } else if (tableName.equals("notice_respondent")) {
                sqlText.append(
                        "select decode(nr.is_notice, null, 3, nr.is_notice) isNotice\n" +
                                "  from notice_respondent nr,\n" +
                                "       v_ref_respondent r,\n" +
                                "       f_users u\n" +
                                " where nr.resp_rec_id = r.REC_ID\n" +
                                "   and r.begin_date = (select max(r1.begin_date)\n" +
                                "                         from v_ref_respondent r1\n" +
                                "                        where r1.rec_id = r.rec_id\n" +
                                "                          and r1.begin_date <= sysdate)\n" +
                                "   and u.ref_respondent_rec_id = r.REC_ID\n" +
                                "   and u.blockfl = 0\n" +
                                "	and nr.ae_kind_event = " + kindEvent + "\n" +
                                "   and u.user_id = " + userId);
            } else if (tableName.equals("notice_user")) {
                sqlText.append(
                        "select decode(is_notice, null, 3, is_notice) isNotice\n" +
                                "  from notice_user \n" +
                                " where ae_kind_event = " + kindEvent + "\n" +
                                "   and user_id = " + userId);
            }

            ps = connection.prepareStatement(sqlText.toString());
            cursor = ps.executeQuery();

            if (cursor.next()) {
                Integer isNotice = cursor.getInt("isNotice");
                if (isNotice == 0)
                    return false;
                else if (isNotice == 1)
                    return true;
                else if (isNotice == 3)
                    return null;
            } else {
                return result;
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(localCon ? connection : null, ps, cursor);
        }
        return result;
    }

    @Override
    public Role getRoleByUserId(Long userId) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection connection = null;
        Role result = new Role();
        try {
            connection = getConnection();
            ps = connection.prepareStatement(
                    "select r.*\n" +
                            "  from role r,\n" +
                            "       groups g,\n" +
                            "       group_users gu\n" +
                            " where r.id = g.role_id\n" +
                            "   and g.group_id = gu.group_id\n" +
                            "   and gu.user_id = ?");

            ps.setLong(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.setId(rs.getLong("ID"));
                result.setName(rs.getString("NAME"));
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
    public PortalUser getUserByUserId(long userId, Connection connection) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean localCon = false;
        PortalUser user = null;
        try {
            if (connection == null) {
                localCon = true;
                connection = getConnection();
            }
            ps = connection.prepareStatement("select u.id, u.user_id, u.screen_name, u.email, nvl(u.first_name, ' ') as first_name, nvl(u.last_name,' ' ) as last_name, " +
                    " nvl(u.middle_name, ' ') as middle_name, u.modified_date, u.blockfl, u.idn, u.ref_respondent_rec_id, u.ref_post_rec_id, u.must_sign, u.design_user_name " +
                    " from f_users u" +
                    " where u.user_id = ?");

            ps.setLong(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                user = new PortalUser();

                user.setId(rs.getLong("ID"));
                user.setUserId(rs.getLong("USER_ID"));
                user.setFirstName(rs.getString("FIRST_NAME"));
                user.setLastName(rs.getString("LAST_NAME"));
                user.setMiddleName(rs.getString("MIDDLE_NAME"));
                user.setScreenName(rs.getString("SCREEN_NAME"));
                user.setEmailAddress(rs.getString("EMAIL"));
                user.setIdn(rs.getString("IDN"));
                user.setRespondentId(rs.getLong("REF_RESPONDENT_REC_ID"));
                user.setRefPostId(rs.getLong("REF_POST_REC_ID"));
                user.setModifiedDate(DataUtils.convert(rs.getDate("MODIFIED_DATE")));
                user.setBlocked(rs.getInt("BLOCKFL") > 0);
                user.setMustSign(rs.getInt("MUST_SIGN") > 0);
                user.setDesignUserName(rs.getString("DESIGN_USER_NAME"));
            }

        } catch (SQLException e) {
            throw new EJBException(e);
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(localCon ? connection : null, ps, rs);
        }

        return user;
    }

    @Override
    public void updateAEMainSts(Long id, int idSts, Connection connection) {
        boolean localCon = false;
        PreparedStatement ps = null;
        try {
            if (connection == null) {
                localCon = true;
                connection = getConnection();
            }
            ps = connection.prepareStatement("UPDATE AE_MAIN SET notice_sts = ? WHERE id = ?");

            ps.setInt(1, idSts);
            ps.setLong(2, id);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");
            connection.commit();
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(localCon ? connection : null, ps);
        }
    }

    // Сохранение данных входных форм в виде списка
    @Schedule(hour = "*", minute = "*/5", second = "*")
    @Override
    public void saveToReportHistoryListAll() {
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            List<ReportHistory> reportHistorys = getReportHistoryNoLobsList(0);
            for (ReportHistory reportHistory : reportHistorys) {
                Map<String, ReportHistoryList> kvRh = getReportHistoryMap(reportHistory, connection);
                deleteReportHistoryList(reportHistory.getId(), connection);
                insertReportHistoryLists(reportHistory.getId(), kvRh, connection);
                connection.commit();
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException se) {
                    logger.error(se.getMessage());
                }
            }
            throw new EJBException(e);
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException se) {
                    logger.error(se.getMessage());
                }
            }
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection);
        }
    }

    private Map<String, ReportHistoryList> getReportHistoryMap(ReportHistory reportHistory, Connection connection) {

        if (reportHistory == null || reportHistory.getId() == null) throw new EJBException("reportHistory is null");

        String inputValueCheck = getFormHistoryIVCH(reportHistory.getReport().getFormCode(), reportHistory.getReport().getReportDate(), connection);
        if (inputValueCheck == null) throw new EJBException("inputValueCheck is null");

        Map<String, ReportHistoryList> kvRh = new HashMap<String, ReportHistoryList>();

        String reportHistoryData = getReportHistoryData(reportHistory.getId(), connection);
        if (reportHistoryData == null) return kvRh;

        List<InputValueCheck> inputValueChecks;
        Gson gson = new Gson();
        Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {
        }.getType();
        inputValueChecks = gson.fromJson(inputValueCheck, typeListInputValueCheck);

        String jsonData = reportHistoryData;
        Type typeMapStringString = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> kvMap = gson.fromJson(jsonData, typeMapStringString);

        String valueType;
        String ref;
        Long multiValue;

        for (Map.Entry<String, String> entry : kvMap.entrySet()) {
            if ((reportHistory.getReport().getFormCode().equalsIgnoreCase("balance_accounts")) && (entry.getKey().startsWith("balance_accounts_array*sum:code:"))) {
                ReportHistoryList reportHistoryList = new ReportHistoryList();
                reportHistoryList.setReportHisoryId(reportHistory.getId());
                reportHistoryList.setKey(entry.getKey());
                reportHistoryList.setValue(entry.getValue());
                reportHistoryList.setValueType("n0");
                reportHistoryList.setRef(null);
                reportHistoryList.setMultiValue(0L);
                kvRh.put(entry.getKey(), reportHistoryList);
            } else {
                for (InputValueCheck item : inputValueChecks) {
                    if (item.getKey() != null) {
                        if (entry.getKey() != null &&
                                ((item.getGroupId() == null && entry.getKey().contains(item.getKey())) ||
                                        (item.getGroupId() != null && entry.getKey().startsWith(item.getGroupId())))) {
                            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                                if (item.getValueType() != null) {
                                    if (item.getValueType().equalsIgnoreCase("int"))
                                        valueType = "n0";
                                    else if (item.getValueType().equalsIgnoreCase("float")) {
                                        if (item.getMask() != null && item.getMask().startsWith("money"))
                                            valueType = "n" + item.getMask().substring(item.getMask().length() - 2);
                                        else
                                            valueType = item.getValueType();
                                    } else if (item.getValueType().equalsIgnoreCase("date"))
                                        valueType = "d";
                                    else if (item.getValueType().equalsIgnoreCase("time"))
                                        valueType = "t";
                                    else
                                        valueType = item.getValueType();
                                } else if (item.getMask() != null && item.getMask().startsWith("money"))
                                    valueType = "n" + item.getMask().substring(item.getMask().length() - 2);
                                else
                                    valueType = null;

                                if (item.getRef() != null)
                                    ref = item.getRef();
                                else
                                    ref = null;

                                if (item.getMultiValue() != null && item.getMultiValue())
                                    multiValue = 1L;
                                else
                                    multiValue = 0L;

                                ReportHistoryList reportHistoryList = new ReportHistoryList();
                                reportHistoryList.setReportHisoryId(reportHistory.getId());
                                reportHistoryList.setKey(entry.getKey());
                                reportHistoryList.setValue(entry.getValue());
                                reportHistoryList.setValueType(valueType);
                                reportHistoryList.setRef(ref);
                                reportHistoryList.setMultiValue(multiValue);
                                kvRh.put(entry.getKey(), reportHistoryList);
                            }
                        }
                    }
                }
            }
        }

        return kvRh;
    }

    private Long insertReportHistoryLists(long reportHistoryId, Map<String, ReportHistoryList> kvRh, Connection connection) {
        Long result = null;
        PreparedStatement ps = null;
        boolean localCon = false;
        try {
            if (connection == null) {
                localCon = true;
                connection = getConnection();
            }

            ps = connection.prepareStatement("insert into report_history_list " +
                    "(id, report_history_id, key, value, value_type, ref, multi_value) " +
                    "values " +
                    "(seq_report_history_list.nextval, ?, ?, ?, ?, ?, ?)");
            for (Map.Entry<String, ReportHistoryList> entry : kvRh.entrySet()) {
                ps.clearParameters();
                ps.setLong(1, entry.getValue().getReportHisoryId());
                ps.setString(2, entry.getValue().getKey());
                ps.setString(3, entry.getValue().getValue());
                ps.setString(4, entry.getValue().getValueType());
                ps.setString(5, entry.getValue().getRef());
                ps.setLong(6, entry.getValue().getMultiValue());
                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
            }

            /* OracleConnection oraConn = connection.unwrap(OracleConnection.class);
            ReportHistoryList[] arr = reportHistoryLists.toArray(new ReportHistoryList[reportHistoryLists.size()]);
            java.sql.Array array = oraConn.createARRAY("REPORT_HISTORY_LIST_TABLE", arr);
            ps = connection.prepareStatement("insert into report_history_list " +
                    "select seq_report_history_list.nextval, report_history_id, key, value, value_type, ref, multi_value from table(?)");
            ps.setArray(1, array);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) result = generatedKeys.getLong(1); */

            updateReportHistoryIsExistList(reportHistoryId, true, connection);
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(localCon ? connection : null, ps);
        }
        return result;
    }

    private void deleteReportHistoryList(long reportHistoryId, Connection connection) {
        PreparedStatement ps = null;
        boolean localCon = false;
        try {
            if (connection == null) {
                localCon = true;
                connection = getConnection();
            }
            ps = connection.prepareStatement(
                    "delete from report_history_list " +
                            "where report_history_id = ?");
            ps.setLong(1, reportHistoryId);
            ps.executeUpdate();

            updateReportHistoryIsExistList(reportHistoryId, false, connection);
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(localCon ? connection : null, ps);
        }
    }

    private void updateReportHistoryIsExistList(long reportHistoryId, boolean isExistList, Connection connection) {
        PreparedStatement ps = null;
        boolean localCon = false;
        try {
            if (connection == null) {
                localCon = true;
                connection = getConnection();
            }
            ps = connection.prepareStatement(
                    "update report_history " +
                            "set is_exist_list = ? " +
                            "where id = ?");
            ps.setInt(1, isExistList ? 1 : 0);
            ps.setLong(2, reportHistoryId);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Update an item failed, no rows affected.");
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(localCon ? connection : null, ps);
        }
    }

    private List<ReportHistory> getReportHistoryNoLobsList(int isExistList) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ReportHistory> result = new ArrayList<ReportHistory>();
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT rh.id, rh.report_id, r.report_date, r.form_code FROM report_history rh, reports r, forms f, form_history fh " +
                    "WHERE rh.is_exist_list=? and rh.report_id = r.id and r.form_code = f.code and f.type_code = 'INPUT' and " +
                    "f.id = fh.form_id and r.report_date >= fh.begin_date and (r.report_date < fh.end_date or fh.end_date is null) and fh.is_fill_list = 1 ORDER BY rh.id");
            ps.setInt(1, isExistList);
            rs = ps.executeQuery();
            while (rs.next()) {
                ReportHistory reportHistory = new ReportHistory();
                reportHistory.setId(rs.getLong("id"));
                Report report = new Report();
                report.setId(rs.getLong("report_id"));
                report.setReportDate(rs.getDate("report_date"));
                report.setFormCode(rs.getString("form_code"));
                reportHistory.setReport(report);
                result.add(reportHistory);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

    private String getReportHistoryData(long reportHistoryId, Connection connection) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean localCon = false;
        String result = null;
        try {
            if (connection == null) {
                localCon = true;
                connection = getConnection();
            }
            ps = connection.prepareStatement("SELECT rh.data FROM report_history rh WHERE rh.id=?");
            ps.setLong(1, reportHistoryId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Clob clobData = rs.getClob("data");
                if (clobData != null) {
                    result = clobData.getSubString(1, (int) clobData.length());
                    clobData.free();
                }
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(localCon ? connection : null, ps, rs);
        }
        return result;
    }

    private String getFormHistoryIVCH(String formCode, Date date, Connection connection) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean localCon = false;
        String result = null;
        try {
            if (connection == null) {
                localCon = true;
                connection = getConnection();
            }
            ps = connection.prepareStatement("SELECT f.id as form_id," +
                    "fh.input_value_checks " +
                    "FROM forms f, " +
                    "form_history fh " +
                    "WHERE f.id = fh.form_id " +
                    "AND f.code = ? " +
                    "AND fh.begin_date = (select max(fh1.begin_date) " +
                    "from form_history fh1 " +
                    "where fh1.form_id = fh.form_id " +
                    "and fh1.begin_date <= nvl(?,sysdate) " +
                    "and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");

            ps.setString(1, formCode);
            ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
            ps.setTimestamp(3, date == null ? null : new java.sql.Timestamp(date.getTime()));
            rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getString("input_value_checks");
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(localCon ? connection : null, ps, rs);
        }
        return result;
    }

    // -------------------------------------------------------------------------------------------------------------

    @Schedule(hour = "17", minute = "10")
    @Override
    public void overdueReports() {
        List<ReportPeriod> reports = persistence.getAllOverdueReports(1);
        List<ReportPeriod> overdueReports = new ArrayList<ReportPeriod>();
        List<ReportPeriod> reportsForNotice = new ArrayList<ReportPeriod>();
        List<ReportPeriod> reportsForApprove = new ArrayList<ReportPeriod>();
        for (ReportPeriod r : reports) {
            if (r.getStatus() != null && r.getStatus().equalsIgnoreCase(ReportStatus.Status.COMPLETED.name()) && r.getLeftDays() < 0) {
                reportsForApprove.add(r);
            } else if (r.getStatus() == null
                    || r.getStatus().equalsIgnoreCase(ReportStatus.Status.DRAFT.name())
                    || r.getStatus().equalsIgnoreCase(ReportStatus.Status.SIGNED.name())) {
                if (r.getLeftDays() < 0)
                    overdueReports.add(r);
                else
                    reportsForNotice.add(r);
            }
        }

        PortalUser portalUser = persistence.getUserByUserId(systemUserId, null);
        AbstractUser user = new AbstractUser();
        user.setId(portalUser.getUserId());
        user.setDisplayName(portalUser.getScreenName());
        user.setLocation(userLocation == null ? "127.0.0.1" : userLocation);


        for (ReportPeriod r : overdueReports) {
            String msg = MessageFormat.format("Отчет {0}-{1} за {2} просрочен на {3} дней",
                    r.getFormCode(),
                    r.getFormName(),
                    Convert.dateFormatRus.format(r.getReportDate()),
                    Math.abs(r.getLeftDays()));

            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(r.getFormCode());
            auditEvent.setNameObject(null);
            auditEvent.setIdKindEvent((long) 59);
            auditEvent.setDateEvent(new Date());
            auditEvent.setIdRefRespondent(r.getRespondentId());
            auditEvent.setDateIn(r.getReportDate());
            auditEvent.setRecId(null);
            auditEvent.setUserId(user.getId());
            auditEvent.setUserLocation(userLocation);
            auditEvent.setAddMessage(msg);
            try {
                persistence.insertAuditEvent(auditEvent);
            } catch (OracleException e) {
                e.printStackTrace();
            }
        }

        for (ReportPeriod r : reportsForNotice) {
            String msg = MessageFormat.format("Срок предоставления отчета {0}-{1} за {2} через {3} дней",
                    r.getFormCode(),
                    r.getFormName(),
                    Convert.dateFormatRus.format(r.getReportDate()),
                    Math.abs(r.getLeftDays()));

            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(r.getFormCode());
            auditEvent.setNameObject(null);
            auditEvent.setIdKindEvent((long) 117);
            auditEvent.setDateEvent(new Date());
            auditEvent.setIdRefRespondent(r.getRespondentId());
            auditEvent.setDateIn(r.getReportDate());
            auditEvent.setRecId(null);
            auditEvent.setUserId(user.getId());
            auditEvent.setUserLocation(userLocation);
            auditEvent.setAddMessage(msg);
            try {
                persistence.insertAuditEventWithParams(auditEvent,
                        new AuditEventParam[]{new AuditEventParam(
                                "left_days",
                                ValueType.NUMBER_0,
                                Variant.createNumber0(r.getLeftDays())
                        )}
                );
            } catch (OracleException e) {
                e.printStackTrace();
            }
        }

        for (ReportPeriod r : reportsForApprove) {
            performControl.runTask(r.getReportDate(), r.getFormCode(), r.getIdn(), true, r.getSubjectTypeRecId(), false);
            ReportHistory h = persistence.getLastReportHistoryByReportIdNoLobs(r.getReportId(), true, null);
            if (h.getControlResultCode2() == null || h.getControlResultCode2().equalsIgnoreCase(ControlResult.ResultType.SUCCESS.name())) {

                AuditEvent auditEvent = new AuditEvent();
                auditEvent.setIdNameEvent((long) 2);
                auditEvent.setCodeObject(r.getFormCode());
                auditEvent.setNameObject(null);
                auditEvent.setIdKindEvent(22L);
                auditEvent.setDateEvent(new Date());
                auditEvent.setIdRefRespondent(respondentId);
                auditEvent.setDateIn(r.getReportDate());
                auditEvent.setRecId(r.getReportId());
                auditEvent.setUserId(user.getId());
                auditEvent.setUserLocation(user.getLocation());

                persistence.setReportApproved(r.getReportId(), true, user, new Date(), auditEvent);
            }
        }
    }

    @Override
    public synchronized void updateBalanceAccTemplate(Long respId, Long userId, String userLocation){
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            Calendar startDate = Calendar.getInstance();
            startDate.setTime(RefBalanceAccItem.dateFirstVersion);
            Date maxDate = getMaxBeginDate("ref_balance_account", connection);
            Date finishDate = new Date();
            finishDate = maxDate.after(finishDate) ? maxDate : finishDate;
            Date firstDate;
            Date secondDate;

            persistence.deleteFormHistoryByCode(RefBalanceAccItem.balanceCode, connection);
            persistence.deleteFormHistoryByCode(RefBalanceAccItem.balanceCodeOut, connection);

            insertBalanceAccTemplate(RefBalanceAccItem.dateFirstVersion, 1, connection);
            insertBalanceAccTemplate(RefBalanceAccItem.dateSecVersion, 2, connection);

            while (startDate.getTime().before(finishDate)){
                firstDate = startDate.getTime();
                startDate.add(Calendar.MONTH, 1);
                secondDate = startDate.getTime();
                if(getDiffCountBalanceAcc(connection, firstDate, secondDate) > 0 && !secondDate.equals(RefBalanceAccItem.dateFirstVersion) && !secondDate.equals(RefBalanceAccItem.dateSecVersion)){
                    insertBalanceAccTemplate(secondDate, secondDate.before(RefBalanceAccItem.dateSecVersion) ? 1 : 2, connection);
                }
            }

            updateFormHistoryDates(connection, RefBalanceAccItem.balanceCode);
            updateFormHistoryDates(connection, RefBalanceAccItem.balanceCodeOut);

            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setIdNameEvent(4L);
            auditEvent.setCodeObject("ref_balance_accounts/out");
            auditEvent.setNameObject("Обновление шаблонов");
            auditEvent.setIdKindEvent(32L);
            auditEvent.setDateEvent(finishDate);
            auditEvent.setIdRefRespondent(respId);
            auditEvent.setDateIn(finishDate);
            auditEvent.setRecId(null);
            auditEvent.setUserId(userId);
            auditEvent.setUserLocation(userLocation);
            persistence.insertAuditEvent(auditEvent);

            connection.commit();

        }catch (Exception e){
            throw new EJBException(e);
        }finally {
            DbUtil.closeConnection(connection);
        }
    }

    private void insertBalanceAccTemplate(Date date, int verNum, Connection connection){
        try {
            BalanceAccountsFormHtml balanceAccountsFormHtml = new BalanceAccountsFormHtml(reference);
            String xmlIn = balanceAccountsFormHtml.buildBalanceAccountsFormTemplate(verNum, date);
            persistence.updateBalanceAccountTemplate(xmlIn, RefBalanceAccItem.balanceCode, 32L, respondentId, systemUserId, userLocation, date, connection);
            updateBalanceAccExcelTemplate(true, RefBalanceAccItem.balanceCode, date, connection);

            BalanceAccountsOutFormHtml balanceAccountsOutFormHtml = new BalanceAccountsOutFormHtml(reference);
            String xmlOut = balanceAccountsOutFormHtml.buildBalanceAccountsOutFormTemplate(verNum, date);
            persistence.updateBalanceAccountTemplate(xmlOut, RefBalanceAccItem.balanceCodeOut, 33L, respondentId, systemUserId, userLocation, date, connection);
            updateBalanceAccExcelTemplate(false, RefBalanceAccItem.balanceCodeOut, date, connection);

        }catch (SQLException e) {
            throw new EJBException(e);
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    private int getDiffCountBalanceAcc(Connection connection, Date firstDate, Date secondDate){
        int result;
        CallableStatement stmt = null;
        try {
            stmt = connection.prepareCall("BEGIN ? := getDiffBalanceAcc(?,?); end;");
            stmt.registerOutParameter(1, OracleTypes.INTEGER);
            stmt.setDate(2, new java.sql.Date(firstDate.getTime()));
            stmt.setDate(3, new java.sql.Date(secondDate.getTime()));
            stmt.execute();
            result = stmt.getInt(1);
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(null, stmt);
        }
        return result;
    }

    private void updateFormHistoryDates(Connection connection, String formCode){
        CallableStatement stmt = null;
        try {
            stmt = connection.prepareCall("BEGIN ? := update_form_history_date(?); end;");
            stmt.registerOutParameter(1, OracleTypes.INTEGER);
            stmt.setString(2, formCode);
            stmt.execute();
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(null, stmt);
        }
    }

    private void updateBalanceAccExcelTemplate(Boolean input, String code, Date secondDate, Connection connection) throws Exception{
        FormListItem formListItem = new FormListItem();
        formListItem.setLastUpdateXlsOut(new Date());
        Long formHistoryId = persistence.getFormHistoryId(code, secondDate, connection);
        formListItem.setFhId(formHistoryId);

        Template template = new Template();
        template.setCode(code);
        template.setBeginDate(secondDate);
        byte[] xlsxTemplate = reference.getTemplateData(template).getXlsOut();
        if(xlsxTemplate == null)
            throw new EJBException("Не найден шаблон Excel");

        if(input){
            template.setCode(code + "_xlsm");
            byte[] xlsmTemplate = reference.getTemplateData(template).getXlsOut();
            if(xlsmTemplate == null)
                throw new EJBException("Не найден шаблон Excel");
            formListItem.setLastUpdateXls(new Date());
            formListItem.setXls(xlsmTemplate);
            formListItem.setXlsVersion(3);
            persistence.updateFormHistoryWithXls(formListItem, null);
        }

        BalanceAccExcelGenerate excelGenerate = new BalanceAccExcelGenerate(xlsxTemplate, secondDate, connection);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        excelGenerate.getWorkbook().write(outputStream);
        xlsxTemplate = outputStream.toByteArray();
        outputStream.close();

        formListItem.setXlsOut(xlsxTemplate);

        persistence.updateFormHistoryWithXlsOut(formListItem, null);
    }

    private Date getMaxBeginDate(String refCode, Connection connection){
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean localCon = false;
        Date result = null;
        try {
            if (connection == null) {
                localCon = true;
                connection = getConnection();
            }
            ps = connection.prepareStatement(
                    "SELECT max(begin_date) as begin_date from v_" + refCode);
            rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getDate("begin_date");
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(localCon ? connection : null, ps, rs);
        }
        return result;

    }
}
