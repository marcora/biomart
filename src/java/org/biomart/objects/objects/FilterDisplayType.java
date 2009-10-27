package org.biomart.objects.objects;



public enum FilterDisplayType {
		
	TEXTFIELD		("textfield"),	
	BOOLEAN			("boolean"),
	LIST			("list"),
	TREE			("tree");
	
	private String value = null;
	private FilterDisplayType() {
		this(null);
	}
	private FilterDisplayType(String value) {
		this.value = value;
	}
	public String getValue() {
		return this.value;
	}
	public static FilterDisplayType fromValue(String value) {
		for (FilterDisplayType filterDisplayType : values()) {
			if (filterDisplayType.value.equals(value)) {
				return filterDisplayType;
			}
		}
		return null;
	}
}
