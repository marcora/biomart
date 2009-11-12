package org.biomart.configurator.utils;

import org.biomart.configurator.utils.type.JdbcType;

/**
 * Has all information for creating a JDBC connection
 * This is an immutable object. 
 * @author yliang
 *
 */
public class DbConnectionInfoObject {
	private String jdbcUrl;
	private String databaseName;
	private String userName;
	private String password;
	private String schemaName;
	private JdbcType type;
	
	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}
	
	public DbConnectionInfoObject(String url,String dbName, String schemaName,
			String userName, String pwd, JdbcType type) {
		this.jdbcUrl = url;
		this.userName = userName;
		this.password = pwd;
		this.type = type;
		this.databaseName = dbName;
		this.schemaName = schemaName;
	}
	
	public JdbcType getJdbcType() {
		return this.type;
	}
	
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		
		if(obj == null)
			return false;
		
		if(this.getClass() != obj.getClass())
			return false;
		
		if(!(obj instanceof DbConnectionInfoObject))
			return false;
		
		DbConnectionInfoObject conObj = (DbConnectionInfoObject)obj;
		if(conObj.getJdbcType().equals(this.type) && 
				conObj.getJdbcUrl().equals(this.jdbcUrl) && 
				conObj.getUserName().equals(this.userName) &&
				conObj.getPassword().equals(this.password) &&
				conObj.getDatabaseName().equals(this.databaseName))
			return true;
		else
			return false;
	}
	
	public int hashCode() {
        final int PRIME = 31;
        int result = PRIME + 
        	this.jdbcUrl.hashCode() + this.userName.hashCode() + this.password.hashCode() +
        	this.databaseName.hashCode();
        return result;
	}

	public String getDatabaseName() {
		return databaseName;
	}
	
	public String getSchemaName() {
		return this.schemaName;
	}
}