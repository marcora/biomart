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
	/*public List<Container> getContainerList() {
		return super.containerList;
	}*/
	public Container getRootContainer() {
		return super.rootContainer;
	}
	public void populateObjects() throws FunctionalException {
		super.populateObjects();
	}
	
	@Override
	public Jsoml createOutputResponse(boolean xml, Jsoml root) throws FunctionalException {
		root.addContent(super.rootContainer.generateOutputForWebService(xml));
		/*for (Container container : this.containerList) {
			root.addContent(container.generateOutputForWebService(xml));
		}*/
		return root;
	}
}
