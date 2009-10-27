package org.biomart.transformation.helpers;

import org.biomart.objects.MartConfiguratorConstants;


public class DimensionPartitionNameAndKeyAndValue {

	public static void main(String[] args) {}

	private String dimensionName = null;
	private String key = null;
	private String value = null;

	public DimensionPartitionNameAndKeyAndValue(String dimensionName, String key, String value) {
		super();
		this.dimensionName = dimensionName;
		this.key = key;
		this.value = value;
	}

	public String getDimensionName() {
		return dimensionName;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public void setDimensionName(String dimensionName) {
		this.dimensionName = dimensionName;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return 
			"dimensionName = " + dimensionName + ", " + 
			"key = " + key + ", " +
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
		DimensionPartitionNameAndKeyAndValue dimensionNameAndValue=(DimensionPartitionNameAndKeyAndValue)object;
		return (
			(this.dimensionName==dimensionNameAndValue.dimensionName || (this.dimensionName!=null && dimensionName.equals(dimensionNameAndValue.dimensionName))) &&
			(this.key==dimensionNameAndValue.key || (this.key!=null && key.equals(dimensionNameAndValue.key))) &&
			(this.value==dimensionNameAndValue.value || (this.value!=null && value.equals(dimensionNameAndValue.value)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==dimensionName? 0 : dimensionName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==key? 0 : key.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==value? 0 : value.hashCode());
		return hash;
	}

	/*@Override
	public int compare(DimensionNameAndValue dimensionNameAndValue1, DimensionNameAndValue dimensionNameAndValue2) {
		if (dimensionNameAndValue1==null && dimensionNameAndValue2!=null) {
			return -1;
		} else if (dimensionNameAndValue1!=null && dimensionNameAndValue2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(dimensionNameAndValue1.dimensionName, dimensionNameAndValue2.dimensionName);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(dimensionNameAndValue1.value, dimensionNameAndValue2.value);
	}

	@Override
	public int compareTo(DimensionNameAndValue dimensionNameAndValue) {
		return compare(this, dimensionNameAndValue);
	}*/

}
