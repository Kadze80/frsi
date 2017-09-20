/*
package ejb;

import oracle.jdbc.pool.OracleDataSource;
import org.junit.*;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.*;

public class PersistenceTest {

	private static EJBContainer ejbContainer;
	private static Context context;

	private static final String url = "jdbc:oracle:thin:@10.10.11.23:1521:FRSI";
	private static final String user = "frsi";
	private static final String password = "frsi";

	@BeforeClass
	public static void beforeAllTests() {
		System.out.println("*** Before all tests");

		// Create and run an EJB container
		Properties properties = new Properties();
		properties.put(EJBContainer.MODULES, new File("target/classes"));
		ejbContainer = EJBContainer.createEJBContainer(properties);
		if (ejbContainer == null) System.out.println("*** EJB Container == NULL");
		context = ejbContainer.getContext();

		// Setup the JNDI context and the datasource
		try {
			System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
			System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
			InitialContext ic = new InitialContext();
			ic.createSubcontext("java:");
			ic.createSubcontext("java:/comp");
			ic.createSubcontext("java:/comp/env");
			ic.createSubcontext("java:/comp/env/jdbc");

			OracleDataSource ds = new OracleDataSource();
			ds.setURL(url);
			ds.setUser(user);
			ds.setPassword(password);
			ic.bind("java:/comp/env/jdbc/FrsiPool", ds);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void afterAllTests() {
		//ejbContainer.close();
		System.out.println("*** After all tests");
	}

	@Before
	@Ignore
	public void beforeEachTest() {
		System.out.println("*** Before each test");
	}

	@After
	@Ignore
	public void afterEachTest() {
		System.out.println("*** After each test");
	}

	@Test
	@Ignore
	public void test() {
		assertTrue(true);
	}

	@Test
	@Ignore
	public void testPersistenceEjb() {
		try {
			Persistence persistence = (Persistence) context.lookup("java:global/classes/PersistenceBean!ejb.PersistenceRemote");
			if (persistence == null) System.out.println("*** PERS = NULL");
			String s = persistence.getTestMessage();
			System.out.println(s);
			assertNotNull(persistence);
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

}
*/