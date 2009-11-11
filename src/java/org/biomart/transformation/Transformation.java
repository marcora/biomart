package org.biomart.transformation;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.helpers.CurrentPath;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.Location;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartRegistry;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.objects.objects.types.LocationType;
import org.biomart.old.martService.Configuration;
import org.biomart.old.martService.objects.DatasetInMart;
import org.biomart.old.martService.objects.MartInVirtualSchema;
import org.biomart.transformation.helpers.DatabaseCheck;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationGeneralVariable;
import org.biomart.transformation.helpers.TransformationHelper;
import org.biomart.transformation.helpers.TransformationParameter;
import org.biomart.transformation.helpers.TransformationVariable;
import org.biomart.transformation.oldXmlObjects.OldDatasetConfig;
import org.jdom.Document;
import org.jdom.Element;

public class Transformation {

	private TransformationGeneralVariable general = null;
	private TransformationParameter params = null;
	private TransformationVariable vars = null;
	private TransformationHelper help = null;
	
	private Boolean valid = null;
	
	public Boolean isValid() {
		return valid;
	}
	public Transformation(TransformationGeneralVariable general, TransformationParameter params) {
		this.general = general;
		this.params = params;
		this.vars = new TransformationVariable();
		this.help = new TransformationHelper(general, vars);
	}
		
	private OldDatasetConfig oldDatasetConfig = null;
	private MartRegistry martRegistry = null;
	private Document transformedDocument = null;

	
	public boolean transform() throws FunctionalException, TechnicalException {
		Document xmlDocument = fetchXmlConfiguration();
		return transform(xmlDocument);
	}
	public Document fetchXmlConfiguration() throws TechnicalException {

		Document xmlDocument = null;
		
		// Fetch xml (and database info if a template)
		if (!params.isWebservice()) {
			DatabaseCheck databaseCheck = general.getDatabaseCheck();
			String templateName = params.getTemplateName();
			vars.setTemplate(true);	// TODO fusion with isWebservice? We may want to transform a non-template with RDBS...		
			xmlDocument = databaseCheck.fetchTemplateXml(params.getVirtualSchema(), templateName);
			databaseCheck.fetchTableList(templateName);
			databaseCheck.fetchTableColumnMap(templateName);	
		} else {
			vars.setTemplate(false);	// Cannot be template if web service
			xmlDocument = general.getDatasetNameToWebServiceJdomDocument().get(params.getDatasetName());
								// Has been either added before launching the complete transformation, 
								// or added before launching a sub-transformation (for pointers)
			MyUtils.checkStatusProgram(null!=xmlDocument);		
		}
		return xmlDocument;
	}
	public boolean transform(Document xmlDocument) throws FunctionalException, TechnicalException {
		
		// Populate old objects
		PopulateOldObjects populateOldObjects = new PopulateOldObjects(xmlDocument, vars.isTemplate());
		this.oldDatasetConfig = populateOldObjects.populate();	//TODO use params fully
		this.valid = null!=this.oldDatasetConfig;	// If not a TableSet
		if (!this.valid) {
			System.out.println("invalid dataset: GenomicSequence");
			return false;
		}
		
		// Write file for old config
		/*MyUtils.writeFile(params.getOuputOldConfigurationPathAndName(), "oldDatasetConfig = " + oldDatasetConfig.toString());*/

		// Create empty Mart Registry with no dataset (added later)
		Mart mart = createEmptyConfiguration();
		
		// Transform the dataset
		DatasetTransformation datasetTransformation = new DatasetTransformation(general, params, vars, help);
		Dataset transformedDataset = datasetTransformation.transformDataset(this.oldDatasetConfig);
		mart.addDataset(transformedDataset);
		
		// If template, change all dataset names and their references with the fixed part of their name
		if (!params.isWebservice()) {
			updateDatasetNames();
		}
		
		// Generate the XML structure and output XML as file and on STDOUT
		xmlGeneration();
		
		return true;
	}
	
	/**
	 * Remove partition info (should be done differently)
	 */
	private void updateDatasetNames() {
		
		List<Transformation> allTransformedTemplates = new ArrayList<Transformation>(general.getAllTransformedTemplates());
		allTransformedTemplates.add(this);
		
		Map<String, String> renamingMap = new HashMap<String, String>();
		for (Transformation transformation : allTransformedTemplates) {
			Dataset dataset = transformation.vars.getDataset();;
			String newDatasetName = transformation.vars.getFixedPart();
			newDatasetName = (newDatasetName.startsWith("_") ? newDatasetName.substring(1) : newDatasetName);
			renamingMap.put(dataset.getName(), newDatasetName);
		}
		
		for (Transformation transformation : allTransformedTemplates) {
			Dataset dataset = transformation.vars.getDataset();;
			dataset.setName(renamingMap.get(dataset.getName()));
			
			HashMap<String, Attribute> attributeMap = transformation.vars.getAttributeMap();
			for (Iterator<Attribute> it = attributeMap.values().iterator(); it.hasNext();) {
				Attribute attribute = it.next();
				if (attribute.getPointer()) {
					attribute.setDatasetName(renamingMap.get(attribute.getDatasetName()));
				}
			}
			
			HashMap<String, Filter> filterMap = transformation.vars.getFilterMap();
			for (Iterator<Filter> it = filterMap.values().iterator(); it.hasNext();) {
				Filter filter = it.next();
				if (filter.getPointer()) {
					filter.setDatasetName(renamingMap.get(filter.getDatasetName()));
				}
			}
		}
	}
	
