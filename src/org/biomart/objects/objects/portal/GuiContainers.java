package org.biomart.objects.objects.portal;


import java.io.Serializable;

import org.biomart.configurator.utils.type.McNodeType;


public class GuiContainers extends PortalObjectList implements Serializable {

	private static final long serialVersionUID = -8752125418778311871L;
	
	public static final String XML_ELEMENT_NAME = "guiContainers";
	public static final McNodeType MC_NODE_TYPE = null;
	
	public GuiContainers() {
		super(XML_ELEMENT_NAME);
	}
}
