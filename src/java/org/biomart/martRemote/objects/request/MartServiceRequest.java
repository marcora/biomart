package org.biomart.martRemote.objects.request;


import org.biomart.martRemote.enums.MartServiceFormat;
import org.biomart.martRemote.objects.MartServiceAction;
import org.jdom.Namespace;

public class MartServiceRequest extends MartServiceAction {
	
	protected String username = null;
	protected String password = null;
	protected MartServiceFormat format = null;
	
	public MartServiceRequest(String requestName, Namespace martServiceNamespace, Namespace xsiNamespace, String xsdFile,
			String username, String password, MartServiceFormat format) {
		super(requestName, martServiceNamespace, xsiNamespace, xsdFile);
		this.username = username;
		this.password = password;
		this.format = format;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public MartServiceFormat getFormat() {
		return format;
	}
	
	/**
	 * Can be overriden
	 */
	public void buildObjects() {}
}
