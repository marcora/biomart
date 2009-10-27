package org.biomart.common.general.utils.comparators;


import java.util.Comparator;
import java.util.List;

import org.biomart.common.general.utils.CompareUtils;

public class StringListComparison implements Comparator<List<String>>, Comparable<List<String>> {

	public int compare(List<String> l1, List<String> l2) {
		int compare = CompareUtils.compareSize(l1, l2);
		if (compare!=0) {
			return compare;
		}
		for (int i = 0; i < l1.size(); i++) {
			compare = l1.get(i).compareTo(l2.get(i));
			if (compare!=0) {
				return compare;
			}
		}
		return 0;
	}

	@Deprecated
	public int compareTo(List<String> arg0) {
		return 0;//TODO
	}
}
