package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldOptionValue extends OldNode /*implements Comparable<OldOptionValue>, Comparator<OldOptionValue> */{

	public static void main(String[] args) {}

	private String internalName = null;
	private String displayName = null;
	private Boolean isSelectable = null;
	private String value = null;
	
	private List<OldPushAction> oldPushActionList = null;
	private List<OldOptionValue> oldOptionValueList = null;
	
	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"internalName", "displayName", "isSelectable", "value", "hidden",
			"multipleValues", "graph", "autoCompletion", "style", "displayType",	// for "est" dataset, <option> "aggregation_stage"
			"tableConstraint", "key",// for value "1" in "gene" dataset
			"type", "qualifier", "legal_qualifiers", "hideDisplay",	// for value "IEA" in "gene"
			"description",	// for value "4" in "dssweetv01" dataset
			"field", // for value "icl_proteome" in "macronuclear"
			"defaultOn", // "Approved" in "hgnc" dataset
			"defaultValue", // "primary" in "hgnc" dataset
	}));

	public OldOptionValue(Element jdomElement) throws FunctionalException {
		this(jdomElement, 
				jdomElement.getAttributeValue("internalName"),
				jdomElement.getAttributeValue("displayName"),
				jdomElement.getAttributeValue("isSelectable"),
				jdomElement.getAttributeValue("value")
		);
	}
	
	public OldOptionValue(Element jdomElement, String internalName, String displayName, String isSelectable, String value) throws FunctionalException {
		super(jdomElement);
		
		this.internalName = internalName;
		this.displayName = displayName;
		this.isSelectable = TransformationUtils.getBooleanValueFromString(isSelectable, "isSelectable");
		this.value = value;
		
		this.oldPushActionList = new ArrayList<OldPushAction>();
		this.oldOptionValueList = new ArrayList<OldOptionValue>();
		
		TransformationUtils.checkJdomElementProperties(jdomElement, OldOptionValue.propertyList);
	}

	public void addOldOptionValue(OldOptionValue oldOptionValue) {
		this.oldOptionValueList.add(oldOptionValue);
	}
	
	public void addOldPushAction(OldPushAction oldPushAction) {
		this.oldPushActionList.add(oldPushAction);
	}

	public List<OldPushAction> getOldPushActionList() {
		return oldPushActionList;
	}

	public void setOldPushActionList(List<OldPushAction> oldPushActionList) {
		this.oldPushActionList = oldPushActionList;
	}

	public List<OldOptionValue> getOldOptionValueList() {
		return oldOptionValueList;
	}

	public void setOldOptionValueList(List<OldOptionValue> oldOptionValueList) {
		this.oldOptionValueList = oldOptionValueList;
	}

	@Override
	public String toString() {
		int tabLevel = TransformationConstants.OPTION_VALUE_TAB_LEVEL1;
		String node = 
			super.toString() + ", " + 
			"internalName = " + internalName + ", " +
			"displayName = " + displayName + ", " +
			"isSelectable = " + isSelectable + ", " +
			"value = " + value; 
		
		StringBuffer sb = TransformationUtils.displayNode(tabLevel, node);
		TransformationUtils.displayChildren(this.oldPushActionList, tabLevel, sb, "oldPushActionList");
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
		OldOptionValue oldOptionValue=(OldOptionValue)object;
		return (
			(this.oldPushActionList==oldOptionValue.oldPushActionList || (this.oldPushActionList!=null && oldPushActionList.equals(oldOptionValue.oldPushActionList))) &&
			(this.oldOptionValueList==oldOptionValue.oldOptionValueList || (this.oldOptionValueList!=null && oldOptionValueList.equals(oldOptionValue.oldOptionValueList)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldPushActionList? 0 : oldPushActionList.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldOptionValueList? 0 : oldOptionValueList.hashCode());
		return hash;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getInternalName() {
		return internalName;
	}

	public Boolean getIsSelectable() {
		return isSelectable;
	}

	public String getValue() {
		return value;
	}

	/*@Override
	public int compare(OldOptionValue oldOptionValue1, OldOptionValue oldOptionValue2) {
		if (oldOptionValue1==null && oldOptionValue2!=null) {
			return -1;
		} else if (oldOptionValue1!=null && oldOptionValue2==null) {
			return 1;
		}
		return CompareUtils.compareNull(oldOptionValue1.oldPushActionList, oldOptionValue2.oldPushActionList);
	}

	@Override
	public int compareTo(OldOptionValue oldOptionValue) {
		return compare(this, oldOptionValue);
	}*/

}