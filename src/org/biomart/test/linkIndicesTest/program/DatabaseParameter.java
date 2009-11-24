package org.biomart.test.linkIndicesTest.program;

import org.biomart.objects.helpers.Rdbs;



@Deprecated	// use the one in martconfigurator instead, with private members
public class DatabaseParameter {
	public Rdbs rdbs = null;
	public String databaseHost = null;
	public Integer databasePort = null;
	public String databaseUser = null;
	public String databasePassword = null;
	public String databaseName = null;

	public DatabaseParameter(Rdbs rdbs, String databaseHost, Integer databasePort, String databaseUser, String databasePassword, String databaseName) {
		super();
		this.rdbs = rdbs;
		this.databaseHost = databaseHost;
		this.databasePort = databasePort;
		this.databaseUser = databaseUser;
		this.databasePassword = databasePassword;
		this.databaseName = databaseName;
	}
	
	@Override
	public String toString () {	
		return 
		"rdbs =  " + rdbs + ", " +
		"databaseHost =  " + databaseHost + ", " +
		"databasePort =  " + databasePort + ", " +
		"databaseUser =  " + databaseUser + ", " +
		"databaseName =  " + databaseName;
	}
	public String toShortString () {	
		return rdbs + ", " + databaseHost + ", " + databasePort + ", " + databaseUser + ", " + databaseName;
	}
}
