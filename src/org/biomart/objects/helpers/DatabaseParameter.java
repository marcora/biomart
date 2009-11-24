package org.biomart.objects.helpers;




public class DatabaseParameter implements Cloneable {
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	private Rdbs rdbs = null;
	private String databaseHost = null;
	private Integer databasePort = null;
	private String databaseUser = null;
	private String databasePassword = null;
	private String databaseName = null;

	public DatabaseParameter(Rdbs rdbs, String databaseHost, Integer databasePort, String databaseUser, String databasePassword) {
		this(rdbs, databaseHost, databasePort, databaseUser, databasePassword, null);
	}
	public DatabaseParameter(Rdbs rdbs, String databaseHost, Integer databasePort, String databaseUser, String databasePassword, String databaseName) {
		super();
		this.rdbs = rdbs;
		this.databaseHost = databaseHost;
		this.databasePort = databasePort;
		this.databaseUser = databaseUser;
		this.databasePassword = databasePassword;
		this.databaseName = databaseName;
	}
	
	public String getDatabaseHost() {
		return databaseHost;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public Integer getDatabasePort() {
		return databasePort;
	}

	public String getDatabaseUser() {
		return databaseUser;
	}

	public Rdbs getRdbs() {
		return rdbs;
	}

	@Override
	public String toString () {	
		return 
		"rdbs =  " + rdbs + ", " +
		"databaseHost =  " + databaseHost + ", " +
		"databasePort =  " + databasePort + ", " +
		"databaseUser =  " + databaseUser + ", " +
		"databasePassword =  " + databasePassword + ", " +
		"databaseName =  " + databaseName;
	}
	public String toShortString () {	
		return rdbs + ", " + databaseHost + ", " + databasePort + ", " + databaseUser + ", " + databasePassword + ", " + databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
}
