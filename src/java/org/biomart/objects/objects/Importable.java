package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorUtils;



public class Importable extends Portable implements /*Comparable<Importable>, Comparator<Importable>,*/ Serializable {

	private static final long serialVersionUID = -7990001822496911207L;
	
	public static final String XML_ELEMENT_NAME = "importable";
	public static final McNodeType MC_NODE_TYPE = null;
	
	public static void main(String[] args) {}

	private List<Filter> filters = null;
	
	// Redundant
	private List<String> filterNames = null;

	public Importable(PartitionTable mainPartitionTable, String name) {
		super(mainPartitionTable, name, XML_ELEMENT_NAME);	// displayName, description & visible do not apply for that object
		
		this.filters = new ArrayList<Filter>();
		this.filterNames = new ArrayList<String>();
	}
	
	public void addFilter(Filter filter) {
		this.filters.add(filter);
		this.filterNames.add(filter.getName());
	}

	public List<Filter> getFilters() {
		return new ArrayList<Filter>(filters);
	}
	
	public Filter getFilter(String name) {
		return (Filter)super.getMartConfiguratorObjectByName(this.filters, name);
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"filterNames = " + filterNames;
	}

	/*@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Importable importable=(Importable)object;
		return (
			(this.filters==importable.filters || (this.filters!=null && filters.equals(importable.filters)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==filters? 0 : filters.hashCode());
		return hash;
	}

	public int compare(Importable importable1, Importable importable2) {
		if (importable1==null && importable2!=null) {
			return -1;
		} else if (importable1!=null && importable2==null) {
			return 1;
		}
		return CompareUtils.compareNull(importable1.filters, importable2.filters);
	}

	public int compareTo(Importable importable) {
		return compare(this, importable);
	}*/
	
	public org.jdom.Element generateXml() {
		org.jdom.Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "filters", this.filterNames);
		return element;
	}
}
