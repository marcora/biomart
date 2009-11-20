package org.biomart.objects.lite;

import java.io.Serializable;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.objects.MartConfiguratorObject;
import org.jdom.Document;

public abstract class LiteSimpleMartConfiguratorObject extends LiteMartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = -3075201719659966338L;
	
	protected String name = null;
	protected String displayName = null;
	protected String description = null;
	protected Boolean visible = null;

	protected LiteSimpleMartConfiguratorObject(
			String xmlElementName, String name, String displayName, String description, Boolean visible) {
		super(xmlElementName, name, displayName, description, visible);
		
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

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Boolean getVisible() {
		return visible;
	}

	public String getDescription() {
		return description;
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
		LiteSimpleMartConfiguratorObject liteMartConfiguratorObject=(LiteSimpleMartConfiguratorObject)object;
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
	protected Document generateXml(Document document) throws FunctionalException {
		Jsoml root = new Jsoml(document.getRootElement());
		generateExchangeFormat(true, root).getXmlElement();
		return document;
	}
	@Override
	protected JSONObject generateJson(String responseName) throws FunctionalException {
		return generateExchangeFormat(false, new Jsoml(false, responseName)).getJsonObject();		
	}
	
	protected Jsoml generateExchangeFormat(boolean xml, Jsoml root) throws FunctionalException {	// unless overriden
		root.addContent(this.generateExchangeFormat(xml));
		return root;
	}
	protected abstract Jsoml generateExchangeFormat(boolean xml) throws FunctionalException;
}
