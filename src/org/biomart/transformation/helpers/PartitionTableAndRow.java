package org.biomart.transformation.helpers;


import java.util.Comparator;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.objects.PartitionTable;



public class PartitionTableAndRow extends Object implements Comparable<PartitionTableAndRow>, Comparator<PartitionTableAndRow> {

	public static void main(String[] args) {}

	private PartitionTable partitionTable = null;
	private Integer row = null;

	public PartitionTableAndRow(PartitionTable partitionTable, Integer row) {
		super();
		this.partitionTable = partitionTable;
		this.row = row;
	}

	public PartitionTable getPartitionTable() {
		return partitionTable;
	}

	public Integer getRow() {
		return row;
	}

	public void setPartitionTable(PartitionTable partitionTable) {
		this.partitionTable = partitionTable;
	}

	public void setRow(Integer row) {
		this.row = row;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"partitionTable = " + partitionTable + ", " +
			"row = " + row;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		PartitionTableAndRow partitionTableAndRow=(PartitionTableAndRow)object;
		return (
			(this.partitionTable==partitionTableAndRow.partitionTable || (this.partitionTable!=null && partitionTable.equals(partitionTableAndRow.partitionTable))) &&
			(this.row==partitionTableAndRow.row || (this.row!=null && row.equals(partitionTableAndRow.row)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==partitionTable? 0 : partitionTable.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==row? 0 : row.hashCode());
		return hash;
	}

	public int compare(PartitionTableAndRow partitionTableAndRow1, PartitionTableAndRow partitionTableAndRow2) {
		if (partitionTableAndRow1==null && partitionTableAndRow2!=null) {
			return -1;
		} else if (partitionTableAndRow1!=null && partitionTableAndRow2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(partitionTableAndRow1.partitionTable, partitionTableAndRow2.partitionTable);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(partitionTableAndRow1.row, partitionTableAndRow2.row);
	}

	public int compareTo(PartitionTableAndRow partitionTableAndRow) {
		return compare(this, partitionTableAndRow);
	}

}
