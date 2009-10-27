package org.biomart.martRemote.objects;


import java.io.ByteArrayOutputStream;
import java.io.IOException;


import org.biomart.common.general.utils.MyUtils;
import org.biomart.martRemote.MartRemoteUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class MartServiceAction {

	protected String actionName = null;
	protected StringBuffer errorMessage = null;

	protected Namespace martServiceNamespace = null;
	protected Namespace xsiNamespace = null;
	protected String xsdFile = null;
	
	protected MartServiceAction(String actionName, Namespace martServiceNamespace, Namespace xsiNamespace, String xsdFile) {
		this.actionName = actionName;
		this.martServiceNamespace = martServiceNamespace;
		this.xsiNamespace = xsiNamespace;
		this.xsdFile = xsdFile;
		
		this.errorMessage = new StringBuffer();
	}
	
	public StringBuffer getErrorMessage() {
		return errorMessage;
	}
	
	public boolean isValid() {
		return errorMessage.length()==0;
	}
		
	protected Document createNewResponseXmlDocument(String rootName) {
		Element root = new org.jdom.Element(rootName, martServiceNamespace);
		root.addNamespaceDeclaration(xsiNamespace);
		root.setAttribute("schemaLocation", 
				martServiceNamespace.getURI() + " " + xsdFile, xsiNamespace);
		return new Document(root);
	}

	protected boolean validateXml(Document document) throws IOException {
		String errorValidation = MartRemoteUtils.validationXml(document);
		if (null!=errorValidation) {
			createErrorResponse(document, errorValidation);
			return false;
		}
		return true;
	}
	
	// Error response creation
	protected void createErrorResponse(Document document, String errorValidation) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new XMLOutputter(Format.getPrettyFormat()).output(document, baos);
		this.errorMessage.append(MyUtils.LINE_SEPARATOR + baos.toString() + MyUtils.LINE_SEPARATOR + MyUtils.LINE_SEPARATOR + errorValidation);
	}
}
