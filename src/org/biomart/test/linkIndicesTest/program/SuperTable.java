package org.biomart.test.linkIndicesTest.program;

import org.biomart.common.general.utils.MyUtils;

public class SuperTable {
	Table[] tableTab = null;
	JoinTable joinTable = null;
	public SuperTable(Table[] tableTab, JoinTable joinTable) {
		super();	
		this.tableTab = tableTab;
		this.joinTable = joinTable;
	}
	@Override
	public String toString() {
		return "[" + 
		"tableTab = " + MyUtils.arrayToStringBuffer(tableTab) + ", " +
		"joinTable = " + joinTable + ", " +
		"]";
	}
}
