package org.biomart.lib.BioMart;

import java.awt.*;
import java.util.*;
import java.util.logging.Logger;


import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class Initializer {
	//... Constants
    private static final String INITIAL_VALUE = "0";
    public Logger log = Logger.getLogger(Initializer.class.getName());

    public String registryFile = null;

    // Constructor 
    public Initializer(String regFile) {    	
    	registryFile = regFile;
    }
    
    public void initRegistry() {
        XMLParser XMLDocumentObj = new XMLParser(this.registryFile);
        //get the root element
		Element root = XMLDocumentObj.getDocRoot();

		//get a nodelist of  elements
		NodeList nl = root.getElementsByTagName("martuser");
		if(nl != null && nl.getLength() > 0) {
			for (int i=0; i< nl.getLength(); i++){
				this.processMartUser(nl);	
			}			
		}

        
    }
    
    public void processMartUser (NodeList nl) {
		log.info("helloUser");
	}
    
    public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
