package org.biomart.objects.data;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.XmlUtils;
import org.biomart.martRemote.Jsoml;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.Part;
import org.jdom.Element;

/**
 * !!! TODO sufficient for the transformation but will need to be much more sophisticated in reality
 */
public class FilterData implements Serializable {

	private static final long serialVersionUID = 1316941933451308039L;
	
	public static final String XML_ELEMENT_NAME = "data";

	public static void main(String[] args) {}

	private Filter filter = null;	// We'll see if we make this specific to a filter in particular
			
	private Boolean template = null;	// template if all parts have the same data -> [P?R*]
	//TODO add checks that if template, can't add more than one part, add the template onsetting inside the addPart method (by checking the main PT row)
	
	private File dataFile = null;
	private LinkedHashMap<Part, LinkedHashMap<FilterDataRow, LinkedHashMap<Filter, ArrayList<FilterDataRow>>>> map = null;
					// !!! sufficient for the transformation but will need to be much more sophisticated in reality
	
	public FilterData(Filter filter) {
		super();
		this.filter = filter;
		this.dataFile = new File(filter.getDataFolderPath().getAbsolutePath() + MyUtils.FILE_SEPARATOR + filter.getName());
		this.map = new LinkedHashMap<Part, LinkedHashMap<FilterDataRow, LinkedHashMap<Filter,ArrayList<FilterDataRow>>>>();
		this.template = false;	// unless specified otherwise later
	}
	
	public void setToTemplate() {
		this.template = true; 
	}

	// Methods to access and populate the map "easily" 
	public LinkedHashMap<FilterDataRow, LinkedHashMap<Filter, ArrayList<FilterDataRow>>> getPartValue(Part part) {
		return this.map.get(part);
	}
	public LinkedHashMap<FilterDataRow, LinkedHashMap<Filter, ArrayList<FilterDataRow>>> addPart(Part part) throws FunctionalException {
		LinkedHashMap<FilterDataRow, LinkedHashMap<Filter, ArrayList<FilterDataRow>>> partValue = getPartValue(part);
		if (partValue!=null) {
			throw new FunctionalException("Part " + partValue + " is already in the data");
		}
		partValue = new LinkedHashMap<FilterDataRow, LinkedHashMap<Filter,ArrayList<FilterDataRow>>>();
		this.map.put(part, partValue);
		return partValue;
	}
	public LinkedHashMap<Filter,ArrayList<FilterDataRow>> getRowForPartValue(Part part, FilterDataRow dataRow) throws FunctionalException {
		LinkedHashMap<FilterDataRow, LinkedHashMap<Filter, ArrayList<FilterDataRow>>> partValue = getPartValue(part);
		if (null==partValue) {
			throw new FunctionalException("No row " + dataRow + " for part " + part);
		}
		return partValue.get(dataRow);
	}
	public LinkedHashMap<Filter,ArrayList<FilterDataRow>> addRowForPart(Part part, FilterDataRow dataRow) throws FunctionalException {
		LinkedHashMap<Filter,ArrayList<FilterDataRow>> rowForPartValue = getRowForPartValue(part, dataRow);
		if (rowForPartValue!=null) {
			throw new FunctionalException("Row " + XmlUtils.displayJdomElement(dataRow.generateXml()) + 
					" for part " + part.getXmlValue() + " is already in the data");
		}
		rowForPartValue = new LinkedHashMap<Filter, ArrayList<FilterDataRow>>();
		
		LinkedHashMap<FilterDataRow, LinkedHashMap<Filter, ArrayList<FilterDataRow>>> partMap = getPartValue(part);
		partMap.put(dataRow, rowForPartValue);
		return rowForPartValue;
	}
	public ArrayList<FilterDataRow> getCascadeChildForRowAndPartValue(
			Part part, FilterDataRow dataRow, Filter cascadeChild) throws FunctionalException {
		LinkedHashMap<Filter, ArrayList<FilterDataRow>> rowForPartValue = getRowForPartValue(part, dataRow);
		if (null==rowForPartValue) {
			throw new FunctionalException("No cascade child " + cascadeChild.getName() + " for row " + dataRow + " and part " + part);
		}
		return rowForPartValue.get(cascadeChild);
	}
	
