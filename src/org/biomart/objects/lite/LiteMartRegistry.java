package org.biomart.objects.lite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.objects.request.MartRemoteRequest;

public class LiteMartRegistry extends ListLiteMartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 6245776134408584897L;

	public LiteMartRegistry(MartRemoteRequest martRemoteRequest) {
		super(martRemoteRequest);
	}
	
	/**
	 * Only used to populate the objects, should not be used once locked
	 */
	public void addLiteMart(LiteMart liteMart) throws FunctionalException {
		super.addliteMartConfiguratorObject(liteMart);
	}
	
	@SuppressWarnings("unchecked")
	public List<LiteMart> getLiteMartList() {
		return new ArrayList<LiteMart>((List<LiteMart>)super.getLiteMartConfiguratorObjectList());
	}

	@Override
	public String toString() {
		return "liteMartList = " + super.toString();
	}
}
