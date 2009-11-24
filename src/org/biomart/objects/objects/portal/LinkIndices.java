package org.biomart.objects.objects.portal;


import java.io.Serializable;

import org.biomart.configurator.utils.type.McNodeType;


public class LinkIndices extends PortalObjectList implements Serializable {

	private static final long serialVersionUID = 1770274084924277827L;
	
	public static final String XML_ELEMENT_NAME = "linkIndices";
	public static final McNodeType MC_NODE_TYPE = null;
	
	public LinkIndices() {
		super(XML_ELEMENT_NAME);
	}
}
