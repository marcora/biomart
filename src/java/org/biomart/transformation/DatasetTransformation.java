package org.biomart.transformation;


import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.data.FilterData;
import org.biomart.objects.data.TreeFilterData;
import org.biomart.objects.helpers.PartitionReference;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.GroupFilter;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.objects.objects.Range;
import org.biomart.objects.objects.Relation;
import org.biomart.objects.objects.RelationType;
import org.biomart.objects.objects.SimpleFilter;
import org.biomart.objects.objects.Table;
import org.biomart.objects.objects.TableType;
import org.biomart.transformation.helpers.ContainerPath;
import org.biomart.transformation.helpers.NamingConventionTableName;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationGeneralVariable;
import org.biomart.transformation.helpers.TransformationHelper;
import org.biomart.transformation.helpers.TransformationParameter;
import org.biomart.transformation.helpers.TransformationUtils;
import org.biomart.transformation.helpers.TransformationVariable;
import org.biomart.transformation.oldXmlObjects.OldAttributePage;
import org.biomart.transformation.oldXmlObjects.OldDatasetConfig;
import org.biomart.transformation.oldXmlObjects.OldDynamicDataset;
import org.biomart.transformation.oldXmlObjects.OldElement;
import org.biomart.transformation.oldXmlObjects.OldFilterPage;
import org.biomart.transformation.oldXmlObjects.OldNode;


public class DatasetTransformation {

	private TablesAndDimensionPartitionTablesGeneration tablesAndDimensionPartitionTablesGeneration = null;	

	private Dataset dataset = null;

	private TransformationGeneralVariable general = null;
	private TransformationParameter params = null;
	private TransformationVariable vars = null;
	private TransformationHelper help = null;

	private PortableTransformation portableTransformation = null;
	private AttributeTransformation attributeTransformation = null;
	private FilterTransformation filterTransformation = null;
	private PointerTransformation pointerTransformation = null;
	
	public DatasetTransformation (TransformationGeneralVariable general, TransformationParameter params, 
			TransformationVariable vars, TransformationHelper help) throws TechnicalException {
		this.general = general;
		this.params = params;
		this.vars = vars;
		this.help = help;
		
		this.pointerTransformation = new PointerTransformation(general, params, vars, help);
		this.attributeTransformation = new AttributeTransformation(general, params, vars, help, this.pointerTransformation);
		this.filterTransformation = new FilterTransformation(general, params, vars, help, this.pointerTransformation);
		this.portableTransformation = new PortableTransformation(params, vars, help);
	}

