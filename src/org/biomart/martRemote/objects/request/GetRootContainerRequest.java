package org.biomart.martRemote.objects.request;

import java.util.List;

import org.biomart.martRemote.XmlParameters;
import org.biomart.martRemote.enums.MartRemoteEnum;
import org.biomart.martRemote.enums.MartServiceFormat;


public class GetRootContainerRequest extends GetContaineesRequest {
	public GetRootContainerRequest(XmlParameters xmlParameters, 
			String username, String password, MartServiceFormat format, String datasetName, String partitionFilter, List<String> partitionFilterValues) {
		super(MartRemoteEnum.GET_ROOT_CONTAINER, xmlParameters, username, password, format, datasetName, partitionFilter, partitionFilterValues);
	}
}
