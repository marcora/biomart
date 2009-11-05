package org.biomart.martRemote.objects.response;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.MartRegistry;
import org.jdom.Document;
import org.jdom.Element;

public class GetRootContainerResponse extends GetContaineesResponse {

	public GetRootContainerResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
	}
	public List<Container> getContainerList() {
		return super.containerList;
	}
	public void populateObjects() throws FunctionalException {
		super.populateObjects();
	}
	protected Document createXmlResponse(Document document) throws FunctionalException {
		Element root = document.getRootElement();
		for (Container container : this.containerList) {
			root.addContent(container.generateXmlForWebService());
		}
		return document;
	}
	protected JSONObject createJsonResponse() {
		JSONArray array = new JSONArray();
		
		for (Container container : this.containerList) {
			array.add(container.generateJsonForWebService());
		}
		
		JSONObject root = new JSONObject();
		root.put(martRemoteRequest.getType().getResponseName(), array);
		return root;
	}
}