	public Dataset transformDataset(OldDatasetConfig oldDatasetConfig) throws FunctionalException, TechnicalException {
		
		// Transform and combine the DynamicDataset nodes into a partitionTable, and populate maps to ease transformation
		PartitionTable mainPartitionTable = null;
		String newDatasetName = null;
		if (vars.isTemplate()) {
			List<OldDynamicDataset> oldDynamicDatasetList = oldDatasetConfig.getOldDynamicDatasetList();
			String fixedPart = OldDynamicDataset.extractFixedPart(oldDynamicDatasetList);
			mainPartitionTable = OldDynamicDataset.transformToPartitionTable(fixedPart,	
					oldDynamicDatasetList, vars.getDdptColumnNumberToColumnNameMap(), vars.getDdptColumnNameToColumnNumberMap());
			vars.setDdPT(mainPartitionTable);
			
			// Update dataset name according to the main partition table
			newDatasetName = mainPartitionTable.getReference(
					TransformationConstants.DYNAMIC_DATASET_PARTITION_TABLE_DATASET_NAME_VARIABLE_PART_COLUMN_NUMBER) +
														// variable part of the dataset name is stored on this column
					fixedPart;		// Name becomes (PmC1)fixedPart, eg: hsapiens_gene_ensembl		
		} else {
			String datasetOriginalName = oldDatasetConfig.getDataset();
			mainPartitionTable = createDefaultPartitionTable(datasetOriginalName);
			vars.setDefaultPT(mainPartitionTable);
			vars.setDefaultPartitionReference(
					new PartitionReference(mainPartitionTable, TransformationConstants.WEBSERVICE_DEFAULT_PARTITION_TABLE_ROW));
			newDatasetName = datasetOriginalName;	// non-template: dataset name remains the same
		}
/*for (int i = 0; i < mainPartitionTable.getTotalRows(); i++) {
	System.out.println(mainPartitionTable.getRowName(i));
}
MyUtils.pressKeyToContinue();*/
		
		// Transform DatasetConfig node and create a default config based on it
		this.dataset = oldDatasetConfig.transformToDataset(newDatasetName, help);
		Config config = oldDatasetConfig.transformToConfig();
		vars.getCurrentPath().setDatasetAndConfig(dataset, config);
		vars.setDataset(this.dataset);
		
		dataset.addPartitionTable(mainPartitionTable);
		MyUtils.checkStatusProgram(!vars.getNameToPartitionTableMap().keySet().contains(mainPartitionTable.getName()));
		vars.getNameToPartitionTableMap().put(mainPartitionTable.getName(), mainPartitionTable);
		vars.setMainPartitionTable(mainPartitionTable);
		
		// If template, generate database info from database, accounting for main partition
		if (vars.isTemplate()) {
			this.tablesAndDimensionPartitionTablesGeneration = new TablesAndDimensionPartitionTablesGeneration(params, vars);
			this.tablesAndDimensionPartitionTablesGeneration.generateTablesAndDimensionPartitionTables(general.getDatabaseCheck());
			String centralTableName = this.tablesAndDimensionPartitionTablesGeneration.computeCentralTableName(this.dataset.getTableList());
			this.dataset.setCentralTable(centralTableName);
		}
		
		// Transform the <MainTable> & <Key>
		int mainTablesListSize = oldDatasetConfig.getOldMainTableList().size();	// the 2 list are necessary the same size
		MyUtils.checkStatusProgram(!params.isWebservice() || mainTablesListSize==oldDatasetConfig.getOldKeyList().size());
																// Only critical for web service
		for (int i = 0; i < mainTablesListSize; i++) {
			OldNode oldMainTable = oldDatasetConfig.getOldMainTableList().get(i);
			OldNode oldKey = oldDatasetConfig.getOldKeyList().get(i);
			
			if (!vars.isTemplate()) { // Add it if non-template
				Table table = transformMainTableAndKey(oldDatasetConfig.getDataset(), oldMainTable, oldKey);		
				this.dataset.addTable(table);
				
				// Assumption that MainTables are listed in order: first the main main, then it's submain, then the submain's submain, and so on
				if (i==0) {
					this.dataset.setCentralTable(table.getName());
				}
			}
			//TODO check consistency with DB ("encode" will be a problem...)
		}
		
		// Transform the filter pages, group & collections (not the composing filters -> later)
		Map<ContainerPath, List<OldElement>> oldFilterDescriptionMap = new LinkedHashMap<ContainerPath, List<OldElement>>();
		for (OldFilterPage oldFilterPage : oldDatasetConfig.getOldFilterPageList()) {
			Container container = oldFilterPage.transform(oldFilterDescriptionMap);//TODO
			config.addContainer(container);
		}
		
		// Transform the attribute pages, group & collections (not the composing attributes -> later)
		Map<ContainerPath, List<OldElement>> oldAttributeDescriptionMap = new LinkedHashMap<ContainerPath, List<OldElement>>();
		for (OldAttributePage oldAttributePage : oldDatasetConfig.getOldAttributePageList()) {
			Container container = oldAttributePage.transform(oldAttributeDescriptionMap);
			config.addContainer(container);
		}
		dataset.addConfig(config);
		
		// Transform all the filter descriptions
		filterTransformation.transformElementsDescriptions(attributeTransformation, oldFilterDescriptionMap);
		
		// Transform all the attribute descriptions
		attributeTransformation.transformElementsDescriptions(filterTransformation, oldAttributeDescriptionMap);
		
		// Transform any pointed remote dataset
		pointerTransformation.transformPointedDatasets();
		
		// Update pointers (now that all local pointed elements have been created and all pointed dataset have been transformed)
		pointerTransformation.updatePointers();
				
		// Transform the importables
		this.portableTransformation.transformImportables(config, oldDatasetConfig.getOldImportableList());	
		
		// Transform the exportables
		this.portableTransformation.transformExportables(config, oldDatasetConfig.getOldExportableList());

		// Handle filters with a specified filter list
		filterTransformation.updateFiltersWithFilterList();
		
		if (!vars.isTemplate()) {
			// Add fields from main tables into their submains, assuming their listed in order in the old config
			populateSubmainTablesWithSuperMainFields(this.dataset.getTableList());
		}
		
		// Add relations based on tables/keys
		generateRelations(this.dataset.getTableList());
		
		// Write filter data files
		writeDataFiles();

		// Display warnings
		displayWarnings();
		
		return dataset;
	}
	
