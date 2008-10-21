package org.biomart.lib.BioMart;

import java.util.Collection;
import java.util.LinkedList;

public class Registry extends Root {

	public Collection martUsers;
	public Collection locations;
	
	// constructors
	public Registry() {		
		log.info("creating Regisry Object");
		martUsers = new LinkedList();
		locations = new LinkedList();
	}

	public void addMartUser(MartUser martUserObj) {
		log.info("adding MartUser object to registry");
		this.martUsers.add(martUserObj);		
	}
	
	public void addLocation(Location locationObj) {
		log.info("adding Location object to registry");
		this.martUsers.add(locationObj);		
	}
}
