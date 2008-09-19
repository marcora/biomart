package org.biomart.configurator.model;

import java.awt.*;
import java.util.*;
import java.util.logging.Logger;

import org.biomart.lib.BioMart.*;

public class LibraryAdaptor  extends Observable {

	/**
	 * @param args
	 */
	public Logger log = Logger.getLogger(LibraryAdaptor.class.getName());
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public void processRegistryFile (String regFile) {
		Initializer initObj = new Initializer(regFile);
		initObj.initRegistry();
	}

}
