package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.jdom.Element;


public class OldAttributeContainer extends OldContainer /*implements Comparable<OldAttributeContainer>, Comparator<OldAttributeContainer>*/ {

	public static void main(String[] args) {}

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
	}));
	
	protected OldAttributeContainer(Element jdomElement, Integer level) throws FunctionalException {
		super(jdomElement, level);	
	}

	@Override
	public String toString() {
		return 
			super.toString();
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		OldAttributeContainer oldAttributeContainer=(OldAttributeContainer)object;
		return (
				true
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		return hash;
	}

	/*@Override
	public int compare(OldAttributeContainer oldAttributeContainer1, OldAttributeContainer oldAttributeContainer2) {
		if (oldAttributeContainer1==null && oldAttributeContainer2!=null) {
			return -1;
		} else if (oldAttributeContainer1!=null && oldAttributeContainer2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(oldAttributeContainer1.oldAttributeContainerList, oldAttributeContainer2.oldAttributeContainerList);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(oldAttributeContainer1.oldAttributeList, oldAttributeContainer2.oldAttributeList);
	}

	@Override
	public int compareTo(OldAttributeContainer oldAttributeContainer) {
		return compare(this, oldAttributeContainer);
	}*/

}