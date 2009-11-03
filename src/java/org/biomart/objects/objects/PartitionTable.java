package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.helpers.PartitionReference;
import org.jdom.Element;


public class PartitionTable extends MartConfiguratorObject implements Comparable<PartitionTable>, Comparator<PartitionTable>, Serializable {

	private static final long serialVersionUID = 2214465812364823821L;
	
	public static final String XML_ELEMENT_NAME = "partitionTable";
	public static final String CELL_XML_ELEMENT_NAME = "cell";
	
	public static void main(String[] args) {}

	private Integer totalRows = null;
	private Integer totalColumns = null;
	private List<List<String>> table = null;
	private Boolean flatten = null;
	
	private Boolean main = null;	// Whether this is the main partition table
	private Map<String, Integer> rowNameToRowNumberMap = null;
	private Map<Integer, String> rowNumberToRowNameMap = null;

	public Map<String, Integer> getRowNameToRowNumberMap() {
		return rowNameToRowNumberMap;
	}

	public Map<Integer, String> getRowNumberToRowNameMap() {
		return rowNumberToRowNameMap;
	}

	public PartitionTable(String name, String firstValue, boolean main) {
		this(name, 1, 1, createBasicPartitionTableTable(firstValue), main);		// because this(...) must be the first call...
	}

	private static ArrayList<List<String>> createBasicPartitionTableTable(String firstValue) {
		ArrayList<List<String>> table = new ArrayList<List<String>>();
		ArrayList<String> list = new ArrayList<String>();
		list.add(firstValue);
		table.add(list);
		return table;
	}
	
	public PartitionTable(String name, 
			Integer totalRows, Integer totalColumns, List<List<String>> table, Boolean main) {
		super(name, null, null, null, XML_ELEMENT_NAME);	// displayName, description & visible do not apply for that object
		this.totalRows = totalRows;
		this.totalColumns = totalColumns;
		this.flatten = false;
		this.table = table;
		this.rowNameToRowNumberMap = new HashMap<String, Integer>();
		this.rowNumberToRowNameMap = new HashMap<Integer, String>();
		if (this.table!=null) {
			MyUtils.checkStatusProgram(this.table.size()>=1);	//Must have at least 1 row
			for (int rowNumber = 0; rowNumber < this.table.size(); rowNumber++) {
				MyUtils.checkStatusProgram(this.table.get(rowNumber).size()>=1);	//Must have at least 1 column
				
				String rowName = this.table.get(rowNumber).get(0);
				MyUtils.checkStatusProgram(!this.rowNameToRowNumberMap.keySet().contains(rowName));	// can't have 2 rows with the same C0
				this.rowNameToRowNumberMap.put(rowName, rowNumber);
				MyUtils.checkStatusProgram(!this.rowNumberToRowNameMap.keySet().contains(rowNumber));	// can't have 2 rows with the same C0
				this.rowNumberToRowNameMap.put(rowNumber, rowName);
			}
		}
		this.main = main;
		
		MyUtils.checkStatusProgram(this.table!=null && this.table.size()>=1);	// == assert
		MyUtils.checkStatusProgram(totalRows.intValue()==table.size() && totalColumns.intValue()==table.get(0).size());	// at least 1 row for sure (see above)
	}
	
	public Boolean getFlatten() {
		return flatten;
	}

	public void setFlatten(Boolean flatten) {
		this.flatten = flatten;
	}

	public Integer getRowNumber(String rowName) {
		return this.rowNameToRowNumberMap.get(rowName);
	}
	
	public String getRowName(int rowNumber) {
		return this.rowNumberToRowNameMap.get(rowNumber);
	}

	public Boolean getMain() {
		return main;
	}

	public Integer getTotalRows() {
		return totalRows;
	}

	public Integer getTotalColumns() {
		return totalColumns;
	}

	public List<List<String>> getTable() {
		return table;
	}

	public void setTotalRows(Integer totalRows) {
		this.totalRows = totalRows;
	}

	public void setTotalColumns(Integer totalColumns) {
		this.totalColumns = totalColumns;
	}

