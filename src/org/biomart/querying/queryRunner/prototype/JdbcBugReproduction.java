package org.biomart.querying.queryRunner.prototype;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JdbcBugReproduction {


	public static final String DATABASE_HOST = "localhost";
	public static final Integer DATABASE_PORT = 3306;
	public static final String DATABASE_USER = "root";
	public static final String DATABASE_PASSWORD = "root";
	public static final String DATABASE_NAME = "ac_query_runner_z";
	
	public static void main(String[] args) throws Exception {
		
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		String connectionString = "jdbc:mysql://" + DATABASE_HOST + ":" + 
		DATABASE_PORT + "/" + DATABASE_NAME;
		Connection connection = DriverManager.getConnection(
				connectionString, DATABASE_USER, DATABASE_PASSWORD);
		
		String query = 
			"select ac_query_runner_z.table0.l0, ac_query_runner_z.table0.r0, ac_query_runner_z.table0.pk0 " +
			"from ac_query_runner_z.table0"
			//+ " limit 10000000000000000000";
			//query += " limit 1000000000000000000";
		;
		
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
}
