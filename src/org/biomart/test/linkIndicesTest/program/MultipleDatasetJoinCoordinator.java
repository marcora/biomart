package org.biomart.test.linkIndicesTest.program;


import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.SqlUtils;
import org.biomart.common.general.utils.Timer;
import org.biomart.test.linkIndicesTest.LinkIndexesUtils;


public class MultipleDatasetJoinCoordinator {

	LinkIndexesParameters parameters = null;
	
	public MultipleDatasetJoinCoordinator(LinkIndexesParameters parameters) {
		this.parameters = parameters;
		
		MyUtils.println("parameters = " + parameters);
	}
	public Timer buildAllLinkIndex() throws FunctionalException {
		
		Timer buildLinkIndexTimer = new Timer("build link index timer");
		buildLinkIndexTimer.startTimer();
		
		try {
			parameters.connectAll();
			
			for (int datasetNumber = 0; datasetNumber < parameters.totalDataset-1; datasetNumber++) {
				buildIndex(parameters.localDatabase.dbParam.databaseName, datasetNumber, datasetNumber+1);
			}
			
		} catch (SQLException e) {
			parameters.error = Boolean.TRUE;
			e.printStackTrace();
		} catch (IOException e) {
			parameters.error = Boolean.TRUE;
			e.printStackTrace();
		} finally {
			parameters.disconnectAll();
		}

		buildLinkIndexTimer.stopTimer();
		
		return buildLinkIndexTimer;
	}
	
	private void buildIndex(String linkIndexDatabaseName, int datasetNumber1, int datasetNumber2) throws SQLException, IOException, FunctionalException {
		buildIndex(linkIndexDatabaseName, parameters.dataSources[datasetNumber1], parameters.dataSources[datasetNumber2]);
	}
	
	private void buildIndex(String linkIndexDatabaseName, DataSource dataSource1, DataSource dataSource2) throws SQLException, IOException, FunctionalException {

		LinkIndexesUtils.useLocalDatabase(parameters);
		Integer size = null;
	
		String linkIndexTableName = null;
		String linkIndexTableKey = null;
		if (dataSource1 instanceof DatabaseSchema) {
			linkIndexTableName = ((DatabaseSchema)dataSource1).getLinkTableName();
			linkIndexTableKey = ((DatabaseSchema)dataSource1).getLinkTableKey();
		} else if (dataSource1 instanceof MartServiceSchema){
			linkIndexTableName = null;
			linkIndexTableKey = null;
		}
		
		if (!parameters.localDatabase.sqlUtils.tableExists(linkIndexDatabaseName, linkIndexTableName)) {
			MyUtils.println("index table did not already exists: creating and loading...");
			
			LinkIndexesUtils.useLocalDatabase(parameters);
			parameters.localDatabase.sqlUtils.runUpdateQuery(LinkIndexesUtils.getDropTableIfExistsQuery(linkIndexTableName));
			parameters.localDatabase.sqlUtils.runUpdateQuery(LinkIndexesUtils.getCreateLinkIndexTableQuery(linkIndexTableName, linkIndexTableKey));	
			
			DataSource dataSourceLeft = null;
			DataSource dataSourceRight = null;
			
			if (dataSource1 instanceof DatabaseSchema) {
				DatabaseSchema database1 = (DatabaseSchema)dataSource1;
				dataSourceLeft = new DatabaseSchema(
						database1.dbParam,
						new SuperTable(
								new Table[] {
										new Table(database1.superTable.joinTable.getTable(),
												database1.superTable.joinTable.getShortRightField())	// Right
										}, 
								new JoinTable(database1.superTable.joinTable.getTable(),
											database1.superTable.joinTable.getShortRightField())	// Right
								
							),
						true, null, null);	// isLinkIndex is irrelevant here
			} else if (dataSource1 instanceof MartServiceSchema){
				//TODO
				MartServiceSchema schema1 = (MartServiceSchema)dataSource1;
		schema1.getClass();
				dataSourceLeft = null;
			}

			if (dataSource2 instanceof DatabaseSchema) {
				DatabaseSchema database2 = (DatabaseSchema)dataSource2;
				dataSourceRight = new DatabaseSchema(
						database2.dbParam,
						new SuperTable(
								new Table[] {
										new Table(database2.superTable.joinTable.getTable(),
												database2.superTable.joinTable.getShortLeftField())	// Left
										}, 
								new JoinTable(database2.superTable.joinTable.getTable(),
											database2.superTable.joinTable.getShortLeftField())	// Left
							),
						true, null, null);	// isLinkIndex is irrelevant here
			} else if (dataSource2 instanceof MartServiceSchema){
				//TODO
				MartServiceSchema schema2 = (MartServiceSchema)dataSource2;
			schema2.getClass();
				dataSourceRight = null;
			}
			
			int indexCreationBatchSizeBase = 1000000;
			double indexCreationBatchSizeMultiplier = 1;
			int indexCreationBatchSizeIncrease = 1;	// 1000000
			
			// Populate table
			LinkIndexesParameters indexCreationParameters = new LinkIndexesParameters(
						parameters.localDatabase, Mode.LINK_INDEX_CREATION_TMP, 
						false, true, true,
						indexCreationBatchSizeBase, indexCreationBatchSizeMultiplier, indexCreationBatchSizeIncrease, 
						parameters.printDebug, parameters.displaySuperDebug, dataSourceLeft, dataSourceRight);
			File createIndextemporaryFile = File.createTempFile(linkIndexTableName, ".txt", new File("." + MyUtils.FILE_SEPARATOR));
			indexCreationParameters.setProperties(false, false, true, createIndextemporaryFile);	// not distinct (much faster, let program handle the uniqueness with set)
			MultipleDatasetJoiner createIndex = new MultipleDatasetJoiner(indexCreationParameters);
			LinkIndicesTestEnvironmentResult createIndexResult = null;
			createIndexResult = createIndex.run();
			String query = LinkIndexesUtils.getLoadDataLocalInfileQuery(createIndextemporaryFile.getAbsolutePath(), linkIndexTableName, null);
			if (parameters.displaySuperDebug) {
				MyUtils.println("query = " + SqlUtils.displayQuery(parameters.localDatabase.sqlUtils, query));
			}
			parameters.localDatabase.sqlUtils.runUpdateQuery(query);		
			createIndextemporaryFile.deleteOnExit();
			size = createIndexResult.getResultSize();
		} else {
			MyUtils.println("index table already exists");
			size = parameters.localDatabase.sqlUtils.countRows(linkIndexTableName);
		}
		
		// Check if result table created properly
		LinkIndexesUtils.useLocalDatabase(parameters);		
		if (!parameters.localDatabase.sqlUtils.existsAndContains(linkIndexDatabaseName, linkIndexTableName, size)) {			
			throw new SQLException("problem creating index table " + linkIndexTableName);
		}

		MyUtils.println("index table: " + linkIndexTableName + ": " + parameters.localDatabase.sqlUtils.countRows(linkIndexTableName) + " rows");
		MyUtils.println();
		
	}
		