	private void populateSubmainTablesWithSuperMainFields(List<Table> tableList) {
		for (int i = 0; i < tableList.size(); i++) {	// They have been added in order
			Table table = tableList.get(i);
			if (table.getMain()) {
				for (int j = i+1; j < tableList.size(); j++) {
					Table table2 = tableList.get(j);
					if (table2.getMain()) {
						table2.addFields(new HashSet<String>(table.getFields()));
					}
				}
			}
		}
	}

	private void generateRelations(List<Table> tableList) {
		
		// In the case of db-transfo, infer relations among main tables from the number of "_key" fields
		if (vars.isTemplate()) {
			HashMap<Table, Integer> map = new HashMap<Table, Integer>();
			for (Table table : tableList) {
				if (table.getMain()) {
					HashSet<String> fields = table.getFields();
					map.put(table, 0);
					for (String field : fields) {
						if (TransformationUtils.isKey(field)) {
							Integer totalKeys = map.get(table);
							MyUtils.checkStatusProgram(totalKeys!=null, "table = " + table);
							map.put(table, totalKeys+1);
						}
					}
				}
			}
			
			Table[] orderedMainTables = new Table[map.size()];
			for (Iterator<Table> it = map.keySet().iterator(); it.hasNext();) {
				Table table = it.next();
				Integer totalKeys = map.get(table);
				MyUtils.checkStatusProgram(orderedMainTables[totalKeys-1]==null, 
						(orderedMainTables[totalKeys-1]!=null ? orderedMainTables[totalKeys-1].getName() : null) + ", " + table.getName());
				orderedMainTables[totalKeys-1] = table;	// -1 because arrays start at 0 while the first total of keys is 1
			}
			
			for (int i = 0; i < orderedMainTables.length-1; i++) {
				addRelation(orderedMainTables[i+1], orderedMainTables[i]);
			}
		} else {
			// In the case of web-transfo, follow the order in which they have been added
			for (int i = 0; i < tableList.size(); i++) {
				Table table = tableList.get(i);
				if (table.getMain()) {
					if (i<tableList.size()-1) {
						Table table2 = tableList.get(i+1);
						if (table2.getMain()) {
							addRelation(table2, table);
						}
					}
				}
			}
		}
		
		for (Table table : tableList) {
			if (table.getMain()) {
				String mainKey = table.getKey();
				for (Table table2 : tableList) {
					if (!table2.getMain()) {
						HashSet<String> fieldsDimension = table.getFields();
						if (fieldsDimension.contains(mainKey)) {
							addRelation(table2, table);
						}
					}
				}
			}
		}
	}

