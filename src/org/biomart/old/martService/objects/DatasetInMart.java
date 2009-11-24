package org.biomart.old.martService.objects;


import java.io.Serializable;

import org.biomart.common.general.constants.MyConstants;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.old.martService.MartServiceConstants;


public class DatasetInMart implements Serializable {
	private static final long serialVersionUID = 2270358792401619533L;
	
	private String virtualSchema = null;
	private MartInVirtualSchema martInVirtualSchema = null;
	
	public String datasetName = null;
	private String timeStampLastUpdate = null;
	private Boolean visible = null;
	private String datasetType = null;
	public String martServiceLine = null;
	
	@Override
	public String toString() {
		return "(" + virtualSchema + ")" + MyUtils.TAB_SEPARATOR + "(" + martInVirtualSchema.martName + ")" + MyUtils.TAB_SEPARATOR + 
		datasetName + MyUtils.TAB_SEPARATOR + timeStampLastUpdate + MyUtils.TAB_SEPARATOR + datasetType + MyUtils.TAB_SEPARATOR + visible;
	}
	public DatasetInMart(MartInVirtualSchema mart,
			String datasetName, String timeStampLastUpdate, String visibility, String datasetType, String martServiceLine) {
		super();
		this.martInVirtualSchema = mart;
		this.virtualSchema = mart.getVirtualSchema();
		this.datasetName = datasetName;
		this.timeStampLastUpdate = timeStampLastUpdate!=null ? timeStampLastUpdate : MyConstants.NOT_APPLICABLE;
		this.visible = MartConfiguratorUtils.binaryDigitToBoolean(visibility);
		this.datasetType = datasetType;
		this.martServiceLine = martServiceLine;
	}
	public boolean sameVersion(DatasetInMart datasetInMart) {
		return this.timeStampLastUpdate.equals(datasetInMart.timeStampLastUpdate) ||
		(this.timeStampLastUpdate.equals(MartServiceConstants.ATTRIBUTE_TIMESTAMP) && datasetInMart.timeStampLastUpdate.equals(MyConstants.NOT_APPLICABLE)) ||
		(this.timeStampLastUpdate.equals(MyConstants.NOT_APPLICABLE) && datasetInMart.timeStampLastUpdate.equals(MartServiceConstants.ATTRIBUTE_TIMESTAMP));
	}
	@Override
	public boolean equals(Object object) {
		DatasetInMart datasetInMart = (DatasetInMart)object;
		return this.datasetName.equals(datasetInMart.datasetName);
	}
	public String getDatasetType() {
		return datasetType;
	}
	public Boolean getVisible() {
		return visible;
	}

	public String getVirtualSchema() {
		return virtualSchema;
	}
	public MartInVirtualSchema getMartInVirtualSchema() {
		return martInVirtualSchema;
	}
}
