package org.biomart.lib.BioMart;


import java.util.Collection;
import java.util.LinkedList;

public class MartUser extends Root {

	public String name = null;
	public String password = null;
	
	public Collection groups;
	
	// constructors
	public MartUser(String name, String password){
		log.info("creating MartUser Object: "+name);
		
		this.groups = new LinkedList();
		this.name = name;
		this.password = password;
	}

	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setPassword(String pass){
		this.password = pass;
	}
	
	public String getPassword(){
		return this.password;
	}
	
	public void addGroup(Group groupObj) {
		log.info("adding Group object to MartUser");		
		this.groups.add(groupObj);	
	}
	
}
