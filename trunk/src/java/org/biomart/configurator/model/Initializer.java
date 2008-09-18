package org.biomart.configurator.model;

import java.awt.*;
import java.util.*;
import java.util.logging.Logger;


import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import org.biomart.configurator.model.XMLParser;
import org.biomart.configurator.controller.Start;

public class Initializer  extends Observable {
	//... Constants
    private static final String INITIAL_VALUE = "0";
    public Logger log = Logger.getLogger(Start.class.getName());

    
    //============================================================== constructor
    /** Constructor */
    public Initializer() {
    
    }
    
    public void initRegistry(String registryFile) {
        XMLParser XMLDocumentObj = new XMLParser(registryFile);
        //get the root element
		Element root = XMLDocumentObj.getDocRoot();

        for (int i=0; i<3; i++) {
        	log.info(root.getTagName());
       		//log.info("not NULL");

        }
        
    }
    
    public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
