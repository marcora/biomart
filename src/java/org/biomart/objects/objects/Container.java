package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.CompareUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;


public class Container extends Containee implements Comparable<Container>, Comparator<Container>, Serializable {

	private static final long serialVersionUID = 8818099786702183740L;
	
	public static final String XML_ELEMENT_NAME = "container";
	
	public static void main(String[] args) {}

	private Integer level = null;
	private Integer queryRestriction = null;

	private List<Container> containerList = null;
	private List<Filter> filterList = null;
	private List<Attribute> attributeList = null;
	
	private List<Containee> containeeList = null;	// Ordered references to above lists of containers, filters & attributes 
	
	public Container() {}
	public Container(Container parentContainer, String name, String displayName, String description, Boolean visible,
			Integer level, Integer queryRestriction) {
		super(name, displayName, description, visible, XML_ELEMENT_NAME, parentContainer);
		this.level = level;
		this.queryRestriction = queryRestriction;
		
		this.containeeList = new ArrayList<Containee>();
		
		this.containerList = new ArrayList<Container>();
		this.filterList = new ArrayList<Filter>();
		this.attributeList = new ArrayList<Attribute>();
	}
	
	public void addContainer(Container container) {
		this.containerList.add(container);
		this.containeeList.add(container);
	}
	
	public void addFilter(Filter filter) {
		this.filterList.add(filter);
		this.containeeList.add(filter);
	}
	
	public void addAttribute(Attribute attribute) {
		this.attributeList.add(attribute);
		this.containeeList.add(attribute);
	}

	public Integer getLevel() {
		return level;
	}

	public List<Containee> getContaineeList() {
		return containeeList;
	}

	public Integer getQueryRestriction() {
		return queryRestriction;
	}

	public List<Container> getContainerList() {
		return containerList;
	}

	public List<Filter> getFilterList() {
		return filterList;
	}

	public List<Attribute> getAttributeList() {
		return attributeList;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public void setQueryRestriction(Integer queryRestriction) {
		this.queryRestriction = queryRestriction;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"level = " + level + ", " +
			"queryRestriction = " + queryRestriction + ", " +
			"containerList = " + containerList + ", " +
			"filterList = " + filterList + ", " +
			"attributeList = " + attributeList;
	}

	@Override
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
	}

	public int compare(Container container1, Container container2) {
		if (container1==null && container2!=null) {
			return -1;
		} else if (container1!=null && container2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(container1.level, container2.level);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(container1.queryRestriction, container2.queryRestriction);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(container1.containerList, container2.containerList);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(container1.filterList, container2.filterList);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(container1.attributeList, container2.attributeList);
	}

	public int compareTo(Container container) {
		return compare(this, container);
	}

	public Element generateXml() {
		Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "level", this.level);
		MartConfiguratorUtils.addAttribute(element, "queryRestriction", this.queryRestriction);
		
		for (Containee containee : this.containeeList) {
			element.addContent(containee.generateXml());
		}
		
		return element;
	}


	// ===================================== Should be a different class ============================================
	
