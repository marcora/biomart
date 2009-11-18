package org.biomart.martRemote;


import java.io.File;

import org.biomart.common.general.constants.MyConstants;
import org.biomart.common.general.utils.MyUtils;

public class MartRemoteConstants {

	public static final String MART_SERVICE_NAMESPACE = "MartServiceNameSpace";
	
	public static final String XSD_FILE_NAME = "martservice.xsd";
	public static final String XML_FILES_FOLDER_NAME = "conf/xml";
	public static final String ADDITIONAL_FILES_FOLDER_NAME = "conf/files";
	
	public static final Boolean WEB_PORTAL = !new File(".").getAbsolutePath().contains("anthony");
	
	public static final String PORTAL_SERIAL_FILE_NAME = WEB_PORTAL ? "portal.serial" : "rdbms_portal.serial"; 	// "web_portal.serial"
	public static final String QUERY_TEST_PROPERTIES_FILE_NAME = "queries.properties";
	
	public static final String APPLICATION_ROOT = new File("").getAbsolutePath() + MyUtils.FILE_SEPARATOR;

	public static final String XSD_FILE_FILE_PATH_AND_NAME = 
		MyConstants.FILE_SYSTEM_PROTOCOL + APPLICATION_ROOT + XML_FILES_FOLDER_NAME + MyUtils.FILE_SEPARATOR + XSD_FILE_NAME;

	public static final String PORTAL_SERIAL_FILE_PATH_AND_NAME = 
		MyConstants.FILE_SYSTEM_PROTOCOL + APPLICATION_ROOT + ADDITIONAL_FILES_FOLDER_NAME + MyUtils.FILE_SEPARATOR + PORTAL_SERIAL_FILE_NAME;
	
	public static final String BIOMART_JAVA_SERIALIZED_PORTAL_FILE = 
		new File("conf/files/portal.serial").getAbsolutePath();
	
	public static final String QUERY_TEST_PROPERTIES_FILE_PATH_AND_NAME = 
		APPLICATION_ROOT + ADDITIONAL_FILES_FOLDER_NAME + MyUtils.FILE_SEPARATOR + QUERY_TEST_PROPERTIES_FILE_NAME;
	
	public static final int ELEMENT_NAME_POSITION = 0;
	public static final int ELEMENT_DISPLAY_NAME_POSITION = 1;
	
	// Functions
	public static final String REQUEST_SUFFIX = "Request";
	public static final String RESPONSE_SUFFIX = "Response";

	public static final String GET_REGISTRY_IDENTIFIER = "getRegistry";
	public static final String GET_DATASETS_IDENTIFIER = "getDatasets";
	public static final String GET_ROOT_CONTAINER_IDENTIFIER = "getRootContainer";
	public static final String GET_ATTRIBUTES_IDENTIFIER = "getAttributes";
	public static final String GET_FILTERS_IDENTIFIER = "getFilters";
	public static final String GET_LINKS_IDENTIFIER = "getLinks";
	public static final String QUERY_IDENTIFIER = "query";
}
