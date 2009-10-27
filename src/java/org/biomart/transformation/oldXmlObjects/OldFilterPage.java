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


public class OldFilterPage extends OldFilterContainer /*implements Comparable<OldFilterPage>, Comparator<OldFilterPage>*/ {

	public static void main(String[] args) {}

	private List<OldFilterGroup> oldFilterGroupList = null;

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
	}));
	
	public OldFilterPage(Element jdomElement) throws FunctionalException {
		super(jdomElement, 0);
		
		this.oldFilterGroupList = new ArrayList<OldFilterGroup>();

		TransformationUtils.checkJdomElementProperties(jdomElement, OldContainer.propertyList,
				OldFilterContainer.propertyList, OldFilterPage.propertyList);
	}
	
	public void addOldFilterGroup (OldFilterGroup oldFilterGroup) {
		this.oldFilterGroupList.add(oldFilterGroup);		
	}

	public List<OldFilterGroup> getOldFilterGroupList() {
		return oldFilterGroupList;
	}

	public void setOldFilterGroupList(List<OldFilterGroup> oldFilterGroupList) {
		this.oldFilterGroupList = oldFilterGroupList;
	}

	@Override
	public String toString() {
		int tabLevel = TransformationConstants.ELEMENT_PAGE_TAB_LEVEL;
		StringBuffer sb = new StringBuffer();
		 
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + super.toString() + MyUtils.LINE_SEPARATOR);
		
		sb.append(MartConfiguratorUtils.getTabSpace(tabLevel) + "oldFilterGroupList.size() = " + oldFilterGroupList.size() + MyUtils.LINE_SEPARATOR);	
		for (OldFilterGroup oldFilterGroup : this.oldFilterGroupList) {
			sb.append(MartConfiguratorUtils.getTabSpace(tabLevel+1) + "oldFilterGroup = {" + oldFilterGroup); 
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
		OldFilterPage oldFilterPage=(OldFilterPage)object;
		return (
			(this.oldFilterGroupList==oldFilterPage.oldFilterGroupList || (this.oldFilterGroupList!=null && oldFilterGroupList.equals(oldFilterPage.oldFilterGroupList)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldFilterGroupList? 0 : oldFilterGroupList.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldFilterPage oldFilterPage1, OldFilterPage oldFilterPage2) {
		if (oldFilterPage1==null && oldFilterPage2!=null) {
			return -1;
		} else if (oldFilterPage1!=null && oldFilterPage2==null) {
			return 1;
		}
		return CompareUtils.compareNull(oldFilterPage1.oldFilterGroupList, oldFilterPage2.oldFilterGroupList);
	}

	@Override
	public int compareTo(OldFilterPage oldFilterPage) {
		return compare(this, oldFilterPage);
	}*/
	
	public Container transform(Map<ContainerPath, List<OldElement>> oldFilterDescriptionMap) {
		Container container = super.transform(null, TransformationConstants.PAGE_CONTAINER_LEVEL, null);  // no query restriction on pages, only in attribute collections
		
		// Transform the filter groups
		for (OldFilterGroup oldFilterGroup : this.oldFilterGroupList) {
			Container containerGroup = oldFilterGroup.transform(container, oldFilterDescriptionMap);
			container.addContainer(containerGroup);
		}
		
		return container;
	}
}