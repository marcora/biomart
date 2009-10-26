package org.biomart.configurator.utils;

import java.util.List;
import java.util.Map;

import org.biomart.configurator.utils.type.MartType;

import martService.Configuration;

/**
 * should merge with ConnectionObject class
 * @author yliang
 *
 */
public class DsConnectionObject {
	protected String name;
	protected Map<String, List<String>> dsInfoMap;
	protected MartType type;
	
	private String host;
	private String port;
	private String path;
	private Map<String, Configuration> configMap;

	
	public void setName(String value) {
		this.name = value;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setDsInfoMap(Map<String, List<String>> value) {
		this.dsInfoMap = value;
	}
	
	public Map<String, List<String>> getDsInfoMap() {
		return this.dsInfoMap;
	}
	
	public void setType(MartType value){
		this.type = value;
	}
	
	public MartType getType() {
		return this.type;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	public String getHost() {
		return host;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getPort() {
		return port;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getPath() {
		return path;
	}
	public void setConfigMap(Map<String, Configuration> value) {
		this.configMap = value;
	}
	public Map<String, Configuration> getConfigMap() {
		return this.configMap;
	}
	
}