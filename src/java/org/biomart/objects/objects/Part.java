package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.transformation.helpers.PartitionTableAndRow;



public class Part implements /*Comparable<Part>, Comparator<Part>, */Serializable {

	private static final long serialVersionUID = 2721818632834760378L;

	public static void main(String[] args) {}

	private Map<PartitionTable, Integer> map = null;
	private Boolean visible = null;
	private Element partSpecificElement = null;
	private Boolean target = null;
	
	private PartitionTable mainPartitionTable = null;	// A reference to the main partition table to make things easier
	
	public Part(Boolean target, Boolean visible, Element partSpecificElement, PartitionTableAndRow... partitionTableAndRows) {
		super();
		this.target = target;
		this.map = new LinkedHashMap<PartitionTable, Integer>();
		if (null!=partitionTableAndRows) {
			for (int i = 0; i < partitionTableAndRows.length; i++) {
				addPartitionRow(partitionTableAndRows[i].getPartitionTable(), partitionTableAndRows[i].getRow());
			}
		}
		this.visible = visible;
		this.partSpecificElement = partSpecificElement;
	}
	public Part(Part part) {
		this(part.mainPartitionTable, part.getMainRowNumber());
	}
	public Part(PartitionTable mainPartitionTable, Integer mainRow) {
		this(false, null, null, new PartitionTableAndRow(mainPartitionTable, mainRow));
	}
	
	public boolean contains(PartitionTable partitionTable, int row) throws TechnicalException {
		Integer rowNumber = this.map.get(partitionTable);
		return rowNumber!=null && rowNumber.intValue()==row;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		Element partSpecificElement = this.partSpecificElement;//TODO clone it!
		Part part = new Part(this.target, this.visible, partSpecificElement);
		for (Iterator<PartitionTable> it = map.keySet().iterator(); it.hasNext();) {
			PartitionTable partitionTable = it.next();
			Integer row = map.get(partitionTable);
			part.addPartitionRow(partitionTable, row);
		}
		return (Object)part;
	}

	public void addPartitionRow(PartitionTable partitionTable, int row) {
		MyUtils.checkStatusProgram(this.map.get(partitionTable)==null, partitionTable.getName() + ", " + row + ", " + this.getXmlValue());
		if (partitionTable.getMain()) {
			this.mainPartitionTable = partitionTable;
		}
		this.map.put(partitionTable, row);
	}

	public Integer removePartition(PartitionTable partitionTable) {
		MyUtils.checkStatusProgram(this.map.get(partitionTable)!=null);
		return map.remove(partitionTable);
	}
	
	public void modifyPartitionRow(PartitionTable partitionTable, int row) {
		Integer originalRow = this.map.get(partitionTable);
		MyUtils.checkStatusProgram(originalRow!=null && originalRow!=row, "this = " + getXmlValue() + 
				", partitionTableName = " + partitionTable.getName() + ", originalRow = " + originalRow + ", row = " + row);
		MyUtils.checkStatusProgram(this.map.keySet().contains(partitionTable));
		this.map.put(partitionTable, row);	// do not use addPartitionRow
	}
	
	public boolean containsPartition(PartitionTable partitionTable) {
		return this.map.keySet().contains(partitionTable);
	}

	public int getMainRowNumber() {
		MyUtils.checkStatusProgram(this.mainPartitionTable!=null);
		return getRowNumber(this.mainPartitionTable);
	}

	public Set<PartitionTable> getPartitionTables() {
		return this.map.keySet();
	}
	public PartitionTable getPartitionTableByName(String partitionTableName) {
		for (PartitionTable partitionTableTmp : this.map.keySet()) {
			if (partitionTableTmp.getName().equals(partitionTableName)) {
				return partitionTableTmp;
			}
		}
		return null;
	}
	
	public int getRowNumber(PartitionTable partitionTable) {
		MyUtils.checkStatusProgram(this.map.keySet().contains(partitionTable));
		return map.get(partitionTable);
	}

	public Boolean getVisible() {
		return visible;
	}

	public Element getPartSpecificElement() {
		return partSpecificElement;
	}
	

