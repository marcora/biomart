package org.biomart.martRemote.objects.request;

import java.util.List;

import org.biomart.martRemote.XmlParameters;
import org.biomart.martRemote.enums.MartRemoteEnum;
import org.biomart.martRemote.enums.MartServiceFormat;

public abstract class GetContaineesRequest extends MartRemoteRequest {
	protected String datasetName = null;
	protected String partitionFilter = null;
	protected List<String> partitionFilterValues = null;
	
	/*protected GetContaineesRequest(String username, String password, 
			String datasetName, String partitionFilter, List<String> partitionFilterValues) {
		this(null, null, null, null, username, password, null, datasetName, partitionFilter, partitionFilterValues);
	}*/
	/*protected GetContaineesRequest(String username, String password, MartServiceFormat format, 
			String datasetName, String partitionFilter, List<String> partitionFilterValues) {
		this(null, null, null, null, username, password, format, datasetName, partitionFilter, partitionFilterValues);
	}*/
	protected GetContaineesRequest(MartRemoteEnum type, XmlParameters xmlParameters,
			String username, String password, MartServiceFormat format, String datasetName, String partitionFilter, List<String> partitionFilterValues) {
		super(type, xmlParameters, username, password, format);
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