	public LinkIndicesTestEnvironmentResult joinMultipleDataset(boolean distinct, boolean useIndex, boolean indexCreation, File createIndextemporaryFile) throws FunctionalException {
		if (!useIndex) {
			parameters.setProperties(distinct, false, indexCreation, createIndextemporaryFile);	// distinct to false because Java is much faster choosing distinct values
if (LinkIndicesTestEnvironmentMain.reverse) {parameters.dataSources=reverse((DatabaseSchema[])parameters.dataSources);}
			MultipleDatasetJoiner withoutIndex = new MultipleDatasetJoiner(parameters);
			MyUtils.println("running without index...");
			return withoutIndex.run();
		} else {
			
			// Create specific parameters for it
			LinkIndexesParameters withIndexParameters = parameters;
			
			int newTotalDataset = parameters.totalDataset*2-1;	// all dataset + link indexes (1 link index less than datasets)		
			DataSource[] newDatabases = new DatabaseSchema[newTotalDataset];
			for (int datasetNumber = 0; datasetNumber < parameters.totalDataset; datasetNumber++) {
				newDatabases[datasetNumber*2] = parameters.dataSources[datasetNumber];
				if (datasetNumber<parameters.totalDataset-1) {			
String linkIndexTableName =
	null;//TODO 
	//parameters.dataSources[datasetNumber].superTable.withIndexJoinWrapper.joinTableSource.getTable();
String linkIndexTableKey =
	null;//TODO
	//parameters.dataSources[datasetNumber].superTable.withIndexJoinWrapper.joinTableSource.getShortSourceField();
					newDatabases[(datasetNumber*2)+1] = new DatabaseSchema(
							parameters.localDatabase.dbParam,
							new SuperTable(
									new Table[] {new Table(linkIndexTableName, linkIndexTableKey)}, 
									new JoinTable(linkIndexTableName, linkIndexTableKey)),
true, "", ""//TODO		// isLinkIndex
					);
				}
			}
			
if (LinkIndicesTestEnvironmentMain.reverse) {	
// Reverse
newDatabases = reverse(newDatabases);
System.out.println(newDatabases);
System.out.println(newDatabases.length);
System.out.println(newDatabases[0]);
System.out.println(newDatabases[1]);
System.out.println(newDatabases[2]);
System.out.println(newDatabases[3]);
System.out.println(newDatabases[4]);
System.out.println(newDatabases[5]);
System.out.println(newDatabases[6]);
System.out.println(newDatabases[7]);
System.out.println(newDatabases[8]);
			System.out.println(MyUtils.arrayToStringBuffer(newDatabases));
System.out.println("###############");}
			
			// Swap the 1st two ones
			DataSource databaseTmp = newDatabases[0];
			newDatabases[0] = newDatabases[1];
			newDatabases[1] = databaseTmp;
			
			withIndexParameters = new LinkIndexesParameters(
						parameters.localDatabase, Mode.USE_INDEX_TMP, 
						useIndex, false, false,
						parameters.batchSizeBase, parameters.batchSizeMultiplier, parameters.batchSizeIncrease, 
						parameters.printDebug, parameters.displaySuperDebug, newDatabases);
			MultipleDatasetJoiner withIndex = new MultipleDatasetJoiner(withIndexParameters);
			
			MyUtils.println("running with index...");
			return withIndex.run();
		}
	}
private DataSource[] reverse(DataSource[] newDatabases) {
	List<DataSource> list = new ArrayList<DataSource>(Arrays.asList(newDatabases));
	Collections.reverse(list);
	System.out.println(list.size());
	int i =0;
	DataSource[] d = new DataSource[newDatabases.length];
	for (DataSource db : list) {
		d[i] = db;
		i++;
	}
	return d;
}
}
