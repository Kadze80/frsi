package ejb;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import dataform.*;
import entities.*;
import entities.Error;
import excelreport.*;
import excelreport.impl.ExcelReportImpl;
import form.calcfield.CalcField;
import form.calcfield2.CalcField2;
import form.calcfield2.DataProvider2;
import form.excel.*;
import form.html.HtmlParser;
import form.process.WebFormProcessor;
import form.tag.DynamicFunction;
import form.tag.FormType;
import jaxb.HtmlOption;
import jaxb.HtmlSelect;
import jaxb.HtmlSpan;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.apache.pdfbox.util.Matrix;
import parser.DataType;
import util.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;

@Stateless
public class PersistenceBean implements PersistenceLocal, PersistenceRemote {
	private static final Logger logger = Logger.getLogger("fileLogger");
	private static final String JDBC_POOL_NAME = "jdbc/FrsiPool";
	private static Map<String, Set<String>> compatibleStatuses = new HashMap<String, Set<String>>();

	private static String tempDir = "d:\\temp";
	private static String tempExcelDir = "d:\\temp_excel";
	private static long NOTICE_NEW_MESSAGE = 57L;

	static {


		compatibleStatuses.put(ReportStatus.Status.DRAFT.name(), new HashSet<String>(Arrays.asList(
				ReportStatus.Status.DRAFT.name(),
				ReportStatus.Status.SIGNED.name(),
				ReportStatus.Status.COMPLETED.name(),
				ReportStatus.Status.DISAPPROVED.name()
		)));
		compatibleStatuses.put(ReportStatus.Status.APPROVED.name(), new HashSet<String>(Arrays.asList(
				ReportStatus.Status.COMPLETED.name(),
				ReportStatus.Status.DISAPPROVED.name()
		)));
		compatibleStatuses.put(ReportStatus.Status.DISAPPROVED.name(), new HashSet<String>(Arrays.asList(
				ReportStatus.Status.APPROVED.name()
		)));
		compatibleStatuses.put(ReportStatus.Status.SIGNED.name(), new HashSet<String>(Arrays.asList(
				ReportStatus.Status.DRAFT.name(),
				ReportStatus.Status.SIGNED.name()
		)));
		compatibleStatuses.put(ReportStatus.Status.COMPLETED.name(), new HashSet<String>(Arrays.asList(

				ReportStatus.Status.SIGNED.name()
		)));
		compatibleStatuses.put("SU_OPEN", new HashSet<String>(Arrays.asList(
				ReportStatus.Status.APPROVED.name(),
				ReportStatus.Status.DISAPPROVED.name(),
				ReportStatus.Status.COMPLETED.name()
		)));
	}

	@EJB private CoreLocal core;
	@EJB private ReferenceLocal reference;
	@EJB private PdfConverterLocal pdfConverter;
	private DataSource dataSource;
	private Gson gson = new Gson();
	private DateEval dateEval = new DateEval();
	private SqlExecutor sqlExecutor = new SqlExecutor();
	private String systemUserEmail;
	private int daysBeforeOverdueDateSettingDefaultValue = 7;
	private String groupPrefix = "ФРСП."; // Set JVM option -Dfrsi.group.prefix in GlassFish to override this default value.

	@PostConstruct
	@Override
	public void init() {
		Date dateStart = new Date();

		// Connect to JDBC pool
		try {
			Context context = new InitialContext();
			dataSource = (DataSource) context.lookup(JDBC_POOL_NAME);
			logger.info("Connected to " + JDBC_POOL_NAME);
			systemUserEmail = "frsp@nationalbank.kz";
		} catch (NamingException e) {
			logger.error("Could not connect to " + JDBC_POOL_NAME);
			throw new EJBException(e);
		}

		// TESTS

        /*
		*/

		String propGroupPrefix = System.getProperty("frsi.group.prefix");
		if (propGroupPrefix != null) groupPrefix = propGroupPrefix;

		String downloadTempDir = System.getProperty("frsi.tempdir");
		if (downloadTempDir != null) tempDir = downloadTempDir;

		String downloadTempExcelDir = System.getProperty("frsi.tempexceldir");
		if (downloadTempExcelDir != null) tempExcelDir = downloadTempExcelDir;

		Date dateEnd = new Date();
		long duration = dateEnd.getTime() - dateStart.getTime();
		logger.debug(MessageFormat.format("t: {0}, d: {1} ms", dateStart, duration));
	}

	private Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@PreDestroy
	public void preDestroy() {
	}

	@Override
	public String getTestMessage() {
		return "Test message from Persistence EJB";
	}

	// Form CRUD
	@Override
	public List<Form> getReportFormsByUserIdNoDate(Long userId, List<RefNpaItem> npaList) {
		Connection connection = null;
		List<Form> result = new ArrayList<Form>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
					String sqlText =
					"SELECT DISTINCT f.id as form_id," +
							"f.code," +
							"f.type_code," +
							"fh.name," +
							"fh.short_name " +
					"FROM forms f, " +
							"v_report_history_short fh, " +
							"F_SESSION_RESP_FORMS sf, " +
							"npa_forms nf "+
					"WHERE f.id = fh.form_id "+
							"AND fh.id = nf.form_history_id(+) "+
							"AND sf.form_code = f.code "+
							"AND sf.user_id = ? " +
							"AND f.type_code in ('INPUT', 'INPUT_RAW')";

			if(npaList != null && npaList.size() > 0) {
				StringBuilder sbNpa = new StringBuilder();
				Boolean noNpa = false;
				for (int i = 0; i < npaList.size(); i++) {
					if(npaList.get(i).getId() == 0)
						noNpa = true;
					if (i > 0)
						sbNpa.append(",");
					sbNpa.append(npaList.get(i).getRecId());
				}
				if(noNpa) {
					sqlText = sqlText + " and (nf.npa_rec_id is null or nf.npa_rec_id in (" + sbNpa.toString() + "))";
				}else {
					sqlText = sqlText + " and nf.npa_rec_id in (" + sbNpa.toString() + ")";
				}
			}

			ps = connection.prepareStatement(sqlText);
			ps.setLong(1, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = new Form();
				form.setId(rs.getLong("form_id"));
				form.setCode(rs.getString("code"));
				form.setTypeCode(rs.getString("type_code"));
				FormHistory formHistory = new FormHistory();
				formHistory.setName(rs.getString("name"));
				formHistory.setShortName(rs.getString("short_name"));
				form.setFormHistory(formHistory);
				result.add(form);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	/*@Override
	@Deprecated
	public List<Form> getFormsNoLob(Date date) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Form> result = new ArrayList<Form>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code " +
					"FROM forms f, " +
					"form_history fh " +
					"WHERE f.id = fh.form_id " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");

			ps.setTimestamp(1, date == null ? null : new java.sql.Timestamp(date.getTime()));
			ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				result.add(form);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try { rs.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
			try { connection.close();
			} catch (Exception e) {
			}
		}
		return result;
	}*/

	@Override
	public List<Form> getFormsNoLob() {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Form> result = new ArrayList<Form>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("" +
					"SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code " +
					"FROM forms f, " +
					"form_history fh " +
					"WHERE f.id = fh.form_id " +
					"AND fh.begin_date <= sysdate " +
					"AND (fh.end_date is null or fh.end_date > sysdate)");

			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				result.add(form);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	/*@Override
	@Deprecated
	public List<Form> getInputForms() {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Form> result = new ArrayList<Form>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("" +
					"SELECT f.id as form_id, " +
					"f.code, " +
					"f.type_code, " +
					"fh.name, " +
					"fh.short_name " +
					"FROM forms f," +
					"v_report_history_short fh " +
					"WHERE f.type_code = 'INPUT' " +
					"AND f.id = fh.form_id");
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				result.add(form);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try { rs.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
			try { connection.close();
			} catch (Exception e) {
			}
		}
		return result;
	}*/

	@Override
	public List<Form> getFormsByReportDate(Date reportDate) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Form> result = new ArrayList<Form>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code " +
					"FROM forms f, " +
					"form_history fh " +
					"WHERE f.id = fh.form_id " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setDate(1, repDate);
			ps.setDate(2, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				result.add(form);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	/*@Override
	@Deprecated
	public List<Form> getFormsByReportDateSubjectType(Date reportDate, String subject_type_code) {
		Connection connection = null;
		List<Form> result = new ArrayList<Form>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code " +
					"FROM forms f, " +
					"form_history fh," +
					"subjecttype_forms stf " +
					"WHERE f.id = fh.form_id " +
					"AND f.type_code = 'INPUT' " +
					"AND f.code = stf.form_name " +
					"AND stf.code = ? " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			ps.setString(1, subject_type_code);
			ps.setDate(2, repDate);
			ps.setDate(3, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				result.add(form);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try { rs.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
			try { connection.close(); } catch (Exception e) {}
		}
		return result;
	}*/

	@Override
	public List<Form> getFormsByCodeReportDate(String code, Date reportDate) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Form> result = new ArrayList<Form>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code " +
					"FROM forms f, " +
					"form_history fh " +
					"WHERE f.id = fh.form_id " +
					"AND f.code = ? " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			ps.setString(1, code);
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setDate(2, repDate);
			ps.setDate(3, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				result.add(form);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public List<Form> getFormsByCodeLanguageReportDate(String code, String languageCode, Date reportDate, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		List<Form> result = new ArrayList<Form>();
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code," +
					"fh.xml_version," +
					"fh.xls_version, " +
					"fh.period_count " +
					"FROM forms f, " +
					"form_history fh " +
					"WHERE f.id = fh.form_id " +
					"AND f.code = ? " +
					"AND LOWER(fh.language_code) = ? " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			ps.setString(1, code);
			ps.setString(2, languageCode.toLowerCase());
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setDate(3, repDate);
			ps.setDate(4, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				form.getFormHistory().setXmlVersion(rs.getInt("xml_version"));
				form.getFormHistory().setXlsVersion(rs.getInt("xls_version"));
				form.getFormHistory().setPeriodCount(rs.getInt("period_count"));
				result.add(form);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public List<Form> getFormsByUserSubjectTypeRecIds(long userId, List<Long> stRecIds, List<RefNpaItem> npaList) {
		Connection connection = null;
		List<Form> result = new ArrayList<Form>();
		PreparedStatement ps = null;
		ResultSet rs = null;

		if (stRecIds == null)
			throw new NullPointerException();
		if (stRecIds.size() == 0)
			return result;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < stRecIds.size(); i++) {
			if (i > 0)
				sb.append(",");
			sb.append(stRecIds.get(i));
		}

		try {
			connection = getConnection();
			String sqlText =
			"SELECT DISTINCT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name " +
				"FROM forms f, " +
					"v_report_history_short fh, " +
					"F_SESSION_RESP_FORMS sf, " +
					"npa_forms nf "+
				"WHERE f.id = fh.form_id "+
					"and sf.form_code = f.code "+
					"and fh.id = nf.form_history_id(+) "+
					"AND sf.user_id = ? " +
					"AND f.type_code in ('INPUT', 'INPUT_RAW')" +
					"AND exists (select 'x' from subjecttype_forms stf " +
					"where stf.ref_subject_type_rec_id in (" + sb.toString() + ") and stf.form_code=f.code " +
					"group by stf.form_code)";


			if(npaList != null && npaList.size() > 0) {
				StringBuilder sbNpa = new StringBuilder();
				Boolean noNpa = false;
				for (int i = 0; i < npaList.size(); i++) {
					if(npaList.get(i).getId() == 0)
						noNpa = true;
					if (i > 0)
						sbNpa.append(",");
					sbNpa.append(npaList.get(i).getRecId());
				}
				if(noNpa) {
					sqlText = sqlText + " and (nf.npa_rec_id is null or nf.npa_rec_id in (" + sbNpa.toString() + "))";
				}else {
					sqlText = sqlText + " and nf.npa_rec_id in (" + sbNpa.toString() + ")";
				}
			}

			ps = connection.prepareStatement(sqlText);
			ps.setLong(1, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = new Form();
				form.setId(rs.getLong("form_id"));
				form.setCode(rs.getString("code"));
				form.setTypeCode(rs.getString("type_code"));
				FormHistory formHistory = new FormHistory();
				formHistory.setName(rs.getString("name"));
				formHistory.setShortName(rs.getString("short_name"));
				form.setFormHistory(formHistory);
				result.add(form);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public List<Form> getFormsByUserIdDate(long userId, Date reportDate, RefRespondentItem respondentItem, boolean editForms) {
		Connection connection = null;
		List<Form> result = new ArrayList<Form>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			String permissionName = editForms ? "F:EDIT" : "F:SHOW";
			ps = connection.prepareStatement(
					"SELECT DISTINCT f.id as form_id," +
							"f.code," +
							"f.type_code," +
							"fh.name," +
							"fh.short_name," +
							"fh.id as fh_id," +
							"fh.begin_date," +
							"fh.end_date," +
							"fh.last_update_xml," +
							"fh.last_update_xls," +
							"fh.last_update_xls_out," +
							"fh.tag," +
							"fh.language_code," +
							"p.code as period_code " +
							"FROM forms f, " +
							"form_history fh, " +
							"V_USER_RESP_FORMS sf, " +
							"subjecttype_forms s," +
							"rep_per_dur_months p " +
							"WHERE f.id = fh.form_id " +
							"AND f.code = sf.FORM_CODE " +
							"AND sf.ref_respondent_rec_id = ?" +
							"AND sf.RIGHT_ITEM_NAME = ? " +
							"AND f.code = s.form_code " +
							"AND s.period_id = p.id " +
							"AND sf.user_id = ? " +
							"AND pkg_frsi_util.check_period(p.code, ?) = 1 " +
							"AND f.type_code in ('INPUT', 'INPUT_RAW') " +
							"AND s.ref_subject_type_rec_id = ? " +
							"AND fh.begin_date = (select max(fh1.begin_date) " +
							"from form_history fh1 " +
							"where fh1.form_id = fh.form_id " +
							"and fh1.begin_date <= nvl(?,sysdate)" +
							"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			ps.setLong(1, respondentItem.getRecId());
			ps.setString(2, permissionName);
			ps.setLong(3, userId);
			ps.setDate(4, repDate);
			ps.setLong(5, respondentItem.getRefSubjectTypeRecId());
			ps.setDate(6, repDate);
			ps.setDate(7, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				form.getFormHistory().setPeriodCode(rs.getString("period_code"));
				result.add(form);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public List<Form> getFormsByUserIdDateSTList(long userId, Date reportDate, List<Long> subjectTypeRecIdList) {
		Connection connection = null;
		List<Form> result = new ArrayList<Form>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());


			StringBuilder sbSubjectTypeList = new StringBuilder();
			for (int i = 0; i < subjectTypeRecIdList.size(); i++) {
				if (i > 0)
					sbSubjectTypeList.append(",");
				sbSubjectTypeList.append(subjectTypeRecIdList.get(i));
			}
			ps = connection.prepareStatement(
					"SELECT DISTINCT f.id as form_id," +
							"f.code," +
							"f.type_code," +
							"fh.name," +
							"fh.short_name," +
							"fh.id as fh_id," +
							"fh.begin_date," +
							"fh.end_date," +
							"fh.last_update_xml," +
							"fh.last_update_xls," +
							"fh.last_update_xls_out," +
							"fh.tag," +
							"fh.language_code," +
							"t.codes " +
							"FROM forms f, " +
							"form_history fh," +
							"(select stf.form_code," +
							"listagg(p.code, ',') within group (order by p.code) codes " +
							"from subjecttype_forms stf," +
							" rep_per_dur_months p  " +
							"where stf.ref_subject_type_rec_id in (" + sbSubjectTypeList.toString() + ")" +
							"and stf.period_id = p.id " +
							"and pkg_frsi_util.check_period(p.code, ?) = 1 " +
							"group by stf.form_code) t " +
//							   "having count(distinct stf.ref_subject_type_rec_id) = ?) t " +
							"WHERE f.id = fh.form_id " +
							"AND f.code = t.form_code " +
							"AND f.type_code = 'INPUT' " +
							"AND fh.begin_date = (select max(fh1.begin_date) " +
							"from form_history fh1 " +
							"where fh1.form_id = fh.form_id " +
							"and fh1.begin_date <= nvl(?,sysdate)" +
							"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			ps.setDate(1, repDate);
			ps.setDate(2, repDate);
			ps.setDate(3, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				String[] codes = rs.getString("codes").split(",");
				Set<String> uniqueCodes = new HashSet<String>(Arrays.asList(codes));
				String joinedCodes = "";
				for (String code : uniqueCodes) {
					if (!joinedCodes.isEmpty()) {
						joinedCodes += ",";
					}
					joinedCodes += code;
				}
				form.getFormHistory().setPeriodCode(joinedCodes);
				result.add(form);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}


	/**
	 *	Список выходных форм с учетем типов периодов в разрезе выбранных типов отчетов
	 */
	/*@Override
	public List<Form> getOutFormsByUserSubjectTypeRecIds(long userId, Date reportDate, List<Long> stRecIds, String languageCode) {
		Connection connection = null;
		List<Form> result = new ArrayList<Form>();
		PreparedStatement ps = null;
		ResultSet rs = null;

		if (stRecIds == null)
			throw new NullPointerException();
		if (stRecIds.size() == 0)
			return result;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < stRecIds.size(); i++) {
			if (i > 0)
				sb.append(",");
			sb.append(stRecIds.get(i));
		}

		try {
			connection = getConnection();
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());

			ps = connection.prepareStatement("select f.id as form_id, fh.id as fh_id, f.code, f.type_code, fh.language_code, f.name, f.short_name, fh.begin_date, fh.end_date, fh.last_update_xml, fh.last_update_xls, fh.last_update_xls_out, fh.tag " +
					" from forms f,  " +
					"      form_history fh," +
					" 	   f_session_edit_forms sf, " +
					"	  (select stf.form_code from subjecttype_forms stf, rep_per_dur_months p " +
					"                     where stf.ref_subject_type_rec_id in (" + sb.toString() + ")" +
					"						and stf.period_id = p.id and pkg_frsi_util.check_period(p.code, ?) = 1 " +
					"                     group by stf.form_code" +
					"                     having count(distinct stf.ref_subject_type_rec_id) = ?) t " +
					" WHERE f.code=t.form_code and f.code=sf.form_name and sf.user_id = ? AND LOWER(fh.language_code) = lower(?) " +
					"            AND (f.type_code = 'OUTPUT' OR f.type_code = 'CONSOLIDATED') " +
					"AND f.id = fh.form_id " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");

			int paramIndex = 0;
			ps.setDate(++paramIndex, repDate);
			ps.setInt(++paramIndex, stRecIds.size());
			ps.setLong(++paramIndex, userId);
			ps.setString(++paramIndex, languageCode);
			ps.setDate(++paramIndex, repDate);
			ps.setDate(++paramIndex, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				result.add(form);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
			//throw new EJBException(e);
		} finally {
			try { if (rs!=null) rs.close(); } catch (Exception e) {}
			try { if (ps!=null) ps.close(); } catch (Exception e) {}
			try { if(connection!=null) connection.close(); } catch (Exception e) {}
		}
		return result;
	}*/

	@Override
	public List<Form> getOutFormsByUserSubjectTypeRecIds(long userId, Date reportDate, List<Long> stRecIds, String languageCode) {
		Connection connection = null;
		List<Form> result = new ArrayList<Form>();
		PreparedStatement ps = null;
		ResultSet rs = null;

		if (stRecIds == null)
			throw new NullPointerException();
		if (stRecIds.size() == 0)
			return result;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < stRecIds.size(); i++) {
			if (i > 0)
				sb.append(",");
			sb.append(stRecIds.get(i));
		}

		try {
			connection = getConnection();
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());

			ps = connection.prepareStatement(
					"SELECT DISTINCT \n" +
							"  t.CODES,\n" +
							"  f.id  AS form_id,\n" +
							"  fh.id AS fh_id,\n" +
							"  f.code,\n" +
							"  f.type_code,\n" +
							"  fh.language_code,\n" +
							"  fh.name,\n" +
							"  fh.short_name,\n" +
							"  fh.begin_date,\n" +
							"  fh.end_date,\n" +
							"  fh.last_update_xml,\n" +
							"  fh.last_update_xls,\n" +
							"  fh.last_update_xls_out,\n" +
							"  fh.tag,\n" +
							"  fh.period_count\n" +
							"FROM forms f,\n" +
							"  form_history fh,\n" +
							"  V_USER_FORMS sf,\n" +
							"  (SELECT\n" +
							"     stf.form_code,\n" +
							"     listagg(p.code, ',')\n" +
							"     WITHIN GROUP (\n" +
							"       ORDER BY p.code) codes\n" +
							"   FROM subjecttype_forms stf INNER JOIN rep_per_dur_months p ON stf.period_id = p.id\n" +
							"   WHERE stf.ref_subject_type_rec_id IN (" + sb.toString() + ")\n" +
							"         AND pkg_frsi_util.check_period(p.code, ?) = 1\n" +
							"   GROUP BY stf.form_code\n" +
							"   HAVING count(DISTINCT stf.ref_subject_type_rec_id) = ?) t\n" +
							"WHERE f.code = t.form_code AND f.code = sf.FORM_CODE AND sf.RIGHT_ITEM_NAME='F:EDIT' AND sf.user_id = ? AND LOWER(fh.language_code) = lower(?)\n" +
							"      AND (f.type_code = 'OUTPUT' OR f.type_code = 'CONSOLIDATED')\n" +
							"      AND f.id = fh.form_id\n" +
							"      AND fh.begin_date = (SELECT max(fh1.begin_date)\n" +
							"                           FROM form_history fh1\n" +
							"                           WHERE fh1.form_id = fh.form_id\n" +
							"                                 AND fh1.begin_date <= nvl(?, sysdate)\n" +
							"                                 AND (fh1.end_date IS NULL OR\n" +
							"                                      fh1.end_date > nvl(?, sysdate)))");

			ps.setDate(1, repDate);
			ps.setInt(2, stRecIds.size());
			ps.setLong(3, userId);
			ps.setString(4, languageCode);
			ps.setDate(5, repDate);
			ps.setDate(6, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				form.getFormHistory().setPeriodCount(rs.getInt("period_count"));
				String[] codes = rs.getString("codes").split(",");
				Set<String> uniqueCodes = new HashSet<String>(Arrays.asList(codes));
				String joinedCodes = "";
				for (String code : uniqueCodes) {
					if (!joinedCodes.isEmpty()) {
						joinedCodes += ",";
					}
					joinedCodes += code;
				}
				form.getFormHistory().setPeriodCode(joinedCodes);
				result.add(form);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public List<Form> getAllOutFormsByUserSubjectTypeRecIds(long userId, List<Long> stRecIds, List<RefNpaItem> npaList) {
		Connection connection = null;
		List<Form> result = new ArrayList<Form>();
		PreparedStatement ps = null;
		ResultSet rs = null;

		if (stRecIds == null)
			throw new NullPointerException();
		if (stRecIds.size() == 0)
			return result;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < stRecIds.size(); i++) {
			if (i > 0)
				sb.append(",");
			sb.append(stRecIds.get(i));
		}

		try {
			connection = getConnection();
			String sqlText =
					"SELECT DISTINCT f.id as form_id, " +
							"f.code, " +
							"f.type_code, " +
							"fh.name, " +
							"fh.short_name " +
					"FROM forms f, " +
							"v_report_history_short fh, " +
							"F_SESSION_FORMS sf, " +
							"npa_forms nf "+
					"WHERE f.id = fh.form_id "+
							"AND sf.form_name = f.code "+
							"AND fh.id = nf.form_history_id(+) "+
							"AND  sf.user_id = ?" +
							"AND (f.type_code = 'OUTPUT' OR f.type_code = 'CONSOLIDATED') " +
							"AND exists (select 'x' from subjecttype_forms stf " +
								" where stf.ref_subject_type_rec_id in (" + sb.toString() + ") and stf.form_code=f.code  " +
								" group by stf.form_code) ";

			if(npaList != null && npaList.size() > 0) {
				StringBuilder sbNpa = new StringBuilder();
				Boolean noNpa = false;
				for (int i = 0; i < npaList.size(); i++) {
					if(npaList.get(i).getId() == 0)
						noNpa = true;
					if (i > 0)
						sbNpa.append(",");
					sbNpa.append(npaList.get(i).getRecId());
				}
				if(noNpa) {
					sqlText = sqlText + " and (nf.npa_rec_id is null or nf.npa_rec_id in (" + sbNpa.toString() + "))";
				}else {
					sqlText = sqlText + " and nf.npa_rec_id in (" + sbNpa.toString() + ")";
				}
			}

			ps = connection.prepareStatement(sqlText);
			ps.setLong(1, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = new Form();
				form.setId(rs.getLong("form_id"));
				form.setCode(rs.getString("code"));
				form.setTypeCode(rs.getString("type_code"));
				FormHistory formHistory = new FormHistory();
				formHistory.setName(rs.getString("name"));
				formHistory.setShortName(rs.getString("short_name"));
				form.setFormHistory(formHistory);
				result.add(form);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public List<Form> getFormsNotInSubjForm(Long subjectTypeRecId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Form> result = new ArrayList<Form>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name " +
					"FROM forms f, " +
					"v_report_history_short fh " +
					"WHERE f.id = fh.form_id " +
					"AND not exists (select 'x' from subjecttype_forms stf where stf.form_code = f.code and stf.ref_subject_type_rec_id = ?)");

			ps.setLong(1, subjectTypeRecId);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = new Form();
				form.setId(rs.getLong("form_id"));
				form.setCode(rs.getString("code"));
				form.setTypeCode(rs.getString("type_code"));
				FormHistory formHistory = new FormHistory();
				formHistory.setName(rs.getString("name"));
				form.setFormHistory(formHistory);
				form.setTypeName(Form.resMap.get("ru" + "_" + form.getTypeCode()));
				result.add(form);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	/*@Override
	@Deprecated
	public List<Form> getReportsByReportDate(Date reportDate, String subject_type_code) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Form> result = new ArrayList<Form>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code " +
					"FROM forms f, " +
					"form_history fh " +
					"WHERE f.id = fh.form_id " +
					"AND f.type_code = 'OUTPUT' " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setDate(1, repDate);
			ps.setDate(2, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				result.add(form);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try { rs.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
			try { connection.close(); } catch (Exception e) {}
		}
		return result;
	}*/

	/*@Override
	@Deprecated
	public List<Form> getOutFormsByReportDate(Date reportDate) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Form> result = new ArrayList<Form>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code " +
					"FROM forms f, " +
					"form_history fh " +
					"WHERE f.id = fh.form_id " +
					"AND f.type_code = 'CONSOLIDATED' " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setDate(1, repDate);
			ps.setDate(2, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				result.add(form);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try { rs.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
			try { connection.close(); } catch (Exception e) {}
		}
		return result;
	}*/

	/*@Override
	@Deprecated
	public List<Form> getInputFormsByReportDate(Date reportDate) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Form> result = new ArrayList<Form>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code " +
					"FROM forms f, " +
					"form_history fh " +
					"WHERE f.id = fh.form_id " +
					"AND f.type_code = 'INPUT' " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setDate(1, repDate);
			ps.setDate(2, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				result.add(form);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try { rs.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
			try { connection.close(); } catch (Exception e) {}
		}
		return result;
	}*/

	/*@Override
    public List<Form> getFormsBySubjectType(Date reportDate, long resp) {
        Connection connection = null;
        CallableStatement stmt = null;
        String subject_type_code = null;
        List<Form> result = new ArrayList<Form>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = getConnection();
            stmt = connection.prepareCall("BEGIN ? := pkg_frsi_util.get_subjecttype_byresp(?); end;");
            stmt.registerOutParameter(1, OracleTypes.NVARCHAR); //REF CURSOR
            java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
            stmt.setLong(2, resp);
			//stmt.setDate(2, repDate);
			stmt.execute();
			subject_type_code = (String) stmt.getObject(1);

			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"f.name," +
					"f.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code " +
					"FROM forms f, " +
					"form_history fh," +
					"subjecttype_forms stf " +
					"WHERE f.id = fh.form_id " +
					"AND f.type_code = 'INPUT' " +
					"AND f.code = stf.form_name " +
					"AND stf.code = ? " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			ps.setString(1, subject_type_code);
			ps.setDate(2, repDate);
			ps.setDate(3, repDate);
            rs = ps.executeQuery();
            while (rs.next()) {
                Form form = getFormFromResultSet(rs, true, false, false, false);
                result.add(form);
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            try { rs.close(); } catch (Exception e) {}
            try { ps.close(); } catch (Exception e) {}
            try { stmt.close(); } catch (Exception e) {}
            try { connection.close(); } catch (Exception e) {}
        }
        return result;
    }*/

	@Override
	public List<FormListItem> getFormListItemsNoLobNoDate() {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<FormListItem> result = new ArrayList<FormListItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name " +
					"FROM forms f," +
					"v_report_history_short fh " +
					"WHERE f.id = fh.form_id " +
					"ORDER BY fh.name");
			rs = ps.executeQuery();
			while (rs.next()) {
				FormListItem formListItem = getFormListItemFromResultSet(rs, false);
				result.add(formListItem);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	// Forms of user
	/*@Override
	@Deprecated
	public List<Form> getReportsByUserReportDate(long userId, Date reportDate) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Form> result = new ArrayList<Form>();
		try {
			connection = getConnection();
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code " +
					"FROM forms f, " +
					"form_history fh," +
					"f_session_forms sf " +
					"WHERE f.id = fh.form_id " +
					"AND f.code = sf.form_name " +
					"AND sf.user_id = ? " +
					"AND f.type_code = 'OUTPUT' " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			ps.setLong(1, userId);
			ps.setDate(2, repDate);
			ps.setDate(3, repDate);
			//ps.setString(3, subject_type_code);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				result.add(form);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try { rs.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
			try { connection.close(); } catch (Exception e) {}
		}
		return result;
	}*/

	/*@Override
	@Deprecated
	public List<Form> getOutFormsByUserReportDate(long userId, Date reportDate) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Form> result = new ArrayList<Form>();
		try {
			connection = getConnection();
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code " +
					"FROM forms f, " +
					"form_history fh," +
					"f_session_forms sf " +
					"WHERE f.id = fh.form_id " +
					"AND f.code = sf.form_name " +
					"AND sf.user_id = ? " +
					"AND f.type_code = 'CONSOLIDATED' " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate) " +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			ps.setLong(1, userId);
			ps.setDate(2, repDate);
			ps.setDate(3, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				result.add(form);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try { rs.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
			try { connection.close(); } catch (Exception e) {}
		}
		return result;
	}*/

	/*@Override
	@Deprecated
	public List<Form> getFormsNoLobByUser(long userId, Date date) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Form> result = new ArrayList<Form>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code " +
					"FROM forms f, " +
					"form_history fh," +
					"f_session_forms sf " +
					"WHERE f.id = fh.form_id" +
					"AND f.code = sf.form_name " +
					"AND sf.user_id = ? " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");

			ps.setLong(1, userId);
			ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
			ps.setTimestamp(3, date == null ? null : new java.sql.Timestamp(date.getTime()));
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = getFormFromResultSet(rs, true, false, false, false);
				result.add(form);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try { rs.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
			try { connection.close(); } catch (Exception e) {}
		}
		return result;
	}*/

	@Override
	public List<FormListItem> getFormListItemsNoLob(Date dateBegin, Date dateEnd, List<String> formCodes, String formCode, String languageCode) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<FormListItem> result = new ArrayList<FormListItem>();
		if (formCodes == null || formCodes.size() == 0) return result;
		try {
			connection = getConnection();
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			String[] arr = formCodes.toArray(new String[formCodes.size()]);
			java.sql.Array array = oraConn.createARRAY("FORM_CODE_ARRAY", arr);
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code," +
					"fh.xml_version," +
					"fh.xls_version, " +
					"fh.is_fill_list " +
					"FROM forms f, " +
					"form_history fh," +
					"table(?) t " +
					"WHERE f.id = fh.form_id " +
					"AND t.column_value = f.code " +
					"AND (? is null or fh.begin_date <= ?) " +
					"AND (? is null or (fh.end_date > ? or fh.end_date is null)) " +
					"AND (trim(?) is null or upper(f.code) like upper(trim(?)))" +
					"AND (trim(?) is null or upper(fh.language_code) like upper(trim(?)))" +
					"order by fh.name ");
			ps.setArray(1, array);
			ps.setTimestamp(2, dateEnd == null ? null : new java.sql.Timestamp(dateEnd.getTime()));
			ps.setTimestamp(3, dateEnd == null ? null : new java.sql.Timestamp(dateEnd.getTime()));
			ps.setTimestamp(4, dateBegin == null ? null : new java.sql.Timestamp(dateBegin.getTime()));
			ps.setTimestamp(5, dateBegin == null ? null : new java.sql.Timestamp(dateBegin.getTime()));
			ps.setString(6, formCode);
			ps.setString(7, formCode);
			ps.setString(8, languageCode);
			ps.setString(9, languageCode);
			rs = ps.executeQuery();
			while (rs.next()) {
				FormListItem formListItem = getFormListItemFromResultSet(rs, true);
				formListItem.setXmlVersion(rs.getInt("xml_version"));
				formListItem.setXlsVersion(rs.getInt("xls_version"));
				formListItem.setTypeCode(rs.getString("TYPE_CODE"));
				result.add(formListItem);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public List<SubjectType_Form> getSubjTypeForms(Long subjectTypeRecId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<SubjectType_Form> result = new ArrayList<SubjectType_Form>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("" +
					"select sf.*, " +
					"fh.name, " +
					"p.name as period_name, " +
					"p.code as period_code, " +
					"f.type_code as form_type_code " +
					" from subjecttype_forms sf, " +
					"forms f," +
					"v_report_history_short fh, " +
					"rep_per_dur_months p " +
					" where sf.ref_subject_type_rec_id = ? " +
					"and f.code = sf.form_code " +
					"and sf.period_id = p.id " +
					"and f.id = fh.form_id");
			ps.setLong(1, subjectTypeRecId);
			rs = ps.executeQuery();
			while (rs.next()) {
				SubjectType_Form subjectType_Form = new SubjectType_Form();
				subjectType_Form.setId(rs.getLong("ID"));
				subjectType_Form.setSubjectTypeRecId(rs.getLong("REF_SUBJECT_TYPE_REC_ID"));
				subjectType_Form.setFormCode(rs.getString("FORM_CODE"));
				subjectType_Form.setFormName(rs.getString("NAME"));
				subjectType_Form.setPeriodId(rs.getLong("PERIOD_ID"));
				subjectType_Form.setPeriodName(rs.getString("PERIOD_NAME"));
				subjectType_Form.setPeriodCode(rs.getString("PERIOD_CODE"));
				subjectType_Form.setFormTypeCode(rs.getString("FORM_TYPE_CODE"));
				subjectType_Form.setFormTypeName(Form.resMap.get("ru" + "_" + subjectType_Form.getFormTypeCode()));
				subjectType_Form.setRefPeriodRecId(rs.getLong("REF_PERIOD_REC_ID"));
				if (rs.wasNull())
					subjectType_Form.setRefPeriodRecId(null);
				result.add(subjectType_Form);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public void addSubjTypeForms(Long subjectTypeRecId, String formCode, AuditEvent auditEvent) {
		Connection connection = null;
		PreparedStatement ps = null;
		Form result = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("INSERT INTO subjecttype_forms" +
					" (id, ref_subject_type_rec_id, form_code) VALUES (SEQ_SUBJECTTYPE_FORMS.nextval,?, ?)");
			ps.setLong(1, subjectTypeRecId);
			ps.setString(2, formCode);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");

			insertAuditEvent(auditEvent, connection);
			connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void updateSubjTypeForm(SubjectType_Form subjectType_form, AuditEvent auditEvent) {
		Connection connection = null;
		PreparedStatement ps = null;
		Date approvalDate = new Date();
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("UPDATE subjecttype_forms SET period_id = ?, REF_PERIOD_REC_ID = ? WHERE id = ?");
			ps.setLong(1, subjectType_form.getPeriodId());
			if (subjectType_form.getRefPeriodRecId() == null)
				ps.setNull(2, OracleTypes.NULL);
			else
				ps.setLong(2, subjectType_form.getRefPeriodRecId());
			ps.setLong(3, subjectType_form.getId());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");

			insertAuditEvent(auditEvent, connection);
			connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void delSubjTypeForms(Long id, AuditEvent auditEvent) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("DELETE FROM subjecttype_forms WHERE id = ?");
			ps.setLong(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Deleting an item failed, no rows affected.");

			insertAuditEvent(auditEvent, connection);
			connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public Long getFormId(String code, Date date) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Long result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("" +
					"SELECT f.id as form_id " +
					"FROM forms f, " +
					"form_history fh " +
					"WHERE f.id = fh.form_id " +
					"AND f.code = ? " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			ps.setString(1, code);
			java.sql.Date sqlReportDate = new java.sql.Date(date.getTime());
			ps.setDate(2, sqlReportDate);
			ps.setDate(3, sqlReportDate);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getLong("form_id");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public Long getFormIdByCode(String code) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Long result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT f.id as form_id " +
					"FROM forms f " +
					"WHERE f.code = ? ");
			ps.setString(1, code);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getLong("form_id");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public Long getCountHistoryIdByFormId(Long id) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Long result = (long)0;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT count(fh.id) as cnt_fh_id " +
					"FROM form_history fh " +
					"WHERE fh.form_id = ? ");
			ps.setLong(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getLong("cnt_fh_id");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public Form getForm(Long id, Date date) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Form result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.xml," +
					"fh.html," +
					"fh.html_view," +
					"fh.xls," +
					"fh.xls_out," +
					"fh.input_value_checks," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code " +
					"FROM forms f, " +
					"form_history fh " +
					"WHERE f.id = fh.form_id " +
					"AND f.id = ? " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");

			ps.setLong(1, id);
			ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
			ps.setTimestamp(3, date == null ? null : new java.sql.Timestamp(date.getTime()));
			rs = ps.executeQuery();
			if (rs.next()) {
				result = getFormFromResultSet(rs, true, true, false, false);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public Form getFormNoLob(Long id, Date date) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Form result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code " +
					"FROM forms f, " +
					"form_history fh " +
					"WHERE f.id = fh.form_id " +
					"AND f.id = ? " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");

			ps.setLong(1, id);
			ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
			ps.setTimestamp(3, date == null ? null : new java.sql.Timestamp(date.getTime()));
			rs = ps.executeQuery();
			if (rs.next()) {
				result = getFormFromResultSet(rs, true, false, false, false);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public Form getFormNoHistoryByCode(String code) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Form result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code " +
					"FROM forms f " +
					"WHERE f.code = ?");

			ps.setString(1, code);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = getFormFromResultSet(rs, false, false, false, false);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public Form getFormWithXls(Long id, Date date) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Form result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.xls," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code " +
					"FROM forms f, " +
					"form_history fh " +
					"WHERE f.id = fh.form_id " +
					"AND f.id = ? " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			ps.setLong(1, id);
			ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
			ps.setTimestamp(3, date == null ? null : new java.sql.Timestamp(date.getTime()));
			rs = ps.executeQuery();
			if (rs.next()) {
				result = getFormFromResultSet(rs,true, false, true, false);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public Form getFormWithXlsOut(Long id, Date date) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Form result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT f.id as form_id," +
					"f.code," +
					"f.type_code," +
					"fh.name," +
					"fh.short_name," +
					"fh.id as fh_id," +
					"fh.begin_date," +
					"fh.end_date," +
					"fh.xls_out," +
					"fh.last_update_xml," +
					"fh.last_update_xls," +
					"fh.last_update_xls_out," +
					"fh.tag," +
					"fh.language_code " +
					"FROM forms f, " +
					"form_history fh " +
					"WHERE f.id = fh.form_id " +
					"AND f.id = ? " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			ps.setLong(1, id);
			ps.setTimestamp(2, date == null ? null : new java.sql.Timestamp(date.getTime()));
			ps.setTimestamp(3, date == null ? null : new java.sql.Timestamp(date.getTime()));
			rs = ps.executeQuery();
			if (rs.next()) {
				result = getFormFromResultSet(rs,true, false, false, true);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public Form getFormWithActualXls(Long id, Date reportDate, boolean updateRef) {
		Form result = getFormWithXls(id, reportDate);
		if (result != null && result.getFormHistory().getXls() != null && result.getFormHistory().getXls().length > 0) {
			ExcelUtil excelUtil = new ExcelUtil(reference);
			excelUtil.createExcelDocumentFromBytes(result.getFormHistory().getXls());
			if (updateRef)
				excelUtil.updateReferences(reportDate);
			byte[] bytes = excelUtil.getExcelDocumentBytes();
			result.getFormHistory().setXls(bytes);
		}
		return result;
	}

	@Override
	public Form newForm(String xml, String languageCode) {
		WebFormProcessor formProcessor = new WebFormProcessor(xml, languageCode, false);
		WebFormProcessor formProcessorForView = new WebFormProcessor(xml, languageCode, true);
		Form form = new Form();
		FormHistory formHistory = new FormHistory();
		formHistory.setIsValid(formProcessor.isValidationOk());
		formHistory.setValidationMessage(formProcessor.getValidationMessage());
		formHistory.setErrorMessage(formProcessor.getErrorMessage());
		if (formHistory.isValid()) {
			form.setCode(formProcessor.getFormName());
			form.setTypeCode(formProcessor.getFormTypeCode());
			formHistory.setName(formProcessor.getFormTitle());
			formHistory.setShortName(formProcessor.getFormShortName());
			formHistory.setLanguageCode(formProcessor.getFormLanguageCode().toLowerCase());
			formHistory.setBeginDate(formProcessor.getFormBeginDate());
			formHistory.setEndDate(formProcessor.getFormEndDate());
			formHistory.setLastUpdateXml(new Date());
			formHistory.setLastUpdateXls(null);
			formHistory.setTag(formProcessor.getFormTag());
			formHistory.setXml(formProcessor.getFormXml());
			formHistory.setHtml(formProcessor.getHtml());
			formHistory.setHtmlView(formProcessorForView.getHtml());
			formHistory.setInputValueChecks(gson.toJson(formProcessor.getInputValueChecks()));
			formHistory.setJsCode(formProcessor.getJsCode());
			formHistory.setPeriodCount(formProcessor.getFormPeriodCount());
			formHistory.setXmlVersion(formProcessor.getFormXmlVersion());
		}
		form.setFormHistory(formHistory);
		return form;
	}

	@Override
	public Long insertForm(Form form, AuditEvent auditEvent) {
		Connection connection = null;
		PreparedStatement ps = null;
		Long result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("INSERT INTO forms" +
					" (id, code, type_code) VALUES (SEQ_FORMS_ID.nextval, ?, ?)", new String[] {"id"});
			ps.setString(1, form.getCode());
			ps.setString(2, form.getTypeCode());

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
			ResultSet generatedKeys = ps.getGeneratedKeys();
			if (generatedKeys.next()) result = generatedKeys.getLong(1);

			form.getFormHistory().setFormId(result);
			insertFormHistory(form.getFormHistory(), form.getTypeCode().equals(Form.Type.INPUT.name()), auditEvent, connection);

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
		return result;
	}

	@Override
	public void updateForm(Form form, AuditEvent auditEvent) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("UPDATE forms" +
					" SET code = ?, type_code = ? WHERE id = ?");
			ps.setString(1, form.getCode());
			ps.setString(2, form.getTypeCode());
			ps.setLong(3, form.getId());

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");

			updateFormHistory(form.getFormHistory(), connection);

			if(auditEvent != null) {
				insertAuditEvent(auditEvent, connection);
			}
			connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void updateXmlForm(String xml, Long id){
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("UPDATE form_history SET xml = ? WHERE id = ? ");

			Clob clobXml = connection.createClob();
			clobXml.setString(1, xml);
			ps.setClob(1, clobXml);
			ps.setLong(2, id);

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void deleteForm(Long id, AuditEvent auditEvent) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("DELETE FROM forms WHERE id = ?");
			ps.setLong(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Deleting an item failed, no rows affected.");

			insertAuditEvent(auditEvent, connection);
			connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void deleteFormHistory(Long id, AuditEvent auditEvent) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("DELETE FROM form_history WHERE id = ?");
			ps.setLong(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Deleting an item failed, no rows affected.");

			insertAuditEvent(auditEvent, connection);
			connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void deleteFormHistoryByCode(String formCode, Connection connection){
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement(
					"delete(\n" +
					"select fh.*\n" +
					"  from form_history fh,\n" +
					"       forms f\n" +
					" where fh.form_id = f.id\n" +
					"   and f.code = ?)");
			ps.setString(1, formCode);
			ps.execute();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public Long insertFormHistory(FormHistory formHistory, boolean isFillList, AuditEvent auditEvent, Connection connection) {
		PreparedStatement ps = null;
		boolean localCon = false;
		Long result = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}

			ps = connection.prepareStatement("INSERT INTO form_history" +
					" (id, form_id, language_code, begin_date, end_date, tag, xml, html, html_view, input_value_checks, js_code, last_update_xml, period_count, xml_version, name, short_name, is_fill_list) VALUES (SEQ_FORM_HISTORY_ID.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{"id"});
			ps.setLong(1, formHistory.getFormId());
			ps.setString(2, formHistory.getLanguageCode().toLowerCase());
			ps.setDate(3, formHistory.getBeginDate() == null ? null : new java.sql.Date(formHistory.getBeginDate().getTime()));
			ps.setDate(4, formHistory.getEndDate() == null ? null : new java.sql.Date(formHistory.getEndDate().getTime()));
			ps.setString(5, formHistory.getTag());

			Clob clobXml = connection.createClob();
			clobXml.setString(1, formHistory.getXml());
			ps.setClob(6, clobXml);
			Clob clobHtml = connection.createClob();
			clobHtml.setString(1, formHistory.getHtml());
			ps.setClob(7, clobHtml);
			Clob clobHtmlView = connection.createClob();
			clobHtmlView.setString(1, formHistory.getHtmlView());
			ps.setClob(8, clobHtmlView);
			Clob clobInputValueChecks = connection.createClob();
			clobInputValueChecks.setString(1, formHistory.getInputValueChecks());
			ps.setClob(9, clobInputValueChecks);
			Clob clobJsCode = connection.createClob();
			clobJsCode.setString(1, formHistory.getJsCode());
			ps.setClob(10, clobJsCode);

			ps.setTimestamp(11, formHistory.getLastUpdateXml() == null ? null : new java.sql.Timestamp(formHistory.getLastUpdateXml().getTime()));
			ps.setInt(12, formHistory.getPeriodCount());
			ps.setInt(13, formHistory.getXmlVersion());
			ps.setString(14, formHistory.getName());
			ps.setString(15, formHistory.getShortName());
			ps.setInt(16, isFillList ? 1 : 0);

			int affectedRowsH = ps.executeUpdate();
			if (affectedRowsH == 0) throw new SQLException("Inserting an item failed, no rows affected.");
			ResultSet generatedKeys = ps.getGeneratedKeys();
			if (generatedKeys.next()) result = generatedKeys.getLong(1);

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
		return result;
	}

	private void updateFormHistory(FormHistory formHistory, Connection connection) {
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("UPDATE form_history" +
					" SET language_code = ?, begin_date = ?, end_date = ?, tag = ?, html = ?, html_view = ?, input_value_checks = ?, js_code = ?, last_update_xml = ?, period_count = ?, xml_version = ?, name = ?, short_name = ? WHERE id = ?");
			ps.setString(1, formHistory.getLanguageCode().toLowerCase());
			ps.setDate(2, new java.sql.Date(formHistory.getBeginDate().getTime()));
			ps.setDate(3, formHistory.getEndDate() == null ? null : new java.sql.Date(formHistory.getEndDate().getTime()));
			ps.setString(4, formHistory.getTag());

			/*Clob clobXml = connection.createClob();
			clobXml.setString(1, formHistory.getXml());
			ps.setClob(5, clobXml);*/
			Clob clobHtml = connection.createClob();
			clobHtml.setString(1, formHistory.getHtml());
			ps.setClob(5, clobHtml);
			Clob clobHtmlView = connection.createClob();
			clobHtmlView.setString(1, formHistory.getHtmlView());
			ps.setClob(6, clobHtmlView);
			Clob clobInputValueChecks = connection.createClob();
			clobInputValueChecks.setString(1, formHistory.getInputValueChecks());
			ps.setClob(7, clobInputValueChecks);
			Clob clobJsCode = connection.createClob();
			clobJsCode.setString(1, formHistory.getJsCode());
			ps.setClob(8, clobJsCode);

			ps.setTimestamp(9, formHistory.getLastUpdateXml() == null ? null : new java.sql.Timestamp(formHistory.getLastUpdateXml().getTime()));
			ps.setLong(10, formHistory.getPeriodCount());
			ps.setInt(11, formHistory.getXmlVersion());
			ps.setString(12, formHistory.getName());
			ps.setString(13, formHistory.getShortName());
			ps.setLong(14, formHistory.getId());
			int affectedRowsH = ps.executeUpdate();
			if (affectedRowsH == 0) throw new SQLException("Updating an item failed, no rows affected.");

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void updateFormHistoryWithXls(FormListItem formListItem, AuditEvent auditEvent) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("UPDATE form_history SET last_update_xls = ?, xls = ?, xls_version = ? WHERE id = ?");
			ps.setTimestamp(1, formListItem.getLastUpdateXls() == null ? null : new java.sql.Timestamp(formListItem.getLastUpdateXls().getTime()));

			Blob blobXls = connection.createBlob();
			blobXls.setBytes(1, formListItem.getXls());
			ps.setBlob(2, blobXls);
			ps.setInt(3, formListItem.getXlsVersion());

			ps.setLong(4, formListItem.getFhId());
			ps.execute();

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);
			connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void updateFormHistoryWithXlsOut(FormListItem formListItem, AuditEvent auditEvent) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("UPDATE form_history SET last_update_xls_out = ?, xls_out = ? WHERE id = ?");

			ps.setTimestamp(1, formListItem.getLastUpdateXlsOut() == null ? null : new java.sql.Timestamp(formListItem.getLastUpdateXlsOut().getTime()));
			Blob blobXls = connection.createBlob();
			blobXls.setBytes(1, formListItem.getXlsOut());
			ps.setBlob(2, blobXls);
			ps.setLong(3, formListItem.getFhId());
			ps.execute();

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void updateFormHistoryIsFillList(long formHisoryId, boolean isFillList) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("UPDATE form_history SET is_fill_list = ? WHERE id = ?");
			ps.setInt(1, isFillList ? 1 : 0);
			ps.setLong(2, formHisoryId);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public FormHistory getFormHistoryWithInputValueChecks(Long id) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		FormHistory result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT input_value_checks FROM form_history WHERE id = ?");
			ps.setLong(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = new FormHistory();
				Clob clobInputValueChecks = rs.getClob("input_value_checks");
				if (clobInputValueChecks != null) {
					result.setInputValueChecks(clobInputValueChecks.getSubString(1, (int) clobInputValueChecks.length()));
					clobInputValueChecks.free();
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public Long getFormHistoryId(String formCode, Date beginDate, Connection connection){
		boolean localCon = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Long result = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement(
					"select fh.id \n" +
					"  from FORM_HISTORY fh,\n" +
					"       forms f\n" +
					" where fh.form_id = f.id\n" +
					"   and upper(f.code) = upper(?)\n" +
					"   and fh.begin_date = ?");
			ps.setString(1, formCode);
			ps.setDate(2, new java.sql.Date(beginDate.getTime()));
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getLong("id");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}


	@Override
	public FormHistory getFormHistoryWithJsCode(Long id) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		FormHistory result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT js_code FROM form_history WHERE id = ?");
			ps.setLong(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = new FormHistory();
				Clob clobJsCode = rs.getClob("js_code");
				if (clobJsCode != null) {
					result.setJsCode(clobJsCode.getSubString(1, (int) clobJsCode.length()));
					clobJsCode.free();
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public String getFormNameByFormCodeLanguageCodeReportDate(String formCode, String languageCode, Date reportDate) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String result = "";
		try {
			connection = getConnection();
			ps = connection.prepareStatement("" +
					"SELECT nvl(fh.short_name,fh.name) as name " +
					"FROM forms f, " +
					"form_history fh " +
					"WHERE f.id = fh.form_id " +
					"AND f.code = ? " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			ps.setString(1, formCode);
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setDate(2, repDate);
			ps.setDate(3, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				result = rs.getString("name");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public List<Period> getPeriods() {
		Connection connection = null;
		List<Period> result = new ArrayList<Period>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();

			ps = connection.prepareStatement("select t.* from rep_per_dur_months t\n" +
					"order by id");
			rs = ps.executeQuery();
			while (rs.next()) {
				Period period = new Period();
				period.setId(rs.getLong("ID"));
				period.setCode(rs.getString("CODE"));
				period.setName(rs.getString("NAME"));
				result.add(period);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
    public Period getPeriod(Long id){
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Period result = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement("SELECT * FROM rep_per_dur_months WHERE id = ?");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                result = new Period();
                result.setId(rs.getLong("ID"));
                result.setName(rs.getString("NAME"));
                result.setCode(rs.getString("CODE"));
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, ps, rs);
        }
        return result;
    }

	@Override
	public List<Form> getNpaForms(Long npaRecId, Boolean exist) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Form> result = new ArrayList<Form>();
		try {
			connection = getConnection();
			String sqlText =
					"select fh.id,\n" +
					"       fh.begin_date,\n" +
					"       fh.end_date,\n" +
					"       fh.name,\n" +
					"       f.type_code,\n" +
					"       f.code\n" +
					"  from form_history fh," +
					"		forms f" +
					" where fh.form_id = f.id and ";
			if(!exist) {
				sqlText = sqlText + " not exists (select nf.*" +
												"   from npa_forms nf " +
												"  where nf.form_history_id = fh.id) ";
			}else {
				sqlText = sqlText +
						" exists (select nf.* \n" +
						"           from npa_forms nf \n" +
						"          where nf.form_history_id = fh.id \n" +
						"            and nf.npa_rec_id = " + npaRecId + ")";
			}
			ps = connection.prepareStatement(sqlText);
			rs = ps.executeQuery();
			while (rs.next()) {
				Form form = new Form();
				form.setTypeName(Form.resMap.get("ru" + "_" + rs.getString("TYPE_CODE")));
				form.setCode(rs.getString("CODE"));
				FormHistory formHistory = new FormHistory();
				formHistory.setId(rs.getLong("id"));
				formHistory.setBeginDate(rs.getDate("BEGIN_DATE"));
				formHistory.setEndDate(rs.getDate("END_DATE"));
				formHistory.setName(rs.getString("NAME"));
				form.setFormHistory(formHistory);
				result.add(form);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public void addNpaForms(Long npaRecId, Long formHistoryId, AuditEvent auditEvent) {
		Connection connection = null;
		PreparedStatement ps = null;
		Form result = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("INSERT INTO npa_forms (npa_rec_id, form_history_id) VALUES (?, ?)");
			ps.setLong(1, npaRecId);
			ps.setLong(2, formHistoryId);
			ps.execute();

			insertAuditEvent(auditEvent, connection);
			connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void delNpaForms(Long npaRecId, Long formHistoryId, AuditEvent auditEvent) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("DELETE FROM npa_forms WHERE npa_rec_id = ? and form_history_id = ?");
			ps.setLong(1, npaRecId);
			ps.setLong(2, formHistoryId);
			ps.execute();
			insertAuditEvent(auditEvent, connection);
			connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	private Form getFormFromResultSet(ResultSet rs, boolean withFormHistory, boolean xml, boolean xls, boolean xlsOut) throws SQLException {
		Form form = new Form();
		form.setId(rs.getLong("form_id"));
		form.setCode(rs.getString("code"));
		form.setTypeCode(rs.getString("type_code"));


		if(withFormHistory) {
			FormHistory formHistory = new FormHistory();
			formHistory.setName(rs.getString("name"));
			formHistory.setShortName(rs.getString("short_name"));
			formHistory.setId(rs.getLong("fh_id"));
			formHistory.setLanguageCode(rs.getString("language_code"));
			formHistory.setBeginDate(rs.getDate("begin_date"));
			formHistory.setEndDate(rs.getDate("end_date"));
			formHistory.setLastUpdateXml(rs.getDate("last_update_xml"));
			formHistory.setLastUpdateXls(rs.getDate("last_update_xls"));
			formHistory.setLastUpdateXls(rs.getDate("last_update_xls_out"));
			String tag = rs.getString("tag");
			formHistory.setTag(tag);
			if (tag != null && !tag.trim().isEmpty())
				formHistory.setFormTag(gson.fromJson(tag, FormTag.class));

			if (xml) {
				Clob clobXml = rs.getClob("xml");
				if (clobXml != null) {
					formHistory.setXml(clobXml.getSubString(1, (int) clobXml.length()));
					clobXml.free();
				}
				Clob clobHtml = rs.getClob("html");
				if (clobHtml != null) {
					formHistory.setHtml(clobHtml.getSubString(1, (int) clobHtml.length()));
					clobHtml.free();
				}
				Clob clobHtmlView = rs.getClob("html_view");
				if (clobHtmlView != null) {
					formHistory.setHtmlView(clobHtmlView.getSubString(1, (int) clobHtmlView.length()));
					clobHtmlView.free();
				}
				Clob clobInputValueChecks = rs.getClob("input_value_checks");
				if (clobInputValueChecks != null) {
					formHistory.setInputValueChecks(clobInputValueChecks.getSubString(1, (int) clobInputValueChecks.length()));
					clobInputValueChecks.free();
				}
			}
			if (xls) {
				Blob blobXls = rs.getBlob("xls");
				if (blobXls != null) {
					formHistory.setXls(blobXls.getBytes(1, (int) blobXls.length()));
					blobXls.free();
				}
			}

			if (xlsOut) {
				Blob blobXlsOut = rs.getBlob("xls_out");
				if (blobXlsOut != null) {
					formHistory.setXlsOut(blobXlsOut.getBytes(1, (int) blobXlsOut.length()));
					blobXlsOut.free();
				}
			}
			form.setFormHistory(formHistory);
		}
		return form;
	}

	private FormListItem getFormListItemFromResultSet(ResultSet rs, boolean withFormHistory) throws SQLException {
		FormListItem formListItem = new FormListItem();
		formListItem.setFormId(rs.getLong("form_id"));
		formListItem.setCode(rs.getString("code"));
		formListItem.setName(rs.getString("name"));
		if (withFormHistory) {
			formListItem.setFhId(rs.getLong("fh_id"));
			formListItem.setLanguageCode(rs.getString("language_code"));
			formListItem.setBeginDate(rs.getDate("begin_date"));
			formListItem.setEndDate(rs.getDate("end_date"));
			formListItem.setLastUpdateXml(rs.getDate("last_update_xml"));
			formListItem.setLastUpdateXls(rs.getDate("last_update_xls"));
			formListItem.setLastUpdateXlsOut(rs.getDate("last_update_xls_out"));
			formListItem.setFillList(rs.getInt("is_fill_list") == 1? true : false);
			String tag = rs.getString("tag");
			formListItem.setTag(tag);
		}
		return formListItem;
	}

	/*private Form getFormByAttributes(Connection connection, Form form) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Form result = null;
		try {
			ps = connection.prepareStatement("SELECT * FROM forms WHERE code = ? AND type_code = ? AND language_code = ? AND name = ? AND short_name = ? AND begin_date = ? AND end_date = ? AND tag = ? AND is_active = ?");
			ps.setString(1, form.getCode());
			ps.setString(2, form.getTypeCode());
			ps.setString(3, form.getLanguageCode());
			ps.setString(4, form.getName());
			ps.setString(5, form.getShortName());
			ps.setDate(6, new java.sql.Date(form.getBeginDate().getTime()));
			ps.setDate(7, form.getEndDate() == null ? null : new java.sql.Date(form.getEndDate().getTime()));
			ps.setString(8, form.getTag());
			ps.setLong(9, form.getIsActive());
			rs = ps.executeQuery();
			if (rs.next()) {
				result = getFormFromResultSet(rs, true, false, false);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try { rs.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
		}
		return result;
	}*/

	// Reports

	/*@Override
	public List<ReportListItem> getReportListByIdn(String idn, String languageCode) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ReportListItem> result = new ArrayList<ReportListItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT r.*, st.status_code, st.status_date " +
					" FROM reports r " +
					"	inner join report_status_history st on st.report_id=r.id and st.id=(select max(st2.id) from report_status_history st2 where st2.report_id=r.id)" +
					" WHERE idn = '" + idn + "'");
			//ps.setString(1, idn); // Doesn't work because JDBC doesn't wrap a value in quotes if a string value contains only numeric characters.
			rs = ps.executeQuery();
			while (rs.next()) {
				Report report = getReportFromResultSet(rs);

				List<ReportHistory> reportHistory = getReportHistoryByReportNoLobs(report);
				report.setReportHistory(reportHistory);

				ReportListItem item = new ReportListItem();
				item.setId(report.getId());
				item.setReportDate(report.getReportDate());
				item.setFormCode(report.getFormCode());
				List<Form> forms = getFormsByCodeLanguageReportDate(report.getFormCode(), languageCode, report.getReportDate());
				if (!forms.isEmpty()){
					item.setFormName(forms.get(0).getFormHistory().getName());
					item.setCanAttachedFile(forms.get(0).getFormHistory().getFormTag() == null ? false : forms.get(0).getFormHistory().getFormTag().canAttachedFile);
				}
				ReportHistory lastReport = reportHistory.isEmpty() ? null : reportHistory.get(reportHistory.size()-1);
				if (lastReport != null) {
					item.setSaveDate(lastReport.getSaveDate());
					item.setUserInfo(lastReport.getUserInfo());
					item.setDeliveryWay(lastReport.getDeliveryWayCode());
				}
				item.setStatus(rs.getString("STATUS_CODE"));
				item.setStatusDate(rs.getDate("STATUS_DATE"));
				result.add(item);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try { rs.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
			try { connection.close(); } catch (Exception e) {}
		}
		return result;
	}*/

	@Override
	public List<ReportListItem> getReportListByIdnReportDateFormCode(String idn, boolean child, Date reportDate, String formCode, String languageCode) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ReportListItem> result = new ArrayList<ReportListItem>();
		try {
			connection = getConnection();

			String idnClause = child ? "idn_child = '" + idn + "'" : "idn = '" + idn + "' AND idn_child is null";
			String sqlText =
					"SELECT r.*, " +
					"		st.status_code, " +
					"		st.status_date, " +
					"		(select count(rf.id) from attached_file rf where rf.link_id = h.id and rf.file_kind = 1 ) as have_file,  " +
					"		(select count(rf.id) from attached_file rf where rf.link_id = h.id and rf.file_kind = 2 ) as have_letter  " +
					" FROM reports r " +
					"   INNER JOIN REPORT_HISTORY h ON h.ID=(SELECT max(h2.ID) FROM REPORT_HISTORY h2 WHERE h2.REPORT_ID=r.ID)\n" +
					"   INNER JOIN REPORT_HISTORY_STATUSES st on st.ID=(SELECT max(st2.ID) FROM REPORT_HISTORY_STATUSES st2 WHERE st2.REPORT_HISTORY_ID=h.ID)" +
					" WHERE " + idnClause + " AND TRUNC(report_date) = TRUNC(?) AND form_code = ?";
			ps = connection.prepareStatement(sqlText);

			//ps.setString(1, idn); // Doesn't work because JDBC doesn't wrap a value in quotes if a string value contains only numeric characters.
			java.sql.Date sqlReportDate = new java.sql.Date(reportDate.getTime());
			ps.setDate(1, sqlReportDate);
			ps.setString(2, formCode);
			rs = ps.executeQuery();
			while (rs.next()) {
				Report report = getReportFromResultSet(rs);

				List<ReportHistory> reportHistory = getReportHistoryByReportNoLobs(report, connection);
				report.setReportHistory(reportHistory);

				ReportListItem item = new ReportListItem();
				item.setIdn(report.getIdn());
				item.setId(report.getId());
				item.setReportDate(report.getReportDate());
				item.setFormCode(report.getFormCode());
				List<Form> forms = getFormsByCodeLanguageReportDate(report.getFormCode(), languageCode, report.getReportDate(), connection);
				if (!forms.isEmpty()){
					item.setFormName(forms.get(0).getFormHistory().getName());
					item.setCanAttachedFile(forms.get(0).getFormHistory().getFormTag() == null ? false : forms.get(0).getFormHistory().getFormTag().canAttachedFile);
				}
				ReportHistory lastReport = reportHistory.isEmpty() ? null : reportHistory.get(reportHistory.size()-1);
				if (lastReport != null) {
					item.setSaveDate(lastReport.getSaveDate());
					item.setUserInfo(lastReport.getUserInfo());
					item.setDeliveryWay(lastReport.getDeliveryWayCode());
				}
				item.setStatus(rs.getString("STATUS_CODE"));
				item.setStatusDate(rs.getDate("STATUS_DATE"));
				item.setStatusName(ReportStatus.resMap.get("ru" + "_" + item.getStatus()));
				item.setHaveAttachedFile(rs.getInt("HAVE_FILE") > 0);
				item.setHaveAttachedLetter(rs.getInt("HAVE_LETTER") > 0);

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
	public List<ReportHistory> getReportHistoryByReportNoLobs(Report report, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		List<ReportHistory> result = new ArrayList<ReportHistory>();
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("SELECT id, report_id, save_date, data_size, comments, attachment_size, attachment_file_name, hash, delivery_way_code, user_id, user_info, su_user_id, su_user_info, su_comments, note, control_result_code, control_result_code2 FROM report_history WHERE report_id = ? ORDER BY id");
			ps.setLong(1, report.getId());
			rs = ps.executeQuery();
			while (rs.next()) {
				ReportHistory reportHistory = getReportHistoryFromResultSet(rs, false, false);
				reportHistory.setReport(report);
				result.add(reportHistory);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public List<ReportHistory> getReportHistoryByReportIdNoLobs(Long reportId, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		List<ReportHistory> result = new ArrayList<ReportHistory>();
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("SELECT id, report_id, save_date, data_size, comments, attachment_size, attachment_file_name, hash, delivery_way_code, user_id, user_info, su_user_id, su_user_info, su_comments, note, control_result_code, control_result_code2 FROM report_history WHERE report_id = ? ORDER BY id");
			ps.setLong(1, reportId);
			rs = ps.executeQuery();
			while (rs.next()) {
				ReportHistory reportHistory = getReportHistoryFromResultSet(rs, false, false);
				reportHistory.setReport(getReport(reportId, connection));
				result.add(reportHistory);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public List<ReportHistoryListItem> getReportHistoryListByReportId(long reportId, boolean forSuperUser) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ReportHistoryListItem> result = new ArrayList<ReportHistoryListItem>();
		try {
			connection = getConnection();
			String filter = "";
			if (forSuperUser) {
				String statuses = "";
				for (ReportStatus.Status status : ReportStatus.suUserStatuses) {
					if (!statuses.isEmpty()) {
						statuses += ",";
					}
					statuses += "'" + status.name() + "'";
				}
				filter = "\nAND s.STATUS_CODE IN ("+statuses+")\n";
			}
			ps = connection.prepareStatement("SELECT\n" +
					"  h.ID,\n" +
					"  h.REPORT_ID,\n" +
					"  h.SAVE_DATE,\n" +
					"  h.DELIVERY_WAY_CODE,\n" +
					"  h.USER_ID,\n" +
					"  h.USER_INFO,\n" +
					"  h.CONTROL_RESULT_CODE2,\n" +
					"  h.NOTE,\n" +
					"  h.SU_COMMENTS,\n" +
					"  s.STATUS_DATE,\n" +
					"  s.STATUS_CODE,\n" +
					"  (SELECT max(s1.STATUS_DATE)\n" +
					"   FROM REPORT_HISTORY_STATUSES s1\n" +
					"   WHERE s1.REPORT_HISTORY_ID = h.ID AND s1.STATUS_CODE = 'COMPLETED') COMPLETE_DATE,\n" +
					"  (select count(rf.id) from attached_file rf where rf.link_id = h.id and rf.file_kind = 1 ) as have_file,\n" +
					"  (select count(rf.id) from attached_file rf where rf.link_id = h.id and rf.file_kind = 2 ) as have_letter\n" +
					"FROM report_history h\n" +
					"  INNER JOIN REPORT_HISTORY_STATUSES s ON s.ID = (SELECT max(s2.ID)\n" +
					"                                                  FROM REPORT_HISTORY_STATUSES s2\n" +
					"                                                  WHERE h.ID = s2.REPORT_HISTORY_ID)\n" +
					"WHERE h.report_id = ?\n" + filter +
					"ORDER BY h.id");
			ps.setLong(1, reportId);
			rs = ps.executeQuery();
			while (rs.next()) {
				ReportHistoryListItem reportHistory = new ReportHistoryListItem();
				reportHistory.setId(rs.getLong("ID"));
				reportHistory.setReportId(rs.getLong("REPORT_ID"));
				reportHistory.setSaveDate(rs.getDate("SAVE_DATE"));
				reportHistory.setDeliveryWay(rs.getString("DELIVERY_WAY_CODE"));
				reportHistory.setUserId(rs.getLong("USER_ID"));
				reportHistory.setUserInfo(rs.getString("USER_INFO"));
				reportHistory.setStatus(rs.getString("STATUS_CODE"));
				reportHistory.setStatusDate(rs.getDate("STATUS_DATE"));
				ReportStatus reportStatus = new ReportStatus();
				reportStatus.setStatusCode(rs.getString("STATUS_CODE"));
				reportHistory.setStatusName(reportStatus.getStatusName("ru"));
				reportHistory.setCompleteDate(rs.getDate("COMPLETE_DATE"));
				reportHistory.setHaveAttachedFile(rs.getInt("HAVE_FILE") > 0);
				reportHistory.setHaveAttachedLetter(rs.getInt("HAVE_LETTER") > 0);
				reportHistory.setControlResultCode(rs.getString("CONTROL_RESULT_CODE2"));
				reportHistory.setControlResultName(ControlResultType.resMap.get("ru" + "_" + reportHistory.getControlResultCode()));
				reportHistory.setComment(rs.getString("SU_COMMENTS"));
				result.add(reportHistory);
			}
			if (result.size() > 0) {
				result.get(result.size() - 1).setCurrent(true);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public ReportHistoryListItem getReportHistoryListItemById(long reportHistoryId, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		ReportHistoryListItem result = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("SELECT\n" +
					"  h.ID,\n" +
					"  h.REPORT_ID,\n" +
					"  h.SAVE_DATE,\n" +
					"  h.DELIVERY_WAY_CODE,\n" +
					"  h.USER_ID,\n" +
					"  h.USER_INFO,\n" +
					"  h.CONTROL_RESULT_CODE2,\n" +
					"  h.SU_COMMENTS,\n" +
					"  h.NOTE,\n" +
					"  s.STATUS_DATE,\n" +
					"  s.STATUS_CODE,\n" +
					"  (SELECT max(s1.STATUS_DATE)\n" +
					"   FROM REPORT_HISTORY_STATUSES s1\n" +
					"   WHERE s1.REPORT_HISTORY_ID = h.ID AND s1.STATUS_CODE = 'COMPLETED') COMPLETE_DATE,\n" +
					"  (select count(rf.id) from attached_file rf where rf.link_id = h.id and rf.file_kind = 1 ) as have_file,\n" +
					"  (select count(rf.id) from attached_file rf where rf.link_id = h.id and rf.file_kind = 2 ) as have_letter\n" +
					"FROM report_history h\n" +
					"  INNER JOIN REPORT_HISTORY_STATUSES s ON s.ID = (SELECT max(s2.ID)\n" +
					"                                                  FROM REPORT_HISTORY_STATUSES s2\n" +
					"                                                  WHERE h.ID = s2.REPORT_HISTORY_ID)\n" +
					"WHERE h.id = ?\n" +
					"ORDER BY h.id");
			ps.setLong(1, reportHistoryId);
			rs = ps.executeQuery();
			if (rs.next()) {
				ReportHistoryListItem reportHistory = new ReportHistoryListItem();
				reportHistory.setId(rs.getLong("ID"));
				reportHistory.setReportId(rs.getLong("REPORT_ID"));
				reportHistory.setSaveDate(rs.getDate("SAVE_DATE"));
				reportHistory.setDeliveryWay(rs.getString("DELIVERY_WAY_CODE"));
				reportHistory.setUserId(rs.getLong("USER_ID"));
				reportHistory.setUserInfo(rs.getString("USER_INFO"));
				reportHistory.setStatus(rs.getString("STATUS_CODE"));
				reportHistory.setStatusDate(rs.getDate("STATUS_DATE"));
				ReportStatus reportStatus = new ReportStatus();
				reportStatus.setStatusCode(rs.getString("STATUS_CODE"));
				reportHistory.setStatusName(reportStatus.getStatusName("ru"));
				reportHistory.setCompleteDate(rs.getDate("COMPLETE_DATE"));
				reportHistory.setHaveAttachedFile(rs.getInt("HAVE_FILE") > 0);
				reportHistory.setHaveAttachedLetter(rs.getInt("HAVE_LETTER") > 0);
				reportHistory.setControlResultCode(rs.getString("CONTROL_RESULT_CODE2"));
				reportHistory.setControlResultName(ControlResultType.resMap.get("ru" + "_" + reportHistory.getControlResultCode()));
				reportHistory.setComment(rs.getString("SU_COMMENTS"));
				result = reportHistory;
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public ReportHistory getLastReportHistoryByReportId(Long reportId, boolean withData, boolean withAttachment, boolean forSuperUser, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		ReportHistory result = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			String filter = "";
			if (forSuperUser) {
				String statuses = "";
				for (ReportStatus.Status status : ReportStatus.suUserStatuses) {
					if (!statuses.isEmpty()) {
						statuses += ",";
					}
					statuses += "'" + status.name() + "'";
				}
				filter = "\nAND PKG_FRSI_UTIL.GET_LAST_STATUS_CODE(h.ID) IN ("+statuses+")\n";
			}
			ps = connection.prepareStatement("SELECT\n" +
					"  h.*\n" +
					"FROM report_history h\n" +
					"WHERE h.report_id = ? " + filter +
					"ORDER BY h.ID DESC\n" +
					"OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY");
			ps.setLong(1, reportId);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = getReportHistoryFromResultSet(rs, withData, withAttachment);
				result.setReport(getReport(reportId, connection));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public ReportHistory getLastReportHistoryByReportIdNoLobs(Long reportId, boolean forSuperUser, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		ReportHistory result = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			String filter = "";
			if (forSuperUser) {
				String statuses = "";
				for (ReportStatus.Status status : ReportStatus.suUserStatuses) {
					if (!statuses.isEmpty()) {
						statuses += ",";
					}
					statuses += "'" + status.name() + "'";
				}
				filter = "\nAND PKG_FRSI_UTIL.GET_LAST_STATUS_CODE(h.ID) IN (" + statuses + ")\n";
			}
			ps = connection.prepareStatement("SELECT\n" +
					"  h.ID,\n" +
					"  h.ATTACHMENT_FILE_NAME,\n" +
					"  h.ATTACHMENT_SIZE,\n" +
					"  h.COMMENTS,\n" +
					"  h.DATA_SIZE,\n" +
					"  h.DELIVERY_WAY_CODE,\n" +
					"  h.HASH,\n" +
					"  h.REPORT_ID,\n" +
					"  h.SAVE_DATE,\n" +
					"  h.SU_COMMENTS,\n" +
					"  h.SU_USER_ID,\n" +
					"  h.SU_USER_INFO,\n" +
					"  h.NOTE,\n" +
					"  h.USER_ID,\n" +
					"  h.USER_INFO,\n" +
					"  h.CONTROL_RESULT_CODE,\n" +
					"  h.CONTROL_RESULT_CODE2\n" +
					"FROM report_history h\n" +
					"WHERE h.report_id = ? " + filter +
					"ORDER BY h.ID DESC\n" +
					"OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY");
			ps.setLong(1, reportId);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = getReportHistoryFromResultSet(rs, false, false);
				result.setReport(getReport(reportId, connection));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public Long getLastReportHistoryIdByReportId(Long reportId, boolean forSuperUser, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		Long result = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			String filter = "";
			if (forSuperUser) {
				String statuses = "";
				for (ReportStatus.Status status : ReportStatus.suUserStatuses) {
					if (!statuses.isEmpty()) {
						statuses += ",";
					}
					statuses += "'" + status.name() + "'";
				}
				filter = "\nAND PKG_FRSI_UTIL.GET_LAST_STATUS_CODE(h2.ID) IN (" + statuses + ")\n";
			}
			ps = connection.prepareStatement("SELECT h.ID\n" +
					"FROM report_history h\n" +
					"WHERE h.ID = (SELECT max(h2.ID)\n" +
					"              FROM REPORT_HISTORY h2\n" +
					"              WHERE h2.REPORT_ID = ? " + filter + ")\n" +
					"ORDER BY h.ID DESC");
			ps.setLong(1, reportId);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getLong("ID");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public List<ReportStatus> getReportStatusHistoryByReportHistoryId(Long reportHistoryId, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		List<ReportStatus> result = new ArrayList<ReportStatus>();
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("SELECT * FROM REPORT_HISTORY_STATUSES WHERE report_history_id = ? ORDER BY id");
			ps.setLong(1, reportHistoryId);
			rs = ps.executeQuery();
			while (rs.next()) {
				ReportStatus reportStatus = getReportStatusFromResultSet(rs);
				reportStatus.setReportHistory(getReportHistoryNoLobs(reportHistoryId, connection));
				result.add(reportStatus);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public ReportStatus getLastReportStatusByReportId(long reportId, boolean forSuperUser, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		ReportStatus result = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			String filter = "";

			if (forSuperUser) {
				String statuses = "";
				for (ReportStatus.Status status : ReportStatus.suUserStatuses) {
					if (!statuses.isEmpty()) {
						statuses += ",";
					}
					statuses += "'" + status.name() + "'";
				}
				filter = "\nAND s.STATUS_CODE IN ("+statuses+")\n";
			}

			ps = connection.prepareStatement("SELECT\n" +
					"  s.*\n" +
					"FROM report_history h\n" +
					"  INNER JOIN REPORT_HISTORY_STATUSES s ON s.ID = (SELECT max(s2.ID)\n" +
					"                                                  FROM REPORT_HISTORY_STATUSES s2\n" +
					"                                                  WHERE h.ID = s2.REPORT_HISTORY_ID)\n" +
					"WHERE h.report_id = ?" +filter+
					"ORDER BY h.ID DESC\n" +
					"OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY");
			ps.setLong(1, reportId);
			rs = ps.executeQuery();
			if (rs.next()) {
				ReportStatus reportStatus = getReportStatusFromResultSet(rs);
				reportStatus.setReportHistory(getReportHistoryNoLobs(rs.getLong("REPORT_HISTORY_ID"), connection));
				result = reportStatus;
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public ReportStatus getLastReportStatusByReportHistoryId(long reportHistoryId, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		ReportStatus result = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("SELECT * FROM REPORT_HISTORY_STATUSES s\n" +
					"WHERE s.ID=(SELECT max(s2.ID) FROM REPORT_HISTORY_STATUSES s2 WHERE s2.REPORT_HISTORY_ID = ?)");
			ps.setLong(1, reportHistoryId);
			rs = ps.executeQuery();
			if (rs.next()) {
				ReportStatus reportStatus = getReportStatusFromResultSet(rs);
				reportStatus.setReportHistory(getReportHistoryNoLobs(rs.getLong("REPORT_HISTORY_ID"), connection));
				result = reportStatus;
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public List<ReportStatus> getReportStatusHistoryByReportId(long reportId, boolean forSuperUser, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		List<ReportStatus> result = new ArrayList<ReportStatus>();
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			String filter = "";
			if (forSuperUser) {
				String statuses = "";
				for (ReportStatus.Status status : ReportStatus.suUserStatuses) {
					if (!statuses.isEmpty()) {
						statuses += ",";
					}
					statuses += "'" + status.name() + "'";
				}
				filter = "\nAND PKG_FRSI_UTIL.GET_LAST_STATUS_CODE(h.ID) IN ("+statuses+")\n";
			}
			ps = connection.prepareStatement("SELECT *\n" +
					"FROM REPORT_HISTORY_STATUSES s\n" +
					"WHERE s.REPORT_HISTORY_ID = (SELECT max(h.ID)\n" +
					"                              FROM REPORT_HISTORY h\n" +
					"                              WHERE h.REPORT_ID = ? " + filter + " ) " +
					"ORDER BY s.ID");
			ps.setLong(1, reportId);
			rs = ps.executeQuery();
			while (rs.next()) {
				ReportStatus reportStatus = getReportStatusFromResultSet(rs);
				reportStatus.setReportHistory(getReportHistoryNoLobs(reportId, connection));
				result.add(reportStatus);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public Long getReportId(String idn, Date reportDate, String formCode) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Long result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT id FROM reports WHERE idn = '" + idn + "' AND TRUNC(report_date) = TRUNC(?) AND form_code = ?");
			//ps.setString(1, idn); // Doesn't work because JDBC doesn't wrap a value in quotes if a string value contains only numeric characters.
			java.sql.Date sqlReportDate = new java.sql.Date(reportDate.getTime());
			ps.setDate(1, sqlReportDate);
			ps.setString(2, formCode);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getLong("id");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public Report getReport(Long id, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		Report result = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("SELECT * FROM reports WHERE id = ?");
			ps.setLong(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = getReportFromResultSet(rs);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public Report getReportByIdnDateForm(String idn, Date reportDate, String formCode) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Report result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT * FROM reports WHERE idn = '" + idn + "' AND TRUNC(report_date) = TRUNC(?) AND form_code = ?");
			//ps.setString(1, idn); // Doesn't work because JDBC doesn't wrap a value in quotes if a string value contains only numeric characters.
			java.sql.Date sqlReportDate = new java.sql.Date(reportDate.getTime());
			ps.setDate(1, sqlReportDate);
			ps.setString(2, formCode);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = getReportFromResultSet(rs);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	private Report getReportFromResultSet(ResultSet rs) throws SQLException {
		Report item = new Report();
		item.setId(rs.getLong("id"));
		item.setIdn(rs.getString("idn"));
		item.setIdnChild(rs.getString("idn_child"));
		item.setReportDate(rs.getDate("report_date"));
		item.setFormCode(rs.getString("form_code"));
		return item;
	}

	private ReportHistory getReportHistoryFromResultSet(ResultSet rs, boolean withData, boolean withAttachment) throws SQLException {
		ReportHistory item = new ReportHistory();
		item.setId(rs.getLong("id"));
		item.setSaveDate(rs.getDate("save_date"));
		if (withData) {
			Clob clobData = rs.getClob("data");
			if (clobData != null) {
				item.setData(clobData.getSubString(1, (int) clobData.length()));
				clobData.free();
			}
		}
		item.setDataSize(rs.getLong("data_size"));
		item.setComments(rs.getString("comments"));
		if (withAttachment) {
			Blob blobAttachment = rs.getBlob("attachment");
			if (blobAttachment != null) {
				item.setAttachment(blobAttachment.getBytes(1, (int) blobAttachment.length()));
				blobAttachment.free();
			}
		}
		item.setAttachmentSize(rs.getLong("attachment_size"));
		item.setAttachmentFileName(rs.getString("attachment_file_name"));
		item.setHash(rs.getString("hash"));
		item.setDeliveryWayCode(rs.getString("delivery_way_code"));
		item.setUserId(rs.getLong("user_id"));
		item.setUserInfo(rs.getString("user_info"));
		item.setSuUserId(rs.getLong("su_user_id"));
		item.setSuUserInfo(rs.getString("su_user_info"));
		item.setSuComments(rs.getString("su_comments"));
		item.setNote(rs.getString("note"));
		item.setControlResultCode(rs.getString("control_result_code"));
		item.setControlResultCode2(rs.getString("control_result_code2"));
		return item;
	}

	private ReportStatus getReportStatusFromResultSet(ResultSet rs) throws SQLException {
		ReportStatus item = new ReportStatus();
		item.setId(rs.getLong("id"));
		item.setStatusCode(rs.getString("status_code"));
		item.setStatusDate(rs.getDate("status_date"));
		item.setMessage(rs.getString("message"));
		item.setUserId(rs.getLong("user_id"));
		item.setUserInfo(rs.getString("user_info"));
		item.setUserLocation(rs.getString("user_location"));
		item.setUserWarrantId(rs.getLong("USER_WARRANT_ID"));
		if(item.getUserWarrantId() != null && item.getUserWarrantId() != 0)
			item.setHaveUserWarrant(true);
		else
			item.setHaveUserWarrant(false);
		item.setRespWarrantId(rs.getLong("RESP_WARRANT_ID"));
		if(item.getRespWarrantId() != null && item.getRespWarrantId() != 0)
			item.setHaveRespWarrant(true);
		else
			item.setHaveRespWarrant(false);
		return item;
	}

	private Long createReport(Report report, ReportHistory reportHistory, ReportStatus reportStatus, Form form, AuditEvent auditEvent) {
		Long result = null;
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);

			result = insertReport(report, connection);

			reportHistory.getReport().setId(result);
			Long reportHistoryId = insertReportHistory(reportHistory, connection);

			updateReportHistoryIsExistList(reportHistoryId, false, connection);

			reportStatus.getReportHistory().setId(reportHistoryId);
			insertReportStatusHistory(reportStatus, connection, auditEvent);

			ReportHistory lastReportHistory = getLastReportHistoryByReportId(result, true, false, false, connection);

			fillDefaultValueByFormWithValues(connection, lastReportHistory.getId(), form.getFormHistory().getInputValueChecks(), lastReportHistory.getData(), true);

			connection.commit();
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
		return result;
	}

	@Override
	public Long createReportOutputReport(Date reportDate, Form form, Map<String, String> inputValues, Set<Long> inputReportIds, PortalUser user, String idn, String initStatus, Date curDate) {

		fillInitialData(reportDate, inputValues, form.getCode(), "ru");

		Long result = null;
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);

			Report report = new Report();
			ReportHistory lastReportHistoryItem = null;

			report.setReportDate(reportDate);
			report.setFormCode(form.getCode());

			if (initStatus.equals(ReportStatus.Status.DRAFT.toString()))
				report.setIdn("DRAFT " + String.valueOf(core.getOracleSequenceNextValue("SEQ_OUTREP_IDN")));
			else {
				if (idn == null || idn.trim().isEmpty())
					throw new EJBException("БИН для организации не установлен");
				report.setIdn(idn);
			}
			Long reportId = insertReport(report, connection);
			report.setId(reportId);

			lastReportHistoryItem = new ReportHistory();
			lastReportHistoryItem.setReport(report);
			lastReportHistoryItem.setSaveDate(curDate);
			String jsonData = gson.toJson(inputValues);
			lastReportHistoryItem.setData(jsonData);
			lastReportHistoryItem.setDataSize((long) lastReportHistoryItem.getData().length());
			if (initStatus.equals(ReportStatus.Status.DRAFT.toString()))
				lastReportHistoryItem.setComments("DRAFT");
			else
				lastReportHistoryItem.setComments(null);
			lastReportHistoryItem.setAttachment(null);
			lastReportHistoryItem.setAttachmentSize(0L);
			lastReportHistoryItem.setAttachmentFileName(null);
			lastReportHistoryItem.setUserId(user.getUserId());
			lastReportHistoryItem.setUserInfo(user.getFullName());
			lastReportHistoryItem.setSuUserId(null);
			lastReportHistoryItem.setSuUserInfo(null);
			lastReportHistoryItem.setSuComments(null);
			lastReportHistoryItem.setKvMap(inputValues);
			long lastReportHistoryId = insertReportHistory(lastReportHistoryItem, connection);
			lastReportHistoryItem.setId(lastReportHistoryId);

			ReportStatus status = new ReportStatus();
			status.setReportHistory(lastReportHistoryItem);
			status.setStatusCode(initStatus);
			status.setStatusDate(curDate);
			status.setUserId(user.getUserId());
			status.setUserInfo(user.getFullName());
			status.setUserLocation(null);
			status.setMessage("Сформировал(а): " + status.getUserInfo());
			insertReportStatusHistory(status, connection, null);

			for (Long inputReportId : inputReportIds) {
				insertInputReportId(reportId, inputReportId, connection);
			}

			connection.commit();
			result = reportId;
		} catch (SQLException e) {
			if (connection != null) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} catch (Exception e) {
			if (connection != null) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
		return result;
	}

	private void updateReport(Report report, ReportHistory reportHistory, ReportStatus reportStatus, Form form, AuditEvent auditEvent) {
		Connection connection = null;
		Long reportHistoryId = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			ReportHistory lastReportHistory = getLastReportHistoryByReportIdNoLobs(report.getId(), false, connection);
			if (lastReportHistory == null) {
				throw new EJBException(MessageFormat.format("Не найдены данные отчета с id {0}", report.getId()));
			}

			// boolean createNewStatus;
			ReportStatus lastReportStatus = getLastReportStatusByReportHistoryId(lastReportHistory.getId(), connection);
			ReportStatus.Status lastStatus = ReportStatus.Status.valueOf(lastReportStatus.getStatusCode());
			if (!ReportStatus.suUserStatuses.contains(lastStatus)) {
				reportHistory.setId(lastReportHistory.getId());
				updateReportHistory(reportHistory, connection);
				reportHistoryId = lastReportHistory.getId();
				// createNewStatus = !lastStatus.equals(ReportStatus.Status.DRAFT);
			} else {
				long prevReportHistoryId = getLastReportHistoryIdByReportId(report.getId(), false, connection);
				reportHistoryId = insertReportHistory(reportHistory, connection);
				copyReportHistoryStatuses(prevReportHistoryId, reportHistoryId, connection);
				copyReportFiles(prevReportHistoryId, reportHistoryId, connection);
				reportHistory.setId(reportHistoryId);
				// createNewStatus = true;
			}

			updateReportHistoryIsExistList(reportHistoryId, false, connection);

			// if(createNewStatus) {
			if(reportStatus!=null) {
				reportStatus.getReportHistory().setId(reportHistoryId);
				insertReportStatusHistory(reportStatus, connection, null);
			}
			// }
			/*else {
				status.setId(getLastReportStatusByReportHistoryId(reportHistory.getId()).getId());
				updateReportStatusHistory(status, connection);
			}*/

			ReportHistory lastReportHistoryWithData = getLastReportHistoryByReportId(report.getId(), true, false, false, connection);
			fillDefaultValueByFormWithValues(connection, lastReportHistoryWithData.getId(), form.getFormHistory().getInputValueChecks(), lastReportHistoryWithData.getData(), true);

			insertAuditEvent(auditEvent, connection);

			connection.commit();
		} catch (SQLException e) {
			if (connection != null) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} catch (Exception e) {
			if (connection != null) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
	}

	@Override
	public void updateReportOutputReport(Long reportId, Map<String, String> inputValues, Set<Long> inputReportIds, PortalUser user, String initStatus, Date curDate) {
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);

			Report report = getReport(reportId, connection);
			long prevReportHistoryId = getLastReportHistoryIdByReportId(reportId, true, connection);

			fillInitialData(report.getReportDate(), inputValues, report.getFormCode(), "ru");

			ReportHistory lastReportHistoryItem = new ReportHistory();
			lastReportHistoryItem.setReport(report);
			lastReportHistoryItem.setSaveDate(curDate);
			String jsonData = gson.toJson(inputValues);
			lastReportHistoryItem.setData(jsonData);
			lastReportHistoryItem.setDataSize((long) lastReportHistoryItem.getData().length());
			if (initStatus.equals(ReportStatus.Status.DRAFT.toString()))
				lastReportHistoryItem.setComments("DRAFT: пересоздан");
			else
				lastReportHistoryItem.setComments("Пересоздан");
			lastReportHistoryItem.setAttachment(null);
			lastReportHistoryItem.setAttachmentSize(0L);
			lastReportHistoryItem.setAttachmentFileName(null);
			lastReportHistoryItem.setUserId(user.getUserId());
			lastReportHistoryItem.setUserInfo(user.getFullName());
			lastReportHistoryItem.setSuUserId(null);
			lastReportHistoryItem.setSuUserInfo(null);
			lastReportHistoryItem.setSuComments(null);
			lastReportHistoryItem.setKvMap(inputValues);
			long lastReportHistoryId = insertReportHistory(lastReportHistoryItem, connection);
			lastReportHistoryItem.setId(lastReportHistoryId);

			copyReportHistoryStatuses(prevReportHistoryId, lastReportHistoryId, connection);

			ReportStatus status = new ReportStatus();
			status.setReportHistory(lastReportHistoryItem);
			status.setStatusCode(initStatus);
			status.setStatusDate(curDate);
			status.setUserId(user.getUserId());
			status.setUserInfo(user.getFullName());
			status.setUserLocation(null);
			status.setMessage("Переформировал(а): " + status.getUserInfo());
			insertReportStatusHistory(status, connection, null);


			deleteInputReports(reportId, connection);
			for(Long inputReportId:inputReportIds) {
				insertInputReportId(reportId, inputReportId, connection);
			}

			connection.commit();
		} catch (SQLException e) {
			if (connection != null) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} catch (Exception e) {
			if (connection != null) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
	}

	private Long insertReport(Report report, Connection connection) throws SQLException {
		PreparedStatement ps = null;
		Long result = null;
		try {
			ps = connection.prepareStatement("INSERT INTO reports (id, idn, idn_child, report_date, form_code) VALUES (seq_reports_id.nextval, ?, ?, TRUNC(?), ?)", new String[]{"id"});
			ps.setString(1, report.getIdn());
			ps.setString(2, report.getIdnChild());
			ps.setDate(3, report.getReportDate() == null ? null : new java.sql.Date(report.getReportDate().getTime()));
			ps.setString(4, report.getFormCode());

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
			ResultSet generatedKeys = ps.getGeneratedKeys();
			if (generatedKeys.next()) result = generatedKeys.getLong(1);
		} finally {
			DbUtil.closeStatement(ps);
		}
		return result;
	}

	private void copyReportHistoryStatuses(long sourceReportHistoryId, long targetReportHistoryId, Connection connection) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("INSERT INTO REPORT_HISTORY_STATUSES (ID, REPORT_HISTORY_ID, STATUS_CODE, STATUS_DATE, MESSAGE, USER_ID, USER_INFO, USER_LOCATION)\n" +
					"SELECT\n" +
					"  SEQ_REPORT_HISTORY_STATUSES_ID.nextval,\n" +
					"  ?,\n" +
					"  STATUS_CODE,\n" +
					"  STATUS_DATE,\n" +
					"  MESSAGE,\n" +
					"  USER_ID,\n" +
					"  USER_INFO,\n" +
					"  USER_LOCATION\n" +
					"FROM (SELECT * FROM REPORT_HISTORY_STATUSES ORDER BY ID)\n" +
					"WHERE REPORT_HISTORY_ID = ?");
			ps.setLong(1, targetReportHistoryId);
			ps.setLong(2, sourceReportHistoryId);

			int affectedRows = ps.executeUpdate();
		} finally {
			DbUtil.closeStatement(ps);
		}
	}

	private void copyReportFiles(long sourceReportHistoryId, long targetReportHistoryId, Connection connection) throws SQLException{
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("INSERT INTO attached_file (\n" +
					"  ID,\n" +
					"  LINK_ID,\n" +
					"  FILE_DATA,\n" +
					"  FILE_TYPE,\n" +
					"  FILE_NAME,\n" +
					"  FILE_DATE,\n" +
					"  ID_USR,\n" +
					"  FILE_DATA_PDF,\n" +
					"  FILE_KIND)\n" +
					"  SELECT\n" +
					"    SEQ_REPORT_FILE_ID.nextval,\n" +
					"    ?,\n" +
					"    FILE_DATA,\n" +
					"    FILE_TYPE,\n" +
					"    FILE_NAME,\n" +
					"    FILE_DATE,\n" +
					"    ID_USR,\n" +
					"    FILE_DATA_PDF,\n" +
					"    FILE_KIND\n" +
					"  FROM (SELECT * FROM attached_file ORDER BY ID)\n" +
					"  WHERE LINK_ID = ?");
			ps.setLong(1, targetReportHistoryId);
			ps.setLong(2, sourceReportHistoryId);
			ps.execute();
		} finally {
			DbUtil.closeStatement(ps);
		}
	}

	@Override
	public void deleteReport(Long id, Connection connection) {
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }

			ps = connection.prepareStatement("DELETE FROM reports WHERE id = ?");
			ps.setLong(1, id);
			int affectedRows = ps.executeUpdate();
			//if (affectedRows == 0) throw new SQLException("Deleting an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteReportTransactional(Long id, ReportStatus status, AuditEvent auditEvent) {
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			insertReportStatusHistory(status, connection, auditEvent);
			deleteReportRefLink(connection, null, id);
			deleteReport(id, connection);
		} catch (SQLException e) {
			if (connection != null) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} catch (Exception e) {
			if (connection != null) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
	}

	@Override
	public ReportHistory getReportHistory(Long id, boolean withData, boolean withAttachment) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ReportHistory result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT * FROM report_history WHERE id = ?");
			ps.setLong(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = getReportHistoryFromResultSet(rs, withData, withAttachment);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public long getReportIdByReportHistoryId(long reportHistoryId, Connection connection){
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		long reportId = 0;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("SELECT report_id FROM report_history WHERE id = ?");
			ps.setLong(1, reportHistoryId);
			rs = ps.executeQuery();
			if (rs.next()) {
				reportId = rs.getLong("report_id");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return reportId;
	}

	private ReportHistory getReportHistoryNoLobs(Long id, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		ReportHistory result = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("SELECT id, report_id, save_date, data_size, comments, attachment_size, " +
					"attachment_file_name, hash, delivery_way_code, user_id, user_info, su_user_id, su_user_info, su_comments, note," +
					"control_result_code, control_result_code2 " +
					"FROM report_history WHERE id = ?");
			ps.setLong(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = getReportHistoryFromResultSet(rs, false, false);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public Long insertReportHistory(ReportHistory reportHistory) {
		Connection connection = null;
		Long result = null;
		try {
			connection = getConnection();
			result = insertReportHistory(reportHistory, connection);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
		return result;
	}

	private Long insertReportHistory(ReportHistory reportHistory, Connection connection) throws SQLException{
		PreparedStatement ps = null;
		Long result = null;
		try {
			ps = connection.prepareStatement("INSERT INTO report_history" +
					" (id, report_id, save_date, data, data_size, comments, attachment, attachment_size, attachment_file_name, hash, delivery_way_code, user_id, user_info, su_user_id, su_user_info, su_comments)" +
					" VALUES (seq_report_history_id.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{"id"});
			ps.setLong(1, reportHistory.getReport().getId());
			ps.setTimestamp(2, reportHistory.getSaveDate() == null ? null : new java.sql.Timestamp(reportHistory.getSaveDate().getTime()));

			Clob clobData = connection.createClob();
			clobData.setString(1, reportHistory.getData());
			ps.setClob(3, clobData);

			ps.setLong(4, reportHistory.getDataSize());
			ps.setString(5, reportHistory.getComments());

			Blob blobAttachment = connection.createBlob();
			blobAttachment.setBytes(1, reportHistory.getAttachment());
			ps.setBlob(6, blobAttachment);

			ps.setLong(7, reportHistory.getAttachmentSize() == null ? 0 : reportHistory.getAttachmentSize());
			ps.setString(8, reportHistory.getAttachmentFileName());

			ps.setString(9, reportHistory.getHash());

			ps.setString(10, reportHistory.getDeliveryWayCode());
			ps.setLong(11, reportHistory.getUserId());
			ps.setString(12, reportHistory.getUserInfo());
			if (reportHistory.getSuUserId() == null) ps.setNull(13, Types.NUMERIC); else ps.setLong(13, reportHistory.getSuUserId());
			ps.setString(14, reportHistory.getSuUserInfo());
			ps.setString(15, reportHistory.getSuComments());

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
			ResultSet generatedKeys = ps.getGeneratedKeys();
			if (generatedKeys.next()) {
				result = generatedKeys.getLong(1);
			}

		} finally {
			DbUtil.closeStatement(ps);
		}
		return result;
	}

	@Override
	public void updateReportHistory(ReportHistory reportHistory) {
		Connection connection = null;
		try {
			connection = getConnection();
			updateReportHistory(reportHistory, connection);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
	}

	@Override
	public void updateReportHistory(ReportHistory reportHistory, Connection connection) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("UPDATE report_history SET report_id=?, save_date=?, data=?, data_size=?, comments=?, attachment=?, attachment_size=?, attachment_file_name=?, hash=?, delivery_way_code=?, user_id=?, user_info=?, su_user_id=?, su_user_info=?, su_comments=? WHERE id = ?");
			ps.setLong(1, reportHistory.getReport().getId());
			ps.setTimestamp(2, reportHistory.getSaveDate() == null ? null : new java.sql.Timestamp(reportHistory.getSaveDate().getTime()));

			Clob clobData = connection.createClob();
			clobData.setString(1, reportHistory.getData());
			ps.setClob(3, clobData);

			ps.setLong(4, reportHistory.getDataSize());
			ps.setString(5, reportHistory.getComments());

			Blob blobAttachment = connection.createBlob();
			blobAttachment.setBytes(1, reportHistory.getAttachment());
			ps.setBlob(6, blobAttachment);

			ps.setLong(7, reportHistory.getAttachmentSize() == null ? 0 : reportHistory.getAttachmentSize());
			ps.setString(8, reportHistory.getAttachmentFileName());

			ps.setString(9, reportHistory.getHash());

			ps.setString(10, reportHistory.getDeliveryWayCode());
			ps.setLong(11, reportHistory.getUserId());
			ps.setString(12, reportHistory.getUserInfo());
			if (reportHistory.getSuUserId() == null) ps.setNull(13, Types.NUMERIC);
			else ps.setLong(13, reportHistory.getSuUserId());
			ps.setString(14, reportHistory.getSuUserInfo());
			ps.setString(15, reportHistory.getSuComments());

			ps.setLong(16, reportHistory.getId());

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");
		} finally {
			DbUtil.closeStatement(ps);
		}
	}

	@Override
	public void updateReportHistoryData(Long reportHistoryId, String data, AuditEvent auditEvent) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			Long reportId = getReportIdByReportHistoryId(reportHistoryId, connection);
			Report report = getReport(reportId, connection);
			ReportHistory reportHistory = getReportHistoryNoLobs(reportHistoryId, connection);
			reportHistory.setReport(report);
			reportHistory.setData(data);
			reportHistory.updateHash(null);
			ps = connection.prepareStatement("UPDATE report_history SET data=?, data_size=?, hash=? WHERE id = ?");
			Clob clobData = connection.createClob();
			clobData.setString(1, data);
			ps.setClob(1, clobData);
			ps.setLong(2, (long) data.length());
			ps.setString(3, reportHistory.getHash());
			ps.setLong(4, reportHistoryId);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void deleteReportHistory(Long id) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("DELETE FROM report_history WHERE id = ?");
			ps.setLong(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Deleting an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public ReportStatus getReportStatus(Long id) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ReportStatus result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT * FROM report_history_statuses WHERE id = ?");
			ps.setLong(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = getReportStatusFromResultSet(rs);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public Long insertReportStatusHistory(ReportStatus reportStatus, AuditEvent auditEvent) {
		Connection connection = null;
		Long result = null;
		try {
			connection = getConnection();
			result = insertReportStatusHistory(reportStatus, connection, auditEvent);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
		return result;
	}

	private Long insertReportStatusHistory(ReportStatus reportStatus, Connection connection, AuditEvent auditEvent) throws SQLException{
		PreparedStatement ps = null;
		Long result = null;
		try {
			ps = connection.prepareStatement("INSERT INTO report_history_statuses" +
					" (id, report_history_id, status_code, status_date, message, user_id, user_info, user_location, user_warrant_id, resp_warrant_id)" +
					" VALUES (seq_report_history_statuses_id.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{"id"});
			ps.setLong(1, reportStatus.getReportHistory().getId());
			ps.setString(2, reportStatus.getStatusCode());
			ps.setTimestamp(3, reportStatus.getStatusDate() == null ? null : new java.sql.Timestamp(reportStatus.getStatusDate().getTime()));
			ps.setString(4, reportStatus.getMessage());
			ps.setLong(5, reportStatus.getUserId());
			ps.setString(6, reportStatus.getUserInfo());
			ps.setString(7, reportStatus.getUserLocation());
			if(reportStatus.getUserWarrantId() != null && reportStatus.getUserWarrantId() != 0) {
				ps.setLong(8, reportStatus.getUserWarrantId());
			}else{
				ps.setNull(8, OracleTypes.NULL);
			}
			if(reportStatus.getRespWarrantId() != null && reportStatus.getRespWarrantId() != 0) {
				ps.setLong(9, reportStatus.getRespWarrantId());
			}else{
				ps.setLong(9, OracleTypes.NULL);
			}
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
			ResultSet generatedKeys = ps.getGeneratedKeys();
			if (generatedKeys.next()) result = generatedKeys.getLong(1);

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);
		} finally {
			DbUtil.closeStatement(ps);
		}
		return result;
	}

	@Override
	public void updateReportStatusHistory(ReportStatus reportStatus) {
		Connection connection = null;
		try {
			connection = getConnection();
			updateReportStatusHistory(reportStatus, connection);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
	}

	private void updateReportStatusHistory(ReportStatus reportStatus, Connection connection) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("UPDATE report_history_statuses SET report_history_id=?, status_code=?, status_date=?, message=?, user_id=?, user_info=?, user_location=? WHERE id = ?");
			ps.setLong(1, reportStatus.getReportHistory().getId());
			ps.setString(2, reportStatus.getStatusCode());
			ps.setTimestamp(3, reportStatus.getStatusDate() == null ? null : new java.sql.Timestamp(reportStatus.getStatusDate().getTime()));
			ps.setString(4, reportStatus.getMessage());
			ps.setLong(5, reportStatus.getUserId());
			ps.setString(6, reportStatus.getUserInfo());
			ps.setString(7, reportStatus.getUserLocation());

			ps.setLong(8, reportStatus.getId());

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");
		}  finally {
			DbUtil.closeStatement(ps);
		}
	}

	@Override
	public void deleteReportStatusHistory(Long id) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("DELETE FROM report_history_statuses WHERE id = ?");
			ps.setLong(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Deleting an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public Report getReportFromXml(String xml, String languageCode, Date reportDate) throws Exception {
		XmlImporter xmlImporter = new XmlImporter(xml);
		xmlImporter.createKvMap(this, languageCode, reportDate);
		Report report = xmlImporter.getReport();

		// Validation

		List<Form> forms = getFormsByCodeLanguageReportDate(report.getFormCode(), languageCode, reportDate, null);
		if (forms.isEmpty()) return report;

		FormHistory dummyForm = getFormHistoryWithInputValueChecks(forms.get(0).getFormHistory().getId());
		Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {}.getType();
		List<InputValueCheck> inputValueChecks = gson.fromJson(dummyForm.getInputValueChecks(), typeListInputValueCheck);
		if (inputValueChecks == null || inputValueChecks.isEmpty()) return report;

		Map<String, InputValueCheck> mapKeyIvc = new HashMap<String, InputValueCheck>();
		for (InputValueCheck item : inputValueChecks) {
			if(item.getAuto() != null && item.getAuto().booleanValue()) continue;
			mapKeyIvc.put(item.getKey(), item);
		}
		if (mapKeyIvc.isEmpty()) return report;

		List<ReportHistory> reportHistoryList = report.getReportHistory();
		if (reportHistoryList == null || reportHistoryList.isEmpty()) return report;
		ReportHistory lastReportHistory = reportHistoryList.get(0);
		Map<String, String> kvMap = lastReportHistory.kvMap;
		if (kvMap == null || kvMap.isEmpty()) return report;

		for (Map.Entry<String, String> entry : kvMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			int dynPos = key.indexOf("$D.");
			if (dynPos >= 0) { // Dynamic row item
				key = key.substring(0, dynPos) + "$DynamicRowId";
			}
			InputValueCheck ivc = mapKeyIvc.get(key);
			if (ivc == null) continue;

			String refName = ivc.getRef();

			if (ivc.getMultiValue() != null && ivc.getMultiValue().booleanValue()) { // Incoming value is a list of comma separated values
				// References
				if (refName != null && !refName.isEmpty()) {
					if (ivc.getInputType().equals("InputSelect")) {
						String[] valueParts = value.split(",");
						List<String> ids = new ArrayList<String>();
						for (int i = 0; i < valueParts.length; i++) {
							String valuePart = valueParts[i];
							Long rec_id = null;
							try {
								rec_id = Long.valueOf(valuePart.trim());
							} catch (NumberFormatException e) {
								logger.error(e.getMessage());
							}
							ids.add(rec_id != null && reference.refItemExists(refName, rec_id, reportDate) ? rec_id.toString() : "-1");
						}
						MultiSelectValue multiSelectValue = new MultiSelectValue();
						multiSelectValue.setValues(ids);
						entry.setValue(gson.toJson(multiSelectValue));
					}
				}
			} else { // Incoming value is a single value
				if (ivc.getValueType() != null) {
					try {
						if (ivc.getValueType().equals("date")) {
							Date date = Convert.dateFormatRus.parse(entry.getValue());
						} else if (ivc.getValueType().equals("time")){
							Date date = Convert.timeFormat.parse(entry.getValue());
						} else if (ivc.getValueType().equals("int")) {
							long l = Long.parseLong(entry.getValue());
						} else if (ivc.getValueType().equals("float")) {
							double d = Double.parseDouble(entry.getValue());
						}
					} catch (Exception e) {
						throw new Exception("Значение параметра " + key + " не соответствует заданному формату!");
					}
				}
				// References
				Long rec_id = null;
				if (refName != null && !refName.isEmpty()) {
					if (ivc.getInputType().equals("InputSelect")) {
						try {
							rec_id = Long.valueOf(value.trim());
						} catch (NumberFormatException e) {logger.error(e.getMessage());}
						if (rec_id == null || !reference.refItemExists(refName, rec_id, reportDate)) entry.setValue("-1");
					} else if (ivc.getInputType().equals("InputText")) {
						rec_id = reference.getRefRecId(ivc.getRef(), ivc.getRefCaption(), value, reportDate);
						entry.setValue(rec_id == -1 ? "не найден в справочнике" : reference.getRefOriginalValue(ivc.getRef(), ivc.getRefCaption(), value, reportDate));
					}
				}
			}
		}
		return report;
	}

	@Override
	public Report getReportFromXml(byte[] array, String languageCode, Date reportDate) throws Exception {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(array);
		UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(byteArrayInputStream);
		ubis.skipBOM();
		InputStreamReader isr = new InputStreamReader(ubis);
		BufferedReader br = new BufferedReader(isr);
		StringBuilder xml = new StringBuilder();
		String s;
		while ((s = br.readLine()) != null) {
			xml.append(s + "\n");
		}
		br.close();
		isr.close();
		ubis.close();
		byteArrayInputStream.close();
		return getReportFromXml(xml.toString(), languageCode, reportDate);
	}

	@Override
	public boolean checkPeriod(Date reportDate, String formCode, Long subjectTypeRecId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean result = false;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select pkg_frsi_util.check_period(p.code,?) as is_valid\n" +
					"from subjecttype_forms sf inner join rep_per_dur_months p on sf.period_id=p.id\n" +
					"where sf.ref_subject_type_rec_id=? and sf.form_code=?");
			ps.setDate(1, new java.sql.Date(reportDate.getTime()));
			ps.setLong(2, subjectTypeRecId);
			ps.setString(3, formCode);
			rs = ps.executeQuery();
			while (rs.next()) {
				result = rs.getInt("IS_VALID") == 1;
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public String getSignerInfoByReportIdRefPostRecId(long reportHistoryId, long refPostRecId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String result = "";
		try {
			connection = getConnection();

			ps = connection.prepareStatement("SELECT s.*, w.PRINCIPAL\n" +
					"FROM report_sign s\n" +
					"  INNER JOIN v_ref_post p ON s.ref_post = p.rec_id AND p.begin_date = (SELECT max(p2.begin_date)\n" +
					"                                                                       FROM v_ref_post p2\n" +
					"                                                                       WHERE p2.rec_id = p.rec_id AND\n" +
					"                                                                             p.begin_date <= nvl(s.sign_date, sysdate))\n" +
					"                             AND (p.end_date IS NULL OR p.end_date > s.sign_date) AND p.REC_ID = ?\n" +
					"  LEFT JOIN USER_WARRANT w ON s.USER_WARRANT_ID = w.ID\n" +
					"WHERE s.report_history_id = ?\n" +
					"ORDER BY s.id");
			ps.setLong(1, refPostRecId);
			ps.setLong(2, reportHistoryId);
			rs = ps.executeQuery();
			while (rs.next()) {
				PortalUser user = getUserByUserId(rs.getLong("USER_ID"), connection);
				result = user.getFullName();
				Date signDate = rs.getDate("SIGN_DATE");
				if (signDate != null) {
					DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
					result += ", " + format.format(signDate);
				}
				Long principal = rs.getLong("principal");
				if (!rs.wasNull()) {
					user = getUserByUserId(principal, connection);
					result += " от имени " + user.getFullName();
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public void updateReportControlResultCode(long reportHistoryId, String controlResultCode) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("UPDATE report_history SET control_result_code=? WHERE id = ?");
			ps.setString(1, controlResultCode);
			ps.setLong(2, reportHistoryId);

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void updateReportControlResultCode2(long reportHistoryId, String controlResultCode) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("UPDATE report_history SET control_result_code2=? WHERE id = ?");
			ps.setString(1, controlResultCode);
			ps.setLong(2, reportHistoryId);

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public boolean isStatusCompatible(String oldStatus, String newStatus) {
		Set<String> statuses = compatibleStatuses.get(newStatus);
		if (statuses == null)
			return false;

		return statuses.contains(oldStatus);
	}

	@Override
	public void setReportApproved(Long reportId, boolean approved, AbstractUser user, Date approvalDate, AuditEvent auditEvent) {
		Report report = getReport(reportId, null);
//		Date approvalDate = new Date();

		ReportStatus status = new ReportStatus();
		status.setReportHistory(getLastReportHistoryByReportIdNoLobs(reportId, true, null));
		status.setStatusCode(approved ? ReportStatus.Status.APPROVED.name() : ReportStatus.Status.DISAPPROVED.name());
		status.setStatusDate(approvalDate);
		status.setMessage((approved ? "Утвердил(а): " : "Разутвердил: ") + user.getDisplayName() + " [" + user.getLocation() + "]");
		status.setUserId(user.getId());
		status.setUserInfo(user.getDisplayName());
		status.setUserLocation(user.getLocation());

		Connection connection = null;
		try {
			connection = getConnection();
			insertReportStatusHistory(status, connection, auditEvent);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
	}

	@Override
	public void insertReportRefLink(Connection connection, Long reportHistoryId, String refCode, Long recId){
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("INSERT INTO report_ref_link (id,report_history,ref_code, rec_id, delfl) values(SEQ_REPORT_REF_LINK_ID.NEXTVAL,?,?,?,0)");
			ps.setLong(1,reportHistoryId);
			ps.setString(2, refCode);
			ps.setLong(3, recId);

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteReportRefLink(Connection connection, Long reportHistoryId, Long reportId){
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			if(reportId != null){
				ps = connection.prepareStatement("update(\n" +
						"select rr.*\n" +
						"  from report_ref_link rr,\n" +
						"       report_history rh,\n" +
						"       reports r       \n" +
						" where rr.delfl = 0\n" +
						"   and rr.report_history = rh.id\n" +
						"   and rh.report_id = r.id\n" +
						"   and r.id = ?\n" +
						") d\n" +
						"set d.delfl = 1");
				ps.setLong(1,reportId);
				ps.executeUpdate();
			}else if (reportHistoryId != null){
				ps = connection.prepareStatement("UPDATE report_ref_link SET delfl = 1 WHERE report_history = ?");
				ps.setLong(1,reportHistoryId);
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void editReportNote(Long reportHistoryId, String note, boolean append, boolean newLine){
		Connection connection = null;
		PreparedStatement ps = null;
		String sqlText = "";
		try {
			connection = getConnection();

			String oldNote = getReportNote(reportHistoryId, connection);

			sqlText = "update(select note\n" +
					"         from report_history\n" +
					"        where id = ?) d\n";
			if(append && oldNote != null && !oldNote.trim().isEmpty()){
				sqlText = sqlText + " " + "set d.note = d.note || ' ' ||  " + " ?";
			}else{
				sqlText = sqlText + " " + "set d.note = ?";
			}
			ps = connection.prepareStatement(sqlText);
			ps.setLong(1, reportHistoryId);
			if(newLine && oldNote != null && !oldNote.trim().isEmpty())
				note = "\n" + note;
			ps.setString(2, note);

			ps.executeUpdate();

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public String getReportNote(Long reportHistoryId, Connection connection){
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		String result = "";
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("SELECT note from report_history where id = ?");
			ps.setLong(1, reportHistoryId);
			rs = ps.executeQuery();
			while (rs.next()) {
				result = rs.getString("note");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	// Документы прикрепленные к отчетам
	@Override
	public List<AttachedFile> getFileListByLinkId(Long linkId, int fileKind, Connection connection) {
		boolean localCon = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<AttachedFile> result = new ArrayList<AttachedFile>();
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement(
					"SELECT rf.ID," +
							"rf.link_id," +
							"rf.FILE_TYPE," +
							"rf.FILE_NAME," +
							"rf.FILE_DATE," +
							"u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME," +
							"rf.FILE_KIND," +
							"rf.HASH " +
							" FROM ATTACHED_FILE rf," +
							"      F_USERS u " +
							" WHERE rf.id_usr = u.user_id" +
							"  AND rf.link_id = ? " +
							"  AND rf.file_kind = ?");
			ps.setLong(1, linkId);
			ps.setInt(2, fileKind);
			rs = ps.executeQuery();
			while (rs.next()) {
				AttachedFile item = new AttachedFile();
				item.setId(rs.getLong("ID"));
				item.setLinkId(rs.getLong("LINK_ID"));
				item.setFileType(rs.getString("FILE_TYPE"));
				item.setFileName(rs.getString("FILE_NAME"));
				item.setFileDate(rs.getDate("FILE_DATE"));
				item.setUserName(rs.getString("USER_NAME"));
				item.setFileKind(rs.getInt("FILE_KIND"));
				item.setHash(rs.getString("HASH"));
				result.add(item);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public List<AttachedFile> getFileListWithDataByLinkId(Long linkId, int fileKind) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<AttachedFile> result = new ArrayList<AttachedFile>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement(
					"SELECT rf.ID," +
							"rf.LINK_ID," +
							"rf.FILE_TYPE," +
							"rf.FILE_NAME," +
							"rf.FILE_DATE," +
							"u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME," +
							"rf.FILE_KIND," +
							"rf.HASH, " +
							"rf.file_data, " +
							"rf.file_data_pdf " +
							" FROM ATTACHED_FILE rf," +
							"      F_USERS u " +
							" WHERE rf.id_usr = u.user_id" +
							"  AND rf.LINK_ID = ? " +
							"  AND rf.file_kind = ?");
			ps.setLong(1, linkId);
			ps.setInt(2, fileKind);
			rs = ps.executeQuery();
			while (rs.next()) {
				AttachedFile item = new AttachedFile();
				item.setId(rs.getLong("ID"));
				item.setLinkId(rs.getLong("LINK_ID"));
				item.setFileType(rs.getString("FILE_TYPE"));
				item.setFileName(rs.getString("FILE_NAME"));
				item.setFileDate(rs.getDate("FILE_DATE"));
				item.setUserName(rs.getString("USER_NAME"));
				item.setFileKind(rs.getInt("FILE_KIND"));
				item.setHash(rs.getString("HASH"));
				Blob fileData = rs.getBlob("FILE_DATA");
				if (fileData != null) {
					item.setFile(fileData.getBytes(1, (int) fileData.length()));
					fileData.free();
				}
				Blob fileDataPdf = rs.getBlob("FILE_DATA_PDF");
				if (fileDataPdf != null) {
					item.setPdfFile(fileDataPdf.getBytes(1, (int) fileDataPdf.length()));
					fileDataPdf.free();
				}
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
	public void uploadFile(AttachedFile attachedFile, AuditEvent auditEvent, Connection connection){
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			ps = connection.prepareStatement("INSERT INTO attached_file (id,link_id,file_data,file_data_pdf,file_type,file_name,file_date,id_usr, file_kind, hash) values(SEQ_REPORT_FILE_ID.NEXTVAL,?,?,?,?,?,?,?,?,?)");
			java.sql.Date fileDate = new java.sql.Date(attachedFile.getFileDate().getTime());
			ps.setLong(1, attachedFile.getLinkId());
			if(attachedFile.getFile() == null){
				ps.setNull(2, OracleTypes.NULL);
			}else {
				Blob blob = connection.createBlob();
				blob.setBytes(1, attachedFile.getFile());
				ps.setBlob(2, blob);
			}
			Blob blobPdf = connection.createBlob();
			blobPdf.setBytes(1, attachedFile.getPdfFile());
			ps.setBlob(3, blobPdf);
			ps.setString(4, attachedFile.getFileType());
			ps.setString(5, attachedFile.getFileName());
			ps.setDate(6, fileDate);
			ps.setLong(7, attachedFile.getIdUsr());
			ps.setInt(8, attachedFile.getFileKind());
			ps.setString(9, attachedFile.getHash());
			ps.execute();
			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);
			if(localCon){
				connection.commit();
			}
		} catch (Exception e) {
			if (localCon && connection != null) {
				try {
					connection.rollback();
				} catch (SQLException ex) {
				}
			}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void updatePdfFile(AttachedFile attachedFile){
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement(
					"update attached_file " +
							"set file_data_pdf = ? " +
							"where id = ?");
			if(attachedFile.getPdfFile() == null){
				ps.setNull(1, OracleTypes.NULL);
			}else {
				Blob blob = connection.createBlob();
				blob.setBytes(1, attachedFile.getPdfFile());
				ps.setBlob(1, blob);
			}
			ps.setLong(2, attachedFile.getId());

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Update an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public String getDataFileFormat(Long id, Connection connection){
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		String result = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("SELECT file_name FROM attached_file WHERE id = ?");
			ps.setLong(1, id);
			rs = ps.executeQuery();
			while (rs.next()) {
				result = rs.getString("FILE_NAME");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result.substring(result.lastIndexOf(".") + 1).toLowerCase();
	}

	@Override
	public AttachedFile getDataFile(Long id, boolean forPrint){
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		AttachedFile result = null;
		String fileDataName = null;
		try {
			connection = getConnection();

			if (forPrint) {
				if (getDataFileFormat(id, connection).equalsIgnoreCase("pdf"))
					fileDataName = "FILE_DATA";
				else
					fileDataName = "FILE_DATA_PDF";
			}
			else
				fileDataName = "FILE_DATA";

			ps = connection.prepareStatement("SELECT " + fileDataName + ",file_type,file_name FROM attached_file WHERE id = ?");
			ps.setLong(1, id);
			rs = ps.executeQuery();
			while (rs.next()) {
				AttachedFile attachedFile = new AttachedFile();
				if (forPrint) {
					Blob blobPdf = rs.getBlob(fileDataName);
					if (blobPdf != null) {
						attachedFile.setPdfFile(blobPdf.getBytes(1, (int) blobPdf.length()));
						blobPdf.free();
					}
				}
				else {
					Blob blob = rs.getBlob("FILE_DATA");
					if (blob != null) {
						attachedFile.setFile(blob.getBytes(1, (int) blob.length()));
						blob.free();
					}
				}

				attachedFile.setFileType(rs.getString("FILE_TYPE"));
				attachedFile.setFileName(rs.getString("FILE_NAME"));
				result = attachedFile;
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public void deleteFile(Long id, AuditEvent auditEvent, Connection connection){
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			ps = connection.prepareStatement("delete attached_file where id = ?");
			ps.setLong(1,id);
			ps.execute();

			if (auditEvent != null)
				insertAuditEvent(auditEvent, connection);
			if (localCon) {
				connection.commit();
			}
		} catch (Exception e) {
			if (localCon && connection != null) {
				try {
					connection.rollback();
				} catch (SQLException ex) {
				}
			}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteAllFilesByLinkId(Long linkId, int fileKind, Connection connection){
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
			}
			ps = connection.prepareStatement("delete attached_file where link_id = ? AND FILE_KIND = ?");
			ps.setLong(1,linkId);
			ps.setInt(2,fileKind);
			ps.execute();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public Boolean haveAttachedFiles(Long linkId, int fileKind){
		Boolean result = null;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet cursor = null;

		try {
			connection = getConnection();
			ps = connection.prepareStatement(
					"select decode(sum(dbms_lob.getlength(af.file_data)),null,0,1) as have_file " +
					"  from attached_file af " +
					" where af.link_id = ? " +
					"   and af.file_kind = ?");
			ps.setLong(1, linkId);
			ps.setInt(2, fileKind);
			cursor = ps.executeQuery();

			while (cursor.next()) {
				result = cursor.getInt("have_file") > 0;
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, cursor);
		}
		return result;
	}

	@Override
	public Map<String, String> updateCalculatedFields(String formCode, Date reportDate, Map<String, String> kvMap, String languageCode, boolean fillExpectedData) {
		if (kvMap == null) {
			return new HashMap<String, String>();
		}
		List<Form> forms = getFormsByCodeLanguageReportDate(formCode, languageCode, reportDate, null);
		if (forms.size() == 0)
			return kvMap;
		FormHistory formHistory = getFormHistoryWithJsCode(forms.get(0).getFormHistory().getId());
		if (formHistory.getJsCode() == null)
			return kvMap;

		FormHistory formHistory2 = getFormHistoryWithInputValueChecks(forms.get(0).getFormHistory().getId());
		List<InputValueCheck> inputValueChecks = new ArrayList<InputValueCheck>();
		Gson gson = new Gson();
		Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {}.getType();
		inputValueChecks = gson.fromJson(formHistory2.getInputValueChecks(), typeListInputValueCheck);

		if (fillExpectedData)
			fillExpectedData(kvMap, inputValueChecks);

		if (formCode.equals("balance_accounts")) {
			kvMap = validateAndNormalizeDataFormatBA(kvMap, inputValueChecks);
		} else {
			kvMap = validateAndNormalizeDataFormat(kvMap, inputValueChecks, false);
		}

		try {
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
			CalcField calcField = new CalcField(engine);
			calcField.setProvider(new form.calcfield.DataProvider(kvMap, reference, reportDate, inputValueChecks));

			CalcField2 calcField2 = new CalcField2(engine);
			calcField2.setProvider(new DataProvider2(kvMap));

//			calcField.updateCalculatedFields(formHistory.getJsCode());
			calcField2.updateCalculatedFields(formHistory.getJsCode());
		} catch (ScriptException e) {
			throw new EJBException(e.getMessage());
		} catch (IOException e) {
			throw new EJBException(e.getMessage());
		} catch (Exception e) {
			throw new EJBException(e);
		}

		return kvMap;
	}

	/**
	 * destroyAllmasks на стороне сервера: убирает знак пробела с тысячных чисел и проверяет формат числовых данных
	 * @param kvMap
	 * @param inputValueChecks
	 * @return
	 */
	@Override
	public Map<String, String> validateAndNormalizeDataFormat(Map<String, String> kvMap, List<InputValueCheck> inputValueChecks, boolean returnInvalidData) {
		Map<String, List<InputValueCheck>> inputValueCheckIndex = new HashMap<String, List<InputValueCheck>>();
		for (InputValueCheck inputValueCheck : inputValueChecks) {
			if (inputValueCheck.getRef() == null && inputValueCheck.getMask() != null
					&& (inputValueCheck.getValueType().equals("int") || inputValueCheck.getValueType().equals("float"))) {
				if (!inputValueCheckIndex.containsKey(inputValueCheck.getKey()))
					inputValueCheckIndex.put(inputValueCheck.getKey(), new ArrayList<InputValueCheck>());
				inputValueCheckIndex.get(inputValueCheck.getKey()).add(inputValueCheck);
			}
		}
		Map<String, String> newValues = new HashMap<String, String>();
		for (Map.Entry<String, String> entry : kvMap.entrySet()) {
			String finalKey;
			int dPos = entry.getKey().indexOf("$D.");
			if (dPos == -1)
				finalKey = entry.getKey();
			else {
				finalKey = entry.getKey().substring(0, dPos) + "$DynamicRowId";
			}
			if (inputValueCheckIndex.containsKey(finalKey)) {
				String groupId = "";
				if (entry.getKey().contains(".")) {
					groupId = entry.getKey().substring(0, entry.getKey().lastIndexOf(".") + 1);
				}
				InputValueCheck inputValueCheck = null;
				for (InputValueCheck ivch : inputValueCheckIndex.get(finalKey)) {
					inputValueCheck = ivch;
					if (!groupId.isEmpty() && inputValueCheck.getGroupId() != null && groupId.equals(inputValueCheck.getGroupId())) {
						break;
					}
				}
				if (inputValueCheck != null) {
					if (entry.getValue() != null && !entry.getValue().isEmpty()) {
						String value = entry.getValue();
						if (value.contains(" ")) {
							if (returnInvalidData) {
								newValues.put(entry.getKey(), value);
								continue;
							} else {
								value = entry.getValue().replace(" ", "");
							}
						}
						try {
							Double.parseDouble(value);
						} catch (NumberFormatException e) {
							if (returnInvalidData) {
								newValues.put(entry.getKey(), value);
							} else {
								logger.debug(MessageFormat.format("NumberFormatException. Key:{0} Value:{1}", entry.getKey(), value));
								throw new EJBException(MessageFormat.format("Неверный формат числа {0} для ключа {1}", entry.getValue(), entry.getKey()));
							}
						}
						if (!returnInvalidData) {
							newValues.put(entry.getKey(), value);
						}
					}
				}
			}
		}
		if (returnInvalidData) {
			return newValues;
		} else {
			kvMap.putAll(newValues);
			return kvMap;
		}
	}

	@Override
	public Map<String, String> validateAndNormalizeDataFormatBA(Map<String, String> kvMap, List<InputValueCheck> inputValueChecks) {
		Map<String, String> newValues = new HashMap<String, String>();
		for (Map.Entry<String, String> entry : kvMap.entrySet()) {
			if(entry.getKey().startsWith("balance_accounts_array*sum:code:")){
				if (entry.getValue() != null && !entry.getValue().isEmpty()) {
					String value = entry.getValue();
					if (value.contains(" ")) {
						value = entry.getValue().replace(" ", "");
					}
					try {
						Double.parseDouble(value);
					} catch (NumberFormatException e) {
						throw new EJBException(MessageFormat.format("Неверный формат числа {0} для ключа {1}", entry.getValue(), entry.getKey()));

					}
					newValues.put(entry.getKey(), value);
				}
			}
		}
		kvMap.putAll(newValues);
		return kvMap;
	}

	private Map<String, String> fillExpectedData(Map<String, String> kvMap, List<InputValueCheck> inputValueChecks) {
		if(inputValueChecks==null)
			return kvMap;

		for(InputValueCheck inputValueCheck:inputValueChecks){
			if (inputValueCheck.getMask() != null && !inputValueCheck.getMask().trim().isEmpty()
					&& inputValueCheck.getMask().startsWith("money") && !inputValueCheck.getKey().contains("$D")){
				if (!kvMap.containsKey(inputValueCheck.getKey())) {
					int mask = 6;
					try {
						mask = Integer.parseInt(inputValueCheck.getMask().substring("money".length()));
					} catch (NumberFormatException e) {
						logger.error(e.getMessage());
					}

					String pattern = "###";
					for (int i = 0; i < mask; i++) {
						if (i == 0)
							pattern += ".";
						pattern += "#";
					}
					DecimalFormat df = new DecimalFormat(pattern);
					String value = df.format(0.0);

					kvMap.put(inputValueCheck.getKey(), value);
				}
			}
		}

		return kvMap;
	}

	private List<Long> getAllReportIds(){
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Long> result = new ArrayList<Long>();
		StringBuilder sb = new StringBuilder();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT id FROM reports");
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getLong("ID"));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	// Approval

	@Override
	public List<ApprovalItem> getApprovalItems() {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ApprovalItem> result = new ArrayList<ApprovalItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT * FROM approval");
			rs = ps.executeQuery();
			while (rs.next()) {
				ApprovalItem item = getApprovalItemFromResultSet(rs);
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
	public List<ApprovalItem> getApprovalItemsByRespondent(Long respondentId, String language_code) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ApprovalItem> result = new ArrayList<ApprovalItem>();
		try {
			connection = getConnection();
			/*ps = connection.prepareStatement("SELECT a.id, a.batch_id, a.user_id, a.respondent_id, a.entity_id, a.report_date," +
                    " a.received_date, a.is_approved, a.form_name, f.name, a.APPROVAL_DATE FROM approval a, forms f WHERE a.respondent_id = ?" +
                    " AND a.form_name = f.code AND f.language_code = ?");*/
			ps = connection.prepareStatement(
					"SELECT a.id, " +
							"a.batch_id, " +
							"a.user_id, " +
							"a.respondent_id, " +
							"a.entity_id, " +
							"a.report_date," +
							"a.received_date, " +
							"a.is_approved, " +
							"a.form_name, " +
							"fh.name, " +
							"a.APPROVAL_DATE " +
							"FROM approval a, " +
							"forms f, " +
							"v_report_history_short fh " +
							"WHERE a.respondent_id = ?" +
							" AND a.form_name = f.code AND fh.language_code = ? AND f.id = fh.form_id");

			ps.setLong(1, respondentId);
			ps.setString(2, language_code);
			rs = ps.executeQuery();
			while (rs.next()) {
				//ApprovalItem item = getApprovalItemFromResultSet(rs);
				ApprovalItem item = new ApprovalItem();
				item.setId(rs.getLong("ID"));
				item.setBatchId(rs.getLong("BATCH_ID"));
				item.setUserId(rs.getLong("USER_ID"));
				item.setEntityId(rs.getLong("ENTITY_ID"));
				item.setReportDate(rs.getDate("REPORT_DATE"));
				item.setReceivedDate(rs.getDate("RECEIVED_DATE"));
				item.setIsApproved(rs.getLong("IS_APPROVED"));
				item.setFormName(rs.getString("FORM_NAME"));
				item.setTitle(rs.getString("name"));
				item.setRespondentId(rs.getLong("RESPONDENT_ID"));
				item.setApprovalDate(rs.getTimestamp("APPROVAL_DATE"));
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
	public List<ApprovalItem> getApprovalItemsByRepDateRespondent(Long respondentId, String language_code, Date reportDate) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ApprovalItem> result = new ArrayList<ApprovalItem>();
		try {
			connection = getConnection();
            /*ps = connection.prepareStatement("SELECT a.id, a.batch_id, a.user_id, a.respondent_id, a.entity_id, a.report_date," +
					" a.received_date, a.is_approved, a.form_name, f.name, a.APPROVAL_DATE FROM approval a, forms f WHERE a.respondent_id = ? AND TRUNC(a.report_date) = TRUNC(?) " +
					" and is_approved = 1 AND a.form_name = f.code AND f.language_code = ?");*/
			ps = connection.prepareStatement("SELECT a.id, " +
					"a.batch_id, " +
					"a.user_id, " +
					"a.respondent_id, " +
					"a.entity_id, " +
					"a.report_date, " +
					"a.received_date, " +
					"a.is_approved, " +
					"a.form_name, " +
					"fh.name, " +
					"a.APPROVAL_DATE " +
					"FROM approval a, " +
					"forms f, " +
					"form_history fh " +
					"WHERE a.respondent_id = ? " +
					"AND TRUNC(a.report_date) = TRUNC(?) " +
					"AND is_approved = 1 " +
					"AND a.form_name = f.code " +
					"AND fh.language_code = ? " +
					"AND f.id = fh.form_id " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate) " +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");

			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setLong(1, respondentId);
			ps.setDate(2, repDate);
			ps.setString(3, language_code);
			ps.setDate(4, repDate);
			ps.setDate(5, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				//ApprovalItem item = getApprovalItemFromResultSet(rs);
				ApprovalItem item = new ApprovalItem();
				item.setId(rs.getLong("ID"));
				item.setBatchId(rs.getLong("BATCH_ID"));
				item.setUserId(rs.getLong("USER_ID"));
				item.setEntityId(rs.getLong("ENTITY_ID"));
				item.setReportDate(rs.getDate("REPORT_DATE"));
				item.setReceivedDate(rs.getDate("RECEIVED_DATE"));
				item.setIsApproved(rs.getLong("IS_APPROVED"));
				item.setFormName(rs.getString("FORM_NAME"));
				item.setTitle(rs.getString("name"));
				item.setRespondentId(rs.getLong("RESPONDENT_ID"));
				item.setApprovalDate(rs.getTimestamp("APPROVAL_DATE"));
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
	public List<ApprovalItem> getUnApprovalItemsByRepDateRespondent(Long respondentId, String language_code, Date reportDate) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ApprovalItem> result = new ArrayList<ApprovalItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT a.id, " +
					"a.batch_id, " +
					"a.user_id, " +
					"a.respondent_id, " +
					"a.entity_id, " +
					"a.report_date, " +
					"a.received_date, " +
					"a.is_approved, " +
					"a.form_name, " +
					"fh.name, " +
					"a.APPROVAL_DATE  " +
					"FROM approval a, " +
					"forms f, " +
					"form_history fh " +
					"WHERE a.respondent_id = ? " +
					"AND TRUNC(a.report_date) = TRUNC(?) " +
					"AND is_approved = 0 " +
					"AND a.form_name = f.code " +
					"AND fh.language_code = ? " +
					"AND f.id = fh.form_id " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setLong(1, respondentId);
			ps.setDate(2, repDate);
			ps.setString(3, language_code);
			ps.setDate(4, repDate);
			ps.setDate(5, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				//ApprovalItem item = getApprovalItemFromResultSet(rs);
				ApprovalItem item = new ApprovalItem();
				item.setId(rs.getLong("ID"));
				item.setBatchId(rs.getLong("BATCH_ID"));
				item.setUserId(rs.getLong("USER_ID"));
				item.setEntityId(rs.getLong("ENTITY_ID"));
				item.setReportDate(rs.getDate("REPORT_DATE"));
				item.setReceivedDate(rs.getDate("RECEIVED_DATE"));
				item.setIsApproved(rs.getLong("IS_APPROVED"));
				item.setFormName(rs.getString("FORM_NAME"));
				item.setTitle(rs.getString("name"));
				item.setRespondentId(rs.getLong("RESPONDENT_ID"));
				item.setApprovalDate(rs.getTimestamp("APPROVAL_DATE"));
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
	public List<ApprovalItem> getApprovalItemsByUser(Long userId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ApprovalItem> result = new ArrayList<ApprovalItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT * FROM approval WHERE user_id = ?");
			ps.setLong(1, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				ApprovalItem item = getApprovalItemFromResultSet(rs);
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
	public List<ApprovalItem> getApprovalItemsByUserEntity(Long userId, Long entityId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ApprovalItem> result = new ArrayList<ApprovalItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT * FROM approval WHERE user_id = ? AND entity_id = ?");
			ps.setLong(1, userId);
			ps.setLong(2, entityId);
			rs = ps.executeQuery();
			while (rs.next()) {
				ApprovalItem item = getApprovalItemFromResultSet(rs);
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
	public List<ApprovalItem> getApprovalItemsByRepDateForm(Date reportDate, String formName) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ApprovalItem> result = new ArrayList<ApprovalItem>();
		try {
			connection = getConnection();
			System.out.println("connection:"+connection);
			ps = connection.prepareStatement("SELECT * FROM approval WHERE TRUNC(report_date) = TRUNC(?) AND form_name = ?");
			System.out.println("connection1");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setDate(1, repDate);
			ps.setString(2, formName);
			System.out.println("connection2");
			rs = ps.executeQuery();
			System.out.println("connection3");
			while (rs.next()) {
				ApprovalItem item = getApprovalItemFromResultSet(rs);
				System.out.println("item:"+item);
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
	public List<ApprovalItem> getApprovalItemsByRepDateFormResp(Date reportDate, String formName, Long respondentId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ApprovalItem> result = new ArrayList<ApprovalItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT * FROM approval WHERE TRUNC(report_date) = TRUNC(?) AND form_name = ? AND respondent_id = ?");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setDate(1, repDate);
			ps.setString(2, formName);
			ps.setLong(3, respondentId);
			rs = ps.executeQuery();
			while (rs.next()) {
				ApprovalItem item = getApprovalItemFromResultSet(rs);
				//System.out.println("item:"+item);
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
	public String getFormTitleByRepDateFormResp(Date reportDate, String formName, Long respondentId, String language_code) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String result = new String();
		try {
			connection = getConnection();
//            ps = connection.prepareStatement("SELECT * FROM forms WHERE begin_date <= ? AND (? <= end_date OR end_date IS NULL) AND code = ?");
			ps = connection.prepareStatement(
					"SELECT fh.name " +
							"FROM forms f, " +
							"form_history fh " +
							"WHERE f.id = fh.form_id " +
							"AND code = ? " +
							"AND fh.begin_date = (select max(fh1.begin_date) " +
							"from form_history fh1 " +
							"where fh1.form_id = fh.form_id " +
							"and fh1.begin_date <= ? " +
							"and (fh1.end_date is null or fh1.end_date > ?))");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setString(1, formName);
			ps.setDate(2, repDate);
			ps.setDate(3, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				result = rs.getString("name");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}
	@Override
	public List<ApprovalItem> getApprovalItemsByRepDateFormResps(Date reportDate, String formName, String resps) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ApprovalItem> result = new ArrayList<ApprovalItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT * FROM approval WHERE TRUNC(report_date) = TRUNC(?) AND form_name = ? AND respondent_id " + " in (" + resps + ")");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setDate(1, repDate);
			ps.setString(2, formName);
			//ps.setLong(3, respondentId);
			rs = ps.executeQuery();
			while (rs.next()) {
				ApprovalItem item = getApprovalItemFromResultSet(rs);
				//System.out.println("item:"+item);
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
	public List<ApprovalItem> getApprovalDistinctItemsByRepDate(Date reportDate) {
		System.out.println("getApprovalDistinctItemsByRepDate...");
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ApprovalItem> result = new ArrayList<ApprovalItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT distinct(form_name) FROM approval WHERE TRUNC(report_date) = TRUNC(?)");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setDate(1, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				ApprovalItem item = new ApprovalItem();
				item.setFormName(rs.getString("FORM_NAME"));
				//ApprovalItem item = getApprovalItemFromResultSet(rs);
				System.out.println("item:"+item);
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
	public ApprovalItem getApprovalItem(Long id) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ApprovalItem result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT * FROM approval WHERE id = ?");
			ps.setLong(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = getApprovalItemFromResultSet(rs);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public void updateApprovalItem(ApprovalItem approvalItem) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("UPDATE approval" +
					" SET batch_id = ?, user_id = ?, entity_id = ?, report_date = TRUNC(?), received_date = ?, is_approved = ?, form_name = ? WHERE id = ?");
			ps.setLong(1, approvalItem.getBatchId());
			ps.setLong(2, approvalItem.getUserId());
			ps.setLong(3, approvalItem.getEntityId());
			ps.setDate(4, new java.sql.Date(approvalItem.getReportDate().getTime()));
			ps.setTimestamp(5, new java.sql.Timestamp(approvalItem.getReceivedDate().getTime()));
			ps.setLong(6, approvalItem.getIsApproved());
			ps.setString(7, approvalItem.getFormName());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void deleteApprovalItem(Long id) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("DELETE FROM approval WHERE id = ?");
			ps.setLong(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Deleting an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	private ApprovalItem getApprovalItemFromResultSet(ResultSet rs) throws SQLException {
		ApprovalItem item = new ApprovalItem();
		item.setId(rs.getLong("ID"));
		item.setBatchId(rs.getLong("BATCH_ID"));
		item.setUserId(rs.getLong("USER_ID"));
		item.setEntityId(rs.getLong("ENTITY_ID"));
		item.setReportDate(rs.getDate("REPORT_DATE"));
		item.setReceivedDate(rs.getDate("RECEIVED_DATE"));
		item.setIsApproved(rs.getLong("IS_APPROVED"));
		item.setFormName(rs.getString("FORM_NAME"));
		item.setRespondentId(rs.getLong("RESPONDENT_ID"));
		item.setApprovalDate(rs.getTimestamp("APPROVAL_DATE"));
		return item;
	}

	private OutDataItem getOutDataItemFromResultSet(ResultSet rs) throws SQLException {
		OutDataItem item = new OutDataItem();
		item.setId(rs.getLong("ID"));
		item.setUserId(rs.getLong("USER_ID"));
		item.setReportDate(rs.getDate("REPORT_DATE"));
		item.setReceivedDate(rs.getDate("RECEIVED_DATE"));
		item.setIsApproved(rs.getLong("IS_APPROVED"));
		item.setFormName(rs.getString("FORM_NAME"));
		item.setCouchbase_id(rs.getLong("COUCHBASE_ID"));
		return item;
	}


	// Report data

	@Override
	public Map<String, String> getKvMap(Long reportId) {
		Map<String, String> result = null;
		if (reportId == null) return result;

		Report report = getReport(reportId, null);
		if (report == null) return result;

		/*List<ReportHistory> reportHistory = getReportHistoryByReportIdNoLobs(reportId);
		if (reportHistory.isEmpty()) return result;

		Long lastReportHistoryItemId = reportHistory.get(reportHistory.size()-1).getId();*/

		Long lastReportHistoryId = getLastReportHistoryIdByReportId(report.getId(), false, null);
		ReportHistory lastReportHistoryItem = getReportHistory(lastReportHistoryId, true, false);
		if (lastReportHistoryItem == null) return result;

		String jsonData = lastReportHistoryItem.getData();
		if (jsonData == null || jsonData.isEmpty()) return result;

		Type typeMapStringString = new TypeToken<Map<String, String>>() {}.getType();
		result = gson.fromJson(jsonData, typeMapStringString);

		return result;
	}

	@Override
	public String getHtmlWithInitialData(Form form, RefRespondentItem respondent, Date reportDate, boolean forView) {
		String formCode = form.getCode();
		String html = forView ? getForm(form.getId(),reportDate).getFormHistory().getHtmlView() : getForm(form.getId(),reportDate).getFormHistory().getHtml();
		JAXBContext jaxbContext;
		Marshaller marshaller;
		Unmarshaller unmarshaller;
		StringBuilder sb;
		String openTag, closeTag;
		int pos, startPos, endPos;
		List<String> rateACurrencies = null;
		try {
			// Processing <span> tags
			jaxbContext = JAXBContext.newInstance(HtmlSpan.class);
			marshaller = jaxbContext.createMarshaller();
			unmarshaller = jaxbContext.createUnmarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
			sb = new StringBuilder();
			openTag = "<span id=\"" + formCode;
			closeTag = "</span>";
			pos = 0;
			while (pos < html.length()) {
				startPos = html.indexOf(openTag, pos);
				if (startPos >= 0) {
					sb.append(html.substring(pos, startPos));
					endPos = html.indexOf(closeTag, startPos);
					if (endPos >= 0) {
						pos = endPos + closeTag.length();
					} else break;
					if (endPos >= 0) {
						String srcHtmlTag = html.substring(startPos, pos);
						InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
						HtmlSpan htmlSpan = (HtmlSpan) unmarshaller.unmarshal(is);
						String[] nameParts = htmlSpan.id.split("\\*");
						/*
						String containerName = nameParts[0];
						if (containerName.contains(":")) {
							containerName = containerName.replace(":", "_") + "_item"; // not used with spans
						}
						String key = containerName + "*" + nameParts[1];
						*/
						if (nameParts[1].contains("$ReportDate")) {
							htmlSpan.content = Convert.getDateStringFromDate(reportDate);
						}
						else if (nameParts[1].contains("$ReportYear")) {
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(reportDate);
							calendar.set(Calendar.DAY_OF_YEAR, 1);
							calendar.set(Calendar.MONTH, 1);
							htmlSpan.content = Convert.getDateStringFromDate(calendar.getTime());
						}
						else if (nameParts[1].contains("$RespondentInfo")) {
							if (respondent != null)
								htmlSpan.content = respondent.getNameRu();
							else {
								if (form.getTypeCode().equalsIgnoreCase(FormType.INPUT.name()))
									htmlSpan.content = "null";
								else
									htmlSpan.content = "";
							}
						}
						else if (nameParts[1].contains("ceo")) {
							htmlSpan.content = "ФИО руководителя";
						}
						else if (nameParts[1].contains("chief_accountant")) {
							htmlSpan.content = "ФИО гл. бухгалтера";
						}
						else if (nameParts[1].contains("drafted_by")) {
							htmlSpan.content = "ФИО исполнителя";
						}
						else if (nameParts[1].contains("signature_date")) {
							htmlSpan.content = "дд.мм.гггг";
						}
						if (htmlSpan.content != null && htmlSpan.content.equalsIgnoreCase("$reportdate")) {
							htmlSpan.content = Convert.dateFormatRus.format(reportDate);
						} else if (htmlSpan.content != null && htmlSpan.content.toLowerCase().startsWith("$reportdate(")) {
							if (!htmlSpan.content.endsWith(")"))
								throw new EJBException(MessageFormat.format("Ошибка при разборе выражения {0}", htmlSpan.content));
							else {
								try {
									Date d = dateEval.eval(htmlSpan.content.substring(htmlSpan.content.indexOf("(") + 1, htmlSpan.content.lastIndexOf(")")), reportDate);
									htmlSpan.content = Convert.dateFormatRus.format(d);
								} catch (FormulaSyntaxError ex) {
									throw new EJBException(MessageFormat.format("Ошибка при разборе выражения {0}", htmlSpan.content));
								}
							}
						}
						StringWriter stringWriter = new StringWriter();
						marshaller.marshal(htmlSpan, stringWriter);
						String dstHtmlTag = stringWriter.toString();
						sb.append(dstHtmlTag);
					}
				} else {
					sb.append(html.substring(pos));
					break;
				}
			}
			html = sb.toString();

			// Processing <select> tags
			jaxbContext = JAXBContext.newInstance(HtmlSelect.class);
			marshaller = jaxbContext.createMarshaller();
			unmarshaller = jaxbContext.createUnmarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
			sb = new StringBuilder();
			openTag = "<select id=\"" + formCode;
			closeTag = "</select>";
			pos = 0;
			while (pos < html.length()) {
				startPos = html.indexOf(openTag, pos);
				if (startPos >= 0) {
					sb.append(html.substring(pos, startPos));
					endPos = html.indexOf(closeTag, startPos);
					if (endPos >= 0) {
						pos = endPos + closeTag.length();
					} else break;
					if (endPos >= 0) {
						String srcHtmlTag = html.substring(startPos, pos);
						InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
						HtmlSelect htmlSelect = (HtmlSelect) unmarshaller.unmarshal(is);

						if (htmlSelect.options != null && htmlSelect.options.size() > 0 && htmlSelect.options.get(0).value.startsWith("$:")) {
							String[] refParts = htmlSelect.options.get(0).value.split(":");
							String ref = refParts[1];
							String refCode = refParts[2];
							String refCaption = refParts[3];

							List<? extends AbstractReference> refs;
							refs = reference.getReferenceItemsByName(ref, refCaption, reportDate);
							htmlSelect.options = new ArrayList<HtmlOption>();
							HtmlOption emptyOption = new HtmlOption();
							emptyOption.value = "";
							emptyOption.content = "";
							htmlSelect.options.add(emptyOption);
							for (AbstractReference reference : refs) {
								HtmlOption option = new HtmlOption();
								option.value = reference.getRecId().toString();
								if (refCaption.equals("name_ru"))
									option.content = reference.getNameRu();
								else
									option.content = reference.getCode();
								htmlSelect.options.add(option);
							}

							// если не найдено значение при загрузке excel
							SimpleReference refetence = new SimpleReference();
							refetence.setRecId(-1L);
							HtmlOption option = new HtmlOption();
							option.value = refetence.getRecId().toString();
							option.content = "не найден в справочнике";
							htmlSelect.options.add(option);

							StringWriter stringWriter = new StringWriter();
							marshaller.marshal(htmlSelect, stringWriter);
							String dstHtmlTag = stringWriter.toString();
							sb.append(dstHtmlTag);
						}
					}
				} else {
					sb.append(html.substring(pos));
					break;
				}
			}
			html = sb.toString();

			// Processing <DynamicFunction> tags
			jaxbContext = JAXBContext.newInstance(DynamicFunction.class);
			marshaller = jaxbContext.createMarshaller();
			unmarshaller = jaxbContext.createUnmarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
			sb = new StringBuilder();
			openTag = "<DynamicFunction";
			closeTag = "/>";
			pos = 0;
			while (pos < html.length()) {
				startPos = html.indexOf(openTag, pos);
				if (startPos >= 0) {
					sb.append(html.substring(pos, startPos));
					endPos = html.indexOf(closeTag, startPos);
					if (endPos >= 0) {
						pos = endPos + closeTag.length();
					} else break;
					if (endPos >= 0) {
						String srcTag = html.substring(startPos, pos);
						InputStream is = new ByteArrayInputStream(srcTag.getBytes(StandardCharsets.UTF_8));
						DynamicFunction dynamicFunction = (DynamicFunction) unmarshaller.unmarshal(is);
						StringBuilder sbFunction = new StringBuilder();

						if (dynamicFunction.name.equals("hasRateA")) {
							sbFunction.append("function hasRateA(currencyId) { ");
							sbFunction.append("var rateA = [");

							if (rateACurrencies == null) rateACurrencies = reference.getRateACurrencyRecIds(reportDate);
							if (rateACurrencies != null && !rateACurrencies.isEmpty()) {
								if (!rateACurrencies.isEmpty()) {
									sbFunction.append("'" + rateACurrencies.get(0) + "'");
									for (int i = 1; i < rateACurrencies.size(); i++)
										sbFunction.append(",'" + rateACurrencies.get(i) + "'");
								}
							}

							sbFunction.append("];");
							sbFunction.append(" return rateA.indexOf(currencyId) >= 0; }");
						}
						sb.append(sbFunction.toString());
					}
				} else {
					sb.append(html.substring(pos));
					break;
				}
			}
			html = sb.toString();

		} catch (JAXBException e) {
			throw new EJBException(e);
		}
		return html;
	}

	@Override
	public String getHtmlWithoutDynamicRowTemplates(String html) {
		final String FINISH_TAG = "<style";
		StringBuilder sb = new StringBuilder();
		int pos, startPos, endPos, finishPos, dynPos;
		String openTag = "<tr";
		String closeTag = "</tr>";
		String dynTag = "$DynamicRowIdMinor";
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();
		while (pos < finishPos) {
			dynPos = html.indexOf(dynTag, pos);
			if (dynPos < 0 || dynPos > finishPos) break;

			startPos = html.lastIndexOf(openTag, dynPos);
			endPos = html.indexOf(closeTag, dynPos);

			if (startPos < 0 || endPos < 0) break;
			sb.append(html.substring(pos, startPos));
			pos = endPos + closeTag.length();

			//String srcHtmlTag = html.substring(startPos, pos);
		}
		sb.append(html.substring(pos));
		return sb.toString();
	}

	@Override
	public String getHtmlWithReportData(Long reportId, boolean forView, boolean forSuperUser) {
		String result = null;
		if (reportId == null) return result;

		Long lastReportHistoryItemId = getLastReportHistoryIdByReportId(reportId, forSuperUser, null);
		if(lastReportHistoryItemId==null || lastReportHistoryItemId==0){
			return result;
		}

		result = getHtmlWithReportHistoryData(lastReportHistoryItemId, forView);
		return result;
	}

	@Override
	public String getHtmlWithReportHistoryData(long reportHistoryId, boolean forView) {
		String result = null;
		if (reportHistoryId == 0) return result;

		ReportHistory reportHistoryItem = getReportHistory(reportHistoryId, true, false);
		if (reportHistoryItem == null) return result;
		long reportId = getReportIdByReportHistoryId(reportHistoryId, null);
		if(reportId == 0) return result;
		Report report = getReport(reportId, null);
		if (report == null) return result;

		String jsonData = reportHistoryItem.getData();
		if (jsonData == null || jsonData.isEmpty()) return result;

		Type typeMapStringString = new TypeToken<Map<String, String>>() {}.getType();
		Map<String, String> kvMap = gson.fromJson(jsonData, typeMapStringString);

		Form form = getForm(getFormId(report.getFormCode(), report.getReportDate()), report.getReportDate());
		updateSignDate(reportHistoryId, new Date(), form.getFormHistory().getInputValueChecks(), kvMap, null);

		/*Реквизиты респондента для формирования черновика в сводных и выходных формах, не нужны*/
		RefRespondentItem respondent = null;
		if(!report.getIdn().substring(0,5).equals("DRAFT")){
			respondent = reference.getRespondentByIdn(report.getIdnChild() == null ? report.getIdn() : report.getIdnChild(), report.getReportDate());
			if (respondent == null) {
				StringBuilder sb = new StringBuilder();
				sb.append("Не найден респондент за {0}. Причиной этого могут быть:<br/>")
						.append("a) за данную дату не найден респондент;</br>")
						.append("б) произашел сбой в сети. В этом случае необходимо выйти из сессии и зайти заново.");
				return getFormErrorMessage(MessageFormat.format(sb.toString(), Convert.getDateStringFromDate(report.getReportDate())));
			}
		}
		List<Form> forms = getFormsByCodeLanguageReportDate(report.getFormCode(), "ru", report.getReportDate(), null);
		if (forms.isEmpty()) return result;

		String htmlWithInitialData = getHtmlWithInitialData(forms.get(0), respondent, report.getReportDate(), forView);
		if (report.getFormCode().equals("balance_accounts")) {
			BalanceAccountsFormHtml dataFormHtml = new BalanceAccountsFormHtml(htmlWithInitialData, kvMap, reference, report.getReportDate());
			return dataFormHtml.getHtml();
		} else {
			DataFormHtml dataFormHtml = new DataFormHtml(htmlWithInitialData, kvMap, reference, report.getReportDate());
			return dataFormHtml.getHtml();
		}
	}

	private String getFormErrorMessage(String msg){
		return "<span style=\"color:red\">" + msg + "</span>";
	}

	@Override
	public byte[] downloadTemplateExcel(Long formHistoryId, Boolean in) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		byte[] xlsFile = null;
		try {
			connection = getConnection();
			String sqlText;
			if(in){
				sqlText = "select xls ";
			}else{
				sqlText = "select xls_out ";
			}
			sqlText = sqlText + " from form_history where id = ?";

			ps = connection.prepareStatement(sqlText);
			ps.setLong(1, formHistoryId);
			rs = ps.executeQuery();
			if (rs.next()) {
				xlsFile = rs.getBytes(1);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return xlsFile;
	}

	@Override
	public void putReportProp(Long reportHistoryId, String propKey, String propValue) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("merge into report_props p " +
					"using (" +
					"    select" +
					"    ? report_history_id, " +
					"    ? prop_key, " +
					"    ? prop_value " +
					"    from " +
					"    dual " +
					") val on ( " +
					"    p.report_history_id=val.report_history_id " +
					"    and p.prop_key=val.prop_key " +
					") " +
					"when matched then " +
					"    update set p.prop_value = val.prop_value " +
					"when not matched then " +
					"    insert (report_history_id, prop_key, prop_value) values (val.report_history_id, val.prop_key, val.prop_value)");
			ps.setLong(1, reportHistoryId);
			ps.setString(2, propKey);
			ps.setString(3, propValue);

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void putReportProps(Long reportHistoryId, Map<String, String> props) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			for(Map.Entry<String, String> entry:props.entrySet()) {
				ps = connection.prepareStatement("merge into report_props p " +
						"using (" +
						"    select" +
						"    ? report_history_id, " +
						"    ? prop_key, " +
						"    ? prop_value " +
						"    from " +
						"    dual " +
						") val on ( " +
						"    p.report_history_id=val.report_history_id " +
						"    and p.prop_key=val.prop_key " +
						") " +
						"when matched then " +
						"    update set p.prop_value = val.prop_value " +
						"when not matched then " +
						"    insert (report_history_id, prop_key, prop_value) values (val.report_history_id, val.prop_key, val.prop_value)");
				ps.setLong(1, reportHistoryId);
				ps.setString(2, entry.getKey());
				ps.setString(3, entry.getValue());

				int affectedRows = ps.executeUpdate();
				if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public String getReportPropValue(Long reportHistoryId, String propKey) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select * from report_props p where p.report_history_id=? and p.prop_key=?");
			ps.setLong(1, reportHistoryId);
			ps.setString(2, propKey);
			rs = ps.executeQuery();
			while (rs.next()) {
				return rs.getString("prop_value");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return null;
	}

	// OutData

	@Override
	public void insertOutData(OutDataItem item) {
		Connection connection = null;
		CallableStatement stmt = null;
		OracleCallableStatement ocs = null;
		Form result = null;
		try {
			connection = getConnection();
			stmt = connection.prepareCall("{ call Insert_OutData(?,?,?,?,?,?,?,?,?)}");
			ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);

			oracle.jdbc.OracleDriver ora = new oracle.jdbc.OracleDriver();
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			oracle.sql.ARRAY array = oraConn.createARRAY("RESPS_ARRAY", item.getRespondentsName());
			java.sql.Date repDate = new java.sql.Date(item.getReportDate().getTime());
			java.sql.Timestamp recDate = new java.sql.Timestamp(item.getReceivedDate().getTime());
			ocs.setArray(1, array);
			ocs.setLong(2, item.getUserId());
			ocs.setDate(3, repDate);
			ocs.setTimestamp(4, recDate);
			ocs.setLong(5, item.getIsApproved());
			ocs.setString(6, item.getFormName());
			ocs.setLong(7, item.getCouchbase_id());
			ocs.registerOutParameter(8, OracleTypes.NUMBER);
			ocs.registerOutParameter(9, OracleTypes.VARCHAR);
			ocs.execute();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
		//return result;
	}

	@Override
	public List<OutDataItem> getOutDataItemsByRepDate(Date reportDate, String language_code, int is_approved) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<OutDataItem> result = new ArrayList<OutDataItem>();
		try {
			connection = getConnection();
            /*ps = connection.prepareStatement("SELECT * FROM out_data o, forms f WHERE TRUNC(o.report_date) = TRUNC(?)" +
                    " AND is_approved = ? AND o.form_name = f.code and f.language_code = ?");*/
			ps = connection.prepareStatement("SELECT o.id, " +
					"o.user_id, " +
					"o.report_date, " +
					"o.received_date, " +
					"o.is_approved, " +
					"o.form_name," +
					"fh.name, " +
					"o.note, " +
					"o.couchbase_id, " +
					"o.approval_date " +
					"FROM out_data o, " +
					"forms f, " +
					"form_history fh " +
					"WHERE TRUNC(o.report_date) = TRUNC(?) " +
					"AND o.is_approved = ? " +
					"AND o.form_name = f.code " +
					"AND fh.language_code = ? " +
					"AND f.id = fh.form_id " +
					"AND fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh1.form_id = fh.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate) " +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setDate(1, repDate);
			ps.setInt(2, is_approved);
			ps.setString(3, language_code);
			ps.setDate(4, repDate);
			ps.setDate(5, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				//OutDataItem item = getOutDataItemFromResultSet(rs);
				OutDataItem item = new OutDataItem();
				item.setId(rs.getLong("ID"));
				item.setUserId(rs.getLong("USER_ID"));
				item.setReportDate(rs.getDate("REPORT_DATE"));
				item.setReceivedDate(rs.getDate("RECEIVED_DATE"));
				item.setIsApproved(rs.getLong("IS_APPROVED"));
				item.setFormName(rs.getString("FORM_NAME"));
				item.setTitle(rs.getString("NAME"));
				item.setNote(rs.getString("NOTE"));
				item.setCouchbase_id(rs.getLong("COUCHBASE_ID"));
				item.setApprovalDate(rs.getTimestamp("APPROVAL_DATE"));
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
	public List<RefRespondentItem> getOutDataRespItemsByOutDataId(Long outDataId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<RefRespondentItem> result = new ArrayList<RefRespondentItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT * FROM out_data_resps o WHERE o.out_data_id = ?");
			ps.setLong(1, outDataId);
			rs = ps.executeQuery();
			while (rs.next()) {
				//OutDataItem item = getOutDataItemFromResultSet(rs);
				RefRespondentItem item = new RefRespondentItem();
				item.setNameRu(rs.getString("RESPNAME"));
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
	public void updateOutDataItem(Long id, boolean approved) {
		Connection connection = null;
		PreparedStatement ps = null;
		Timestamp dateApproval = new Timestamp(new Date().getTime());
		try {
			connection = getConnection();
			ps = connection.prepareStatement("UPDATE out_data SET is_approved = ?, APPROVAL_DATE = ? WHERE id = ?");
			ps.setLong(1, approved ? 1L : 0L);
			ps.setTimestamp(2, dateApproval);
			ps.setLong(3, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public int updateOutDataItemNote(Long id, String note) {
		Connection connection = null;
		PreparedStatement ps = null;
		int affectedRows = 0;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("UPDATE out_data SET note = ? WHERE id = ?");
			ps.setString(1, note);
			ps.setLong(2, id);
			affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
		return affectedRows;
	}

	@Override
	public void deleteOutDataItem(Long id) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("DELETE FROM out_data WHERE id = ?");
			ps.setLong(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Deleting an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	private void deleteInputReports(Long outputReportId, Connection connection) throws SQLException{
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("DELETE FROM input_reports WHERE out_report_id = ?");
			ps.setLong(1, outputReportId);
			ps.executeUpdate();
		} finally {
			DbUtil.closeStatement(ps);
		}
	}

	@Override
	public List<ReportListItem> getFilteredOutputReportList(long userId, List<String> formCodes, List<String> idnList,
															List<RefNpaItem> filterNpa, Date reportDateBegin, Date reportDateEnd, String languageCode) {

		if (formCodes.size() == 0)
			return new ArrayList<ReportListItem>();

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ReportListItem> result = new ArrayList<ReportListItem>();
		List<Report> reports = new ArrayList<Report>();

		StringBuilder sbFormCodeList = new StringBuilder();
		for (int i = 0; i < formCodes.size(); i++) {
			if (i > 0)
				sbFormCodeList.append(",");
			sbFormCodeList.append("'").append(formCodes.get(i)).append("'");
		}

		try {
			connection = getConnection();
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);

			String[] arrIdn = idnList.toArray(new String[idnList.size()]);
			java.sql.Array arrayIdn = oraConn.createARRAY("FORM_CODE_ARRAY", arrIdn);

			String sqlText =
					"SELECT r.*, " +
					"       st.status_code, " +
					"		st.status_date, " +
					"		h.CONTROL_RESULT_CODE2, " +
					" 		npa.name_ru as name_npa "+
					" FROM  reports r " +
					" 		inner join F_SESSION_FORMS sf on r.form_code = sf.FORM_NAME and sf.user_id = ? " +
					" 		INNER JOIN REPORT_HISTORY h ON h.ID=(SELECT max(h2.ID) FROM REPORT_HISTORY h2 WHERE h2.REPORT_ID=r.ID)\n" +
					" 		INNER JOIN REPORT_HISTORY_STATUSES st on st.ID=(SELECT max(st2.ID) FROM REPORT_HISTORY_STATUSES st2 WHERE st2.REPORT_HISTORY_ID=h.ID)" +
					" 		inner join forms f on f.code = r.form_code"+
					" 		inner join form_history fh on fh.form_id = f.id and fh.begin_date <= r.report_date and (fh.end_date is null or fh.end_date > r.report_date)" +
					"		left join npa_forms nf on nf.form_history_id = fh.id " +
					"		left join v_ref_npa npa \n" +
					"        		on npa.rec_id = nf.npa_rec_id \n" +
					"       	   and npa.begin_date = (select max(npa1.begin_date) \n" +
					"                               	   from v_ref_npa npa1\n" +
					"                                     where npa1.rec_id = npa.rec_id\n" +
					"                                       and npa1.begin_date <= r.report_date)" +
					" WHERE r.form_code in ("+sbFormCodeList.toString()+") " +
					"	AND TRUNC(r.report_date) BETWEEN NVL(TRUNC(?),TRUNC(r.report_date)) AND NVL(TRUNC(?),TRUNC(r.report_date))" +
					"	AND st.status_code in (select substr(ri.name, instr(ri.name, ':')+1) as status_name " +
					"  							 from right_items ri," +
					"       					  	  f_session_right_items fri " +
					" 							where ri.parent = (select id " +
					"                     							from right_items t " +
					"                    							where t.name = 'OUT_REP_STAT') " +
					"   							and ri.id = fri.right_item_id " +
					"   							and fri.user_id = ?)" +
					"	and (select count(*) \n" +
					"          from input_reports ir \n" +
					"         where ir.out_report_id=r.id)=(select count(distinct ir.in_report_id) \n" +
					"          from input_reports ir \n" +
					"               inner join reports r2 on ir.in_report_id=r2.id                \n" +
					"               inner join V_REF_UNIONPERSONS up on (up.IDN = r2.IDN AND up.BEGIN_DATE=(select max(up2.BEGIN_DATE) from V_REF_UNIONPERSONS up2 WHERE up2.REC_ID=up.REC_ID AND up2.TYPE=up.TYPE AND up.BEGIN_DATE<=r2.REPORT_DATE AND (up.END_DATE IS NULL OR up.END_DATE>r2.REPORT_DATE)))\n" +
					"               inner join v_ref_respondent re on (re.REF_UNIONPERSONS=up.id AND re.begin_date=(select max(re2.begin_date) \n" +
					"                                                                                                 from v_ref_respondent re2 \n" +
					"                                                                                                 where re2.rec_id=re.rec_id " +
					"																                            		and re2.begin_date<=r2.report_date) " +
					"																					 and (re.end_date is null or re.end_date > r2.report_date))\n" +
					"                     inner join f_session_creditors c on c.creditor_id=re.rec_id and c.user_id=?\n" +
					"                where ir.out_report_id=r.id)\n" +
					"            and exists (select 'x' \n" +
					"                from input_reports ir2 \n" +
					"                     inner join reports r3 on ir2.in_report_id=r3.id                \n" +
					"					  inner join table(?) i on r3.idn=to_char(i.column_value) \n" +
					"                where ir2.out_report_id=r.id )";

			if(filterNpa != null && filterNpa.size() > 0) {
				StringBuilder sbNpa = new StringBuilder();
				Boolean noNpa = false;
				for (int i = 0; i < filterNpa.size(); i++) {
					if(filterNpa.get(i).getId() == 0)
						noNpa = true;
					if (i > 0)
						sbNpa.append(",");
					sbNpa.append(filterNpa.get(i).getRecId());
				}
				if(noNpa) {
					sqlText = sqlText + " and (nf.npa_rec_id is null or nf.npa_rec_id in (" + sbNpa.toString() + "))";
				}else {
					sqlText = sqlText + " and nf.npa_rec_id in (" + sbNpa.toString() + ")";
				}
			}

			ps = connection.prepareStatement(sqlText);
			//ps.setString(1, idn); // Doesn't work because JDBC doesn't wrap a value in quotes if a string value contains only numeric characters.
			ps.setLong(1, userId);
			ps.setDate(2, reportDateBegin == null ? null : new java.sql.Date(reportDateBegin.getTime()));
			ps.setDate(3, reportDateEnd == null ? null : new java.sql.Date(reportDateEnd.getTime()));
			ps.setLong(4, userId);
			ps.setLong(5, userId);
			ps.setArray(6, arrayIdn);
			rs = ps.executeQuery();
			while (rs.next()) {
				Report report = getReportFromResultSet(rs);

				List<ReportHistory> reportHistory = getReportHistoryByReportNoLobs(report, connection);

				ReportListItem item = new ReportListItem();
				item.setId(report.getId());
				item.setIdn(report.getIdn());
				item.setIdnChild(report.getIdnChild());
				item.setReportDate(report.getReportDate());
				item.setFormCode(report.getFormCode());
				List<Form> forms = getFormsByCodeLanguageReportDate(report.getFormCode(), languageCode, report.getReportDate(), connection);
				if (!forms.isEmpty()) {
					item.setFormName(forms.get(0).getFormHistory().getName());
					item.setFormTypeCode(forms.get(0).getTypeCode());
					item.setFormTypeName(forms.get(0).getTypeName(languageCode));
					item.setCanAttachedFile(forms.get(0).getFormHistory().getFormTag() == null ? false : forms.get(0).getFormHistory().getFormTag().canAttachedFile);
				}
				ReportHistory lastReport = reportHistory.isEmpty() ? null : reportHistory.get(reportHistory.size()-1);
				if (lastReport != null) {
					item.setSaveDate(lastReport.getSaveDate());
					item.setUserInfo(lastReport.getUserInfo());
					item.setDeliveryWay(lastReport.getDeliveryWayCode());
					item.setHaveNote(lastReport.getNote() != null ? true : false);
				}
				item.setStatus(rs.getString("STATUS_CODE"));
				item.setStatusDate(rs.getDate("STATUS_DATE"));
				item.setControlResultCode(rs.getString("CONTROL_RESULT_CODE2"));
				item.setNameNPA(rs.getString("NAME_NPA"));
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
	public void updateOutputReport(Long id, boolean approved, AbstractUser user, Date approvalDate, AuditEvent auditEvent) throws Exception{
		String statusCode = approved ? ReportStatus.Status.APPROVED.name() : ReportStatus.Status.COMPLETED.name();
		ReportStatus oldStatus = getLastReportStatusByReportId(id, true, null);
		if (oldStatus.getStatusCode().equals(ReportStatus.Status.DRAFT.toString()))
			throw new Exception(statusCode.equals(ReportStatus.Status.APPROVED.name()) ? "Нельзя утверждать черновик" : "Нельзя разутвердить черновик");
		if (statusCode.equals(oldStatus.getStatusCode()))
			throw new Exception(statusCode.equals(ReportStatus.Status.APPROVED.name()) ? "Уже утвержден" : "Уже разутвержден");

		ReportStatus status = new ReportStatus();
		status.setReportHistory(getLastReportHistoryByReportIdNoLobs(id, true, null));
		status.setStatusCode(statusCode);
		status.setStatusDate(approvalDate);
		status.setMessage((approved ? "Утвердил(а): " : "Разутвердил(а): ") + user.getDisplayName() + " [" + user.getLocation() + "]");
		status.setUserId(user.getId());
		status.setUserInfo(user.getDisplayName());
		status.setUserLocation(user.getLocation());

		Connection connection = null;
		try {
			connection = getConnection();
			insertReportStatusHistory(status, connection, auditEvent);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
	}

	@Override
	public String getHtmlWithOutputReportData(Long reportId, boolean forView) {
		String result = null;
		if (reportId == null) return result;

		Report report = getReport(reportId, null);
		if (report == null) return result;

		List<ReportHistory> reportHistory = getReportHistoryByReportIdNoLobs(reportId, null);
		if (reportHistory.isEmpty()) return result;

		Long lastReportHistoryItemId = reportHistory.get(reportHistory.size()-1).getId();
		ReportHistory lastReportHistoryItem = getReportHistory(lastReportHistoryItemId, true, false);
		if (lastReportHistoryItem == null) return result;

		String jsonData = lastReportHistoryItem.getData();
		if (jsonData == null || jsonData.isEmpty()) return result;

		Type typeMapStringString = new TypeToken<Map<String, String>>() {}.getType();
		Map<String, String> kvMap = gson.fromJson(jsonData, typeMapStringString);

		List<Form> forms = getFormsByCodeLanguageReportDate(report.getFormCode(), "ru", report.getReportDate(), null);
		if (forms.isEmpty()) return result;

		RefRespondentItem respondent = null;
		List<RefRespondentItem> respondents = getInputReportRespondentsByOutputReportId(reportId);
		if (respondents.size() == 1) {
			respondent = respondents.get(0);
		}

		String htmlWithInitialData = getHtmlWithInitialData(forms.get(0), respondent, report.getReportDate(), forView);
		if (report.getFormCode().equals("balance_accounts_out")) {
			BalanceAccountsOutFormHtml dataFormHtml = new BalanceAccountsOutFormHtml(htmlWithInitialData, kvMap, reference, report.getReportDate());
			return dataFormHtml.getHtml();
		} else {
			Date startDate  = null, endDate = null;
			PeriodType periodType = null;
			if (forms.get(0).getFormHistory().getPeriodCount() > 0) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
				String startDateStr = getReportPropValue(lastReportHistoryItemId, ReportProps.START_PERIOD_KEY);
				String endDateStr = getReportPropValue(lastReportHistoryItemId, ReportProps.END_PERIOD_KEY);
				String periodTypeStr = getReportPropValue(lastReportHistoryItemId, ReportProps.PERIOD_DURATION_KEY);
				if (startDateStr != null) {
					try {
						startDate = dateFormat.parse(startDateStr);
					} catch (ParseException e) {
						logger.error(e.getMessage());
					}
				}
				if (endDateStr != null) {
					try {
						endDate = dateFormat.parse(endDateStr);
					} catch (ParseException e) {
						logger.error(e.getMessage());
					}
				}
				if (periodTypeStr != null) {
					periodType = PeriodUtil.getPeriodTypeByName(periodTypeStr);
				}
			}
			DataFormHtml dataFormHtml = new DataFormHtml(htmlWithInitialData, kvMap, reference, report.getReportDate(), startDate, endDate, periodType);
			return dataFormHtml.getHtml();
		}
	}

	@Override
	public List<InputReportListItem> getInputReportsByOutputReportId(Long outputReportId, String languageCode) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<InputReportListItem> result = new ArrayList<InputReportListItem>();
		try {
			connection = getConnection();
			/*ps = connection.prepareStatement("select r.*, l.name_ru as resp_name_ru, l.name_kz as resp_name_kz, l.name_en as resp_name_en, f.name as form_name, s.name_ru as subjecttype_name_ru  " +
					" from reports r inner join input_reports ir on r.id=ir.in_report_id " +
					" inner join forms f on r.form_code=f.code and LOWER(language_code) = ? AND begin_date <= r.report_date AND (r.report_date <= f.end_date OR f.end_date IS NULL) " +
					" inner join ref_legal_person l on r.idn=l.idn and l.begin_date = (select max(ps.begin_date) " +
					"                               from ref_legal_person ps " +
					"                              where ps.rec_id = l.rec_id " +
					"                                and ps.begin_date <= r.report_date) " +
					"   inner join ref_subject_type s on l.ref_subject_type=s.id and s.begin_date = (select max(t.begin_date) " +
					"                                                   from ref_subject_type t " +
					"                                                 where t.rec_id = s.rec_id " +
					"                                                    and t.begin_date <= sysdate) " +
					" where ir.out_report_id=? ");*/
			ps = connection.prepareStatement("SELECT\n" +
					"  r.*,\n" +
					"  up.name_ru                        AS resp_name_ru,\n" +
					"  nvl(up.short_name_ru, up.name_ru) AS resp_short_name_ru,\n" +
					"  fh.name                           AS form_name,\n" +
					"  s.name_ru                         AS subjecttype_name_ru\n" +
					"FROM reports r,\n" +
					"  input_reports ir,\n" +
					"  forms f,\n" +
					"  form_history fh,\n" +
					"  V_REF_UNIONPERSONS up,\n" +
					"  v_ref_subject_type s,\n" +
					"  v_ref_respondent re\n" +
					"WHERE r.id = ir.in_report_id\n" +
					"      AND ir.out_report_id = ?\n" +
					"      AND r.form_code = f.code\n" +
					"      AND up.id = re.REF_UNIONPERSONS\n" +
					"      AND f.id = fh.form_id\n" +
					"      AND lower(fh.language_code) = ?\n" +
					"      AND fh.begin_date = (SELECT max(fh1.begin_date)\n" +
					"                           FROM form_history fh1\n" +
					"                           WHERE fh1.form_id = fh.form_id\n" +
					"                                 AND fh1.begin_date <= r.report_date\n" +
					"                                 AND (fh1.end_date IS NULL OR fh1.end_date > r.report_date))\n" +
					"      AND r.idn = up.idn\n" +
					"      AND up.begin_date = (SELECT max(up1.begin_date)\n" +
					"                           FROM V_REF_UNIONPERSONS up1\n" +
					"                           WHERE up.rec_id = up1.rec_id AND up.TYPE = up1.TYPE\n" +
					"                                 AND up1.begin_date <= r.report_date)\n" +
					"      AND (up.end_date IS NULL OR up.end_date > r.report_date)\n" +
					"      AND re.ref_subject_type = s.id\n" +
					"      AND s.begin_date = (SELECT max(s1.begin_date)\n" +
					"                          FROM v_ref_subject_type s1\n" +
					"                          WHERE s1.rec_id = s.rec_id\n" +
					"                                AND s1.begin_date <= sysdate)\n" +
					"      AND (s.end_date IS NULL OR s.end_date > sysdate)");
			ps.setLong(1, outputReportId);
			ps.setString(2, languageCode);
			rs = ps.executeQuery();
			while (rs.next()) {
				InputReportListItem item = new InputReportListItem();
				item.setId(rs.getLong("id"));
				item.setIdn(rs.getString("idn"));
				item.setReportDate(rs.getDate("report_date"));
				item.setFormCode(rs.getString("form_code"));
				item.setFormName(rs.getString("form_name"));
				item.setRespondentNameRu(rs.getString("resp_name_ru"));
				item.setRespondentShortNameRu(rs.getString("resp_short_name_ru"));
				item.setSubjectTypeNameRu(rs.getString("subjecttype_name_ru"));
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
	public List<RefRespondentItem> getInputReportRespondentsByOutputReportId(Long outputReportId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<RefRespondentItem> result = new ArrayList<RefRespondentItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT\n" +
					"  up1.PERSON_ID,\n" +
					"  up1.idn,\n" +
					"  up1.REC_ID                            person_rec_id,\n" +
					"  s.ID                                 ref_subjecttype,\n" +
					"  s.REC_ID                             ref_subjecttype_rec_id,\n" +
					"  up1.name_ru                        AS resp_name_ru,\n" +
					"  nvl(up1.short_name_ru, up1.name_ru) AS resp_short_name_ru,\n" +
					"  s.name_ru                         AS subjecttype_name_ru,\n" +
					"  dep.name_ru                         AS dep_name_ru\n" +
					"FROM V_REF_UNIONPERSONS up1\n" +
					"  INNER JOIN V_REF_RESPONDENT re ON up1.ID = re.REF_UNIONPERSONS\n" +
					"  INNER JOIN V_REF_SUBJECT_TYPE s ON re.ref_subject_type = s.ID\n" +
					"  LEFT JOIN V_REF_DEPARTMENT dep ON re.ref_department = dep.ID\n" +
					"WHERE (up1.ID,up1.PERSON_ID) IN (\n" +
					"  SELECT up.id, up.PERSON_ID\n" +
					"  FROM input_reports ir\n" +
					"    INNER JOIN REPORTS r ON ir.IN_REPORT_ID = r.ID\n" +
					"    INNER JOIN V_REF_UNIONPERSONS up ON up.IDN = r.IDN\n" +
					"  WHERE ir.OUT_REPORT_ID = ?\n" +
					"        AND up.begin_date = (SELECT max(up2.begin_date)\n" +
					"                            FROM V_REF_UNIONPERSONS up2\n" +
					"                            WHERE up.rec_id = up2.rec_id AND up.TYPE=up2.TYPE\n" +
					"                                  AND up2.begin_date <= r.report_date)\n" +
					"        AND (up.end_date IS NULL OR up.end_date > r.report_date)\n" +
					")");
			ps.setLong(1, outputReportId);
			rs = ps.executeQuery();
			while (rs.next()) {
				RefRespondentItem item = new RefRespondentItem();
				item.setIdn(rs.getString("idn"));
				item.setId(rs.getLong("person_id"));
				item.setPersonName(rs.getString("resp_name_ru"));
				item.setNameRu(rs.getString("resp_name_ru"));
				item.setPersonShortName(rs.getString("resp_short_name_ru"));
				item.setShortNameRu(rs.getString("resp_short_name_ru"));
				item.setPerson(rs.getLong("person_id"));
				item.setPersonRecId(rs.getLong("person_rec_id"));
				item.setRefSubjectTypeName(rs.getString("subjecttype_name_ru"));
				item.setRefSubjectType(rs.getLong("ref_subjecttype"));
				item.setRefSubjectTypeRecId(rs.getLong("ref_subjecttype_rec_id"));
				item.setRefDepartmentName(rs.getString("dep_name_ru"));
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
	public List<ReportListItem> getOutputReportListByInputReportId(Long inputReportId, String languageCode) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ReportListItem> result = new ArrayList<ReportListItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select r.*, st.status_code, st.status_date " +
					" from reports r " +
					"	INNER JOIN REPORT_HISTORY h ON h.ID=(SELECT max(h2.ID) FROM REPORT_HISTORY h2 WHERE h2.REPORT_ID=r.ID)\n" +
					"	INNER JOIN REPORT_HISTORY_STATUSES st on st.ID=(SELECT max(st2.ID) FROM REPORT_HISTORY_STATUSES st2 WHERE st2.REPORT_HISTORY_ID=h.ID)" +
					"where substr(r.idn,0,5) != 'DRAFT' and  exists (select 'x' from input_reports ir where ir.out_report_id=r.id and ir.in_report_id=?)");
			ps.setLong(1, inputReportId);
			rs = ps.executeQuery();
			while (rs.next()) {
				Report report = getReportFromResultSet(rs);

				List<ReportHistory> reportHistory = getReportHistoryByReportNoLobs(report, connection);
				report.setReportHistory(reportHistory);
				List<ReportStatus> reportStatusHistory = getReportStatusHistoryByReportId(report.getId(), true, connection);
				report.setReportStatusHistory(reportStatusHistory);

				ReportListItem item = new ReportListItem();
				item.setId(report.getId());
				item.setReportDate(report.getReportDate());
				item.setFormCode(report.getFormCode());
				List<Form> forms = getFormsByCodeLanguageReportDate(report.getFormCode(), languageCode, report.getReportDate(), connection);
				if (!forms.isEmpty()) {
					item.setFormName(forms.get(0).getFormHistory().getName());
					item.setFormTypeName(Form.resMap.get(languageCode + "_" + forms.get(0).getTypeCode()));
					item.setCanAttachedFile(forms.get(0).getFormHistory().getFormTag() == null ? false : forms.get(0).getFormHistory().getFormTag().canAttachedFile);
				}
				ReportHistory lastReport = reportHistory.isEmpty() ? null : reportHistory.get(reportHistory.size()-1);
				if (lastReport != null) {
					item.setSaveDate(lastReport.getSaveDate());
					item.setUserInfo(lastReport.getUserInfo());
					item.setDeliveryWay(lastReport.getDeliveryWayCode());
				}
				item.setStatus(rs.getString("STATUS_CODE"));
				item.setStatusDate(rs.getDate("STATUS_DATE"));

				result.add(item);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	private void insertInputReportId(Long outputReportId, Long inputReportId, Connection connection) throws SQLException{
		PreparedStatement ps = null;
		try{
			ps = connection.prepareStatement("INSERT INTO input_reports(out_report_id, in_report_id) values(?, ?)");
			ps.setLong(1, outputReportId);
			ps.setLong(2, inputReportId);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
		} finally {
			DbUtil.closeStatement(ps);
		}
	}

	// Export

	@Override
	public byte[] getExcelFileContent(ReportListItem reportListItem, boolean forView, boolean forSuperUser) {
		String html = getHtmlWithReportData(reportListItem.getId(), forView, forSuperUser);
		HtmlParser htmlParser = new HtmlParser(html);
		htmlParser.createExcelDocument();
		return htmlParser.getExcelDocumentBytes();
	}

	@Override
	public byte[] getExcelFileContentForReport(String html) {
		HtmlParser htmlParser = new HtmlParser(html);
		htmlParser.createExcelDocument();
		return htmlParser.getExcelDocumentBytes();
	}

	@Override
	public FileWrapper generateExcelFile(ReportListItem reportListItem, boolean forSuperUser) throws Exception {
		Long reportId = reportListItem.getId();
		if (reportId == null) throw new Exception("Не установлен ID отчета");

		long lastReportHistoryId = getLastReportHistoryIdByReportId(reportId, forSuperUser, null);
		return generateExcelFileFromReportHistory(lastReportHistoryId);
	}

	@Override
	public FileWrapper generateExcelFileFromReportHistory(long reportHistoryId) throws Exception {

		long reportId = getReportIdByReportHistoryId(reportHistoryId, null);
		if (reportId == 0)
			throw new Exception(MessageFormat.format("Не найден отчет для истории с id {0}", reportHistoryId));

		Report report = getReport(reportId, null);
		if (report == null) throw new Exception(MessageFormat.format("Не найден отчет по ID {0}", reportId));

		List<Form> forms = getFormsByCodeLanguageReportDate(report.getFormCode(), "ru", report.getReportDate(), null);
		if (forms.isEmpty())
			throw new Exception(MessageFormat.format("Не найдена форма: код {0}, дата {1}, язык {2}", report.getFormCode(), report.getReportDate(), "ru"));

		Date reportDate = report.getReportDate();
		Form form = getFormWithXlsOut(forms.get(0).getId(), reportDate);

		FileWrapper fileWrapper = new FileWrapper();
		fileWrapper.setFileName(report.getIdn());
		if (form.getFormHistory().getXlsOut() == null) {
			String html = getHtmlWithReportHistoryData(reportHistoryId, false);
			byte[] bytes = getExcelFileContentForReport(html);
//			byte[] bytes = getExcelFileContent(reportListItem,false);
			fileWrapper.setBytes(bytes);
			fileWrapper.setFileFormat("xls");
		} else {
			byte[] bytes = form.getFormHistory().getXlsOut();

			ReportHistory reportHistory = getReportHistory(reportHistoryId, true, false);
			if (reportHistory == null) throw new Exception();

			String jsonData = reportHistory.getData();
			if (jsonData == null || jsonData.isEmpty()) throw new Exception();

			Type typeMapStringString = new TypeToken<Map<String, String>>() {
			}.getType();
			Map<String, String> kvMap = gson.fromJson(jsonData, typeMapStringString);

			Form formWithIVC = getForm(getFormId(report.getFormCode(), report.getReportDate()), report.getReportDate());
			updateSignDate(reportHistoryId, new Date(), formWithIVC.getFormHistory().getInputValueChecks(), kvMap, null);

			String respondentName = "";
			if (form.getTypeCode().equalsIgnoreCase(Form.Type.INPUT.name())) {
				if (!report.getIdn().startsWith("DRAFT")) {
					RefRespondentItem respondent = reference.getRespondentByIdn(report.getIdn(), report.getReportDate());
					if (respondent == null) throw new Exception();
					respondentName = respondent.getNameRu();
				}
			} else {
				List<RefRespondentItem> respondents = getInputReportRespondentsByOutputReportId(reportId);
				if (respondents.size() == 1) {
					respondentName = respondents.get(0).getNameRu();
				} else {
					respondentName = "";
				}
			}


			Date startDate  = null, endDate = null;
			PeriodType periodType = null;
			if (forms.get(0).getFormHistory().getPeriodCount() > 0) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
				String startDateStr = getReportPropValue(reportHistoryId, ReportProps.START_PERIOD_KEY);
				String endDateStr = getReportPropValue(reportHistoryId, ReportProps.END_PERIOD_KEY);
				String periodTypeStr = getReportPropValue(reportHistoryId, ReportProps.PERIOD_DURATION_KEY);
				if (startDateStr != null) {
					try {
						startDate = dateFormat.parse(startDateStr);
					} catch (ParseException e) {
						logger.error(e.getMessage());
					}
				}
				if (endDateStr != null) {
					try {
						endDate = dateFormat.parse(endDateStr);
					} catch (ParseException e) {
						logger.error(e.getMessage());
					}
				}
				if (periodTypeStr != null) {
					periodType = PeriodUtil.getPeriodTypeByName(periodTypeStr);
				}
			}

			ExcelDataFiller filler = new ExcelDataFiller(bytes, respondentName, reportDate, new DataProvider(kvMap), reference, startDate, endDate, periodType);
			filler.process();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			filler.getWorkbook().write(outputStream);

			fileWrapper.setBytes(outputStream.toByteArray());
			fileWrapper.setFileFormat("xlsx");
		}

		return fileWrapper;
	}

	@Override
	public List<FileWrapper> generateExcelFiles(List<ReportListItem> reportListItems, boolean forSuperUser, Long respondentId, Long userId, String userLocation) throws Exception {
		DateFormat dfReportDate = new SimpleDateFormat("dd.MM.yyyy");
		List<FileWrapper> fileWrappers = new ArrayList<FileWrapper>();
		List<AuditEvent> auditEventList = new ArrayList<AuditEvent>();
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			for (ReportListItem item : reportListItems) {
				Long reportId = item.getId();
				if (reportId == null) throw new Exception("Не установлен ID отчета");

				long lastReportHistoryId = getLastReportHistoryIdByReportId(reportId, forSuperUser, connection);

				FileWrapper fileWrapper = generateExcelFileFromReportHistory(lastReportHistoryId);
				String fileName = dfReportDate.format(item.getReportDate()) + "_"  + fileWrapper.getFileName() + "_" + item.getFormCode() + "_draft." + fileWrapper.getFileFormat();
				fileWrapper.setFileName(fileName);
				fileWrappers.add(fileWrapper);

				AuditEvent auditEvent = new AuditEvent();
				auditEvent.setCodeObject(item.getFormCode());
				auditEvent.setNameObject(null);
				auditEvent.setIdKindEvent(10L);
				auditEvent.setDateEvent(new Date());
				auditEvent.setIdRefRespondent(respondentId);
				auditEvent.setDateIn(item.getReportDate());
				auditEvent.setRecId(item.getId());
				auditEvent.setUserId(userId);
				auditEvent.setUserLocation(userLocation);
				auditEventList.add(auditEvent);
			}

			AuditEvent auditEvent = new AuditEvent();
			auditEvent.setCodeObject("UNION_EXCEL");
			auditEvent.setNameObject("Выгрузка отчетов в один Excel файл");
			auditEvent.setIdKindEvent(10L);
			auditEvent.setDateEvent(new Date());
			auditEvent.setIdRefRespondent(respondentId);
			auditEvent.setDateIn(new Date());
			auditEvent.setRecId(null);
			auditEvent.setUserId(userId);
			auditEvent.setUserLocation(userLocation);

			Long aeId = insertAuditEvent(auditEvent, connection);

			for(AuditEvent aeItem : auditEventList){
				aeItem.setParentId(aeId);
				insertAuditEvent(aeItem, connection);
			}
			connection.commit();
		}catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
		return fileWrappers;
	}

	@Override
	public FileWrapper unionExcel(List<FileWrapper> fileWrappers, String fileName) throws Exception{
		String dirName = tempExcelDir + "\\" + fileName;
		File dirFile = new File(dirName);

		if (!dirFile.exists()) {
			boolean created = dirFile.mkdir();
			if(!created)
				throw new Exception("Ошибка при создании директории!");
		}
		for(FileWrapper fileWrapper : fileWrappers) {
			String fileFullPath = dirName + "\\" + fileWrapper.getFileName();
			File file = new File(fileFullPath);

			if (!file.exists()) {
				boolean created = file.createNewFile();
				if(!created)
					throw new Exception("Ошибка при создании файла!");
			}

			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(fileWrapper.getBytes());
			fileOutputStream.flush();
			fileOutputStream.close();
		}

		String unionResult = pdfConverter.unionExcel(dirFile.getPath());

		File unionExcel = new File(dirFile.getPath() + ".xlsx");
		byte[] bytes = new byte[(int) unionExcel.length()];

		FileInputStream pdfFileInputStream = new FileInputStream(unionExcel);
		pdfFileInputStream.read(bytes);
		pdfFileInputStream.close();

		return new FileWrapper(bytes, ".xlsx");
	}

	@Override
	public FileWrapper generatePdfFile(ReportListItem reportListItem, boolean forSuperUser) throws Exception {
		if (reportListItem == null)
			throw new Exception("Отчет не выбран");

		Long reportId = reportListItem.getId();
		if (reportId == null) throw new Exception("Не установлен ID отчета");

		Report report = getReport(reportId, null);
		if (report == null) throw new Exception(MessageFormat.format("Не найден отчет по ID {0}", reportId));

		String fileBaseName = String.valueOf(reportListItem.getId());

		FileWrapper excelFileWrapper = generateExcelFile(reportListItem, forSuperUser);
		return convertFileToPdf(excelFileWrapper, fileBaseName);
	}

	@Override
	public FileWrapper generatePdfFileFromReportHistory(long reportHistoryId) throws Exception {
		long reportId = getReportIdByReportHistoryId(reportHistoryId, null);
		if (reportId == 0)
			throw new Exception(MessageFormat.format("Не найден отчет для истории с id {0}", reportHistoryId));

		Report report = getReport(reportId, null);
		if (report == null) throw new Exception(MessageFormat.format("Не найден отчет по ID {0}", reportId));

		String fileBaseName = String.valueOf(reportHistoryId);

		FileWrapper excelFileWrapper = generateExcelFileFromReportHistory(reportHistoryId);
		return convertFileToPdf(excelFileWrapper, fileBaseName);
	}

	@Override
	public ExcelData extractExcelData(byte[] xlsFile, Date reportDate) throws Exception {
		ExtractExcelData ee = new ExtractExcelData(xlsFile, reportDate, reference);
		ee.ExtractExcelTable();
		return new ExcelData(ee.getFormName(), ee.getInputValues(), ee.getExcelForm());
	}

	@Override
	public byte[] replaceExcelData(byte[] xlsFile) throws Exception {
		ExcelDataReplacer replacer = new ExcelDataReplacer(xlsFile);
		replacer.process();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		replacer.getWorkbook().write(outputStream);
		byte[] result =  outputStream.toByteArray();
		outputStream.close();
		return result;
	}

	@Override
	public FileWrapper controlResultsToExcelFile(List<ControlResultItem> controlResults, final ReportListItem reportListItem) throws Exception {
		FileWrapper fileWrapper = new FileWrapper();

		Template template = new Template();
		template.setCodeTemplate("tml_control_result");

		byte[] xlsOut = reference.getTemplateData(template).getXlsOut();

		if(xlsOut == null)
			throw new Exception("Файл шаблона не найден");

		InputStream myInputStream = new ByteArrayInputStream(xlsOut);

		try {
			ExcelReport excelReport = new ExcelReportImpl(myInputStream);

			ReportSheet sheet = excelReport.addSheet(0);
			sheet.setSheetTitle("Лист 1");

			MapKeyHandler<ReportListItem> reportListItemHandler = new MapKeyHandler<ReportListItem>(new EntityMapConverter<ReportListItem>() {
				@Override
				public Map<String, String> convert(ReportListItem entity) {
					Report report = getReport(entity.getId(), null);
					RefRespondentItem respondent = reference.getRespondentByIdn(report.getIdn(), report.getReportDate());
					Map<String, String> data = new HashMap<String, String>();
					data.put("report_name", entity.getFormName());
					data.put("respondent_name", respondent.getNameRu());
					DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
					data.put("report_date", format.format(entity.getReportDate()));
					return data;
				}
			});
			reportListItemHandler.setData(reportListItem);

			sheet.out("header", reportListItemHandler);

			MapKeyHandler<ControlResultItem> controlResultHandler = new MapKeyHandler<ControlResultItem>(new EntityMapConverter<ControlResultItem>() {
				Map<Long, ControlResult.ResultType> controlResultTypes;

				{
					controlResultTypes = new HashMap<Long, ControlResult.ResultType>();
					controlResultTypes.put(ControlResult.ResultType.SUCCESS.getId(), ControlResult.ResultType.SUCCESS);
					controlResultTypes.put(ControlResult.ResultType.FAIL.getId(), ControlResult.ResultType.FAIL);
					controlResultTypes.put(ControlResult.ResultType.ERROR.getId(), ControlResult.ResultType.ERROR);
					controlResultTypes.put(ControlResult.ResultType.NO_DATA.getId(), ControlResult.ResultType.NO_DATA);
				}

				String getControlResultTypeName(Long controlResultType) {
					ControlResult.ResultType resultType = controlResultTypes.get(controlResultType);
					if (resultType == null)
						return "";
					else
						return resultType.getName("ru");
				}

				@Override
				public Map<String, String> convert(ControlResultItem entity) {
					Map<String, String> data = new HashMap<String, String>();
					data.put("crosscheckTypeName", entity.getCrosscheckTypeNameRu());
					data.put("description", entity.getDescriptionRu());
					data.put("resultTypeName", getControlResultTypeName(entity.getResultType()));
					data.put("reportDate", Convert.getDateStringFromDate(entity.getReportDate()));
					data.put("extSysName", entity.getExternalSystemNameRu() != null ? entity.getExternalSystemNameRu() : "");
					return data;
				}
			});
			for (ControlResultItem controlResult : controlResults) {
				controlResultHandler.setData(controlResult);
				sheet.out("data", controlResultHandler);
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			excelReport.saveResult(outputStream);

			fileWrapper.setFileFormat("xlsx");
			fileWrapper.setBytes(outputStream.toByteArray());
		}finally {
			myInputStream.close();
		}
		return fileWrapper;
	}

	@Override
	public FileWrapper controlResultsAllToExcelFile(List<ControlResultItem> controlResults, RefRespondentItem refRespondentItem) throws Exception {
		FileWrapper fileWrapper = new FileWrapper();

		Template template = new Template();
		template.setCodeTemplate("tml_control_result_all");

		byte[] xlsOut = reference.getTemplateData(template).getXlsOut();

		if(xlsOut == null)
			throw new Exception("Файл шаблона не найден");

		InputStream myInputStream = new ByteArrayInputStream(xlsOut);

		try {
			ExcelReport excelReport = new ExcelReportImpl(myInputStream);

			ReportSheet sheet = excelReport.addSheet(0);
			sheet.setSheetTitle("Лист 1");

			MapKeyHandler<RefRespondentItem> headerHandler = new MapKeyHandler<RefRespondentItem>(new EntityMapConverter<RefRespondentItem>() {
				@Override
				public Map<String, String> convert(RefRespondentItem entity) {
					Map<String, String> data = new HashMap<String, String>();
					data.put("respondent_name", entity != null ? entity.getName("ru") : "");
					return data;
				}
			});
			headerHandler.setData(refRespondentItem);

			sheet.out("header", headerHandler);

			MapKeyHandler<ControlResultItem> controlResultHandler = new MapKeyHandler<ControlResultItem>(new EntityMapConverter<ControlResultItem>() {
				Map<Long, ControlResult.ResultType> controlResultTypes;

				{
					controlResultTypes = new HashMap<Long, ControlResult.ResultType>();
					controlResultTypes.put(ControlResult.ResultType.SUCCESS.getId(), ControlResult.ResultType.SUCCESS);
					controlResultTypes.put(ControlResult.ResultType.FAIL.getId(), ControlResult.ResultType.FAIL);
					controlResultTypes.put(ControlResult.ResultType.ERROR.getId(), ControlResult.ResultType.ERROR);
					controlResultTypes.put(ControlResult.ResultType.NO_DATA.getId(), ControlResult.ResultType.NO_DATA);
				}

				String getControlResultTypeName(Long controlResultType) {
					ControlResult.ResultType resultType = controlResultTypes.get(controlResultType);
					if (resultType == null)
						return "";
					else
						return resultType.getName("ru");
				}

				@Override
				public Map<String, String> convert(ControlResultItem entity) {
					Map<String, String> data = new HashMap<String, String>();
					data.put("crosscheckTypeName", entity.getCrosscheckTypeNameRu());
					data.put("description", entity.getDescriptionRu());
					data.put("resultTypeName", getControlResultTypeName(entity.getResultType()));
					data.put("reportDate", Convert.getDateStringFromDate(entity.getReportDate()));
					data.put("extSysName", entity.getExternalSystemNameRu() != null ? entity.getExternalSystemNameRu() : "");
					return data;
				}
			});
			for (ControlResultItem controlResult : controlResults) {
				controlResultHandler.setData(controlResult);
				sheet.out("data", controlResultHandler);
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			excelReport.saveResult(outputStream);

			fileWrapper.setFileFormat("xlsx");
			fileWrapper.setBytes(outputStream.toByteArray());
		}finally {
			myInputStream.close();
		}
		return fileWrapper;
	}

	@Override
	public FileWrapper extControlResultsAllToExcelFile(List<ControlResultItem> controlResults, RefRespondentItem refRespondentItem) throws Exception {
		FileWrapper fileWrapper = new FileWrapper();

		Template template = new Template();
		template.setCodeTemplate("tml_ext_control_result_all");

		byte[] xlsOut = reference.getTemplateData(template).getXlsOut();

		if(xlsOut == null)
			throw new Exception("Файл шаблона не найден");

		InputStream myInputStream = new ByteArrayInputStream(xlsOut);

		try {
			ExcelReport excelReport = new ExcelReportImpl(myInputStream);

			ReportSheet sheet = excelReport.addSheet(0);
			sheet.setSheetTitle("Лист 1");

			MapKeyHandler<RefRespondentItem> headerHandler = new MapKeyHandler<RefRespondentItem>(new EntityMapConverter<RefRespondentItem>() {
				@Override
				public Map<String, String> convert(RefRespondentItem entity) {
					Map<String, String> data = new HashMap<String, String>();
					data.put("respondent_name", entity != null ? entity.getName("ru") : "");
					return data;
				}
			});
			headerHandler.setData(refRespondentItem);

			sheet.out("header", headerHandler);

			MapKeyHandler<ControlResultItem> controlResultHandler = new MapKeyHandler<ControlResultItem>(new EntityMapConverter<ControlResultItem>() {
				Map<Long, ControlResult.ResultType> controlResultTypes;

				{
					controlResultTypes = new HashMap<Long, ControlResult.ResultType>();
					controlResultTypes.put(ControlResult.ResultType.SUCCESS.getId(), ControlResult.ResultType.SUCCESS);
					controlResultTypes.put(ControlResult.ResultType.FAIL.getId(), ControlResult.ResultType.FAIL);
					controlResultTypes.put(ControlResult.ResultType.ERROR.getId(), ControlResult.ResultType.ERROR);
					controlResultTypes.put(ControlResult.ResultType.NO_DATA.getId(), ControlResult.ResultType.NO_DATA);
				}

				String getControlResultTypeName(Long controlResultType) {
					ControlResult.ResultType resultType = controlResultTypes.get(controlResultType);
					if (resultType == null)
						return "";
					else
						return resultType.getName("ru");
				}

				@Override
				public Map<String, String> convert(ControlResultItem entity) {
					Map<String, String> data = new HashMap<String, String>();
					data.put("description", entity.getDescriptionRu());
					data.put("resultTypeName", getControlResultTypeName(entity.getResultType()));
					data.put("reportDate", Convert.getDateStringFromDate(entity.getReportDate()));
					data.put("sys_frsi", entity.getResultL());
					data.put("sys_1", entity.getExternalSystemId() == 1 ? entity.getResultR() : "");
					data.put("sys_2", entity.getExternalSystemId() == 2 ? entity.getResultR() : "");
					data.put("sys_3", entity.getExternalSystemId() == 3 ? entity.getResultR() : "");
					return data;
				}
			});
			for (ControlResultItem controlResult : controlResults) {
				controlResultHandler.setData(controlResult);
				sheet.out("data", controlResultHandler);
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			excelReport.saveResult(outputStream);

			fileWrapper.setFileFormat("xlsx");
			fileWrapper.setBytes(outputStream.toByteArray());
		}finally {
			myInputStream.close();
		}
		return fileWrapper;
	}

	@Override
	public FileWrapper controlResultGroupsToExcelFile(List<ControlResultItemGroup> controlResultGroups) throws Exception {
		FileWrapper fileWrapper = new FileWrapper();

		Template template = new Template();
		template.setCodeTemplate("tml_control_result_grouped");

		byte[] xlsOut = reference.getTemplateData(template).getXlsOut();

		if(xlsOut == null)
			throw new Exception("Файл шаблона не найден");

		InputStream myInputStream = new ByteArrayInputStream(xlsOut);

		try {
			ExcelReport excelReport = new ExcelReportImpl(myInputStream);

			ReportSheet sheet = excelReport.addSheet(0);
			sheet.setSheetTitle("Лист 1");

			sheet.out("header", null);

			MapKeyHandler<ControlResultItemGroup> controlResultGroupHandler = new MapKeyHandler<ControlResultItemGroup>(new EntityMapConverter<ControlResultItemGroup>() {
				@Override
				public Map<String, String> convert(ControlResultItemGroup entity) {
					Map<String, String> data = new HashMap<String, String>();
					data.put("groupTitle", entity.getTitle());
					return data;
				}
			});

			MapKeyHandler<ControlResultItem> controlResultHandler = new MapKeyHandler<ControlResultItem>(new EntityMapConverter<ControlResultItem>() {
				Map<Long, ControlResult.ResultType> controlResultTypes;

				{
					controlResultTypes = new HashMap<Long, ControlResult.ResultType>();
					controlResultTypes.put(ControlResult.ResultType.SUCCESS.getId(), ControlResult.ResultType.SUCCESS);
					controlResultTypes.put(ControlResult.ResultType.FAIL.getId(), ControlResult.ResultType.FAIL);
					controlResultTypes.put(ControlResult.ResultType.ERROR.getId(), ControlResult.ResultType.ERROR);
					controlResultTypes.put(ControlResult.ResultType.NO_DATA.getId(), ControlResult.ResultType.NO_DATA);
				}

				String getControlResultTypeName(Long controlResultType) {
					ControlResult.ResultType resultType = controlResultTypes.get(controlResultType);
					if (resultType == null)
						return "";
					else
						return resultType.getName("ru");
				}

				@Override
				public Map<String, String> convert(ControlResultItem entity) {
					Map<String, String> data = new HashMap<String, String>();
					data.put("crosscheckTypeName", entity.getCrosscheckTypeNameRu());
					data.put("description", entity.getDescriptionRu());
					data.put("resultTypeName", getControlResultTypeName(entity.getResultType()));
					data.put("reportDate", Convert.getDateStringFromDate(entity.getReportDate()));
					data.put("extSysName", entity.getExternalSystemNameRu() != null ? entity.getExternalSystemNameRu() : "");
					return data;
				}
			});
			for (ControlResultItemGroup group : controlResultGroups) {
				controlResultGroupHandler.setData(group);
				sheet.out("groupTitle", controlResultGroupHandler);
				for (ControlResultItem controlResult : group.getItems()) {
					controlResultHandler.setData(controlResult);
					sheet.out("data", controlResultHandler);
				}
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			excelReport.saveResult(outputStream);

			fileWrapper.setFileFormat("xlsx");
			fileWrapper.setBytes(outputStream.toByteArray());
		}finally {
			myInputStream.close();
		}
		return fileWrapper;
	}

	@Override
	public FileWrapper extControlResultGroupsToExcelFile(List<ControlResultItemGroup> controlResultGroups) throws Exception {
		FileWrapper fileWrapper = new FileWrapper();

		Template template = new Template();
		template.setCodeTemplate("tml_ext_control_result_grouped");

		byte[] xlsOut = reference.getTemplateData(template).getXlsOut();

		if(xlsOut == null)
			throw new Exception("Файл шаблона не найден");

		InputStream myInputStream = new ByteArrayInputStream(xlsOut);

		try {
			ExcelReport excelReport = new ExcelReportImpl(myInputStream);

			ReportSheet sheet = excelReport.addSheet(0);
			sheet.setSheetTitle("Лист 1");

			sheet.out("header", null);

			MapKeyHandler<ControlResultItemGroup> controlResultGroupHandler = new MapKeyHandler<ControlResultItemGroup>(new EntityMapConverter<ControlResultItemGroup>() {
				@Override
				public Map<String, String> convert(ControlResultItemGroup entity) {
					Map<String, String> data = new HashMap<String, String>();
					data.put("groupTitle", entity.getTitle());
					return data;
				}
			});

			MapKeyHandler<ControlResultItem> controlResultHandler = new MapKeyHandler<ControlResultItem>(new EntityMapConverter<ControlResultItem>() {
				Map<Long, ControlResult.ResultType> controlResultTypes;

				{
					controlResultTypes = new HashMap<Long, ControlResult.ResultType>();
					controlResultTypes.put(ControlResult.ResultType.SUCCESS.getId(), ControlResult.ResultType.SUCCESS);
					controlResultTypes.put(ControlResult.ResultType.FAIL.getId(), ControlResult.ResultType.FAIL);
					controlResultTypes.put(ControlResult.ResultType.ERROR.getId(), ControlResult.ResultType.ERROR);
					controlResultTypes.put(ControlResult.ResultType.NO_DATA.getId(), ControlResult.ResultType.NO_DATA);
				}

				String getControlResultTypeName(Long controlResultType) {
					ControlResult.ResultType resultType = controlResultTypes.get(controlResultType);
					if (resultType == null)
						return "";
					else
						return resultType.getName("ru");
				}

				@Override
				public Map<String, String> convert(ControlResultItem entity) {
					Map<String, String> data = new HashMap<String, String>();
					data.put("description", entity.getDescriptionRu());
					data.put("resultTypeName", getControlResultTypeName(entity.getResultType()));
					data.put("reportDate", Convert.getDateStringFromDate(entity.getReportDate()));
					data.put("sys_frsi", entity.getResultL());
					data.put("sys_1", entity.getExternalSystemId() == 1 ? entity.getResultR() : "");
					data.put("sys_2", entity.getExternalSystemId() == 2 ? entity.getResultR() : "");
					data.put("sys_3", entity.getExternalSystemId() == 3 ? entity.getResultR() : "");
					return data;
				}
			});
			for (ControlResultItemGroup group : controlResultGroups) {
				controlResultGroupHandler.setData(group);
				sheet.out("groupTitle", controlResultGroupHandler);
				for (ControlResultItem controlResult : group.getItems()) {
					controlResultHandler.setData(controlResult);
					sheet.out("data", controlResultHandler);
				}
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			excelReport.saveResult(outputStream);

			fileWrapper.setFileFormat("xlsx");
			fileWrapper.setBytes(outputStream.toByteArray());
		}finally {
			myInputStream.close();
		}
		return fileWrapper;
	}

	@Override
	public FileWrapper generateControlResultsPdfFile(List<ControlResultItem> controlResults, ReportListItem reportListItem) throws Exception {
		if (reportListItem == null)
			throw new Exception("Отчет не выбран");

		String fileBaseName = "control_result_"+String.valueOf(reportListItem.getId());

		FileWrapper excelFileWrapper = controlResultsToExcelFile(controlResults, reportListItem);
		return convertFileToPdf(excelFileWrapper, fileBaseName);
	}

	@Override
	public FileWrapper generateControlResultsAllPdfFile(List<ControlResultItem> controlResults, RefRespondentItem refRespondentItem) throws Exception {
		String fileBaseName = "control_result";

		FileWrapper excelFileWrapper = controlResultsAllToExcelFile(controlResults, refRespondentItem);
		return convertFileToPdf(excelFileWrapper, fileBaseName);
	}

	@Override
	public FileWrapper generateControlResultGroupsPdfFile(List<ControlResultItemGroup> controlResultGroups) throws Exception {
		String fileBaseName = "control_result";

		FileWrapper excelFileWrapper = controlResultGroupsToExcelFile(controlResultGroups);
		return convertFileToPdf(excelFileWrapper, fileBaseName);
	}

	@Override
	public FileWrapper validationResultsToExcelFile(final String validationMessage, List<Error> errors, ReportListItem reportListItem) throws Exception {
		FileWrapper fileWrapper = new FileWrapper();

		Template template = new Template();
		template.setCodeTemplate("tml_validation_result");

		byte[] xlsOut = reference.getTemplateData(template).getXlsOut();
		if(xlsOut == null)
			throw new Exception("Файл шаблона не найден");

		InputStream myInputStream = new ByteArrayInputStream(xlsOut);

		try {
			ExcelReport excelReport = new ExcelReportImpl(myInputStream);
			ReportSheet sheet = excelReport.addSheet(0);
			sheet.setSheetTitle("Лист 1");

			MapKeyHandler<ReportListItem> reportListItemHandler = new MapKeyHandler<ReportListItem>(new EntityMapConverter<ReportListItem>() {
				@Override
				public Map<String, String> convert(ReportListItem entity) {
					Report report = getReport(entity.getId(), null);
					RefRespondentItem respondent = reference.getRespondentByIdn(report.getIdn(), report.getReportDate());
					Map<String, String> data = new HashMap<String, String>();
					data.put("report_name", entity.getFormName());
					data.put("respondent_name", respondent.getNameRu());
					DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
					data.put("report_date", format.format(entity.getReportDate()));
					data.put("validationMessage", validationMessage);
					return data;
				}
			});
			reportListItemHandler.setData(reportListItem);

			sheet.out("header", reportListItemHandler);

			if (errors.size() > 0) {
				sheet.out("errorsHeader", null);
				MapKeyHandler<Error> validationResultHandler = new MapKeyHandler<Error>(new EntityMapConverter<Error>() {
					@Override
					public Map<String, String> convert(Error entity) {
						Map<String, String> data = new HashMap<String, String>();
						data.put("description", (entity.getDescriptionRu() + " (" + entity.getLocation() + ")"));
						return data;
					}
				});
				for (Error error : errors) {
					validationResultHandler.setData(error);
					sheet.out("errorsBody", validationResultHandler);
				}
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			excelReport.saveResult(outputStream);

			fileWrapper.setFileFormat("xlsx");
			fileWrapper.setBytes(outputStream.toByteArray());
		}finally {
			myInputStream.close();
		}
		return fileWrapper;
	}

	@Override
	public FileWrapper generateValidationResultsPdfFile(String validationMessage, List<Error> errors, ReportListItem reportListItem) throws Exception {
		if (reportListItem == null)
			throw new Exception("Отчет не выбран");

		String fileBaseName = "validation_result_"+String.valueOf(reportListItem.getId());

		FileWrapper excelFileWrapper = validationResultsToExcelFile(validationMessage, errors, reportListItem);
		return convertFileToPdf(excelFileWrapper, fileBaseName);
	}

	@Override
	public FileWrapper generateSuInfoPdfFile(String typeInfo, String infoName, List<ReportValueNameListItem> itemList, List<ColumnModel> columns) throws Exception {

		String fileBaseName = "suInfo_" + typeInfo;

		FileWrapper excelFileWrapper = suInfoToExcelFile(typeInfo, infoName, itemList, columns);
		return convertFileToPdf(excelFileWrapper, fileBaseName);
	}

	@Override
	public FileWrapper convertFileToPdf(FileWrapper fileWrapper, String fileBaseName) throws Exception{
		String fileFullPath = tempDir + "\\" + fileBaseName + "." + fileWrapper.getFileFormat();
		File file = new File(fileFullPath);
		if (!file.exists()) {
			boolean created = file.createNewFile();
			if(!created)
				throw new Exception("Ошибка при создании файла!");
		}
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		fileOutputStream.write(fileWrapper.getBytes());
		fileOutputStream.flush();
		fileOutputStream.close();

		String pdfFileFullPath = tempDir + "\\" + fileBaseName + ".pdf";

		String convertResult;
		String fmt = fileWrapper.getFileFormat();
		if (fmt.equalsIgnoreCase("jpg") || fmt.equalsIgnoreCase("jpeg") ||
				fmt.equalsIgnoreCase("gif") || fmt.equalsIgnoreCase("png"))
			convertResult = convertImageToPdf(fileFullPath, pdfFileFullPath, false);
		else if (fmt.equalsIgnoreCase("tif") || fmt.equalsIgnoreCase("tiff")) {
			convertResult = convertImageToPdf(fileFullPath, pdfFileFullPath, true);
		} else if (fmt.equalsIgnoreCase("docx") || fmt.equalsIgnoreCase("doc")) {
			try {
				convertResult = pdfConverter.convertWord(fileFullPath, pdfFileFullPath);
			} catch (Exception e) {
				convertResult = "-1";
			}
		} else if (fmt.equalsIgnoreCase("xlsx") || fmt.equalsIgnoreCase("xls") || fmt.equalsIgnoreCase("xlsm")) {
			try {
				convertResult = pdfConverter.convertExcel(fileFullPath, pdfFileFullPath);
			} catch (Exception e) {
				convertResult = "-1";
			}
		} else {
			convertResult = "-1";
		}
		if(!convertResult.equals("0"))
			throw new Exception("Ошибка при конвертации файла в pdf!");

		File pdfFile = new File(pdfFileFullPath);
		byte[] bytes = new byte[(int) pdfFile.length()];

		FileInputStream pdfFileInputStream = new FileInputStream(pdfFile);
		pdfFileInputStream.read(bytes);
		pdfFileInputStream.close();

		return new FileWrapper(bytes, "pdf");
	}



	private String convertImageToPdf(String imageFileName, String pdfFileName, boolean isTiff) {
		String result = "0";
		try {
			ImageInputStream is = ImageIO.createImageInputStream(new File(imageFileName));
			if (is == null || is.length() == 0) {
				throw new IllegalStateException("Can't open image "+imageFileName);
			}

			PDDocument doc = new PDDocument();
			PDRectangle r = PDRectangle.A4;

			int pageCount = 1;
			ImageReader tiffReader = null;
			if (isTiff) {
				tiffReader = getTiffImageReader();
				if (tiffReader == null)
					throw new IllegalStateException("Can't open tiff file");
				tiffReader.setInput(is, false, true);
				pageCount = tiffReader.getNumImages(true);
			}

			try {
				for (int i = 0; i < pageCount; i++) {

					PDImageXObject pdImage;
					if (isTiff) {
						BufferedImage bi = tiffReader.read(i);
						pdImage = LosslessFactory.createFromImage(doc, bi);
					} else {
						pdImage = PDImageXObject.createFromFile(imageFileName, doc);
					}

					if (i == 0) {
						if (Math.max(PDRectangle.A3.getWidth(), PDRectangle.A3.getHeight()) < Math.max(pdImage.getWidth(), pdImage.getHeight()))
							r = PDRectangle.A3;
						else
							r = PDRectangle.A4;
					}

					PDPage page = new PDPage(r);
					doc.addPage(page);

					boolean pageRotated;

					if ((pdImage.getWidth() <= pdImage.getHeight()) || (pdImage.getWidth() <= r.getWidth())) {
						pageRotated = false;
					} else {
						page.setRotation(90);
						pageRotated = true;
					}

					PDPageContentStream contents = new PDPageContentStream(doc, page);

					float imgWidth = pdImage.getWidth();
					float imgHeight = pdImage.getHeight();
					if (pageRotated) {
						double n1 = r.getHeight() / imgWidth;
						double n2 = r.getWidth() / imgHeight;
						double n3 = Math.min(n1, n2);
						if (n3 < 1) {
							imgWidth = Math.round(imgWidth * n3);
							imgHeight = Math.round(imgHeight * n3);
						}

						AffineTransform at = new AffineTransform();

						// 4. translate it to the center of the component
						at.translate(r.getWidth() * 0.5, r.getHeight() * 0.5);

						// 3. do the actual rotation
						at.rotate(Math.toRadians(90));

						// 1. translate the object so that you rotate it around the
						//    center (easier :))
						at.translate(-r.getHeight() / 2, -(r.getHeight() - 0.5 * imgHeight));

						Matrix matrix = new Matrix(at);
						contents.transform(matrix);
					} else {
						double n1 = r.getWidth() / imgWidth;
						double n2 = r.getHeight() / imgHeight;
						double n3 = Math.min(n1, n2);
						if (n3 < 1) {
							imgWidth = Math.round(imgWidth * n3);
							imgHeight = Math.round(imgHeight * n3);
						}
					}

					contents.drawImage(pdImage, 0, r.getUpperRightY() - imgHeight, imgWidth, imgHeight);

					contents.close();
				}

				doc.save(pdfFileName);
			} finally {
				doc.close();
			}

		} catch (Exception e) {
			result = e.getMessage();
		}
		return result;
	}

	private ImageReader getTiffImageReader() {
		try {
			Iterator iterator = ImageIO.getImageReadersByFormatName("tiff");
			return (ImageReader) iterator.next();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@Override
	public FileWrapper suInfoToExcelFile(String typeInfo, String infoName, List<ReportValueNameListItem> itemList, List<ColumnModel> columns) throws Exception{
		FileWrapper result = new FileWrapper();
		if (typeInfo.equals("REPORTS") || typeInfo.equals("RESPONDENTS")) {
			result = suInfoReportsToExcelFile(typeInfo, infoName, itemList);
		} else if (typeInfo.equals("SUMMARY")) {
			result = suInfoSummaryToExcelFile(infoName, itemList);
		} else if (typeInfo.equals("RESPONDENT_FORMS")) {
			result = suInfoMatrixToExcelFile(infoName, itemList, columns);
		}
		return result;
	}

	@Override
	public FileWrapper suInfoReportsToExcelFile(final String typeInfo, final String infoName, List<ReportValueNameListItem> itemList) throws Exception {
		FileWrapper fileWrapper = new FileWrapper();

		Template template = new Template();
		if (typeInfo.equals("REPORTS")) {
			template.setCodeTemplate("tml_info_reports");
		} else if (typeInfo.equals("RESPONDENTS")) {
			template.setCodeTemplate("tml_info_respondents");
		}

		byte[] xlsOut = reference.getTemplateData(template).getXlsOut();

		if(xlsOut == null)
			throw new Exception("Файл шаблона не найден");

		InputStream myInputStream = new ByteArrayInputStream(xlsOut);

		try {
			ExcelReport excelReport = new ExcelReportImpl(myInputStream);
			ReportSheet sheet = excelReport.addSheet(0);
			sheet.setSheetTitle("Лист 1");

			ReportValueNameListItem reportValueNameListItem = new ReportValueNameListItem();
			reportValueNameListItem.setInfoName(infoName);

			MapKeyHandler<ReportValueNameListItem> headerMapKeyHandler = new MapKeyHandler<ReportValueNameListItem>(new EntityMapConverter<ReportValueNameListItem>() {
				@Override
				public Map<String, String> convert(ReportValueNameListItem entity) {
					Map<String, String> data = new HashMap<String, String>();
					data.put("info_name", entity.getInfoName());
					return data;
				}
			});

			headerMapKeyHandler.setData(reportValueNameListItem);

			sheet.out("header", headerMapKeyHandler);

			if (itemList.size() > 0) {
				for (ReportValueNameListItem item : itemList) {
					MapKeyHandler<ReportValueNameListItem> parentDataMapKeyHandler = new MapKeyHandler<ReportValueNameListItem>(new EntityMapConverter<ReportValueNameListItem>() {
						@Override
						public Map<String, String> convert(ReportValueNameListItem entity) {
							Map<String, String> data = new HashMap<String, String>();
							data.put("formName", entity.getValueName());
							return data;
						}
					});
					parentDataMapKeyHandler.setData(item);
					sheet.out("parent_data", parentDataMapKeyHandler);

					MapKeyHandler<ReportListItem> dataMapKeyHandler = new MapKeyHandler<ReportListItem>(new EntityMapConverter<ReportListItem>() {
						@Override
						public Map<String, String> convert(ReportListItem entity) {
							Map<String, String> data = new HashMap<String, String>();
							data.put("rowNum", String.valueOf(entity.getRowNum()));
							data.put("periodCode", entity.getPeriodCode());
							if (typeInfo.equals("REPORTS")) {
								data.put("formName", entity.getFormName() == null ? "" : entity.getFormName());
							} else if (typeInfo.equals("RESPONDENTS")) {
								data.put("formName", entity.getFormName() == null ? "" : entity.getRespondentNameRu());
							}
							data.put("submitReportText", entity.getSubmitReportText() == null ? "" : entity.getSubmitReportText());
							data.put("firstCompletedDate", entity.getFirstCompletedDate() == null ? "" : Convert.getDateTimeStringFromDateRus(entity.getFirstCompletedDate()));
							data.put("lastCompletedDate", entity.getLastCompletedDate() == null ? "" : Convert.getDateTimeStringFromDateRus(entity.getLastCompletedDate()));
							data.put("completeCount", entity.getCompleteCount() == 0 ? "" : String.valueOf(entity.getCompleteCount()));
							data.put("controlResultName", entity.getControlResultName() == null ? "" : entity.getControlResultName());
							data.put("statusName", entity.getStatusName() == null ? "" : entity.getStatusName());
							data.put("statusDate", entity.getStatusDate() == null ? "" : Convert.getDateTimeStringFromDateRus(entity.getStatusDate()));
							return data;
						}
					});
					for (ReportListItem reportListItem : item.getReportListItems()) {
						dataMapKeyHandler.setData(reportListItem);
						sheet.out("data", dataMapKeyHandler);
					}
				}
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			excelReport.saveResult(outputStream);

			fileWrapper.setFileFormat("xlsx");
			fileWrapper.setBytes(outputStream.toByteArray());
		}finally {
			myInputStream.close();
		}
		return fileWrapper;
	}

	@Override
	public FileWrapper suInfoSummaryToExcelFile(final String infoName, List<ReportValueNameListItem> itemList) throws Exception {
		FileWrapper fileWrapper = new FileWrapper();

		Template template = new Template();
		template.setCodeTemplate("tml_info_summary");

		byte[] xlsOut = reference.getTemplateData(template).getXlsOut();

		if(xlsOut == null)
			throw new Exception("Файл шаблона не найден");

		InputStream myInputStream = new ByteArrayInputStream(xlsOut);

		try {
			ExcelReport excelReport = new ExcelReportImpl(myInputStream);
			ReportSheet sheet = excelReport.addSheet(0);
			sheet.setSheetTitle("Лист 1");

			ReportValueNameListItem reportValueNameListItem = new ReportValueNameListItem();
			reportValueNameListItem.setInfoName(infoName);

			MapKeyHandler<ReportValueNameListItem> headerMapKeyHandler = new MapKeyHandler<ReportValueNameListItem>(new EntityMapConverter<ReportValueNameListItem>() {
				@Override
				public Map<String, String> convert(ReportValueNameListItem entity) {
					Map<String, String> data = new HashMap<String, String>();
					data.put("info_name", entity.getInfoName());
					return data;
				}
			});

			headerMapKeyHandler.setData(reportValueNameListItem);

			sheet.out("header", headerMapKeyHandler);

			if (itemList.size() > 0) {
				MapKeyHandler<ReportValueNameListItem> dataMapKeyHandler = new MapKeyHandler<ReportValueNameListItem>(new EntityMapConverter<ReportValueNameListItem>() {
					@Override
					public Map<String, String> convert(ReportValueNameListItem entity) {
						Map<String, String> data = new HashMap<String, String>();
						data.put("rowNum", String.valueOf(entity.getRowNum()));
						data.put("formName", entity.getValueName());
						data.put("cnt", String.valueOf(entity.getCnt()));
						data.put("submitCnt", String.valueOf(entity.getSubmitCnt()));
						data.put("notSubmitCnt", String.valueOf(entity.getNotSubmitCnt()));
						return data;
					}
				});
				for (ReportValueNameListItem item : itemList) {
					dataMapKeyHandler.setData(item);
					sheet.out("data", dataMapKeyHandler);
				}
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			excelReport.saveResult(outputStream);

			fileWrapper.setFileFormat("xlsx");
			fileWrapper.setBytes(outputStream.toByteArray());
		} finally {
			myInputStream.close();
		}
		return fileWrapper;
	}

	private FileWrapper suInfoMatrixToExcelFile(final String infoName, List<ReportValueNameListItem> itemList, List<ColumnModel> columns) throws Exception {
		FileWrapper fileWrapper = new FileWrapper();

		Template template = new Template();
		template.setCodeTemplate("tml_info_matrix");

		byte[] xlsOut = reference.getTemplateData(template).getXlsOut();

		if(xlsOut == null)
			throw new Exception("Файл шаблона не найден");

		InputStream myInputStream = new ByteArrayInputStream(xlsOut);

		try {
			InfoBeanExcel excel = new InfoBeanExcel(xlsOut, itemList, columns, infoName);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			excel.getWorkbook().write(outputStream);

			fileWrapper.setFileFormat("xlsx");
			fileWrapper.setBytes(outputStream.toByteArray());
		} finally {
			myInputStream.close();
		}
		return fileWrapper;
	}

	@Override
	public Map<String, String> getGuide(Date reportDate, String className, String fieldName) {
		Connection connection = null;
		CallableStatement stmt = null;
		ResultSet rs = null;
		Map<String, String> result = new HashMap<String, String>();
		try {
			connection = getConnection();
			stmt = connection.prepareCall("BEGIN ? := pkg_frsi_util.read_guide(?,?,?); end;");
			stmt.registerOutParameter(1, OracleTypes.CURSOR); //REF CURSOR
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			stmt.setDate(2, repDate);
			stmt.setString(3, className);
			stmt.setString(4, fieldName);
			stmt.execute();
			rs = (ResultSet) stmt.getObject(1);
			while (rs.next()) {
				result.put(rs.getString("name"), rs.getString("code"));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, stmt, rs);
		}
		return result;
	}

	/*@Override
	public List<ApprovalItem> infoSubmittedReports(Long respondentId, String language_code, Date reportDate) {
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
		SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ApprovalItem> result = new ArrayList<ApprovalItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT a.id, a.batch_id, a.user_id, a.respondent_id, r.name as resp_name, a.entity_id, a.report_date," +
					" a.received_date, a.is_approved, a.form_name, fh.name, a.APPROVAL_DATE, decode(is_approved, 0, 'Не утвержден', '1', 'Утвержден') as status " +
					" FROM approval a, forms f, form_history fh, " +
					"        (select " +
					"          e.id as id, " +
					"          pkg_eav_util.get_string_value(e.id, 'name') as name " +
					"        from " +
					"          eav_be_entities e, " +
					"          eav_m_classes c " +
					"        where " +
					"         c.id = e.class_id and " +
					"         c.name = 'ref_respondent') r" +
					" WHERE a.respondent_id = ? AND TRUNC(a.report_date) = TRUNC(?) " +
					" AND a.form_name = f.code AND fh.language_code = ?" +
					" and a.respondent_id = r.id " +
					" and f.id = fh.form_id and fh.begin_date = (select max(fh1.begin_date) " +
					"from form_history fh1 " +
					"where fh.form_id = fh1.form_id " +
					"and fh1.begin_date <= nvl(?,sysdate)" +
					"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate)))");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setLong(1, respondentId);
			ps.setDate(2, repDate);
			ps.setString(3, language_code);
			ps.setDate(4, repDate);
			ps.setDate(5, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				//ApprovalItem item = getApprovalItemFromResultSet(rs);
				ApprovalItem item = new ApprovalItem();
				item.setId(rs.getLong("ID"));
				item.setBatchId(rs.getLong("BATCH_ID"));
				item.setUserId(rs.getLong("USER_ID"));
				item.setEntityId(rs.getLong("ENTITY_ID"));
				item.setRespondentId(rs.getLong("RESPONDENT_ID"));
				item.setReportDate(rs.getDate("REPORT_DATE"));
				if (item.getReportDate() != null) {
					item.setsReportDate(format.format(item.getReportDate()));
				}
				item.setReceivedDate(rs.getDate("RECEIVED_DATE"));
				if (item.getReceivedDate() != null) {
					item.setsReceivedDate(format.format(item.getReceivedDate()));
				}
				item.setIsApproved(rs.getLong("IS_APPROVED"));
				item.setFormName(rs.getString("FORM_NAME"));
				item.setTitle(rs.getString("NAME"));
				item.setApprovalDate(rs.getTimestamp("APPROVAL_DATE"));
				if (item.getApprovalDate() != null) {
					item.setsApprovalDate(format1.format(item.getApprovalDate()));
				}
				item.setRespName(rs.getString("RESP_NAME"));
				item.setStatus(rs.getString("STATUS"));
				result.add(item);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try { rs.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
			try { connection.close(); } catch (Exception e) {}
		}
		return result;
	}*/

	/*@Override
	public List<ApprovalItem> infoNotSubmittedReports(Long respondentId, String language_code, Date reportDate) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ApprovalItem> result = new ArrayList<ApprovalItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement(
					"select rownum as id, " +
							"t.* " +
						"from (select sf.form_name, " +
									"fh.name " +
								"from subjecttype_forms sf, " +
								"forms f," +
								"form_history fh,  " +
						"        (select " +
						"           e.id as id, " +
						"           pkg_eav_util.get_string_value(e.id, 'name') as name, " +
						"			pkg_eav_util.get_string_value(e.id, 'subject_type.code') as subject_type_code" +
						"        from " +
						"          eav_be_entities e, " +
						"          eav_m_classes c " +
						"        where " +
						"         c.id = e.class_id and " +
						"         c.name = 'ref_respondent') r" +
					" WHERE r.id = ? " +
						"and sf.code = r.subject_type_code " +
						"and sf.form_name = f.code " +
						"and f.id = fh.form_id " +
						"and fh.begin_date <= nvl(?,sysdate)" +
						"and (fh.end_date is null or fh.end_date > nvl(?,sysdate)) " +
					" minus " +
					" select a.form_name, " +
							"fh.name " +
						"from approval a, " +
							"forms f, " +
							"form_history fh " +
					" where a.form_name = f.code " +
						"AND TRUNC(a.report_date) = TRUNC(?) " +
						"AND fh.language_code = ?" +
						"AND f.id = fh.form_id " +
						"AND fh.begin_date = (select max(fh1.begin_date) " +
												"from form_history fh1 " +
												"where fh.form_id = fh1.form_id " +
												"and fh1.begin_date <= nvl(?,sysdate)" +
												"and (fh1.end_date is null or fh1.end_date > nvl(?,sysdate))) " +
						"AND a.respondent_id = ?) t");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setLong(1, respondentId);
			ps.setDate(2, repDate);
			ps.setDate(3, repDate);
			ps.setDate(4, repDate);
			ps.setString(5, language_code);
			ps.setDate(6, repDate);
			ps.setDate(7, repDate);
			ps.setLong(8, respondentId);
			rs = ps.executeQuery();
			while (rs.next()) {
				//ApprovalItem item = getApprovalItemFromResultSet(rs);
				ApprovalItem item = new ApprovalItem();
				item.setId(rs.getLong("ID"));
*//*
				item.setBatchId(rs.getLong("BATCH_ID"));
				item.setUserId(rs.getLong("USER_ID"));
				item.setEntityId(rs.getLong("ENTITY_ID"));
				item.setReportDate(rs.getDate("REPORT_DATE"));
				item.setReceivedDate(rs.getDate("RECEIVED_DATE"));
				item.setIsApproved(rs.getLong("IS_APPROVED"));
				item.setFormName(rs.getString("FORM_NAME"));
*//*
				item.setTitle(rs.getString("NAME"));
*//*
				item.setApprovalDate(rs.getTimestamp("APPROVAL_DATE"));
				item.setRespName(rs.getString("RESP_NAME"));
				item.setStatus(rs.getString("STATUS"));
*//*
				result.add(item);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try { rs.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
			try { connection.close(); } catch (Exception e) {}
		}
		return result;
	}*/

	/*@Override
	public List<ApprovalItem> infoSubmittedRespsByReport(String form_code, Date reportDate) {
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
		SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ApprovalItem> result = new ArrayList<ApprovalItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select rp.id,\n" +
					"       decode(is_approved, 0, 'Не утвержден', '1', 'Утвержден') as status,\n" +
					"       a.respondent_id as resp_id,\n" +
					"       pkg_eav_util.get_string_value(a.respondent_id, 'name') as resp_name,\n" +
					"       rp.idn,\n" +
					"       (select min(rps.status_date)\n" +
					"          from report_status_history rps\n" +
					"         where rps.report_id = rp.id\n" +
					"           and rps.status_code = 'COMPLETED') as received_date,\n" +
					"       (select max(rps.status_date)\n" +
					"          from report_status_history rps\n" +
					"         where rps.report_id = rp.id\n" +
					"           and rps.status_code = 'APPROVED') as approval_date\n" +
					"           \n" +
					"  from approval a,\n" +
					"       reports rp\n" +
					"where rp.form_code = ? AND TRUNC(a.report_date) = TRUNC(?)" +
					"   and TRUNC(a.report_date) = TRUNC(rp.report_date)\n" +
					"   and get_respondent_bin(a.respondent_id, a.report_date) = rp.idn\n" +
					"   and a.form_name = rp.form_code");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setString(1, form_code);
			ps.setDate(2, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				//ApprovalItem item = getApprovalItemFromResultSet(rs);
				ApprovalItem item = new ApprovalItem();
				item.setId(rs.getLong("ID"));
				item.setReceivedDate(rs.getDate("RECEIVED_DATE"));
				if (item.getReceivedDate() != null) {
					item.setsReceivedDate(format.format(item.getReceivedDate()));
				}
				item.setApprovalDate(rs.getTimestamp("APPROVAL_DATE"));
				if (item.getApprovalDate() != null) {
					item.setsApprovalDate(format1.format(item.getApprovalDate()));
				}
				item.setRespondentId(rs.getLong("RESP_ID"));
				item.setBin(rs.getString("IDN"));
				item.setRespName(rs.getString("RESP_NAME"));
				item.setStatus(rs.getString("STATUS"));
				result.add(item);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try { rs.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
			try { connection.close(); } catch (Exception e) {}
		}
		return result;
	}*/

	/*@Override
	public List<ApprovalItem> infoNotSubmittedRespsByReport(String form_code, Date reportDate) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ApprovalItem> result = new ArrayList<ApprovalItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select rownum as id, t.* from (" +
					"select pkg_eav_util.get_string_value(e.id, 'name') as resp_name\n" +
					"  from eav_be_entities e,\n" +
					"       eav_m_classes c\n" +
					" where c.id = e.class_id \n" +
					"   and c.name = 'ref_respondent'\n" +
					"   and pkg_eav_util.get_string_value(e.id, 'subject_type.code') in (select sf.code \n" +
					"                                                                      from subjecttype_forms sf\n" +
					"                                                                     where sf.form_name = ?)\n" +
					"minus\n" +
					"select pkg_eav_util.get_string_value(a.respondent_id, 'name') as resp_name\n" +
					"  from approval a,\n" +
					"       reports rp\n" +
					"where rp.form_code = ? AND TRUNC(a.report_date) = TRUNC(?)" +
					"  and TRUNC(a.report_date) = TRUNC(rp.report_date)\n" +
					"  and get_respondent_bin(a.respondent_id, a.report_date) = rp.idn\n" +
					"  and a.form_name = rp.form_code) t");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setString(1, form_code);
			ps.setString(2, form_code);
			ps.setDate(3, repDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				//ApprovalItem item = getApprovalItemFromResultSet(rs);
				ApprovalItem item = new ApprovalItem();
				item.setId(rs.getLong("ID"));
				item.setRespName(rs.getString("RESP_NAME"));
				result.add(item);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try { rs.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
			try { connection.close(); } catch (Exception e) {}
		}
		return result;
	}*/


	@Override
	public void savePermissions(Set<Permission> permissions, Set<PermissionFormContainer> pfContainers, Set<PermissionDepartment> departments, Set<PermissionSubjectType> subjectTypes, Set<PermissionRespondent> respondents,
						 long selectedUserGroupId, long selectedUserId, Long userId, String userLocation, AuditEvent auditEvent, Date date){

		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);

			Long parentAEId = insertAuditEvent(auditEvent, connection);

			boolean fillRightItems = false;
			boolean fillDepartments = false;
			boolean fillSubjectTypes = false;
			boolean fillCreditors = false;
			boolean fillForms = false;

			if (permissions.size() > 0) {
				fillRightItems = true;
				if (selectedUserGroupId != 0)
					saveGroupCommonPermissions(permissions, selectedUserGroupId, userId, userLocation, date, parentAEId, connection);
				else
					saveUserCommonPermissions(permissions, selectedUserId, userId, userLocation, date, parentAEId, connection);
			}

			if (respondents.size() > 0) {
				fillCreditors = true;
				if (selectedUserGroupId != 0)
					saveUserGroupRespondents(respondents, selectedUserGroupId, userId, userLocation, date, parentAEId, connection);
				else
					saveUserRespondents(respondents, selectedUserId, userId, userLocation, date, parentAEId, connection);
			}

			if (departments.size() > 0) {
				fillDepartments = true;
				if (selectedUserGroupId != 0)
					saveUserGroupPermissionDepartments(departments, selectedUserGroupId, userId, userLocation, date, parentAEId, connection);
				else
					saveUserPermissionDepartments(departments, selectedUserId, userId, userLocation, date, parentAEId, connection);
			}

			if (subjectTypes.size() > 0) {
				fillSubjectTypes = true;
				if (selectedUserGroupId != 0)
					saveUserGroupPermissionSubjectTypes(subjectTypes, selectedUserGroupId, userId, userLocation, date, parentAEId, connection);
				else
					saveUserPermissionSubjectTypes(subjectTypes, selectedUserId, userId, userLocation, date, parentAEId, connection);
			}

			if (pfContainers.size() > 0) {
				fillForms = true;
				if (selectedUserGroupId != 0)
					updateUserGroupPermissionContainers(selectedUserGroupId, new ArrayList<PermissionFormContainer>(pfContainers), userId, userLocation, date, parentAEId, connection);
				else
					updateUserPermissionContainers(selectedUserId, new ArrayList<PermissionFormContainer>(pfContainers), userId, userLocation, date, parentAEId, connection);
			}

			if (fillRightItems || fillDepartments || fillSubjectTypes || fillCreditors || fillForms) {
				if (selectedUserGroupId != 0) {
					Set<Long> userIds = getGroupUsers(selectedUserGroupId, connection);
					for (Long id : userIds) {
						fillUserPermissions(id, fillRightItems, fillDepartments, fillSubjectTypes, fillCreditors, fillForms, connection);
					}
				} else {
					fillUserPermissions(selectedUserId, fillRightItems, fillDepartments, fillSubjectTypes, fillCreditors, fillForms, connection);
				}
			}

			connection.commit();
		} catch (SQLException e) {
			if (connection != null) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} catch (Exception e) {
			if (connection != null) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
	}

	// Permissions for UserGroup
	@Override
	public void saveGroupCommonPermissions(Set<Permission> permissions, long selectedUserGroupId, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException {
		AuditEvent auditEvent = new AuditEvent();
		auditEvent.setCodeObject("COMMON_PERMISSIONS_GROUP");
		auditEvent.setNameObject("Общие права на группу");
		auditEvent.setIdKindEvent(66L);
		auditEvent.setDateEvent(date);
		auditEvent.setIdRefRespondent(null);
		auditEvent.setDateIn(date);
		auditEvent.setRecId(null);
		auditEvent.setUserId(userId);
		auditEvent.setUserLocation(userLocation);
		auditEvent.setParentId(parentAEId);
		Long aeId = insertAuditEvent(auditEvent, connection);
		auditEvent.setParentId(aeId);

		for (Permission permission : permissions) {
			auditEvent.setCodeObject(permission.getName());
			auditEvent.setNameObject(permission.getTitleRu());
			if ((permission.getItemId() == null || permission.getItemId() == 0) && permission.isActive()) {
				auditEvent.setIdKindEvent(67L);
				insertUserGroupPermission(selectedUserGroupId, permission, connection, auditEvent);
			} else if ((permission.getItemId() != null && permission.getItemId() != 0) && !permission.isActive()) {
				auditEvent.setIdKindEvent(68L);
				deleteUserGroupPermission(permission.getItemId(), connection, auditEvent);
			}
		}
	}

	@Override
	public String getGroupPrefix() {
		return groupPrefix;
	}

	@Override
	public List<Permission> getAllPermissionByUserGroup(long userGroupId, long roleId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Permission> result = new ArrayList<Permission>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select p.*, p.is_active, p.item_id, connect_by_isleaf is_leaf" +
					" from (select p1.*, gr.is_active, gr.id as item_id from RIGHT_ITEMS p1, ROLE_RIGHT_ITEMS ri," +
					" (select gr1.* from GROUP_RIGHT_ITEMS gr1 where group_id = ?) gr" +
					" where p1.for_forms = 0 and p1.id = ri.right_item_id and ri.role_id = ? and p1.id = gr.right_item_id(+)) p" +
					" start with p.parent is null connect by prior p.id = p.parent order siblings by ord");

			ps.setLong(1, userGroupId);
			ps.setLong(2, roleId);
			rs = ps.executeQuery();
			while (rs.next()) {
				Permission permission = new Permission();
				permission.setId(rs.getLong("ID"));
				permission.setParentId(rs.getLong("PARENT"));
				permission.setName(rs.getString("NAME"));
				permission.setTitleKz(rs.getString("TITLE_KAZ"));
				permission.setTitleRu(rs.getString("TITLE_RUS"));
				permission.setTitleEn(rs.getString("TITLE_ENG"));
				permission.setItemId(rs.getLong("ITEM_ID"));
				permission.setActive(rs.getInt("IS_ACTIVE") > 0);
				permission.setLeaf(rs.getInt("IS_LEAF") > 0);
				result.add(permission);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public void insertUserGroupPermission(long userGroupId, Permission permission, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			String seqNextValue = "SEQ_GROUP_RIGHT_ITEMS_ID.nextval";
			ps = connection.prepareStatement("INSERT INTO GROUP_RIGHT_ITEMS" +
					" (id, group_id, right_item_id, is_active) VALUES (" + seqNextValue + ", ?, ?, ?)");
			ps.setLong(1, userGroupId);
			ps.setLong(2, permission.getId());
			ps.setBoolean(3, permission.isActive());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");

			deleteSamePermissionInUsers(permission.getId(), userGroupId, permission.isActive(), connection);

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);
			if(localCon)
				connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	private void deleteSamePermissionInUsers(long rightItemId, long userGroupId, boolean isActive, Connection connection){
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("DELETE FROM USER_RIGHT_ITEMS\n" +
					"WHERE ID IN (\n" +
					"  SELECT ri.id\n" +
					"  FROM USER_RIGHT_ITEMS ri\n" +
					"    INNER JOIN GROUP_USERS gu ON ri.USER_ID = gu.USER_ID AND gu.GROUP_ID = ?\n" +
					"  WHERE ri.RIGHT_ITEM_ID = ?\n" +
					"        AND ri.IS_ACTIVE = ?\n" +
					"        AND NOT exists(SELECT 'x'\n" +
					"                       FROM GROUP_RIGHT_ITEMS gri\n" +
					"                         INNER JOIN GROUP_USERS gu2 ON gri.GROUP_ID = gu2.GROUP_ID\n" +
					"                       WHERE gri.RIGHT_ITEM_ID = ? AND gu2.GROUP_ID <> ? AND gu2.USER_ID = gu.USER_ID)\n" +
					")");
			ps.setLong(1, userGroupId);
			ps.setLong(2, rightItemId);
			ps.setBoolean(3, isActive);
			ps.setLong(4, rightItemId);
			ps.setLong(5, userGroupId);
			int affectedRows = ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	private void deleteSamePermissionInUsers(long groupRightItemId, Connection connection){
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("DELETE FROM USER_RIGHT_ITEMS\n" +
					"WHERE ID IN (\n" +
					"  SELECT ri.id\n" +
					"  FROM USER_RIGHT_ITEMS ri\n" +
					"    INNER JOIN GROUP_RIGHT_ITEMS g ON g.RIGHT_ITEM_ID = ri.RIGHT_ITEM_ID\n" +
					"    INNER JOIN GROUP_USERS gu ON ri.USER_ID = gu.USER_ID AND gu.GROUP_ID = g.GROUP_ID\n" +
					"  WHERE g.ID = ? AND ri.IS_ACTIVE=0\n" +
					"        AND NOT exists(SELECT 'x'\n" +
					"                       FROM GROUP_RIGHT_ITEMS gri\n" +
					"                         INNER JOIN GROUP_USERS gu2 ON gri.GROUP_ID = gu2.GROUP_ID\n" +
					"                       WHERE gri.RIGHT_ITEM_ID = g.RIGHT_ITEM_ID AND gu2.GROUP_ID <> g.GROUP_ID AND gu2.USER_ID = gu.USER_ID)\n" +
					")");
			ps.setLong(1, groupRightItemId);
			int affectedRows = ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	private void deleteSameDepartmentsInUsers(long departmentRecId, long userGroupId, boolean isActive, Connection connection){
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("DELETE FROM USER_DEPARTMENTS\n" +
					"WHERE ID IN (\n" +
					"  SELECT ri.id\n" +
					"  FROM USER_DEPARTMENTS ri\n" +
					"    INNER JOIN GROUP_USERS gu ON ri.USER_ID = gu.USER_ID AND gu.GROUP_ID = ?\n" +
					"  WHERE ri.REF_DEPARTMENT_REC_ID = ?\n" +
					"        AND ri.IS_ACTIVE = ?\n" +
					"        AND NOT exists(SELECT 'x'\n" +
					"                       FROM GROUP_DEPARTMENTS gri\n" +
					"                         INNER JOIN GROUP_USERS gu2 ON gri.GROUP_ID = gu2.GROUP_ID\n" +
					"                       WHERE gri.REF_DEPARTMENT_REC_ID = ? AND gu2.GROUP_ID <> ? AND gu2.USER_ID = gu.USER_ID)\n" +
					")");
			ps.setLong(1, userGroupId);
			ps.setLong(2, departmentRecId);
			ps.setBoolean(3, isActive);
			ps.setLong(4, departmentRecId);
			ps.setLong(5, userGroupId);
			int affectedRows = ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	private void deleteSameDepartmentsInUsers(long groupDepartmentsId, Connection connection){
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("DELETE FROM USER_DEPARTMENTS\n" +
					"WHERE ID IN (\n" +
					"  SELECT ri.id\n" +
					"  FROM USER_DEPARTMENTS ri\n" +
					"    INNER JOIN GROUP_DEPARTMENTS g ON g.REF_DEPARTMENT_REC_ID = ri.REF_DEPARTMENT_REC_ID\n" +
					"    INNER JOIN GROUP_USERS gu ON ri.USER_ID = gu.USER_ID AND gu.GROUP_ID = g.GROUP_ID\n" +
					"  WHERE g.ID = ? AND ri.IS_ACTIVE=0\n" +
					"        AND NOT exists(SELECT 'x'\n" +
					"                       FROM GROUP_DEPARTMENTS gri\n" +
					"                         INNER JOIN GROUP_USERS gu2 ON gri.GROUP_ID = gu2.GROUP_ID\n" +
					"                       WHERE gri.REF_DEPARTMENT_REC_ID = g.REF_DEPARTMENT_REC_ID AND gu2.GROUP_ID <> g.GROUP_ID AND gu2.USER_ID = gu.USER_ID)\n" +
					")");
			ps.setLong(1, groupDepartmentsId);
			int affectedRows = ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	private void deleteSameSubjectTypesInUsers(long subjectTypeRecId, long userGroupId, boolean isActive, Connection connection){
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("DELETE FROM USER_SUBJECT_TYPES\n" +
					"WHERE ID IN (\n" +
					"  SELECT ri.id\n" +
					"  FROM USER_SUBJECT_TYPES ri\n" +
					"    INNER JOIN GROUP_USERS gu ON ri.USER_ID = gu.USER_ID AND gu.GROUP_ID = ?\n" +
					"  WHERE ri.REF_SUBJECT_TYPE_REC_ID = ?\n" +
					"        AND ri.IS_ACTIVE = ?\n" +
					"        AND NOT exists(SELECT 'x'\n" +
					"                       FROM GROUP_SUBJECT_TYPES gri\n" +
					"                         INNER JOIN GROUP_USERS gu2 ON gri.GROUP_ID = gu2.GROUP_ID\n" +
					"                       WHERE gri.REF_SUBJECT_TYPE_REC_ID = ? AND gu2.GROUP_ID <> ? AND gu2.USER_ID = gu.USER_ID)\n" +
					")");
			ps.setLong(1, userGroupId);
			ps.setLong(2, subjectTypeRecId);
			ps.setBoolean(3, isActive);
			ps.setLong(4, subjectTypeRecId);
			ps.setLong(5, userGroupId);
			int affectedRows = ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	private void deleteSameSubjectTypesInUsers(long groupDepartmentsId, Connection connection){
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("DELETE FROM USER_SUBJECT_TYPES\n" +
					"WHERE ID IN (\n" +
					"  SELECT ri.id\n" +
					"  FROM USER_SUBJECT_TYPES ri\n" +
					"    INNER JOIN GROUP_SUBJECT_TYPES g ON g.REF_SUBJECT_TYPE_REC_ID = ri.REF_SUBJECT_TYPE_REC_ID\n" +
					"    INNER JOIN GROUP_USERS gu ON ri.USER_ID = gu.USER_ID AND gu.GROUP_ID = g.GROUP_ID\n" +
					"  WHERE g.ID = ? AND ri.IS_ACTIVE=0\n" +
					"        AND NOT exists(SELECT 'x'\n" +
					"                       FROM GROUP_SUBJECT_TYPES gri\n" +
					"                         INNER JOIN GROUP_USERS gu2 ON gri.GROUP_ID = gu2.GROUP_ID\n" +
					"                       WHERE gri.REF_SUBJECT_TYPE_REC_ID = g.REF_SUBJECT_TYPE_REC_ID AND gu2.GROUP_ID <> g.GROUP_ID AND gu2.USER_ID = gu.USER_ID)\n" +
					")");
			ps.setLong(1, groupDepartmentsId);
			int affectedRows = ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	private void deleteSameRespondentsInUsers(long respondentRecId, long userGroupId, boolean isActive, Connection connection){
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("DELETE FROM USER_RESPONDENTS\n" +
					"WHERE ID IN (\n" +
					"  SELECT ri.id\n" +
					"  FROM USER_RESPONDENTS ri\n" +
					"    INNER JOIN GROUP_USERS gu ON ri.USER_ID = gu.USER_ID AND gu.GROUP_ID = ?\n" +
					"  WHERE ri.REF_RESPONDENT_REC_ID = ?\n" +
					"        AND ri.IS_ACTIVE = ?\n" +
					"        AND NOT exists(SELECT 'x'\n" +
					"                       FROM GROUP_RESPONDENTS gri\n" +
					"                         INNER JOIN GROUP_USERS gu2 ON gri.GROUP_ID = gu2.GROUP_ID\n" +
					"                       WHERE gri.REF_RESPONDENT_REC_ID = ? AND gu2.GROUP_ID <> ? AND gu2.USER_ID = gu.USER_ID)\n" +
					")");
			ps.setLong(1, userGroupId);
			ps.setLong(2, respondentRecId);
			ps.setBoolean(3, isActive);
			ps.setLong(4, respondentRecId);
			ps.setLong(5, userGroupId);
			int affectedRows = ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	private void deleteSameRespondentsInUsers(long groupRespondentsId, Connection connection){
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("DELETE FROM USER_RESPONDENTS\n" +
					"WHERE ID IN (\n" +
					"  SELECT ri.id\n" +
					"  FROM USER_RESPONDENTS ri\n" +
					"    INNER JOIN GROUP_RESPONDENTS g ON g.REF_RESPONDENT_REC_ID = ri.REF_RESPONDENT_REC_ID\n" +
					"    INNER JOIN GROUP_USERS gu ON ri.USER_ID = gu.USER_ID AND gu.GROUP_ID = g.GROUP_ID\n" +
					"  WHERE g.ID = ? AND ri.IS_ACTIVE=0\n" +
					"        AND NOT exists(SELECT 'x'\n" +
					"                       FROM GROUP_RESPONDENTS gri\n" +
					"                         INNER JOIN GROUP_USERS gu2 ON gri.GROUP_ID = gu2.GROUP_ID\n" +
					"                       WHERE gri.REF_RESPONDENT_REC_ID = g.REF_RESPONDENT_REC_ID AND gu2.GROUP_ID <> g.GROUP_ID AND gu2.USER_ID = gu.USER_ID)\n" +
					")");
			ps.setLong(1, groupRespondentsId);
			int affectedRows = ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void updateUserGroupPermission(Permission permission) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			String stmt = "update GROUP_RIGHT_ITEMS set is_active = ?" +
					" where id = ?";
			ps = connection.prepareStatement(stmt);
			ps.setBoolean(1, permission.isActive());
			ps.setLong(2, permission.getItemId());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void deleteUserGroupPermission(long id, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}

			deleteSamePermissionInUsers(id, connection);

			ps = connection.prepareStatement("delete from GROUP_RIGHT_ITEMS where id=? ");
			ps.setLong(1, id);
			ps.executeUpdate();

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteUserGroupPermissions(long userGroupId, boolean isDeleteUserPermissions, Connection connection) {
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }

			if (isDeleteUserPermissions) {
				Set<Long> userIds = getGroupUsers(userGroupId, connection);
				for (Long id : userIds)
					deleteUserPermissions(id, connection);
			}

			ps = connection.prepareStatement("delete from GROUP_RIGHT_ITEMS where group_id=? ");
			ps.setLong(1, userGroupId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public List<PermissionFormContainer> getAllPfContainerByUserGroupNSubjectTypeFormTypeCode(long userGroupId, List<Long> stRecIds, String formTypeCode, List<RefNpaItem> npaList, String languageCode, Map<Long, String> refRespondents) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map<String, PermissionFormContainer> map = new HashMap<String, PermissionFormContainer>();
		List<PermissionFormContainer> result = new ArrayList<PermissionFormContainer>();
		if (stRecIds.size() == 0) {
			return result;
		}
		try {
			connection = getConnection();
			Map<Long, Long> respSubjectTypeMap = getRespondentSubjectTypeMap(new ArrayList<Long>(refRespondents.keySet()), connection);

			int paramIndex = 0;

			StringBuilder sb = new StringBuilder(
					"SELECT fh.name as form_name, " +
							"f.code as form_code, " +
							"f.id as form_id, " +
							"f.type_code as form_type_code, " +
							"sf.ref_subject_type_rec_id " +
							" FROM forms f, " +
							" v_report_history_short fh, " +
							" subjecttype_forms sf, " +
							" npa_forms nf " +
							"WHERE f.code = sf.form_code " +
							" and fh.id = nf.form_history_id(+) " +
							" and f.id = fh.form_id");

			StringBuilder sbS = new StringBuilder();
			for (int i = 0; i < stRecIds.size(); i++) {
				if (i > 0)
					sbS.append(",");
				sbS.append(stRecIds.get(i));
			}
			sb.append(" and sf.ref_subject_type_rec_id in (" + sbS.toString() + ") ");
			sb.append(" and f.type_code=? ");

			if (npaList != null && npaList.size() > 0) {
				StringBuilder sbNpa = new StringBuilder();
				Boolean noNpa = false;
				for (int i = 0; i < npaList.size(); i++) {
					if (npaList.get(i).getId() == 0)
						noNpa = true;
					if (i > 0)
						sbNpa.append(",");
					sbNpa.append(npaList.get(i).getRecId());
				}
				if (noNpa) {
					sb.append(" and (nf.npa_rec_id is null or nf.npa_rec_id in (" + sbNpa.toString() + "))");
				} else {
					sb.append(" and nf.npa_rec_id in (" + sbNpa.toString() + ")");
				}
			}

			sb.append(" order by f.code ");

			ps = connection.prepareStatement(sb.toString());
			ps.setString(++paramIndex, formTypeCode);

			rs = ps.executeQuery();
			while (rs.next()) {
				String formCode = rs.getString("FORM_CODE");
				PermissionFormContainer pfContainer;
				if(map.containsKey(formCode)) {
					pfContainer = map.get(formCode);
				} else {
					pfContainer = new PermissionFormContainer();
					pfContainer.setId(rs.getLong("FORM_ID"));
					pfContainer.setFormCode(rs.getString("FORM_CODE"));
					pfContainer.setFormName(rs.getString("FORM_NAME"));
					pfContainer.setFormTypeCode(rs.getString("FORM_TYPE_CODE"));
					pfContainer.setFormTypeName(Form.resMap.get(languageCode + "_" + pfContainer.getFormTypeCode()));
					map.put(pfContainer.getFormCode(), pfContainer);
				}
				if (formTypeCode.equals(Form.Type.CONSOLIDATED.name()) || formTypeCode.equals(Form.Type.OUTPUT.name())) {
					pfContainer.setRefRespondents(refRespondents);
				} else {
					long stRecId = rs.getLong("ref_subject_type_rec_id");
					for (Long respRecId : refRespondents.keySet()) {
						if (!respSubjectTypeMap.containsKey(respRecId)) continue;

						if (stRecId == respSubjectTypeMap.get(respRecId))
							pfContainer.getRefRespondents().put(respRecId, refRespondents.get(respRecId));
					}
				}
			}
			List<PermissionForm> permissionForms;
			if (formTypeCode.equals(Form.Type.CONSOLIDATED.name()) || formTypeCode.equals(Form.Type.OUTPUT.name())) {
				permissionForms = getOutputPermissionFormsByUserGroup(userGroupId, connection);
			} else {
				permissionForms = getPermissionFormsByUserGroup(userGroupId, refRespondents, connection);
			}
			for (PermissionForm permissionForm : permissionForms) {
				if (map.containsKey(permissionForm.getFormCode())) {
					PermissionFormContainer pfContainer = map.get(permissionForm.getFormCode());
					pfContainer.addItem(permissionForm);
				}
			}
			for (PermissionFormContainer pfContainer : map.values()) {
				pfContainer.updateAllStates();
				result.add(pfContainer);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	private void updateUserGroupPermissionContainers(long selectedUserGroupId, List<PermissionFormContainer> containers, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException  {
		boolean localCon = false;
		CallableStatement stmt = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
			}
			stmt = connection.prepareCall("BEGIN update_group_permissions(?, ?, ?, ?, ?, ?, ?, ?); END;");
			List<Struct> permissionForms = new ArrayList<Struct>();
			for (PermissionFormContainer pfContainer : containers) {
				for (Map.Entry<String, Map<Long, PermissionForm>> entry : pfContainer.getItems().entrySet()) {
					for (PermissionForm pf : entry.getValue().values()) {
						permissionForms.add(pf.toStruct(connection));
					}
				}
			}
			java.sql.Date sqlDate = new java.sql.Date(date.getTime());
			Struct[] structs = permissionForms.toArray(new Struct[permissionForms.size()]);
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			java.sql.Array arrayStructs = oraConn.createOracleArray("PERMISSION_FORM_TABLE", structs);
			stmt.setArray(1, arrayStructs);
			stmt.setLong(2, selectedUserGroupId);
			stmt.setLong(3, userId);
			stmt.setDate(4, sqlDate);
			stmt.setString(5, userLocation);
			stmt.setLong(6, parentAEId);
			stmt.registerOutParameter(7, OracleTypes.INTEGER);
			stmt.registerOutParameter(8, OracleTypes.VARCHAR);

			stmt.execute();

			int errCode = stmt.getInt(7);
			String errMsg = stmt.getString(8);

			if (errCode != 0) {
				throw new EJBException(errMsg);
			}

			arrayStructs.free();
		}catch (SQLException e){
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, stmt);
		}
	}

	@Override
	public void deleteUserGroupPermissionForms(long userGroupId, boolean isDeleteUserPermissions, Connection connection) {
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }

			if (isDeleteUserPermissions) {
				Set<Long> userIds = getGroupUsers(userGroupId, connection);
				for (Long id : userIds)
					deleteUserPermissionForms(id, connection);
			}

			ps = connection.prepareStatement("DELETE FROM GROUP_RESP_FORMS WHERE group_id = ?");
			ps.setLong(1, userGroupId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void saveUserGroupPermissionDepartments(Set<PermissionDepartment> departments, long selectedUserGroupId, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException{
		AuditEvent auditEvent = new AuditEvent();
		auditEvent.setCodeObject("DEPARTMENTS_PERMISSIONS_GROUP");
		auditEvent.setNameObject("Доступ группы на филиалы");
		auditEvent.setIdKindEvent(72L);
		auditEvent.setDateEvent(date);
		auditEvent.setIdRefRespondent(null);
		auditEvent.setDateIn(date);
		auditEvent.setRecId(null);
		auditEvent.setUserId(userId);
		auditEvent.setUserLocation(userLocation);
		auditEvent.setParentId(parentAEId);
		Long aeId = insertAuditEvent(auditEvent, connection);
		auditEvent.setParentId(aeId);

		for (PermissionDepartment department : departments) {
			auditEvent.setCodeObject(null);
			auditEvent.setNameObject(department.getDepartment().getNameRu());
			if ((department.getId() == null || department.getId().equals(0L)) && department.isActive()) {
				auditEvent.setIdKindEvent(73L);
				insertUserGroupPermissionDepartment(selectedUserGroupId, department, connection, auditEvent);
			} else if ((department.getId() != null && department.getId() != 0L) && !department.isActive()) {
				auditEvent.setIdKindEvent(74L);
				deleteUserGroupPermissionDepartment(department.getId(), connection, auditEvent);
			}
		}
	}

	@Override
	public List<PermissionDepartment> getAllDepartmentByUserGroup(long userGroupId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PermissionDepartment> result = new ArrayList<PermissionDepartment>();
		Map<Long, PermissionDepartment> map = new HashMap<Long, PermissionDepartment>();
		for(RefDepartmentItem department:(List<RefDepartmentItem>) reference.getRefAbstractByFilterList(RefDepartmentItem.REF_CODE,new RefDepartmentItem(2L), new Date())){
			PermissionDepartment pr = new PermissionDepartment(department);
			result.add(pr);
			map.put(pr.getDepartment().getRecId(), pr);
		}

		try {
			connection = getConnection();
			ps = connection.prepareStatement("select * from group_departments where group_id=?");

			ps.setLong(1, userGroupId);
			rs = ps.executeQuery();
			while (rs.next()) {
				PermissionDepartment pr = map.get(rs.getLong("REF_DEPARTMENT_REC_ID"));
				if (pr == null) continue;
				pr.setId(rs.getLong("ID"));
				pr.setActive(rs.getInt("IS_ACTIVE") > 0);
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
	public void insertUserGroupPermissionDepartment(long userGroupId, PermissionDepartment department, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			ps = connection.prepareStatement("INSERT INTO group_departments" +
					" (id, group_id, ref_department_rec_id, is_active) VALUES (SEQ_GROUP_DEPARTMENTS_ID.nextval, ?, ?, ?)");
			ps.setLong(1, userGroupId);
			ps.setLong(2, department.getDepartment().getRecId());
			ps.setBoolean(3, department.isActive());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");

			deleteSameDepartmentsInUsers(department.getDepartment().getRecId(), userGroupId, department.isActive(), connection);

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();

		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteUserGroupPermissionDepartment(long id, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}

			deleteSameDepartmentsInUsers(id, connection);

			ps = connection.prepareStatement("delete from group_departments where id=? ");
			ps.setLong(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Deleting items failed, no rows affected.");

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteUserGroupPermissionDepartments(long userGroupId, boolean isDeleteUserPermissions, Connection connection) {
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }

			if (isDeleteUserPermissions) {
				Set<Long> userIds = getGroupUsers(userGroupId, connection);
				for (Long id : userIds)
					deleteUserPermissionDepartments(id, connection);
			}

			ps = connection.prepareStatement("delete from group_departments where group_id=? ");
			ps.setLong(1, userGroupId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void saveUserGroupPermissionSubjectTypes(Set<PermissionSubjectType> subjectTypes, long selectedUserGroupId, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException {
		AuditEvent auditEvent = new AuditEvent();
		auditEvent.setCodeObject("SUBJECT_TYPES_PERMISSIONS_GROUP");
		auditEvent.setNameObject("Доступ группы на типы субъектов");
		auditEvent.setIdKindEvent(118L);
		auditEvent.setDateEvent(date);
		auditEvent.setIdRefRespondent(null);
		auditEvent.setDateIn(date);
		auditEvent.setRecId(null);
		auditEvent.setUserId(userId);
		auditEvent.setUserLocation(userLocation);
		auditEvent.setParentId(parentAEId);
		Long aeId = insertAuditEvent(auditEvent, connection);
		auditEvent.setParentId(aeId);

		for (PermissionSubjectType subjectType : subjectTypes) {
			auditEvent.setCodeObject(null);
			auditEvent.setNameObject(subjectType.getSubjectType().getNameRu());
			if ((subjectType.getId() == null || subjectType.getId().equals(0L)) && subjectType.isActive()) {
				auditEvent.setIdKindEvent(119L);
				insertUserGroupPermissionSubjectType(selectedUserGroupId, subjectType, connection, auditEvent);
			} else if ((subjectType.getId() != null && subjectType.getId() != 0L) && !subjectType.isActive()) {
				auditEvent.setIdKindEvent(120L);
				deleteUserGroupPermissionSubjectType(subjectType.getId(), connection, auditEvent);
			}
		}
	}

	@Override
	public List<PermissionSubjectType> getAllPermissionSubjectTypesByUserGroup(long userGroupId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PermissionSubjectType> result = new ArrayList<PermissionSubjectType>();
		Map<Long, PermissionSubjectType> map = new HashMap<Long, PermissionSubjectType>();
		for(RefSubjectTypeItem subjectType:(List<RefSubjectTypeItem>)reference.getRefAbstractByFilterList(RefSubjectTypeItem.REF_CODE, null, new Date())){
			PermissionSubjectType pr = new PermissionSubjectType(subjectType);
			result.add(pr);
			map.put(pr.getSubjectType().getRecId(), pr);
		}

		try {
			connection = getConnection();
			ps = connection.prepareStatement("select * from group_subject_types where group_id=?");

			ps.setLong(1, userGroupId);
			rs = ps.executeQuery();
			while (rs.next()) {
				PermissionSubjectType pr = map.get(rs.getLong("REF_SUBJECT_TYPE_REC_ID"));
				if (pr == null) continue;
				pr.setId(rs.getLong("ID"));
				pr.setActive(rs.getInt("IS_ACTIVE") > 0);
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
	public void insertUserGroupPermissionSubjectType(long userGroupId, PermissionSubjectType subjectType, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			ps = connection.prepareStatement("INSERT INTO GROUP_SUBJECT_TYPES" +
					" (id, group_id, REF_SUBJECT_TYPE_REC_ID, is_active) VALUES (SEQ_GROUP_SUBJECT_TYPES_ID.nextval, ?, ?, ?)");
			ps.setLong(1, userGroupId);
			ps.setLong(2, subjectType.getSubjectType().getRecId());
			ps.setBoolean(3, subjectType.isActive());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");

			deleteSameSubjectTypesInUsers(subjectType.getSubjectType().getRecId(), userGroupId, subjectType.isActive(), connection);

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();

		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteUserGroupPermissionSubjectType(long id, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}

			deleteSameSubjectTypesInUsers(id, connection);

			ps = connection.prepareStatement("delete from GROUP_SUBJECT_TYPES where id=? ");
			ps.setLong(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Deleting items failed, no rows affected.");

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteUserGroupPermissionSubjectType(long userGroupId, boolean isDeleteUserPermissions, Connection connection) {
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }

			if (isDeleteUserPermissions) {
				Set<Long> userIds = getGroupUsers(userGroupId, connection);
				for (Long id : userIds)
					deleteUserPermissionSubjectTypes(id, connection);
			}

			ps = connection.prepareStatement("delete from GROUP_SUBJECT_TYPES where group_id=? ");
			ps.setLong(1, userGroupId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void saveUserGroupRespondents(Set<PermissionRespondent> respondents, long selectedUserGroupId, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException{
		boolean localCon = false;
		CallableStatement stmt = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
			}
			stmt = connection.prepareCall("BEGIN update_group_respondents(?, ?, ?, ?, ?, ?, ?, ?); END;");
			List<Struct> permissionResps = new ArrayList<Struct>();
			for(PermissionRespondent resp:respondents){
				permissionResps.add(resp.toStruct(connection));
			}
			java.sql.Date sqlDate = new java.sql.Date(date.getTime());
			Struct[] structs = permissionResps.toArray(new Struct[permissionResps.size()]);
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			java.sql.Array arrayStructs = oraConn.createOracleArray("PERMISSION_RESP_TABLE", structs);
			stmt.setArray(1, arrayStructs);
			stmt.setLong(2, selectedUserGroupId);
			stmt.setLong(3, userId);
			stmt.setDate(4, sqlDate);
			stmt.setString(5, userLocation);
			stmt.setLong(6, parentAEId);
			stmt.registerOutParameter(7, OracleTypes.INTEGER);
			stmt.registerOutParameter(8, OracleTypes.VARCHAR);

			stmt.execute();

			int errCode = stmt.getInt(7);
			String errMsg = stmt.getString(8);

			if (errCode != 0) {
				throw new EJBException(errMsg);
			}

			arrayStructs.free();
		}catch (SQLException e){
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, stmt);
		}
	}

	@Override
	public List<PermissionRespondent> getAllRespondentByUserGroup(long userGroupId, Long refDepartmentRecId, Long refSubjectTypeRecId, Long refRespondentRecId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PermissionRespondent> result = new ArrayList<PermissionRespondent>();
		Map<Long, PermissionRespondent> map = new HashMap<Long, PermissionRespondent>();
		RefRespondentItem respondent = new RefRespondentItem();
		respondent.setRefDepartment(refDepartmentRecId == null || refDepartmentRecId.equals(0L) ? null : reference.getRefDepartmentId(refDepartmentRecId, new Date()));
		respondent.setRefSubjectType(refSubjectTypeRecId == null || refSubjectTypeRecId.equals(0L) ? null : reference.getRefSubjectTypeId(refSubjectTypeRecId, new Date()));
		respondent.setRecId(refRespondentRecId == null || refRespondentRecId == 0 ? null : refRespondentRecId);
		List<RefRespondentItem> respondents = (List<RefRespondentItem>)reference.getRefAbstractByFilterList(RefRespondentItem.REF_CODE, respondent, new Date());
		for(RefRespondentItem item:respondents) {
			PermissionRespondent pr = new PermissionRespondent(item);
			result.add(pr);
			map.put(pr.getRespondent().getRecId(), pr);
		}

		try {
			connection = getConnection();
			ps = connection.prepareStatement("select * from group_respondents t " +
					"where t.group_id=?");

			ps.setLong(1, userGroupId);
			rs = ps.executeQuery();
			while (rs.next()) {
				PermissionRespondent pr = map.get(rs.getLong("REF_RESPONDENT_REC_ID"));
				if (pr == null) continue;
				pr.setId(rs.getLong("ID"));
				pr.setActive(rs.getInt("IS_ACTIVE") > 0);
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
	public List<Long> getUserGroupPermissionRespondentsRecIds(long userGroupId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Long> result = new ArrayList<Long>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select REF_RESPONDENT_REC_ID from group_respondents t " +
					"where t.group_id=?");

			ps.setLong(1, userGroupId);
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getLong("REF_RESPONDENT_REC_ID"));
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
	public void insertUserGroupPermissionRespondent(long userGroupId, PermissionRespondent respondent, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			String seqNextValue = "SEQ_GROUP_RESPONDENTS_ID.nextval";
			ps = connection.prepareStatement("INSERT INTO group_respondents" +
					" (id, group_id, ref_respondent_rec_id, is_active) VALUES (" + seqNextValue + ", ?, ?, ?)");
			ps.setLong(1, userGroupId);
			ps.setLong(2, respondent.getRespondent().getRecId());
			ps.setBoolean(3, respondent.isActive());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");

			deleteSameRespondentsInUsers(respondent.getRespondent().getRecId(), userGroupId, respondent.isActive(), connection);

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();

		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void updateUserGroupPermissionRespondent(PermissionRespondent respondent) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			String stmt = "update group_respondents set is_active = ?" +
					" where id = ?";
			ps = connection.prepareStatement(stmt);
			ps.setBoolean(1, respondent.isActive());
			ps.setLong(2, respondent.getId());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void deleteUserGroupPermissionRespondent(long id, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}

			deleteSameRespondentsInUsers(id, connection);

			ps = connection.prepareStatement("delete from group_respondents where id=? ");
			ps.setLong(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Deleting items failed, no rows affected.");

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteUserGroupPermissionRespondents(long userGroupId, boolean isDeleteUserPermissions, Connection connection) {
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }

			if (isDeleteUserPermissions) {
				Set<Long> userIds = getGroupUsers(userGroupId, connection);
				for (Long id : userIds)
					deleteUserPermissionRespondents(id, connection);
			}

			ps = connection.prepareStatement("delete from group_respondents where group_id=? ");
			ps.setLong(1, userGroupId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	private List<PermissionForm> getPermissionFormsByUserGroup(long userGroupId, Map<Long, String> refRespondents, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		List<PermissionForm> result = new ArrayList<PermissionForm>();
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
			}
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			java.sql.Array arrayRespondents = oraConn.createARRAY("NUMBER_ARRAY", refRespondents.keySet().toArray(new Long[refRespondents.size()]));
			ps = connection.prepareStatement("SELECT\n" +
					"  fh.name                          AS form_name,\n" +
					"  f.code                           AS form_code,\n" +
					"  gf.REF_RESPONDENT_REC_ID,\n" +
					"  gf.id,\n" +
					"  (SELECT ri.name\n" +
					"   FROM right_items ri\n" +
					"   WHERE ri.id = gf.right_item_id) AS right_item,\n" +
					"  gf.is_active\n" +
					"FROM Forms f\n" +
					"  INNER JOIN GROUP_RESP_FORMS gf ON f.code = gf.form_code AND gf.group_id = ?\n" +
					"  INNER JOIN v_report_history_short fh ON f.id = fh.form_id\n" +
					"WHERE gf.REF_RESPONDENT_REC_ID IN (SELECT t.COLUMN_VALUE\n" +
					"                                   FROM TABLE (?) t)\n" +
					"ORDER BY f.code");

			ps.setLong(1, userGroupId);
			ps.setArray(2, arrayRespondents);

			rs = ps.executeQuery();
			while (rs.next()) {
				PermissionForm permissionForm = new PermissionForm();
				permissionForm.setItemId(userGroupId);
				permissionForm.setFormCode(rs.getString("FORM_CODE"));
				permissionForm.setId(rs.getLong("ID"));
				permissionForm.setFormName(rs.getString("FORM_NAME"));
				permissionForm.setPermissionName(rs.getString("RIGHT_ITEM"));
				permissionForm.setRefRespondentRecId(rs.getLong("REF_RESPONDENT_REC_ID"));
				String idn = refRespondents.get(permissionForm.getRefRespondentRecId());
				permissionForm.setIdn(idn);
				permissionForm.setActive(rs.getInt("IS_ACTIVE") > 0);
				permissionForm.setInitActive(rs.getInt("IS_ACTIVE") > 0);
				permissionForm.setForGroup(true);
				result.add(permissionForm);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	private List<PermissionForm> getOutputPermissionFormsByUserGroup(long userGroupId, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		List<PermissionForm> result = new ArrayList<PermissionForm>();
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
			}
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			ps = connection.prepareStatement("SELECT\n" +
					"  fh.name                          AS form_name,\n" +
					"  f.code                           AS form_code,\n" +
					"  gf.id,\n" +
					"  (SELECT ri.name\n" +
					"   FROM right_items ri\n" +
					"   WHERE ri.id = gf.right_item_id) AS right_item,\n" +
					"  gf.is_active\n" +
					"FROM Forms f\n" +
					"  INNER JOIN GROUP_FORMS gf ON f.code = gf.form_code AND gf.group_id = ?\n" +
					"  INNER JOIN v_report_history_short fh ON f.id = fh.form_id\n" +
					"ORDER BY f.code");

			ps.setLong(1, userGroupId);

			rs = ps.executeQuery();
			while (rs.next()) {
				PermissionForm permissionForm = new PermissionForm();
				permissionForm.setItemId(userGroupId);
				permissionForm.setFormCode(rs.getString("FORM_CODE"));
				permissionForm.setId(rs.getLong("ID"));
				permissionForm.setFormName(rs.getString("FORM_NAME"));
				permissionForm.setPermissionName(rs.getString("RIGHT_ITEM"));
				permissionForm.setRefRespondentRecId(0l); //fake rec_id
				permissionForm.setActive(rs.getInt("IS_ACTIVE") > 0);
				permissionForm.setInitActive(rs.getInt("IS_ACTIVE") > 0);
				permissionForm.setForGroup(true);
				result.add(permissionForm);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public boolean hasPermission(Long userId, String permissionName, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		boolean result = false;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
			}
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			ps = connection.prepareStatement("SELECT *\n" +
					"FROM F_SESSION_RIGHT_ITEMS s\n" +
					"WHERE s.USER_ID = ?\n" +
					"      AND s.RIGHT_ITEM_ID = (SELECT r.ID\n" +
					"                             FROM RIGHT_ITEMS r\n" +
					"                             WHERE r.NAME = ?)");

			ps.setLong(1, userId);
			ps.setString(2, permissionName);

			rs = ps.executeQuery();
			result = rs.next();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	// Permissions for User
	@Override
	public void saveUserCommonPermissions(Set<Permission> permissions, long selectedUserId, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException {
		AuditEvent auditEvent = new AuditEvent();
		auditEvent.setCodeObject("COMMON_PERMISSIONS_USER");
		auditEvent.setNameObject("Общие права для пользователя");
		auditEvent.setIdKindEvent(69L);
		auditEvent.setDateEvent(date);
		auditEvent.setIdRefRespondent(null);
		auditEvent.setDateIn(date);
		auditEvent.setRecId(null);
		auditEvent.setUserId(userId);
		auditEvent.setUserLocation(userLocation);
		auditEvent.setParentId(parentAEId);
		Long aeId = insertAuditEvent(auditEvent, connection);
		auditEvent.setParentId(aeId);

		for (Permission permission : permissions) {
			auditEvent.setCodeObject(permission.getName());
			auditEvent.setNameObject(permission.getTitleRu());
			auditEvent.setIdKindEvent(permission.isActive() ? 70L : 71L);

			if ((permission.getItemId() == null || permission.getItemId() == 0) && permission.isActive())
				insertUserPermission(selectedUserId, permission, connection, auditEvent);
			else if (permission.getItemId() != null && permission.getItemId() != 0) {
				if (permission.isInhActive() && !permission.isActive()) {
					if (permission.isForGroup())
						insertUserPermission(selectedUserId, permission, connection, auditEvent);
					else
						updateUserPermission(permission, connection, auditEvent);
				}
				else if (!permission.isInhActive() && permission.isActive())
					updateUserPermission(permission, connection, auditEvent);
				else
					deleteUserPermission(permission.getItemId(), connection, auditEvent);
			}
		}
	}


	@Override
	public List<Permission> getAllPermissionByUser(long userId, long roleId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Permission> result = new ArrayList<Permission>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select p.*, connect_by_isleaf is_leaf from (" +
					"          select p.id, p.parent, p.name, p.title_kaz, p.title_rus, p.title_eng," +
					"                 max(coalesce(u.is_active, g.is_active)) as is_active," +
					"                 max(coalesce(u.id, g.id)) as item_id," +
					"              max(g.is_active) as inh_is_active," +
					"              max(case when g.id is not null and u.id is null then 1 else 0 end) as for_group," +
					"              max(p.ord) as ord" +
					"          from (select p1.* from right_items p1, role_right_items ri" +
					"                 where p1.for_forms = 0 and p1.id = ri.right_item_id and ri.role_id=?) p" +
					"               left join user_right_items u on p.id=u.right_item_id and u.user_id=?" +
					"               left join (group_right_items g inner join group_users gu on g.group_id=gu.group_id and gu.user_id=?) on p.id=g.right_item_id" +
					"          group by p.id, p.parent, p.name, p.title_kaz, p.title_rus, p.title_eng" +
					"          ) p start with p.parent is null" +
					"           connect by prior p.id = p.parent" +
					"           order siblings by ord");

			ps.setLong(1, roleId);
			ps.setLong(2, userId);
			ps.setLong(3, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				Permission permission = new Permission();
				permission.setId(rs.getLong("ID"));
				permission.setParentId(rs.getLong("PARENT"));
				permission.setName(rs.getString("NAME"));
				permission.setTitleKz(rs.getString("TITLE_KAZ"));
				permission.setTitleRu(rs.getString("TITLE_RUS"));
				permission.setTitleEn(rs.getString("TITLE_ENG"));
				permission.setItemId(rs.getLong("ITEM_ID"));
				permission.setActive(rs.getInt("IS_ACTIVE") > 0);
				permission.setInhActive(rs.getInt("INH_IS_ACTIVE") > 0);
				permission.setForGroup(rs.getInt("FOR_GROUP") > 0);
				permission.setLeaf(rs.getInt("IS_LEAF") > 0);
				result.add(permission);
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
	public void insertUserPermission(long userId, Permission permission, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			String seqNextValue = "SEQ_USER_RIGHT_ITEMS_ID.nextval";
			ps = connection.prepareStatement("INSERT INTO USER_RIGHT_ITEMS" +
					" (id, user_id, right_item_id, is_active) VALUES (" + seqNextValue + ", ?, ?, ?)");
			ps.setLong(1, userId);
			ps.setLong(2, permission.getId());
			ps.setBoolean(3, permission.isActive());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();

		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void updateUserPermission(Permission permission, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.commit();
			}
			String stmt = "update USER_RIGHT_ITEMS set is_active = ?" +
					" where id = ?";
			ps = connection.prepareStatement(stmt);
			ps.setBoolean(1, permission.isActive());
			ps.setLong(2, permission.getItemId());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteUserPermission(long id, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			ps = connection.prepareStatement("delete from USER_RIGHT_ITEMS where id=? ");
			ps.setLong(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Deleting items failed, no rows affected.");

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	private void deleteUserPermissions(long userId, Connection connection) {
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("delete from USER_RIGHT_ITEMS where user_id=? ");
			ps.setLong(1, userId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public List<PermissionFormContainer> getAllPfContainerByUserNSubjectTypeFormTypeCode(long userId, List<Long> stRecIds, String formTypeCode, List<RefNpaItem> npaList, String languageCode, Map<Long, String> refRespondents) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map<String, PermissionFormContainer> map = new HashMap<String, PermissionFormContainer>();
		List<PermissionFormContainer> result = new ArrayList<PermissionFormContainer>();
		if (stRecIds.size() == 0) {
			return result;
		}
		try {
			connection = getConnection();
			Map<Long, Long> respSubjectTypeMap = getRespondentSubjectTypeMap(new ArrayList<Long>(refRespondents.keySet()), connection);

			int paramIndex = 0;

			StringBuilder sb = new StringBuilder(
					"select fh.name as form_name, " +
							"f.code as form_code, " +
							"f.id as form_id, " +
							"f.type_code as form_type_code, " +
							"sf.ref_subject_type_rec_id " +
					" from forms f," +
						" v_report_history_short fh," +
						" subjecttype_forms sf, " +
						" npa_forms nf " +
					" where f.code = sf.form_code " +
							" and fh.id = nf.form_history_id(+) "+
							" and f.id = fh.form_id ");

			StringBuilder sbS = new StringBuilder();
			for (int i = 0; i < stRecIds.size(); i++) {
				if (i > 0)
					sbS.append(",");
				sbS.append(stRecIds.get(i));
			}
			sb.append(" and sf.ref_subject_type_rec_id in (" + sbS.toString() + ") ");
			sb.append(" and f.type_code=? ");

			if(npaList != null && npaList.size() > 0) {
				StringBuilder sbNpa = new StringBuilder();
				Boolean noNpa = false;
				for (int i = 0; i < npaList.size(); i++) {
					if(npaList.get(i).getId() == 0)
						noNpa = true;
					if (i > 0)
						sbNpa.append(",");
					sbNpa.append(npaList.get(i).getRecId());
				}
				if (noNpa) {
					sb.append(" and (nf.npa_rec_id is null or nf.npa_rec_id in (" + sbNpa.toString() + "))");
				} else {
					sb.append(" and nf.npa_rec_id in (" + sbNpa.toString() + ")");
				}
			}

			sb.append(" order by f.code");

			ps = connection.prepareStatement(sb.toString());
			ps.setString(++paramIndex, formTypeCode);

			rs = ps.executeQuery();
			while (rs.next()) {
				String formCode = rs.getString("FORM_CODE");
				PermissionFormContainer pfContainer;
				if(map.containsKey(formCode)) {
					pfContainer = map.get(formCode);
				} else {
					pfContainer = new PermissionFormContainer();
					pfContainer.setId(rs.getLong("FORM_ID"));
					pfContainer.setFormCode(formCode);
					pfContainer.setFormName(rs.getString("FORM_NAME"));
					pfContainer.setFormTypeCode(rs.getString("FORM_TYPE_CODE"));
					pfContainer.setFormTypeName(Form.resMap.get(languageCode + "_" + pfContainer.getFormTypeCode()));
					map.put(pfContainer.getFormCode(), pfContainer);
				}
				if (formTypeCode.equals(Form.Type.CONSOLIDATED.name()) || formTypeCode.equals(Form.Type.OUTPUT.name())) {
					pfContainer.setRefRespondents(refRespondents);
				} else {
					long stRecId = rs.getLong("ref_subject_type_rec_id");
					for (Long respRecId : refRespondents.keySet()) {
						if (!respSubjectTypeMap.containsKey(respRecId)) continue;

						if (stRecId == respSubjectTypeMap.get(respRecId))
							pfContainer.getRefRespondents().put(respRecId, refRespondents.get(respRecId));
					}
				}
			}
			List<PermissionForm> permissionForms;
			if (formTypeCode.equals(Form.Type.OUTPUT.name()) || formTypeCode.equals(Form.Type.CONSOLIDATED.name())) {
				permissionForms = getOutputPermissionFormsByUser(userId, connection);
			} else {
				permissionForms = getPermissionFormsByUser(userId, refRespondents, connection);
			}
			for(PermissionForm permissionForm:permissionForms){
				if(map.containsKey(permissionForm.getFormCode())){
					PermissionFormContainer pfContainer = map.get(permissionForm.getFormCode());
					pfContainer.addItem(permissionForm);
				}
			}
			for(PermissionFormContainer pfContainer:map.values()){
				pfContainer.updateAllStates();
				result.add(pfContainer);
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

	private Map<Long, Long> getRespondentSubjectTypeMap(List<Long> respondentRecIds, Connection connection) {
		if (respondentRecIds.size() == 0) return new HashMap<Long, Long>();

		Map<Long, Long> map = new HashMap<Long, Long>();
		StringBuilder sb = new StringBuilder(
				"SELECT r.REC_ID, r.REF_SUBJECT_TYPE\n" +
						"FROM V_REF_RESPONDENT r\n" +
						"WHERE r.REC_ID IN ( ");
		int i = 0;
		for (Long respRecId : respondentRecIds) {
			if (i > 0) sb.append(",");
			sb.append(respRecId);
			i++;
		}
		sb.append(")\n" +
				"GROUP BY r.REC_ID, r.REF_SUBJECT_TYPE");
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sb.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				long respRecId = rs.getLong("REC_ID");
				long stRecId = rs.getLong("REF_SUBJECT_TYPE");
				map.put(respRecId, stRecId);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(null, ps, rs);
		}
		return map;
	}

	private void updateUserPermissionContainers(long selectedUserId, List<PermissionFormContainer> containers, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException {
		boolean localCon = false;
		CallableStatement stmt = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
			}
			stmt = connection.prepareCall("BEGIN update_user_permissions(?, ?, ?, ?, ?, ?, ?, ?); END;");
			List<Struct> permissionForms = new ArrayList<Struct>();
			for (PermissionFormContainer pfContainer : containers) {
				for (Map.Entry<String, Map<Long, PermissionForm>> entry : pfContainer.getItems().entrySet()) {
					for(PermissionForm pf:entry.getValue().values()){
						if (pf.isInitActive() != pf.isActive()) {
							permissionForms.add(pf.toStruct(connection));
						}
					}
				}
			}
			java.sql.Date sqlDate = new java.sql.Date(date.getTime());
			Struct[] structs = permissionForms.toArray(new Struct[permissionForms.size()]);
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			java.sql.Array arrayStructs = oraConn.createOracleArray("PERMISSION_FORM_TABLE", structs);
			stmt.setArray(1, arrayStructs);
			stmt.setLong(2, selectedUserId);
			stmt.setLong(3, userId);
			stmt.setDate(4, sqlDate);
			stmt.setString(5, userLocation);
			stmt.setLong(6, parentAEId);
			stmt.registerOutParameter(7, OracleTypes.INTEGER);
			stmt.registerOutParameter(8, OracleTypes.VARCHAR);

			stmt.execute();

			int errCode = stmt.getInt(7);
			String errMsg = stmt.getString(8);

			if (errCode != 0) {
				throw new EJBException(errMsg);
			}

			arrayStructs.free();
		}catch (SQLException e){
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, stmt);
		}
	}

	private void deleteUserPermissionForms(long userId, Connection connection) {
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("DELETE FROM USER_RESP_FORMS WHERE user_id = ?");
			ps.setLong(1, userId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void saveUserPermissionDepartments(Set<PermissionDepartment> departments, long selectedUserId, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException{
		AuditEvent auditEvent = new AuditEvent();
		auditEvent.setCodeObject("DEPARTMENTS_PERMISSIONS_USER");
		auditEvent.setNameObject("Доступ пользователя на филиалы");
		auditEvent.setIdKindEvent(75L);
		auditEvent.setDateEvent(date);
		auditEvent.setIdRefRespondent(null);
		auditEvent.setDateIn(date);
		auditEvent.setRecId(null);
		auditEvent.setUserId(userId);
		auditEvent.setUserLocation(userLocation);
		auditEvent.setParentId(parentAEId);
		Long aeId = insertAuditEvent(auditEvent, connection);
		auditEvent.setParentId(aeId);

		for (PermissionDepartment department : departments) {
			auditEvent.setCodeObject(null);
			auditEvent.setNameObject(department.getDepartment().getNameRu());
			auditEvent.setIdKindEvent(department.isActive() ? 76L : 77L);
			if ((department.getId() == null || department.getId() == 0) && department.isActive())
				insertUserPermissionDepartment(selectedUserId, department, connection, auditEvent);
			else if (department.getId() != null && department.getId() != 0) {
				if (department.isInhActive() && !department.isActive())
					if (department.isForGroup())
						insertUserPermissionDepartment(selectedUserId, department, connection, auditEvent);
					else
						updateUserPermissionDepartment(department, connection, auditEvent);
				else if (!department.isInhActive() && department.isActive())
					updateUserPermissionDepartment(department, connection, auditEvent);
				else
					deleteUserPermissionDepartment(department.getId(), connection, auditEvent);
			}
		}
	}

	@Override
	public List<PermissionDepartment> getAllPermissionDepartmentByUser(long userId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PermissionDepartment> result = new ArrayList<PermissionDepartment>();
		Map<Long, PermissionDepartment> map = new HashMap<Long, PermissionDepartment>();
		List<RefDepartmentItem> departments = (List<RefDepartmentItem>)reference.getRefAbstractByFilterList(RefDepartmentItem.REF_CODE, new RefDepartmentItem(2L), new Date());
		for(RefDepartmentItem item:departments){
			PermissionDepartment pr = new PermissionDepartment(item);
			result.add(pr);
			map.put(pr.getDepartment().getRecId(), pr);
		}

		try {
			connection = getConnection();
			ps = connection.prepareStatement("select nvl(u.ref_department_rec_id, g.ref_department_rec_id) ref_department_rec_id, nvl(u.id, g.id) id, " +
					" nvl(u.is_active, g.is_active) is_active, g.is_active inh_is_active, " +
					" case when g.id is not null and u.id is null then 1 else 0 end for_group " +
					" from (select *  from  user_departments where user_id=?) u " +
					" full join (select g.* from group_departments g inner join group_users gu on g.group_id=gu.group_id where gu.user_id=?) g on u.ref_department_rec_id=g.ref_department_rec_id");

			ps.setLong(1, userId);
			ps.setLong(2, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				PermissionDepartment pr = map.get(rs.getLong("REF_DEPARTMENT_REC_ID"));
				if (pr == null) continue;
				pr.setId(rs.getLong("ID"));
				pr.setActive(rs.getInt("IS_ACTIVE") > 0);
				pr.setInhActive(rs.getInt("INH_IS_ACTIVE") > 0);
				pr.setForGroup(rs.getInt("FOR_GROUP") > 0);
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
	public void insertUserPermissionDepartment(long userId, PermissionDepartment department, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			String seqNextValue = "SEQ_USER_DEPARTMENTS_ID.nextval";
			ps = connection.prepareStatement("INSERT INTO user_departments" +
					" (id, user_id, ref_department_rec_id, is_active) VALUES (" + seqNextValue + ", ?, ?, ?)");
			ps.setLong(1, userId);
			ps.setLong(2, department.getDepartment().getRecId());
			ps.setBoolean(3, department.isActive());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();

		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void updateUserPermissionDepartment(PermissionDepartment department, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			String stmt = "update user_departments set is_active = ?" +
					" where id = ?";
			ps = connection.prepareStatement(stmt);
			ps.setBoolean(1, department.isActive());
			ps.setLong(2, department.getId());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteUserPermissionDepartment(long id, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
			}
			ps = connection.prepareStatement("delete from user_departments where id=? ");
			ps.setLong(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Deleting items failed, no rows affected.");

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteUserPermissionDepartments(long userId, Connection connection) {
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("delete from user_departments where user_id=? ");
			ps.setLong(1, userId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void saveUserPermissionSubjectTypes(Set<PermissionSubjectType> subjectTypes, long selectedUserId, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException {
		AuditEvent auditEvent = new AuditEvent();
		auditEvent.setCodeObject("SUBJECT_TYPES_PERMISSIONS_USER");
		auditEvent.setNameObject("Доступ пользователя на типы субъектов");
		auditEvent.setIdKindEvent(121L);
		auditEvent.setDateEvent(date);
		auditEvent.setIdRefRespondent(null);
		auditEvent.setDateIn(date);
		auditEvent.setRecId(null);
		auditEvent.setUserId(userId);
		auditEvent.setUserLocation(userLocation);
		auditEvent.setParentId(parentAEId);
		Long aeId = insertAuditEvent(auditEvent, connection);
		auditEvent.setParentId(aeId);

		for (PermissionSubjectType subjectType : subjectTypes) {
			auditEvent.setCodeObject(null);
			auditEvent.setNameObject(subjectType.getSubjectType().getNameRu());
			auditEvent.setIdKindEvent(subjectType.isActive() ? 122L : 123L);
			if ((subjectType.getId() == null || subjectType.getId() == 0) && subjectType.isActive())
				insertUserPermissionSubjectType(selectedUserId, subjectType, connection, auditEvent);
			else if (subjectType.getId() != null && subjectType.getId() != 0) {
				if (subjectType.isInhActive() && !subjectType.isActive())
					if (subjectType.isForGroup())
						insertUserPermissionSubjectType(selectedUserId, subjectType, connection, auditEvent);
					else
						updateUserPermissionSubjectType(subjectType, connection, auditEvent);
				else if (!subjectType.isInhActive() && subjectType.isActive())
					updateUserPermissionSubjectType(subjectType, connection, auditEvent);
				else
					deleteUserPermissionSubjectType(subjectType.getId(), connection, auditEvent);
			}
		}
	}

	@Override
	public List<PermissionSubjectType> getAllPermissionSubjectTypesByUser(long userId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PermissionSubjectType> result = new ArrayList<PermissionSubjectType>();
		Map<Long, PermissionSubjectType> map = new HashMap<Long, PermissionSubjectType>();
		List<RefSubjectTypeItem> subjectTypes = (List<RefSubjectTypeItem>)reference.getRefAbstractByFilterList(RefSubjectTypeItem.REF_CODE, null, new Date());
		for(RefSubjectTypeItem item:subjectTypes){
			PermissionSubjectType pr = new PermissionSubjectType(item);
			result.add(pr);
			map.put(pr.getSubjectType().getRecId(), pr);
		}

		try {
			connection = getConnection();
			ps = connection.prepareStatement("select nvl(u.ref_subject_type_rec_id, g.ref_subject_type_rec_id) ref_subject_type_rec_id, nvl(u.id, g.id) id, " +
					" nvl(u.is_active, g.is_active) is_active, g.is_active inh_is_active, " +
					" case when g.id is not null and u.id is null then 1 else 0 end for_group " +
					" from (select *  from  USER_SUBJECT_TYPES where user_id=?) u " +
					" full join (select g.* from GROUP_SUBJECT_TYPES g inner join group_users gu on g.group_id=gu.group_id where gu.user_id=?) g on u.REF_SUBJECT_TYPE_REC_ID=g.REF_SUBJECT_TYPE_REC_ID");

			ps.setLong(1, userId);
			ps.setLong(2, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				PermissionSubjectType pr = map.get(rs.getLong("REF_SUBJECT_TYPE_REC_ID"));
				if (pr == null) continue;
				pr.setId(rs.getLong("ID"));
				pr.setActive(rs.getInt("IS_ACTIVE") > 0);
				pr.setInhActive(rs.getInt("INH_IS_ACTIVE") > 0);
				pr.setForGroup(rs.getInt("FOR_GROUP") > 0);
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
	public void insertUserPermissionSubjectType(long userId, PermissionSubjectType subjectType, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			String seqNextValue = "SEQ_USER_SUBJECT_TYPES_ID.nextval";
			ps = connection.prepareStatement("INSERT INTO USER_SUBJECT_TYPES" +
					" (id, user_id, REF_SUBJECT_TYPE_REC_ID, is_active) VALUES (" + seqNextValue + ", ?, ?, ?)");
			ps.setLong(1, userId);
			ps.setLong(2, subjectType.getSubjectType().getRecId());
			ps.setBoolean(3, subjectType.isActive());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void updateUserPermissionSubjectType(PermissionSubjectType subjectType, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			String stmt = "update user_subject_types set is_active = ?" +
					" where id = ?";
			ps = connection.prepareStatement(stmt);
			ps.setBoolean(1, subjectType.isActive());
			ps.setLong(2, subjectType.getId());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteUserPermissionSubjectType(long id, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			ps = connection.prepareStatement("delete from USER_SUBJECT_TYPES where id=? ");
			ps.setLong(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Deleting items failed, no rows affected.");

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteUserPermissionSubjectTypes(long userId, Connection connection) {
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("delete from USER_SUBJECT_TYPES where user_id=? ");
			ps.setLong(1, userId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void saveUserRespondents(Set<PermissionRespondent> respondents, long selectedUserId, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException{
		boolean localCon = false;
		CallableStatement stmt = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
			}
			stmt = connection.prepareCall("BEGIN update_user_respondents(?, ?, ?, ?, ?, ?, ?, ?); END;");
			List<Struct> permissionResps = new ArrayList<Struct>();
			for(PermissionRespondent resp:respondents){
				permissionResps.add(resp.toStruct(connection));
			}
			java.sql.Date sqlDate = new java.sql.Date(date.getTime());
			Struct[] structs = permissionResps.toArray(new Struct[permissionResps.size()]);
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			java.sql.Array arrayStructs = oraConn.createOracleArray("PERMISSION_RESP_TABLE", structs);
			stmt.setArray(1, arrayStructs);
			stmt.setLong(2, selectedUserId);
			stmt.setLong(3, userId);
			stmt.setDate(4, sqlDate);
			stmt.setString(5, userLocation);
			stmt.setLong(6, parentAEId);
			stmt.registerOutParameter(7, OracleTypes.INTEGER);
			stmt.registerOutParameter(8, OracleTypes.VARCHAR);

			stmt.execute();

			int errCode = stmt.getInt(7);
			String errMsg = stmt.getString(8);

			if (errCode != 0) {
				throw new EJBException(errMsg);
			}

			arrayStructs.free();
		}catch (SQLException e){
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, stmt);
		}
	}

	@Override
	public List<PermissionRespondent> getAllRespondentByUser(long userId, Long refDepartmentRecId, Long refSubjectTypeRecId, Long refRespondentRecId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PermissionRespondent> result = new ArrayList<PermissionRespondent>();
		Map<Long, PermissionRespondent> map = new HashMap<Long, PermissionRespondent>();
		RefRespondentItem respondent = new RefRespondentItem();
		respondent.setRefDepartment(refDepartmentRecId == null || refDepartmentRecId.equals(0L) ? null : reference.getRefDepartmentId(refDepartmentRecId, new Date()));
		respondent.setRefSubjectType(refSubjectTypeRecId == null || refSubjectTypeRecId.equals(0L) ? null : reference.getRefSubjectTypeId(refSubjectTypeRecId, new Date()));
		respondent.setRecId(refRespondentRecId == null || refRespondentRecId == 0 ? null : refRespondentRecId);
		List<RefRespondentItem> respondents = (List<RefRespondentItem>)reference.getRefAbstractByFilterList(RefRespondentItem.REF_CODE, respondent, new Date());
		for(RefRespondentItem item:respondents){
			PermissionRespondent pr = new PermissionRespondent(item);
			result.add(pr);
			map.put(pr.getRespondent().getRecId(), pr);
		}

		try {
			connection = getConnection();
			ps = connection.prepareStatement("select nvl(u.ref_respondent_rec_id, g.ref_respondent_rec_id) ref_respondent_rec_id, nvl(u.id, g.id) id, " +
					" nvl(u.is_active, g.is_active) is_active, g.is_active inh_is_active, " +
					" case when g.id is not null and u.id is null then 1 else 0 end for_group " +
					" from (select *  from  user_respondents where user_id=?) u " +
					" full join (select g.* from group_respondents g inner join group_users gu on g.group_id=gu.group_id where gu.user_id=?) g on u.ref_respondent_rec_id=g.ref_respondent_rec_id");

			ps.setLong(1, userId);
			ps.setLong(2, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				PermissionRespondent pr = map.get(rs.getLong("REF_RESPONDENT_REC_ID"));
				if (pr == null) continue;
				pr.setId(rs.getLong("ID"));
				pr.setActive(rs.getInt("IS_ACTIVE") > 0);
				pr.setInhActive(rs.getInt("INH_IS_ACTIVE") > 0);
				pr.setForGroup(rs.getInt("FOR_GROUP") > 0);
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
	public List<PermissionRespondent> getRespondentsByWarrant(long userId, long refRespondentRecId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PermissionRespondent> result = new ArrayList<PermissionRespondent>();
		Map<Long, PermissionRespondent> map = new HashMap<Long, PermissionRespondent>();
		List<RespondentWarrant> warrants = reference.getRespondentWarrantList(refRespondentRecId, new Date());
		RefRespondentItem filter = new RefRespondentItem();
		Set<Long> respondentRecIds = new HashSet<Long>();
		respondentRecIds.add(refRespondentRecId);
		Map<Long, RespondentWarrant> warrantMap = new HashMap<Long, RespondentWarrant>();
		for (RespondentWarrant warrant : warrants) {
			respondentRecIds.add(warrant.getRecId());
			warrantMap.put(warrant.getRecId(), warrant);
		}
		for (Long respRecId : respondentRecIds) {
			filter.setRecId(respRecId);
			List<RefRespondentItem> respondents = (List<RefRespondentItem>) reference.getRefAbstractByFilterList(RefRespondentItem.REF_CODE, filter, new Date());
			if (respondents.size() == 0) {
				continue;
			}
			PermissionRespondent pr = new PermissionRespondent(respondents.get(0));
			result.add(pr);
			map.put(pr.getRespondent().getRecId(), pr);
			if (warrantMap.containsKey(respRecId)) {
				RespondentWarrant warrant = warrantMap.get(respRecId);
				pr.getRespondent().setWarrantDate(warrant.getbDate());
				pr.getRespondent().setWarrantNum(warrant.getNum());
			}
		}

		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT\n" +
					"  u.ref_respondent_rec_id,\n" +
					"  u.id,\n" +
					"  u.IS_ACTIVE\n" +
					"FROM user_respondents u\n" +
					"WHERE user_id = ?");

			ps.setLong(1, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				PermissionRespondent pr = map.get(rs.getLong("REF_RESPONDENT_REC_ID"));
				if (pr == null) continue;
				pr.setId(rs.getLong("ID"));
				pr.setActive(rs.getInt("IS_ACTIVE") > 0);
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
	public void insertUserPermissionRespondent(long userId, PermissionRespondent respondent, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			String seqNextValue = "SEQ_USER_RESPONDENTS_ID.nextval";
			ps = connection.prepareStatement("INSERT INTO user_respondents" +
					" (id, user_id, ref_respondent_rec_id, is_active) VALUES (" + seqNextValue + ", ?, ?, ?)");
			ps.setLong(1, userId);
			ps.setLong(2, respondent.getRespondent().getRecId());
			ps.setBoolean(3, respondent.isActive());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();

		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void updateUserPermissionRespondent(PermissionRespondent respondent, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			String stmt = "update user_respondents set is_active = ?" +
					" where id = ?";
			ps = connection.prepareStatement(stmt);
			ps.setBoolean(1, respondent.isActive());
			ps.setLong(2, respondent.getId());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteUserPermissionRespondent(long id, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			ps = connection.prepareStatement("delete from user_respondents where id=? ");
			ps.setLong(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Deleting items failed, no rows affected.");

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if(localCon)
				connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteUserPermissionRespondents(long userId, Connection connection) {
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("delete from user_respondents where user_id=? ");
			ps.setLong(1, userId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	private List<PermissionForm> getPermissionFormsByUser(long userId, Map<Long, String> refRespondents, Connection connection) {
		PreparedStatement ps = null, ps2 = null;
		ResultSet rs = null, rs2 = null;
		boolean localCon = false;
		List<PermissionForm> result = new ArrayList<PermissionForm>();
		Map<String, List<String>> index = new HashMap<String, List<String>>();
		Map<PermissionFormIndex, PermissionForm> idx = new HashMap<PermissionFormIndex, PermissionForm>();
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			java.sql.Array arrayRespondents = oraConn.createARRAY("NUMBER_ARRAY", refRespondents.keySet().toArray(new Long[refRespondents.size()]));
			ps = connection.prepareStatement("SELECT\n" +
					"  fh.name                         AS form_name,\n" +
					"  f.code                          AS form_code,\n" +
					"  u.REF_RESPONDENT_REC_ID,\n" +
					"  u.id,\n" +
					"  u.is_active,\n" +
					"  (SELECT ri.name\n" +
					"   FROM right_items ri\n" +
					"   WHERE ri.id = u.right_item_id) AS right_item\n" +
					"FROM USER_RESP_FORMS u,\n" +
					"  forms f,\n" +
					"  v_report_history_short fh\n" +
					"WHERE u.user_id = ?\n" +
					"      AND u.form_code = f.code\n" +
					"      AND f.id = fh.form_id\n" +
					"      AND u.REF_RESPONDENT_REC_ID IN (SELECT t.COLUMN_VALUE\n" +
					"                                      FROM TABLE (?) t)");

			ps.setLong(1, userId);
			ps.setArray(2, arrayRespondents);

			rs = ps.executeQuery();
			while (rs.next()) {
				PermissionForm permissionForm = new PermissionForm();
				permissionForm.setItemId(userId);
				permissionForm.setFormCode(rs.getString("FORM_CODE"));
				permissionForm.setId(rs.getLong("ID"));
				permissionForm.setFormName(rs.getString("FORM_NAME"));
				permissionForm.setPermissionName(rs.getString("RIGHT_ITEM"));
				permissionForm.setRefRespondentRecId(rs.getLong("REF_RESPONDENT_REC_ID"));
				String idn = refRespondents.get(permissionForm.getRefRespondentRecId());
				permissionForm.setIdn(idn);
				permissionForm.setActive(rs.getInt("IS_ACTIVE") > 0);
				permissionForm.setInitActive(rs.getInt("IS_ACTIVE") > 0);
				permissionForm.setForGroup(false);
				permissionForm.setInhActive(false);
				result.add(permissionForm);

				PermissionFormIndex i = new PermissionFormIndex(permissionForm.getFormCode(), permissionForm.getPermissionName(), permissionForm.getRefRespondentRecId());
				idx.put(i, permissionForm);
			}


			ps2 = connection.prepareStatement("SELECT\n" +
					"  fh.name                         AS form_name,\n" +
					"  f.code                          AS form_code,\n" +
					"  u.id,\n" +
					"  u.is_active,\n" +
					"  (SELECT ri.name\n" +
					"   FROM right_items ri\n" +
					"   WHERE ri.id = u.right_item_id) AS right_item,\n" +
					"  u.group_id,\n" +
					"  u.REF_RESPONDENT_REC_ID\n" +
					"FROM (SELECT\n" +
					"        gf.form_code,\n" +
					"        gf.right_item_id,\n" +
					"        gf.REF_RESPONDENT_REC_ID,\n" +
					"        max(gf.is_active) is_active,\n" +
					"        max(gf.id)        id,\n" +
					"        max(gf.group_id)  group_id\n" +
					"      FROM GROUP_RESP_FORMS gf\n" +
					"        INNER JOIN group_users gu ON gf.group_id = gu.group_id\n" +
					"      WHERE gu.user_id = ?\n" +
					"            AND gf.REF_RESPONDENT_REC_ID IN (SELECT t.COLUMN_VALUE\n" +
					"                                            FROM TABLE (?) t)\n" +
					"      GROUP BY gf.form_code, gf.right_item_id, gf.REF_RESPONDENT_REC_ID) u,\n" +
					"  forms f,\n" +
					"  v_report_history_short fh\n" +
					"WHERE u.form_code = f.code\n" +
					"      AND f.id = fh.form_id");

			ps2.setLong(1, userId);
			ps2.setArray(2, arrayRespondents);

			rs2 = ps2.executeQuery();
			while (rs2.next()) {
				PermissionForm permissionForm = new PermissionForm();
				permissionForm.setItemId(rs2.getLong("GROUP_ID"));
				permissionForm.setFormCode(rs2.getString("FORM_CODE"));
				permissionForm.setId(rs2.getLong("ID"));
				permissionForm.setFormName(rs2.getString("FORM_NAME"));
				permissionForm.setPermissionName(rs2.getString("RIGHT_ITEM"));
				permissionForm.setRefRespondentRecId(rs2.getLong("REF_RESPONDENT_REC_ID"));
				String idn = refRespondents.get(permissionForm.getRefRespondentRecId());
				permissionForm.setIdn(idn);
				permissionForm.setActive(rs2.getInt("IS_ACTIVE") > 0);
				permissionForm.setInitActive(rs2.getInt("IS_ACTIVE") > 0);
				permissionForm.setForGroup(true);
				permissionForm.setInhActive(true);

				PermissionFormIndex i = new PermissionFormIndex(permissionForm.getFormCode(), permissionForm.getPermissionName(), permissionForm.getRefRespondentRecId());
				if (!idx.containsKey(i)) {
					result.add(permissionForm);
					idx.put(i, permissionForm);
				} else {
					PermissionForm p = idx.get(i);
					p.setInhActive(permissionForm.isActive());
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(null, ps, rs);
			DbUtil.closeConnection(null, ps2, rs2);
			DbUtil.closeConnection(localCon ? connection : null);
		}
		return result;
	}

	private List<PermissionForm> getOutputPermissionFormsByUser(long userId, Connection connection) {
		PreparedStatement ps = null, ps2 = null;
		ResultSet rs = null, rs2 = null;
		boolean localCon = false;
		List<PermissionForm> result = new ArrayList<PermissionForm>();
		Map<String, List<String>> index = new HashMap<String, List<String>>();
		Map<PermissionFormIndex, PermissionForm> idx = new HashMap<PermissionFormIndex, PermissionForm>();
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("SELECT\n" +
					"  fh.name                         AS form_name,\n" +
					"  f.code                          AS form_code,\n" +
					"  u.id,\n" +
					"  u.is_active,\n" +
					"  (SELECT ri.name\n" +
					"   FROM right_items ri\n" +
					"   WHERE ri.id = u.right_item_id) AS right_item\n" +
					"FROM USER_FORMS u,\n" +
					"  forms f,\n" +
					"  v_report_history_short fh\n" +
					"WHERE u.user_id = ?\n" +
					"      AND u.form_code = f.code\n" +
					"      AND f.id = fh.form_id\n");

			ps.setLong(1, userId);

			rs = ps.executeQuery();
			while (rs.next()) {
				PermissionForm permissionForm = new PermissionForm();
				permissionForm.setItemId(userId);
				permissionForm.setFormCode(rs.getString("FORM_CODE"));
				permissionForm.setId(rs.getLong("ID"));
				permissionForm.setFormName(rs.getString("FORM_NAME"));
				permissionForm.setPermissionName(rs.getString("RIGHT_ITEM"));
				permissionForm.setRefRespondentRecId(0l);
				permissionForm.setActive(rs.getInt("IS_ACTIVE") > 0);
				permissionForm.setInitActive(rs.getInt("IS_ACTIVE") > 0);
				permissionForm.setForGroup(false);
				permissionForm.setInhActive(false);
				result.add(permissionForm);

				PermissionFormIndex i = new PermissionFormIndex(permissionForm.getFormCode(), permissionForm.getPermissionName(), permissionForm.getRefRespondentRecId());
				idx.put(i, permissionForm);
			}


			ps2 = connection.prepareStatement("SELECT\n" +
					"  fh.name                         AS form_name,\n" +
					"  f.code                          AS form_code,\n" +
					"  u.id,\n" +
					"  u.is_active,\n" +
					"  (SELECT ri.name\n" +
					"   FROM right_items ri\n" +
					"   WHERE ri.id = u.right_item_id) AS right_item,\n" +
					"  u.group_id\n" +
					"FROM (SELECT\n" +
					"        gf.form_code,\n" +
					"        gf.right_item_id,\n" +
					"        max(gf.is_active) is_active,\n" +
					"        max(gf.id)        id,\n" +
					"        max(gf.group_id)  group_id\n" +
					"      FROM GROUP_FORMS gf\n" +
					"        INNER JOIN group_users gu ON gf.group_id = gu.group_id\n" +
					"      WHERE gu.user_id = ?\n" +
					"      GROUP BY gf.form_code, gf.right_item_id) u,\n" +
					"  forms f,\n" +
					"  v_report_history_short fh\n" +
					"WHERE u.form_code = f.code\n" +
					"      AND f.id = fh.form_id");

			ps2.setLong(1, userId);

			rs2 = ps2.executeQuery();
			while (rs2.next()) {
				PermissionForm permissionForm = new PermissionForm();
				permissionForm.setItemId(rs2.getLong("GROUP_ID"));
				permissionForm.setFormCode(rs2.getString("FORM_CODE"));
				permissionForm.setId(rs2.getLong("ID"));
				permissionForm.setFormName(rs2.getString("FORM_NAME"));
				permissionForm.setPermissionName(rs2.getString("RIGHT_ITEM"));
				permissionForm.setRefRespondentRecId(0l);
				permissionForm.setActive(rs2.getInt("IS_ACTIVE") > 0);
				permissionForm.setInitActive(rs2.getInt("IS_ACTIVE") > 0);
				permissionForm.setForGroup(true);
				permissionForm.setInhActive(true);

				PermissionFormIndex i = new PermissionFormIndex(permissionForm.getFormCode(), permissionForm.getPermissionName(), permissionForm.getRefRespondentRecId());
				if (!idx.containsKey(i)) {
					result.add(permissionForm);
					idx.put(i, permissionForm);
				} else {
					PermissionForm p = idx.get(i);
					p.setInhActive(permissionForm.isActive());
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(null, ps, rs);
			DbUtil.closeConnection(null, ps2, rs2);
			DbUtil.closeConnection(localCon ? connection : null);
		}
		return result;
	}

	@Override
	public boolean hasPermissionRespForm(long userId, String formCode, String permissionName, String idn) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean result = false;
		Connection connection = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT count(*) cnt\n" +
					"FROM v_user_resp_forms f\n" +
					"WHERE USER_ID = ? AND f.RIGHT_ITEM_NAME = ? AND f.FORM_CODE = ? AND f.IDN = ?");

			ps.setLong(1, userId);
			ps.setString(2, permissionName);
			ps.setString(3, formCode);
			ps.setString(4, idn);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getLong("cnt") > 0;
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
	public boolean hasOutputPermissionForm(long userId, String formCode, String permissionName) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean result = false;
		Connection connection = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT count(*) cnt\n" +
					"FROM v_user_forms f\n" +
					"WHERE USER_ID = ? AND f.RIGHT_ITEM_NAME = ? AND f.FORM_CODE = ?");

			ps.setLong(1, userId);
			ps.setString(2, permissionName);
			ps.setString(3, formCode);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getLong("cnt") > 0;
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
	public Role getRole(long roleId, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		Role result = new Role();
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("select id, name from role where id = ?");

			ps.setLong(1, roleId);
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
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public List<Role> getRoles(Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		List<Role> result = new ArrayList<Role>();
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("select id, name from role");

			rs = ps.executeQuery();
			while (rs.next()) {
				Role role = new Role();
				role.setId(rs.getLong("ID"));
				role.setName(rs.getString("NAME"));
				result.add(role);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public List<Role> getRolesByDepRecId(List<Long> depRecIdList) {
		List<Role> result = new ArrayList<Role>();

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			connection = getConnection();

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < depRecIdList.size(); i++) {
				if (i > 0)
					sb.append(",");
				sb.append(depRecIdList.get(i));
			}
			ps = connection.prepareStatement(
					"select r.*\n" +
					"  from role r,\n" +
					"       groups g,\n" +
					"       group_users gu\n" +
					" where r.id = g.role_id\n" +
					"   and g.group_id = gu.group_id\n" +
					"   and g.ref_department_rec_id in (" + sb.toString() +")");

			rs = ps.executeQuery();
			while (rs.next()) {
				Role role = new Role();
				role.setId(rs.getLong("ID"));
				role.setName(rs.getString("NAME"));
				result.add(role);
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
	public Role getRoleByUserId(Long userId){
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
	public void synchronizeUserGroups(List<PortalUserGroup> groups) {
		Connection connection = null;
		CallableStatement stmt = null;
		try {
			connection = getConnection();
			stmt = connection.prepareCall("BEGIN sync_groups(?, ?, ?, ?); END;");
			Struct[] structs = new Struct[groups.size()];
			int i = 0;
			for(PortalUserGroup group : groups) {
				structs[i] = group.toStruct(connection);
				i++;
			}
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			java.sql.Array arrayStructs = oraConn.createOracleArray("GROUP_TABLE", structs);
			stmt.setArray(1, arrayStructs);
			stmt.setString(2, groupPrefix);
			stmt.registerOutParameter(3, OracleTypes.INTEGER);
			stmt.registerOutParameter(4, OracleTypes.VARCHAR);

			stmt.execute();

			int errCode = stmt.getInt(3);
			String errMsg = stmt.getString(4);

			if (errCode != 0) {
				throw new EJBException(errMsg);
			}

			arrayStructs.free();
		} catch (SQLException e) {
			if (connection != null) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} catch (Exception e) {
			if (connection != null) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
	}

	private HashMap<Long, PortalUserGroup> loadGroupsFromDB(){
		HashMap<Long, PortalUserGroup> groupsFromDB = new HashMap<Long, PortalUserGroup>();

		Connection connection = null;
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select group_id, name, description from groups where lower(name) like lower(?) ");

			ps.setString(1, groupPrefix + "%");
			rs = ps.executeQuery();
			while (rs.next()) {
				PortalUserGroup portalUserGroup = new PortalUserGroup();
				portalUserGroup.setUserGroupId(rs.getLong("GROUP_ID"));
				portalUserGroup.setName(rs.getString("NAME"));
				Clob desc = rs.getClob("DESCRIPTION");
				if(desc!=null) {
					portalUserGroup.setDescription(desc.getSubString(1, (int) desc.length()));
				}

				groupsFromDB.put(portalUserGroup.getUserGroupId(), portalUserGroup);
			}

			ps2 = connection.prepareStatement("select group_id, user_id from group_users");
			rs2 = ps2.executeQuery();
			while (rs2.next()){
				long groupId = rs2.getLong("GROUP_ID");
				long userId = rs2.getLong("USER_ID");
				if(groupsFromDB.containsKey(groupId))
					groupsFromDB.get(groupId).getUserIds().add(userId);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(null, ps, rs);
			DbUtil.closeConnection(null, ps2, rs2);
			DbUtil.closeConnection(connection);
		}

		return groupsFromDB;
	}

	@Override
	public void deletePortalUserGroup(long userGroupId, Connection connection, AuditEvent auditEvent){
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement ps2 = null;
		boolean localCon = false;
		PreparedStatement psDepartments = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); connection.setAutoCommit(false);}

			Set<Long> userIds = getGroupUsers(userGroupId, connection);
			for (Long id:userIds)
				deleteUserFromUserGroup(userGroupId, id, connection, null);

			psDepartments = connection.prepareStatement("DELETE FROM GROUP_DEPARTMENTS WHERE group_id = ?");
			psDepartments.setLong(1, userGroupId);
			psDepartments.executeUpdate();

			deleteUserGroupPermissionDepartments(userGroupId, false, connection);
			deleteUserGroupPermissionRespondents(userGroupId, false, connection);
			deleteUserGroupPermissionForms(userGroupId, false, connection);
			deleteUserGroupPermissions(userGroupId, false, connection);

			ps2 = connection.prepareStatement("DELETE FROM GROUPS WHERE group_id = ?");
			ps2.setLong(1, userGroupId);
			ps2.executeUpdate();

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if (localCon) connection.commit();

		} catch (SQLException e) {
			if ((connection != null) && (localCon)) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} catch (Exception e) {
			if ((connection != null) && (localCon)) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(null, psDepartments);
			DbUtil.closeConnection(null, ps2);
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
	}

	@Override
	public void insertPortalUserGroup(PortalUserGroup userGroup, boolean isFull, Connection connection, AuditEvent auditEvent){
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); connection.setAutoCommit(false);}
			String stmt = null;
			if (isFull)
				stmt = "INSERT INTO GROUPS (id, group_id, name, description, role_id, ref_department_rec_id, ref_subject_type_rec_id, REF_RESPONDENT_REC_ID) VALUES (SEQ_GROUPS_ID.nextval, ?, ?, ?, ?, ?, ?, ?)";
			else
				stmt = "INSERT INTO GROUPS (id, group_id, name, description) VALUES (SEQ_GROUPS_ID.nextval, ?, ?, ?)";
			ps = connection.prepareStatement(stmt);
			ps.setLong(1, userGroup.getUserGroupId());
			ps.setString(2, userGroup.getName());
			Clob desc = connection.createClob();
			desc.setString(1, userGroup.getDescription());
			ps.setClob(3, desc);
			if (isFull) {
				if (userGroup.getRoleId() == null || userGroup.getRoleId() == 0)
					ps.setNull(4, OracleTypes.NULL);
				else
					ps.setLong(4, userGroup.getRoleId());

				if (userGroup.getRefDepartmentRecId() == null || userGroup.getRefDepartmentRecId() == 0)
					ps.setNull(5, OracleTypes.NULL);
				else
					ps.setLong(5, userGroup.getRefDepartmentRecId());

				if (userGroup.getRefSubjectTypeRecId() == null || userGroup.getRefSubjectTypeRecId() == 0)
					ps.setNull(6, OracleTypes.NULL);
				else
					ps.setLong(6, userGroup.getRefSubjectTypeRecId());

				if (userGroup.getRefRespondentRecId() == null || userGroup.getRefRespondentRecId() == 0)
					ps.setNull(7, OracleTypes.NULL);
				else
					ps.setLong(7, userGroup.getRefRespondentRecId());
			}

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");

			synchronizeGroupUsers(userGroup.getUserGroupId(), userGroup.getUserIds(), connection);

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if (localCon) connection.commit();
		} catch (SQLException e) {
			if ((connection != null) && (localCon)) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} catch (Exception e) {
			if ((connection != null) && (localCon)) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void updatePortalUserGroup(PortalUserGroup userGroup, boolean isFull, Map<String, Boolean> deletePermissions, Connection connection, AuditEvent auditEvent){
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); connection.setAutoCommit(false);}
			String stmt = null;
			if (isFull)
				stmt = "UPDATE groups SET name = ?, description = ?, role_id = ?, ref_department_rec_id = ?, ref_subject_type_rec_id = ?, REF_RESPONDENT_REC_ID = ? WHERE group_id = ?";
			else
				stmt = "UPDATE groups SET name = ?, description = ? WHERE group_id = ?";
			ps = connection.prepareStatement(stmt);
			ps.setString(1, userGroup.getName());
			Clob desc = connection.createClob();
			desc.setString(1, userGroup.getDescription());
			ps.setClob(2, desc);

			if (isFull) {
				if (userGroup.getRoleId() == null || userGroup.getRoleId() == 0)
					ps.setNull(3, OracleTypes.NULL);
				else
					ps.setLong(3, userGroup.getRoleId());

				if (userGroup.getRefDepartmentRecId() == null || userGroup.getRefDepartmentRecId() == 0)
					ps.setNull(4, OracleTypes.NULL);
				else
					ps.setLong(4, userGroup.getRefDepartmentRecId());

				if (userGroup.getRefSubjectTypeRecId() == null || userGroup.getRefSubjectTypeRecId() == 0)
					ps.setNull(5, OracleTypes.NULL);
				else
					ps.setLong(5, userGroup.getRefSubjectTypeRecId());

				if (userGroup.getRefRespondentRecId() == null || userGroup.getRefRespondentRecId() == 0)
					ps.setNull(6, OracleTypes.NULL);
				else
					ps.setLong(6, userGroup.getRefRespondentRecId());

				ps.setLong(7, userGroup.getUserGroupId());
			}
			else
				ps.setLong(3, userGroup.getUserGroupId());

			ps.executeUpdate();

			if (!isFull) synchronizeGroupUsers(userGroup.getUserGroupId(), userGroup.getUserIds(), connection);

			if (deletePermissions != null) {
				boolean fillRightItems = false;
				boolean fillDepartments = false;
				boolean fillSubjectTypes = false;
				boolean fillCreditors = false;
				boolean fillForms = false;

				if (deletePermissions.get("Role").booleanValue()
						|| deletePermissions.get("RefSubjectType").booleanValue()
						|| deletePermissions.get("RefDepartment").booleanValue()
						|| deletePermissions.get("RefRespondent").booleanValue()) {
					deleteUserGroupPermissionRespondents(userGroup.getUserGroupId(), true, connection);
					deleteUserGroupPermissionForms(userGroup.getUserGroupId(), true, connection);
					fillSubjectTypes = true;
					fillForms = true;
					fillCreditors = true;
					fillForms = true;
				}
				if (deletePermissions.get("Role").booleanValue()) {
					deleteUserGroupPermissions(userGroup.getUserGroupId(), true, connection);
					deleteUserGroupPermissionDepartments(userGroup.getUserGroupId(), true, connection);
					fillRightItems = true;
					fillDepartments = true;
					fillSubjectTypes = true;
					fillCreditors = true;
					fillForms = true;
				}
				Set<Long> userIds = getGroupUsers(userGroup.getUserGroupId(), connection);
				for (Long userId : userIds) {
					fillUserPermissions(userId, fillRightItems, fillDepartments, fillSubjectTypes, fillCreditors, fillForms, connection);
				}
			}

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if (localCon) connection.commit();
		} catch (SQLException e) {
			if ((connection != null) && (localCon)) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} catch (Exception e) {
			if ((connection != null) && (localCon)) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void synchronizeUsers(List<PortalUser> users) {
		Connection connection = null;
		CallableStatement stmt = null;
		try {
			connection = getConnection();
			stmt = connection.prepareCall("BEGIN sync_users(?, ?, ?); END;");
			Struct[] structs = new Struct[users.size()];
			int i = 0;
			for(PortalUser user : users) {
				structs[i] = user.toStruct(connection);
				i++;
			}
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			java.sql.Array arrayStructs = oraConn.createOracleArray("USER_TABLE", structs);
			stmt.setArray(1, arrayStructs);
			stmt.registerOutParameter(2, OracleTypes.INTEGER);
			stmt.registerOutParameter(3, OracleTypes.VARCHAR);

			stmt.execute();

			int errCode = stmt.getInt(2);
			String errMsg = stmt.getString(3);

			if (errCode != 0) {
				throw new EJBException(errMsg);
			}

			arrayStructs.free();
		} catch (SQLException e) {
			if (connection != null) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} catch (Exception e) {
			if (connection != null) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
	}

	@Override
	public List<PortalUserGroup> getUserGroupsByFilter(long userId, Long[] roleIds, Long[] subjectTypeRecIds, Long[] departmentRecIds, String filterGroupName, String filterUser) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PortalUserGroup> result = new ArrayList<PortalUserGroup>();
		try {
			connection = getConnection();

			String stmt = "select id, group_id, name, description, role_id, ref_department_rec_id, ref_subject_type_rec_id, REF_RESPONDENT_REC_ID " +
					"from groups g where lower(name) like lower(?)";
			boolean userIdClause = false;
			if (userId != 0L) {
				PortalUserGroup group =  getUserGroupByUser(userId, connection);
				if (group != null) {
					if (group.getRoleId() == 2) {
						userIdClause = true;
						stmt += " and (select count(*) from f_session_departments si where si.department_id = g.ref_department_rec_id and si.user_id = ?) = 1";
					}
					if (group.getRoleId() == 6) {
						userIdClause = true;
						stmt += " and (select count(*) from f_session_subject_types si where si.subject_type_id = g.ref_subject_type_rec_id and si.user_id = ?) = 1";
					}
					if (group.getRoleId() == 7) {
						userIdClause = true;
						stmt += " and g.REF_RESPONDENT_REC_ID IN (SELECT c.CREDITOR_ID\n" +
								"                                  FROM F_SESSION_CREDITORS c WHERE c.USER_ID=?)";
					}
				}
			}
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			java.sql.Array arrayRoles = null;
			if (roleIds != null) {
				arrayRoles = oraConn.createARRAY("NUMBER_ARRAY", roleIds);
				stmt += " AND ROLE_ID IN (SELECT t2.COLUMN_VALUE\n" +
						"                FROM table(?) t2)";
			}
			java.sql.Array arraySubjectTypes = null;
			if (subjectTypeRecIds != null) {
				arraySubjectTypes = oraConn.createARRAY("NUMBER_ARRAY", subjectTypeRecIds);
				stmt += " AND coalesce(g.REF_SUBJECT_TYPE_REC_ID, -1) IN (SELECT t3.COLUMN_VALUE\n" +
						"                                  FROM table(?) t3)";
			}
			java.sql.Array arrayDepartments = null;
			if (departmentRecIds != null) {
				arrayDepartments = oraConn.createARRAY("NUMBER_ARRAY", departmentRecIds);
				stmt += " AND coalesce(g.REF_DEPARTMENT_REC_ID, -1) IN (SELECT t4.COLUMN_VALUE\n" +
						"                                  FROM table(?) t4)";
			}
			if (filterGroupName != null && !filterGroupName.isEmpty()) {
				stmt += " AND lower(g.NAME) LIKE lower(?)";
			}

			if (filterUser != null && !filterUser.isEmpty()) {
				stmt += " AND g.GROUP_ID IN (SELECT gu.GROUP_ID\n" +
						"                     FROM GROUP_USERS gu INNER JOIN F_USERS u ON gu.USER_ID = u.USER_ID\n" +
						"                     WHERE lower(u.SCREEN_NAME) LIKE lower(?))";
			}
			stmt += " order by name";

			ps = connection.prepareStatement(stmt);

			int paramIndex = 0;
			ps.setString(++paramIndex, groupPrefix + "%");
			if (userIdClause)
				ps.setLong(++paramIndex, userId);
			if (roleIds != null)
				ps.setArray(++paramIndex, arrayRoles);
			if (subjectTypeRecIds != null)
				ps.setArray(++paramIndex, arraySubjectTypes);
			if (departmentRecIds != null)
				ps.setArray(++paramIndex, arrayDepartments);
			if (filterGroupName != null && !filterGroupName.isEmpty())
				ps.setString(++paramIndex, "%" + filterGroupName + "%");
			if (filterUser != null && !filterUser.isEmpty())
				ps.setString(++paramIndex, "%" + filterUser + "%");
			rs = ps.executeQuery();
			while (rs.next()) {
				PortalUserGroup portalUserGroup = new PortalUserGroup();
				portalUserGroup.setId(rs.getLong("ID"));
				portalUserGroup.setUserGroupId(rs.getLong("GROUP_ID"));
				portalUserGroup.setName(rs.getString("NAME"));
				Clob desc = rs.getClob("DESCRIPTION");
				if (desc != null) {
					portalUserGroup.setDescription(desc.getSubString(1, (int) desc.length()));
				}
				portalUserGroup.setRoleId(rs.getLong("ROLE_ID"));
				portalUserGroup.setRefDepartmentRecId(rs.getLong("REF_DEPARTMENT_REC_ID"));
				portalUserGroup.setRefSubjectTypeRecId(rs.getLong("REF_SUBJECT_TYPE_REC_ID"));
				portalUserGroup.setRefRespondentRecId(rs.getLong("REF_RESPONDENT_REC_ID"));

				result.add(portalUserGroup);
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

	private <T> String joinArray(T[] array, String symbol) {
		String result = "";
		boolean firstItem = true;
		for (T item : array) {
			if (firstItem) {
				firstItem = false;
			} else {
				result += symbol;
			}
			result += item;
		}
		return result;
	}

	@Override
	public List<PortalUser> getUsers(String screenName, String lastName, String firstName, String middleName) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PortalUser> result = new ArrayList<PortalUser>();
		try {
			connection = getConnection();

			String stmt = "select id, user_id, screen_name, email, first_name, last_name, " +
					" middle_name, modified_date, blockfl, idn, ref_respondent_rec_id, ref_post_rec_id, must_sign, DESIGN_USER_NAME " +
					" from f_users u" +
					" where 1 = 1";
			if (screenName != null) stmt += " and lower(screen_name) like ?";
			if (lastName != null) stmt += " and lower(last_name) like ?";
			if (firstName != null) stmt += " and lower(first_name) like ?";
			if (middleName != null) stmt += " and lower(middle_name) like ?";
			ps = connection.prepareStatement(stmt);

			int i = 0;
			if (screenName != null) ps.setString(++i, screenName.toLowerCase());
			if (lastName != null) ps.setString(++i, lastName.toLowerCase());
			if (firstName != null) ps.setString(++i, firstName.toLowerCase());
			if (middleName != null) ps.setString(++i, middleName.toLowerCase());

			rs = ps.executeQuery();
			while (rs.next()) {

				result.add(resultSetToPortalUser(rs));
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
	public List<PortalUser> getUsersByGroupId(long groupId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PortalUser> result = new ArrayList<PortalUser>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select u.id, u.user_id, u.screen_name, u.email, u.first_name, u.last_name, u.middle_name, " +
					" u.modified_date, u.blockfl, u.idn, u.ref_respondent_rec_id, u.ref_post_rec_id, u.must_sign, design_user_name " +
					" from f_users u inner join group_users gu on u.user_id = gu.user_id" +
					" where gu.group_id = ?");

			ps.setLong(1, groupId);
			rs = ps.executeQuery();
			while (rs.next()) {

				result.add(resultSetToPortalUser(rs));
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
	public List<PortalUser> getUsersByGroupIdRespondentId(long groupId, long respondentId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PortalUser> result = new ArrayList<PortalUser>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select u.id, u.user_id, u.screen_name, u.email, u.first_name, u.last_name, u.middle_name, " +
					" u.modified_date, u.blockfl, u.idn, u.ref_respondent_rec_id, u.ref_post_rec_id, u.must_sign, design_user_name " +
					" from f_users u inner join group_users gu on u.user_id = gu.user_id" +
					" where gu.group_id = ? and u.ref_respondent_rec_id = ?");

			ps.setLong(1, groupId);
			ps.setLong(2, respondentId);
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(resultSetToPortalUser(rs));
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
	public List<PortalUser> getUsersByFilter(long groupId, Long[] respondentIds, String filterUser) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PortalUser> result = new ArrayList<PortalUser>();
		try {
			connection = getConnection();
			String sql = "SELECT\n" +
					"  u.id,\n" +
					"  u.user_id,\n" +
					"  u.screen_name,\n" +
					"  u.email,\n" +
					"  u.first_name,\n" +
					"  u.last_name,\n" +
					"  u.middle_name,\n" +
					"  u.modified_date,\n" +
					"  u.blockfl,\n" +
					"  u.idn,\n" +
					"  u.ref_respondent_rec_id,\n" +
					"  u.ref_post_rec_id,\n" +
					"  u.must_sign,\n" +
					"  design_user_name\n" +
					"FROM f_users u INNER JOIN group_users gu ON u.user_id = gu.user_id\n" +
					"WHERE 0=0";

			java.sql.Array arrayRespondents = null;
			if (respondentIds != null) {
				OracleConnection oraConn = connection.unwrap(OracleConnection.class);
				arrayRespondents = oraConn.createARRAY("NUMBER_ARRAY", respondentIds);
				sql += " AND u.REF_RESPONDENT_REC_ID IN (SELECT t.COLUMN_VALUE\n" +
						"                    FROM TABLE (?) t)\n";
			}
			if (groupId != 0) {
				sql += " AND u.USER_ID IN (SELECT gu.USER_ID\n" +
						"                        FROM GROUP_USERS gu\n" +
						"                        WHERE gu.GROUP_ID = ?)\n";
			}
			if (filterUser != null && !filterUser.isEmpty()) {
				sql += " AND lower(SCREEN_NAME) LIKE lower(?)";
			}
			ps = connection.prepareStatement(sql);
			int paramIndex = 0;
			if (arrayRespondents != null) {
				ps.setArray(++paramIndex, arrayRespondents);
			}
			if (groupId != 0) {
				ps.setLong(++paramIndex, groupId);
			}
			if (filterUser != null && !filterUser.isEmpty()) {
				ps.setString(++paramIndex, "%" + filterUser + "%");
			}
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(resultSetToPortalUser(rs));
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

	private PortalUser resultSetToPortalUser(ResultSet rs) throws SQLException{
		PortalUser user = new PortalUser();

		user.setId(rs.getLong("ID"));
		user.setUserId(rs.getLong("USER_ID"));
		user.setFirstName(rs.getString("FIRST_NAME"));
		user.setLastName(rs.getString("LAST_NAME"));
		user.setMiddleName(rs.getString("MIDDLE_NAME"));
		user.setScreenName(rs.getString("SCREEN_NAME"));
		user.setEmailAddress(rs.getString("EMAIL"));
		user.setIdn(rs.getString("IDN"));
		user.setBlocked(rs.getInt("BLOCKFL") > 0);
		user.setRespondentId(rs.getLong("REF_RESPONDENT_REC_ID"));
		user.setRefPostId(rs.getLong("REF_POST_REC_ID"));
		user.setModifiedDate(DataUtils.convert(rs.getDate("MODIFIED_DATE")));
		user.setMustSign(rs.getInt("MUST_SIGN") > 0);
		user.setDesignUserName(rs.getString("DESIGN_USER_NAME"));

		return  user;
	}

	@Override
	public PortalUserGroup getUserGroupByGroupId(long groupId, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		PortalUserGroup group = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("select id, name, group_id, description, role_id, ref_department_rec_id, ref_subject_type_rec_id, REF_RESPONDENT_REC_ID " +
					" from groups g" +
					" where g.group_id = ?");

			ps.setLong(1, groupId);
			rs = ps.executeQuery();
			while (rs.next()) {
				group = new PortalUserGroup();

				group.setId(rs.getLong("ID"));
				group.setName(rs.getString("NAME"));
				group.setUserGroupId(rs.getLong("GROUP_ID"));
				group.setDescription(rs.getString("DESCRIPTION"));
				group.setRoleId(rs.getLong("ROLE_ID"));
				group.setRefDepartmentRecId(rs.getLong("REF_DEPARTMENT_REC_ID"));
				group.setRefSubjectTypeRecId(rs.getLong("REF_SUBJECT_TYPE_REC_ID"));
				group.setRefRespondentRecId(rs.getLong("REF_RESPONDENT_REC_ID"));
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}

		return group;
	}

	@Override
	public PortalUser getUserByUserId(long userId, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		PortalUser user = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("select u.id, u.user_id, u.screen_name, u.email, u.first_name, u.last_name, " +
					" u.middle_name, u.modified_date, u.blockfl, u.idn, u.PASSPORT, u.ref_respondent_rec_id, u.ref_post_rec_id, u.must_sign, u.design_user_name " +
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
				user.setPassport(rs.getString("PASSPORT"));
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
	public void insertPortalUser(PortalUser user, boolean isFull, Connection connection){
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			String stmt = null;
			if (isFull)
				stmt = "INSERT INTO GROUPS (id, group_id, name, description, ref_department_rec_id, ref_subject_type_rec_id) VALUES (SEQ_GROUPS_ID.nextval, ?, ?, ?, ?, ?)";
			else
				stmt = "INSERT INTO f_users (id, user_id, screen_name, email, first_name, last_name, middle_name, modified_date, blockfl)" +
						" VALUES (SEQ_F_USERS_ID.nextval, ?, ?, ?, ?, ?, ?, ?, ?)";
			ps = connection.prepareStatement(stmt);
			ps.setLong(1, user.getUserId());
			ps.setString(2, user.getScreenName());
			ps.setString(3, user.getEmailAddress());
			ps.setString(4, user.getFirstName());
			ps.setString(5, user.getLastName());
			ps.setString(6, user.getMiddleName());
			ps.setDate(7, DataUtils.convert(user.getModifiedDate()));
			ps.setBoolean(8, user.isBlocked());

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void updatePortalUser(PortalUser user, boolean isFull, Connection connection, AuditEvent auditEvent) {
		updatePortalUser(user, isFull, null, connection, auditEvent, true);
	}

	@Override
	public void updatePortalUser(PortalUser user, boolean isFull, List<UserWarrant> warrants, Connection connection, AuditEvent auditEvent, boolean fillUserPermissions){
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); connection.setAutoCommit(false); }
			String stmt = null;
			if (isFull)
				stmt = "UPDATE f_users" +
						" SET user_id = ?, screen_name = ?, email = ?, first_name = ?, last_name = ?, middle_name = ?, modified_date = ?, blockfl = ?," +
						" ref_respondent_rec_id = ?, ref_post_rec_id = ?, idn = ?, must_sign = ?, design_user_name = ?, PASSPORT = ? " +
						" WHERE user_id = ?";
			else
				stmt = "UPDATE f_users" +
						" SET user_id = ?, screen_name = ?, email = ?, first_name = ?, last_name = ?, middle_name = ?, modified_date = ?, blockfl = ?" +
						" WHERE user_id = ?";
			ps = connection.prepareStatement(stmt);
			ps.setLong(1, user.getUserId());
			ps.setString(2, user.getScreenName());
			ps.setString(3, user.getEmailAddress());
			ps.setString(4, user.getFirstName());
			ps.setString(5, user.getLastName());
			ps.setString(6, user.getMiddleName());
			ps.setDate(7, DataUtils.convert(user.getModifiedDate()));
			ps.setBoolean(8, user.isBlocked());
			if (isFull) {
				if (user.getRespondentId() == null || user.getRespondentId() == 0)
					ps.setNull(9, OracleTypes.NULL);
				else
					ps.setLong(9, user.getRespondentId());

				if (user.getRefPostId() == null || user.getRefPostId() == 0)
					ps.setNull(10, OracleTypes.NULL);
				else
					ps.setLong(10, user.getRefPostId());
				ps.setString(11, user.getIdn());
				/*if (user.getType() == null)
					ps.setNull(12, OracleTypes.NULL);
				else
					ps.setLong(12, user.getType().getId());*/
				ps.setBoolean(12, user.isMustSign());
				ps.setString(13, user.getDesignUserName());
				ps.setString(14, user.getPassport());
				ps.setLong(15, user.getUserId());
			}
			else
				ps.setLong(9, user.getUserId());

			ps.executeUpdate();

			if (fillUserPermissions) {
				fillUserPermissions(user.getUserId(), true, true, true, true, true, connection);
			}

			if (auditEvent != null) {
				Long aeId = insertAuditEvent(auditEvent, connection);
				auditEvent.setId(aeId);
			}

            if (warrants != null) {
				AuditEvent a = null;
				if (auditEvent != null) {
					a = new AuditEvent();
					a.setUserId(auditEvent.getUserId());
					a.setUserLocation(auditEvent.getUserLocation());
					a.setUserName(auditEvent.getUserName());
					a.setParentId(auditEvent.getId());
				}
				saveUserWarrants(user.getUserId(), warrants, connection, a);
            }

			if(localCon)
				connection.commit();

		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void blockPortalUser(long userId, boolean value, Connection connection){
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection();}
			ps = connection.prepareStatement("UPDATE f_users SET blockfl = ? WHERE user_id = ? ");
			ps.setBoolean(1, value);
			ps.setLong(2, userId);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	private void synchronizeGroupUsers(long userGroupId, Set<Long> userIds, Connection connection) {
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); connection.setAutoCommit(false);}

			Set<Long> userIdsFromDB = getGroupUsers(userGroupId, connection);

			Set<Long> toDelete = new HashSet<Long>(userIdsFromDB);
			toDelete.removeAll(userIds);

			Set<Long> toAdd = new HashSet<Long>(userIds);
			toAdd.removeAll(userIdsFromDB);

			for (Long id : toDelete) {
				deleteUserFromUserGroup(userGroupId, id, connection, null);
			}

			for (Long id : toAdd) {
				addUserToUserGroup(userGroupId, id, connection, null);
			}
			if (localCon) connection.commit();
		} catch (SQLException e) {
			if ((connection != null) && (localCon)) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} catch (Exception e) {
			if ((connection != null) && (localCon)) { try { connection.rollback(); } catch (SQLException se) {logger.error(se.getMessage());}}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null);
		}
	}

	private Set<Long> getGroupUsers(long userGroupId, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		Set<Long> userIds = new HashSet<Long>();
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("SELECT user_id FROM GROUP_USERS WHERE group_id = ?");
			ps.setLong(1, userGroupId);

			rs = ps.executeQuery();
			while (rs.next()) {
				userIds.add(rs.getLong("USER_ID"));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}

		return userIds;
	}

	@Override
	public void addUserToUserGroup(long userGroupId, long userId, Connection connection, AuditEvent auditEvent) {
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			ps = connection.prepareStatement("INSERT INTO GROUP_USERS (id, group_id, user_id) VALUES (SEQ_GROUP_USERS_ID.nextval, ?, ?)");
			ps.setLong(1, userGroupId);
			ps.setLong(2, userId);

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");


			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			Set<Long> groupIds = getUserGroupsByUser(userId, null);
			for (Long groupId : groupIds) {
				if (groupId != userGroupId) {
					if (auditEvent != null) {
						auditEvent.setIdKindEvent(65L);
					}
					deleteUserFromUserGroup(groupId, userId, connection, auditEvent, false);
				}
			}

			fillUserPermissions(userId, true, true, true, true, true, connection);

			if(localCon) connection.commit();

		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public void deleteUserFromUserGroup(long userGroupId, long userId, Connection connection, AuditEvent auditEvent) {
		deleteUserFromUserGroup(userGroupId, userId, connection, auditEvent, true);
	}

	@Override
	public void deleteUserFromUserGroup(long userGroupId, long userId, Connection connection, AuditEvent auditEvent, boolean fillUserPermission) {
		PreparedStatement ps = null;
		PreparedStatement psu = null;
		boolean localCon = false;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			boolean isDeletePermission = getUserGroupsByUser(userId, connection).size() == 1;
			if (isDeletePermission) {
				deleteUserPermissionDepartments(userId, connection);
				deleteUserPermissionRespondents(userId, connection);
				deleteUserPermissionForms(userId, connection);
				deleteUserPermissions(userId, connection);

				fillUserPermissions(userId, true, true, true, true, true, connection);
			}

			ps = connection.prepareStatement("delete from GROUP_USERS where group_id=? and user_id=? ");
			ps.setLong(1, userGroupId);
			ps.setLong(2, userId);

			ps.executeUpdate();

			psu = connection.prepareStatement("update f_users set must_sign = 0, ref_respondent_rec_id = null, ref_post_rec_id = null, design_user_name = null where user_id = ?");
			psu.setLong(1, userId);

			psu.executeUpdate();

			if(auditEvent != null)
				insertAuditEvent(auditEvent, connection);

			if (localCon) connection.commit();
		} catch (SQLException e) {
			if ((connection != null) && (localCon)) {
				try {
					connection.rollback();
				} catch (SQLException se) {
					logger.error(se.getMessage());
				}
			}
			throw new EJBException(e);
		} catch (Exception e) {
			if ((connection != null) && (localCon)) {
				try {
					connection.rollback();
				} catch (SQLException se) {
					logger.error(se.getMessage());
				}
			}
			throw new EJBException(e);
		} finally {
			DbUtil.closeStatement(psu);
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public Set<Long> getDuplicatedUserInUserGroups(String groupPrefix, Connection connection) {
		Set<Long> result =  new HashSet<Long>();

		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("select gu.user_id from group_users gu, groups g " +
					"where gu.group_id = g.group_id and g.name like ? || '%' " +
					"group by gu.user_id having count(*) > 1");

			ps.setString(1, groupPrefix);
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getLong("USER_ID"));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}

		return result;
	}

	@Override
	public Set<Long> getUserGroupsByUser(long userId, Connection connection) {
		Set<Long> result = new HashSet<Long>();

		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("select group_id from group_users where user_id = ?");

			ps.setLong(1, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getLong("GROUP_ID"));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}

		return result;
	}

	@Override
	public PortalUserGroup getUserGroupByUser(long userId, Connection connection) {
		PortalUserGroup result = new PortalUserGroup();

		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("select g.* from group_users gu, groups g where gu.user_id = ? and gu.group_id = g.group_id");

			ps.setLong(1, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				result.setId(rs.getLong("GROUP_ID"));
				result.setName(rs.getString("NAME"));
				result.setUserGroupId(rs.getLong("GROUP_ID"));
				result.setDescription(rs.getString("DESCRIPTION"));
				result.setRefDepartmentRecId(rs.getLong("REF_DEPARTMENT_REC_ID"));
				result.setRefSubjectTypeRecId(rs.getLong("REF_SUBJECT_TYPE_REC_ID"));
				result.setRoleId(rs.getLong("ROLE_ID"));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}

		return result;
	}

	@Override
	public boolean isExistUserInUserGroup(long groupId, long userId, Connection connection) {
		boolean result = false;

		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("select count(*) as cnt from group_users where group_id = ? and user_id = ?");

			ps.setLong(1, groupId);
			ps.setLong(2, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				result = rs.getLong("CNT") > 0;
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}

		return result;
	}

	@Override
	public List<PortalUser> getUsersByRespondentId(long respondentId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PortalUser> result = new ArrayList<PortalUser>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select u.id, u.user_id, u.screen_name, u.email, u.first_name, u.last_name, " +
					" u.middle_name, u.modified_date, u.blockfl, u.idn, u.ref_respondent_rec_id, u.ref_post_rec_id, u.must_sign," +
					"get_fio(u.LAST_NAME, u.FIRST_NAME, u.MIDDLE_NAME, 1) fio " +
					" from f_users u " +
					" where u.ref_respondent_rec_id = ? and u.blockfl = 0");

			ps.setLong(1, respondentId);
			rs = ps.executeQuery();
			while (rs.next()) {
				PortalUser user = new PortalUser();

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
				user.setMustSign(rs.getInt("MUST_SIGN") > 0);
				user.setFio(rs.getString("FIO"));

				result.add(user);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}

		return result;
	}

	@Override
	public List<PortalUser> getRespondentUsersByRespondentId(long respondentId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PortalUser> result = new ArrayList<PortalUser>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT\n" +
					"  u.id,\n" +
					"  u.user_id,\n" +
					"  u.screen_name,\n" +
					"  u.email,\n" +
					"  u.first_name,\n" +
					"  u.last_name,\n" +
					"  u.middle_name,\n" +
					"  u.modified_date,\n" +
					"  u.blockfl,\n" +
					"  u.idn,\n" +
					"  u.ref_respondent_rec_id,\n" +
					"  u.ref_post_rec_id,\n" +
					"  u.must_sign,\n" +
					"  get_fio(u.LAST_NAME, u.FIRST_NAME, u.MIDDLE_NAME, 1) fio\n" +
					"FROM f_users u\n" +
					"WHERE u.ref_respondent_rec_id = ? AND u.blockfl = 0\n" +
					"      AND u.USER_ID IN (SELECT gu.USER_ID\n" +
					"                            FROM GROUP_USERS gu INNER JOIN GROUPS g ON gu.GROUP_ID = g.GROUP_ID\n" +
					"                            WHERE g.ROLE_ID = 5)");

			ps.setLong(1, respondentId);
			rs = ps.executeQuery();
			while (rs.next()) {
				PortalUser user = new PortalUser();

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
				user.setMustSign(rs.getInt("MUST_SIGN") > 0);
				user.setFio(rs.getString("FIO"));

				result.add(user);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}

		return result;
	}

	@Override
	public List<PortalUserGroup> getUserGroupByRoleIdList(List<Long> roleIdList, List<Long> depRecIdList){
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PortalUserGroup> result = new ArrayList<PortalUserGroup>();
		try {
			connection = getConnection();
			StringBuilder sqlText = new StringBuilder();

			sqlText.append("select t.id,\n" +
					"       t.name,\n" +
					"       t.group_id,\n" +
					"       t.description,\n" +
					"       t.ref_department_rec_id,\n" +
					"       t.ref_subject_type_rec_id,\n" +
					"       t.role_id \n" +
					"  from GROUPS t\n" +
					" where upper(t.name) like upper('" + groupPrefix  + "%')");

			if(roleIdList != null && roleIdList.size() > 0) {
				StringBuilder sbRoleList = new StringBuilder();
				for (int i = 0; i < roleIdList.size(); i++) {
					if (i > 0)
						sbRoleList.append(",");
					sbRoleList.append("'").append(roleIdList.get(i)).append("'");

				}
				sqlText.append("and t.role_id in (" + sbRoleList.toString() + ")\n");
			}

			if(depRecIdList != null && depRecIdList.size() > 0) {

				StringBuilder sbDepRecIdList = new StringBuilder();
				for (int i = 0; i < depRecIdList.size(); i++) {
					if (i > 0)
						sbDepRecIdList.append(",");
					sbDepRecIdList.append("'").append(depRecIdList.get(i)).append("'");

				}
				sqlText.append("and t.ref_department_rec_id in (" + sbDepRecIdList.toString() +")");
			}

			ps = connection.prepareStatement(sqlText.toString());

			rs = ps.executeQuery();
			while (rs.next()) {
				PortalUserGroup item = new PortalUserGroup();
				item.setId(rs.getLong("ID"));
				item.setName(rs.getString("NAME"));
				item.setUserGroupId(rs.getLong("GROUP_ID"));
				Clob desc = rs.getClob("DESCRIPTION");
				if (desc != null) {
					item.setDescription(desc.getSubString(1, (int) desc.length()));
				}
				item.setRefDepartmentRecId(rs.getLong("REF_DEPARTMENT_REC_ID"));
				item.setRefSubjectTypeRecId(rs.getLong("REF_SUBJECT_TYPE_REC_ID"));
				item.setRoleId(rs.getLong("ROLE_ID"));
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
	public List<PortalUser> getUsers(List<Long> roleIdList, List<Long> groupIdList, List<Long> subjectTypeRecIdList, List<Long> respondentRecIdList) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PortalUser> result = new ArrayList<PortalUser>();
		try {
			connection = getConnection();
			StringBuilder sqlText = new StringBuilder();
			sqlText.append(
					"select r.name as role_name,\n" +
							"       g.name as group_name,\n" +
							"       rst.st_name,\n" +
							"       rst.resp_name,\n" +
							"       p.post_name,\n" +
							"       u.id,\n" +
							"       u.user_id,\n" +
							"       u.screen_name,\n" +
							"       u.email,\n" +
							"       nvl(u.first_name,' ') as first_name,\n" +
							"       nvl(u.last_name, ' ') as last_name,\n" +
							"       nvl(u.middle_name, ' ') as middle_name,\n" +
							"       u.modified_date,\n" +
							"       u.blockfl,\n" +
							"       u.idn,\n" +
							"       u.ref_respondent_rec_id,\n" +
							"       u.ref_post_rec_id,\n" +
							"       u.must_sign\n" +
							"  from f_users u,\n" +
							"       group_users gu,\n" +
							"       groups g,\n" +
							"       role r,\n" +
							"       (select r.id as resp_id,\n" +
							"               r.rec_id as resp_rec_id,\n" +
							"               nvl(up.short_name_ru, up.name_ru) as resp_name,\n" +
							"               st.id as st_id,\n" +
							"               st.rec_id as st_rec_id,\n" +
							"               nvl(st.short_name_ru, st.name_ru) as st_name\n" +
							"          from v_ref_respondent r,\n" +
							"               v_ref_subject_type st,\n" +
							"               V_REF_UNIONPERSONS up\n" +
							"          where r.ref_subject_type = st.id\n" +
							"            and r.REF_UNIONPERSONS = up.id\n" +
							"            and r.begin_date = (select max(r1.begin_date)\n" +
							"                                  from v_ref_respondent r1\n" +
							"                                 where r1.rec_id = r.rec_id\n" +
							"                                   and r1.begin_date <= sysdate)\n" +
							"\t\t\t and (r.end_date is null or r.end_date > sysdate)\n" +
							"            and st.begin_date = (select max(st1.begin_date)\n" +
							"                                   from v_ref_subject_type st1\n" +
							"                                  where st1.rec_id = st.rec_id\n" +
							"                                    and st.begin_date <= sysdate)\n" +
							"\t\t\t and (st.end_date is null or st.end_date > sysdate)\n" +
							"            and up.begin_date = (select max(lp1.begin_date)\n" +
							"                                   from V_REF_UNIONPERSONS lp1\n" +
							"                                  where lp1.rec_id = up.rec_id AND lp1.TYPE = up.TYPE \n" +
							"                                    and lp1.begin_date <= sysdate) \n" +
							"\t\t\t and (up.end_date is null or up.end_date > sysdate)\n" +
							"\t\t )rst,\n" +
							"       (select p.id as post_id,\n" +
							"               p.rec_id as post_rec_id,\n" +
							"               p.name_ru as post_name\n" +
							"          from v_ref_post p\n" +
							"         where p.begin_date = (select max(p1.begin_date)\n" +
							"                                from v_ref_post p1\n" +
							"                               where p1.rec_id = p.rec_id\n" +
							"                                 and p1.begin_date <= sysdate) \n" +
							"\t\t    and (p.end_date is null or p.end_date > sysdate)\n" +
							"\t\t ) p\n" +
							" where u.blockfl = 0\n" +
							"   and g.role_id = r.id\n" +
							"   and u.user_id = gu.user_id\n" +
							"   and g.group_id = gu.group_id\n" +
							"   and u.ref_respondent_rec_id = rst.resp_rec_id(+)\n" +
							"   and u.ref_post_rec_id = p.post_rec_id(+) \n" +
							"   and upper(g.name) like upper('"+ groupPrefix +"%')");
			if(roleIdList != null && roleIdList.size() > 0) {
				StringBuilder sbRoleIdList = new StringBuilder();
				for (int i = 0; i < roleIdList.size(); i++) {
					if (i > 0)
						sbRoleIdList.append(",");
					sbRoleIdList.append(roleIdList.get(i));
				}
				sqlText.append(" and g.role_id in (" + sbRoleIdList.toString() + ") ");
			}
			if(groupIdList != null && groupIdList.size() > 0) {
				StringBuilder sbGroupIdList = new StringBuilder();
				for (int i = 0; i < groupIdList.size(); i++) {
					if (i > 0)
						sbGroupIdList.append(",");
					sbGroupIdList.append(groupIdList.get(i));
				}
				sqlText.append(" and gu.group_id in (" + sbGroupIdList.toString() + ") ");
			}
			if(subjectTypeRecIdList != null && subjectTypeRecIdList.size() > 0) {
				StringBuilder sbSubjectTypeRecIdList = new StringBuilder();
				for (int i = 0; i < subjectTypeRecIdList.size(); i++) {
					if (i > 0)
						sbSubjectTypeRecIdList.append(",");
					sbSubjectTypeRecIdList.append(subjectTypeRecIdList.get(i));
				}
				sqlText.append(" and rst.st_rec_id in (" + sbSubjectTypeRecIdList.toString() + ") ");
			}
			if(respondentRecIdList != null && respondentRecIdList.size() > 0) {
				StringBuilder sbRespondentRecIdList = new StringBuilder();
				for (int i = 0; i < respondentRecIdList.size(); i++) {
					if (i > 0)
						sbRespondentRecIdList.append(",");
					sbRespondentRecIdList.append(respondentRecIdList.get(i));
				}
				sqlText.append(" and u.ref_respondent_rec_id in (" + sbRespondentRecIdList.toString() + ") ");
			}

			ps = connection.prepareStatement(sqlText.toString());
			rs = ps.executeQuery();

			while (rs.next()) {
				PortalUser user = new PortalUser();

				user.setId(rs.getLong("ID"));
				user.setUserId(rs.getLong("USER_ID"));
				user.setFirstName(rs.getString("FIRST_NAME"));
				user.setLastName(rs.getString("LAST_NAME"));
				user.setMiddleName(rs.getString("MIDDLE_NAME"));
				user.setFio(user.getLastName() + " " + user.getFirstName() + " " + user.getMiddleName());
				user.setScreenName(rs.getString("SCREEN_NAME"));
				user.setEmailAddress(rs.getString("EMAIL"));
				user.setIdn(rs.getString("IDN"));
				user.setRespondentId(rs.getLong("REF_RESPONDENT_REC_ID"));
				user.setRefPostId(rs.getLong("REF_POST_REC_ID"));
				user.setModifiedDate(DataUtils.convert(rs.getDate("MODIFIED_DATE")));
				user.setMustSign(rs.getInt("MUST_SIGN") > 0);
                user.setRoleName(rs.getString("ROLE_NAME"));
                user.setGroupName(rs.getString("GROUP_NAME"));
                user.setSubjectTypeName(rs.getString("ST_NAME"));
                user.setRespondentName(rs.getString("RESP_NAME"));
				user.setPostName(rs.getString("POST_NAME"));

				result.add(user);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public void fillUserPermissions(long userId, boolean fillRightItems, boolean fillDepartments, boolean fillSubjectTypes, boolean fillCreditors, boolean fillForms, Connection connection) throws SQLException {
		int Err_Code = 0;
		String Err_Msg = " ";
		boolean localCon = false;
		CallableStatement stmt = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
			}
			stmt = connection.prepareCall("BEGIN FILL_USER_PERMISSIONS(?, ?, ?, ?, ?, ?, ?, ?, ?); END;");
			stmt.setLong(1, userId);
			stmt.setInt(2, fillRightItems ? 1 : 0);
			stmt.setInt(3, fillDepartments ? 1 : 0);
			stmt.setInt(4, fillSubjectTypes ? 1 : 0);
			stmt.setInt(5, fillCreditors ? 1 : 0);
			stmt.setInt(6, fillForms ? 1 : 0);
			stmt.registerOutParameter(7, OracleTypes.INTEGER);
			stmt.registerOutParameter(8, OracleTypes.VARCHAR);
			stmt.setInt(9, 0);
			stmt.execute();
			Err_Code = stmt.getInt(7);
			Err_Msg = stmt.getString(8);
			if (Err_Code != 0) throw new SQLException(Err_Msg);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, stmt);
		}
	}

	@Override
	public List<String> getUserPermissionNames(long userId) {
		Connection connection = null;
		List<String> result = new ArrayList<String>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();

			ps = connection.prepareStatement("select r.name " +
					" from right_items r inner join f_session_right_items sr on r.id=sr.right_item_id" +
					" where sr.user_id=?");
			ps.setLong(1, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getString("NAME"));
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public Set<Long> filterUsersByFormPermission(Set<Long> users, long reportId, String formCode, String permissionName) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Set<Long> result = new HashSet<Long>();

		try {
			connection = getConnection();

			users = filterUsersByRespondentRecId(users, reportId, connection);
			if (users.size() == 0) {
				return Collections.emptySet();
			}

			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			Long[] arr = users.toArray(new Long[users.size()]);
			java.sql.Array array = oraConn.createARRAY("NUMBER_ARRAY", arr);

			ps = connection.prepareStatement("SELECT DISTINCT t.USER_ID\n" +
					"FROM V_USER_RESP_FORMS t\n" +
					"WHERE t.FORM_CODE = ?\n" +
					"      AND t.USER_ID IN (SELECT a.COLUMN_VALUE\n" +
					"                        FROM TABLE (?) a)\n" +
					"      AND t.IDN = (SELECT coalesce(r.IDN_CHILD, r.IDN)\n" +
					"                   FROM REPORTS r\n" +
					"                   WHERE r.ID = ?)");

			ps.setString(1, formCode);
			ps.setArray(2, array);
			ps.setLong(3, reportId);
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getLong("user_id"));
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	private Set<Long> filterUsersByRespondentRecId(Set<Long> users, long reportId, Connection connection) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Set<Long> result = new HashSet<Long>();

		try {
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			Long[] arr = users.toArray(new Long[users.size()]);
			java.sql.Array array = oraConn.createARRAY("NUMBER_ARRAY", arr);

			ps = connection.prepareStatement("SELECT u.USER_ID\n" +
					"FROM F_USERS u INNER JOIN TABLE(?) t ON u.USER_ID = t.COLUMN_VALUE\n" +
					"WHERE u.REF_RESPONDENT_REC_ID = (\n" +
					"  SELECT r.REC_ID\n" +
					"  FROM REF_RESPONDENT r\n" +
					"  WHERE r.REF_UNIONPERSONS IN (\n" +
					"    SELECT up.ID\n" +
					"    FROM V_REF_UNIONPERSONS up\n" +
					"    WHERE up.IDN = (\n" +
					"      SELECT r.IDN\n" +
					"      FROM REPORTS r\n" +
					"      WHERE r.ID = ?)\n" +
					"  )\n" +
					"  FETCH FIRST ROW ONLY\n" +
					")");

			ps.setArray(1, array);
			ps.setLong(2, reportId);
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getLong("user_id"));
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(null, ps, rs);
		}
		return result;
	}

	@Override
	public List<Long> getUserRespondents(long userId) {
		Connection connection = null;
		List<Long> result = new ArrayList<Long>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();

			ps = connection.prepareStatement("SELECT c.CREDITOR_ID ID FROM F_SESSION_CREDITORS c WHERE c.USER_ID = ?");
			ps.setLong(1, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getLong("ID"));
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	/*@Override
	public boolean hasAnyUserPermission(String permissionName) {
		Connection connection = null;
		boolean result = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();

			ps = connection.prepareStatement("select count(*) count\n" +
					"from Right_Items ri\n" +
					"     left join (User_Right_Items ur inner join f_users u on ur.user_id=u.user_id and u.blockfl=0) on ri.id=ur.right_item_id\n" +
					"     left join (group_right_items g inner join group_users gu on g.group_id=gu.group_id" +
					"	  inner join groups gg on g.group_id=gg.group_id and lower(gg.name) like lower(?) ) on ri.id=g.right_item_id " +
					"where ri.name=? and nvl(ur.is_active, g.is_active)=1");
			ps.setString(1, groupPrefix + "%");
			ps.setString(2, permissionName);
			rs = ps.executeQuery();
			while (rs.next()) {
				result = rs.getInt("COUNT")>0;
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}*/

	// ReportHistorySignature
	private void insertReportHistorySignature(ReportHistorySignature signatureItem, Connection connection) throws SQLException {
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			String stmt = "INSERT INTO REPORT_SIGN (id, report_history_id, user_id, ref_post, signature, sign_date, USER_WARRANT_ID) " +
					" VALUES (SEQ_REPORT_SIGN_ID.nextval, ?, ?, ?, ?, ?, ?)";
			ps = connection.prepareStatement(stmt);
			ps.setLong(1, signatureItem.getReportHistoryId());
			ps.setLong(2, signatureItem.getUserId());
			ps.setLong(3, signatureItem.getRefPostId());
			Clob clobSignature = connection.createClob();
			clobSignature.setString(1, signatureItem.getSignature());
			ps.setClob(4, clobSignature);
			ps.setDate(5, new java.sql.Date(signatureItem.getSignDate().getTime()));
			if (signatureItem.getUserWarrantId() == null) {
				ps.setNull(6, OracleTypes.NULL);
			} else {
				ps.setLong(6, signatureItem.getUserWarrantId());
			}

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	// todo поменять на хранимую процедуру
	@Override
	public void updateSignature(long reportId, String signature, long userId, String userLocation, Date signDate, long userWarrantId, AuditEvent auditEvent){
		Connection connection = null;
		try {
			connection = getConnection();
			//connection.setAutoCommit(false);

			List<ReportHistory> reportHistory = getReportHistoryByReportIdNoLobs(reportId, connection);
			ReportHistory lastHistoryItem = reportHistory.get(reportHistory.size() - 1);

			PortalUser portalUser = getUserByUserId(userId, connection);
			Long refPostId;
			Long principalRefPostId = null;
			UserWarrant userWarrant = null;
			PortalUser principal = null;
			String msg;
			String userInfo = portalUser.getFullName()+ " [" + userLocation + "]";

			refPostId = portalUser.getRefPostId();
			if (refPostId == null || refPostId == 0)
				throw new EJBException("Должность не установлена");

			if (userWarrantId > 0) {
				userWarrant = getUserWarrantById(userWarrantId, connection);
				if (userWarrant == null) {
					throw new EJBException(MessageFormat.format("Доверенность с ID {0} не найдена", userWarrantId));
				}
				if (userWarrant.isCanceled())
					throw new EJBException(MessageFormat.format("Доверенность отозвана", userWarrantId));
				Date now = new Date();
				if (userWarrant.getBeginDate().after(now) || (userWarrant.getEndDate() != null && userWarrant.getEndDate().before(now))) {
					throw new EJBException(MessageFormat.format("Срок действия доверенности не подходит", userWarrantId));
				}
				principal = getUserByUserId(userWarrant.getPrincipal(), connection);
				if (principal == null) {
					throw new EJBException(MessageFormat.format("Пользователь с USER_ID {0}  не найден", userWarrant.getPrincipal()));
				}

				if (principal.getRespondentId() == null) {
					throw new EJBException("У доверителя не установлена организация");
				}
				if (principal.getRespondentId().longValue() != portalUser.getRespondentId()) {
					throw new EJBException("Доверитель относится к другой организации");
				}

				if (principal.getRefPostId() == null || principal.getRefPostId() == 0)
					throw new EJBException("Должность не установлена у доверителя");
				RefPostItem filter = new RefPostItem();
				filter.setRecId(refPostId);
				filter.setBeginDate(signDate);
				RefPostItem userPost = (RefPostItem)reference.getRefAbstractItem(RefPostItem.REF_CODE, filter);
				filter.setRecId(principal.getRefPostId());
				RefPostItem principalPost = (RefPostItem)reference.getRefAbstractItem(RefPostItem.REF_CODE, filter);
				principalRefPostId = principal.getRefPostId();
				msg = "Подписал(а): " + userInfo + " в должности " + userPost.getNameRu() + " от имени " +
						principal.getFullName() + " в должности " + principalPost.getNameRu();
			} else {
				msg = "Подписал(а): " + userInfo;
			}

			ReportHistorySignature signatureItem = new ReportHistorySignature();
			signatureItem.setReportHistoryId(lastHistoryItem.getId());
			signatureItem.setUserId(portalUser.getUserId());
			signatureItem.setSignature(signature);
			signatureItem.setSignDate(signDate);
			if (userWarrantId > 0) {
				signatureItem.setUserWarrantId(userWarrantId);
				signatureItem.setRefPostId(principalRefPostId);
			} else {
				signatureItem.setRefPostId(portalUser.getRefPostId());
			}
			insertReportHistorySignature(signatureItem, connection);

			ReportStatus status = new ReportStatus();
			status.setReportHistory(getLastReportHistoryByReportIdNoLobs(reportId, false, connection));
			status.setStatusCode(ReportStatus.Status.SIGNED.name());
			status.setStatusDate(signatureItem.getSignDate());
			status.setUserId(portalUser.getUserId());
			status.setUserInfo(userInfo);
			status.setUserLocation(userLocation);
			status.setMessage(msg);
			if(userWarrantId > 0){
				status.setUserWarrantId(userWarrantId);
			}
			insertReportStatusHistory(status, connection, auditEvent);

			if (userWarrant != null) {
				userWarrant.setReadonly(true);
				AuditEvent childAe = null;
				if (auditEvent != null) {
					Date date = new Date();
					childAe = new AuditEvent();
					childAe.setUserName(auditEvent.getUserName());
					childAe.setUserLocation(auditEvent.getUserLocation());
					childAe.setUserId(auditEvent.getUserId());
					childAe.setDateEvent(date);
					childAe.setDateIn(date);
					childAe.setIdKindEvent(130L);
					childAe.setCodeObject("USER_WARRANT");
					childAe.setNameObject("Доверенность");
					childAe.setRecId(userWarrant.getId());
				}
				updateUserWarrant(userWarrant, connection, childAe);
			}

			//connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
	}

	@Override
	public void clearSignatures(long reportId) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();

			List<ReportHistory> reportHistory = getReportHistoryByReportIdNoLobs(reportId, connection);
			if (reportHistory.size() == 0)
				return;
			ReportHistory lastHistoryItem = reportHistory.get(reportHistory.size() - 1);

			ps = connection.prepareStatement("DELETE FROM report_sign WHERE report_history_id = ?");
			ps.setLong(1, lastHistoryItem.getId());
			int affectedRows = ps.executeUpdate();
			// if (affectedRows == 0) throw new SQLException("Deleting an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public List<ReportHistorySignature> getSignaturesByReportHistory(long reportHistoryId) {
		Connection connection = null;
		List<ReportHistorySignature> result = new ArrayList<ReportHistorySignature>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();

			ps = connection.prepareStatement("select * from report_sign s where s.report_history_id=?");
			ps.setLong(1, reportHistoryId);
			rs = ps.executeQuery();
			while (rs.next()) {
				ReportHistorySignature signatureItem = new ReportHistorySignature();
				signatureItem.setId(rs.getLong("ID"));
				signatureItem.setReportHistoryId(rs.getLong("REPORT_HISTORY_ID"));
				signatureItem.setUserId(rs.getLong("USER_ID"));
				signatureItem.setRefPostId(rs.getLong("REF_POST"));
				Clob signature = rs.getClob("SIGNATURE");
				if (signature != null) {
					signatureItem.setSignature(signature.getSubString(1, (int) signature.length()));
				}
				result.add(signatureItem);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	// Reports of user
	@Override
	public ReportListItem getReportListByReportId(long reportId, String languageCode, boolean forSuperUser) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ReportListItem result = new ReportListItem();
		Report report = new Report();
		try {
			connection = getConnection();
			ps = connection.prepareStatement(
					"SELECT * " +
					" FROM reports " +
					" WHERE id = ? ");
			ps.setLong(1, reportId);
			rs = ps.executeQuery();
			while (rs.next()) {
				report = getReportFromResultSet(rs);
			}

			ReportHistory reportHistory = getLastReportHistoryByReportId(reportId, false, false, forSuperUser, connection);
			ReportStatus reportStatusHistory = getLastReportStatusByReportId(reportId, forSuperUser, connection);

			ReportListItem item = new ReportListItem();
			item.setId(report.getId());
			item.setIdn(report.getIdn());
			if(item.getIdn() != null){
				item.setRespondentNameRu(reference.getRespondentByIdn(item.getIdn(), new Date()).getPersonName());
			}
			item.setIdnChild(report.getIdnChild());
			if(item.getIdnChild() != null){
				item.setChildRespondentNameRu(reference.getRespondentByIdn(item.getIdnChild(), new Date()).getPersonName());
			}
			item.setReportDate(report.getReportDate());
			item.setFormCode(report.getFormCode());
			List<Form> forms = getFormsByCodeLanguageReportDate(report.getFormCode(), languageCode, report.getReportDate(), connection);
			if (!forms.isEmpty()){
				item.setFormName(forms.get(0).getFormHistory().getName());
				item.setCanAttachedFile(forms.get(0).getFormHistory().getFormTag() == null ? false : forms.get(0).getFormHistory().getFormTag().canAttachedFile);
			}
			if (reportHistory != null) {
				item.setSaveDate(reportHistory.getSaveDate());
				item.setUserInfo(reportHistory.getUserInfo());
				item.setDeliveryWay(reportHistory.getDeliveryWayCode());
				ReportHistoryListItem reportHistoryListItem = getReportHistoryListItemById(reportHistory.getId(), connection);
				item.setHaveAttachedFile(reportHistoryListItem.getHaveAttachedFile());
				item.setHaveAttachedLetter(reportHistoryListItem.getHaveAttachedLetter());
			}
			if (reportStatusHistory != null) {
				item.setStatus(reportStatusHistory.getStatusCode());
				item.setStatusDate(reportStatusHistory.getStatusDate());
				item.setStatusName(ReportStatus.resMap.get("ru" + "_" + item.getStatus()));
			}
			result = item;

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public ReportListItem getReportListByReportIdAdvanced(long reportId, String languageCode, boolean forSuperUser) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ReportListItem result = new ReportListItem();
		Report report = new Report();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT * " +
					" FROM reports " +
					" WHERE id = ? ");
			ps.setLong(1, reportId);
			rs = ps.executeQuery();
			while (rs.next()) {
				report = getReportFromResultSet(rs);
			}

			if (report == null) {
				throw new EJBException(MessageFormat.format("Отчет с id {0} не найден", reportId));
			}

			ReportHistory reportHistory = getLastReportHistoryByReportId(reportId, false, false, forSuperUser, connection);
			ReportStatus reportStatusHistory = getLastReportStatusByReportId(reportId, forSuperUser, connection);

			ReportListItem item = new ReportListItem();
			item.setId(report.getId());
			item.setIdn(report.getIdn());
			item.setIdnChild(report.getIdnChild());
			item.setReportDate(report.getReportDate());
			item.setFormCode(report.getFormCode());
			List<Form> forms = getFormsByCodeLanguageReportDate(report.getFormCode(), languageCode, report.getReportDate(), connection);
			if (!forms.isEmpty()){
				item.setFormName(forms.get(0).getFormHistory().getName());
				item.setCanAttachedFile(forms.get(0).getFormHistory().getFormTag() == null ? false : forms.get(0).getFormHistory().getFormTag().canAttachedFile);
			}
			if (reportHistory != null) {
				item.setSaveDate(reportHistory.getSaveDate());
				item.setUserInfo(reportHistory.getUserInfo());
				item.setDeliveryWay(reportHistory.getDeliveryWayCode());
				if (forSuperUser) {
					item.setControlResultCode(reportHistory.getControlResultCode2());
				} else {
					item.setControlResultCode(reportHistory.getControlResultCode());
				}
			}
			if (reportStatusHistory != null) {
				item.setStatus(reportStatusHistory.getStatusCode());
				item.setStatusDate(reportStatusHistory.getStatusDate());
			}

			int completeCount = 0;
			for (ReportStatus status : getReportStatusHistoryByReportId(report.getId(), forSuperUser, connection)) {
				if (status.getStatusCode().equals(ReportStatus.Status.COMPLETED.toString())) {
					completeCount++;
					if (item.getFirstCompletedDate() == null)
						item.setFirstCompletedDate(status.getStatusDate());
					item.setLastCompletedDate(status.getStatusDate());
				}
			}
			item.setCompleteCount(completeCount);

			result = item;

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public List<ReportListItem> getReportListByUserIdnFormCodesReportDateRange(long userId, String idn, List<String> idnList, boolean haveWarrant, List<String> formCodes,
																			   List<RefNpaItem> filterNpa, Date reportDateBegin, Date reportDateEnd, String languageCode) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ReportListItem> result = new ArrayList<ReportListItem>();
		if (idn == null || idn.isEmpty()) return result;
		if (formCodes == null || formCodes.size() == 0) return result;
		if (haveWarrant && (idnList == null || idnList.size() == 0)) return result;

		try {
			connection = getConnection();
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			String[] arr = formCodes.toArray(new String[formCodes.size()]);
			java.sql.Array array = oraConn.createARRAY("FORM_CODE_ARRAY", arr);

			String idnClause;
			if(idnList != null && idnList.size() != 0) {
				boolean haveParent = false;
				StringBuilder sbIdn = new StringBuilder();
				for(int i = 0; i < idnList.size(); i++){
					if(idnList.get(i).equals(idn))
						haveParent = true;
					if (i > 0)
						sbIdn.append(",");
					sbIdn.append("'").append(idnList.get(i)).append("'");

				}
				if(haveParent){
					idnClause = "(r.idn = '" + idn + "' " + " or r.idn_child in (" + sbIdn.toString() + ")) ";
				}else {
					idnClause = " r.idn_child in (" + sbIdn.toString() + ") ";
				}
			}else{
				idnClause = " r.idn = '" + idn + "' ";;
			}

			String sqlText = "SELECT r.ID,r.IDN,r.IDN_CHILD, r.REPORT_DATE,r.FORM_CODE,st.status_code, st.status_date,h.CONTROL_RESULT_CODE, " +
					"  (select count(rf.id) from attached_file rf where rf.link_id = h.id and rf.file_kind = 1 ) as have_file, " +
					"  (select count(rf.id) from attached_file rf where rf.link_id = h.id and rf.file_kind = 2 ) as have_letter, " +
					"  (select count(rf.id) from attached_file rf where rf.link_id = h.id and rf.file_kind = 3 ) as have_files, " +
					"	npa.name_ru as name_npa " +
					" FROM reports r " +
					" 	inner join F_SESSION_RESP_FORMS sf on r.form_code = sf.FORM_CODE and sf.user_id = ? AND sf.IDN = r.IDN " +
					" 	inner join table(?) t on r.form_code=t.column_value" +
					"	inner join forms f on f.code = r.form_code"+
					"	inner join form_history fh on fh.form_id = f.id and fh.begin_date <= r.report_date and (fh.end_date is null or fh.end_date > r.report_date)" +
					"	left join npa_forms nf on nf.form_history_id = fh.id " +
					"	left join v_ref_npa npa \n" +
					"        on npa.rec_id = nf.npa_rec_id \n" +
					"       and npa.begin_date = (select max(npa1.begin_date) \n" +
					"                               from v_ref_npa npa1\n" +
					"                              where npa1.rec_id = npa.rec_id\n" +
					"                                and npa1.begin_date <= r.report_date)" +
					"	INNER JOIN REPORT_HISTORY h ON h.ID=(SELECT max(h2.ID) FROM REPORT_HISTORY h2 WHERE h2.REPORT_ID=r.ID)" +
					"   INNER JOIN REPORT_HISTORY_STATUSES st on st.ID=(SELECT max(st2.ID) FROM REPORT_HISTORY_STATUSES st2 WHERE st2.REPORT_HISTORY_ID=h.ID)" +
					" WHERE " + idnClause + " AND TRUNC(r.report_date) BETWEEN NVL(TRUNC(?),TRUNC(r.report_date)) AND NVL(TRUNC(?),TRUNC(r.report_date))" +
					"		and st.status_code in (select substr(ri.name, instr(ri.name, ':')+1) as status_name " +
					"  								from right_items ri," +
					"       							f_session_right_items fri " +
					" 								where ri.parent = (select id " +
					"                     								from right_items t " +
					"                    								where t.name = 'REP_STAT') " +
					"   								and ri.id = fri.right_item_id " +
					"   								and fri.user_id = ?)";

			if(filterNpa != null && filterNpa.size() > 0) {
				StringBuilder sbNpa = new StringBuilder();
				Boolean noNpa = false;
				for (int i = 0; i < filterNpa.size(); i++) {
					if(filterNpa.get(i).getId() == 0)
						noNpa = true;
					if (i > 0)
						sbNpa.append(",");
					sbNpa.append(filterNpa.get(i).getRecId());
				}
				if(noNpa) {
					sqlText = sqlText + " and (nf.npa_rec_id is null or nf.npa_rec_id in (" + sbNpa.toString() + "))";
				}else {
					sqlText = sqlText + " and nf.npa_rec_id in (" + sbNpa.toString() + ")";
				}
			}

			ps = connection.prepareStatement(sqlText);
			//ps.setString(1, idn); // Doesn't work because JDBC doesn't wrap a value in quotes if a string value contains only numeric characters.
			ps.setLong(1, userId);
			ps.setArray(2, array);
			ps.setDate(3, reportDateBegin == null ? null : new java.sql.Date(reportDateBegin.getTime()));
			ps.setDate(4, reportDateEnd == null ? null : new java.sql.Date(reportDateEnd.getTime()));
			ps.setLong(5, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				Report report = getReportFromResultSet(rs);

				ReportListItem item = new ReportListItem();
				item.setIdn(idn);
				item.setIdnChild(report.getIdnChild());
				item.setId(report.getId());
				item.setReportDate(report.getReportDate());
				item.setFormCode(report.getFormCode());
				List<Form> forms = getFormsByCodeLanguageReportDate(report.getFormCode(), languageCode, report.getReportDate(), connection);
				if (!forms.isEmpty()){
					Form form = forms.get(0);
					item.setFormName(form.getFormHistory().getName());
					item.setCanAttachedFile(form.getFormHistory().getFormTag() == null ? false : form.getFormHistory().getFormTag().canAttachedFile);
					item.setFormTypeCode(form.getTypeCode());
				}
				ReportHistory lastReport = getLastReportHistoryByReportIdNoLobs(report.getId(), false, connection);
				if (lastReport != null) {
					item.setSaveDate(lastReport.getSaveDate());
					item.setUserInfo(lastReport.getUserInfo());
					item.setDeliveryWay(lastReport.getDeliveryWayCode());
				}

				item.setStatus(rs.getString("STATUS_CODE"));
				item.setStatusDate(rs.getDate("STATUS_DATE"));

				if (item.getStatus().equals(ReportStatus.Status.SIGNED.name())) {
					item.setSignInfo(getReportSignInfo(item.getId(), false, connection));
				} else item.setSignInfo("");

				List<ReportStatus> statuses = getReportStatusHistoryByReportHistoryId(lastReport.getId(), connection);
				if (statuses.size() > 0) {
					ReportStatus lastStatus = statuses.get(statuses.size() - 1);
					item.setStatus(lastStatus.getStatusCode());
					item.setStatusDate(lastStatus.getStatusDate());
				}
				int completeCount = 0;
				for (ReportStatus status : statuses) {
					if (status.getStatusCode().equals(ReportStatus.Status.COMPLETED.toString())) {
						completeCount++;
						if (item.getFirstCompletedDate() == null)
							item.setFirstCompletedDate(status.getStatusDate());
						item.setLastCompletedDate(status.getStatusDate());
					}
				}
				item.setCompleteCount(completeCount);

				item.setControlResultCode(rs.getString("CONTROL_RESULT_CODE"));
				item.setHaveAttachedFile(rs.getInt("HAVE_FILE") > 0);
				item.setHaveAttachedLetter(rs.getInt("HAVE_LETTER") > 0);
				if(item.getIdnChild() != null) {
					item.setChildRespondentNameRu(reference.getRespondentByIdn(item.getIdnChild(), report.getReportDate()).getPersonName());
				}
				item.setNameNPA(rs.getString("NAME_NPA"));
				item.setHaveFiles(rs.getInt("HAVE_FILES") > 0);
				result.add(item);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	private String getReportSignInfo(Long reportId, boolean forSuperUser, Connection connection){
		ReportHistory reportHistory = getLastReportHistoryByReportId(reportId, false, false, forSuperUser, connection);
		if(reportHistory==null)
			return "";

		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		String result = "";
		List<Long> postTypeIds = new ArrayList<Long>();
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement("select p.type_post \n" +
					"from report_sign s \n" +
					"     inner join v_ref_post p on s.ref_post=p.rec_id and p.begin_date = (select max(p2.begin_date) from v_ref_post p2 where p2.rec_id=p.rec_id and p2.begin_date<=nvl(s.sign_date, sysdate)) and (p.end_date is null or p.end_date > nvl(s.sign_date,sysdate)) \n" +
					"where s.report_history_id=? and  p.type_post in (3,5)     \n" +
					"order by p.type_post");

			ps.setLong(1, reportHistory.getId());
			rs = ps.executeQuery();
			while (rs.next()) {
				Long postTypeId = rs.getLong("TYPE_POST");
				if (!postTypeIds.contains(postTypeId))
					postTypeIds.add(postTypeId);
			}

			for (int i = 0; i < postTypeIds.size(); i++) {
				if (i > 0) result += ", ";
				Long postTypeId = postTypeIds.get(i);
				if (postTypeId == 3)
					result += "ГБ";
				else if (postTypeId == 5)
					result += "ПР";
			}

			if(postTypeIds.size()>0){
				result = "[" + result + "]";
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	@Override
	public List<ReportListItem> getReportListByUserIdnListFormCodesReportDateRange(long userId, List<String> idnList, List<String> formCodes,
																				   List<RefNpaItem> filterNpa, Date reportDateBegin, Date reportDateEnd, String languageCode) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ReportListItem> result = new ArrayList<ReportListItem>();
		List<Report> reports = new ArrayList<Report>();
		if (idnList == null || idnList.size() == 0) return result;
		if (formCodes == null || formCodes.size() == 0) return result;

		try {
			connection = getConnection();
			OracleConnection oraConn = connection.unwrap(OracleConnection.class);

			String[] arrFormCodes = formCodes.toArray(new String[formCodes.size()]);
			java.sql.Array arrayFormCodes = oraConn.createARRAY("FORM_CODE_ARRAY", arrFormCodes);

			String[] arrIdn = idnList.toArray(new String[idnList.size()]);
			java.sql.Array arrayIdn = oraConn.createARRAY("FORM_CODE_ARRAY", arrIdn);


			String sqlText = "SELECT r.ID,r.IDN, r.IDN_CHILD, r.REPORT_DATE,r.FORM_CODE, l.name_ru as respondent_name_ru, " +
					"nvl(l.short_name_ru,l.name_ru) as respondent_short_name_ru, s.name_ru as subjecttype_name_ru, s.short_name_ru as subjecttype_short_name_ru, " +
					"st.status_code, st.status_date, st.CONTROL_RESULT_CODE2, dep.name_ru as dep_name_ru,  " +
					"(select count(rf.id) from attached_file rf where rf.link_id = st.id and rf.file_kind = 1 ) as have_file, " +
					"(select count(rf.id) from attached_file rf where rf.link_id = st.id and rf.file_kind = 2 ) as have_letter, " +
					"(select count(rf.id) from attached_file rf where rf.link_id = st.id and rf.file_kind = 3 ) as have_files, " +
					"npa.name_ru as name_npa "+
					" FROM reports r " +
					" 	inner join table(?) t on r.form_code=t.column_value " +
					"	inner join table(?) i on r.idn=to_char(i.column_value) " +
					"	inner join forms f on f.code = r.form_code"+
					"	inner join form_history fh on fh.form_id = f.id and fh.begin_date <= r.report_date and (fh.end_date is null or fh.end_date > r.report_date)" +
					"	left join npa_forms nf on nf.form_history_id = fh.id " +
					"	left join v_ref_npa npa \n" +
					"        on npa.rec_id = nf.npa_rec_id \n" +
					"       and npa.begin_date = (select max(npa1.begin_date) \n" +
					"                               from v_ref_npa npa1\n" +
					"                              where npa1.rec_id = npa.rec_id\n" +
					"                                and npa1.begin_date <= r.report_date)" +
					"   inner join V_REF_UNIONPERSONS l on l.idn=r.idn and l.begin_date = (select max(t.begin_date) " +
					"                                                   from V_REF_UNIONPERSONS t " +
					"                                                  where t.rec_id = l.rec_id AND t.TYPE = l.TYPE" +
					"                                                    and t.begin_date <= ?) " +
					"                                    and (? < l.end_date or l.end_date is null) " +
					"   inner join v_ref_respondent resp on resp.REF_UNIONPERSONS=l.id and resp.begin_date = (select max(resp2.begin_date) " +
					"                                                   from v_ref_respondent resp2 " +
					"                                                  where resp2.rec_id = resp.rec_id " +
					"                                                    and resp2.begin_date <= ?) " +
					"                                     and (? < resp.end_date or resp.end_date is null) " +
					"   inner join v_ref_subject_type s on resp.ref_subject_type=s.id and s.begin_date = (select max(t.begin_date) " +
					"                                                   from v_ref_subject_type t " +
					"                                                 where t.rec_id = s.rec_id " +
					"                                                    and t.begin_date <= ?) " +
					"                                    and (? < s.end_date or s.end_date is null) " +
					"   left join v_ref_department dep on dep.id=resp.ref_department and dep.begin_date = (select max(t.begin_date) " +
					"                                                   from v_ref_department t " +
					"                                                 where t.rec_id = dep.rec_id " +
					"                                                    and t.begin_date <= ?) " +
					"                                    and (? < dep.end_date or dep.end_date is null) " +
					" 	inner join F_SESSION_RESP_FORMS sf on r.form_code = sf.FORM_CODE AND sf.user_id = ? AND sf.REF_RESPONDENT_REC_ID = resp.REC_ID " +
					"	INNER JOIN V_HISTORY_LAST_STATUS st ON st.ID=(SELECT max(h2.ID) FROM V_HISTORY_LAST_STATUS h2 WHERE h2.REPORT_ID=r.ID AND h2.status_code in ('APPROVED', 'DISAPPROVED', 'COMPLETED'))" +
					" WHERE TRUNC(r.report_date) BETWEEN NVL(TRUNC(?),TRUNC(r.report_date)) AND NVL(TRUNC(?),TRUNC(r.report_date))" +
					"		and st.status_code in (select substr(ri.name, instr(ri.name, ':')+1) as status_name " +
					"  								from right_items ri," +
					"       						f_session_right_items fri " +
					" 								where ri.parent = (select id " +
					"                     								from right_items t " +
					"                    								where t.name = 'REP_STAT') " +
					"   								and ri.id = fri.right_item_id " +
					"  	 								and fri.user_id = ?)";

			if(filterNpa != null && filterNpa.size() > 0) {
				StringBuilder sbNpa = new StringBuilder();
				Boolean noNpa = false;
				for (int i = 0; i < filterNpa.size(); i++) {
					if(filterNpa.get(i).getId() == 0)
						noNpa = true;
					if (i > 0)
						sbNpa.append(",");
					sbNpa.append(filterNpa.get(i).getRecId());
				}
				if(noNpa) {
					sqlText = sqlText + " and (nf.npa_rec_id is null or nf.npa_rec_id in (" + sbNpa.toString() + "))";
				}else {
					sqlText = sqlText + " and nf.npa_rec_id in (" + sbNpa.toString() + ")";
				}
			}

			ps = connection.prepareStatement(sqlText);
			//ps.setString(1, idn); // Doesn't work because JDBC doesn't wrap a value in quotes if a string value contains only numeric characters.
			ps.setArray(1, arrayFormCodes);
			ps.setArray(2, arrayIdn);
			ps.setDate(3, new java.sql.Date(new Date().getTime()));
			ps.setDate(4, new java.sql.Date(new Date().getTime()));
			ps.setDate(5, new java.sql.Date(new Date().getTime()));
			ps.setDate(6, new java.sql.Date(new Date().getTime()));
			ps.setDate(7, new java.sql.Date(new Date().getTime()));
			ps.setDate(8, new java.sql.Date(new Date().getTime()));
            ps.setDate(9, new java.sql.Date(new Date().getTime()));
            ps.setDate(10, new java.sql.Date(new Date().getTime()));
			ps.setLong(11, userId);
			ps.setDate(12, reportDateBegin == null ? null : new java.sql.Date(reportDateBegin.getTime()));
			ps.setDate(13, reportDateEnd == null ? null : new java.sql.Date(reportDateEnd.getTime()));
			ps.setLong(14, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				Report report = getReportFromResultSet(rs);
				//reports.add(report);

				/*List<ReportHistory> reportHistory = getReportHistoryByReportNoLobs(report);
				report.setReportHistory(reportHistory);*/

				ReportListItem item = new ReportListItem();
				item.setId(report.getId());
				item.setIdn(report.getIdn());
				item.setIdnChild(report.getIdnChild());
				item.setReportDate(report.getReportDate());
				item.setFormCode(report.getFormCode());
				List<Form> forms = getFormsByCodeLanguageReportDate(report.getFormCode(), languageCode, report.getReportDate(), connection);
				if (!forms.isEmpty()){
					Form form = forms.get(0);
					item.setFormName(form.getFormHistory().getName());
					item.setCanAttachedFile(form.getFormHistory().getFormTag() == null ? false : form.getFormHistory().getFormTag().canAttachedFile);
					item.setFormTypeCode(form.getTypeCode());
				}
				ReportHistory lastReport = getLastReportHistoryByReportIdNoLobs(report.getId(), true, connection);
				if (lastReport != null) {
					item.setSaveDate(lastReport.getSaveDate());
					item.setUserInfo(lastReport.getUserInfo());
					item.setDeliveryWay(lastReport.getDeliveryWayCode());
				}

				int completeCount = 0;
				for (ReportStatus status : getReportStatusHistoryByReportId(report.getId(), true, connection)) {
					if (status.getStatusCode().equals(ReportStatus.Status.COMPLETED.toString())) {
						completeCount++;
						if (item.getFirstCompletedDate() == null)
							item.setFirstCompletedDate(status.getStatusDate());
						item.setLastCompletedDate(status.getStatusDate());
					}
				}
				item.setCompleteCount(completeCount);

				item.setStatus(rs.getString("STATUS_CODE"));
				item.setStatusDate(rs.getDate("STATUS_DATE"));
				item.setStatusName(ReportStatus.resMap.get("ru" + "_" + item.getStatus()));


				item.setRespondentNameRu(rs.getString("respondent_name_ru"));
				item.setRespondentShortNameRu(rs.getString("respondent_short_name_ru"));
				item.setSubjectTypeNameRu(rs.getString("subjecttype_name_ru"));
				item.setSubjectTypeShortNameRu(rs.getString("subjecttype_short_name_ru"));
				item.setControlResultCode(rs.getString("CONTROL_RESULT_CODE2"));
				item.setHaveAttachedFile(rs.getInt("HAVE_FILE") > 0);
				item.setHaveAttachedLetter(rs.getInt("HAVE_LETTER") > 0);
				item.setDepartmentNameRu(rs.getString("DEP_NAME_RU"));
				item.setNameNPA(rs.getString("NAME_NPA"));
				item.setHaveFiles(rs.getInt("HAVE_FILES") > 0);
				if(item.getIdnChild() != null) {
					item.setChildRespondentNameRu(reference.getRespondentByIdn(item.getIdnChild(), report.getReportDate()).getPersonName());
				}
				result.add(item);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}


	private List<String> getUserStatuses(long userId){
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<String> result = new ArrayList<String>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select substr(ri.name, instr(ri.name, ':')+1) as status_name " +
					"  from right_items ri," +
					"       f_session_right_items fri " +
					" where ri.parent = (select id " +
					"                     from right_items t " +
					"                    where t.name = 'REP_STAT') " +
					"   and ri.id = fri.right_item_id " +
					"   and fri.user_id = ?");

			ps.setLong(1, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getString("STATUS_NAME"));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public PortalUser getRespondentCeo(long respondentId, Date date) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PortalUser> users = new ArrayList<PortalUser>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select u.* from f_users u inner join v_ref_post r on u.ref_post_rec_id=r.rec_id \n" +
					"          where u.ref_respondent_rec_id=?\n" +
					"            and u.blockfl=0\n" +
					"            and r.begin_date = (select max(ps.begin_date) \n" +
					"                                  from v_ref_post ps \n" +
					"                                 where ps.rec_id = r.rec_id\n" +
					"                                   and ps.begin_date <= ? and ps.type_post=5)" +
					"            and (? < r.end_date or r.end_date is null)");

			ps.setLong(1, respondentId);
			java.sql.Date sqlDate = new java.sql.Date(date.getTime());
			ps.setDate(2, sqlDate);
			ps.setDate(3, sqlDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				users.add(resultSetToPortalUser(rs));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return users.size() == 1 ? users.get(0) : null;
	}

	@Override
	public PortalUser getRespondentChiefAccountant(long respondentId, Date date) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PortalUser> users = new ArrayList<PortalUser>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select u.* from f_users u inner join v_ref_post r on u.ref_post_rec_id=r.rec_id \n" +
					"          where u.ref_respondent_rec_id=?\n" +
					"            and u.blockfl=0\n" +
					"            and r.begin_date = (select max(ps.begin_date) \n" +
					"                                  from v_ref_post ps \n" +
					"                                 where ps.rec_id = r.rec_id\n" +
					"                                   and ps.begin_date <= ? and ps.type_post=3)" +
					"            and (? < r.end_date or r.end_date is null)");

			ps.setLong(1, respondentId);
			java.sql.Date sqlDate = new java.sql.Date(date.getTime());
			ps.setDate(2, sqlDate);
			ps.setDate(3, sqlDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				users.add(resultSetToPortalUser(rs));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return users.size() == 1 ? users.get(0) : null;
	}

	@Override
	public PortalUser getRespondentDraftedBy(long respondentId, Date date) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<PortalUser> users = new ArrayList<PortalUser>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select u.* from f_users u inner join v_ref_post r on u.ref_post_rec_id=r.rec_id \n" +
					"          where u.ref_respondent_rec_id=?\n" +
					"            and u.blockfl=0\n" +
					"            and r.begin_date = (select max(ps.begin_date) \n" +
					"                                  from v_ref_post ps \n" +
					"                                 where ps.rec_id = r.rec_id\n" +
					"                                   and ps.begin_date <= ? and ps.type_post=4)" +
					"            and (? < r.end_date or r.end_date is null)");

			ps.setLong(1, respondentId);
			java.sql.Date sqlDate = new java.sql.Date(date.getTime());
			ps.setDate(2, sqlDate);
			ps.setDate(3, sqlDate);
			rs = ps.executeQuery();
			while (rs.next()) {
				users.add(resultSetToPortalUser(rs));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return users.size() == 1 ? users.get(0) : null;
	}

	/*@Override
	public Report getReportByApprovalId(long approvalId){
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Report result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select r.* " +
					" from reports r inner join approval a on r.form_code=a.form_name and r.report_date=a.report_date " +
					" and get_respondent_bin(a.respondent_id, a.report_date) = r.idn " +
					" where a.id = ? ");
			ps.setLong(1, approvalId);
			rs = ps.executeQuery();
			while (rs.next()) {
				result = getReportFromResultSet(rs);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			try { rs.close(); } catch (Exception e) {}
			try { ps.close(); } catch (Exception e) {}
			try { connection.close(); } catch (Exception e) {}
		}
		return result;
	}*/

	@Override
	public List<Report> getReportByRepDateFormRespondents(Date reportDate, String formName, List<RefRespondentItem> respondents) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Report> result = new ArrayList<Report>();
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<respondents.size();i++){
			if (i > 0)
				sb.append(",");
			sb.append("'").append(respondents.get(i).getIdn()).append("'");

		}
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT * FROM reports WHERE TRUNC(report_date) = TRUNC(?) AND form_code = ? AND idn in (" + sb.toString() + ")");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setDate(1, repDate);
			ps.setString(2, formName);
			//ps.setLong(3, respondentId);
			rs = ps.executeQuery();
			while (rs.next()) {
				Report report = getReportFromResultSet(rs);
				//System.out.println("item:"+item);
				result.add(report);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public List<Report> getReportByRepDateFormRespondentsStatusCode(Date reportDate, String formName, List<RefRespondentItem> respondents, String statusCode) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Report> result = new ArrayList<Report>();
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<respondents.size();i++){
			if(i>0)
				sb.append(",");
			sb.append("'").append(respondents.get(i).getIdn()).append("'");

		}
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT * " +
					" FROM reports r " +
					"	inner join REPORT_HISTORY st on st.id=(select max(st2.id) from V_HISTORY_LAST_STATUS st2 where st2.report_id=r.id AND st2.STATUS_CODE = ?)" +
					" WHERE TRUNC(report_date) = TRUNC(?) AND form_code = ? AND idn in (" + sb.toString() + ") ");
			java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
			ps.setString(1, statusCode);
			ps.setDate(2, repDate);
			ps.setString(3, formName);
			//ps.setLong(3, respondentId);
			rs = ps.executeQuery();
			while (rs.next()) {
				Report report = getReportFromResultSet(rs);
				//System.out.println("item:"+item);
				result.add(report);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public List<Report> getAllReports() {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Report> result = new ArrayList<Report>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT * FROM reports");
			rs = ps.executeQuery();
			while (rs.next()) {
				Report report = getReportFromResultSet(rs);
				result.add(report);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public String getReportValueByKeyReportHistoryId(String key, Long reportHistoryId) {
		Connection connection = null;
		CallableStatement stmt = null;
		ResultSet rs = null;
		String result = null;
		try {
			connection = getConnection();
			stmt = connection.prepareCall("BEGIN ? := pkg_frsi_util.get_report_value(?,?); end;");
			stmt.registerOutParameter(1, OracleTypes.VARCHAR);
			stmt.setString(2, key);
			stmt.setLong(3, reportHistoryId);
			stmt.execute();
			result = stmt.getString(1);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, stmt, rs);
		}
		return result;
	}

	// Out data of user
	@Override
	public List<ReportListItem> getReportListByIdnNoLobsV2(String idn, Date date, String code, String formType) {
		String s;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ReportListItem> result = new ArrayList<ReportListItem>();
		try {
			connection = getConnection();
			s = "SELECT r.id, r.idn, r.idn_child, r.report_date, r.form_code " +
					" FROM reports r INNER JOIN forms f ON r.form_code = f.code " +
					" WHERE r.report_date = nvl(?, r.report_date) and r.form_code = nvl(?, r.form_code)";
			if (idn != null && !idn.isEmpty())
				s += " and idn = '" + idn + "'";
			if (formType != null && !formType.isEmpty()) {
				s += " and f.TYPE_CODE = '" + formType + "'";
			}
			ps = connection.prepareStatement(s);
			ps.setTimestamp(1, date == null ? null : new java.sql.Timestamp(date.getTime()));
			ps.setString(2, code);
			//ps.setString(1, idn); // Doesn't work because JDBC doesn't wrap a value in quotes if a string value contains only numeric characters.
			rs = ps.executeQuery();
			while (rs.next()) {
				Report report = getReportFromResultSet(rs);

				ReportListItem item = new ReportListItem();
				item.setId(report.getId());
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
	public Long insertTmpReportV2(Long idReport, Long idReportHistory) {
		Connection connection = null;
		PreparedStatement ps = null;
		Long result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("INSERT INTO tmp_report_v2 (id_report, id_report_history) VALUES (?, ?)", new String[]{"id_report"});
			ps.setLong(1, idReport);
			ps.setLong(2, idReportHistory);

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
			result = idReport;
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
		return result;
	}

	@Override
	public void deleteTmpReportV2(Long idReport) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();

			ps = connection.prepareStatement("DELETE FROM tmp_report_v2 WHERE id_report = ?");
			ps.setLong(1, idReport);
			int affectedRows = ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public Long insertTmpReportV2Dtl(Long idReport, Long idKnd, String keyName, String keyValue) {
		Connection connection = null;
		PreparedStatement ps = null;
		Long result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("INSERT INTO tmp_report_v2_dtl (id, id_report, id_knd, key_name, key_value) VALUES (seq_tmp_report_v2_dtl.nextval, ?, ?, ?, ?)", new String[]{"id"});
			ps.setLong(1, idReport);
			ps.setLong(2, idKnd);
			ps.setString(3, keyName);
			ps.setString(4, keyValue);

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
			ResultSet generatedKeys = ps.getGeneratedKeys();
			if (generatedKeys.next()) result = generatedKeys.getLong(1);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
		return result;
	}

	@Override
	public Long insertGrpTmpReportV2Dtl(Long idReport, Long idKnd, Map<String, String> kvMap) {
		Connection connection = null;
		PreparedStatement ps = null;
		Long result = null;
		int affectedRows;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("INSERT INTO tmp_report_v2_dtl (id, id_report, id_knd, key_name, key_value) VALUES (seq_tmp_report_v2_dtl.nextval, ?, ?, ?, ?)", new String[]{"id"});
			ps.setLong(1, idReport);
			ps.setLong(2, idKnd);
			for (Map.Entry<String, String> entry : kvMap.entrySet()) {
				ps.setString(3, entry.getKey());
				ps.setString(4, entry.getValue().toString());
				affectedRows = ps.executeUpdate();
				if (affectedRows == 0) throw new SQLException("Inserting an item failed, no rows affected.");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
		return result;
	}

	@Override
	public Map<String, String> transformRefCaptionToRecId(String formCode, Date reportDate, String languageCode, Map<String, String> kvMap, String idn) {
		Map<String, String> resultMap = new HashMap<String, String>();
		List<Form> forms = getFormsByCodeLanguageReportDate(formCode, languageCode, reportDate, null);
		if (forms.size() == 0)
			return resultMap;

		FormHistory formHistory = getFormHistoryWithInputValueChecks(forms.get(0).getFormHistory().getId());

		Gson gson = new Gson();
		Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {
		}.getType();
		List<InputValueCheck> inputValueChecks = gson.fromJson(formHistory.getInputValueChecks(), typeListInputValueCheck);

		if (inputValueChecks == null)
			return resultMap;

		Map<String, List<InputValueCheck>> inputValueCheckIndex = new HashMap<String, List<InputValueCheck>>();
		for (InputValueCheck inputValueCheck : inputValueChecks) {
			if (inputValueCheck.getRef() != null && !inputValueCheck.getRef().trim().isEmpty() && inputValueCheck.getKey().contains("$D")) {
				if (!inputValueCheckIndex.containsKey(inputValueCheck.getKey()))
					inputValueCheckIndex.put(inputValueCheck.getKey(), new ArrayList<InputValueCheck>());
				inputValueCheckIndex.get(inputValueCheck.getKey()).add(inputValueCheck);
			}
		}

		for (Map.Entry<String, String> entry : kvMap.entrySet()) {
			String finalKey;
			int dPos = entry.getKey().indexOf("$D.");
			if (dPos == -1)
				finalKey = entry.getKey();
			else {
				finalKey = entry.getKey().substring(0, dPos) + "$DynamicRowId";
			}
			if (inputValueCheckIndex.containsKey(finalKey)) {
				String groupId = "";
				if (entry.getKey().contains(".")) {
					groupId = entry.getKey().substring(0, entry.getKey().lastIndexOf(".") + 1);
				}
				Long recId = -1L;
				for (InputValueCheck inputValueCheck : inputValueCheckIndex.get(finalKey)) {
					recId = reference.getRefRecId(inputValueCheck.getRef(), inputValueCheck.getRefCaption(), entry.getValue(), reportDate);
					if (!groupId.isEmpty() && inputValueCheck.getGroupId() != null && groupId.equals(inputValueCheck.getGroupId())) {
						break;
					}
				}
				if (recId == -1)
					throw new EJBException(MessageFormat.format("Значение rec_id не найдено для ключа {0} со значением {1} в отчете: код {2}, дата {3}, БИН {4}",
							entry.getKey(), entry.getValue(), formCode, reportDate, idn));
				resultMap.put(entry.getKey(), recId.toString());
			}
		}

		return resultMap;
	}

	// Аудит
	@Override
	public Long insertAuditEvent(AuditEvent auditEvent) throws OracleException{
		Connection connection = null;
		try {
			connection = getConnection();
			return insertAuditEvent(auditEvent, connection);
		} catch (SQLException e) {
			throw new EJBException(e);
		}finally {
			DbUtil.closeConnection(connection);
		}
	}

	@Override
	public Long insertAuditEventWithParams(AuditEvent auditEvent, AuditEventParam[] params) throws OracleException {
		Connection connection = null;
		Long id;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			id = insertAuditEvent(auditEvent, connection);
			for (AuditEventParam param : params) {
				param.setAuditEventId(id);
				insertAuditEventParam(param, connection);
			}
			connection.commit();
		} catch (SQLException e) {
			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException ex) {
					logger.error(ex.getMessage());
				}
			}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
		return id;
	}

	@Override
	public AuditEventParam getAuditEventParam(long auditEventId, String code) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		AuditEventParam result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT *\n" +
					"FROM AE_MAIN_PARAMS p\n" +
					"WHERE p.AE_MAIN_ID = ? AND p.CODE = ?");
			ps.setLong(1, auditEventId);
			ps.setString(2, code);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = getAuditEventParamFromRS(rs);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	private AuditEventParam getAuditEventParamFromRS(ResultSet rs) throws SQLException {
		AuditEventParam param = new AuditEventParam();
		param.setId(rs.getLong("id"));
		param.setAuditEventId(rs.getLong("ae_main_id"));
		param.setValueType(ValueType.valueOf(rs.getString("VALUE_TYPE")));

		Variant value = null;
		switch (param.getValueType()){
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
				throw new IllegalStateException("Cant't cast value type: " + param.getValueType().name());
		}
		if (rs.wasNull())
			param.setValue(null);
		else
			param.setValue(value);

		return param;
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
			if(Err_Code != 0) throw new SQLException(Err_Msg);
		} finally {
			DbUtil.closeConnection(null, stmt, null, ocs);
		}
		return id;
	}

	private void insertAuditEventParam(AuditEventParam param, Connection connection) throws SQLException {
		PreparedStatement ps = null;
		ValueType vt = param.getValueType();
		Variant value = param.getValue();
		try {
			ps = connection.prepareStatement("INSERT INTO AE_MAIN_PARAMS (ID, AE_MAIN_ID, CODE, VALUE_TYPE, INTEGER_VALUE, REAL_VALUE, BOOLEAN_VALUE, STRING_VALUE, DATE_VALUE)\n" +
					"VALUES (SEQ_AE_MAIN_PARAMS_ID.nextval, ?, ?, ?, ?, ?, ?, ?, ?)");

			ps.setLong(1,param.getAuditEventId());
			ps.setString(2,param.getCode());
			ps.setString(3, param.getValueType().name());
			if (vt == ValueType.NUMBER_0) {
				ps.setLong(4, param.getValue().getLngValue());
			} else {
				ps.setNull(4, OracleTypes.NULL);
			}
			if (value != null && (vt == ValueType.NUMBER_1
					|| vt == ValueType.NUMBER_2
					|| vt == ValueType.NUMBER_3
					|| vt == ValueType.NUMBER_4
					|| vt == ValueType.NUMBER_5
					|| vt == ValueType.NUMBER_6
					|| vt == ValueType.NUMBER_7
					|| vt == ValueType.NUMBER_8)) {
				ps.setDouble(5, value.getDblValue());
			} else {
				ps.setNull(5, OracleTypes.NULL);
			}
			if (value != null && vt == ValueType.BOOLEAN) {
				ps.setBoolean(6, value.getBoolValue());
			} else {
				ps.setNull(6, OracleTypes.NULL);
			}
			if (value != null && vt == ValueType.STRING && value.getStrValue() != null) {
				ps.setString(7, value.getStrValue());
			} else {
				ps.setNull(7, OracleTypes.NULL);
			}
			if (value != null && vt == ValueType.DATE && value.getDateValue() != null) {
				ps.setDate(8, new java.sql.Date(param.getValue().getDateValue().getTime()));
			} else {
				ps.setNull(8, OracleTypes.NULL);
			}

			ps.executeUpdate();
		} finally {
			DbUtil.closeConnection(null, ps);
		}
	}

	@Override
	public AuditEvent getAuditEvent(Long aeId, Connection connection){
		AuditEvent auditEvent = null;
		boolean localCon = false;
		ResultSet rs = null;
		PreparedStatement ps = null;

		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			ps = connection.prepareStatement(
					"SELECT\n" +
							"  m.id,\n" +
							"  m.date_in,\n" +
							"  m.name_object,\n" +
							"  m.code_object,\n" +
							"  ke.name    AS kind_event_name,\n" +
							"  m.date_event,\n" +
							"  up.idn,\n" +
							"  up.name_ru AS person_name\n" +
							"FROM ae_main m\n" +
							"  INNER JOIN ae_kind_event ke ON m.ae_kind_event = ke.id\n" +
							"  LEFT JOIN v_ref_respondent r ON m.ref_respondent = r.id\n" +
							"  LEFT JOIN V_REF_UNIONPERSONS up\n" +
							"    ON (r.REF_UNIONPERSONS = up.id\n" +
							"        AND up.BEGIN_DATE = (SELECT max(t.BEGIN_DATE)\n" +
							"                             FROM V_REF_UNIONPERSONS t\n" +
							"                             WHERE t.REC_ID = up.REC_ID AND t.TYPE = up.TYPE\n" +
							"                                   AND t.BEGIN_DATE <= m.DATE_EVENT))\n" +
							"WHERE m.id = ?");
			ps.setLong(1, aeId);
			rs = ps.executeQuery();

			if (rs.next()) {
				auditEvent = new AuditEvent();
				auditEvent.setId(rs.getLong("ID"));
				auditEvent.setDateIn(rs.getDate("DATE_IN"));
				auditEvent.setNameObject(rs.getString("NAME_OBJECT"));
				auditEvent.setCodeObject(rs.getString("CODE_OBJECT"));
				auditEvent.setKindEvent(rs.getString("KIND_EVENT_NAME"));
				auditEvent.setDateEvent(rs.getDate("DATE_EVENT"));
				auditEvent.setIdn(rs.getString("IDN"));
				auditEvent.setRespondentName(rs.getString("PERSON_NAME"));
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}

		return auditEvent;
	}

	@Override
	public List<AuditEvent> getAuditEventList(Date dateBegin, Date dateEnd, List<Long> respondentList, String userCode,
											  List<Long> eventNameList, List<Long> evenKindList, String codeObejct,
											  String nameObject, Boolean isArchive, Long userId){
		int Err_Code = 0;
		String Err_Msg = " ";
		List<AuditEvent> result = new ArrayList<AuditEvent>();

		Connection connection = null;
		CallableStatement stmt = null;
		OracleCallableStatement ocs = null;
		ResultSet cursor = null;

		try {
			connection = getConnection();

			StringBuilder sbRespondentId = new StringBuilder();
			if(respondentList != null && respondentList.size() > 0) {
				for (int i = 0; i < respondentList.size(); i++) {
					if (i > 0)
						sbRespondentId.append(",");
					sbRespondentId.append(respondentList.get(i));
				}
			}

			StringBuilder sbEventNameId = new StringBuilder();
			if(eventNameList != null && eventNameList.size() > 0) {
				for (int i = 0; i < eventNameList.size(); i++) {
					if (i > 0)
						sbEventNameId.append(",");
					sbEventNameId.append(eventNameList.get(i));
				}
			}

			StringBuilder sbEventKindId = new StringBuilder();
			if(evenKindList != null && evenKindList.size() > 0) {
				for (int i = 0; i < evenKindList.size(); i++) {
					if (i > 0)
						sbEventKindId.append(",");
					sbEventKindId.append(evenKindList.get(i));
				}
			}

			// вызов процедуры
			stmt = connection.prepareCall("{ call PKG_FRSI_AE.AE_READ_MAIN(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
			ocs.setTimestamp(1, dateBegin == null ? null : new java.sql.Timestamp(dateBegin.getTime()));
			ocs.setTimestamp(2, dateEnd == null ? null : new java.sql.Timestamp(dateEnd.getTime()));
			if(respondentList != null && respondentList.size() > 0)
				ocs.setString(3, sbRespondentId.toString());
			else
				ocs.setNull(3, OracleTypes.NULL);
			ocs.setString(4, userCode);
			if(eventNameList != null && eventNameList.size() > 0)
				ocs.setString(5, sbEventNameId.toString());
			else
				ocs.setNull(5, OracleTypes.NULL);
			if(evenKindList != null && evenKindList.size() > 0)
				ocs.setString(6, sbEventKindId.toString());
			else
				ocs.setNull(6, OracleTypes.NULL);
			ocs.setString(7, codeObejct);
			ocs.setString(8, nameObject);
			ocs.setBoolean(9, isArchive);
			ocs.setLong(10, userId);
			ocs.setString(11, groupPrefix);
			ocs.registerOutParameter(12, OracleTypes.CURSOR);
			ocs.registerOutParameter(13, OracleTypes.INTEGER);
			ocs.registerOutParameter(14, OracleTypes.VARCHAR);
			ocs.execute();
			Err_Code = ocs.getInt(13);
			Err_Msg = ocs.getString(14);

			cursor = ocs.getCursor(12);

			while (cursor.next()) {
				AuditEvent auditEvent = new AuditEvent();
				auditEvent.setId(cursor.getLong("ID"));
				auditEvent.setIdNameEvent(cursor.getLong("AE_NAME_EVENT"));
				auditEvent.setNameEvent(cursor.getString("NAME_EVENT"));
				auditEvent.setNameObject(cursor.getString("NAME_OBJECT"));
				auditEvent.setCodeObject(cursor.getString("CODE_OBJECT"));
				auditEvent.setIdKindEvent(cursor.getLong("AE_KIND_EVENT"));
				auditEvent.setKindEvent(cursor.getString("KIND_EVENT"));
				auditEvent.setDateEvent(cursor.getDate("DATE_EVENT"));
				auditEvent.setIdRefRespondent(cursor.getLong("REF_RESPONDENT"));
				auditEvent.setRespondentName(cursor.getString("RESPONDENT_NAME"));
				auditEvent.setDateIn(cursor.getDate("DATE_IN"));
				if(cursor.getLong("REC_ID") == 0)
					auditEvent.setRecId(null);
				else
					auditEvent.setRecId(cursor.getLong("REC_ID"));
				auditEvent.setUserId(cursor.getLong("USER_ID"));
				auditEvent.setUserName(cursor.getString("USER_NAME"));
				auditEvent.setScreenName(cursor.getString("SCREEN_NAME"));
				auditEvent.setUserLocation(cursor.getString("USER_LOCATION"));
				auditEvent.setDatlast(cursor.getDate("DATLAST"));
				auditEvent.setIsArchive(cursor.getInt("IS_ARCHIVE") > 0);
				auditEvent.setParentId(cursor.getLong("PARENT_ID"));
				result.add(auditEvent);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, stmt, cursor, ocs);
		}

		return result;
	}

	@Override
	public void moveToFromArchive(List<Long> eventList, Boolean isArchive) throws OracleException {
		int Err_Code = 0;
		String Err_Msg = " ";

		Connection connection = null;
		CallableStatement stmt = null;
		OracleCallableStatement ocs = null;

		try {
			connection = getConnection();

			OracleConnection oraConn = connection.unwrap(OracleConnection.class);
			Long[] arrEvent = eventList.toArray(new Long[eventList.size()]);
			java.sql.Array arrayEvent = oraConn.createARRAY("NUMBER_ARRAY", arrEvent);
			// вызов процедуры
			stmt = connection.prepareCall("{ call PKG_FRSI_AE.AE_MOVE_TO_FROM_ARCHIVE (?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ocs = stmt.unwrap(oracle.jdbc.OracleCallableStatement.class);
			ocs.setArray(1, arrayEvent);
			ocs.setBoolean(2, isArchive);
			ocs.setInt(3,1);
			ocs.registerOutParameter(4, OracleTypes.INTEGER);
			ocs.registerOutParameter(5, OracleTypes.VARCHAR);
			ocs.execute();
			Err_Code = ocs.getInt(4);
			Err_Msg = ocs.getString(5);
			if(Err_Code != 0) throw new OracleException(Err_Msg);
		} catch (SQLException e) {
			throw new EJBException(e);
		}finally {
			DbUtil.closeConnection(connection, stmt, null, ocs);
		}
	}

	@Override
	public List<Notice> getNoticeList(List<Long> eventNameList, String nameNotice){
		List<Notice> result = new ArrayList<Notice>();

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet cursor = null;

		try {
			connection = getConnection();

			StringBuilder sqlText = new StringBuilder();
			sqlText.append(
				"SELECT ke.id," +
				"		ke.name," +
				"		ke.message," +
				"		ke.subjectmsg," +
				"		ke.ae_name_event," +
				"		ne.name as name_event, " +
				"		nr.role_render, " +
				"		nr.group_render, " +
				"		nr.subject_type_render, " +
				"		nr.respondent_render, " +
				"		nr.user_render " +
				"  FROM ae_kind_event ke, " +
				" 	    ae_name_event ne, " +
				" 	    notice_render nr " +
				"WHERE  ke.ae_name_event = ne.id " +
				"	AND ke.is_active = 1" +
				"	AND nr.ae_kind_event = ke.id" +
				"	AND (ke.is_notice = 1 or ke.id = " + NOTICE_NEW_MESSAGE +")");

			if(nameNotice != null && !nameNotice.trim().isEmpty())
				sqlText.append(" AND upper(ke.name) like upper('%" + nameNotice + "%')");

			if(eventNameList != null && eventNameList.size() > 0) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < eventNameList.size(); i++) {
					if (i > 0)
						sb.append(",");
					sb.append(eventNameList.get(i));
				}
				sqlText.append(" AND ke.ae_name_event in (" + sb.toString() + ")");
			}

			ps = connection.prepareStatement(sqlText.toString());
			cursor = ps.executeQuery();

			while (cursor.next()) {
				Notice notice = new Notice();
				notice.setId(cursor.getLong("ID"));
				notice.setName(cursor.getString("NAME"));
				notice.setMessage(cursor.getString("MESSAGE"));
				notice.setSubjectMsg(cursor.getString("SUBJECTMSG"));
				notice.setIdNameEvent(cursor.getLong("AE_NAME_EVENT"));
				notice.setNameEvent(cursor.getString("NAME_EVENT"));
				notice.setRoleRender(cursor.getInt("ROLE_RENDER") > 0);
				notice.setGroupRender(cursor.getInt("GROUP_RENDER") > 0);
				notice.setStRender(cursor.getInt("SUBJECT_TYPE_RENDER") > 0);
				notice.setRespondentRender(cursor.getInt("RESPONDENT_RENDER") > 0);
				notice.setUserRender(cursor.getInt("USER_RENDER") > 0);
				result.add(notice);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, cursor);
		}
		return result;
	}

	@Override
	public void updateNoticeMessage(Long id, String subjectMsg, String message, AuditEvent auditEvent) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("UPDATE AE_KIND_EVENT SET message = ?, subjectmsg = ? WHERE id = ?");

			ps.setString(1, message);
			ps.setString(2, subjectMsg);
			ps.setLong(3, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");

			insertAuditEvent(auditEvent, connection);
			connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public List<NoticeSettings> getNoticeSettings(String tableName, Long idNotice){
		List<NoticeSettings> result = new ArrayList<NoticeSettings>();

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet cursor = null;

		try {
			connection = getConnection();

			StringBuilder sqlText = new StringBuilder();
			if (tableName.equals("notice_role")){
				sqlText.append("select r.name, \n" +
						"       decode(n.is_notice, null, 3, n.is_notice) isNotice, \n" +
						"       r.id \n" +
						"  from role r,\n" +
						"       (select * \n" +
						"          from notice_role\n" +
						"         where ae_kind_event = " + idNotice + ") n \n" +
						" where n.role(+) = r.id ");
			}else if (tableName.equals("notice_group")){
				sqlText.append("select g.name, \n" +
						"       decode(n.is_notice, null, 3, n.is_notice) isNotice,   \n" +
						"       g.group_id as id   \n" +
						"  from groups g, \n" +
						"       (select *   \n" +
						"          from notice_group\n" +
						"         where ae_kind_event = " + idNotice + ") n   \n" +
						" where n.group_id(+) = g.group_id\n" +
						"   and upper(g.name) like upper('" + groupPrefix + "%')");
			}else if(tableName.equals("notice_subjecttype")) {
				sqlText.append(
						"select st.name_ru as name,\n" +
						"       decode(n.is_notice, null, 3, n.is_notice) isNotice,\n" +
						"       st.rec_id as id\n" +
						"  from v_ref_subject_type st,\n" +
						"       (select *\n" +
						"          from notice_subjecttype \n" +
						"         where ae_kind_event = " + idNotice + ") n\n" +
						" where n.st_rec_id(+) = st.rec_id\n" +
						"   and st.begin_date = (select max(st1.begin_date)\n" +
						"                          from v_ref_subject_type st1\n" +
						"                         where st1.rec_id = st.rec_id\n" +
						"                           and st1.begin_date <= sysdate)");
			}else if (tableName.equals("notice_respondent")){
				sqlText.append("SELECT\n" +
						"  up.name_ru AS                             name,\n" +
						"  decode(n.is_notice, NULL, 3, n.is_notice) isNotice,\n" +
						"  r.rec_id   AS                             id\n" +
						"FROM v_ref_respondent r,\n" +
						"  V_REF_UNIONPERSONS up,\n" +
						"  (SELECT *\n" +
						"   FROM notice_respondent \n" +
						"   WHERE ae_kind_event =" +idNotice +") n\n" +
						"WHERE n.resp_rec_id (+) = r.rec_id\n" +
						"      AND r.REF_UNIONPERSONS = up.id\n" +
						"      AND r.begin_date = (SELECT max(r1.begin_date)\n" +
						"                          FROM v_ref_respondent r1\n" +
						"                          WHERE r1.rec_id = r.rec_id\n" +
						"                                AND r1.begin_date <= sysdate)\n" +
						"      AND up.begin_date = (SELECT max(up1.begin_date)\n" +
						"                           FROM V_REF_UNIONPERSONS up1\n" +
						"                           WHERE up1.rec_id = up.rec_id AND up1.TYPE = up.TYPE\n" +
						"                                 AND up1.begin_date <= sysdate)");
			}else if (tableName.equals("notice_user")){
				sqlText.append("select u.last_name || ' ' || u.first_name || ' ' || u.middle_name || '(' || u.screen_name || ')' as name,\n" +
						"       decode(n.is_notice, null, 3, n.is_notice) isNotice,  \n" +
						"       u.user_id as id  \n" +
						"  from f_users u,\n" +
						"		group_users gu,\n" +
						"		groups g,\n" +
						"       (select *  \n" +
						"          from notice_user\n" +
						"         where ae_kind_event = " + idNotice + ") n  \n" +
						" where n.user_id(+) = u.user_id \n" +
						"	and u.user_id = gu.user_id\n" +
						"	and gu.group_id = g.group_id\n" +
						"	and upper(g.name) like upper('" + groupPrefix + "%')\n" +
						"   and u.blockfl = 0");
			}

			ps = connection.prepareStatement(sqlText.toString());
			cursor = ps.executeQuery();

			while (cursor.next()) {
				NoticeSettings item = new NoticeSettings();
				item.setId(cursor.getLong("id"));
				item.setName(cursor.getString("name"));
				Integer isNotice = cursor.getInt("isNotice");
				if(isNotice == 0)
					item.setNotice(false);
				else if (isNotice == 1)
					item.setNotice(true);
				else if (isNotice == 3)
					item.setNotice(null);
				result.add(item);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, null, cursor);
		}
		return result;
	}

	@Override
	public void updateNoticeSettings(Long noticeId, List<NoticeSettings> stList, List<NoticeSettings> roleList, List<NoticeSettings> respList, List<NoticeSettings> userList, List<NoticeSettings> groupList, AuditEvent auditEvent) {
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);

			updateNoticeSettingsSt(connection, noticeId, stList);
			updateNoticeSettingsRole(connection, noticeId, roleList);
			updateNoticeSettingsRespondent(connection, noticeId, respList);
			updateNoticeSettingsUser(connection, noticeId, userList);
			updateNoticeSettingsGroup(connection, noticeId, groupList);

			insertAuditEvent(auditEvent, connection);

			connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
	}

	private void updateNoticeSettingsSt(Connection connection, Long noticeId, List<NoticeSettings> stList){
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("delete notice_subjecttype where ae_kind_event = ? ");
			ps.setLong(1, noticeId);
			ps.execute();

			for (NoticeSettings item : stList){
				if(item.getNotice() != null) {
					ps = connection.prepareStatement("insert into notice_subjecttype (ae_kind_event, st_rec_id, is_notice) values(?, ?, ?)");
					ps.setLong(1, noticeId);
					ps.setLong(2, item.getId());
					ps.setInt(3, item.getNotice() ? 1 : 0);
					ps.execute();
				}
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(null, ps);
		}
	}

	private void updateNoticeSettingsRole(Connection connection, Long noticeId, List<NoticeSettings> roleList){
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("delete notice_role where ae_kind_event = ? ");
			ps.setLong(1, noticeId);
			ps.execute();

			for (NoticeSettings item : roleList){
				if(item.getNotice() != null) {
					ps = connection.prepareStatement("insert into notice_role (ae_kind_event, role, is_notice) values(?, ?, ?)");
					ps.setLong(1, noticeId);
					ps.setLong(2, item.getId());
					ps.setInt(3, item.getNotice() ? 1 : 0);
					ps.execute();
				}
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(null, ps);
		}
	}

	private void updateNoticeSettingsRespondent(Connection connection, Long noticeId, List<NoticeSettings> respList){
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("delete notice_respondent where ae_kind_event = ? ");
			ps.setLong(1, noticeId);
			ps.execute();

			for (NoticeSettings item : respList){
				if(item.getNotice() != null) {
					ps = connection.prepareStatement("insert into notice_respondent (ae_kind_event, resp_rec_id, is_notice) values(?, ?, ?)");
					ps.setLong(1, noticeId);
					ps.setLong(2, item.getId());
					ps.setInt(3, item.getNotice() ? 1 : 0);
					ps.execute();
				}
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(null, ps);
		}
	}

	private void updateNoticeSettingsUser(Connection connection, Long noticeId, List<NoticeSettings> userList){
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("delete notice_user where ae_kind_event = ? ");
			ps.setLong(1, noticeId);
			ps.execute();

			for (NoticeSettings item : userList){
				if(item.getNotice() != null) {
					ps = connection.prepareStatement("insert into notice_user (ae_kind_event, user_id, is_notice) values(?, ?, ?)");
					ps.setLong(1, noticeId);
					ps.setLong(2, item.getId());
					ps.setInt(3, item.getNotice() ? 1 : 0);
					ps.execute();
				}
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(null, ps);
		}
	}

	private void updateNoticeSettingsGroup(Connection connection, Long noticeId, List<NoticeSettings> groupList){
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("delete notice_group where ae_kind_event = ? ");
			ps.setLong(1, noticeId);
			ps.execute();

			for (NoticeSettings item : groupList){
				if(item.getNotice() != null) {
					ps = connection.prepareStatement("insert into notice_group (ae_kind_event, group_id, is_notice) values(?, ?, ?)");
					ps.setLong(1, noticeId);
					ps.setLong(2, item.getId());
					ps.setInt(3, item.getNotice() ? 1 : 0);
					ps.execute();
				}
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(null, ps);
		}
	}

	@Override
	public List<NoticeMail> getNoticeMailList(Long userId){
		List<NoticeMail> result = new ArrayList<NoticeMail>();

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet cursor = null;

		try {
			connection = getConnection();
			ps = connection.prepareStatement(
					"select m.id, \n" +
					"       m.user_id_from,\n" +
					"       u_from.last_name || ' ' || u_from.first_name || ' ' || u_from.middle_name as USER_NAME_FROM,\n" +
					"       m.email_from,\n" +
					"       m.user_id_to,\n" +
					"       u_to.last_name || ' ' || u_to.first_name || ' ' || u_to.middle_name as USER_NAME_TO,\n" +
					"       m.email_to,\n" +
					"       m.message,\n" +
					"       m.subjectmsg,\n" +
					"       am.notice_sts,\n" +
					"       sts.name as name_sts,\n" +
					"       m.is_read,\n" +
					"       m.datlast,\n" +
					"		m.ae_kind_event,\n" +
					"		ke.name as kind_event_name" +
					"  from notice_mail m, \n" +
                    "       ae_main am,\n" +
					"       notice_sts sts,\n" +
					"       f_users u_from,\n" +
					"       f_users u_to,\n" +
					"		ae_kind_event ke" +
					" where m.ae_main = am.id\n" +
					"   and am.notice_sts = sts.id\n" +
					"   and m.user_id_from = u_from.user_id\n" +
					"   and m.user_id_to = u_to.user_id\n" +
					"	and m.ae_kind_event = ke.id\n" +
					"	and m.delfl = 0\n" +
					"   and m.user_id_to = ? " +
					" order by m.datlast desc");
			ps.setLong(1, userId);
			cursor = ps.executeQuery();

			while (cursor.next()) {
				NoticeMail item = new NoticeMail();
				item.setId(cursor.getLong("ID"));
				item.setUserIdFrom(cursor.getLong("USER_ID_FROM"));
				item.setUserNameFrom(cursor.getString("USER_NAME_FROM"));
				item.setEmailFrom(cursor.getString("EMAIL_FROM"));
				item.setUserIdTo(cursor.getLong("USER_ID_TO"));
				item.setUserNameTo(cursor.getString("USER_NAME_TO"));
				item.setEmailTo(cursor.getString("EMAIL_TO"));
				item.setNoticeSts(cursor.getInt("NOTICE_STS"));
				item.setNoticeStsName(cursor.getString("NAME_STS"));
				item.setIsRead(cursor.getInt("IS_READ") > 0);
				item.setDatlast(cursor.getTimestamp("DATLAST"));
				item.setMessage(cursor.getString("MESSAGE"));
				item.setSubjectMsg(cursor.getString("SUBJECTMSG"));
				item.setKindEventId(cursor.getLong("AE_KIND_EVENT"));
				item.setKindEventName(cursor.getString("KIND_EVENT_NAME"));
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
	public void setNoticeMailHowRead(Long mailId, int isRead, AuditEvent auditEvent){
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("UPDATE NOTICE_MAIL SET is_read = ? WHERE id = ?");

			ps.setInt(1, isRead);
			ps.setLong(2, mailId);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");

			insertAuditEvent(auditEvent, connection);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void deleteNoticeMail(Long id){
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("UPDATE NOTICE_MAIL SET delfl = 1 WHERE id = ?");

			ps.setLong(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void deleteNoticeMail(List<NoticeMail> noticeMailList, Long userId, String userLocation){
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < noticeMailList.size(); i++) {
				if (i > 0)
					sb.append(",");
				sb.append(noticeMailList.get(i).getId());
			}
			ps = connection.prepareStatement("UPDATE NOTICE_MAIL SET delfl = 1 WHERE id in (" + sb.toString() + ")");

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");

			Date date = new Date();

			AuditEvent auditEvent = new AuditEvent();
			auditEvent.setCodeObject("NOTICE_MAIL");
			auditEvent.setNameObject("DELETE_NOTICE");
			auditEvent.setIdKindEvent(127L);
			auditEvent.setDateEvent(date);
			auditEvent.setIdRefRespondent(null);
			auditEvent.setDateIn(date);
			auditEvent.setRecId(null);
			auditEvent.setUserId(userId);
			auditEvent.setUserLocation(userLocation);
			Long aeId = insertAuditEvent(auditEvent, connection);

			for(NoticeMail noticeMail : noticeMailList) {
				auditEvent.setParentId(aeId);
				auditEvent.setCodeObject("NOTICE_MAIL");
				auditEvent.setNameObject(noticeMail.getKindEventName());
				auditEvent.setIdKindEvent(127L);
				auditEvent.setDateEvent(date);
				auditEvent.setIdRefRespondent(null);
				auditEvent.setDateIn(date);
				auditEvent.setRecId(noticeMail.getId());
				auditEvent.setUserId(userId);
				auditEvent.setUserLocation(userLocation);
				insertAuditEvent(auditEvent, connection);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void insertNoticeMail(List<PortalUser> userToList, PortalUser userFrom, String userLocation, String subjectMsg, String message){
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);

            Timestamp curDate = new Timestamp(new Date().getTime());

            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject("send_message");
            auditEvent.setNameObject("Отправка сообщения");
            auditEvent.setIdKindEvent(NOTICE_NEW_MESSAGE);
            auditEvent.setDateEvent(curDate);
            auditEvent.setIdRefRespondent(null);
            auditEvent.setDateIn(curDate);
            auditEvent.setRecId(null);
            auditEvent.setUserId(userFrom.getUserId());
            auditEvent.setUserLocation(userLocation);
			Long aeId = insertAuditEvent(auditEvent, connection);

			if(!sendNotice(aeId, userFrom.getUserId(), userToList, subjectMsg, message, connection))
				throw new EJBException("Ошибка отправки сообщения!");

			for(PortalUser item : userToList) {
				message = changeOfVariables(message, aeId, userFrom.getUserId(), item.getUserId(), connection);
				subjectMsg = changeOfVariables(subjectMsg, aeId, userFrom.getUserId(), item.getUserId(), connection);

				ps = connection.prepareStatement(
                        "insert into notice_mail\n" +
						"  (id, user_id_from, email_from, user_id_to, email_to, message, ae_main, is_read, datlast, ae_kind_event, delfl, subjectmsg)\n" +
						"values\n" +
						"  (seq_notice_mail_id.nextval, ?, ?, ?, ?, ?, ?, 0, ?, ?, 0, ?)");
                ps.setLong(1, userFrom.getUserId());
                ps.setString(2, userFrom.getEmailAddress());
                ps.setLong(3, item.getUserId());
                ps.setString(4, item.getEmailAddress());
                ps.setString(5, message);
                ps.setLong(6, aeId);
                ps.setTimestamp(7, curDate);
                ps.setLong(8, NOTICE_NEW_MESSAGE);
				ps.setString(9, subjectMsg);
				ps.execute();
			}
			connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public String getNoticeMessageById(Long id){
		Connection connection = null;
		PreparedStatement ps = null;
		String result = null;
		ResultSet cursor = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select message from ae_kind_event where id = ?");
			ps.setLong(1, id);
			cursor = ps.executeQuery();
			while (cursor.next()) {
				result = cursor.getString("MESSAGE");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
		return result;
	}

	@Override
	public String getNoticeSubjectMessageById(Long id){
		Connection connection = null;
		PreparedStatement ps = null;
		String result = null;
		ResultSet cursor = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select subjectmsg from ae_kind_event where id = ?");
			ps.setLong(1, id);
			cursor = ps.executeQuery();
			while (cursor.next()) {
				result = cursor.getString("SUBJECTMSG");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
		return result;
	}

	@Override
	public List<NoticeSettings> getNoticeForUser(Long userId){
		List<NoticeSettings> result = new ArrayList<NoticeSettings>();

		Connection connection = null;

		try {
			connection = getConnection();

			Long roleId = getRoleByUserId(userId).getId();
			List<Notice> noticeList = getNoticeList(null, null);


			for (Notice notice : noticeList) {
				Boolean noticeUser;
				noticeUser = isNoticeUser("notice_user", userId, notice.getId());
				if (noticeUser == null) {
					if (roleId.equals(Constants.FRSI_ADMIN_ROLE) || roleId.equals(Constants.FRSI_ADMIN_DEPARTMENT_ROLE) || roleId.equals(Constants.FRSI_USER_NB_MAIN_ROLE) || roleId.equals(Constants.FRSI_USER_NB_DEPARTMENT_ROLE)) { // Администраторы и Пользователи
						result = checkGroupRoleNotice(userId, notice, result);
					} else if (roleId.equals(Constants.FRSI_RESPONDENT_ROLE)) { // Респонденты
						Boolean noticeRespondent = isNoticeUser("notice_respondent", userId, notice.getId());
						if(noticeRespondent == null){
							Boolean noticeSubjectType = isNoticeUser("notice_subjecttype", userId, notice.getId());
							if(noticeSubjectType == null){
								result = checkGroupRoleNotice(userId, notice, result);
							}
							else if (noticeSubjectType)
								result.add(getNoticeSettingFromNotice(notice, checkUserNoticeOff(userId, notice.getId(), 1), checkUserNoticeOff(userId, notice.getId(), 2)));
						}
						else if (noticeRespondent)
							result.add(getNoticeSettingFromNotice(notice, checkUserNoticeOff(userId, notice.getId(), 1), checkUserNoticeOff(userId, notice.getId(), 2)));
					}
				}
				else if(noticeUser)
					result.add(getNoticeSettingFromNotice(notice, checkUserNoticeOff(userId, notice.getId(), 1), checkUserNoticeOff(userId, notice.getId(), 2)));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
		return result;
	}

	private List<NoticeSettings> checkGroupRoleNotice(Long userId, Notice notice, List<NoticeSettings> noticeList){
		Boolean noticeGroup = isNoticeUser("notice_group", userId, notice.getId());
		if (noticeGroup == null){
			Boolean noticeRole = isNoticeUser("notice_role", userId, notice.getId());
			if(noticeRole != null) {
				if (noticeRole)
					noticeList.add(getNoticeSettingFromNotice(notice, checkUserNoticeOff(userId, notice.getId(), 1), checkUserNoticeOff(userId, notice.getId(), 2)));
			}
		}
		else if (noticeGroup)
			noticeList.add(getNoticeSettingFromNotice(notice, checkUserNoticeOff(userId, notice.getId(), 1), checkUserNoticeOff(userId, notice.getId(), 2)));

		return noticeList;
	}

	private NoticeSettings getNoticeSettingFromNotice(Notice notice, Boolean isNoticeOffMail, Boolean isNoticeOffSystem){
		NoticeSettings noticeSettings = new NoticeSettings();
		noticeSettings.setId(notice.getId());
		noticeSettings.setName(notice.getName());
		noticeSettings.setNoticeMail(!isNoticeOffMail);
		noticeSettings.setNoticeSystem(!isNoticeOffSystem);
		return noticeSettings;
	}

	@Override
	public Boolean checkUserNoticeOff(Long userId, Long kindEvent, int typeAddress){
		Boolean result = null;

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet cursor = null;

		try{
			connection = getConnection();

			String sqlText = "select count(*) as cnt ";
			if(typeAddress == 1){
				sqlText = sqlText + " from NOTICE_USER_OFF ";
			}else if (typeAddress == 2){
				sqlText = sqlText + " from NOTICE_USER_OFF_SYS ";
			}
			sqlText = sqlText +
					" where ae_kind_event = ?\n" +
					"   and user_id = ?";

			ps = connection.prepareStatement(sqlText);

			ps.setLong(1, kindEvent);
			ps.setLong(2, userId);

			cursor = ps.executeQuery();

			if(cursor.next()) {
				Integer isNoticeOff = cursor.getInt("cnt");
				if (isNoticeOff == 0)
					return false;
				else if (isNoticeOff == 1)
					return true;
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, cursor);
		}
		return result;
	}

	@Override
	public Boolean isNoticeUser(String tableName, Long userId, Long kindEvent){
		Boolean result = null;

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet cursor = null;

		try {
			connection = getConnection();

			StringBuilder sqlText = new StringBuilder();
			if (tableName.equals("notice_role")){
				sqlText.append(
						"select decode(nr.is_notice, null, 3, nr.is_notice) isNotice\n" +
						"  from notice_role nr,\n" +
						"       groups g,\n" +
						"       group_users gu\n" +
						" where nr.ae_kind_event = " + kindEvent + "\n" +
						"   and nr.role = g.role_id\n" +
						"   and g.group_id = gu.group_id\n" +
						"   and gu.user_id = " + userId);
			}else if (tableName.equals("notice_group")){
				sqlText.append(
						"select decode(ng.is_notice, null, 3, ng.is_notice) isNotice\n" +
						"  from notice_group ng,\n" +
						"       group_users gu\n" +
						" where ng.ae_kind_event = " + kindEvent + "\n" +
						"   and ng.group_id = gu.group_id\n" +
						"   and gu.user_id = " + userId);
			}else if(tableName.equals("notice_subjecttype")) {
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
			}else if (tableName.equals("notice_respondent")){
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
			}else if (tableName.equals("notice_user")){
				sqlText.append(
						"select decode(is_notice, null, 3, is_notice) isNotice\n" +
						"  from notice_user \n" +
						" where ae_kind_event = " + kindEvent + "\n" +
						"   and user_id = " + userId);
			}

			ps = connection.prepareStatement(sqlText.toString());
			cursor = ps.executeQuery();

			if(cursor.next()) {
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
			DbUtil.closeConnection(connection, ps, cursor);
		}
		return result;
	}

	@Override
	public void insertNoticeUserOff(List<NoticeSettings> settingsList, Long userId, String userLocation, Connection connection) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}

			if(settingsList != null && settingsList.size() > 0){
				ps = connection.prepareStatement("delete notice_user_off where user_id = ?");
				ps.setLong(1, userId);
				ps.execute();

				ps = connection.prepareStatement("delete notice_user_off_sys where user_id = ?");
				ps.setLong(1, userId);
				ps.execute();

				Date date = new Date();

				AuditEvent auditEvent = new AuditEvent();
				auditEvent.setCodeObject("NOTICE_USER_SETTINGS");
				auditEvent.setNameObject("NOTICE");
				auditEvent.setIdKindEvent(128L);
				auditEvent.setDateEvent(date);
				auditEvent.setIdRefRespondent(null);
				auditEvent.setDateIn(date);
				auditEvent.setRecId(null);
				auditEvent.setUserId(userId);
				auditEvent.setUserLocation(userLocation);
				Long aeId = insertAuditEvent(auditEvent, connection);

				for(NoticeSettings item : settingsList) {
					if(!item.getNoticeMail()) {
						ps = connection.prepareStatement(
								"insert into notice_user_off\n" +
										"  (ae_kind_event, user_id)\n" +
										"values\n" +
										"  (?, ?)");
						ps.setLong(1, item.getId());
						ps.setLong(2, userId);
						ps.execute();
					}
					if(!item.getNoticeSystem()) {
						ps = connection.prepareStatement(
								"insert into notice_user_off_sys\n" +
										"  (ae_kind_event, user_id)\n" +
										"values\n" +
										"  (?, ?)");
						ps.setLong(1, item.getId());
						ps.setLong(2, userId);
						ps.execute();
					}

					auditEvent.setParentId(aeId);
					auditEvent.setCodeObject("NOTICE");

					String str = (item.getNoticeMail() ? "Подключил " : "Отключил ") +
							"уведомление на почту: " + item.getName();
					str = str + (item.getNoticeSystem() ? "Подключил " : "Отключил ") +
							"уведомление в систему: " + item.getName();

					auditEvent.setNameObject(str);
					auditEvent.setIdKindEvent(128L);
					auditEvent.setDateEvent(date);
					auditEvent.setIdRefRespondent(null);
					auditEvent.setDateIn(date);
					auditEvent.setRecId(item.getId());
					auditEvent.setUserId(userId);
					auditEvent.setUserLocation(userLocation);
					insertAuditEvent(auditEvent, connection);

				}
			}

			if (localCon) {
				connection.commit();
			}
		} catch (SQLException e) {
			if (localCon) {
				if (connection != null) {
					try {
						connection.rollback();
					} catch (SQLException ex) {
						logger.error(ex.getMessage());
					}
				}
			}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public boolean sendNotice(Long aeId, Long userIdFrom, List<PortalUser> portalUserList, String subjectMsg, String textMessage, Connection connection){
		try {
			InitialContext ctx = new InitialContext();
			Session session = (Session) ctx.lookup("mail/mailfrsi");

			// Создание email и заголовков.
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(systemUserEmail));

			// Отправка письма
			Transport transport = session.getTransport();
			transport.connect();
			for(PortalUser portalUser : portalUserList) {
				textMessage = changeOfVariables(textMessage, aeId, userIdFrom, portalUser.getUserId(), connection);
				subjectMsg = changeOfVariables(subjectMsg, aeId, userIdFrom, portalUser.getUserId(), connection);
				message.setText(textMessage);
				message.setSubject(subjectMsg);
				transport.sendMessage(message, InternetAddress.parse(portalUser.getEmailAddress()));
			}
			transport.close();
			return true;
		}catch (MessagingException me){
			return false;
			//throw new EJBException(me);
		} catch (Exception e){
			return false;
			//throw new EJBException(e);
		}
	}

	@Override
	public String changeOfVariables(String message, Long aeId, Long userIdFrom, Long userIdTo, Connection connection){
		if(message == null) {
			return message;
		}

		if(message.contains("@{ObjectDate}") || message.contains("@{RespondentInfo}") || message.contains("@{IDN}") ||
				message.contains("@{ObjectName}") || message.contains("@{ObjectCode}") || message.contains("@{NoticeName}") ||
				message.contains("@{DateEvent}")){

			AuditEvent auditEvent = getAuditEvent(aeId, connection);

			message = message.replace("@{ObjectDate}", Convert.getDateStringFromDate(auditEvent.getDateIn())).
					replace("@{RespondentInfo}", auditEvent.getRespondentName() == null ? " " : auditEvent.getRespondentName()).
					replace("@{IDN}", auditEvent.getIdn() == null ? " " : auditEvent.getIdn()).
					replace("@{ObjectName}", auditEvent.getNameObject()).
					replace("@{ObjectCode}", auditEvent.getCodeObject()).
					replace("@{NoticeName}", auditEvent.getKindEvent()).
					replace("@{DateEvent}", Convert.getDateTimeStringFromDateRus(auditEvent.getDateEvent()));

		}

		if(message.contains("@{SenderFIO}")){
			PortalUser portalUser = getUserByUserId(userIdFrom, connection);
			message = message.replace("@{SenderFIO}", portalUser.getLastName() + " " + portalUser.getFirstName() + " " + portalUser.getMiddleName());
		}

		if(message.contains("@{AcceptorFIO}")){
			PortalUser portalUser = getUserByUserId(userIdTo, connection);
			message = message.replace("@{AcceptorFIO}", portalUser.getLastName() + " " + portalUser.getFirstName() + " " + portalUser.getMiddleName());
		}

		return message;
	}

	@Override
	public void testInsert(int n) {
		Connection connection = null;
		PreparedStatement ps = null;
		AbstractReference result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("INSERT INTO TEST_TABLE_UNIQ (field1) VALUES (?)");
			ps.setInt(1, n);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public List<Integer> getTestNumbers() {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Integer> result = new ArrayList<Integer>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select field1 from TEST_TABLE_UNIQ");
			rs = ps.executeQuery();
			while (rs.next()) {
				Integer n = rs.getInt("field1");
				result.add(n);
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}

		return result;
	}

	/**
	 * Возвращает максимальную версию шаблона (по всем версиям xml-шаблона)
	 * @param code
	 * @return
	 */
	@Override
	public int getFormMaxXlsVersion(String code, Date date) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select max(h.xls_version) xls_version from form_history h inner join forms f on h.form_id=f.id \n" +
					"where f.code=? and h.begin_date = ?");
			ps.setString(1, code);
			java.sql.Date bDate = new java.sql.Date(date.getTime());
			ps.setDate(2, bDate);

			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt("xls_version");
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}

		return 0;
	}

	/**
	 * Заполняет map начальными значниями из заданной формы
	 * @param formId
	 * @param reportDate
	 * @param keyValueMap
	 */
	protected void fillInitialData(Date reportDate, Map<String, String> keyValueMap, String formCode, String languageCode){
		List<Form> forms = getFormsByCodeLanguageReportDate(formCode, languageCode, reportDate, null);
		if (forms.size() == 0)
			return;

		// TODO здесь кажется можно использовать formHistory из forms
		FormHistory formHistory2 = getFormHistoryWithInputValueChecks(forms.get(0).getFormHistory().getId());
		Gson gson = new Gson();
		Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {
		}.getType();
		List<InputValueCheck> inputValueChecks = gson.fromJson(formHistory2.getInputValueChecks(), typeListInputValueCheck);
		if (inputValueChecks == null) {
			return;
		}
		for (InputValueCheck inputValueCheck : inputValueChecks) {
			if (inputValueCheck.getKey() != null && !inputValueCheck.getKey().trim().isEmpty()
					&& inputValueCheck.getDefaultValue() != null) {
				if (inputValueCheck.getReadonly() != null && inputValueCheck.getReadonly().booleanValue()) {
					keyValueMap.put(inputValueCheck.getKey(), inputValueCheck.getDefaultValue());
				} else if (!keyValueMap.containsKey(inputValueCheck.getKey())) {
					keyValueMap.put(inputValueCheck.getKey(), inputValueCheck.getDefaultValue());
				}
			}
		}
	}

	/**
	 * Заполняет map значниями пользователей (первый руководитель, главный бухгалтер, испольнитель)
	 * @param formCode
	 * @param respondentId
	 * @param date
	 * @param keyValueMap
	 */
	protected void fillUserData(String formCode, long respondentId, Date date, Map<String, String> keyValueMap){
		String ceoInputName = genInputName(formCode, null, "ceo", "", "");
		if (keyValueMap.get(ceoInputName) == null || keyValueMap.get(ceoInputName).trim().isEmpty()) {
			PortalUser user = getRespondentCeo(respondentId, date);
			if (user != null)
				keyValueMap.put(ceoInputName, getUserFIO(user));
		}

		String chiefAccountantInputName = genInputName(formCode, null, "chief_accountant", "", "");
		if (keyValueMap.get(chiefAccountantInputName) == null || keyValueMap.get(chiefAccountantInputName).trim().isEmpty()) {
			PortalUser user = getRespondentChiefAccountant(respondentId, date);
			if (user != null)
				keyValueMap.put(chiefAccountantInputName, getUserFIO(user));
		}

		String draftedByInputName = genInputName(formCode, null, "drafted_by", "", "");
		if (keyValueMap.get(draftedByInputName) == null || keyValueMap.get(draftedByInputName).trim().isEmpty()) {
			PortalUser user = getRespondentDraftedBy(respondentId, date);
			if (user != null)
				keyValueMap.put(draftedByInputName, getUserFIO(user));
		}
	}

	private String genInputName(String formCode, String tableName, String name, String key, String keyValue) {
		if (tableName == null || tableName.equals(""))
			return formCode + "*" + name + ":" + key + ":" + keyValue;
		else
			return formCode + "_" + tableName + "*" + name + ":" + key + ":" + keyValue;
	}

	/**
	 * Возвращает ФИО пользователя в сокращенном виде
	 * @param user
	 * @return
	 */
	protected String getUserFIO(PortalUser user) {
		StringBuilder b = new StringBuilder();

		if (user.getLastName() != null)
			b.append(user.getLastName());
		else
			return b.toString();

		if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty())
			b.append(" " + user.getFirstName().trim().toUpperCase().substring(0, 1) + ".");
		else
			return b.toString();

		if (user.getMiddleName() != null && !user.getMiddleName().trim().isEmpty())
			b.append(" " + user.getMiddleName().trim().toUpperCase().substring(0, 1) + ".");

		return b.toString();
	}

	@Override
	public Long saveAndGetId(String source, Report report, Long respondentId, Long formId, AbstractUser user, Date curDate, Map<String, String> keyValueMap, Date prevReportDate, boolean isAutoSave) {
		Long resultId = null;

		Long kindEvent = null;
		String message = "";
		boolean fillExpectedData = true;
		String deliveryWayCode = "";

		if (!source.equals("WEB_COPY"))
			fillInitialData(report.getReportDate(), keyValueMap, report.getFormCode(), "ru");

		if ((source.equals("WEB") && report.getId() == null) || (source.equals("EXCEL")) || (source.equals("XML")))
			fillUserData(report.getFormCode(), respondentId, report.getReportDate(), keyValueMap);

		if (source.equals("WEB")) {
			deliveryWayCode = "WEB_FORM";
			if (report.getId() == null) { // new report
				kindEvent = 1L;
				message = "Создал(а): " + user.getDisplayName() + " [" + user.getLocation() + "]";
				fillExpectedData = true;
			} else { // existing report
				kindEvent = 3L;
				message = "Изменил(а): " + user.getDisplayName() + " [" + user.getLocation() + "]";
				fillExpectedData = false;
			}
		}
		else if (source.equals("WEB_COPY")) {
			deliveryWayCode = "WEB_FORM";
			kindEvent = 9L;
			message = "Отчет скопирован от " + prevReportDate + ", " + user.getDisplayName() + " [" + user.getLocation() + "]";
			fillExpectedData = true;
		}
		else if (source.equals("EXCEL")) {
			deliveryWayCode = "EXCEL";
			kindEvent = 17L;
			message = "Загрузил(а): " + user.getDisplayName() + " [" + user.getLocation() + "]" + ". Загружен из Excel-файла.";
			fillExpectedData = true;
		}
		else if (source.equals("XML")) {
			deliveryWayCode = "XML";
			kindEvent = 16L;
			message = "Загрузил(а): " + user.getDisplayName() + " [" + user.getLocation() + "]" + ". Загружен из XML-файла.";
			fillExpectedData = true;
		}

		if (!source.equals("WEB_COPY"))
			keyValueMap = updateCalculatedFields(report.getFormCode(), report.getReportDate(), keyValueMap, "ru", fillExpectedData);

		Form form = getForm(formId, report.getReportDate());

		clearSignDate(form.getFormHistory().getInputValueChecks(), keyValueMap);

		//keyValueMap.putAll(getUpdatedCurrentDate(form.getFormHistory().getInputValueChecks(), keyValueMap, curDate));

		if (source.equals("WEB") && report.getId() != null) {
			ReportHistory reportHistory = getLastReportHistoryByReportId(report.getId(), true, false, false, null);
			Type typeMapStringString = new TypeToken<Map<String, String>>() {
			}.getType();
			Map<String, String> oldInputValues = gson.fromJson(reportHistory.getData(), typeMapStringString);
			if (!isModified(oldInputValues, keyValueMap))
				return report.getId();
		}

		if (report.getId() != null) {
			clearSignatures(report.getId());
		}


		ReportHistory reportHistory = new ReportHistory();
		reportHistory.setReport(report);
		reportHistory.setSaveDate(curDate);
		String jsonData = gson.toJson(keyValueMap);
		reportHistory.setData(jsonData);
		reportHistory.setDataSize((long) reportHistory.getData().length());
		reportHistory.updateHash(null);
		reportHistory.setDeliveryWayCode(deliveryWayCode);
		reportHistory.setUserId(user.getId());
		reportHistory.setUserInfo(user.getDisplayName());
		reportHistory.setKvMap(keyValueMap);

		ReportStatus reportStatus;
		if (report.getId() != null && isAutoSave) {
			reportStatus = null;
		} else {
			reportStatus = new ReportStatus();
			reportStatus.setReportHistory(reportHistory);
			reportStatus.setStatusCode("DRAFT");
			reportStatus.setStatusDate(curDate);
			reportStatus.setUserId(user.getId());
			reportStatus.setUserInfo(user.getDisplayName());
			reportStatus.setUserLocation(user.getLocation());
			reportStatus.setMessage(message);
		}

		AuditEvent auditEvent = new AuditEvent();
		auditEvent.setCodeObject(report.getFormCode());
		auditEvent.setNameObject(null);
		auditEvent.setIdKindEvent(kindEvent);
		auditEvent.setDateEvent(curDate);
		auditEvent.setIdRefRespondent(respondentId);
		auditEvent.setDateIn(report.getReportDate());
		auditEvent.setRecId(report.getId());
		auditEvent.setUserId(user.getId());
		auditEvent.setUserLocation(user.getLocation());

		if (report.getId() == null) {
			resultId = createReport(report, reportHistory, reportStatus, form, auditEvent);
		} else {
			updateReport(report, reportHistory, reportStatus, form, auditEvent);
			resultId = report.getId();
		}

		return resultId;
	}

	@Override
	public String getHashFiles(Long reportHistoryId){
		String hashFiles = null;
		if(reportHistoryId != null) {
			List<AttachedFile> attachedFileList = getFileListByLinkId(reportHistoryId, 3, null);
			for (AttachedFile item : attachedFileList) {
				hashFiles = hashFiles + item.getHash();
			}
		}
		return hashFiles;
	}

	private boolean isModified(Map<String, String> oldInputValues, Map<String, String> newInputValues) {
		Set<String> removedKeys = new HashSet<String>(oldInputValues.keySet());
		removedKeys.removeAll(newInputValues.keySet());
		if(removedKeys.size()>0){
			return  true;
		}

		Set<String> addedKeys = new HashSet<String>(newInputValues.keySet());
		addedKeys.removeAll(oldInputValues.keySet());
		if (addedKeys.size() > 0) {
			return true;
		}


		Set<Map.Entry<String, String>> changedEntries = new HashSet<Map.Entry<String, String>>(
				newInputValues.entrySet());
		changedEntries.removeAll(oldInputValues.entrySet());

		for (Map.Entry<String, String> newEntry : changedEntries) {
			String oldValue = oldInputValues.get(newEntry.getKey());
			String newValue = newEntry.getValue();

			if (newValue != null && oldValue != null) {
				if (!newValue.equals(oldValue)) {
					return true;
				}
			}

			if (newValue != oldValue) {
				return true;
			}
		}
		return false;
	}

	private void updateReportHistoryIsExistList(long reportHistoryId, boolean isExistList, Connection connection){
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
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


	@Override
	public boolean fillDefaultValueByFormWithValues(Connection connection, Long reportHistoryId, String inputValueCheck, String reportHistoryData, boolean insRefValue){
		boolean result = false;
		if(reportHistoryId != null && inputValueCheck != null && reportHistoryData != null) {
			List<InputValueCheck> inputValueChecks;
			Gson gson = new Gson();
			Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {}.getType();
			inputValueChecks = gson.fromJson(inputValueCheck, typeListInputValueCheck);

			String jsonData = reportHistoryData;
			Type typeMapStringString = new TypeToken<Map<String, String>>() {}.getType();
			Map<String, String> kvMap = gson.fromJson(jsonData, typeMapStringString);
			Map<String,String> refRecIdMap = new HashMap<String, String>();

			int n;
			String key;

			for (InputValueCheck item : inputValueChecks) {
				if(insRefValue) {
					if (item.getRef() != null && !item.getRef().isEmpty()) {
						if (item.getKey() != null) {
							n = item.getKey().indexOf("$DynamicRowId");
							if (n >= 0)
								key = item.getKey().substring(0, n + 2);
							else
								key = item.getKey();

							for (Map.Entry<String, String> entry : kvMap.entrySet()) {
								if (entry.getKey() != null && entry.getKey().contains(key)) {
									if (entry.getValue() != null && !entry.getValue().trim().isEmpty() && !entry.getValue().equals("-1") && Validators.IsValidLong(entry.getValue())){
										if (item.getMultiValue() != null && item.getMultiValue()) {
											try {
												MultiSelectValue multiSelectValue = gson.fromJson(entry.getValue(), MultiSelectValue.class);
												for(String value : multiSelectValue.getValues()){
													refRecIdMap.put(value,item.getRef());
												}
											} catch (JsonSyntaxException e) {
//												errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Неправильные данные для многозначного справочника"));
												throw new EJBException("Неправильные данные для многозначного справочника");
											}
										} else {
											refRecIdMap.put(entry.getValue(), item.getRef());
										}
									}
								}
							}
						}
					}
				}
			}
			if (insRefValue) {
				if(refRecIdMap.size() > 0){
					deleteReportRefLink(connection, reportHistoryId, null);
					for (Map.Entry<String, String> repRefLinkMap : refRecIdMap.entrySet()) {
						if(repRefLinkMap.getKey() != null && repRefLinkMap.getValue() != null) {
							insertReportRefLink(connection, reportHistoryId, repRefLinkMap.getValue(), Long.valueOf(repRefLinkMap.getKey()));
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public Date getSignDate(Long reportHistoryId, String valueFunc, Date date, Connection connection){
		boolean localCon = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Date result = null;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }
			String sqlText = "";
			if(valueFunc.equalsIgnoreCase("FirstSignDate")) {
				sqlText = "select min(t.sign_date) as sign_date\n";
			}else if (valueFunc.equalsIgnoreCase("LastSignDate"))
				sqlText = "select max(t.sign_date) as sign_date\n";
			else{
				sqlText = "select t.sign_date\n";
			}

			sqlText = sqlText +
					"  from REPORT_SIGN t,\n" +
					"       v_ref_post p\n" +
					" where t.report_history_id = ?\n" +
					"   and t.ref_post = p.REC_ID\n" +
					"   and p.BEGIN_DATE = (select max(p1.begin_date)\n" +
					"                         from v_ref_post p1\n" +
					"                        where p1.rec_id = p.rec_id\n" +
					"                          and p1.begin_date <= ?)\n" +
					"   and (p.END_DATE is null or p.end_date > ?)";
			if(valueFunc.equalsIgnoreCase("CeoSignDate")) {
				sqlText = sqlText + " and p.type_post = 5";
			}else if (valueFunc.equalsIgnoreCase("ChiefSignDate")) {
				sqlText = sqlText + " and p.type_post = 3";
			}

			ps = connection.prepareStatement(sqlText);
			ps.setLong(1, reportHistoryId);
			java.sql.Date sqlReportDate = new java.sql.Date(date.getTime());
			ps.setDate(2, sqlReportDate);
			ps.setDate(3, sqlReportDate);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getDate("sign_date");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	private void clearSignDate(String inputValueCheck, Map<String, String> kvMap) {
		if(inputValueCheck != null && kvMap != null) {
			Gson gson = new Gson();
			Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {
			}.getType();
			List<InputValueCheck> inputValueChecks = gson.fromJson(inputValueCheck, typeListInputValueCheck);
			for (InputValueCheck item : inputValueChecks) {
				if (item.getValueFunc() != null) {
					if(item.getValueFunc().equalsIgnoreCase("FirstSignDate") || item.getValueFunc().equalsIgnoreCase("LastSignDate") ||
							item.getValueFunc().equalsIgnoreCase("CeoSignDate") || item.getValueFunc().equalsIgnoreCase("ChiefSignDate")){
						//if(kvMap.get(item.getKey()) != null && !kvMap.get(item.getKey()).isEmpty()){
							kvMap.remove(item.getKey());
						//}
					}
				}
			}
		}
	}

	@Override
	public void updateSignDate(Long reportHistoryId, Date date, String inputValueCheck, Map<String, String> kvMap, Connection connection) {
		if(inputValueCheck != null && kvMap != null) {
			Gson gson = new Gson();
			Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {
			}.getType();
			List<InputValueCheck> inputValueChecks = gson.fromJson(inputValueCheck, typeListInputValueCheck);
			for (InputValueCheck item : inputValueChecks) {
				if (item.getValueFunc() != null) {
					if(item.getValueFunc().equalsIgnoreCase("FirstSignDate") || item.getValueFunc().equalsIgnoreCase("LastSignDate") ||
							item.getValueFunc().equalsIgnoreCase("CeoSignDate") || item.getValueFunc().equalsIgnoreCase("ChiefSignDate")){
						if(kvMap.get(item.getKey()) == null || kvMap.get(item.getKey()).isEmpty()){
							Date signDate = getSignDate(reportHistoryId, item.getValueFunc(), date, connection);
							if(signDate != null) {
								kvMap.put(item.getKey(), Convert.getDateStringFromDate(signDate));
							}
						}
					}
				}
			}
		}
	}

	@Override
	public Map<String, String> getUpdatedCurrentDate(String inputValueCheck, Map<String, String> kvMap, Date defaultDate) {
		Map<String, String> result = new HashMap<String, String>();
		if(inputValueCheck != null && kvMap != null && defaultDate != null) {
			List<InputValueCheck> inputValueChecks;
			Gson gson = new Gson();
			Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {
			}.getType();
			inputValueChecks = gson.fromJson(inputValueCheck, typeListInputValueCheck);

			for (InputValueCheck item : inputValueChecks) {
				if (item.getValueFunc() != null) {
					if(item.getValueFunc().equals("CurrentDate")){
						if(kvMap.get(item.getKey()) == null || !Validators.equalDates(Convert.getDateFromString(kvMap.get(item.getKey())), defaultDate)){
							result.put(item.getKey(), Convert.getDateStringFromDate(defaultDate));
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public boolean updateCurrentDate(long reportHistoryId, long respondentId, AbstractUser user) {
		long reportId = getReportIdByReportHistoryId(reportHistoryId, null);
		Report report = getReport(reportId, null);
		Long formId = getFormIdByCode(report.getFormCode());
		Date newReportDate = new Date();
		Form formWithInputValueChecks = getForm(formId, newReportDate);

		ReportHistory reportHistory = getReportHistory(reportHistoryId, true, false);
		Type typeMapStringString = new TypeToken<Map<String, String>>() {}.getType();
		Map<String, String> kvMap = gson.fromJson(reportHistory.getData(), typeMapStringString);

		Map<String, String> currDateMap = getUpdatedCurrentDate(formWithInputValueChecks.getFormHistory().getInputValueChecks(),kvMap,newReportDate);
		if(currDateMap.size()==0)
			return false;
		else {
			saveAndGetId("WEB",report,respondentId,formId,user,newReportDate,kvMap,report.getReportDate(),false);
			return true;
		}
	}

	@Override
	public List<ReportListItem> getAllReportsForInfo(long userId, List<Long> subjectTypeRecIdList, List<Long> respRecIdList, List<String> formCodesList, Date date, String languageCode, Boolean stateSender, AuditEvent auditEvent) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ReportListItem> result = new ArrayList<ReportListItem>();
		long reportId = 0;
		String idn;
		Date reportDate;
		String formCode;
		if (subjectTypeRecIdList == null || subjectTypeRecIdList.size() == 0) return result;
		if (respRecIdList == null || respRecIdList.size() == 0) return result;
		if (formCodesList == null || formCodesList.size() == 0) return result;

		try {
			connection = getConnection();
			connection.setAutoCommit(false);

			StringBuilder sbSubjectTypeRecIdList = new StringBuilder();
			for (int i = 0; i < subjectTypeRecIdList.size(); i++) {
				if (i > 0)
					sbSubjectTypeRecIdList.append(",");
				sbSubjectTypeRecIdList.append(subjectTypeRecIdList.get(i));
			}

			StringBuilder sbRespRecIdList = new StringBuilder();
			for (int i = 0; i < respRecIdList.size(); i++) {
				if (i > 0)
					sbRespRecIdList.append(",");
				sbRespRecIdList.append(respRecIdList.get(i));
			}

			StringBuilder sbFormCodeList = new StringBuilder();
			for (int i = 0; i < formCodesList.size(); i++) {
				if (i > 0)
					sbFormCodeList.append(",");
				sbFormCodeList.append("'").append(formCodesList.get(i)).append("'");
			}

			ps = connection.prepareStatement(
					"select v_forms.form_id, " +
							"       v_forms.form_code, " +
							"       v_forms.respondent_rec_id, " +
							"       v_forms.respondent_name_ru, " +
							"       v_forms.respondent_short_name_ru, " +
							"       v_forms.subjecttype_name_ru, " +
							"       v_forms.subjecttype_short_name_ru, " +
							"       v_forms.type_code, " +
							"       v_forms.form_name, " +
							"       v_forms.period_code, " +
							"       v_forms.resp_idn as idn, " +
							"       v_forms.REF_PERIOD_REC_ID, " +
							"       v_reports.report_id, " +
							"       v_reports.report_date, " +
							"       v_reports.status_code, " +
							"       v_reports.status_date, " +
							"       v_reports.control_result_code2, " +
							"       v_reports.save_date, " +
							"       v_reports.user_info, " +
							"       v_reports.delivery_way_code, " +
							"       v_reports.have_file, " +
							"       v_reports.have_letter " +
							"  from (select f.id as form_id, " +
							"               f.code as form_code, " +
							"               resp.rec_id as respondent_rec_id, " +
							"               up.name_ru as respondent_name_ru, " +
							"               nvl(up.short_name_ru,up.name_ru) as respondent_short_name_ru, " +
							"               st.name_ru as subjecttype_name_ru, " +
							"               st.short_name_ru as subjecttype_short_name_ru, " +
							"               f.type_code, " +
							"               fh.name as form_name, " +
							"               p.code as period_code, " +
							"               up.idn as resp_idn, " +
							"               s.REF_PERIOD_REC_ID " +
							"          from forms f," +
							"               form_history fh, " +
							"               subjecttype_forms s, " +
							"               F_SESSION_RESP_FORMS sf, " +
							"               rep_per_dur_months p, " +
							"               v_ref_subject_type st, " +
							"               V_REF_UNIONPERSONS up, " +
							"               v_ref_respondent resp " +
							"         where f.code = s.form_code" +
							"			and f.id = fh.form_id " +
							"			and fh.begin_date <= nvl(?,sysdate)" +
							"			and (fh.end_date is null or fh.end_date > nvl(?,sysdate)) " +
							"           and s.period_id = p.id " +
							"           and f.code = sf.FORM_CODE AND sf.REF_RESPONDENT_REC_ID = resp.REC_ID " +
							"           and sf.user_id = ? " +
							"           and st.id = (select max(t.id) " +
							"                          from v_ref_subject_type t " +
							"                         where t.rec_id = s.ref_subject_type_rec_id " +
							"                           and t.begin_date <= nvl(?,sysdate)) " +
							"           and resp.ref_subject_type = st.id " +
							"           and up.id = resp.REF_UNIONPERSONS " +
							"           and up.BEGIN_DATE = (select max(t.BEGIN_DATE) " +
							"                          from V_REF_UNIONPERSONS t " +
							"                         where t.rec_id = up.REC_ID AND t.TYPE = up.TYPE" +
							"                           and t.begin_date <= nvl(?,sysdate)) " +
							"           and pkg_frsi_util.check_period(p.code, ?) = 1 " +
							"           and upper(f.type_code) = 'INPUT' " +
							"           and s.ref_subject_type_rec_id in (" + sbSubjectTypeRecIdList.toString() + ")" +
							"        ) v_forms, " +
							"        (select r.id as report_id, " +
							"                r.idn as rep_idn, " +
							"                r.report_date, " +
							"                r.form_code, " +
							"                st.status_code, " +
							"                st.status_date, " +
							"                st.control_result_code2, " +
							"                st.save_date, " +
							"                st.user_info, " +
							"                st.delivery_way_code, " +
							"                (select count(rf.id) " +
							"                   from attached_file rf, " +
							"                        report_history rh " +
							"                  where rf.link_id = rh.id " +
							"                    and rh.report_id = r.id " +
							"                    and rf.file_kind = 1 " +
							"                    and rh.id = (select max(t.id)  " +
							"                                   from report_history t  " +
							"                                  where t.report_id = r.id) " +
							"                ) as have_file, " +
							"                (select count(rf.id) " +
							"                   from attached_file rf, " +
							"                        report_history rh " +
							"                  where rf.link_id = rh.id " +
							"                    and rh.report_id = r.id " +
							"                    and rf.file_kind = 2 " +
							"                    and rh.id = (select max(t.id)  " +
							"                                   from report_history t  " +
							"                                  where t.report_id = r.id) " +
							"                ) as have_letter " +
							"          from forms f, " +
							"               reports r " +
							"               inner join V_REF_UNIONPERSONS u on u.idn=r.idn and u.begin_date = (select max(t.begin_date) " +
							"                                                                                    from V_REF_UNIONPERSONS t " +
							"                                                                                   where t.rec_id = u.rec_id AND t.TYPE=u.TYPE " +
							"                                                                                     and t.begin_date <= ?) " +
							"                                                                                     and (? < u.end_date or u.end_date is null) " +
							"               inner join v_ref_respondent resp on resp.REF_UNIONPERSONS=u.id " +
							"				  and resp.begin_date = (select max(resp2.begin_date) " +
							"                                          from v_ref_respondent resp2 " +
							"                                         where resp2.rec_id = resp.rec_id " +
							"                                           and resp2.begin_date <= ?) " +
							"                                           and (? < resp.end_date or resp.end_date is null) " +
							"               inner join v_ref_subject_type s on resp.ref_subject_type=s.id and s.begin_date = (select max(t.begin_date) " +
							"                                                                                                from v_ref_subject_type t " +
							"                                                                                               where t.rec_id = s.rec_id " +
							"                                                                                                 and t.begin_date <= ?) " +
							"                                                                                                 and (? < s.end_date or s.end_date is null) " +
							"               inner join v_history_last_status st on st.id = (select max(h2.id)  " +
							"                                                                 from v_history_last_status h2  " +
							"                                                                where h2.report_id=r.id  " +
							"                                                                  and upper(h2.status_code) in ('APPROVED', 'DISAPPROVED', 'COMPLETED')) " +
							"               inner join F_SESSION_RESP_FORMS sf on r.form_code = sf.FORM_CODE and sf.user_id = ? AND sf.REF_RESPONDENT_REC_ID = resp.REC_ID " +
							"         where trunc(r.report_date) between nvl(trunc(?),trunc(r.report_date)) and nvl(trunc(?),trunc(r.report_date)) " +
							"           and f.code = r.form_code " +
							"           and st.status_code in (select substr(ri.name, instr(ri.name, ':')+1) as status_name " +
							"                                    from right_items ri, " +
							"                                         f_session_right_items fri " +
							"                                   where ri.parent = (select id " +
							"                                                       from right_items t " +
							"                                                      where upper(t.name) = 'REP_STAT') " +
							"                                     and ri.id = fri.right_item_id " +
							"                                     and fri.user_id = ? " +
							"                                  ) " +
							"         ) v_reports " +
							"         where v_forms.form_code = v_reports.form_code(+) " +
							"           and v_forms.resp_idn = v_reports.rep_idn(+) " +
							"           and v_forms.form_code in (" + sbFormCodeList.toString() + ")" +
							"         	and v_forms.respondent_rec_id in (" + sbRespRecIdList.toString() + ") " +
							"		 order by v_forms.respondent_name_ru, v_forms.form_name");
			ps.setDate(1, date == null ? null : new java.sql.Date(date.getTime()));
			ps.setDate(2, date == null ? null : new java.sql.Date(date.getTime()));
			ps.setLong(3, userId);
			ps.setDate(4, date == null ? null : new java.sql.Date(date.getTime()));
			ps.setDate(5, date == null ? null : new java.sql.Date(date.getTime()));
			ps.setDate(6, date == null ? null : new java.sql.Date(date.getTime()));
			ps.setDate(7, date == null ? null : new java.sql.Date(date.getTime()));
			ps.setDate(8, date == null ? null : new java.sql.Date(date.getTime()));
			ps.setDate(9, date == null ? null : new java.sql.Date(date.getTime()));
			ps.setDate(10, date == null ? null : new java.sql.Date(date.getTime()));
			ps.setDate(11, date == null ? null : new java.sql.Date(date.getTime()));
			ps.setDate(12, date == null ? null : new java.sql.Date(date.getTime()));
			ps.setLong(13, userId);
			ps.setDate(14, date == null ? null : new java.sql.Date(date.getTime()));
			ps.setDate(15, date == null ? null : new java.sql.Date(date.getTime()));
			ps.setLong(16, userId);
			rs = ps.executeQuery();
			while (rs.next()) {
				reportId = rs.getLong("report_id");

				if (stateSender != null) {
					if (stateSender && reportId == 0)
						continue;
					else if (!stateSender && reportId != 0)
						continue;
				}

				idn = rs.getString("idn");
				reportDate = rs.getDate("report_date");
				formCode = rs.getString("form_code");

				ReportListItem item = new ReportListItem();
				item.setFormCode(formCode);
				item.setFormName(rs.getString("form_name"));
				item.setRespondentNameRu(rs.getString("respondent_name_ru"));
				item.setRespondentShortNameRu(rs.getString("respondent_short_name_ru"));
				item.setSubjectTypeNameRu(rs.getString("subjecttype_name_ru"));
				item.setSubjectTypeShortNameRu(rs.getString("subjecttype_short_name_ru"));
				item.setHaveAttachedFile(rs.getInt("HAVE_FILE") > 0);
				item.setHaveAttachedLetter(rs.getInt("HAVE_LETTER") > 0);
				item.setPeriodCode(rs.getString("PERIOD_CODE"));

				if(reportId != 0) {
					item.setId(reportId);
					item.setReportDate(reportDate);
					item.setSaveDate(rs.getDate("save_date"));
					item.setUserInfo(rs.getString("user_info"));
					item.setDeliveryWay(rs.getString("delivery_way_code"));

					int completeCount = 0;
					for (ReportStatus status : getReportStatusHistoryByReportId(reportId, true, connection)) {
						if (status.getStatusCode().equals(ReportStatus.Status.COMPLETED.toString())) {
							completeCount++;
							if (item.getFirstCompletedDate() == null)
								item.setFirstCompletedDate(status.getStatusDate());
							item.setLastCompletedDate(status.getStatusDate());
						}
					}
					item.setCompleteCount(completeCount);
					item.setStatus(rs.getString("STATUS_CODE"));
					item.setStatusDate(rs.getDate("STATUS_DATE"));
					item.setStatusName(ReportStatus.resMap.get("ru" + "_" + item.getStatus()));
					item.setControlResultCode(rs.getString("CONTROL_RESULT_CODE2"));
					item.setControlResultName(ControlResultType.resMap.get("ru" + "_" + item.getControlResultCode()));
					item.setSubmitReport(true);
					item.setSubmitReportText("Да");
				}else{
					item.setSubmitReport(false);
					item.setSubmitReportText("Нет");
				}

				if (item.getStatus() == null || (!item.getStatus().equals(ReportStatus.Status.APPROVED.name()) && !item.getStatus().equals(ReportStatus.Status.COMPLETED.name()))) {
					Long refPeriodRecId = rs.getLong("REF_PERIOD_REC_ID");
					if (!rs.wasNull()) {
						Date rd;
						if (reportDate == null) {
							rd = PeriodUtil.floor(LocalDate.now(), PeriodType.valueOf(item.getPeriodCode().toUpperCase())).toDate();
						} else {
							rd = reportDate;
						}
						PeriodAlgResult algResult = executePeriodAlg(refPeriodRecId, formCode, rd, idn);
						if (algResult != null) {
							if (algResult.hasError()) {
								item.setPeriodAlgError(algResult.getErrorMessage());
							} else if (algResult.getResult() < 0) {
								item.setOverdueDays(Math.abs(algResult.getResult()));
							}
						}
					}
				}

				result.add(item);
			}

			insertAuditEvent(auditEvent, connection);
			connection.commit();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public List<Template> getTemplateList(Long typeTemplate) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Template> result = new ArrayList<Template>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT id,code,name,code_template,xls_out, begin_date, end_date FROM project_template where (? is null or type_template = ?)");
			if(typeTemplate != null && typeTemplate != 0) {
				ps.setLong(1, typeTemplate);
				ps.setLong(2, typeTemplate);
			} else {
				ps.setNull(1, OracleTypes.NULL);
				ps.setNull(2, OracleTypes.NULL);
			}
			rs = ps.executeQuery();
			while (rs.next()) {
				Template item = new Template();
				item.setId(rs.getLong("id"));
				item.setCode(rs.getString("code"));
				item.setName(rs.getString("name"));
				item.setCodeTemplate(rs.getString("code_template"));
				item.setBeginDate(rs.getDate("begin_date"));
				item.setEndDate(rs.getDate("end_date"));
				Blob blobXlsOut = rs.getBlob("xls_out");
				if (blobXlsOut != null) {
					item.setHaveTemplate(true);
//					item.setXlsOut(blobXlsOut.getBytes(1, (int) blobXlsOut.length()));
					blobXlsOut.free();
				}
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
	public void updateTemplateXlsOut(Template template) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("UPDATE project_template SET xls_out = ? WHERE id = ?");

			Blob blobXls = connection.createBlob();
			blobXls.setBytes(1, template.getXlsOut());
			ps.setBlob(1, blobXls);

			ps.setLong(2, template.getId());
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public List<Error> validateKvMap(Map<String, String> kvMap, Long id, Date reportDate) {
		List<Error> errors = new ArrayList<Error>();

		/*String keyCeo, keyChiefAccountant, keyDraftedBy, keySignatureDate, keyDraftedByTel;
		keyCeo = keyChiefAccountant = keyDraftedBy = keySignatureDate = keyDraftedByTel = null;*/

		FormHistory formHistory = getFormHistoryWithInputValueChecks(id);
		List<InputValueCheck> inputValueChecks = new ArrayList<InputValueCheck>();
		Gson gson = new Gson();
		Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {}.getType();
		inputValueChecks = gson.fromJson(formHistory.getInputValueChecks(), typeListInputValueCheck);
		String key;
		int n;
		Set<String> groupIds = new HashSet<String>();
		Map<String,String> uniqueValueMap = new HashMap<String, String>();
		for (InputValueCheck item : inputValueChecks) {
			if (item.getAuto() == null || !item.getAuto().booleanValue()) {
				String groupId = item.getGroupId();
				if (groupId != null) {
					/*String groupKey = item.getKey().substring(0, item.getKey().indexOf("$DynamicRowId")) + "$D." + item.getGroupId() + ".";
					item.setGroupId(groupKey);*/
					groupIds.add(item.getGroupId());
				}
			}
		}
		for (InputValueCheck item: inputValueChecks) {
			if(item.getAuto()!=null && item.getAuto().booleanValue())
				continue;
			if (item.getKey() != null) {
				n = item.getKey().indexOf("$DynamicRowId");
				if (n >= 0)
					key = item.getKey().substring(0, n + 2);
				else
					key = item.getKey();

				List<ValueCheckFuncParser.Function> functions = new ArrayList<ValueCheckFuncParser.Function>();
				if (item.getValueCheckFunc() != null && !item.getValueCheckFunc().trim().isEmpty()) {
					try {
						ValueCheckFuncParser parser = new ValueCheckFuncParser();
						functions.addAll(parser.parse(item.getValueCheckFunc()));
					} catch (FormulaSyntaxError e) {
						errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, "", e.getMessage(), e.getMessage(), e.getMessage()));
						continue;
					}
					if (item.getKey() == null || item.getKey().isEmpty()) {
						for (ValueCheckFuncParser.Function function : functions) {
							if(function.getName().equals("contains")){
								errors.addAll(valuecheckFuncContains(item, kvMap, function));
							}
						}
						continue;
					}
				}

				for (Map.Entry<String, String> entry : kvMap.entrySet()) {

					if (entry.getKey() == null) {
						continue;
					}

					boolean isDynamicRow = entry.getKey().contains("$D.");
					boolean hasGroup = item.getGroupId() != null;

					if (!isDynamicRow && !entry.getKey().equalsIgnoreCase(key)) {
						continue;
					}

					if (isDynamicRow) {
						if (hasGroup) {
							boolean belongsToGroup = entry.getKey().toLowerCase().startsWith(item.getGroupId().toLowerCase());
							if (!belongsToGroup) {
								continue;
							}
						} else {
							boolean sameColumnAndRowGroup = entry.getKey().contains(key);
							boolean belongsToGroupChecks = groupIds.contains(entry.getKey().substring(0, entry.getKey().lastIndexOf(".") + 1));
							if (!sameColumnAndRowGroup || belongsToGroupChecks) {
								continue;
							}
						}
					}

					/*if (entry.getKey() != null && entry.getKey().contains(key)
							&& ((item.getGroupId() == null && (!entry.getKey().contains("$D.") || !groupIds.contains(entry.getKey().substring(0, entry.getKey().lastIndexOf(".") + 1))))
								|| (item.getGroupId() != null && entry.getKey().startsWith(item.getGroupId())))) {*/
					if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
						if (item.getRequired())
							errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Поле является обязательным для заполнения"));
						else if (item.getValueCheckFunc() != null && !item.getValueCheckFunc().trim().isEmpty()) {
							for (ValueCheckFuncParser.Function function : functions) {
								if (function.getName().equals("RIsAColumnNZ")) {
									errors.addAll(valuecheckFuncRequiredIsAnotherColumnNoZero(entry, kvMap, function));
								}
							}
						}
							/*if (n == -1)
								break;*/
					} else {
						if (item.getRef() != null && !item.getRef().isEmpty()) {
							if (item.getMultiValue() != null && item.getMultiValue()) {
								try {
									MultiSelectValue multiSelectValue = gson.fromJson(entry.getValue(), MultiSelectValue.class);
									if (multiSelectValue.getValues().contains("-1"))
										errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Выберите данные из справочника"));
								} catch (JsonSyntaxException e) {
									errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Неправильные данные для многозначного справочника"));
								}
							} else if (entry.getValue().equals("-1")) {
								errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Выберите данные из справочника"));
							}
							if (item.getFilterField() != null && !item.getFilterField().isEmpty()) {
								Variant val = getRefItemValueByRecId(item.getRef(),item.getFilterField(),Long.valueOf(entry.getValue()),reportDate,ValueType.NUMBER_0);
								if (val == null) {
									errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен",
											MessageFormat.format("Не найдена запись в справочнике {0} с значением {1}", item.getRef(), entry.getValue())));
								} else {
									if (item.getFilterValue() != null) {
										Variant filterValue = DataType.stringToVariant(item.getFilterValue(), DataType.toValueType(item.getFilterDataType()));
										if(!filterValue.equal(val)){
											errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен",
													MessageFormat.format("Не найден во фильтрованном пикере", item.getFilterValue())));
										}
									} else {
										String parentKey = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
										String rowId = parentKey.substring(parentKey.indexOf("$D."));
										if (rowId.lastIndexOf(".") == rowId.indexOf(".")) {
											parentKey = parentKey.substring(0, parentKey.indexOf("$D.")) + rowId.substring(rowId.indexOf(".") + 1);
										}
										String parentValue = kvMap.get(parentKey);
										if (parentValue == null) {
											errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен",
													MessageFormat.format("Значение не установлено для родительской записи {0}", parentKey)));
										} else {
											Variant filterValue = DataType.stringToVariant(parentValue, DataType.toValueType(item.getFilterDataType()));
											if (!filterValue.equal(val)) {
												errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен",
														MessageFormat.format("Не относится к родительской группе", val.getLngValue(), filterValue)));
											}
										}
									}
								}
							}
								/*if (n == -1)
									break;*/
						} else if (item.getValueType() != null && !item.getValueType().isEmpty()) {
							if (item.getValueType().equals("int") && !Validators.IsValidLong(entry.getValue()))
								errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Введено неверное целое число"));
							else if (item.getValueType().equals("float") && !Validators.IsValidDouble(entry.getValue()))
								errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Введено неверное дробное число"));
							else if (item.getValueType().equals("date") && !Validators.IsValidDate(entry.getValue()))
								errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Введена неверная дата"));
							else if (item.getValueType().equals("time") && !Validators.IsValidTime(entry.getValue()))
								errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Введено неверное время"));
								/*if (n == -1)
									break;*/
						}
						if (item.getValueCheckFunc() != null && !item.getValueCheckFunc().trim().isEmpty()) {
							for (ValueCheckFuncParser.Function function : functions) {
								if (function.getName().equals("validateIDN") && !entry.getValue().equals("несовершеннолетний") && !entry.getValue().equals("нерезидент РК")) {
									if (!Validators.validateIDN(entry.getValue()))
										errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Введен некорректный ИИН/БИН"));
								} else if (function.getName().equals("IsPositiveNumber")) {
									String sNumber = entry.getValue().replace(" ", "").replace(",", ".");
									if (item.getValueType().equals("int") && !Validators.IsPositiveNumber(Long.parseLong(sNumber)))
										errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Значение должно быть положительным"));
									else if (item.getValueType().equals("float") && !Validators.IsPositiveNumber(Float.parseFloat(sNumber)))
										errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Значение должно быть положительным"));
								} else if (function.getName().equals("IsNegativeNumber")) {
									String sNumber = entry.getValue().replace(" ", "").replace(",", ".");
									if (item.getValueType().equals("int") && !Validators.IsNegativeNumber(Long.parseLong(sNumber)))
										errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Значение должно быть отрицательным"));
									else if (item.getValueType().equals("float") && !Validators.IsNegativeNumber(Float.parseFloat(sNumber)))
										errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Значение должно быть отрицательным"));
								} else if (function.getName().equals("IsNotEqualToZero")) {
									String sNumber = entry.getValue().replace(" ", "").replace(",", ".");
									if (item.getValueType().equals("int") && !Validators.IsNotEqualToZero(Long.parseLong(sNumber)))
										errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Значение должно быть не равным нулю"));
									else if (item.getValueType().equals("float") && !Validators.IsNotEqualToZero(Float.parseFloat(sNumber)))
										errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Значение должно быть не равным нулю"));
								} else if (function.getName().equals("IsPercent")) {
									String sNumber = entry.getValue().replace(" ", "").replace(",", ".");
									if (item.getValueType().equals("int") && !Validators.IsPercent(Long.parseLong(sNumber)))
										errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Значение должно быть в интервале между 0 и 100 процентов"));
									else if (item.getValueType().equals("float") && !Validators.IsPercent(Float.parseFloat(sNumber)))
										errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Значение должно быть в интервале между 0 и 100 процентов"));
								} else if (function.getName().equals("RIsAColumnNZ")) {
									errors.addAll(valuecheckFuncRequiredIsAnotherColumnNoZero(entry, kvMap, function));
								} else if (function.getName().equals("RIsAColumnLE")) {
									errors.addAll(valuecheckFuncRequiredIsAnotherColumnLE(entry, kvMap, function));
								}
							}
						}
						if (item.getUnique() != null && item.getUnique().booleanValue() && item.getUniqueArea() != null && !item.getUniqueArea().isEmpty() &&
								!entry.getValue().equals("несовершеннолетний") && !entry.getValue().equals("нерезидент РК")) {

							for (Map.Entry<String, String> entry_in : kvMap.entrySet()) {
								if (item.getUniqueArea().equalsIgnoreCase("column")) {
									if (entry_in.getKey() != null && entry_in.getKey().contains(key)
											&& ((item.getGroupId() == null && (!entry_in.getKey().contains("$D.") || !groupIds.contains(entry_in.getKey().substring(0, entry_in.getKey().lastIndexOf(".") + 1)))) || (item.getGroupId() != null && entry_in.getKey().startsWith(item.getGroupId())))) {
										if (entry_in.getValue() != null && !entry_in.getValue().trim().isEmpty()) {
											if (entry.getValue().equals(entry_in.getValue()) && !entry.getKey().equals(entry_in.getKey())) {
												uniqueValueMap.put(entry.getKey(), entry.getValue());
											}
										}
									}
								} else if (item.getUniqueArea().equalsIgnoreCase("table")) {
									if(entry.getKey().substring(0,entry.getKey().indexOf("*")).equals(entry_in.getKey().substring(0,entry_in.getKey().indexOf("*")))){
										if (entry_in.getValue() != null && !entry_in.getValue().trim().isEmpty()) {
											if (entry.getValue().equals(entry_in.getValue()) && !entry.getKey().equals(entry_in.getKey())) {
												uniqueValueMap.put(entry.getKey(), entry.getValue());
											}
										}
									}

								} else if (item.getUniqueArea().equalsIgnoreCase("form")) {
									if (entry_in.getValue() != null && !entry_in.getValue().trim().isEmpty()) {
										if (entry.getValue().equals(entry_in.getValue()) && !entry.getKey().equals(entry_in.getKey())) {
											uniqueValueMap.put(entry.getKey(), entry.getValue());
										}
									}
								}
							}
						}
					}
//					}

				}

			}
		}

		for (Map.Entry<String, String> duplicate : uniqueValueMap.entrySet()) {
			if(duplicate.getKey() != null) {
				errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, duplicate.getKey(), "broken uniqueness", "нарушена уникальность",
						"Значение \"" + duplicate.getValue() + "\" должно быть уникальным"));
			}
		}

		for (Map.Entry<String,String> entry : kvMap.entrySet()) {

			// Error error = null;

			if (entry.getValue() != null && entry.getValue().equals("не найден в справочнике"))
				errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, entry.getKey(), "no found", "не определен", "Выберите данные из справочника"));
			/*else if (entry.getKey().contains("*ceo::")) keyCeo = entry.getKey();
			else if (entry.getKey().contains("*chief_accountant::")) keyChiefAccountant = entry.getKey();
			else if (entry.getKey().contains("*drafted_by::")) keyDraftedBy = entry.getKey();
			else if (entry.getKey().contains("*signature_date::")) keySignatureDate = entry.getKey();
			else if (entry.getKey().contains("*drafted_by_tel::")) keyDraftedByTel = entry.getKey();*/

			// Required fields
			// Reference values

			// if (error != null) errors.add(error);
		}

		// Signatures
		/*if (keyCeo == null || kvMap.get(keyCeo).trim().isEmpty())
			errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, keyCeo, "Field \"CEO\" is empty", "\"Бірінші басшы\" жолағы толтырылмаған", "Не заполнено поле \"Первый руководитель\""));
		if (keyChiefAccountant == null || kvMap.get(keyChiefAccountant).trim().isEmpty())
			errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, keyChiefAccountant, "Field \"Chief accountant\" is empty", "\"Бас есепші\" жолағы толтырылмаған", "Не заполнено поле \"Главный бухгалтер\""));
		if (keyDraftedBy == null || kvMap.get(keyDraftedBy).trim().isEmpty())
			errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, keyDraftedBy, "Field \"Drafted by\" is empty", "\"Орындаушы\" жолағы толтырылмаған", "Не заполнено поле \"Исполнитель\""));
		if (keySignatureDate == null || kvMap.get(keySignatureDate).trim().isEmpty()) {
			if (keyDraftedByTel == null || !kvMap.containsKey(keyDraftedByTel)) // если новый шаблон
				errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, keySignatureDate, "Field \"Signature date\" is empty", "\"Қол қойылған күні\" жолағы толтырылмаған", "Не заполнено поле \"Дата подписания\""));
		}*/

		Collections.sort(errors, new Comparator<Error>() {
			@Override
			public int compare(Error o1, Error o2) {
				if (o1.getLevel() != o2.getLevel()) {
					if (o1.getLevel() == Error.Level.ERROR) {
						return -1;
					} else if (o2.getLevel() == Error.Level.ERROR) {
						return 1;
					} else {
						return 0;
					}
				}
				return 0;
			}
		});

		return errors;
	}

	@Override
	public List<Error> validateRefValues(Long reportId, Date reportDate){
		List<Error> errorList = new ArrayList<Error>();
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			Long reportHistoryId = getLastReportHistoryByReportId(reportId, false, false, false, connection).getId();

			ps = connection.prepareStatement(
					"select *\n" +
					"  from report_ref_link\n" +
					" where report_history = ?" +
					"   and ref_code in ('ref_legal_person', 'ref_person')" +
					"   and delfl = 0");
			ps.setLong(1, reportHistoryId);
			rs = ps.executeQuery();
			while (rs.next()) {
				String refCode = rs.getString("REF_CODE");
				Long recId = rs.getLong("REC_ID");
				if(getUserUnConfirmCount(refCode, recId, connection) > 0){
					String msg = "В справочнике " + refCode + ", не подтверждена запись, где rec_id = " + recId;
					errorList.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, "", msg, msg, msg));
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return errorList;
	}

	private int getUserUnConfirmCount(String refCode, Long recId, Connection connection){
		CallableStatement stmt = null;
		int result;
		try {
			stmt = connection.prepareCall("BEGIN ? := pkg_frsi_ref.check_user_confirm(?,?); end;");
			stmt.registerOutParameter(1, OracleTypes.INTEGER);
			stmt.setString(2, refCode);
			stmt.setLong(3, recId);
			stmt.execute();
			result = stmt.getInt(1);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(null, stmt);
		}
		return result;
	}

	@Override
	public Map<String, String> executeValueFunctions(Map<String, String> kvMap, String formCode, Date reportDate) {
		List<Form> forms = getFormsByCodeLanguageReportDate(formCode, "ru", reportDate, null);
		if (forms.size() == 0) {
			return kvMap;
		}

		// TODO здесь кажется можно использовать formHistory из forms
		FormHistory formHistory = getFormHistoryWithInputValueChecks(forms.get(0).getFormHistory().getId());
		Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {}.getType();
		List<InputValueCheck> inputValueChecks = gson.fromJson(formHistory.getInputValueChecks(), typeListInputValueCheck);

		Set<String> groupIds = new HashSet<String>();
		for (InputValueCheck item : inputValueChecks) {
			if (item.getAuto() == null || !item.getAuto().booleanValue()) {
				String groupId = item.getGroupId();
				if (groupId != null) {
					groupIds.add(item.getGroupId());
				}
			}
		}

		Map<String, String> resultMap = new HashMap<String, String>(kvMap);

		for (InputValueCheck inputValueCheck : inputValueChecks) {

			if(inputValueCheck.getKey()==null || inputValueCheck.getKey().isEmpty()){
				continue;
			}

			if (inputValueCheck.getValueFunc() == null ||
					inputValueCheck.getValueFunc().equalsIgnoreCase("CurrentDate") ||
					inputValueCheck.getValueFunc().equalsIgnoreCase("LastSignDate") ||
					inputValueCheck.getValueFunc().equalsIgnoreCase("CeoSignDate") ||
					inputValueCheck.getValueFunc().equalsIgnoreCase("ChiefSignDate") ||
					inputValueCheck.getValueFunc().equalsIgnoreCase("FirstSignDate")) {
				continue;
			}

			String key;
			int n = inputValueCheck.getKey().indexOf("$DynamicRowId");
			if (n >= 0)
				key = inputValueCheck.getKey().substring(0, n)+"$D.";
			else
				key = inputValueCheck.getKey();

			List<ValueCheckFuncParser.Function> functions;
			try {
				ValueCheckFuncParser parser = new ValueCheckFuncParser();
				functions = parser.parse(inputValueCheck.getValueFunc());
			} catch (FormulaSyntaxError e) {
				throw new EJBException(e.getMessage());
			}

			for (Map.Entry<String, String> entry : kvMap.entrySet()) {

				if (entry.getKey() == null) {
					continue;
				}

				boolean isDynamicRow = entry.getKey().contains("$D.");
				boolean hasGroup = inputValueCheck.getGroupId() != null;

				if (!isDynamicRow && !entry.getKey().equalsIgnoreCase(key)) {
					continue;
				}

				if (isDynamicRow) {
					if (hasGroup) {
						boolean belongsToGroup = entry.getKey().toLowerCase().startsWith(inputValueCheck.getGroupId().toLowerCase());
						if (!belongsToGroup) {
							continue;
						}
					} else {
						boolean sameColumnAndRowGroup = entry.getKey().contains(key);
						boolean belongsToGroupChecks = groupIds.contains(entry.getKey().substring(0, entry.getKey().lastIndexOf(".") + 1));
						if (!sameColumnAndRowGroup || belongsToGroupChecks) {
							continue;
						}
					}
				}

				for (ValueCheckFuncParser.Function function : functions) {
					if (function.getName().equalsIgnoreCase("copyValue")) {
						String targetColumn = function.getParams()[0];
						if (targetColumn.startsWith("'") && targetColumn.endsWith("'")) {
							targetColumn = targetColumn.substring(1, targetColumn.length() - 1);
						}
						String targetKey = key.substring(0, key.indexOf("*") + 1) + targetColumn + key.substring(key.indexOf(":"));
						if (isDynamicRow) {
							targetKey += entry.getKey().substring(entry.getKey().indexOf("$D.") + 3);
						}
						resultMap.put(targetKey, entry.getValue());
					}
				}

			}
		}


		return resultMap;
	}

	private List<Error> valuecheckFuncContains(InputValueCheck item, Map<String, String> kvMap, ValueCheckFuncParser.Function function) {
		List<Error> errors = new ArrayList<Error>();

		ContainsFuncItem containsFuncItem = new ContainsFuncItem();
		try {
			if (function.getParams().length != 3) {
				throw new FormulaSyntaxError("Неверные аргументы функции contains");
			}
			String param1 = function.getParams()[0];
			String param2 = function.getParams()[1];

			if (param1.isEmpty() || param2.isEmpty()) {
				throw new FormulaSyntaxError("Неверные аргументы функции contains");
			}

			if (param1.charAt(0) != '[' || param1.charAt(param1.length() - 1) != ']'
					|| param2.charAt(0) != '[' || param2.charAt(param2.length() - 1) != ']') {
				throw new FormulaSyntaxError("Неверные аргументы функции contains");
			}

			param1 = param1.substring(1, param1.length() - 1);
			param2 = param2.substring(1, param2.length() - 1);

			containsFuncItem.setSourceColumns(param1.split(","));
			containsFuncItem.setTargetColumns(param2.split(","));
			if (containsFuncItem.getSourceColumns() == null
					|| containsFuncItem.getTargetColumns() == null
					|| containsFuncItem.getSourceColumns().length != containsFuncItem.getTargetColumns().length) {
				throw new FormulaSyntaxError("Неверные аргументы функции contains");
			}

		} catch (FormulaSyntaxError e) {
			errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, "", e.getMessage(), e.getMessage(), e.getMessage()));
			return errors;
		}

		for (Map.Entry<String, String> entry : kvMap.entrySet()) {
			if (!entry.getKey().contains(":")) {
				continue;
			}
			String rowId = entry.getKey().substring(entry.getKey().lastIndexOf(":") + 1);
			boolean contains = false;
			for (String sourceKey : containsFuncItem.getSourceColumns()) {
				if (entry.getKey().contains(sourceKey)) {
					containsFuncItem.addSourceValue(rowId, sourceKey, entry.getKey(), entry.getValue());
					contains = true;
					break;
				}
			}
			if (!contains) {
				for (String targetKey : containsFuncItem.getTargetColumns()) {
					if (entry.getKey().contains(targetKey)) {
						containsFuncItem.addTargetValue(rowId, targetKey, entry.getKey(), entry.getValue());
						break;
					}
				}
			}
		}

		for (Map.Entry<String, Map<String, KeyValue>> sourceEntry : containsFuncItem.getSourceRows().entrySet()) {
			Map<String, KeyValue> sourceRow = sourceEntry.getValue();

			boolean rowFound = false;

			for (Map.Entry<String, Map<String, KeyValue>> targetEntry : containsFuncItem.getTargetRows().entrySet()) {
				Map<String, KeyValue> targetRow = targetEntry.getValue();

				Map<String, Boolean> searchResult = new HashMap<String, Boolean>();

				for (int i = 0; i < containsFuncItem.getSourceColumns().length; i++) {
					String sourceColumn = containsFuncItem.getSourceColumns()[i];
					String targetColumn = containsFuncItem.getTargetColumns()[i];
					KeyValue sourceKeyValue = sourceRow.get(sourceColumn);
					KeyValue targetKeyValue = targetRow.get(targetColumn);

					String sourceValue = sourceKeyValue == null ? null : sourceKeyValue.getValue();
					String targetValue = targetKeyValue == null ? null : targetKeyValue.getValue();

					boolean matches;
					if (sourceValue == null && targetValue == null) {
						matches = true;
					} else if (sourceValue == null || targetValue == null) {
						matches = false;
					} else {
						matches = sourceValue.equals(targetValue);
					}

					searchResult.put(sourceColumn, matches);
				}

				rowFound = !searchResult.values().contains(false);

				if (rowFound) {
					break;
				}

			}

			if (!rowFound) {
				String searchKey = "";
				String searchValues = "";
				for (String col : containsFuncItem.getSourceColumns()) {
					if (sourceRow.keySet().contains(col)) {
						KeyValue keyValue = sourceRow.get(col);
						searchKey = col + keyValue.getKey().substring(keyValue.getKey().indexOf(":"));
						if (keyValue.getValue() != null) {
							if (!searchValues.isEmpty()) searchValues += ", ";
							searchValues += keyValue.getValue();
						}
					}
				}
				String rowId = sourceEntry.getKey();
				if (rowId.startsWith("$D.group.")) {
					rowId = rowId.substring("$D.group.".length());
				} else if (rowId.startsWith("$D.")) {
					rowId = rowId.substring("$D.".length());
				}
				String notFoundMessage = MessageFormat.format("Запись {0} не найдена {1}", rowId + " (" + searchValues + ")", function.getParams()[2]);
				Error.Level errorLevel = function.getErrorLevel() != null ? function.getErrorLevel() : Error.Level.ERROR;
				errors.add(new Error(errorLevel, Error.Stage.WEB_FORM_VALIDATION, searchKey, notFoundMessage, notFoundMessage, notFoundMessage));
			}

		}

		return errors;
	}

	private List<Error> valuecheckFuncRequiredIsAnotherColumnNoZero(Map.Entry<String, String> entry, Map<String, String> kvMap, ValueCheckFuncParser.Function function) {
		// RequiredIsAnotherColumnNoZero short RIsAColumnNZ
		List<Error> errors = new ArrayList<Error>();

		String param1 = null;
		String param2 = null;

		try {
			if (function.getParams().length != 2) {
				throw new FormulaSyntaxError("Неверные аргументы функции RIsAColumnNZ");
			}
			param1 = function.getParams()[0];
			param2 = function.getParams()[1].toLowerCase();

			if (param1.isEmpty() || param2.isEmpty()) {
				throw new FormulaSyntaxError("Неверные аргументы функции RIsAColumnNZ");
			}

			if (param1.charAt(0) != '[' || param1.charAt(param1.length() - 1) != ']') {
				throw new FormulaSyntaxError("Неверные аргументы функции RIsAColumnNZ");
			}
			if (!param2.equals("n") && !param2.equals("s")) {
				throw new FormulaSyntaxError("Неверные аргументы функции RIsAColumnNZ");
			}

			param1 = param1.substring(1, param1.length() - 1);

		} catch (FormulaSyntaxError e) {
			errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, "", e.getMessage(), e.getMessage(), e.getMessage()));
			return errors;
		}

		String key1 = entry.getKey();
		String key2 = param1 + entry.getKey().substring(entry.getKey().indexOf(":"));
		String sValue1 = null;
		Double nValue1 = null;
		Double nValue2;
		try {
			String sNumber;
			if (param2.equals("n")) {
				sNumber = entry.getValue().replace(" ", "").replace(",", ".");
				nValue1 = Double.parseDouble(sNumber);
			}
			else
				sValue1 = entry.getValue();

			sNumber = kvMap.get(key2).replace(" ", "").replace(",", ".");
			nValue2 = Double.parseDouble(sNumber);
		} catch (NumberFormatException e) {
			errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, "", e.getMessage(), e.getMessage(), e.getMessage()));
			return errors;
		}

		if (param2.equals("n") && nValue1 == null) {
			errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, key1, "no found", "не определен", "Значение не должно быть пустым"));
			return errors;
		}

		if (nValue2 == null) {
			errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, key2, "no found", "не определен", "Значение не должно быть пустым"));
			return errors;
		}

		if (param2.equals("n") && nValue1 == 0 && nValue2 != 0) {
			String rowId = key1.substring(key1.lastIndexOf(":") + 1);
			if (key1.lastIndexOf("$D.") > -1)
				rowId = rowId.substring(3);
			errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, key1, "no found", "не определен", MessageFormat.format("Значение должно быть не равным нулю, строка {0}", rowId)));
			return errors;
		}
		else if (param2.equals("s") && (sValue1 == null || sValue1.trim().isEmpty()) && nValue2 != 0) {
			String rowId = key1.substring(key1.lastIndexOf(":") + 1);
			if (key1.lastIndexOf("$D.") > -1)
				rowId = rowId.substring(3);
			errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, key1, "no found", "не определен", MessageFormat.format("Значение не должно быть пустым, строка {0}", rowId)));
			return errors;
		}

		return errors;
	}

	private List<Error> valuecheckFuncRequiredIsAnotherColumnLE(Map.Entry<String, String> entry, Map<String, String> kvMap, ValueCheckFuncParser.Function function) {
		// RequiredIsAnotherColumnLE short RIsAColumnLE
		List<Error> errors = new ArrayList<Error>();

		String param1 = null;

		try {
			if (function.getParams().length != 1) {
				throw new FormulaSyntaxError("Неверные аргументы функции RIsAColumnLE");
			}
			param1 = function.getParams()[0];

			if (param1.isEmpty()) {
				throw new FormulaSyntaxError("Неверные аргументы функции RIsAColumnLE");
			}

			if (param1.charAt(0) != '[' || param1.charAt(param1.length() - 1) != ']') {
				throw new FormulaSyntaxError("Неверные аргументы функции RIsAColumnLE");
			}

			param1 = param1.substring(1, param1.length() - 1);

		} catch (FormulaSyntaxError e) {
			errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, "", e.getMessage(), e.getMessage(), e.getMessage()));
			return errors;
		}

		String key1 = entry.getKey();
		String key2 = param1 + entry.getKey().substring(entry.getKey().indexOf(":"));
		Double nValue1 = null;
		Double nValue2;
		try {
			String sNumber;
			sNumber = entry.getValue().replace(" ", "").replace(",", ".");
			nValue1 = Double.parseDouble(sNumber);

			sNumber = kvMap.get(key2).replace(" ", "").replace(",", ".");
			nValue2 = Double.parseDouble(sNumber);
		} catch (NumberFormatException e) {
			errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, "", e.getMessage(), e.getMessage(), e.getMessage()));
			return errors;
		}

		if (nValue1 == null) {
			errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, key1, "no found", "не определен", "Значение не должно быть пустым"));
			return errors;
		}

		if (nValue2 == null) {
			errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, key2, "no found", "не определен", "Значение не должно быть пустым"));
			return errors;
		}

		if (nValue1 < nValue2) {
			String rowId = key1.substring(key1.lastIndexOf(":") + 1);
			if (key1.lastIndexOf("$D.") > -1)
				rowId = rowId.substring(3);
			errors.add(new Error(Error.Level.ERROR, Error.Stage.WEB_FORM_VALIDATION, key1, "no found", "не определен", MessageFormat.format("Значение \"{0}\" должно быть больше или равно значения \"{1}\", строка {2}", nValue1, nValue2, rowId)));
			return errors;
		}

		return errors;
	}

	@Override
	public void updateReportHistoryComment(Long reportHistoryId, String currentHistoryComment) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("UPDATE report_history SET su_comments=? WHERE id = ?");

			ps.setString(1, currentHistoryComment);

			ps.setLong(2, reportHistoryId);

			int affectedRows = ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public List<String> getAllOracleUsers() {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<String> result = new ArrayList<String>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement(
					"select t.USERNAME" +
							"  from SYS.ALL_USERS t" +
							" where t.COMMON = 'NO'");
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getString("USERNAME"));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public boolean checkUniqueDesignUserName(long id, String designUserName) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean result = false;
		try {
			connection = getConnection();
			ps = connection.prepareStatement(
					"select t.id" +
							"  from f_users t" +
							" where t.DESIGN_USER_NAME = ?" +
							"   and t.blockfl = 0" +
							"	and t.id != ?");
			ps.setString(1, designUserName);
			ps.setLong(2, id);
			rs = ps.executeQuery();
			if(rs.next()) result = true;
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public List<OutReportRuleItem> getOutReportRuleListByFormCodeDate(String formCode, Date reportDate) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<OutReportRuleItem> result = new ArrayList<OutReportRuleItem>();
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT\n" +
					"  rr.ID,\n" +
					"  f.CODE FORM_CODE,\n" +
					"  rr.TABLE_NAME,\n" +
					"  rr.FIELD_NAME,\n" +
					"  rr.FORMULA,\n" +
					"  rr.FORMULA2,\n" +
					"  rr.PRIORITY,\n" +
					"  rr.KEYVALUE,\n" +
					"  rr.DATA_TYPE,\n" +
					"  rr.GROUPING,\n" +
					"  rr.keyvalue,\n" +
					"  rr.table_name\n" +
					"FROM outreport_rules rr\n" +
					"  INNER JOIN (FORM_HISTORY h INNER JOIN FORMS f ON h.FORM_ID = f.ID\n" +
					"                                                   AND h.begin_date <= ?\n" +
					"                                                   AND (? < h.end_date OR h.end_date IS NULL))\n" +
					"    ON rr.FORM_HISTORY_ID = h.ID\n" +
					"WHERE f.code = ?");
			ps.setDate(1, new java.sql.Date(reportDate.getTime()));
			ps.setDate(2, new java.sql.Date(reportDate.getTime()));
			ps.setString(3, formCode);
			rs = ps.executeQuery();
			while (rs.next()) {
				OutReportRuleItem item = new OutReportRuleItem();
				item.setId(rs.getLong("ID"));
				item.setFormCode(rs.getString("FORM_CODE"));
				item.setFieldName(rs.getString("FIELD_NAME"));
				String formula = rs.getString("formula");
				String formula2 = rs.getString("formula2");
				if (rs.wasNull()) {
					formula2 = "";
				}
				item.setFormula(formula + formula2);
				item.setPriority(rs.getInt("PRIORITY"));
				item.setKeyValue(rs.getString("KEYVALUE"));
				item.setTableName(rs.getString("TABLE_NAME"));
				item.setGrouping(rs.getInt("GROUPING") == 1);
				item.setDataType(rs.getString("DATA_TYPE"));
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
	public void insertOutReportRuleItem(OutReportRuleItem item, long formHistoryId) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("INSERT INTO TMP_G_OUTREPORT_RULES (FORM_HISTORY_ID, TABLE_NAME, FIELD_NAME, FORMULA, PRIORITY, KEYVALUE, DATA_TYPE, GROUPING)\n" +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

			ps.setLong(1, formHistoryId);
			ps.setString(2, item.getTableName());
			ps.setString(3, item.getFieldName());
			ps.setString(4, item.getFormula());
			ps.setInt(5, item.getPriority());
			ps.setString(6, item.getKeyValue());
			ps.setString(7, item.getDataType());
			ps.setInt(8, item.isGrouping() ? 1 : 0);

			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void clearOutReportRuleItems(long formHistoryId) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("delete from TMP_G_OUTREPORT_RULES where form_history_id=?");
			ps.setLong(1, formHistoryId);

			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps);
		}
	}

	@Override
	public void makeConsolidatedReport(String formCode, Date reportDate) {
		ConsFormExpressionMaker maker = new ConsFormExpressionMaker(this, formCode, reportDate);
		maker.make();
	}

	@Override
	public DataSet execQuery(String query, QueryType qt) {
		Connection connection = null;
		try {
			connection = getConnection();
			sqlExecutor.setQueryType(qt);
			sqlExecutor.runQuery(query, connection);
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection);
		}
		return sqlExecutor.getDataSet();
	}

	@Override
	public int copyReportHistory() {
		List<ReportHistory> histories = getAllReportHistories();
		insertReportHistoriesTmp(histories);
		return histories.size();
	}

	@Override
	public Variant getExternalSystemIndicatorValue(String code, String formCode, Date reportDate, String idn, Param[] params) throws SQLException {
		Connection connection = null;
		Variant resultValue = null;

		RefExtIndicatorItem filter = new RefExtIndicatorItem();
		filter.setCode(code);
		List<RefExtIndicatorItem> indList = reference.getExtIndicatorItemsByFilters(reportDate, filter);
		if (indList.size() == 0)
			return null;

		String alg = indList.get(0).getAlgorithm();
		List<Param> paramList = new ArrayList<Param>(Arrays.asList(params));
		paramList.add(new Param("reportDate", Variant.createDate(reportDate)));
		paramList.add(new Param("idn", Variant.createString(idn)));
		paramList.add(new Param("formCode", Variant.createString(idn)));

		try {
			connection = getConnection();
			resultValue = SqlBlockExecutor.execute(connection, alg, paramList.toArray(new Param[paramList.size()]), ValueType.valueOf(indList.get(0).getValueType()));
		} finally {
			DbUtil.closeConnection(connection);
		}
		return resultValue;
	}

	@Override
	public Variant getRefItemValueByRecId(String refName, String refColumn, Long recId, Date date, ValueType vt) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Variant result = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("select "+refColumn+" as col " +
					" from "+refName+" r " +
					" where r.rec_id=? " +
					" and r.begin_date=(select max(r2.begin_date) from "+refName+" r2 " +
					" where r2.rec_id=r.rec_id and r2.begin_date<=?)" +
					"   and (? < r.end_date or r.end_date is null)" +
					" order by r.delfl");
			java.sql.Date sqlDate = new java.sql.Date(date.getTime());
			ps.setLong(1, recId);
			ps.setDate(2, sqlDate);
			ps.setDate(3, sqlDate);

			rs = ps.executeQuery();
			if (rs.next()) {
				result = new Variant();
				result.setValueType(vt);
				switch (vt) {
					case STRING:
						result.setStrValue(rs.getString("COL"));
						break;
					case BOOLEAN:
						result.setBoolValue(rs.getBoolean("COL"));
						break;
					case DATE:
						result.setDateValue(rs.getDate("COL"));
						break;
					case NUMBER_0:
						result.setLngValue(rs.getLong("COL"));
						break;
					case NUMBER_1:
					case NUMBER_2:
					case NUMBER_3:
					case NUMBER_4:
					case NUMBER_5:
					case NUMBER_6:
					case NUMBER_7:
					case NUMBER_8:
						result.setDblValue(rs.getDouble("COL"));
						break;
					default:
						throw new IllegalStateException(MessageFormat.format("Unknown ValueType {0}", vt.name()));
				}
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public List<RefExtIndicatorItem> getExtIndicatorItemsByCode(String code) {
		RefExtIndicatorItem filter = new RefExtIndicatorItem();
		filter.setCode(code);
		return reference.getExtIndicatorItemsByFilters(null, filter);
	}

	private List<ReportHistory> getAllReportHistories() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ReportHistory> result = new ArrayList<ReportHistory>();
        Connection connection = null;
		DataSource dataSource;
		try {
			try {
				Context context = new InitialContext();
				dataSource = (DataSource) context.lookup("jdbc/TmpFrsiPool");
				logger.info("Connected to " + "jdbc/TmpFrsiPool");
			} catch (NamingException e) {
				logger.error("Could not connect to " + "jdbc/TmpFrsiPool");
				throw new EJBException(e);
			}
			connection = dataSource.getConnection();
			ps = connection.prepareStatement("SELECT\n" +
					"  h.*\n" +
					"FROM tmp_report_history h\n");
			rs = ps.executeQuery();
			while (rs.next()) {
				ReportHistory h = getReportHistoryFromResultSet(rs, true, true);
				Report r = new Report();
				r.setId(rs.getLong("report_id"));
				h.setReport(r);
				result.add(h);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	private void insertReportHistoriesTmp(List<ReportHistory> histories){
		Connection connection = null;
		try {
			connection = getConnection();

			for (ReportHistory h : histories) {
				insertReportHistoryTmp(h, connection);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, null);
		}
	}

	private void insertReportHistoryTmp(ReportHistory reportHistory, Connection connection) throws SQLException{
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("INSERT INTO tmp_report_history" +
					" (id, report_id, save_date, data, data_size, comments, attachment, attachment_size, attachment_file_name, hash, delivery_way_code, user_id, user_info, su_user_id, su_user_info, su_comments)" +
					" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{"id"});
			ps.setLong(1, reportHistory.getId());
			ps.setLong(2, reportHistory.getReport().getId());
			ps.setTimestamp(3, reportHistory.getSaveDate() == null ? null : new java.sql.Timestamp(reportHistory.getSaveDate().getTime()));

			Clob clobData = connection.createClob();
			clobData.setString(1, reportHistory.getData());
			ps.setClob(4, clobData);

			ps.setLong(5, reportHistory.getDataSize());
			ps.setString(6, reportHistory.getComments());

			Blob blobAttachment = connection.createBlob();
			blobAttachment.setBytes(1, reportHistory.getAttachment());
			ps.setBlob(7, blobAttachment);

			ps.setLong(8, reportHistory.getAttachmentSize() == null ? 0 : reportHistory.getAttachmentSize());
			ps.setString(9, reportHistory.getAttachmentFileName());

			ps.setString(10, reportHistory.getHash());

			ps.setString(11, reportHistory.getDeliveryWayCode());
			ps.setLong(12, reportHistory.getUserId());
			ps.setString(13, reportHistory.getUserInfo());
			if (reportHistory.getSuUserId() == null) ps.setNull(14, Types.NUMERIC); else ps.setLong(14, reportHistory.getSuUserId());
			ps.setString(15, reportHistory.getSuUserInfo());
			ps.setString(16, reportHistory.getSuComments());

			ps.executeUpdate();
		} finally {
			DbUtil.closeStatement(ps);
		}
	}


	@Override
	public void insertIntoReportRefLink (){
		try {
			List<Report> allReports = getAllReports();

			for(Report v_item : allReports){
				Long reportId = v_item.getId();

				if(reportId != 0) {
					//ReportHistory lastReportHistory = sessionBean.getPersistence().getLastReportHistoryByReportId(id, true, false, false, null);
					List<ReportHistory> listReportHistory = getReportHistoryByReportIdNoLobs(reportId, null);
					for (ReportHistory item : listReportHistory) {
						if (item != null) {
							Long formId = getFormIdByCode(item.getReport().getFormCode());
							if (formId != null && formId != 0) {
								Form formWithInputValueChecks = getForm(formId, v_item.getReportDate());
								if (formWithInputValueChecks != null) {
									String data = getReportHistory(item.getId(), true, false).getData();
									fillDefaultValueByFormWithValues(null, item.getId(), formWithInputValueChecks.getFormHistory().getInputValueChecks(), data, true);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw new EJBException(e);
		}
	}

	@Override
	public List<Long> getDepRecIdListByUser(Long userId){
		List<Long> result = new ArrayList<Long>();

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet cursor = null;

		try {
			connection = getConnection();

			ps = connection.prepareStatement(
					"select ref_department_rec_id\n" +
					"  from user_departments \n" +
					" where user_id = ?");
			ps.setLong(1, userId);
			cursor = ps.executeQuery();

			while (cursor.next()) {
				result.add(cursor.getLong("REF_DEPARTMENT_REC_ID"));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, cursor);
		}
        return result;
	}

	@Override
	public void updateBalanceAccountTemplate(String xml, String code, Long kindEvent, Long respondentId, Long userId, String userLocation, Date beginDate, Connection connection){
		try {
			Form form = newForm(xml, "ru");
			if (form.getFormHistory().isValid()) {
				Date curDate = new Date();
				form.getFormHistory().setLastUpdateXml(curDate);
				form.getFormHistory().setBeginDate(beginDate);
				form.getFormHistory().setEndDate(null);

				Long fhId = getFormHistoryId(code, beginDate, connection);
				if (fhId != null && fhId != 0) {
					Long formId = getFormId(code, beginDate);
					form.setId(formId);
					form.getFormHistory().setId(fhId);
				} else {
					Long formId = getFormIdByCode(code);
					form.setId(formId);
					form.getFormHistory().setFormId(formId);
					Long formHistoryId = insertFormHistory(form.getFormHistory(), form.getTypeCode().equals(Form.Type.INPUT.name()), null, connection);
					form.getFormHistory().setId(formHistoryId);
				}
				if (form.getId() != null) {
					updateForm(form, null);
				} else {
					Long newFormId = insertForm(form, null);
					form.setId(newFormId);
				}
			} else {
				throw new EJBException(form.getFormHistory().getErrorMessage());
			}
		}catch (Exception e) {
			throw new EJBException(e);
		}
	}

	@Override
	public Long getDepRecIdByUser(Long userId){
		Long result = null;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet cursor = null;

		try {
			connection = getConnection();

			ps = connection.prepareStatement(
					"select g.ref_department_rec_id\n" +
					"  from groups g,\n" +
					"       group_users gu\n" +
					" where g.group_id = gu.group_id\n" +
					"   and g.ref_department_rec_id is not null\n"+
					"   and gu.user_id = ?");
			ps.setLong(1, userId);
			cursor = ps.executeQuery();

			while (cursor.next()) {
				result = cursor.getLong("REF_DEPARTMENT_REC_ID");
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, cursor);
		}
		return result;
	}

	@Override
	public PeriodAlgResult executePeriodAlg(long refPeriodRecId, String formCode, Date reportDate, String idn) {
		Connection connection = null;
		PeriodAlgResult resultValue;

		RefPeriodItem filterPeriod = new RefPeriodItem();
		filterPeriod.setRecId(refPeriodRecId);
		List<RefPeriodItem> periods = (List<RefPeriodItem>)reference.getRefAbstractByFilterList(RefPeriodItem.REF_CODE, filterPeriod, reportDate);
		if (periods.size() == 0) {
			return null;
		}

		RefPeriodAlgItem filterAlg = new RefPeriodAlgItem();
		filterAlg.setRecId(periods.get(0).getRefPeriodAlgId());
		List<RefPeriodAlgItem> algs = (List<RefPeriodAlgItem>) reference.getRefAbstractByFilterList(RefPeriodAlgItem.REF_CODE, filterAlg, reportDate);
		if (algs.size() == 0) {
			throw new EJBException(MessageFormat.format("Алгоритм периода представления с REC_ID {0} для даты {1} не найден",
					periods.get(0).getRefPeriodAlgId(), Convert.dateFormatRus.format(reportDate)));
		}

		List<RefPeriodArgument> argItems = reference.getRefPeriodArguments(periods.get(0).getId());

		RefPeriodAlgItem algItem = algs.get(0);
		String alg = algItem.getAlg();
		List<Param> arguments = new ArrayList<Param>();
		for (RefPeriodArgument a : argItems) {
			arguments.add(new Param(a.getName(), a.getValue()));
		}
		Date backendDate = new Date();
		arguments.add(new Param("reportDate", Variant.createDate(reportDate)));
		arguments.add(new Param("idn", Variant.createString(idn)));
		arguments.add(new Param("formCode", Variant.createString(idn)));
		arguments.add(new Param("backendDate", Variant.createDate(backendDate)));


		try {
			connection = getConnection();

			Variant result = SqlBlockExecutor.execute(connection, alg, arguments.toArray(new Param[arguments.size()]), ValueType.NUMBER_0);
			if (result == null)
				resultValue = null;
			else {
				int days = (int) result.getLngValue();
				if (algItem.isOnlyWorkingDays()) {
					int searchDays = 15; // ищем рабочие дни в этом диапазоне
					LocalDate d1 = new LocalDate(backendDate.getTime()).plusDays(days);
					LocalDate d2 = d1.plusDays(searchDays);
					Set<LocalDate> nonWorkingDays = getNonWorkingDaysBetween(d1, d2, connection);
					LocalDate workingDay = d1;
					for (LocalDate d = d1; !d.isAfter(d2); d = d.plusDays(1)) {
						if (!nonWorkingDays.contains(d)) {
							workingDay = d;
							break;
						}
					}
					days = Days.daysBetween(d1, workingDay).getDays() + days;
				}
				resultValue = PeriodAlgResult.success(days);
			}
		} catch (Exception e) {
			return PeriodAlgResult.error(e.getMessage());
		} finally {
			DbUtil.closeConnection(connection);
		}
		return resultValue;
	}

	private Set<LocalDate> getNonWorkingDaysBetween(LocalDate d1, LocalDate d2, Connection connection){
		Set<LocalDate> dates = new HashSet<LocalDate>();
		PreparedStatement ps = null;
		ResultSet cursor = null;
		try {
			connection = getConnection();

			ps = connection.prepareStatement(
					"SELECT r.date_value\n" +
							"FROM v_ref_wkd_holidays r\n" +
							"WHERE r.date_value BETWEEN ? AND ?\n" +
							"ORDER BY r.date_value");
			ps.setDate(1, new java.sql.Date(d1.toDate().getTime()));
			ps.setDate(2, new java.sql.Date(d2.toDate().getTime()));
			cursor = ps.executeQuery();

			while (cursor.next()) {
				Date d = cursor.getDate("date_value");
				dates.add(new LocalDate(d.getTime()));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(null, ps, cursor);
		}
		return dates;
	}

	@Override
	public List<ReportPeriod> getRespondentOverdueReports(Long userId, String idn, int periodCount) {
		RefRespondentItem respondentItem = reference.getRespondentByIdn(idn, new Date());
		List<ReportPeriod> result = new ArrayList<ReportPeriod>();

		Connection connection = null;
		try {
			connection = getConnection();

			for (int periodOffset = 0; periodOffset < periodCount; periodOffset++) {
				List<ReportPeriod> reports = new ArrayList<ReportPeriod>();
				PreparedStatement ps = null;
				ResultSet cursor = null;

				try {

					ps = connection.prepareStatement(
							"SELECT\n" +
									"  t.FORM_CODE,\n" +
									"  t.report_date,\n" +
									"  t.REF_PERIOD_REC_ID,\n" +
									"  fh.SHORT_NAME,\n" +
									"  st.STATUS_CODE\n" +
									"FROM (\n" +
									"       SELECT\n" +
									"         stf.FORM_CODE,\n" +
									"         stf.REF_PERIOD_REC_ID,\n" +
									"         PKG_FRSI_UTIL.PLUS_PERIOD(PKG_FRSI_UTIL.FLOOR_DATE(trunc(?), p.CODE), p.CODE, ?) report_date\n" +
									"       FROM SUBJECTTYPE_FORMS stf INNER JOIN REP_PER_DUR_MONTHS p ON stf.PERIOD_ID = p.ID\n" +
									"       WHERE stf.REF_SUBJECT_TYPE_REC_ID = ?) t\n" +
									"  INNER JOIN FORMS f ON t.FORM_CODE = f.CODE AND f.TYPE_CODE = 'INPUT'\n" +
									"  INNER JOIN FORM_HISTORY fh\n" +
									"    ON f.ID = fh.FORM_ID AND fh.BEGIN_DATE <= t.report_date AND (fh.END_DATE IS NULL OR fh.END_DATE > t.report_date)\n" +
									"  LEFT JOIN (\n" +
									"      REPORTS r\n" +
									"      INNER JOIN REPORT_HISTORY h ON h.ID = (SELECT max(h2.ID)\n" +
									"                                             FROM REPORT_HISTORY h2\n" +
									"                                             WHERE h2.REPORT_ID = r.ID)\n" +
									"      INNER JOIN REPORT_HISTORY_STATUSES st ON st.ID = (SELECT max(st2.ID)\n" +
									"                                                        FROM REPORT_HISTORY_STATUSES st2\n" +
									"                                                        WHERE st2.REPORT_HISTORY_ID = h.ID)\n" +
									"    ) ON r.FORM_CODE = t.FORM_CODE AND r.REPORT_DATE = t.report_date AND r.IDN = ?\n" +
									"WHERE t.REF_PERIOD_REC_ID IS NOT NULL\n" +
									"      AND (st.STATUS_CODE IS NULL OR st.STATUS_CODE NOT IN ('APPROVED', 'COMPLETED', 'DISAPPROVED'))\n" +
									"      AND f.CODE IN (SELECT sf.FORM_CODE\n" +
									"                     FROM F_SESSION_RESP_FORMS sf\n" +
									"                     WHERE sf.USER_ID = ? " +
									"						AND sf.IDN = ?)\n" +
									"      AND (SELECT count(hs.id)\n" +
									"           FROM REPORT_HISTORY_STATUSES hs\n" +
									"           WHERE hs.REPORT_HISTORY_ID = h.ID AND STATUS_CODE = 'COMPLETED') < 1");
					Date now = new Date();
					ps.setDate(1, new java.sql.Date(now.getTime()));
					ps.setInt(2, periodOffset);
					ps.setLong(3, respondentItem.getRefSubjectTypeRecId());
					ps.setString(4, idn);
					ps.setLong(5, userId);
					ps.setString(6, idn);
					cursor = ps.executeQuery();

					while (cursor.next()) {
						ReportPeriod report = new ReportPeriod();
						report.setFormCode(cursor.getString("form_code"));
						report.setReportDate(cursor.getDate("report_date"));
						report.setFormName(cursor.getString("short_name"));
						report.setRefPeriodRecId(cursor.getLong("ref_period_rec_id"));
						report.setIdn(idn);
						report.setStatus(cursor.getString("status_code"));
						reports.add(report);
					}
				} catch (SQLException e) {
					throw new EJBException(e);
				} finally {
					DbUtil.closeConnection(null, ps, cursor);
				}

				for (ReportPeriod r : reports) {
					PeriodAlgResult periodAlg = executePeriodAlg(r.getRefPeriodRecId(), r.getFormCode(), r.getReportDate(), r.getIdn());
					if (periodAlg != null) {
						if (periodAlg.hasError()) {
							r.setPeriodAlgError(periodAlg.getErrorMessage());
							result.add(r);
						} else if (periodAlg.getResult() < 0) {
							r.setLeftDays(Math.abs(periodAlg.getResult()));
							result.add(r);
						}
					}
				}
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, null, null);
		}
		return result;
	}

	@Override
	public List<ReportPeriod> getAllOverdueReports(int periodCount) {
		List<ReportPeriod> result = new ArrayList<ReportPeriod>();
		Connection connection = null;
		try {
			connection = getConnection();

			for (int p = 0; p < periodCount; p++) {
				int periodOffset = (-1) * p;
				List<ReportPeriod> reports = new ArrayList<ReportPeriod>();
				PreparedStatement ps = null;
				ResultSet cursor = null;

				try {

					ps = connection.prepareStatement(
							"SELECT\n" +
									"  t.REF_SUBJECT_TYPE_REC_ID,\n" +
									"  t.FORM_CODE,\n" +
									"  t.report_date,\n" +
									"  up.IDN,\n" +
									"  t.REF_PERIOD_REC_ID,\n" +
									"  fh.SHORT_NAME,\n" +
									"  st.STATUS_CODE,\n" +
									"  +\n" +
									"  res.ID respondent_id,\n" +
									"  r.ID   report_id,\n" +
									"  h.ID   report_history_is\n" +
									"FROM (\n" +
									"       SELECT\n" +
									"         stf.REF_SUBJECT_TYPE_REC_ID,\n" +
									"         stf.FORM_CODE,\n" +
									"         stf.REF_PERIOD_REC_ID,\n" +
									"         PKG_FRSI_UTIL.PLUS_PERIOD(PKG_FRSI_UTIL.FLOOR_DATE(trunc(?), p.CODE), p.CODE, ?) report_date\n" +
									"       FROM SUBJECTTYPE_FORMS stf\n" +
									"         INNER JOIN REP_PER_DUR_MONTHS p ON stf.PERIOD_ID = p.ID\n" +
									"     ) t\n" +
									"  INNER JOIN REF_SUBJECT_TYPE st ON st.REC_ID = t.REF_SUBJECT_TYPE_REC_ID AND st.BEGIN_DATE <= t.report_date AND\n" +
									"                                    (st.END_DATE IS NULL OR st.END_DATE > t.report_date) AND st.DELFL = 0\n" +
									"  INNER JOIN REF_RESPONDENT res ON res.REF_SUBJECT_TYPE = st.ID AND res.BEGIN_DATE <= t.report_date AND\n" +
									"                                   (res.END_DATE IS NULL OR res.END_DATE > t.report_date) AND res.DELFL = 0\n" +
									"  INNER JOIN V_REF_UNIONPERSONS up ON up.ID = res.REF_UNIONPERSONS AND \n" +
									"  									up.BEGIN_DATE <= t.report_date AND\n" +
									"                                   (up.END_DATE IS NULL OR up.END_DATE > t.report_date)\n" +
									"  INNER JOIN FORMS f ON t.FORM_CODE = f.CODE AND f.TYPE_CODE = 'INPUT'\n" +
									"  INNER JOIN FORM_HISTORY fh\n" +
									"    ON f.ID = fh.FORM_ID AND fh.BEGIN_DATE <= t.report_date AND (fh.END_DATE IS NULL OR fh.END_DATE > t.report_date)\n" +
									"  LEFT JOIN (\n" +
									"      REPORTS r\n" +
									"      INNER JOIN REPORT_HISTORY h ON h.ID = (SELECT max(h2.ID)\n" +
									"                                             FROM REPORT_HISTORY h2\n" +
									"                                             WHERE h2.REPORT_ID = r.ID)\n" +
									"      INNER JOIN REPORT_HISTORY_STATUSES st ON st.ID = (SELECT max(st2.ID)\n" +
									"                                                        FROM REPORT_HISTORY_STATUSES st2\n" +
									"                                                        WHERE st2.REPORT_HISTORY_ID = h.ID)\n" +
									"    ) ON r.FORM_CODE = t.FORM_CODE AND r.REPORT_DATE = t.report_date AND r.IDN = up.IDN\n" +
									"WHERE t.REF_PERIOD_REC_ID IS NOT NULL\n" +
									"      AND (st.STATUS_CODE IS NULL OR st.STATUS_CODE NOT IN ('APPROVED', 'DISAPPROVED'))\n" +
									"      AND (st.STATUS_CODE = 'APPROVED' OR (SELECT count(hs.id)\n" +
									"                                           FROM REPORT_HISTORY_STATUSES hs\n" +
									"                                           WHERE hs.REPORT_HISTORY_ID = h.ID AND STATUS_CODE = 'COMPLETED') < 1)");
					Date now = new Date();
					ps.setDate(1, new java.sql.Date(now.getTime()));
					ps.setInt(2, periodOffset);
					cursor = ps.executeQuery();

					while (cursor.next()) {
						ReportPeriod report = new ReportPeriod();
						report.setFormCode(cursor.getString("form_code"));
						java.sql.Date sqlDate = cursor.getDate("report_date");
						Date date = new Date(sqlDate.getTime());
						report.setReportDate(date);
						report.setFormName(cursor.getString("short_name"));
						report.setRefPeriodRecId(cursor.getLong("ref_period_rec_id"));
						report.setIdn(cursor.getString("IDN"));
						report.setStatus(cursor.getString("status_code"));
						report.setRespondentId(cursor.getLong("respondent_id"));
						report.setSubjectTypeRecId(cursor.getLong("ref_subject_type_rec_id"));
						report.setReportId(cursor.getLong("report_id"));
						reports.add(report);
					}
				} catch (SQLException e) {
					throw new EJBException(e);
				} finally {
					DbUtil.closeConnection(null, ps, cursor);
				}

				for (ReportPeriod r : reports) {
					PeriodAlgResult algResult = executePeriodAlg(r.getRefPeriodRecId(), r.getFormCode(), r.getReportDate(), r.getIdn());
					if (algResult != null && !algResult.hasError()) {
						r.setLeftDays(algResult.getResult());
						result.add(r);
					}
				}
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, null, null);
		}
		return result;
	}

	@Override
	public SettingsItem getSettingsItemByTypeUserId(SettingsItem.Type settingType, Long userId) {
		if (settingType == SettingsItem.Type.NOTICE_MAIL_OVERDUE_DATE || settingType == SettingsItem.Type.NOTICE_SYS_OVERDUE_DATE) {
			SettingsItem item = new SettingsItem(settingType, userId);
			List<NoticeSettings> noticeSettings = getNoticeForUser(userId);
			NoticeSettings ns = null;
			for (NoticeSettings t : noticeSettings) {
				if (t.getId() == 117) {
					ns = t;
					break;
				}
			}
			if (ns == null) {
				return null;
			} else {
				if (settingType == SettingsItem.Type.NOTICE_MAIL_OVERDUE_DATE)
					item.setRawValue(SettingsValueConverter.toRaw(ns.getNoticeMail(), false, Boolean.class));
				if (settingType == SettingsItem.Type.NOTICE_SYS_OVERDUE_DATE)
					item.setRawValue(SettingsValueConverter.toRaw(ns.getNoticeSystem(), false, Boolean.class));
			}
			return item;
		}

		SettingsItem result = null;

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement("SELECT\n" +
					"  si.USER_ID,\n" +
					"  si.SETTING_TYPE,\n" +
					"  si.RAW_VALUE\n" +
					"FROM SETTINGS_ITEMS si\n" +
					"WHERE si.SETTING_TYPE = ?\n" +
					"      AND si.USER_ID = ?\n" +
					"UNION ALL\n" +
					"SELECT\n" +
					"  ?,\n" +
					"  si.SETTING_TYPE,\n" +
					"  si.DEF_VALUE\n" +
					"FROM SETTINGS_ITEMS_DEF_VAL si\n" +
					"WHERE si.SETTING_TYPE = ?");
			ps.setString(1, settingType.name());
			ps.setLong(2,userId);
			ps.setLong(3,userId);
			ps.setString(4, settingType.name());

			rs = ps.executeQuery();
			if (rs.next()) {
				result = new SettingsItem();
				result.setType(settingType);
				result.setUserId(userId);
				Clob value = rs.getClob("raw_value");
				if (value != null) {
					result.setRawValue(value.getSubString(1, (int) value.length()));
					value.free();
				}
			}

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public void saveSettingsItems(SettingsItem[] settingsItems, String userLocation) {
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			for (SettingsItem settingsItem : settingsItems) {
				if (settingsItem.getType() == SettingsItem.Type.NOTICE_MAIL_OVERDUE_DATE || settingsItem.getType() == SettingsItem.Type.NOTICE_SYS_OVERDUE_DATE) {
					List<NoticeSettings> noticeSettings = getNoticeForUser(settingsItem.getUserId());
					NoticeSettings ns = null;
					for (NoticeSettings t : noticeSettings) {
						if (t.getId() == 117) {
							ns = t;
							break;
						}
					}
					boolean value = SettingsValueConverter.fromRaw(settingsItem.getRawValue(), false, Boolean.class);
					if (ns == null) {
						ns = new NoticeSettings();
						ns.setId(117L);
						if (settingsItem.getType() == SettingsItem.Type.NOTICE_MAIL_OVERDUE_DATE)
							ns.setNoticeMail(value);
						if (settingsItem.getType() == SettingsItem.Type.NOTICE_SYS_OVERDUE_DATE)
							ns.setNoticeSystem(value);
						noticeSettings.add(ns);
					} else {
						if (settingsItem.getType() == SettingsItem.Type.NOTICE_MAIL_OVERDUE_DATE)
							ns.setNoticeMail(value);
						if (settingsItem.getType() == SettingsItem.Type.NOTICE_SYS_OVERDUE_DATE)
							ns.setNoticeSystem(value);
					}
					insertNoticeUserOff(noticeSettings, settingsItem.getUserId(), userLocation, connection);
					continue;
				}
				mergeSettingsItem(settingsItem, connection);
			}
			connection.commit();
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


	private void mergeSettingsItem(SettingsItem settingsItem, Connection connection) {
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) { localCon = true; connection = getConnection(); }

			ps = connection.prepareStatement("MERGE INTO SETTINGS_ITEMS si\n" +
					"USING (SELECT ? SETTING_TYPE, ? USER_ID\n" +
					"         FROM dual) t\n" +
					"ON (si.SETTING_TYPE = t.SETTING_TYPE AND si.USER_ID = t.USER_ID)\n" +
					"WHEN MATCHED THEN\n" +
					"  UPDATE SET si.RAW_VALUE = ?\n" +
					"WHEN NOT MATCHED THEN\n" +
					"  INSERT\n" +
					"    (SETTING_TYPE, USER_ID, RAW_VALUE)\n" +
					"  VALUES\n" +
					"    (?, ?, ?)");

			ps.setString(1, settingsItem.getType().name());
			ps.setLong(2, settingsItem.getUserId());

			Clob clobUpd = connection.createClob();
			clobUpd.setString(1, settingsItem.getRawValue());
			ps.setClob(3, clobUpd);

			ps.setString(4, settingsItem.getType().name());
			ps.setLong(5, settingsItem.getUserId());

			Clob clobIns = connection.createClob();
			clobIns.setString(1, settingsItem.getRawValue());
			ps.setClob(6, clobIns);

			int affectedRowsH = ps.executeUpdate();
			if (affectedRowsH == 0) throw new SQLException("Inserting an item failed, no rows affected.");

		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	@Override
	public int getDaysBeforeOverdueDateSettingDefaultValue() {
		return daysBeforeOverdueDateSettingDefaultValue;
	}

	@Override
	public List<UserWarrant> getUserWarrantsByPrincipal(Long userId) {
		List<UserWarrant> result = new ArrayList<UserWarrant>();
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			String sql = "SELECT\n" +
					"  w.ID, w.CODE, w.PRINCIPAL,w.ATTORNEY,w.BEGIN_DATE,w.END_DATE,w.CANCELED,w.READONLY,w.FILE_NAME,\n" +
					"  (SELECT u.SCREEN_NAME || '-' || get_fio(u.LAST_NAME, u.FIRST_NAME, u.MIDDLE_NAME, 1)\n" +
					"   FROM F_USERS u\n" +
					"   WHERE u.USER_ID = w.PRINCIPAL) PRINCIPAL_NAME,\n" +
					"  (SELECT u.SCREEN_NAME || '-' || get_fio(u.LAST_NAME, u.FIRST_NAME, u.MIDDLE_NAME, 1)\n" +
					"   FROM F_USERS u\n" +
					"   WHERE u.USER_ID = w.ATTORNEY)  ATTORNEY_NAME\n" +
					"FROM USER_WARRANT w\n" +
					"WHERE w.PRINCIPAL = ?";
			ps = connection.prepareStatement(sql);
			ps.setLong(1,userId);

			rs = ps.executeQuery();
			while (rs.next()) {
				UserWarrant w = getUserWarrantFromResultSet(rs);
				w.setFiles(getFileListByLinkId(w.getId(), 4, connection));
				result.add(w);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public List<UserWarrant> getUserWarrantsByAttorney(Long userId) {
		List<UserWarrant> result = new ArrayList<UserWarrant>();
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			String sql = "SELECT\n" +
					"  w.ID, w.CODE, w.PRINCIPAL,w.ATTORNEY,w.BEGIN_DATE,w.END_DATE,w.CANCELED,w.READONLY,w.FILE_NAME,\n" +
					"  (SELECT u.SCREEN_NAME || '-' || get_fio(u.LAST_NAME, u.FIRST_NAME, u.MIDDLE_NAME, 1)\n" +
					"   FROM F_USERS u\n" +
					"   WHERE u.USER_ID = w.PRINCIPAL) PRINCIPAL_NAME,\n" +
					"  (SELECT u.SCREEN_NAME || '-' || get_fio(u.LAST_NAME, u.FIRST_NAME, u.MIDDLE_NAME, 1)\n" +
					"   FROM F_USERS u\n" +
					"   WHERE u.USER_ID = w.ATTORNEY)  ATTORNEY_NAME\n" +
					"FROM USER_WARRANT w\n" +
					"WHERE w.ATTORNEY = ?";
			ps = connection.prepareStatement(sql);
			ps.setLong(1,userId);

			rs = ps.executeQuery();
			while (rs.next()) {
				UserWarrant w = getUserWarrantFromResultSet(rs);
				w.setFiles(getFileListByLinkId(w.getId(), 4, connection));
				result.add(w);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	@Override
	public List<UserWarrant> getActiveWarrantsByAttorney(Long userId, Date date) {
		List<UserWarrant> result = new ArrayList<UserWarrant>();
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			String sql = "SELECT\n" +
					"  w.ID, w.CODE, w.PRINCIPAL,w.ATTORNEY,w.BEGIN_DATE,w.END_DATE,w.CANCELED,w.READONLY,\n" +
					"  (SELECT u.SCREEN_NAME || '-' || get_fio(u.LAST_NAME, u.FIRST_NAME, u.MIDDLE_NAME, 1)\n" +
					"   FROM F_USERS u\n" +
					"   WHERE u.USER_ID = w.PRINCIPAL) PRINCIPAL_NAME,\n" +
					"  (SELECT u.SCREEN_NAME || '-' || get_fio(u.LAST_NAME, u.FIRST_NAME, u.MIDDLE_NAME, 1)\n" +
					"   FROM F_USERS u\n" +
					"   WHERE u.USER_ID = w.ATTORNEY)  ATTORNEY_NAME\n" +
					"FROM USER_WARRANT w\n" +
					"WHERE w.ATTORNEY = ?\n" +
					"      AND w.CANCELED = 0\n" +
					"      AND trunc(w.BEGIN_DATE) <= trunc(?) AND (w.END_DATE IS NULL OR trunc(w.END_DATE) >= trunc(?))";
			ps = connection.prepareStatement(sql);
			java.sql.Date sqlDate = new java.sql.Date(date.getTime());
			ps.setLong(1, userId);
			ps.setDate(2, sqlDate);
			ps.setDate(3, sqlDate);

			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(getUserWarrantFromResultSet(rs));
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(connection, ps, rs);
		}
		return result;
	}

	private void saveUserWarrants(Long userId, List<UserWarrant> warrants, Connection connection, AuditEvent auditEvent){
        List<UserWarrant> oldWarrants = getUserWarrantsByAttorney(userId);
        Map<Long, UserWarrant> map = new HashMap<Long, UserWarrant>();
        for(UserWarrant w:oldWarrants){
            map.put(w.getId(), w);
        }
		Date date = new Date();
		for (UserWarrant w : warrants) {
            if (w.getId() == null) {
				AuditEvent childAe = null;
				if (auditEvent != null) {
					childAe = new AuditEvent();
					childAe.setUserName(auditEvent.getUserName());
					childAe.setUserLocation(auditEvent.getUserLocation());
					childAe.setUserId(auditEvent.getUserId());
					childAe.setDateEvent(date);
					childAe.setDateIn(date);
					childAe.setIdKindEvent(129L);
					childAe.setCodeObject("USER_WARRANT");
					childAe.setNameObject("Доверенность");
					childAe.setParentId(auditEvent.getParentId());
				}
                insertUserWarrant(w, connection, childAe);
            }
            if (map.containsKey(w.getId())) {
            	UserWarrant o = map.get(w.getId());
				if (!w.isSame(o) || w.isFilesChanged()) {
					AuditEvent childAe = null;
					if (auditEvent != null) {
						childAe = new AuditEvent();
						childAe.setUserName(auditEvent.getUserName());
						childAe.setUserLocation(auditEvent.getUserLocation());
						childAe.setUserId(auditEvent.getUserId());
						childAe.setDateEvent(date);
						childAe.setDateIn(date);
						childAe.setIdKindEvent(130L);
						childAe.setCodeObject("USER_WARRANT");
						childAe.setNameObject("Доверенность");
						childAe.setRecId(w.getId());
						childAe.setParentId(auditEvent.getParentId());
					}
					updateUserWarrant(w, connection, childAe);
				}
				map.remove(w.getId());
            }
        }
		for (UserWarrant w : map.values()) {
			if (!w.isReadonly()) {
				AuditEvent childAe = null;
				if (auditEvent != null) {
					childAe = new AuditEvent();
					childAe.setUserName(auditEvent.getUserName());
					childAe.setUserLocation(auditEvent.getUserLocation());
					childAe.setUserId(auditEvent.getUserId());
					childAe.setDateEvent(date);
					childAe.setDateIn(date);
					childAe.setIdKindEvent(131L);
					childAe.setCodeObject("USER_WARRANT");
					childAe.setNameObject("Доверенность");
					childAe.setRecId(w.getId());
					childAe.setParentId(auditEvent.getParentId());
				}
				deleteUserWarrant(w.getId(), connection, childAe);
			}
		}
	}

	private UserWarrant getUserWarrantFromResultSet(ResultSet rs) throws SQLException{
		UserWarrant w = new UserWarrant();
		w.setId(rs.getLong("id"));
		w.setCode(rs.getString("code"));
		w.setPrincipal(rs.getLong("principal"));
		w.setAttorney(rs.getLong("attorney"));
		w.setBeginDate(rs.getDate("begin_date"));
		w.setEndDate(rs.getDate("end_date"));
		w.setReadonly(rs.getInt("readonly") > 0);
		w.setCanceled(rs.getInt("canceled") > 0);
		w.setPrincipalName(rs.getString("principal_name"));
		w.setAttorneyName(rs.getString("attorney_name"));
		w.setHaveFile(haveAttachedFiles(w.getId(), 4));
		return w;
	}

	private UserWarrant getUserWarrantById(Long id, Connection connection) {
		UserWarrant result = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean localCon = false;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
			}

			String sql = "SELECT\n" +
					"  w.ID, w.CODE, w.PRINCIPAL,w.ATTORNEY,w.BEGIN_DATE,w.END_DATE,w.CANCELED,w.READONLY,\n" +
					"  (SELECT u.SCREEN_NAME || '-' || u.FIRST_NAME || ' ' || u.LAST_NAME\n" +
					"   FROM F_USERS u\n" +
					"   WHERE u.USER_ID = w.PRINCIPAL) PRINCIPAL_NAME,\n" +
					"  (SELECT u.SCREEN_NAME || '-' || u.FIRST_NAME || ' ' || u.LAST_NAME\n" +
					"   FROM F_USERS u\n" +
					"   WHERE u.USER_ID = w.ATTORNEY)  ATTORNEY_NAME\n" +
					"FROM USER_WARRANT w\n" +
					"WHERE w.ID = ?";
			ps = connection.prepareStatement(sql);
			ps.setLong(1, id);

			rs = ps.executeQuery();
			if (rs.next()) {
				result = getUserWarrantFromResultSet(rs);
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps, rs);
		}
		return result;
	}

	private void insertUserWarrant(UserWarrant warrant, Connection connection, AuditEvent auditEvent) {
		PreparedStatement ps = null;
		boolean localCon = false;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}

			ps = connection.prepareStatement("INSERT INTO USER_WARRANT (ID, CODE, PRINCIPAL, ATTORNEY, BEGIN_DATE, END_DATE, READONLY, CANCELED)\n" +
					"VALUES (SEQ_USER_WARRANT_ID.nextval, ?, ?, ?, ?, ?, ?, ?)", new String[] {"id"});

			ps.setString(1, warrant.getCode());
			ps.setLong(2, warrant.getPrincipal());
			ps.setLong(3, warrant.getAttorney());
			ps.setDate(4, new java.sql.Date(warrant.getBeginDate().getTime()));
			if (warrant.getEndDate() != null) {
				ps.setDate(5, new java.sql.Date(warrant.getEndDate().getTime()));
			} else {
				ps.setNull(5, OracleTypes.NULL);
			}
			ps.setInt(6, warrant.isReadonly() ? 1 : 0);
			ps.setInt(7, warrant.isCanceled() ? 1 : 0);

			int affectedRowsH = ps.executeUpdate();
			if (affectedRowsH == 0) throw new SQLException("Inserting an item failed, no rows affected.");

			ResultSet generatedKeys = ps.getGeneratedKeys();
			long id = 0;
			if (generatedKeys.next()) {
				id = generatedKeys.getLong(1);
			}
			if (auditEvent != null) {
				auditEvent.setRecId(id);
				insertAuditEvent(auditEvent, connection);
			}

			if (warrant.getFiles() != null) {
				Date now = new Date();
				for (AttachedFile file : warrant.getFiles()) {
					file.setLinkId(id);
					file.setFileKind(4);
					file.setFileDate(now);

					AuditEvent childAe = null;
					if (auditEvent != null) {
						childAe = new AuditEvent();
						childAe.setUserName(auditEvent.getUserName());
						childAe.setUserLocation(auditEvent.getUserLocation());
						childAe.setUserId(auditEvent.getUserId());
						childAe.setDateEvent(auditEvent.getDateEvent());
						childAe.setDateIn(auditEvent.getDateIn());
						childAe.setIdKindEvent(132L);
						childAe.setCodeObject("USER_WARRANT_FILE");
						childAe.setNameObject("Файл доверенности");
						childAe.setParentId(auditEvent.getParentId());
					}
					uploadFile(file, childAe, connection);
				}
			}
			if (localCon) {
				connection.commit();
			}
		} catch (Exception e) {
			if (localCon && connection != null) {
				try {
					connection.rollback();
				} catch (SQLException e2) {
				}
			}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	private void updateUserWarrant(UserWarrant warrant, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			String sql = "UPDATE USER_WARRANT\n" +
					"SET CODE = ?, PRINCIPAL = ?, BEGIN_DATE = ?, END_DATE = ?, READONLY = ?, CANCELED = ?\n" +
					"WHERE ID = ?";
			ps = connection.prepareStatement(sql);
			ps.setString(1, warrant.getCode());
			ps.setLong(2, warrant.getPrincipal());
			ps.setDate(3, new java.sql.Date(warrant.getBeginDate().getTime()));
			if (warrant.getEndDate() != null) {
				ps.setDate(4, new java.sql.Date(warrant.getEndDate().getTime()));
			} else {
				ps.setNull(4, OracleTypes.NULL);
			}
			ps.setInt(5, warrant.isReadonly() ? 1 : 0);
			ps.setInt(6, warrant.isCanceled() ? 1 : 0);
			ps.setLong(7, warrant.getId());

			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Updating an item failed, no rows affected.");

			if (auditEvent != null) {
				insertAuditEvent(auditEvent, connection);
			}

			if (warrant.isFilesChanged()) {
				List<AttachedFile> oldFiles = getFileListByLinkId(warrant.getId(), 4, connection);
				Map<Long, AttachedFile> map = new HashMap<Long, AttachedFile>();
				for (AttachedFile f : oldFiles) {
					map.put(f.getId(), f);
				}
				List<AttachedFile> newFiles = warrant.getFiles() == null ? new ArrayList<AttachedFile>() : warrant.getFiles();
				Date now = new Date();
				for (AttachedFile f : newFiles) {
					f.setLinkId(warrant.getId());
					f.setFileKind(4);
					f.setFileDate(now);
					if (f.getId() == null) {
						AuditEvent childAe = null;
						if (auditEvent != null) {
							childAe = new AuditEvent();
							childAe.setUserName(auditEvent.getUserName());
							childAe.setUserLocation(auditEvent.getUserLocation());
							childAe.setUserId(auditEvent.getUserId());
							childAe.setDateEvent(auditEvent.getDateEvent());
							childAe.setDateIn(auditEvent.getDateIn());
							childAe.setIdKindEvent(132L);
							childAe.setCodeObject("USER_WARRANT_FILE");
							childAe.setNameObject("Файл доверенности");
							childAe.setParentId(auditEvent.getParentId());
						}
						uploadFile(f, childAe, connection);
					} else if (map.containsKey(f.getId())) {
						map.remove(f.getId());
					}
				}
				for (Long fileId : map.keySet()) {
					AuditEvent childAe = null;
					if (auditEvent != null) {
						childAe = new AuditEvent();
						childAe.setUserName(auditEvent.getUserName());
						childAe.setUserLocation(auditEvent.getUserLocation());
						childAe.setUserId(auditEvent.getUserId());
						childAe.setDateEvent(auditEvent.getDateEvent());
						childAe.setDateIn(auditEvent.getDateIn());
						childAe.setIdKindEvent(133L);
						childAe.setCodeObject("USER_WARRANT_FILE");
						childAe.setNameObject("Файл доверенности");
						childAe.setParentId(auditEvent.getParentId());
						childAe.setRecId(fileId);
					}
					deleteFile(fileId, childAe, connection);
				}
			}
			if(localCon){
				connection.commit();
			}
		} catch (Exception e) {
			if (localCon && connection != null) {
				try {
					connection.rollback();
				} catch (SQLException ex) {
				}
			}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}

	private void deleteUserWarrant(Long warrantId, Connection connection, AuditEvent auditEvent) {
		boolean localCon = false;
		PreparedStatement ps = null;
		try {
			if (connection == null) {
				localCon = true;
				connection = getConnection();
				connection.setAutoCommit(false);
			}
			ps = connection.prepareStatement("DELETE FROM USER_WARRANT WHERE ID = ?");
			ps.setLong(1, warrantId);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) throw new SQLException("Deleting an item failed, no rows affected.");

			if (auditEvent != null) {
				insertAuditEvent(auditEvent, connection);
			}
			deleteAllFilesByLinkId(warrantId, 4, connection);
			if(localCon){
				connection.commit();
			}
		} catch (Exception e) {
			if (localCon && connection != null) {
				try {
					connection.rollback();
				} catch (SQLException ex) {
				}
			}
			throw new EJBException(e);
		} finally {
			DbUtil.closeConnection(localCon ? connection : null, ps);
		}
	}
}
