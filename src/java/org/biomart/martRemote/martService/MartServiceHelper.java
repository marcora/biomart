package org.biomart.martRemote.martService;

import java.io.File;
import java.io.StringWriter;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.martRemote.MartApi;
import org.biomart.martRemote.MartRemoteConstants;
import org.biomart.martRemote.enums.MartRemoteEnum;
import org.biomart.martRemote.enums.MartServiceFormat;
import org.biomart.martRemote.objects.request.GetAttributesRequest;
import org.biomart.martRemote.objects.request.GetDatasetsRequest;
import org.biomart.martRemote.objects.request.GetFiltersRequest;
import org.biomart.martRemote.objects.request.GetRegistryRequest;
import org.biomart.martRemote.objects.request.GetRootContainerRequest;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.martRemote.objects.request.QueryRequest;
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
		
	public static MartApi martApi = null;
	public static boolean loadedProperly = false;
	
	public static String initialize() {
		if (null==martApi) {	// should happen only once
			try {
				martApi = new MartApi(true, 
						XSD_FILE_PATH, XSD_FILE_URL, 
						PORTAL_SERIAL_FILE_PATH, PORTAL_SERIAL_FILE_URL);
				loadedProperly = true;
			} catch (TechnicalException e) {
				e.printStackTrace();
				return e.getMessage();
			}
		}
		return null;
	}

	public static String getRegistry(String username, String password, String format)
			throws FunctionalException {
		initialize();
		MartRemoteRequest martServiceRequest = martApi.prepareGetRegistry(
				username, password, MartServiceFormat.getFormat(format));
		return martServiceRequest.isValid() ?
				executeRequest(martServiceRequest) : "invalid request";
	}

	public static String getDatasets(String username, String password,
			String format, String mart, Integer version)
			throws FunctionalException {
		initialize();
		MartRemoteRequest martServiceRequest = martApi.prepareGetDatasets(
				username, password, MartServiceFormat.getFormat(format), mart, version);
		return martServiceRequest.isValid() ?
				executeRequest(martServiceRequest) : "invalid request";
	}
	
	public static String getContainees(MartRemoteEnum type, String username, String password, String format, String dataset, String partitionFilter) throws FunctionalException, TechnicalException {
		MartServiceHelper.initialize();
		MartRemoteRequest martServiceRequest = null;
		if (MartRemoteEnum.GET_ROOT_CONTAINER.equals(type)) {
			martServiceRequest = martApi.prepareGetRootContainer(
					username, password, MartServiceFormat.getFormat(format), dataset, partitionFilter);
		} else if (MartRemoteEnum.GET_ATTRIBUTES.equals(type)) {
			martServiceRequest = martApi.prepareGetAttributes(
					username, password, MartServiceFormat.getFormat(format), dataset, partitionFilter);
		}if (MartRemoteEnum.GET_FILTERS.equals(type)) {
			martServiceRequest = martApi.prepareGetFilters(
					username, password, MartServiceFormat.getFormat(format), dataset, partitionFilter);
		}
		return martServiceRequest.isValid() ?
				MartServiceHelper.executeRequest(martServiceRequest) : "invalid request";
	}

	public static String query(String username, String password, String format,
			String query) throws FunctionalException {
		initialize();
		MartRemoteRequest martServiceRequest = null;
		try {
			martServiceRequest = martApi.prepareQuery(
					username, password, MartServiceFormat.getFormat(format), query);
		} catch (TechnicalException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (FunctionalException e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return martServiceRequest.isValid() ?
			executeRequest(martServiceRequest) : "invalid request";
	}
	
	public static String executeRequest(MartRemoteRequest martServiceRequest) throws FunctionalException {

		MartRemoteResponse martServiceResponse = null;
		try {
			if (martServiceRequest instanceof GetRegistryRequest) {
				martServiceResponse = martApi.executeGetRegistry(martServiceRequest);
			} else if (martServiceRequest instanceof GetDatasetsRequest) {
				martServiceResponse = martApi.executeGetDatasets(martServiceRequest);		
			} else if (martServiceRequest instanceof GetRootContainerRequest) {
				martServiceResponse = martApi.executeGetRootContainer(martServiceRequest);
			} else if (martServiceRequest instanceof GetAttributesRequest) {
				martServiceResponse = martApi.executeGetAttributes(martServiceRequest);
			} else if (martServiceRequest instanceof GetFiltersRequest) {
				martServiceResponse = martApi.executeGetFilters(martServiceRequest);
			} else if (martServiceRequest instanceof QueryRequest) {
				martServiceResponse = martApi.executeQuery(martServiceRequest);		
			} else {
				return "unknown request: " + martServiceRequest.getClass();
			}
		} catch (TechnicalException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (FunctionalException e) {
			e.printStackTrace();
			return e.getMessage();
		}
		
		try {
			StringWriter sw = new StringWriter();
			return martApi.processMartServiceResult(martServiceResponse, sw);
		} catch (TechnicalException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
}
