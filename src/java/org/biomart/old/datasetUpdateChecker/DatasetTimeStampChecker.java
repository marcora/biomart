package org.biomart.old.datasetUpdateChecker;


import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.biomart.common.general.constants.MyConstants;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.SqlUtils;
import org.biomart.common.general.utils.Trilean;
import org.biomart.objects.helpers.Rdbs;
import org.biomart.old.martService.Configuration;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.old.martService.objects.DatasetInMart;
import org.biomart.old.martService.objects.MartInVirtualSchema;
import org.biomart.test.linkIndicesTest.program.DatabaseParameter;


/**
 * Manifest-Version: 1.0
 * Main-Class: datasetUpdateChecker.DatasetTimeStampChecker
 * Class-Path: ./jdom.jar ./mysql-connector-java-5.1.7-bin.jar
 * //Class-Path: jar1.jar jar2.jar my_dir/jar3.jar
 * //Class-Path: /home/anthony/workspace/librairies/jdom-1.1/build/jdom.jar /home/anthony/workspace/librairies/mysql-connector-java-5.1.7/mysql-connector-java-5.1.7-bin.jar
 * java6 -jar check2.jar 
 * @author anthony
 */
public class DatasetTimeStampChecker {

	public static final String QUERY = "select " + MartServiceConstants.DATASET_NAME_FIELD + ", " + 
	MartServiceConstants.DATASET_MODIFIED_FIELD + ", " + MartServiceConstants.DATASET_TYPE_FIELD + 
	" from " + MartServiceConstants.DATASET_TABLE;
	public static final String EXTRA = ".0";
	public static final boolean DEBUG = false;
	public static final String MART_SERVICE_URL = 
		MartServiceConstants.CENTRAL_PORTAL_MART_SERVICE_STRING_URL;
		//MartServiceConstants.BMTEST_MART_SERVICE_STRING_URL;
	
	public static void main(String[] args) {
		DatasetTimeStampChecker datasetUpdateChecker = new DatasetTimeStampChecker();
		datasetUpdateChecker.process();
	}

    // For exceptions
    String currentServerVirtualSchema = null;
    MartInVirtualSchema currentMart = null;
    DatasetInMart currentBiomartPortalDataset = null;
    DatasetInMart currentRemoteDataset = null;
    String currentLocalSource = null;
    String currentRemoteSource = null;
    
    int correctCount = 0;
    int outOfSyncCount = 0;
    int missingCount = 0;
    int obsoleteCount = 0;
    
