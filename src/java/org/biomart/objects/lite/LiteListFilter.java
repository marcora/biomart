package org.biomart.objects.lite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.objects.request.MartRemoteRequest;

public class LiteListFilter extends ListLiteMartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 6245776134408584897L;

	public LiteListFilter(MartRemoteRequest martRemoteRequest) {
		super(martRemoteRequest);
	}
	
	/**
	 * Only used to populate the objects, should not be used once locked
	 */
	public void addLiteFilter(LiteFilter liteFilter) throws FunctionalException {
		super.checkLock();
		super.liteMartConfiguratorObjectList.add(liteFilter);
	}
	
	@SuppressWarnings("unchecked")
	public List<LiteFilter> getLiteFilterList() {
		return new ArrayList<LiteFilter>((List<LiteFilter>)super.getLiteMartConfiguratorObjectList());
	}

	@Override
	public String toString() {
		return "liteFilterList = " + super.toString();
	}
}
