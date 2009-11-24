package org.biomart.objects.objects.portal;

import java.io.Serializable;

import org.biomart.common.general.exceptions.FunctionalException;
import org.jdom.Element;

public class Portal implements Serializable {

	private static final long serialVersionUID = 3193550598191848391L;

	public static final String XML_ELEMENT_NAME = "portal";
	private Users users = null;
	private Aliases aliases = null;
	private LinkIndices linkIndices = null;
	private GuiContainers guiContainers = null;
	
	public Portal() {
		this.users = new Users();
		this.aliases = new Aliases();
		this.linkIndices = new LinkIndices();
		this.guiContainers = new GuiContainers();
	}
	
	@Override
	public String toString() {
		return 
			"users = " + users + ", " +
			"aliases = " + aliases + ", " +
			"linkIndices = " + linkIndices + ", " +
			"guiContainers = " + guiContainers;
	}
	
	public Element generateXml() throws FunctionalException {
		Element element = new Element(XML_ELEMENT_NAME);
		element.addContent(users.generateXml());
		element.addContent(aliases.generateXml());
		element.addContent(linkIndices.generateXml());
		element.addContent(guiContainers.generateXml());
		return element;
	}
}
