package org.biomart.objects.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import org.biomart.objects.MartConfiguratorConstants;
import org.jdom.Element;


public class TreeFilterDataRow implements Serializable /*implements Comparable<TreeFilterDataRow>, Comparator<TreeFilterDataRow> */{

	private static final long serialVersionUID = 810814640524263047L;

	public static void main(String[] args) {}

	private filterDataRow listFilterDataRow = null;
	private List<TreeFilterDataRow> children = null;

	public TreeFilterDataRow(filterDataRow listFilterDataRow) {
		super();
		this.listFilterDataRow = listFilterDataRow;
		this.children = new ArrayList<TreeFilterDataRow>();
	}
	
	public void addChild(TreeFilterDataRow treeFilterDataRow) {
		this.children.add(treeFilterDataRow);
	}

	public filterDataRow getListFilterDataRow() {
		return listFilterDataRow;
	}

	public List<TreeFilterDataRow> getChildren() {
		return children;
	}

	public void setListFilterDataRow(filterDataRow listFilterDataRow) {
		this.listFilterDataRow = listFilterDataRow;
	}

	public void setChildren(List<TreeFilterDataRow> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"listFilterDataRow = " + listFilterDataRow + ", " +
			"children = " + children;
	}

	public Element generateXml() {
		Element listFilterDataRowElement = listFilterDataRow.generateXml();
		for (TreeFilterDataRow row : this.children) {
			listFilterDataRowElement.addContent(row.generateXml());
		}
		return listFilterDataRowElement;
	}
	
	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		TreeFilterDataRow treeFilterDataRow=(TreeFilterDataRow)object;
		return (
			(this.listFilterDataRow==treeFilterDataRow.listFilterDataRow || (this.listFilterDataRow!=null && listFilterDataRow.equals(treeFilterDataRow.listFilterDataRow))) &&
			(this.children==treeFilterDataRow.children || (this.children!=null && children.equals(treeFilterDataRow.children)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==listFilterDataRow? 0 : listFilterDataRow.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==children? 0 : children.hashCode());
		return hash;
	}

	/*@Override
	public int compare(TreeFilterDataRow treeFilterDataRow1, TreeFilterDataRow treeFilterDataRow2) {
		if (treeFilterDataRow1==null && treeFilterDataRow2!=null) {
			return -1;
		} else if (treeFilterDataRow1!=null && treeFilterDataRow2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(treeFilterDataRow1.listFilterDataRow, treeFilterDataRow2.listFilterDataRow);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(treeFilterDataRow1.children, treeFilterDataRow2.children);
	}

	@Override
	public int compareTo(TreeFilterDataRow treeFilterDataRow) {
		return compare(this, treeFilterDataRow);
	}*/

}
