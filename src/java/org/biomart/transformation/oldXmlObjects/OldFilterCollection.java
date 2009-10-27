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


public class OldFilterCollection extends OldFilterContainer /*implements Comparable<OldFilterCollection>, Comparator<OldFilterCollection>*/ {

	public static void main(String[] args) {}

	public List<OldElement> oldElementDescriptionList = null;

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
	}));
	
	public OldFilterCollection(Element jdomElement) throws FunctionalException {
		super(jdomElement, 2);
		
		this.oldElementDescriptionList = new ArrayList<OldElement>();
		
		TransformationUtils.checkJdomElementProperties(jdomElement, OldContainer.propertyList,
				OldFilterContainer.propertyList, OldFilterCollection.propertyList);
	}
	
	public void addOldElementDescription (OldElement oldElementDescription) {
		this.oldElementDescriptionList.add(oldElementDescription);		
	}

	public List<OldElement> getOldFilterDescriptionList() {
		return oldElementDescriptionList;
	}

	@Override
	public String toString() {
		
		int tabLevel = TransformationConstants.ELEMENT_COLLECTION_TAB_LEVEL;
		StringBuffer sb = new StringBuffer();
		 
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + super.toString() + MyUtils.LINE_SEPARATOR);
		
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + "OldFilterDescriptionList.size() = " + oldElementDescriptionList.size() + MyUtils.LINE_SEPARATOR);	
		for (OldElement oldElementDescription : this.oldElementDescriptionList) {
			
			if (oldElementDescription instanceof OldAttributeDescription) {
				OldFilterDescription oldFilterDescription = (OldFilterDescription)oldElementDescription;
				sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "oldFilterDescription = {" + oldFilterDescription); 
				sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "}" + MyUtils.LINE_SEPARATOR);
			} else {
				OldAttributeDescription oldAttributeDescription = (OldAttributeDescription)oldElementDescription;
				sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "oldAttributeDescription = {" + oldAttributeDescription); 
				sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "}" + MyUtils.LINE_SEPARATOR);
			}
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
		OldFilterCollection oldFilterCollection=(OldFilterCollection)object;
		return (
			(this.oldElementDescriptionList==oldFilterCollection.oldElementDescriptionList || (this.oldElementDescriptionList!=null && oldElementDescriptionList.equals(oldFilterCollection.oldElementDescriptionList)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldElementDescriptionList? 0 : oldElementDescriptionList.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldFilterCollection oldFilterCollection1, OldFilterCollection oldFilterCollection2) {
		if (oldFilterCollection1==null && oldFilterCollection2!=null) {
			return -1;
		} else if (oldFilterCollection1!=null && oldFilterCollection2==null) {
			return 1;
		}
		return CompareUtils.compareNull(oldFilterCollection1.OldFilterDescriptionList, oldFilterCollection2.OldFilterDescriptionList);
	}

	@Override
	public int compareTo(OldFilterCollection oldFilterCollection) {
		return compare(this, oldFilterCollection);
	}*/
	
	public Container transform(Container parentPageContainer, Container parentGroupContainer, 
			Map<ContainerPath, List<OldElement>> oldFilterDescriptionMap) {
		Container container = super.transform(parentGroupContainer, TransformationConstants.COLLECTION_CONTAINER_LEVEL, null);  // no query restriction on pages, only in attribute collections
		
		// Delegate transformation of attribute description to the Transformation classes
		for (OldElement oldElementDescription : this.oldElementDescriptionList) {
			List<OldElement> oldFilterDescriptionListTmp = oldFilterDescriptionMap.get(container);
			if (null==oldFilterDescriptionListTmp) {
				oldFilterDescriptionListTmp = new ArrayList<OldElement>();
			}
			oldFilterDescriptionListTmp.add(oldElementDescription);
			ContainerPath containerPath = new ContainerPath(parentPageContainer, parentGroupContainer, container);
			oldFilterDescriptionMap.put(containerPath, oldFilterDescriptionListTmp);		
		}
		
		return container;
	}
}