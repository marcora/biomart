package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.martRemote.Jsoml;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;


public class Container extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 8818099786702183740L;
	
	public static final String XML_ELEMENT_NAME = "container";
	public static final McNodeType MC_NODE_TYPE = McNodeType.Container;
	
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
	public Container getContainer(String name) {
		return (Container)super.getMartConfiguratorObjectByName(this.containerList, name);
	}
	public Attribute getAttribute(String name) {
		return (Attribute)super.getMartConfiguratorObjectByName(this.attributeList, name);
	}
	public Filter getFilter(String name) {
		return (Filter)super.getMartConfiguratorObjectByName(this.filterList, name);
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

	public Element generateXml() throws FunctionalException  {
		Element element = super.generateXml();
		
		MartConfiguratorUtils.addAttribute(element, "queryRestriction", this.queryRestriction);
		
		for (MartConfiguratorObject containee : this.containeeList) {
			element.addContent(containee.generateXml());
		}
		
		return element;
	}


	// ===================================== Should be a different class ============================================
	
	public Container(Container container, List<Integer> mainRowNumbersWanted) throws FunctionalException {	// creates a light clone (temporary solution)
		this(container.name, container.displayName, container.description, container.visible, container.queryRestriction);
		
		for (MartConfiguratorObject containee : container.containeeList) {
			if (containee instanceof Container) {
				if (containee.getVisible()) {	// Only the visible ones
					Container containerClone = new Container((Container)containee, mainRowNumbersWanted); 
					addContainer(containerClone);	// also handles containeeList
				}
			} else if (containee instanceof org.biomart.objects.objects.Element) {
			
				org.biomart.objects.objects.Element element = (org.biomart.objects.objects.Element)containee;
				if (element instanceof SimpleFilter && ((SimpleFilter)element).partition) {
					SimpleFilter filterPartitionClone = new SimpleFilter((SimpleFilter)element);
					addFilter(filterPartitionClone);
				} else {
					Range targetRange = element.getTargetRange();
					Set<Part> partSet = targetRange.getPartSet();
					for (Part part : partSet) {
						if (part.getVisible()) {	// Only the visible ones
							if (element.getPointer() && null==element.getPointedElement()) continue;	// broken pointers (from transformation for instance)
							int mainRowNumber = part.getMainRowNumber();	// could be taken from part too
							if (mainRowNumbersWanted.contains(mainRowNumber)) {
								if (element.getPointer()) {	// FIXME not adequate if pointers of pointers... -> but will be solved by light objects anyway
									org.biomart.objects.objects.Element pointedElement = element.getPointedElement();
									if (pointedElement instanceof Attribute) {
										Attribute pointedAttributeClone = new Attribute((Attribute)pointedElement, part);
										pointedAttributeClone.updatePointerClone(element);
										if (!this.attributeList.contains(pointedAttributeClone)) {	// No repetitions
											addAttribute(pointedAttributeClone);	// also handles containeeList
										}
									} else if (pointedElement instanceof Filter) {
										Filter pointedFilterClone = null;
										if (pointedElement instanceof SimpleFilter) {
											SimpleFilter pointedSimpleFilterClone = new SimpleFilter((SimpleFilter)pointedElement, part);
											pointedSimpleFilterClone.updatePointerClone(element);
											pointedFilterClone = pointedSimpleFilterClone;
										} else if (pointedElement instanceof GroupFilter) {
											GroupFilter pointedGroupFilterClone = new GroupFilter((GroupFilter)pointedElement, part);
											pointedGroupFilterClone.updatePointerClone(element);
											pointedFilterClone = pointedGroupFilterClone;
										}
										if (!this.filterList.contains(pointedFilterClone)) {	// No repetitions
											addFilter(pointedFilterClone);	// also handles containeeList
										}
									}
								} else {
									if (element instanceof Attribute) {
										Attribute attributeClone = new Attribute((Attribute)element, part);
										if (!this.attributeList.contains(attributeClone)) {	// No repetitions
											addAttribute(attributeClone);	// also handles containeeList
										}
									} else if (element instanceof Filter) {
										Filter filterClone = null;
										if (element instanceof SimpleFilter) {
											filterClone = new SimpleFilter((SimpleFilter)element, part);
										} else if (element instanceof GroupFilter) {
											filterClone = new GroupFilter((GroupFilter)element, part);
										}
										if (!this.filterList.contains(filterClone)) {	// No repetitions
											addFilter(filterClone);	// also handles containeeList
										}
									}
								}
							}
						}	
					}
				}
			} 
		}
	}
	
	public Jsoml generateOutputForWebService(boolean xml) throws FunctionalException {
		Jsoml jsoml = super.generateOutputForWebService(xml);
		
		jsoml.setAttribute("queryRestriction", this.queryRestriction);
		
		for (MartConfiguratorObject containee : this.containeeList) {
			if (containee instanceof Container) {
				Jsoml containeeJsoml = containee.generateOutputForWebService(xml);
				jsoml.addContent(containeeJsoml);
			} else if (containee instanceof org.biomart.objects.objects.Element) {
				org.biomart.objects.objects.Element martConfiguratorElement = (org.biomart.objects.objects.Element)containee;
				Jsoml elementJsonml = martConfiguratorElement.generateOutputForWebService(xml);
				jsoml.addContent(elementJsonml);
			}
		}
		
		return jsoml;
	}
}


/*package org.biomart.objects.objects;

import java.io.Serializable;


public abstract class Containee2 extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = -8763113328755703892L;

	public static void main(String[] args) {}
	
	// Redundant
	protected Integer level = null;
	protected Container parentContainer = null;	// The parent container if any

	public Containee2() {} 	// for Serialization
	public Containee2(String name, String displayName, String description, Boolean visible, String xmlElementName) {
		super(name, displayName, description, visible, xmlElementName);
	}
	
	public void setParentContainer(Container parentContainer) {
		this.level = this.parentContainer==null ? 0 : this.parentContainer.level+1;
		this.parentContainer = parentContainer;
	}
	
	public Container getParentContainer() {
		return parentContainer;
	}

	public Integer getLevel() {
		return level;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"level = " + level + ", " +
			"parentContainer = " + (null==parentContainer ? null : parentContainer.getName());
	}
	
	protected Element generateXml() {
		return super.generateXml();
	}
	


	
	// ===================================== Should be a different class ============================================

	protected Containee2(Containee2 containee) {
		this(containee, null);
	}
	protected Containee2(Containee2 containee, Part part) {	// creates a light clone (temporary solution)
		super(containee, part);
	}
	protected void updatePointerClone(org.biomart.objects.objects.Element pointingElement) {
		super.updatePointerClone(pointingElement);
		this.parentContainer = pointingElement.parentContainer;
	}
	
	protected Jsoml generateOutputForWebService(boolean xml) throws FunctionalException {
		return super.generateOutputForWebService(xml);
	}
}*/

