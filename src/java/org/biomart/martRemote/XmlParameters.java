package org.biomart.martRemote;


import org.jdom.Namespace;


public class XmlParameters {
	protected Namespace martServiceNamespace = null;
	protected Namespace xsiNamespace = null;
	protected String xsdFile = null;
	
	public XmlParameters(Namespace martServiceNamespace, Namespace xsiNamespace, String xsdFile) {
		this.martServiceNamespace = martServiceNamespace;
		this.xsiNamespace = xsiNamespace;
		this.xsdFile = xsdFile;
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
