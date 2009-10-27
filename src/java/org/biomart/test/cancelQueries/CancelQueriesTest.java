package org.biomart.test.cancelQueries;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.biomart.common.general.constants.MyConstants;
import org.biomart.common.general.utils.SqlUtils;
import org.biomart.objects.helpers.Rdbs;
import org.biomart.test.linkIndicesTest.program.DatabaseParameter;


public class CancelQueriesTest {
 
	public static void main(String[] args) {
		CancelQueriesTest cancelQueries = new CancelQueriesTest();
		try {
			cancelQueries.process();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public final static String BM = MyConstants.BMTEST_SERVER;
	public final static String databaseUser = "martadmin";
	public final static String databasePassword = "biomart";
	public final static String databaseName = "ac_cancel";
	public final static DatabaseParameter database = new DatabaseParameter(
			//Rdbs.POSGRESQL, BM, 5432, databaseUser, databasePassword, databaseName
			//Rdbs.ORACLE, "localhost", 1521, databaseUser, databasePassword, "XE"
			Rdbs.MYSQL, BM, 3306, databaseUser, databasePassword, databaseName
	);
	public static final String tableName = "long_query_test13";
	public static final Boolean cancel = true;
	public static final int totalRows =
		40;
		//10000;
	
	public static PreparedStatement psToCancel = null;
	public static ResultSet resultSetToClose = null;

	public void process() throws SQLException, InterruptedException {
		
		if (cancel) {
			new CancelQueriesThread().start();
		}
			
		// Connect and recreate database and table
		ThreadCommunication.println("Creating");
		Connection connection = SqlUtils.getConnection(database, false);
		Statement statement = SqlUtils.createStatement(connection, false);
		if (database.rdbs.isMySql()) {
			statement.executeUpdate("drop database if exists " + database.databaseName + ";");
			statement.executeUpdate("create database " + database.databaseName + ";");
			statement.executeUpdate("use " + database.databaseName);
			statement.executeUpdate("drop table if exists " + tableName + ";");
			statement.executeUpdate("create table " + tableName + "(num int);");			
		} else if (database.rdbs.isOracle()) {
			try {
				statement.executeUpdate("drop table " + tableName);
			} catch (SQLException e) {}
			statement.executeUpdate("create table " + tableName + "(num number)");
		} else if (database.rdbs.isPostgreSql()) {
			try {
				statement.executeUpdate("drop table " + tableName);
			} catch (SQLException e) {}
			statement.executeUpdate("create table " + tableName + "(num bigint)");
		}
		
		ThreadCommunication.println("Populating");
		psToCancel = SqlUtils.prepareStatement(connection, false, "insert into " + tableName + "(num) values (?)");
		for( int i=1; i<=totalRows; i++ ) {
			psToCancel.setInt(1, i);
			psToCancel.execute();
		}
		
		ThreadCommunication.println("Selecting");
		psToCancel = SqlUtils.prepareStatement(connection, true, 
				"select l1.num, l2.num, l3.num, l4.num from " + tableName + " l1, " + tableName + " l2, " + tableName + " l3, " + tableName + " l4");
		launched();
		try {
			resultSetToClose = psToCancel.executeQuery();
			ThreadCommunication.println("Getting results");
			while (resultSetToClose.next()) {
				System.out.println(resultSetToClose.getInt(1) + ", " + resultSetToClose.getInt(2) + ", " + 
						resultSetToClose.getInt(3) + ", " + resultSetToClose.getInt(4));
				Thread.sleep(500);	// To slow down results getting
			}
		} catch (SQLException e) {
			ThreadCommunication.println("SQLException caught (expected): " + e);
		}
		
		ThreadCommunication.println("Waiting");
		while (cancel && !isCancelled()) {
			Thread.sleep(500);
		}
		if (cancel) {
			ThreadCommunication.println("Cancelled properly");
		}
		
		if (database.rdbs.isMySql()) {
			ThreadCommunication.pressKey("Close preparedStatement?");
			psToCancel.close();
			ThreadCommunication.pressKey("Close resultSet?");
			resultSetToClose.close();
		} else if (database.rdbs.isPostgreSql()) {
			ThreadCommunication.pressKey("Close preparedStatement?");
			psToCancel.close();
			ThreadCommunication.pressKey("Close resultSet?");
			resultSetToClose.close();
		} else if (database.rdbs.isOracle()) {
			ThreadCommunication.pressKey("Cancel preparedStatement?");
			psToCancel.cancel();
			ThreadCommunication.pressKey("Close preparedStatement?");
			psToCancel.close();
		}
	
		ResultSet resultSet = statement.executeQuery("select count(*) from " + tableName);
		ThreadCommunication.println(SqlUtils.getAllQueryResult(resultSet).toString());
		resultSet.close();
		
		statement.executeUpdate("drop table " + tableName);
		try {
			statement.executeUpdate("select * from " + tableName);
			throw new Exception("Should have thrown an exception before arriving here");
		} catch (SQLException e) {
			ThreadCommunication.println("Excpected Excecption");
		} catch (Exception e) {
			ThreadCommunication.println("problem");
		}
		
		statement.close();
		connection.close();
		ThreadCommunication.println("End of thread");
	}

	private static boolean launched = false;
	private static boolean cancelled = false;
	public static synchronized void launched() {
		launched = true;
	}
	public static synchronized boolean isLaunched() {
		return launched;
	}
	public static synchronized void cancelPreparedStatement() throws SQLException {
		psToCancel.cancel();
		cancelled = true;
	}
	public static synchronized void closeResultSet() throws SQLException {
		resultSetToClose.close();
		cancelled = true;
	}
	public static synchronized boolean isCancelled() {
		return cancelled;
	}
}

/*
Have something like

create table long_query_test(num int);
create table long_query_test(num number/int/bigint);

Connection c;
PreparedStatement ps = c.prepareStatement( "insert into long_query_test( num ) values ( ? )" );
for( int i=1; i<10000; i++ ) {
  ps.setInteger( 1, i );
  ps.execute();
}

and then something like

ps = c.prepareStatement( "
select l1.num, l2.num, l3.num, l4.num from
 long_query_test l1,
 long_query_test l2,
 long_query_test l3,
 long_query_test l4;
" );
ResultSet rs = ps.execute()
int i=0;
while(( i < 10000) && rs.next() ) {
  sum += rs.getInt( 1) + rs.getInt( 2 ) ...
  i++;
}

ps.cancel()
rs.close()
ps.close()

and check the database after a short while if the query is removed :-)
Maybe it needs
  - Statement instead of prepared statement
  - the connection to close as well
 ??
For mysql it definately needs version>5.0, is that a problem?


Any takers?? I will do the postgresql test. needs to be done for oracle and mysql, too.

Arne
*/