package org.biomart.old.martService;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;


import org.biomart.common.general.constants.MyConstants;
import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.Timer;
import org.biomart.old.bioMartPortalLinks.LinkableDataset;
import org.biomart.old.martService.objects.DatasetInMart;
import org.biomart.old.martService.objects.Exportable;
import org.biomart.old.martService.objects.Importable;
import org.biomart.old.martService.objects.MartInVirtualSchema;
import org.biomart.old.martService.restFulQueries.objects.Field;
import org.biomart.old.martService.restFulQueries.objects.Filter;
import org.biomart.transformation.TransformationMain;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


/**
 * 
 * 
 * importable -> if (linkVersion!=null && linkVersion.startsWith("*") && linkVersion.endsWith("*")) {z2++;zSet.add(linkVersion);}
		z2 = 179
        zSet = [*link_version2*]
                ==> there are 179 importables named "*link_version2*"
 * @author anthony
 *
 */
public class Configuration implements Serializable {
	
	private static final long serialVersionUID = 4458674743899312040L;
	
/*public static String ds1 = "wormbase_gene";
public static String ds2 = "interaction";*/
/*public static String ds1 = "reaction";
public static String ds2 = "pathway";*/
public static String DEBUG_DATASET1 = "celegans_gene_ensembl";
public static String DEBUG_DATASET2 = "olatipes_gene_ensembl";

	// Constants
	//public static final Boolean USE_MART_SERVICE_REGISTRY = Boolean.FALSE;
	/*private static final String REAL_REGISTRY = "/home/anthony/biomart_releases/CentralPortalRegistryFetchHelper/centralPortalRegistry.xml";
	private static final String REGISTRY_STRING_URL = USE_MART_SERVICE_REGISTRY ? 
			"http://" + MART_SERVICE_URL + "?type=registry" : "file://" + REAL_REGISTRY;*/
	
	public static final String WEB_SERVICE_DEFAULT_VIRTUAL_SCHEMA_NAME = "! UNVAILABLE VIA WEB SERVICE !";

	// Variables
	private Boolean dbRegistry = null;
	private transient SAXBuilder builder = null;
	
	public Map<String, Set<MartInVirtualSchema>> virtualSchemaMartSetMap = null;
	public Map<String, List<DatasetInMart>> martDatasetListMap = null;	// FIXME pblm if 2 marts have the same name within 2 different VS
	
	public List<LinkableDataset> datasetList = null;
	
	private Integer totalMarts = 0;
	private Integer totalDatasets = 0;
	private Integer totalLinkableDatasets = 0;
	
	private Map<String, MartInVirtualSchema> martByName = null;
	
	private String martServiceUrl = null;
	public String registryUrlString = null;
	public String martStringUrlPrefix = null;
	public String datasetStringUrlPrefix = null;
	public String virtualSchemaParameter = null;
	
	public Timer martSetTimer = null;
	public Timer datasetSetTimer = null;
	public Timer portableSetTimer = null;
	
	public Configuration(String martServiceUrl) {
		this(martServiceUrl, true);
	}
	public Configuration(String martServiceUrl, boolean dbRegistry) {
		this.dbRegistry = dbRegistry;
		this.builder = new SAXBuilder();
		this.virtualSchemaMartSetMap = new HashMap<String, Set<MartInVirtualSchema>>();
		this.martDatasetListMap = new HashMap<String, List<DatasetInMart>>();
		this.datasetList = new ArrayList<LinkableDataset>();
		this.totalDatasets = 0;
		
		this.martServiceUrl = martServiceUrl;
		this.registryUrlString = martServiceUrl + MartServiceConstants.MART_SERVICE_REGISTRY_PARAMETER;
		this.martStringUrlPrefix = martServiceUrl + MartServiceConstants.MART_SERVICE_DATASET_LIST_PARAMETER;
		this.datasetStringUrlPrefix = martServiceUrl + MartServiceConstants.MART_SERVICE_CONFIGURATION_PARAMETER;
		this.virtualSchemaParameter = MartServiceConstants.MART_SERVICE_VIRTUAL_SCHEMA_PARAMETER;
		
		this.martSetTimer = new Timer();
		this.datasetSetTimer = new Timer();
		this.portableSetTimer = new Timer();
	}
	
