package org.biomart.lib.BioMart;

import java.util.Collection;
import java.util.LinkedList;


public class DefaultFilter extends Root {

	public String name = null;
	public String value = null;
	
	public DefaultFilter(String name, String value) {
		log.info("creating DefaultFilter Object: " + name);
		this.name = name;
		this.value = value;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
