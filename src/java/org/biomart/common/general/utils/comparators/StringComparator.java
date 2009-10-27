package org.biomart.common.general.utils.comparators;

import java.util.Comparator;

public class StringComparator implements Comparator<String> {	// String already implements Comparable<String>
	public int compare(String arg0, String arg1) {
		return arg0.compareTo(arg1);
	}
}
