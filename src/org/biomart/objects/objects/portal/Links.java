package org.biomart.objects.objects.portal;

import java.io.Serializable;

public class Links extends PortalObject implements Serializable {

	private static final long serialVersionUID = -5810163820758015395L;

	public static final String XML_ELEMENT_NAME = "links";
	
	public Links(String name) {
		super(XML_ELEMENT_NAME, name);
	}
	
	//TODO
}
