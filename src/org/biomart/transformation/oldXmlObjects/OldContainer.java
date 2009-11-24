package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.objects.Container;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldContainer extends OldNode /*implements Comparable<OldContainer>, Comparator<OldContainer>*/ {

	public static void main(String[] args) {}

	public Boolean hidden = null;
	public String internalName = null;
	public String displayName = null;
	public String description = null;
	public Boolean hideDisplay = null;
	public List<String> outFormats = null;
	public Integer level = null;

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"hidden", "internalName", "displayName", "description", "hideDisplay", "outFormats", "maxSelect",
			"enableSelectAll"	// for "dspotatov01" dataset
	}));
	
	protected OldContainer(Element jdomDatasetConfig, Integer level) throws FunctionalException {
		this(jdomDatasetConfig, level,
				jdomDatasetConfig.getAttributeValue("hidden"),
				jdomDatasetConfig.getAttributeValue("internalName"),
				jdomDatasetConfig.getAttributeValue("displayName"),
				jdomDatasetConfig.getAttributeValue("description"),
				jdomDatasetConfig.getAttributeValue("hideDisplay"),
				jdomDatasetConfig.getAttributeValue("outFormats")
		);
	}
			
	private OldContainer(Element jdomElement, Integer level, 
			String hidden, String internalName, String displayName, String description, 
			String hideDisplay, String outFormats) throws FunctionalException {
		super(jdomElement);
		
		this.hidden = TransformationUtils.getBooleanValueFromString(hidden, "hidden");
		if (this.hidden) {
			this.valid = false;
			return;
		}
		
		this.internalName = internalName;
		this.displayName = displayName;
		this.description = description;
		this.hideDisplay = TransformationUtils.getBooleanValueFromString(hideDisplay, "hideDisplay");
		this.outFormats = new ArrayList<String>();
		if (null!=outFormats) {
			String[] split = outFormats.split(MartServiceConstants.ELEMENT_SEPARATOR);
			this.outFormats.addAll(Arrays.asList(split));
		}
		this.level = level;
	}

	public boolean isNotHidden() {
		return !hidden;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public String getInternalName() {
		return internalName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	public Boolean getHideDisplay() {
		return hideDisplay;
	}

	public List<String> getOutFormats() {
		return outFormats;
	}

	public Integer getLevel() {
		return level;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setHideDisplay(Boolean hideDisplay) {
		this.hideDisplay = hideDisplay;
	}

	public void setOutFormats(List<String> outFormats) {
		this.outFormats = outFormats;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " +
			"hidden = " + hidden + ", " +
			"internalName = " + internalName + ", " +
			"displayName = " + displayName + ", " +
			"description = " + description + ", " +
			"hideDisplay = " + hideDisplay + ", " +
			"outFormats = " + outFormats + ", " +
			"level = " + level;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		OldContainer oldContainer=(OldContainer)object;
		return (
			(this.hidden==oldContainer.hidden || (this.hidden!=null && hidden.equals(oldContainer.hidden))) &&
			(this.internalName==oldContainer.internalName || (this.internalName!=null && internalName.equals(oldContainer.internalName))) &&
			(this.displayName==oldContainer.displayName || (this.displayName!=null && displayName.equals(oldContainer.displayName))) &&
			(this.description==oldContainer.description || (this.description!=null && description.equals(oldContainer.description))) &&
			(this.hideDisplay==oldContainer.hideDisplay || (this.hideDisplay!=null && hideDisplay.equals(oldContainer.hideDisplay))) &&
			(this.outFormats==oldContainer.outFormats || (this.outFormats!=null && outFormats.equals(oldContainer.outFormats))) &&
			(this.level==oldContainer.level || (this.level!=null && level.equals(oldContainer.level)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==hidden? 0 : hidden.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==internalName? 0 : internalName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==displayName? 0 : displayName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==description? 0 : description.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==hideDisplay? 0 : hideDisplay.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==outFormats? 0 : outFormats.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==level? 0 : level.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldContainer oldContainer1, OldContainer oldContainer2) {
		if (oldContainer1==null && oldContainer2!=null) {
			return -1;
		} else if (oldContainer1!=null && oldContainer2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(oldContainer1.hidden, oldContainer2.hidden);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldContainer1.internalName, oldContainer2.internalName);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldContainer1.displayName, oldContainer2.displayName);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldContainer1.description, oldContainer2.description);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldContainer1.hideDisplay, oldContainer2.hideDisplay);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldContainer1.outFormats, oldContainer2.outFormats);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldContainer1.maxSelect, oldContainer2.maxSelect);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(oldContainer1.level, oldContainer2.level);
	}

	@Override
	public int compareTo(OldContainer oldContainer) {
		return compare(this, oldContainer);
	}*/
	
	public Container transform(int level, Integer queryRestriction) {
		Container container = new Container(this.internalName, this.displayName, this.description, !this.hideDisplay, 
				queryRestriction);
		return container;	
	}

}