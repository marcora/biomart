package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;


public class Location extends MartConfiguratorObject implements Comparable<Location>, Comparator<Location>, Serializable {

	private static final long serialVersionUID = 3492546591865583968L;
	
	public static final String XML_ELEMENT_NAME = "location";
	
	public static void main(String[] args) {}

	private String host = null;
	private String user = null;
	private LocationType type = null;
	
	private List<Mart> martList = null;

	public List<Mart> getMartList() {
		return martList;
	}

	public Location(String name, String displayName, String description, Boolean visible, 
			String host, String user, LocationType type) {
		super(name, displayName, description, visible, XML_ELEMENT_NAME);
		this.host = host;
		this.user = user;
		this.type = type;
		
		this.martList = new ArrayList<Mart>();
	}
	
	public void addMart(Mart mart) {
		this.martList.add(mart);
	}

	public String getHost() {
		return host;
	}

	public String getUser() {
		return user;
	}

	public LocationType getType() {
		return type;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"host = " + host + ", " + 
			"user = " + user + ", " + 
			"type = " + type;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Location location=(Location)object;
		return (
			super.equals(location) &&
			(this.host==location.host || (this.host!=null && host.equals(location.host)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==host? 0 : host.hashCode());
		return hash;
	}

	public int compare(Location location1, Location location2) {
		if (location1==null && location2!=null) {
			return -1;
		} else if (location1!=null && location2==null) {
			return 1;
		}
		return CompareUtils.compareNull(location1.host, location2.host);
	}

	public int compareTo(Location location) {
		return compare(this, location);
	}
	
	/**
	 * Only for the node, children are treated separately
	 */
	public Element generateXml() {
		Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "host", this.host);
		MartConfiguratorUtils.addAttribute(element, "type", (this.type!=null ? this.type.getXmlValue() : null));
		MartConfiguratorUtils.addAttribute(element, "user", this.user);

		for (Mart mart : this.martList) {
			element.addContent(mart.generateXml());
		}
		
		return element;
	}

	
	
	// ===================================== Should be a different class ============================================

	public Location(Location location) throws CloneNotSupportedException {	// creates a light clone (temporary solution)
		this(null, null, null, null,	// irrelevant here 
				location.getHost(), location.getUser(), location.getType());
		
		this.martList = new ArrayList<Mart>();
		for (Mart mart : location.martList) {
			this.martList.add(new Mart(mart));
		}
	}
}
