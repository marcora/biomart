package org.biomart.martRemote;


import org.jdom.Namespace;


public class XmlParameters {
	private Boolean validate = null;
	private Namespace martServiceNamespace = null;
	private Namespace xsiNamespace = null;
	private String xsdFile = null;
	
	public XmlParameters() {
		this.validate = false;
	}
	public XmlParameters(boolean validate, Namespace martServiceNamespace, Namespace xsiNamespace, String xsdFile) {
		this.validate = validate;
		this.martServiceNamespace = martServiceNamespace;
		this.xsiNamespace = xsiNamespace;
		this.xsdFile = xsdFile;
	}
	
	public Boolean getValidate() {
		return validate;
	}
	
	public Namespace getMartServiceNamespace() {
		return martServiceNamespace;
	}

	public Namespace getXsiNamespace() {
		return xsiNamespace;
	}

	public String getXsdFile() {
		return xsdFile;
	}
}
