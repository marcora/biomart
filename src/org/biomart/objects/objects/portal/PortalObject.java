package org.biomart.objects.objects.portal;

import java.io.Serializable;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;

public class PortalObject implements Serializable {	// TODO merge with MartConfiguratorObject?

	private static final long serialVersionUID = 4832749515681067224L;
	
	public static void main(String[] args) {}

	private String xmlElementName = null;
	protected String name = null;
	
	public PortalObject(String xmlElementName, String name) {
		this.xmlElementName = xmlElementName;
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return "name = " + name;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		User user=(User)object;
		return (
			this.getClass().equals(object.getClass()) &&
			(this.name==user.name || (this.name!=null && name.equals(user.name)))
		);
	}
	
	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==name? 0 : name.hashCode());	// Sufficient for our system
		return hash;
	}
	
	public Element generateXml() throws FunctionalException {
		Element element = new Element(xmlElementName);
		MartConfiguratorUtils.addAttribute(element, "name", this.name);
		return element;
	}
}
