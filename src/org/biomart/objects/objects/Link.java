package org.biomart.objects.objects;

import java.io.Serializable;

public class Link extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = -5810163820758015395L;

	public static void main(String[] args) {}
	
	public Link() {} 	// for Serialization
	public Link(String name, String displayName, String description, Boolean visible, String xmlElementName) {
		super(name, displayName, description, visible, xmlElementName);
	}
	
	//TODO
}
