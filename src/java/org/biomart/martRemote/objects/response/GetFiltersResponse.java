package org.biomart.martRemote.objects.response;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.biomart.martRemote.objects.request.MartServiceRequest;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.MartRegistry;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

public class GetFiltersResponse extends MartServiceResponse {

	private List<Filter> filterList = null;

	public GetFiltersResponse(String responseName, Namespace martServiceNamespace, Namespace xsiNamespace, String xsdFile,
			MartRegistry martRegistry, MartServiceRequest martServiceRequest) {
		super(responseName, martServiceNamespace, xsiNamespace, xsdFile, martRegistry, martServiceRequest);
		this.filterList = new ArrayList<Filter>();
	}

	public List<Filter> getFilterList() {
		return filterList;
	}

	public void populateObjects() {}	//TODO
	
	protected Document createXmlResponse(Document document) {
		Element root = document.getRootElement();
		for (Filter filter : this.filterList) {
			root.addContent(filter.generateXmlForWebService());
		}
		return document;
	}
	protected JSONObject createJsonResponse() {
		JSONArray array = new JSONArray();

		for (Filter filter : this.filterList) {
			array.add(filter.generateJsonForWebService());
		}
		
		JSONObject root = new JSONObject();
		root.put(super.responseName, array);
		return root;
	}
}