	private void addRelation(Table sourceTable, Table targetTable) {
		String relationName = sourceTable.getName() + "/" + targetTable.getName();
		Relation relation = new Relation(relationName, targetTable, sourceTable, 
				targetTable.getKey(), targetTable.getKey(), RelationType.ONE_TO_MANY);
		this.dataset.addRelation(relation);	
	}

	private PartitionTable createDefaultPartitionTable(String datasetOriginalName) {
		PartitionTable mainPartitionTable;
		mainPartitionTable = new PartitionTable(
				MartConfiguratorConstants.MAIN_PARTITION_TABLE_DEFAULT_NAME, datasetOriginalName, true);
		return mainPartitionTable;
	}
	
	private Table transformMainTableAndKey(String datasetOriginalName, OldNode oldMainTable, OldNode oldKey) throws TechnicalException {
		String newTableName = oldMainTable.getText();
		String keyName = oldKey.getText();
		NamingConventionTableName nctn = new NamingConventionTableName(newTableName);
		MyUtils.checkStatusProgram(datasetOriginalName.equalsIgnoreCase(nctn.getDatasetName()) && 
				TransformationConstants.NAMING_CONVENTION_MAIN_TABLE_CONSTRAINT.equalsIgnoreCase(nctn.getTableType()),
				datasetOriginalName + ", " + nctn.getDatasetName() + ", " + nctn.getTableType());

		// Update name and key to main table map
		vars.addKeyNameToMainTableShortNameMap(keyName, nctn.getTableShortName());
		
		Range range = new Range(false);
		range.addRangePartitionRow(vars.getMainPartitionTable(), TransformationConstants.WEBSERVICE_DEFAULT_PARTITION_TABLE_ROW);
		
		Table table = new Table(newTableName, true, TableType.TARGET, keyName, new HashSet<String>(Arrays.asList(new String[] {keyName})));
		table.setRange(range);
		
		return table;
	}

	private void writeDataFiles() throws TechnicalException {
		for (Iterator<Filter> it = vars.getFilterMap().values().iterator(); it.hasNext();) {
			Filter filter = it.next();
			
			if (filter instanceof SimpleFilter) {
				SimpleFilter simpleFilter = (SimpleFilter)filter;
				if (null!=simpleFilter.getDataFolderPath()) {
					if (!simpleFilter.getTree()) {
						FilterData listFilterData = simpleFilter.getFilterData();
						/*MyUtils.checkStatusProgram(null!=simpleFilter.getDataFolderPath() && null!=listFilterData,
								(null!=simpleFilter.getDataFolderPath()) + ", " + (null!=listFilterData));*/
						listFilterData.writeFile();	// listFilterData is fully populated by now
					} else {
						TreeFilterData treeFilterData = simpleFilter.getTreeFilterData();
						/*MyUtils.checkStatusProgram(null!=simpleFilter.getDataFolderPath() && null!=treeFilterData,
								(null!=simpleFilter.getDataFolderPath()) + ", " + (null!=treeFilterData));*/
						treeFilterData.writeFile();	// treeFilterData is fully populated by now
					}
				}
			} else {
				GroupFilter groupFilter = (GroupFilter)filter;
				if (null!=groupFilter.getDataFolderPath()) {
					FilterData listFilterData = groupFilter.getFilterData();
					listFilterData.writeFile();	// listFilterData is fully populated
				}
			}
		}
	}
	
