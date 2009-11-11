package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorUtils;



public class Portable extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 4539635290976611423L;

	public static void main(String[] args) {}
	
	protected Range range = null;
	protected ElementList elementList = null;
	
	// For backward compatibility
	protected String formerLinkName = null;
	protected String formerLinkVersion = null;
	
	// Internal use
	protected PartitionTable mainPartitionTable = null;
	protected List<PartitionTable> otherPartitionTableList = null;
		
	public Portable() {} 	// for Serialization
	protected Portable(PartitionTable mainPartitionTable, String name, String xmlElementName) {
		super(name, null, null, null, xmlElementName);	// displayName, description & visible do not apply for that object
		
		this.range = new Range(mainPartitionTable, false);

		this.mainPartitionTable = mainPartitionTable;
		this.otherPartitionTableList = new ArrayList<PartitionTable>();
	
		this.elementList = new ElementList(); 
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

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"range = " + range + ", " + 
			"elementList = " + elementList + ", " + 
			"formerLinkName = " + formerLinkName + ", " + 
			"formerLinkVersion = " + formerLinkVersion;
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
		if (this instanceof Exportable) {
			return generateXml(Attribute.XML_ELEMENT_NAME);
		} else {
			return generateXml(Filter.XML_ELEMENT_NAME);
		}
	}
	protected org.jdom.Element generateXml(String type) {
		org.jdom.Element element = super.generateXml();
		
		MartConfiguratorUtils.addAttribute(element, "formerLinkName", this.formerLinkName);
		MartConfiguratorUtils.addAttribute(element, "formerLinkVersion", this.formerLinkVersion);
		this.range.addXmlAttribute(element, "range");
		MartConfiguratorUtils.addAttribute(element, type + "s", (this.elementList!=null ? this.elementList.getXmlValue() : null));
		
		return element;
	}

}
