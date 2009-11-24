package org.biomart.martRemote.objects.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.biomart.martRemote.enums.BiomartVersion;
import org.biomart.martRemote.enums.QueryProcessor;
import org.jdom.Document;
import org.jdom.Element;


public class Query {

	private String formerVirtualSchema = null;
	private BiomartVersion biomartVersion = null;
	private QueryProcessor processor = null;
	private Boolean header = null;
	private Boolean uniqueRows = null;
	private Boolean count = null;
	private Integer limitStart = null;
	private Integer limitSize = null;
	private QueryDataset queryDataset = null;
	
	private Query(QueryDataset queryDataset) {
		this(queryDataset, null, BiomartVersion.ZERO_POINT_EIGHT, QueryProcessor.TSV, true, false, false, null, null);
	}
	private Query(QueryDataset queryDataset, BiomartVersion biomartVersion, QueryProcessor processor,
			Boolean header, Boolean uniqueRows, Boolean count) {
		this(queryDataset, null, biomartVersion, processor, header, uniqueRows, count, null, null);
	}
	private Query(QueryDataset queryDataset, String formerVirtualSchemaString, String biomartVersionString, String processorString,
			String headerString, String uniqueRowsString, String countString,
			String limitStartString, String limitSizeString) {
		this(queryDataset, 
				formerVirtualSchemaString,
				biomartVersionString==null ? BiomartVersion.ZERO_POINT_EIGHT : BiomartVersion.getEnumFromValue(biomartVersionString), 
				processorString==null ? QueryProcessor.TSV : QueryProcessor.getEnumFromValue(processorString),
				headerString==null ? true : Boolean.valueOf(headerString),
				uniqueRowsString==null ? false : Boolean.valueOf(uniqueRowsString),
				countString==null ? false : Boolean.valueOf(countString),
				limitStartString==null ? null : Integer.valueOf(limitStartString),
				limitSizeString==null ? null : Integer.valueOf(limitSizeString));
	}
	private Query(QueryDataset queryDataset, String formerVirtualSchema, BiomartVersion biomartVersion, QueryProcessor processor,
			Boolean header, Boolean uniqueRows, Boolean count,
			Integer limitStart, Integer limitSize) {
		super();
		this.queryDataset = queryDataset;
		this.biomartVersion = biomartVersion;
		this.formerVirtualSchema = formerVirtualSchema;
		this.processor = processor;
		this.header = header;
		this.uniqueRows = uniqueRows;
		this.count = count;
		this.limitStart = limitStart;
		this.limitSize = limitSize;
	}
	@Override
	public String toString() {
		return 
		"formerVirtualSchema = " + formerVirtualSchema + ", " +
		"biomartVersion = " + biomartVersion + ", " +
		"processor = " + processor + ", " +
		"header = " + header + ", " +
		"uniqueRows = " + uniqueRows + ", " +
		"count = " + count + ", " +
		"limitStart = " + limitStart + ", " +
		"limitSize = " + limitSize + ", " +
		"queryDataset = " + queryDataset;
	}
	public QueryDataset getQueryDataset() {
		return queryDataset;
	}
	public String getFormerVirtualSchema() {
		return formerVirtualSchema;
	}
	public BiomartVersion getBiomartVersion() {
		return biomartVersion;
	}
	public QueryProcessor getProcessor() {
		return processor;
	}
	public Boolean getHeader() {
		return header;
	}
	public Boolean getUniqueRows() {
		return uniqueRows;
	}
	public Boolean getCount() {
		return count;
	}
	public Integer getLimitStart() {
		return limitStart;
	}
	public Integer getLimitSize() {
		return limitSize;
	}
	@SuppressWarnings("unchecked")
	public static Query fromXml(Document document) {
		Element root = document.getRootElement();
		
		Element queryElement = root.getChild("query");
		
		String formerVirtualSchemaString = queryElement.getAttributeValue("formerVirtualSchema");
		String processorString = queryElement.getAttributeValue("processor");
		String headerString = queryElement.getAttributeValue("header");
		String uniqueRowsString = queryElement.getAttributeValue("uniqueRows");
		String countString = queryElement.getAttributeValue("count");
		String biomartVersionString = queryElement.getAttributeValue("datasetConfigVersion");		
		String limitStartString = queryElement.getAttributeValue("limitStart");
		String limitSizeString = queryElement.getAttributeValue("limitSize");
		
		Element dataset = queryElement.getChild("dataset");
		String datasetNameString = dataset.getAttributeValue("name");
	
		String operationString = dataset.getAttributeValue("operation");
		
		List<Element> attributes = dataset.getChildren("attribute");
		List<String> attributeNames = new ArrayList<String>();
		for (Element attribute : attributes) {
			String attributeNameString = attribute.getAttributeValue("name");
			attributeNames.add(attributeNameString);
		}
		
		List<Element> filters = dataset.getChildren("filter");
		Map<String, StringBuffer> filterNameAndValues = new HashMap<String, StringBuffer>();
		for (Element filter : filters) {
			String filterNameString = filter.getAttributeValue("name");
			StringBuffer filterValueString = new StringBuffer(filter.getAttributeValue("value"));
			filterNameAndValues.put(filterNameString, filterValueString);
		}
		
		QueryDataset queryDataset = new QueryDataset(datasetNameString, operationString, attributeNames, filterNameAndValues);
		Query query = new Query(queryDataset, formerVirtualSchemaString, biomartVersionString, processorString, 
				headerString, uniqueRowsString, countString, limitStartString, limitSizeString);
		return query;
	}
}
