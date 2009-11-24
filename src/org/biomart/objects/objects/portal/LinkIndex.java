package org.biomart.objects.objects.portal;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;


public class LinkIndex extends PortalObject implements Serializable {

	private static final long serialVersionUID = -649640508783684691L;
	
	public static final String XML_ELEMENT_NAME = "linkIndex";
	public static final McNodeType MC_NODE_TYPE = null;
	
	private List<Link> linkList = null;
	private List<String> linkNameList = null;	// internal
	
	public static void main(String[] args) {}
	
	public LinkIndex(String name) {
		super(XML_ELEMENT_NAME, name);
		this.linkList = new ArrayList<Link>();
		this.linkNameList = new ArrayList<String>();
	}
	public void addUser(Link link) {
		this.linkList.add(link);
		this.linkNameList.add(link.getName());
	}
	public List<Link> getUserList() {
		return new ArrayList<Link>(linkList);
	}
	
	@Override
	public String toString() {
		StringBuffer links = new StringBuffer();
		for (int i = 0; i < this.linkList.size(); i++) {
			links.append((i == 0 ? "" : ",") + this.linkList.get(i).getName());
		}
		return 
			super.toString() + ", " + 
			"links = " + links.toString();
	}
	
	public Element generateXml() throws FunctionalException {
		Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(
				element, "links", MartConfiguratorUtils.collectionToCommaSeparatedString(this.linkNameList));
		return element;
	}
}