	private Mart createEmptyConfiguration() throws TechnicalException, FunctionalException {
		
		this.martRegistry = new MartRegistry();
        
		// Get configuration in order to specify appropriate values in <location> and <mart>
		Location location = null;
		Mart mart = null;
		if (params.isWebservice()) {
			Map<String, Configuration> webServiceConfigurationMap = general.getWebServiceConfigurationMap();
			
			String formattedTrueHostMartService = params.getTrueHostIdentifier().formatMartServiceUrl();
			Configuration configuration = webServiceConfigurationMap.get(formattedTrueHostMartService);		
			
			// In cases where a dataset from a remote host points to a dataset from another remote host that isn't from the portal.. 
			// TODO not sure how that's even possible...
			if (null==configuration) {
				TransformationMain.expandWebServiceConfigurationMap(params.getTrueHostIdentifier());
			}
			MyUtils.checkStatusProgram(null!=configuration, formattedTrueHostMartService + ", " + webServiceConfigurationMap.keySet());
			
			DatasetInMart datasetInMart = help.getDatasetInMart(configuration, params.getVirtualSchema(), params.getDatasetName());
			
			MyUtils.checkStatusProgram(null!=datasetInMart, 
					formattedTrueHostMartService + ", " + params.getVirtualSchema() + ", " + params.getDatasetName());
			MartInVirtualSchema martInVirtualSchema = datasetInMart.getMartInVirtualSchema();
			
			location = new Location(martInVirtualSchema.host, martInVirtualSchema.host, martInVirtualSchema.host, 
					true, martInVirtualSchema.host, TransformationConstants.DEFAULT_USER, LocationType.URL);
			mart = new Mart(martInVirtualSchema.martName, martInVirtualSchema.displayName, martInVirtualSchema.displayName, 
					martInVirtualSchema.visible, -1);	// TODO -1 for now
		} else {
			DatabaseCheck databaseCheck = general.getDatabaseCheck();
			String databaseName = databaseCheck.getDatabaseName(params.getTemplateName());
			String host = databaseCheck.getDatabaseParameter().getDatabaseHost();
			location = new Location(databaseName, databaseName, databaseName, true, host, TransformationConstants.DEFAULT_USER, LocationType.RDBMS);
			Integer version = extractVersion(databaseName);
			mart = new Mart(databaseName, databaseName, databaseName, true, version);
		}
		
		location.addMart(mart);
		martRegistry.addLocation(location);
		
		vars.setCurrentPath(new CurrentPath(location, mart));
		
		return mart;
	}
		
	private void xmlGeneration() throws TechnicalException {
		// Generate all the xml elements
		Element newRootElement = this.martRegistry.generateXml();
		
		// Write the file
		this.transformedDocument = MyUtils.writeXmlFile(newRootElement, params.getOutputXmlFilePathAndName());
	}
	
	/**
	 * To make it easier to access the name from the transformation object
	 * @return
	 */
	public String getTransformedDatasetName() {
		String name = null;
		Dataset dataset = vars.getDataset();
		if (null!=dataset) {
			name = dataset.getName(); 
		}
		return name;
	}
	
	public org.biomart.objects.objects.Element getElementFromTransformation(String virtualSchema, String datasetName, String elementName, boolean getAttribute) {
		MyUtils.checkStatusProgram(datasetName.equals(vars.getDataset().getName()) && virtualSchema.equals(params.getVirtualSchema()));
															// Check virtual schema and dataset name matche
		org.biomart.objects.objects.Element element = getAttribute ? 
				vars.getAttributeMap().get(elementName) : vars.getFilterMap().get(elementName);
		return element;
	}

	
	public MartRegistry getMartRegistry() {
		return this.martRegistry;
	}
	public PartitionTable getMainPartitionTable() {
		return vars.getMainPartitionTable();
	}
	public Document getTransformedDocument() {
		return transformedDocument;
	}
	private Integer extractVersion(String databaseName) {
		Integer version = null;
		String[] split = databaseName.split("_");
		String potentialVersion = split[split.length-1];
		try {
			version = Integer.valueOf(potentialVersion);
		} catch (NumberFormatException e) {
			version = -1;
		}
		return version;
	}
}

