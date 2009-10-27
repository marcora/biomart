package org.biomart.martRemote.objects.request;


import org.biomart.martRemote.enums.MartServiceFormat;
import org.jdom.Namespace;

public class GetRegistryRequest extends MartServiceRequest {
	
	public GetRegistryRequest(String username, String password) {
		this(null, null, null, null, username, password, null);
	}
	public GetRegistryRequest(String username, String password, MartServiceFormat format) {
		this(null, null, null, null, username, password, format);
	}
	public GetRegistryRequest(String requestName, Namespace martServiceNamespace, Namespace xsiNamespace, String xsdFile,
			String username, String password, MartServiceFormat format) {
		super(requestName, martServiceNamespace, xsiNamespace, xsdFile, username, password, format);
	}
}
