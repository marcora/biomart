package org.biomart.objects.objects;


import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.data.FilterData;
import org.biomart.objects.data.TreeFilterData;
import org.jdom.Namespace;


public class Filter extends org.biomart.objects.objects.Element	// to avoid any ambiguity with jdom's 
	implements Comparable<Filter>, Comparator<Filter>, Serializable {

	private static final long serialVersionUID = 8117878349721027751L;
	
	public static final String XML_ELEMENT_NAME = "filter";
	
	public static void main(String[] args) {}

	protected String qualifier = null;
	protected Boolean caseSensitive = null;
	protected File dataFolderPath = null;

	// For internal use only
	private FilterData filterData = null;
	
	public Filter() {} 	// for Serialization
	protected Filter(Container parentContainer, PartitionTable mainPartitionTable, String name) {
		this(parentContainer, mainPartitionTable, name, 
				null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
	}
	protected Filter(Container parentContainer, PartitionTable mainPartitionTable, String name, String displayName, String description, Boolean visible, 
			String locationName, String martName, Integer version, String datasetName, String configName, String tableName, String keyName, String fieldName, 
			List<String> targetRangeList, Boolean selectedByDefault, Boolean pointer, String pointedElementName, Boolean checkForNulls, 
			List<String> sourceRangeList) {
		super(mainPartitionTable, name, displayName, description, visible, XML_ELEMENT_NAME, parentContainer,
				locationName, martName, version, datasetName, configName, tableName, keyName, fieldName, targetRangeList, selectedByDefault, 
				pointer, pointedElementName, checkForNulls, sourceRangeList);
	}
	
	public File getDataFolderPath() {
		return dataFolderPath;
	}

	public FilterData getFilterData() {
		return filterData;
	}
	
	public Boolean getCaseSensitive() {
		return caseSensitive;
	}
	public void setCaseSensitive(Boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}
	public String getQualifier() {
		return qualifier;
	}
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"qualifier = " + qualifier + ", " + 
			"caseSensitive = " + caseSensitive + ", " +
			"dataFolderPath = " + (dataFolderPath!=null ? dataFolderPath.getAbsolutePath() : null);
	}
	
	public void copyDataRelatedInformation(Filter filter) throws FunctionalException {
		this.dataFolderPath = filter.getDataFolderPath();
		if (this instanceof SimpleFilter && filter instanceof SimpleFilter && ((SimpleFilter)filter).tree) {
			SimpleFilter simpleFilter = (SimpleFilter)this;
			SimpleFilter simpleFilter2 = (SimpleFilter)filter;
			simpleFilter.setTreeFilterData(simpleFilter2.getTreeFilterData());
		} else {			
			this.filterData = filter.getFilterData();
		}
	}
	public void setDataFolderPath(String stringDataFolderPath) throws FunctionalException {
		this.dataFolderPath = new File(stringDataFolderPath);
		if (!this.dataFolderPath.exists()) {
			throw new FunctionalException("dataFolderPath does not exist: " + this.dataFolderPath.getAbsolutePath());
		}
		if (this instanceof SimpleFilter && ((SimpleFilter)this).tree) {
			SimpleFilter simpleFilter = (SimpleFilter)this;
			simpleFilter.setTreeFilterData(new TreeFilterData(this));
		} else {
			this.filterData = new FilterData(this);			
		}
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Filter filter=(Filter)object;
		return (
			super.equals(filter) &&
			(this.qualifier==filter.qualifier || (this.qualifier!=null && qualifier.equals(filter.qualifier))) &&
			(this.caseSensitive==filter.caseSensitive || (this.caseSensitive!=null && caseSensitive.equals(filter.caseSensitive))) &&
			(this.dataFolderPath==filter.dataFolderPath || (this.dataFolderPath!=null && dataFolderPath.equals(filter.dataFolderPath)))
			//(this.filterData==filter.filterData || (this.filterData!=null && filterData.equals(filter.filterData)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==qualifier? 0 : qualifier.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==caseSensitive? 0 : caseSensitive.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==dataFolderPath? 0 : dataFolderPath.hashCode());
		//hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==filterData? 0 : filterData.hashCode());
		return hash;
	}

	public int compare(Filter filter1, Filter filter2) {
		if (filter1==null && filter2!=null) {
			return -1;
		} else if (filter1!=null && filter2==null) {
			return 1;
		}
		return 0;
	}

	public int compareTo(Filter filter) {
		return compare(this, filter);
	}
	
	public org.jdom.Element generateXml() {
		org.jdom.Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "dataFolderPath", this.dataFolderPath);
		return element;
	}
	
	
	
	// ===================================== Should be a different class ============================================

	protected Filter(Filter filter, Part part) {	// creates a light clone (temporary solution)
		super(filter, part);
		this.qualifier = filter.qualifier;
		this.caseSensitive = filter.caseSensitive;
		this.filterData = filter.filterData;
	}
	
	public org.jdom.Element generateXmlForWebService() throws FunctionalException {
		return generateXmlForWebService(null);
	}
	public org.jdom.Element generateXmlForWebService(Namespace namespace) throws FunctionalException {
		org.jdom.Element jdomObject = super.generateXmlForWebService(namespace);
		
		MartConfiguratorUtils.addAttribute(jdomObject, "qualifier", this.qualifier);
		MartConfiguratorUtils.addAttribute(jdomObject, "caseSensitive", this.caseSensitive);
		if (this.filterData!=null) {
			jdomObject.addContent(this.filterData.generateXml(true));
		}
		
		return jdomObject;
	}
	public JSONObject generateJsonForWebService() {
		JSONObject jsonObject = super.generateJsonForWebService();
		
		JSONObject object = (JSONObject)jsonObject.get(super.xmlElementName);
		object.put("qualifier", this.qualifier);
		object.put("caseSensitive", this.caseSensitive);
		if (this.filterData!=null) {
			object.put(FilterData.XML_ELEMENT_NAME, this.filterData.generateJson(true));
		}
		
		jsonObject.put(super.xmlElementName, object);
		return jsonObject;
	}
}
