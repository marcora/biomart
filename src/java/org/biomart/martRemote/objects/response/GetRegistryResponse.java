package org.biomart.martRemote.objects.response;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.biomart.martRemote.objects.request.MartServiceRequest;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.objects.Location;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartRegistry;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

public class GetRegistryResponse extends MartServiceResponse {

	private List<Location> locationList = null;	// same index
	private List<Mart> martList = null;	// same index

	public GetRegistryResponse(String responseName, Namespace martServiceNamespace, Namespace xsiNamespace, String xsdFile, 
			MartRegistry martRegistry, MartServiceRequest martServiceRequest) {
		super(responseName, martServiceNamespace, xsiNamespace, xsdFile, martRegistry, martServiceRequest);
		this.martList = new ArrayList<Mart>();
		this.locationList = new ArrayList<Location>();
	}
	
	public List<Mart> getMartList() {
		return martList;
	}

	public List<Location> getLocationList() {
		return locationList;
	}

	public void populateObjects() {
		this.martList = new ArrayList<Mart>();
		this.locationList = new ArrayList<Location>();
		
		List<Location> locationListTmp = martRegistry.getLocationList();
		for (Location location : locationListTmp) {
			if (super.martServiceRequest.getUsername().equals(location.getUser())) {
				List<Mart> martListTmp = location.getMartList();
				martList.addAll(martListTmp);
				for (int i = 0; i < martListTmp.size(); i++) {
					this.locationList.add(location);
				}
			}
		}
	}
	
	protected Document createXmlResponse(Document document) {
		Element root = document.getRootElement();
		for (int i = 0; i < this.martList.size(); i++) {
			Mart mart = this.martList.get(i);	// martList is never null (empty at worse)
			Location location = this.locationList.get(i);
			
			Element jdomObject = new Element("mart");
			
			// Mart info
			MartConfiguratorUtils.addAttribute(jdomObject, "name", mart.getName());
			MartConfiguratorUtils.addAttribute(jdomObject, "displayName", mart.getDisplayName());
			MartConfiguratorUtils.addAttribute(jdomObject, "description", mart.getDescription());
			MartConfiguratorUtils.addAttribute(jdomObject, "visible", mart.getVisible());		
			
			MartConfiguratorUtils.addAttribute(jdomObject, "version", mart.getVersion());
			
			// Location info
			MartConfiguratorUtils.addAttribute(jdomObject, "locationName", location.getName());
			MartConfiguratorUtils.addAttribute(jdomObject, "locationDisplayName", location.getDisplayName());
			MartConfiguratorUtils.addAttribute(jdomObject, "locationDescription", location.getDescription());
			MartConfiguratorUtils.addAttribute(jdomObject, "locationVisible", location.getVisible());
			
			MartConfiguratorUtils.addAttribute(jdomObject, "host", location.getHost());
			MartConfiguratorUtils.addAttribute(jdomObject, "type", (location.getType()!=null ? location.getType().getXmlValue() : null));
			MartConfiguratorUtils.addAttribute(jdomObject, "user", location.getUser());
			
			root.addContent(jdomObject);
		}
		return document;
	}
	protected JSONObject createJsonResponse() {
		JSONArray array = new JSONArray();
		for (int i = 0; i < this.martList.size(); i++) {
			Mart mart = this.martList.get(i);	// martList is never null (empty at worse)
			Location location = this.locationList.get(i);
			
			JSONObject object = new JSONObject();
			
			// Mart info
			object.put("name", mart.getName());
			object.put("displayName", mart.getDisplayName());
			object.put("description", mart.getDescription());
			object.put("visible", mart.getVisible());		
			
			object.put("version", mart.getVersion());
			
			// Location info
			object.put("locationName", location.getName());
			object.put("locationDisplayName", location.getDisplayName());
			object.put("locationDescription", location.getDescription());
			object.put("locationVisible", location.getVisible());
			
			object.put("host", location.getHost());
			object.put("type", (location.getType()!=null ? location.getType().getXmlValue() : null));
			object.put("user", location.getUser());
			
			JSONObject wrapper = new JSONObject();
			wrapper.put("mart", object);
			array.add(wrapper);
		}
		
		JSONObject root = new JSONObject();
		root.put(super.responseName, array);
		return root;
	}
}