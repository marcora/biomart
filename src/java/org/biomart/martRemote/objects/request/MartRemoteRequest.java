package org.biomart.martRemote.objects.request;


import org.biomart.martRemote.XmlParameters;
import org.biomart.martRemote.enums.MartRemoteEnum;
import org.biomart.martRemote.enums.MartServiceFormat;

public abstract class MartRemoteRequest {

	protected MartRemoteEnum type = null;
	protected String username = null;
	protected String password = null;
	protected MartServiceFormat format = null;
	
	protected StringBuffer errorMessage = null;
	protected XmlParameters xmlParameters = null;

	public MartRemoteRequest(MartRemoteEnum type, XmlParameters xmlParameters,
			String username, String password, MartServiceFormat format) {
		this.username = username;
		this.password = password;
		this.format = format;
		
		this.xmlParameters = xmlParameters;
		this.type = type;
		this.errorMessage = new StringBuffer();
	}
	
	public StringBuffer getErrorMessage() {
		return errorMessage;
	}
	
	public boolean isValid() {
		return errorMessage.length()==0;
	}

	public String getUsername() {
		return username;
	}
	
	public XmlParameters getXmlParameters() {
		return xmlParameters;
	}
	
	public MartRemoteEnum getType() {
		return this.type;
	}

	public String getPassword() {
		return password;
	}

	public MartServiceFormat getFormat() {
		return format;
	}
		
	/**
	 * Can implement that (query for instance does)
	 */
	public void buildObjects() {}
}
