package org.biomart.objects.helpers;


import java.util.Map;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.objects.PartitionTable;



public class PartitionReference /*implements Comparable<PartitionReference>, Comparator<PartitionReference> */{

	public static void main(String[] args) {}

	private PartitionTable partitionTable = null;
	private String partitionTableName = null;
	private Integer column = null;

	public PartitionReference(String partitionTableName) {
		this(null, partitionTableName, MartConfiguratorConstants.PARTITION_TABLE_MAIN_COLUMN);
	}
	public PartitionReference(String partitionTableName, Integer column) {
		this(null, partitionTableName, column);
	}
	public PartitionReference(PartitionTable partitionTable) {
		this(partitionTable, partitionTable.getName(), MartConfiguratorConstants.PARTITION_TABLE_MAIN_COLUMN);
	}
	public PartitionReference(PartitionTable partitionTable, Integer column) {
		this(partitionTable, partitionTable.getName(), column);
	}
	private PartitionReference(PartitionTable partitionTable, String partitionTableName, Integer column) {
		super();
		this.partitionTable = partitionTable;
		this.partitionTableName = partitionTableName;
		this.column = column;
	}

	public PartitionTable getPartitionTable() {
		return partitionTable;
	}

	public String getPartitionTableName() {
		return partitionTableName;
	}

	public Integer getColumn() {
		return column;
	}

	public void setPartitionTable(PartitionTable partitionTable) {
		this.partitionTable = partitionTable;
	}

	public void setPartitionTableName(String partitionTableName) {
		this.partitionTableName = partitionTableName;
	}

	public void setColumn(Integer column) {
		this.column = column;
	}

	@Override
	public String toString() {
		return
			"partitionTable = " + (null!=partitionTable ? partitionTable.getName() : null) + ", " +
			"partitionTableName = " + partitionTableName + ", " +
			"column = " + column;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		PartitionReference partitionReference=(PartitionReference)object;
		return (
			/*(this.partitionTable==partitionReference.partitionTable || (this.partitionTable!=null && partitionTable.equals(partitionReference.partitionTable))) &&*/
			(this.partitionTableName==partitionReference.partitionTableName || (this.partitionTableName!=null && partitionTableName.equals(partitionReference.partitionTableName))) &&
			(this.column==partitionReference.column || (this.column!=null && column.equals(partitionReference.column)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		/*hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==partitionTable? 0 : partitionTable.hashCode());*/
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==partitionTableName? 0 : partitionTableName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==column? 0 : column.hashCode());
		return hash;
	}

	/*@Override
	public int compare(PartitionReference partitionReference1, PartitionReference partitionReference2) {
		if (partitionReference1==null && partitionReference2!=null) {
			return -1;
		} else if (partitionReference1!=null && partitionReference2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(partitionReference1.partitionTable, partitionReference2.partitionTable);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(partitionReference1.partitionTableName, partitionReference2.partitionTableName);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(partitionReference1.column, partitionReference2.column);
	}

	@Override
	public int compareTo(PartitionReference partitionReference) {
		return compare(this, partitionReference);
	}*/
	
	public static PartitionReference fromString(String partitionReferenceString) {
		PartitionReference partitionReference = null;
		String partitionTableName = MartConfiguratorUtils.extractPartitionTableNameFromPartitionReferenceString(partitionReferenceString);
		Integer columnNumber = MartConfiguratorUtils.extractColumnNumberFromPartitionReferenceString(partitionReferenceString);
		if (null!=partitionTableName && null!=columnNumber) {
			partitionReference = new PartitionReference(partitionTableName, columnNumber);
		}
		return partitionReference;
	}
	
	public String getValue(Map<String, PartitionTable> nameToPartitionTableMap, int row) throws FunctionalException {
		PartitionTable partitionTable = nameToPartitionTableMap.get(partitionTableName);
		if (this.partitionTable!=null) { 
			if (!this.partitionTable.getName().equals(partitionTable.getName())) {
				throw new FunctionalException("A different partition table is already set");
			}
		} else {
			this.partitionTable = partitionTable;
		}
		return getValue(row);
	}
	
	public String getValue(int row) throws FunctionalException {
		if (this.partitionTable==null) {
			throw new FunctionalException("Partition table is unknown");
		}
		return this.partitionTable.getValue(row, this.column);
	}

	public String toXmlString() {
		return MartConfiguratorConstants.RANGE_PARTITION_TABLE_REFERENCE_START + 
		MartConfiguratorConstants.RANGE_PARTITION_TABLE_PREFIX + this.partitionTableName + 
		MartConfiguratorConstants.RANGE_COLUMN_PREFIX + this.column + MartConfiguratorConstants.RANGE_PARTITION_TABLE_REFERENCE_END;
	}
}
