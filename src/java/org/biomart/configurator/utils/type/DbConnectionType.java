package org.biomart.configurator.utils.type;

public enum DbConnectionType {
	MySQL (""),
	PostgreSQL (""),
	Oracle ("");
	
	private String connectionURL;
	
	DbConnectionType(String conStr) {
		this.connectionURL = conStr;
	}
	
	public String toString() {
		return this.connectionURL;
	}
}