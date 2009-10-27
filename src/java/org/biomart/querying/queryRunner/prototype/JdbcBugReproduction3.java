package org.biomart.querying.queryRunner.prototype;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.objects.helpers.DatabaseParameter;
import org.biomart.objects.helpers.Rdbs;


public class JdbcBugReproduction3 {

	public static void main(String[] args) throws Exception {
		
		DatabaseParameter databaseParameter = DATABASE_PARAMETER;
		databaseParameter.setDatabaseName("ac_query_runner_0_1");
		Connection connection = connect(databaseParameter);
		
		String query = 
			"select ac_query_runner_0_1.table0.l0, ac_query_runner_0_1.table0.r0, ac_query_runner_0_1.table0.pk0 from ac_query_runner_0_1.table0";
		//query += " limit 10000000000000000000";
		
		PreparedStatement preparedStatement = connection.prepareStatement(
				query
				, java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY
				);
		preparedStatement.setFetchSize(Integer.MIN_VALUE);

		ResultSet resultSet = preparedStatement.executeQuery();
		
		int row = 0;
		while (resultSet.next()) {
			System.out.print(".");
			row++;
			if (row%1000==0) {
				System.out.println();
			}
		}
		System.out.println("done, row = " + row);
	}

	public static final String DATABASE_HOST2 = "localhost";
	public static final Integer DATABASE_PORT2 = 3306;
	public static final String DATABASE_USER2 = "root";
	public static final String DATABASE_PASSWORD2 = "root";
	public static final DatabaseParameter DATABASE_PARAMETER = 
		new DatabaseParameter(Rdbs.MYSQL, DATABASE_HOST2, DATABASE_PORT2, DATABASE_USER2, DATABASE_PASSWORD2);
	
	public static Connection connect(DatabaseParameter databaseParameter) throws TechnicalException {
		
		Connection connection = null;
		try {		
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			String connectionString = null;
			if (databaseParameter.getRdbs().isMySql()) {
				connectionString = "jdbc:mysql://" + databaseParameter.getDatabaseHost() + ":" + 
				databaseParameter.getDatabasePort() + "/" + databaseParameter.getDatabaseName();
			} else if (databaseParameter.getRdbs().isPostgreSql()) {
				connectionString = "jdbc:postgresql://" + databaseParameter.getDatabaseHost() + ":" + 
				databaseParameter.getDatabasePort() + "/" + databaseParameter.getDatabaseName();
			} else if (databaseParameter.getRdbs().isOracle()) {
				connectionString = "jdbc:oracle:thin:@" + databaseParameter.getDatabaseHost() + ":" + 
				databaseParameter.getDatabasePort() + ":" + databaseParameter.getDatabaseName();
				DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());			// Need to load driver
			}		
			connection = DriverManager.getConnection(
					connectionString, databaseParameter.getDatabaseUser(), databaseParameter.getDatabasePassword());
		} catch (SQLException e) {
			throw new TechnicalException(e);
		} catch (InstantiationException e) {
			throw new TechnicalException(e);
		} catch (IllegalAccessException e) {
			throw new TechnicalException(e);
		} catch (ClassNotFoundException e) {
			throw new TechnicalException(e);
		}
		return connection;
	}
}
