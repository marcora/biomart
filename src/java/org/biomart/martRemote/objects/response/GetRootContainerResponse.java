package org.biomart.martRemote.objects.response;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.martRemote.MartRemoteUtils;
import org.biomart.martRemote.objects.request.GetRootContainerRequest;
import org.biomart.martRemote.objects.request.MartServiceRequest;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.Location;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartRegistry;
import org.biomart.objects.objects.PartitionTable;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

public class GetRootContainerResponse extends MartServiceResponse {

	private List<Container> containerList = null;

	public GetRootContainerResponse(String responseName, Namespace martServiceNamespace, Namespace xsiNamespace, String xsdFile,
			MartRegistry martRegistry, MartServiceRequest martServiceRequest) {
		super(responseName, martServiceNamespace, xsiNamespace, xsdFile, martRegistry, martServiceRequest);
		this.containerList = new ArrayList<Container>();
	}

	public List<Container> getContainerList() {
		return containerList;
	}

	public void populateObjects() throws FunctionalException {
		GetRootContainerRequest getRootContainerRequest = (GetRootContainerRequest)super.martServiceRequest;
		Dataset dataset = fetchDatasetByName(
				getRootContainerRequest.getUsername(), getRootContainerRequest.getDatasetName());
		this.containerList = fetchContainerList(
				dataset, getRootContainerRequest.getPartitionFilter(), getRootContainerRequest.getPartitionFilterValues());
	}
	private Dataset fetchDatasetByName(String username, String datasetName) {
		Dataset dataset = null;
		List<Location> locationList = super.martRegistry.getLocationList();
		for (Location location : locationList) {
			if (username.equals(location.getUser())) {
				List<Mart> martList = location.getMartList();
				for (Mart mart : martList) {
					List<Dataset> datasetList = mart.getDatasetList();
					for (Dataset datasetTmp : datasetList) {
						if (datasetName.equals(datasetTmp.getName())) {
							dataset = datasetTmp;
						}
					}
				}
			}
		}
		return dataset;
	}
public List<Integer> mainRowNumbersWanted = new ArrayList<Integer>();	//TODO
	private List<Container> fetchContainerList(Dataset dataset, String partitionFilter, List<String> partitionFilterValues) throws FunctionalException {
		this.containerList = new ArrayList<Container>();
		if (dataset!=null) {
			List<Config> configList = dataset.getConfigList();
			Config config = configList.get(0);	// For now always just 1 config per dataset

			// Filter the invisible containers & the invisible attribute/filters and the ones not-wanted based on the partitionFilter choice (if any)
			if (null==partitionFilter) {	// add all main rows
				PartitionTable mainPartitionTable = dataset.getMainPartitionTable();
				for (int rowNumber = 0; rowNumber < mainPartitionTable.getTotalRows(); rowNumber++) {
					mainRowNumbersWanted.add(rowNumber);					
				}
			}
			

System.out.println(mainRowNumbersWanted);

			try {
				for (Container container : config.getContainerList()) {
					if (container.getVisible()) {
						this.containerList.add(new Container(container, mainRowNumbersWanted));
					}
				}
			} catch (CloneNotSupportedException e) {
				throw new FunctionalException(e);
			}
		}
		
		return containerList;
	}
	
	protected Document createXmlResponse(Document document) {
		Element root = document.getRootElement();
		for (Container container : this.containerList) {
			if (container.getVisible()) {	// filtering should take place here but in populate objects TODO
				root.addContent(container.generateXmlForWebService());
			}
		}
		return document;
	}
	protected JSONObject createJsonResponse() {
		JSONArray array = new JSONArray();
		
		for (Container container : this.containerList) {
			if (container.getVisible()) {
				array.add(container.generateJsonForWebService());
			}
		}
		
		JSONObject root = new JSONObject();
		root.put(super.responseName, array);
		return root;
	}
}
