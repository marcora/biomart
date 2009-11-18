package org.biomart.martRemote.martService;

import java.io.File;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.martRemote.MartRemoteConstants;

public class MartServiceConstants {
	
	public static final boolean LOCAL = new File(".").getAbsolutePath().contains("tomcat6");
	public static final String APPLICATION_PATH = LOCAL ?
			"/var/lib/tomcat6/webapps" : "/var/lib/tomcat5.5/webapps" ;
	public static final String SERVER_PATH = LOCAL ?
			"http://localhost:8082" : "http://bm-test.res.oicr.on.ca:9180";
	public static final String PATH_TO_MART_SERVICE_FILES = "/axis2/axis2-web/MartService";
	
	public static final String XSD_FILE_PATH = APPLICATION_PATH + 
		PATH_TO_MART_SERVICE_FILES + MyUtils.FILE_SEPARATOR + MartRemoteConstants.XSD_FILE_NAME;
	public static final String XSD_FILE_URL = SERVER_PATH + 
		PATH_TO_MART_SERVICE_FILES + MyUtils.FILE_SEPARATOR + MartRemoteConstants.XSD_FILE_NAME;
	
	public static final String PORTAL_SERIAL_FILE_PATH = APPLICATION_PATH + 
		PATH_TO_MART_SERVICE_FILES + MyUtils.FILE_SEPARATOR + MartRemoteConstants.PORTAL_SERIAL_FILE_NAME;
	public static final String PORTAL_SERIAL_FILE_URL = SERVER_PATH + 
		PATH_TO_MART_SERVICE_FILES + MyUtils.FILE_SEPARATOR + MartRemoteConstants.PORTAL_SERIAL_FILE_NAME;
		
	public static final String INVALID_REQUEST_ERROR_MESSAGE = "invalid request";
}
