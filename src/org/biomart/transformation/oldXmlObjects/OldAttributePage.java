package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.objects.Container;
import org.biomart.transformation.helpers.ContainerPath;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldAttributePage extends OldAttributeContainer /*implements Comparable<OldAttributePage>, Comparator<OldAttributePage>*/ {

	public static void main(String[] args) {}

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
	}));
	
	public List<OldAttributeGroup> oldAttributeGroupList = null;

	public OldAttributePage(Element jdomElement) throws FunctionalException {
		super(jdomElement, 0);
		
		this.oldAttributeGroupList = new ArrayList<OldAttributeGroup>();

		TransformationUtils.checkJdomElementProperties(jdomElement, OldContainer.propertyList,
				OldAttributeContainer.propertyList, OldAttributePage.propertyList);
	}
	
	public void addOldAttributeGroup (OldAttributeGroup oldAttributeGroup) {
		this.oldAttributeGroupList.add(oldAttributeGroup);		
	}
		
	public List<OldAttributeGroup> getOldAttributeGroupList() {
		return oldAttributeGroupList;
	}

	public void setOldAttributeGroupList(List<OldAttributeGroup> oldAttributeGroupList) {
		this.oldAttributeGroupList = oldAttributeGroupList;
	}

	@Override
	public String toString() {
		int tabLevel = TransformationConstants.ELEMENT_PAGE_TAB_LEVEL;
		StringBuffer sb = new StringBuffer();
		 
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + super.toString() + MyUtils.LINE_SEPARATOR);
		
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + "oldAttributeGroupList.size() = " + oldAttributeGroupList.size() + MyUtils.LINE_SEPARATOR);	
		for (OldAttributeGroup oldAttributeGroup : this.oldAttributeGroupList) {
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "oldAttributeGroup = {" + oldAttributeGroup); 
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "}" + MyUtils.LINE_SEPARATOR);
		}
		
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
		OldAttributePage oldAttributePage=(OldAttributePage)object;
		return (
			(this.oldAttributeGroupList==oldAttributePage.oldAttributeGroupList || (this.oldAttributeGroupList!=null && oldAttributeGroupList.equals(oldAttributePage.oldAttributeGroupList)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldAttributeGroupList? 0 : oldAttributeGroupList.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldAttributePage oldAttributePage1, OldAttributePage oldAttributePage2) {
		if (oldAttributePage1==null && oldAttributePage2!=null) {
			return -1;
		} else if (oldAttributePage1!=null && oldAttributePage2==null) {
			return 1;
		}
		return CompareUtils.compareNull(oldAttributePage1.oldAttributeGroupList, oldAttributePage2.oldAttributeGroupList);
	}

	@Override
	public int compareTo(OldAttributePage oldAttributePage) {
		return compare(this, oldAttributePage);
	}*/
	
	public Container transform(Container parentContainer, Map<ContainerPath, List<OldElement>> oldAttributeDescriptionMap) {
		Container container = super.transform(TransformationConstants.PAGE_CONTAINER_LEVEL, null);  // no query restriction on pages, only in attribute collections
		
		// Transform the attribute groups
		for (OldAttributeGroup oldAttributeGroup : this.oldAttributeGroupList) {
			Container containerGroup = oldAttributeGroup.transform(container, oldAttributeDescriptionMap);
			container.addContainer(containerGroup);
		}
		
		return container;
	}
}