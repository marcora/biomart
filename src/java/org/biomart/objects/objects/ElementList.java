package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.biomart.objects.MartConfiguratorUtils;


public class ElementList implements Serializable {

	private static final long serialVersionUID = 464427342130171787L;
		
	public static void main(String[] args) {}

	private Boolean set = null;			// Whether repetitions are allowed
	private List<Element> elements = null;
	private List<String> elementNames = null;

	public ElementList() {
		this(false);
	}
	public ElementList(Boolean set) {
		this.elements = new ArrayList<Element>();
		this.elementNames = new ArrayList<String>();
		this.set = set;
	}
	
	public void addElements(Collection<? extends Element> collection) {
		for (Element element : collection) {
			addElement(element);
		}
	}
	
	public void addElement(Element element) {
		if (!this.set || !elements.contains(element)) {
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
	
	public String getStringValue() {
		return MartConfiguratorUtils.collectionToCommaSeparatedString(this.elementNames);
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
				System.out.println(MartConfiguratorUtils.displayJdomElement(element.generateXml()));
			}
		}
		return rangeList;
	}

	@Override
	public String toString() {
		return "elementNames = " + elementNames;
	}
}
