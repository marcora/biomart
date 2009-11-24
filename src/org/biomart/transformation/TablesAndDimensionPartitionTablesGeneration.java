package org.biomart.transformation;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.objects.Column;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.objects.objects.Table;
import org.biomart.objects.objects.types.TableType;
import org.biomart.transformation.helpers.DatabaseCheck;
import org.biomart.transformation.helpers.DimensionPartition;
import org.biomart.transformation.helpers.DimensionPartitionNameAndKeyAndValue;
import org.biomart.transformation.helpers.NamingConventionTableName;
import org.biomart.transformation.helpers.TableNameAndKeyName;
import org.biomart.transformation.helpers.TemplateDatabaseDescription;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationParameter;
import org.biomart.transformation.helpers.TransformationUtils;
import org.biomart.transformation.helpers.TransformationVariable;


public class TablesAndDimensionPartitionTablesGeneration {
	
	// Local maps
	private Map<String, PartitionTable> dtDimensionPartitionsMap = null;
	
	// Temporary collections (just to reorder at the end)
	private List<PartitionTable> tmpPartitionTableList = null;
	private List<Table> tmpTableList = null;
	
	private TransformationParameter params = null;
	private TransformationVariable vars = null;
	
	public TablesAndDimensionPartitionTablesGeneration(TransformationParameter params, TransformationVariable vars) {
		this.dtDimensionPartitionsMap = new HashMap<String, PartitionTable>();
		
		this.tmpPartitionTableList = new ArrayList<PartitionTable>();
		this.tmpTableList = new ArrayList<Table>();
		
		this.params = params;
		this.vars = vars;
	}
	
	public void generateTablesAndDimensionPartitionTables(DatabaseCheck databaseCheck) throws TechnicalException, FunctionalException {
		
		// Generate all
		generate(databaseCheck);		
		
		// Reorder and actually add to dataset
		Collections.sort(this.tmpPartitionTableList);
		Collections.sort(this.tmpTableList);
		for (PartitionTable partitionTable : this.tmpPartitionTableList) {
			vars.getDataset().addPartitionTable(partitionTable);			
		}
		for (Table table : this.tmpTableList) {
			vars.getDataset().addTable(table);			
		}
	}
	
