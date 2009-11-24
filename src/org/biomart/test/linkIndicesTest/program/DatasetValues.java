package org.biomart.test.linkIndicesTest.program;

import java.io.Serializable;
import java.util.List;

public class DatasetValues implements Serializable {
	private static final long serialVersionUID = 3812648512573728625L;
	String joinFieldValueLeft = null;
	String joinFieldValueRight = null;
	List<String> fieldValues = null;
	@Override
	public boolean equals(Object arg0) {
		DatasetValues datasetValues = (DatasetValues)arg0;
		boolean equal =
			datasetValues!=null && 
			((this.fieldValues==null && datasetValues.fieldValues==null) || datasetValues.fieldValues!=null) &&
			this.joinFieldValueLeft.equals(datasetValues.joinFieldValueLeft) &&
			this.joinFieldValueRight.equals(datasetValues.joinFieldValueRight);
		if (!equal) {
			return false;
		}
		for (int i = 0; i < this.fieldValues.size(); i++) {
			if (!this.fieldValues.get(i).equals(datasetValues.fieldValues.get(i))) {
				return false;
			}                                                       
		}
		return true;
	}
	public DatasetValues(String joinFieldValueLeft, String joinFieldValueRight) {
		super();
		this.joinFieldValueLeft = joinFieldValueLeft;
		this.joinFieldValueRight = joinFieldValueRight;
		this.fieldValues = null;
	}
	public DatasetValues(String joinFieldValueLeft, String joinFieldValueRight, List<String> fieldValues) {
		super();
		this.joinFieldValueLeft = joinFieldValueLeft;
		this.joinFieldValueRight = joinFieldValueRight;
		this.fieldValues = fieldValues;
	}
	@Override
	public String toString() {
		return "[" + 
		"joinFieldValueLeft = " + joinFieldValueLeft + ", " +
		"joinFieldValueRight = " + joinFieldValueRight + ", " +
		"fieldValues = " + fieldValues
		+ "]";
	}
}