	public Container(Container container, List<Integer> mainRowNumbersWanted) {	// creates a light clone (temporary solution)
		this(
				container.parentContainer!=null ? new Container(null, container.parentContainer.name, null, null, null, null, null) : null,	// just to have the name 
				container.name, container.displayName, container.description, container.visible, 
				container.level, container.queryRestriction);
		
		for (Containee containee : container.containeeList) {
			if (containee instanceof Container) {
				if (containee.getVisible()) {	// Only the visible ones
					Container containerClone = new Container((Container)containee, mainRowNumbersWanted); 
					addContainer(containerClone);	// also handles containeeList
				}
			} else if (containee instanceof org.biomart.objects.objects.Element) {
			
				org.biomart.objects.objects.Element element = (org.biomart.objects.objects.Element)containee;
				if (element instanceof SimpleFilter && ((SimpleFilter)element).partition) {
					SimpleFilter filterPartitionClone = new SimpleFilter((SimpleFilter)element, null);
					addFilter(filterPartitionClone);
				} else {
					Range targetRange = element.getTargetRange();
					Set<Part> partSet = targetRange.getPartSet();
					for (Part part : partSet) {
						if (part.getVisible()) {	// Only the visible ones
							if (element.getPointer() && null==element.getPointedElement()) continue;	// broken pointers (from transformation for instance)
							int mainRowNumber = part.getMainRowNumber();
							if (mainRowNumbersWanted.contains(mainRowNumber)) {
								if (element.getPointer()) {	// FIXME not adequate if pointers of pointers...
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
		
		/*for (Attribute attribute : container.getAttributeList()) {
			Range targetRange = attribute.getTargetRange();
			Set<Part> partSet = targetRange.getPartSet();
			for (Part part : partSet) {
				if (part.getVisible()) {	// Only the visible ones
					if (attribute.getPointer() && null==attribute.getPointedElement()) continue;	// broken pointers (from transformation for instance)
					int mainRowNumber = part.getMainRowNumber();
					if (mainRowNumbersWanted.contains(mainRowNumber)) {
						
						if (attribute.getPointer()) {
							org.biomart.objects.objects.Element pointedElement = attribute.getPointedElement();
							if (pointedElement instanceof Attribute) {
								Attribute pointedAttributeClone = (Attribute)pointedElement.lightClone(part);
							} else if (pointedElement instanceof SimpleFilter) {
								SimpleFilter pointedSimpleFilterClone = (SimpleFilter)pointedElement.lightClone(part);
							} else if (pointedElement instanceof GroupFilter) {
								GroupFilter pointedGroupFilterClone = (GroupFilter)pointedElement.lightClone(part);
							}
						}
						
						Attribute attributeClone = (Attribute)attribute.lightClone(part);
						if (!this.attributeList.contains(attributeClone)) {	// No repetitions
							addAttribute(attributeClone);	// also handles containeeList
						}
					}
				}					
			}
		}
		for (Filter filter : container.getFilterList()) {	
			Range targetRange = filter.getTargetRange();
			Set<Part> partSet = targetRange.getPartSet();
			for (Part part : partSet) {
				if (part.getVisible()) {	// Only the visible ones
					if (filter.getPointer() && null==filter.getPointedElement()) continue;	// broken pointers (from transformation for instance)
					int mainRowNumber = part.getMainRowNumber();
					if (mainRowNumbersWanted.contains(mainRowNumber)) {
						Filter filterClone = (Filter)filter.lightClone(part);
						if (!this.filterList.contains(filterClone)) {	// No repetitions
							addFilter(filterClone);		// also handles containeeList
						}
					}
				}					
			}
		}*/
	}
	
	public Element generateXmlForWebService() throws FunctionalException {
		return generateXmlForWebService(null);
	}
	public Element generateXmlForWebService(org.jdom.Namespace namespace) throws FunctionalException {
		Element jdomObject = super.generateXmlForWebService(namespace);
		
		MartConfiguratorUtils.addAttribute(jdomObject, "level", this.level);
		MartConfiguratorUtils.addAttribute(jdomObject, "queryRestriction", this.queryRestriction);
		
		for (Containee containee : containeeList) {
			if (containee instanceof Container) {
				Element containeeElement = containee.generateXmlForWebService(namespace);
				jdomObject.addContent(containeeElement);
			} else if (containee instanceof org.biomart.objects.objects.Element) {
				org.biomart.objects.objects.Element martConfiguratorElement = (org.biomart.objects.objects.Element)containee;
				Element elementElement = martConfiguratorElement.generateXmlForWebService(namespace);
				jdomObject.addContent(elementElement);
			}
		}
		
		return jdomObject;
	}
	public JSONObject generateJsonForWebService() {
		JSONObject jsonObject = super.generateJsonForWebService();
		
		JSONObject object = (JSONObject)jsonObject.get(super.xmlElementName);
		object.put("level", this.level);
		object.put("queryRestriction", this.queryRestriction);
		
		JSONArray array = new JSONArray();
		boolean atLeastOne = false;
		for (Containee containee : containeeList) {
			if (containee instanceof Container) {
				JSONObject containeeJSONObject = containee.generateJsonForWebService();
				array.add(containeeJSONObject);
				atLeastOne = true;
			} else if (containee instanceof org.biomart.objects.objects.Element) {
				org.biomart.objects.objects.Element martConfiguratorElement = (org.biomart.objects.objects.Element)containee;
				JSONObject elementJSONObject = martConfiguratorElement.generateJsonForWebService();
				array.add(elementJSONObject);
				atLeastOne = true;
			}
		}
		if (atLeastOne) {
			object.put("containees", array);
		}
		
		jsonObject.put(super.xmlElementName, object);
		return jsonObject;
	}
}
