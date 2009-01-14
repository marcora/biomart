package org.biomart.lib.BioMart;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biomart.lib.utils.DBConnectionUtils;

public class Mart extends Root{
	
	public String name = null;
	public String version = null;
	public Collection partitionTables;
	public Location location;
	private String databaseName;
	private String schemaName;

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
	
	public String getMetaInfoXML() throws Exception, SQLException {
		String metaInfoXML = null;
		
		// TODO: check if location.getType() is "martservice", if yes, get metaInfoXML through martservice
		if (location.getType().equals("martservice")) {
			log.info("getting metaInfoXML from martservice at: " + location.getHost());
			// code for getting XML through martservice here
			return metaInfoXML;
		}
		
		Connection conn;
		
		log.info("connecting to DB at: " + location.getHost());
		conn = DBConnectionUtils.getConnection(location.getType(), location.getHost(),
					location.getPort(), databaseName, location.getUserName(), location.getPassword());
		
		DatabaseMetaData dmd;
		try {
			dmd = conn.getMetaData();
		} catch (Exception e) {
			conn.close();
			throw e;
		}
		
		// TODO: try to get the XML from metatable in the DB, or create one if it's necessary
		/* pseudocode here
		If NOT metatables with XML
		{
		    check_if_you_are_admin_for_writeBack;
		    if YES
		    {
		        readSchema;
		        makeXML;
		        dumpTheXMLBackToMart;
		        $xml = XMLJustCreated;
		    }
		    NOT Admin
		    {
		       sorryMate;
		       exit();
		    }
		}
		else
		{
		    $xml = XML_FROM_MetaTables;
		}
		*/
		
		// go directly to make metaInfoXML from DB schema for now
		metaInfoXML = (new MartMetaInfoHelper()).makeMetaInfoXML(dmd, schemaName);
		
		conn.close();
		return metaInfoXML;
	}


	/**
	 * main for testing
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Location myLocation = new Location("ensembl", "mysql", "dev1.res", "3306", "martadmin", "");
		Mart myMart = new Mart("ensembl", "51", "jz_ensembl_mart_51_08", "", myLocation);
		String xml = null;
		try {
			xml = myMart.getMetaInfoXML();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println(xml);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("registry08.xml"));
		bw.write(xml);
		bw.close();
	}

	
	public String getDatabaseName() {
		return databaseName;
	}

	public String getSchemaName() {
		return schemaName;
	}

}
