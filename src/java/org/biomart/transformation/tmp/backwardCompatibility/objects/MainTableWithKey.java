package org.biomart.transformation.tmp.backwardCompatibility.objects;

public class MainTableWithKey {

	@Override
	public String toString() {
		return tableName + ", " + keyName;
	}
	public String tableName = null;
	public String keyName = null;
	public MainTableWithKey(String tableName) {
		super();
		this.tableName = tableName;
	}
}
