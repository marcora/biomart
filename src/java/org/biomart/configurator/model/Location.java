package org.biomart.configurator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.biomart.builder.model.JDBCSchema;
import org.biomart.builder.model.Mart;
import org.biomart.builder.model.Schema;
import org.biomart.builder.model.Table;
import org.biomart.common.resources.Settings;
import org.biomart.configurator.utils.DbInfoObject;
import org.biomart.configurator.utils.McUtils;

/**
 * needs to handle new location or a location from XML
 * @author yliang
 *
 */
public class Location {

	private String name;
	//TODO should be a generic connectionobject
	//TODO should have a location type
	private DbInfoObject conObject;
	private boolean isKeyGuessing=true;
	private boolean isSourceSchema = true;
	//TODO this one should not be here
	private Map<String, List<String>> selectedTables;
	private Map<String, List<String>> dbtablesMap;
	//should use BeanMap later
	private Map<String, Mart> marts;
	
	public Location(String name) {
		this.marts = new HashMap<String, Mart>();
		this.name = name;
	}
	
	public void addMart(Mart mart) {
		//check if the mart already exist
		if(this.marts.get(mart.getMartName())==null) {
			marts.put(mart.getMartName(), mart);	
		}
	}
	
	public void addSelectedTables(Map<String, List<String>> tables) {
		this.selectedTables = tables;
	}

	public void addDBTablesMap(Map<String, List<String>> tables) {
		this.dbtablesMap = tables;
	}
	
	public Map<String, List<String>> getDbTablesMap() {
		return this.dbtablesMap;
	}

	public Map<String, List<String>> getSelectedTables() {
		return this.selectedTables;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Map<String, Mart> getMarts() {
		return this.marts;
	}
	
	public Mart getMart(String name) {
		return this.marts.get(name);
	}
	
	public void storeInHistory() {
		final Properties history = new Properties();
		history.setProperty("driverClass", this.conObject.getDriverClassString());
		history.setProperty("jdbcURL", this.conObject.getJdbcUrl());
		history.setProperty("username", this.conObject.getUserName());
		history.setProperty("password", this.conObject.getPassword());
		history.setProperty("keyguessing", "" + this.isKeyGuessing);
		Settings.saveHistoryProperties(Location.class, this.name, history);
	}
	
	public void setConnectionObject(DbInfoObject conObj) {
		this.conObject = conObj;
	}
	
	public DbInfoObject getConnectionObject() {
		return this.conObject;
	}
		
	public String getDriverClassString() {
		return this.conObject.getDriverClassString();
	}
		
	public String getUserName() {
		return this.conObject.getUserName();
	}
		
	public String getJDBCUrl() {
		return this.conObject.getJdbcUrl();
	}
			
	public String getPassWord() {
		return this.conObject.getPassword();
	}
	
	public void setKeyGuessing(boolean value) {
		this.isKeyGuessing = value;
	}
	
	public void requestCreateLocationFromDB() {
		if(this.isSourceSchema)
			this.requestCreateLocationFromSource();
		else
			this.requestCreateLocationFromTarget();
	}
	
	private void requestCreateLocationFromSource(){
		Map<String, List<String>>selectedTables = this.getSelectedTables();
		//need to check if the mart is a new mart? a mart is an old mart if there is no
		// selectedTables attached to it.
		//suggestdatasets one mart one schema
		long t1 = McUtils.getCurrentTime();
		for(Iterator<Mart> i=this.marts.values().iterator(); i.hasNext();) {
			Mart mart = i.next();
			List<String> stStrings = selectedTables.get(mart.getMartName());
			if(stStrings == null || stStrings.size()==0) 
				continue;
			this.requestLoadSchemaInMart(mart, false,this.dbtablesMap.get(mart.getMartName()));
		}
		long t2 = McUtils.getCurrentTime();
		
		//generate dataset
		for(Iterator<Mart> i=this.marts.values().iterator(); i.hasNext();) {
			Mart mart = i.next();
			List<String> stStrings = selectedTables.get(mart.getMartName());
			
			if(stStrings == null || stStrings.size()==0) 
				continue;
			List<Table> suggestTables = new ArrayList<Table>();
			Map<String, Schema> schemas = mart.getSchemasObj().getSchemas();
			for (final Iterator<Schema> s = schemas.values().iterator(); s.hasNext();) {
				JDBCSchema schema = (JDBCSchema)s.next();
				Map<String,Table> tables = schema.getTables();
				
				for(String st:stStrings) {
					Table table = (Table)tables.get(st);
					suggestTables.add(table);
				}
			}
			try{
				mart.suggestDataSets(suggestTables);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}	
		long t3 = McUtils.getCurrentTime();
		System.err.println("create sources "+(t2-t1));
		System.err.println("create target "+(t3-t2));
	}
	
	private void requestCreateLocationFromTarget(){
		//suggestdatasets one mart one schema
		Map<String, List<String>>selectedTables = this.getSelectedTables();

		for(Iterator<Mart> i=this.marts.values().iterator(); i.hasNext();) {
			Mart mart = i.next();
			List<String> stStrings = selectedTables.get(mart.getMartName());
			if(stStrings == null || stStrings.size()==0)
				continue;
			mart.setMainTableList(selectedTables.get(mart.getMartName()));
			this.requestLoadSchemaInMart(mart, true,this.dbtablesMap.get(mart.getMartName()));
		}
		
		
		for(Iterator<Mart> i=this.marts.values().iterator(); i.hasNext();) {
			Mart mart = i.next();
			List<String> stStrings = selectedTables.get(mart.getMartName());
			if(stStrings == null || stStrings.size()==0)
				continue;
			List<Table> suggestTables = new ArrayList<Table>();
			Map schemas = mart.getSchemas();
			for (final Iterator s = schemas.values().iterator(); s.hasNext();) {
				Schema schema = (Schema)s.next();				
				Map tables = schema.getTables();
				
				for(String st:stStrings) {
					Table table = (Table)tables.get(st);					
					suggestTables.add(table);
				}
			}
			//check number of keys and relations in the suggestTables
			Table[] martTables = new Table[suggestTables.size()];
			for(int j=0; j<suggestTables.size(); j++) {
				Table table = suggestTables.get(j);
			}
			try{
				mart.suggestDataSets(suggestTables);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}		
	}

	public void setIsSourceSchema(boolean isSourceSchema){
		this.isSourceSchema = isSourceSchema;
	}
		
	public boolean isFromSourceSchema() {
		return this.isSourceSchema;
	}

	/*
	 * this function should go in Mart 
	 */
	public boolean requestLoadSchemaInMart(Mart mart, boolean isTarget, List<String> tablesInDb) {
		for(Iterator<Schema> i=mart.getSchemasObj().getSchemas().values().iterator(); i.hasNext();) {
			JDBCSchema schema = (JDBCSchema)i.next();
			schema.setIsMart(isTarget);
			this.marts.get(mart.getMartName()).getSchemasObj().requestInitSchema(schema, false, tablesInDb);
		}
		return true;
	}

}