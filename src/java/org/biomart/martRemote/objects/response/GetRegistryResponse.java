package org.biomart.martRemote.objects.response;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.objects.Location;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartRegistry;
import org.jdom.Document;
import org.jdom.Element;

public class GetRegistryResponse extends MartRemoteResponse {

	private List<Location> locationList = null;	// same index
	private List<Mart> martList = null;	// same index

	public GetRegistryResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
		this.martList = new ArrayList<Mart>();
		this.locationList = new ArrayList<Location>();
	}
	
	public List<Mart> getMartList() {
		return martList;
	}

	public List<Location> getLocationList() {
		return locationList;
	}

	public void populateObjects() throws FunctionalException {
		this.martList = new ArrayList<Mart>();
		this.locationList = new ArrayList<Location>();
		
		List<Location> locationListTmp = martRegistry.getLocationList();
		try {
			for (Location location : locationListTmp) {
				if (super.martRemoteRequest.getUsername().equals(location.getUser())) {
					Location locationClone = new Location(location);
					List<Mart> martListTmp = location.getMartList();
						for (Mart mart : martListTmp) {
							this.martList.add(new Mart(mart));
							this.locationList.add(locationClone);
						}
				}
			}
		} catch (CloneNotSupportedException e) {
			throw new FunctionalException(e);
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
			MartConfiguratorUtils.addAttribute(jdomObject, "visible", mart.getVisible());		
			
			MartConfiguratorUtils.addAttribute(jdomObject, "version", mart.getVersion());
			
			// Location info
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
			object.put("visible", mart.getVisible());		
			
			object.put("version", mart.getVersion());
			
			// Location info
			
			object.put("host", location.getHost());
			object.put("type", (location.getType()!=null ? location.getType().getXmlValue() : null));
			object.put("user", location.getUser());
			
			JSONObject wrapper = new JSONObject();
			wrapper.put("mart", object);
			array.add(wrapper);
		}
		
		JSONObject root = new JSONObject();
		root.put(martRemoteRequest.getType().getResponseName(), array);
		return root;
	}
}
