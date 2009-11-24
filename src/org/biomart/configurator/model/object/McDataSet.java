package org.biomart.configurator.model.object;

import org.biomart.configurator.utils.type.McNodeType;

public class McDataSet extends McObject {
	protected String locationName;
	protected String martName;
	
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	public String getLocationName() {
		return locationName;
	}
	public void setMartName(String martName) {
		this.martName = martName;
	}
	public String getMartName() {
		return martName;
	}
	
	public McDataSet(McNodeType type, String locName, String martName, String name) {
		this.nodeType = type;
		this.name = name;
		this.locationName = locName;
		this.martName = martName;
	}
	
	public String toString() {
		return this.locationName+"->"+this.martName+"->"+this.name;
	}
}