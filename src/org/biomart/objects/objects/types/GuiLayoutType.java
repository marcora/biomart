package org.biomart.objects.objects.types;


public enum GuiLayoutType {
	MART_SEARCH	("martSearch"),	
	MART_VIEW	("martView"),
	WIZARD		("wizard");
	
	private String value = null;
	private GuiLayoutType() {
		this(null);
	}
	private GuiLayoutType(String value) {
		this.value = value;
	}
	public String getValue() {
		return this.value;
	}
	public static GuiLayoutType fromValue(String value) {
		for (GuiLayoutType filterDisplayType : values()) {
			if (filterDisplayType.value.equals(value)) {
				return filterDisplayType;
			}
		}
		return null;
	}
}