	public void serialize(String serialFilePathAndName) throws TechnicalException {
		MyUtils.writeSerializedObject(this.martByName, serialFilePathAndName);
	}
	@SuppressWarnings("unchecked")
	public void deserialize(String serialFilePathAndName) throws TechnicalException {
		this.martByName = (Map<String, MartInVirtualSchema>)MyUtils.readSerializedObject(serialFilePathAndName);		
		System.out.println("Quick check: " + this.martByName.size());
	}
	
	public Integer getTotalDatasets() {
		return totalDatasets;
	}

	public Integer getTotalLinkableDatasets() {
		return totalLinkableDatasets;
	}

	public Integer getTotalMarts() {
		return totalMarts;
	}
	
	public MartInVirtualSchema getMartByName(String martName) {
MyUtils.checkStatusProgram(this.martByName!=null, "", true);		
		return this.martByName.get(martName);
	}
	/*public DatasetInMart getDatasetByName(String virtualSchemaName, String datasetName) {
		VirtualSchemaDatasetCouple couple = new VirtualSchemaDatasetCouple(virtualSchemaName, datasetName);
		return this.datasetByName.get(couple);
	}*/
	
	public void displayStatistics () {
		System.out.println("this.virtualSchemaMartSetMap.size() = " + this.virtualSchemaMartSetMap.size());
		System.out.println("this.martDatasetListMap.size() = " + this.martDatasetListMap.size());
		System.out.println("this.linkableDatasetList.size() = " + this.datasetList.size());
		
		System.out.println("this.martByName.size() = " + this.martByName.size());
		
		System.out.println("this.martSetTimer = " + this.martSetTimer);
		System.out.println("this.datasetSetTimer = " + this.datasetSetTimer);
		System.out.println("this.portableSetTimer = " + this.portableSetTimer);
	}
	
	public void fetchDatasets() throws IOException, JDOMException, FunctionalException, InterruptedException {
        
 		// Fetch datasets for each mart
 		this.datasetSetTimer.startTimer();
 		totalMarts = 0;
 		for (Iterator<Set<MartInVirtualSchema>> it = virtualSchemaMartSetMap.values().iterator(); it.hasNext();) {
 			totalMarts+=it.next().size();
 		}
 		MyUtils.showProgress(totalMarts);

 		for (Iterator<String> it = virtualSchemaMartSetMap.keySet().iterator(); it.hasNext();) {
			String serverVirtualSchema = it.next();
			Set<MartInVirtualSchema> martSet = virtualSchemaMartSetMap.get(serverVirtualSchema);
	 		for (MartInVirtualSchema mart : martSet) {
	 			// Show progress
	 	    	System.out.print(".");
 	    	
				// Fetch
	 	    	String martStringUrl = getLocalMartServiceStringUrl(mart);	 	    	
	        	fetchDatasetSet(martStringUrl , serverVirtualSchema, mart);
	 		}
        }
 		this.datasetSetTimer.stopTimer();
 		
 		MyUtils.println(MyUtils.LINE_SEPARATOR + "totalMarts = " + totalMarts);
 		//buildDatasetByNameMap();
	}

	public String getLocalMartServiceStringUrl(MartInVirtualSchema mart) throws UnsupportedEncodingException {
		return this.martStringUrlPrefix + URLEncoder.encode(mart.martName, MyConstants.URL_ENCODING);
	}
	
