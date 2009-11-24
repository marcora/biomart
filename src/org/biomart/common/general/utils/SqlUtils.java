package org.biomart.common.general.utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.biomart.objects.helpers.Rdbs;
import org.biomart.test.linkIndicesTest.program.DatabaseParameter;



public class SqlUtils {
	
	public static final int DISPLAY_QUERY_BEGINNING_THRESHOLD_LENGTH = 500;	// In characters
	public static final int DISPLAY_QUERY_END_THRESHOLD_LENGTH = 50;	// In characters

	public static final String ORACLE_MONITORING_QUERY_BASE = "SELECT SYS.V_$SQL.SQL_ID, SYS.V_$SESSION.SQL_ID, SYS.V_$SQL.SQL_TEXT, SYS.V_$SQL.EXECUTIONS, SYS.V_$SQL.CHILD_ADDRESS, " +
	"SYS.V_$SQL.ROWS_PROCESSED, SYS.V_$SQL.BUFFER_GETS, SYS.V_$SQL.MODULE, SYS.V_$SQL.LOADS, SYS.V_$SQL.FIRST_LOAD_TIME, SYS.V_$SQL.LAST_ACTIVE_TIME, " +
	"SYS.V_$SESSION.SID, SYS.V_$SESSION.SADDR, SYS.V_$SESSION.SQL_CHILD_NUMBER, SYS.V_$SESSION.PREV_SQL_ADDR, SYS.V_$SESSION.LOGON_TIME, SYS.V_$SESSION.USER#, " +
	"SYS.V_$SESSION.STATUS, SYS.V_$SESSION.PROCESS, SYS.V_$SESSION.PROGRAM, SYS.V_$SESSION.BLOCKING_SESSION_STATUS, SYS.V_$SESSION.EVENT  " +
	"FROM SYS.V_$SQL, SYS.V_$SESSION WHERE SYS.V_$SQL.SQL_ID = SYS.V_$SESSION.SQL_ID(+) ";	// Must add ";" if finished
	
	// Defaults
	public DatabaseParameter database = null;
	
	public SqlUtils(DatabaseParameter database) {
		this.database = database;
	}
	
public Connection conn = null;
public Statement stmt = null;
	boolean stream = false;
	Rdbs rdbs = null;
	public ResultSet rs = null;

	public void connect() throws SQLException {
		connect(true, false);
	}
	public void connect(boolean showConnection, boolean stream) throws SQLException {
		this.stream = stream;
        conn = getConnection(this.database, showConnection);
        stmt = createStatement(conn, stream);
	}
	
