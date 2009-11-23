package org.biomart.builder.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.resources.Resources;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorConstants;
import org.jdom.Element;

public class PartitionTable {

	public final McNodeType MC_NODE_TYPE = McNodeType.PartitionTable;
	private DataSet dataSet;
	private Integer nameI;
	private Integer totalRows = null;
	private Integer totalColumns = null;
	private List<List<String>> table = null;
	private Boolean flatten = null;
	//may need a partition type
	private Boolean main = null;	// Whether this is the main partition table
	
	// Internal use
	private Map<String, Integer> rowNameToRowNumberMap = null;
	private Map<Integer, String> rowNumberToRowNameMap = null;

	public Map<String, Integer> getRowNameToRowNumberMap() {
		return rowNameToRowNumberMap;
	}

	public Map<Integer, String> getRowNumberToRowNameMap() {
		return rowNumberToRowNameMap;
	}

	
	public PartitionTable(DataSet ds) {
		this.dataSet = ds;
		this.table = new ArrayList<List<String>>();
		this.nameI = ds.getNextPartitionIntName();
	}
	
	public PartitionTable(Element ptElement) {
		
	}
	

	private static ArrayList<List<String>> createBasicPartitionTableTable(String firstValue) {
		ArrayList<List<String>> table = new ArrayList<List<String>>();
		ArrayList<String> list = new ArrayList<String>();
		list.add(firstValue);
		table.add(list);
		return table;
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

	public int getTotalRows() {
		return this.table.size();
	}

	public int getTotalColumns() {
		if(this.table.isEmpty())
			return 0;
		else
			return this.table.get(0).size();
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
	
	public int addRow(String value) {
		List<String> list = new ArrayList<String>();
		list.add(value);
		this.table.add(list);
		return this.table.size();
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
		if (row<this.getTotalRows() && col<this.getTotalColumns()) {
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
	
	public int getNameInt() {
		return this.nameI;
	}
	
	public String getNameString() {
		return "p"+this.nameI.toString();
	}
	
	public Element toXML() {
		Element ptElement = new Element(Resources.get("PARTITIONTABLE"));
		ptElement.setAttribute(Resources.get("NAME"),this.getNameString());
		ptElement.setAttribute("cols",""+this.getTotalColumns());
		ptElement.setAttribute("rows",""+this.getTotalRows());
		ptElement.setAttribute(Resources.get("FLATTEN"),"0");
		//generate all cells;
		if(this.getTotalRows()>0) {
			for(int i=0; i<this.getTotalRows(); i++) {
				for(int j=0; j<this.getTotalColumns(); j++) {
					Element cellElement = new Element("cell");
					cellElement.setAttribute("row",""+(i+1));
					cellElement.setAttribute("col",""+(j+1));
					cellElement.setAttribute("value",this.getValue(i, j));
					ptElement.addContent(cellElement);
				}
			}		
		}
		return ptElement;
	}
//	public String getReference() {
//		return getReference(MartConfiguratorConstants.MAIN_PARTITION_TABLE_MAIN_COLUMN);
//	}
//	public String getReference(int columnNumber) {
//		PartitionReference partitionReference = new PartitionReference(this, columnNumber);
//		return partitionReference.toXmlString();
//	}

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

	/*@Override
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
		
		//hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==totalRows? 0 : totalRows.hashCode());
		//hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==totalColumns? 0 : totalColumns.hashCode());
		//hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==table? 0 : table.hashCode());		//FIXME don't understand that makes it crash
		
		return hash;
	}*/

/*	public int compare(PartitionTable partitionTable1, PartitionTable partitionTable2) {
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
*/	
	/**
	 * Only for the node, children are treated separately
	 */
/*	public Element generateXml() throws FunctionalException {
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
	*/
}
