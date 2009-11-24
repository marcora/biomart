
package org.biomart.old.bioMartPortalLinks;

import org.biomart.common.general.utils.MyUtils;

public class PerlLink2 implements Comparable<PerlLink2> {
	String virtualSchema = null;
	String sourceDatasetName = null;
	String targetDatasetName = null;
	String linkName = null;
	Integer hashProblem = null;
	public static String getHeader() {
		return "virtualSchema" + MyUtils.TAB_SEPARATOR + "sourceDatasetName" + MyUtils.TAB_SEPARATOR + "targetDatasetName" + 
		MyUtils.TAB_SEPARATOR + "linkName" + MyUtils.TAB_SEPARATOR + "hashProblem";
	}
	public PerlLink2(String virtualSchema, String sourceDatasetName, String targetDatasetName, String linkName, Integer hashProblem) {
		super();
		this.virtualSchema = virtualSchema;
		this.sourceDatasetName = sourceDatasetName;
		this.targetDatasetName = targetDatasetName;
		this.linkName = linkName;
		this.hashProblem = hashProblem;
	}
	@Override
	public boolean equals(Object arg0) {
		PerlLink2 data = (PerlLink2)arg0;
		return 
		// Ignore number
		virtualSchema.equalsIgnoreCase(data.virtualSchema) &&
		sourceDatasetName.equalsIgnoreCase(data.sourceDatasetName) &&
		targetDatasetName.equalsIgnoreCase(data.targetDatasetName) &&
		linkName.equalsIgnoreCase(data.linkName) &&
		hashProblem.intValue()==data.hashProblem.intValue();
	}
	public boolean similar(Object arg0) {
		PerlLink2 data = (PerlLink2)arg0;
		return 
		// Ignore number
		virtualSchema.equalsIgnoreCase(data.virtualSchema) &&
		sourceDatasetName.equalsIgnoreCase(data.sourceDatasetName) &&
		targetDatasetName.equalsIgnoreCase(data.targetDatasetName);
	}
	public String toShortString2() {
		return
		virtualSchema + MyUtils.TAB_SEPARATOR + sourceDatasetName + MyUtils.TAB_SEPARATOR + targetDatasetName + MyUtils.TAB_SEPARATOR + linkName + 
		/*MyUtils.TAB_SEPARATOR + biDirectional + MyUtils.TAB_SEPARATOR + bothSideVisible + */MyUtils.TAB_SEPARATOR + hashProblem;
	}
	public int compareTo(PerlLink2 link) {
		int compare = this.virtualSchema.compareTo(link.virtualSchema);
		if (compare!=0) {
			return compare;
		}
		compare = this.sourceDatasetName.compareTo(link.sourceDatasetName);
		if (compare!=0) {
			return compare;
		}
		compare = this.targetDatasetName.compareTo(link.targetDatasetName);
		if (compare!=0) {
			return compare;
		}
		compare = this.linkName.compareTo(link.linkName);
		if (compare!=0) {
			return compare;
		}
		return this.hashProblem.compareTo(link.hashProblem);
	}
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + virtualSchema.hashCode();
		hash = 31 * hash + sourceDatasetName.hashCode();
		hash = 31 * hash + targetDatasetName.hashCode();
		hash = 31 * hash + linkName.hashCode();
		hash = 31 * hash + hashProblem.hashCode();
		return hash;
	}
}
