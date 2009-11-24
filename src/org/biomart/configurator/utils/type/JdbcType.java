package org.biomart.configurator.utils.type;

public enum JdbcType {
	MySQL("MySQL"),
	PostGreSQL("PostGreSQL"),
	Oracle("Oracle");
	
	private String name;

	
	JdbcType(String name) {
		this.name = name;

	}
	
	public String toString() {
		return this.name;
	}
	
	public String getDefaultPort() {
		switch(this) {
		case PostGreSQL:
			return "5432";
		case Oracle:
			return "1521";
		default:
			return "3306";
		}			
	}
	
	public String getDriverClassName() {
		switch(this) {
		case PostGreSQL:
			return "org.postgresql.Driver";
		case Oracle:
			return "oracle.jdbc.driver.OracleDriver";
		default:
			return "com.mysql.jdbc.Driver";		
		}
	}
	
	public String getUrlTemplate() {
		switch(this) {
		case PostGreSQL:
			return "jdbc:postgresql://<HOST>:<PORT>/";
		case Oracle:
			return "jdbc:oracle:thin:@<HOST>:<PORT>:";
		default:
			return "jdbc:mysql://<HOST>:<PORT>/";		
		}		
	}
}