	private void displayWarnings() throws TechnicalException {
		try {
			String mainMessage = "Warnings/Errors for dataset \"" + params.getDatasetOrTemplateName() + "\"";
			System.out.println(mainMessage);
			params.getErrorFileWriter().write(mainMessage + MyUtils.LINE_SEPARATOR + MyUtils.LINE_SEPARATOR);
			
			displayWarningList(vars.getUnusedTablesInDataset(), "Unused tables in dataset (no main partition on it based on aliases):");
			displayWarningList(vars.getObsoleteElementList(), "Obsolete elements (no table+key+field combination in database):");
			displayWarningList(vars.getPointingToGenomicSequenceWarningList(), "Elements pointing to genomic sequence:");
			displayWarningList(vars.getUnexistingPointedDatasetWarningList(), "Elements pointing to unexisting datasets:");
			displayWarningList(vars.getUnexistingPointedElementWarningList(), "Elements pointing to unexisting elements:");
			displayWarningList(vars.getNameConflictWarningList(), "Name conflicts on the following names:");
			displayWarningList(vars.getPortableReferencesAnInvalidElementList(), "Exporables refering to invalid attributes:");
			displayWarningList(vars.getIgnoredPushActions(), "Extra push action values specified that will be ignored:");
			
			System.out.println();
			params.getErrorFileWriter().write(MyUtils.LINE_SEPARATOR + MyUtils.EQUAL_LINE + MyUtils.LINE_SEPARATOR + MyUtils.LINE_SEPARATOR);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
	}

	private void displayWarningList(List<String> warningList, String warningDescription) throws TechnicalException {
		try {
			System.out.println(warningDescription);
			params.getErrorFileWriter().write(warningDescription + MyUtils.LINE_SEPARATOR);
			for (String warningMessage : warningList) {
				System.out.println(MyUtils.TAB_SEPARATOR + warningMessage);
				params.getErrorFileWriter().write(MyUtils.TAB_SEPARATOR + warningMessage + MyUtils.LINE_SEPARATOR);
			}
			System.out.println();
			params.getErrorFileWriter().write(MyUtils.LINE_SEPARATOR);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
	}
	
	


	/*private void updateDimensionPartitionTemplateAttribute(Attribute templateAttribute, 
			Attribute newAttribute, DimensionPartition dimensionPartitionString rangeValue) throws FunctionalException {
		
		PartitionTable dimensionPartitionTable = dimensionPartition.getDimensionPartitionTable();
		Integer dimensionPartitionTableRowNumber = dimensionPartition.getDimensionTableRowNumber();
		Integer dimensionPartitionTableRowName = dimensionPartition.getDimensionTableRowNumber();
		
		MyUtils.checkStatusProgram(dimensionPartitionTableRowNumber!=null && dimensionPartitionTableRowName!=null);
		
		templateAttribute.setDisplayName(updateProperty2(
				templateAttribute.getDisplayName(), newAttribute.getDisplayName(), dimensionPartitionTable, dimensionPartitionTableRowNumber));
	}
	
	private String updateProperty2(String templateProperty, String newProperty, 
			PartitionTable dimensionPartitionTable, int row) throws FunctionalException {
		
		String property = templateProperty;
		
		List<String> listTemplate = MartConfiguratorUtils.extractPartitionReferences(templateProperty);
		List<String> listNew = MartConfiguratorUtils.extractPartitionReferences(newProperty);
		boolean noPartitionReferenceTemplate = listTemplate.size()==1;
		boolean noPartitionReferenceNew = listNew.size()==1;
		boolean templateIsPartitionReference = (listTemplate.size()==3 && listTemplate.get(0).isEmpty() && listTemplate.get(2).isEmpty());
		boolean newIsPartitionReference = (listNew.size()==3 && listNew.get(0).isEmpty() && listNew.get(2).isEmpty());
		boolean validTemplate = noPartitionReferenceTemplate || templateIsPartitionReference;
		boolean validNew = noPartitionReferenceNew || newIsPartitionReference;
		if (!validTemplate || !validNew) {
			throw new FunctionalException("Unhandled");
		}
		
		MyUtils.checkStatusProgram(!newIsPartitionReference);
		if (templateIsPartitionReference) {
			PartitionReference partitionReferenceTemplate = PartitionReference.fromString(listTemplate.get(1));
			
			property = templateProperty;
			
			// Update partition table with new value: the new one (make sure empty slot)
			dimensionPartitionTable.updateValue(row, partitionReferenceTemplate.getColumn(), newProperty);
			
		} else if (!CompareUtils.same(templateProperty, newProperty)) {
			int newColumn = dimensionPartitionTable.addColumn();
			
			PartitionReference dimensionPartitionReference = new PartitionReference(dimensionPartitionTable, newColumn);
			
			property = dimensionPartitionReference.toXmlString();
			
			updatePartitionTable(dimensionPartitionTable, row, newColumn, templateProperty, newProperty);
		}
		
		return property;
	}
	
	private String updateProperty(Integer currentMainRow, String templateProperty, String newProperty) throws FunctionalException {
		
		String property = templateProperty;
		
		boolean referenceInTemplate = MartConfiguratorUtils.containsPartitionReferences(templateProperty);
		boolean referenceInNew = MartConfiguratorUtils.containsPartitionReferences(newProperty);
		
		PartitionTable mainPartitionTable = vars.getMainPartitionTable();
		if (referenceInTemplate || referenceInNew) {
			
			List<String> listPreExisting = MartConfiguratorUtils.extractPartitionReferences(templateProperty);
			List<String> listNew = MartConfiguratorUtils.extractPartitionReferences(newProperty);
			
			// If different
			if (CompareUtils.compareStringList(listPreExisting, listNew)!=0) {
				if (referenceInTemplate && referenceInNew) {
					if (listPreExisting.size()!=listNew.size()) {
						throw new FunctionalException("Unhandled, list1 = " + listPreExisting + ", list2 = " + listNew);
					} else {
						if (listPreExisting.size()!=3 || !CompareUtils.same(listPreExisting, listNew)) {
							throw new FunctionalException("Unhandled, list1 = " + listPreExisting + ", list2 = " + listNew);
						}
						
						String reference = listPreExisting.get(1);
						int column = MartConfiguratorUtils.extractColumnNumberFromPartitionReferenceString(reference);
						
						String newPartitionTableValue = listNew.get(1);
						
						// Update partition table with new value: the new one (make sure empty slot)
						mainPartitionTable.updateValue(currentMainRow, column, newPartitionTableValue);
					}
				} else if (referenceInTemplate) {	// and !referenceInNew
					
					if (listPreExisting.size()!=3 || !listPreExisting.get(0).isEmpty() || !listPreExisting.get(2).isEmpty()) {
																				// only handled case is [, ref, ] == property="(PxCy)"
						throw new FunctionalException("Unhandled, listPreExisting = " + listPreExisting + ", listNew = " + listNew);
					}
					String reference = listPreExisting.get(1);
					int column = MartConfiguratorUtils.extractColumnNumberFromPartitionReferenceString(reference);
					
					// Update partition table with new value: the new one (make sure empty slot)
					mainPartitionTable.updateValue(currentMainRow, column, newProperty);
					
				} else if (referenceInNew) {	// and !referenceInPreExisting
					throw new FunctionalException("Unhandled");
				}
			} // else nothing to do, both the same
		} else {
			if (!CompareUtils.same(templateProperty, newProperty)) {
				MyUtils.checkStatusProgram(null!=currentMainRow);
				
				int newColumn = mainPartitionTable.addColumn();
				
				PartitionReference mainPartitionReference = new PartitionReference(mainPartitionTable, newColumn);
				
				property = mainPartitionReference.toXmlString();
				
				updatePartitionTable(mainPartitionTable, currentMainRow, newColumn, templateProperty, newProperty);
			}
		}
		
		return property;
	}

	private void updatePartitionTable(PartitionTable partitionTable, Integer currentRow, int column, String preExistingProperty, String newProperty) {
		// Update all rows with pre-existing value
		for (int row = 0; row < partitionTable.getTotalRows(); row++) {
			partitionTable.updateValue(row, column, preExistingProperty);					
		}
		// Update specific row
		partitionTable.updateValue(currentRow, column, newProperty);
	}*/
}
