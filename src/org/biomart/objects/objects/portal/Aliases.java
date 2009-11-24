package org.biomart.objects.objects.portal;


import java.io.Serializable;

import org.biomart.configurator.utils.type.McNodeType;


public class Aliases extends PortalObjectList implements Serializable {

	private static final long serialVersionUID = -46670820727037179L;
	
	public static final String XML_ELEMENT_NAME = "aliases";
	public static final McNodeType MC_NODE_TYPE = null;
	
	public Aliases() {
		super(XML_ELEMENT_NAME);
	}
}
