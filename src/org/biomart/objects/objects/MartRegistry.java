package org.biomart.objects.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.jdom.Document;
import org.jdom.Element;


public class MartRegistry extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 4555425904982314129L;
	
	public static final String XML_ELEMENT_NAME = "martRegistry";
	
	public static void main(String[] args) {}
	
	private List<Location> locationList = null;
	
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





/*MartRegistry martRegistry = new MartRegistry();
List<Location> locationList = new ArrayList<Location>();
Map<Location, List<Mart>> martMap = new HashMap<Location, List<Mart>>();
for (MartRegistry martRegistryTmp : martRegistryList) {
	List<Location> locationListTmp = martRegistryTmp.getLocationList();
	for (Location location : locationListTmp) {
		if (!locationList.contains(location)) {
			martRegistry.addLocation(location);
			locationList.add(location);
			martMap.put(location, location.getMartList());
		} else {
			Location currentLocation = locationList.get(locationList.indexOf(location));
			List<Mart> martListTmp = location.getMartList();
			List<Mart> martList = martMap.get(location);
			for (Mart mart : martListTmp) {
				if (!martList.contains(mart)) {
					currentLocation.addMart(mart);
					martList.add(mart);
				} else {
					Mart currentMart = martList.get(martList.indexOf(mart));
					List<Dataset> datasetList = mart.getDatasetList();
					for (Dataset dataset : datasetList) {
						currentMart.addDataset(dataset);
					}
				}
			}
		}
	}
}*/