	public void process() {
		MyUtils.alterConsoleOutput(MyUtils.OUTPUT_FILES_PATH + "DatasetUpdateCheckerConsole", Trilean.MINUS);
        System.out.println("start.");
        Configuration biomartPortalConfiguration = new Configuration(MART_SERVICE_URL);
        StringBuffer sbCorrect = new StringBuffer();
        StringBuffer sbOutOfSync = new StringBuffer();
        StringBuffer sbMissing = new StringBuffer();
        StringBuffer sbObsolete = new StringBuffer();
        StringBuffer sbException = new StringBuffer();
        
		try {
			biomartPortalConfiguration.fetchMartSet();
			biomartPortalConfiguration.fetchDatasets();
			biomartPortalConfiguration.displayInitialStats();
			System.out.println();
			System.out.println();
			System.out.println();
			
			for (Iterator<String> it = biomartPortalConfiguration.virtualSchemaMartSetMap.keySet().iterator(); it.hasNext();) {
				currentServerVirtualSchema = it.next();
				
				Set<MartInVirtualSchema> martSet = biomartPortalConfiguration.virtualSchemaMartSetMap.get(currentServerVirtualSchema);
				
				for (MartInVirtualSchema mart : martSet) {
					System.out.println("> " + mart);
					/*if (mart.martName.equals("Eurexpress Biomart")) continue;*/
					
					currentMart = mart;
					
					List<DatasetInMart> biomartPortalDatasetList = biomartPortalConfiguration.martDatasetListMap.get(mart.martName);
					List<DatasetInMart> remoteDatasetList = null;
					
					currentLocalSource = biomartPortalConfiguration.getLocalMartServiceStringUrl(mart);
					
					if (mart.type.equals(MartServiceConstants.MART_TYPE_URL_LOCATION)) {
						currentRemoteSource = mart.getPathToRemoteMartService() + MartServiceConstants.MART_SERVICE_DATASET_LIST_PARAMETER + URLEncoder.encode(mart.martName, MyConstants.URL_ENCODING);
						if (DEBUG) {
							System.out.print(currentRemoteSource);
						}
		
						Configuration configuration = new Configuration(MART_SERVICE_URL);
						configuration.fetchDatasetSet(currentRemoteSource, currentServerVirtualSchema, mart);
						remoteDatasetList = configuration.martDatasetListMap.get(mart.martName);				
					} else if (mart.type.equals(MartServiceConstants.MART_TYPE_DB_LOCATION)) {
						remoteDatasetList = new ArrayList<DatasetInMart>();	//TODO use include dataset?
						
						Rdbs rdbs = mart.databaseType.equals(MartServiceConstants.XML_ATTRIBUTE_DATABASE_VALUE_MYSQL) ? Rdbs.MYSQL : null;		//TODO null for now
						int port = Integer.valueOf(mart.port);
						DatabaseParameter databaseParameter = new DatabaseParameter(rdbs, mart.host, port, mart.user, mart.password, mart.databaseName);
						currentRemoteSource = databaseParameter.toShortString() + " - " + QUERY;
						if (DEBUG) {
							System.out.print(currentRemoteSource);
						}
		
						Connection conn = SqlUtils.getConnection(databaseParameter, false);
				        Statement stmt = SqlUtils.createStatement(conn, false);
				        
						ResultSet rs = stmt.executeQuery(QUERY);
						
						rs.beforeFirst();
						while (rs.next()) {
							String modified = rs.getString(MartServiceConstants.DATASET_MODIFIED_FIELD);
							String datasetName = rs.getString(MartServiceConstants.DATASET_NAME_FIELD);
String visibility = null;	//TODO not sure why i didn't fetch it here
							String datasetType = rs.getString(MartServiceConstants.DATASET_TYPE_FIELD);
							DatasetInMart datasetInMart = new DatasetInMart(mart, datasetName, 
									(modified.endsWith(EXTRA) ? modified.substring(0, modified.length()-EXTRA.length()) : modified), 
									visibility, datasetType, "");
							remoteDatasetList.add(datasetInMart);
						}
						
						rs.close();
						stmt.close();
						conn.close();
					} else {
						throw new UnsupportedOperationException("mart = " + mart);
					}
					if (DEBUG) {
						System.out.println(": " + remoteDatasetList);	/*System.out.println(SqlUtils.getAllQueryResult(rs).toString());*/
					}
		
					// Check all datasets still exist and are up to date
					for (DatasetInMart biomartPortalDataset : biomartPortalDatasetList) {
						currentBiomartPortalDataset = biomartPortalDataset;
						
						int index = remoteDatasetList.indexOf(biomartPortalDataset);
						if (index!=-1) {
							currentRemoteDataset = remoteDatasetList.get(index);
							
							if (biomartPortalDataset.sameVersion(currentRemoteDataset)) {
								String string = createMessage(Status.CORRECT, mart, biomartPortalDataset, null, currentLocalSource, currentRemoteSource);
								sbCorrect.append(string + MyUtils.LINE_SEPARATOR);
								correctCount++;
							} else {
								String string = createMessage(Status.OUT_OF_SYNC, mart, biomartPortalDataset, currentRemoteDataset, currentLocalSource, currentRemoteSource);
								sbOutOfSync.append(string + MyUtils.LINE_SEPARATOR);
								outOfSyncCount++;
							}
						}
						// Dataset doesn't exist in remote resource anymore
						else {
							String string = createMessage(Status.OBSOLETE, mart, biomartPortalDataset, null, currentLocalSource, currentRemoteSource);
							sbObsolete.append(string + MyUtils.LINE_SEPARATOR);
							obsoleteCount++;
						}
					}
					
					// Check no missing dataset
					for (DatasetInMart remoteDataset : remoteDatasetList) {
						int index = biomartPortalDatasetList.indexOf(remoteDataset);
						if (index==-1) {
							String string = createMessage(Status.MISSING, mart, null, remoteDataset, currentLocalSource, currentRemoteSource);
							sbMissing.append(string + MyUtils.LINE_SEPARATOR);
							missingCount++;
						}
					}
				}
			}
        } catch (Exception e1) {
        	sbException.append(MyUtils.LINE_SEPARATOR + "Exception Message = " + e1.getMessage() + MyUtils.LINE_SEPARATOR +
        			MyUtils.TAB_SEPARATOR + "currentServerVirtualSchema = " + currentServerVirtualSchema + ", currentMart = " + currentMart + 
        			MyUtils.TAB_SEPARATOR + ", currentBiomartPortalDataset = " + currentBiomartPortalDataset + ", currentRemoveDataset = " + currentRemoteDataset + MyUtils.LINE_SEPARATOR +
        			MyUtils.TAB_SEPARATOR + "currentSource = " + currentRemoteSource + MyUtils.LINE_SEPARATOR +
        			MyUtils.TAB_SEPARATOR + MyUtils.arrayToStringBuffer(e1.getStackTrace()) + MyUtils.LINE_SEPARATOR);
			
		} finally {
			if (DEBUG) {
				// Display results
				System.out.println();
				System.out.println(sbCorrect.toString());
				System.out.println();
			}
			
			try {
				Thread.sleep(500);	// To print error message at the end
			} catch (InterruptedException e) {}
			
			if (sbException.length()==0 && sbOutOfSync.length()==0 && sbObsolete.length()==0 && sbMissing.length()==0) {
				System.out.println("All datasets are up to date!!");
			} else {
				System.out.println(sbOutOfSync.toString());
				System.out.println();
				System.out.println(sbObsolete.toString());
				System.out.println();
				System.out.println(sbMissing.toString());
				System.out.println();
				System.out.println(sbException.toString());
				System.out.println();
				System.out.println("correctCount =  " + MyUtils.TAB_SEPARATOR + correctCount);
				System.out.println("outOfSyncCount = " + MyUtils.TAB_SEPARATOR + outOfSyncCount);
				System.out.println("obsoleteCount = " + MyUtils.TAB_SEPARATOR + obsoleteCount);
				System.out.println("missingCount =  " + MyUtils.TAB_SEPARATOR + missingCount);
				System.out.println();
				System.out.println("total =  " + MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + biomartPortalConfiguration.getTotalDatasets());
				System.out.println();
			}
		}
        System.out.println("done.");
		MyUtils.closeConsoleOutput();
	}
	
