package org.biomart.martRemote.objects.request;

import java.util.List;

import org.biomart.martRemote.XmlParameters;
import org.biomart.martRemote.enums.MartRemoteEnum;
import org.biomart.martRemote.enums.MartServiceFormat;


public class GetAttributesRequest extends GetContaineesRequest {
	
	public GetAttributesRequest(XmlParameters xmlParameters,
			String username, String password, MartServiceFormat format, String datasetName, String partitionFilter, List<String> partitionFilterValues) {
		super(MartRemoteEnum.GET_ATTRIBUTES, xmlParameters, username, password, format, datasetName, partitionFilter, partitionFilterValues);
	}
}
