package org.biomart.objects.objects;

import java.io.Serializable;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.objects.types.ElementListType;


public class GroupFilter extends Filter implements Serializable {

	private static final long serialVersionUID = 329028129461650472L;

	public static void main(String[] args) {}

	private ElementList simpleFilterList = null;
	private String logicalOperator = null;
	private String multipleFilter = null;	// 1, N or ALL (TODO create enum?)

	public GroupFilter() {}
	public GroupFilter(PartitionTable mainPartitionTable, String name) {
		super(mainPartitionTable, name);
		
		this.simpleFilterList = new ElementList(ElementListType.FILTER_GROUP);
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
			"simpleFilterList = " + (simpleFilterList!=null ? simpleFilterList.getStringValue() : null);
	}
	
	public org.jdom.Element generateXml() throws FunctionalException {
		org.jdom.Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "logicalOperator", this.logicalOperator);
		MartConfiguratorUtils.addAttribute(element, "multipleFilter", this.multipleFilter);
		MartConfiguratorUtils.addAttribute(element, "filterList", (simpleFilterList!=null ? simpleFilterList.getStringValue() : null));
		return element;
	}
}
