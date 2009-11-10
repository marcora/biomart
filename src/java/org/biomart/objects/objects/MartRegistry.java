package org.biomart.objects.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
			super.toString() +
			", locationList.size() = " + this.locationList.size();
	}

	// No real need for those (we won't ever compare registries)
	/*@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		
		MartRegistry martRegistry=(MartRegistry)object;
		return (
				this.locationList.size()==martRegistry.locationList.size()	// locationList is never null (constructor) TODO better
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		return hash;
	}

	public int compare(MartRegistry martRegistry1, MartRegistry martRegistry2) {
		if (martRegistry1==null && martRegistry2!=null) {
			return -1;
		} else if (martRegistry1!=null && martRegistry2==null) {
			return 1;
		}
		return 0;
	}

	public int compareTo(MartRegistry martRegistry) {
		return compare(this, martRegistry);
	}*/
	
	public Element generateXml() {
		Element element = new Element(XML_ELEMENT_NAME);
		
		for (Location location : this.locationList) {
			element.addContent(location.generateXml());
		}
		
		return element;
	}
}
