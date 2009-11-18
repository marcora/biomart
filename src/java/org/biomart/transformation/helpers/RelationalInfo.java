package org.biomart.transformation.helpers;

import org.biomart.objects.MartConfiguratorConstants;

public class RelationalInfo {

	private String tableName = null;
	private String keyName = null;
	private String columnName = null;
	public RelationalInfo(String tableName, String keyName, String columnName) {
		super();
		this.tableName = tableName;
		this.keyName = keyName;
		this.columnName = columnName;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getKeyName() {
		return keyName;
	}
	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	@Override
	public boolean equals(Object obj) {
		RelationalInfo relationalInfo  = (RelationalInfo)obj;
		return 
			this.tableName.equals(relationalInfo.tableName) &&
			this.keyName.equals(relationalInfo.keyName) &&
			this.columnName.equals(relationalInfo.columnName);
	}
	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==tableName ? 0 : tableName.hashCode());
		return hash;
	}
	@Override
	public String toString() {
		return "tableName = " + tableName + ", " +
		"keyName = " + keyName + ", " +
		"columnName = " + columnName;
	}
}
