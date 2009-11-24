package org.biomart.objects.objects.portal;


import java.io.Serializable;

import org.biomart.configurator.utils.type.McNodeType;


public class User extends PortalObject implements Serializable {

	private static final long serialVersionUID = -6247510819913400939L;
	
	public static final String XML_ELEMENT_NAME = "user";
	public static final McNodeType MC_NODE_TYPE = null;
	
	public static void main(String[] args) {}
	
	public User(String name) {
		super(XML_ELEMENT_NAME, name);
	}
}
