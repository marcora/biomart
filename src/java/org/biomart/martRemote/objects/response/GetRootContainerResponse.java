package org.biomart.martRemote.objects.response;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.lite.LiteRootContainer;
import org.biomart.objects.objects.MartRegistry;

public class GetRootContainerResponse extends GetContaineesResponse {

	private LiteRootContainer liteRootContainer = null;
	
	public GetRootContainerResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
		
		this.liteRootContainer = new LiteRootContainer(martServiceRequest);
	}
	public LiteRootContainer getLiteRootContainer() {
		return this.liteRootContainer;
	}
	public void populateObjects() throws FunctionalException {
		super.populateObjects();
		this.liteRootContainer.setLiteRootContainer(super.liteRootContainer);
		this.liteRootContainer.lock();
	}
	
	@Override
	protected Jsoml createOutputResponse(boolean xml, Jsoml root)
			throws FunctionalException {
		// TODO Auto-generated method stub
		return null;
	}
}
