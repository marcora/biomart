package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;


public class Table extends MartConfiguratorObject implements Comparable<Table>, Comparator<Table>, Serializable {

	private static final long serialVersionUID = 994381514343812305L;
	
	public static final String XML_ELEMENT_NAME = "table";
	public static final String COLUMN_XML_ELEMENT_NAME = "column";
	
	public static void main(String[] args) {}

	private Range range = null;
	private HashSet<String> fields = null;
	private String key = null;
	private Boolean main = null;	// Whether this is a main table
	private TableType type = null;
	
	public Table(String name, PartitionTable mainPartitionTable, boolean main, TableType type,
			String key, HashSet<String> fields) {
		super(name, null, null, null, XML_ELEMENT_NAME);	// displayName, description & visible do not apply for that object
		this.key = key;
		this.type = type;
		this.fields = fields;
		
		this.range = new Range(mainPartitionTable, false);
		
		this.main = main;
	}
	
	public void setKey(String key) {
		this.key = key;
	}

	public TableType getType() {
		return type;
	}

	public Boolean getMain() {
		return main;
	}

	public String getKey() {
		return key;
	}

	public Range getRange() {
		return range;
	}

	public HashSet<String> getFields() {
		return fields;
	}

	public void setRange(Range range) {
		this.range = range;
	}

	public void setFields(HashSet<String> fields) {
		this.fields = fields;
	}

	public void addFields(HashSet<String> fields) {
		this.fields.addAll(fields);
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"main = " + main + ", " +
			"key = " + key + ", " +
			"type = " + type + ", " +
			"range = " + range + ", " +
			"fields = " + fields;
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
		return CompareUtils.compareString(table1.key.toLowerCase(), table2.key.toLowerCase());
	}

	public int compareTo(Table table) {
		return compare(this, table);
	}
	
	/**
	 * Only for the node, children are treated separately
	 */
	public Element generateXml() {
		Element element = super.generateXml();
		
		MartConfiguratorUtils.addAttribute(element, "key", this.key);
		MartConfiguratorUtils.addAttribute(element, "main", this.main);
		MartConfiguratorUtils.addAttribute(element, "type", (this.type!=null ? this.type.getXmlValue() : null));
		this.range.addXmlAttribute(element, "range");
		
		for (String field : this.fields) {
			Element fieldJdom = new Element(COLUMN_XML_ELEMENT_NAME);
			MartConfiguratorUtils.addAttribute(fieldJdom, "name", field);
			element.addContent(fieldJdom);
		}
		return element;
	}

}
