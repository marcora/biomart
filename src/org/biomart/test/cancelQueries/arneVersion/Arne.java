package org.biomart.test.cancelQueries.arneVersion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.SqlUtils;
import org.biomart.objects.helpers.Rdbs;




public class Arne {
	public static void main(String[] args) {
		try {
			new Arne(Rdbs.MYSQL).run();
			new Arne(Rdbs.POSTGRESQL).run();
			//new Arne(Rdbs.ORACLE).run();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static final String BMTEST = "bm-test.res.oicr.on.ca";
	public static final String USERNAME = "martadmin";
	public static final String PASSWORD = "biomart";
	public static final String LOCALHOST = "localhost";
	public static final String TABLE_NAME = "kill_test";
	public static final int TOTAL_ROWS = 5000;
	public static final int TOTAL_RESULTS = 10;
    
	private Rdbs rdbs = null;
	private String fieldType = null;
	private String connectionString = null;
	private Connection connection = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	
	public Arne(Rdbs rdbs) throws SQLException {
		System.out.println(MyUtils.DASH_LINE);
		
	    this.rdbs = rdbs;
	    
		if (rdbs.isMySql()) {
			this.fieldType = "int";	// MySQL's
			this.connectionString = "jdbc:mysql://" + BMTEST + ":" + "3306" + "/" + "ac_cancel";
		} else if (rdbs.isPostgreSql()) {
			this.fieldType = "bigint";	// Postgres's
			this.connectionString = "jdbc:postgresql://" + LOCALHOST + ":" + "5432" + "/" + "ac_cancel";
		} else if (rdbs.isOracle()) {
			this.fieldType = "number";	// Oracle's
			this.connectionString = "jdbc:oracle:thin:@" + LOCALHOST + ":" + "1521" + ":" + "xe";
			DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());			// Need to load driver
		}
		
		// Connecting
		System.out.println("Connecting to " + rdbs.name());
		this.connection = DriverManager.getConnection(connectionString, USERNAME, PASSWORD);
		if (rdbs.isPostgreSql()) {
			// Need to explicitely set auto-commit to false (for streaming results)
			this.connection.setAutoCommit(false);
		}
	}
	
	private void run() throws SQLException {
        
		System.out.println("Trying to drop table before recreating it");
		preparedStatement = connection.prepareStatement(
				"drop table " + (rdbs.isMySql() ? " if exists " : "") + TABLE_NAME );
        try {
			preparedStatement.execute();
		} catch (SQLException e) {
			System.out.println("Table probably does not exists: " + e);
			// Need to rollback if Postgres
			if (rdbs.isPostgreSql()) {
				connection.rollback();
			}
		}
        
		System.out.println("(Re)Creating table");
		preparedStatement = connection.prepareStatement( "create table " + TABLE_NAME + "( num " + fieldType + ")" );
        preparedStatement.execute();
        preparedStatement = connection.prepareStatement( "insert into " + TABLE_NAME + "( num ) values( ? )" );
		for( int i=0; i<TOTAL_ROWS; i++ ) {
			preparedStatement.setInt( 1, i );
			preparedStatement.execute();
        }
		// Commit if Postgres
		if (rdbs.isPostgreSql()) {
			connection.commit();
		}
        
		// Preparing and Executing long statement
		System.out.println("Executing");
        String selectStatement = "select x1.num, x2.num, x3.num, x4.num from " +
		                " " + TABLE_NAME + " x1, " + TABLE_NAME + " x2, " + 
		                TABLE_NAME + " x3, " + TABLE_NAME + " x4";
		preparedStatement = connection.prepareStatement( selectStatement, java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
        if (rdbs.isMySql()) {
            preparedStatement.setFetchSize(Integer.MIN_VALUE);	// MySql's way of telling server to stream (only way)
		} else if (rdbs.isPostgreSql()) {
            preparedStatement.setFetchSize(1);	// Postgres' way of telling server to stream (could be another value)
		} /*else if (rdbs.isOracle()) {
			// Nothing to do for Oracle: streaming is on by default
		}*/
        resultSet = preparedStatement.executeQuery();
        
        // Getting results
        System.out.println("Getting results");
        int sum = 0;
        int i = TOTAL_RESULTS;
        while(resultSet.next() && (i>0)) {
            i--;
            System.out.println(resultSet.getInt(1) + ", " + resultSet.getInt(2) + 
            		", " + resultSet.getInt(3) + ", " + resultSet.getInt(4));	// caution! they don't take the results in the same order (sum solves the problem here)
            sum += (resultSet.getInt(1) + resultSet.getInt(2) + resultSet.getInt(3) + resultSet.getInt(4));
        }
        System.out.println("Final result = " + sum);       
        
        // Cancelling query (they all have different ways of doing so)
        MyUtils.pressKey("Cancel everything?");
        if (rdbs.isMySql()) {
        	preparedStatement.cancel();
        	System.out.println("Cancelled statement");
            preparedStatement.close();
            System.out.println("Closed statement");
            resultSet.close();
            System.out.println("Closed resultSet");
            
            // Need to get a new connection for MySql
            connection.close();
    		connection = DriverManager.getConnection(connectionString, USERNAME, PASSWORD);
        } else if (rdbs.isPostgreSql()) {
        	preparedStatement.cancel();
        	System.out.println("Cancelled statement");
            preparedStatement.close();
            System.out.println("Closed statement");
            resultSet.close();
            System.out.println("Closed resultSet");    		
        } else if (rdbs.isOracle()) {
        	resultSet.close();
        	System.out.println("Closed resultSet");
            preparedStatement.close();
            System.out.println("Closed statement");
            // Statement must not be cancelled with Oracle, otherwise get error: ORA-01013: user requested cancel of current operation
        }

        // Test that connection is working
        MyUtils.pressKey("Test if connection is still working?");
        preparedStatement = connection.prepareStatement("select count(*) from " + TABLE_NAME);
        resultSet = preparedStatement.executeQuery();
		System.out.println("Results: " + SqlUtils.getAllQueryResult(resultSet).toString());
		preparedStatement.close();
		
		// Try to drop the table (won't let you if query is not neatly cancelled)
        System.out.println("Dropping table to make sure query has been cancelled");
		preparedStatement = connection.prepareStatement( "drop table " + TABLE_NAME );
		preparedStatement.execute();
		if (rdbs.isPostgreSql()) {
			connection.commit();
		}
		resultSet.close();
		preparedStatement.close();
		
		// Try to select on dropped table (shouldn't let you if table has been dropped properly)
        System.out.print("Making sure table is gone: ");
		preparedStatement = connection.prepareStatement( "select * from " + TABLE_NAME );
		try {
			preparedStatement.execute();	// Must throw an exception
			
			// Shouldn't be there
			System.out.println("#############################################");	// So we don't miss it :)
			throw new IllegalStateException("");
		} catch (SQLException e) {
			System.out.println("everything is ok!");	// Exception throw as expected
		} catch (IllegalStateException e) {
			System.out.println("we've got a problem");	// Not the exception we hoped for...
			e.printStackTrace();
		}
		preparedStatement.close();
		
		connection.close();
		System.out.println();
		System.out.println();
	}
}
/*
----------------------------------------------------------------------------------------------------------
Connecting
Trying to drop table before recreating it
(Re)Creating table
Executing
Getting results
0, 0, 0, 0
1, 0, 0, 0
2, 0, 0, 0
3, 0, 0, 0
4, 0, 0, 0
5, 0, 0, 0
6, 0, 0, 0
7, 0, 0, 0
8, 0, 0, 0
9, 0, 0, 0
Final result = 45
Cancel everything?
Cancelled statement
Closed statement
Closed resultSet
Test if connection is still working?
Results: 
		count(*)	
0	|	5000	


Dropping table to make sure query has been cancelled
Making sure table is gone: everything is ok!

----------------------------------------------------------------------------------------------------------
Connecting
Trying to drop table before recreating it
Table probably does not exists: org.postgresql.util.PSQLException: ERROR: table "kill_test" does not exist
(Re)Creating table
Executing
Getting results
0, 0, 0, 0
0, 1, 0, 0
0, 2, 0, 0
0, 3, 0, 0
0, 4, 0, 0
0, 5, 0, 0
0, 6, 0, 0
0, 7, 0, 0
0, 8, 0, 0
0, 9, 0, 0
Final result = 45
Cancel everything?
Cancelled statement
Closed statement
Closed resultSet
Test if connection is still working?
Results: 
		count	
0	|	5000	


Dropping table to make sure query has been cancelled
Making sure table is gone: everything is ok!

----------------------------------------------------------------------------------------------------------
Connecting
Trying to drop table before recreating it
Table probably does not exists: java.sql.SQLException: ORA-00942: table or view does not exist

(Re)Creating table

Executing
Getting results
0, 0, 0, 0
0, 0, 0, 1
0, 0, 0, 2
0, 0, 0, 3
0, 0, 0, 4
0, 0, 0, 5
0, 0, 0, 6
0, 0, 0, 7
0, 0, 0, 8
0, 0, 0, 9
Final result = 45
Cancel everything?Closed resultSet
Closed statement
Test if connection is still working?
Results: 
		COUNT(*)	
0	|	5000	


Dropping table to make sure query has been cancelled
Making sure table is gone: everything is ok!
*/