package org.biomart.martRemote.objects.request;


import org.biomart.martRemote.enums.MartServiceFormat;
import org.jdom.Namespace;


public class GetDatasetsRequest extends MartServiceRequest {
	
	protected String martName = null;
	protected Integer martVersion = null;
	public GetDatasetsRequest(String username, String password, String martName, Integer martVersion) {
		this(null, null, null, null, username, password, null, martName, martVersion);
	}
	public GetDatasetsRequest(String username, String password, MartServiceFormat format, String martName, Integer martVersion) {
		this(null, null, null, null, username, password, format, martName, martVersion);
	}
	public GetDatasetsRequest(String requestName, Namespace martServiceNamespace, Namespace xsiNamespace, String xsdFile,
			String username, String password, MartServiceFormat format, String martName, Integer martVersion) {
		super(requestName, martServiceNamespace, xsiNamespace, xsdFile, username, password, format);
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
