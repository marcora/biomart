package org.biomart.transformation.helpers;

import org.biomart.transformation.oldXmlObjects.OldAttribute;


public class TableNameAndKeyName {

	public static void main(String[] args) {}

	private String tableName = null;
	private String keyName = null;

	public TableNameAndKeyName(String tableName, String key) {
		super();
		this.tableName = tableName;
		this.keyName = key;
	}

	public String getTableName() {
		return tableName;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setKey(String key) {
		this.keyName = key;
	}

	@Override
	public String toString() {
		return 
			"tableName = " + tableName + ", " +
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
		TableNameAndKeyName tableAndKey=(TableNameAndKeyName)object;
		return (
			(this.tableName==tableAndKey.tableName || (this.tableName!=null && tableName.equals(tableAndKey.tableName))) &&
			(this.keyName==tableAndKey.keyName || (this.keyName!=null && keyName.equals(tableAndKey.keyName)))
		);
	}

	public boolean equalsIgnoreCase(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		TableNameAndKeyName tableAndKey=(TableNameAndKeyName)object;
		return (
			(this.tableName==tableAndKey.tableName || (this.tableName!=null && tableName.equalsIgnoreCase(tableAndKey.tableName))) &&
			(this.keyName==tableAndKey.keyName || (this.keyName!=null && keyName.equalsIgnoreCase(tableAndKey.keyName)))
		);
	}

	@Override
	public int hashCode() {
		/*int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==tableName? 0 : tableName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==keyName? 0 : keyName.hashCode());
		return hash;*/
		return 0;	// No idea why above code isn't correct
	}

	/*@Override
	public int compare(TableAndKey tableAndKey1, TableAndKey tableAndKey2) {
		if (tableAndKey1==null && tableAndKey2!=null) {
			return -1;
		} else if (tableAndKey1!=null && tableAndKey2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(tableAndKey1.tableName, tableAndKey2.tableName);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(tableAndKey1.key, tableAndKey2.key);
	}

	@Override
	public int compareTo(TableAndKey tableAndKey) {
		return compare(this, tableAndKey);
	}*/

	public static TableNameAndKeyName getTableAndKey(DimensionPartitionNameAndKeyAndValue dimensionPartitionNameAndKeyAndValue, OldAttribute oldAttribute) {
		TableNameAndKeyName tableNameAndKeyName = new TableNameAndKeyName(
				dimensionPartitionNameAndKeyAndValue.getDimensionName(), oldAttribute.getKey());
		return tableNameAndKeyName;
	}

}
