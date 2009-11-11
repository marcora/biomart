package org.biomart.transformation.helpers;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.helpers.DatabaseParameter;
import org.biomart.old.martService.Configuration;
import org.biomart.transformation.Transformation;
import org.jdom.Document;

public class TransformationGeneralVariable {
	
	private Map<String, Configuration> webServiceConfigurationMap = null;
	private String databaseVersion = null;
	private MartServiceIdentifier portalIdentifier = null;
	private DatabaseCheck databaseCheck = null;
	private String transformationsGeneralOutput = null;
	private HashMap<String, Document> datasetNameToWebServiceJdomDocument = null;
	private HashMap<String, Transformation> transformedTemplatesMap = null;
	private HashMap<String, Transformation> transformedDatasetsMap = null;
	private HashMap<String, String> plainDatasetNameToTransformedDatasetName = null;
	private String datasetGeneralOutputFolderPathAndName = null;

	public TransformationGeneralVariable(String databaseVersion, Map<String, Configuration> webServiceConfigurationMap, MartServiceIdentifier portalIdentifier,
			String transformationsGeneralOutput, String datasetGeneralOutputFolderPathAndName, 
			DatabaseParameter databaseParameter, String[] databaseNames) throws TechnicalException {
		
		this.databaseCheck = new DatabaseCheck(databaseParameter, databaseNames, transformationsGeneralOutput, datasetGeneralOutputFolderPathAndName);
		this.databaseVersion = databaseVersion;
		this.portalIdentifier = portalIdentifier;
		this.webServiceConfigurationMap = webServiceConfigurationMap;
		this.transformationsGeneralOutput = transformationsGeneralOutput;
		this.datasetNameToWebServiceJdomDocument = new HashMap<String, Document>();
		this.transformedDatasetsMap = new HashMap<String, Transformation>();
		this.transformedTemplatesMap = new HashMap<String, Transformation>();
		this.plainDatasetNameToTransformedDatasetName = new HashMap<String, String>();
		this.datasetGeneralOutputFolderPathAndName = datasetGeneralOutputFolderPathAndName;
	}
	
	public MartServiceIdentifier getPortalIdentifier() {
		return portalIdentifier;
	}
	
	public DatabaseCheck getDatabaseCheck() {
		return databaseCheck;
	}

	public Map<String, Configuration> getWebServiceConfigurationMap() {
		return webServiceConfigurationMap;
	}
	public String getDatasetGeneralOutputFolderPathAndName() {
		return datasetGeneralOutputFolderPathAndName;
	}

	public void addDatasetToPlainDatasetNameToTransformedDatasetName(String pointedDatasetPlainName, String datasetOrTemplateToTransform) {
		this.plainDatasetNameToTransformedDatasetName.put(pointedDatasetPlainName, datasetOrTemplateToTransform);
	}
	public void removeDatasetToPlainDatasetNameToTransformedDatasetName(String datasetOrTemplateName) {
		for (Iterator<String> it = this.plainDatasetNameToTransformedDatasetName.keySet().iterator(); it.hasNext();) {
			String pointedDatasetPlainName = it.next();
			String datasetOrTemplateToTransform = 
				this.plainDatasetNameToTransformedDatasetName.get(pointedDatasetPlainName);
			if (datasetOrTemplateToTransform.equals(datasetOrTemplateName)) {
				this.plainDatasetNameToTransformedDatasetName.remove(pointedDatasetPlainName);
			}
		}
	}
	public void updateTransformedDatasetNameInPlainDatasetNameToTransformedDatasetName(
			String datasetOrTemplateName, String transformedDatasetName, boolean isWebService) {
		boolean atLeastOne = false;
		for (Iterator<String> it = this.plainDatasetNameToTransformedDatasetName.keySet().iterator(); it.hasNext();) {
			String pointedDatasetPlainName = it.next();
			String datasetOrTemplateNameTmp = this.plainDatasetNameToTransformedDatasetName.get(pointedDatasetPlainName);
			if (datasetOrTemplateNameTmp.equals(datasetOrTemplateName)) {
				atLeastOne = true;
				/*MyUtils.checkStatusProgram(datasetOrTemplateNameTmp!=null && 
						(isWebService || (!datasetOrTemplateNameTmp.equals(transformedDatasetName))),
						(datasetOrTemplateNameTmp!=null) + ", " + isWebService + ", " + (!datasetOrTemplateNameTmp.equals(transformedDatasetName)) + ", " +
						datasetOrTemplateNameTmp + ", " + transformedDatasetName);*/
				this.plainDatasetNameToTransformedDatasetName.put(pointedDatasetPlainName, transformedDatasetName);
			}
		}
		MyUtils.checkStatusProgram(atLeastOne);
	}
	public String getTransformedDatasetName(String pointedDatasetPlainName) {
		return this.plainDatasetNameToTransformedDatasetName.get(pointedDatasetPlainName);
	}
	
	// Adds
	public void addTransformedDatasets(String datasetOrTemplateName, Transformation transformation) {
		this.transformedDatasetsMap.put(datasetOrTemplateName, transformation);
	}
	public void addTransformedTemplates(String datasetOrTemplateName, Transformation transformation) {
		this.transformedTemplatesMap.put(datasetOrTemplateName, transformation);
	}
	
	// Contains
	public boolean transformedDatasetsContains(String datasetOrTemplateName) {
		return this.transformedDatasetsMap.keySet().contains(datasetOrTemplateName);
	}
	public boolean transformedTemplatesContains(String datasetOrTemplateName) {
		return this.transformedTemplatesMap.keySet().contains(datasetOrTemplateName);
	}
	
	// Gets
	public Transformation getFromTransformedDatasets(String datasetOrTemplateName) {
		return this.transformedDatasetsMap.get(datasetOrTemplateName);
	}
	public Transformation getFromTransformedTemplates(String datasetOrTemplateName) {
		return this.transformedTemplatesMap.get(datasetOrTemplateName);
	}

	// Get All
	public Collection<Transformation> getAllTransformedDatasets() {
		return this.transformedDatasetsMap.values();
	}
	public Collection<Transformation> getAllTransformedTemplates() {
		return this.transformedTemplatesMap.values();
	}
	
	public HashMap<String, Document> getDatasetNameToWebServiceJdomDocument() {
		return datasetNameToWebServiceJdomDocument;
	}
	public String getTransformationsGeneralOutput() {
		return transformationsGeneralOutput;
	}
	public String getDatabaseVersion() {
		return databaseVersion;
	}
	
	/*// Display (debug)
	public String getFromTransformedDatasets() {
		for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
			String s = it.next();
			String s2 = map.get();
		}
		return this.transformedDatasetsMap.get(datasetOrTemplateName);
	}
	public String getFromTransformedTemplates() {
		return this.transformedTemplatesMap.get(datasetOrTemplateName);
	}*/
	
}