	public ArrayList<FilterDataRow> addCascadeChildForRowAndPart(
			Part part, FilterDataRow dataRow, Filter cascadeChild) throws FunctionalException {
		ArrayList<FilterDataRow> cascadeChildForRowAndPartMapValue = 
			getCascadeChildForRowAndPartValue(part, dataRow, cascadeChild);
		if (cascadeChildForRowAndPartMapValue!=null) {
			throw new FunctionalException(
					"cascade child " + cascadeChild.getName() + " for row " + dataRow + " and part " + part + " is already in the data");
		}
		cascadeChildForRowAndPartMapValue = new ArrayList<FilterDataRow>();
		
		LinkedHashMap<Filter, ArrayList<FilterDataRow>> rowForPartValue = getRowForPartValue(part, dataRow);
		rowForPartValue.put(cascadeChild, cascadeChildForRowAndPartMapValue);
		return cascadeChildForRowAndPartMapValue;
	}
	public void removeCascadeChildForRowAndPart(
			Part part, FilterDataRow dataRow, Filter cascadeChild) throws FunctionalException {
		ArrayList<FilterDataRow> cascadeChildForRowAndPartMapValue = 
			getCascadeChildForRowAndPartValue(part, dataRow, cascadeChild);
		if (cascadeChildForRowAndPartMapValue==null) {
			throw new FunctionalException(
					"cascade child " + cascadeChild.getName() + " for row " + dataRow + " and part " + part + " is not in the data");
		}
		
		LinkedHashMap<Filter, ArrayList<FilterDataRow>> rowForPartValue = getRowForPartValue(part, dataRow);
		rowForPartValue.remove(cascadeChild);
	}
	public FilterDataRow getRowForCascadeChildAndRowAndPart(
			Part part, FilterDataRow dataRow, Filter cascadeChild, FilterDataRow subDataRow) throws FunctionalException {
		ArrayList<FilterDataRow> cascadeChildForRowAndPartValue = getCascadeChildForRowAndPartValue(part, dataRow, cascadeChild);
		if (null==cascadeChildForRowAndPartValue) {
			throw new FunctionalException("No row " + subDataRow + " for cascade child " + cascadeChild.getName() + " and row " + dataRow + " and part " + part);
		}
		int index = cascadeChildForRowAndPartValue.indexOf(subDataRow);
		return index!=-1 ? cascadeChildForRowAndPartValue.get(index) : null;
	}
	public void addRowForCascadeChildAndRowAndPart(
			Part part, FilterDataRow dataRow, Filter cascadeChild, FilterDataRow subDataRow) throws FunctionalException {
		
		FilterDataRow rowForCascadeChildAndRowAndPart = getRowForCascadeChildAndRowAndPart(part, dataRow, cascadeChild, subDataRow);
		if (rowForCascadeChildAndRowAndPart!=null) {
			throw new FunctionalException(
					"subDataRow " + subDataRow + " for cascade child " + cascadeChild.getName() + 
					" for row " + dataRow + " and part " + part + " is already in the data");
		}
	
		ArrayList<FilterDataRow> cascadeChildForRowAndPartValue = getCascadeChildForRowAndPartValue(part, dataRow, cascadeChild);
		cascadeChildForRowAndPartValue.add(subDataRow);		
	}
	
	public Filter getFilter() {
		return filter;
	}

