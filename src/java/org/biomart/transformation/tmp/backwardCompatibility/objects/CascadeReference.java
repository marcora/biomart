package org.biomart.transformation.tmp.backwardCompatibility.objects;

public class CascadeReference {

	private String cascadeParent = null;
	private String orderBy = null;
	public CascadeReference(String cascadeParent, String orderBy) {
		super();
		this.cascadeParent = cascadeParent!=null ? cascadeParent : "";
		this.orderBy = orderBy!=null ? orderBy : "";
	}
	@Override
	public boolean equals(Object arg0) {
		CascadeReference cascadeReference = (CascadeReference)arg0;
		return cascadeParent.equals(cascadeReference.cascadeParent) && orderBy.equals(cascadeReference.orderBy);
	}
	public String getOrderBy() {
		return orderBy;
	}
	public String getCascadeParent() {
		return cascadeParent;
	}
}
