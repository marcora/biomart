package org.biomart.martRemote.objects.response;

import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.objects.request.GetDatasetsRequest;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.lite.LiteDataset;
import org.biomart.objects.lite.LiteListDataset;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.Location;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartRegistry;

public class GetDatasetsResponse extends MartRemoteResponse {

	private LiteListDataset liteListDataset = null;

	public GetDatasetsResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
		this.liteListDataset = new LiteListDataset(martServiceRequest);
	}

	public LiteListDataset getLiteListDataset() {
		return liteListDataset;
	}

	public void populateObjects() throws FunctionalException {
		List<Location> locationList = martRegistry.getLocationList();
		GetDatasetsRequest getDatasetsRequest = (GetDatasetsRequest)super.martRemoteRequest;
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
								LiteDataset liteDataset = new LiteDataset(mart, dataset, config);
								this.liteListDataset.addLiteDataset(liteDataset);
							}
						}
						break;	// there should be only 1 such match
					}
				}
			}
		}
		this.liteListDataset.lock();
	}

	@Override
	protected Jsoml createOutputResponse(boolean xml, Jsoml root)
			throws FunctionalException {
		// TODO Auto-generated method stub
		return null;
	}
}
