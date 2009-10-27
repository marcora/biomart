package org.biomart.transformation.helpers;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.helpers.DatabaseParameter;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;


public class DatabaseCheck {
	
	public static void main(String[] args) throws TechnicalException, FunctionalException {
		/*DatabaseCheck databaseCheck = new DatabaseCheck(
				new DatabaseParameter(Rdbs.MYSQL, "martdb.ensembl.org", 5316, "anonymous", "", "ensembl_mart_55"),
				Transformation.TABLE_LIST_SERIAL_FILE_PATH_AND_NAME, Transformation.TABLE_COLUMN_MAP_CONTENT_SERIAL_FILE_PATH_AND_NAME);
		databaseCheck.connect();
		databaseCheck.fetchTableList();
		databaseCheck.fetchTableDescriptions();
		System.out.println(databaseCheck.checkDatabase("hsapiens_gene_ensembl__gene__main", "gene_id_1020_key"));
		System.out.println(databaseCheck.checkDatabase("hsapiens_gene_ensembl__gene__mai", "gene_id_1020_key"));
		System.out.println(databaseCheck.checkDatabase("hsapiens_gene_ensembl__gene__main", "gene_id_1020_ke"));
		databaseCheck.disconnect();*/
	}
	

	public static final String DATABASE_MAP_SERIAL_FILE_NAME = "DatabaseMap";
	public static final String DATASET_MAP_SERIAL_FILE_NAME = "DatasetMap";
	public static final String DATASET_MAP2_SERIAL_FILE_NAME = "DatasetMap2";
	public static final String TEMPLATE_MAP_SERIAL_FILE_NAME = "TemplateMap";
	public static final String TABLE_LIST_SERIAL_FILE_PREFIX = "TableList_";
	public static final String TABLE_COLUMN_LIST_SERIAL_FILE_PREFIX = "TableColumnList_";
	public static final String TEMPLATE_XML_CONTENT_SERIAL_FILE_PREFIX = "TemplateXml_";

	private DatabaseParameter databaseParameter = null;


	private String[] databaseNames = null;
	private String serialFolderPathAndName = null;
	private String transformationsGeneralSerialFolderPathAndName = null;
	
	private Connection connection = null;
	private PreparedStatement preparedStatementDatabase = null;
	private PreparedStatement preparedStatementTable = null;
	private PreparedStatement preparedStatementField = null;
	private PreparedStatement preparedStatementDatasetXml = null;
	private PreparedStatement preparedStatementTemplateXml = null;
	
	private HashMap<String, HashMap<String, List<String>>> databaseNameToDatasetInformationMap = null;	//TODO useless?
	private HashMap<String, TemplateDatabaseDescription> templateNameToDatabaseDescriptionMap = null;
	private HashMap<String, String> templateNameToDatabaseNameMap = null;
	private HashMap<String, String> datasetNameToTemplateName = null;
	private HashMap<String, String> datasetNameToDatasetType = null;

	public DatabaseCheck(DatabaseParameter databaseParameter, String[] databaseNames, 
			String transformationsGeneralOutput, String generalOutputFolderPathAndName) {		
		super();
		this.databaseParameter = databaseParameter;
		this.databaseNames = databaseNames;
		this.transformationsGeneralSerialFolderPathAndName = 
			transformationsGeneralOutput + MyUtils.FILE_SEPARATOR + TransformationConstants.TRANSFORMATIONS_GENERAL_SERIAL_FOLDER_NAME + MyUtils.FILE_SEPARATOR;
		this.serialFolderPathAndName = generalOutputFolderPathAndName + TransformationConstants.SERIAL_FOLDER_NAME + MyUtils.FILE_SEPARATOR;
		
		// Create folder if doesn't exist
		File serialFolder = new File(this.serialFolderPathAndName);
		if (!serialFolder.exists()) {
			serialFolder.mkdirs();
		}
		
		this.templateNameToDatabaseDescriptionMap = new HashMap<String, TemplateDatabaseDescription>();
		//this.datasetNameToDatabaseNameMap = new HashMap<String, String>();
		this.templateNameToDatabaseNameMap = new HashMap<String, String>();
		this.datasetNameToTemplateName = new HashMap<String, String>();
		this.datasetNameToDatasetType = new HashMap<String, String>();
	}
	
