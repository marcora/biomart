package org.biomart.configurator.utils.type;

public enum MartType {
	SOURCE ("Relational Source"),
	TARGET ("Relational Mart"),
	URL ("URL"),
	XML ("XML"),
	FILE ("File"),
	FAKE ("Fake");
	
	private String description;
	
	MartType(String des) {
		this.description = des;
	}
	
	public String toString() {
		return this.description;
	}
}