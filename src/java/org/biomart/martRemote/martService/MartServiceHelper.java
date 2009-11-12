package org.biomart.martRemote.martService;

import java.io.File;
import java.io.StringWriter;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.martRemote.MartApi;
import org.biomart.martRemote.MartRemoteConstants;
import org.biomart.martRemote.enums.MartRemoteEnum;
import org.biomart.martRemote.objects.response.MartRemoteResponse;

public class MartServiceHelper {

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
		
	public static final String INVALID_REQUEST_ERROR_MESSAGE = "invalid request";
	
	public static MartApi martApi = null;
	public static boolean loadedProperly = false;
	
	public static void main(String[] args) {
		try {
			MartServiceHelper.martApi = new MartApi();
			System.out.println(getRegistry(MartApi.username, MartApi.password, MartApi.format.getValue()));
		} catch (FunctionalException e) {
			e.printStackTrace();
		} catch (TechnicalException e) {
			e.printStackTrace();
		}
	}
	
	public static String initialize() {
		if (null==martApi) {	// should happen only once
			try {
				martApi = new MartApi(true, 
						XSD_FILE_PATH, XSD_FILE_URL, 
						PORTAL_SERIAL_FILE_PATH, PORTAL_SERIAL_FILE_URL);
				loadedProperly = true;
			} catch (TechnicalException e) {
				e.printStackTrace();
				return "TechnicalException is: " + e.getMessage();
			}
		}
		return null;
	}

	public static String getRegistry(String username, String password, String format)
			throws FunctionalException {
		initialize();
		MartRemoteResponse martRemoteResponse = martApi.getRegistry(username, password, format);
		return getResultAsString(martRemoteResponse);
	}

	public static String getDatasets(String username, String password,
			String format, String mart, Integer version) throws FunctionalException {
		initialize();
		MartRemoteResponse martRemoteResponse = martApi.getDatasets(username, password, format, mart, version);
		return getResultAsString(martRemoteResponse);
	}
	
	public static String getContainees(MartRemoteEnum type, String username, String password, String format, String dataset, String partitionFilter) throws FunctionalException, TechnicalException {
		MartServiceHelper.initialize();
		MartRemoteResponse martRemoteResponse = martApi.getContainees(type, username, password, format, dataset, partitionFilter);
		return getResultAsString(martRemoteResponse);
	}

	public static String query(String username, String password, String format,
			String query) throws FunctionalException {
		initialize();
		MartRemoteResponse martRemoteResponse = martApi.query(username, password, format, query);
		return getResultAsString(martRemoteResponse);
	}

	private static String getResultAsString(MartRemoteResponse martRemoteResponse) throws FunctionalException {
		if (martRemoteResponse!=null && martRemoteResponse.isValid()) {
			try {
				StringWriter sw = new StringWriter();
				martApi.processMartServiceResult(martRemoteResponse, sw);
				return sw.toString();
			} catch (TechnicalException e) {
				e.printStackTrace();
				return e.getMessage();
			}
		} else {
			return INVALID_REQUEST_ERROR_MESSAGE;
		}
	}
}
