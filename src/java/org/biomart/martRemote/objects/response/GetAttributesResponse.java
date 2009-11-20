package org.biomart.martRemote.objects.response;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.lite.LiteAttribute;
import org.biomart.objects.lite.LiteListAttribute;
import org.biomart.objects.lite.LiteSimpleMartConfiguratorObject;
import org.biomart.objects.objects.MartRegistry;

public class GetAttributesResponse extends GetElementsResponse {

	private LiteListAttribute liteListAttribute = null;

	public GetAttributesResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
		
		this.liteListAttribute = new LiteListAttribute(martServiceRequest);
	}

	public LiteListAttribute getLiteListAttribute() {
		return liteListAttribute;
	}

	public void populateObjects() throws FunctionalException {
		super.populateObjects(true);
		for (LiteSimpleMartConfiguratorObject liteElement : super.liteElementList) {
			this.liteListAttribute.addLiteAttribute((LiteAttribute)liteElement);
		}
		this.liteListAttribute.lock();
	}
	
	@Override
	protected Jsoml createOutputResponse(boolean xml, Jsoml root)
			throws FunctionalException {
		// TODO Auto-generated method stub
		return null;
	}
}
