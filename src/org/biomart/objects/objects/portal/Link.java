package org.biomart.objects.objects.portal;

import java.io.Serializable;

public class Link extends PortalObject implements Serializable {

	private static final long serialVersionUID = -5810163820758015395L;

	public static final String XML_ELEMENT_NAME = "link";
	
	public Link(String name) {
		super(XML_ELEMENT_NAME, name);
	}
	
	//TODO
}
