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
public class TreeFilterData implements Serializable {	//TODO merge with ListFilterData

	private static final long serialVersionUID = 3532905052415360197L;

	public static void main(String[] args) {}

	private Filter filter = null;	// We'll see if we make this specific to a filter in particular
			
	private File dataFile = null;
	private LinkedHashMap<Part, ArrayList<TreeFilterDataRow>> map = null;
					// !!! sufficient for the transformation but will need to be much more sophisticated in reality
	
	public TreeFilterData(Filter filter) {
		super();
		this.filter = filter;
		this.dataFile = new File(filter.getDataFolderPath().getAbsolutePath() + MyUtils.FILE_SEPARATOR + filter.getName());
		this.map = new LinkedHashMap<Part, ArrayList<TreeFilterDataRow>>();
	}

	// Methods to access and populate the map "easily" 
	public ArrayList<TreeFilterDataRow> getPartValue(Part part) {
		return this.map.get(part);
	}
	public ArrayList<TreeFilterDataRow> addPart(Part part) throws FunctionalException {
		ArrayList<TreeFilterDataRow> partValue = getPartValue(part);
		if (partValue!=null) {
			throw new FunctionalException("Part " + partValue + " is already in the data");
		}
		partValue = new ArrayList<TreeFilterDataRow>();
		this.map.put(part, partValue);
		return partValue;
	}
	
	public Filter getFilter() {
		return filter;
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
		TreeFilterData dataFile=(TreeFilterData)object;
		return (
			(this.filter==dataFile.filter || (this.filter!=null && filter.equals(dataFile.filter)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		//hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==listFilter? 0 : listFilter.hashCode());
		return hash;
	}

	public File getDataFile() {
		return dataFile;
	}

	public void setDataFile(File dataFile) {
		this.dataFile = dataFile;
	}
	
	public void writeFile() throws TechnicalException {
		
		// Generate the elements
		Element rootElement = generateXml(false);
		
		// Write the file
		MyUtils.writeXmlFile(rootElement, this.dataFile.getAbsolutePath());
	}
	
	public Element generateXml(boolean flatten) {
		Element rootElement = new Element("treeData");
		for (Iterator<Part> it = this.map.keySet().iterator(); it.hasNext();) {
			Part part = it.next();
			
			Element partElement = new Element("part");
			partElement.setAttribute("name", part.getXmlValue(flatten));
			rootElement.addContent(partElement);
			
			ArrayList<TreeFilterDataRow> children = this.map.get(part);
			for (TreeFilterDataRow treeFilterDataRow : children) {
				Element treeFilterDataRowElement = treeFilterDataRow.generateXml();
				partElement.addContent(treeFilterDataRowElement);
			}
		}
		return rootElement;
	}
}
