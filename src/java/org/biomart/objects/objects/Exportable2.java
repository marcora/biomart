package org.biomart.objects.objects;


import java.io.Serializable;

import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorUtils;



public class Exportable2 extends Portable implements Serializable {

	private static final long serialVersionUID = -6467892724196660537L;
	
	public static final String XML_ELEMENT_NAME = "exportable";
	public static final McNodeType MC_NODE_TYPE = null;
	
	public static void main(String[] args) {}
	
	// For backward compatibility
	private Boolean formerDefault = null;	// Only mattered in the exportable
	
	public Exportable2(PartitionTable mainPartitionTable, String name) {
		super(mainPartitionTable, name, XML_ELEMENT_NAME);	// displayName, description & visible do not apply for that object
	}

	public Boolean getFormerDefault() {
		return formerDefault;
	}

	public void setFormerDefault(Boolean formerDefault) {
		this.formerDefault = formerDefault;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"formerDefault = " + formerDefault;
	}
	
	public org.jdom.Element generateXml() {
		org.jdom.Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "formerDefault", this.formerDefault);
		return element;
	}
}
