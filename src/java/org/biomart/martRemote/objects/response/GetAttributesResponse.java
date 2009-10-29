package org.biomart.martRemote.objects.response;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.biomart.martRemote.objects.request.MartServiceRequest;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.MartRegistry;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

public class GetAttributesResponse extends MartServiceResponse {

	private List<Attribute> attributeList = null;

	public GetAttributesResponse(String responseName, Namespace martServiceNamespace, Namespace xsiNamespace, String xsdFile,
			MartRegistry martRegistry, MartServiceRequest martServiceRequest) {
		super(responseName, martServiceNamespace, xsiNamespace, xsdFile, martRegistry, martServiceRequest);
		this.attributeList = new ArrayList<Attribute>();
	}

	public List<Attribute> getAttributeList() {
		return attributeList;
	}
	
	public void populateObjects() {}	//TODO
	
	protected Document createXmlResponse(Document document) {
		Element root = document.getRootElement();
		for (Attribute attribute : this.attributeList) {
			//root.addContent(attribute.generateXmlForWebService());
		}
		return document;
	}
	protected JSONObject createJsonResponse() {
		JSONArray array = new JSONArray();
		for (Attribute attribute : this.attributeList) {
			array.add(attribute.generateJsonForWebService());
		}
		
		JSONObject root = new JSONObject();
		root.put(super.responseName, array);
		return root;
	}
}
