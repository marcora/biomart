package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldAttributeDescription extends OldAttribute /*implements Comparable<OldAttributeDescription>, Comparator<OldAttributeDescription>*/ {

	public static void main(String[] args) {}

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"imageURL",	// for "dspotatov01"
			"homepageURL",	// for "marker_symbol" in dataset "kermit" 
			/*"datasetLink", "imageURL"*/// For GenomicSequence only it seems, ignored then
	}));
	
	private List<OldSpecificAttributeContent> oldSpecificAttributeContentList = null;
	private List<OldEmptySpecificElementContent> oldEmptySpecificAttributeContentList = null;

	public OldAttributeDescription(Element jdomElement) throws FunctionalException {
		super(jdomElement);
		
		this.oldSpecificAttributeContentList = new ArrayList<OldSpecificAttributeContent>();
		this.oldEmptySpecificAttributeContentList = new ArrayList<OldEmptySpecificElementContent>();
		
		TransformationUtils.checkJdomElementProperties(jdomElement, OldElementPlaceHolder.propertyList,
				OldElement.propertyList, OldAttribute.propertyList, OldAttributeDescription.propertyList);
	}
	
	public void addOldEmptySpecificAttributeContent(OldEmptySpecificElementContent oldEmptySpecificFilterContent) {
		this.oldEmptySpecificAttributeContentList.add(oldEmptySpecificFilterContent);
	}
	
	public void addOldSpecificAttributeContent(OldSpecificAttributeContent oldSpecificAttributeContent) {
		this.oldSpecificAttributeContentList.add(oldSpecificAttributeContent);
	}
	
	public List<OldSpecificAttributeContent> getOldSpecificAttributeContentList() {
		return this.oldSpecificAttributeContentList;
	}

	public List<OldEmptySpecificElementContent> getOldEmptySpecificAttributeContentList() {
		return oldEmptySpecificAttributeContentList;
	}

	@Override
	public String toString() {
		int tabLevel = TransformationConstants.ELEMENT_LEVEL1_TAB_LEVEL;
		String node = 
			super.toString();
		
		StringBuffer sb = TransformationUtils.displayNode(tabLevel, node);
		TransformationUtils.displayChildren(this.oldSpecificAttributeContentList, tabLevel, sb, "oldSpecificAttributeContentList");
		
		return sb.toString();
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		OldAttributeDescription oldAttributeDescription=(OldAttributeDescription)object;
		return (
			(this.oldSpecificAttributeContentList==oldAttributeDescription.oldSpecificAttributeContentList || (this.oldSpecificAttributeContentList!=null && oldSpecificAttributeContentList.equals(oldAttributeDescription.oldSpecificAttributeContentList)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldSpecificAttributeContentList? 0 : oldSpecificAttributeContentList.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldAttribute oldAttribute1, OldAttribute oldAttribute2) {
		if (oldAttribute1==null && oldAttribute2!=null) {
			return -1;
		} else if (oldAttribute1!=null && oldAttribute2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(oldAttribute1.source, oldAttribute2.source);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldAttribute1.linkoutURL, oldAttribute2.linkoutURL);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldAttribute1.maxLength, oldAttribute2.maxLength);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldAttribute1.default, oldAttribute2.default);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(oldAttribute1.oldSpecificAttributeContentList, oldAttribute2.oldSpecificAttributeContentList);
	}

	@Override
	public int compareTo(OldAttribute oldAttribute) {
		return compare(this, oldAttribute);
	}*/
	
	/*@Override
	public Attribute transform(CurrentPath currentPath) {
		return super.transform(currentPath);
	}*/
	
	/*@Override
	public void update(Attribute attribute) {
		//Attribute attribute = new Attribute(this.internalName, this.displayName, this.description, !this.hideDisplay, locationName, martName, version, datasetName, configName, tableName, fieldName, targetRangeList, selectedByDefault, pointer, pointedElementName, sourceRangeList, maxLength, linkURL)
		Attribute attribute = new Attribute(this.internalName, this.displayName, this.description, !this.hideDisplay, 
				null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		
		
	}*/
}