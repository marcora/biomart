package org.biomart.martRemote.objects.response;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.Location;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartRegistry;
import org.jdom.Document;

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
	
	protected Document createXmlResponse(Document document) {	//TODO
		/*Element root = document.getRootElement();
		for (Dataset dataset : this.datasetList) {	
			root.addContent(dataset.generateXmlForWebService());
		}*/
		return document;
	}
	protected JSONObject createJsonResponse(String responseName) {	//TODO
		JSONArray array = new JSONArray();
		/*for (Dataset dataset : this.datasetList) {
			array.add(dataset.generateJsonForWebService());
		}*/
		
		JSONObject root = new JSONObject();
		root.put(martRemoteRequest.getType().getResponseName(), array);
		return root;
	}
}
