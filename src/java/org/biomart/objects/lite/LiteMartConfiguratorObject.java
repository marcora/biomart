package org.biomart.objects.lite;

import java.io.Serializable;

import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.objects.MartConfiguratorObject;

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
}
