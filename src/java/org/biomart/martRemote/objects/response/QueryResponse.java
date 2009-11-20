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
import org.biomart.objects.lite.QueryResult;
import org.biomart.objects.objects.MartRegistry;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.old.martService.restFulQueries.RestFulQuery;
import org.biomart.old.martService.restFulQueries.RestFulQueryDataset;
import org.biomart.old.martService.restFulQueries.objects.Attribute;
import org.biomart.old.martService.restFulQueries.objects.Filter;

public class QueryResponse extends MartRemoteResponse {

	private QueryResult queryResult = null;
	
	public QueryResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
		this.queryResult = new QueryResult(martServiceRequest);
	}
	
	public QueryResult getQueryResult() {
		return queryResult;
	}

	public void populateObjects() throws TechnicalException, FunctionalException {
		QueryRequest queryRequest = (QueryRequest)super.martRemoteRequest;
		Query query = queryRequest.getQuery();
		List<List<String>> data = fetchData(query);
		this.queryResult.setData(data);
		if (queryRequest.getQuery().getHeader()) {
			this.queryResult.setHeaders(data.remove(0));	// because headers are the first row if it has been requested
		}
	}

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
	protected Jsoml createOutputResponse(boolean xml, Jsoml root)
			throws FunctionalException {
		// TODO Auto-generated method stub
		return null;
	}
}
