package org.biomart.martRemote.objects.response;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.martRemote.objects.request.Query;
import org.biomart.martRemote.objects.request.QueryDataset;
import org.biomart.martRemote.objects.request.QueryRequest;
import org.biomart.objects.objects.MartRegistry;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.old.martService.restFulQueries.RestFulQuery;
import org.biomart.old.martService.restFulQueries.RestFulQueryDataset;
import org.biomart.old.martService.restFulQueries.objects.Attribute;
import org.biomart.old.martService.restFulQueries.objects.Filter;

public class QueryResponse extends MartRemoteResponse {

	private List<List<String>> data = null;
	private List<String> headers = null;
	
	public QueryResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
		this.data = new ArrayList<List<String>>();
		//this.headers = new ArrayList<String>();
		this.headers = null;
	}

	public void populateObjects() throws TechnicalException, FunctionalException {
		QueryRequest queryRequest = (QueryRequest)super.martRemoteRequest;
		Query query = queryRequest.getQuery();
		this.data = fetchData(query);
		//this.headers = fetchHeaders(query);
		if (queryRequest.getQuery().getHeader()) {
			this.headers = this.data.remove(0);
		}
	}

	/*private List<String> fetchHeaders(Query query) throws TechnicalException { 	// for now only 1 dataset & no aliases

		QueryDataset queryDataset = query.getQueryDataset();
		String datasetName = queryDataset.getDatasetNameList().get(0);	// only 1 dataset for now
		List<String> attributes = queryDataset.getAttributeNameList();	// no aliases for now
		
		String urlString = MartServiceConstants.CENTRAL_PORTAL_MART_SERVICE_STRING_URL + 
		"?" + "type" + "=" + "attributes" + "&" + "dataset" + "=" + datasetName + 
			(query.getFormerVirtualSchema()!=null ? "&" + "virtualSchema" + "=" + query.getFormerVirtualSchema() : ""); //http://www.biomart.org/biomart/martservice?type=filters&dataset=oanatinus_gene_ensembl
		List<List<String>> datasetAttributes = null;
		try {
			datasetAttributes = MyUtils.copyUrlContentToListStringList(new URL(urlString), MyUtils.TAB_SEPARATOR);
		} catch (MalformedURLException e) {
			throw new TechnicalException(e);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		
		Map<String, String> map = new HashMap<String, String>();
		for (List<String> attribute : datasetAttributes) {
			if (attributes.contains(attribute.get(MartRemoteConstants.ELEMENT_NAME_POSITION))) {
				map.put(attribute.get(
						MartRemoteConstants.ELEMENT_NAME_POSITION), attribute.get(MartRemoteConstants.ELEMENT_DISPLAY_NAME_POSITION));
			}
		}
		
		List<String> headers = new ArrayList<String>();
		for (String attribute : attributes) {
			headers.add(map.get(attribute));
		}
		
		return headers;
	}*/
	
	private List<List<String>> fetchData(Query query) 
	throws FunctionalException, TechnicalException {	// for now only 1 dataset & no aliases		
		QueryDataset queryDataset = query.getQueryDataset();
		String datasetName = queryDataset.getDatasetNameList().get(0);
		List<Attribute> attributeList = Attribute.getAttributeList(queryDataset.getAttributeNameList());
		List<Filter> filterList = Filter.getFilterList(queryDataset.getFilterNameMap());
		RestFulQueryDataset restFulQueryDataset = new RestFulQueryDataset(datasetName, attributeList, filterList);
		RestFulQueryDataset[] datasets = new RestFulQueryDataset[] {restFulQueryDataset};	// for now only 1 dataset
		
		RestFulQuery restFulQuery = null;
		
		try {
			restFulQuery = new RestFulQuery(
					MartServiceConstants.CENTRAL_PORTAL_MART_SERVICE_STRING_URL, query.getFormerVirtualSchema(), 
					query.getBiomartVersion().getValue(), query.getProcessor().getValue(), 
					query.getCount(), query.getHeader(), query.getUniqueRows(), query.getLimitStart(), query.getLimitSize(), datasets);
		} catch (UnsupportedEncodingException e) {
			throw new TechnicalException(e);
		}
		
		List<List<String>> data = new ArrayList<List<String>>();
		List<String> lines = null;
		try {
			lines = MyUtils.copyUrlContentToStringList(restFulQuery.getUrlGet());	// the lines contain the error message if applicable
		} catch (MalformedURLException e) {
			throw new TechnicalException(e);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		
		for (String line : lines) {
			List<String> row = new ArrayList<String>(Arrays.asList(line.split(MyUtils.TAB_SEPARATOR)));
			data.add(row);
		}
		
		return data;
	}
	
	@Override
	public Jsoml createOutputResponse(boolean xml, Jsoml root) throws FunctionalException {
		if (this.headers!=null) {
			Jsoml jsomlHeaders = new Jsoml(xml, "headers");
			for (String header : this.headers) {		// Should happen in a specific object that would contain headers and data 
														// (along with the light read-only clones)
				Jsoml jsomlHeader = new Jsoml(xml, "header");
				jsomlHeader.setText(header);
				jsomlHeaders.addContent(jsomlHeader);
			}
			root.addContent(jsomlHeaders);
		}
		
		Jsoml jsomlRows = new Jsoml(xml, "rows");
		for (List<String> row : this.data) {
			
			Jsoml jsomlRow = new Jsoml(xml, "row");
			for (String value : row) {
				Jsoml jsomlValue = new Jsoml(xml, "value");
				jsomlValue.setText(value);
				jsomlRow.addContent(jsomlValue);
			}
			jsomlRows.addContent(jsomlRow);
		}
		root.addContent(jsomlRows);
		
		return root;
	}
	
	/**
	
		<queryResponse>
			<count>12</count>
			<rows>
				<row></row>
			</rows>
			<fields>
				<field name="ensembl_gene_id" />
				<field name="ensembl_transcript_id" />
			</fields>
			<columns>
				<columns header="Ensembl Gene ID" id="ensembl_gene_id" dataIndex="ensembl_gene_id" />
				<columns header="Ensembl Transcript ID" id="ensembl_transcript_id" dataIndex="ensembl_transcript_id" />
			</columns>
		</queryResponse>
		
	**/
	/*protected Document createXmlResponse(Document document) {
		Element root = document.getRootElement();
		
		int size = data.size();
		Element rows = new Element("rows");
		root.addContent(rows);
		
		rows.setAttribute("count", String.valueOf(size));
	
		QueryRequest queryRequest = (QueryRequest)super.martRemoteRequest;
		List<String> attributeNameList = queryRequest.getQuery().getQueryDataset().getAttributeNameList();
		
		for (List<String> row : data) {
			Element rowElement = new Element("row");
			rows.addContent(rowElement);
			for (int i = 0; i < row.size(); i++) {
				Element field = new Element("field");
				field.setAttribute("fieldName", attributeNameList.get(i));
				field.setText(row.get(i));
				rowElement.addContent(field);
			}
		}
		
		return document;
	}
	protected JSONObject createJsonResponse(String responseName) {

		QueryRequest queryRequest = (QueryRequest)super.martRemoteRequest;
		List<String> attributeNameList = queryRequest.getQuery().getQueryDataset().getAttributeNameList();
		int size = data.size();
		
		JSONArray rows = new JSONArray();
		for (List<String> dataRow : data) {
			JSONArray row = new JSONArray();
			for (int i = 0; i < attributeNameList.size(); i++) {
				String attributeName = attributeNameList.get(i);
				JSONObject field = new JSONObject();
				field.put(attributeName, dataRow.get(i));
				row.add(field);
			}
			rows.add(row);
		}
		
		JSONArray fields = new JSONArray();
		for (int i = 0; i < attributeNameList.size(); i++) {
			String attributeName = attributeNameList.get(i);
			JSONObject field = new JSONObject();
			field.put("name", attributeName);
			fields.add(field);
		}
		
		JSONArray columns = new JSONArray();
		for (int i = 0; i < attributeNameList.size(); i++) {
			String attributeName = attributeNameList.get(i);
			JSONObject column = new JSONObject();
			column.put("header", headers.get(i));
			column.put("id", attributeName);
			column.put("dataIndex", attributeName);
			columns.add(column);
		}
		
		JSONObject root = new JSONObject();
		root.put("count", size);
		root.put("rows", rows);
		root.put("fields", fields);
		root.put("columns", columns);
		return root;
	}*/
}
