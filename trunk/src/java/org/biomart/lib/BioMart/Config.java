package org.biomart.lib.BioMart;

import java.util.Collection;
import java.util.LinkedList;

public class Config extends Root {

	public String name = null;
	public String location = null;
	public String mart = null;
	public String version = null;
	public String dataset = null;
	public String processors = null;	
	
	public Collection defaultFilters, links;
	
	public Config(String name, String location, String mart, 
						String version, String dataset, String processors) {
		
		log.info("creating Config Object: " + name);
		this.defaultFilters = new LinkedList();
		this.links = new LinkedList();
		
		this.name = name;
		this.location = location;
		this.mart = mart;
		this.version = version;
		this.dataset =  dataset;
		this.processors = processors;		
		
	}


	public void addDefaultFilter(DefaultFilter defaultFilterObj) {
		log.info("adding defaultFilter object to the config");
		this.defaultFilters.add(defaultFilterObj);
	}
	
	public void addLink(Link linkObj) {
		log.info("adding Link Object to the config");
		this.links.add(linkObj);
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