	@SuppressWarnings("unchecked")
	public void fetchMartSet() throws MalformedURLException, JDOMException, IOException, InterruptedException, FunctionalException {
		
		this.martSetTimer.startTimer();
	
		URL registryURL = null;
		if (this.dbRegistry) {
			String filePathAndName = 
		//		MyUtils.OUTPUT_FILES_PATH
				"./" + "centralPortal.xml";

			/*private static final String GETREGISTRYSQL1 = "select xml, compressed_xml from ";
			central_registry
			private static final String GETREGISTRYSQL2 = ".meta_registry limit 1";*/
			
				//cd ~/biomart_releases/martj-0.7_new_release_2/martj-0.7/bin
				//martregistrydbtool.sh -f ~/Desktop/cp9.xml -H martdb.ebi.ac.uk -P 3306 -I central_registry -S central_registry -U anonymous
				
			String command = "/home/anthony/biomart_releases/martj-0.7_new_release_2/martj-0.7/bin/martregistrydbtool.sh " +
					"-f " + filePathAndName + " -H martdb.ebi.ac.uk -P 3306 -I central_registry -S central_registry -U anonymous";
			StringBuffer sb = MyUtils.runCommand(command);
			if (sb.equals("All Complete")) {
				throw new FunctionalException(sb.toString());
			}
			
			new File(filePathAndName).deleteOnExit();
			this.registryUrlString = MyConstants.FILE_SYSTEM_PROTOCOL + filePathAndName;
		}
		
		registryURL = new URL(this.registryUrlString);
		Document registryDocument = builder.build(registryURL);
		
		Element rootElement = registryDocument.getRootElement();
		
		if (!this.dbRegistry) {
			List<Element> locationList = rootElement.getChildren();
			for (Element location : locationList) {
				fetchMart(location);
			}
		} else {
			List<Element> virtualSchemaList = rootElement.getChildren();
			for (Element virtualSchema : virtualSchemaList) {
				String virtualSchemaName = virtualSchema.getAttributeValue(MartServiceConstants.XML_ATTRIBUTE_VIRTUAL_SCHEMA_NAME);
				
				List<Element> locationList = virtualSchema.getChildren();
				for (Element location : locationList) {
					fetchMart(location, virtualSchemaName);
				}
			}
		}
		this.martSetTimer.stopTimer();
		
 		buildMartByNameMap();
	}
	
	private void fetchMart(Element location) {
		fetchMart(location, WEB_SERVICE_DEFAULT_VIRTUAL_SCHEMA_NAME);
	}
	private void fetchMart(Element location, String virtualSchemaName) {
		String type = location.getName(); 
		String martName = location.getAttribute(MartServiceConstants.XML_ATTRIBUTE_MART_NAME).getValue();
		String serverVirtualSchema = location.getAttributeValue(MartServiceConstants.XML_ATTRIBUTE_VIRTUAL_SCHEMA);
		
		Attribute attributeDatabaseType = location.getAttribute(MartServiceConstants.XML_ATTRIBUTE_DATABASE_TYPE);
		String databaseType = attributeDatabaseType!=null ? attributeDatabaseType.getValue() : null;
		
		String displayName = location.getAttribute(MartServiceConstants.XML_ATTRIBUTE_DISPLAY_NAME).getValue(); 
		
		String host = location.getAttribute(MartServiceConstants.XML_ATTRIBUTE_HOST).getValue();
		
		String port = location.getAttribute(MartServiceConstants.XML_ATTRIBUTE_PORT).getValue();
if (port.equals("8000")) port = "80";		
		
		Attribute attributeUser = location.getAttribute(MartServiceConstants.XML_ATTRIBUTE_USER);
		String user = attributeUser!=null ? attributeUser.getValue() : null;
		
		Attribute attributePassword = location.getAttribute(MartServiceConstants.XML_ATTRIBUTE_PASSWORD);		
		String password = attributePassword!=null ? attributePassword.getValue() : null;

		Attribute attributeDatabaseName = location.getAttribute(MartServiceConstants.XML_ATTRIBUTE_DATABASE_NAME);		
		String databaseName = attributeDatabaseName!=null ? attributeDatabaseName.getValue() : null;
		
		Attribute attributePath = location.getAttribute(MartServiceConstants.XML_ATTRIBUTE_PATH);
		String path = attributePath!=null ? attributePath.getValue() : MartServiceConstants.DEFAULT_PATH_TO_MART_SERVICE;
		
		String trimmedHost = MyUtils.trimHost(host);
		String trimmedPath = path.startsWith(MyUtils.FILE_SEPARATOR) ? path.substring(1) : path;
		Boolean local = this.martServiceUrl.contains(trimmedHost) && this.martServiceUrl.contains(port) && this.martServiceUrl.contains(trimmedPath);

		String visible = location.getAttribute(MartServiceConstants.XML_ATTRIBUTE_VISIBLE).getValue();
		
		String includeDatasets = location.getAttribute(MartServiceConstants.XML_ATTRIBUTE_INCLUDE_DATASETS).getValue();
				
		/*// Remove lagging "/" at the end of host if any
		host = host.endsWith(MyConstants.URL_SEPARATOR) ? host.substring(0, host.length()-2) : host;*/
		
		/*// Adding first "/" at the beginning of the path if none
		path = !path.startsWith(MyConstants.URL_SEPARATOR) ? MyConstants.URL_SEPARATOR + path : path;*/
		
		String virtualSchemaTmp = this.dbRegistry || !local ? virtualSchemaName : serverVirtualSchema;
		Set<MartInVirtualSchema> martSet = this.virtualSchemaMartSetMap.get(virtualSchemaTmp);
		if (null==martSet) {
			martSet = new TreeSet<MartInVirtualSchema>();
		}
		martSet.add(new MartInVirtualSchema(virtualSchemaTmp, serverVirtualSchema, local,
				type, martName, databaseType, displayName, host, port, user, password, databaseName, path, visible, includeDatasets, location));
		this.virtualSchemaMartSetMap.put(virtualSchemaTmp, martSet);
	}
	
