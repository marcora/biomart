package org.biomart.configurator.utils;

/**
 * Has all information for creating a JDBC connection
 * @author yliang
 *
 */
public class DbInfoObject {
	private String jdbcUrl;
	private String databaseName;
	private String userName;
	private String password;
	private String driverClassString;
	
	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}
	public String getJdbcUrl() {
		return jdbcUrl;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserName() {
		return userName;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPassword() {
		return password;
	}
	
	public DbInfoObject(String url,String dbName, String userName, String pwd, String driverClassString) {
		this.jdbcUrl = url;
		this.userName = userName;
		this.password = pwd;
		this.driverClassString = driverClassString;
		this.databaseName = dbName;
	}
	public void setDriverClassString(String driverClassString) {
		this.driverClassString = driverClassString;
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
		
		if(!(obj instanceof DbInfoObject))
			return false;
		
		DbInfoObject conObj = (DbInfoObject)obj;
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
	
}