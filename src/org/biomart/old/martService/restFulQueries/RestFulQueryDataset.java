package org.biomart.old.martService.restFulQueries;


import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.old.martService.restFulQueries.objects.Attribute;
import org.biomart.old.martService.restFulQueries.objects.Filter;


public class RestFulQueryDataset {

	String datasetName = null;
	protected List<Attribute> attributesList = null;
	protected List<Filter> filtersList = null;
	protected List<String> attributeNamesList = null;
	protected List<String> filterNamesList = null;
	
	public void setAttributesList(List<Attribute> attributesList) {
		this.attributesList = attributesList;
		this.attributeNamesList = new ArrayList<String>();
		if (null!=attributesList) {	
			for (Attribute attribute : attributesList) {
				this.attributeNamesList.add(attribute.internalName);
			}
		}
	}
	public void setFiltersList(List<Filter> filtersList) {
		this.filtersList = filtersList;
		this.filterNamesList = new ArrayList<String>();
		if (null!=filtersList) {
			for (Filter filter : filtersList) {
				this.filterNamesList.add(filter.internalName);
			}
		}
	}
	public List<String> getAttributeNamesList() {
		return attributeNamesList;
	}
	public List<String> getFilterNamesList() {
		return filterNamesList;
	}
	public String toShortString() {
		return "datasetName	= " + 	datasetName + MyUtils.LINE_SEPARATOR + 
		MyUtils.TAB_SEPARATOR + "attributesList = " + attributesList + 
		", filtersList = " + filtersList;
	}
	public RestFulQueryDataset(String datasetName, List<Attribute> attributesList) {
		this(datasetName, attributesList, null);
	}
	public RestFulQueryDataset(String datasetName, List<Attribute> attributesList, List<Filter> filtersList) {
		super();
		this.datasetName = datasetName;
		this.attributesList = attributesList;
		this.filtersList = filtersList;
		setAttributesList(attributesList);
		setFiltersList(filtersList);
	}
}
