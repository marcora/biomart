package org.biomart.old.bioMartPortalLinks;

public class DatasetToLink implements Comparable<DatasetToLink> {
	String virtualSchema = null;
	String mart = null;
	String dataset = null;
	public DatasetToLink(String virtualSchema, String mart, String dataset) {
		super();
		this.virtualSchema = virtualSchema;
		this.mart = mart;
		this.dataset = dataset;
	}
	@Override
	public boolean equals(Object arg0) {
		DatasetToLink data = (DatasetToLink)arg0;
		
		/*System.out.println("in equal");
		System.out.println(this);
		System.out.println(data);
		System.out.println(virtualSchema.equalsIgnoreCase(data.virtualSchema) &&
		mart.equalsIgnoreCase(data.mart) &&
		dataset.equalsIgnoreCase(data.dataset));*/
		
		return 
		virtualSchema.equalsIgnoreCase(data.virtualSchema) &&
		mart.equalsIgnoreCase(data.mart) &&
		dataset.equalsIgnoreCase(data.dataset);
	}
	@Override
	public String toString() {
		return 
		"{Dataset = " + dataset + ",\t" +
		"Mart = " + mart + ",\t" +
		"Virtual Schema = " + virtualSchema + "}";
	}
	public int compareTo(DatasetToLink link) {
		//System.out.print("^");
		int compare = this.virtualSchema.compareTo(link.virtualSchema);
		if (compare!=0) {
			return compare;
		}
		compare = this.mart.compareTo(link.mart);
		if (compare!=0) {
			return compare;
		}
		return this.dataset.compareTo(link.dataset);
	}
	@Override
	public int hashCode() {
		//System.out.println("IN THE HASH!!!");
		return 0;
	}
}
