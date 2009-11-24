package org.biomart.martRemote.enums;


public enum MartServiceFormat {
	XML		("xml"),
	JSON	("json");
	
	private String value = null;
	private MartServiceFormat(String value) {
		this.value = value;
	}
	public String getValue() {
		return this.value;
	}
	public static MartServiceFormat getFormat(String value)  {
		for (MartServiceFormat format : MartServiceFormat.values()) {
			if (format.getValue().equals(value)) {
				return format;
			}
		}
		return null;
	}
	
	public boolean isXml() {
		return MartServiceFormat.XML.equals(this);
	}
	public boolean isJson() {
		return MartServiceFormat.JSON.equals(this);
	}
}
