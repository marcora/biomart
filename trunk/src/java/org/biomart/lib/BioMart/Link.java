package org.biomart.lib.BioMart;

import java.util.Collection;
import java.util.LinkedList;

public class Link extends Root {

	public String location = null;
	public String mart = null;
	public String version = null;
	public String dataset = null;
	public String config = null;	
	public String source = null;
	public String target = null;	

	
	public Link(String location, String mart, String version, String dataset, 
								String config, String source, String target) {
		log.info("creating Link Object: ");
		
		this.location = location;
		this.mart = mart;
		this.version = version;
		this.dataset =  dataset;
		this.config = config;
		this.source = source;
		this.target = target;
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
