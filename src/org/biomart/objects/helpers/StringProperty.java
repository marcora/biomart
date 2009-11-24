package org.biomart.objects.helpers;


import java.util.Comparator;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;



public class StringProperty extends Property implements Comparable<StringProperty>, Comparator<StringProperty> {

	public static void main(String[] args) {}

	private String value = null;	// Should be List<Something> where Something is either String or PartitionReference

	public StringProperty(String property, String value) {
		super(property, false, null, null);
		this.value = value;
	}
	
	public StringProperty(String property, Boolean generic, String partitionTableName, Integer partitionTableColumn, String value) {
		super(property, generic, partitionTableName, partitionTableColumn);
		this.value = value;
	}

	public void setValue(String value) {
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
		StringProperty stringProperty=(StringProperty)object;
		return (
			(this.value==stringProperty.value || (this.value!=null && value.equals(stringProperty.value)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==value? 0 : value.hashCode());
		return hash;
	}

	public int compare(StringProperty stringProperty1, StringProperty stringProperty2) {
		if (stringProperty1==null && stringProperty2!=null) {
			return -1;
		} else if (stringProperty1!=null && stringProperty2==null) {
			return 1;
		}
		return CompareUtils.compareNull(stringProperty1.value, stringProperty2.value);
	}

	public int compareTo(StringProperty stringProperty) {
		return compare(this, stringProperty);
	}
	
	@Override
	public boolean same(Property property) {
		MyUtils.checkStatusProgram(!this.generic);
		StringProperty stringProperty = (StringProperty)property;
		return CompareUtils.same(this.value, stringProperty.value);
	}

	@Override
	public String getStringValue() {
		return value;
	}
}
