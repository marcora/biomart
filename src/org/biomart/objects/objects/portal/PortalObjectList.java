package org.biomart.objects.objects.portal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.jdom.Element;

public class PortalObjectList implements Serializable {

	private static final long serialVersionUID = -8506372260965562980L;

	protected String xmlElementName = null;
	private List<PortalObject> portalObjectList = null;
	
	public PortalObjectList(String xmlElementName) {
		this.xmlElementName = xmlElementName;
		this.portalObjectList = new ArrayList<PortalObject>();
	}
	public void addPortalObject(PortalObject portalObject) {
		this.portalObjectList.add(portalObject);
	}
	public PortalObject getPortalObject(String name) {
		for (PortalObject portalObject : this.portalObjectList) {
			if (portalObject.name.equals(name)) {
				return portalObject;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuffer portalObjects = new StringBuffer();
		for (int i = 0; this.portalObjectList!=null && i < this.portalObjectList.size(); i++) {
			portalObjects.append((i == 0 ? "" : ",") + this.portalObjectList.get(i).name);
		}
		return 
			"portalObjectList = " + portalObjects.toString();
	}
	
	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		PortalObjectList portalObjectList = (PortalObjectList)object;
		return (
			this.xmlElementName.equals(portalObjectList.xmlElementName)
		);
	}
	
	@Override
	public int hashCode() {
		return 0;	// always very few such objects in the portal
	}
	
	public Element generateXml() throws FunctionalException {
		Element element = new Element(xmlElementName);
		if (null!=this.portalObjectList) {
			for (PortalObject portalObject : this.portalObjectList) {
				element.addContent(portalObject.generateXml());
			}
		}
		return element;
	}
}