	public void setTable(List<List<String>> table) {
		this.table = table;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"main = " + main + ", " +
			"totalRows = " + totalRows + ", " +
			"totalColumns = " + totalColumns + ", " +
			"flatten = " + flatten + ", " +
			"table.length = " + table.size()
			+ ", " + this.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		PartitionTable partitionTable=(PartitionTable)object;
		return (
			super.equals(partitionTable) &&
			
			(this.main==partitionTable.main || (this.main!=null && main.equals(partitionTable.main))) &&
			(this.flatten==partitionTable.flatten || (this.flatten!=null && flatten.equals(partitionTable.flatten))) &&
			
			(this.totalRows==partitionTable.totalRows || (this.totalRows!=null && totalRows.equals(partitionTable.totalRows))) &&
			(this.totalColumns==partitionTable.totalColumns || (this.totalColumns!=null && totalColumns.equals(partitionTable.totalColumns))) &&
			
			(this.table==partitionTable.table || (this.table!=null && table.equals(partitionTable.table)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==main? 0 : main.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==flatten? 0 : flatten.hashCode());
		
		/*hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==totalRows? 0 : totalRows.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==totalColumns? 0 : totalColumns.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==table? 0 : table.hashCode());*/		//FIXME don't understand that makes it crash
		
		return hash;
	}

	public int compare(PartitionTable partitionTable1, PartitionTable partitionTable2) {
		if (partitionTable1==null && partitionTable2!=null) {
			return -1;
		} else if (partitionTable1!=null && partitionTable2==null) {
			return 1;
		}
		return CompareUtils.compareString(partitionTable1.name, partitionTable2.name);
	}

	public int compareTo(PartitionTable partitionTable) {
		return compare(this, partitionTable);
	}
	
	/**
	 * Only for the node, children are treated separately
	 */
	public Element generateXml() {
		Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "rows", this.totalRows);
		MartConfiguratorUtils.addAttribute(element, "cols", this.totalColumns);
		MartConfiguratorUtils.addAttribute(element, "flatten", this.flatten);
		
		for (int i = 0; i < this.totalRows; i++) {
			for (int j = 0; j < this.totalColumns; j++) {
				Element cell = new Element(CELL_XML_ELEMENT_NAME);
				MartConfiguratorUtils.addAttribute(cell, "row", i);
				MartConfiguratorUtils.addAttribute(cell, "col", j);
				MartConfiguratorUtils.addText(cell, this.table.get(i).get(j));				
				element.addContent(cell);
			}
		}
		
		return element;
	}
	public int addRow(String value) {
		int newRowNumber = this.totalRows;
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < this.totalColumns; i++) {
			list.add(MartConfiguratorConstants.PARTITION_TABLE_EMPTY_VALUE);
		}
		this.table.add(list);
		this.updateValue(newRowNumber, MartConfiguratorConstants.DIMENSION_TABLE_PARTITION_TABLE_MAIN_COLUMN, value);
		this.totalRows++;
		return newRowNumber;
	}
	
	public int addColumn () {
		int newColumnNumber = this.totalColumns;
		for (List<String> list : this.table) {
			list.add(MartConfiguratorConstants.PARTITION_TABLE_EMPTY_VALUE);
		}
		this.totalColumns++;
		return newColumnNumber;
	}
	
	public void updateValue(int row, int col, String value) {
		this.table.get(row).set(col, value);
		if (col==0) {
			MyUtils.checkStatusProgram(!this.rowNameToRowNumberMap.keySet().contains(value));	// can't have 2 rows with the same C0
			this.rowNameToRowNumberMap.put(value, row);
			MyUtils.checkStatusProgram(!this.rowNumberToRowNameMap.keySet().contains(row));	// can't have 2 rows with the same C0
			this.rowNumberToRowNameMap.put(row, value);
		}
	}
	
	public String getValue(int row, int col) {
		String value = null;
		if (row<this.totalRows && col<this.totalColumns) {
			List<String> list = this.table.get(row);
			if (null!=list) {
				value = list.get(col);
			}
		}
		return value;
	}
	
	public List<String> getRowNamesList() {
		List<String> rowNamesList = new ArrayList<String>();
		for (List<String> list : this.table) {
			rowNamesList.add(list.get(MartConfiguratorConstants.PARTITION_TABLE_MAIN_COLUMN));	// There is always at least one column
		}
		return rowNamesList;
	}
	
	public String getReference() {
		return getReference(MartConfiguratorConstants.MAIN_PARTITION_TABLE_MAIN_COLUMN);
	}
	public String getReference(int columnNumber) {
		PartitionReference partitionReference = new PartitionReference(this, columnNumber);
		return partitionReference.toXmlString();
	}
}
