package org.biomart.old.bioMartPortalLinks;

public class DatasetToLink2 implements Comparable<DatasetToLink2> {
	String linkName = null;
	String sourceDataset = null;
	String targetDataset = null;
	String sourceMart = null;
	String targetMart = null;
	public DatasetToLink2(String virtualSchema, String sourceDataset, String targetDataset, String sourceMart, String targetMart) {
		super();
		this.linkName = virtualSchema;
		this.sourceDataset = sourceDataset;
		this.targetDataset = targetDataset;
		this.sourceMart = sourceMart;
		this.targetMart = targetMart;
	}
	@Override
	public boolean equals(Object arg0) {
		DatasetToLink2 data = (DatasetToLink2)arg0;
		return 
		linkName.equalsIgnoreCase(data.linkName) &&
		sourceDataset.equalsIgnoreCase(data.sourceDataset) &&
		targetDataset.equalsIgnoreCase(data.targetDataset) &&
		sourceMart.equalsIgnoreCase(data.sourceMart) &&
		targetMart.equalsIgnoreCase(data.targetMart);
	}
	@Override
	public String toString() {
		return 
		linkName + "\t" +
		sourceDataset + "\t" + 
		targetDataset + "\t" +
		sourceMart + "\t" + 
		targetMart;
	}
	public int compareTo(DatasetToLink2 link) {
		int compare = this.linkName.compareTo(link.linkName);
		if (compare!=0) {
			return compare;
		}
		compare = this.sourceDataset.compareTo(link.sourceDataset);
		if (compare!=0) {
			return compare;
		}
		compare = this.targetDataset.compareTo(link.targetDataset);
		if (compare!=0) {
			return compare;
		}
		compare = this.sourceMart.compareTo(link.sourceMart);
		if (compare!=0) {
			return compare;
		}
		return this.targetMart.compareTo(link.targetMart);
	}
	@Override
	public int hashCode() {
		System.out.println("IN THE HASH!!!");
		return 0;
	}
}
