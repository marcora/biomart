package org.biomart.martRemote.objects.request;


import org.biomart.martRemote.enums.MartServiceFormat;
import org.jdom.Namespace;


public class GetLinksRequest extends MartServiceRequest {
	
	protected String datasetName = null;
	public GetLinksRequest(String username, String password, String datasetName) {
		this(null, null, null, null, username, password, null, datasetName);
	}
	public GetLinksRequest(String username, String password, MartServiceFormat format, String datasetName) {
		this(null, null, null, null, username, password, format, datasetName);
	}
	public GetLinksRequest(String requestName, Namespace martServiceNamespace, Namespace xsiNamespace, String xsdFile,
			String username, String password, MartServiceFormat format, String datasetName) {
		super(requestName, martServiceNamespace, xsiNamespace, xsdFile, username, password, format);
		this.datasetName = datasetName;
	}
	public String getDatasetName() {
		return datasetName;
	}
}
