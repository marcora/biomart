package org.biomart.objects.lite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.objects.request.MartRemoteRequest;

public class LiteListDataset extends ListLiteMartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = -6146431958869873204L;

	public LiteListDataset(MartRemoteRequest martRemoteRequest) {
		super(martRemoteRequest);
	}
	
	/**
	 * Only used to populate the objects, should not be used once locked
	 */
	public void addLiteDataset(LiteDataset liteDataset) throws FunctionalException {
		super.addliteMartConfiguratorObject(liteDataset);
	}
	
	@SuppressWarnings("unchecked")
	public List<LiteDataset> getLiteDatasetList() {
		return new ArrayList<LiteDataset>((List<LiteDataset>)super.getLiteMartConfiguratorObjectList());
	}

	@Override
	public String toString() {
		return "liteDatasetList = " + super.toString();
	}
}