	public void fetchDatasetSet (String martStringUrl, String serverVirtualSchema, MartInVirtualSchema mart) throws FunctionalException, MalformedURLException, IOException {

    	URL martURL = new URL(martStringUrl);
    	
    	StringBuffer martURLContent = MyUtils.copyUrlContentToStringBuffer(martURL);
		if (-1!=martURLContent.indexOf("Problem retrieving datasets")) {
    		throw new FunctionalException("Problem retrieving datasets, mart = " + mart + " with URL " + martURL);
    	}
    	
		StringTokenizer martStringTokenizer = new StringTokenizer(martURLContent.toString(), MyUtils.LINE_SEPARATOR);
		
		List<DatasetInMart> martDatasetList = new ArrayList<DatasetInMart>();
		while (martStringTokenizer.hasMoreTokens()) {
			String line = martStringTokenizer.nextToken();
			String[] values = line.split(MyUtils.TAB_SEPARATOR);
			String datasetType = values[MartServiceConstants.TAB_PAGE_DATASET_TYPE_ATTRIBUTE_INDEX];
			if (values!=null && values.length>=MartServiceConstants.TAB_PAGE_MINIMUM_ATTRIBUTES && 
					(MartServiceConstants.ATTRIBUTE_TABLE_SET.equals(datasetType) || 
							MartServiceConstants.ATTRIBUTE_GENOMIC_SEQUENCE.equals(datasetType))) {
				String visibility = values.length>=MartServiceConstants.TAB_PAGE_MAX_ATTRIBUTES ? 
						values[MartServiceConstants.TAB_PAGE_VISIBILITY_ATTRIBUTE_INDEX] : null;
				String timeStamp = values.length>=MartServiceConstants.TAB_PAGE_MAX_ATTRIBUTES ? 
						values[MartServiceConstants.TAB_PAGE_TIMESTAMP_ATTRIBUTE_INDEX] : null;
				DatasetInMart datasetInMart = new DatasetInMart(mart, 
						values[MartServiceConstants.TAB_PAGE_DATASET_NAME_ATTRIBUTE_INDEX], 
						timeStamp, visibility, datasetType, line);
				martDatasetList.add(datasetInMart);
			}
		}
		this.totalDatasets+=martDatasetList.size();
		this.martDatasetListMap.put(mart.martName, martDatasetList);
	}
	
