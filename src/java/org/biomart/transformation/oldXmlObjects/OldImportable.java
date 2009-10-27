package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldImportable extends OldPortable /*implements Comparable<OldImportable>, Comparator<OldImportable>*/ {

	public static void main(String[] args) {}
	
	private List<String> filters = null;

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"filters",
			"displayName"	// for "gene" dataset
	}));
	
	public OldImportable(Element jdomDatasetConfig) throws FunctionalException {
		this(jdomDatasetConfig, 
				jdomDatasetConfig.getAttributeValue("filters")
		);
	}
	private OldImportable(Element jdomElement, String filters) throws FunctionalException {
		super(jdomElement);
		
		String[] split = filters.split(MartServiceConstants.ELEMENT_SEPARATOR);	// Can't be empty here
		this.filters = new ArrayList<String>(Arrays.asList(split));
		
		TransformationUtils.checkJdomElementProperties(jdomElement, OldPortable.propertyList, OldImportable.propertyList);
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " +  
			"filters = " + filters;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		OldImportable oldImportable=(OldImportable)object;
		return (
		true
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		return hash;
	}
	public List<String> getFilters() {
		return filters;
	}

	/*@Override
	public int compare(OldImportable oldImportable1, OldImportable oldImportable2) {
		if (oldImportable1==null && oldImportable2!=null) {
			return -1;
		} else if (oldImportable1!=null && oldImportable2==null) {
			return 1;
		}
		return 0;
	}

	@Override
	public int compareTo(OldImportable oldImportable) {
		return compare(this, oldImportable);
	}*/

}