	enum Status {
		CORRECT, OUT_OF_SYNC, MISSING, OBSOLETE; 
	}

	private String createMessage(Status status, MartInVirtualSchema mart, DatasetInMart biomartPortalDataset, DatasetInMart remoteDataset, 
			String localSource, String remoteSource) {
		StringBuffer stringBuffer = new StringBuffer();
		if (status.equals(Status.CORRECT)) {
			stringBuffer.append("Up to date!" + MyUtils.LINE_SEPARATOR);
		} else if (status.equals(Status.OUT_OF_SYNC)) {
			stringBuffer.append("Out of sync" + MyUtils.LINE_SEPARATOR);
		} else if (status.equals(Status.MISSING)) {
			stringBuffer.append("Missing  (exist in remote config but not in local  one)" + MyUtils.LINE_SEPARATOR);
		} else if (status.equals(Status.OBSOLETE)) {
			stringBuffer.append("Obsolete (exist in local  config but not in remote one)" + MyUtils.LINE_SEPARATOR);
		}
		stringBuffer.append(MyUtils.TAB_SEPARATOR + "mart = " + MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + mart + MyUtils.LINE_SEPARATOR);
		stringBuffer.append(MyUtils.TAB_SEPARATOR + "biomartPortalDataset = " + MyUtils.TAB_SEPARATOR + biomartPortalDataset + MyUtils.LINE_SEPARATOR);
		stringBuffer.append(MyUtils.TAB_SEPARATOR + "remoteDataset =   " + MyUtils.TAB_SEPARATOR + remoteDataset + MyUtils.LINE_SEPARATOR);
		stringBuffer.append(MyUtils.TAB_SEPARATOR + "localSource  =  " + MyUtils.TAB_SEPARATOR + localSource + MyUtils.LINE_SEPARATOR);
		stringBuffer.append(MyUtils.TAB_SEPARATOR + "remoteSource =  " + MyUtils.TAB_SEPARATOR + remoteSource + MyUtils.LINE_SEPARATOR);
		return stringBuffer.toString();
	}
}
