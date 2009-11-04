package org.biomart.martRemote.martService;

import java.io.StringWriter;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.martRemote.MartApi;
import org.biomart.martRemote.objects.request.GetDatasetsRequest;
import org.biomart.martRemote.objects.request.GetRegistryRequest;
import org.biomart.martRemote.objects.request.GetRootContainerRequest;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.martRemote.objects.request.QueryRequest;
import org.biomart.martRemote.objects.response.MartRemoteResponse;

public class MartServiceHelper {
	
	public static String initialize() {
		if (null==MartService.martServiceApi) {	// should happen only once
			try {
				MartService.martServiceApi = new MartApi(true, 
						MartService.XSD_FILE_PATH, MartService.XSD_FILE_URL, 
						MartService.PORTAL_SERIAL_FILE_PATH, MartService.PORTAL_SERIAL_FILE_URL);
				MartService.loadedProperly = true;
			} catch (TechnicalException e) {
				e.printStackTrace();
				return e.getMessage();
			}
		}
		return null;
	}
	
	public static String executeRequest(MartRemoteRequest martServiceRequest) throws FunctionalException {

		MartRemoteResponse martServiceResponse = null;
		try {
			if (martServiceRequest instanceof GetRegistryRequest) {
				martServiceResponse = MartService.martServiceApi.executeGetRegistry(martServiceRequest);
			} else if (martServiceRequest instanceof GetDatasetsRequest) {
				martServiceResponse = MartService.martServiceApi.executeGetDatasets(martServiceRequest);		
			} else if (martServiceRequest instanceof GetRootContainerRequest) {
				martServiceResponse = MartService.martServiceApi.executeGetRootContainer(martServiceRequest);
			} else if (martServiceRequest instanceof QueryRequest) {
				martServiceResponse = MartService.martServiceApi.executeQuery(martServiceRequest);		
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
			return MartService.martServiceApi.processMartServiceResult(martServiceResponse, sw);
		} catch (TechnicalException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
}
