package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldSpecificAttributeContent extends OldAttribute /*implements Comparable<OldSpecificAttributeContent>, Comparator<OldSpecificAttributeContent>*/ {

	public static void main(String[] args) {}

	private String rangeInternalName = null;

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"internalName"
	}));
	
	public OldSpecificAttributeContent(Element jdomElement) throws FunctionalException {
		this(jdomElement, 
				jdomElement.getAttributeValue("internalName")
		);
	}
	private OldSpecificAttributeContent(Element jdomElement, String rangeInternalName) throws FunctionalException {
		super(jdomElement);
		
		this.internalName = null;	// Erase internalName as it's erroneous
		this.rangeInternalName = rangeInternalName;

		TransformationUtils.checkJdomElementProperties(jdomElement, OldElementPlaceHolder.propertyList,
				OldElement.propertyList, OldAttribute.propertyList, OldSpecificAttributeContent.propertyList);
	}

	public String getRangeInternalName() {
		return rangeInternalName;
	}

	public void setRangeInternalName(String rangeInternalName) {
		this.rangeInternalName = rangeInternalName;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " +
			"rangeInternalName = " + rangeInternalName;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		OldSpecificAttributeContent oldSpecificAttributeContent=(OldSpecificAttributeContent)object;
		return (
			(this.rangeInternalName==oldSpecificAttributeContent.rangeInternalName || (this.rangeInternalName!=null && rangeInternalName.equals(oldSpecificAttributeContent.rangeInternalName)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==rangeInternalName? 0 : rangeInternalName.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldSpecificAttributeContent oldSpecificAttributeContent1, OldSpecificAttributeContent oldSpecificAttributeContent2) {
		if (oldSpecificAttributeContent1==null && oldSpecificAttributeContent2!=null) {
			return -1;
		} else if (oldSpecificAttributeContent1!=null && oldSpecificAttributeContent2==null) {
			return 1;
		}
		return CompareUtils.compareNull(oldSpecificAttributeContent1.rangeInternalName, oldSpecificAttributeContent2.rangeInternalName);
	}

	@Override
	public int compareTo(OldSpecificAttributeContent oldSpecificAttributeContent) {
		return compare(this, oldSpecificAttributeContent);
	}*/

}