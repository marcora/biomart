package org.biomart.martRemote.objects.response;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.MartRegistry;

public class GetRootContainerResponse extends GetContaineesResponse {

	public GetRootContainerResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
	}
	public Container getRootContainer() {
		return super.rootContainer;
	}
	public void populateObjects() throws FunctionalException {
		super.populateObjects();
	}
	
	@Override
	public Jsoml createOutputResponse(boolean xml, Jsoml root) throws FunctionalException {
		if (null!=super.rootContainer) {
			root.addContent(super.rootContainer.generateOutputForWebService(xml));
		}
		return root;
	}
}
