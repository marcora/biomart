package org.biomart.objects.lite;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.JsonUtils;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.XmlUtils;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.MartRemoteUtils;
import org.biomart.martRemote.XmlParameters;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.jdom.Document;

public abstract class MartRemoteObject implements Serializable {

	private static final long serialVersionUID = -7771063236020188948L;

	protected MartRemoteRequest martRemoteRequest = null;
	protected String xmlElementName = null;
	
	protected MartRemoteObject(MartRemoteRequest martRemoteRequest, String xmlElementName) {
		this.martRemoteRequest = martRemoteRequest;
		this.xmlElementName = xmlElementName;
	}
	
	protected Document getXmlDocument() throws TechnicalException, FunctionalException {
		return getXmlDocument(false, null);
	}
	protected Document getXmlDocument(boolean debug, Writer printWriter) throws TechnicalException, FunctionalException {
		XmlParameters xmlParameters = this.martRemoteRequest.getXmlParameters();
		
		Document document = MartRemoteUtils.createNewMartRemoteXmlDocument(
				xmlParameters, this.martRemoteRequest.getType().getResponseName());
		document = generateXml(document);
		if (debug && printWriter!=null) {
			try {
				printWriter.append(XmlUtils.getXmlDocumentString(document) + MyUtils.LINE_SEPARATOR);
			} catch (IOException e) {
				throw new TechnicalException(e);
			}
		}
		
		if (xmlParameters.getValidate()) {		// valide only if required
			StringBuffer errorMessage = new StringBuffer();
			MartRemoteUtils.validateXml(document, errorMessage);	// Validation with XSD
					// update errorMessage if not validation fails
		}
			
		return document;
	}
	protected JSONObject getJsonObject() throws TechnicalException, FunctionalException {
		return getJsonObject(false, null);
	}
	protected JSONObject getJsonObject(boolean debug, Writer printWriter) throws TechnicalException, FunctionalException {
		JSONObject jsonObject = null;
		try {
			jsonObject = generateJson(this.martRemoteRequest.getType().getResponseName());
			if (debug && printWriter!=null) {
				printWriter.append(JsonUtils.getJSONObjectNiceString(jsonObject) + MyUtils.LINE_SEPARATOR);
			}
		} catch (JSONException e) {
			throw new TechnicalException(e);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		
		return jsonObject;
	}
	protected Document generateXml(Document document) throws FunctionalException {
		Jsoml root = new Jsoml(document.getRootElement());
		generateExchangeFormat(true, root).getXmlElement();
		return document;
	}
	protected JSONObject generateJson(String responseName) throws FunctionalException {
		return generateExchangeFormat(false, new Jsoml(false, responseName)).getJsonObject();		
	}
	protected abstract Jsoml generateExchangeFormat(boolean xml, Jsoml root) throws FunctionalException;
}
