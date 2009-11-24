package org.biomart.transformation.helpers;

import org.biomart.objects.MartConfiguratorConstants;


public class PartitionNameAndKeyName {

	public static void main(String[] args) {}

	private String partitionName = null;
	private String keyName = null;

	public PartitionNameAndKeyName(String partitionName, String key) {
		super();
		this.partitionName = partitionName;
		this.keyName = key;
	}

	public String getPartitionName() {
		return partitionName;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setPartitionName(String partitionName) {
		this.partitionName = partitionName;
	}

	public void setKey(String key) {
		this.keyName = key;
	}

	@Override
	public String toString() {
		return 
			"partitionName = " + partitionName + ", " +
			"keyName = " + keyName;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		PartitionNameAndKeyName partitionAndKey=(PartitionNameAndKeyName)object;
		return (
			(this.partitionName==partitionAndKey.partitionName || (this.partitionName!=null && partitionName.equals(partitionAndKey.partitionName))) &&
			(this.keyName==partitionAndKey.keyName || (this.keyName!=null && keyName.equals(partitionAndKey.keyName)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		/*hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();*/
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==partitionName? 0 : partitionName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==keyName? 0 : keyName.hashCode());
		return hash;
	}

	/*@Override
	public int compare(PartitionAndKey partitionAndKey1, PartitionAndKey partitionAndKey2) {
		if (partitionAndKey1==null && partitionAndKey2!=null) {
			return -1;
		} else if (partitionAndKey1!=null && partitionAndKey2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(partitionAndKey1.partitionName, partitionAndKey2.partitionName);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(partitionAndKey1.key, partitionAndKey2.key);
	}

	@Override
	public int compareTo(PartitionAndKey partitionAndKey) {
		return compare(this, partitionAndKey);
	}*/

}
