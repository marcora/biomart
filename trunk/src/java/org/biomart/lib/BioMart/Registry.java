package org.biomart.lib.BioMart;

import java.util.Collection;
import java.util.LinkedList;

public class Registry extends Root {

	public Collection martUsers;

	// constructors
	public Registry() {		
		log.info("creating Regisry Object");
		martUsers = new LinkedList();
	}

	public void addMartUser(MartUser martUserObj) {
		log.info("adding MartUser object to registry");
		this.martUsers.add(martUserObj);		
	}

}
