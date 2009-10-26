package org.biomart.configurator.utils;

public class MRunnerInfoObject {
	private String host;
	private String port;
	
	public MRunnerInfoObject(String host, String port) {
		this.host = host;
		this.port = port;
	}
	
	public String getHost() {
		return this.host;
	}
	
	public String getPort() {
		return this.port;
	}
}