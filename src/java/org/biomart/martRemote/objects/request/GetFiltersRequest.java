package org.biomart.martRemote.objects.request;

import java.util.List;


import org.biomart.martRemote.enums.MartServiceFormat;
import org.jdom.Namespace;


public class GetFiltersRequest extends MartServiceRequest {
	
	protected String datasetName = null;
	protected String partitionFilter = null;
	protected List<String> partitionFilterValues = null;
	
	public GetFiltersRequest(String username, String password, 
			String datasetName, String partitionFilter, List<String> partitionFilterValues) {
		this(null, null, null, null, username, password, null, datasetName, partitionFilter, partitionFilterValues);
	}
	public GetFiltersRequest(String username, String password, MartServiceFormat format, 
			String datasetName, String partitionFilter, List<String> partitionFilterValues) {
		this(null, null, null, null, username, password, format, datasetName, partitionFilter, partitionFilterValues);
	}
	public GetFiltersRequest(String requestName, Namespace martServiceNamespace, Namespace xsiNamespace, String xsdFile,
			String username, String password, MartServiceFormat format, String datasetName, String partitionFilter, List<String> partitionFilterValues) {
		super(requestName, martServiceNamespace, xsiNamespace, xsdFile, username, password, format);
		this.datasetName = datasetName;
		this.partitionFilter = partitionFilter;
		this.partitionFilterValues = partitionFilterValues;
	}
	public List<String> getPartitionFilterValues() {
		return partitionFilterValues;
	}
	public String getDatasetName() {
		return datasetName;
	}
	public String getPartitionFilter() {
		return partitionFilter;
	}
}
