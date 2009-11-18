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
public class TreeFilterData implements Serializable {	//TODO merge with ListFilterData

	private static final long serialVersionUID = 3532905052415360197L;

	public static void main(String[] args) {}

	private Filter filter = null;	// We'll see if we make this specific to a filter in particular
			
	private Boolean template = null;	// template if all parts have the same data -> [P?R*]
	//TODO add checks that if template, can't add more than one part, add the template onsetting inside the addPart method (by checking the main PT row)
	
	private File dataFile = null;
	private LinkedHashMap<Part, ArrayList<TreeFilterDataRow>> map = null;
					// !!! sufficient for the transformation but will need to be much more sophisticated in reality
	
	public TreeFilterData(Filter filter) {
		super();
		this.filter = filter;
		this.dataFile = new File(filter.getDataFolderPath().getAbsolutePath() + MyUtils.FILE_SEPARATOR + filter.getName());
		this.map = new LinkedHashMap<Part, ArrayList<TreeFilterDataRow>>();
		this.template = false;	// unless specified otherwise later
	}
	
	public void setToTemplate() {
		this.template = true; 
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
		XmlUtils.writeXmlFile(rootElement, this.dataFile.getAbsolutePath());
	}
	
	
	// ===================================== Should be a different class ============================================

	@SuppressWarnings("unused")
	private String filterName = null;
	public TreeFilterData(TreeFilterData treeFilterData, Part part) throws FunctionalException {	// creates a light clone (temporary solution)
		this.map = new LinkedHashMap<Part, ArrayList<TreeFilterDataRow>>();
		
		if (treeFilterData.template) {
			if (treeFilterData.map.keySet().size()!=1) {
				throw new FunctionalException(
						"Unexpected filterData structure for a null part, size = " + treeFilterData.map.keySet().size() + " (expected 1)");
			}
			part = treeFilterData.map.keySet().iterator().next();
		}
		
		for (Part filterDataPart : treeFilterData.map.keySet()) {
			if (filterDataPart.getMainRowNumber()==part.getMainRowNumber()) {
				ArrayList<TreeFilterDataRow> value = treeFilterData.map.get(filterDataPart);
				Part partClone = new Part(filterDataPart);
				this.map.put(partClone, value);
						// not a full clone: wait until FilterData is fully fleshed out TODO
			}
		}
		
		this.filterName = treeFilterData.filter.getName();
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
		Jsoml rootElement = new Jsoml(xml, "treeData");
		for (Iterator<Part> it = this.map.keySet().iterator(); it.hasNext();) {
			Part part = it.next();
			
			Jsoml partElement = new Jsoml(xml, "part");
			partElement.setAttribute("name", part.getXmlValue(flatten));
			
			ArrayList<TreeFilterDataRow> children = this.map.get(part);
			for (TreeFilterDataRow treeFilterDataRow : children) {
				Jsoml treeFilterDataRowElement = treeFilterDataRow.generateExchangeFormat(xml);
				partElement.addContent(treeFilterDataRowElement);
			}
			rootElement.addContent(partElement);
		}
		return rootElement;
	}
	/*public Element generateXml(boolean flatten) {
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
	}*/
}
