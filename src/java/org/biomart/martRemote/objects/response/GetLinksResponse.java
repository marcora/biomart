package org.biomart.martRemote.objects.response;

import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.Location;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartRegistry;

public class GetLinksResponse extends MartRemoteResponse {
	
	private List<Dataset> datasetList = null;
	
	public GetLinksResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
		this.datasetList = new ArrayList<Dataset>();
	}

	public List<Dataset> getDatasetList() {
		return datasetList;
	}
	

	public void populateObjects() {
		this.datasetList = new ArrayList<Dataset>();
		
		//TODO dummy for now
		List<Location> locationList = martRegistry.getLocationList();
		for (Location location : locationList) {
			if (super.martRemoteRequest.getUsername().equals(location.getUser())) {
				List<Mart> martList = location.getMartList();
				for (Mart mart : martList) {
					datasetList.addAll(mart.getDatasetList());
				}
			}
		}
	}

	@Override
	public Jsoml createOutputResponse(boolean xml, Jsoml root) throws FunctionalException {
		//TODOs
		return root;
	}
}
