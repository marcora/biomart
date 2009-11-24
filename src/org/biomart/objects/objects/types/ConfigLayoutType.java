package org.biomart.objects.objects.types;



public enum ConfigLayoutType {
	LEFT_VERTICAL		("leftVertical"),	
	RIGHT_VERTICAL		("rightVertical"),
	CENTRE_VERTICAL		("centreVertical"),
	LEFT_HORIZONTAL		("leftHorizontal"),
	RIGHT_HORIZONTAL	("rightHorizontal"),
	CENTRE_HORIZONTAL	("centreHorizontal"),
	GRID				("grid"),
	SEARCH				("search"),
	DROPDOWN			("dropdown");
	
	private String value = null;
	private ConfigLayoutType() {
		this(null);
	}
	private ConfigLayoutType(String value) {
		this.value = value;
	}
	public String getValue() {
		return this.value;
	}
	public static ConfigLayoutType fromValue(String value) {
		for (ConfigLayoutType filterDisplayType : values()) {
			if (filterDisplayType.value.equals(value)) {
				return filterDisplayType;
			}
		}
		return null;
	}
}
