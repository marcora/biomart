package org.biomart.transformation.tmp.backwardCompatibility.objects;


public enum TransformationFilterDisplayType {
		
	TEXTFIELD		("textfield"),	
	BOOLEAN			("boolean"),
	LIST			("list"),
	TREE			("tree"),
	
	LIST_CASCADE,
	LIST_BOOLEAN,
	LIST_TEXTFIELD	
	;
	
	private String value = null;
	private TransformationFilterDisplayType() {
		this(null);
	}
	private TransformationFilterDisplayType(String value) {
		this.value = value;
	}
	public String getValue() {
		return this.value;
	}
}
