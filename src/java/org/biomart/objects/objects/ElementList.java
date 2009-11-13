package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.objects.types.ElementListType;


public class ElementList extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 464427342130171787L;
		
	public static void main(String[] args) {}
	
	public static final String IMPORTABLE_XML_ELEMENT_NAME = "importable";
	public static final String EXPORTABLE_XML_ELEMENT_NAME = "exportable";
	
	private List<Element> elements = null;
	private List<String> elementNames = null;
	private Range range = null;
	
	private ElementListType type = null;
	
	// For backward compatibility
	private String formerLinkName = null;
	private String formerLinkVersion = null;
	private Boolean formerDefault = null;	// Only mattered in the exportable
	
	// Internal use
	protected PartitionTable mainPartitionTable = null;
	protected List<PartitionTable> otherPartitionTableList = null;
	
	public ElementList(ElementListType type) {
		this(type, null, null);
	}
	public ElementList(ElementListType type, String name, PartitionTable mainPartitionTable) {
		super(name, null, null, null, type.getXmlElementName());	// displayName, description & visible do not apply for that object
		
		this.elements = new ArrayList<Element>();
		this.elementNames = new ArrayList<String>();
		this.type = type;
		
		if (null!=mainPartitionTable) {
			this.range = new Range(mainPartitionTable, false);
			this.mainPartitionTable = mainPartitionTable;
			this.otherPartitionTableList = new ArrayList<PartitionTable>();
		}
	}
	
	public void addElements(Collection<? extends Element> collection) {
		for (Element element : collection) {
			addElement(element);
		}
	}
	
	public void addElement(Element element) {
		if (!this.type.isSet() || !elements.contains(element)) {
			this.elements.add(element);
			this.elementNames.add(element.getName());
		}
	}
	
	public Element getElement(String name) {
		for (Element element : this.elements) {
			if (name.equals(element.name)) {
				return element;
			}
		}
		return null;
	}
	
	public List<String> getElementNames() {
		return new ArrayList<String>(this.elementNames);
	}
	
	public List<Element> getElements() {
		return this.elements;
	}
	
	public int getSize() {
		return this.elements.size();
	}
	
	public void addOtherPartitionTable(PartitionTable partitionTable) {
		MyUtils.checkStatusProgram(!this.otherPartitionTableList.contains(partitionTable));
		this.otherPartitionTableList.add(partitionTable);
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
	
	public Boolean getFormerDefault() {
		return formerDefault;
	}

	public void setFormerDefault(Boolean formerDefault) {
		this.formerDefault = formerDefault;
	}
	
	public String getStringValue() {
		return MartConfiguratorUtils.collectionToCommaSeparatedString(this.elementNames);
	}
	public org.jdom.Element generateXml() throws FunctionalException {
		if (this.type.isImportable()) {
			return generateXml(Filter.XML_ELEMENT_NAME + "s");
		} else if (this.type.isExportable()) {
			return generateXml(Attribute.XML_ELEMENT_NAME + "s");
		} else {
			throw new FunctionalException(
					"Unsupported operation, only importables and exportables can be generate an XML element");
		}
	}
	private org.jdom.Element generateXml(String listName) throws FunctionalException {
		org.jdom.Element element = super.generateXml();
		
		MartConfiguratorUtils.addAttribute(element, listName, this.getStringValue());
		MartConfiguratorUtils.addAttribute(element, "formerLinkName", this.formerLinkName);
		MartConfiguratorUtils.addAttribute(element, "formerLinkVersion", this.formerLinkVersion);
		if (this.type.isExportable()) {
			MartConfiguratorUtils.addAttribute(element, "formerDefault", this.formerDefault);	
		}
		this.range.getStringValue(element, "range");
		
		return element;
	}

	public List<Range> computeRangeList() {
		List<Range> rangeList = new ArrayList<Range>();
		boolean needIntersection = false;	//TODO?
		for (Element element : this.elements) {
			Range targetRange = element.getTargetRange();
			rangeList.add(targetRange);
			if (rangeList.size()>1 && targetRange.getPartitionTableSet().size()>1) {
				System.out.println(targetRange.getXmlValue());
				needIntersection = true;
			}
		}
		if (needIntersection) {
			for (Element element : this.elements) {
				try {
					System.out.println(MartConfiguratorUtils.displayJdomElement(element.generateXml()));
				} catch (FunctionalException e) {
					e.printStackTrace();
				}
			}
		}
		return rangeList;
	}

	@Override
	public String toString() {
		return super.toString() + ", " + 
		"type = " + type + ", " +
		"range = " + range + ", " + 
		"elementNames = " + elementNames + ", " + 
		"formerLinkName = " + formerLinkName + ", " + 
		"formerLinkVersion = " + formerLinkVersion + ", " + 
		"formerDefault = " + formerDefault;
	}
}
