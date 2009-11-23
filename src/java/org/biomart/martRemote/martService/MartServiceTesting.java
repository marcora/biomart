package org.biomart.martRemote.martService;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.martRemote.MartApi;
import org.biomart.martRemote.MartServiceHelper;
import org.biomart.martRemote.testing.MartApiDevelopment;

public class MartServiceTesting {

	
	public static void main(String[] args) {
		try {
			MartService martService = new MartService();
			MartServiceHelper.martApi = new MartApi();
			System.out.println(martService.getRegistry(MartApiDevelopment.username, MartApiDevelopment.password, MartApiDevelopment.format.getValue()));
		} catch (FunctionalException e) {
			e.printStackTrace();
		} catch (TechnicalException e) {
			e.printStackTrace();
		}
	}
}