	public Map<PartitionTable, Integer> getMap() {
		return map;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public void setPartSpecificElement(Element partSpecificElement) {
		this.partSpecificElement = partSpecificElement;
	}

	@Override
	public String toString() {
		return
			"target = " + target + ", " +
			"map = " + map + ", " +
			"visible = " + visible/* + ", " +
			"partSpecificElement = " + partSpecificElement*/;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Part part=(Part)object;
		return (
			(this.target==part.target || (this.target!=null && target.equals(part.target))) &&
			(this.map==part.map || (this.map!=null && map.equals(part.map))) &&
			(this.visible==part.visible || (this.visible!=null && visible.equals(part.visible)))
			//(this.partSpecificElement==part.partSpecificElement || (this.partSpecificElement!=null && partSpecificElement.equals(part.partSpecificElement)))
			//TODO partSpecificElement
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==target? 0 : target.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==map? 0 : map.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==visible? 0 : visible.hashCode());
		//hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==partSpecificElement? 0 : partSpecificElement.hashCode());
		//TODO partSpecificElement
		return hash;
	}

	/*public int compare(Part part1, Part part2) {
		if (part1==null && part2!=null) {
			return -1;
		} else if (part1!=null && part2==null) {
			return 1;
		}
		return comparePartMap(part1.map, part2.map);
	}

	private int comparePartMap(Map<PartitionTable, Integer> map1, Map<PartitionTable, Integer> map2) {
																		// Compare Map<PartitionTable, Integer>
		if (CompareUtils.bothNull(map1, map2)) {
			return 0;
		}
		int compare = CompareUtils.compareNull(map1, map2);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareMapSize(map1, map2);
		if (compare!=0) {
			return compare;
		}
		Iterator<PartitionTable> it = map1.keySet().iterator();
		for (Iterator<PartitionTable> it2 = map2.keySet().iterator(); it2.hasNext();) {
													// They are not null & the same sizes by now
			PartitionTable partitionTable1 = it.next();
			PartitionTable partitionTable2 = it2.next();
			
			compare = partitionTable1.compareTo(partitionTable2);
			if (compare!=0) {
				return compare;
			}
			
			Integer row1 = map1.get(partitionTable1);
			Integer row2 = map2.get(partitionTable2);
			
			compare = CompareUtils.compareInteger(row1, row2);
			if (compare!=0) {
				return compare;
			}
		}
		return 0;
	}

	public int compareTo(Part part) {
		return compare(this, part);
	}*/
	
	public String getXmlValue() {
		return getXmlValue(false);
	}
	public String getXmlValue(boolean flatten) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(MartConfiguratorConstants.RANGE_RANGE_START);
		int partition = 0;
			
		// FIXME to be done in a more efficient manner
		// Display Main first
		for (Iterator<PartitionTable> it = map.keySet().iterator(); it.hasNext();) {
			PartitionTable partitionTable = it.next();
			if (partitionTable.getMain()) {
				Integer row = map.get(partitionTable);
				stringBuffer.append(getPartName(partitionTable, row, flatten));
			}
		}
		
		// Display others
		for (Iterator<PartitionTable> it = map.keySet().iterator(); it.hasNext();) {
			PartitionTable partitionTable = it.next();
			if (!partitionTable.getMain()) {
				Integer row = map.get(partitionTable);
				MyUtils.checkStatusProgram(null!=row, 
						"map = " + map + ", partitionTable = " + partitionTable +  " --- " + displayMapHashCodes(partitionTable));
				stringBuffer.append(MartConfiguratorConstants.RANGE_INTRA_SEPARATOR +
						getPartName(partitionTable, row, flatten));
			}
			partition++;
		}
		
		if (this.target) {
			stringBuffer.append(MartConfiguratorConstants.RANGE_INTRA_SEPARATOR + 
					MartConfiguratorUtils.booleanToBinaryDigit(this.visible));
			
			if (this.partSpecificElement!=null) {
				if (this.partSpecificElement.getDisplayName()!=null) {
					stringBuffer.append(getSuperPropertyString(MartConfiguratorConstants.PART_SUPER_SPECIFIC_DISPLAY_NAME_PARAMETER_NAME));
				}
			}
		}
		stringBuffer.append(MartConfiguratorConstants.RANGE_RANGE_END);
		return stringBuffer.toString();
	}

	private String getPartName(PartitionTable partitionTable, Integer row, boolean flatten) {
		if (flatten) {
			return row==MartConfiguratorConstants.PARTITION_TABLE_ROW_WILDCARD_NUMBER ? 
					MartConfiguratorConstants.PARTITION_TABLE_ROW_WILDCARD : partitionTable.getRowName(row);
		} else {			
			return MartConfiguratorConstants.RANGE_PARTITION_TABLE_PREFIX + partitionTable.getName() + 
				MartConfiguratorConstants.RANGE_ROW_PREFIX + displayRow(row);
		}
	}
	
	private String displayMapHashCodes(PartitionTable partitionTable) {
		String s = "";
		for (Iterator<PartitionTable> it = map.keySet().iterator(); it.hasNext();) {
			PartitionTable partitionTableTmp = it.next();
			s +=partitionTableTmp.getName() + ": " + partitionTableTmp.hashCode() + " (" + partitionTableTmp.equals(partitionTable) + ")" + ", ";
		}
		s += " / " + partitionTable.hashCode() + "." ;
		for (Iterator<Integer> it2 = map.values().iterator(); it2.hasNext();) {
			Integer value = it2.next();
			s += value + ", ";
		}
		return s;
	}

	private String displayRow(int row) {
		return row==MartConfiguratorConstants.PARTITION_TABLE_ROW_WILDCARD_NUMBER ? 
				MartConfiguratorConstants.PARTITION_TABLE_ROW_WILDCARD : String.valueOf(row);
	}

	private String getSuperPropertyString(String parameterName) {
		return MartConfiguratorConstants.RANGE_INTRA_SEPARATOR + parameterName + 
				MartConfiguratorConstants.PART_SUPER_SPECIFIC_PARAMETER_VALUE_ASSIGNOR +
				MartConfiguratorConstants.PART_SUPER_SPECIFIC_PARAMETER_VALUE_DELIMITER +
				this.partSpecificElement.getDisplayName() +
				MartConfiguratorConstants.PART_SUPER_SPECIFIC_PARAMETER_VALUE_DELIMITER;
	}

	public void setTarget(Boolean target) {
		this.target = target;
		this.visible = null;	// becomes irrelevant
		this.partSpecificElement = null;	// becomes irrelevant
	}
}
