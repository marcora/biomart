package org.biomart.martRemote.martService;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.martRemote.enums.MartRemoteEnum;


/**
 * 
 * POJO class for Axis to generate the web service
 *
 */
public class MartService {
		
	public String getRegistry(String username, String password, String format) throws FunctionalException, TechnicalException {
		return MartServiceHelper.getRegistry(username, password, format);
	}

	public String getDatasets(String username, String password, String format, String mart, Integer version) throws FunctionalException, TechnicalException {
		return MartServiceHelper.getDatasets(username, password, format, mart, version);
	}

	public String query(String username, String password, String format, String query) throws FunctionalException, TechnicalException {
		return MartServiceHelper.query(username, password, format, query);
	}

	public String getRootContainer(String username, String password, String format, String dataset, String partitionFilter) throws FunctionalException, TechnicalException {
		return MartServiceHelper.getContainees(MartRemoteEnum.GET_ROOT_CONTAINER, username, password, format, dataset, partitionFilter);
	}
	
	public String getAttributes(String username, String password, String format, String dataset, String partitionFilter) throws FunctionalException, TechnicalException {
		return MartServiceHelper.getContainees(MartRemoteEnum.GET_ATTRIBUTES, username, password, format, dataset, partitionFilter);
	}
	
	public String getFilters(String username, String password, String format, String dataset, String partitionFilter) throws FunctionalException, TechnicalException {
		return MartServiceHelper.getContainees(MartRemoteEnum.GET_FILTERS, username, password, format, dataset, partitionFilter);
	}

	public String testXsd() {
		return "" + MartServiceHelper.martApi.getXsd();
	}
	
	public String testPortal() {
		return "" + MartServiceHelper.martApi.getMartRegistry();
	}
	
	public String testServer() {
		return "" + MartServiceHelper.loadedProperly;
	}
	
	public String debug(String param) {
		return "param = " + param;
	}
	
	public String test(String arg1, String arg2) {
		return "ok2: " + arg1 + " " + arg2;
	}
}
