package org.biomart.objects.helpers;


import java.util.Comparator;
import java.util.List;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;



public class ListStringProperty extends Property implements Comparable<ListStringProperty>, Comparator<ListStringProperty> {

	public static void main(String[] args) {}

	private List<String> value = null;

	public ListStringProperty(String property, Boolean generic, String partitionTableName, Integer partitionTableColumn, List<String> value) {
		super(property, generic, partitionTableName, partitionTableColumn);
		this.value = value;
	}

	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"value = " + value;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		ListStringProperty listStringProperty=(ListStringProperty)object;
		return (
			(this.value==listStringProperty.value || (this.value!=null && value.equals(listStringProperty.value)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==value? 0 : value.hashCode());
		return hash;
	}

	public int compare(ListStringProperty listStringProperty1, ListStringProperty listStringProperty2) {
		if (listStringProperty1==null && listStringProperty2!=null) {
			return -1;
		} else if (listStringProperty1!=null && listStringProperty2==null) {
			return 1;
		}
		return CompareUtils.compareNull(listStringProperty1.value, listStringProperty2.value);
	}

	public int compareTo(ListStringProperty listStringProperty) {
		return compare(this, listStringProperty);
	}

	@Override
	public String getStringValue() {
		return this.value!=null ? 
				MartConfiguratorUtils.collectionToString(this.value, MartConfiguratorConstants.LIST_ELEMENT_SEPARATOR) : null;
	}

}
