package org.biomart.configurator.model.object;

import org.biomart.configurator.utils.type.McNodeType;

public class McDsTable extends McObject {

	protected String locationName;
	protected String martName;
	protected String dataSetName;
	
	public String getLocationName() {
		return locationName;
	}
	public String getMartName() {
		return martName;
	}
	public String getDataSetName() {
		return this.dataSetName;
	}
	
	public McDsTable(McNodeType type, String locName, String martName, String datasetName, String name) {
		this.nodeType = type;
		this.name = name;
		this.locationName = locName;
		this.martName = martName;
		this.dataSetName = datasetName;
	}
	
	public String toString() {
		return this.locationName+"->"+this.martName+"->"+this.dataSetName+"->"+this.name;
	}

	
}