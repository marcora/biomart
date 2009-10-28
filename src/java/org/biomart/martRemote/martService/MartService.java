package org.biomart.martRemote.martService;

import org.biomart.common.general.constants.MyConstants;
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

	public static final String SERVER_PATH = "http://localhost:8082/MartService/";
	public static final String XSD_FILE_URL = 
		//SERVER_PATH + MartRemoteConstants.XML_FILES_FOLDER_NAME + MyUtils.FILE_SEPARATOR + MartRemoteConstants.XSD_FILE_NAME;
		MyConstants.FILE_SYSTEM_PROTOCOL + "/var/lib/tomcat5.5/webapps/MartService" + MyUtils.FILE_SEPARATOR + MartRemoteConstants.XML_FILES_FOLDER_NAME + MyUtils.FILE_SEPARATOR + MartRemoteConstants.XSD_FILE_NAME;
	
	public static final String PORTAL_SERIAL_FILE_URL = 
		//SERVER_PATH + MartRemoteConstants.ADDITIONAL_FILES_FOLDER_NAME + MyUtils.FILE_SEPARATOR + MartRemoteConstants.PORTAL_SERIAL_FILE_NAME;
		MyConstants.FILE_SYSTEM_PROTOCOL + "/var/lib/tomcat5.5/webapps/MartService" + MyUtils.FILE_SEPARATOR + MartRemoteConstants.ADDITIONAL_FILES_FOLDER_NAME + MyUtils.FILE_SEPARATOR + MartRemoteConstants.PORTAL_SERIAL_FILE_NAME;

	private static MartApi martServiceApi = null;
	static {
		initialize();
	}
	
	public static void initialize() {
		try {
			MartService.martServiceApi = new MartApi(true, XSD_FILE_URL, PORTAL_SERIAL_FILE_URL);
		} catch (TechnicalException e) {
			e.printStackTrace();
		}
	}
	
	public String getRegistry(String username, String password, MartServiceFormat format) {
		String result = null;
		
		if (null==MartService.martServiceApi) {	// should'nt happen though
			initialize();
		}
		
		MartServiceRequest martServiceRequest = MartService.martServiceApi.prepareGetRegistry(username, password, format);
		MartServiceResponse martServiceResponse = MartService.martServiceApi.executeGetRegistry(martServiceRequest);
		try {
			result = MartService.martServiceApi.processMartServiceResult(martServiceResponse, null);
		} catch (TechnicalException e) {
			result = e.getMessage();
			e.printStackTrace();
		}
		return result;
	}
}
