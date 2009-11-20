package org.biomart.martRemote.martService;

import java.io.StringWriter;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.martRemote.MartApi;
import org.biomart.martRemote.MartRemoteConstants;
import org.biomart.martRemote.objects.response.MartRemoteResponse;

public class MartServiceHelper {
	
	public static MartApi martApi = null;
	public static boolean loadedProperly = false;
	
	public static String initialize() {
		if (null==MartServiceHelper.martApi) {	// should happen only once
			try {
				MartServiceHelper.martApi = new MartApi(false, false,
	MartServiceConstants.XSD_FILE_PATH, MartServiceConstants.XSD_FILE_URL, 
						MartRemoteConstants.BIOMART_JAVA_SERIALIZED_PORTAL_FILE);
				MartServiceHelper.loadedProperly = MartServiceHelper.martApi!=null;
			} catch (TechnicalException e) {
				e.printStackTrace();
				return "TechnicalException is: " + e.getMessage();
			} catch (FunctionalException e) {
				e.printStackTrace();
				return "FunctionalException is: " + e.getMessage();
			}
		}
		return null;
	}

	public static String getRegistry(String username, String password, String format)
			throws FunctionalException {
		initialize();
		MartRemoteResponse martRemoteResponse = martApi.getRegistryResponse(username, password, format);
		return getResultAsString(martRemoteResponse);
	}

	public static String getDatasets(String username, String password,
			String format, String mart, Integer version) throws FunctionalException {
		initialize();
		MartRemoteResponse martRemoteResponse = martApi.getDatasetsResponse(username, password, format, mart, version);
		return getResultAsString(martRemoteResponse);
	}
	
	public static String getRootContainer(String username, String password, String format, String dataset, String partitionFilter) throws FunctionalException, TechnicalException {
		MartServiceHelper.initialize();
		MartRemoteResponse martRemoteResponse = martApi.getRootContainerResponse(username, password, format, dataset, partitionFilter);
		return getResultAsString(martRemoteResponse);
	}
	public static String getAttributes(String username, String password, String format, String dataset, String partitionFilter) throws FunctionalException, TechnicalException {
		MartServiceHelper.initialize();
		MartRemoteResponse martRemoteResponse = martApi.getAttributesResponse(username, password, format, dataset, partitionFilter);
		return getResultAsString(martRemoteResponse);
	}
	public static String getFilters(String username, String password, String format, String dataset, String partitionFilter) throws FunctionalException, TechnicalException {
		MartServiceHelper.initialize();
		MartRemoteResponse martRemoteResponse = martApi.getFiltersResponse(username, password, format, dataset, partitionFilter);
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
			return MartServiceConstants.INVALID_REQUEST_ERROR_MESSAGE;
		}
	}
}
