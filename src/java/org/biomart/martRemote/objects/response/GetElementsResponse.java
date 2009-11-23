package org.biomart.martRemote.objects.response;

import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.lite.LiteAttribute;
import org.biomart.objects.lite.LiteContainer;
import org.biomart.objects.lite.LiteFilter;
import org.biomart.objects.lite.LiteMartConfiguratorObject;
import org.biomart.objects.objects.MartRegistry;

public abstract class GetElementsResponse extends GetContaineesResponse {

	protected List<LiteMartConfiguratorObject> liteElementList = null;

	public GetElementsResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
		this.liteElementList = new ArrayList<LiteMartConfiguratorObject>();
	}
	
	public void populateObjects(boolean attribute) throws FunctionalException {
		super.populateObjects();
		
		// Grab only the elements of interest
		addElements(super.liteRootContainer, attribute);
	}
	private void addElements(LiteContainer liteContainer, boolean attribute) {
		List<LiteMartConfiguratorObject> containeeList = liteContainer.getLiteContaineeList();
		for (LiteMartConfiguratorObject liteContainee : containeeList) {
			if (attribute && liteContainee instanceof LiteAttribute) {
				this.liteElementList.add((LiteAttribute)liteContainee);		
			} else if (!attribute && liteContainee instanceof LiteFilter) {
				this.liteElementList.add((LiteFilter)liteContainee);		
			} else if (liteContainee instanceof LiteContainer) {
				addElements((LiteContainer)liteContainee, attribute);
			}
		}
	}
}
