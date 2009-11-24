package org.biomart.martRemote.objects.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.biomart.martRemote.enums.QueryOperation;



public class QueryDataset {

	private List<String> datasetNameList = null;
	private QueryOperation operation = null;
	private List<String> attributeNameList = null;
	private Map<String, StringBuffer> filterNameMap = null;		// StringBuffer because value can be substancially long
	public QueryDataset(String datasetNameString, String operationString, List<String> attributeNames, Map<String, StringBuffer> filterNameAndValues) {
		this(new ArrayList<String>(Arrays.asList(datasetNameString.split(","))), 
				operationString==null ? QueryOperation.UNION : QueryOperation.getEnumFromValue(operationString), 
				attributeNames, filterNameAndValues);
	}
	public QueryDataset(List<String> datasetNameList, QueryOperation operation,
			List<String> attributeNameList, Map<String, StringBuffer> filterNameMap) {
		super();
		this.datasetNameList = datasetNameList;
		this.operation = operation;
		this.attributeNameList = attributeNameList;
		this.filterNameMap = filterNameMap;
	}
	public List<String> getDatasetNameList() {
		return datasetNameList;
	}
	public QueryOperation getOperation() {
		return operation;
	}
	public List<String> getAttributeNameList() {
		return attributeNameList;
	}
	public Map<String, StringBuffer> getFilterNameMap() {
		return filterNameMap;
	}
	@Override
	public String toString() {
		return 
		"datasetNameList = " + datasetNameList + ", " +
		"operation = " + operation + ", " +
		"attributeNameList = " + attributeNameList + ", " +
		"filterNameMap = " + filterNameMap;
	}
}
