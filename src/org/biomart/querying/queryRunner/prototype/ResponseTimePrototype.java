package org.biomart.querying.queryRunner.prototype;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.Timer;
import org.biomart.objects.helpers.Rdbs;


public class ResponseTimePrototype {

	public static void main(String[] args) {
		try {
			new ResponseTimePrototype().run();
		} catch (TechnicalException e) {
			e.printStackTrace();
		} catch (FunctionalException e) {
			e.printStackTrace();
		}
	}

	public static final String BMTEST = "martdb.ensembl.org";
	public static final String USERNAME = "anonymous";
	public static final String PASSWORD = "";
	//public static final String TABLE_NAME = "hsapiens_gene_ensembl__exon_transcript__dm";
	public static final int TOTAL_RESULTS = 1000000000;
    
	private Rdbs rdbs = null;
	private String connectionString = null;
	private Connection connection = null;
	private ResultSet resultSet = null;
	
	private void run() throws FunctionalException, TechnicalException {
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		
			System.out.println(MyUtils.DASH_LINE);
		    this.rdbs = Rdbs.MYSQL;
			this.connectionString = "jdbc:mysql://" + BMTEST + ":" + "5316" + "/" + "ensembl_mart_55";
			System.out.println("Connecting to " + rdbs.name());
			this.connection = DriverManager.getConnection(connectionString, USERNAME, PASSWORD);
			String selectStatement = 
				//"select cds_length from" + " " + TABLE_NAME;
				" select hsapiens_gene_ensembl__transcript__main.transcript_id_1064_key, " +
				"hsapiens_gene_ensembl__exon_transcript__dm.cds_length from " + 
				"hsapiens_gene_ensembl__transcript__main, hsapiens_gene_ensembl__exon_transcript__dm where " +
				"hsapiens_gene_ensembl__transcript__main.transcript_id_1064_key = hsapiens_gene_ensembl__exon_transcript__dm.transcript_id_1064_key";
			System.out.println("Executing: " + selectStatement);
			System.out.println(MyUtils.DASH_LINE);
	
			List<String> result = noStreamming(selectStatement);
	        
			System.out.println(MyUtils.DASH_LINE);
	        
	        // Need to get a new connection for MySql
	        connection.close();
			connection = DriverManager.getConnection(connectionString, USERNAME, PASSWORD);
	
			List<String> result2 = streamming(selectStatement);
	        
	        MyUtils.checkStatusProgram(result.equals(result2));
	    
			connection.close();
			System.out.println(MyUtils.DASH_LINE);
		} catch (InstantiationException e) {
			throw new TechnicalException(e);
		} catch (IllegalAccessException e) {
			throw new TechnicalException(e);
		} catch (ClassNotFoundException e) {
			throw new TechnicalException(e);
		} catch (SQLException e) {
			throw new TechnicalException(e);
		}
	}

	private List<String> noStreamming(String selectStatement) throws SQLException, FunctionalException {
		// Streaming
		PreparedStatement preparedStatement2 = connection.prepareStatement(selectStatement);
		preparedStatement2.setFetchSize(0);
        
        Timer timerNoStreamming = new Timer();
		Timer timerNoStreammingFirstResult = new Timer();
        timerNoStreamming.startTimer();
        timerNoStreammingFirstResult.startTimer();
        
        Runtime.getRuntime().gc();
        resultSet = preparedStatement2.executeQuery();
        
        // Getting results
        System.out.println("Not streaming results");
        int row = 0;
        List<String> result2 = new ArrayList<String>();
        while(resultSet.next()) {
			if (row==0) {
				timerNoStreammingFirstResult.stopTimer();
			}
            String string2 = resultSet.getString(1);
            result2.add(string2);
            row++;
        }
        System.out.println("result2 = " + result2.size());       
        
        timerNoStreamming.stopTimer();
        System.out.println("timerNoStreammingFirstResult = " + timerNoStreammingFirstResult.getTimeEllapsedMs() + " ms");
        System.out.println("timerNoStreamming = " + timerNoStreamming.getTimeEllapsedMs() + " ms");
        System.out.println("Available memory: " + Runtime.getRuntime().freeMemory());
        
        // Cancelling query (they all have different ways of doing so)
        preparedStatement2.cancel();
    	System.out.println("Cancelled statement");
        preparedStatement2.close();
        System.out.println("Closed statement");
        resultSet.close();
        System.out.println("Closed resultSet");
		return result2;
	}

	private List<String> streamming(String selectStatement) throws SQLException, FunctionalException {
		// Streaming
        PreparedStatement preparedStatement1 = connection.prepareStatement(selectStatement, java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
		preparedStatement1.setFetchSize(Integer.MIN_VALUE);
        
		Timer timerStreamming = new Timer();
		Timer timerStreammingFirstResult = new Timer();
        timerStreamming.startTimer();
        timerStreammingFirstResult.startTimer();
        
        Runtime.getRuntime().gc();
        resultSet = preparedStatement1.executeQuery();
        
        // Getting results
        System.out.println("Streaming results");
        int row = 0;
        List<String> result = new ArrayList<String>();
		while(resultSet.next()) {
			if (row==0) {
				timerStreammingFirstResult.stopTimer();
			}
            String string = resultSet.getString(1);
            result.add(string);
            row++;
        }
        System.out.println("result = " + result.size());     
        
        timerStreamming.stopTimer();
        System.out.println("timerStreammingFirstResult = " + timerStreammingFirstResult.getTimeEllapsedMs() + " ms");
        System.out.println("timerStreamming = " + timerStreamming.getTimeEllapsedMs() + " ms");
        System.out.println("Available memory: " + Runtime.getRuntime().freeMemory());
        
        // Cancelling query (they all have different ways of doing so)
        preparedStatement1.cancel();
    	System.out.println("Cancelled statement");
        preparedStatement1.close();
        System.out.println("Closed statement");
        resultSet.close();
        System.out.println("Closed resultSet");
		return result;
	}
}
