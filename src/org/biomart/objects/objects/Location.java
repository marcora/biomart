package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.objects.types.LocationType;
import org.jdom.Element;

public class Location extends MartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 3492546591865583968L;
	
	public static final String XML_ELEMENT_NAME = 	//TODO put in McNodeType enum?
		"location" + (MyUtils.anthony() && (1>0) ? MyUtils.getDateAndTime() : "");	//TODO remove (to help debug)
	public static final McNodeType MC_NODE_TYPE = McNodeType.Location;
	
	public static void main(String[] args) {}

	private String host = null;
	private String user = null;
	private LocationType type = null;
	
	private List<Mart> martList = null;

	public void addMart(Mart mart) {
		this.martList.add(mart);
	}
	public List<Mart> getMartList() {
		return new ArrayList<Mart>(this.martList);
	}
	public Mart getMart(String name) {
		return (Mart)super.getMartConfiguratorObjectByName(this.martList, name);
	}

	public Location(String name, String displayName, String description, Boolean visible, 
			String host, String user, LocationType type) {
		super(name, displayName, description, visible, XML_ELEMENT_NAME);
		this.host = host;
		this.user = user;
		this.type = type;
		
		this.martList = new ArrayList<Mart>();
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

	public void merge (Location location) {
		List<Mart> martList1 = this.getMartList();
		List<Mart> martList2 = location.getMartList();
		for (Mart mart2 : martList2) {
			int index = martList1.indexOf(mart2);
			if (index==-1) {
				this.addMart(mart2);
			} else {	// They are the same, but their content may be different
				Mart mart1 = martList1.get(index);
				mart1.merge(mart2);
			}
		}
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"host = " + host + ", " + 
			"user = " + user + ", " + 
			"type = " + type + ", " + 
			"martList.size() = " + martList.size();
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
			(this.host==location.host || (this.host!=null && host.equals(location.host))) &&
			(this.type==location.type || (this.type!=null && type.equals(location.type)))
		);
	}
	
	public Element generateXml() throws FunctionalException {
		Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "host", this.host);
		MartConfiguratorUtils.addAttribute(element, "type", (this.type!=null ? this.type.getXmlValue() : null));
		MartConfiguratorUtils.addAttribute(element, "user", this.user);

		for (Mart mart : this.martList) {
			element.addContent(mart.generateXml());
		}
		
		return element;
	}
}
