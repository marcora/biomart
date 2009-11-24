package org.biomart.objects.objects.portal;


import java.io.Serializable;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;


public class Users extends PortalObjectList implements Serializable {

	private static final long serialVersionUID = -46670820727037179L;
	
	public static final String XML_ELEMENT_NAME = "users";
	public static final McNodeType MC_NODE_TYPE = null;
	
	private PortalObject defaultObject = null;
	
	public Users() {
		super(XML_ELEMENT_NAME);
	}
	public PortalObject getDefaultObject() {
		return defaultObject;
	}
	@Override
	public void addPortalObject(PortalObject portalObject) {
		this.addPortalObject(portalObject, false);
	}
	public void addPortalObject(PortalObject portalObject, boolean isDefaultObject) {
		super.addPortalObject(portalObject);
		if (isDefaultObject) {
			this.defaultObject = portalObject;
		}
	}
	public Element generateXml() throws FunctionalException {
		Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "default", this.defaultObject);
		return element;
	}
}
