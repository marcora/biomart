package org.biomart.test.linkIndicesTest.program;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.SqlUtils;
import org.biomart.old.martService.restFulQueries.RestFulQuery;
import org.biomart.old.martService.restFulQueries.RestFulQueryDataset;
import org.biomart.old.martService.restFulQueries.objects.Filter;
import org.biomart.test.linkIndicesTest.LinkIndexesUtils;


public class MultipleDatasetJoiner {

	
public final boolean storeResultMemory = false;

	private LinkIndexesParameters parameters = null;
	
	private Integer firstDatasetNumber = null;
	private Integer secondDatasetNumber = null;
	private Integer lastDatasetNumber = null;
	
	private Boolean enoughMatchingRows = Boolean.FALSE;
	private Integer totalMatchingRows = 0;
	
	private List<Set<String>> currentIdSetList = new ArrayList<Set<String>>();
	private List<List<List<DatasetValues>>> batchResultListDatasetList = null;
	
	private List<List<DatasetValues>> memoryResultList = new ArrayList<List<DatasetValues>>();

	public MultipleDatasetJoiner(LinkIndexesParameters parameters) {
		
		// Store parameters
		this.parameters = parameters;
		MyUtils.println("parameters = " + parameters);
		
		// Initialize
		this.enoughMatchingRows = Boolean.FALSE;
		this.totalMatchingRows = 0;
		this.memoryResultList = new ArrayList<List<DatasetValues>>();
			
		this.firstDatasetNumber = 0;
		this.secondDatasetNumber = 1;
		this.lastDatasetNumber = parameters.totalDataset-1;
		
		this.batchResultListDatasetList = new ArrayList<List<List<DatasetValues>>>();
		this.currentIdSetList = new ArrayList<Set<String>>();
		for (int i = 0; i < parameters.totalDataset; i++) {
			this.batchResultListDatasetList.add(null);
			this.currentIdSetList.add(null);
		}
	}
	
