package org.biomart.martRemote.objects.response;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.objects.request.GetDatasetsRequest;
import org.biomart.martRemote.objects.request.MartServiceRequest;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.Location;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartRegistry;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

public class GetDatasetsResponse extends MartServiceResponse {

	//private List<Mart> martList = null;
	private List<Dataset> datasetList = null;
	//private List<Config> configList = null;

	public GetDatasetsResponse(String responseName, Namespace martServiceNamespace, Namespace xsiNamespace, String xsdFile,
			MartRegistry martRegistry, MartServiceRequest martServiceRequest) {
		super(responseName, martServiceNamespace, xsiNamespace, xsdFile, martRegistry, martServiceRequest);
		//this.martList = new ArrayList<Mart>();
		this.datasetList = new ArrayList<Dataset>();
		//this.configList = new ArrayList<Config>();
	}

	public List<Dataset> getDatasetList() {
		return datasetList;
	}

	public void populateObjects() throws FunctionalException {
		//this.martList = new ArrayList<Mart>();
		this.datasetList = new ArrayList<Dataset>();
		//this.configList = new ArrayList<Config>();
		
		List<Location> locationList = martRegistry.getLocationList();
		GetDatasetsRequest getDatasetsRequest = (GetDatasetsRequest)super.martServiceRequest;
		try {
			for (Location location : locationList) {
				if (getDatasetsRequest.getUsername().equals(location.getUser())) {
					List<Mart> martList = location.getMartList();
					for (Mart mart : martList) {
						if (getDatasetsRequest.getMartName().equals(mart.getName()) && 
								getDatasetsRequest.getMartVersion().intValue()==mart.getVersion().intValue()) {
							//Mart cloneMart = new Mart(mart);
							List<Dataset> datasetListTmp = mart.getDatasetList();
							for (Dataset dataset : datasetListTmp) {
								List<Config> configListTmp = dataset.getConfigList();
								for (Config config : configListTmp) {
									//this.martList.add(cloneMart);
									this.datasetList.add(new Dataset(dataset, computeDatasetName(mart, config)));
									//this.configList.add(new Config(config));
								}
							}
							break;	// there should be only 1 such match
						}
					}
				}
			}
		} catch (CloneNotSupportedException e) {
			throw new FunctionalException(e);
		}
	}
	protected Document createXmlResponse(Document document) {
		Element root = document.getRootElement();
		for (int i = 0; i < this.datasetList.size(); i++) {
			//Mart mart = this.martList.get(i);
			Dataset dataset = this.datasetList.get(i);
			//Config config = this.configList.get(i);
			
			Element jdomObject = new Element("dataset");
			
			// Dataset info
			MartConfiguratorUtils.addAttribute(jdomObject, "materialized", dataset.getMaterialized());
			MartConfiguratorUtils.addAttribute(jdomObject, "centralTable", dataset.getCentralTable());
			
			// Config info
			MartConfiguratorUtils.addAttribute(jdomObject, "name", dataset.getName());
			MartConfiguratorUtils.addAttribute(jdomObject, "visible", dataset.getVisible()/* && config.getVisible()*/);
			
			root.addContent(jdomObject);
		}
		return document;
	}
	protected JSONObject createJsonResponse() {
		JSONArray array = new JSONArray();
		for (int i = 0; i < this.datasetList.size(); i++) {
			//Mart mart = this.martList.get(i);
			Dataset dataset = this.datasetList.get(i);
			//Config config = this.configList.get(i);
			
			JSONObject object = new JSONObject();
			
			// Dataset info
			object.put("materialized", dataset.getMaterialized());
			object.put("centralTable", dataset.getCentralTable());
			
			// Config info
			object.put("name", dataset.getName());
			object.put("visible", dataset.getVisible()/* && config.getVisible()*/);
			
			JSONObject wrapper = new JSONObject();
			wrapper.put("dataset", object);
			array.add(wrapper);
		}
		
		JSONObject root = new JSONObject();
		root.put(super.responseName, array);
		return root;
	}
	private String computeDatasetName(Mart mart, Config config) {
		return config.getName() + "." + mart.getName() + "." + mart.getVersion();
	}
}
