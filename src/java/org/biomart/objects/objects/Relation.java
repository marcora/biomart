package org.biomart.objects.objects;

import java.io.Serializable;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.objects.types.RelationType;
import org.jdom.Element;


public class Relation extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 7272012626195036989L;
	
	public static final String XML_ELEMENT_NAME = "relation";
	public static final McNodeType MC_NODE_TYPE = null;
	
	public static void main(String[] args) {}

	private RelationType type = null;
	private Table firstTable = null;
	private Table secondTable = null;

	// Redundant
	private String firstKey = null;
	private String secondKey = null;

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
			(super.equals(relation)) &&
			(this.firstTable==relation.firstTable || (this.firstTable!=null && firstTable.equals(relation.firstTable))) &&
			(this.secondTable==relation.secondTable || (this.secondTable!=null && secondTable.equals(relation.secondTable))) &&
			(this.firstKey==relation.firstKey || (this.firstKey!=null && firstKey.equals(relation.firstKey))) &&
			(this.secondKey==relation.secondKey || (this.secondKey!=null && secondKey.equals(relation.secondKey))) &&
			(this.type==relation.type || (this.type!=null && type.equals(relation.type)))
		);
	}

	/*@Override
	public int hashCode() {
		return super.hashCode();
	}*/
	
	/**
	 * Only for the node, children are treated separately
	 */
	public Element generateXml() throws FunctionalException {
		Element element = super.generateXml();
		
		MartConfiguratorUtils.addAttribute(element, "firstTable", (this.firstTable!=null ? this.firstTable.getName() : null));
		MartConfiguratorUtils.addAttribute(element, "secondTable", (this.secondTable!=null ? this.secondTable.getName() : null));
		MartConfiguratorUtils.addAttribute(element, "firstKey", this.firstKey);
		MartConfiguratorUtils.addAttribute(element, "secondKey", this.secondKey);
		MartConfiguratorUtils.addAttribute(element, "type", (this.type!=null ? this.type.getXmlValue() : null));
		
		return element;
	}

}