	public LinkIndicesTestEnvironmentResult run() throws FunctionalException {
		
		LinkIndicesTestEnvironmentResult result = new LinkIndicesTestEnvironmentResult(
				"{index = "+String.valueOf(parameters.useIndex)+ ", interactive = "+String.valueOf(parameters.interactive) + "}");
		result.getTimer().startTimer();

		try {
			parameters.connectAll();			
			writeResultInitialization();
			fetchResultRecursively(0);
			result.setResultSize(totalMatchingRows);
			writeResultFinalization();
		} catch (SQLException e1) {
			e1.printStackTrace();
			parameters.error = Boolean.TRUE;
		} catch (IOException e1) {
			e1.printStackTrace();
			parameters.error = Boolean.TRUE;
		} catch (FunctionalException e1) {
			e1.printStackTrace();
			parameters.error = Boolean.TRUE;
		} finally {
			parameters.disconnectAll();
		}
		
		result.getTimer().stopTimer();
		
		// Display and serialize
		MyUtils.println("final total rows = " + totalMatchingRows + " rows");
		if (storeResultMemory) {
			try {
				MyUtils.writeSerializedObject(memoryResultList, MyUtils.OUTPUT_FILES_PATH + 
						MyUtils.getCurrentTimeOfDayToMillisecondAsString() + parameters.useIndex);
			} catch (TechnicalException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	@SuppressWarnings("deprecation")
	public void fetchResultRecursively(int currentDatasourceNumber) throws SQLException, FunctionalException, IOException {

		int batchOffset = 0;
		int batchTabIndex = 0;
		int batchSize = parameters.batchTab[batchTabIndex];
		Boolean lastBatch = Boolean.FALSE;
		
		DataSource dataSource = parameters.dataSources[currentDatasourceNumber];
		Boolean currentDataSourceIsDatabaseSchema = dataSource instanceof DatabaseSchema;
		MartServiceSchema martServiceSchema = currentDataSourceIsDatabaseSchema ? null : (MartServiceSchema)dataSource;
		DatabaseSchema databaseSchema = currentDataSourceIsDatabaseSchema ? (DatabaseSchema)dataSource : null;
		
		Boolean currentIsFirstDataset = currentDatasourceNumber==firstDatasetNumber;
		Boolean currentIsSecondDataset = currentDatasourceNumber==secondDatasetNumber;
		Boolean currentIsLastDataset = currentDatasourceNumber==lastDatasetNumber;
		Integer previousDatasetNumber = !currentIsFirstDataset ? currentDatasourceNumber-1 : null;
		Integer nextDatasetNumber = !currentIsLastDataset ? currentDatasourceNumber+1 : null;
		Boolean isSecondDatasetOnLinkIndexCreation = (parameters.indexCreation && currentIsSecondDataset);
		Boolean isLinkIndexAndNotFirstLinkIndex = (!parameters.indexCreation &&
				!currentIsFirstDataset && dataSource.getIsLinkIndex());
		Boolean useIndexAndDatasetIsLinkIndex = (parameters.useIndex && dataSource.getIsLinkIndex());
	
		Boolean deActivateBatching = isSecondDatasetOnLinkIndexCreation || isLinkIndexAndNotFirstLinkIndex;	// We only subbatch when there is risk of increasing result size
		
		while (!lastBatch) {
			
			// Get previousIdSet and previousBatchResultList or create new ones
			Set<String> previousIdSet = currentIsFirstDataset ? 
					new TreeSet<String>() : currentIdSetList.get(previousDatasetNumber);
			List<List<DatasetValues>> previousBatchResultList = currentIsFirstDataset ? 
					new ArrayList<List<DatasetValues>>() : batchResultListDatasetList.get(previousDatasetNumber);
			
			Set<String> newIdSet = new TreeSet<String>();	// Does distinct on the field used for joining
			List<List<DatasetValues>> newBatchResultList = new ArrayList<List<DatasetValues>>();		
			
			// For display
			if (currentIsFirstDataset) {
				MyUtils.println(MyUtils.LINE_SEPARATOR + MyUtils.EQUAL_LINE);
			}
			MyUtils.println("==> " + "currentDatasetNumber = " + currentDatasourceNumber + "   ---   " + 
					(deActivateBatching ? "full table scan" : batchOffset + ", " + batchSize) + 
					(dataSource.getIsLinkIndex() ? "   (link index)" : "") + " <==");
					
			String tableName = null;
			String leftFieldName = null;
			String rightFieldName = null;
			String[] resultFieldNames = null;
			Integer rowCountRegular = null;
			HashMap<String, List<String>> mapData = null;	// For mart service only
			if (currentDataSourceIsDatabaseSchema) {
				// Get parameters necessary for the following query
				tableName = databaseSchema.superTable.joinTable.getTable();
				leftFieldName = databaseSchema.superTable.joinTable.getFullLeftField();
				rightFieldName = databaseSchema.superTable.joinTable.getFullRightField();
				resultFieldNames = !parameters.indexCreation ? 
						databaseSchema.superTable.tableTab[0].getFieldNameTab() : new String[0];	// TODO only handles 1 table for now
				if (parameters.displaySuperDebug) {MyUtils.println("targetTable = " + tableName + ", targetFieldLeft = " + leftFieldName +
						", targetFieldRight = " + rightFieldName + ", targetFieldNames = " + MyUtils.arrayToStringBuffer(resultFieldNames));}
						
				// Build regular query
				StringBuffer whereInClause = LinkIndexesUtils.buildWhereInClause(
						(currentIsFirstDataset ? rightFieldName : leftFieldName), previousIdSet);	// targetFieldRight is only for 1st dataset when using an index
				StringBuffer limitClause = deActivateBatching ? 
						new StringBuffer() : LinkIndexesUtils.buildLimitClause(batchOffset, batchSize);
				StringBuffer filteringClause = new StringBuffer();
				filteringClause.append(whereInClause);
				filteringClause.append(limitClause);
				StringBuffer queryRegular = useIndexAndDatasetIsLinkIndex ? 
						LinkIndexesUtils.getLinkIndexQuery(parameters.distinct, rightFieldName, tableName, filteringClause) :
						LinkIndexesUtils.getQuery(parameters.distinct, 
						leftFieldName, (!currentIsLastDataset ? rightFieldName : null), resultFieldNames,
						tableName, filteringClause);
		
				// Run it and count results
				SqlUtils mySqlUtilsRegular = databaseSchema.sqlUtils;
				LinkIndexesUtils.useDatabase((DatabaseSchema)dataSource, parameters.displaySuperDebug);
				if (parameters.displaySuperDebug) {MyUtils.println("db " + currentDatasourceNumber + 
						", queryRegular = " + SqlUtils.displayQuery(mySqlUtilsRegular, queryRegular) + 
						", with " + previousIdSet.size() + " elements in WHERE IN clause");}
				mySqlUtilsRegular.runExecuteQuery(queryRegular);
				rowCountRegular = mySqlUtilsRegular.countRowsAfterQuery();
			} else {
				
				// Build mart service query
				String formatter = "TSV";
				Boolean count = false;
				Boolean header = false;
				Boolean unique = false;		// faster when index creation?
				
				List<Filter> filterList = null;
				if (!previousIdSet.isEmpty()) {
					StringBuffer WhereInClause = LinkIndexesUtils.buildMartServiceInList(previousIdSet);	// Where IN clause
					Filter filter = currentIsFirstDataset ?
							martServiceSchema.superTable2.getRightFilter(WhereInClause) :
							martServiceSchema.superTable2.getLeftFilter(WhereInClause);	// targetFieldRight is only for 1st dataset when using an index
					filterList = new ArrayList<Filter>(Arrays.asList(new Filter[] {filter}));
				}
				
				RestFulQuery restFulQuery = new RestFulQuery(
						martServiceSchema.superTable2.martServiceServer,
						martServiceSchema.superTable2.virtualSchemaName,
						martServiceSchema.superTable2.datasetConfigVersion,
						formatter, count, header, unique, 
						batchOffset, batchSize, 	// Limit clause
						new RestFulQueryDataset(martServiceSchema.superTable2.datasetName,
								martServiceSchema.superTable2.getAllAttributesList(), filterList)	// Result attributes
				);
				if (parameters.displaySuperDebug) {MyUtils.println("restFulQuery = " + restFulQuery + 
						", with " + previousIdSet.size() + " elements in WHERE IN clause");}
				mapData = (HashMap<String, List<String>>)restFulQuery.getMapData(false);
				rowCountRegular = mapData.get(martServiceSchema.superTable2.leftAttributeName).size();	// Take the first one (they all have the same length (could be optimized) TODO
			}
			if (parameters.displaySuperDebug) {MyUtils.println("rowCountRegular = " + rowCountRegular);}
					
			// Write query to result file
			/*if (!parameters.indexCreation) {
				parameters.resultWriter.write(
						(currentIsFirstDataset ? MyUtils.DASH_LINE + MyUtils.LINE_SEPARATOR : "") +
						MyUtils.TAB_SEPARATOR + rowCountRegular + MyUtils.TAB_SEPARATOR + 
						queryRegular + MyUtils.LINE_SEPARATOR + MyUtils.LINE_SEPARATOR);
			}*/
		
			// Update status variables
			Boolean noMatches = rowCountRegular==0;
			if (parameters.displaySuperDebug) {
				MyUtils.println("noMatches = " + noMatches);
				MyUtils.println(!noMatches ? 
					"matches found after regular query at datasetNumber " + currentDatasourceNumber : 
						"no matches after regular query at datasetNumber " + currentDatasourceNumber);
			}
			lastBatch = deActivateBatching ? true : rowCountRegular==0/*rowCountRegular<batchSize*/;	// because of limitStart in martservice.. 
			if (parameters.displaySuperDebug) {MyUtils.println("lastBatch = " + lastBatch);}
			
			// Populate result list (temporary for each batch) and prepare next currentIdList if not the last dataset
			List<DatasetValues> currentDatasetValuesList = new ArrayList<DatasetValues>();

			if (currentDataSourceIsDatabaseSchema) {
				SqlUtils sqlUtils = databaseSchema.sqlUtils;
				sqlUtils.rs.beforeFirst();
				while (sqlUtils.rs.next()) {
					
					// Get and store all values of interest and skip record when obsolete record +
					// add to currentIdList (which is actually for the next iteration's "in list") only if not null and has a value 
					String targetFieldRightValue = sqlUtils.rs.getString(rightFieldName);
					
					// Skip this record if its right value is null or empty (since we won't be able to use it as a join)
					if (!LinkIndexesUtils.isValidValue(targetFieldRightValue)) {
						continue;	// jump to next result
					}
					newIdSet.add(targetFieldRightValue);	// Preparing next dataset's "in list"
					
					// If createIndex then the currentIdList is sufficient
					if (!parameters.indexCreation) {
						String targetFieldLeftValue = sqlUtils.rs.getString(leftFieldName);
						List<String> targetFieldValues = new ArrayList<String>();
						for (int fieldNumber = 0; fieldNumber < resultFieldNames.length; fieldNumber++) {
							targetFieldValues.add(sqlUtils.rs.getString(resultFieldNames[fieldNumber]));
						}
						DatasetValues currentDatasetValues = new DatasetValues(targetFieldLeftValue, targetFieldRightValue, targetFieldValues);
						currentDatasetValuesList.add(currentDatasetValues);
					}
				}
				sqlUtils.closeResultSet();
				currentIdSetList.set(currentDatasourceNumber, newIdSet);
			} else {
				
				List<String> targetFieldRightValueList = mapData.get(martServiceSchema.superTable2.rightAttributeName);
				List<String> targetFieldLeftValueList = null;
				List<List<String>> targetFieldValuesList = null;
				if (!parameters.indexCreation) {
					targetFieldLeftValueList = mapData.get(martServiceSchema.superTable2.leftAttributeName);
					targetFieldValuesList = new ArrayList<List<String>>();
					for (String attributeName : martServiceSchema.superTable2.otherResultAttributeNames) {
						targetFieldValuesList.add(mapData.get(attributeName));
					}
				}
				
				for (int i = 0; i < targetFieldRightValueList.size(); i++) {
					String targetFieldRightValue = targetFieldRightValueList.get(i);
				
					// Skip this record if its right value is null or empty (since we won't be able to use it as a join)
					if (!LinkIndexesUtils.isValidValue(targetFieldRightValue)) {
						continue;	// jump to next result
					}
					newIdSet.add(targetFieldRightValue);	// Preparing next dataset's "in list"
					
					// If createIndex then the currentIdList is sufficient
					if (!parameters.indexCreation) {
						String targetFieldLeftValue = targetFieldLeftValueList.get(i);
						
						List<String> targetFieldValues = new ArrayList<String>();
						for (List<String> targetFieldValueList : targetFieldValuesList) {
							targetFieldValues.add(targetFieldValueList.get(i));
						}
						DatasetValues currentDatasetValues = new DatasetValues(targetFieldLeftValue, targetFieldRightValue, targetFieldValues);
						currentDatasetValuesList.add(currentDatasetValues);
					}				
				}
				currentIdSetList.set(currentDatasourceNumber, newIdSet);
			}
			
			MyUtils.println();
			if (parameters.displaySuperDebug) {
				MyUtils.println(MyUtils.TAB_SEPARATOR + "-> dataset results:");
				MyUtils.println("currentDatasetValuesList.size() = " + currentDatasetValuesList.size());
				MyUtils.println("currentIdSet (regular) = " + getCurrentIdSetString(newIdSet));
				MyUtils.println("useIndex = " + parameters.useIndex);
				MyUtils.println();
			}
			
			// For each result, inject into batchResultList
			// Will be skipped for index creation as currentDatasetValuesList is empty (only newIdSet matters then)
			for (int validResultNumber = 0; validResultNumber < currentDatasetValuesList.size(); validResultNumber++) {
				DatasetValues currentDatasetValues = currentDatasetValuesList.get(validResultNumber);
				
				// If 1st dataset for current batch, create the batch result list from scratch
				if ((!parameters.indexCreation && currentIsFirstDataset) || 
						(parameters.indexCreation && currentIsSecondDataset)) {
					List<DatasetValues> newDatasetValuesList = new ArrayList<DatasetValues>();
					newDatasetValuesList.add(currentDatasetValues);						
					newBatchResultList.add(newDatasetValuesList);
				}
				
				// Otherwise
				else {
					if (!parameters.indexCreation) {
						/*// If unique			
						Integer batchDatasetResultListIndex = getFirstMatchingRow(
								previousBatchResultList, previousDatasetNumber, currentDatasetValues.joinFieldValueLeft);									
										
						MyUtils.checkStatusProgram(batchDatasetResultListIndex!=null, 
								"batchDatasetResultListIndex!=null, batchDatasetResultListIndex = " + 
								batchDatasetResultListIndex, true);	// 1 and only 1 row
						
						List<DatasetValues> currentListDatasetValues = new ArrayList<DatasetValues>(
								previousBatchResultList.get(batchDatasetResultListIndex));
						currentListDatasetValues.add(currentDatasetValues);	// add latest dataset data
						newBatchResultList.add(currentListDatasetValues);*/
						
					
						// Obtain list of records from previous result list, that are a match for the latest filtering				
						List<Integer> batchDatasetResultListIndexList = getAllMatchingRows(
								previousBatchResultList, previousDatasetNumber, currentDatasetValues.joinFieldValueLeft);									
										
						MyUtils.checkStatusProgram(!batchDatasetResultListIndexList.isEmpty(), 
								"!batchDatasetResultListIndexList.isEmpty(), batchDatasetResultListIndexList.size() = " + 
								batchDatasetResultListIndexList.size(), true);	// at least 1 row if 1-to-1 (more if many-to-1)
						
						int size = batchDatasetResultListIndexList.size();
						for (int i = 0; i < size; i++) {
							List<DatasetValues> currentListDatasetValues = new ArrayList<DatasetValues>(
									previousBatchResultList.get(batchDatasetResultListIndexList.get(i)));
							currentListDatasetValues.add(currentDatasetValues);	// add latest dataset data
							newBatchResultList.add(currentListDatasetValues);
						}
					}
				}
			}
			batchResultListDatasetList.set(currentDatasourceNumber, newBatchResultList);
			
			// Update noMatches
			noMatches = (!parameters.indexCreation && newBatchResultList.isEmpty()) || (parameters.indexCreation && newIdSet.isEmpty());
			if (noMatches) {
				MyUtils.checkStatusProgram(newIdSet.isEmpty(), 
						"newIdSet.isEmpty(), newIdSet.size() = " + newIdSet.size(), true);
				MyUtils.checkStatusProgram(newBatchResultList.isEmpty(), 
						"newBatchResultList.isEmpty(), = " + newBatchResultList.size(), true);
				newIdSet.clear();	// Should already be empty
				newBatchResultList.clear();	// Should already be empty
			}
			if (parameters.displaySuperDebug) {
				MyUtils.println("noMatches = " + noMatches/* + ", datasetNumberNoMatches = " + datasetNumberNoMatches*/);
				MyUtils.println(!noMatches ? 
						"matches found after merging results at datasetNumber " + currentDatasourceNumber : 
							"no matches after merging results at datasetNumber " + currentDatasourceNumber);
			}
			
			MyUtils.println();
			if (parameters.displaySuperDebug) {
				MyUtils.println("batchResultList = " + getResultListString(newBatchResultList));
				MyUtils.println();
			}
			
			// Writing in result file
			if (parameters.indexCreation) {
				if (currentIsLastDataset) {		// Write results (no more filtering needed and all values are available)
					for (String id : newIdSet) {
						parameters.resultWriter.write(id+MyUtils.LINE_SEPARATOR);
					}
				}
			} else {
				if (currentIsFirstDataset) {
					parameters.resultWriter.write(MyUtils.TAB_SEPARATOR + newBatchResultList.size() + MyUtils.LINE_SEPARATOR);
					parameters.resultWriter.write(MyUtils.LINE_SEPARATOR + MyUtils.LINE_SEPARATOR);
				}
				
				if (currentIsLastDataset) {		// Write results (no more filtering needed and all values are available)
					for (int i = 0; i < newBatchResultList.size(); i++) {
						List<DatasetValues> listDatasetValues = newBatchResultList.get(i);
						
						parameters.resultWriter.write((totalMatchingRows+i) + MyUtils.TAB_SEPARATOR);
						for (int datasetNumber = 0; datasetNumber < listDatasetValues.size(); datasetNumber++) {
							if (!parameters.dataSources[datasetNumber].getIsLinkIndex()) {
								List<String> fieldValues = listDatasetValues.get(datasetNumber).fieldValues;
								for (int columnNumber = 0; columnNumber < fieldValues.size(); columnNumber++) {
									parameters.resultWriter.write(fieldValues.get(columnNumber) + MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR);
								}
							}
						}
						parameters.resultWriter.write(MyUtils.LINE_SEPARATOR);
					}
				}
			} 

			if (currentIsLastDataset) {
				
				if (storeResultMemory) {
					memoryResultList.addAll(newBatchResultList);
				}
				
				// Update row counts
				int currentDatasetMatchingRowsCount = parameters.indexCreation ? newIdSet.size() : newBatchResultList.size();
				totalMatchingRows+=currentDatasetMatchingRowsCount;
				enoughMatchingRows=totalMatchingRows>=parameters.batchSizeBase;
				
				if (parameters.displaySuperDebug) {
					MyUtils.println("-> batch results:");
					
					MyUtils.println("batchOffset = " + batchOffset);
					MyUtils.println("batchSize = " + batchSize);
					MyUtils.println("batchTabIndex = " + batchTabIndex);
					MyUtils.println();
					
					MyUtils.println("noMatches = " + noMatches/* + ", datasetNumberNoMatches = " + datasetNumberNoMatches*/);
					MyUtils.println("totalMatchingRows = " + totalMatchingRows);
					MyUtils.println("enoughMatchingRows = " + enoughMatchingRows);
					MyUtils.println();
				}
			}

			if (parameters.displaySuperDebug) {
				MyUtils.print("Finished with dataset " + currentDatasourceNumber + " - press enter to continue");
				MyUtils.println();
				MyUtils.println();
				
	//if (batchOffset+batchSize>8999999)			
	//	MyUtils.wrappedReadInput();
				MyUtils.println("\t\t\t------------------------\t\t");
			}
			
			if (!noMatches && !currentIsLastDataset) {
				fetchResultRecursively(nextDatasetNumber);
			}
			
			if (parameters.displaySuperDebug) {
				MyUtils.println("dataset " + currentDatasourceNumber + " BEFORE change:");
				MyUtils.println("currentIdSetList = " + getDescription1(currentIdSetList));
				MyUtils.println("batchResultListDatasetList = " + getDescription2(batchResultListDatasetList));
				MyUtils.println();
			}
			
			// Erase result list (written and stored now)
			batchResultListDatasetList.set(currentDatasourceNumber, null);
			currentIdSetList.set(currentDatasourceNumber, null);
			
			if (parameters.displaySuperDebug) {
				MyUtils.println("dataset " + currentDatasourceNumber + " AFTER change:");
				MyUtils.println("currentIdSetList = " + getDescription1(currentIdSetList));
				MyUtils.println("batchResultListDatasetList = " + getDescription2(batchResultListDatasetList));
				MyUtils.println();
			}
			
			batchOffset = batchOffset+batchSize;
			if (batchTabIndex<parameters.batchTab.length-1) {
				batchTabIndex++;
				batchSize = parameters.batchTab[batchTabIndex];
			}
		}
	}
	
	public List<Integer> getAllMatchingRows (List<List<DatasetValues>> batchResultList, int latestElement, String newTargetFieldLeftValue) throws FunctionalException {
		
//MyUtils.println("size = " + batchResultList.size());
//MyUtils.println(latestElement);
//MyUtils.println(newTargetFieldLeftValue);
		
		List<Integer> batchDatasetResultListIndexList = new ArrayList<Integer>();
		for (int rowNumber = 0; rowNumber < batchResultList.size(); rowNumber++) {
			List<DatasetValues> list = batchResultList.get(rowNumber);
			DatasetValues datasetValues = list.get(latestElement);
			String previousTargetFieldRightValue = datasetValues.joinFieldValueRight;
//MyUtils.println("previousTargetFieldRightValue = " + previousTargetFieldRightValue + ", newTargetFieldLeftValue = " + newTargetFieldLeftValue);			
			if (previousTargetFieldRightValue.equals(newTargetFieldLeftValue)) {
//MyUtils.println("match");				
				batchDatasetResultListIndexList.add(rowNumber);
			}
		}
		return batchDatasetResultListIndexList;
	}
	
	public Integer getUniqueMatchingRow (List<List<DatasetValues>> batchResultList, int latestElement, String newTargetFieldLeftValue) throws FunctionalException {
		
//		MyUtils.println("size = " + batchResultList.size());
//		MyUtils.println(latestElement);
//		MyUtils.println(newTargetFieldLeftValue);
				
		Integer index = null;
		for (int rowNumber = 0; rowNumber < batchResultList.size(); rowNumber++) {
			List<DatasetValues> list = batchResultList.get(rowNumber);
			DatasetValues datasetValues = list.get(latestElement);
			String previousTargetFieldRightValue = datasetValues.joinFieldValueRight;
//		MyUtils.println("previousTargetFieldRightValue = " + previousTargetFieldRightValue + ", newTargetFieldLeftValue = " + newTargetFieldLeftValue);			
			if (previousTargetFieldRightValue.equals(newTargetFieldLeftValue)) {
//		MyUtils.println("match");				
				index = rowNumber;
				break;
			}
		}
		return index;
	}

	
	public static StringBuffer getDescription1(List<Set<String>> currentIdSetList) {
		StringBuffer stringBuffer = new StringBuffer("size = " + currentIdSetList.size() + ": ");
		int i=0;
		for (Collection collection : currentIdSetList) {
			stringBuffer.append((i==0 ? "" : ", ") + (null!=collection ? collection.size() : null));
			i++;
		}
		return stringBuffer;
	}
	
	public static StringBuffer getDescription2(List<List<List<DatasetValues>>> batchResultListDatasetList) {
		StringBuffer stringBuffer = new StringBuffer("size = " + batchResultListDatasetList.size() + ": ");
		int i=0;
		for (Collection collection : batchResultListDatasetList) {
			stringBuffer.append((i==0 ? "" : ", ") + (null!=collection ? collection.size() : null));
			i++;
		}
		return stringBuffer;
	}
	
	/*private StringBuffer getCurrentIdListString(List<String> currentIdList) {
		StringBuffer stringBuffer = new StringBuffer();
		if (null!=currentIdList) {
			stringBuffer.append("currentIdList.size() = " + currentIdList.size() + "." + MyUtils.TAB_SEPARATOR);
			for (int rowNumber = 0; rowNumber < currentIdList.size() && rowNumber < 10; rowNumber++) {
				stringBuffer.append((rowNumber==0 ? "" : ", ") + currentIdList.get(rowNumber));
			}
			if (currentIdList.size()>20) {
				stringBuffer.append(", ...");
			}
			if (currentIdList.size()>10) {
				for (int rowNumber = 0; rowNumber < currentIdList.size() && rowNumber < 10; rowNumber++) {
					stringBuffer.append(", " + currentIdList.get(currentIdList.size()-10+rowNumber));
				}
			}
		} else {
			stringBuffer.append("null");
		}
		return stringBuffer;
	}*/
	
	private StringBuffer getResultListString(List<List<DatasetValues>> resultList) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int rowNumber = 0; rowNumber < resultList.size() && rowNumber < 10; rowNumber++) {
			stringBuffer.append(MyUtils.TAB_SEPARATOR + resultList.get(rowNumber) + MyUtils.LINE_SEPARATOR);
		}
		if (resultList.size()>20) {
			stringBuffer.append(MyUtils.TAB_SEPARATOR + "..." + MyUtils.LINE_SEPARATOR);
		}
		if (resultList.size()>10) {
			for (int rowNumber = 0; rowNumber < resultList.size() && rowNumber < 10; rowNumber++) {
				stringBuffer.append(MyUtils.TAB_SEPARATOR + resultList.get(resultList.size()-10+rowNumber) + MyUtils.LINE_SEPARATOR);
			}
		}
		stringBuffer.append(" - resultList.size() = " + resultList.size());
		return stringBuffer;
	}
	
	private StringBuffer getCurrentIdSetString(Set<String> currentIdSet) {
		StringBuffer stringBuffer = new StringBuffer();
		if (null!=currentIdSet) {
			stringBuffer.append("currentIdList.size() = " + currentIdSet.size() + "." + MyUtils.TAB_SEPARATOR);
			
			int rowNumber = 0;
			for (String id : currentIdSet) {
				if (rowNumber<10) {
					stringBuffer.append((rowNumber==0 ? "" : ", ") + id);
				}
				rowNumber++;
			}
			if (currentIdSet.size()>20) {
				stringBuffer.append(", ...");
			}
			if (currentIdSet.size()>10) {
				int size = currentIdSet.size();
				rowNumber = 0;
				for (String id : currentIdSet) {
					if (rowNumber>size-10) {
						stringBuffer.append((rowNumber==0 ? "" : ", ") + id);
					}
					rowNumber++;
				}
			}
		} else {
			stringBuffer.append("null");
		}
		return stringBuffer;
	}

	private void writeResultInitialization() throws IOException {
		
		if (!parameters.indexCreation) {
			parameters.resultWriter = new BufferedWriter(new FileWriter(new File(
					MyUtils.OUTPUT_FILES_PATH + "LinkIndexesTmp" + MyUtils.FILE_SEPARATOR + 
					(parameters.useIndex ? "ResultWithIndex_" : "ResultWithoutIndex_") + 
					MyUtils.getCurrentTimeOfDayToMillisecondAsString())));	
			
			parameters.resultWriter.write ("results:" + MyUtils.LINE_SEPARATOR);
			parameters.resultWriter.write("#" + MyUtils.TAB_SEPARATOR);
			for (int datasetNumber = 0; datasetNumber < parameters.totalDataset; datasetNumber++) {
				
				DataSource dataSource = parameters.dataSources[datasetNumber];
				if (dataSource instanceof DatabaseSchema) {
					DatabaseSchema databaseSchema = (DatabaseSchema)dataSource;
					if (!dataSource.getIsLinkIndex()) {
						SuperTable superTableResult = databaseSchema.superTable;
						for (int resultTableNumber = 0; resultTableNumber < superTableResult.tableTab.length; resultTableNumber++) {
							for (int resultFieldNumber = 0; resultFieldNumber < superTableResult.tableTab[resultTableNumber].getFieldNameTab().length; resultFieldNumber++) {
								parameters.resultWriter.write(databaseSchema.dbParam.databaseName + "." + 
										superTableResult.tableTab[resultTableNumber].getFieldNameTab()[resultFieldNumber] + MyUtils.TAB_SEPARATOR);
							}
						}
					}					
				} else {
					//TODO
				}
			}
			parameters.resultWriter.write(MyUtils.LINE_SEPARATOR);
			parameters.resultWriter.write(MyUtils.DASH_LINE + MyUtils.DASH_LINE + MyUtils.DASH_LINE + MyUtils.LINE_SEPARATOR);
		} else {
			parameters.resultWriter = new BufferedWriter(new FileWriter(parameters.createIndextemporaryFile));
		}
	}
	
	private void writeResultFinalization() throws IOException {
		if (!parameters.indexCreation) {
			parameters.resultWriter.write(totalMatchingRows + " rows");
			parameters.resultWriter.write(MyUtils.LINE_SEPARATOR);	
		}
		parameters.resultWriter.close();		
	}
}
/*MyUtils.wrappedReadInput();*/

/*
	private void displayLog(boolean interactive, Boolean lastBatch, Boolean noMatches, Boolean enoughMatchingRows, 
			int batchIndex, Integer batchSize, Integer batchRowCount, Integer temporaryMatchingRowsCount, LinkIndexesParameters parameters) {
		String batchInfo = "\t" + "s=" + batchSize + ", " +
		"r=[" + batchIndex + "-" + (batchIndex+batchSize) + "], " + 
		(noMatches ? "\t\tX\t\t" : "\t\t" + temporaryMatchingRowsCount + " matching rows (for given batch)\t\t") +		
		(lastBatch ? ", last: " + batchRowCount + " rows /" + batchSize : "") + "\n";
		if (interactive && enoughMatchingRows) {
			batchInfo+="\t\tSTOPPING: enough results\n\n";
		} else if (lastBatch) {
			batchInfo+="\t\tEND\n\n";;
		}
		MyUtils.print(batchInfo);
		
		if (parameters.printDebug) {
			lastBatch=Boolean.valueOf(MyUtils.wrappedReadInput());
		}
	}
		
		public void displayResult(LinkIndicesTestEnvironmentResult result, boolean index, LinkIndexesParameters parameters) throws SQLException {
		String tableName = index ? parameters.withIndexResultTableName : parameters.withoutIndexResultTableName;
			
		// Display results
		MyUtils.println(result.toString());
		MyUtils.println();
		
		useLocalDatabase(parameters);
		parameters.localDatabase.mySqlUtils.runExecuteQuery("select count(*) as c from " + tableName + ";");
		Integer count = (parameters.localDatabase.mySqlUtils.rs.first() ? parameters.localDatabase.mySqlUtils.rs.getInt("c") : null);
		
		if (null!=count && count>0) {
			parameters.localDatabase.mySqlUtils.runExecuteQuery("select * from " + tableName + ";");
			parameters.localDatabase.mySqlUtils.rs.first();
			MyUtils.println("\t\t" + parameters.localDatabase.mySqlUtils.rs.getString("main_id_key") + 
					"\t" + parameters.localDatabase.mySqlUtils.rs.getString("main1_desc"));
			MyUtils.println("\t\t...");
			parameters.localDatabase.mySqlUtils.rs.last();
			MyUtils.println("\t\t" + parameters.localDatabase.mySqlUtils.rs.getString("main_id_key") + 
						"\t" + parameters.localDatabase.mySqlUtils.rs.getString("main1_desc"));
			MyUtils.println();
		}
	}
	
	public StringBuffer a(List<List<DatasetValues>> list) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			stringBuffer.append(list.get(i) + MyUtils.LINE_SEPARATOR);
		}
		return stringBuffer;
	}

	public void buildIndex(String linkIndexDatabaseName, int datasetNumber1, int datasetNumber2) throws SQLException, IOException {

		useLocalDatabase();
		Integer size = null;
		
		String linkIndexTableName = parameters.datasets[datasetNumber1].dataset.withIndexJoinWrapper.joinTableSource.getTable();
		String linkIndexTableKey = parameters.datasets[datasetNumber1].dataset.withIndexJoinWrapper.joinTableSource.getSourceField();

		if (!parameters.localDatabase.mySqlUtils.tableExists(linkIndexDatabaseName, linkIndexTableName)) {
			MyUtils.println("index table did not already exists: creating and loading...");
									
			String tableToIndex1 = parameters.datasets[datasetNumber1].dataset.withIndexJoinWrapper.joinTable.getTable();
			String tableToIndex2 = parameters.datasets[datasetNumber2].dataset.withIndexJoinWrapper.joinTable.getTable();
			String fieldToIndex1 = parameters.datasets[datasetNumber1].dataset.withIndexJoinWrapper.joinTable.getRightField();
			String fieldToIndex2 = parameters.datasets[datasetNumber2].dataset.withIndexJoinWrapper.joinTable.getLeftField();
			if (parameters.displaySuperDebug) {
				MyUtils.println("linkIndexTableName = " + linkIndexTableKey + ", linkIndexTableKey = " + linkIndexTableKey + 
						", tableToIndex1 = " + tableToIndex1 + ", tableToIndex2 = " + tableToIndex2 + 
						", fieldToIndex1 = " + fieldToIndex1 + ", fieldToIndex2 = " + fieldToIndex2);}				
			
			// Compute list of common IDs
			Set<String> setID1 = fetchIdList(datasetNumber1, tableToIndex1, fieldToIndex1);
			Set<String> setID2 = fetchIdList(datasetNumber2, tableToIndex2, fieldToIndex2);
			Set<String> setCommonID = setID1;
			setCommonID.retainAll(setID2);			
			size = setCommonID.size();
			buildLinkIndexTable(linkIndexDatabaseName, linkIndexTableName, linkIndexTableKey, setCommonID);
		} else {
			MyUtils.println("index table already exists");
			size = parameters.localDatabase.mySqlUtils.countRows(linkIndexTableName);
		}
		
		// Check if result table created properly
		useLocalDatabase();
		if (!parameters.localDatabase.mySqlUtils.existsAndContains(linkIndexDatabaseName, linkIndexTableName, size)) {			
			throw new SQLException("problem creating index table " + linkIndexTableName);
		}
		
		MyUtils.println("index table: " + linkIndexTableName + ": " + parameters.localDatabase.mySqlUtils.countRows(linkIndexTableName) + " rows");
		MyUtils.println();
	}
	
	public void buildDbIndex(String databaseName, String dbIndexName, String tableToDbIndex, String fieldToDbIndex) throws SQLException {
		
		mySqlUtils.runUpdateQuery("use " + databaseName + ";");
		
		if (!mySqlUtils.indexExists(databaseName, tableToDbIndex, dbIndexName)) {
			MyUtils.println("db index did not already exists: creating and loading...");
			mySqlUtils.runUpdateQuery(LinkIndicesUtils.getCreateDbIndexQuery(dbIndexName, tableToDbIndex, fieldToDbIndex));
		} else {
			MyUtils.println("db index already exists");
		}
		
		// Check if index created properly
		if (!mySqlUtils.indexExists(databaseName, tableToDbIndex, dbIndexName)) {
			throw new SQLException("problem creating db index " + dbIndexName);
		}
		
		MyUtils.println("db index: " + dbIndexName);
		MyUtils.println();
	}//TODO broken for now

	private void buildLinkIndexTable(String linkIndexDatabaseName, String linkIndexTableName, 
			String linkIndexTableKey, Set<String> setCommonID, LinkIndexesParameters parameters) throws SQLException, IOException {
		useLocalDatabase(parameters);
		parameters.localDatabase.mySqlUtils.runUpdateQuery(LinkIndicesUtils.getDropTableIfExistsQuery(linkIndexTableName));
		parameters.localDatabase.mySqlUtils.runUpdateQuery(LinkIndicesUtils.getCreateLinkIndexTableQuery(linkIndexTableName, linkIndexTableKey));	
		
		// Populate table
		String filePath = "." + MyUtils.FILE_SEPARATOR;
		File temp = File.createTempFile(linkIndexTableName, ".txt", new File(filePath));
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(temp));
		for (String id : setCommonID) {
			bufferedWriter.write(id + MyUtils.LINE_SEPARATOR);
		}
		bufferedWriter.close();
		parameters.localDatabase.mySqlUtils.runUpdateQuery(LinkIndicesUtils.getLoadDataLocalInfileQuery(temp.getAbsolutePath(), linkIndexTableName, null));
		temp.deleteOnExit();
	}

	public void buildResultTable(boolean index, LinkIndexesParameters parameters) throws SQLException {
		String resultTableName = index ? parameters.withIndexResultTableName : parameters.withoutIndexResultTableName;
		
		useLocalDatabase(parameters);
		if (!parameters.localDatabase.mySqlUtils.tableExists(parameters.localDatabase.databaseName, resultTableName)) {
			MyUtils.println("result table does not already exists: creating");
			parameters.localDatabase.mySqlUtils.runUpdateQuery(LinkIndicesUtils.getDropTableIfExistsQuery(resultTableName));
			parameters.localDatabase.mySqlUtils.runUpdateQuery(LinkIndicesUtils.getCreateResultTableQuery(resultTableName));
		} else {
			MyUtils.println("result table already exists: emptying");
			parameters.localDatabase.mySqlUtils.runUpdateQuery("delete from " + resultTableName + ";");
		}
		
		// Check if result table created properly
		if (!parameters.localDatabase.mySqlUtils.isEmptyTable(parameters.localDatabase.databaseName, resultTableName)) {
			throw new SQLException("problem creating or emptying result table " + resultTableName);
		}
		
		MyUtils.println("result table: " + resultTableName + ": " + parameters.localDatabase.mySqlUtils.countRows(resultTableName) + " rows");
		MyUtils.println();
	}
	
	private Set<String> fetchIdList(int datasetNumber, String tableToIndex, String fieldToIndex) throws SQLException {
		useDatabase(datasetNumber);
		Set<String> setID = new TreeSet<String>();
		String query = LinkIndicesUtils.getSelectAllIDsQuery(tableToIndex, fieldToIndex);
		if (parameters.displaySuperDebug) {MyUtils.println("db " + datasetNumber + ", " +
				"query = " + (query.length()<N ? query : query.substring(0, N)) + "...");}
		parameters.datasets[datasetNumber].mySqlUtils.runExecuteQuery(query);
		parameters.datasets[datasetNumber].mySqlUtils.rs.beforeFirst();
		while (parameters.datasets[datasetNumber].mySqlUtils.rs.next()) {
			String value = parameters.datasets[datasetNumber].mySqlUtils.rs.getString(fieldToIndex);
			if (isValidValue(value)) {

//if (value.equals("ENSG00000160828") || value.equals("ENSG00000214990"))						
				setID.add(value);
			}
		}
		return setID;
	}
*/





/*



algo multibatching

	while (!lastBatch[dataset]) {
		for (dataset=dataset to total-1)
			
	
	
	
	
	}













 */

/*	public LinkIndicesTestEnvironmentResult run2(boolean useIndex, boolean createIndex, boolean distinct, LinkIndexesParameters parameters) 
throws SQLException, IOException, FunctionalException {
	
	LinkIndicesTestEnvironmentResult result = new LinkIndicesTestEnvironmentResult(
			"{index = "+String.valueOf(useIndex)+ ", interactive = "+String.valueOf(parameters.interactive) + "}");
	result.startTimer();

	if (!createIndex) {
		writeResultInitialization(useIndex, parameters);
	}
	
	// Batching by increasing size until no more records		
int t = parameters.totalDataset;
	Integer[] batchTabIndex = new Integer[t];
	Integer[] batchSize = new Integer[t];
	
	Integer[] batchIndex = new Integer[t];
	Boolean[] lastBatch = new Boolean[t];
	
	Boolean[] subBatching = new Boolean[t];
	
	for (int i = 0; i < t; i++) {
		batchTabIndex[i] = 0;
		batchSize[i] = parameters.batchTab[batchTabIndex[i]];
		batchIndex[i] = 0;
		lastBatch[i] = Boolean.FALSE;
		subBatching[i] = Boolean.FALSE;
	}
	
	Boolean noMatches = null;
	Integer datasetNumberNoMatches = null;
	
	Boolean enoughMatchingRows = Boolean.FALSE;
	Integer totalMatchingRows = 0;
	List<Integer> batchMatchingRowsCountList = new ArrayList<Integer>();
	List<List<DatasetValues>> memoryResultList = new ArrayList<List<DatasetValues>>();
	
Data[] data = new Data[parameters.totalDataset];
	
	int firstDatasetNumber = 0;
	int lastDatasetNumber = parameters.totalDataset-1;
	
	// Start batching
	boolean stop = false;	
	while (!stop && 
			((parameters.interactive && !enoughMatchingRows) || !parameters.interactive)) {
		
		MyUtils.println(MyUtils.LINE_SEPARATOR + MyUtils.EQUAL_LINE + MyUtils.LINE_SEPARATOR);
					
		List<String> currentIdList = new ArrayList<String>();	//TODO use set instead (no doublon)
		List<String> firstIndexCurrentIdList = null;
		List<List<DatasetValues>> batchResultList = new ArrayList<List<DatasetValues>>();
		Integer currentBatchMatchingRowsCount = null;
		Integer currentDatasetMatchingRowsCount = null;
		
		for (int i = 0; i < t; i++) {
			subBatching[i] = Boolean.FALSE;
		}
		
		// Going through each dataset to filter+get values
		for (int currentDatasetNumber = 0; currentDatasetNumber <= lastDatasetNumber; currentDatasetNumber++) {
						
			MyUtils.println(MyUtils.LINE_SEPARATOR + "==> " + "datasetNumber = " + currentDatasetNumber + " <==");				
			
			boolean currentIsFirstDataset = currentDatasetNumber==firstDatasetNumber;
			boolean currentIsLastDataset = currentDatasetNumber==lastDatasetNumber;
			
			currentDatasetMatchingRowsCount = 0;
				
			// If index: create an inList based on the index
			if (useIndex && !currentIsLastDataset) {	// We overwrite the currentIdList
				
				// Get the inList until it's already available (like on the 2nd iteration => currentDatasetNumber=1)
				if (currentDatasetNumber!=1) {	
				
					int indexDatasetNumber = currentIsFirstDataset ? currentDatasetNumber : currentDatasetNumber-1;
						
					// Get parameters necessary for the following query
					String sourceTable = parameters.datasets[indexDatasetNumber].dataset.withIndexJoinWrapper.joinTableSource.getTable();
					String sourceField = parameters.datasets[indexDatasetNumber].dataset.withIndexJoinWrapper.joinTableSource.getSourceField();
					if (parameters.displaySuperDebug) {MyUtils.println("sourceTable = " + sourceTable + ", sourceField = " + sourceField);}

					// Run query for index (batching for the iteration 1, then normal for the others)
					useLocalDatabase(parameters);
					String filteringClause = null;
					if (currentIsFirstDataset) {
				filteringClause=" limit " + batchIndex[currentDatasetNumber] + "," + batchSize[currentDatasetNumber];
					} else {
						filteringClause="where " + sourceField + " in(" + LinkIndicesUtils.buildInList(currentIdList) + ")";
					}
					StringBuffer queryLinkIndex = LinkIndicesUtils.getIndexQuery(filteringClause, distinct, sourceTable, sourceField);
					if (parameters.displaySuperDebug) {MyUtils.println("db local" + 
							", queryLinkIndex = " + LinkIndicesUtils.displayQuery(parameters.localDatabase.mySqlUtils, queryLinkIndex) + 
							", with " + currentIdList.size() + " elements in WHERE IN clause");}
					parameters.localDatabase.mySqlUtils.runExecuteQuery(queryLinkIndex);
					currentBatchMatchingRowsCount = parameters.localDatabase.mySqlUtils.countRowsAfterQuery();
					if (parameters.displaySuperDebug) {MyUtils.println("batchRowCount = " + currentBatchMatchingRowsCount);}
			
					// Update status variables
					if (currentIsFirstDataset) {
				lastBatch[currentDatasetNumber] = currentBatchMatchingRowsCount<batchSize[currentDatasetNumber];
					}
					noMatches = currentBatchMatchingRowsCount==0;
					if (noMatches) {
						batchResultList.clear();
						if (parameters.displaySuperDebug) {MyUtils.println("noMatches 1: breaking");}
						break;
					}
					
					// Populate join field list
					currentIdList = new ArrayList<String>();
					parameters.localDatabase.mySqlUtils.rs.beforeFirst();		
					while (parameters.localDatabase.mySqlUtils.rs.next()) {
						String sourceFieldValue = parameters.localDatabase.mySqlUtils.rs.getString(sourceField);
						if (!currentIdList.contains(sourceFieldValue)) {
							currentIdList.add(sourceFieldValue);
						}
		            }
					
					// Copy list for 2nd iteration (so we don't fetch it again)
					if (currentIsFirstDataset) {
						firstIndexCurrentIdList = new ArrayList<String>(currentIdList);
					}
				}
				// No need to get it again, it's the same as the first iteration (when currentDatasetNumber=0)
				else {
					currentIdList = firstIndexCurrentIdList;
					if (parameters.displaySuperDebug) {MyUtils.println("using same list");}
				}
				
				// Display join field list: copied (iteration 2) or created (other iterations)
				if (parameters.displaySuperDebug) {MyUtils.println("index = " + useIndex + 
						", currentIdList (1) = " + getCurrentIdListString(currentIdList));}
			}
			
			if (!subBatching[currentDatasetNumber]) {
				
				MySqlUtils mySqlUtilsRegular = parameters.datasets[currentDatasetNumber].mySqlUtils;
				Integer rowCountRegular = null;
				
				// Get values
				String targetTable = null;
				String targetFieldLeft = null;
				String targetFieldRight = null;
				String[] targetFieldNames = null;
				
				// Get parameters necessary for the following query
				if (!useIndex) {
					targetTable = parameters.datasets[currentDatasetNumber].dataset.withoutIndexJoinWrapper.joinTable.getTable();
					targetFieldLeft = parameters.datasets[currentDatasetNumber].dataset.withoutIndexJoinWrapper.joinTable.getFullLeftField();
					targetFieldRight = parameters.datasets[currentDatasetNumber].dataset.withoutIndexJoinWrapper.joinTable.getFullRightField();					
				} else {
					targetTable = parameters.datasets[currentDatasetNumber].dataset.withIndexJoinWrapper.joinTable.getTable();
					targetFieldLeft = parameters.datasets[currentDatasetNumber].dataset.withIndexJoinWrapper.joinTable.getFullLeftField();
					targetFieldRight = parameters.datasets[currentDatasetNumber].dataset.withIndexJoinWrapper.joinTable.getFullRightField();					
				}
				targetFieldNames = !createIndex ? parameters.datasets[currentDatasetNumber].dataset.tableTab[0].getFieldNameTab() : new String[0];	// TODO only handles 1 table for now
				if (parameters.displaySuperDebug) {MyUtils.println("targetTable = " + targetTable + ", targetFieldLeft = " + targetFieldLeft +
						", targetFieldRight = " + targetFieldRight + ", targetFieldNames = " + MyUtils.getStringBuffer(targetFieldNames));}
				
				// Build regular query
				StringBuffer whereInClause = null;
				StringBuffer limitClause = null;	
				StringBuffer filteringClause = null;	
				StringBuffer queryRegular = null;
				whereInClause = LinkIndicesUtils.buildWhereInClause(
						(currentIsFirstDataset ? targetFieldRight : targetFieldLeft), currentIdList);	// targetFieldRight is only for 1st dataset when using an index
				limitClause = LinkIndicesUtils.buildLimitClause(batchIndex[currentDatasetNumber], batchSize[currentDatasetNumber]);
				filteringClause = new StringBuffer();
				filteringClause.append(whereInClause);
				filteringClause.append(limitClause);
				queryRegular = LinkIndicesUtils.getQuery(distinct, 
						targetFieldLeft, (!currentIsLastDataset ? targetFieldRight : null), targetFieldNames,
						targetTable, filteringClause);

				// Run it and count results
				useDatabase(currentDatasetNumber, parameters);
				if (parameters.displaySuperDebug) {MyUtils.println("db " + currentDatasetNumber + 
						", queryRegular = " + LinkIndicesUtils.displayQuery(mySqlUtilsRegular, queryRegular) + 
						", with " + currentIdList.size() + " elements in WHERE IN clause");}
				mySqlUtilsRegular.runExecuteQuery(queryRegular);
				rowCountRegular = mySqlUtilsRegular.countRowsAfterQuery();
				if (parameters.displaySuperDebug) {MyUtils.println("rowCountRegular = " + rowCountRegular);}
						
				// Write query to result file
				if (!createIndex) {
					parameters.resultWriter.write(
							(currentIsFirstDataset ? MyUtils.DASH_LINE + MyUtils.LINE_SEPARATOR : "") +
							MyUtils.TAB_SEPARATOR + rowCountRegular + MyUtils.TAB_SEPARATOR + 
							queryRegular + MyUtils.LINE_SEPARATOR + MyUtils.LINE_SEPARATOR);
				}
			
				// Update status variables
				noMatches = rowCountRegular==0;
				datasetNumberNoMatches = noMatches ? currentDatasetNumber : null;
				if (parameters.displaySuperDebug) {
					MyUtils.println("noMatches = " + noMatches + ", datasetNumberNoMatches = " + datasetNumberNoMatches);
					MyUtils.println(!noMatches ? 
						"matches found at datasetNumber " + currentDatasetNumber : 
							"no matches after regular query at datasetNumber " + datasetNumberNoMatches);
				}
				if (!useIndex) {	// not if using index because already done then
					currentBatchMatchingRowsCount = rowCountRegular;
					lastBatch[currentDatasetNumber] = rowCountRegular<batchSize[currentDatasetNumber];					
					if (parameters.displaySuperDebug) {MyUtils.println("lastBatch[currentDatasetNumber] = " + lastBatch[currentDatasetNumber]);}
				}
				
				currentIdList = new ArrayList<String>();
				if (noMatches) {
					currentDatasetMatchingRowsCount = 0;
					currentIdList.clear();
					batchResultList.clear();
				} else {
					int progressDotCount = 1;	// for nicer display
					MyUtils.println();		// for nicer display
				
					// Populate result list (temporary for each batch) and prepare next currentIdList if not the last dataset
			//List<List<DatasetValues>> batchDatasetResultList = new ArrayList<List<DatasetValues>>();	// For new list of rows (common on both sides)					
					currentIdList = new ArrayList<String>();
					Set<Integer> batchDatasetResultListIndexSet = new TreeSet<Integer>();
					mySqlUtilsRegular.rs.beforeFirst();
					while (mySqlUtilsRegular.rs.next()) {
	
						String targetFieldRightValue = null;
						
						// If createIndex then the currentIdList is sufficient
						if (createIndex) {
							targetFieldRightValue = mySqlUtilsRegular.rs.getString(targetFieldRight);
							if (LinkIndicesUtils.isValidValue(targetFieldRightValue) && !currentIdList.contains(targetFieldRightValue)) {
								currentIdList.add(targetFieldRightValue);	// Preparing next dataset's "in list"
								if (currentIsLastDataset) {
									parameters.indexWriter.write(targetFieldRightValue+MyUtils.LINE_SEPARATOR);
									currentDatasetMatchingRowsCount+=1;
								}
							}
						} else {
							// Get and store all values of interest and skip record when obsolete record +
							// add to currentIdList (which is actually for the next iteration's "in list") only if not null and has a value 
							if (!currentIsLastDataset || (currentIsLastDataset && createIndex)) {
								targetFieldRightValue = mySqlUtilsRegular.rs.getString(targetFieldRight);
								
								// Skip this record if its right value is null or empty (since we won't be able to use it as a join)
								if (!LinkIndicesUtils.isValidValue(targetFieldRightValue)) {
									continue;	// jump to next result
								} else if (!currentIdList.contains(targetFieldRightValue)) {
									currentIdList.add(targetFieldRightValue);	// Preparing next dataset's "in list"
								}
							}
							String targetFieldLeftValue = mySqlUtilsRegular.rs.getString(targetFieldLeft);
							List<String> targetFieldValues = new ArrayList<String>();
							for (int fieldNumber = 0; fieldNumber < targetFieldNames.length; fieldNumber++) {
								targetFieldValues.add(mySqlUtilsRegular.rs.getString(targetFieldNames[fieldNumber]));
							}
							DatasetValues currentDatasetValues = new DatasetValues(targetFieldLeftValue, targetFieldRightValue, targetFieldValues);
		
							// If 1st dataset for current batch, create the batch result list from scratch
							if (currentIsFirstDataset) {
								List<DatasetValues> currentDatasetValuesList = new ArrayList<DatasetValues>();
								currentDatasetValuesList.add(currentDatasetValues);						
								batchResultList.add(currentDatasetValuesList);
								currentDatasetMatchingRowsCount+=1;	// update row count
							}
							// Otherwise
							else {
								// Obtain list of records from previous result list, that are a match for the latest filtering
								List<Integer> batchDatasetResultListIndexList = getMatchingRows(batchResultList, currentDatasetNumber-1, targetFieldLeftValue);
MyUtils.println("batchDatasetResultListIndexList = " + batchDatasetResultListIndexList);									
								batchDatasetResultListIndexSet.addAll(batchDatasetResultListIndexList);
MyUtils.println("batchDatasetResultListIndexSet = " + batchDatasetResultListIndexSet);									

								MyUtils.checkStatusProgram(!batchDatasetResultListIndexList.isEmpty(), 
										"!batchDatasetResultListIndexList.isEmpty(), batchDatasetResultListIndexList.size() = " + 
										batchDatasetResultListIndexList.size(), true);	// at least 1 row if 1-to-1 (more if many-to-1)
								
								int size = batchDatasetResultListIndexList.size();
								for (int i = 0; i < size; i++) {
									List<DatasetValues> currentListDatasetValues = batchResultList.get(batchDatasetResultListIndexList.get(i));
									currentListDatasetValues.add(currentDatasetValues);	// add latest dataset data
									progressDotCount++;
			
									if (currentIsLastDataset) {	// Write results (no more filtering needed and all values are available)
										parameters.resultWriter.write((totalMatchingRows+currentDatasetMatchingRowsCount+i) + MyUtils.TAB_SEPARATOR);
										for (int datasetNumber2 = 0; datasetNumber2 < currentListDatasetValues.size(); datasetNumber2++) {
											List<String> fieldValues = currentListDatasetValues.get(datasetNumber2).fieldValues;
											for (int columnNumber = 0; columnNumber < fieldValues.size(); columnNumber++) {
												parameters.resultWriter.write(fieldValues.get(columnNumber) + MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR);
											}
										}
										parameters.resultWriter.write(MyUtils.LINE_SEPARATOR);
									}
								}
								currentDatasetMatchingRowsCount += size;
							}
						}
					}
					
					// Remove obsolete rows
					if (!currentIsFirstDataset) {
	//MyUtils.println("batchDatasetResultListIndexList = " + batchDatasetResultListIndexList);
						for (int rowNumber = batchResultList.size()-1; rowNumber >=0; rowNumber--) {
							if (!batchDatasetResultListIndexSet.contains(rowNumber)) {
	//MyUtils.println("removing " + rowNumber);
								batchResultList.remove(rowNumber);
							}
						}
					}
					if (progressDotCount>LinkIndicesUtils.PROGRESS_DOT_FREQUENCY) {MyUtils.println();}
					
					// Update noMatches
					noMatches = (!createIndex && batchResultList.isEmpty()) || (createIndex && currentIdList.isEmpty());
					datasetNumberNoMatches = noMatches ? currentDatasetNumber : null;
					if (noMatches) {
						currentDatasetMatchingRowsCount = 0;
						currentIdList.clear();
						batchResultList.clear();
					}
					if (parameters.displaySuperDebug) {
						MyUtils.println("noMatches = " + noMatches + ", datasetNumberNoMatches = " + datasetNumberNoMatches);
						MyUtils.println(!noMatches ? 
								"matches found at datasetNumber " + currentDatasetNumber : 
									"no matches after merging results at datasetNumber " + datasetNumberNoMatches);
					}
					
//					MyUtils.checkStatusProgram(createIndex || currentIsLastDataset || (!currentIsLastDataset &&
//							null!=currentIdList && batchDatasetResultList!=null && currentIdList.size()<=batchDatasetResultList.size()), 
//							"currentIsLastDataset || (!currentIsLastDataset && " +
//							"null!=currentIdList && resultListDatasetTmp3!=null && currentIdList.size()<=resultListDatasetTmp3.size()), " +
//							"currentIsLastDataset = " + currentIsLastDataset + 
//							(null!=currentIdList ? ", currentIdList.size() = " + currentIdList.size() : "currentIdList=null") +
//							(null!=currentIdList ? ", resultListDatasetTmp3.size() = " + batchDatasetResultList.size() : "resultListDatasetTmp3=null"), true);
					
				}
				
				// Save
				data[currentDatasetNumber] = new Data(currentIdList, batchResultList, currentDatasetMatchingRowsCount); 
			} else {
				currentDatasetMatchingRowsCount = data[currentDatasetNumber].currentDatasetMatchingRowsCount;
				currentIdList = data[currentDatasetNumber].currentIdList;
				batchResultList = data[currentDatasetNumber].batchResultList;
				data[currentDatasetNumber] = null;
			}
			
			if (currentIsFirstDataset) {
				if (!createIndex) {	
					parameters.resultWriter.write(MyUtils.TAB_SEPARATOR + currentDatasetMatchingRowsCount + MyUtils.LINE_SEPARATOR);
					parameters.resultWriter.write(MyUtils.LINE_SEPARATOR + MyUtils.LINE_SEPARATOR);
				}
			}
			
			MyUtils.println();
			if (parameters.displaySuperDebug) {
				MyUtils.println(MyUtils.TAB_SEPARATOR + "-> dataset results:");
				MyUtils.println("noMatches = " + noMatches + ", datasetNumberNoMatches = " + datasetNumberNoMatches);
				MyUtils.println("batchResultList = " + getResultListString(batchResultList));
				MyUtils.println("currentIdList (regular) = " + getCurrentIdListString(currentIdList));
				MyUtils.println("useIndex = " + useIndex);
				MyUtils.println();
			}
							
			subBatching[currentDatasetNumber]=!currentIsLastDataset && !noMatches;
			if (noMatches) {
				break;
			}
		}			
		
		//batchResultList = batchDatasetResultList;
		if (storeResultMemory) {
			memoryResultList.addAll(batchResultList);
		}
		
		if (parameters.displaySuperDebug) {
			MyUtils.println("-> batch results:");
		}
			
		if (parameters.displaySuperDebug) {
			MyUtils.println("BEFORE:");
			MyUtils.println("batchIndex = " + MyUtils.getStringBuffer(batchIndex));
			MyUtils.println("batchSize = " + MyUtils.getStringBuffer(batchSize));
			MyUtils.println("batchTabIndex = " + MyUtils.getStringBuffer(batchTabIndex));
			MyUtils.println("lastBatch = " + MyUtils.getStringBuffer(lastBatch));
			MyUtils.println("subBatching = " + MyUtils.getStringBuffer(subBatching));
			MyUtils.println();
		}
		
		// Update batchIndex for current dataset and try to increase batch size until reach maximum
		Integer datasetNumber = null;
		if (noMatches) {
			batchIndex[datasetNumberNoMatches]=0;
			batchTabIndex[datasetNumberNoMatches]=0;
			batchSize[datasetNumberNoMatches] = parameters.batchTab[batchTabIndex[datasetNumberNoMatches]];
			lastBatch[datasetNumberNoMatches]=Boolean.FALSE;
			if (datasetNumberNoMatches==firstDatasetNumber) {
				stop = true;
			}
			
			datasetNumber = datasetNumberNoMatches-1;
		} else {
			datasetNumber = t-1;
		}
		for (int datasetNumberForBatch = datasetNumber; datasetNumberForBatch >=0; datasetNumberForBatch--) {
			if (!lastBatch[datasetNumberForBatch]) {
				batchIndex[datasetNumberForBatch]+=batchSize[datasetNumberForBatch];
				if (batchTabIndex[datasetNumberForBatch]<parameters.batchSizeIncrease-1) {
					batchTabIndex[datasetNumberForBatch]++;
					batchSize[datasetNumberForBatch] = parameters.batchTab[batchTabIndex[datasetNumberForBatch]];
				}
				break;
			} else { 
				batchIndex[datasetNumberForBatch]=0;
				batchTabIndex[datasetNumberForBatch]=0;
				batchSize[datasetNumberForBatch] = parameters.batchTab[batchTabIndex[datasetNumberForBatch]];
				lastBatch[datasetNumberForBatch]=Boolean.FALSE;
				if (datasetNumberForBatch==firstDatasetNumber) {
					stop = true;
				}
			}
		}
		
		if (parameters.displaySuperDebug) {
			MyUtils.println("AFTER:");
			MyUtils.println("batchIndex = " + MyUtils.getStringBuffer(batchIndex));
			MyUtils.println("batchSize = " + MyUtils.getStringBuffer(batchSize));
			MyUtils.println("batchTabIndex = " + MyUtils.getStringBuffer(batchTabIndex));
			MyUtils.println("lastBatch = " + MyUtils.getStringBuffer(lastBatch));
			MyUtils.println("subBatching = " + MyUtils.getStringBuffer(subBatching));
			MyUtils.println();
		}
		
		// Update row counts
		currentBatchMatchingRowsCount = currentDatasetMatchingRowsCount;
		batchMatchingRowsCountList.add(currentBatchMatchingRowsCount);
		totalMatchingRows+=currentBatchMatchingRowsCount;
		enoughMatchingRows=totalMatchingRows>=parameters.batchSizeBase;
		
		if (parameters.displaySuperDebug) {
			MyUtils.println("currentBatchMatchingRowsCount = " + currentBatchMatchingRowsCount);
			MyUtils.println("noMatches = " + noMatches + ", datasetNumberNoMatches = " + datasetNumberNoMatches);
			MyUtils.println("totalMatchingRows = " + totalMatchingRows);
			MyUtils.println("enoughMatchingRows = " + enoughMatchingRows);
			MyUtils.println();
		}
					
		// Logs
//		displayLog(parameters.interactive, lastBatch, noMatches, enoughMatchingRows, 
//				batchIndex, batchSize, batchRowCount, temporaryMatchingRowsCount, parameters);
		

		//MyUtils.wrappedReadInput();

	}

	
	result.setResultSize(totalMatchingRows);
	result.stopTimer();
	
	MyUtils.println("final total rows = " + totalMatchingRows + " rows - " + batchMatchingRowsCountList);
	if (!createIndex) {
		writeResultFinalization(parameters, useIndex, totalMatchingRows);
	}
	if (storeResultMemory) {
try {
			MyUtils.writeSerializedObject(memoryResultList, MyUtils.OUTPUT_FILES_PATH + MyUtils.getCurrentTimeOfDayToMillisecondAsString() + useIndex);
		} catch (TechnicalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	return result;
}*/

