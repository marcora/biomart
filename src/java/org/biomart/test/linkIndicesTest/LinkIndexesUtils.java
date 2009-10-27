package org.biomart.test.linkIndicesTest;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.test.linkIndicesTest.program.DatabaseSchema;
import org.biomart.test.linkIndicesTest.program.LinkIndexesParameters;





public class LinkIndexesUtils {
	
	public static final int PROGRESS_DOT_FREQUENCY = 10000;
	
	public static boolean isValidValue(String targetFieldRightValue) {
		return targetFieldRightValue!=null && !MyUtils.isEmpty(targetFieldRightValue) && !targetFieldRightValue.equals("null");
	}
	
	public static String getCreateDbIndexQuery (String dbIndexName,  String tableToDbIndex,  String  fieldToDbIndex) {
		return "create unique index " + dbIndexName + " on " + tableToDbIndex + " (" + fieldToDbIndex + ");";
	}
	
	public static String getCreateResultTableQuery (String resultTableName) {
		return "create table " + resultTableName + "(main_id_key int(1), main1_desc varchar(64));";
	}
	
	public static String getDropTableIfExistsQuery (String tableName) {
		return "drop table if exists " + tableName + ";";
	}
	
	public static String getDropDbIndexIfExistsQuery (String dbIndexName,  String tableToDbIndex,  String  fieldToDbIndex) {
		return "";
	}
	
	public static String getLoadDataLocalInfileQuery(String filePath,  String tableName) {
		return "load data local infile '" + filePath + tableName + ".txt' into table " + tableName + ";";
	}
	public static String getLoadDataLocalInfileQuery(String filePath,  String tableName, Boolean differentName) {
		return "load data local infile '" + filePath + "' into table " + tableName + ";";
	}
	
	public static String getUseDatabaseQuery(String databaseName) {
		return "use " + databaseName + ";";
	}
	public static String getUseDatabaseQuery(String databaseNameBase, int datasetNumber) {
		return "use " + getDatabaseName(databaseNameBase, datasetNumber) + ";";
	}
	
	public static String getLinkIndexTableName(String linkIndexDatabaseName, int datasetNumber1, int datasetNumber2) {
		return linkIndexDatabaseName + getDatasetIndexCombination(datasetNumber1, datasetNumber2) + "_link_index";
	}
	
	private static String getDatasetIndexCombination(int datasetNumber1, int datasetNumber2) {
		return "_" + datasetNumber1 + "_" + datasetNumber2;
	}
	
	public static String getSelectAllIDsQuery(String tableToIndex, String fieldToIndex) {
		return "select " + fieldToIndex + " from " + tableToIndex;
	}
	
	public static String getCreateLinkIndexTableQuery (String linkIndexTableName, String linkIndexTableKey) {
return "create table " + linkIndexTableName + " (" + linkIndexTableKey + " varchar(256));";
	}
	
	public static String getInsertIntoLinkIndexTableValuesQuery (String linkIndexTableName, int id) {
		return "insert into " + linkIndexTableName + " values(" + id + ");";
	}
	
	public static StringBuffer buildWhereInClause(String field, Collection<String> idCollection) throws FunctionalException {
		StringBuffer stringBuffer = new StringBuffer();
		if (null==idCollection || idCollection.isEmpty()) {
			return stringBuffer;
		}
		stringBuffer.append(" where " + field + " in (");
		stringBuffer.append(buildInList(idCollection));
		stringBuffer.append(") ");
		return stringBuffer;
	}
	
	public static StringBuffer buildLimitClause(int batchIndex, int batchSize) throws FunctionalException {
		return new StringBuffer(" limit " + batchIndex + "," + batchSize + " ");
	}
	
	public static StringBuffer getLinkIndexQuery(boolean distinct, String field, String table, StringBuffer filteringClause) {
		StringBuffer query = new StringBuffer();		
		query.append("select " + (distinct ? "distinct " : "") + field + " from " + table + " " + filteringClause + ";");
		return query;
	}
	
