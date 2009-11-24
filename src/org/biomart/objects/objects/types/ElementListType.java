package org.biomart.objects.objects.types;

import org.biomart.objects.objects.ElementList;

public enum ElementListType {
	
	IMPORTABLE		(ElementList.IMPORTABLE_XML_ELEMENT_NAME, true, true, false, false),
	EXPORTABLE		(ElementList.EXPORTABLE_XML_ELEMENT_NAME, true, false, true, false),
	FILTER_GROUP	(null, false, false, false, false),
	FILTER_CASCADE	(null, false, false, false, true );
	
	private String xmlElementName = null;
	private Boolean isPortable = null;
	private Boolean isImportable = null;
	private Boolean isExportable = null;
	private Boolean isSet = null;	// Whether repetitions are allowed
	
	private ElementListType(String xmlElementName, Boolean isPortable, Boolean isImportable, Boolean isExportable, Boolean isSet) {
		this.xmlElementName = xmlElementName;
		this.isPortable = isPortable;
		this.isImportable = isImportable;
		this.isExportable = isExportable;
		this.isSet = isSet;
	}
	public String getXmlElementName() {
		return this.xmlElementName;
	}
	public boolean isSet() {
		return this.isSet;
	}
	@Deprecated
	public boolean isPortable() {
		return this.isPortable;
	}
	public boolean isImportable() {
		return this.isImportable;
	}
	public boolean isExportable() {
		return this.isExportable;
	}
}
