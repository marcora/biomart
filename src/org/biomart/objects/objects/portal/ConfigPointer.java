package org.biomart.objects.objects.portal;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.processors.Processor;
import org.jdom.Element;


public class ConfigPointer extends PortalObject implements Serializable {

	private static final long serialVersionUID = -6247510819913400939L;
	
	public static final String XML_ELEMENT_NAME = "configPointer";
	public static final McNodeType MC_NODE_TYPE = null;
	
	private List<Processor> processorList = null;
	private List<String> processorNameList = null;	// internal
	
	public ConfigPointer(String alias) {
		super(XML_ELEMENT_NAME, alias);
		this.processorList = new ArrayList<Processor>();
		this.processorNameList = new ArrayList<String>();
	}
	public String getAlias() {	// name is used for aliass
		return super.name;
	}
	public List<Processor> getProcessorList() {
		return new ArrayList<Processor>(processorList);
	}
	public Element generateXml() throws FunctionalException {
		Element element = super.generateXml();
		
		// rename "name" into "alias"
		element.removeAttribute("name");
		MartConfiguratorUtils.addAttribute(element, "alias", super.name);
		
		MartConfiguratorUtils.addAttribute(element, "processors", this.processorNameList);
		
		return element;
	}
}
