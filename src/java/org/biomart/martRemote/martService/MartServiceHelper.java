package org.biomart.martRemote.martService;

import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.martRemote.MartApi;

public class MartServiceHelper {

	public static String initialize() {
		try {
			MartService.martServiceApi = new MartApi(true, 
					MartService.XSD_FILE_PATH, MartService.XSD_FILE_URL, 
					MartService.PORTAL_SERIAL_FILE_PATH, MartService.PORTAL_SERIAL_FILE_URL);
		} catch (TechnicalException e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return null;
	}
}
