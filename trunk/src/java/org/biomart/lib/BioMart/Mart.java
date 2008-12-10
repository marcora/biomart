package org.biomart.lib.BioMart;

import java.util.Collection;
import java.util.LinkedList;

public class Mart extends Root{
	
	public String name = null;
	public String version = null;
	public Collection partitionTables;
	public Location location;
	private String databaseName;
	private String schemaName;

	public Mart(String name, String version, Location location) {
		
		log.info("creating Mart Object: " + name);
		partitionTables = new LinkedList();
		
		this.name = name;
		this.version = version;
		this.location = location;
	}
	
	public void addPartitionTable(PartitionTable partitionTableObj) {
		log.info("adding PartitionTable object to Mart");
		this.partitionTables.add(partitionTableObj);		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

}
