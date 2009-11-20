package org.biomart.martRemote.objects.response;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.lite.LiteFilter;
import org.biomart.objects.lite.LiteListFilter;
import org.biomart.objects.lite.LiteSimpleMartConfiguratorObject;
import org.biomart.objects.objects.MartRegistry;

public class GetFiltersResponse extends GetElementsResponse {

	private LiteListFilter liteListFilter = null;

	public GetFiltersResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
		
		this.liteListFilter = new LiteListFilter(martServiceRequest);
	}

	public LiteListFilter getLiteListFilter() {
		return liteListFilter;
	}

	public void populateObjects() throws FunctionalException {
		super.populateObjects(false);
		for (LiteSimpleMartConfiguratorObject liteElement : super.liteElementList) {
			this.liteListFilter.addLiteFilter((LiteFilter)liteElement);
		}
		this.liteListFilter.lock();
	}
	
	@Override
	protected Jsoml createOutputResponse(boolean xml, Jsoml root)
			throws FunctionalException {
		// TODO Auto-generated method stub
		return null;
	}
}
