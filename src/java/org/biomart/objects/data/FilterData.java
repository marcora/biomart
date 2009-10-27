package org.biomart.objects.data;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.Part;
import org.jdom.Element;

/**
 * !!! TODO sufficient for the transformation but will need to be much more sophisticated in reality
 */
public class FilterData implements Serializable {

	private static final long serialVersionUID = 1316941933451308039L;

	public static void main(String[] args) {}

	private Filter filter = null;	// We'll see if we make this specific to a filter in particular
			
	private File dataFile = null;
	private LinkedHashMap<Part, LinkedHashMap<filterDataRow, LinkedHashMap<Filter, ArrayList<filterDataRow>>>> map = null;
					// !!! sufficient for the transformation but will need to be much more sophisticated in reality
	
	public FilterData(Filter filter) {
		super();
		this.filter = filter;
		this.dataFile = new File(filter.getDataFolderPath().getAbsolutePath() + MyUtils.FILE_SEPARATOR + filter.getName());
		this.map = new LinkedHashMap<Part, LinkedHashMap<filterDataRow, LinkedHashMap<Filter,ArrayList<filterDataRow>>>>();
	}

	// Methods to access and populate the map "easily" 
	public LinkedHashMap<filterDataRow, LinkedHashMap<Filter, ArrayList<filterDataRow>>> getPartValue(Part part) {
		return this.map.get(part);
	}
	public LinkedHashMap<filterDataRow, LinkedHashMap<Filter, ArrayList<filterDataRow>>> addPart(Part part) throws FunctionalException {
		LinkedHashMap<filterDataRow, LinkedHashMap<Filter, ArrayList<filterDataRow>>> partValue = getPartValue(part);
		if (partValue!=null) {
			throw new FunctionalException("Part " + partValue + " is already in the data");
		}
		partValue = new LinkedHashMap<filterDataRow, LinkedHashMap<Filter,ArrayList<filterDataRow>>>();
		this.map.put(part, partValue);
		return partValue;
	}
	public LinkedHashMap<Filter,ArrayList<filterDataRow>> getRowForPartValue(Part part, filterDataRow dataRow) throws FunctionalException {
		LinkedHashMap<filterDataRow, LinkedHashMap<Filter, ArrayList<filterDataRow>>> partValue = getPartValue(part);
		if (null==partValue) {
			throw new FunctionalException("No row " + dataRow + " for part " + part);
		}
		return partValue.get(dataRow);
	}
	public LinkedHashMap<Filter,ArrayList<filterDataRow>> addRowForPart(Part part, filterDataRow dataRow) throws FunctionalException {
		LinkedHashMap<Filter,ArrayList<filterDataRow>> rowForPartValue = getRowForPartValue(part, dataRow);
		if (rowForPartValue!=null) {
			/*throw new FunctionalException("Row " + MartConfiguratorUtils.displayJdomElement(dataRow.generateXml()) + 
					" for part " + part.getXmlValue() + " is already in the data");*/
		}
		rowForPartValue = new LinkedHashMap<Filter, ArrayList<filterDataRow>>();
		
		LinkedHashMap<filterDataRow, LinkedHashMap<Filter, ArrayList<filterDataRow>>> partMap = getPartValue(part);
		partMap.put(dataRow, rowForPartValue);
		return rowForPartValue;
	}
	public ArrayList<filterDataRow> getCascadeChildForRowAndPartValue(
			Part part, filterDataRow dataRow, Filter cascadeChild) throws FunctionalException {
		LinkedHashMap<Filter, ArrayList<filterDataRow>> rowForPartValue = getRowForPartValue(part, dataRow);
		if (null==rowForPartValue) {
			throw new FunctionalException("No cascade child " + cascadeChild.getName() + " for row " + dataRow + " and part " + part);
		}
		return rowForPartValue.get(cascadeChild);
	}
	
