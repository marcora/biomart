package org.biomart.lib.BioMart;

import java.awt.*;
import java.util.*;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.biomart.lib.BioMart.*;

public class Initializer {
	//... Constants
    private static final String INITIAL_VALUE = "0";
    public Logger log = Logger.getLogger(Initializer.class.getName());

    public String registryFile = null;
    public Registry registryObj = null;
    // Constructor 
    public Initializer(String regFile) {    	
    	registryFile = regFile;
    }
    
    public void initRegistry() {

    	registryObj = new Registry();
    	
    	XMLParser XMLDocumentObj = new XMLParser(this.registryFile);
              
        //get the root element
		Element root = XMLDocumentObj.getDocRoot();

		//get a nodelist of  elements [SECTION-1 PORTAL CONFIGURATION]
		NodeList nl = root.getElementsByTagName("martuser");
		if(nl != null && nl.getLength() > 0) {
			for (int i=0; i< nl.getLength(); i++){
				this.processMartUser(registryObj, (Element)nl.item(i));	
			}
		}
		
		//get a nodelist of  elements [SECTION-1 MART CONFIGURATION]
		nl = root.getElementsByTagName("location");
		if(nl != null && nl.getLength() > 0) {
			for (int i=0; i< nl.getLength(); i++){
				this.processLocation(registryObj, (Element)nl.item(i));	
			}
		}
    }
    
    public Registry getRegistry(){
    	return this.registryObj;
    }
    
    //[SECTION-1 PORTAL CONFIGURATION]
    public void processMartUser (Registry registryObj, Element node) {
		
    	String name = node.getAttribute("name"); 
    	String password = node.getAttribute("password");
    	MartUser martUserObj = new MartUser(name, password);
    	
    	//get a nodelist of  elements
		NodeList nl = node.getElementsByTagName("group");
		if(nl != null && nl.getLength() > 0) {
			for (int i=0; i< nl.getLength(); i++){
				this.processGroup(martUserObj, (Element)nl.item(i));	
			}
		}
    	    	
		registryObj.addMartUser(martUserObj);
	}
    
    public void processGroup (MartUser martUserObj, Element node) {
		
    	String name = node.getAttribute("name"); 
    	String groupDisplayName = node.getAttribute("groupDisplayName");
    	String configDisplayName = node.getAttribute("configDisplayName");
    	Group groupObj = new Group(name, groupDisplayName, configDisplayName);
    	
    	//get a nodelist of  elements - sub groups
		NodeList nl = node.getElementsByTagName("group");
		if(nl != null && nl.getLength() > 0) {
			for (int i=0; i< nl.getLength(); i++){
				this.processNestedGroups(groupObj, (Element)nl.item(i));	
			}
		}
		//get a nodelist of  elements - configs
		nl = node.getElementsByTagName("config");
		if(nl != null && nl.getLength() > 0) {
			for (int i=0; i< nl.getLength(); i++){
				this.processConfig(groupObj, (Element)nl.item(i));	
			}
		}
		
		martUserObj.addGroup(groupObj);
	}
    
    public void processNestedGroups (Group parentGroupObj, Element node) {
		
    	String name = node.getAttribute("name"); 
    	String groupDisplayName = node.getAttribute("groupDisplayName");
    	String configDisplayName = node.getAttribute("configDisplayName");
    	Group groupObj = new Group(name, groupDisplayName, configDisplayName);
    	
    	//get a nodelist of  elements
		NodeList nl = node.getElementsByTagName("group");
		if(nl != null && nl.getLength() > 0) {
			for (int i=0; i< nl.getLength(); i++){
				this.processNestedGroups(groupObj, (Element)nl.item(i));	
			}
		}
		
		parentGroupObj.addGroup(groupObj);
	}
    
    public void processConfig (Group groupObj, Element node) {
		
    	String name = node.getAttribute("name"); 
    	String location = node.getAttribute("location");
    	String mart = node.getAttribute("mart");
    	String version = node.getAttribute("version");
    	String dataset = node.getAttribute("dataset");
    	String processors = node.getAttribute("processors");
    	
    	Config configObj = new Config(name, location, mart, version, dataset, processors);
    	
    	//get a nodelist of  elements
		NodeList nl = node.getElementsByTagName("defaultfilter");
		if(nl != null && nl.getLength() > 0) {
			for (int i=0; i< nl.getLength(); i++){
				this.processDefaultFilter(configObj, (Element)nl.item(i));	
			}
		}
		
		//get a nodelist of  elements
		nl = node.getElementsByTagName("link");
		if(nl != null && nl.getLength() > 0) {
			for (int i=0; i< nl.getLength(); i++){
				this.processLink(configObj, (Element)nl.item(i));	
			}
		}
		
		groupObj.addConfig(configObj);
	}
	
    public void processDefaultFilter (Config configObj, Element node) {
		
    	String name = node.getAttribute("name"); 
    	String value = node.getAttribute("value");
    	
    	DefaultFilter defaultFilterObj = new DefaultFilter(name, value);    	
    			
		configObj.addDefaultFilter(defaultFilterObj);
	}

    public void processLink (Config configObj, Element node) {
		
    	String location = node.getAttribute("location");
    	String mart = node.getAttribute("mart");
    	String version = node.getAttribute("version");
    	String dataset = node.getAttribute("dataset");
    	String config = node.getAttribute("config");
    	String source = node.getAttribute("source");
    	String target = node.getAttribute("target");
    	
    	Link linkObj = new Link(location, mart, version, dataset, config, source, target);
    			
		configObj.addLink(linkObj);
	}
    
    //[SECTION-2 MART CONFIGURATION]
    public void processLocation (Registry registryObj, Element node) {
		
    	Location locationObj = new Location(node.getAttribute("name"),
    										node.getAttribute("type"),
    										node.getAttribute("host"),
    										node.getAttribute("port"),
    										node.getAttribute("userName"),
    										node.getAttribute("password"));
    	
    	//get a nodelist of  elements
		NodeList nl = node.getElementsByTagName("mart");
		if(nl != null && nl.getLength() > 0) {
			for (int i=0; i< nl.getLength(); i++){
				this.processMart(locationObj, (Element)nl.item(i));	
			}
		}
    	    	
		registryObj.addLocation(locationObj);
	}
    
    public void processMart (Location locationObj, Element node) {
		
    	Mart martObj = new Mart(node.getAttribute("name"),
    							node.getAttribute("version"),
    							node.getAttribute("databaseName"),
    							node.getAttribute("schemaName"),
    							locationObj);
    	
    	//get a nodelist of  elements
		NodeList nl = node.getElementsByTagName("partitionTable");
		if(nl != null && nl.getLength() > 0) {
			for (int i=0; i< nl.getLength(); i++){
				this.processPartitionTable(martObj, (Element)nl.item(i));	
			}
		}

		locationObj.addMart(martObj);
	}
    
    public void processPartitionTable (Mart martObj, Element node) {
		
    	String name = node.getAttribute("name"); 
    	String rows = node.getAttribute("rows");
    	String cols = node.getAttribute("cols");
    	PartitionTable partitionTableObj = new PartitionTable(name, rows, cols);
    	
    	//get a nodelist of  elements
		NodeList nl = node.getElementsByTagName("partitionTable");
		if(nl != null && nl.getLength() > 0) {
			for (int i=0; i< nl.getLength(); i++){
			//	this.processPartitionTable(martObj, (Element)nl.item(i));	
			}
		}
    	    	
		martObj.addPartitionTable(partitionTableObj);
	}
    
	/* main */
    public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
