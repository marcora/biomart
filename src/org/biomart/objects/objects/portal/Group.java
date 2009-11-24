package org.biomart.objects.objects.portal;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;


public class Group extends PortalObject implements Serializable {

	private static final long serialVersionUID = -6247510819913400939L;

	public static final String XML_ELEMENT_NAME = "group";
	public static final McNodeType MC_NODE_TYPE = null;
	
	private List<User> userList = null;
	private List<String> userNameList = null;	// internal
	
	public static void main(String[] args) {}
	
	public Group(String name) {
		this(XML_ELEMENT_NAME, name);
	}
	public Group(String xmlElementName, String name) {	// for use of GuiContainer
		super(xmlElementName, name);
		this.userList = new ArrayList<User>();
		this.userNameList = new ArrayList<String>();
	}
	public void addUser(User user) {
		this.userList.add(user);
		this.userNameList.add(user.name);
	}
	public List<User> getUserList() {
		return new ArrayList<User>(userList);
	}
	
	@Override
	public String toString() {
		StringBuffer users = new StringBuffer();
		for (int i = 0; i < this.userList.size(); i++) {
			users.append((i == 0 ? "" : ",") + this.userList.get(i).name);
		}
		return 
			super.toString() + ", " + 
			"users = " + users.toString();
	}
	
	public Element generateXml() throws FunctionalException {
		Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(
				element, "users", MartConfiguratorUtils.collectionToCommaSeparatedString(this.userNameList));
		return element;
	}
}