	public void connect(String databaseName) throws TechnicalException {
		
		this.databaseParameter.setDatabaseName(databaseName);
		
		try {
			String connectionString = null;
			if (databaseParameter.getRdbs().isMySql()) {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				connectionString = "jdbc:mysql://" + databaseParameter.getDatabaseHost() + ":" + 
				databaseParameter.getDatabasePort() + "/" + databaseParameter.getDatabaseName();
			} else if (databaseParameter.getRdbs().isPostgreSql()) {
				connectionString = "jdbc:postgresql://" + databaseParameter.getDatabaseHost() + ":" + 
				databaseParameter.getDatabasePort() + "/" + databaseParameter.getDatabaseName();
			} else if (databaseParameter.getRdbs().isOracle()) {
				connectionString = "jdbc:oracle:thin:@" + databaseParameter.getDatabaseHost() + ":" + 
				databaseParameter.getDatabasePort() + ":" + databaseParameter.getDatabaseName();
				DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());			// Need to load driver
			}

			connection = DriverManager.getConnection(
					connectionString, databaseParameter.getDatabaseUser(), databaseParameter.getDatabasePassword());
			
		} catch (SQLException e) {
			throw new TechnicalException(e);
		} catch (InstantiationException e) {
			throw new TechnicalException(e);
		} catch (IllegalAccessException e) {
			throw new TechnicalException(e);
		} catch (ClassNotFoundException e) {
			throw new TechnicalException(e);
		}
	}
	
	public void fetchDatasetInformation() throws TechnicalException {
		
		try {
			String databaseMapSerialFilePathAndName = this.serialFolderPathAndName + DATABASE_MAP_SERIAL_FILE_NAME;
			String templateMapSerialFilePathAndName = this.serialFolderPathAndName + TEMPLATE_MAP_SERIAL_FILE_NAME;
			String datasetMapSerialFilePathAndName = this.serialFolderPathAndName + DATASET_MAP_SERIAL_FILE_NAME;
			String datasetMap2SerialFilePathAndName = this.serialFolderPathAndName + DATASET_MAP2_SERIAL_FILE_NAME;
			if (new File(databaseMapSerialFilePathAndName).exists() && new File(templateMapSerialFilePathAndName).exists() && 
					new File(datasetMapSerialFilePathAndName).exists() && new File(datasetMap2SerialFilePathAndName).exists()) {
				databaseNameToDatasetInformationMap = (HashMap<String, HashMap<String, List<String>>>)MyUtils.readSerializedObject(
						databaseMapSerialFilePathAndName);
				System.out.println("Using serial, quick check: " + this.databaseNameToDatasetInformationMap.size());
				/*datasetNameToDatabaseNameMap = (HashMap<String, String>)MyUtils.readSerializedObject(
						datasetMapSerialFilePathAndName);
				System.out.println("Using serial, quick check: " + this.datasetNameToDatabaseNameMap.size());*/
				templateNameToDatabaseNameMap = (HashMap<String, String>)MyUtils.readSerializedObject(
						templateMapSerialFilePathAndName);
				System.out.println("Using serial, quick check: " + this.templateNameToDatabaseNameMap.size());
				datasetNameToTemplateName = (HashMap<String, String>)MyUtils.readSerializedObject(
						datasetMapSerialFilePathAndName);
				System.out.println("Using serial, quick check: " + this.datasetNameToTemplateName.size());
				datasetNameToDatasetType = (HashMap<String, String>)MyUtils.readSerializedObject(
						datasetMap2SerialFilePathAndName);
				System.out.println("Using serial, quick check: " + this.datasetNameToDatasetType.size());
			} else {
				
				System.out.println("No serial at " + databaseMapSerialFilePathAndName + " , connecting...");
				
				this.databaseNameToDatasetInformationMap = new HashMap<String, HashMap<String, List<String>>>();
				
				// Check every database for the dataset or the template name
				for (String databaseName : this.databaseNames) {
				
					connect(databaseName);
					
					// Prepare list of tables
					this.preparedStatementDatabase = this.connection.prepareStatement(
							"select meta_conf__dataset__main.dataset, meta_conf__dataset__main.type, meta_template__template__main.template " +
							"from meta_conf__dataset__main, meta_template__template__main " +
							"where meta_conf__dataset__main.dataset_id_key = meta_template__template__main.dataset_id_key;");
					
					ResultSet resultSet = this.preparedStatementDatabase.executeQuery();
					HashMap<String, List<String>> map = new HashMap<String, List<String>>();
					resultSet.beforeFirst();
					while (resultSet.next()) {
						String datasetName = resultSet.getString(1);
						String datasetType = resultSet.getString(2);
						String templateName = resultSet.getString(3);
						
						List<String> list = map.get(templateName);
						if (null==list) {
							list = new ArrayList<String>();
						}
						list.add(datasetName);
						map.put(templateName, list);
						
						MyUtils.checkStatusProgram(TransformationUtils.isGenomicSequence(datasetType) || TransformationUtils.isTableSet(datasetType));
						
						/*MyUtils.checkStatusProgram(datasetNameToDatabaseNameMap.get(datasetName)==null);
						datasetNameToDatabaseNameMap.put(datasetName, databaseName);*/
						
						MyUtils.checkStatusProgram(datasetNameToTemplateName.get(datasetName)==null);
						datasetNameToTemplateName.put(datasetName, templateName);
						
						MyUtils.checkStatusProgram(datasetNameToDatasetType.get(datasetName)==null);
						datasetNameToDatasetType.put(datasetName, datasetType);
						
						String databaseNameTmp = templateNameToDatabaseNameMap.get(templateName);
						if (null==databaseNameTmp) {
							templateNameToDatabaseNameMap.put(templateName, databaseName);							
						} else {
							MyUtils.checkStatusProgram(databaseNameTmp.equals(databaseName));
						}
			        }
	
					this.preparedStatementDatabase.close();
					resultSet.close();
					
					disconnect();
					
					MyUtils.checkStatusProgram(this.databaseNameToDatasetInformationMap.get(databaseName)==null);
					this.databaseNameToDatasetInformationMap.put(databaseName, map);
					
					MyUtils.writeSerializedObject(this.databaseNameToDatasetInformationMap, databaseMapSerialFilePathAndName);
					//MyUtils.writeSerializedObject(this.datasetNameToDatabaseNameMap, datasetMapSerialFilePathAndName);
					MyUtils.writeSerializedObject(this.templateNameToDatabaseNameMap, templateMapSerialFilePathAndName);
					MyUtils.writeSerializedObject(this.datasetNameToTemplateName, datasetMapSerialFilePathAndName);
					MyUtils.writeSerializedObject(this.datasetNameToDatasetType, datasetMap2SerialFilePathAndName);
				}
			}

			System.out.println(databaseNameToDatasetInformationMap);
		} catch (SQLException e) {
			throw new TechnicalException(e);
		}
	}
	
	@Deprecated
	public boolean isTemplate(String name) throws FunctionalException {
		Boolean matchesDatasetName = false;
		Boolean matchesTemplateName = false;
		String databaseNameMatch = null;
		
		for (Iterator<String> it = databaseNameToDatasetInformationMap.keySet().iterator(); it.hasNext();) {
			String databaseName = it.next();
			HashMap<String, List<String>> map = databaseNameToDatasetInformationMap.get(databaseName);
			
			for (Iterator<String> it2 = map.keySet().iterator(); it2.hasNext();) {
				String templateName = it2.next();
				List<String> datasetNames = map.get(templateName);
				
				for (String datasetName : datasetNames) {
					if (name.equals(datasetName)) {
						MyUtils.checkStatusProgram(!matchesDatasetName && !matchesTemplateName && null==databaseNameMatch);
						matchesDatasetName = true;
						databaseNameMatch = databaseName;
					}
					if (name.equals(templateName)) {
						MyUtils.checkStatusProgram(!matchesDatasetName && 
								(!matchesTemplateName || (null!=databaseNameMatch && databaseNameMatch.equals(databaseName))));
						matchesTemplateName = true;
						databaseNameMatch = databaseName;
					}
				}
			}
		}
		
		if (!matchesDatasetName && !matchesTemplateName) {
			throw new FunctionalException("Unknown dataset/template name: " + name);
		} else if (matchesDatasetName && matchesTemplateName) {
			throw new FunctionalException("Ambiguous dataset/template name: " + name);
		}
		return matchesTemplateName;
	}
	
	public void fetchTableList(String templateName) throws TechnicalException {
		
		String databaseName = getDatabaseName(templateName);
		MyUtils.checkStatusProgram(null!=databaseName);
			
		try {
			List<String> tableList = null;
			String tableListSerialFilePathAndName = this.transformationsGeneralSerialFolderPathAndName + TABLE_LIST_SERIAL_FILE_PREFIX + databaseName;
			if (new File(tableListSerialFilePathAndName).exists()) {
				tableList = (List<String>)MyUtils.readSerializedObject(
						tableListSerialFilePathAndName);
				System.out.println("Using serial, quick check: " + tableList.size());
			} else {
				System.out.println("No serial at " + tableListSerialFilePathAndName + " , connecting...");
				connect(databaseName);
				
				tableList = new ArrayList<String>();
				
				// Prepare list of tables
				this.preparedStatementTable = this.connection.prepareStatement("show tables");
				
				ResultSet resultSet = this.preparedStatementTable.executeQuery();
		    	
				tableList = new ArrayList<String>();
				resultSet.beforeFirst();
				while (resultSet.next()) {
					tableList.add(resultSet.getString(1));
		        }

				this.preparedStatementTable.close();
				resultSet.close();
				
				disconnect();
				
				MyUtils.writeSerializedObject(tableList, tableListSerialFilePathAndName);
			}
			
			MyUtils.checkStatusProgram(templateNameToDatabaseDescriptionMap.get(templateName)==null);
			templateNameToDatabaseDescriptionMap.put(templateName, new TemplateDatabaseDescription(tableList));
			
		} catch (SQLException e) {
			throw new TechnicalException(e);
		}
	}
	
	public void fetchTableColumnMap(String templateName) throws TechnicalException {
		
		String databaseName = getDatabaseName(templateName);
		MyUtils.checkStatusProgram(null!=databaseName);
		
		try {
			
			Map<String, List<String>> tableColumnMap = null;
			TemplateDatabaseDescription templateDatabaseDescription = templateNameToDatabaseDescriptionMap.get(templateName);
			MyUtils.checkStatusProgram(templateDatabaseDescription!=null);
			
			String tableColumnMapContentSerialFilePathAndName = this.transformationsGeneralSerialFolderPathAndName +
					TABLE_COLUMN_LIST_SERIAL_FILE_PREFIX + databaseName;
			if (new File(tableColumnMapContentSerialFilePathAndName).exists()) {
				tableColumnMap = (Map<String, List<String>>)MyUtils.readSerializedObject(
						tableColumnMapContentSerialFilePathAndName);
				System.out.println("Using serial, quick check: " + tableColumnMap.size());
			} else {
				
				System.out.println("No serial at " + tableColumnMapContentSerialFilePathAndName + " , connecting...");
				connect(databaseName);
				
				tableColumnMap = new HashMap<String, List<String>>();
				List<String> tableList = templateDatabaseDescription.getTableList();
				int table = 0;
				for (String tableName : tableList) {
					
					System.out.print("tableName = " + tableName + ", " + (table+1) + "/" + tableList.size());
					
					// Prepare list of descriptions
					this.preparedStatementField = this.connection.prepareStatement("desc " + tableName);
					
					ResultSet resultSet = this.preparedStatementField.executeQuery();
			    	
					List<String> columnList = new ArrayList<String>();
					resultSet.beforeFirst();
					while (resultSet.next()) {
						columnList.add(resultSet.getString(1));
			        }
					
					tableColumnMap.put(tableName, columnList);

					this.preparedStatementField.close();
					resultSet.close();
					
					System.out.println(", " + columnList.size());
					
					table++;
				}
				
				disconnect();
				
				MyUtils.writeSerializedObject(tableColumnMap, tableColumnMapContentSerialFilePathAndName);
			}
			
			templateDatabaseDescription.setTableColumnMap(tableColumnMap);
			
		} catch (SQLException e) {
			throw new TechnicalException(e);
		}
	}
	
	public TemplateDatabaseDescription getTemplateDatabaseDescription(String templateName) {
		TemplateDatabaseDescription templateDatabaseDescription = this.templateNameToDatabaseDescriptionMap.get(templateName);
		MyUtils.checkStatusProgram(null!=templateDatabaseDescription);
		return templateDatabaseDescription;
	}
	
	/*public Document fetchDatasetXml(String datasetName) throws TechnicalException {
		
		// Find what database the dataset/template belongs to
		String databaseName = this.datasetNameToDatabaseNameMap.get(datasetName);
		MyUtils.checkStatusProgram(null!=databaseName);
		
		Document document = null;
		try {
			String datasetXmlSerialFilePathAndName = this.serialFolderPathAndName + DATASET_XML_CONTENT_SERIAL_FILE_PREFIX + datasetName;
			if (new File(datasetXmlSerialFilePathAndName).exists()) {
				document = (Document)MyUtils.readSerializedObject(
						datasetXmlSerialFilePathAndName);
				System.out.println("Using serial, quick check: " + document.getDocType());
			} else {
				System.out.println("No serial at " + datasetXmlSerialFilePathAndName + " , connecting...");
				connect(databaseName);
				
				// Prepare list of tables
				this.preparedStatementDatasetXml = this.connection.prepareStatement(
						"select meta_conf__xml__dm.xml " +
						"from meta_conf__dataset__main, meta_conf__xml__dm " +
						"where meta_conf__dataset__main.dataset_id_key = meta_conf__xml__dm.dataset_id_key;");
				
				ResultSet resultSet = this.preparedStatementDatasetXml.executeQuery();
		    	
				resultSet.first();
				byte[] bytes = resultSet.getBytes(1);
				MyUtils.checkStatusProgram(!resultSet.next());	// Check only 1 row

				this.preparedStatementDatasetXml.close();
				resultSet.close();
				
				disconnect();
				
				// Transform into JDOM document
				InputStream rstream = null;
				rstream = new ByteArrayInputStream(bytes);
				SAXBuilder builder = new SAXBuilder();
				InputSource is = new InputSource(rstream);
				document = builder.build(is);
				
				MyUtils.writeSerializedObject(document, datasetXmlSerialFilePathAndName);
			}
		} catch (SQLException e) {
			throw new TechnicalException(e);
		} catch (IOException e) {
			throw new TechnicalException(e);
		} catch (JDOMException e) {
			throw new TechnicalException(e);
		}
		return document;
	}*/

	public Document fetchTemplateXml(String virtualSchema, String templateName) throws TechnicalException {		
		// Find what database the dataset/template belongs to
		String databaseName = getDatabaseName(templateName);
		MyUtils.checkStatusProgram(null!=databaseName, templateName);
		
		return fetchTemplateXml(virtualSchema, templateName, databaseName);
	}

	public String getDatabaseName(String templateName) {
		return this.templateNameToDatabaseNameMap.get(templateName);
	}
	
	public Document fetchTemplateXml(String virtualSchema, String templateName, String databaseName) throws TechnicalException {
		
		Document document = null;
		try {
			String templateXmlSerialFilePathAndName = this.transformationsGeneralSerialFolderPathAndName + TEMPLATE_XML_CONTENT_SERIAL_FILE_PREFIX + 
			virtualSchema + MyUtils.INFO_SEPARATOR + MyUtils.INFO_SEPARATOR + templateName;
			String templateXmlFilePathAndName = this.transformationsGeneralSerialFolderPathAndName + 
			virtualSchema + MyUtils.INFO_SEPARATOR + MyUtils.INFO_SEPARATOR + templateName;
			if (new File(templateXmlSerialFilePathAndName).exists()) {
				System.out.println("Using serial at " + templateXmlSerialFilePathAndName);
				document = (Document)MyUtils.readSerializedObject(
						templateXmlSerialFilePathAndName);
				System.out.println("quick check: " + document.getDocType());
			} else {
				System.out.println("No serial at " + templateXmlSerialFilePathAndName + " , connecting...");
				connect(databaseName);
				
				this.preparedStatementTemplateXml = this.connection.prepareStatement(
						"select meta_template__xml__dm.compressed_xml " +
						"from meta_template__xml__dm " +
						"where meta_template__xml__dm.template = '" + templateName + "';");
				
				ResultSet resultSet = this.preparedStatementTemplateXml.executeQuery();
		    	
				resultSet.first();
				byte[] bytes = resultSet.getBytes(1);
				MyUtils.checkStatusProgram(!resultSet.next());	// Check only 1 row

				this.preparedStatementTemplateXml.close();
				resultSet.close();
				
				disconnect();
				
				/* Transform into uncompressed XML document:
					Compression of XML in MartEditor, see:
						DatabaseDatasetConfigUtils.storeTemplateXML (compress)
						MartRegistryXMLUtils.DataSourceToRegistryDocument (uncompress)
				*/
				InputStream rstream = null;
				rstream = new GZIPInputStream(new ByteArrayInputStream(bytes));
				SAXBuilder builder = new SAXBuilder();
				InputSource is = new InputSource(rstream);
				document = builder.build(is);	
				
				MyUtils.writeSerializedObject(document, templateXmlSerialFilePathAndName);			
				MyUtils.writeXmlFile(document, templateXmlFilePathAndName);
			}
		} catch (SQLException e) {
			throw new TechnicalException(e);
		} catch (JDOMException e) {
			throw new TechnicalException(e);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		return document;
	}
	
	/*public void outputDatabaseDescription(String databaseDescriptionOutputFilePathAndName) throws TechnicalException {
		
		File file = new File(databaseDescriptionOutputFilePathAndName);
		try {
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			
			bufferedWriter.write(this.tableList.size() + MyUtils.LINE_SEPARATOR);
			for (String tableName : this.tableList) {
				bufferedWriter.write(tableName + MyUtils.LINE_SEPARATOR);
			}
			bufferedWriter.write(MyUtils.LINE_SEPARATOR);
			
			for (Iterator<String> it = this.tableColumnMap.keySet().iterator(); it.hasNext();) {
				String tableName = it.next();
				List<String> columnList = this.tableColumnMap.get(tableName);
				bufferedWriter.write(tableName + " (" + columnList.size() + ")" + MyUtils.LINE_SEPARATOR);
				for (String columnName : columnList) {
					bufferedWriter.write(MyUtils.TAB_SEPARATOR + columnName + MyUtils.LINE_SEPARATOR);
				}
			}
			bufferedWriter.write(MyUtils.LINE_SEPARATOR);
			
			bufferedWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
	}*/
	
	public void disconnect() throws TechnicalException {
		try {
			this.connection.close();
		} catch (SQLException e) {
			throw new TechnicalException(e);
		}
	}
	
	public boolean checkDatabase(String templateName, String tableName, String columnName) throws FunctionalException {
		return checkDatabaseForTable(templateName, tableName) && 
		checkDatabaseForField(templateName, tableName, columnName);
	}
	
	private boolean checkDatabaseForTable(String templateName, String tableName) {
		TemplateDatabaseDescription templateDatabaseDescription = templateNameToDatabaseDescriptionMap.get(templateName);
		MyUtils.checkStatusProgram(templateDatabaseDescription!=null);
		List<String> tableList = templateDatabaseDescription.getTableList();
		return MartConfiguratorUtils.containsIgnoreCase(tableList, tableName);
	}

	private boolean checkDatabaseForField(String templateName, String tableName, String columnName) throws FunctionalException {
		TemplateDatabaseDescription templateDatabaseDescription = templateNameToDatabaseDescriptionMap.get(templateName);
		MyUtils.checkStatusProgram(templateDatabaseDescription!=null);
		Map<String, List<String>> tableColumnMap = templateDatabaseDescription.getTableColumnMap();
		List<String> columnList = tableColumnMap.get(tableName);
		if (null==columnList) {
			throw new FunctionalException("No table called " + tableName + " found in map");
		}
		return MartConfiguratorUtils.containsIgnoreCase(columnList, columnName);
	}

	public HashMap<String, String> getDatasetNameToTemplateName() {
		return datasetNameToTemplateName;
	}

	public HashMap<String, String> getDatasetNameToDatasetType() {
		return datasetNameToDatasetType;
	}
	public DatabaseParameter getDatabaseParameter() {
		return databaseParameter;
	}
}
