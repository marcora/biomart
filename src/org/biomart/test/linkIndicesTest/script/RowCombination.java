package org.biomart.test.linkIndicesTest.script;

class RowCombination {
	private Integer mainRow = null;
	private Integer dimensionRowForMainRow = null;
	public RowCombination(Integer mainRow, Integer dimensionRowForMainRow) {
		super();
		this.mainRow = mainRow;
		this.dimensionRowForMainRow = dimensionRowForMainRow;
	}
	@Override
	public boolean equals(Object obj) {
		return this.mainRow.intValue()==((RowCombination)obj).mainRow.intValue() && 
		this.dimensionRowForMainRow.intValue()==((RowCombination)obj).dimensionRowForMainRow.intValue();
	}
	@Override
	public int hashCode() {
		return 0;
	}
	@Override
	public String toString() {
		return "[mainRow = " + mainRow + ", dimensionRowForMainRow = " + dimensionRowForMainRow + "]";
	}
}