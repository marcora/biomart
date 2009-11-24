package org.biomart.configurator.controller;

import org.biomart.configurator.model.object.McFilterObject;
import org.biomart.configurator.view.component.FilterDropDownComponent;

public class McFilterController {
	private McFilterObject model;
	private FilterDropDownComponent view;
	
	public McFilterController (McFilterObject model, FilterDropDownComponent view) {
		this.model = model;
		this.view = view;
	}
}