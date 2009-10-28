package org.biomart.martRemote.martService;

import java.io.File;
import java.io.StringWriter;

import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.martRemote.MartApi;
import org.biomart.martRemote.MartRemoteConstants;
import org.biomart.martRemote.enums.MartServiceFormat;
import org.biomart.martRemote.objects.request.MartServiceRequest;
import org.biomart.martRemote.objects.response.MartServiceResponse;


/**
 * 
 * POJO class for Axis to generate the web service
 *
 */
public class MartService {

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
		//SERVER_PATH + MartRemoteConstants.XML_FILES_FOLDER_NAME + MyUtils.FILE_SEPARATOR + MartRemoteConstants.XSD_FILE_NAME;
		//MyConstants.FILE_SYSTEM_PROTOCOL + "/var/lib/tomcat5.5/webapps/MartService" + MyUtils.FILE_SEPARATOR + MartRemoteConstants.XML_FILES_FOLDER_NAME + MyUtils.FILE_SEPARATOR + MartRemoteConstants.XSD_FILE_NAME;
	
	public static final String PORTAL_SERIAL_FILE_PATH = APPLICATION_PATH + 
		PATH_TO_MART_SERVICE_FILES + MyUtils.FILE_SEPARATOR + MartRemoteConstants.PORTAL_SERIAL_FILE_NAME;
	public static final String PORTAL_SERIAL_FILE_URL = SERVER_PATH + 
		PATH_TO_MART_SERVICE_FILES + MyUtils.FILE_SEPARATOR + MartRemoteConstants.PORTAL_SERIAL_FILE_NAME; 
		//SERVER_PATH + MartRemoteConstants.ADDITIONAL_FILES_FOLDER_NAME + MyUtils.FILE_SEPARATOR + MartRemoteConstants.PORTAL_SERIAL_FILE_NAME;
		//MyConstants.FILE_SYSTEM_PROTOCOL + "/var/lib/tomcat5.5/webapps/MartService" + MyUtils.FILE_SEPARATOR + MartRemoteConstants.ADDITIONAL_FILES_FOLDER_NAME + MyUtils.FILE_SEPARATOR + MartRemoteConstants.PORTAL_SERIAL_FILE_NAME;
		
	private static MartApi martServiceApi = null;
	
	public static String initialize() {
		try {
			MartService.martServiceApi = new MartApi(true, XSD_FILE_PATH, XSD_FILE_URL, PORTAL_SERIAL_FILE_PATH, PORTAL_SERIAL_FILE_URL);
		} catch (TechnicalException e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return null;
	}
	
	public String test(String arg1, String arg2) {
		return "ok: " + arg1 + " " + arg2;
	}
	
	public String getRegistry(String username, String password, String format) {
		String result = null;
		
		if (null==MartService.martServiceApi) {	// should happen only once
			String initializeError = initialize();
			if (null!=initializeError) {
				return "initializeError = " + initializeError;
			}
		}
		
		MartServiceFormat martServiceFormat = MartServiceFormat.getFormat(format);
		MartServiceRequest martServiceRequest = MartService.martServiceApi.prepareGetRegistry(username, password, martServiceFormat);
		MartServiceResponse martServiceResponse = MartService.martServiceApi.executeGetRegistry(martServiceRequest);
		try {
			StringWriter sw = new StringWriter();
			MartService.martServiceApi.processMartServiceResult(martServiceResponse, sw);
			result = sw.toString();
		} catch (TechnicalException e) {
			result = e.getMessage();
			e.printStackTrace();
		}
		
		return result;
	}
}
