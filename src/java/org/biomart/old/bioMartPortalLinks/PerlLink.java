package org.biomart.old.bioMartPortalLinks;

import org.biomart.common.general.utils.MyUtils;

public class PerlLink implements Comparable<PerlLink> {
	Integer number = null;
	String virtualSchema = null;
	String location = null;
	String sourceDatasetName = null;
	String targetDatasetName = null;
	String linkName = null;
	public PerlLink(String number, String virtualSchema, String location, String sourceDatasetName, String targetDatasetName, String linkName) {
		super();
		this.number = number!=null ? Integer.valueOf(number) : null;
		this.virtualSchema = virtualSchema;
		this.location = location;
		this.sourceDatasetName = sourceDatasetName;
		this.targetDatasetName = targetDatasetName;
		this.linkName = linkName;
	}
	@Override
	public boolean equals(Object arg0) {
		PerlLink data = (PerlLink)arg0;
		return 
		// Ignore number
		virtualSchema.equalsIgnoreCase(data.virtualSchema) &&
		location.equalsIgnoreCase(data.location) &&
		sourceDatasetName.equalsIgnoreCase(data.sourceDatasetName) &&
		targetDatasetName.equalsIgnoreCase(data.targetDatasetName) &&
		linkName.equalsIgnoreCase(data.linkName);
	}
	public boolean similar(Object arg0) {
		PerlLink data = (PerlLink)arg0;
		return 
		// Ignore number
		virtualSchema.equalsIgnoreCase(data.virtualSchema) &&
		location.equalsIgnoreCase(data.location) &&
		sourceDatasetName.equalsIgnoreCase(data.sourceDatasetName) &&
		targetDatasetName.equalsIgnoreCase(data.targetDatasetName);
	}
	@Override
	public String toString() {
		return "n=" + number + ", vs=" + virtualSchema + ",loc=" + location + ",src="+sourceDatasetName + ",trg=" + targetDatasetName + ",name=" + linkName;
	}
	public String toShortString2() {
		return
		virtualSchema + MyUtils.TAB_SEPARATOR + sourceDatasetName + MyUtils.TAB_SEPARATOR + targetDatasetName + MyUtils.TAB_SEPARATOR + linkName + 
		/*MyUtils.TAB_SEPARATOR + biDirectional + MyUtils.TAB_SEPARATOR + bothSideVisible + */MyUtils.TAB_SEPARATOR + null;
	}
	public String toPerlOutputString() {
		return number + MyUtils.TAB_SEPARATOR + virtualSchema + MyUtils.TAB_SEPARATOR + location + MyUtils.TAB_SEPARATOR + sourceDatasetName + 
		MyUtils.TAB_SEPARATOR + targetDatasetName + MyUtils.TAB_SEPARATOR + linkName;
	}
	public int compareTo(PerlLink link) {
		int compare = this.virtualSchema.compareTo(link.virtualSchema);
		if (compare!=0) {
			return compare;
		}
		compare = this.location.compareTo(link.location);
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
		return this.linkName.compareTo(link.linkName);
	}
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + virtualSchema.hashCode();
		hash = 31 * hash + location.hashCode();
		hash = 31 * hash + sourceDatasetName.hashCode();
		hash = 31 * hash + targetDatasetName.hashCode();
		return hash;
	}
}