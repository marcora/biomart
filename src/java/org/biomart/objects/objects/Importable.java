package org.biomart.objects.objects;


import java.io.Serializable;

import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorUtils;



public class Importable extends Portable implements Serializable {

	private static final long serialVersionUID = -7990001822496911207L;
	
	public static final String XML_ELEMENT_NAME = "importable";
	public static final McNodeType MC_NODE_TYPE = null;
	
	public static void main(String[] args) {}

	public Importable(PartitionTable mainPartitionTable, String name) {
		super(mainPartitionTable, name, XML_ELEMENT_NAME);	// displayName, description & visible do not apply for that object
	}
	
	public org.jdom.Element generateXml() {
		return super.generateXml();	// do not delete
	}
}
