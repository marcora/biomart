package org.biomart.common.general.utils;

public class DoubleString implements Comparable<DoubleString> {

	private String string1 = null;
	private String string2 = null;
	public DoubleString(String string1, String string2) {
		super();
		this.string1 = string1;
		this.string2 = string2;
	}
	public String getString1() {
		return string1;
	}
	public String getString2() {
		return string2;
	}
	@Override
	public boolean equals(Object arg0) {
		DoubleString doubleString = (DoubleString)arg0;
		return string1.equals(doubleString.string1) && string2.equals(doubleString.string2);
	}
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + (null == string1 ? 0 : string1.hashCode());
		hash = 31 * hash + (null == string2 ? 0 : string2.hashCode());
		return hash;
	}
	@Override
	public String toString() {
		return "string1 = " + string1 + ", string2 = " + string2;
	}
	public int compareTo(DoubleString doubleString) {
		int compare = string1.compareTo(doubleString.string1);
		if (compare!=0) {
			return compare;
		}
		return string2.compareTo(doubleString.string2);
	}
}
