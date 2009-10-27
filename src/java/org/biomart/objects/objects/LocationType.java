package org.biomart.objects.objects;

public enum LocationType {
	URL		("url"),
	RDBMS	("rdbms");
	
	private String xmlValue = null;
	private LocationType(String xmlValue) {
		this.xmlValue = xmlValue;
	}
	public String getXmlValue() {
		return xmlValue;
	}
}
