package org.biomart.objects.helpers;


import java.util.Comparator;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.objects.MartConfiguratorConstants;



public class IntegerProperty extends Property implements Comparable<IntegerProperty>, Comparator<IntegerProperty> {

	public static void main(String[] args) {}

	private Integer value = null;

	public IntegerProperty(String property, Boolean generic, String partitionTableName, Integer partitionTableColumn, Integer value) {
		super(property, generic, partitionTableName, partitionTableColumn);
		this.value = value;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
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
		IntegerProperty integerProperty=(IntegerProperty)object;
		return (
			(this.value==integerProperty.value || (this.value!=null && value.equals(integerProperty.value)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==value? 0 : value.hashCode());
		return hash;
	}

	public int compare(IntegerProperty integerProperty1, IntegerProperty integerProperty2) {
		if (integerProperty1==null && integerProperty2!=null) {
			return -1;
		} else if (integerProperty1!=null && integerProperty2==null) {
			return 1;
		}
		return CompareUtils.compareNull(integerProperty1.value, integerProperty2.value);
	}

	public int compareTo(IntegerProperty integerProperty) {
		return compare(this, integerProperty);
	}

	@Override
	public String getStringValue() {
		return this.value!=null ? String.valueOf(this.value) : null;
	}
}
