package org.biomart.lib.BioMart;

import org.biomart.lib.BioMart.*;

public class MartUser extends Root {

	public String name = null;
	public String password = null;
	
	// constructors
	public MartUser(String name, String password){
		log.info("creating MartUser Object: "+name);
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
	
}
