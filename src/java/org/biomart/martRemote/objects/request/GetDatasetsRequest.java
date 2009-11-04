package org.biomart.martRemote.objects.request;


import org.biomart.martRemote.XmlParameters;
import org.biomart.martRemote.enums.MartRemoteEnum;
import org.biomart.martRemote.enums.MartServiceFormat;


public class GetDatasetsRequest extends MartRemoteRequest {
	
	protected String martName = null;
	protected Integer martVersion = null;
	public GetDatasetsRequest(XmlParameters xmlParameters, String username, String password, MartServiceFormat format, String martName, Integer martVersion) {
		super(MartRemoteEnum.GET_DATASETS, xmlParameters, username, password, format);
		this.martName = martName;
		this.martVersion = martVersion;
	}
	public String getMartName() {
		return martName;
	}
	public Integer getMartVersion() {
		return martVersion;
	}
}
