package org.biomart.martRemote.martService;

import java.io.File;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.martRemote.MartApi;
import org.biomart.martRemote.MartRemoteConstants;
import org.biomart.martRemote.enums.MartServiceFormat;
import org.biomart.martRemote.objects.request.MartServiceRequest;


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
		
	public static MartApi martServiceApi = null;
	public static boolean loadedProperly = false;
		
	public String getRegistry(String username, String password, String format) throws FunctionalException, TechnicalException {
		MartServiceHelper.initialize();
		MartServiceRequest martServiceRequest = MartService.martServiceApi.prepareGetRegistry(
				username, password, MartServiceFormat.getFormat(format));
		return martServiceRequest.isValid() ?
				MartServiceHelper.executeRequest(martServiceRequest) : "invalid request";
	}

	public String getDatasets(String username, String password, String format, String mart, Integer version) throws FunctionalException, TechnicalException {
		MartServiceHelper.initialize();
		MartServiceRequest martServiceRequest = MartService.martServiceApi.prepareGetDatasets(
				username, password, MartServiceFormat.getFormat(format), mart, version);
		return martServiceRequest.isValid() ?
				MartServiceHelper.executeRequest(martServiceRequest) : "invalid request";
	}
	
	public String query(String username, String password, String format, String query) throws FunctionalException, TechnicalException {
		MartServiceHelper.initialize();
		MartServiceRequest martServiceRequest = null;
		try {
			martServiceRequest = MartService.martServiceApi.prepareQuery(
					username, password, MartServiceFormat.getFormat(format), query);
		} catch (TechnicalException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (FunctionalException e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return martServiceRequest.isValid() ?
			MartServiceHelper.executeRequest(martServiceRequest) : "invalid request";
	}
	
	public String getRootContainer(String username, String password, String format, String dataset, String partitionFilter) throws FunctionalException, TechnicalException {
		MartServiceHelper.initialize();
		MartServiceRequest martServiceRequest = MartService.martServiceApi.prepareGetRootContainer(
				username, password, MartServiceFormat.getFormat(format), dataset, partitionFilter);
		return martServiceRequest.isValid() ?
				MartServiceHelper.executeRequest(martServiceRequest) : "invalid request";
	}
	
	

	public String testXsd() {
		return "" + MartService.martServiceApi.getXsd();
	}
	
	public String testPortal() {
		return "" + MartService.martServiceApi.getMartRegistry();
	}
	
	public String testServer() {
		return "" + MartService.loadedProperly;
	}
	
	public String debug(String param) {
		return "param = " + param;
	}
	
	public String test(String arg1, String arg2) {
		return "ok2: " + arg1 + " " + arg2;
	}
}
