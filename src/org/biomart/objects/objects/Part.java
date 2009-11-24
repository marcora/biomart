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



public class Part implements Serializable {

	private static final long serialVersionUID = 2721818632834760378L;

	public static void main(String[] args) {}

	private Map<PartitionTable, Integer> map = null;
	private Boolean visible = null;
	private Element partSpecificElement = null;	//TODO use a smaller object (made of properties that are "bracketable")
	private Boolean target = null;
	
	// The main partition table has a predominant role, isolating it speeds up a lot (it is mandatory anyway)
	private PartitionTable mainPartitionTable = null;	// A reference to the main partition table to make things easier
	
	public Part(Boolean target, Boolean visible, Element partSpecificElement, PartitionTableAndRow... partitionTableAndRows) {
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
			"visible = " + visible + ", " +
			"mainPartitionTable = " + (mainPartitionTable!=null ? mainPartitionTable.name : null) + ", " +
			"map = " + map;
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
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==map? 0 : map.hashCode());
		return hash;
	}
	
	public boolean contains(PartitionTable partitionTable, int row) throws TechnicalException {
		Integer rowNumber = this.map.get(partitionTable);
		return rowNumber!=null && rowNumber.intValue()==row;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		Element partSpecificElement = this.partSpecificElement;//TODO actually clone the object!
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

	public void setSource() {
		this.target = false;
		this.visible = null;	// becomes irrelevant
		this.partSpecificElement = null;	// becomes irrelevant
	}
	
	public String getXmlValue() {
		return getXmlValue(false);
	}
	public String getXmlValue(boolean flatten) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(MartConfiguratorConstants.RANGE_RANGE_START);
		int partition = 0;
			
		// FIXME could be done in a more efficient manner?
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
}
