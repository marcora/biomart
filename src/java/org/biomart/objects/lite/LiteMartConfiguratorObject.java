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
import org.biomart.martRemote.MartRemoteUtils;
import org.biomart.martRemote.XmlParameters;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.objects.MartConfiguratorObject;
import org.jdom.Document;

public abstract class LiteMartConfiguratorObject extends MartRemoteObject implements Serializable {

	private static final long serialVersionUID = -4092400660475456118L;

	protected String name = null;
	protected String displayName = null;
	protected String description = null;
	protected Boolean visible = null;
	
	protected LiteMartConfiguratorObject(MartRemoteRequest martRemoteRequest) {
		this(martRemoteRequest, null, null, null, null, null);
	}		
	protected LiteMartConfiguratorObject(String xmlElementName, String name, String displayName, String description, Boolean visible) {
		this(null, xmlElementName, name, displayName, description, visible);
	}
	protected LiteMartConfiguratorObject(MartRemoteRequest martRemoteRequest, 
			String xmlElementName, String name, String displayName, String description, Boolean visible) {
		super(martRemoteRequest, xmlElementName);
			
		this.name = name;
		this.displayName = displayName;
		this.description = description;
		this.visible = visible;
	}
	protected void updatePointerClone(MartConfiguratorObject martConfiguratorObject) {
		this.name = martConfiguratorObject.getName();
		this.displayName = martConfiguratorObject.getDisplayName();
		this.description = martConfiguratorObject.getDescription();
	}

	@Override
	public String toString() {
		return 
			"name = " + name + ", " +
			"displayName = " + displayName + ", " +
			"description = " + description + ", " +
			"visible = " + visible;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		LiteMartConfiguratorObject liteMartConfiguratorObject=(LiteMartConfiguratorObject)object;
		return (
			this.getClass().equals(object.getClass()) &&
			this.name.equals(liteMartConfiguratorObject.name)		//TODO better in MCO
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==name? 0 : name.hashCode());	// Sufficient for our system
		return hash;
	}
	
	@Override
	protected Document getXmlDocument() throws TechnicalException, FunctionalException {
		return getXmlDocument(false, null);
	}
	@Override
	protected Document getXmlDocument(boolean debug, Writer printWriter) throws TechnicalException, FunctionalException {
		XmlParameters xmlParameters = super.martRemoteRequest.getXmlParameters();
		
		Document document = MartRemoteUtils.createNewMartRemoteXmlDocument(
				xmlParameters, super.martRemoteRequest.getType().getResponseName());
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
	@Override
	protected JSONObject getJsonObject() throws TechnicalException, FunctionalException {
		return getJsonObject(false, null);
	}
	@Override
	protected JSONObject getJsonObject(boolean debug, Writer printWriter) throws TechnicalException, FunctionalException {
		JSONObject jsonObject = null;
		try {
			jsonObject = generateJson(super.martRemoteRequest.getType().getResponseName());
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

	protected abstract Document generateXml(Document document) throws FunctionalException;
	protected abstract JSONObject generateJson(String responseName) throws FunctionalException;
}
