package org.biomart.objects.objects;


import java.io.Serializable;

import org.biomart.configurator.utils.type.McNodeType;



public class Importable2 extends Portable implements Serializable {

	private static final long serialVersionUID = -7990001822496911207L;
	
	public static final String XML_ELEMENT_NAME = "importable";
	public static final McNodeType MC_NODE_TYPE = null;
	
	public static void main(String[] args) {}

	public Importable2(PartitionTable mainPartitionTable, String name) {
		super(mainPartitionTable, name, XML_ELEMENT_NAME);	// displayName, description & visible do not apply for that object
	}
}
