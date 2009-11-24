package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldPortable extends OldNode /*implements Comparable<OldPortable>, Comparator<OldPortable>*/ {

	public static void main(String[] args) {}

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"default", "internalName", "linkName", "name", "type", "linkVersion", "description", "pointer", "orderBy", "hidden",
			"hideDisplay", 	// property ignored for importable "wormbase_phenotype_id" in dataset "wormbase_gene"
	}));
	
	protected String internalName = null;
	protected String linkName = null;
	protected String name = null;
	protected String type = null;
	protected String linkVersion = null;
	protected String description = null;
	protected Boolean pointer = null;
	protected String orderBy = null;

	protected OldPortable(Element jdomPortable) throws FunctionalException {
		this(jdomPortable,
				jdomPortable.getAttributeValue("internalName"),
				jdomPortable.getAttributeValue("hidden"),
				jdomPortable.getAttributeValue("linkName"),
				jdomPortable.getAttributeValue("name"),
				jdomPortable.getAttributeValue("type"),
				jdomPortable.getAttributeValue("linkVersion"),
				jdomPortable.getAttributeValue("description"),
				jdomPortable.getAttributeValue("pointer"),
				jdomPortable.getAttributeValue("orderBy")
		);
	}
			
	private OldPortable(Element jdomDatasetConfig, String internalName, String hidden, String linkName, 
			String name, String type, String linkVersion, String description, String pointer, String orderBy) throws FunctionalException {
		super(jdomDatasetConfig);

		if (this.hidden) {		// assigned in OldNode
			this.valid = false;
			return;
		}
		
		this.internalName = internalName;
		this.linkName = linkName;
		this.name = name;
		this.type = type;
		this.linkVersion = linkVersion;
		this.description = description;
		this.pointer = TransformationUtils.getBooleanValueFromString(pointer, "pointer");
		this.orderBy = orderBy;
		
		MyUtils.checkStatusProgram(this.name!=null && this.linkName!=null && this.name.equals(this.linkName));
	}

	public String getInternalName() {
		return internalName;
	}

	public String getLinkName() {
		return linkName;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getLinkVersion() {
		return linkVersion;
	}

	public String getDescription() {
		return description;
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setLinkVersion(String linkVersion) {
		this.linkVersion = linkVersion;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public Boolean getPointer() {
		return pointer;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " +
			"internalName = " + internalName + ", " +
			"linkName = " + linkName + ", " +
			"name = " + name + ", " +
			"type = " + type + ", " +
			"linkVersion = " + linkVersion + ", " +
			"description = " + description + ", " +
			"pointer = " + pointer + ", " +
			"orderBy = " + orderBy;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		OldPortable oldPortable=(OldPortable)object;
		return (
			(this.internalName==oldPortable.internalName || (this.internalName!=null && internalName.equals(oldPortable.internalName))) &&
			(this.linkName==oldPortable.linkName || (this.linkName!=null && linkName.equals(oldPortable.linkName))) &&
			(this.name==oldPortable.name || (this.name!=null && name.equals(oldPortable.name))) &&
			(this.type==oldPortable.type || (this.type!=null && type.equals(oldPortable.type))) &&
			(this.linkVersion==oldPortable.linkVersion || (this.linkVersion!=null && linkVersion.equals(oldPortable.linkVersion))) &&
			(this.description==oldPortable.description || (this.description!=null && description.equals(oldPortable.description)))/* &&
			(this.elements==oldPortable.elements || (this.elements!=null && elements.equals(oldPortable.elements)))*/
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==internalName? 0 : internalName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==linkName? 0 : linkName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==name? 0 : name.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==type? 0 : type.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==linkVersion? 0 : linkVersion.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==description? 0 : description.hashCode());
		/*hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==elements? 0 : elements.hashCode());*/
		return hash;
	}

	/*@Override
	public int compare(OldPortable oldPortable1, OldPortable oldPortable2) {
		if (oldPortable1==null && oldPortable2!=null) {
			return -1;
		} else if (oldPortable1!=null && oldPortable2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(oldPortable1.internalName, oldPortable2.internalName);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldPortable1.linkName, oldPortable2.linkName);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldPortable1.name, oldPortable2.name);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldPortable1.type, oldPortable2.type);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldPortable1.linkVersion, oldPortable2.linkVersion);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldPortable1.description, oldPortable2.description);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(oldPortable1.elements, oldPortable2.elements);
	}

	@Override
	public int compareTo(OldPortable oldPortable) {
		return compare(this, oldPortable);
	}*/
	
	/*public Portable transform(List<martConfigurator.objects.Element> elementList) {
		Importable importable = new Importable(this.internalName, null, elementList);	//String name, String displayName, String description, Boolean visible, List<String> range, List<String> filters) {
		return importable;
	}*/

}