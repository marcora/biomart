package org.biomart.objects.lite;


import java.io.Serializable;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.Mart;

public class LiteDataset extends LiteMartConfiguratorObject implements Serializable {
	
	private static final long serialVersionUID = 5954018490665763734L;
	
	private static final String XML_ELEMENT_NAME = "dataset";
	
	private Boolean materialized = null;

	public LiteDataset(Mart mart, Dataset dataset, Config config) {
		super(XML_ELEMENT_NAME, null, null, null, dataset.getVisible()/* && config.getVisible()*/);
		super.name = computeDatasetName(mart, config);	// cannot invoke method in constructor call........
		
		this.materialized = dataset.getMaterialized();
	}
	private String computeDatasetName(Mart mart, Config config) {
		return config.getName() + "." + mart.getName() + "." + mart.getVersion();
	}
	
	// Properties in super class available for this light object
	public Boolean getVisible() {
		return super.visible;
	}

	public Boolean getMaterialized() {
		return materialized;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"materialized = " + materialized;
	}
	@Override
	protected Jsoml generateExchangeFormat(boolean xml) throws FunctionalException {
		Jsoml jsoml = new Jsoml(xml, super.xmlElementName);
		
		// Config info
		jsoml.setAttribute("name", this.name);
		jsoml.setAttribute("visible", this.visible);
		
		// Dataset info
		jsoml.setAttribute("materialized", this.materialized);
		
		return jsoml;
	}
}
