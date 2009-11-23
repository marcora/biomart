package org.biomart.objects.lite;


import java.io.Serializable;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.objects.objects.Location;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.types.LocationType;

public class LiteMart extends LiteMartConfiguratorObject implements Serializable {
	
	private static final long serialVersionUID = 5954018490665763734L;
	
	private static final String XML_ELEMENT_NAME = "mart";
	
	private Integer version = null;
	
	private String host = null;
	private String user = null;
	private LocationType type = null;

	public LiteMart(Location location, Mart mart) {
		
		super(XML_ELEMENT_NAME, mart.getName(), mart.getDisplayName(), null, mart.getVisible());
		
		this.version = mart.getVersion();
		
		this.host = location.getHost();
		this.user = location.getUser();
		this.type = location.getType();
	}
	
	// Properties in super class available for this light object
	public String getDisplayName() {
		return super.displayName;
	}
	public Boolean getVisible() {
		return super.visible;
	}

	public String getHost() {
		return host;
	}

	public String getUser() {
		return user;
	}

	public LocationType getType() {
		return type;
	}

	public Integer getVersion() {
		return version;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"host = " + host + ", " + 
			"user = " + user + ", " + 
			"type = " + type + ", " + 
			"version = " + version;
	}

	@Override
	protected Jsoml generateExchangeFormat(boolean xml) throws FunctionalException {
		Jsoml jsoml = new Jsoml(xml, super.xmlElementName);
		
		// Mart info
		jsoml.setAttribute("name", super.name);
		jsoml.setAttribute("displayName", super.displayName);
		jsoml.setAttribute("visible", super.visible);		
		
		jsoml.setAttribute("version", this.version);
		
		// Location info
		jsoml.setAttribute("host", this.host);
		jsoml.setAttribute("type", (this.getType()!=null ? this.getType().getXmlValue() : null));
		jsoml.setAttribute("user", this.user);
		
		return jsoml;
	}
}
