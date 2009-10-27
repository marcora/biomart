package org.biomart.common.general.utils;

import java.util.TreeSet;

public class CollectionsUtils {
	
	public static TreeSet<String> mergeTreeSetString(TreeSet<String> treeSetLeft, TreeSet<String> treeSetRight) {
		TreeSet<String> treeSetLinkIndex = null;
		if (treeSetLeft.size()<treeSetRight.size()) {	// So we copy the smallest
			treeSetLinkIndex = new TreeSet<String>(treeSetLeft);	
			treeSetLinkIndex.retainAll(treeSetRight);
		} else {
			treeSetLinkIndex = new TreeSet<String>(treeSetRight);	
			treeSetLinkIndex.retainAll(treeSetLeft);
		}
		return treeSetLinkIndex;
	}
}