	public static StringBuffer getQuery(boolean distinct, String fieldLeft, String fieldRight, String[] fieldNames, 
			String table, StringBuffer filteringClause) {
				
		StringBuffer fieldList = MyUtils.arrayToStringBuffer(fieldNames);
		if (null!=fieldLeft && !MyUtils.contains(fieldNames, fieldLeft)) {			
			fieldList.append((fieldList.length()>0 ? ", " : "") + fieldLeft);
		}
		if (null!=fieldRight && !MyUtils.contains(fieldNames, fieldRight)) {
			fieldList.append((fieldList.length()>0 ? ", " : "") + fieldRight);
		}
		StringBuffer query = new StringBuffer();		
		query.append("select " + (distinct ? "distinct " : "") + fieldList + " from " + table + " " + filteringClause + ";");
		return query;
	}

	public static void useLocalDatabase(LinkIndexesParameters parameters) throws SQLException {
		String query = LinkIndexesUtils.getUseDatabaseQuery(parameters.localDatabase.dbParam.databaseName);
		if (parameters.displaySuperDebug) {System.out.println("query = " + query + 
				" " + parameters.localDatabase.sqlUtils.toShortString());}
		parameters.localDatabase.sqlUtils.runUpdateQuery(query);
	}
	
	public static void useDatabase(DatabaseSchema databaseSchema, boolean displaySuperDebug) throws SQLException {
		String query = LinkIndexesUtils.getUseDatabaseQuery(databaseSchema.dbParam.databaseName);
		if (displaySuperDebug) {System.out.println("query = " + query + 
				" " + databaseSchema.sqlUtils.toShortString());}
		databaseSchema.sqlUtils.runUpdateQuery(query);
	}
	
	// ===================================================================================================
	
	public static final String MAIN_TABLE_TYPE = "main";
	public static final String DIMENSION_TABLE_TYPE = "dm";
	public static final String TABLE_LEVEL_SEPARATOR = "__";
	public static final String OUTPUT_FOLDER_NAME = "LinkIndicesScript2";
	
	public static String getDatabaseName(String databaseNameBase, int datasetNumber) {
		return databaseNameBase + "_" + datasetNumber;
	}
	
	public static String getResultTableName(String databaseNameBase, boolean index) {
		return databaseNameBase + "_result_" + (index ? "with" : "without") + "_index";
	}
	
	public static String getMainTableName(int datasetNumber) {
		return getDatasetName(datasetNumber) + TABLE_LEVEL_SEPARATOR + 
							getMainTableBaseName(datasetNumber) + TABLE_LEVEL_SEPARATOR + MAIN_TABLE_TYPE;
	}
	
	public static String getDatasetName(int datasetNumber) {
		return "ds" + datasetNumber;
	}
	
	public static String getMainTableBaseName(int datasetNumber) {
		return "main" + datasetNumber;
	}
	
	public static String getDimensionDesc(String dimensionDescField, int mainRow, int dimensionRowForMainRow, int mainID, int dimensionID) {
		return "'" + dimensionDescField + "_"  + mainRow + "-" + dimensionRowForMainRow + "_" + + mainID + "-" + dimensionID + "'";
	}
	
	public static String getDimensionTableBaseName(int datasetNumber, char dimensionLetter) {
		return "dm" + datasetNumber + dimensionLetter;
	}

	public static String getDimensionIdField(Character dimensionLetter, int datasetNumber) {
		return "dm" + datasetNumber + dimensionLetter + "_id";
	}

	public static Character getDimensionLetter(int dimensionNumber) {
		return (char)((int)'a'+dimensionNumber);
	}

	public static String getDimensionDescField(Character dimensionLetter, int datasetNumber) {
		return "dm" + datasetNumber + dimensionLetter + "_desc";
	}

	public static String getMainDescField(int datasetNumber) {
		return "main" + datasetNumber + "_desc";
	}

	public static String getMainIdField(int datasetNumber) {
		return "main" + datasetNumber + "_id_key";
	}
	
	public static String getOutputFolderPathAndName(String host, String subFolderName) {
		return MyUtils.OUTPUT_FILES_PATH + LinkIndexesUtils.OUTPUT_FOLDER_NAME + MyUtils.INFO_SEPARATOR + host + 
		MyUtils.FILE_SEPARATOR + subFolderName + MyUtils.FILE_SEPARATOR;
	}

