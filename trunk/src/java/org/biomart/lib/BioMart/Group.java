package org.biomart.lib.BioMart;

import java.util.Collection;
import java.util.LinkedList;

public class Group extends Root{

	public String name = null;
	public String groupDisplayType = null;
	public String configDisplayType = null;
	
	public Collection groups, configs;

	
	public Group(String name, String groupDisplayName, String configDisplayName)
	{
		log.info("creating Group Object: " + name);
		this.groups = new LinkedList();
		this.configs = new LinkedList();
		
		this.name = name;
		this.groupDisplayType = groupDisplayName;
		this.configDisplayType = configDisplayName;
	}
	
		
	public void addGroup(Group groupObj) {
		log.info("adding Nested Group object to the parent group");
		this.groups.add(groupObj);
	}
	
	public void addConfig(Config configObj) {
		log.info("adding Config object to the group");
		this.configs.add(configObj);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
