package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.Collection;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.CompareUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;


public abstract class MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 3168050129952793078L;
	
	public static void main(String[] args) {}

	protected String name = null;
	protected String displayName = null;
	protected String description = null;
	protected Boolean visible = null;
	
	protected String xmlElementName = null;
	
	public MartConfiguratorObject() {}	// for Serialization
	public MartConfiguratorObject(String name, String displayName, String description, Boolean visible, String xmlElementName) {
		super();
		this.xmlElementName = xmlElementName;
		this.name = name;
		this.displayName = displayName;
		this.description = description;
		this.visible = visible;
	}

	/**
	 * Will pick the first element in case of name conflict... TODO throw exception then?
	 */
	public MartConfiguratorObject getMartConfiguratorObjectByName(Collection<? extends MartConfiguratorObject> collection, String name) {
		for (MartConfiguratorObject mco : collection) {
			if (name.equals(mco.name)) {
				return mco;
			}
		}
		return null;
	}
	public MartConfiguratorObject getMartConfiguratorObjectByNameIgnoreCase(Collection<? extends MartConfiguratorObject> collection, String name) {
		for (MartConfiguratorObject mco : collection) {
			if (name.equalsIgnoreCase(mco.name)) {
				return mco;
			}
		}
		return null;
	}

	public String getXmlElementName() {
		return xmlElementName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public String getDescription() {
		return description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Boolean getVisible() {
		return visible;
	}

	@Override
	public String toString() {
		return 
			"name = " + name + ", " +
			"displayName = " + displayName + ", " +
			"description = " + description + ", " +
			"visible = " + visible;
	}

	/*return (
			(this.name==martConfiguratorObject.name || (this.name!=null && name.equals(martConfiguratorObject.name))) &&
			(this.displayName==martConfiguratorObject.displayName || (this.displayName!=null && displayName.equals(martConfiguratorObject.displayName))) &&
			(this.description==martConfiguratorObject.description || (this.description!=null && description.equals(martConfiguratorObject.description))) &&
			(this.visible==martConfiguratorObject.visible || (this.visible!=null && visible.equals(martConfiguratorObject.visible))) &&
			(this.xmlElementName==martConfiguratorObject.xmlElementName || (this.xmlElementName!=null && xmlElementName.equals(martConfiguratorObject.xmlElementName)))
		);*/
	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		
		// If not the same type or different names -> not equal
		MartConfiguratorObject martConfiguratorObject=(MartConfiguratorObject)object;
		return (
				this.getClass().equals(object.getClass()) &&
				this.name.equals(martConfiguratorObject.name)
		);
	}
	protected boolean equalsIgnoreCase(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		
		// If not the same type or different names -> not equal
		MartConfiguratorObject martConfiguratorObject=(MartConfiguratorObject)object;
		return (
				this.getClass().equals(object.getClass()) &&
				this.name.equalsIgnoreCase(martConfiguratorObject.name)
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==name? 0 : name.hashCode());	// Sufficient for our system
		return hash;
		
		/*int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==name? 0 : name.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==displayName? 0 : displayName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==description? 0 : description.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==visible? 0 : visible.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==xmlElementName? 0 : xmlElementName.hashCode());
		return hash;*/
	}

	/**
	 * Unused for now, keep?
	 */
	protected int compareSuper(MartConfiguratorObject martConfiguratorObject1, MartConfiguratorObject martConfiguratorObject2) {
		if (martConfiguratorObject1==null && martConfiguratorObject2!=null) {
			return -1;
		} else if (martConfiguratorObject1!=null && martConfiguratorObject2==null) {
			return 1;
		}
		int compare = CompareUtils.compareString(martConfiguratorObject1.getName(), martConfiguratorObject2.getName());
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareString(martConfiguratorObject1.getDisplayName(), martConfiguratorObject2.getDisplayName());
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareString(martConfiguratorObject1.getDescription(), martConfiguratorObject2.getDescription());
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareBoolean(martConfiguratorObject1.getVisible(), martConfiguratorObject2.getVisible());
	}
	
	/**
	 * For XML generation
	 */
	protected Element generateXml() throws FunctionalException {
		Element jdomElement = new Element(xmlElementName);
		MartConfiguratorUtils.addAttribute(jdomElement, "name", this.name);
		MartConfiguratorUtils.addAttribute(jdomElement, "displayName", this.displayName);
		MartConfiguratorUtils.addAttribute(jdomElement, "description", this.description);
		MartConfiguratorUtils.addAttribute(jdomElement, "visible", this.visible);
		return jdomElement;
	}
}
