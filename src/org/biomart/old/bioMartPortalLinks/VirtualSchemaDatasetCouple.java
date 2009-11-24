package org.biomart.old.bioMartPortalLinks;

public class VirtualSchemaDatasetCouple {

	public String virtualSchemaName = null;
	public String datasetName = null;
	public VirtualSchemaDatasetCouple(String virtualSchemaName, String datasetName) {
		super();
		this.virtualSchemaName = virtualSchemaName;
		this.datasetName = datasetName;
	}
	@Override
	public boolean equals(Object arg0) {
		VirtualSchemaDatasetCouple couple = (VirtualSchemaDatasetCouple)arg0;
		return virtualSchemaName.equals(couple.virtualSchemaName) &&
		datasetName.equals(couple.datasetName);
	}
	@Override
	public int hashCode() {
		return 0;
	}
	@Override
	public String toString() {
		return "virtualSchemaName = " + virtualSchemaName + ", datasetName = " + datasetName; 
	}
}
