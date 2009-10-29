package org.biomart.objects.objects;


import java.io.Serializable;

import net.sf.json.JSONObject;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;
import org.jdom.Namespace;


public class MartConfiguratorObject implements Serializable /*implements Comparable<MartConfiguratorObject>, Comparator<MartConfiguratorObject>*/ {

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
			/*super.toString() + ", " + */
			"name = " + name + ", " +
			"displayName = " + displayName + ", " +
			"description = " + description + ", " +
			"visible = " + visible/* + ", " +
			"jdomElement = " + jdomElement*/;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		MartConfiguratorObject martConfiguratorObject=(MartConfiguratorObject)object;
		return (
			(this.name==martConfiguratorObject.name || (this.name!=null && name.equals(martConfiguratorObject.name))) &&
			(this.displayName==martConfiguratorObject.displayName || (this.displayName!=null && displayName.equals(martConfiguratorObject.displayName))) &&
			(this.description==martConfiguratorObject.description || (this.description!=null && description.equals(martConfiguratorObject.description)))
			/*(this.visible==martConfiguratorObject.visible || (this.visible!=null && visible.equals(martConfiguratorObject.visible))) &&
			(this.jdomElement==martConfiguratorObject.jdomElement || (this.jdomElement!=null && jdomElement.equals(martConfiguratorObject.jdomElement)))*/
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==name? 0 : name.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==displayName? 0 : displayName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==description? 0 : description.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==visible? 0 : visible.hashCode());
		/*hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==jdomElement? 0 : jdomElement.hashCode());*/
		return hash;
	}

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
	
	protected Element generateXml() {
		Element jdomElement = new Element(xmlElementName);
		MartConfiguratorUtils.addAttribute(jdomElement, "name", this.name);
		MartConfiguratorUtils.addAttribute(jdomElement, "displayName", this.displayName);
		MartConfiguratorUtils.addAttribute(jdomElement, "description", this.description);
		MartConfiguratorUtils.addAttribute(jdomElement, "visible", this.visible);
		return jdomElement;
	}
	
	
	
	
	// ===================================== Should be a different class ============================================

	protected MartConfiguratorObject(MartConfiguratorObject martConfiguratorObject) throws CloneNotSupportedException {
		this(martConfiguratorObject, null);
	}
	protected MartConfiguratorObject(MartConfiguratorObject martConfiguratorObject, Part part) throws CloneNotSupportedException {	// creates a light clone (temporary solution)
		this(
				part==null ? martConfiguratorObject.name : 
					MartConfiguratorUtils.replacePartitionReferencesByValues(martConfiguratorObject.name, part),
				part==null ? martConfiguratorObject.displayName : 
					MartConfiguratorUtils.replacePartitionReferencesByValues(martConfiguratorObject.displayName, part),
				part==null ? martConfiguratorObject.description : 
					MartConfiguratorUtils.replacePartitionReferencesByValues(martConfiguratorObject.description, part),
				martConfiguratorObject.visible, martConfiguratorObject.xmlElementName);
	}
	
	protected Element generateXmlForWebService(Namespace namespace) {
		Element jdomObject = new Element(xmlElementName, namespace);
		
		MartConfiguratorUtils.addAttribute(jdomObject, "name", this.name);
		MartConfiguratorUtils.addAttribute(jdomObject, "displayName", this.displayName);
		MartConfiguratorUtils.addAttribute(jdomObject, "description", this.description);
		MartConfiguratorUtils.addAttribute(jdomObject, "visible", this.visible);		
		
		return jdomObject;
	}
	
	protected JSONObject generateJsonForWebService() {
		JSONObject object = new JSONObject();
		
		object.put("name", this.name);
		object.put("displayName", this.displayName);
		object.put("visible", this.visible);
		object.put("description", this.description);
		
		JSONObject datasetJsonObject = new JSONObject();
		datasetJsonObject.put(xmlElementName, object);
		
		return datasetJsonObject;
	}
}
