package org.biomart.transformation.helpers;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;


public class TransformationParameter {

	public static void main(String[] args) {}

	// Originals
	private Boolean webservice = null;
	
	private MartServiceIdentifier trueHostIdentifier = null;
	private String virtualSchema = null;
	private String datasetName = null;
	private String templateName = null;
	
	private String dataFolderName = null;
		
	// Infered
	private String datasetOutputFolderPathAndName = null;
	private String dataFolderPath = null;
	
	private String datasetOrTemplateName = null;
	
	private FileWriter errorFileWriter = null;
	
	public TransformationParameter(Boolean webservice, MartServiceIdentifier trueHostIdentifier, 
			String virtualSchema, String datasetName, String templateName,
			TransformationGeneralVariable general, String dataFolderName) throws TechnicalException {
		super();
		
		this.webservice = webservice;
		MyUtils.checkStatusProgram((webservice && datasetName!=null && templateName==null) || 
				(!webservice && datasetName==null && templateName!=null));
		
		this.trueHostIdentifier = trueHostIdentifier;
		this.virtualSchema = virtualSchema;
		
		this.datasetName = datasetName;
		this.templateName = templateName;
		this.datasetOrTemplateName = webservice ? datasetName : templateName;
	
		this.dataFolderName = dataFolderName;
		
		// Infered
		this.datasetOutputFolderPathAndName = general.getDatasetGeneralOutputFolderPathAndName() + datasetOrTemplateName + "/";
		new File(this.datasetOutputFolderPathAndName).mkdirs();
		this.dataFolderPath = this.datasetOutputFolderPathAndName + this.dataFolderName;
		new File(this.dataFolderPath).mkdirs();
		
		File errorFile = new File(this.datasetOutputFolderPathAndName + TransformationConstants.PROPERTY_ERROR_FILE_NAME);
		try {
			this.errorFileWriter = new FileWriter(errorFile);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
	}
	
	public FileWriter getErrorFileWriter() {
		return this.errorFileWriter;
	}
	
	public MartServiceIdentifier getTrueHostIdentifier() {
		return trueHostIdentifier;
	}

	public String getOutputXmlFilePathAndName() {
		return this.datasetOutputFolderPathAndName + "new_" + this.datasetOrTemplateName + ".xml";
	}
	public String getOuputOldConfigurationPathAndName() {
		return this.datasetOutputFolderPathAndName + "old_" + this.datasetOrTemplateName;
	}
	public String getDefaultDataFolderPath() {
		return this.dataFolderPath;
	}
	public Boolean isWebservice() {
		return webservice;
	}
	
	public String getVirtualSchema() {
		return virtualSchema;
	}
	public String getDatasetName() {
		return datasetName;
	}
	public String getTemplateName() {
		return templateName;
	}

	public String getDataFolderName() {
		return dataFolderName;
	}

	public String getDatasetOrTemplateName() {
		return datasetOrTemplateName;
	}
}
