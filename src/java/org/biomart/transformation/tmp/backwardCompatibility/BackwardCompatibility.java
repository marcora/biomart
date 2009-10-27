package org.biomart.transformation.tmp.backwardCompatibility;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


import org.biomart.common.general.utils.DoubleString;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.old.martService.Configuration;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.old.martService.objects.DatasetInMart;
import org.biomart.old.martService.objects.MartInVirtualSchema;
import org.biomart.old.martService.restFulQueries.objects.Field;
import org.biomart.old.martService.restFulQueries.objects.PointerInfo;
import org.biomart.transformation.helpers.FilterOldDisplayType;
import org.biomart.transformation.helpers.FilterOldType;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.tmp.backwardCompatibility.objects.CascadeReference;
import org.biomart.transformation.tmp.backwardCompatibility.objects.ContainerFilterInfo;
import org.biomart.transformation.tmp.backwardCompatibility.objects.MainTableWithKey;
import org.biomart.transformation.tmp.backwardCompatibility.objects.Path;
import org.biomart.transformation.tmp.backwardCompatibility.objects.TransformationFilterDisplayType;
import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class BackwardCompatibility {

	public static final String VIRTUAL_SCHEMA_NAME = "default"; 
	public static String DATASET_NAME = 
		//null;
		//"gene_ensembl_template";
		//"ojaponica_snp";
		"hsapiens_gene_ensembl";
		//"UNIPROT";
	public static Boolean fetchAll = DATASET_NAME==null;
	public static String INPUT_FILE_NAME = "/home/anthony/Desktop/BC/" + DATASET_NAME + ".xml";
	public static String OUTPUT_FOLDER = "/home/anthony/Desktop/BC";
	
	public static final String SERIAL_FILE_PATH_AND_NAME = OUTPUT_FOLDER + "/Serial";
	
	public static void main(String[] args) {
		subMain(new String[] {"hsapiens_gene_ensembl"});
		System.out.println(MyUtils.EQUAL_LINE);
		System.out.println();
		subMain(new String[] {"gene_ensembl_template"});
	}
	
	public static void subMain(String[] args) {
		
		DATASET_NAME = args[0];
		fetchAll = !DATASET_NAME.equals("gene_ensembl_template");
		INPUT_FILE_NAME = "/home/anthony/Desktop/BC/" + DATASET_NAME + ".xml";
		OUTPUT_FOLDER = "/home/anthony/Desktop/BC";
		
		try {
			if (fetchAll) {
				
				Configuration configuration = null;
				if (!new File(SERIAL_FILE_PATH_AND_NAME).exists()) {
					System.out.println("Fetching...");
					configuration = new Configuration(MartServiceConstants.BMTEST_MART_SERVICE_STRING_URL);
					configuration.fetchMartSet();
					configuration.fetchDatasets();
					System.out.println("Writing serial: " + SERIAL_FILE_PATH_AND_NAME);
					MyUtils.writeSerializedObject(configuration, SERIAL_FILE_PATH_AND_NAME);						
				} else {
					System.out.println("Using serial: " + SERIAL_FILE_PATH_AND_NAME);
					configuration = (Configuration)MyUtils.readSerializedObject(SERIAL_FILE_PATH_AND_NAME);
				}
boolean okNow = false;				
				for (Iterator<String> it = configuration.virtualSchemaMartSetMap.keySet().iterator(); it.hasNext();) {
					String virtualSchemaName = it.next();
					
					// Skip pancreas_expression_db virtualSchema
					if (virtualSchemaName.equals("pancreas_expression_db")) {
						continue;
					}
					
					Set<MartInVirtualSchema> martInVirtualSchemaSet = configuration.virtualSchemaMartSetMap.get(virtualSchemaName);
					for (MartInVirtualSchema martInVirtualSchema : martInVirtualSchemaSet) {
						String martName = martInVirtualSchema.martName;
						String host = martInVirtualSchema.host;
						
						if (!martInVirtualSchema.visible || martName.toLowerCase().contains("gramene") || host.toLowerCase().contains("gramene")) {
							continue;
						}
						
						List<DatasetInMart> datasetList = configuration.martDatasetListMap.get(martName);
						for (DatasetInMart datasetInMart : datasetList) {
							String datasetName = datasetInMart.datasetName;
							
							if (!datasetName.equals(
									"hsapiens_gene_ensembl"
									) && !datasetName.equals(
									"UNIPROT"
									)/* && !okNow*/) continue;
							okNow = true;
							
							if (!datasetInMart.getVisible()) {
								continue;
							}
							
							System.out.println(MyUtils.EQUAL_LINE);
							System.out.println(virtualSchemaName + MyUtils.TAB_SEPARATOR + datasetName);
							System.out.println();
							
							Document xmlDocument = configuration.getXml(virtualSchemaName, datasetInMart.datasetName);
							BackwardCompatibility backwardCompatibility = new BackwardCompatibilityNonTemplate(
									virtualSchemaName, martInVirtualSchema, datasetInMart, xmlDocument);
							backwardCompatibility.processDocument();
							Thread.sleep(1000);
						}
					}
				}
			} else {
				BackwardCompatibility backwardCompatibility = "gene_ensembl_template".equals(DATASET_NAME) ? 
					new BackwardCompatibilityTemplate(VIRTUAL_SCHEMA_NAME, DATASET_NAME, INPUT_FILE_NAME) :
					new BackwardCompatibilityNonTemplate(VIRTUAL_SCHEMA_NAME, DATASET_NAME, INPUT_FILE_NAME);
				backwardCompatibility.processDocument();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static final String DEFAULT_PARTITION_TABLE_NAME = "default";
	public static final String DEFAULT_MART_VERSION = "0";
	public static final String DEFAULT_CONFIGURATION_NAME = "default_configuration";
	
	public static final String CONTAINER = "container";
	public static final String NOT_APPLICABLE = "NA";
	
	public static final String DEFAULT_USER_ID = "0";
	public static final String ALL_USERS_GROUP_ID = "99";
	
	public static final String PLUS_SECTION_SEPARATOR = " ++++++++++++++++++++++++++++ ";
	public static final String EQUAL_SECTION_SEPARATOR = " ============================ ";
	public static final String SEMI_DASH_SECTION_SEPARATOR = " - - - - - - - - - - - - - -  ";
		
	Element oldRootElement = null;
	Element newRootElement = null;
	List<Element> oldRootChildren = null;
	
	String virtualSchemaName = null;
	String datasetName = null;
	MartInVirtualSchema martInVirtualSchema = null;
	DatasetInMart datasetInMart = null;
	
	Document xmlDocument = null;	
	String xmlDocumentFilePathAndName = null;	
	
	String outputFolder = null; 
	String ouputFilePathAndName = null;
	String defaultDataFolderPath = null;
	
	Element location = null;
	Element mart = null;
	Element partitionTables = null;
	Element dataset = null;
	Element tables = null;
	Element config = null;
	Element portables = null;
	Element tree = null;
	
	Element configPointer = null;
	
	Path currentPath = null;
	String defaultRange = null;
	
	Map<String, CascadeReference> refMap = null;

	Map<String, Map<String, String>> dynamicDatasetPartitionTableMap = null;
	Map<Integer, String> dynamicDatasetPartitionTableColumnMapping = null;
	Map<String, Integer> dynamicDatasetPartitionTableColumnMapping2 = null;	
	
	/*public BackwardCompatibility(String virtualSchemaName, String datasetName, String xmlDocumentFilePathAndName) {
		this(virtualSchemaName, null, null, null, datasetName, xmlDocumentFilePathAndName);
	}
	private BackwardCompatibility(String virtualSchemaName, MartInVirtualSchema martInVirtualSchema, 
			DatasetInMart datasetInMart, Document xmlDocument) {
		this(virtualSchemaName, martInVirtualSchema, datasetInMart, xmlDocument, datasetInMart.datasetName, null);
	}*/
	protected BackwardCompatibility(String virtualSchemaName, MartInVirtualSchema martInVirtualSchema, DatasetInMart datasetInMart, 
			Document xmlDocument, String datasetName, String xmlDocumentFilePathAndName) throws Exception {
		this.virtualSchemaName = virtualSchemaName;
		this.xmlDocument = xmlDocument;
		this.martInVirtualSchema = martInVirtualSchema;
		this.datasetInMart = datasetInMart;
		this.datasetName = datasetName;
		this.xmlDocumentFilePathAndName = xmlDocumentFilePathAndName;
		
		String datasetIdentifier = getDatasetIdentifier();
		this.outputFolder = OUTPUT_FOLDER + MyUtils.FILE_SEPARATOR + datasetIdentifier;
		new File(this.outputFolder).mkdirs();
		this.ouputFilePathAndName = outputFolder + MyUtils.FILE_SEPARATOR + datasetIdentifier + "_new.xml";
		this.defaultDataFolderPath = this.outputFolder + MyUtils.FILE_SEPARATOR + "DataFolder";
		new File(this.defaultDataFolderPath).mkdirs();
		
		this.refMap = new HashMap<String, CascadeReference>();	

		SAXBuilder builder = new SAXBuilder();
		if (null==this.xmlDocument) {
			this.xmlDocument = builder.build(new File(this.xmlDocumentFilePathAndName));
		}		
		oldRootElement = this.xmlDocument.getRootElement();
		oldRootChildren = oldRootElement.getContent();/*oldRootElement.cloneContent();*/
		newRootElement = new Element("martRegistry");
	}
	
	public void processDocument() throws Exception {
		createDefaultPortal();
		createInitialPlaceHolders();
		
		if (this instanceof BackwardCompatibilityNonTemplate) {
			
			createDefaultPartitionTable();
			/*addMainTables();*/
			transformPortables();
			transformPages();
			convertDimensionPartitionTables();
		} else if (this instanceof BackwardCompatibilityTemplate) {
			createPartitionTables();
			transformPages();
		}
		outputXml();
	}
	
	public void createPartitionTables() throws Exception {
		// <DynamicDataset aliases="mouse_formatter1=,mouse_formatter2=,mouse_formatter3=,species1=Homo sapiens,species2=Homo_sapiens,species3=hsapiens,species4=hsap,species5=Homo+sapiens,version=NCBI36,link_version=hsapiens_36,default=true" internalName="hsapiens_gene_ensembl" />
		
		Set<String> partitionTableColumnSet = new LinkedHashSet<String>();
		dynamicDatasetPartitionTableMap = new TreeMap<String, Map<String,String>>();
		for (Element element : oldRootChildren) {
			if ("DynamicDataset".equals(element.getName())) {
				String internalName = getMandatoryPropertyValue(element, "internalName");
				String aliases = getMandatoryPropertyValue(element, "aliases");
				String[] aliasesSplit = aliases.split(MartServiceConstants.ELEMENT_SEPARATOR);
				Map<String, String> map = new TreeMap<String, String>();
				partitionTableColumnSet.add("internalName");
				map.put("internalName", internalName);
				for (String alias : aliasesSplit) {
					String[] aliasSplit = alias.split("=");
					partitionTableColumnSet.add(aliasSplit[0]);
					System.out.println(MyUtils.arrayToStringBuffer(aliasSplit));
					String key = aliasSplit[0];
					String value = aliasSplit.length==2 ? aliasSplit[1] : "";
					map.put(key, value);
				}
				dynamicDatasetPartitionTableMap.put(internalName, map);
			}
		}
		this.partitionTables.addContent(transformDynamicDatasetsToPartitionTable(partitionTableColumnSet));
	}
	
	private Element transformDynamicDatasetsToPartitionTable(Set<String> partitionTableColumnSet) throws Exception {
	
		int totalRows = dynamicDatasetPartitionTableMap.size();
		int totalColumns = partitionTableColumnSet.size();
		
		dynamicDatasetPartitionTableColumnMapping = new TreeMap<Integer, String>();
		dynamicDatasetPartitionTableColumnMapping2 = new TreeMap<String, Integer>();
		int columnIndex = 0;
		for (String key : partitionTableColumnSet) {
			dynamicDatasetPartitionTableColumnMapping.put(columnIndex, key);
			dynamicDatasetPartitionTableColumnMapping2.put(key, columnIndex);
			columnIndex++;
		}
		
		String[][] cells = new String[totalRows][totalColumns];
		for (int i = 0; i < totalRows; i++) {
			for (int j = 0; j < totalColumns; j++) {
				cells[i][j] = "";
			}
		}
		int row = 0;
		for (Iterator<Map<String, String>> it = dynamicDatasetPartitionTableMap.values().iterator(); it.hasNext();) {
			Map<String, String> map = it.next();
			for (Iterator<String> it2 = map.keySet().iterator(); it2.hasNext();) {
				String key = it2.next();
				String value = map.get(key);
				int column = dynamicDatasetPartitionTableColumnMapping2.get(key);
				cells[row][column] = value;
			}
			row++;
		}
		Element defaultPartitionTable = createPartitionTable("P0", cells);
		return defaultPartitionTable;
	}
	
	public void convertDimensionPartitionTables() throws Exception {
		
		String PARTITION_SEPARATOR = "_";
		String ELEMENT_SEPARATOR = "__";
		
		Map<DoubleString, Set<String>> mapTemplateTableNameAndKeyToPartsValues = new TreeMap<DoubleString, Set<String>>();
		
		Map<String, DoubleString> mapOriginalTableNameToTemplateTableNameAndPartValue = new TreeMap<String, DoubleString>();
		
		//Set<List<String>> setList = new HashSet<List<String>>();
		//List<List<String>> setList = new ArrayList<List<String>>();
		List<Element> listTables = this.tables.getChildren();
		for (Element table : listTables) {
			String tableName = getMandatoryPropertyValue(table, "name");
			String keyName = getMandatoryPropertyValue(table, "key");
			if (!isMainTable(tableName)) {
				String[] elementSplits = tableName.split(ELEMENT_SEPARATOR);
				MyUtils.checkStatusProgram(elementSplits.length==3 || elementSplits.length==2, 
						"elementSplits.length = " + elementSplits.length +
						", elementSplits = " + MyUtils.arrayToStringBuffer(elementSplits), true);
				String dimensionTableName = elementSplits.length==2 ? elementSplits[0] : elementSplits[1];
				
				String[] partitionSplits = dimensionTableName.split(PARTITION_SEPARATOR);
				if (partitionSplits.length>1) {	// Can't be partitioned
					String templateTableName = null;
					String partitionValue = null;
					
					boolean partition = false;
					List<DoubleString> list = new ArrayList<DoubleString>();
					for (int i = 1; i < partitionSplits.length; i++) {
						StringBuffer tableSb = new StringBuffer();
						for (int j = 0; j < i; j++) {
							tableSb.append(partitionSplits[j] + (j==i-1 ? "" : PARTITION_SEPARATOR));
						}
						StringBuffer valueSb = new StringBuffer();
						for (int j = i; j < partitionSplits.length; j++) {
							valueSb.append((j==i ? "" : PARTITION_SEPARATOR) + partitionSplits[j]);
						}
						list.add(new DoubleString(tableSb.toString(), valueSb.toString()));
					}
					
					for (int i = list.size()-1; i>=0; i--) {
						DoubleString doubleString = list.get(i);
						String s1 = doubleString.getString1();
						String s2 = doubleString.getString2();
						if (listTemplateTable.contains(s1)) {
							templateTableName = s1;
							partitionValue = s2;
							partition = true;
							break;
						}
					}
					
					if (partition) {
						DoubleString templateTableNameAndKeyName = new DoubleString (templateTableName, keyName);
						Set<String> values = mapTemplateTableNameAndKeyToPartsValues.get(templateTableNameAndKeyName);
						if (null==values) {
							values = new TreeSet<String>();
						}
						values.add(partitionValue);
						mapTemplateTableNameAndKeyToPartsValues.put(
								templateTableNameAndKeyName, values);
						mapOriginalTableNameToTemplateTableNameAndPartValue.put(
								tableName, new DoubleString(templateTableName, partitionValue));
					}
				}
			}
		}

		System.out.println();
		for (Iterator<DoubleString> it = mapTemplateTableNameAndKeyToPartsValues.keySet().iterator(); it.hasNext();) {
			DoubleString doubleString = it.next();
			String tableName = doubleString.getString1();
			String keyName = doubleString.getString2();
			Set<String> l = mapTemplateTableNameAndKeyToPartsValues.get(doubleString);
			System.out.println(tableName + " (" + keyName + ")" + " -> " + l);
		}

		/*System.out.println();
		System.out.println();
		for (List<String> l : setList) {
			System.out.println(l);
		}
		System.out.println();*/

		Map<DoubleString, Element> mapTemplateTableNameAndKeyToTemplateTable = new HashMap<DoubleString, Element>();
		Map<DoubleString, Element> mapTemplateTableNameAndKeyToPartitionTable = new HashMap<DoubleString, Element>();
		for (Iterator<DoubleString> it = mapTemplateTableNameAndKeyToPartsValues.keySet().iterator(); it.hasNext();) {
			
			DoubleString templateTableNameAndKey = it.next();
			String templateTableName = templateTableNameAndKey.getString1();
			String templateTableKey = templateTableNameAndKey.getString2();

			String partitionTableName = templateTableName;
			int count = 1;
			List<Element> listPartitionTables = this.partitionTables.getChildren();
			for (int i = 0; i < listPartitionTables.size(); i++) {
				Element partitionTableTmp = listPartitionTables.get(i);
				String partitionTableNameTmp = getMandatoryPropertyValue(partitionTableTmp, "name");
				if (partitionTableName.equals(partitionTableNameTmp)) {
					count++;
					partitionTableName = templateTableName + count;
					i=0;
				}
			}
			
			Set<String> set = mapTemplateTableNameAndKeyToPartsValues.get(templateTableNameAndKey);
			String[][] cells = new String[set.size()][1];
			int row = 0;
			for (String s : set) {
				cells[row][0] = s;
				row++;
			}
			
			Element newPartitionTable = createPartitionTable(
					partitionTableName, cells);
			partitionTables.addContent(newPartitionTable);	
			mapTemplateTableNameAndKeyToPartitionTable.put(templateTableNameAndKey, newPartitionTable);
			
			
			Element newTable = new Element("table");
			newTable.setAttribute("name", templateTableName);
			newTable.setAttribute("key", templateTableKey);
			List<String> ls = new ArrayList<String>();
			for (int rowCount = 0; rowCount < set.size(); rowCount++) {
				ls.add(getRangeWithoutVisibilityString(getRangeString(partitionTableName, rowCount, null)));
			}
			newTable.setAttribute("range", MartConfiguratorUtils.collectionToString(ls, ""));	// no serarator ([] do the trick)
			this.tables.addContent(newTable);
			mapTemplateTableNameAndKeyToTemplateTable.put(templateTableNameAndKey, newTable);
		}
		
		for (int i = listTables.size()-1; i>=0; i--) {
			Element table = listTables.get(i);
			
			String originalTableName = getMandatoryPropertyValue(table, "name");
			String originalTableKey = getMandatoryPropertyValue(table, "key");
			if (!isMainTable(originalTableName)) {
				DoubleString templateTableNameAndPartValue = 
					mapOriginalTableNameToTemplateTableNameAndPartValue.get(originalTableName);
				if (null!=templateTableNameAndPartValue) {
					
					String templateTableName = templateTableNameAndPartValue.getString1();
					String partValue = templateTableNameAndPartValue.getString2();
					
					System.out.println(MyUtils.DASH_LINE);
					System.out.println("templateTableName = " + templateTableName);
					System.out.println("partValue = " + partValue);
					
					System.out.println("originalTableName = " + originalTableName + 
							", templateTableNameAndPartValue = " + templateTableNameAndPartValue);
					
					Element templateTable = mapTemplateTableNameAndKeyToTemplateTable.get(
							new DoubleString(templateTableName, originalTableKey));
					Element partitionTable = mapTemplateTableNameAndKeyToPartitionTable.get(
							new DoubleString(templateTableName, originalTableKey));
					
					System.out.println("templateTable = " + getMandatoryPropertyValue(templateTable, "name"));
					System.out.println("partitionTable = " + getMandatoryPropertyValue(partitionTable, "name"));
					
					String rangeString = getMandatoryPropertyValue(templateTable, "range");
					String[] rangeList = rangeString.split(MartServiceConstants.ELEMENT_SEPARATOR);
					
					System.out.println("rangeString = " + rangeString);
					
					List<Element> originalTableAttributeList = table.getChildren();
					for (Element originalTableAttribute : originalTableAttributeList) {
						String originalTableAttributeName = originalTableAttribute.getValue();
						
						System.out.println("\toriginalTableAttributeName = " + originalTableAttributeName);
						
						boolean exists = false;
						List<Element> templateTableAttributeList = templateTable.getChildren();
						for (Element templateTableAttribute : templateTableAttributeList) {
							String templateTableAttributeName = templateTableAttribute.getValue();
							if (originalTableAttributeName.equals(templateTableAttributeName)) {
								exists = true;
								break;
							}
						}
						
						System.out.println("\t\texists = " + exists);
						
						if (!exists) {
							Element newAttribute = new Element("attribute");
							newAttribute.setText(originalTableAttributeName);
							templateTable.addContent(newAttribute);
						}
						
						//rangeList = rangeList
						String valueRange = getRangeString(partitionTable, partValue);
						rangeString = MyUtils.isEmpty(rangeString) ? valueRange : rangeString+valueRange;
						table.setAttribute("range", rangeString);
					}
					
					this.tables.removeContent(table);
				}
			}
		}
		
		List<Element> listPages = this.tree.getChildren();
		for (Element formerPage : listPages) {
			List<Element> listGroups = formerPage.getChildren();
			for (Element formerGroup : listGroups) {
				List<Element> listCollections = formerGroup.getChildren();
				for (Element formerCollection : listCollections) {
					List<Element> listElements = formerCollection.getChildren();
					for (Element element : listElements) {
						/*String pointer = getMandatoryPropertyValue(element, "pointer");
						if (!new Boolean(pointer)) {
							
							String name = getMandatoryPropertyValue(element, "name");
							System.out.println(name);
							//String table = getMandatoryPropertyValue(element, "table");
							//adf
						}*/
					}
				}
			}
		}
	}

	List<String> listTemplateTable = new ArrayList<String>(Arrays.asList(new String[] {
			"go", "ox", "exp", "protein_feature", "homolog"
			
	}));
	
	private void outputXml() throws FileNotFoundException, IOException {
		// Output XML
		XMLOutputter fmt = new XMLOutputter(Format.getPrettyFormat());
		Document newDoc = new Document(newRootElement);
		FileOutputStream fos = new FileOutputStream(this.ouputFilePathAndName);
		fmt.output(newDoc, fos);/*fmt.output(doc, System.out);*/
	}

	private void transformPages() throws Exception {
		for (Element element : oldRootChildren) {	
			boolean attributePage = "AttributePage".equals(element.getName());
			boolean filterPage = "FilterPage".equals(element.getName());
			if (attributePage || filterPage) {
				if (filterPage) {
					Comment filterPagesComment = new Comment(SEMI_DASH_SECTION_SEPARATOR + "Filters" + SEMI_DASH_SECTION_SEPARATOR);
					tree.addContent(filterPagesComment);
				} else {
					Comment attributePagesComment = new Comment(SEMI_DASH_SECTION_SEPARATOR + "Attributes" + SEMI_DASH_SECTION_SEPARATOR);
					tree.addContent(attributePagesComment);
				}
				
				Element containerPage = transformElementPage(element, attributePage);
				if (null!=containerPage) {
					tree.addContent(containerPage);
				}
			}
		}
	}

	private void transformPortables() throws Exception {
		// Portables
		for (Element element : oldRootChildren) {
			if ("Importable".equals(element.getName())) {
				portables.addContent(transformPortable(element, true));
			} else if ("Exportable".equals(element.getName())) {
				portables.addContent(transformPortable(element, false));
			}	
		}
	}
	private String getDatasetIdentifier() {
		return this.virtualSchemaName + MyUtils.INFO_SEPARATOR + MyUtils.INFO_SEPARATOR + this.datasetName;
	}
	
	private Element transformPortable(Element portable, boolean importable) throws Exception {
		if (isHiddenElement(portable)) {
	MyUtils.errorProgram("", true);	// tmp to check if any
			return null;
		}
		
		Element newPortable = new Element(importable ? "importable" : "exportable");
		
		String internalName = getMandatoryPropertyValue(portable, "internalName");
		String elements = getMandatoryPropertyValue(portable, importable ? "filters" : "attributes");
		String type = getMandatoryPropertyValue(portable, "type");
		String name = getMandatoryPropertyValue(portable, "name");
		String linkName = getMandatoryPropertyValue(portable, "linkName");
		MyUtils.checkStatusProgram(name!=null && name.equals(linkName), "", true);
		
		String linkVersion = getOptionalPropertyValue(portable, "linkVersion");
		String default_ = getOptionalPropertyValue(portable, "default");

		Attribute hideDisplay = portable.getAttribute("hideDisplay");
	MyUtils.checkStatusProgram(hideDisplay==null, portable.getName() + "internalName = " + internalName + ", hideDisplay = " + hideDisplay, true);	// tmp to check if any
				
		newPortable.setAttribute("name", getRangeOnColumnString(DEFAULT_PARTITION_TABLE_NAME, 1) + "." + internalName);	//"(P1C1).imp_ensembl_gene_id"
		newPortable.setAttribute(importable ? "filters" : "attributes", elements);
		newPortable.setAttribute("linkName", linkName);		// For backward compatibility
		newPortable.setAttribute("type", type);		// For backward compatibility?
		
		if (linkVersion!=null) {
			newPortable.setAttribute("linkVersion", linkVersion);
		}
		if (!importable && default_!=null) {	// Only considered for exportable anyway
			newPortable.setAttribute("default", MartConfiguratorUtils.binaryDigitToBoolean(default_).toString());
		}
		
		printOutUntreatedProperties(portableProperties, newPortable);
		
		return newPortable;
	}
	
	private void createDefaultPortal() {
		Comment portalComment = new Comment(PLUS_SECTION_SEPARATOR + "PORTAL" + PLUS_SECTION_SEPARATOR);
		Comment usersComment = new Comment(EQUAL_SECTION_SEPARATOR + "Users" + EQUAL_SECTION_SEPARATOR);
		Element martUsers = new Element("martUsers");
		Element user = new Element("user");
		Element userGroup = new Element("userGroup");
		Comment configurationsComment = new Comment(EQUAL_SECTION_SEPARATOR + "Configuration Pointers" + EQUAL_SECTION_SEPARATOR);
		Element guiContainer = new Element("guiContainer");
		Element subGuiContainer = new Element("guiContainer");
		this.configPointer = new Element("configPointer");
		
		newRootElement.addContent(portalComment);
		newRootElement.addContent(usersComment);
		newRootElement.addContent(martUsers);
		martUsers.addContent(user);
		martUsers.addContent(userGroup);
		newRootElement.addContent(configurationsComment);
		newRootElement.addContent(guiContainer);
		guiContainer.addContent(subGuiContainer);
		subGuiContainer.addContent(configPointer);
		
		user.setAttribute("id", DEFAULT_USER_ID);
		user.setAttribute("name", "default");
		user.setAttribute("password", "");	// no password
		user.setAttribute("default", String.valueOf(true));	// Always default user
		
		userGroup.setAttribute("id", ALL_USERS_GROUP_ID);
		userGroup.setAttribute("name", "all");
		userGroup.setAttribute("users", DEFAULT_USER_ID);
		
		guiContainer.setAttribute("name", "root");
		guiContainer.setAttribute("displayName", "");
		guiContainer.setAttribute("guiLayout", "");
		guiContainer.setAttribute("configLayout", "");
		guiContainer.setAttribute("martUsers", DEFAULT_USER_ID + "," + ALL_USERS_GROUP_ID);
		
		guiContainer.setAttribute("name", "default");
		guiContainer.setAttribute("displayName", "Default");
		guiContainer.setAttribute("guiLayout", "grid");
		guiContainer.setAttribute("configLayout", "newMartView");
		guiContainer.setAttribute("martUsers", DEFAULT_USER_ID + "," + ALL_USERS_GROUP_ID);
	}
	
	private void createInitialPlaceHolders() throws Exception {
		
		// Create basic placeholders for the new config
		Comment locationComment = new Comment(PLUS_SECTION_SEPARATOR + "CONFIGURATIONS" + PLUS_SECTION_SEPARATOR);
		location = new Element("location");
		mart = new Element("mart");
		Comment partitionTablesComment = new Comment(EQUAL_SECTION_SEPARATOR + "Partition Tables" + EQUAL_SECTION_SEPARATOR);
		Comment datasetComment = new Comment(EQUAL_SECTION_SEPARATOR + "Datasets" + EQUAL_SECTION_SEPARATOR);
		partitionTables = new Element("partitionTables");
		Comment tablesComment = new Comment(EQUAL_SECTION_SEPARATOR + "Database Tables" + EQUAL_SECTION_SEPARATOR);
		tables = new Element("tables");
		config = new Element("config");
		Comment portablesComment = new Comment(EQUAL_SECTION_SEPARATOR + "Portables" + EQUAL_SECTION_SEPARATOR);
		portables = new Element("portables");
		Comment treeComment = new Comment(EQUAL_SECTION_SEPARATOR + "Configurations" + EQUAL_SECTION_SEPARATOR);
		tree = new Element("tree");
		
		newRootElement.addContent(locationComment);
		newRootElement.addContent(location);
		location.addContent(mart);
		mart.addContent(partitionTablesComment);
		mart.addContent(partitionTables);
		mart.addContent(datasetComment);
		
		processDatasetConfig(oldRootElement);
		
		mart.addContent(dataset);	// dataset is already instanciated from processRoot
		dataset.addContent(tablesComment);
		dataset.addContent(tables);
		dataset.addContent(config);
		config.addContent(portablesComment);
		config.addContent(portables);
		config.addContent(treeComment);
		config.addContent(tree);
		
		// Fill location
		String locationName = null;
		String martName = null;
		String martVersion = DEFAULT_MART_VERSION;
		String configurationName = DEFAULT_CONFIGURATION_NAME;
		if (null!=this.martInVirtualSchema && null!=this.datasetInMart) {
			Element martServiceLineMart = this.martInVirtualSchema.martServiceLine;
	
			System.out.println(martInVirtualSchema);
			locationName = martInVirtualSchema.martName;
			this.location.setAttribute("database", this.martInVirtualSchema.databaseName);
			this.location.setAttribute("host", this.martInVirtualSchema.host);
			this.location.setAttribute("port", this.martInVirtualSchema.port);
			String displayName = getMandatoryPropertyValue(martServiceLineMart, "displayName");
			this.location.setAttribute("displayName", displayName);
			this.location.setAttribute("path", this.martInVirtualSchema.path);
			this.location.setAttribute("visible", this.martInVirtualSchema.visible.toString());
			this.location.setAttribute("user", this.martInVirtualSchema.user!=null ? this.martInVirtualSchema.user : "");
			this.location.setAttribute("password", this.martInVirtualSchema.password!=null ? this.martInVirtualSchema.password : "");
			
			String type = null;
			if (martServiceLineMart.getName().equals(MartServiceConstants.MART_TYPE_DB_LOCATION)) {
				type = "database";
			} else if (martServiceLineMart.getName().equals(MartServiceConstants.MART_TYPE_URL_LOCATION)) {
				type = "url";
			} 
			this.location.setAttribute("type", type);
			
			// Fill mart
			martName = martInVirtualSchema.martName;
			//this.mart.setAttribute("name", martInVirtualSchema.);
			
			this.dataset.setAttribute("datasetType", this.datasetInMart.getDatasetType());
			// Visible and displayName are already in the configuration file
			//String[] split = this.datasetInMart.martServiceLine.split(MyUtils.TAB_SEPARATOR);
			//this.dataset.setAttribute("visible", this.datasetInMart.visible.toString());
			//this.dataset.setAttribute("displayName", split[2]);
		} else {
			locationName = "?Unavailable?";
			martName = "?Unavailable?";
		}
		this.location.setAttribute("name", locationName);
		this.mart.setAttribute("name", martName);
		this.mart.setAttribute("version", martVersion);
		this.dataset.setAttribute("name", this.datasetName);
		this.config.setAttribute("name", configurationName);
		
		this.currentPath = new Path(locationName, martName, martVersion, this.datasetName, configurationName);
		
		// Add a config pointer to newly created config
		fillPathInfo(configPointer);
		configPointer.setAttribute("processors", "FASTA");
	}
	
	private void processDatasetConfig(Element oldRootElement) throws Exception {
		
		dataset = new Element("dataset");
		
		String dataset = getMandatoryPropertyValue(oldRootElement, "dataset");
		MyUtils.checkStatusProgram(dataset.equals(this.datasetName), 
				"this.datasetName = " + this.datasetName + ", dataset = " + dataset, true);
		
		String description = getOptionalPropertyValue(oldRootElement, "description");
		if (null!=description) {
			this.dataset.setAttribute("description", description);
		}
		
		String displayName = getMandatoryPropertyValue(oldRootElement, "displayName");
		this.dataset.setAttribute("displayName", displayName);
		
		String visibleAsString = getOptionalPropertyValue(oldRootElement, "visible");
		this.dataset.setAttribute("visible", MartConfiguratorUtils.binaryDigitToBoolean(visibleAsString).toString());

		printOutUntreatedProperties(datasetConfigProperties, oldRootElement);
	}

	/*private void addDefaultPartitionTable() {
		
		Element defaultPartitionTable = new Element("partitionTable");
		defaultPartitionTable.setAttribute("name", DEFAULT_PARTITION_TABLE_NAME);
		defaultPartitionTable.setAttribute("rows", "1");
		defaultPartitionTable.setAttribute("cols", "1");
		partitionTables.addContent(defaultPartitionTable);
		Element defaultCell = new Element("cell");
		defaultCell.setAttribute("row", "1");
		defaultCell.setAttribute("col", "1");
		defaultCell.setText("");
		defaultPartitionTable.addContent(defaultCell);
		this.defaultRange = getRangeString(DEFAULT_PARTITION_TABLE_NAME, 1, null);
	}*/
	private void createDefaultPartitionTable() throws Exception {
		
		String[][] cells = new String[1][1];
		cells[0][0] = "";
		Element defaultPartitionTable = createPartitionTable(
				DEFAULT_PARTITION_TABLE_NAME, cells);
		partitionTables.addContent(defaultPartitionTable);
		this.defaultRange = getRangeString(DEFAULT_PARTITION_TABLE_NAME, 0, null);
	}
	
	public static Element createPartitionTable(String name, String[][] cells) throws Exception {
		if (null==cells || cells.length<=0) {
			throw new Exception("Invalid cells table");
		}
		int rows = cells.length;
		int cols = cells[0].length;
		Element partitionTable = new Element("partitionTable");
		partitionTable.setAttribute("name", name);
		partitionTable.setAttribute("rows", String.valueOf(rows));
		partitionTable.setAttribute("cols", String.valueOf(cols));
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				Element defaultCell = new Element("cell");
				defaultCell.setAttribute("row", String.valueOf(i));
				defaultCell.setAttribute("col", String.valueOf(j));
				defaultCell.setText(cells[i][j]);
				partitionTable.addContent(defaultCell);				
			}
		}
		return partitionTable;
	}
	
	@Deprecated
	private void addMainTables2() throws Exception {
		
		List<MainTableWithKey> mainTableList = new ArrayList<MainTableWithKey>();
		for (Element element : oldRootChildren) {
			if ("MainTable".equals(element.getName())) {
				String mainTableName = getElementMandatoryValue(element);
				MainTableWithKey mainTableWithKey = new MainTableWithKey(mainTableName.toLowerCase());
				mainTableList.add(mainTableWithKey);
			}
		}
		
		int i = 0;
		for (Element element : oldRootChildren) {
			if ("Key".equals(element.getName())) {
				MyUtils.checkStatusProgram(mainTableList.size()>=i, "mainTableList.size() = " + mainTableList.size(), true);
				
				String key = getElementMandatoryValue(element);
				MainTableWithKey mainTableWithKey = mainTableList.get(i);
				mainTableWithKey.keyName = key.toLowerCase();
				i++;
			}
		}
		MyUtils.checkStatusProgram(mainTableList.size()>0, "", true);
		
		System.out.println("mainTableList.size() = " + mainTableList.size());
		for (MainTableWithKey mainTableWithKey : mainTableList) {
			System.out.println(MyUtils.TAB_SEPARATOR + mainTableWithKey);
		}
		System.out.println();
		
		for (MainTableWithKey mainTableWithKey : mainTableList) {
			addNewTable(mainTableWithKey);
		}
	}

	private boolean isHiddenElement(Element element) throws Exception {
		return getOptionBooleanValue(element, "hidden");
	}
	private void transformContainerOnly(Element attributePage, Element newAttributePage) throws Exception {
		String internalName = getMandatoryPropertyValue(attributePage, "internalName");
		String displayName = getOptionalPropertyValue(attributePage, "displayName");
		String description = getOptionalPropertyValue(attributePage, "description");
		Boolean hideDisplay = getOptionBooleanValue(attributePage, "hideDisplay");
		
		newAttributePage.setAttribute("name", internalName);
		if (displayName!=null) {
			newAttributePage.setAttribute("displayName", displayName);
		}
		if (description!=null) {
			newAttributePage.setAttribute("description", description);
		}
		if (hideDisplay) {
			newAttributePage.setAttribute("visible", String.valueOf(!hideDisplay));
		}
		printOutUntreatedProperties(containerProperties, attributePage);
	}
	private Element transformElementPage(Element elementPage, boolean containsAttributes) throws Exception {
		if (isHiddenElement(elementPage)) {
			return null;
		}
		Element newElementPage = new Element(CONTAINER);
		transformContainerOnly(elementPage, newElementPage);
		List<Element> elementPageChildren = elementPage.getChildren();
		for (Element element : elementPageChildren) {
			Element containerGroup = transformElementGroup(element, containsAttributes);
			if (null!=containerGroup) {
				newElementPage.addContent(containerGroup);
			}
		}
		return newElementPage;
	}
	private Element transformElementGroup(Element elementGroup, boolean containsAttributes) throws Exception {
		if (isHiddenElement(elementGroup)) {
			return null;
		}
		Element newElementGroup = new Element(CONTAINER);
		transformContainerOnly(elementGroup, newElementGroup);
		List<Element> elementGroupChildren = elementGroup.getChildren();
		for (Element element : elementGroupChildren) {
			Element containerCollection = transformElementCollection(element, containsAttributes);
			if (null!=containerCollection) {
				newElementGroup.addContent(containerCollection);
			}
		}
		return newElementGroup;
	}
	private Element transformElementCollection(Element elementCollection, boolean containsAttributes) throws Exception {
		if (isHiddenElement(elementCollection)) {
			return null;
		}
		Element newElementCollection = new Element(CONTAINER);
		transformContainerOnly(elementCollection, newElementCollection);
		List<Element> elementCollectionChildren = elementCollection.getChildren();
		for (Element element : elementCollectionChildren) {
			Element elementPointer = containsAttributes ? 
					transformAttributeDescription(element) : transformFilterDescription(element, newElementCollection);
			if (null!=elementPointer) {
				newElementCollection.addContent(elementPointer);
			}
		}
		/*// Group dimension table partition elements together
		List<Element> newElementCollectionChildren = newElementCollection.getChildren();
		for (Element element : newElementCollectionChildren) {	// Can only be either all attributes or all filters
			String pointer = getMandatoryPropertyValue(element, "pointer");
			if (!new Boolean(pointer)) {
				String table = getMandatoryPropertyValue(element, "table");
				
			}
		}*/
		return newElementCollection;
	}
	
	List<String> attributesWithFilterPointer = new ArrayList<String>(Arrays.asList(new String[] {
			"upstream_flank", "downstream_flank"
			}));
			// Genomic sequence
	List<String> attributeContainersWithMaxSelect = new ArrayList<String>(Arrays.asList(new String[] {
			//"xrefs"
			}));
	
	final List<String> attributeProperties = new ArrayList<String>(Arrays.asList(new String[] {
			"internalName", "hidden", "hideDisplay", "displayName", "description", "linkoutURL", "maxLength", "default",
			"tableConstraint", "key", "field",
			
			// Not handled yet
			"pointerAttribute", "pointerDataset", "pointerInterface",
			"checkForNulls", "source"
	}));
	final List<String> filterProperties = new ArrayList<String>(Arrays.asList(new String[] {
			"internalName", "hidden", "hideDisplay", "description", "displayName", "type", "displayType", "qualifier", "legal_qualifiers",
			"isSelectable",
			"tableConstraint", "key", "field",
			
			"pointerFilter", "pointerDataset", "pointerInterface",
			"multipleValues", "defaultValue", "regexp",
			"filterList", "style", "otherFilters", "checkForNulls",
			"graph", "autoCompletion", "buttonURL",
	}));
	final List<String> containerProperties = new ArrayList<String>(Arrays.asList(new String[] {
			"description", "displayName", "internalName", "hidden", "hideDisplay",
			
			// Not handled yet
			"outFormats", "maxSelect"
	}));
	final List<String> portableProperties = new ArrayList<String>(Arrays.asList(new String[] {
			"internalName", "name", "linkName", "type", "filters", "attributes", "linkVersion", "default"
			
			// Not handled yet
			
	}));
	final List<String> datasetConfigProperties = new ArrayList<String>(Arrays.asList(new String[] {
			"dataset", "description", "displayName", "visible",
			
			// Not handled yet
			"datasetID", "defaultDataset", "entryLabel", "interfaces", "internalName", "martUsers", 
			"modified", "softwareVersion", "template", "type", "version"
	}));

	private Element transformFilterDescription(Element filterDescription, Element parentNewContainer) throws Exception {		
		return transformFilterDescription(filterDescription, parentNewContainer, null, null);
	}
	private Element transformFilterDescription(Element filterDescription, Element parentNewContainer, 
			Boolean forcedVisibility, Map<String, String> subBooleanDisplayNamesMap) throws Exception {		
		// Ignore hidden elements (make be broken)
		if (isHiddenElement(filterDescription)) {
			return null;
		}
		
		Element newFilterDescription = new Element("filterPointer");
		
		String internalName = getMandatoryPropertyValue(filterDescription, "internalName");
		newFilterDescription.setAttribute("name", internalName);
/*if (internalName.equals("proteome_name")) {
	return null;
}*/
		
		// Determine if pointer
		String pointerDataset = getOptionalPropertyValue(filterDescription, "pointerDataset");
		String pointerFilter = getOptionalPropertyValue(filterDescription, "pointerFilter");
		String pointerInterface = getOptionalPropertyValue(filterDescription, "pointerInterface");
		PointerInfo pointerInfo = new PointerInfo(pointerDataset, pointerFilter, pointerInterface);
		boolean pointer = pointerInfo.isValid();
		newFilterDescription.setAttribute("pointer", String.valueOf(pointer));
			
		System.out.print("==> " + internalName);
		
		if (!pointer) {
			
			// Display Name
			String displayName = getOptionalPropertyValue(filterDescription, "displayName");
			if (displayName!=null) {
				newFilterDescription.setAttribute("displayName", displayName);
			}
			
			// Description
			String description = getOptionalPropertyValue(filterDescription, "description");
			if (description!=null) {
				newFilterDescription.setAttribute("description", description);
			}
	
			// Default (selected by default or not)
			String default_ = getOptionalPropertyValue(filterDescription, "default");
			if (default_!=null) {
				newFilterDescription.setAttribute("default", default_);
			}
			
			// Hide display (!=hidden)
			Boolean hideDisplay = getOptionBooleanValue(filterDescription, "hideDisplay");
			if (forcedVisibility!=null) {
				hideDisplay = !forcedVisibility;
			}
			newFilterDescription.setAttribute("targetRange", 
					getRangeWithVisibilityString(this.defaultRange, hideDisplay));		
			
			// Default value
			String defaultValue = getOptionalPropertyValue(filterDescription, "defaultValue");
			if (null!=defaultValue) {
				newFilterDescription.setAttribute("defaultValue", defaultValue);
			}
			
			// Regexp
			String regexp = getOptionalPropertyValue(filterDescription, "regexp");
			if (null!=regexp) {
				newFilterDescription.setAttribute("regexp", regexp);
			}
			
			// Multiple values
			String multipleValues = getOptionalPropertyValue(filterDescription, "multipleValues");
			if (null!=multipleValues) {
				newFilterDescription.setAttribute("multiple", MartConfiguratorUtils.binaryDigitToBoolean(multipleValues).toString());
			}
			
			// Display Type & type
			String oldDisplayType = getOptionalPropertyValue(filterDescription, "displayType");
			FilterOldDisplayType filterOldDisplayType = null;//!=oldDisplayType ? FilterOldDisplayType.getFilterOldDisplayType(oldDisplayType) : FilterOldDisplayType.EMPTY;
			
			String oldType = getOptionalPropertyValue(filterDescription, "type");
			FilterOldType filterOldType = null!=oldType ? FilterOldType.getFilterOldType(oldType) : FilterOldType.EMPTY;

			System.out.print(MyUtils.TAB_SEPARATOR + filterOldDisplayType + 
					MyUtils.TAB_SEPARATOR + filterOldType);
			
			TransformationFilterDisplayType filterDisplayType = null;/*TODO broken FilterTransformation.getTransformationFilterDisplayType(
					filterOldDisplayType, filterOldType);*/
			newFilterDescription.setAttribute("displayType", filterDisplayType.getValue());	
			
			System.out.print(MyUtils.TAB_SEPARATOR + filterDisplayType + MyUtils.TAB_SEPARATOR + 
					pointer + MyUtils.TAB_SEPARATOR);
			
			// Fill path info
			fillPathInfo(newFilterDescription);
			
			// If part of a cascade: update cascade info
			CascadeReference cascadeReference = refMap.get(internalName);
			if (cascadeReference!=null) {
				newFilterDescription.setAttribute("cascadeParent", cascadeReference.getCascadeParent());
				newFilterDescription.setAttribute("orderBy", cascadeReference.getOrderBy());
			}
			
			// Determine if container of filters
			ContainerFilterInfo containerFilterInfo = isContainer(filterDescription);
			System.out.println(containerFilterInfo);

			// If cascade or not container at all, update database info
			if ((containerFilterInfo.container && containerFilterInfo.cascade) || 
					!containerFilterInfo.container) {
				
				// Qualifier & legal_qualifiers
				String qualifier = getOptionalPropertyValue(filterDescription, "qualifier");
				String legal_qualifiers = getOptionalPropertyValue(filterDescription, "legal_qualifiers");
				/*MyUtils.checkStatusProgram(qualifier!=null && qualifier.equals(legal_qualifiers), 
						"qualifier = " + qualifier + ", legal_qualifiers = " + legal_qualifiers, true);*/
				if (null!=qualifier) {
					newFilterDescription.setAttribute("qualifier", qualifier);
				}
				
				// Fill tables info
				Field field = new Field(getMandatoryPropertyValue(filterDescription, "tableConstraint"), 
						getMandatoryPropertyValue(filterDescription, "key"), 
						getMandatoryPropertyValue(filterDescription, "field"));	
				
				updateTables(field, filterDescription.getName() + MyUtils.INFO_SEPARATOR + internalName);
				newFilterDescription.setAttribute("table", field.getTableConstraint()/*field.getTableName()*/);
				newFilterDescription.setAttribute("field", field.getFieldName());
			}	
	
			// Process embedded data and/or filters
			List<String> filterList = null;
			StringBuffer dataStringBuffer = null;
			if (filterDisplayType.equals(TransformationFilterDisplayType.LIST)) {		// e.g "biotype"
				
				// Get data in options
				dataStringBuffer = fetchData(filterDescription);
				
				// If a cascade, go deeper to pull out children data
				if (containerFilterInfo.cascade) {	// e.g "chromosome_name
					
					// Go through each "Option" children
					int rowNumber = 0;
					Set<String> setChildren = new HashSet<String>();
					List<Element> filterDescriptionOptions = filterDescription.getChildren();
					for (Element filterDescriptionOption : filterDescriptionOptions) {
						
						// Pull out data from each "pushed" filter
						// (at this stage we assume a counterpart filter is declared somewhere and we get data for it here)
						StringBuffer subDataStringBuffer = null;
						List<Element> filterDescriptionOptions2 = filterDescriptionOption.getChildren();
						for (Element filterDescriptionOption2 : filterDescriptionOptions2) {
							
							// Update ref list
							String ref = getMandatoryPropertyValue(filterDescriptionOption2, "ref");
							String orderBy = getOptionalPropertyValue(filterDescriptionOption2, "orderBy");
							setChildren.add(ref);
							CascadeReference newCascadeReference = new CascadeReference(internalName, orderBy);
							CascadeReference previousCascadeReference = refMap.get(ref);
							MyUtils.checkStatusProgram(previousCascadeReference==null || 
									newCascadeReference.equals(previousCascadeReference), "", true);		// It shouldn't be possible for the same element to have 2 parents
							if (null==previousCascadeReference) {
								refMap.put(ref, newCascadeReference);
							}
							
							// Only getting the data for now
							subDataStringBuffer = fetchData(filterDescriptionOption2);
							
							
							String folderPathAndName = this.defaultDataFolderPath + MyUtils.FILE_SEPARATOR + ref;
							new File(folderPathAndName).mkdirs();
							String dataFilePathAndName = folderPathAndName + MyUtils.FILE_SEPARATOR + ref + 
							MyUtils.INFO_SEPARATOR + MyUtils.INFO_SEPARATOR + internalName + MyUtils.INFO_SEPARATOR + rowNumber;	// ref + (parent's) rowNumber is a unique combination
							MyUtils.writeFile(dataFilePathAndName, subDataStringBuffer.toString());
						}
						
						rowNumber++;
					}
					
					MyUtils.checkStatusProgram(!setChildren.isEmpty(), "", true);
					newFilterDescription.setAttribute("cascadeChildren", MartConfiguratorUtils.collectionToCommaSeparatedString(setChildren));
				}
			} else if (filterDisplayType.equals(TransformationFilterDisplayType.BOOLEAN)) {		// e.g "with_tranmembrane_domain"
							
				// Get data
				Map<String, String> displayNamesMap = subBooleanDisplayNamesMap==null ? 
						new LinkedHashMap<String, String>() : subBooleanDisplayNamesMap;	// Needs to respect the order
				transformFilterDropdownOption(filterDescription, displayNamesMap);			
				if (subBooleanDisplayNamesMap==null) {
					dataStringBuffer = getBooleanData(displayNamesMap);
				}
			} else if (filterDisplayType.equals(TransformationFilterDisplayType.LIST_BOOLEAN)) {		// e.g "new_id_list_filters"
				
				// Create list of sub-filters & populate filterList accordingly
				filterList = new ArrayList<String>();
				Map<String, String> displayNamesMap = new LinkedHashMap<String, String>();	// Needs to respect the order
				List<Element> filterDescriptionOptions = filterDescription.getChildren();
				for (Element filterDescriptionOption : filterDescriptionOptions) {
					Element newFilterDropdownOption = 
						transformFilterDescription(filterDescriptionOption, null, false, displayNamesMap);
						//transformFilterDropdownOption(filterDescriptionOption, displayNamesMap);
					parentNewContainer.addContent(newFilterDropdownOption);
					String newFilterDropdownOptionName = getMandatoryPropertyValue(newFilterDropdownOption, "name");
					filterList.add(newFilterDropdownOptionName);
				}
				
				// Get data
				dataStringBuffer = getBooleanData(displayNamesMap);
				
			} else if (filterDisplayType.equals(TransformationFilterDisplayType.LIST_TEXTFIELD)) {		// e.g "id_list_limit_filters"
				
				// Create list of sub-filters & populate filterList accordingly
				filterList = new ArrayList<String>();
				List<Element> filterDescriptionOptions = filterDescription.getChildren();
				for (Element filterDescriptionOption : filterDescriptionOptions) {
					Element newFilterDropdownOption = transformFilterDescription(filterDescriptionOption, null, false, null);
					parentNewContainer.addContent(newFilterDropdownOption);
					String newFilterDropdownOptionName = getMandatoryPropertyValue(newFilterDropdownOption, "name");
					filterList.add(newFilterDropdownOptionName);
				}
				
				// No data to get
			}
			newFilterDescription.setAttribute("displayType", filterDisplayType.getValue());
	
			// Write data if any
			boolean hasData = dataStringBuffer!=null;
			if (hasData) {
				newFilterDescription.setAttribute("dataFolderPath", this.defaultDataFolderPath);
				String dataFilePathAndName = this.defaultDataFolderPath + MyUtils.FILE_SEPARATOR + internalName;	// internalName is unique
				MyUtils.writeFile(dataFilePathAndName, dataStringBuffer.toString());
			}
			
			// Fill filterList property
			if (containerFilterInfo.container && !containerFilterInfo.cascade
									&& filterList!=null	// fornow
					) {
				StringBuffer filterListStringBuffer = new StringBuffer();
				for (int i = 0; i < filterList.size(); i++) {
					filterListStringBuffer.append((i==0 ? "" : "," ) + filterList.get(i));
				}
				filterListStringBuffer.toString();
				newFilterDescription.setAttribute("filterList", filterListStringBuffer.toString());
			}
		} else {
			
			// Fill pointer & pointer path info
			fillPointerInfo(newFilterDescription, pointerInfo, false);
			
			System.out.println();
		}
		
		// Check for other property
		printOutUntreatedProperties(filterProperties, filterDescription);
		
		return newFilterDescription;
	}

	private StringBuffer fetchData(Element filter) throws Exception {
		StringBuffer dataStringBuffer = new StringBuffer();
		int rowNumber = 0;
		List<Element> filterDescriptionOptions = filter.getChildren();
		for (Element filterDescriptionOption : filterDescriptionOptions) {
			String dataFileRow = extractCascadeOptionInfo(rowNumber, filterDescriptionOption);
			dataStringBuffer.append(dataFileRow + MyUtils.LINE_SEPARATOR);
			rowNumber++;
		}
		return dataStringBuffer;
	}
	
	private void fillPathInfo(Element element) {
		element.setAttribute("location", this.currentPath.getLocation());
		element.setAttribute("mart", this.currentPath.getMart());
		element.setAttribute("version", this.currentPath.getVersion());
		element.setAttribute("dataset", this.currentPath.getDataset());
		element.setAttribute("config", this.currentPath.getConfig());
	}

	private StringBuffer getBooleanData(Map<String, String> displayNamesMap) {
		StringBuffer dataStringBuffer;
		// Handle the displayName of the 2 boolean values
		MyUtils.checkStatusProgram(displayNamesMap.size()==2, "", true);
		dataStringBuffer = new StringBuffer();
		String displayNameForFalse = "";
		String displayNameForTrue = "";
		for (Iterator<String> it = displayNamesMap.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			String value = displayNamesMap.get(key);
			if (key.equals(TransformationConstants.BOOLEAN_FILTER_EXCLUDED_VALUE) && value.equals(TransformationConstants.BOOLEAN_FILTER_EXCLUDED_DISPLAY_NAME)) {
				displayNameForFalse = TransformationConstants.BOOLEAN_FILTER_EXCLUDED_DISPLAY_NAME;
			} else if (key.equals(TransformationConstants.BOOLEAN_FILTER_ONLY_VALUE) && value.equals(TransformationConstants.BOOLEAN_FILTER_ONLY_DISPLAY_NAME)) {
				displayNameForTrue = TransformationConstants.BOOLEAN_FILTER_ONLY_DISPLAY_NAME;
			}
		}
		MyUtils.checkStatusProgram(null!=displayNameForFalse && null!=displayNameForTrue, "", true);
		dataStringBuffer.append(0 + MyUtils.TAB_SEPARATOR + false + MyUtils.TAB_SEPARATOR + displayNameForFalse + MyUtils.LINE_SEPARATOR);
		dataStringBuffer.append(1 + MyUtils.TAB_SEPARATOR + true + MyUtils.TAB_SEPARATOR + displayNameForTrue + MyUtils.LINE_SEPARATOR);
		return dataStringBuffer;
	}
	private Element transformFilterDropdownOption(Element filterDescriptionOption, Map<String, String> displayNamesMap) throws Exception {
		// Ignore hidden elements (make be broken)
		if (isHiddenElement(filterDescriptionOption)) {
			return null;
		}
		
		Element newfilterDescriptionOption = new Element("filterPointer");
		
		String internalName = getMandatoryPropertyValue(filterDescriptionOption, "internalName");
		newfilterDescriptionOption.setAttribute("name", internalName);
				
		// Determine if pointer
		String pointerDataset = getOptionalPropertyValue(filterDescriptionOption, "pointerDataset");
		String pointerFilter = getOptionalPropertyValue(filterDescriptionOption, "pointerFilter");
		String pointerInterface = getOptionalPropertyValue(filterDescriptionOption, "pointerInterface");
		PointerInfo pointerInfo = new PointerInfo(pointerDataset, pointerFilter, pointerInterface);
		boolean pointer = pointerInfo.isValid();
		newfilterDescriptionOption.setAttribute("pointer", String.valueOf(pointer));
						
		String displayName = getOptionalPropertyValue(filterDescriptionOption, "displayName");
		if (displayName!=null) {
			newfilterDescriptionOption.setAttribute("displayName", displayName);
		}
		
		String description = getOptionalPropertyValue(filterDescriptionOption, "description");
		if (description!=null) {
			newfilterDescriptionOption.setAttribute("description", description);
		}

		String default_ = getOptionalPropertyValue(filterDescriptionOption, "default");
		if (default_!=null) {
			newfilterDescriptionOption.setAttribute("default", default_);
		}
		
		String optionIsSelectable = getOptionalPropertyValue(filterDescriptionOption, "isSelectable");

		//Boolean hideDisplay = getOptionBooleanValue(filterDescriptionOption, "hideDisplay");
		newfilterDescriptionOption.setAttribute("targetRange", 
				getRangeWithVisibilityString(this.defaultRange, true));	// Always invisible (only visible as part of the grouping)		
		
		// No real data needed to be store in this case (we know the values of a boolean), only interested in the displayNames
		List<Element> filterDescriptionOptionOptions = filterDescriptionOption.getChildren();
		int count = 0;
		for (Element filterDescriptionOptionOption : filterDescriptionOptionOptions) {
			String optionValue = getMandatoryPropertyValue(filterDescriptionOptionOption, "value");
			String optionDisplayName = getMandatoryPropertyValue(filterDescriptionOptionOption, "displayName");
			MyUtils.checkStatusProgram(optionValue.equalsIgnoreCase(optionDisplayName), "", true);
			
			String preExistingDisplayName = displayNamesMap.get(optionValue);
			if (preExistingDisplayName==null) {
				displayNamesMap.put(optionValue, optionDisplayName);				
			} else {
				MyUtils.checkStatusProgram(preExistingDisplayName.equals(optionDisplayName), "", true);
			}
			count++;
		}
		MyUtils.checkStatusProgram(count==2, "", true);
		
		// Check for other property
		printOutUntreatedProperties(filterProperties, filterDescriptionOption);
		
		return newfilterDescriptionOption;
	}
	
	private Element transformAttributeDescription(Element attributeDescription) throws Exception {		
		// Ignore hidden elements (make be broken)
		if (isHiddenElement(attributeDescription)) {
			return null;
		}
		
		Element newAttributeDescription = new Element("attributePointer");
		
		String internalName = getMandatoryPropertyValue(attributeDescription, "internalName");
		if (attributesWithFilterPointer.contains(internalName)) {
			return null;
		}	
		newAttributeDescription.setAttribute("name", internalName);
				
		String pointerDataset = getOptionalPropertyValue(attributeDescription, "pointerDataset");
		String pointerAttribute = getOptionalPropertyValue(attributeDescription, "pointerAttribute");
		String pointerInterface = getOptionalPropertyValue(attributeDescription, "pointerInterface");
		PointerInfo pointerInfo = new PointerInfo(pointerDataset, pointerAttribute, pointerInterface);
		boolean pointer = pointerInfo.isValid();
		
		Boolean hideDisplay = getOptionBooleanValue(attributeDescription, "hideDisplay");
		// Add a default targetRange
		newAttributeDescription.setAttribute("targetRange", 
				getRangeWithVisibilityString(this.defaultRange, hideDisplay));	
		
		String displayName = getOptionalPropertyValue(attributeDescription, "displayName");
		if (displayName!=null) {
			newAttributeDescription.setAttribute("displayName", displayName);
		}
		
		String description = getOptionalPropertyValue(attributeDescription, "description");
		if (description!=null) {
			newAttributeDescription.setAttribute("description", description);
		}
		
		String linkoutURL = getOptionalPropertyValue(attributeDescription, "linkoutURL");
		if (linkoutURL!=null) {
			newAttributeDescription.setAttribute("linkURL", linkoutURL);	// name changes
		}
		
		String maxLength = getOptionalPropertyValue(attributeDescription, "maxLength");
		if (maxLength!=null) {
			newAttributeDescription.setAttribute("maxLength", maxLength);
		}
		
		String default_ = getOptionalPropertyValue(attributeDescription, "default");
		if (default_!=null) {
			newAttributeDescription.setAttribute("default", default_);
		}
	
		if (!pointer) {
			
			// Fill path info
			fillPathInfo(newAttributeDescription);
			
			// Fill tables info
			Field field = new Field(getMandatoryPropertyValue(attributeDescription, "tableConstraint"), 
					getMandatoryPropertyValue(attributeDescription, "key"), 
					getMandatoryPropertyValue(attributeDescription, "field"));			
			updateTables(field, attributeDescription.getName() + MyUtils.INFO_SEPARATOR + internalName);
			newAttributeDescription.setAttribute("table", field.getTableConstraint()/*field.getTableName()*/);
			newAttributeDescription.setAttribute("field", field.getFieldName());
		} else {
			// Fill pointer & pointer path info
			fillPointerInfo(newAttributeDescription, pointerInfo, true);
		}

		// Check for other property
		printOutUntreatedProperties(attributeProperties, attributeDescription);
		
		return newAttributeDescription;
	}

	private void fillPointerInfo(Element element, PointerInfo pointerInfo, boolean attribute) {
		element.setAttribute("pointer", String.valueOf(true));
		element.setAttribute((attribute ? "attributePointer" : "filterPointer"), pointerInfo.getPointerElementName());
		
		// If local pointer
		if (pointerInfo.getPointerDatasetName().equals(this.datasetName)) {
			fillPathInfo(element);
		} else {
			element.setAttribute("dataset", pointerInfo.getPointerDatasetName());
			element.setAttribute("formerVirtualSchema", this.virtualSchemaName);	
			element.setAttribute("location", NOT_APPLICABLE);
			element.setAttribute("mart", NOT_APPLICABLE);
			element.setAttribute("version", NOT_APPLICABLE);
			element.setAttribute("config", NOT_APPLICABLE);
		}
	}

	private String extractCascadeOptionInfo(int rowNumber, Element filterDescriptionOption) throws Exception {
		String optionInternalName = getMandatoryPropertyValue(filterDescriptionOption, "internalName");
		String optionDisplayName = getMandatoryPropertyValue(filterDescriptionOption, "displayName");
		String optionIsSelectable = getMandatoryPropertyValue(filterDescriptionOption, "isSelectable");
		
//		 for now
		/*String optionValue = getMandatoryPropertyValue(filterDescriptionOption, "value");
		if (false)	
		MyUtils.checkStatusProgram(optionValue.equals(optionDisplayName), 
				"optionValue = " + optionValue + ", optionDisplayName = " + optionDisplayName, true);*/
		
		String dataFileRow = rowNumber + MyUtils.TAB_SEPARATOR + optionInternalName + MyUtils.TAB_SEPARATOR + 
		optionIsSelectable + MyUtils.TAB_SEPARATOR + optionDisplayName;
		return dataFileRow;
	}
	
	private ContainerFilterInfo isContainer(Element filterDescription) throws Exception {
		ContainerFilterInfo containerFilterInfo = new ContainerFilterInfo();
		List<Element> children = filterDescription.getChildren();
		if (null!=children && !children.isEmpty()) {
			Element firstChild = children.get(0);
			
			// Either child has children itself or child is a filter (as opposed to a value) 
			List<Element> subChildren = firstChild.getChildren();
			boolean childHasChildren = null!=subChildren && !subChildren.isEmpty();
			
			String childField = getOptionalPropertyValue(firstChild, "field");
			String childPointer = getOptionalPropertyValue(firstChild, "pointer");
			boolean childIsFilter = (childField!=null && !MyUtils.isEmpty(childField)) || 
			(childPointer!=null && !MyUtils.isEmpty(childPointer) && !childPointer.equals(String.valueOf(true)));
			
			containerFilterInfo.container = childHasChildren || childIsFilter;
			containerFilterInfo.cascade = childHasChildren && subChildren.get(0).getName().equals("PushAction");
			
			if (containerFilterInfo.container) {
				/*String oldDisplayType = getOptionalPropertyValue(firstChild, "displayType");
				containerFilterInfo.subFilterOldDisplayType = FilterOldDisplayType.getFilterOldDisplayType(oldDisplayType);
				String oldType = getOptionalPropertyValue(firstChild, "type");
				containerFilterInfo.subFilterOldType = FilterOldType.getFilterOldType(oldType);
				containerFilterInfo.subFilterDisplayType = FilterDisplayType.getFilterDisplayType(
						filterOldDisplayType, filterOldType);*/
			}
		}
		return containerFilterInfo;
	}

	private void printOutUntreatedProperties(List<String> listTreatedProperties, Element element) throws Exception {
		List<Attribute> elementAttributeList = element.getAttributes();
		for (Attribute attribute : elementAttributeList) {		
			String propertyName = attribute.getName();
			String propertyValue = attribute.getValue();
			
			String internalName = getOptionalPropertyValue(element, "internalName");
			String name = getOptionalPropertyValue(element, "name");
			String elementName = internalName!=null ? internalName : name;
			
			if (!listTreatedProperties.contains(propertyName) && 
					!attributeContainersWithMaxSelect.contains(elementName) && 
					!attributesWithFilterPointer.contains(elementName)) {
				System.out.println("***************** " + element.getName() + ", " + elementName + ", " + 
						propertyName + " = " + propertyValue);				
			}
		}
	}

	private String getRangeString(Element partitionTable, String rowValue) throws Exception {
		String partitionTableName = getMandatoryPropertyValue(partitionTable, "name");
		List<Element> partList = partitionTable.getChildren();
		int row = 0;
		for (Element part : partList) {
			if (part.getValue().equals(rowValue)) {
				return getRangeString(partitionTableName, row, null);
			}
			row++;
		}
		throw new Exception("not found");
	}
	private String getRangeString(String partitionTableName, Integer row, Integer col) {
		MyUtils.checkStatusProgram((row!=null && col==null) || (row==null && col!=null), "", true);
		String string = partitionTableName + (row!=null ? "R" : "C") + (row!=null ? row : col);
		return string;
	}
	private String getRangeWithVisibilityString(String partitionTableName, Integer row, Integer col, boolean hideDisplay) {
		return getRangeWithVisibilityString(getRangeString(partitionTableName, row, col), hideDisplay);
	}
	private String getRangeWithVisibilityString(String rangeString, boolean hideDisplay) {
		return "[" + rangeString + ":" + MartConfiguratorUtils.booleanToBinaryDigit(!hideDisplay) + "]";
	}
	private String getRangeWithoutVisibilityString(String rangeString) {
		return "[" + rangeString + "]";
	}
	private String getRangeOnColumnString(String partitionTableName, Integer col) {
		return "(" + getRangeString(partitionTableName, null, col) + ")";
	}
	
	private String getElementMandatoryValue(Element element) throws Exception {
		String value = element.getValue();
		if (value==null || "".equals(value)) {
			throw new Exception(element.getName());
		}
		return value;
	}
	private String getMandatoryPropertyValue(Element element, String name) throws Exception {
		Attribute attribute = element.getAttribute(name);
		if (attribute==null || "".equals(attribute.getValue())) {
			throw new Exception(element.getName() + " = " + 
					getOptionalPropertyValue(element, "internalName"));
		}
		return attribute.getValue();
	}
	private boolean getOptionBooleanValue(Element attributeDescription, String name) throws Exception {
		String stringValue = getOptionalPropertyValue(attributeDescription, name);
		return stringValue!=null ? Boolean.valueOf(stringValue) : false;
	}
	private String getOptionalPropertyValue(Element element, String name) throws Exception {
		Attribute attribute = element.getAttribute(name);
		return attribute!=null ? attribute.getValue() : null;
	}
	@Deprecated
	private void updateTables2(Field field, String sourceDescription) throws Exception {
		List<Element> tables = this.tables.getChildren();
		Element table = null;
		String tableName = null;
		/*if (null!=tables) {*/
		for (Element currentTable : tables) {
			String currentTableName = getMandatoryPropertyValue(currentTable, "name");
			String currentTableKey = getMandatoryPropertyValue(currentTable, "key");
			if ((field.isMain() && isMainTable(currentTableName) && currentTableKey.equalsIgnoreCase(field.getKeyName())) || 
					(!field.isMain() && currentTableName.equalsIgnoreCase(field.getTableConstraint()))) {
				table = currentTable;
				tableName = currentTableName;
				break;
			}
		}
		/*}*/
		
		MyUtils.checkStatusProgram(!field.isMain() || (field.isMain() && table!=null), 
				"field.isMain() = " + field.isMain() + ", table = " + table + ", field = " + field + ", sourceDescription = " + sourceDescription, true);
		
		// Update field with appropriate MainTable name if a main table
		if (field.isMain()) {
			field.setMainTableName(tableName);
		}
		
		// Create table if doesn't exist yet (can't be for a main)
		if (null==table) {
			MyUtils.checkStatusProgram(!field.isMain(), "", true);
			table = addNewTable(field);
		} else {
			updateTableFields(field, table);
		}
	}
	
	private void updateTables(Field field, String sourceDescription) throws Exception {
		List<Element> tables = this.tables.getChildren();
		Element table = null;
		String tableName = null;
		if (field.isMain()) {
			for (Element currentTable : tables) {
				String currentTableName = getMandatoryPropertyValue(currentTable, "name");
				if (isMainTable(currentTableName)) {
					table = currentTable;
					tableName = currentTableName;
					break;
				}
			}
		} else {
			for (Element currentTable : tables) {
				String currentTableName = getMandatoryPropertyValue(currentTable, "name");
				String currentTableKey = getMandatoryPropertyValue(currentTable, "key");
				if (currentTableName.equalsIgnoreCase(field.getTableConstraint()) &&
						currentTableKey.equalsIgnoreCase(field.getKeyName())) {
					table = currentTable;
					tableName = currentTableName;
					break;
				}
			}
		}
		
		// Create table if doesn't exist yet (can't be for a main)
		if (null==table) {
			table = addNewTable(field);
		} else {
			if (field.isMain()) {	
				updateMainTableKeys(field, table);
			}
			updateTableFields(field, table);
		}
	}

	private void updateMainTableKeys(Field field, Element table) throws Exception {
		// Check that key is already there	
		String stringKeys = getMandatoryPropertyValue(table, "key");
		String[] keys = stringKeys.split(MartServiceConstants.ELEMENT_SEPARATOR);
		if (!MyUtils.contains(keys, field.getKeyName())) {
			stringKeys += MartServiceConstants.ELEMENT_SEPARATOR + field.getKeyName();
			table.setAttribute("key", stringKeys);
		}
	}

	private void updateTableFields(Field field, Element table) throws Exception {
		// Check if the field already exists
		List<Element> tableFields = table.getChildren();
		MyUtils.checkStatusProgram(tableFields!=null, "", true);	// can't exist but have no fields
		
		Element tableField = null;
		for (Element currentField : tableFields) {
			String tableFieldName = getElementMandatoryValue(currentField);
			if (tableFieldName.equals(field.getFieldName())) {
				tableField = currentField;
				break;
			}
		}
		
		// New field, add it
		if (tableField==null) {
			Element newTableField = new Element("attribute");
			newTableField.setText(field.getFieldName().toLowerCase());
			table.addContent(newTableField);
		}
	}
	@Deprecated
	private boolean isMainTable2 (String tableName) {
		return tableName.endsWith("__main");
	}
	private boolean isMainTable (String tableName) {
		return tableName.equals(MartServiceConstants.MAIN_TABLE_CONSTRAINT);
	}

	private Element addNewTable(MainTableWithKey mainTableWithKey) {
		return addNewTable(mainTableWithKey.tableName, mainTableWithKey.keyName, mainTableWithKey.keyName);
	}
	private Element addNewTable(Field field) {
		/*MyUtils.checkStatusProgram(!field.isMain(), "", true);*/
		return addNewTable(field.getTableConstraint(),	// Must never be a main at this point 
				field.getKeyName(), field.getFieldName());
	}
	private Element addNewTable(String tableName, String keyName, String fieldName) {
		Element mainTable = new Element("table");
		mainTable.setAttribute("name", tableName.toLowerCase());
		mainTable.setAttribute("key", keyName.toLowerCase());
		
		Element field = new Element("attribute");
		field.setText(keyName.toLowerCase());
		
		mainTable.addContent(field);
		
		this.tables.addContent(mainTable);
		
		return mainTable;
	}
}
