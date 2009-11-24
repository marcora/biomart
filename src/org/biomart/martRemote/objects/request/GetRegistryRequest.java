package org.biomart.martRemote.objects.request;


import org.biomart.martRemote.XmlParameters;
import org.biomart.martRemote.enums.MartRemoteEnum;
import org.biomart.martRemote.enums.MartServiceFormat;

public class GetRegistryRequest extends MartRemoteRequest {
	public GetRegistryRequest(XmlParameters xmlParameters, String username, String password, MartServiceFormat format) {
		super(MartRemoteEnum.GET_REGISTRY, xmlParameters, username, password, format);
	}
}