	public void generate(DatabaseCheck databaseCheck) throws TechnicalException, FunctionalException {
		TemplateDatabaseDescription templateDatabaseDescription = 
			databaseCheck.getTemplateDatabaseDescription(params.getTemplateName());
		List<String> tableList = templateDatabaseDescription.getTableList();
		int totalTables = tableList.size();
		int tableNumber = 0;
		
		HashMap<String, Table> mainTableKeyGuessingMainTableNameToTableMap = new HashMap<String, Table>();
		for (String tableName : tableList) {
			
			System.out.println(tableNumber + "/" + totalTables + MyUtils.TAB_SEPARATOR + "tableName = " + tableName);
			tableNumber++;
			
			// Do not treat meta tables
			if (tableName.startsWith(TransformationConstants.NAMING_CONVENTION_META_TABLES_PREFIX)) {
				continue;
			}
			
			NamingConventionTableName nctm = new NamingConventionTableName(tableName);
			boolean isMain = TransformationUtils.isMain(nctm.getTableType());
			
			Map<String, List<String>> tableColumnMap = templateDatabaseDescription.getTableColumnMap();
			HashSet<String> columnSet = new HashSet<String>(tableColumnMap.get(tableName));	// Use a set instead of a List
			MyUtils.checkStatusProgram(null!=columnSet && !columnSet.isEmpty());
			
			String key = null;
			if (!isMain) {
				
				// Get table's key from name
				for (String column : columnSet) {
					if (TransformationUtils.isKey(column)) {
						MyUtils.checkStatusProgram(key==null, 
								"tableName = " + tableName + ", columnSet = " + columnSet);	// not supposed to have more than one
						key = column;
					}
				}
				MyUtils.checkStatusProgram(key!=null, 
						"tableName = " + tableName + ", columnSet = " + columnSet);	// there must be at least one
			}
			
			// Identify dimension partitions
			boolean hasDimensionPartition = false;
			DimensionPartition dimensionPartition = new DimensionPartition(tableName, key);
			dimensionPartition.searchForPotentialDimensionTablePartition();
			dimensionPartition.lookForPatternMatches();
			if (dimensionPartition.getPartition()) {
				updateMapsAndPartitionTables(dimensionPartition);
				hasDimensionPartition = true;
			}
			
			boolean hasMainPartition = false;
			
			PartitionTable mainPartitionTable = vars.getMainPartitionTable();	// Used a lot from that point on
			
			Integer mainPartitionRowNumber = mainPartitionTable.getRowNumber(nctm.getDatasetName());
			if (null!=mainPartitionRowNumber) {
				hasMainPartition = true;
				
				/*PartitionReference mainPartitionReference = new PartitionReference(
						mainPartitionTable, MartConfiguratorConstants.MAIN_PARTITION_TABLE_MAIN_COLUMN);*/
				String newTableShortName = null;
				PartitionTable dimensionPartitionTable = dimensionPartition.getDimensionPartitionTable();
				Integer dimensionPartitionRow = dimensionPartition.getDimensionTableRowNumber();
				
				if (!hasDimensionPartition) {
					newTableShortName = nctm.getTableShortName();
				} else {
					newTableShortName = TransformationUtils.generateTableShortNameWhenDimensionPartition(dimensionPartition, dimensionPartitionTable);
				}
				
				String newTableName = new NamingConventionTableName(
						vars.getDataset().getName(), newTableShortName, nctm.getTableType()).generateTableName();
				
				TableNameAndKeyName tableNameAndKeyName = new TableNameAndKeyName(newTableName, key);
				Table table = vars.getTableFromNameAndKey(tableNameAndKeyName);
				if (null==table) {
					table = new Table(newTableName, mainPartitionTable, isMain, TableType.TARGET, key, columnSet);
					this.tmpTableList.add(table);
					vars.putNameAndKeyToTableMap(tableNameAndKeyName, table);
					
					// If a main, add to a map for guessing the key
					if (isMain) {
						mainTableKeyGuessingMainTableNameToTableMap.put(newTableName, table);
					}
				} else {
					// Add any new columns (rare)
					table.addColumns(columnSet);
				}
				// Add range
				
				if (!table.getRange().contains(mainPartitionTable, mainPartitionRowNumber)) {
					table.getRange().addRangePartitionRow(mainPartitionTable, mainPartitionRowNumber);
				}
				
				if (hasDimensionPartition && !table.getRange().contains(dimensionPartitionTable, dimensionPartitionRow)) {
					table.getRange().addRangePartitionRow(dimensionPartitionTable, dimensionPartitionRow);
				}
				
			}
			if (!TransformationUtils.checkForWarning(!hasMainPartition, vars.getUnusedTablesInDataset(),
					tableName + " has no main partition on it: unused")) {
				continue;
			}
		}
		
		// Determine what the keys of the main tables are
		
		// Create a map main table to list of potential keys
		HashMap<Table, List<String>> mainTableKeyGuessingMainTableNameToKeyListMap = new HashMap<Table, List<String>>();
		MyUtils.checkStatusProgram(mainTableKeyGuessingMainTableNameToTableMap.size()>=1);	// at least 1 main table
		for (Iterator<String> it = mainTableKeyGuessingMainTableNameToTableMap.keySet().iterator(); it.hasNext();) {
			String newTableName = it.next();
			Table table = mainTableKeyGuessingMainTableNameToTableMap.get(newTableName);
			if (table.getMain()) {
				List<String> keyList = new ArrayList<String>();
				/*HashSet<String> columnSet = table.getFields();
				for (String columnName : columnSet) {
					if (TransformationUtils.isKey(columnName)) {
						keyList.add(columnName);
					}
				}*/
				HashSet<Column> columnSet = table.getColumns();
				for (Column column : columnSet) {
					if (TransformationUtils.isKey(column.getName())) {		// if (column.getKey()) { can't work here
						keyList.add(column.getName());
					}
				}
				mainTableKeyGuessingMainTableNameToKeyListMap.put(table, keyList);
			}
		}
		
		// Assign key based on number of keys per table
		List<String> assignedKeys = new ArrayList<String>();
		MyUtils.checkStatusProgram(mainTableKeyGuessingMainTableNameToTableMap.size()==mainTableKeyGuessingMainTableNameToKeyListMap.size(),
				mainTableKeyGuessingMainTableNameToTableMap.size() + ", " + mainTableKeyGuessingMainTableNameToKeyListMap.size());		
		while (assignedKeys.size()<mainTableKeyGuessingMainTableNameToKeyListMap.keySet().size()) {
			for (Iterator<Table> it = mainTableKeyGuessingMainTableNameToKeyListMap.keySet().iterator(); it.hasNext();) {
				Table mainTable = it.next();
		
				List<String> keyList = mainTableKeyGuessingMainTableNameToKeyListMap.get(mainTable);
				MyUtils.checkStatusProgram(keyList!=null/* && keyList.isEmpty()*/, "mainTable = " + mainTable.getName() + ", keyList = " + keyList);
				
				// Remove any assigned key
				for (String assignedKey : assignedKeys) {
					keyList.remove(assignedKey);					
				}
		
				// When only 1 key left, assign it
				if (keyList.size()==1) {
					String keyName = keyList.get(0).toLowerCase();
					
					// Assign to the table
					mainTable.setKey(keyName);
					
					// Update key name to map table (so that when we encounter a tableConstraint="main", we can get the appropriate table thanks to the key)
					NamingConventionTableName nctm = new NamingConventionTableName(mainTable.getName());
					vars.addKeyNameToMainTableShortNameMap(keyName, nctm.getTableShortName());
					
					// Update the map to retrieve the table
					//Map<TableNameAndKeyName, Table> nameAndKeyToTableMap = vars.getNameAndKeyToTableMap();
					vars.updateNameAndKeyToTableMap(mainTable, keyName);
					
					// Set key as assigned so it can be disregarded for the other tables and allow guessing for the next one
					assignedKeys.add(keyName);
				}
			}
		}

		// Rename all conflicting dimension partition table
		/*renameConflictingPartitionTables();*/	//TODO think about it
	}