	public void setMap(
			LinkedHashMap<Part, LinkedHashMap<FilterDataRow, LinkedHashMap<Filter, ArrayList<FilterDataRow>>>> map) {
		this.map = map;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"filter = " + (filter!=null ? filter.getName() : null);
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		FilterData dataFile=(FilterData)object;
		return (
			(this.filter==dataFile.filter || (this.filter!=null && filter.equals(dataFile.filter)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		//hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==filter? 0 : filter.hashCode());
		return hash;
	}

	public LinkedHashMap<Part, LinkedHashMap<FilterDataRow, LinkedHashMap<Filter, ArrayList<FilterDataRow>>>> getMap() {
		return map;
	}

	public File getDataFile() {
		return dataFile;
	}

	public void setDataFile(File dataFile) {
		this.dataFile = dataFile;
	}
	
	public void writeFile() throws TechnicalException {
		String dataFilePathAndName = this.dataFile.getAbsolutePath();
		writeFile(dataFilePathAndName);
	}
	
	public void writeFile(String dataFilePathAndName) throws TechnicalException {
	
		// Generate the elements
		Element rootElement = generateXml(false);
				
		// Write the file
		XmlUtils.writeXmlFile(rootElement, dataFilePathAndName);
	}
		
	/*public Element generateXml(boolean flatten) {
		Element rootElement = new Element(XML_ELEMENT_NAME);
		for (Iterator<Part> it = this.map.keySet().iterator(); it.hasNext();) {
			Part part = it.next();
			
			Element partElement = new Element(MartConfiguratorConstants.XML_ELEMENT_PART);
			String partName = part.getXmlValue(flatten);
			partElement.setAttribute(MartConfiguratorConstants.XML_ELEMENT_ATTRIBUTE_PART_NAME, partName);
			rootElement.addContent(partElement);
			
			LinkedHashMap<filterDataRow, LinkedHashMap<Filter, ArrayList<filterDataRow>>> subMap = this.map.get(part);
			for (Iterator<filterDataRow> it2 = subMap.keySet().iterator(); it2.hasNext();) {
				filterDataRow filterDataRow = it2.next();
				
				Element filterDataRowElement = filterDataRow.generateXml();
				partElement.addContent(filterDataRowElement);
				
				LinkedHashMap<Filter, ArrayList<filterDataRow>> subSubMap = subMap.get(filterDataRow);
				for (Iterator<Filter> it3 = subSubMap.keySet().iterator(); it3.hasNext();) {
					Filter cascadeChild = it3.next();
					
					Element cascadeChildElement = new Element(MartConfiguratorConstants.XML_ELEMENT_CASCADE_CHILD);
					cascadeChildElement.setAttribute(MartConfiguratorConstants.XML_ELEMENT_ATTRIBUTE_PART_NAME, cascadeChild.getName());
					filterDataRowElement.addContent(cascadeChildElement);
					
					ArrayList<filterDataRow> filterDataRowList = subSubMap.get(cascadeChild);
					for (filterDataRow subFilterDataRow : filterDataRowList) {
						Element subFilterDataRowElement = subFilterDataRow.generateXml();
						cascadeChildElement.addContent(subFilterDataRowElement);
					}
				}
			}
		}
		return rootElement;
	}*/
	
	// ===================================== Should be a different class? ============================================

	@SuppressWarnings("unused")
	private String filterName = null;
	public FilterData(FilterData filterData, Part part) throws FunctionalException {	// creates a light clone (temporary solution)
		this.map = new LinkedHashMap<Part, LinkedHashMap<FilterDataRow,LinkedHashMap<Filter,ArrayList<FilterDataRow>>>>();
		
		if (filterData.template) {
			if (filterData.map.keySet().size()!=1) {
				throw new FunctionalException(
						"Unexpected filterData structure for a null part, size = " + filterData.map.keySet().size() + " (expected 1)");
			}
			part = filterData.map.keySet().iterator().next();
		}
		
		for (Part filterDataPart : filterData.map.keySet()) {
			if (filterDataPart.getMainRowNumber()==part.getMainRowNumber()) {
				LinkedHashMap<FilterDataRow, LinkedHashMap<Filter, ArrayList<FilterDataRow>>> value = filterData.map.get(filterDataPart);
				Part partClone = new Part(filterDataPart);
				this.map.put(partClone, value);
						// not a full clone: wait until FilterData is fully fleshed out TODO
			}
		}
		
		this.filterName = filterData.filter.getName();
	}
	public boolean hasData() {
		return this.map.keySet().size()>0;
	}

	public Element generateXml(boolean flatten) {
		return generateExchangeFormat(true, flatten).getXmlElement();
	}
	public JSONObject generateJson(boolean flatten) {
		return generateExchangeFormat(false, flatten).getJsonObject();
	}
	public Jsoml generateExchangeFormat(boolean xml, boolean flatten) {
		Jsoml rootElement = new Jsoml(xml, XML_ELEMENT_NAME);
		for (Iterator<Part> it = this.map.keySet().iterator(); it.hasNext();) {
			Part part = it.next();
			
			Jsoml partElement = new Jsoml(xml, MartConfiguratorConstants.XML_ELEMENT_PART);
			String partName = part.getXmlValue(flatten);
			partElement.setAttribute(MartConfiguratorConstants.XML_ELEMENT_ATTRIBUTE_PART_NAME, partName);
			
			LinkedHashMap<FilterDataRow, LinkedHashMap<Filter, ArrayList<FilterDataRow>>> subMap = this.map.get(part);
			for (Iterator<FilterDataRow> it2 = subMap.keySet().iterator(); it2.hasNext();) {
				FilterDataRow filterDataRow = it2.next();
				
				Jsoml filterDataRowElement = filterDataRow.generateExchangeFormat(xml);
				
				LinkedHashMap<Filter, ArrayList<FilterDataRow>> subSubMap = subMap.get(filterDataRow);			
				for (Iterator<Filter> it3 = subSubMap.keySet().iterator(); it3.hasNext();) {
					Filter cascadeChild = it3.next();
					
					Jsoml cascadeChildElement = new Jsoml(xml, MartConfiguratorConstants.XML_ELEMENT_CASCADE_CHILD);
					cascadeChildElement.setAttribute(MartConfiguratorConstants.XML_ELEMENT_ATTRIBUTE_PART_NAME, cascadeChild.getName());
					
					ArrayList<FilterDataRow> filterDataRowList = subSubMap.get(cascadeChild);
					for (FilterDataRow subFilterDataRow : filterDataRowList) {
						Jsoml subFilterDataRowElement = subFilterDataRow.generateExchangeFormat(xml);
						cascadeChildElement.addContent(subFilterDataRowElement);
					}
					filterDataRowElement.addContent(cascadeChildElement);
				}
				partElement.addContent(filterDataRowElement);
			}
			rootElement.addContent(partElement);
		}
		return rootElement;
	}
}
