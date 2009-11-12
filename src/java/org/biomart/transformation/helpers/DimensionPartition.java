package org.biomart.transformation.helpers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.MartConfiguratorObject;
import org.biomart.objects.objects.PartitionTable;



public class DimensionPartition {

	public static void main(String[] args) {}

	private String tableName = null;
	private String keyName = null;
	
	private Boolean partition = null;
	
	private String tableShortNameFromTableName = null;
	private String datasetNameFromTableName = null;
	private List<DimensionPartitionNameAndKeyAndValue> dimensionPartitionInfoCandidateList = null;
	
	private DimensionPartitionNameAndKeyAndValue dimensionPartitionNameAndKeyAndValue = null;
	private PartitionTable dimensionPartitionTable = null;
	private Integer dimensionTableRowNumber = null;
	private String dimensionTableRowName = null;
	private Map<MartConfiguratorObject, Attribute> containeeToAttributesMap = null;
	private Map<MartConfiguratorObject, Filter> containeeToFiltersMap = null;

	public DimensionPartition(String tableName, String key) {
		super();
		this.tableName = tableName;
		this.keyName = key;
		
		this.partition = false;	// Until found different
		
		this.dimensionPartitionInfoCandidateList = new ArrayList<DimensionPartitionNameAndKeyAndValue>();
		this.containeeToAttributesMap = new HashMap<MartConfiguratorObject, Attribute>();
		this.containeeToFiltersMap = new HashMap<MartConfiguratorObject, Filter>();
	}
	
	public void addAttributeToContainee(MartConfiguratorObject containee, Attribute attribute) throws FunctionalException { 
		Attribute templateAttribute = this.containeeToAttributesMap.get(containee);
		if (null!=templateAttribute) {
			throw new FunctionalException("Containee already has a template attribute associated");
		}
		this.containeeToAttributesMap.put(containee, attribute);
	}
	public Attribute getAttributeForContainee(MartConfiguratorObject containee) {
		return this.containeeToAttributesMap.get(containee);
	}

	
	public void addFilterToContainee(MartConfiguratorObject containee, Filter filter) throws FunctionalException { 
		Filter templateFilter = this.containeeToFiltersMap.get(containee);
		if (null!=templateFilter) {
			throw new FunctionalException("Containee already has a template filter associated");
		}
		this.containeeToFiltersMap.put(containee, filter);
	}
	public Filter getFilterForContainee(MartConfiguratorObject containee) {
		return this.containeeToFiltersMap.get(containee);
	}
	
	/**
	 * 
	 * @param tableName	may be partial: ds__name__type or name__type or main
	 * @param keyName
	 * @return
	 */
	public void searchForPotentialDimensionTablePartition() {
		
		// N/A for main tables
		if (TransformationUtils.isMain(tableName)) {
			this.partition = false;
			return;
		}
		
		String[] tableConstraintSplit = tableName.split(TransformationConstants.NAMING_CONVENTION_ELEMENT_SEPARATOR);
		
		// tableConstraint is either in the form ds__identifier__dm or just identifier__dm, we want to isolate identifer
		MyUtils.checkStatusProgram(tableConstraintSplit.length==2 || tableConstraintSplit.length==3);
		this.datasetNameFromTableName = tableConstraintSplit.length==2 ? null : tableConstraintSplit[0];
		this.tableShortNameFromTableName = 
			tableConstraintSplit.length==2 ? tableConstraintSplit[0] : tableConstraintSplit[1];
						
		String[] partitionSplit = this.tableShortNameFromTableName.split(TransformationConstants.NAMING_CONVENTION_PARTITION_SEPARATOR);
		
		// Only if more than 1 piece, if 1 piece then unpartitioned
		if (partitionSplit.length>1) {
			this.partition = true;
			
			// Consider every combination of 2 non-empty parts: before and after i
			for (int i = 1; i < partitionSplit.length; i++) { 	// non-empty parts so start at 1
				
				// Rebuild part before i
				StringBuffer tableSb = new StringBuffer();
				for (int j = 0; j < i; j++) {
					tableSb.append((j==0 ? "" : TransformationConstants.NAMING_CONVENTION_PARTITION_SEPARATOR) + partitionSplit[j]);
				}
				
				// Rebuild part after i					
				StringBuffer valueSb = new StringBuffer();
				for (int j = i; j < partitionSplit.length; j++) {
					valueSb.append((j==i ? "" : TransformationConstants.NAMING_CONVENTION_PARTITION_SEPARATOR) + partitionSplit[j]);
				}
				dimensionPartitionInfoCandidateList.add(new DimensionPartitionNameAndKeyAndValue(tableSb.toString(), keyName, valueSb.toString()));
			}
		} else {
			this.partition = false;
		}
	}

	public void lookForPatternMatches() {
		
		// Take it backward		// TODO keep this until 100% sure about the new way
		/*public static final List<String> TEMPLATE_DIMENSION_TABLE_LIST = new ArrayList<String>(Arrays.asList(new String[] {
			"go", "ox", "exp", "protein_feature", "homolog", "paralog"
		}));*/
		/*for (int i = dimensionPartitionInfoCandidateList.size()-1; i>=0; i--) {
			DimensionPartitionNameAndKeyAndValue dimensionNameAndValueTmp = dimensionPartitionInfoCandidateList.get(i);
			if (Transformation.TEMPLATE_DIMENSION_TABLE_LIST.contains(dimensionNameAndValueTmp.getDimensionName())) {
				dimensionPartitionNameAndKeyAndValue = dimensionNameAndValueTmp;
				break;
			}
		}*/
		
		// New way of handling it: only the first part is considered to be the dimension partition
		if (dimensionPartitionInfoCandidateList.size()>0) {
			dimensionPartitionNameAndKeyAndValue = dimensionPartitionInfoCandidateList.get(0);
		}
		
		this.partition = dimensionPartitionNameAndKeyAndValue!=null;
	}
	
