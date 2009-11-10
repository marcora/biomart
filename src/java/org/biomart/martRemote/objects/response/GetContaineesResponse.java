package org.biomart.martRemote.objects.response;

import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.objects.request.GetContaineesRequest;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.Location;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartRegistry;
import org.biomart.objects.objects.PartitionTable;

public abstract class GetContaineesResponse extends MartRemoteResponse {

	protected List<Container> containerList = null;

	private Dataset dataset = null;
	
	protected GetContaineesResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
		this.containerList = new ArrayList<Container>();
	}

	public void populateObjects() throws FunctionalException {
		fetchDatasetByName();
		fetchContainerList();
	}
	
	private void fetchDatasetByName() {
		GetContaineesRequest getContaineesRequest = (GetContaineesRequest)super.martRemoteRequest;
		List<Location> locationList = super.martRegistry.getLocationList();
		for (Location location : locationList) {
			if (getContaineesRequest.getUsername().equals(location.getUser())) {
				List<Mart> martList = location.getMartList();
				for (Mart mart : martList) {
					List<Dataset> datasetList = mart.getDatasetList();
					for (Dataset datasetTmp : datasetList) {
						if (getContaineesRequest.getDatasetName().equals(datasetTmp.getName())) {
							this.dataset = datasetTmp;
						}
					}
				}
			}
		}
	}
	
	private void fetchContainerList() throws FunctionalException {
		GetContaineesRequest getContaineesRequest = (GetContaineesRequest)super.martRemoteRequest;
		this.containerList = new ArrayList<Container>();
		if (dataset!=null) {
			List<Config> configList = dataset.getConfigList();
			Config config = configList.get(0);	// For now always just 1 config per dataset

			// Filter the invisible containers & the invisible attribute/filters and the ones not-wanted based on the partitionFilter choice (if any)
			List<Integer> mainRowNumbersWanted = new ArrayList<Integer>();
			PartitionTable mainPartitionTable = dataset.getMainPartitionTable();
			if (null==getContaineesRequest.getPartitionFilter()) {	// add all main rows
				for (int rowNumber = 0; rowNumber < mainPartitionTable.getTotalRows(); rowNumber++) {
					mainRowNumbersWanted.add(rowNumber);					
				}
			} else {
				for (String partitionFilterValue : getContaineesRequest.getPartitionFilterValues()) {
					mainRowNumbersWanted.add(mainPartitionTable.getRowNumber(partitionFilterValue));
				}
			}

			System.out.println("mainRowNumbersWanted = " + mainRowNumbersWanted);

			
			
			/*for (Container container : config.getContainerList()) {
				if (container.getVisible()) {
					this.containerList.add(new Container(container, mainRowNumbersWanted));
				}
			}*/
		}
	}
}
