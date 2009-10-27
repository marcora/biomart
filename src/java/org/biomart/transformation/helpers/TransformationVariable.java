package org.biomart.transformation.helpers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.helpers.CurrentPath;
import org.biomart.objects.helpers.PartitionReference;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.GroupFilter;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.objects.objects.Table;



public class TransformationVariable {

	public static void main(String[] args) {}

	private CurrentPath currentPath = null;
	private PartitionTable mainPartitionTable = null;
	private PartitionReference defaultPartitionReference = null;	// Only for web-service
	private PartitionTable ddPT = null;
	private PartitionTable defaultPT = null;
	private List<Table> tableList = null;
	private Dataset dataset = null;
	
	private Boolean template = null;
	
	private HashMap<String, Attribute> attributeMap = null;
	private HashMap<String, Filter> filterMap = null;
	
	private ArrayList<String> unusedTablesInDataset = null;	// no main partition on it based on the aliases
	private ArrayList<String> obsoleteElementList = null;	// no combination of table+field exists in DB
	private ArrayList<String> pointingToGenomicSequenceWarningList = null;
	private ArrayList<String> unexistingPointedDatasetWarningList = null;
	private ArrayList<String> unexistingPointedElementWarningList = null;
	private ArrayList<String> nameConflictWarningList = null;
	private ArrayList<String> portableReferencesAnInvalidElementList = null;
	private ArrayList<String> ignoredPushActions = null;
	private ArrayList<String> miscellaneousErrorList = null;
	
	protected Map<String, PartitionTable> nameToPartitionTableMap = null;
	
	protected Map<TableNameAndKeyName, Table> nameAndKeyToTableMap = null;
	
	// For template only
	private Map<String, String> keyNameToMainTableShortNameMap = null;
	private Map<DimensionPartitionNameAndKeyAndValue, DimensionPartition> dimensionPartitionsMap = null;
	private Map<Integer, String> ddptColumnNumberToColumnNameMap = null;
	private Map<String, Integer> ddptColumnNameToColumnNumberMap = null;
	
	// For filter with explicit filter list
	private Map<GroupFilter, List<String>> filterWithFilterList = null;
	
	// For pointers
	private HashSet<String> pointedDatasetPlainNamesSet = null;
	private HashSet<String> pointedDatasetGenericNamesSet = null;
	private List<PointerElementInfo> pointerElementList = null;
	
	public TransformationVariable() {
		super();
		
		this.attributeMap = new HashMap<String, Attribute>();
		this.filterMap = new HashMap<String, Filter>();
		
		this.unusedTablesInDataset = new ArrayList<String>();
		this.obsoleteElementList = new ArrayList<String>();
		this.pointingToGenomicSequenceWarningList = new ArrayList<String>();
		this.unexistingPointedDatasetWarningList = new ArrayList<String>();
		this.unexistingPointedElementWarningList = new ArrayList<String>();
		this.nameConflictWarningList = new ArrayList<String>();
		this.portableReferencesAnInvalidElementList = new ArrayList<String>();
		this.ignoredPushActions = new ArrayList<String>();
		this.miscellaneousErrorList = new ArrayList<String>();
		
		this.nameToPartitionTableMap = new HashMap<String, PartitionTable>();
		this.nameAndKeyToTableMap = new HashMap<TableNameAndKeyName, Table>();
		
		this.keyNameToMainTableShortNameMap = new HashMap<String, String>();
		this.dimensionPartitionsMap = new HashMap<DimensionPartitionNameAndKeyAndValue, DimensionPartition>();
		this.ddptColumnNumberToColumnNameMap = new TreeMap<Integer, String>();
		this.ddptColumnNameToColumnNumberMap = new TreeMap<String, Integer>();
		
		this.filterWithFilterList = new HashMap<GroupFilter, List<String>>();
		
		this.pointedDatasetPlainNamesSet = new HashSet<String>();
		this.pointedDatasetGenericNamesSet = new HashSet<String>();
		this.pointerElementList = new ArrayList<PointerElementInfo>();
		
	}

	public List<String> getNameConflictWarningList() {
		return nameConflictWarningList;
	}

	public List<String> getUnexistingPointedElementWarningList() {
		return unexistingPointedElementWarningList;
	}

	public PartitionTable getMainPartitionTable() {
		return mainPartitionTable;
	}

	public void setMainPartitionTable(PartitionTable mainPartitionTable) {
		this.mainPartitionTable = mainPartitionTable;
	}

	public CurrentPath getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(CurrentPath currentPath) {
		this.currentPath = currentPath;
	}

