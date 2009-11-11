package org.biomart.objects.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.objects.MartConfiguratorUtils;


public class GroupFilter extends Filter implements Serializable {

	private static final long serialVersionUID = 329028129461650472L;

	public static void main(String[] args) {}

	private ElementList simpleFilterList = null;
	private String logicalOperator = null;
	private String multipleFilter = null;	// 1, N or ALL (TODO create enum?)

	public GroupFilter() {}
	public GroupFilter(PartitionTable mainPartitionTable, String name) {
		super(mainPartitionTable, name);
		
		this.simpleFilterList = new ElementList();
	}
	
	public ElementList getElementList() {
		return this.simpleFilterList;
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

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"logicalOperator = " + logicalOperator + ", " + 
			"multipleFilter = " + multipleFilter + ", " + 
			"simpleFilterList = " + (simpleFilterList!=null ? simpleFilterList.getXmlValue() : null);
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
		MartConfiguratorUtils.addAttribute(element, "logicalOperator", this.logicalOperator);
		MartConfiguratorUtils.addAttribute(element, "multipleFilter", this.multipleFilter);
		MartConfiguratorUtils.addAttribute(element, "filterList", (simpleFilterList!=null ? simpleFilterList.getXmlValue() : null));
		return element;
	}
	
	
	
	
	
	
	
	
	// ===================================== Should be a different class ============================================

	private List<String> simpleFilterNames = null;
	public GroupFilter(GroupFilter groupFilter, Part part) throws FunctionalException {	// creates a light clone (temporary solution)
		super(groupFilter, part);
		this.logicalOperator = groupFilter.logicalOperator;
		this.multipleFilter = groupFilter.multipleFilter;
		
		this.simpleFilterNames = new ArrayList<String>();
		List<String> simpleFilterNamesTmp = groupFilter.getElementList().getElementNames();
		for (String filterName : simpleFilterNamesTmp) {
			this.simpleFilterNames.add(MartConfiguratorUtils.replacePartitionReferencesByValues(filterName, part));
		}
	}

	public Jsoml generateOutputForWebService(boolean xml) throws FunctionalException {
		Jsoml jsoml = super.generateOutputForWebService(xml);
		
		jsoml.setAttribute("logicalOperator", this.logicalOperator);
		jsoml.setAttribute("multipleFilter", this.multipleFilter);
		jsoml.setAttribute("filterList", (simpleFilterList!=null ? simpleFilterList.getXmlValue() : null));
		
		return jsoml;
	}
}
