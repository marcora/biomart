package org.biomart.transformation.helpers;

import java.util.Set;

import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.objects.Element;



public class PointerElementInfo{

	public static void main(String[] args) {}
	private String pointedDatasetName = null;
	private Element pointingElement = null;
	private String pointedElementName = null;
	private Boolean local = null;
	private Boolean attribute = null;
	private Set<String> plainPointerDatasetSet = null;

	public PointerElementInfo(String pointedDatasetName, Set<String> plainPointerDatasetSet, Element pointingElement, String pointedElementName, Boolean local, Boolean attribute) {
		super();
		this.pointedDatasetName = pointedDatasetName;
		this.plainPointerDatasetSet = plainPointerDatasetSet;
		this.pointingElement = pointingElement;
		this.pointedElementName = pointedElementName;
		this.local = local;
		this.attribute = attribute;
	}

	public Element getPointingElement() {
		return pointingElement;
	}

	public Boolean getLocal() {
		return local;
	}

	public Set<String> getPlainPointerDatasetSet() {
		return plainPointerDatasetSet;
	}

	public void setPointingElement(Element pointingElement) {
		this.pointingElement = pointingElement;
	}

	public void setLocal(Boolean local) {
		this.local = local;
	}

	@Override
	public String toString() {
		return 
			/*super.toString() + ", " +*/ 
			"pointingElement = " + pointingElement + ", " +
			"local = " + local;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		PointerElementInfo pointerElement=(PointerElementInfo)object;
		return (
			(this.pointingElement==pointerElement.pointingElement || (this.pointingElement!=null && pointingElement.equals(pointerElement.pointingElement))) &&
			(this.local==pointerElement.local || (this.local!=null && local.equals(pointerElement.local)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==pointingElement? 0 : pointingElement.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==local? 0 : local.hashCode());
		return hash;
	}

	public Boolean getAttribute() {
		return attribute;
	}

	public void setAttribute(Boolean attribute) {
		this.attribute = attribute;
	}

	public String getPointedDatasetName() {
		return pointedDatasetName;
	}

	public String getPointedElementName() {
		return pointedElementName;
	}

	/*@Override
	public int compare(PointerElement pointerElement1, PointerElement pointerElement2) {
		if (pointerElement1==null && pointerElement2!=null) {
			return -1;
		} else if (pointerElement1!=null && pointerElement2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(pointerElement1.pointingElement, pointerElement2.pointingElement);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(pointerElement1.pointedElement, pointerElement2.pointedElement);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(pointerElement1.local, pointerElement2.local);
	}

	@Override
	public int compareTo(PointerElement pointerElement) {
		return compare(this, pointerElement);
	}*/
}
