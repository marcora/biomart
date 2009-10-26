package org.biomart.configurator.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @author yliang
 *
 */
public enum ConnectionPool {
	Instance;
	
	private Map<ConnectionObject, Connection> connections;
	
	private ConnectionPool() {
		this.connections = new HashMap<ConnectionObject, Connection>();
	}
	/*
	 * if cannot find a connection object, create a new one and put it in pool
	 */
	public Connection getConnection(ConnectionObject conObject) {
		Connection con = this.connections.get(conObject);
		if(con==null) {
			try {
				Class.forName(conObject.getDriverClassString());
				con = DriverManager.getConnection(conObject.getJdbcUrl()+conObject.getDatabaseName(),
						conObject.getUserName(),conObject.getPassword());			
			} catch(java.lang.ClassNotFoundException e) {
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.connections.put(conObject, con);
		}
		return con;
	}
	
	public void releaseConnection(ConnectionObject conObject) {
		Connection con = this.connections.get(conObject);
		if(con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		this.connections.remove(conObject);			
	}
}