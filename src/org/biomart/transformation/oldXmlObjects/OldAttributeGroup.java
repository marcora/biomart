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


public class OldAttributeGroup extends OldAttributeContainer /*implements Comparable<OldAttributeGroup>, Comparator<OldAttributeGroup>*/ {

	public static void main(String[] args) {}

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
	}));

	public List<OldAttributeCollection> oldAttributeCollectionList = null;
	
	public OldAttributeGroup(Element jdomElement) throws FunctionalException {
		super(jdomElement, 1);
		
		this.oldAttributeCollectionList = new ArrayList<OldAttributeCollection>();

		TransformationUtils.checkJdomElementProperties(jdomElement, OldContainer.propertyList,
				OldAttributeContainer.propertyList, OldAttributeGroup.propertyList);
	}
	
	public void addOldAttributeCollection (OldAttributeCollection oldAttributeCollection) {
		this.oldAttributeCollectionList.add(oldAttributeCollection);		
	}

	public List<OldAttributeCollection> getOldAttributeCollectionList() {
		return oldAttributeCollectionList;
	}

	public void setOldAttributeCollectionList(List<OldAttributeCollection> oldAttributeCollectionList) {
		this.oldAttributeCollectionList = oldAttributeCollectionList;
	}

	@Override
	public String toString() {
		int tabLevel = TransformationConstants.ELEMENT_GROUP_TAB_LEVEL;
		StringBuffer sb = new StringBuffer();
		 
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + super.toString() + MyUtils.LINE_SEPARATOR);
		
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + "oldAttributeCollectionList.size() = " + oldAttributeCollectionList.size() + MyUtils.LINE_SEPARATOR);	
		for (OldAttributeCollection oldAttributeCollection : this.oldAttributeCollectionList) {
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "oldAttributeCollection = {" + oldAttributeCollection); 
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
		OldAttributeGroup oldAttributeGroup=(OldAttributeGroup)object;
		return (
			(this.oldAttributeCollectionList==oldAttributeGroup.oldAttributeCollectionList || (this.oldAttributeCollectionList!=null && oldAttributeCollectionList.equals(oldAttributeGroup.oldAttributeCollectionList)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldAttributeCollectionList? 0 : oldAttributeCollectionList.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldAttributeGroup oldAttributeGroup1, OldAttributeGroup oldAttributeGroup2) {
		if (oldAttributeGroup1==null && oldAttributeGroup2!=null) {
			return -1;
		} else if (oldAttributeGroup1!=null && oldAttributeGroup2==null) {
			return 1;
		}
		return CompareUtils.compareNull(oldAttributeGroup1.oldAttributeCollectionList, oldAttributeGroup2.oldAttributeCollectionList);
	}

	@Override
	public int compareTo(OldAttributeGroup oldAttributeGroup) {
		return compare(this, oldAttributeGroup);
	}*/
	
	public Container transform(Container parentPageContainer, Map<ContainerPath, List<OldElement>> oldAttributeDescriptionMap) {
		Container container = super.transform(TransformationConstants.GROUP_CONTAINER_LEVEL, null);  // no query restriction on pages, only in attribute collections
		
		// Transform the attribute collections
		for (OldAttributeCollection oldAttributeCollection : this.oldAttributeCollectionList) {
			Container containerCollection = oldAttributeCollection.transform(parentPageContainer, container, oldAttributeDescriptionMap);
			container.addContainer(containerCollection);
		}
		
		return container;
	}
}