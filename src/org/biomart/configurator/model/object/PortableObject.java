package org.biomart.configurator.model.object;

import org.biomart.configurator.utils.type.PortableType;

public class PortableObject {
	private String name;
	private PortableType type;
	private String dsName;
	private String columnName;
	
	public PortableObject(String name, PortableType type, String dsName, String columnName) {
		this.name = name;
		this.type = type;
		this.dsName = dsName;
		this.columnName = columnName;
	}
	
	public PortableObject(String name, PortableType type) {
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return this.name;
	}
	
	public PortableType getType() {
		return this.type;
	}
	
	public String getDataSetName() {
		return this.dsName;
	}
	
	public String getColumnName() {
		return this.columnName;
	}
	
	
}