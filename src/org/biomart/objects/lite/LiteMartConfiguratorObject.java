package org.biomart.objects.lite;

import java.io.Serializable;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.objects.MartConfiguratorObject;
import org.jdom.Element;

public abstract class LiteMartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = -8493793578216273507L;
	
	protected String xmlElementName = null;
	protected String name = null;
	protected String displayName = null;
	protected String description = null;
	protected Boolean visible = null;

	protected LiteMartConfiguratorObject(MartRemoteRequest martRemoteRequest) {
		this(null, null, null, null, null);
	}		
	protected LiteMartConfiguratorObject(String xmlElementName, String name, String displayName, String description, Boolean visible) {

		this.xmlElementName = xmlElementName;
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
	
	// only property always available for all subclasses
	public String getName() {
		return name;
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
	
	public Element getXmlElement() throws FunctionalException {
		return generateExchangeFormat(true).getXmlElement();
	}
	@Deprecated
	public JSONObject getJsonObject() throws FunctionalException {
		return generateExchangeFormat(false).getJsonObject();
	}
	
	protected abstract Jsoml generateExchangeFormat(boolean xml) throws FunctionalException;
}
