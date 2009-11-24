package org.biomart.test.linkIndicesTest.program;

import org.biomart.common.general.utils.MyUtils;

public class Table {
	
	private String tableName = null;
	private String[] fieldNameTab = null;
	public Table(String tableName, String... fieldNameTab) {
		super();
		this.tableName = tableName;
		this.fieldNameTab = fieldNameTab;			
		for (int i = 0; i < fieldNameTab.length; i++) {
			this.fieldNameTab[i] = tableName + "." + fieldNameTab[i];
		}
	}
	@Override
	public String toString() {
		return "tableName = " + tableName + ", fieldNameTab = " + MyUtils.arrayToStringBuffer(fieldNameTab);
	}
	
	public String[] getFieldNameTab() {
		return this.fieldNameTab;
	}
}
