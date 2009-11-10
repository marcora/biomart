package org.biomart.objects.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Namespace;


public class GroupFilter extends Filter implements Serializable {

	private static final long serialVersionUID = 329028129461650472L;

	public static void main(String[] args) {}

	private List<SimpleFilter> filterList = null;
	private String logicalOperator = null;
	private String multipleFilter = null;	// 1, N or ALL (TODO create enum?)
	
	// For internal use only
	private List<String> filterNamesList = null;

	public GroupFilter() {}
	public GroupFilter(Container parentContainer, PartitionTable mainPartitionTable, String name) {
		super(parentContainer, mainPartitionTable, name);
		this.filterList = new ArrayList<SimpleFilter>();
		this.filterNamesList = new ArrayList<String>();
	}
	
	public void addSimpleFilter(SimpleFilter simpleFilter) {
		this.filterList.add(simpleFilter);
		this.filterNamesList.add(simpleFilter.getName());
	}

	public List<String> getFilterNamesList() {
		return filterNamesList;
	}

	public List<SimpleFilter> getFilterList() {
		return filterList;
	}

	public String getLogicalOperator() {
		return logicalOperator;
	}

	public void setLogicalOperator(String logicalOperator) {
		this.logicalOperator = logicalOperator;
	}

	public String getMultipleFilter() {
		return multipleFilter;
	}

	public void setMultipleFilter(String multipleFilter) {
		this.multipleFilter = multipleFilter;
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
			"logicalOperator = " + logicalOperator + ", " + 
			"multipleFilter = " + multipleFilter + ", " + 
			"filterNamesList = " + filterNamesList;
	}
	
	/*@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		GroupFilter groupFilter=(GroupFilter)object;
		return (
				(super.equals(groupFilter)) &&
				(this.logicalOperator==groupFilter.logicalOperator || (this.logicalOperator!=null && logicalOperator.equals(groupFilter.logicalOperator))) &&
				(this.multipleFilter==groupFilter.multipleFilter || (this.multipleFilter!=null && multipleFilter.equals(groupFilter.multipleFilter))) &&
				(this.filterNamesList==groupFilter.filterNamesList || (this.filterNamesList!=null && filterNamesList.equals(groupFilter.filterNamesList)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==logicalOperator? 0 : logicalOperator.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==multipleFilter? 0 : multipleFilter.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==filterNamesList? 0 : filterNamesList.hashCode());
		return hash;
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
	
	
	
	
	
	
	
	
	// ===================================== Should be a different class ============================================

	public GroupFilter(GroupFilter groupFilter, Part part) throws FunctionalException {	// creates a light clone (temporary solution)
		super(groupFilter, part);
		this.logicalOperator = groupFilter.logicalOperator;
		this.multipleFilter = groupFilter.multipleFilter;
		
		this.filterNamesList = new ArrayList<String>();
		for (String filterName : groupFilter.filterNamesList) {
			this.filterNamesList.add(MartConfiguratorUtils.replacePartitionReferencesByValues(filterName, part));
		}
	}

	public Jsoml generateOutputForWebService(boolean xml) throws FunctionalException {
		Jsoml jsoml = super.generateOutputForWebService(xml);
		
		jsoml.setAttribute("logicalOperator", this.logicalOperator);
		jsoml.setAttribute("multipleFilter", this.multipleFilter);
		jsoml.setAttribute("filterList", this.filterNamesList);
		
		return jsoml;
	}
	public org.jdom.Element generateXmlForWebService() throws FunctionalException {
		return generateXmlForWebService(null);
	}
	public org.jdom.Element generateXmlForWebService(Namespace namespace) throws FunctionalException {
		org.jdom.Element jdomObject = super.generateXmlForWebService(namespace);
		
		MartConfiguratorUtils.addAttribute(jdomObject, "logicalOperator", this.logicalOperator);
		MartConfiguratorUtils.addAttribute(jdomObject, "multipleFilter", this.multipleFilter);
		MartConfiguratorUtils.addAttribute(jdomObject, "filterList", this.filterNamesList);
		
		return jdomObject;
	}
	public JSONObject generateJsonForWebService() {
		JSONObject jsonObject = super.generateJsonForWebService();
		JSONObject object = (JSONObject)jsonObject.get(super.xmlElementName);
		object.put("logicalOperator", this.logicalOperator);
		object.put("multipleFilter", this.multipleFilter);
		object.put("filterList", MartConfiguratorUtils.collectionToCommaSeparatedString(this.filterList));
		
		jsonObject.put(super.xmlElementName, object);
		return jsonObject;
	}
}
