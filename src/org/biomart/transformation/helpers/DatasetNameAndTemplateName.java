package org.biomart.transformation.helpers;

import java.io.Serializable;

public class DatasetNameAndTemplateName implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -340967590818811955L;
	private String datasetName = null;
	private String templateName = null;
	
	public DatasetNameAndTemplateName(String datasetName, String templateName) {
		super();
		this.datasetName = datasetName;
		this.templateName = templateName;
	}
	public String getDatasetName() {
		return datasetName;
	}
	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}
	public String getTemplateName() {
		return templateName;
	}
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
}
