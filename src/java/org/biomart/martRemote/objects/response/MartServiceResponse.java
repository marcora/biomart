package org.biomart.martRemote.objects.response;


import java.io.IOException;
import java.io.PrintWriter;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.martRemote.MartRemoteUtils;
import org.biomart.martRemote.objects.MartServiceAction;
import org.biomart.martRemote.objects.request.MartServiceRequest;
import org.biomart.objects.objects.MartRegistry;
import org.jdom.Document;
import org.jdom.Namespace;


public abstract class MartServiceResponse extends MartServiceAction {

	protected String responseName = null;
	protected MartRegistry martRegistry = null;
	protected MartServiceRequest martServiceRequest = null;
	
	protected MartServiceResponse(String responseName, Namespace martServiceNamespace, Namespace xsiNamespace, String xsdFile, 
			MartRegistry martRegistry, MartServiceRequest martServiceRequest) {
		super(responseName, martServiceNamespace, xsiNamespace, xsdFile);
		this.responseName = super.actionName;
		this.martRegistry = martRegistry;
		this.martServiceRequest = martServiceRequest;
	}
	
	public MartServiceRequest getMartServiceRequest() {
		return martServiceRequest;
	}
	
	public Document getXmlRegistry() throws IOException {
		return getXmlRegistry(false, null);
	}
	/**
	 * Always check that martServiceResult is valid afterwards (if it validates)
	 */
	public Document getXmlRegistry(boolean debug, PrintWriter printWriter) throws IOException {
		Document document = createNewResponseXmlDocument(this.responseName);
		document = createXmlResponse(document);
		if (debug && printWriter!=null) {
			printWriter.println(MartRemoteUtils.getXmlDocumentString(document));
		}
		
		validateXml(document);	// Validation with XSD
				// update errorMessage if not validation fails
			
		return document;
	}	

	public JSONObject getJsonRegistry() throws IOException {
		return createJsonResponse();	// no pre/post processing
	}
	
	protected abstract void populateObjects() throws TechnicalException, FunctionalException;
	protected abstract Document createXmlResponse(Document document);
	protected abstract JSONObject createJsonResponse();
}
