package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.transformation.helpers.TransformationConstants;
import org.jdom.Attribute;
import org.jdom.Element;


public class OldDynamicDataset extends OldNode /*implements Comparable<OldDynamicDataset>, Comparator<OldDynamicDataset>*/ {

	public static void main(String[] args) {}

	private String internalName = null;
	private Map<String, String> aliases = null;
	
	// Only for templates
	private String datasetNameVariablePart = null;

	private List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"internalName", "aliases"
	}));
	
	public OldDynamicDataset(Element jdomDatasetConfig) throws FunctionalException {
		this(jdomDatasetConfig,
				jdomDatasetConfig.getAttributeValue("internalName"),
				jdomDatasetConfig.getAttributeValue("aliases")
		);
		
		@SuppressWarnings("unchecked")
		List<Attribute> attributeList = jdomDatasetConfig.getAttributes();
		for (Attribute attribute : attributeList) {
			String propertyName = attribute.getName();
			if (!propertyList.contains(propertyName)) {
				throw new FunctionalException("Unknown property: " + propertyName + ", in: " + jdomDatasetConfig);
			}
		}
	}
	
	public OldDynamicDataset(Element jdomDatasetConfig, String internalName, String aliases) throws FunctionalException {
		super(jdomDatasetConfig);
		this.internalName = internalName;
		this.aliases = new LinkedHashMap<String, String>();
		if (aliases!=null && !MyUtils.isEmpty(aliases)) {
			String[] split = aliases.split(MartServiceConstants.ELEMENT_SEPARATOR);
			for (String alias : split) {
				String[] aliasSplit = alias.split("=");
				String aliasKey = aliasSplit[0];
				String aliasValue = aliasSplit.length>1 ? aliasSplit[1] : "";	// no null values
				this.aliases.put(aliasKey, aliasValue);
			}
		}
	}

	public String getInternalName() {
		return internalName;
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " +
			"internalName = " + internalName + ", " +
			"aliases = " + aliases;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		OldDynamicDataset oldDynamicDataset=(OldDynamicDataset)object;
		return (
			(this.internalName==oldDynamicDataset.internalName || (this.internalName!=null && internalName.equals(oldDynamicDataset.internalName))) &&
			(this.aliases==oldDynamicDataset.aliases || (this.aliases!=null && aliases.equals(oldDynamicDataset.aliases)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==internalName? 0 : internalName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==aliases? 0 : aliases.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldDynamicDataset oldDynamicDataset1, OldDynamicDataset oldDynamicDataset2) {
		if (oldDynamicDataset1==null && oldDynamicDataset2!=null) {
			return -1;
		} else if (oldDynamicDataset1!=null && oldDynamicDataset2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(oldDynamicDataset1.internalName, oldDynamicDataset2.internalName);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(oldDynamicDataset1.aliases, oldDynamicDataset2.aliases);
	}

	@Override
	public int compareTo(OldDynamicDataset oldDynamicDataset) {
		return compare(this, oldDynamicDataset);
	}*/
	
	public static final String VARIABLE_PART_COLUMN_IDENTIFIER = "variablePart";
	public static final String DATASET_PLAIN_NAME_COLUMN_IDENTIFIER = "internalName";
	
	public static String extractFixedPart(List<OldDynamicDataset> oldDynamicDatasetList) {
		
		// Split the names and populate a list with it, reverse the list to ease common last part recognition
		MyUtils.checkStatusProgram(oldDynamicDatasetList.size()>=1);	// Check at least 1 element
		Integer max = null;
		List<List<String>> superList = new ArrayList<List<String>>();
		for (OldDynamicDataset oldDynamicDataset : oldDynamicDatasetList) {
			String datasetName = oldDynamicDataset.internalName;
			List<String> list = new ArrayList<String>(Arrays.asList(datasetName.split(
					TransformationConstants.DYNAMIC_DATASET_INTERNAL_NAME_ELEMENT_SEPARATOR)));
			Collections.reverse(list);
			if (max==null || list.size()>max) {
				max = list.size();
			}
			superList.add(list);
		}
		
		String fixedPart = null;
		if (max>1) {
			
			// Infer common part
			List<String> commonParts = new ArrayList<String>();
			boolean stop = false;
			for (int i = 0; i < max-1 && !stop; i++) {	// -1 because ...
				List<String> firstRow = superList.get(0);
				String firstRowValue = firstRow.get(i);
				for (int j = 1; j < superList.size(); j++) {
					List<String> list = superList.get(j);
					if (!firstRowValue.equals(list.get(i))) {
						stop = true;
						break;
					}
				}
				if (!stop) {
					commonParts.add(firstRowValue);
				}
			}
			Collections.reverse(commonParts);
			
			// Rebuild common part as a String
			StringBuffer stringBuffer = new StringBuffer();
			for (int i = 0; i < commonParts.size(); i++) {
				String part = commonParts.get(i);
				stringBuffer.append(TransformationConstants.DYNAMIC_DATASET_INTERNAL_NAME_ELEMENT_SEPARATOR + part);		
			}
			fixedPart = stringBuffer.toString();
			MyUtils.checkStatusProgram(fixedPart!=null && !MyUtils.isEmpty(fixedPart), fixedPart);	// assumption: they must have at least 1 part in common
		} else {
			// No common parts, fixedPart is empty 
			fixedPart = "";
		}
		System.out.println("fixedPart = " + fixedPart);
		return fixedPart;
	}
	
	public static PartitionTable transformToPartitionTable(String datasetNameFixedPart, List<OldDynamicDataset> oldDynamicDatasetList, 
			Map<Integer, String> dynamicDatasetPartitionTableColumnMapping, Map<String, Integer> dynamicDatasetPartitionTableColumnMapping2)
	throws FunctionalException {
		
		// Extract variable part of each dynamic dataset names
		extractVariableParts(oldDynamicDatasetList, datasetNameFixedPart);
		/*for (OldDynamicDataset oldDynamicDataset : oldDynamicDatasetList) {
			String datasetName = oldDynamicDataset.internalName;
			if (!datasetName.endsWith(templateName)) {
				throw new FunctionalException("Can't guess dataset name properly: " + datasetName + ", " + templateName);
			}
			oldDynamicDataset.datasetNameVariablePart = datasetName.substring(0, datasetName.length()-templateName.length());
		}*/
		
		// Create a set of keys that we will later map to a column number and vice versa
		// Also populate a map representing the partitionTable
		Set<String> partitionTableColumnSet = new LinkedHashSet<String>();
		Map<String, LinkedHashMap<String,String>> dynamicDatasetPartitionTableMap = new LinkedHashMap<String, LinkedHashMap<String,String>>();
		int row1 = 0;
		for (OldDynamicDataset oldDynamicDataset : oldDynamicDatasetList) {
			LinkedHashMap<String, String> aliasMap = new LinkedHashMap<String, String>();
			
			// Add internalName as if it was an alias (it will be the 1st column necessarily
			partitionTableColumnSet.add(DATASET_PLAIN_NAME_COLUMN_IDENTIFIER);
			aliasMap.put(DATASET_PLAIN_NAME_COLUMN_IDENTIFIER, oldDynamicDataset.internalName);
		
			// Add a column for the variable part of the dataset name
			partitionTableColumnSet.add(VARIABLE_PART_COLUMN_IDENTIFIER);
			aliasMap.put(VARIABLE_PART_COLUMN_IDENTIFIER, oldDynamicDataset.datasetNameVariablePart);
								// c.f TransformationConstants.DYNAMIC_DATASET_PARTITION_TABLE_DATASET_NAME_VARIABLE_PART_COLUMN_NUMBER
			
			// Add each column for the current row
			for (Iterator<String> it = oldDynamicDataset.aliases.keySet().iterator(); it.hasNext();) {
				String aliasKey = it.next();
				String aliasValue = oldDynamicDataset.aliases.get(aliasKey);
				MyUtils.checkStatusProgram(aliasValue!=null);
				partitionTableColumnSet.add(aliasKey);
				aliasMap.put(aliasKey, aliasValue);
			}
			dynamicDatasetPartitionTableMap.put(oldDynamicDataset.internalName, aliasMap);
			
			row1++;
		}
		
		// Create maps linking aliases to partitionTable (to ease transformation)
		int columnIndex = 0;
		for (String key : partitionTableColumnSet) {
			dynamicDatasetPartitionTableColumnMapping.put(columnIndex, key);
			dynamicDatasetPartitionTableColumnMapping2.put(key, columnIndex);
			columnIndex++;
		}
		
		// Prepare partitionTable properties
		int totalRows = dynamicDatasetPartitionTableMap.size();
		int totalColumns = partitionTableColumnSet.size();
		
		// Declare cells for the partitionTable and initialize them to empty values
		List<List<String>> cells = new ArrayList<List<String>>();
		for (int i = 0; i < totalRows; i++) {
			List<String> list = new ArrayList<String>();
			for (int j = 0; j < totalColumns; j++) {
				list.add(MartConfiguratorConstants.PARTITION_TABLE_EMPTY_VALUE);
			}
			cells.add(list);
		}
		
		// Populate the cells
		int row2 = 0;
		for (Iterator<LinkedHashMap<String, String>> it = dynamicDatasetPartitionTableMap.values().iterator(); it.hasNext();) {
			LinkedHashMap<String, String> map = it.next();
			MyUtils.checkStatusProgram(map!=null);
			for (Iterator<String> it2 = map.keySet().iterator(); it2.hasNext();) {
				String key = it2.next();
				String value = map.get(key);	
				MyUtils.checkStatusProgram(value!=null);
				int column = dynamicDatasetPartitionTableColumnMapping2.get(key);
				cells.get(row2).set(column, value);
			}
			row2++;
		}

		// Create and return PartitionTable object
		PartitionTable mainPartitionTable = new PartitionTable(TransformationConstants.DYNAMIC_DATASET_PARTITION_TABLE_NAME, totalRows, totalColumns, cells, true);
		
		// Additional check: 0th column = 1st column + dataset name fixed part
		for (int i = 0; i < mainPartitionTable.getTotalRows(); i++) {
			String plainDatasetName = mainPartitionTable.getValue(i, 0);
			String variablePart = mainPartitionTable.getValue(i, 1);
			MyUtils.checkStatusProgram(plainDatasetName.equals(variablePart + datasetNameFixedPart));
		}
		
		return mainPartitionTable;
	}

	private static void extractVariableParts(List<OldDynamicDataset> oldDynamicDatasetList, String fixedPart) {
		for (OldDynamicDataset oldDynamicDataset : oldDynamicDatasetList) {
			String datasetName = oldDynamicDataset.internalName;
			String variablePart = datasetName.substring(0, datasetName.length()-fixedPart.length());
			oldDynamicDataset.datasetNameVariablePart = variablePart;
			System.out.println("\tvariablePart = " + variablePart);
		}
	}
}