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


public class OldAttributeCollection extends OldAttributeContainer /*implements Comparable<OldAttributeCollection>, Comparator<OldAttributeCollection>*/ {

	public static void main(String[] args) {}

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"maxSelect"
	}));

	private Integer maxSelect = null;

	public List<OldElement> oldElementDescriptionList = null;
	
	public OldAttributeCollection(Element jdomDatasetConfig) throws FunctionalException {
		this(jdomDatasetConfig, 
				jdomDatasetConfig.getAttributeValue("maxSelect")
		);
	}
	private OldAttributeCollection(Element jdomElement, String maxSelect) throws FunctionalException {
		super(jdomElement, 2);
		
		this.maxSelect = TransformationUtils.getIntegerValueFromString(maxSelect, "maxSelect");
		this.oldElementDescriptionList = new ArrayList<OldElement>();

		TransformationUtils.checkJdomElementProperties(jdomElement, OldContainer.propertyList,
				OldAttributeContainer.propertyList, OldAttributeCollection.propertyList);
	}
	
	public void addOldElementDescription (OldElement oldElementDescription) {
		this.oldElementDescriptionList.add(oldElementDescription);		
	}

	public Integer getMaxSelect() {
		return maxSelect;
	}

	@Override
	public String toString() {
		
		int tabLevel = TransformationConstants.ELEMENT_COLLECTION_TAB_LEVEL;
		StringBuffer sb = new StringBuffer();
		 
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + super.toString() + ", ");
		sb.append("maxSelect = " + maxSelect + MyUtils.LINE_SEPARATOR);
		
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + "oldAttributeDescriptionList.size() = " + oldElementDescriptionList.size() + MyUtils.LINE_SEPARATOR);	
		for (OldElement oldElementDescription : this.oldElementDescriptionList) {
			if (oldElementDescription instanceof OldAttributeDescription) {
				OldAttributeDescription oldAttributeDescription = (OldAttributeDescription)oldElementDescription;
				sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "oldAttributeDescription = {" + oldAttributeDescription); 
				sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "}" + MyUtils.LINE_SEPARATOR);
			} else {
				OldFilterDescription oldFilterDescription = (OldFilterDescription)oldElementDescription;
				sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "oldFilterDescription = {" + oldFilterDescription); 
				sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "}" + MyUtils.LINE_SEPARATOR);
			}
		}
		
		/*sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + "oldFilterDescriptionList.size() = " + oldFilterDescriptionList.size() + MyUtils.LINE_SEPARATOR);	
		for (OldFilterDescription oldFilterDescription : this.oldFilterDescriptionList) {
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "oldFilterDescription = {" + oldFilterDescription); 
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "}" + MyUtils.LINE_SEPARATOR);
		}*/
		
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
		OldAttributeCollection oldAttributeCollection=(OldAttributeCollection)object;
		return (
			(this.maxSelect==oldAttributeCollection.maxSelect || (this.maxSelect!=null && maxSelect.equals(oldAttributeCollection.maxSelect)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==maxSelect? 0 : maxSelect.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldAttributeCollection oldAttributeCollection1, OldAttributeCollection oldAttributeCollection2) {
		if (oldAttributeCollection1==null && oldAttributeCollection2!=null) {
			return -1;
		} else if (oldAttributeCollection1!=null && oldAttributeCollection2==null) {
			return 1;
		}
		return CompareUtils.compareNull(oldAttributeCollection1.oldAttributeDescriptionList, oldAttributeCollection2.oldAttributeDescriptionList);
	}

	@Override
	public int compareTo(OldAttributeCollection oldAttributeCollection) {
		return compare(this, oldAttributeCollection);
	}*/
	
	public Container transform(Container parentPageContainer, Container parentGroupContainer, 
			Map<ContainerPath, List<OldElement>> oldAttributeDescriptionMap) {
		Container container = super.transform(parentGroupContainer, TransformationConstants.COLLECTION_CONTAINER_LEVEL, this.maxSelect);
		
		// Delegate transformation of attribute description to the Transformation classes
		for (OldElement oldElementDescription : this.oldElementDescriptionList) {
			
			List<OldElement> oldAttributeDescriptionListTmp = oldAttributeDescriptionMap.get(container);
			if (null==oldAttributeDescriptionListTmp) {
				oldAttributeDescriptionListTmp = new ArrayList<OldElement>();
			}
			oldAttributeDescriptionListTmp.add(oldElementDescription);
			ContainerPath containerPath = new ContainerPath(parentPageContainer, parentGroupContainer, container);
			oldAttributeDescriptionMap.put(containerPath, oldAttributeDescriptionListTmp);	
		}
		
		return container;
	}
}