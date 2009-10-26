package org.biomart.configurator.model.object;

import org.biomart.configurator.utils.type.McNodeType;

public class McDataSet extends McObject {
	private String locationName;
	private String martName;
	
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
	
	public McDataSet(McNodeType type, String name, String locName, String martName) {
		
	}
}