/**
 * 
 */
package org.biomart.common.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.biomart.lib.BioMart.Root;

/**
 * @author junjun
 *
 */
public class DBConnectionUtils {

	/**
	 * This utils class provides a static method to create JDBC connection while 
	 * user specifies some JDBC connection parameters, such as host, username,
	 * port, password, DBType etc. It uses this to determine driverClass and 
	 * construct a JDBC URL, dynamically, and ultimately creates a JDBC connection.
	 */
	
	private static final Map DRIVER_MAP = new HashMap();
	
	static {
		/**
		 * Static variable to keep JDBC driver classes and URL formats.
		 * The keys are DBType ("mysql", "oracle", "postgresql"),
		 * the values are arrays with the following entries:
		 * The first entry in the array is the driver class name;
		 * and the second entry is a JDBC URL template in which
		 * the keywords <HOSTE>, <PORT> and <DATABASE> will later
		 * be substituted by the actual values.
		 */
		DBConnectionUtils.DRIVER_MAP.put("mysql", new String[] { "com.mysql.jdbc.Driver",
				"jdbc:mysql://<HOST>:<PORT>/<DATABASE>" });
		DBConnectionUtils.DRIVER_MAP.put("oracle", new String[] { "oracle.jdbc.driver.OracleDriver",
				"jdbc:oracle:thin:@<HOST>:<PORT>:<DATABASE>" });
		DBConnectionUtils.DRIVER_MAP.put("postgresql", new String[] { "org.postgresql.Driver",
				"jdbc:postgresql://<HOST>:<PORT>/<DATABASE>" });
	}

	/**
	 * @param dbType
	 * @param host
	 * @param port
	 * @param databaseName
	 * @param userName
	 * @param password
	 * @throws Exception
	 * @throws SQLException 
	 * @return JDBC Connection
	 */
	public static Connection getConnection (String dbType, String host, String port, 
			String databaseName, String userName, String password) throws Exception, SQLException {
		Connection conn = null;
		
		if (dbType == null || dbType.equals("") || 
				host == null || host.equals("") ||
				port == null || port.equals("") )
			throw new Exception("Parameters: dbType, host, port can NOT be null or empty String.");
		
		final String [] parts = (String []) DRIVER_MAP.get(dbType);
		
		if (parts == null || parts.length == 0)
			throw new Exception("Specified dbType is invalid or not supported. dbType must be one of these: "
					+ DRIVER_MAP.keySet());
		
		String driverClassName = parts[0];
		String urlTemplate = parts[1];
		
		System.out.println("driver class: " + driverClassName);
		System.out.println("URL template: " + urlTemplate);

		String jdbcURL = urlTemplate;
		jdbcURL = jdbcURL.replaceAll("<HOST>", host);
		jdbcURL = jdbcURL.replaceAll("<PORT>", port);
		if (databaseName == null) databaseName = "";
		jdbcURL = jdbcURL.replaceAll("<DATABASE>", databaseName);
		
		System.out.println("URL: "+jdbcURL);
		
		// load database driver
		try {
			Class.forName(driverClassName);
		} catch (final ClassNotFoundException e) {
			final SQLException e1 = new SQLException();
			e1.initCause(e);
			throw e1;
		}

		conn = DriverManager.getConnection(jdbcURL, userName, password);

		return conn;
	}
	
	private DBConnectionUtils() {
		// no instantiation for static class
	}
	
	/**
	 * main for testing purpose
	 * @param args
	 */
	public static void main (String[] args) {
		Connection conn = null;
		try {
			 conn = DBConnectionUtils.getConnection("mysql", "localhost", "3306", "jz_ensembl_mart_51_08", "", "");
			 //conn = DBConnectionUtils.getConnection("oracle", "localhost", "1521", "TESTDB", "","");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(conn);
		System.out.println("done");
	}

}
