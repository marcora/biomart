package org.biomart.lib.BioMart;

import java.util.Collection;
import java.util.LinkedList;

public class PartitionTable extends Root{

	public String name = null;
	public String rows = null;
	public String cols = null;
	public Collection cells;

	public PartitionTable(String name, String rows, String cols) {
		
		log.info("creating PartitionTable Object: " + name);
		cells = new LinkedList();
		
		this.name = name;
		this.rows = rows;
		this.cols = cols;
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