	/*this.dataFile = new File("/home/anthony/Desktop/debug.xml");
	try {
		writeFile();
	} catch (TechnicalException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	
	public ArrayList<filterDataRow> addCascadeChildForRowAndPart(
			Part part, filterDataRow dataRow, Filter cascadeChild) throws FunctionalException {
		ArrayList<filterDataRow> cascadeChildForRowAndPartMapValue = 
			getCascadeChildForRowAndPartValue(part, dataRow, cascadeChild);
		if (cascadeChildForRowAndPartMapValue!=null) {
			throw new FunctionalException(
					"cascade child " + cascadeChild.getName() + " for row " + dataRow + " and part " + part + " is already in the data");
		}
		cascadeChildForRowAndPartMapValue = new ArrayList<filterDataRow>();
		
		LinkedHashMap<Filter, ArrayList<filterDataRow>> rowForPartValue = getRowForPartValue(part, dataRow);
		rowForPartValue.put(cascadeChild, cascadeChildForRowAndPartMapValue);
		return cascadeChildForRowAndPartMapValue;
	}
	public void removeCascadeChildForRowAndPart(
			Part part, filterDataRow dataRow, Filter cascadeChild) throws FunctionalException {
		ArrayList<filterDataRow> cascadeChildForRowAndPartMapValue = 
			getCascadeChildForRowAndPartValue(part, dataRow, cascadeChild);
		if (cascadeChildForRowAndPartMapValue==null) {
			throw new FunctionalException(
					"cascade child " + cascadeChild.getName() + " for row " + dataRow + " and part " + part + " is not in the data");
		}
		
		LinkedHashMap<Filter, ArrayList<filterDataRow>> rowForPartValue = getRowForPartValue(part, dataRow);
		rowForPartValue.remove(cascadeChild);
	}
	public filterDataRow getRowForCascadeChildAndRowAndPart(
			Part part, filterDataRow dataRow, Filter cascadeChild, filterDataRow subDataRow) throws FunctionalException {
		ArrayList<filterDataRow> cascadeChildForRowAndPartValue = getCascadeChildForRowAndPartValue(part, dataRow, cascadeChild);
		if (null==cascadeChildForRowAndPartValue) {
			throw new FunctionalException("No row " + subDataRow + " for cascade child " + cascadeChild.getName() + " and row " + dataRow + " and part " + part);
		}
		int index = cascadeChildForRowAndPartValue.indexOf(subDataRow);
		return index!=-1 ? cascadeChildForRowAndPartValue.get(index) : null;
	}
	public void addRowForCascadeChildAndRowAndPart(
			Part part, filterDataRow dataRow, Filter cascadeChild, filterDataRow subDataRow) throws FunctionalException {
		
		filterDataRow rowForCascadeChildAndRowAndPart = getRowForCascadeChildAndRowAndPart(part, dataRow, cascadeChild, subDataRow);
		if (rowForCascadeChildAndRowAndPart!=null) {
			throw new FunctionalException(
					"subDataRow " + subDataRow + " for cascade child " + cascadeChild.getName() + 
					" for row " + dataRow + " and part " + part + " is already in the data");
		}
	
		ArrayList<filterDataRow> cascadeChildForRowAndPartValue = getCascadeChildForRowAndPartValue(part, dataRow, cascadeChild);
		cascadeChildForRowAndPartValue.add(subDataRow);		
	}
	
	public Filter getFilter() {
		return filter;
	}

	public void setMap(
			LinkedHashMap<Part, LinkedHashMap<filterDataRow, LinkedHashMap<Filter, ArrayList<filterDataRow>>>> map) {
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

	public LinkedHashMap<Part, LinkedHashMap<filterDataRow, LinkedHashMap<Filter, ArrayList<filterDataRow>>>> getMap() {
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
		Element rootElement = generateXml();
				
		// Write the file
		MyUtils.writeXmlFile(rootElement, dataFilePathAndName);
	}

	public Element generateXml() {
		Element rootElement = new Element("root");
		for (Iterator<Part> it = this.map.keySet().iterator(); it.hasNext();) {
			Part part = it.next();
			
			Element partElement = new Element("part");
			partElement.setAttribute("name", part.getXmlValue());
			rootElement.addContent(partElement);
			
			LinkedHashMap<filterDataRow, LinkedHashMap<Filter, ArrayList<filterDataRow>>> subMap = this.map.get(part);
			for (Iterator<filterDataRow> it2 = subMap.keySet().iterator(); it2.hasNext();) {
				filterDataRow filterDataRow = it2.next();
				
				Element filterDataRowElement = filterDataRow.generateXml();
				partElement.addContent(filterDataRowElement);
				
				LinkedHashMap<Filter, ArrayList<filterDataRow>> subSubMap = subMap.get(filterDataRow);
				for (Iterator<Filter> it3 = subSubMap.keySet().iterator(); it3.hasNext();) {
					Filter cascadeChild = it3.next();
					
					Element cascadeChildElement = new Element("cascadeChild");
					cascadeChildElement.setAttribute("name", cascadeChild.getName());
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
	}

	/*@Override
	public int compare(DataFile dataFile1, DataFile dataFile2) {
		if (dataFile1==null && dataFile2!=null) {
			return -1;
		} else if (dataFile1!=null && dataFile2==null) {
			return 1;
		}
		return CompareUtils.compareNull(dataFile1.filter, dataFile2.filter);
	}

	@Override
	public int compareTo(DataFile dataFile) {
		return compare(this, dataFile);
	}*/

}
