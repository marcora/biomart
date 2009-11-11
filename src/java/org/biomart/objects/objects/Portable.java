package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorUtils;



public class Portable extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 4539635290976611423L;

	public static final String IMPORTABLE_XML_ELEMENT_NAME = "importable";
	public static final String EXPORTABLE_XML_ELEMENT_NAME = "exportable";
	
	public static void main(String[] args) {}
	
	protected Boolean isImportable = null;		// if not: exportable
	protected Range range = null;
	protected ElementList elementList = null;
	
	// For backward compatibility
	protected String formerLinkName = null;
	protected String formerLinkVersion = null;
	private Boolean formerDefault = null;	// Only mattered in the exportable

	// Internal use
	protected PartitionTable mainPartitionTable = null;
	protected List<PartitionTable> otherPartitionTableList = null;
		
	public Portable() {} 	// for Serialization
	public Portable(PartitionTable mainPartitionTable, String name, Boolean isImportable) {
		super(name, null, null, null, isImportable ? IMPORTABLE_XML_ELEMENT_NAME : EXPORTABLE_XML_ELEMENT_NAME);	// displayName, description & visible do not apply for that object
		
		this.isImportable = isImportable;
		
		this.range = new Range(mainPartitionTable, false);

		this.mainPartitionTable = mainPartitionTable;
		this.otherPartitionTableList = new ArrayList<PartitionTable>();
	
		this.elementList = new ElementList(); 
	}
	
	public boolean isImportable() {
		return this.isImportable;
	}
	
	public void addOtherPartitionTable(PartitionTable partitionTable) {
		MyUtils.checkStatusProgram(!this.otherPartitionTableList.contains(partitionTable));
		this.otherPartitionTableList.add(partitionTable);
	}

	public ElementList getElementList() {
		return this.elementList;
	}
	public Element getElement(String name) {
		return this.elementList.getElement(name);
	}

	public Boolean getFormerDefault() {
		return formerDefault;
	}

	public void setFormerDefault(Boolean formerDefault) {
		this.formerDefault = formerDefault;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"range = " + range + ", " + 
			"elementList = " + elementList + ", " + 
			"formerLinkName = " + formerLinkName + ", " + 
			"formerLinkVersion = " + formerLinkVersion + ", " + 
			"formerDefault = " + formerDefault;
	}

	public Range getRange() {
		return range;
	}

	public void setRange(Range range) {
		this.range = range;
	}

	public String getFormerLinkName() {
		return formerLinkName;
	}

	public void setFormerLinkName(String formerLinkName) {
		this.formerLinkName = formerLinkName;
	}

	public String getFormerLinkVersion() {
		return formerLinkVersion;
	}

	public void setFormerLinkVersion(String formerLinkVersion) {
		this.formerLinkVersion = formerLinkVersion;
	}
	
	public org.jdom.Element generateXml() {
		if (this.isImportable) {
			return generateXml(Filter.XML_ELEMENT_NAME);
		} else {
			return generateXml(Attribute.XML_ELEMENT_NAME);
		}
	}
	protected org.jdom.Element generateXml(String type) {
		org.jdom.Element element = super.generateXml();
		
		MartConfiguratorUtils.addAttribute(element, "formerLinkName", this.formerLinkName);
		MartConfiguratorUtils.addAttribute(element, "formerLinkVersion", this.formerLinkVersion);
		MartConfiguratorUtils.addAttribute(element, type + "s", (this.elementList!=null ? this.elementList.getXmlValue() : null));
		MartConfiguratorUtils.addAttribute(element, "formerDefault", this.formerDefault);
		this.range.addXmlAttribute(element, "range");
		
		return element;
	}

}


/*
package org.biomart.objects.objects;


import java.io.Serializable;

import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorUtils;



public class Exportable2 extends Portable implements Serializable {

	private static final long serialVersionUID = -6467892724196660537L;
	
	public static final String XML_ELEMENT_NAME = "exportable";
	public static final McNodeType MC_NODE_TYPE = null;
	
	public static void main(String[] args) {}
	
	// For backward compatibility
	private Boolean formerDefault = null;	// Only mattered in the exportable
	
	public Exportable2(PartitionTable mainPartitionTable, String name) {
		super(mainPartitionTable, name, false);	// displayName, description & visible do not apply for that object
	}

	public Boolean getFormerDefault() {
		return formerDefault;
	}

	public void setFormerDefault(Boolean formerDefault) {
		this.formerDefault = formerDefault;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"formerDefault = " + formerDefault;
	}
	
	public org.jdom.Element generateXml() {
		org.jdom.Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "formerDefault", this.formerDefault);
		return element;
	}
}
*/
/*
package org.biomart.objects.objects;


import java.io.Serializable;

import org.biomart.configurator.utils.type.McNodeType;



public class Importable2 extends Portable implements Serializable {

	private static final long serialVersionUID = -7990001822496911207L;
	
	public static final String XML_ELEMENT_NAME = "importable";
	public static final McNodeType MC_NODE_TYPE = null;
	
	public static void main(String[] args) {}

	public Importable2(PartitionTable mainPartitionTable, String name) {
		super(mainPartitionTable, name, true);	// displayName, description & visible do not apply for that object
	}
}
*/