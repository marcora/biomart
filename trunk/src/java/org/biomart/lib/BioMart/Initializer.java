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

		//get a nodelist of  elements
		NodeList nl = root.getElementsByTagName("martuser");
		if(nl != null && nl.getLength() > 0) {
			for (int i=0; i< nl.getLength(); i++){
				this.processMartUser(registryObj, (Element)nl.item(i));	
			}
		}        
    }
    
    public Registry getRegistry(){
    	return this.registryObj;
    }
    
    public void processMartUser (Registry registryObj, Element node) {
		
    	String name = node.getAttribute("name"); 
    	String password = node.getAttribute("password");
    	MartUser martUserObj = new MartUser(name, password);
    	
		registryObj.addMartUser(martUserObj);
	}
    
    public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