	public static String getDimensionTableName(Character dimensionLetter, int datasetNumber) {
		String string = LinkIndexesUtils.getDatasetName(datasetNumber) + LinkIndexesUtils.TABLE_LEVEL_SEPARATOR + 
		LinkIndexesUtils.getDimensionTableBaseName(datasetNumber, dimensionLetter) + LinkIndexesUtils.TABLE_LEVEL_SEPARATOR + LinkIndexesUtils.DIMENSION_TABLE_TYPE;
		return string;
	}
	
	public static String getMainDesc(String mainDescField, int mainRow, int mainID) {
		return mainDescField + "_" + mainRow + "_" + mainID;
	}
	
	public static String getLinkIndexTableKey (int datasetNumber1, int datasetNumber2) {
		return "main_id_key" + getDatasetIndexCombination(datasetNumber1, datasetNumber2);
	}
	
	// ===================================================================================================

	public static StringBuffer buildInList(Collection<String> idCollection) {
		StringBuffer stringBuffer = new StringBuffer();
		int i=0;
		for (String id : idCollection) {
			stringBuffer.append((i==0 ? "" : ",") + "'" + id + "'");
			i++;
		}
		return stringBuffer;
	}
	public static StringBuffer buildMartServiceInList(Collection<String> idCollection) {
		StringBuffer stringBuffer = null;
		if (null!=idCollection) {
			stringBuffer = new StringBuffer();
			int i=0;
			for (String id : idCollection) {
				stringBuffer.append((i==0 ? "" : ",") + id);
				i++;
			}
		}
		return stringBuffer;
	}
	
	public static StringBuffer getDescription(Collection<Collection> collectionCollection) {
		StringBuffer stringBuffer = new StringBuffer("size = " + collectionCollection.size() + ": ");
		int i=0;
		for (Collection collection : collectionCollection) {
			stringBuffer.append((i==0 ? "" : ", ") + collection.size());
			i++;
		}
		return stringBuffer;
	}
	
	/**
		public static void main(String[] args) throws Exception {
			List<String> list1 = new ArrayList<String>();
			list1.add("1");
			list1.add("2");
			list1.add("2");
			list1.add("3");
			list1.add("3");
			list1.add("3");
			list1.add("4");
			
			List<String> list2 = new ArrayList<String>();
			list2.add("1");
			list2.add("1");
			list2.add("2");
			list2.add("2");
			list2.add("2");
			list2.add("3");
			list2.add("4");
			
			System.out.println(join(list1, list2));
		}
	 */
	public static List<String> join(List<String> list1, List<String> list2) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < list1.size(); i++) {
			String s1 = list1.get(i);
			for (int j = 0; j < list2.size(); j++) {
				String s2 = list2.get(j);
				if (s1.equals(s2)) {
					list.add(s1);
				}
			}
		}
		return list;
	}

	public static String checkForErrors(File file) throws FileNotFoundException, IOException {
		String errorMessage = null;
		FileReader fileReader = new FileReader(file);
		BufferedReader br = new BufferedReader(fileReader);
		String firstLine = br.readLine();
		if (firstLine!=null && firstLine.equals(MartServiceConstants.INVALID_FILE_ERROR_MESSAGE)) {
			String secondLine = br.readLine();
			errorMessage = secondLine;
		}
		br.close();
		fileReader.close();
		return errorMessage;
	}
	
	public static void checkForErrorsAndThrowException(List<String> listValues) throws TechnicalException {
		String errorMessage = LinkIndexesUtils.checkForErrors(listValues);
		if (errorMessage!=null) {
			throw new TechnicalException(errorMessage);
		}
	}
	public static String checkForErrors(List<String> listValues) {
		String errorMessage = null;
		if (listValues!=null && listValues.size()>=2) {
			String firstLine = listValues.get(0);
			if (firstLine!=null && firstLine.equals(MartServiceConstants.INVALID_FILE_ERROR_MESSAGE)) {
				String secondLine = listValues.get(1);
				errorMessage = secondLine;
			}
		}
		return errorMessage;
	}
}
