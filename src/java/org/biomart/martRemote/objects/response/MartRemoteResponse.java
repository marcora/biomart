package org.biomart.martRemote.objects.response;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.lite.MartRemoteWrapper;
import org.biomart.objects.objects.MartRegistry;


public abstract class MartRemoteResponse {

	protected MartRemoteRequest martRemoteRequest = null;
	protected MartRegistry martRegistry = null;
	
	protected StringBuffer errorMessage = null;
	
	protected MartRemoteResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		this.martRegistry = martRegistry;
		this.martRemoteRequest = martServiceRequest;
		this.errorMessage = new StringBuffer();
	}
	
	public abstract MartRemoteWrapper getMartRemoteWrapper();

	public StringBuffer getErrorMessage() {
		return errorMessage;
	}
	public boolean isValid() {
		return errorMessage.length()==0;
	}
	
	public MartRemoteRequest getMartServiceRequest() {
		return martRemoteRequest;
	}
	
	protected abstract void populateObjects() throws TechnicalException, FunctionalException;
}