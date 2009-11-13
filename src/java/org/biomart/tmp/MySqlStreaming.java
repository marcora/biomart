package org.biomart.tmp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * anthony@anthony-desktop:~/workspace/biomart-java/bin/java$ java -cp .:../../lib/mysql-connector-java-5.1.7-bin.jar org.biomart.tmp.MySqlStreaming ../../src/java/org/biomart/tmp/stream.props
 * anthony@anthony-desktop:~/Desktop$ java -cp MySqlStreaming.jar:/home/anthony/workspace/biomart-java/lib/mysql-connector-java-5.1.7-bin.jar org.biomart.tmp.MySqlStreaming /home/anthony/workspace/biomart-java/src/java/org/biomart/tmp/stream.props
 * @author anthony
 */
public class MySqlStreaming {
	public static void main(String[] args) {
		stream(args!=null && args.length>=1 ? args[0] : "src/java/org/biomart/tmp/stream.props");
	}
		
	public static void stream (String propertyFile) {
		try {
			String host = getProperty(propertyFile, "host");
			String port = getProperty(propertyFile, "port");
			String database = getProperty(propertyFile, "database");
			String username = getProperty(propertyFile, "username");
			String password = getProperty(propertyFile, "password");
			String query = getProperty(propertyFile, "query");
			String output = getProperty(propertyFile, "output");
			
			File file = new File (output);
			FileWriter fileWriter = new FileWriter (file);		
			BufferedWriter bufferedWriter = new BufferedWriter (fileWriter);
			
			stream(host, port, database, username, password, query, bufferedWriter);

			bufferedWriter.close();
			fileWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
			
	public static void stream (
			String host, String port, String database, String username, String password, String query, BufferedWriter bufferedWriter) 
			throws IOException {
			
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			
			String connectionString = "jdbc:mysql://" + host + ":" + port + "/" + database;
			System.out.println("Connecting");
			Connection connection = DriverManager.getConnection(connectionString, username, password);
			
			System.out.println("Executing");
			PreparedStatement preparedStatement = connection.prepareStatement(query, 
					java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
			preparedStatement.setFetchSize(Integer.MIN_VALUE);	// MySql's way of telling server to stream (only way)
			ResultSet resultSet = preparedStatement.executeQuery();
			
			
			int size = resultSet.getMetaData().getColumnCount();
			for (int i = 0; i < size; i++) {
				bufferedWriter.write(resultSet.getMetaData().getColumnName(i+1) + "\t");	// starts at 1
			}
			bufferedWriter.write("\n");
			while(resultSet.next()) {
				for (int i = 0; i < size; i++) {
					String value = resultSet.getString(i+1);
					bufferedWriter.write(value + "\t");	// starts at 1			
					System.out.print(value + "\t");
				}
				bufferedWriter.write("\n");
				System.out.println();
				bufferedWriter.flush();
			}
			
			System.out.println("Finished");
		} catch (SQLException e) {
			bufferedWriter.write("\n" + e.getMessage() + "\n");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			bufferedWriter.write("\n" + e.getMessage() + "\n");
			e.printStackTrace();
		} catch (InstantiationException e) {
			bufferedWriter.write("\n" + e.getMessage() + "\n");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			bufferedWriter.write("\n" + e.getMessage() + "\n");
			e.printStackTrace();
		}
	}
	public static String getProperty(String propertyFile, String propertyName) throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(propertyFile));
		return properties.getProperty(propertyName);
	}
}

/*
Anthony:
 
Here's the command for executing my SQL statement:
 
mysql -hmartdb.ensembl.org -P5316 -uanonymous snp_mart_55 < snp_mart.sql > snp_mart.out
 
SQL statements in snp_mart.sql:
 
SELECT main.name_2025 'key', main.name_1059 'chromosome', main.seq_region_start_2026 'chromosome_start', main.seq_region_start_2026
'chromosome_end', main.seq_region_strand_2026 'chromosome_strand', main.allele_string_2026 'refsnp_allele', main.ancestral_allele_2025
'tumor_genotype', snp_mart_55.hsapiens_snp__transcript_variation__dm.stable_id_1023 'gene_affected',
snp_mart_55.hsapiens_snp__transcript_variation__dm.stable_id_1066 'transcript_affected',
snp_mart_55.hsapiens_snp__transcript_variation__dm.consequence_type_2024 'consequence_type' FROM
snp_mart_55.hsapiens_snp__transcript_variation__dm, snp_mart_55.hsapiens_snp__variation_feature__main main WHERE
main.variation_feature_id_2026_key=snp_mart_55.hsapiens_snp__transcript_variation__dm.variation_feature_id_2026_key;
Thanks,
Christina
 
*/