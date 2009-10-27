package org.biomart.martRemote.enums;

public enum QueryOperation {
	
	UNION			("Union"),
	INTERSECTION	("Intersection");
	
	private String value = null;
	private QueryOperation(String value) {
		this.value = value;
	}
	public String getValue() {
		return this.value;
	}
	public static QueryOperation getEnumFromValue(String value) {
		for (QueryOperation queryOperation : QueryOperation.values()) {
			if (queryOperation.getValue().equals(value)) {
				return queryOperation;
			}
		}
		return null;
	}
}
