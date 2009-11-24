package org.biomart.querying.queryRunner.prototype;

import java.util.List;

public class QueryRunnerPrototypeUtils {
	public static boolean stringEquals(String s1, String s2) {
		return s1!=null && s2!=null && s1.equals(s2);
	}
	public static boolean stringListEquals(List<String> l1, List<String> l2) {	// l1 & l2 are non-null and same size
		for (int i = 0; i < l1.size(); i++) {
			String s1 = l1.get(i);
			String s2 = l2.get(i);
			if (s1==null || s2==null || !s1.equals(s2)) {
				return false;
			}
		}
		return true;
	}
}
