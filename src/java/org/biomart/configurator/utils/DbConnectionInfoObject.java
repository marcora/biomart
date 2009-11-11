package org.biomart.configurator.utils;

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
	private String driverClassString;
	private String schemaName;
	
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
			String userName, String pwd, String driverClassString) {
		this.jdbcUrl = url;
		this.userName = userName;
		this.password = pwd;
		this.driverClassString = driverClassString;
		this.databaseName = dbName;
		this.schemaName = schemaName;
	}
	
	public String getDriverClassString() {
		return driverClassString;
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
		if(conObj.getDriverClassString().equals(this.driverClassString) && 
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
        int result = PRIME +  this.driverClassString.hashCode() + 
        	this.jdbcUrl.hashCode() + this.userName.hashCode() + this.password.hashCode() +
        	this.databaseName.hashCode();
        return result;
	}
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	public String getDatabaseName() {
		return databaseName;
	}
	
	public String getSchemaName() {
		return this.schemaName;
	}
}