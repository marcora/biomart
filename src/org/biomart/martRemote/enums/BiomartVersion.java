package org.biomart.martRemote.enums;

public enum BiomartVersion {
	
	ZERO_POINT_FOUR		("0.4"),
	ZERO_POINT_FIVE		("0.5"),
	ZERO_POINT_SIX		("0.6"),
	ZERO_POINT_SEVEN	("0.7"),
	ZERO_POINT_EIGHT	("0.8");
	
	private String value = null;
	private BiomartVersion(String value) {
		this.value = value;
	}
	public String getValue() {
		return this.value;
	}
	public static BiomartVersion getEnumFromValue(String value) {
		for (BiomartVersion biomartVersion : BiomartVersion.values()) {
			if (biomartVersion.getValue().equals(value)) {
				return biomartVersion;
			}
		}
		return null;
	}
}