	private LinkableDataset getConfiguration(String serverVirtualSchema, String martName, DatasetInMart dataset) 
	throws TechnicalException, MalformedURLException, JDOMException, IOException {
		Document datasetDocument = null;
		datasetDocument = getXml(serverVirtualSchema, dataset.datasetName);
		
		/*// Check that DocType=DatasetConfig
		DocType docType = datasetDocument.getDocType();
		if (docType==null || !docType.getElementName().equals(MartServiceConstants.DATASET_CONFIGURATION_DOC_TYPE)) {
			return null;
		}   */    
		
		@SuppressWarnings("unchecked")
		Element rootElement = datasetDocument.getRootElement();
		
		String bioMartVersion = rootElement.getAttributeValue(MartServiceConstants.XML_ATTRIBUTE_SOFTWARE_VERSION);
		
		@SuppressWarnings("unchecked")
		List<Element> mainSectionList = rootElement.getChildren();
        
        List<Element> elementImportableList = new ArrayList<Element>();
        List<Element> elementExportableList = new ArrayList<Element>();
        List<Element> elementMainTableList = new ArrayList<Element>();
        List<Element> elementKeyList = new ArrayList<Element>();
        List<Element> elementAttributePageList = new ArrayList<Element>();
        List<Element> elementFilterPageList = new ArrayList<Element>();
        for (Element mainSection : mainSectionList) {
        	if (MartServiceConstants.XML_IMPORTABLE.equalsIgnoreCase(mainSection.getName())) {
        		elementImportableList.add(mainSection);
        	} else if (MartServiceConstants.XML_EXPORTABLE.equalsIgnoreCase(mainSection.getName())) {
				elementExportableList.add(mainSection);
			}  else if (MartServiceConstants.XML_FILTER_PAGE.equalsIgnoreCase(mainSection.getName())) {
				elementFilterPageList.add(mainSection);
			}  else if (MartServiceConstants.XML_ATTRIBUTE_PAGE.equalsIgnoreCase(mainSection.getName())) {
				elementAttributePageList.add(mainSection);
			}  else if (MartServiceConstants.XML_MAIN_TABLE.equalsIgnoreCase(mainSection.getName())) {
				elementMainTableList.add(mainSection);
			}  else if (MartServiceConstants.XML_KEY.equalsIgnoreCase(mainSection.getName())) {
				elementKeyList.add(mainSection);
			} else {
				MyUtils.errorProgram("Unknown XML tag: " + mainSection.getName(), true);
			}
        }
        
        // Get at least 1 attribute if there's an importable defined
        String firstAttributeName = null;
        if (elementImportableList!=null && !elementImportableList.isEmpty()) {
	        try {
				Element attributePage = elementAttributePageList.get(0);
				@SuppressWarnings("unchecked")
				Element attributeGroup = ((List<Element>)attributePage.getChildren()).get(0);
				@SuppressWarnings("unchecked")
				Element attributeCollection = ((List<Element>)attributeGroup.getChildren()).get(0);
				@SuppressWarnings("unchecked")
				Element attributeDescription = ((List<Element>)attributeCollection.getChildren()).get(0);
				firstAttributeName = attributeDescription.getAttributeValue(MartServiceConstants.XML_ELEMENT_ATTRIBUTE_DESCRIPTION_INTERNAL_NAME);
			} catch (Exception e) {
				// Output the document, use standard formatter
	            XMLOutputter fmt = new XMLOutputter(Format.getPrettyFormat());
	            System.out.println();
	            fmt.output(datasetDocument, System.out);
	            System.out.println();
	            e.printStackTrace();
	            MyUtils.errorProgram("", true);
			}
	/*System.out.println("firstAttributeName = " + firstAttributeName); 
	MyUtils.pressKeyToContinue();*/
        }
        
        Map<String, org.biomart.old.martService.restFulQueries.objects.Element> attributesByNameMap = fetchElements(dataset.datasetName, elementAttributePageList, true);
        Map<String, org.biomart.old.martService.restFulQueries.objects.Element> filtersByNameMap = fetchElements(dataset.datasetName, elementFilterPageList, false);

        Map<String, Filter> mapRemoteDatasetToFilterPointer = new HashMap<String, Filter>();
        for (Iterator<org.biomart.old.martService.restFulQueries.objects.Element> it = filtersByNameMap.values().iterator(); it.hasNext();) {
			Filter filter = (Filter)it.next();
			if (filter.externalPointer) {
				mapRemoteDatasetToFilterPointer.put(filter.pointerInfo.getPointerDatasetName(), filter);
			}
        }
        
        List<Importable> importableList = getImportables(elementImportableList, firstAttributeName, attributesByNameMap, filtersByNameMap);
        List<Exportable> exportableList = getExportables(elementExportableList, attributesByNameMap, filtersByNameMap);
              
        LinkableDataset linkableDataset = new LinkableDataset(
        		bioMartVersion, serverVirtualSchema, martName, dataset.datasetName, dataset.getVisible(), dataset.getDatasetType(), 
        		importableList, exportableList, mapRemoteDatasetToFilterPointer);
		linkableDataset.attributesByNameMap = attributesByNameMap;
		linkableDataset.filtersByNameMap = filtersByNameMap;
		

        // === Debug ===
		if (dataset.datasetName.equals(DEBUG_DATASET1) || dataset.datasetName.equals(DEBUG_DATASET2) ) {
			System.out.println();
			System.out.println("********************************************************");
			System.out.println("datasetName = " + dataset.datasetName);
			System.out.println("importableList = " + importableList);
			System.out.println("exportableList = " + exportableList);
			System.out.println("********************************************************");
			System.out.println();
		}
        
		return linkableDataset;
	}

