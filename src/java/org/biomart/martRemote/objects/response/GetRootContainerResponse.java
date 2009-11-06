package org.biomart.martRemote.objects.response;

import java.util.List;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.MartRegistry;
import org.jdom.Document;

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
		Jsoml root = new Jsoml(document.getRootElement());
		createOutputResponse(true, root).getXmlElement();
		return document;		
	}
	protected JSONObject createJsonResponse(String responseName) throws FunctionalException {
		return createOutputResponse(false, new Jsoml(false, responseName)).getJsonObject();		
	}
	public Jsoml createOutputResponse(boolean xml, Jsoml root) throws FunctionalException {
		for (Container container : this.containerList) {
			root.addContent(container.generateOutputForWebService(xml));
		}
		return root;
	}
	/*protected Document createXmlResponse(Document document) throws FunctionalException {
		Element root = document.getRootElement();
		for (Container container : this.containerList) {
			root.addContent(container.generateOutputForWebService(true).getXmlElement());
					//.generateXmlForWebService());
		}
		return document;
	}
	protected JSONObject createJsonResponse(String responseName) {
		JSONArray array = new JSONArray();
		
		for (Container container : this.containerList) {
			try {
				array.add(container.generateOutputForWebService(false).getJsonObject());
			} catch (FunctionalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					//generateJsonForWebService());
		}
		
		JSONObject root = new JSONObject();
		root.put(martRemoteRequest.getType().getResponseName(), array);
		return root;
	}*/
}
