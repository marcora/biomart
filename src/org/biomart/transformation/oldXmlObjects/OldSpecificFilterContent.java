package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldSpecificFilterContent extends OldFilter /*implements Comparable<OldSpecificFilterContent>, Comparator<OldSpecificFilterContent> */{

	public static void main(String[] args) {}

	private String rangeInternalName = null;

	private List<OldOptionFilter> oldOptionFilterList = null;
	private List<OldOptionValue> oldOptionValueList = null;
	
	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"internalName"
	}));

	public OldSpecificFilterContent(Element jdomElement) throws FunctionalException {
		this(jdomElement,
				jdomElement.getAttributeValue("internalName")
		);
	}
	
	private OldSpecificFilterContent(Element jdomElement, String rangeInternalName) throws FunctionalException {
		super(jdomElement);
		
		this.internalName = null;	// Erase internalName as it's erroneous
		this.rangeInternalName = rangeInternalName;
		
		this.oldOptionFilterList = new ArrayList<OldOptionFilter>();
		this.oldOptionValueList = new ArrayList<OldOptionValue>();

		TransformationUtils.checkJdomElementProperties(jdomElement, OldElementPlaceHolder.propertyList,
				OldElement.propertyList, OldFilter.propertyList, OldSpecificFilterContent.propertyList);
	}
	
	public void addOldOptionFilter(OldOptionFilter oldOptionFilter) {
		this.oldOptionFilterList.add(oldOptionFilter);
	}
	
	public void addOldOptionValue(OldOptionValue oldOptionValue) {
		this.oldOptionValueList.add(oldOptionValue);
	}

	public String getRangeInternalName() {
		return rangeInternalName;
	}

	public void setRangeInternalName(String rangeInternalName) {
		this.rangeInternalName = rangeInternalName;
	}

	@Override
	public String toString() {
		int tabLevel = TransformationConstants.SPECIFIC_FILTER_CONTENT_TAB_LEVEL;
		String node = 
			super.toString() + ", " + 
			"rangeInternalName = " + rangeInternalName;
		
		StringBuffer sb = TransformationUtils.displayNode(tabLevel, node);
		TransformationUtils.displayChildren(this.oldOptionFilterList, tabLevel, sb, "oldOptionFilterList");
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
		OldSpecificFilterContent oldSpecificFilterContent=(OldSpecificFilterContent)object;
		return (
			(this.rangeInternalName==oldSpecificFilterContent.rangeInternalName || (this.rangeInternalName!=null && rangeInternalName.equals(oldSpecificFilterContent.rangeInternalName))) &&
			(this.oldOptionFilterList==oldSpecificFilterContent.oldOptionFilterList || (this.oldOptionFilterList!=null && oldOptionFilterList.equals(oldSpecificFilterContent.oldOptionFilterList))) &&
			(this.oldOptionValueList==oldSpecificFilterContent.oldOptionValueList || (this.oldOptionValueList!=null && oldOptionValueList.equals(oldSpecificFilterContent.oldOptionValueList)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==rangeInternalName? 0 : rangeInternalName.hashCode());
		return hash;
	}

	public List<OldOptionFilter> getOldOptionFilterList() {
		return oldOptionFilterList;
	}

	public List<OldOptionValue> getOldOptionValueList() {
		return oldOptionValueList;
	}

	/*@Override
	public int compare(OldSpecificFilterContent oldSpecificFilterContent1, OldSpecificFilterContent oldSpecificFilterContent2) {
		if (oldSpecificFilterContent1==null && oldSpecificFilterContent2!=null) {
			return -1;
		} else if (oldSpecificFilterContent1!=null && oldSpecificFilterContent2==null) {
			return 1;
		}
		return CompareUtils.compareNull(oldSpecificFilterContent1.rangeInternalName, oldSpecificFilterContent2.rangeInternalName);
	}

	@Override
	public int compareTo(OldSpecificFilterContent oldSpecificFilterContent) {
		return compare(this, oldSpecificFilterContent);
	}*/
}