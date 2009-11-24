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


public class OldFilterGroup extends OldFilterContainer /*implements Comparable<OldFilterGroup>, Comparator<OldFilterGroup>*/ {

	public static void main(String[] args) {}

	public List<OldFilterCollection> oldFilterCollectionList = null;

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
	}));
	
	public OldFilterGroup(Element jdomElement) throws FunctionalException {
		super(jdomElement, 1);
		
		this.oldFilterCollectionList = new ArrayList<OldFilterCollection>();
		
		TransformationUtils.checkJdomElementProperties(jdomElement, OldContainer.propertyList,
				OldFilterContainer.propertyList, OldFilterGroup.propertyList);
	}
	
	public void addOldFilterCollection (OldFilterCollection oldFilterCollection) {
		this.oldFilterCollectionList.add(oldFilterCollection);		
	}

	public List<OldFilterCollection> getOldFilterCollectionList() {
		return oldFilterCollectionList;
	}

	public void setOldFilterCollectionList(List<OldFilterCollection> oldFilterCollectionList) {
		this.oldFilterCollectionList = oldFilterCollectionList;
	}

	@Override
	public String toString() {
		int tabLevel = TransformationConstants.ELEMENT_GROUP_TAB_LEVEL;
		StringBuffer sb = new StringBuffer();
		 
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + super.toString() + MyUtils.LINE_SEPARATOR);
		
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + "oldFilterCollectionList.size() = " + oldFilterCollectionList.size() + MyUtils.LINE_SEPARATOR);	
		for (OldFilterCollection oldFilterCollection : this.oldFilterCollectionList) {
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "oldFilterCollection = {" + oldFilterCollection); 
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
		OldFilterGroup oldFilterGroup=(OldFilterGroup)object;
		return (
			(this.oldFilterCollectionList==oldFilterGroup.oldFilterCollectionList || (this.oldFilterCollectionList!=null && oldFilterCollectionList.equals(oldFilterGroup.oldFilterCollectionList)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldFilterCollectionList? 0 : oldFilterCollectionList.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldFilterGroup oldFilterGroup1, OldFilterGroup oldFilterGroup2) {
		if (oldFilterGroup1==null && oldFilterGroup2!=null) {
			return -1;
		} else if (oldFilterGroup1!=null && oldFilterGroup2==null) {
			return 1;
		}
		return CompareUtils.compareNull(oldFilterGroup1.oldFilterCollectionList, oldFilterGroup2.oldFilterCollectionList);
	}

	@Override
	public int compareTo(OldFilterGroup oldFilterGroup) {
		return compare(this, oldFilterGroup);
	}*/
	
	public Container transform(Container parentPageContainer, Map<ContainerPath, List<OldElement>> oldFilterDescriptionMap) {
		Container container = super.transform(TransformationConstants.GROUP_CONTAINER_LEVEL, null);  // no query restriction on pages, only in attribute collections
		
		// Transform the filter collections
		for (OldFilterCollection oldFilterCollection : this.oldFilterCollectionList) {
			Container containerCollection = oldFilterCollection.transform(parentPageContainer, container, oldFilterDescriptionMap);
			container.addContainer(containerCollection);
		}
		
		return container;
	}
}