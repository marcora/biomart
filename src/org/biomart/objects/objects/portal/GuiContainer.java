package org.biomart.objects.objects.portal;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.objects.types.ConfigLayoutType;
import org.biomart.objects.objects.types.GuiLayoutType;


public class GuiContainer extends Group implements Serializable {

	private static final long serialVersionUID = -8409602102217553508L;
	
	public static final String XML_ELEMENT_NAME = "guiContainer";
	public static final McNodeType MC_NODE_TYPE = null;
	
	private Integer row = null;
	private Integer col = null;
	private GuiLayoutType guiLayout = null;
	private ConfigLayoutType configLayout = null;
	private String icon = null;
	
	private List<ConfigPointer> configPointerList = null;
	private Links links = null;

	public GuiContainer(String name) {
		super(XML_ELEMENT_NAME, name);
	}

	public Integer getRow() {
		return row;
	}

	public void setRow(Integer row) {
		this.row = row;
	}

	public Integer getCol() {
		return col;
	}

	public void setCol(Integer col) {
		this.col = col;
	}

	public GuiLayoutType getGuiLayout() {
		return guiLayout;
	}

	public void setGuiLayout(GuiLayoutType guiLayout) {
		this.guiLayout = guiLayout;
	}

	public ConfigLayoutType getConfigLayout() {
		return configLayout;
	}

	public void setConfigLayout(ConfigLayoutType configLayout) {
		this.configLayout = configLayout;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	
	public List<ConfigPointer> getConfigPointerList() {
		return new ArrayList<ConfigPointer>(configPointerList);
	}

	public void setConfigPointerList(List<ConfigPointer> configPointerList) {
		this.configPointerList = configPointerList;
	}

	public Links getLinks() {
		return links;
	}

	public void setLinks(Links links) {
		this.links = links;
	}
	
}