	public void updateMapsAndPartitionTables(DimensionPartition dimensionPartition) {
		
		DimensionPartitionNameAndKeyAndValue dimensionPartitionNameAndKeyAndValue = dimensionPartition.getDimensionPartitionNameAndKeyAndValue();
		String dimensionPartitionName = dimensionPartitionNameAndKeyAndValue.getDimensionName();
		String value = dimensionPartitionNameAndKeyAndValue.getValue();
		
		PartitionTable partitionTable = this.dtDimensionPartitionsMap.get(dimensionPartitionName);
		Integer rowNumber = null; 
		
		
		if (null==partitionTable) {
			
			// In case a dimension table is called "m" as well (we would need to rename the main partition table)
			/*PartitionTable mainPartitionTable = vars.getMainPartitionTable();
			String mainPartitionTableName = mainPartitionTable.getName();
			if (dimensionPartitionName.equals(mainPartitionTableName)) {
				vars.getNameToPartitionTableMap().remove(mainPartitionTable.getName());
				mainPartitionTable.setName(mainPartitionTableName + "0");
				vars.getNameToPartitionTableMap().put(mainPartitionTable.getName(), mainPartitionTable);
				
				// Must update all the maps 	TODO painful... + we may update things that aren't partitionReference
				this.tmpTableList.add(table);
				vars.putNameAndKeyToTableMap(tableNameAndKeyName, table);
				mainTableKeyGuessingMainTableNameToTableMap.put(newTableName, table);
			}*/	
			
			partitionTable = new PartitionTable(dimensionPartitionName, value, false);
			this.dtDimensionPartitionsMap.put(dimensionPartitionName, partitionTable);	// Should use a PartitionTableNameAndKey (and rename to TableNameAndKey)
			this.tmpPartitionTableList.add(partitionTable);
			MyUtils.checkStatusProgram(!vars.getNameToPartitionTableMap().keySet().contains(partitionTable.getName()), partitionTable.getName());
			vars.getNameToPartitionTableMap().put(partitionTable.getName(), partitionTable);
			rowNumber = 0;
		} else {
			
			rowNumber = partitionTable.getRowNumber(value);
			
			if (rowNumber==null) {
				// Update the partition table
				rowNumber = partitionTable.addRow(value);
			}
		}
		
		dimensionPartition.setDimensionPartitionTable(partitionTable);
		dimensionPartition.setDimensionTableRowNumber(rowNumber);
		dimensionPartition.setDimensionTableRowName(value);
		vars.getDimensionPartitionsMap().put(dimensionPartitionNameAndKeyAndValue, dimensionPartition);
	}
	
