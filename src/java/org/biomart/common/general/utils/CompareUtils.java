package org.biomart.common.general.utils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.biomart.common.general.utils.comparators.StringComparator;

public class CompareUtils {
	
	public static void main(String[] args) {
		List<String> l1 = new ArrayList<String>();
		List<String> l2 = new ArrayList<String>();
		
		compareList(l1, l2, new Comparator<String>() {
		    public int compare(String s1, String s2) {
		        return s1.compareTo(s2);
		    }
		});
		compareList(l1, l2, new StringComparator());
	}
	
	/**
	 * TODO join compareString & compareInteger as generic
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static int compareString(String o1, String o2) {
		if (o1==null && o2!=null) {
			return -1;
		} else if (o1!=null && o2==null) {
			return 1;
		} else if (o1==null && o2==null) {
			return 0;
		} else {
			return o1.compareTo(o2);
		}
	}
	public static int compareInteger(Integer o1, Integer o2) {
		if (o1==null && o2!=null) {
			return -1;
		} else if (o1!=null && o2==null) {
			return 1;
		} else if (o1==null && o2==null) {
			return 0;
		} else {
			return o1.compareTo(o2);
		}
	}
	public static int compareBoolean(Boolean o1, Boolean o2) {
		if (o1==null && o2!=null) {
			return -1;
		} else if (o1!=null && o2==null) {
			return 1;
		} else if (o1==null && o2==null) {
			return 0;
		} else {
			return o1.compareTo(o2);
		}
	}
	
	
	public static boolean bothNull (Object o1, Object o2) {
		return o1==null && o2==null;
	}
	public static int compareNull (Object o1, Object o2) {
		if (o1==null && o2!=null) {
			return -1;
		} else if (o1!=null && o2==null) {
			return 1;
		} else {
			return 0;
		}
	}
	/*public static int compareSize (Object[] t1, Object[] t2) {
		if (t1.length<t2.length) {
			return -1;
		} else if (t1.length>t2.length) {
			return 1;
		} else {
			return 0;
		}
	}*/
	public static int compareSize (Collection l1, Collection l2) {/*Collection<?> l1, Collection<?> l2)*/
		if (l1.size()<l2.size()) {
			return -1;
		} else if (l1.size()>l2.size()) {
			return 1;
		} else {
			return 0;
		}
	}
	public static<T,V> int compareMapSize (Map<? extends T,? extends V> l1, Map<? extends T,? extends V> l2) {
		if (l1.size()<l2.size()) {
			return -1;
		} else if (l1.size()>l2.size()) {
			return 1;
		} else {
			return 0;
		}
	}
	public static<T> int compareList (List<? extends T> l1, List<? extends T> l2, Comparator<T> comparator) {
		if (bothNull(l1, l2)) {
			return 0;
		}
		int compare = compareNull(l1, l2);
		if (compare!=0) {
			return compare;
		}
		compare = compareSize(l1, l2);
		if (compare!=0) {
			return compare;
		}
		for (int i = 0; i < l1.size(); i++) {
			compare = comparator.compare(l1.get(i), l2.get(i));
			if (compare!=0) {
				return compare;
			}
		}
		return 0;
	}
	public static int compareIgnoreCaseStringList (List<String> l1, List<String> l2) {
		if (bothNull(l1, l2)) {
			return 0;
		}
		int compare = compareNull(l1, l2);
		if (compare!=0) {
			return compare;
		}
		compare = compareSize(l1, l2);
		if (compare!=0) {
			return compare;
		}
		for (int i = 0; i < l1.size(); i++) {
			compare = l1.get(i).compareToIgnoreCase(l2.get(i));
			if (compare!=0) {
				return compare;
			}
		}
		return 0;
	}
	public static int compareStringList (List<String> l1, List<String> l2) {
		if (bothNull(l1, l2)) {
			return 0;
		}
		int compare = compareNull(l1, l2);
		if (compare!=0) {
			return compare;
		}
		compare = compareSize(l1, l2);
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
	public static int compareListStringList (List<List<String>> l1, List<List<String>> l2) {
		if (bothNull(l1, l2)) {
			return 0;
		}
		int compare = compareNull(l1, l2);
		if (compare!=0) {
			return compare;
		}
		compare = compareSize(l1, l2);
		if (compare!=0) {
			return compare;
		}
		for (int i = 0; i < l1.size(); i++) {
			compare = compareStringList(l1.get(i), l2.get(i));
			if (compare!=0) {
				return compare;
			}
		}
		return 0;
	}
	/*public static<T,V> int compareMap (Map<? extends T, ? extends V> l1, Map<? extends T, ? extends V> l2) {
		if (bothNull(l1, l2)) {
			return 0;
		}
		int compare = compareNull(l1, l2);
		if (compare!=0) {
			return compare;
		}
		compare = compareMapSize(l1, l2);
		if (compare!=0) {
			return compare;
		}
		for (int i = 0; i < l1.size(); i++) {
			compare = compareStringList(l1.get(i), l2.get(i));
			if (compare!=0) {
				return compare;
			}
		}
		return 0;
	}*/

	public static boolean same(Boolean s1, Boolean s2) {
		if (s1==null && s2==null) {
			return true;
		} else if (s1!=null && s2==null) {
			return false;
		} else if (s1==null && s2!=null) {
			return false;
		} else {
			return s1.equals(s2);
		}
	}

	public static boolean same(String s1, String s2) {
		if (s1==null && s2==null) {
			return true;
		} else if (s1!=null && s2==null) {
			return false;
		} else if (s1==null && s2!=null) {
			return false;
		} else {
			return s1.equals(s2);
		}
	}

	public static boolean same(Integer n1, Integer n2) {
		if (n1==null && n2==null) {
			return true;
		} else if (n1!=null && n2==null) {
			return false;
		} else if (n1==null && n2!=null) {
			return false;
		} else {
			return n1.intValue()==n2.intValue();
		}
	}

	public static boolean same(List<String> l1, List<String> l2) {
		if (l1==null && l2==null) {
			return true;
		} else if (l1!=null && l2==null) {
			return false;
		} else if (l1==null && l2!=null) {
			return false;
		} else if (l1.size()!=l2.size()) {
			return false;
		} else {
			for (int i = 0; i < l1.size(); i++) {
				if (!same(l1.get(i), l2.get(i))) {
					return false;
				}
			}
			return true;
		}
	}
}
