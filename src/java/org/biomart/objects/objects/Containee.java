package org.biomart.objects.objects;

import java.io.Serializable;

import net.sf.json.JSONObject;

import org.biomart.objects.MartConfiguratorConstants;
import org.jdom.Element;
import org.jdom.Namespace;


public class Containee extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = -8763113328755703892L;

	public static void main(String[] args) {}
	
	private Container parentContainer = null;	// The parent container if any

	public Containee() {} 	// for Serialization
	public Containee(String name, String displayName, String description, Boolean visible, String xmlElementName, Container parentContainer) {
		super(name, displayName, description, visible, xmlElementName);
		this.parentContainer = parentContainer;
	}

	public Container getParentContainer() {
		return parentContainer;
	}

	/**
	 * Have to specify it manually sometimes
	 * @param parentContainer
	 */
	public void setParentContainer(Container parentContainer) {
		this.parentContainer = parentContainer;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"parentContainer = " + (null==parentContainer ? null : parentContainer.getName());
	}

	@Override
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
		);	
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		return hash;
	}
	
//	protected abstract Element generateXml();/* {
//		return super.generateXml();
//	}*/
//	protected abstract Element generateXmlForWebService(boolean recursively);/* {
//		return generateXmlForWebService(null, recursively);
//	}*/
//	protected abstract Element generateXmlForWebService(Namespace namespace, boolean recursively);/*{
//		return super.generateXmlForWebService(namespace);
//	}*/
//	protected abstract Element generateXmlForWebService();/*{
//		return generateXmlForWebService(null);
//	}*/
//	protected abstract Element generateXmlForWebService(Namespace namespace);/* {
//		//return super.generateXmlForWebService(namespace);
//	}*/
//	protected abstract JSONObject generateJsonForWebService();/* {
//		return super.generateJsonForWebService();
//	}*/

	protected Element generateXml() {
		return super.generateXml();
	}
	protected Element generateXmlForWebService(boolean recursively) {
		return generateXmlForWebService(null, recursively);
	}
	protected Element generateXmlForWebService(Namespace namespace, boolean recursively) {
		return super.generateXmlForWebService(namespace);
	}
	protected Element generateXmlForWebService() {
		return generateXmlForWebService(null);
	}
	protected Element generateXmlForWebService(Namespace namespace) {
		return super.generateXmlForWebService(namespace);
	}
	protected JSONObject generateJsonForWebService() {
		return super.generateJsonForWebService();
	}
}
