package org.biomart.martRemote.objects.response;


import java.io.IOException;
import java.io.Writer;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.JsonUtils;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.XmlUtils;
import org.biomart.martRemote.MartRemoteUtils;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.objects.MartRegistry;
import org.jdom.Document;
import org.jdom.Element;


public abstract class MartRemoteResponse {

	protected MartRemoteRequest martServiceRequest = null;
	protected MartRegistry martRegistry = null;
	
	protected StringBuffer errorMessage = null;
	
	protected MartRemoteResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		this.martRegistry = martRegistry;
		this.martServiceRequest = martServiceRequest;
		this.errorMessage = new StringBuffer();
	}

	public StringBuffer getErrorMessage() {
		return errorMessage;
	}
	public boolean isValid() {
		return errorMessage.length()==0;
	}
	
	public MartRemoteRequest getMartServiceRequest() {
		return martServiceRequest;
	}
	
	public Document getXmlDocument() throws TechnicalException, FunctionalException {
		return getXmlDocument(false, null);
	}
	/**
	 * Always check that martServiceResult is valid afterwards (if it validates)
	 */
	public Document getXmlDocument(boolean debug, Writer printWriter) throws TechnicalException, FunctionalException {
		Document document = MartRemoteUtils.createNewResponseXmlDocument(
				martServiceRequest.getXmlParameters(), martServiceRequest.getType().getResponseName());
		document = createXmlResponse(document);
		if (debug && printWriter!=null) {
			try {
				printWriter.append(XmlUtils.getXmlDocumentString(document) + MyUtils.LINE_SEPARATOR);
			} catch (IOException e) {
				throw new TechnicalException(e);
			}
		}
		
		MartRemoteUtils.validateXml(document, this.errorMessage);	// Validation with XSD
				// update errorMessage if not validation fails
			
		return document;
	}

	public JSON getJsonObject2() throws TechnicalException, FunctionalException {
		Document document = getXmlDocument();
		
		
		
		Element rootElement = document.getRootElement();
		Element newRoot = new Element(rootElement.getName());
		newRoot.setContent(rootElement.cloneContent());

		Document newDoc = new Document(newRoot);
		
		System.out.println(XmlUtils.getXmlDocumentString(document));
		
		JSON jSON = new XMLSerializer().read(XmlUtils.getXmlDocumentString(newDoc));
		return jSON;
		//return createJsonResponse();	// no pre/post processing
	}
	@Deprecated
	public org.json.JSONObject getJsonObject() throws TechnicalException, FunctionalException {
		//String documentString = MartRemoteUtils.getXmlDocumentString(getXmlDocument());
		org.json.JSONObject jsonObject = null;
		//try {
			//jsonObject = 
				//XML.toJSONObject(documentString);
				//MartRemoteUtils.getJSONObjectFromDocument(getXmlDocument());
		/*} catch (JSONException e) {
			throw new TechnicalException(e);
		}*/
			
		//http://json-lib.sourceforge.net/apidocs/net/sf/json/xml/XMLSerializer.html
		//new JSONObject().fromObject(object)
			
	
			
		return jsonObject;
	}
	public JSONObject getJsonObject4() throws TechnicalException, FunctionalException {
		Document document = getXmlDocument();
		return JsonUtils.getJSONObjectFromDocument(document);
	}
	
	protected abstract void populateObjects() throws TechnicalException, FunctionalException;
	protected abstract Document createXmlResponse(Document document) throws FunctionalException;
	protected abstract JSONObject createJsonResponse();
}