	public String computeCentralTableName(List<Table> tableList) throws TechnicalException, FunctionalException {
		
		String centralTableName = null;
		for (Table table : tableList) {
			if (table.getMain()) {
				int totalKeys = 0;
				/*HashSet<String> fields = table.getFields();
				for (String field : fields) {
					if (TransformationUtils.isKey(field)) {
						totalKeys++;
						if (totalKeys>1) {	// Useless, centralTable only has 1 key
							break;
						}
					}
				}*/
				HashSet<Column> columns = table.getColumns();
				for (Column column : columns) {
					if (TransformationUtils.isKey(column.getName())) {		// if (column.getKey()) { can't work here
						totalKeys++;
						if (totalKeys>1) {	// Useless, centralTable only has 1 key
							break;
						}
					}
				}
				if (totalKeys==1) {
					MyUtils.checkStatusProgram(centralTableName==null, 
							"centralTableName = " + centralTableName + ", table.getName() = " + table.getName());
					centralTableName = table.getName(); 
				}
			}
		}
		MyUtils.checkStatusProgram(centralTableName!=null, "tableList = " + tableList);
		return centralTableName;
	}
	
	// For now: to keep
	/*@SuppressWarnings("unused")
	private void renameConflictingPartitionTables() {
		Map<String, List<String>> dimensionNameOccurencesMap = new HashMap<String, List<String>>();
		for (PartitionNameAndKeyName partitionNameAndKeyName : this.dtDimensionPartitionsMap.keySet()) {
			String tableName = partitionNameAndKeyName.getPartitionName();
			String keyName = partitionNameAndKeyName.getKeyName();
			List<String> keysList = dimensionNameOccurencesMap.get(tableName);
			if (null==keysList) {
				keysList = new ArrayList<String>();
			}
			keysList.add(keyName);
			dimensionNameOccurencesMap.put(tableName, keysList);
		}
		for (Iterator<String> it = dimensionNameOccurencesMap.keySet().iterator(); it.hasNext();) {
			String tableName = it.next();
			List<String> keysList = dimensionNameOccurencesMap.get(tableName);
			if (keysList.size()>1) {
				int index = 0;
				for (Iterator<PartitionNameAndKeyName> it2 = this.dtDimensionPartitionsMap.keySet().iterator(); it2.hasNext();) {
					PartitionNameAndKeyName partitionNameAndKeyName = it2.next();
					if (partitionNameAndKeyName.getPartitionName().equals(tableName)) {
						PartitionTable partitionTable = this.dtDimensionPartitionsMap.get(partitionNameAndKeyName);
						String originalPartitionTableName = partitionTable.getName();
						String key = keysList.get(index);
						
						String newPartitionTableName = originalPartitionTableName + (index+1);
						partitionTable.setName(newPartitionTableName);
						partitionTable.setJdomComment(" key = " + key + " ");

						// FIXME not great
						// For now must manually go and change name of the equivalent <table>s' names (still Strings)
						for (Table table : tmpTableList) {
							
							if (!table.getMain() && table.getKey().equals(key) && MartConfiguratorUtils.detectPartitionReference(
									MartConfiguratorConstants.PARTITION_REFERENCE_PATTERN, table.getName())) {
							
								List<String> list = MartConfiguratorUtils.extractPartitionReferences(
										MartConfiguratorConstants.PARTITION_REFERENCE_PATTERN, table.getName());
								StringBuffer stringBuffer = new StringBuffer();
								for (int i = 0; i < list.size(); i++) {
									String string = list.get(i);
									if (i%2==1) {	// So we don't update something else than a partition reference
										string = string.replace(originalPartitionTableName, newPartitionTableName);
									}
									stringBuffer.append(string);
								}
								table.setName(stringBuffer.toString());
							}
						}
						
						index++;
					}
				}
			}
		}
	}*/
}
