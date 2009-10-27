package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldEmptySpecificElementContent extends OldNode /*implements Comparable<OldEmptySpecificElementContent>, Comparator<OldEmptySpecificElementContent>*/ {

	public static void main(String[] args) {}

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"internalName"
	}));
	
	private String rangeInternalName = null;

	public OldEmptySpecificElementContent(Element jdomElement) throws FunctionalException {
		this(jdomElement, 
				jdomElement.getAttributeValue("internalName")
		);
	}
	
	public OldEmptySpecificElementContent(Element jdomElement, String rangeInternalName) throws FunctionalException {
		super(jdomElement);
		
		this.rangeInternalName = rangeInternalName;
		
		TransformationUtils.checkJdomElementProperties(jdomElement, OldEmptySpecificElementContent.propertyList);
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
			"rangeInternalNamerangeInternalName = " + rangeInternalName;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		OldEmptySpecificElementContent oldEmptySpecificElementContent=(OldEmptySpecificElementContent)object;
		return (
			(this.rangeInternalName==oldEmptySpecificElementContent.rangeInternalName || (this.rangeInternalName!=null && rangeInternalName.equals(oldEmptySpecificElementContent.rangeInternalName)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==rangeInternalName? 0 : rangeInternalName.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldEmptySpecificElementContent oldEmptySpecificElementContent1, OldEmptySpecificElementContent oldEmptySpecificElementContent2) {
		if (oldEmptySpecificElementContent1==null && oldEmptySpecificElementContent2!=null) {
			return -1;
		} else if (oldEmptySpecificElementContent1!=null && oldEmptySpecificElementContent2==null) {
			return 1;
		}
		return CompareUtils.compareNull(oldEmptySpecificElementContent1.internalName, oldEmptySpecificElementContent2.internalName);
	}

	@Override
	public int compareTo(OldEmptySpecificElementContent oldEmptySpecificElementContent) {
		return compare(this, oldEmptySpecificElementContent);
	}*/

}
