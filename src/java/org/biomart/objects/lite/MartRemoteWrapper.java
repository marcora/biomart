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

public abstract class MartRemoteWrapper implements Serializable {

	private static final long serialVersionUID = -7987597129758017949L;

	protected static final String LOCK_VIOLATION_ERROR_MESSAGE = "Invalid operation: objects are already populated";
	
	protected MartRemoteRequest martRemoteRequest = null;
	protected Boolean lock = null;
	
	protected MartRemoteWrapper(MartRemoteRequest martRemoteRequest) {
		this.martRemoteRequest = martRemoteRequest;
		this.lock = false;
	}
	
	/**
	 * In order to "lock" the object once populated (becomes read-only)
	 */
	public void lock() {
		this.lock = true;
	}
	protected void checkLock() throws FunctionalException {
		if (this.lock) {
			throw new FunctionalException(LOCK_VIOLATION_ERROR_MESSAGE);
		}
	}
	
	public Document getXmlDocument() throws TechnicalException, FunctionalException {
		return getXmlDocument(false, null);
	}
	public Document getXmlDocument(boolean debug, Writer printWriter) throws TechnicalException, FunctionalException {
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
	
	@Deprecated
	public JSONObject getJsonObject() throws TechnicalException, FunctionalException {
		return getJsonObject(false, null);
	}
	@Deprecated
	public JSONObject getJsonObject(boolean debug, Writer printWriter) throws TechnicalException, FunctionalException {
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
	
	private Document generateXml(Document document) throws FunctionalException {
		Jsoml root = new Jsoml(document.getRootElement());
		generateExchangeFormat(true, root).getXmlElement();
		return document;
	}
	
	@Deprecated
	private JSONObject generateJson(String responseName) throws FunctionalException {
		return generateExchangeFormat(false, new Jsoml(false, responseName)).getJsonObject();		
	}
	
	protected abstract Jsoml generateExchangeFormat(boolean xml, Jsoml root) throws FunctionalException;
}
