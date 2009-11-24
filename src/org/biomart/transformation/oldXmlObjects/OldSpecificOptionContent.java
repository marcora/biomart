package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldSpecificOptionContent extends OldFilter /*implements Comparable<OldSpecificOptionContent>, Comparator<OldSpecificOptionContent>*/ {

	public static void main(String[] args) {}

	private String rangeInternalName = null;
	
	private List<OldOptionValue> oldOptionValueList = null;

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"internalName"
	}));

	public OldSpecificOptionContent(Element jdomElement) throws FunctionalException {
		this(jdomElement,
				jdomElement.getAttributeValue("internalName")
		);
	}
	
	private OldSpecificOptionContent(Element jdomElement, String rangeInternalName) throws FunctionalException {
		super(jdomElement);
		
		this.internalName = null;	// Erase internalName as it's erroneous
		this.rangeInternalName = rangeInternalName;
		
		this.oldOptionValueList = new ArrayList<OldOptionValue>();
		
		TransformationUtils.checkJdomElementProperties(jdomElement, OldElementPlaceHolder.propertyList,
				OldElement.propertyList, OldFilter.propertyList, OldSpecificOptionContent.propertyList);
	}
	
	public void addOldOptionValue(OldOptionValue oldOptionValue) {
		this.oldOptionValueList.add(oldOptionValue);
	}

	public List<OldOptionValue> getOldOptionValueList() {
		return oldOptionValueList;
	}

	public void setOldOptionValueList(List<OldOptionValue> oldOptionValueList) {
		this.oldOptionValueList = oldOptionValueList;
	}

	public String getRangeInternalName() {
		return rangeInternalName;
	}

	public void setRangeInternalName(String rangeInternalName) {
		this.rangeInternalName = rangeInternalName;
	}

	@Override
	public String toString() {
		int tabLevel = TransformationConstants.SPECIFIC_OPTION_CONTENT_TAB_LEVEL;
		String node = 
			super.toString() + ", " + 
			"rangeInternalName = " + rangeInternalName + ", " +
			"oldOptionValueList = " + oldOptionValueList;
		
		StringBuffer sb = TransformationUtils.displayNode(tabLevel, node);
		TransformationUtils.displayChildren(this.oldOptionValueList, tabLevel, sb, "oldOptionValueList");
		
		return sb.toString();
		
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		OldSpecificOptionContent oldSpecificOptionContent=(OldSpecificOptionContent)object;
		return (
			(this.rangeInternalName==oldSpecificOptionContent.rangeInternalName || (this.rangeInternalName!=null && rangeInternalName.equals(oldSpecificOptionContent.rangeInternalName))) &&
			(this.oldOptionValueList==oldSpecificOptionContent.oldOptionValueList || (this.oldOptionValueList!=null && oldOptionValueList.equals(oldSpecificOptionContent.oldOptionValueList)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==rangeInternalName? 0 : rangeInternalName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldOptionValueList? 0 : oldOptionValueList.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldSpecificOptionContent oldSpecificOptionContent1, OldSpecificOptionContent oldSpecificOptionContent2) {
		if (oldSpecificOptionContent1==null && oldSpecificOptionContent2!=null) {
			return -1;
		} else if (oldSpecificOptionContent1!=null && oldSpecificOptionContent2==null) {
			return 1;
		}
		return CompareUtils.compareNull(oldSpecificOptionContent1.oldOptionValueList, oldSpecificOptionContent2.oldOptionValueList);
	}

	@Override
	public int compareTo(OldSpecificOptionContent oldSpecificOptionContent) {
		return compare(this, oldSpecificOptionContent);
	}*/

}