package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.jdom.Element;


public class OldFilterContainer extends OldContainer /*implements Comparable<OldFilterContainer>, Comparator<OldFilterContainer>*/ {

	public static void main(String[] args) {}

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
	}));
	
	protected OldFilterContainer(Element jdomElement, Integer level) throws FunctionalException {
		super(jdomElement, level);
	}

	@Override
	public String toString() {
		return 
			"";
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		OldFilterContainer oldFilterContainer=(OldFilterContainer)object;
		return (true);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		return hash;
	}

	/*@Override
	public int compare(OldFilterContainer oldFilterContainer1, OldFilterContainer oldFilterContainer2) {
		if (oldFilterContainer1==null && oldFilterContainer2!=null) {
			return -1;
		} else if (oldFilterContainer1!=null && oldFilterContainer2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(oldFilterContainer1.oldFilterContainerList, oldFilterContainer2.oldFilterContainerList);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(oldFilterContainer1.oldFilterList, oldFilterContainer2.oldFilterList);
	}

	@Override
	public int compareTo(OldFilterContainer oldFilterContainer) {
		return compare(this, oldFilterContainer);
	}*/

}