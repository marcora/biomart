package org.biomart.objects.objects.portal;

import java.io.Serializable;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;

public class Alias extends PortalObject implements Serializable {

	private static final long serialVersionUID = -8176991179209098381L;
	
	public static final String XML_ELEMENT_NAME = "alias";
	public static final McNodeType MC_NODE_TYPE = null;
	
	private String locationName = null;	// TODO create an object for those 5 properties? used in elements as well
	private String martName = null;
	private Integer version = null;
	private String datasetName = null;
	private String configName = null;
	
	public Alias(String name) {
		this(name, null, null, null, null, null);
	}
	public Alias(String name, String locationName, String martName, Integer version, String datasetName, String configName) {
		super(XML_ELEMENT_NAME, name);
		this.locationName = locationName;
		this.martName = martName;
		this.version = version;
		this.datasetName = datasetName;
		this.configName = configName;
	}
	
	public String getLocationName() {
		return locationName;
	}
	public String getMartName() {
		return martName;
	}
	public Integer getVersion() {
		return version;
	}
	public String getDatasetName() {
		return datasetName;
	}
	public String getConfigName() {
		return configName;
	}
	
	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"locationName = " + locationName + ", " +
			"martName = " + martName + ", " +
			"version = " + version + ", " +
			"datasetName = " + datasetName + ", " +
			"configName = " + configName;			
	}
	
	public Element generateXml() throws FunctionalException {
		Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "locationName", locationName);
		MartConfiguratorUtils.addAttribute(element, "martName", martName);
		MartConfiguratorUtils.addAttribute(element, "version", version);
		MartConfiguratorUtils.addAttribute(element, "datasetName", datasetName);
		MartConfiguratorUtils.addAttribute(element, "configName", configName);
		return element;
	}
}
