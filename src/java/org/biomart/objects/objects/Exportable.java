package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;



public class Exportable extends Portable implements Comparable<Exportable>, Comparator<Exportable>, Serializable {

	private static final long serialVersionUID = -6467892724196660537L;
	
	public static final String XML_ELEMENT_NAME = "exportable";
	
	public static void main(String[] args) {}

	private List<Attribute> attributes = null;
	private List<String> attributeNames = null;
	
	// For backward compatibility
	private Boolean formerDefault = null;

	public Exportable(PartitionTable mainPartitionTable, String name) {
		super(mainPartitionTable, name, null, null, null, XML_ELEMENT_NAME);	// displayName, description & visible do not apply for that object
		
		this.attributes = new ArrayList<Attribute>();
		this.attributeNames = new ArrayList<String>();
	}
	
	public void addAttribute(Attribute attribute) {
		this.attributes.add(attribute);
		this.attributeNames.add(attribute.getName());
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"attributes = " + attributes + ", " + 
			"formerDefault = " + formerDefault;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Exportable exportable=(Exportable)object;
		return (
			(this.attributes==exportable.attributes || (this.attributes!=null && attributes.equals(exportable.attributes))) &&
			(this.formerDefault==exportable.formerDefault || (this.formerDefault!=null && formerDefault.equals(exportable.formerDefault)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==attributes? 0 : attributes.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==formerDefault? 0 : formerDefault.hashCode());
		return hash;
	}

	public int compare(Exportable exportable1, Exportable exportable2) {
		if (exportable1==null && exportable2!=null) {
			return -1;
		} else if (exportable1!=null && exportable2==null) {
			return 1;
		}
		return CompareUtils.compareNull(exportable1.attributes, exportable2.attributes);
	}

	public int compareTo(Exportable importable) {
		return compare(this, importable);
	}

	public Boolean getFormerDefault() {
		return formerDefault;
	}

	public void setFormerDefault(Boolean formerDefault) {
		this.formerDefault = formerDefault;
	}
	
	public org.jdom.Element generateXml() {
		org.jdom.Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "formerDefault", this.formerDefault);
		MartConfiguratorUtils.addAttribute(element, "attributes", this.attributeNames);
		return element;
	}
}
