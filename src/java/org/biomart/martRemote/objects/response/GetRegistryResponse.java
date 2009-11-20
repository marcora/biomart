package org.biomart.martRemote.objects.response;

import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.lite.LiteMart;
import org.biomart.objects.lite.LiteMartRegistry;
import org.biomart.objects.objects.Location;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartRegistry;

public class GetRegistryResponse extends MartRemoteResponse {

	private LiteMartRegistry liteMartRegistry = null;

	public GetRegistryResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
		this.liteMartRegistry = new LiteMartRegistry(super.martRemoteRequest);
	}
	
	public LiteMartRegistry getLiteMartRegistry() {
		return liteMartRegistry;
	}

	public void populateObjects() throws FunctionalException {
		List<Location> locationListTmp = super.martRegistry.getLocationList();
		for (Location location : locationListTmp) {
			if (super.martRemoteRequest.getUsername().equals(location.getUser())) {
				List<Mart> martListTmp = location.getMartList();
				for (Mart mart : martListTmp) {
					LiteMart liteMart = new LiteMart(location, mart);
					this.liteMartRegistry.addLiteMart(liteMart);
				}
			}
		}
		this.liteMartRegistry.lock();
	}

	@Override
	protected Jsoml createOutputResponse(boolean xml, Jsoml root)
			throws FunctionalException {
		// TODO Auto-generated method stub
		return null;
	}
}
