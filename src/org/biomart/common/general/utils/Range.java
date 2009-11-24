package org.biomart.common.general.utils;

public class Range {
	public Integer lower = null;	// inclusive
	public Integer upper = null;	// inclusive
	public Integer size = null;
	@Override
	public String toString() {
		return "[lower = " + lower + ", upper = " + upper + ", size = " + size + "]";
	}
	public String toShortStringBracket() {
		return "[" + lower + "-" + upper +"]";
	}
	public String toShortStringUnderscore() {
		return lower + "to" + upper;
	}
	public Range(Integer lower, Integer upper) {
		super();
		this.lower = lower;
		this.upper = upper;
		this.size = upper-lower+1;	// inclusive
	}
	public boolean isInRange(int element) {
		return element>=lower && element<=upper;
	}
	public int getSize() {
		return this.size;
	}
	public boolean isValidRangeFor(int totalRow) {
		return upper>=lower && lower>=0 && lower<totalRow && upper>=0 && upper<totalRow;
	}
	public static int getTotalSize(Range[] ranges) {
		int sum = 0;
		if (null!=ranges) {
			for (int i = 0; i < ranges.length; i++) {
				sum+=ranges[i].size;
			}
		}
		return sum;
	}
	public static String toShortStringBracket(Range[] ranges) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("[");
		if (null!=ranges) {
			for (int i = 0; i < ranges.length; i++) {
				stringBuilder.append(ranges[i].toShortStringBracket());
			}
		}
		stringBuilder.append("]");
		return stringBuilder.toString();
	}
	public static String toShortStringUnderscore(Range[] ranges) {
		StringBuilder stringBuilder = new StringBuilder();
		if (null!=ranges) {
			for (int i = 0; i < ranges.length; i++) {
				stringBuilder.append(ranges[i].toShortStringUnderscore());
				if (i!=ranges.length-1) {
					stringBuilder.append("u");					
				}
			}
		} else {
			stringBuilder.append("none");		
		}
		return stringBuilder.toString();
	}
	public static String toShortStringSize(Range[] ranges) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(null!=ranges ? ranges.length + "ranges" : "none");		
		return stringBuilder.toString();
	}
}
