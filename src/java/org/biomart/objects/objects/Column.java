package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.Comparator;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorConstants;


public class Column extends MartConfiguratorObject implements Comparable<Column>, Comparator<Column>,  Serializable {

	private static final long serialVersionUID = 6824928327693517287L;
	
	public static final String XML_ELEMENT_NAME = "column";
	public static final McNodeType MC_NODE_TYPE = null;
	
	public static void main(String[] args) {}

	private Boolean key = null;
	private Column origin = null;	// original column (in a source schema)
	
	public Column(String name) {
		this(name, false);
	}
	public Column(String name, boolean key) {
		super(name, null, null, null, XML_ELEMENT_NAME);
		this.key = key;
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new Column(this.name, this.key);
	}
	
	public void setKey(boolean key) {
		this.key = key;
	}
	
	public Boolean getKey() {
		return key;
	}
	public String getName() {
		return name;
	}
	public Column getOrigin() {
		return origin;
	}
	public void setOrigin(Column origin) {
		this.origin = origin;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"key = " + key;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Column table=(Column)object;
		return super.name.equalsIgnoreCase(table.name);
	}
	
	public boolean equalsIgnoreCase(Object object) {
		return this.equals(object);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==name? 0 : name.toLowerCase().hashCode());	// Must specify toLowerCase here	TODO move to MCO?
		return hash;
	}

	public int compare(Column column1, Column column2) {
		if (column1==null && column2!=null) {
			return -1;
		} else if (column1!=null && column2==null) {
			return 1;
		}
		return CompareUtils.compareString(column1.name.toLowerCase(), column2.name.toLowerCase());
	}

	public int compareTo(Column column) {
		return compare(this, column);
	}
	
	public String getStringValue() {
		return super.name;
	}
	
	/*public Element generateXml() {
		return super.generateXml();
	}*/
}
