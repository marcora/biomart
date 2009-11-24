package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;


public class Container extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 8818099786702183740L;
	
	public static final String XML_ELEMENT_NAME = "container";
//	public static final McNodeType MC_NODE_TYPE = McNodeType.Container;
	
	public static void main(String[] args) {}

	private Integer queryRestriction = null;

	private List<Container> containerList = null;
	private List<Filter> filterList = null;
	private List<Attribute> attributeList = null;
	
	private List<MartConfiguratorObject> containeeList = null;	// Ordered references to above lists of containers, filters & attributes 
	
	public Container() {}
	public Container(String name, String displayName, String description, Boolean visible,
			Integer queryRestriction) {
		super(name, displayName, description, visible, XML_ELEMENT_NAME);
		this.queryRestriction = queryRestriction;
		
		this.containeeList = new ArrayList<MartConfiguratorObject>();
		
		this.containerList = new ArrayList<Container>();
		this.filterList = new ArrayList<Filter>();
		this.attributeList = new ArrayList<Attribute>();
	}
	
	public static Container createRootContainer() {
		Container rootContainer = new Container(
				MartConfiguratorConstants.ROOT_CONTAINER_NAME, MartConfiguratorConstants.ROOT_CONTAINER_DISPLAY_NAME, 
				MartConfiguratorConstants.ROOT_CONTAINER_DISPLAY_NAME, true, null);
		//rootContainer.setParentContainer(null);		// only one with no parents
		return rootContainer;
	}
	
	public void addContainer(Container container) {
		this.containerList.add(container);
		this.containeeList.add(container);
		//container.setParentContainer(this);
	}
	public void addFilter(Filter filter) {
		this.filterList.add(filter);
		this.containeeList.add(filter);
		//filter.setParentContainer(this);
	}
	public void addAttribute(Attribute attribute) {
		this.attributeList.add(attribute);
		this.containeeList.add(attribute);
		//attribute.setParentContainer(this);
	}
	
	public List<MartConfiguratorObject> getContaineeList() {
		return new ArrayList<MartConfiguratorObject>(this.containeeList);
	}
	public List<Container> getContainerList() {
		return new ArrayList<Container>(containerList);
	}
	public List<Filter> getFilterList() {
		return new ArrayList<Filter>(filterList);
	}
	public List<Attribute> getAttributeList() {
		return new ArrayList<Attribute>(attributeList);
	}


	public MartConfiguratorObject getContainee(String name) {
		return super.getMartConfiguratorObjectByName(this.containeeList, name);
	}
	public Container getContainer(String name) {	// TODO potential name conflicts
		return (Container)super.getMartConfiguratorObjectByName(this.containerList, name);
	}
	public Attribute getAttribute(String name) {
		return (Attribute)super.getMartConfiguratorObjectByName(this.attributeList, name);
	}
	public Filter getFilter(String name) {
		return (Filter)super.getMartConfiguratorObjectByName(this.filterList, name);
	}
	public Element getElementRecursively(boolean isAttribute, String name) {
		Element element = null;
		MartConfiguratorObject martConfiguratorObject = super.getMartConfiguratorObjectByName(
				isAttribute ? this.attributeList : this.filterList, name);
		if (martConfiguratorObject==null) {
			for (Container container : this.containerList) {
				if (null!=(martConfiguratorObject = container.getElementRecursively(isAttribute, name))) {
					break;
				}
			}
		} else {
			element = (Element)martConfiguratorObject;
		}
		return element;
	}
	public Attribute getAttributeRecursively(String name) {
		Element element = getElementRecursively(true, name);
		return element!=null ? (Attribute)element : null;
	}
	public Filter getFilterRecursively(String name) {
		Element element = getElementRecursively(false, name);
		return element!=null ? (Filter)element : null;
	}

	public Integer getQueryRestriction() {
		return queryRestriction;
	}

	public void setQueryRestriction(Integer queryRestriction) {
		this.queryRestriction = queryRestriction;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"queryRestriction = " + queryRestriction + ", " +
			"containeeList.size() = " + containeeList.size() + ", " +
			"containerList.size() = " + containerList.size() + ", " +
			"filterList.size() = " + filterList.size() + ", " +
			"attributeList.size() = " + attributeList.size();
	}

	/*@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Container container=(Container)object;
		return (
			(this.level==container.level || (this.level!=null && level.equals(container.level))) &&
			(this.queryRestriction==container.queryRestriction || (this.queryRestriction!=null && queryRestriction.equals(container.queryRestriction))) &&
			(this.containerList==container.containerList || (this.containerList!=null && containerList.equals(container.containerList))) &&
			(this.filterList==container.filterList || (this.filterList!=null && filterList.equals(container.filterList))) &&
			(this.attributeList==container.attributeList || (this.attributeList!=null && attributeList.equals(container.attributeList)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==level? 0 : level.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==queryRestriction? 0 : queryRestriction.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==containerList? 0 : containerList.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==filterList? 0 : filterList.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==attributeList? 0 : attributeList.hashCode());
		return hash;
	}*/

	public org.jdom.Element generateXml() throws FunctionalException  {
		org.jdom.Element element = super.generateXml();
		
		MartConfiguratorUtils.addAttribute(element, "queryRestriction", this.queryRestriction);
		
		for (MartConfiguratorObject containee : this.containeeList) {
			element.addContent(containee.generateXml());
		}
		
		return element;
	}
}

