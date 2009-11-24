package org.biomart.objects.data;

import java.io.Serializable;

import net.sf.json.JSONObject;

import org.biomart.martRemote.Jsoml;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.objects.Filter;
import org.jdom.Element;


public class FilterDataRow implements Serializable {

	private static final long serialVersionUID = -5070333165845435909L;

	public static void main(String[] args) {}

	private Filter filter = null;
	private String value = null;
	private String displayName = null;
	private Boolean selectedByDefault = null;

	public FilterDataRow(Filter filter, String value, String displayName, Boolean selectedByDefault) {
		super();
		this.filter = filter;
		this.value = value;
		this.displayName = displayName;
		this.selectedByDefault = selectedByDefault;
	}

	public Filter getFilter() {
		return filter;
	}

	public String getValue() {
		return value;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Boolean getSelectedByDefault() {
		return selectedByDefault;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setSelectedByDefault(Boolean selectedByDefault) {
		this.selectedByDefault = selectedByDefault;
	}

	@Override
	public String toString() {
		return 
			"filter = " + (filter!=null ? filter.getName() : null) + ", " +
			"value = " + value + ", " +
			"displayName = " + displayName + ", " +
			"selectedByDefault = " + selectedByDefault;
	}
	
	public Element generateXml() {
		return generateExchangeFormat(true).getXmlElement();
	}
	public JSONObject generateJson() {
		return generateExchangeFormat(false).getJsonObject();
	}
	public Jsoml generateExchangeFormat(boolean xml) {
		Jsoml element = new Jsoml(xml, "row");
		element.setAttribute("value", this.value);
		element.setAttribute("displayName", this.displayName);
		element.setAttribute("default", String.valueOf(this.selectedByDefault));
		return element;
	}
	/*public Element generateXml() {
		Element element = new Element("row");
		element.setAttribute("value", this.value);
		element.setAttribute("displayName", this.displayName);
		element.setAttribute("default", String.valueOf(this.selectedByDefault));
		return element;
	}*/

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		FilterDataRow dataRow=(FilterDataRow)object;
		return (
			(this.value==dataRow.value || (this.value!=null && value.equals(dataRow.value))) &&
			(this.displayName==dataRow.displayName || (this.displayName!=null && displayName.equals(dataRow.displayName))) &&
			(this.selectedByDefault==dataRow.selectedByDefault || (this.selectedByDefault!=null && selectedByDefault.equals(dataRow.selectedByDefault)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==value? 0 : value.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==displayName? 0 : displayName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==selectedByDefault? 0 : selectedByDefault.hashCode());
		return hash;
	}
}