	public void setDimensionPartitionTable(PartitionTable dimensionPartitionTable) {
		this.dimensionPartitionTable = dimensionPartitionTable;
	}

	public void setDimensionTableRowNumber(Integer dimensionTableRowNumber) {
		this.dimensionTableRowNumber = dimensionTableRowNumber;
	}

	public String getDatasetNameFromTableName() {
		return datasetNameFromTableName;
	}

	public List<DimensionPartitionNameAndKeyAndValue> getDimensionPartitionInfoCandidateList() {
		return dimensionPartitionInfoCandidateList;
	}

	public DimensionPartitionNameAndKeyAndValue getDimensionPartitionNameAndKeyAndValue() {
		return dimensionPartitionNameAndKeyAndValue;
	}

	public PartitionTable getDimensionPartitionTable() {
		return dimensionPartitionTable;
	}

	public Integer getDimensionTableRowNumber() {
		return dimensionTableRowNumber;
	}

	public String getKeyName() {
		return keyName;
	}

	public Boolean getPartition() {
		return partition;
	}

	public String getTableName() {
		return tableName;
	}

	public String getTableShortNameFromTableName() {
		return tableShortNameFromTableName;
	}

	@Override
	public String toString() {
		StringBuffer sbAttribute = new StringBuffer();
		int i = 0;
		for (Iterator<MartConfiguratorObject> it = containeeToAttributesMap.keySet().iterator(); it.hasNext();) {
			MartConfiguratorObject containee = it.next();
			Attribute attribute = containeeToAttributesMap.get(containee);
			sbAttribute.append((i==0 ? "" : ", ") + attribute.getName());
			i++;
		}
		StringBuffer sbFilter = new StringBuffer();
		i = 0;
		for (Iterator<MartConfiguratorObject> it = containeeToFiltersMap.keySet().iterator(); it.hasNext();) {
			MartConfiguratorObject containee = it.next();
			Filter filter = containeeToFiltersMap.get(containee);
			sbFilter.append((i==0 ? "" : ", ") + filter.getName());
			i++;
		}
		return 
			/*super.toString() + ", " + */
			"tableName = " + tableName + ", " +
			"key = " + keyName + ", " +
			"partition = " + partition + ", " +
			
			"tableShortNameFromTableName = " + tableShortNameFromTableName + ", " +
			"datasetNameFromTableName = " + datasetNameFromTableName + ", " +
			"dimensionPartitionInfoCandidateList = " + dimensionPartitionInfoCandidateList + ", " +
			
			"dimensionPartitionNameAndKeyAndValue = " + dimensionPartitionNameAndKeyAndValue + ", " +
			"dimensionPartitionTable = " + (dimensionPartitionTable!=null ? dimensionPartitionTable.getName() : null) + ", " +
			"dimensionTableRowNumber = " + dimensionTableRowNumber + ", " +
			"dimensionTableRowName = " + dimensionTableRowName + ", " +
			"containeeToAttributesMap.size() = " + containeeToAttributesMap.size() + ", " +
			"{containeeToAttributesMap = " + sbAttribute.toString() + "}" + ", " +
			"containeeToFiltersMap.size() = " + containeeToFiltersMap.size() + ", " +
			"{containeeToFiltersMap = " + sbFilter.toString() + "}";
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		DimensionPartition dimensionPartition=(DimensionPartition)object;
		return (
			(this.tableName==dimensionPartition.tableName || (this.tableName!=null && tableName.equals(dimensionPartition.tableName))) &&
			(this.keyName==dimensionPartition.keyName || (this.keyName!=null && keyName.equals(dimensionPartition.keyName))) &&
			(this.partition==dimensionPartition.partition || (this.partition!=null && partition.equals(dimensionPartition.partition))) &&
			(this.tableShortNameFromTableName==dimensionPartition.tableShortNameFromTableName || (this.tableShortNameFromTableName!=null && tableShortNameFromTableName.equals(dimensionPartition.tableShortNameFromTableName))) &&
			(this.datasetNameFromTableName==dimensionPartition.datasetNameFromTableName || (this.datasetNameFromTableName!=null && datasetNameFromTableName.equals(dimensionPartition.datasetNameFromTableName))) &&
			(this.dimensionPartitionInfoCandidateList==dimensionPartition.dimensionPartitionInfoCandidateList || (this.dimensionPartitionInfoCandidateList!=null && dimensionPartitionInfoCandidateList.equals(dimensionPartition.dimensionPartitionInfoCandidateList)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		/*hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();*/
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==tableName? 0 : tableName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==keyName? 0 : keyName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==partition? 0 : partition.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==tableShortNameFromTableName? 0 : tableShortNameFromTableName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==datasetNameFromTableName? 0 : datasetNameFromTableName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==dimensionPartitionInfoCandidateList? 0 : dimensionPartitionInfoCandidateList.hashCode());
		return hash;
	}

	public String getDimensionTableRowName() {
		return dimensionTableRowName;
	}

	public void setDimensionTableRowName(String dimensionTableRowName) {
		this.dimensionTableRowName = dimensionTableRowName;
	}
}

