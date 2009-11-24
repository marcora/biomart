package org.biomart.martRemote.objects.request;


import org.biomart.martRemote.XmlParameters;
import org.biomart.martRemote.enums.MartRemoteEnum;
import org.biomart.martRemote.enums.MartServiceFormat;


public class GetLinksRequest extends MartRemoteRequest {
	
	protected String datasetName = null;
	public GetLinksRequest(XmlParameters xmlParameters, String username, String password, MartServiceFormat format, String datasetName) {
		super(MartRemoteEnum.GET_LINKS, xmlParameters, username, password, format);
		this.datasetName = datasetName;
	}
	public String getDatasetName() {
		return datasetName;
	}
}
