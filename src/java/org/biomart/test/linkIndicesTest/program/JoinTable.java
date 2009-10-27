package org.biomart.test.linkIndicesTest.program;

public class JoinTable {
	private String table = null;
	private String leftField = null;
	private String rightField = null;	
	
	@Override
	public String toString() {
		return "[" + 
		"table = " + table +
		", " + "leftField = " + leftField +
		", " + "rightField = " + rightField +
		"]";
	}
	public JoinTable(String table, String field) {
		this(table, field, field);
	}
	public JoinTable(String table, String leftField, String rightField) {
		super();
		this.table = table;
		this.leftField = leftField;
		this.rightField = rightField;
	}
	public String getFullLeftField() {
		return table + "." + leftField;
	}
	public String getFullRightField() {
		return table + "." + rightField;
	}
	public String getShortLeftField() {
		return leftField;
	}
	public String getShortRightField() {
		return rightField;
	}
	/*public String getField() {
		return getFullRightField();
	}*/
	public String getFullSourceField() {
		return getFullRightField();
	}
	public String getShortSourceField() {
		return getShortRightField();
	}
	public String getTable() {
		return table;
	}
}