	public static Connection getConnection(DatabaseParameter database, boolean showConnection) throws SQLException {
		Connection connection = null;
		String connectionString = null;
		if (database.rdbs.isMySql()) {
        	connectionString = "jdbc:mysql://" + database.databaseHost + ":" +
        		database.databasePort + "/" + database.databaseName + "?" + "user=" + 
        		database.databaseUser + "&password=" + database.databasePassword;

            if (showConnection) {
            	System.out.println("Getting connection: " + connectionString);
            }
            connection = DriverManager.getConnection(connectionString);
        } else if (database.rdbs.isOracle()) {
        	
        	/*DriverManager.getConnection
            ("jdbc:oracle:thin:@localhost:1521:xe", "acros", "biomart");
                            // @machineName:port:SID,   userid,  password*/
        	
        	DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());

        	
        	connectionString = "jdbc:oracle:thin:@" + database.databaseHost + ":" + database.databasePort + ":" + database.databaseName;/* + "?" + "user=" + 
	        (databaseUser==null ? this.databaseUser : databaseUser) + "&password=" + 
	        (databasePassword==null ? this.databasePassword : databasePassword);*/
        	

            if (showConnection) {
            	System.out.println("Getting connection: " + connectionString);
            }
            connection = DriverManager.getConnection(connectionString, database.databaseUser, database.databasePassword);
        } else if (database.rdbs.isPostgreSql()) {
        	/*
    		pgadminIII shows the query is gone after this is finished!
    		*/
        	
        	connectionString = "jdbc:postgresql://" + database.databaseHost + ":" + database.databasePort + "/" + database.databaseName;/* + "?" + "user=" + 
	        (databaseUser==null ? this.databaseUser : databaseUser) + "&password=" + 
	        (databasePassword==null ? this.databasePassword : databasePassword);*/
        	

            if (showConnection) {
            	System.out.println("Getting connection: " + connectionString);
            }
            connection = DriverManager.getConnection(connectionString, database.databaseUser, database.databasePassword);
        }
		return connection;
	}
	
	public static Statement createStatement(Connection connection, boolean stream) throws SQLException {
		Statement statement = null;
		if (stream) {
			statement = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
	                java.sql.ResultSet.CONCUR_READ_ONLY);
			statement.setFetchSize(Integer.MIN_VALUE);
        } else {
        	statement = connection.createStatement();
        }
		return statement;
	}
	
	public static PreparedStatement prepareStatement(Connection connection, boolean stream, String query) throws SQLException {
		PreparedStatement preparedStatement = null;
		if (stream) {
			preparedStatement = connection.prepareStatement(query, java.sql.ResultSet.TYPE_FORWARD_ONLY,
	                java.sql.ResultSet.CONCUR_READ_ONLY);
			preparedStatement.setFetchSize(Integer.MIN_VALUE);
        } else {
        	preparedStatement = connection.prepareStatement(query);
        }
		return preparedStatement;
	}
	
	public void changeDatabase(String database) throws SQLException {
		runExecuteQuery("use " + database + ";");		
	}
	
	public void runExecuteQuery(StringBuffer query) throws SQLException {
		runExecuteQuery(query.toString());
	}
	
	public void runExecuteQuery(String query) throws SQLException {
		try {
			this.rs=stmt.executeQuery(query);
		} catch (SQLException e) {
			reconnect(query, e);
			this.rs=stmt.executeQuery(query);
		} catch (NullPointerException e) {
			reconnect(query, e);
			this.rs=stmt.executeQuery(query);
		}
	}
	
	public Integer runUpdateQuery(String query) throws SQLException {
		Integer n = null;
		try {
			n = stmt.executeUpdate(query);
		} catch (SQLException e) {
			reconnect(query, e);
			n = stmt.executeUpdate(query);
		} catch (NullPointerException e) {
			reconnect(query, e);
			n = stmt.executeUpdate(query);
		}
		return n;
	}
	public void closeResultSet() throws SQLException {
		if (null!=rs) {
			rs.close();
		}
	}
	
	public static StringBuffer getAllResult(DatabaseParameter database, String query) throws SQLException {
		Connection connection = getConnection(database, false);
		Statement statement = createStatement(connection, false);
		ResultSet resultSet = statement.executeQuery(query);
		return getAllQueryResult(resultSet);
	}
	
	public static StringBuffer getAllQueryResult(ResultSet rs) throws SQLException {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(MyUtils.LINE_SEPARATOR);
		stringBuffer.append(MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR);
		for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
			stringBuffer.append(rs.getMetaData().getColumnName(i+1) + MyUtils.TAB_SEPARATOR);
		}
		stringBuffer.append(MyUtils.LINE_SEPARATOR);
		int rowCount = 0;
		while (rs.next()) {
			stringBuffer.append(rowCount + /*rs.getRow() + */MyUtils.TAB_SEPARATOR + "|" + MyUtils.TAB_SEPARATOR);
			for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
				stringBuffer.append(rs.getString(i+1) + MyUtils.TAB_SEPARATOR);
			}
			stringBuffer.append(MyUtils.LINE_SEPARATOR);
			rowCount++;
		}
		stringBuffer.append(MyUtils.LINE_SEPARATOR);
		return stringBuffer;
	}
	
	public void reconnect(String query, Exception e) throws SQLException {
		System.out.println("SQLException caught for query " + displayQuery(this, query) + 
				" in runExecuteQuery, message = " + e.getMessage());
		System.out.println("Getting a new connection");
		connect(false, this.stream);
	}
	
	public int countRowsAfterQuery() throws SQLException {
		this.rs.last(); 
		int rowCount = this.rs.getRow();
		this.rs.beforeFirst();
		return rowCount;
	}
	
	public int countRows(String tableName) throws SQLException {
		runExecuteQuery("select count(*) as c from " + tableName);
		this.rs.first(); 
		int rowCount = this.rs.getInt("c");
		this.rs.beforeFirst();
		return rowCount;
	}
	
	public boolean tableExists(String databaseName, String tableName) throws SQLException {
		runExecuteQuery("show tables;");
		rs.beforeFirst();
		while (rs.next()) {			
			if (tableName.equals(rs.getString("Tables_in_" + databaseName))) {	// use xxx;show tables; display tables in field: "Tables_in_xxx"
				rs.beforeFirst();
				return true;
			}
		}
		rs.beforeFirst();
		return false;
	}
	
	public boolean indexExists(String databaseName, String tableName, String indexName) throws SQLException {
		runExecuteQuery("show indexes from " + databaseName + "." + tableName + ";");
		rs.beforeFirst();
		while (rs.next()) {
			if (indexName.equals(rs.getString("Key_name"))) {
				rs.beforeFirst();
				return true;
			}
		}
		rs.beforeFirst();
		return false;
	}
	
	public boolean isEmptyTable(String databaseName, String tableName) throws SQLException {
		return tableExists(databaseName, tableName) && 0==countRows(tableName);
	}
	
	public boolean isNotEmptyTable(String databaseName, String tableName) throws SQLException {
		return tableExists(databaseName, tableName) && 0<countRows(tableName);
	}
		
	public boolean existsAndContains(String databaseName, String tableName, int rows) throws SQLException {
		return tableExists(databaseName, tableName) && rows==countRows(tableName);
	}
	
	public void disconnect(){
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException sqlEx) {
                   System.out.println("SQLException: " + sqlEx.getMessage());
            }
            rs = null;
        }
        
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException sqlEx) {
                   System.out.println("SQLException: " + sqlEx.getMessage());
            }

            stmt = null;
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException sqlEx) {
                   // Ignore
            }

            conn = null;
        }
	}

	public String toShortString() {
		return "[" + database.databaseHost + "/" + database.databaseName + "]";
	}
	
	public static StringBuffer displayQuery(SqlUtils mySqlUtils, String query) {
		return displayQuery(mySqlUtils, new StringBuffer(query));
	}
	public static StringBuffer displayQuery(SqlUtils mySqlUtils, StringBuffer query) {
		if (query.length()<(DISPLAY_QUERY_BEGINNING_THRESHOLD_LENGTH+DISPLAY_QUERY_END_THRESHOLD_LENGTH)) {
			StringBuffer queryTmp = new StringBuffer(query);
			queryTmp.append(" " + mySqlUtils.toShortString());
			return queryTmp;
		} else {
			StringBuffer shortQuery = new StringBuffer(query.substring(0, DISPLAY_QUERY_BEGINNING_THRESHOLD_LENGTH));
			shortQuery.append("[...]");
			shortQuery.append(query.substring(query.length()-DISPLAY_QUERY_END_THRESHOLD_LENGTH, query.length()));
			return shortQuery;
		}
	}

	public static void main(String[] args) {
		SqlUtils mySqlUtils1 = null;
		SqlUtils mySqlUtils2 = null;
		try {	
				//mySqlUtils1 = new SqlUtils(Rdbs.MYSQL, "bm-test.res.oicr.on.ca", 3306, "martadmin", "biomart", "ac_test_tmp_0");
				mySqlUtils1.connect();
				mySqlUtils1.runExecuteQuery(
						"select main0_id_key from ds0__main0__main limit 10;");
				mySqlUtils1.rs.beforeFirst();
				while (mySqlUtils1.rs.next()) {
					System.out.println(mySqlUtils1.rs.getString("main0_id_key"));
				}
				mySqlUtils1.disconnect();
				
				//mySqlUtils2 = new SqlUtils(Rdbs.MYSQL, "martdb.ensembl.org", 5316, "anonymous", "", "ensembl_mart_53");
				mySqlUtils2.connect();
				mySqlUtils2.runExecuteQuery(
						"select stable_id_1023, seq_region_start_1020 from hsapiens_gene_ensembl__gene__main " +
						"where stable_id_1023 in ('ENSG00000183044','ENSG00000183044','ENSG00000165029','ENSG00000107331'," +
						"'ENSG00000107331','ENSG00000167972','ENSG00000131269','ENSG00000204574');");
				mySqlUtils2.rs.beforeFirst();
				while (mySqlUtils2.rs.next()) {
					System.out.println(mySqlUtils2.rs.getString("seq_region_start_1020"));
				}
				mySqlUtils2.disconnect();
				
		} catch (SQLException ex) {
	    	ex.printStackTrace();
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("VendorError: " + ex.getErrorCode());
	    } finally {
	    	System.out.println("disconnecting");
	    	if (null!=mySqlUtils1) {
	    		mySqlUtils1.disconnect();
	    	}
	    	if (null!=mySqlUtils2) {
	    		mySqlUtils2.disconnect();
	    	}
	    }
	}
}
