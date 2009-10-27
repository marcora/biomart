package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldOptionFilter extends OldFilter /*implements Comparable<OldOptionFilter>, Comparator<OldOptionFilter>*/ {

	public static void main(String[] args) {}

	private List<OldSpecificOptionContent> oldSpecificOptionContentList = null;
	private List<OldOptionValue> oldOptionValueList = null;

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"setAttribute",		// in "wormbase_gene", <option> "gene"
			"value"	// in "wormbase_vartion", <option> "laboratory"
	}));

	public OldOptionFilter(Element jdomElement) throws FunctionalException {
		super(jdomElement);

		this.oldSpecificOptionContentList = new ArrayList<OldSpecificOptionContent>();
		this.oldOptionValueList = new ArrayList<OldOptionValue>();

		TransformationUtils.checkJdomElementProperties(jdomElement, OldElementPlaceHolder.propertyList,
				OldElement.propertyList, OldFilter.propertyList, OldOptionFilter.propertyList);
	}
	
	public void addOldSpecificOptionContent(OldSpecificOptionContent oldSpecificOptionContent) {
		this.oldSpecificOptionContentList.add(oldSpecificOptionContent);
	}
	
	public void addOldOptionValue(OldOptionValue oldOptionValue) {
		this.oldOptionValueList.add(oldOptionValue);
	}

	public List<OldSpecificOptionContent> getOldSpecificOptionContentList() {
		return oldSpecificOptionContentList;
	}

	public void setOldSpecificOptionContentList(List<OldSpecificOptionContent> oldSpecificOptionContentList) {
		this.oldSpecificOptionContentList = oldSpecificOptionContentList;
	}

	public List<OldOptionValue> getOldOptionValueList() {
		return oldOptionValueList;
	}

	public void setOldOptionValueList(List<OldOptionValue> oldOptionValueList) {
		this.oldOptionValueList = oldOptionValueList;
	}

	@Override
	public String toString() {
		int tabLevel = TransformationConstants.OPTION_FILTER_TAB_LEVEL1;
		String node = 
			super.toString();
		
		StringBuffer sb = TransformationUtils.displayNode(tabLevel, node);
		TransformationUtils.displayChildren(this.oldSpecificOptionContentList, tabLevel, sb, "oldSpecificOptionContentList");
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
		OldOptionFilter oldOptionFilter=(OldOptionFilter)object;
		return (
			(this.oldSpecificOptionContentList==oldOptionFilter.oldSpecificOptionContentList || (this.oldSpecificOptionContentList!=null && oldSpecificOptionContentList.equals(oldOptionFilter.oldSpecificOptionContentList))) &&
			(this.oldOptionValueList==oldOptionFilter.oldOptionValueList || (this.oldOptionValueList!=null && oldOptionValueList.equals(oldOptionFilter.oldOptionValueList)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldSpecificOptionContentList? 0 : oldSpecificOptionContentList.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldOptionValueList? 0 : oldOptionValueList.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldOptionFilter oldOptionFilter1, OldOptionFilter oldOptionFilter2) {
		if (oldOptionFilter1==null && oldOptionFilter2!=null) {
			return -1;
		} else if (oldOptionFilter1!=null && oldOptionFilter2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(oldOptionFilter1.oldSpecificOptionContentList, oldOptionFilter2.oldSpecificOptionContentList);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(oldOptionFilter1.oldOptionValueList, oldOptionFilter2.oldOptionValueList);
	}

	@Override
	public int compareTo(OldOptionFilter oldOptionFilter) {
		return compare(this, oldOptionFilter);
	}*/

}