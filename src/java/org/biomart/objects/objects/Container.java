package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.sf.json.JSONObject;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;
import org.jdom.Namespace;


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
		
		/*for (Attribute attribute : this.attributeList) {
			element.addContent(attribute.generateXml());
		}
		
		for (Filter filter : this.filterList) {
			element.addContent(filter.generateXml());
		}

		for (Container container : this.containerList) {
			element.addContent(container.generateXml());
		}*/
		for (Containee containee : this.containeeList) {
			element.addContent(containee.generateXml());
		}
		
		return element;
	}
	public Element generateXmlForWebService(boolean recursively) {
		return generateXmlForWebService(null, recursively);
	}
	public Element generateXmlForWebService(Namespace namespace, boolean recursively) {
		Element jdomObject = super.generateXmlForWebService(namespace);
		
		MartConfiguratorUtils.addAttribute(jdomObject, "level", this.level);
		MartConfiguratorUtils.addAttribute(jdomObject, "queryRestriction", this.queryRestriction);
		
		if (recursively) {
			for (Containee containee : containeeList) {
				if (containee instanceof Container && containee.getVisible()) {
					Element containeeElement = containee.generateXmlForWebService(namespace, recursively);
					jdomObject.addContent(containeeElement);
				} else if (containee instanceof org.biomart.objects.objects.Element) {
					org.biomart.objects.objects.Element element = (org.biomart.objects.objects.Element)containee;
					Element elementElement = element.generateXmlForWebService(namespace);
					jdomObject.addContent(elementElement);
				}
			}
		}
		
		return jdomObject;
	}
	public JSONObject generateJsonForWebService(boolean recursively) {
		JSONObject jsonObject = super.generateJsonForWebService();
		
		JSONObject object = (JSONObject)jsonObject.get(super.xmlElementName);
		object.put("level", this.level);
		object.put("queryRestriction", this.queryRestriction);
		
		jsonObject.put(super.xmlElementName, object);
		return jsonObject;
	}

}
