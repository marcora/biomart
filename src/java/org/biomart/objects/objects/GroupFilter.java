package org.biomart.objects.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Namespace;


public class GroupFilter extends Filter implements Serializable/*implements Comparable<GroupFilter>, Comparator<GroupFilter> */{

	private static final long serialVersionUID = 329028129461650472L;

	public static void main(String[] args) {}

	private List<SimpleFilter> filterList = null;
	private String logicalOperator = null;
	//private Boolean shareValue = null;
	private String multipleFilter = null;	// 1, N or ALL (TODO create enum?)
	
	// For internal use only
	private List<String> filterNamesList = null;

	public List<String> getFilterNamesList() {
		return filterNamesList;
	}

	public GroupFilter(Container parentContainer, PartitionTable mainPartitionTable, String name) {
		super(parentContainer, mainPartitionTable, name);
		this.filterList = new ArrayList<SimpleFilter>();
		this.filterNamesList = new ArrayList<String>();
		
	}
	
	public void addSimpleFilter(SimpleFilter simpleFilter) {
		this.filterList.add(simpleFilter);
		this.filterNamesList.add(simpleFilter.getName());
	}

	public List<SimpleFilter> getFilterList() {
		return filterList;
	}

	public void setFilterList(List<SimpleFilter> filterList) {
		this.filterList = filterList;
		for (SimpleFilter simpleFilter : filterList) {
			this.filterNamesList.add(simpleFilter.getName());
		}
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"filterNamesList = " + filterNamesList;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		GroupFilter groupFilter=(GroupFilter)object;
		return (
		(super.equals(groupFilter))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		return hash;
	}

	public String getLogicalOperator() {
		return logicalOperator;
	}

	public void setLogicalOperator(String logicalOperator) {
		this.logicalOperator = logicalOperator;
	}

	/*public Boolean getShareValue() {
		return shareValue;
	}

	public void setShareValue(Boolean shareValue) {
		this.shareValue = shareValue;
	}*/

	public String getMultipleFilter() {
		return multipleFilter;
	}

	public void setMultipleFilter(String multipleFilter) {
		this.multipleFilter = multipleFilter;
	}

	/*@Override
	public int compare(GroupFilter groupFilter1, GroupFilter groupFilter2) {
		if (groupFilter1==null && groupFilter2!=null) {
			return -1;
		} else if (groupFilter1!=null && groupFilter2==null) {
			return 1;
		}
		return CompareUtils.compareNull(groupFilter1.filterList, groupFilter2.filterList);
	}

	@Override
	public int compareTo(GroupFilter groupFilter) {
		return compare(this, groupFilter);
	}*/
	
	public org.jdom.Element generateXml() {
		org.jdom.Element element = super.generateXml();
		if (!this.pointer) {
			MartConfiguratorUtils.addAttribute(element, "logicalOperator", this.logicalOperator);
			MartConfiguratorUtils.addAttribute(element, "multipleFilter", this.multipleFilter);
			MartConfiguratorUtils.addAttribute(element, "filterList", this.filterNamesList);
		}
		return element;
	}
	public org.jdom.Element generateXmlForWebService() {
		return generateXmlForWebService(null);
	}
	public org.jdom.Element generateXmlForWebService(Namespace namespace) {
		org.jdom.Element jdomObject = super.generateXmlForWebService(namespace);
		if (!this.pointer) {
			MartConfiguratorUtils.addAttribute(jdomObject, "logicalOperator", this.logicalOperator);
			MartConfiguratorUtils.addAttribute(jdomObject, "multipleFilter", this.multipleFilter);
			MartConfiguratorUtils.addAttribute(jdomObject, "filterList", this.filterNamesList);
		}
		return jdomObject;
	}
	public JSONObject generateJsonForWebService() {
		JSONObject jsonObject = super.generateJsonForWebService();
		JSONObject object = (JSONObject)jsonObject.get(super.xmlElementName);
		if (!this.pointer) {
			object.put("logicalOperator", this.logicalOperator);
			object.put("multipleFilter", this.multipleFilter);
			object.put("filterList", this.filterList);
		}
		
		jsonObject.put(super.xmlElementName, object);
		return jsonObject;
	}
}
