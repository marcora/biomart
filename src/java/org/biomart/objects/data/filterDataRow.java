package org.biomart.objects.data;

import java.io.Serializable;


import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.objects.Filter;
import org.jdom.Element;


public class filterDataRow implements Serializable {

	private static final long serialVersionUID = -5070333165845435909L;

	public static void main(String[] args) {}

	private Filter filter = null;
	private String value = null;
	private String displayName = null;
	private Boolean selectedByDefault = null;

	public filterDataRow(Filter filter, String value, String displayName, Boolean selectedByDefault) {
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
		Element element = new Element("row");
		element.setAttribute("value", this.value);
		element.setAttribute("displayName", this.displayName);
		element.setAttribute("default", String.valueOf(this.selectedByDefault));
		return element;
	}

	/*public String generateString() {
		return this.value + MartConfiguratorConstants.DATA_INFO_SEPARATOR + 
		this.displayName + MartConfiguratorConstants.DATA_INFO_SEPARATOR + this.selectedByDefault;
	}*/

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		filterDataRow dataRow=(filterDataRow)object;
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

	/*@Override
	public int compare(DataRow dataRow1, DataRow dataRow2) {
		if (dataRow1==null && dataRow2!=null) {
			return -1;
		} else if (dataRow1!=null && dataRow2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(dataRow1.simpleFilter, dataRow2.simpleFilter);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(dataRow1.value, dataRow2.value);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(dataRow1.displayName, dataRow2.displayName);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(dataRow1.selectedByDefault, dataRow2.selectedByDefault);
	}

	@Override
	public int compareTo(DataRow dataRow) {
		return compare(this, dataRow);
	}*/
}
