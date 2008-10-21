package org.biomart.lib.BioMart;

import java.util.Collection;
import java.util.LinkedList;

public class Location extends Root{
	
	public String name = null;
	public Collection marts;

	
	public Location(String name) {
		
		log.info("creating Location Object: "+ name);
		this.marts = new LinkedList();	
		
		this.name = name;
		
	}
	
	public void addMart(Mart martObj) {
		log.info("adding Mart object to Location");
		this.marts.add(martObj);		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
