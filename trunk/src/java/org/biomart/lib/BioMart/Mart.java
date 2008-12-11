package org.biomart.lib.BioMart;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import org.biomart.lib.utils.DBConnectionUtils;

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
	
	public Mart(String name, String version, String databaseName,
			String schemaName, Location location) {

		log.info("creating Mart Object: " + name);
		partitionTables = new LinkedList();
		
		this.name = name;
		this.version = version;
		this.databaseName = databaseName;
		this.schemaName = schemaName;
		this.location = location;
	}

	public void addPartitionTable(PartitionTable partitionTableObj) {
		log.info("adding PartitionTable object to Mart");
		this.partitionTables.add(partitionTableObj);		
	}
	
	public String getMetaInfoXML() {
		String metaInfoXML = null;
		
		Connection conn;
		
		try {
			log.info("connecting to DB at: " + location.getHost());
			conn = DBConnectionUtils.getConnection(location.getType(), location.getHost(),
					location.getPort(), databaseName, location.getUserName(), location.getPassword());
		} catch( Exception e) {
			// TODO: how do we show the Exception message to the user?
			
			return metaInfoXML;
		}
		
		// TODO: try to get the XML from metatable in the DB, or create one if it's necessary
		
		
		
		return metaInfoXML;
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

	public String getSchemaName() {
		return schemaName;
	}

}