	public Document getXml(String virtualSchema, String datasetName) throws TechnicalException {
		Document datasetDocument;
		try {
			String datasetStringURL = this.datasetStringUrlPrefix + URLEncoder.encode(datasetName, "UTF-8"); 

			URL datasetURL = new URL(datasetStringURL);
			
			if (null==builder) {
				builder = new SAXBuilder();
			}
			
			datasetDocument = null;
			try {
				datasetDocument = builder.build(datasetURL);
			} catch (Exception e) {
				datasetStringURL += virtualSchema!=null ? this.virtualSchemaParameter + virtualSchema : "";
				datasetDocument = builder.build(datasetStringURL);
			}
		} catch (UnsupportedEncodingException e) {
			throw new TechnicalException(e);
		} catch (MalformedURLException e) {
			throw new TechnicalException(e);
		} catch (JDOMException e) {
			throw new TechnicalException(e);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		return datasetDocument;
	}

	@SuppressWarnings("unchecked")
	private Map<String, org.biomart.old.martService.restFulQueries.objects.Element> fetchElements(String datasetName, List<Element> elementelementPageList, boolean attribute) {
		Map<String, org.biomart.old.martService.restFulQueries.objects.Element> elementByNameMap = 
        	new HashMap<String, org.biomart.old.martService.restFulQueries.objects.Element>();
        for (Element elementPageElement : elementelementPageList) {
        	List<Element> elementelementGroupList = elementPageElement.getChildren();
        	for (Element elementGroupElement : elementelementGroupList) {
        		List<Element> elementelementCollectionList = elementGroupElement.getChildren();
            	for (Element elementCollectionElement : elementelementCollectionList) {
            		List<Element> elementelementDescriptionList = elementCollectionElement.getChildren();
            		for (Element elementDescription : elementelementDescriptionList) {
                		processElement(datasetName, elementDescription, elementByNameMap, attribute);
                		List<Element> optionList = elementDescription.getChildren();
                		
                		// Only for filters
                		if (optionList!=null && !optionList.isEmpty()) {
	                		for (Element option : optionList) {
	                			processElement(datasetName, option, elementByNameMap, attribute);
	                		}
                		}
                	}
            	}
        	}
        }
        return elementByNameMap;
	}

	private void processElement(String datasetName, Element elementDescription, Map<String, org.biomart.old.martService.restFulQueries.objects.Element> elementByNameMap, boolean attribute) {
		String internalName = elementDescription.getAttributeValue(
				"internalName");
		String tableConstraint = elementDescription.getAttributeValue(
				"tableConstraint");
		String fieldName = elementDescription.getAttributeValue(
				"field");
		String keyName = elementDescription.getAttributeValue(
				"key");
		String hidden = elementDescription.getAttributeValue(
			"hidden");
		
		String pointerDatasetName = elementDescription.getAttributeValue("pointerDataset");
		String pointerElementName = elementDescription.getAttributeValue(attribute ? "pointerAttribute" : "pointerFilter");
		String pointerInterface = elementDescription.getAttributeValue("pointerInterface");
		
		Field field = new Field(tableConstraint, keyName, fieldName);
		org.biomart.old.martService.restFulQueries.objects.Element element = null;
		if (attribute) {
			element = new org.biomart.old.martService.restFulQueries.objects.Attribute(internalName, hidden, field);
		} else {
			element = new org.biomart.old.martService.restFulQueries.objects.Filter(internalName, hidden, field, null);
		}
		if (pointerDatasetName!=null && pointerElementName!=null && pointerInterface!=null) {
			element.setPointer(datasetName, pointerDatasetName, pointerElementName, pointerInterface);
		}
		elementByNameMap.put(internalName.toLowerCase(), element);		// to lower case because a few exceptions
	}

	private List<Importable> getImportables(List<Element> elementImportableList, String firstAttributeName,
			Map<String, org.biomart.old.martService.restFulQueries.objects.Element> attributesByNameMap,
			Map<String, org.biomart.old.martService.restFulQueries.objects.Element> filtersByNameMap) {
		List<Importable> importableList = new ArrayList<Importable>();
        for (Element elementImportable : elementImportableList) {
        	String linkType = elementImportable.getAttribute(MartServiceConstants.XML_ATTRIBUTE_TYPE).getValue();
        	String filtersString = elementImportable.getAttribute(MartServiceConstants.XML_ATTRIBUTE_FILTERS).getValue();
        	String[] filters = filtersString.split(",");
			String linkName = elementImportable.getAttributeValue(MartServiceConstants.XML_ATTRIBUTE_LINK_NAME);
			String linkVersion = elementImportable.getAttributeValue(MartServiceConstants.XML_ATTRIBUTE_LINK_VERSION);
			String defaultValue = elementImportable.getAttributeValue(MartServiceConstants.XML_ATTRIBUTE_DEFAULT);
			
    		Importable importable = new Importable(linkName, linkType, linkVersion, defaultValue, firstAttributeName, filters, 
    				attributesByNameMap, filtersByNameMap);
    		//System.out.println("======================> " + importable.getCompleteAttributesList());    		
    		importableList.add(importable);
        }
		return importableList;
	}

	private List<Exportable> getExportables(List<Element> elementExportableList,
			Map<String, org.biomart.old.martService.restFulQueries.objects.Element> attributesByNameMap,
			Map<String, org.biomart.old.martService.restFulQueries.objects.Element> filtersByNameMap) {
		List<Exportable> exportableList = new ArrayList<Exportable>();
        for (Element elementExportable : elementExportableList) {
        	String linkType = elementExportable.getAttribute(MartServiceConstants.XML_ATTRIBUTE_TYPE).getValue();
        	String attributesString = elementExportable.getAttribute(MartServiceConstants.XML_ATTRIBUTE_ATTRIBUTES).getValue();
        	String[] attributes = attributesString.split(",");
			
			String linkName = elementExportable.getAttributeValue(MartServiceConstants.XML_ATTRIBUTE_LINK_NAME);
			String linkVersion = elementExportable.getAttributeValue(MartServiceConstants.XML_ATTRIBUTE_LINK_VERSION);
			String defaultValue = elementExportable.getAttributeValue(MartServiceConstants.XML_ATTRIBUTE_DEFAULT);
			
			boolean ignore = false;
			for (String attributeName : attributes) {
				if (attributeName.contains(MartServiceConstants.ATTRIBUTE_IN_EXPORTABLE_TO_IGNORE)) {
					ignore = true;
					break;
				}
			}
			
			if (!ignore) {
				Exportable exportable = new Exportable(linkName, linkType, linkVersion, defaultValue, attributes, attributesByNameMap, filtersByNameMap);
				exportableList.add(exportable);
			}
        }
		return exportableList;
	}
	
 	public void fetchLinkableDatasets() throws TechnicalException, IOException, JDOMException, FunctionalException, InterruptedException {
 			
 		this.portableSetTimer.startTimer();
 		
        // Fetch linkable datasets (mart name + dataset name + all importables/exportables)
 		MyUtils.showProgress(totalMarts);
 		for (Iterator<String> it = virtualSchemaMartSetMap.keySet().iterator(); it.hasNext();) {
			String serverVirtualSchema = it.next();
			Set<MartInVirtualSchema> martSet = virtualSchemaMartSetMap.get(serverVirtualSchema);
			for (MartInVirtualSchema mart : martSet) {
				// Show progress
	 	    	System.out.print(".");	
	 	    	
	 	    	// Fetch imp/exps
				List<DatasetInMart> martDatasetList = martDatasetListMap.get(mart.martName);
				for (DatasetInMart datasetInMart : martDatasetList) {
					LinkableDataset linkableDataset = getConfiguration(serverVirtualSchema, mart.martName, datasetInMart);
					// Add if valid (example of non-valid: if DocType is missing)
					if (null!=linkableDataset) {
						datasetList.add(linkableDataset);
					}
				}  
	 		}
 		}
		System.out.println();
		totalLinkableDatasets = datasetList.size();
 		MyUtils.println(MyUtils.LINE_SEPARATOR + "totalLinkableDatasets = " + totalLinkableDatasets);
 		
 		this.portableSetTimer.stopTimer();
	}

	private void buildMartByNameMap() {
		this.martByName = new HashMap<String, MartInVirtualSchema>();
 		for (Iterator<Set<MartInVirtualSchema>> it = this.virtualSchemaMartSetMap.values().iterator(); it.hasNext();) {
 			Set<MartInVirtualSchema> setMart = it.next();	
 			for(MartInVirtualSchema mart : setMart) {
 				MyUtils.checkStatusProgram(!this.martByName.containsKey(mart.martName), 
 						"!this.martByName.containsKey(mart.martName)", true);
 				this.martByName.put(mart.martName, mart);
 			}
		}
	}
	
	public DatasetInMart getDatasetInMart(String virtualSchema, String datasetName) {
		DatasetInMart datasetInMart = null;
		Set<MartInVirtualSchema> martSet = this.virtualSchemaMartSetMap.get(virtualSchema);
		if (null!=martSet) {
			for (MartInVirtualSchema mart : martSet) {	
				List<DatasetInMart> datasetList = this.martDatasetListMap.get(mart.martName);
				for (DatasetInMart dataset : datasetList) {
					if (dataset.datasetName.equals(datasetName)) {
//						MyUtils.checkStatusProgram(datasetInMart==null, datasetInMart + ", " + dataset);	
										// assumption: no 2 datasets with the same name in the same virtualSchema (on same host)
						datasetInMart = dataset;
					}
				}
			}
		}
		return datasetInMart;
	}
	
	public List<DatasetInMart> getDatasetInMartList(String datasetName) {
		List<DatasetInMart> datasetInMartList = new ArrayList<DatasetInMart>();
		for (Iterator<String> it = this.virtualSchemaMartSetMap.keySet().iterator(); it.hasNext();) {
			String virtualSchemaTmp = it.next();
			Set<MartInVirtualSchema> martSet = this.virtualSchemaMartSetMap.get(virtualSchemaTmp);
			for (MartInVirtualSchema mart : martSet) {
				List<DatasetInMart> datasetList = this.martDatasetListMap.get(mart.martName);
				for (DatasetInMart dataset : datasetList) {
					if (dataset.datasetName.equals(datasetName)) {
						datasetInMartList.add(dataset);
					}
				}
			}
		}
		return datasetInMartList;
	}

	public void displayInitialStats() {
        MyUtils.println();
		MyUtils.println("virtualSchemaMartSetMap.size() = " + virtualSchemaMartSetMap.size());
		for (Iterator<String> it = virtualSchemaMartSetMap.keySet().iterator(); it.hasNext();) {
			String serverVirtualSchema = it.next();
			Set<MartInVirtualSchema> martSet = virtualSchemaMartSetMap.get(serverVirtualSchema);
			
			MyUtils.println(MyUtils.TAB_SEPARATOR + serverVirtualSchema + " (martNameSet.size() = " + martSet.size() + ")");
			for (MartInVirtualSchema mart : martSet) {
	        	List<DatasetInMart> datasetNameList = martDatasetListMap.get(mart.martName);
				MyUtils.println(MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + mart + " (datasetNameList.size() = " + datasetNameList.size() + ")");
	        	for (DatasetInMart datasetInMart : datasetNameList) {
	        		MyUtils.println(MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + datasetInMart);
	        	}
	 		}
 		}
        MyUtils.println();
        MyUtils.println("linkableDatasetList.size() = " + datasetList.size());
        for (LinkableDataset linkableDataset : datasetList) {
        	MyUtils.println(MyUtils.TAB_SEPARATOR + linkableDataset.toShortString());
        }
        MyUtils.println();
        MyUtils.println();
        MyUtils.println("linkableDatasetList.size() = " + datasetList.size());
        for (LinkableDataset linkableDataset : datasetList) {
        	MyUtils.println(MyUtils.TAB_SEPARATOR + linkableDataset.toImpExpOrientedString());
        }
        MyUtils.println();
	}
	public Map<String, Set<MartInVirtualSchema>> getVirtualSchemaMartSetMap() {
		return virtualSchemaMartSetMap;
	}
	public Map<String, List<DatasetInMart>> getMartDatasetListMap() {
		return martDatasetListMap;
	}
	@Override
	public boolean equals(Object obj) {
		return this.martServiceUrl.equals(((Configuration)obj).martServiceUrl);
	}
	public String getMartServiceUrl() {
		return martServiceUrl;
	}
}
