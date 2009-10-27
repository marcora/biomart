package org.biomart.querying.queryRunner.testEnvironment;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.helpers.Rdbs;


public class DataScriptGenerator {

	public static final String GENERAL_OUPUT_FOLDER_PATH_AND_NAME = "/home/anthony/Desktop/script";
	public static final String SCRIPT_FILE_NAME_PREFIX = "table";
	public static final int TOTAL_TABLES = 10;
	public static final String SCRIPT_EXTENSION = ".sql";

	
	public static void main(String[] args) {
		DataScriptGenerator dataScriptGenerator = new DataScriptGenerator(Rdbs.POSTGRESQL);
		try {
			dataScriptGenerator.process();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public DataScriptGenerator(Rdbs rdbs) {
		this.stringBuffer = new StringBuffer();
		changeRdbs(rdbs);
	}

	private void changeRdbs(Rdbs rdbs) {
		this.rdbs = rdbs;
		this.stringDeclaration = rdbs.isMySql() ? "varchar(16)" : (rdbs.isPostgreSql() ? "text" : "varchar2(16)");
		this.intDeclaration = rdbs.isMySql() ? "int(1)" : (rdbs.isPostgreSql() ? "bigint" : "number");
	}

	private Rdbs rdbs = null;
	private StringBuffer stringBuffer = null;
	private String stringDeclaration = null;
	private String intDeclaration = null;
	private String databaseName = null; // changing
	private String databaseShortName = null;
	
	private static final String BOTTOM = "bottom";
	private static final String BOTTOM2 = "b";
	
	public void process() throws Exception {
		/*createFiles(0, 2, 1000);
		createFiles(10, 2, 1000);
		createFiles(50, 2, 1000);
		createFiles(100, 2, 1000);*/
		
		/*createFiles(0, 1, 1000000);
		createFiles(10, 1, 1000000);
		createFiles(50, 1, 1000000);
		createFiles(100, 1, 1000000);*/
		
		//createFiles(10, 1, 1000000, "bottom");
		//createFiles(50, 1, 1000000, "bottom");
		
		/*changeRdbs(Rdbs.MYSQL);
		createFiles(10, 1, 1000000, "bottom");
		createFiles(50, 1, 1000000, "bottom");
		changeRdbs(Rdbs.POSTGRESQL);
		createFiles(10, 1, 1000000, "bottom");
		createFiles(50, 1, 1000000, "bottom");*/
		changeRdbs(Rdbs.ORACLE);
		createFiles(10, 1, 1000000, BOTTOM2);
		createFiles(50, 1, 1000000, BOTTOM2);
		//createFiles(50, 1, 100000, "bottom");
		
		//createFiles(100, 1, 10000000);
		
		String scriptFileName = "script_" + this.rdbs + SCRIPT_EXTENSION;
		String scriptFilePathAndName = GENERAL_OUPUT_FOLDER_PATH_AND_NAME + MyUtils.FILE_SEPARATOR + scriptFileName;
		MyUtils.writeFile(scriptFilePathAndName, stringBuffer.toString());
		System.out.println("msl < " + scriptFilePathAndName);
	}

	private void createFiles(int overlapPercentage, int ratio, int distinctValues) throws IOException {
		createFiles(overlapPercentage, ratio, distinctValues, "");
	}
	private void createFiles(int overlapPercentage, int ratio, int distinctValues, String comment) throws IOException {
		String folderPathAndName = createFolder(overlapPercentage, ratio, distinctValues, comment);
		writeScript(folderPathAndName, overlapPercentage, ratio, distinctValues, comment);
		int primaryKey = 0;
		for (int tableNumber = 0; tableNumber < TOTAL_TABLES; tableNumber++) {
			primaryKey = writeTablePopulationFile(primaryKey, folderPathAndName, overlapPercentage, ratio, tableNumber, distinctValues, comment);
		}
	}

	
	private void writeScript(String folderPathAndName, int overlapPercentage, int ratio, int distinctValues, String comment) {
		this.databaseName = "ac_query_runner_" + overlapPercentage + "_" + ratio + "_" + distinctValues + "_" + comment;
		this.databaseShortName = "ac_" + "qr_" + overlapPercentage + "_" + ratio + "_" + distinctValues + "_" + comment;
		stringBuffer.append((this.rdbs.isOracle() ? "REMARK " : "/* ") + this.rdbs + (this.rdbs.isOracle() ? "" : " */") + MyUtils.LINE_SEPARATOR);
		if (!rdbs.isOracle()) {
			if (rdbs.isMySql()) {
				stringBuffer.append("drop database if exists " + databaseName + ";" + MyUtils.LINE_SEPARATOR);
			} else if (rdbs.isPostgreSql()) {
				stringBuffer.append("drop database " + databaseName + ";" + MyUtils.LINE_SEPARATOR);
			}
			stringBuffer.append("create database " + databaseName + ";" + MyUtils.LINE_SEPARATOR);
			if (rdbs.isMySql()) {
				stringBuffer.append("use " + databaseName + ";" + MyUtils.LINE_SEPARATOR);
			} else if (rdbs.isPostgreSql()) {
				stringBuffer.append("\\c " + databaseName + ";" + MyUtils.LINE_SEPARATOR);
			}
		}
		if (!rdbs.isMySql()) {
			for (int tableNumber = 0; tableNumber < DataScriptGenerator.TOTAL_TABLES; tableNumber++) {
				String tableName = generateTableName(tableNumber);
				stringBuffer.append("drop table " + tableName + ";" + MyUtils.LINE_SEPARATOR);
			}			
		}
		for (int tableNumber = 0; tableNumber < DataScriptGenerator.TOTAL_TABLES; tableNumber++) {
			String tableName = generateTableName(tableNumber);
			stringBuffer.append("create table " + tableName + "(l" + tableNumber + " " + stringDeclaration + ", " +
							"pk" + tableNumber + " " + intDeclaration + ", r" + tableNumber + " " + stringDeclaration + ");" + MyUtils.LINE_SEPARATOR);			
		}
		for (int tableNumber = 0; tableNumber < DataScriptGenerator.TOTAL_TABLES; tableNumber++) {
			String line = null;
			String scriptFileName = SCRIPT_FILE_NAME_PREFIX + tableNumber;
			String scriptFilePathAndName = folderPathAndName + scriptFileName + SCRIPT_EXTENSION;
			if (rdbs.isMySql()) {
				line = "load data local infile '" + scriptFilePathAndName + "' " +
										"into table " + scriptFileName + ";";
			} else if (rdbs.isPostgreSql()) {
				line = "\\i " + scriptFilePathAndName;
			} else if (rdbs.isOracle()) {
				line = "@" + scriptFilePathAndName;
			}
			stringBuffer.append(line + MyUtils.LINE_SEPARATOR);
			if (rdbs.isOracle()) {
				stringBuffer.append("commit;" + MyUtils.LINE_SEPARATOR);
			}
		}
		for (int tableNumber = 0; tableNumber < DataScriptGenerator.TOTAL_TABLES; tableNumber++) {
			String tableName = generateTableName(tableNumber);
			String leftIndexName = "li_" + tableName;
			String rightIndexName = "ri_" + tableName;
			stringBuffer.append("create unique index " + leftIndexName + " " +
					"on " + tableName + "(l" + tableNumber + ");" + MyUtils.LINE_SEPARATOR);
			if (rdbs.isOracle()) {
				stringBuffer.append("commit;" + MyUtils.LINE_SEPARATOR);
			}
			stringBuffer.append("create unique index " + rightIndexName + " " +
					"on " + tableName + "(r" + tableNumber + ");" + MyUtils.LINE_SEPARATOR);
			if (rdbs.isOracle()) {
				stringBuffer.append("commit;" + MyUtils.LINE_SEPARATOR);
			}
		}
	}

	private String generateTableName(int tableNumber) {
		return (this.rdbs.isOracle() ? this.databaseShortName + MyUtils.INFO_SEPARATOR + MyUtils.INFO_SEPARATOR : "") + 
		(this.rdbs.isOracle() ? "t" : "table") + tableNumber;
	}

	private int getOverlap(int percentage) {
		return percentage==0 ? -1 : (int)(1.0/((double)percentage/100));
	}

	private String createFolder(int overlap, int ratio, int distinctValues, String comment) throws IOException {
		String description = this.rdbs + MyUtils.INFO_SEPARATOR + overlap + MyUtils.INFO_SEPARATOR + ratio + MyUtils.INFO_SEPARATOR + distinctValues + MyUtils.INFO_SEPARATOR + comment;
		System.out.println("Starting " + description);
		String folderPathAndName = GENERAL_OUPUT_FOLDER_PATH_AND_NAME + MyUtils.FILE_SEPARATOR + 
								description + MyUtils.FILE_SEPARATOR;
		File folder = new File(folderPathAndName);
		folder.delete();	// To clear it
		folder.mkdirs();	// Start fresh
		return folderPathAndName;
	}
	private int writeTablePopulationFile(int primaryKey, String folderPathAndName, int overlapPercentage, 
			int ratio, int tableNumber, int distinctValues, String comment) throws IOException {
		
		boolean bottom = BOTTOM.equals(comment) || BOTTOM2.equals(comment);
		boolean scattered = !bottom;
		
		System.out.println(MyUtils.TAB_SEPARATOR + tableNumber);
		String filePathAndName = folderPathAndName + SCRIPT_FILE_NAME_PREFIX + tableNumber + SCRIPT_EXTENSION;
		FileWriter fileWriter = new FileWriter(new File(
				filePathAndName));
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		
		int rowNumber = 0;
		for (int distinctValue = 0; distinctValue < distinctValues; distinctValue++) {	// doesn't handle 1 table only well
			for (int i = 0; i < Math.pow(ratio, tableNumber); i++) {				
				rowNumber++;
			}
		}
		int totalRows = rowNumber;
		
		rowNumber = 0;
		int overlap = getOverlap(overlapPercentage);
		int totalMismatch = totalRows-((int)(totalRows*(overlapPercentage/100.0)));
		for (int distinctValue = 0; distinctValue < distinctValues; distinctValue++) {	// doesn't handle 1 table only well
			Boolean match = null;
			if (scattered) {
				match = (tableNumber==0 ? true : (overlap!=-1 ? distinctValue%overlap==0 : false));
			} else {
				match = (tableNumber==0 ? true : rowNumber>=totalMismatch);
			}
			for (int i = 0; i < Math.pow(ratio, tableNumber); i++) {				
				String leftValue = generateLeftValue(tableNumber, distinctValue, match);
				String rightValue = generateRightValue(tableNumber, distinctValue);
				String line = null;
				if (rdbs.isMySql()) {
					line = leftValue + MyUtils.TAB_SEPARATOR + primaryKey + MyUtils.TAB_SEPARATOR + rightValue;
				} else {
					line = "insert into " + generateTableName(tableNumber) + " values (" + 
					"'" + leftValue + "'" + "," + "'" + primaryKey + "'" + "," + "'" + rightValue + "'" + ");";
				}
				bufferedWriter.write(line + MyUtils.LINE_SEPARATOR);
				primaryKey++;
				rowNumber++;
			}
		}
		
		bufferedWriter.close();
		fileWriter.close();
		
		return primaryKey;
	}
	public static String generateLeftValue(Integer tableNumber, int distinctValue, boolean match) {
		return generateValue(true, tableNumber!=0 ? tableNumber-1 : null, tableNumber, distinctValue, match);
	}
	public static String generateRightValue(Integer tableNumber, int distinctValue) {
		return generateValue(false, tableNumber, tableNumber!=TOTAL_TABLES-1 ? tableNumber+1 : null, distinctValue, true);
	}
	public static String generateValue(boolean left, Integer leftTableNumber, Integer rightTableNumber, int distinctValue, boolean match) {
		String string = (leftTableNumber!=null ? leftTableNumber : "x") + MyUtils.INFO_SEPARATOR + 
		(rightTableNumber!=null ? rightTableNumber : "x") + MyUtils.INFO_SEPARATOR + (match ? "1" : "0") + MyUtils.INFO_SEPARATOR + distinctValue;
		return string;
	}
	
	/*	public static final Rdbs RDBS = Rdbs.MYSQL;
	public static final String DATABASE_HOST = "localhost";
	public static final Integer DATABASE_PORT = 3306;
	public static final String DATABASE_USER = "root";
	public static final String DATABASE_PASSWORD = "root";
	public static final DatabaseParameter DATABASE_PARAMETER = 
		new DatabaseParameter(RDBS, DATABASE_HOST, DATABASE_PORT, DATABASE_USER, DATABASE_PASSWORD);*/
	/*public Connection connect() throws TechnicalException {
		
		Connection connection = null;
		try {		
			String connectionString = null;
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			if (databaseParameter.getRdbs().isMySql()) {
				connectionString = "jdbc:mysql://" + databaseParameter.getDatabaseHost() + ":" + 
				databaseParameter.getDatabasePort() + "/" + databaseParameter.getDatabaseName();
			} else if (databaseParameter.getRdbs().isPosgreSql()) {
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
	}*/
	
	/*private void create100_1() throws IOException {
		int overlap = 1;
		int ratio = 1;
		
		int distinctValues = 1000000;
		String folderPathAndName = createFolder(overlap, ratio);
		for (int tableNumber = 0; tableNumber < TOTAL_TABLES; tableNumber++) {
			writeTablePopulationFile(folderPathAndName, overlap, ratio, tableNumber, distinctValues);
		}
	}
	
	private void create100_4() throws IOException {
		int overlap = 1;
		int ratio = 4;
		
		int distinctValues = 100;
		String folderPathAndName = createFolder(overlap, ratio);
		for (int tableNumber = 0; tableNumber < TOTAL_TABLES; tableNumber++) {
			writeTablePopulationFile(folderPathAndName, overlap, ratio, tableNumber, distinctValues);
		}
	}
	
	private void create50_4() throws IOException {
		int overlap = getOverlap(50);
		int ratio = 4;
		
		int distinctValues = 100;
		String folderPathAndName = createFolder(overlap, ratio);
		for (int tableNumber = 0; tableNumber < TOTAL_TABLES; tableNumber++) {
			writeTablePopulationFile(folderPathAndName, overlap, ratio, tableNumber, distinctValues);
		}
	}*/
	
	
	/*Connection connection = connect();
	PreparedStatement preparedStatement = connection.prepareStatement(
			"create database ac_db1", java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
	preparedStatement.setFetchSize(Integer.MIN_VALUE);
    
	Timer queryTimer = new Timer();
	queryTimer.startTimer();
	
	Timer queryLaunchTimer = new Timer();
	queryLaunchTimer.startTimer();
	
	ResultSet resultSet = preparedStatement.executeQuery();
	
	create database ac_db1;
	use ac_db1;
	create table gene__gene__main (gene_id_key int(1) not null, gene_name varchar(16));
	insert into gene__gene__main values (1, "gene 1");
	
	"load data local infile '" + filePath + tableName + ".txt' into table " + tableName + ";";*/
}
