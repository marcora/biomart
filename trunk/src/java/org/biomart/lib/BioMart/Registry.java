package org.biomart.lib.BioMart;

import java.util.Collection;
import java.util.LinkedList;

public class Registry extends Root {

	public Collection martUsers;

	// constructors
	public Registry() {
		martUsers = new LinkedList();
		log.info("creating Regisry Object");

	}

	public void addMartUser(MartUser martUserObj) {
		this.martUsers.add(martUserObj);		
	}

}