	@Override
	public String toString() {
		return
			"currentPath = " + currentPath;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		TransformationVariable transformationVariable=(TransformationVariable)object;
		return (
			(this.currentPath==transformationVariable.currentPath || (this.currentPath!=null && currentPath.equals(transformationVariable.currentPath)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==currentPath? 0 : currentPath.hashCode());
		return hash;
	}

	public PartitionTable getDdPT() {
		return ddPT;
	}

	public void setDdPT(PartitionTable ddPT) {
		this.ddPT = ddPT;
	}

	public PartitionTable getDefaultPT() {
		return defaultPT;
	}

	public void setDefaultPT(PartitionTable defaultPT) {
		this.defaultPT = defaultPT;
	}

	public Map<String, Integer> getDdptColumnNameToColumnNumberMap() {
		return ddptColumnNameToColumnNumberMap;
	}

	public Map<Integer, String> getDdptColumnNumberToColumnNameMap() {
		return ddptColumnNumberToColumnNameMap;
	}

	public List<Table> getTableList() {
		return tableList;
	}

	public void setTableList(List<Table> tableList) {
		this.tableList = tableList;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public List<PointerElementInfo> getPointerElementList() {
		return pointerElementList;
	}


	public Map<String, PartitionTable> getNameToPartitionTableMap() {
		return nameToPartitionTableMap;
	}

	public List<String> getPointingToGenomicSequenceWarningList() {
		return pointingToGenomicSequenceWarningList;
	}

	public List<String> getUnusedTablesInDataset() {
		return unusedTablesInDataset;
	}

	public List<String> getObsoleteElementList() {
		return obsoleteElementList;
	}

	public List<String> getPortableReferencesAnInvalidElementList() {
		return portableReferencesAnInvalidElementList;
	}

	public HashMap<String, Attribute> getAttributeMap() {
		return attributeMap;
	}

	public HashMap<String, Filter> getFilterMap() {
		return filterMap;
	}

	public Map<DimensionPartitionNameAndKeyAndValue, DimensionPartition> getDimensionPartitionsMap() {
		return dimensionPartitionsMap;
	}

	public void addKeyNameToMainTableShortNameMap(String keyName, String tableShortName) {
		MyUtils.checkStatusProgram(getMainTableShortNameMap(keyName)==null);
		keyNameToMainTableShortNameMap.put(keyName, tableShortName);
	}
	public String getMainTableShortNameMap(String keyName) {
		for (Iterator<String> it = keyNameToMainTableShortNameMap.keySet().iterator(); it.hasNext();) {
			String keyNameTmp = it.next();
			if (keyName.equalsIgnoreCase(keyNameTmp)) {
				return keyNameToMainTableShortNameMap.get(keyNameTmp);
			}
		}
		return null;
	}

	public void putNameAndKeyToTableMap(TableNameAndKeyName tableNameAndKeyName, Table table) {
		MyUtils.checkStatusProgram(getTableFromNameAndKey(tableNameAndKeyName)==null);
		nameAndKeyToTableMap.put(tableNameAndKeyName, table);
	}
	public Table getTableFromNameAndKey(TableNameAndKeyName tableNameAndKeyName) {
		for (Iterator<TableNameAndKeyName> it = nameAndKeyToTableMap.keySet().iterator(); it.hasNext();) {
			TableNameAndKeyName tableNameAndKeyNameTmp = it.next();
			if (tableNameAndKeyNameTmp.equalsIgnoreCase(tableNameAndKeyName)) {
				return nameAndKeyToTableMap.get(tableNameAndKeyNameTmp);
			}
		}
		return null;
	}

	public void updateNameAndKeyToTableMap(Table mainTable, String keyName) {
		for (Iterator<TableNameAndKeyName> it2 = nameAndKeyToTableMap.keySet().iterator(); it2.hasNext();) {
			TableNameAndKeyName tableNameAndKeyName = it2.next();
			Table tableTmp = getTableFromNameAndKey(tableNameAndKeyName);
			MyUtils.checkStatusProgram(tableTmp!=null, "tableNameAndKeyName = " + tableNameAndKeyName);
			if (tableTmp.equalsIgnoreCase(mainTable)) {
				tableNameAndKeyName.setKey(keyName);
				break;
			}
		}
	}
	
	public List<String> getIgnoredPushActions() {
		return ignoredPushActions;
	}

	public HashSet<String> getPointedDatasetGenericNamesSet() {
		return pointedDatasetGenericNamesSet;
	}

	public HashSet<String> getPointedDatasetPlainNamesSet() {
		return pointedDatasetPlainNamesSet;
	}

	public Boolean isTemplate() {
		return template;
	}

	public void setTemplate(Boolean template) {
		this.template = template;
	}

	public ArrayList<String> getUnexistingPointedDatasetWarningList() {
		return unexistingPointedDatasetWarningList;
	}

	public ArrayList<String> getMiscellaneousErrorList() {
		return miscellaneousErrorList;
	}

	public PartitionReference getDefaultPartitionReference() {
		return defaultPartitionReference;
	}

	public void setDefaultPartitionReference(
			PartitionReference defaultPartitionReference) {
		this.defaultPartitionReference = defaultPartitionReference;
	}

	public Map<GroupFilter, List<String>> getFilterWithFilterList() {
		return filterWithFilterList;
	}
}
