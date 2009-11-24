package org.biomart.test.linkIndicesTest.program;

public class DataSource {
	private Boolean doBatch = null;	// not implemented yet
	private Boolean isLinkIndex = null;
	private String linkTableName = null;
	private String linkTableKey = null;

	protected DataSource () {
		this.doBatch = true;
		this.isLinkIndex = false;
	}
	public void setDoBatch(boolean doBatch) {
		this.doBatch = doBatch;
	}
	public void setLinkIndex(String linkTableName, String linkTableKey) {
		this.isLinkIndex = linkTableName!=null && linkTableKey!=null;
		this.linkTableName = linkTableName;
		this.linkTableKey = linkTableKey;
	}

	public Boolean getIsLinkIndex() {
		return isLinkIndex;
	}

	public String getLinkTableName() {
		return linkTableName;
	}

	@Override
	public String toString() {
		return "isLinkIndex = " + isLinkIndex + (isLinkIndex ? ", linkTableName = " + linkTableName + ", linkTableKey = " + linkTableKey : "");
	}

	public String getLinkTableKey() {
		return linkTableKey;
	}
	public Boolean getDoBatch() {
		return doBatch;
	}
}
