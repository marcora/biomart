package org.biomart.martRemote.objects.response;

import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Containee;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.Element;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.MartRegistry;

public abstract class GetElementsResponse extends GetContaineesResponse {

	protected List<Element> elementList = null;

	public GetElementsResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
		this.elementList = new ArrayList<Element>();
	}
	
	public void populateObjects(boolean attribute) throws FunctionalException {
		super.populateObjects();
		
		// Grab only the elements of interest
		for (Container container : this.containerList) {
			addElements(container, attribute);
		}
	}
	private void addElements(Container container, boolean attribute) {
		List<Containee> containeeList = container.getContaineeList();
		for (Containee containee : containeeList) {
			if (attribute && containee instanceof Attribute) {
				this.elementList.add((Attribute)containee);		
			} else if (!attribute && containee instanceof Filter) {
				this.elementList.add((Filter)containee);		
			} else if (containee instanceof Container) {
				addElements((Container)containee, attribute);
			}
		}
	}
}
