package org.biomart.martRemote.objects.response;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.lite.LiteAttribute;
import org.biomart.objects.lite.LiteListAttribute;
import org.biomart.objects.lite.LiteMartConfiguratorObject;
import org.biomart.objects.lite.MartRemoteWrapper;
import org.biomart.objects.objects.MartRegistry;

public class GetAttributesResponse extends GetElementsResponse {

	private LiteListAttribute liteListAttribute = null;

	public GetAttributesResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
		
		this.liteListAttribute = new LiteListAttribute(martServiceRequest);
	}

	@Override
	public MartRemoteWrapper getMartRemoteWrapper() {
		return this.getLiteListAttribute();
	}
	public LiteListAttribute getLiteListAttribute() {
		return liteListAttribute;
	}

	public void populateObjects() throws FunctionalException {
		super.populateObjects(true);
		for (LiteMartConfiguratorObject liteElement : super.liteElementList) {
			this.liteListAttribute.addLiteAttribute((LiteAttribute)liteElement);
		}
		this.liteListAttribute.lock();
	}
}
