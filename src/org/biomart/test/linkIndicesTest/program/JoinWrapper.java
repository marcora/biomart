package org.biomart.test.linkIndicesTest.program;

public class JoinWrapper {
	JoinTable joinTable = null;
	
	@Override
	public String toString() {
		return "[" +
		"joinField = " + joinTable + ", " +
		"]";
	}
	public JoinWrapper(JoinTable joinTable) {
		super();
		this.joinTable = joinTable;
	}
}
