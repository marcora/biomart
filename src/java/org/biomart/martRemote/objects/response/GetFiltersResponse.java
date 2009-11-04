package org.biomart.martRemote.objects.response;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.objects.Element;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.MartRegistry;
import org.jdom.Document;

public class GetFiltersResponse extends GetElementsResponse {

	private List<Filter> filterList = null;

	public GetFiltersResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
		this.filterList = new ArrayList<Filter>();
	}

	public List<Filter> getFilterList() {
		return filterList;
	}

	public void populateObjects() throws FunctionalException {
		super.populateObjects(false);
		for (Element element : super.elementList) {
			this.filterList.add((Filter)element);
		}
	}
	
	protected Document createXmlResponse(Document document) throws FunctionalException {
		org.jdom.Element root = document.getRootElement();
		for (Filter filter : this.filterList) {
			root.addContent(filter.generateXmlForWebService());
		}
		return document;
	}
	protected JSONObject createJsonResponse() {
		//TODO?
		return null;
	}
}
