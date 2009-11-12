package org.biomart.objects.objects;

import java.io.Serializable;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.jdom.Element;


public abstract class Containee extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = -8763113328755703892L;

	public static void main(String[] args) {}
	
	// Redundant
	protected Integer level = null;
	protected Container parentContainer = null;	// The parent container if any

	public Containee() {} 	// for Serialization
	public Containee(String name, String displayName, String description, Boolean visible, String xmlElementName) {
		super(name, displayName, description, visible, xmlElementName);
	}
	
	public void setParentContainer(Container parentContainer) {
		this.level = this.parentContainer==null ? 0 : this.parentContainer.level+1;
		this.parentContainer = parentContainer;
	}
	
	public Container getParentContainer() {
		return parentContainer;
	}

	public Integer getLevel() {
		return level;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"level = " + level + ", " +
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
}
