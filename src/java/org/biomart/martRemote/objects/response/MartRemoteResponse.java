package org.biomart.martRemote.objects.response;


import java.io.IOException;
import java.io.Writer;

import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.JsonUtils;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.XmlUtils;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.MartRemoteUtils;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.objects.MartRegistry;
import org.jdom.Document;
import org.jdom.Element;


public abstract class MartRemoteResponse {

	protected MartRemoteRequest martRemoteRequest = null;
	protected MartRegistry martRegistry = null;
	
	protected StringBuffer errorMessage = null;
	
	protected MartRemoteResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		this.martRegistry = martRegistry;
		this.martRemoteRequest = martServiceRequest;
		this.errorMessage = new StringBuffer();
	}

	public StringBuffer getErrorMessage() {
		return errorMessage;
	}
	public boolean isValid() {
		return errorMessage.length()==0;
	}
	
	public MartRemoteRequest getMartServiceRemote() {
		return martRemoteRequest;
	}
	
	public Document getXmlDocument() throws TechnicalException, FunctionalException {
		return getXmlDocument(false, null);
	}
	/**
	 * Always check that martServiceResult is valid afterwards (if it validates)
	 */
	public Document getXmlDocument(boolean debug, Writer printWriter) throws TechnicalException, FunctionalException {
		Document document = MartRemoteUtils.createNewResponseXmlDocument(
				martRemoteRequest.getXmlParameters(), martRemoteRequest.getType().getResponseName());
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
	public JSONObject getJsonObject(boolean debug, Writer printWriter) throws TechnicalException, FunctionalException {
		//String documentString = MartRemoteUtils.getXmlDocumentString(getXmlDocument());
		//org.json.JSONObject jsonObject = null;
		//try {
			//jsonObject = 
				//XML.toJSONObject(documentString);
				//MartRemoteUtils.getJSONObjectFromDocument(getXmlDocument());
		/*} catch (JSONException e) {
			throw new TechnicalException(e);
		}*/
			
		//http://json-lib.sourceforge.net/apidocs/net/sf/json/xml/XMLSerializer.html
		//new JSONObject().fromObject(object)
		
		JSONObject jsonObject = null;
		try {
			jsonObject = createJsonResponse(martRemoteRequest.getType().getResponseName());
			if (debug && printWriter!=null) {
				printWriter.append(
						//jsonObject
						JsonUtils.getJSONObjectNiceString(jsonObject)
						+ MyUtils.LINE_SEPARATOR);
			}
		} catch (JSONException e) {
			throw new TechnicalException(e);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		
		return jsonObject;
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
	}
	
	public JSONObject getJsonObject4b(boolean debug, Writer printWriter) throws TechnicalException, FunctionalException {
		Document document = getXmlDocument();
		Element rootElement = document.getRootElement();
		
		Element newRootElement = new Element(rootElement.getName());
		newRootElement.addContent(rootElement.cloneContent());
		Document newDocument = new Document(newRootElement);
		
		JSONObject jsonObject = JsonUtils.getJSONObjectFromDocument(newDocument);
		
		if (debug && printWriter!=null) {
			try {
				printWriter.append(JsonUtils.getJSONObjectNiceString(jsonObject) + MyUtils.LINE_SEPARATOR);
			} catch (IOException e) {
				throw new TechnicalException(e);
			}
		}
		
		return jsonObject;
	}
	

	protected Document createXmlResponse(Document document) throws FunctionalException {
		Jsoml root = new Jsoml(document.getRootElement());
		createOutputResponse(true, root).getXmlElement();
		return document;		
	}
	protected JSONObject createJsonResponse(String responseName) throws FunctionalException {
		return createOutputResponse(false, new Jsoml(false, responseName)).getJsonObject();		
	}
	
	protected abstract Jsoml createOutputResponse(boolean xml, Jsoml root) throws FunctionalException;
	
	protected abstract void populateObjects() throws TechnicalException, FunctionalException;
	/*protected abstract Document createXmlResponse(Document document) throws FunctionalException;
	protected abstract JSONObject createJsonResponse(String responseName) throws FunctionalException;*/
}
