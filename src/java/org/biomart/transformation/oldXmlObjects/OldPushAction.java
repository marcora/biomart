package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldPushAction extends OldElementPlaceHolder /*implements Comparable<OldPushAction>, Comparator<OldPushAction>*/ {

	public static void main(String[] args) {}

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"ref", "orderBy", "internalName"
	}));

	private String orderBy = null;
	private String pushActionInternalName = null;
	// ref is kept under OldElementPlaceHolder as internalName
	
	private List<OldOptionValue> oldOptionValueList = null;

	public OldPushAction(Element jdomElement) throws FunctionalException {
		this(jdomElement,
				jdomElement.getAttributeValue("ref"),
				jdomElement.getAttributeValue("orderBy"),
				jdomElement.getAttributeValue("internalName")
		);
	}
	
	private OldPushAction(Element jdomElement, String ref, String orderBy, String pushActionInternalName) throws FunctionalException {
		super(jdomElement, ref);
		
		this.orderBy = orderBy;
		this.pushActionInternalName = pushActionInternalName;
		
		this.oldOptionValueList = new ArrayList<OldOptionValue>();

		TransformationUtils.checkJdomElementProperties(jdomElement, OldElementPlaceHolder.propertyList, OldPushAction.propertyList);
	}
	
	public void addOldOptionValue(OldOptionValue oldOptionValue) {
		this.oldOptionValueList.add(oldOptionValue);
	}

	public String getOrderBy() {
		return orderBy;
	}

	public String getPushActionInternalName() {
		return pushActionInternalName;
	}

	public List<OldOptionValue> getOldOptionValueList() {
		return oldOptionValueList;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public void setPushActionInternalName(String pushActionInternalName) {
		this.pushActionInternalName = pushActionInternalName;
	}

	public void setOldOptionValueList(List<OldOptionValue> oldOptionValueList) {
		this.oldOptionValueList = oldOptionValueList;
	}

	@Override
	public String toString() {
		int tabLevel = TransformationConstants.PUSH_ACTION_TAB_LEVEL1;
		String node = 
			super.toString() + ", " + 
			"orderBy = " + orderBy + ", " +
			"pushActionInternalName = " + pushActionInternalName;
		
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
		OldPushAction oldPushAction=(OldPushAction)object;
		return (
			(this.orderBy==oldPushAction.orderBy || (this.orderBy!=null && orderBy.equals(oldPushAction.orderBy))) &&
			(this.pushActionInternalName==oldPushAction.pushActionInternalName || (this.pushActionInternalName!=null && pushActionInternalName.equals(oldPushAction.pushActionInternalName))) &&
			(this.oldOptionValueList==oldPushAction.oldOptionValueList || (this.oldOptionValueList!=null && oldOptionValueList.equals(oldPushAction.oldOptionValueList)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==orderBy? 0 : orderBy.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==pushActionInternalName? 0 : pushActionInternalName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldOptionValueList? 0 : oldOptionValueList.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldPushAction oldPushAction1, OldPushAction oldPushAction2) {
		if (oldPushAction1==null && oldPushAction2!=null) {
			return -1;
		} else if (oldPushAction1!=null && oldPushAction2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(oldPushAction1.ref, oldPushAction2.ref);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldPushAction1.orderBy, oldPushAction2.orderBy);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldPushAction1.pushActionInternalName, oldPushAction2.pushActionInternalName);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(oldPushAction1.oldOptionValueList, oldPushAction2.oldOptionValueList);
	}

	@Override
	public int compareTo(OldPushAction oldPushAction) {
		return compare(this, oldPushAction);
	}*/

}