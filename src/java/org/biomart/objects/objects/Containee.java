package org.biomart.objects.objects;

import java.io.Serializable;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.jdom.Element;
import org.jdom.Namespace;


public class Containee extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = -8763113328755703892L;

	public static void main(String[] args) {}
	
	// Redundant
	protected Container parentContainer = null;	// The parent container if any

	public Containee() {} 	// for Serialization
	public Containee(String name, String displayName, String description, Boolean visible, String xmlElementName, Container parentContainer) {
		super(name, displayName, description, visible, xmlElementName);
		this.parentContainer = parentContainer;
	}
	
	public Container getParentContainer() {
		return parentContainer;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"parentContainer = " + (null==parentContainer ? null : parentContainer.getName());
	}

	/*@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Containee containee=(Containee)object;
		return (
				super.equals(containee)
				//TODO parentContainer?
		);	
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		//TODO parentContainer?
		return hash;
	}*/
	
	protected Element generateXml() {
		return super.generateXml();
	}
	


	
	// ===================================== Should be a different class ============================================

	protected Containee(Containee containee) {
		this(containee, null);
	}
	protected Containee(Containee containee, Part part) {	// creates a light clone (temporary solution)
		super(containee, part);
	}
	protected void updatePointerClone(org.biomart.objects.objects.Element pointingElement) {
		super.updatePointerClone(pointingElement);
		this.parentContainer = pointingElement.parentContainer;
	}
	
	protected Jsoml generateOutputForWebService(boolean xml) throws FunctionalException {
		return super.generateOutputForWebService(xml);
	}
	
	protected Element generateXmlForWebService(boolean recursively) throws FunctionalException {
		return generateXmlForWebService(null, recursively);
	}
	protected Element generateXmlForWebService(Namespace namespace, boolean recursively) throws FunctionalException {
		return super.generateXmlForWebService(namespace);
	}
	protected Element generateXmlForWebService() throws FunctionalException {
		return generateXmlForWebService(null);
	}
	protected Element generateXmlForWebService(Namespace namespace) throws FunctionalException {
		return super.generateXmlForWebService(namespace);
	}
	protected JSONObject generateJsonForWebService() {
		return super.generateJsonForWebService();
	}
}
