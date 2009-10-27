package org.biomart.objects.objects;

public enum TableType {
	SOURCE	("source"),
	TARGET	("target");
	
	private String xmlValue = null;
	private TableType(String xmlValue) {
		this.xmlValue = xmlValue;
	}
	public String getXmlValue() {
		return xmlValue;
	}
}
