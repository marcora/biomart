package org.biomart.objects.objects;

import java.io.Serializable;


import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;


public class Relation extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 7272012626195036989L;
	
	public static final String XML_ELEMENT_NAME = "relation";
	
	public static void main(String[] args) {}

	private Table firstTable = null;
	private Table secondTable = null;
	private String firstKey = null;
	private String secondKey = null;
	private RelationType type = null;

	public Relation(String name, Table firstTable, Table secondTable, String firstKey, String secondKey, RelationType type) {
		super(name, null, null, null, XML_ELEMENT_NAME);	// displayName, description & visible do not apply for that object
		this.firstTable = firstTable;
		this.secondTable = secondTable;
		this.firstKey = firstKey;
		this.secondKey = secondKey;
		this.type = type;
	}

	public RelationType getType() {
		return type;
	}

	public Table getFirstTable() {
		return firstTable;
	}

	public Table getSecondTable() {
		return secondTable;
	}

	public String getFirstKey() {
		return firstKey;
	}

	public String getSecondKey() {
		return secondKey;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"firstTable = " + firstTable + ", " +
			"secondTable" + secondTable + ", " +
			"firstKey = " + firstKey + ", " +
			"secondKey = " + secondKey + ", " +
			"type = " + type;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Relation relation=(Relation)object;
		return (
			super.name.equals(relation.name)
		);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	/**
	 * Only for the node, children are treated separately
	 */
	public Element generateXml() {
		Element element = super.generateXml();
		
		MartConfiguratorUtils.addAttribute(element, "firstTable", (this.firstTable!=null ? this.firstTable.getName() : null));
		MartConfiguratorUtils.addAttribute(element, "secondTable", (this.secondTable!=null ? this.secondTable.getName() : null));
		MartConfiguratorUtils.addAttribute(element, "firstKey", this.firstKey);
		MartConfiguratorUtils.addAttribute(element, "secondKey", this.secondKey);
		MartConfiguratorUtils.addAttribute(element, "type", (this.type!=null ? this.type.getXmlValue() : null));
		
		return element;
	}

}
