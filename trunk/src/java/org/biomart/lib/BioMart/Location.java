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

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
