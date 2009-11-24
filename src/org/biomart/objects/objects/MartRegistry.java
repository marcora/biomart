package org.biomart.objects.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biomart.common.general.exceptions.FunctionalException;
import org.jdom.Document;
import org.jdom.Element;


public class MartRegistry extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 4555425904982314129L;
	
	public static final String XML_ELEMENT_NAME = "martRegistry";
	
	public static void main(String[] args) {}
	
	private List<Location> locationList = null;
	private Map<String, Dataset> nameToDatasetMap = null;	// dataset names being unique within a portal
	
	public MartRegistry() {
		super();
		this.locationList = new ArrayList<Location>();
	}

	public void addLocation(Location location) {
		this.locationList.add(location);
	}
	public List<Location> getLocationList() {
		return new ArrayList<Location>(this.locationList);
	}	
	public Location getLocation(String name) {
		return (Location)super.getMartConfiguratorObjectByName(this.locationList, name);
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " +
			"locationList.size() = " + this.locationList.size();
	}

	public void merge (MartRegistry martRegistry) {
		List<Location> locationList1 = this.getLocationList();
		List<Location> locationList2 = martRegistry.getLocationList();
		for (Location location2 : locationList2) {
			int index = locationList1.indexOf(location2);
			if (index==-1) {
				this.addLocation(location2);
			} else {	// They are the same, but their content may be different
				Location location1 = locationList1.get(index);
				location1.merge(location2);
			}
		}
	}
	
	/**
	 * to be called before trying to get a dataset from name TODO should be updated in addDataset from Mart class instead
	 */
	public void updateNameToDatasetMap() throws FunctionalException {
		this.nameToDatasetMap = new HashMap<String, Dataset>();
		for (Location location : this.getLocationList()) {
			for (Mart mart : location.getMartList()) {
				for (Dataset dataset : mart.getDatasetList()) {
					if (null!=this.nameToDatasetMap.get(dataset.getName())) {
						throw new FunctionalException("Dataset name conflict on: " + dataset.getName());
					}
					this.nameToDatasetMap.put(dataset.getName(), dataset);
				}
			}
		}
	}
	/**
	 * updateNameToDatasetMap should be called prior to using this method TODO better
	 */
	public Dataset getDataset(String datasetName) throws FunctionalException {
		if (null==this.nameToDatasetMap) {	// TODO better
			throw new FunctionalException("Map not up to date");
		}
		return this.nameToDatasetMap.get(datasetName);
	}
	
	
	public Document generateXmlDocument() throws FunctionalException {
		return new Document(this.generateXml());
	}
	public Element generateXml() throws FunctionalException {
		Element element = new Element(XML_ELEMENT_NAME);
		
		for (Location location : this.locationList) {
			element.addContent(location.generateXml());
		}
		
		return element;
	}
}
