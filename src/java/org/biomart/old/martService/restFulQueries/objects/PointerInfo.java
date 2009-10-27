package org.biomart.old.martService.restFulQueries.objects;

import java.io.Serializable;
import java.util.Comparator;

public class PointerInfo implements Comparable<PointerInfo>, Comparator<PointerInfo>, Serializable {

	private static final long serialVersionUID = -3653310252645035613L;
	
	public String pointerDatasetName = null;
	public String pointerElementName = null;
	public String pointerInterface = null;
		
	public PointerInfo() {}	// for comparator only
	public PointerInfo(String pointerDatasetName, String pointerElementName, String pointerInterface) {
		super();
		this.pointerDatasetName = pointerDatasetName;
		this.pointerElementName = pointerElementName;
		this.pointerInterface = pointerInterface;
	}
	public boolean isValid() {
		return this.pointerDatasetName!=null && this.pointerElementName!=null && this.pointerInterface!=null;
	}
	public String getPointerDatasetName() {
		return pointerDatasetName;
	}
	public String getPointerElementName() {
		return pointerElementName;
	}
	public String getPointerInterface() {
		return pointerInterface;
	}
	@Override
	public boolean equals(Object arg0) {
		PointerInfo pointerInfo = (PointerInfo)arg0;
		return pointerDatasetName.equalsIgnoreCase(pointerInfo.pointerDatasetName) &&
		pointerElementName.equalsIgnoreCase(pointerInfo.pointerElementName) &&
		pointerInterface.equalsIgnoreCase(pointerInfo.pointerInterface);
	}
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + pointerDatasetName.hashCode();
		hash = 31 * hash + pointerElementName.hashCode();
		hash = 31 * hash + pointerInterface.hashCode();
		return hash;
	}
	@Override
	public String toString() {
		return "pointerDatasetName = " + pointerDatasetName + ", pointerElementName = " + pointerElementName + ", pointerInterface = " + pointerInterface;
	}
	public int compareTo(PointerInfo pointerInfo) {
		int compare = pointerDatasetName.compareToIgnoreCase(pointerInfo.pointerDatasetName);
		if (compare!=0) {
			return compare;
		}
		compare = pointerElementName.compareToIgnoreCase(pointerInfo.pointerElementName);
		if (compare!=0) {
			return compare;
		}
		return pointerInterface.compareToIgnoreCase(pointerInfo.pointerInterface);
	}
	public int compare(PointerInfo arg0, PointerInfo arg1) {
		return arg0.compareTo(arg1);
	}
}