package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.objects.types.TableType;
import org.jdom.Element;


public class Table extends MartConfiguratorObject implements Comparable<Table>, Comparator<Table>, Serializable {

	private static final long serialVersionUID = 994381514343812305L;
	
	public static final String XML_ELEMENT_NAME = "table";
	public static final McNodeType MC_NODE_TYPE = null;
	
	public static void main(String[] args) {}

	private Range range = null;
	private HashSet<Column> columns = null;
	private Column key = null;
	private Boolean main = null;	// Whether this is a main table
	private TableType type = null;
	
	public Table(String name, PartitionTable mainPartitionTable, boolean main, TableType type,
			String keyName, 
			HashSet<String> fieldNames) {	// key amongst fieldNames will be removed since added separately
		super(name, null, null, null, XML_ELEMENT_NAME);	// displayName, description & visible do not apply for that object
		this.columns = new HashSet<Column>();
		this.type = type;
		
		if (null!=keyName) {
			//setKey(keyName);
			this.key = new Column(keyName, true);
			addColumn(this.key);
			MyUtils.checkStatusProgram(fieldNames.contains(keyName), keyName + ", " + fieldNames);
			fieldNames.remove(keyName);	// do not add it if  in setKey
		}
		
		addColumns(fieldNames);
		
		this.range = new Range(mainPartitionTable, false);
		
		this.main = main;
	}
	
	public void setKey(String keyName) {	// column should already be among columns
		Column key = this.getColumn(keyName);
		key.setKey(true);
		this.key = key;
	}

	public void addColumns(HashSet<String> columnNames) {
		for (String columnName : columnNames) {
			addColumn(columnName);
		}
	}
	public void addColumn(String columnName) {
		addColumn(new Column(columnName));
	}
	public void addColumn(Column column) {
		this.columns.add(column);
	}
	public HashSet<Column> getColumns() {
		return this.columns;
	}
	public Column getColumn(String name) {
		return (Column)super.getMartConfiguratorObjectByNameIgnoreCase(this.columns, name);
	}
	public void setColumns(HashSet<Column> columns) {
		this.columns = columns;
	}

	public TableType getType() {
		return type;
	}

	public Boolean getMain() {
		return main;
	}

	public Column getKey() {
		return key;
	}

	public Range getRange() {
		return range;
	}

	public void setRange(Range range) {
		this.range = range;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"main = " + main + ", " +
			"key = " + key + ", " +
			"type = " + type + ", " +
			"range = " + range + ", " +
			"columns = " + columns;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Table table=(Table)object;
		return super.name.equalsIgnoreCase(table.name);
	}
	
	public boolean equalsIgnoreCase(Object object) {
		return this.equals(object);
	}

	@Override
	public int hashCode() {
		/*int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==main? 0 : main.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==key? 0 : key.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==range? 0 : range.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==fields? 0 : fields.hashCode());
		return hash;*/
		
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==name? 0 : name.toLowerCase().hashCode());	// Must specify toLowerCase here	TODO move to MCO?
		return hash;
	}

	public int compare(Table table1, Table table2) {
		if (table1==null && table2!=null) {
			return -1;
		} else if (table1!=null && table2==null) {
			return 1;
		}
		int compare = CompareUtils.compareString(table1.name.toLowerCase(), table2.name.toLowerCase());
		if (compare!=0) {
			return compare;
		}
		return table1.key.compareTo(table2.key);
	}
	public int compareTo(Table table) {
		return compare(this, table);
	}
	
	public Element generateXml() {
		Element element = super.generateXml();
		
		MartConfiguratorUtils.addAttribute(element, "key", (this.key!=null ? this.key.getName() : null));
		MartConfiguratorUtils.addAttribute(element, "main", this.main);
		MartConfiguratorUtils.addAttribute(element, "type", (this.type!=null ? this.type.getXmlValue() : null));
		this.range.addXmlAttribute(element, "range");
		
		//for (String field : this.fields) {
		for (Column column : this.columns) {
			element.addContent(column.generateXml());/*
			Element fieldJdom = new Element(Column.XML_ELEMENT_NAME);
			MartConfiguratorUtils.addAttribute(fieldJdom, "name", field);
			element.addContent(fieldJdom);*/
		}
		return element;
	}

}
