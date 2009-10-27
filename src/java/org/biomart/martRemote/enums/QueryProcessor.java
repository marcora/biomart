package org.biomart.martRemote.enums;


public enum QueryProcessor {

	TSV			("TSV");
	
	private String value = null;
	private QueryProcessor(String value) {
		this.value = value;
	}
	public String getValue() {
		return this.value;
	}
	public static QueryProcessor getEnumFromValue(String value) {
		for (QueryProcessor queryProcessor : QueryProcessor.values()) {
			if (queryProcessor.getValue().equals(value)) {
				return queryProcessor;
			}
		}
		return null;
	}
}