package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldFilterDescription extends OldFilter /*implements Comparable<OldFilterDescription>, Comparator<OldFilterDescription>*/ {

	public static void main(String[] args) {}

	private List<OldOptionFilter> oldOptionFilterList = null;
	private List<OldSpecificFilterContent> oldSpecificFilterContentList = null;
	private List<OldEmptySpecificElementContent> oldEmptySpecificFilterContentList = null;
	private List<OldOptionValue> oldOptionValueList = null;

	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"value",	// for "primary_features" in dataset "gene"
			"defaultOn",	// for "unique_pep_filter" in "peptide"
			"setAttribute",		// in "wormbase_gene", filter "gene_class"
			"colForDisplay", 		// in "wormbase_gene", filter "phenotype_options"
	}));

	public OldFilterDescription(Element jdomElement) throws FunctionalException {
		super(jdomElement);
		
		this.oldOptionFilterList = new ArrayList<OldOptionFilter>();
		this.oldSpecificFilterContentList = new ArrayList<OldSpecificFilterContent>();
		this.oldEmptySpecificFilterContentList = new ArrayList<OldEmptySpecificElementContent>();
		this.oldOptionValueList = new ArrayList<OldOptionValue>();
		
		TransformationUtils.checkJdomElementProperties(jdomElement, OldElementPlaceHolder.propertyList,
				OldElement.propertyList, OldFilter.propertyList, OldFilterDescription.propertyList);
	}
	
	public void addOldOptionFilter(OldOptionFilter oldOptionFilter) {
		this.oldOptionFilterList.add(oldOptionFilter);
	}
	
	public void addOldSpecificFilterContent(OldSpecificFilterContent oldSpecificFilterContent) {
		this.oldSpecificFilterContentList.add(oldSpecificFilterContent);
	}
	
	public void addOldEmptySpecificFilterContent(OldEmptySpecificElementContent oldEmptySpecificFilterContent) {
		this.oldEmptySpecificFilterContentList.add(oldEmptySpecificFilterContent);
	}
	
	public void addOldOptionValue(OldOptionValue oldOptionValue) {
		this.oldOptionValueList.add(oldOptionValue);
	}

	public List<OldOptionFilter> getOldOptionFilterList() {
		return oldOptionFilterList;
	}

	public List<OldSpecificFilterContent> getOldSpecificFilterContentList() {
		return oldSpecificFilterContentList;
	}

	public List<OldEmptySpecificElementContent> getOldEmptySpecificFilterContentList() {
		return oldEmptySpecificFilterContentList;
	}

	public List<OldOptionValue> getOldOptionValueList() {
		return oldOptionValueList;
	}

	public void setOldOptionFilterList(List<OldOptionFilter> oldOptionFilterList) {
		this.oldOptionFilterList = oldOptionFilterList;
	}

	public void setOldSpecificFilterContentList(List<OldSpecificFilterContent> oldSpecificFilterContentList) {
		this.oldSpecificFilterContentList = oldSpecificFilterContentList;
	}

	public void setOldOptionValueList(List<OldOptionValue> oldOptionValueList) {
		this.oldOptionValueList = oldOptionValueList;
	}

	@Override
	public String toString() {
		int tabLevel = TransformationConstants.ELEMENT_LEVEL1_TAB_LEVEL;
		String node = 
			super.toString();
		
		StringBuffer sb = TransformationUtils.displayNode(tabLevel, node);
		TransformationUtils.displayChildren(this.oldOptionFilterList, tabLevel, sb, "oldOptionFilterList");
		TransformationUtils.displayChildren(this.oldSpecificFilterContentList, tabLevel, sb, "oldSpecificFilterContentList");
		TransformationUtils.displayChildren(this.oldOptionValueList, tabLevel, sb, "oldOptionValueList");
		
		/*sb.append(MartConfigutarorUtils.getTabSpace(tabLevel) + "oldSpecificFilterContentList.size() = " + oldSpecificFilterContentList.size() + MyUtils.LINE_SEPARATOR);	
		for (OldSpecificFilterContent oldSpecificFilterContent : this.oldSpecificFilterContentList) {
			sb.append(MartConfigutarorUtils.getTabSpace(tabLevel+1) + "oldSpecificFilterContent = {" + oldSpecificFilterContent); 
			sb.append(MartConfigutarorUtils.getTabSpace(tabLevel+1) + "}" + MyUtils.LINE_SEPARATOR);
		}
		
		sb.append(MartConfigutarorUtils.getTabSpace(tabLevel) + "oldOptionValueList.size() = " + oldOptionValueList.size() + MyUtils.LINE_SEPARATOR);	
		for (OldOptionValue oldOptionValue : this.oldOptionValueList) {
			sb.append(MartConfigutarorUtils.getTabSpace(tabLevel+1) + "oldOptionValue = {" + oldOptionValue); 
			sb.append(MartConfigutarorUtils.getTabSpace(tabLevel+1) + "}" + MyUtils.LINE_SEPARATOR);
		}*/
		
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
		OldFilterDescription oldFilterDescription=(OldFilterDescription)object;
		return (
			(this.oldOptionFilterList==oldFilterDescription.oldOptionFilterList || (this.oldOptionFilterList!=null && oldOptionFilterList.equals(oldFilterDescription.oldOptionFilterList))) &&
			(this.oldSpecificFilterContentList==oldFilterDescription.oldSpecificFilterContentList || (this.oldSpecificFilterContentList!=null && oldSpecificFilterContentList.equals(oldFilterDescription.oldSpecificFilterContentList))) &&
			(this.oldOptionValueList==oldFilterDescription.oldOptionValueList || (this.oldOptionValueList!=null && oldOptionValueList.equals(oldFilterDescription.oldOptionValueList)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldOptionFilterList? 0 : oldOptionFilterList.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldSpecificFilterContentList? 0 : oldSpecificFilterContentList.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==oldOptionValueList? 0 : oldOptionValueList.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldFilterDescription oldFilterDescription1, OldFilterDescription oldFilterDescription2) {
		if (oldFilterDescription1==null && oldFilterDescription2!=null) {
			return -1;
		} else if (oldFilterDescription1!=null && oldFilterDescription2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(oldFilterDescription1.oldOptionFilterList, oldFilterDescription2.oldOptionFilterList);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldFilterDescription1.oldSpecificFilterContentList, oldFilterDescription2.oldSpecificFilterContentList);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(oldFilterDescription1.oldOptionValueList, oldFilterDescription2.oldOptionValueList);
	}

	@Override
	public int compareTo(OldFilterDescription oldFilterDescription) {
		return compare(this, oldFilterDescription);
	}*/
	
	/*public Filter transform() {
		PartitionTable mainPartitionTable = DatasetTransformation.mainPartitionTable;
		//Filter filter = new Filter(this.internalName, this.displayName, this.description, !this.hideDisplay, locationName, martName, version, datasetName, configName, tableName, fieldName, targetRangeList, selectedByDefault, pointer, pointedElementName, sourceRangeList, displayType);
		Filter filter = new Filter(mainPartitionTable, this.internalName, this.displayName, this.description, !this.hideDisplay, 
				null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		//TODO
		return filter;	
	}*/
}