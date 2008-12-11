package org.biomart.lib.BioMart;

import java.util.Collection;
import java.util.LinkedList;

public class Location extends Root{
	
	public String name = null;
	public Collection marts;
	private String type;
	private String host;
	private String port;
	private String userName;
	private String password;

	
	public Location(String name) {
		
		log.info("creating Location Object: "+ name);
		this.marts = new LinkedList();	
		
		this.name = name;
		
	}
	
	public Location(String name, String type, String host, String port, String userName, String password) {
		
		log.info("creating Location Object: "+ name);
		this.marts = new LinkedList();
		
		this.name = name;
		this.type = type;
		this.host = host;
		this.port = port;
		this.userName = userName;
		this.password = password;
		
	}

	public void addMart(Mart martObj) {
		log.info("adding Mart object to Location");
		this.marts.add(martObj);		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public String getHost() {
		return host;
	}

	public String getPort() {
		return port;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getType() {
		return type;
	}

}
