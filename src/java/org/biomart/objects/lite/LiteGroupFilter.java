package org.biomart.objects.lite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.objects.GroupFilter;
import org.biomart.objects.objects.Part;

public class LiteGroupFilter extends LiteFilter implements Serializable {

	private static final long serialVersionUID = 9218276344958133747L;

	private String logicalOperator = null;
	private String multipleFilter = null;

	private List<String> simpleFilterList = null;
		
	public LiteGroupFilter(GroupFilter groupFilter, Part part) throws FunctionalException {
		
		super(groupFilter, part);
		
		this.logicalOperator = groupFilter.getLogicalOperator();
		this.multipleFilter = groupFilter.getMultipleFilter();
		
		this.simpleFilterList = new ArrayList<String>();
		List<String> simpleFilterNamesTmp = groupFilter.getElementList().getElementNames();
		for (String filterName : simpleFilterNamesTmp) {
			this.simpleFilterList.add(MartConfiguratorUtils.replacePartitionReferencesByValues(filterName, part));
		}
	}

	
	
	public String getLogicalOperator() {
		return logicalOperator;
	}
	public String getMultipleFilter() {
		return multipleFilter;
	}
	public List<String> getSimpleFilterList() {
		return new ArrayList<String>(simpleFilterList);
	}


	@Override
	public String toString() {
		return 
			super.toString() + ", " +
			"logicalOperator = " + logicalOperator + ", " +
			"multipleFilter = " + multipleFilter + ", " +
			"simpleFilterList = " + simpleFilterList;
	}
	
	protected Jsoml generateExchangeFormat(boolean xml) throws FunctionalException {

		Jsoml jsoml = new Jsoml(xml, this.xmlElementName);
		
		jsoml.setAttribute("name", this.name);
		jsoml.setAttribute("displayName", this.displayName);
		jsoml.setAttribute("description", this.description);
		
		jsoml.setAttribute("default", this.selectedByDefault);
		
		jsoml.setAttribute("qualifier", this.qualifier);
		jsoml.setAttribute("caseSensitive", this.caseSensitive);
		
		jsoml.setAttribute("logicalOperator", this.logicalOperator);
		jsoml.setAttribute("multipleFilter", this.multipleFilter);
		jsoml.setAttribute("filterList", this.simpleFilterList);

		if (this.filterData!=null) {		
			jsoml.addContent(this.filterData.generateExchangeFormat(xml, true));
		}
		
		return jsoml;
	}
}
