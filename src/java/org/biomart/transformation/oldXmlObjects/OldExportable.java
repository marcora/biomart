package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldExportable extends OldPortable /*implements Comparable<OldExportable>, Comparator<OldExportable>*/ {

	public static void main(String[] args) {}

	private Boolean default_ = null;
	private List<String> attributes = null;

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"attributes", 
			"displayName"	
	}));
	
	public OldExportable(Element jdomDatasetConfig) throws FunctionalException {
		this(jdomDatasetConfig, 
				jdomDatasetConfig.getAttributeValue("default"),
				jdomDatasetConfig.getAttributeValue("attributes")
		);
	}
	private OldExportable(Element jdomDatasetConfig, String default_, String attributes) throws FunctionalException {		
		super(jdomDatasetConfig);
		
		this.default_ = TransformationUtils.getBooleanValueFromString(default_, "default");
		String[] split = attributes.split(MartServiceConstants.ELEMENT_SEPARATOR);	// Can't be empty here
		this.attributes = new ArrayList<String>(Arrays.asList(split));

		TransformationUtils.checkJdomElementProperties(jdomElement, OldPortable.propertyList, OldExportable.propertyList);
	}

	public Boolean getDefault_() {
		return default_;
	}

	public void setDefault_(Boolean default_) {
		this.default_ = default_;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " +  
			"default = " + default_ + ", " + 
			"attributes = " + attributes;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		OldExportable oldExportable=(OldExportable)object;
		return (
			(this.default_==oldExportable.default_ || (this.default_!=null && default_.equals(oldExportable.default_)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==default_? 0 : default_.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldExportable oldExportable1, OldExportable oldExportable2) {
		if (oldExportable1==null && oldExportable2!=null) {
			return -1;
		} else if (oldExportable1!=null && oldExportable2==null) {
			return 1;
		}
		return CompareUtils.compareNull(oldExportable1.default_, oldExportable2.default_);
	}

	@Override
	public int compareTo(OldExportable oldExportable) {
		return compare(this, oldExportable);
	}*/
	
	public List<String> getAttributes() {
		return attributes;
	}

}