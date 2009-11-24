package org.biomart.objects.objects;


import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.data.FilterData;
import org.biomart.objects.data.TreeFilterData;


public abstract class Filter extends org.biomart.objects.objects.Element	// to avoid any ambiguity with jdom's 
	implements Serializable {

	private static final long serialVersionUID = 8117878349721027751L;
	
	public static final String XML_ELEMENT_NAME = "filter";
//	public static final McNodeType MC_NODE_TYPE = McNodeType.Filter;
	
	public static void main(String[] args) {}

	protected String qualifier = null;
	protected Boolean caseSensitive = null;
	protected File dataFolderPath = null;

	// For internal use only
	private FilterData filterData = null;
	
	public Filter() {} 	// for Serialization
	protected Filter(PartitionTable mainPartitionTable, String name) {
		this(mainPartitionTable, name, 
				null, null, null, null, null, null, null, null, null, null, null, null, null, null);
	}
	protected Filter(PartitionTable mainPartitionTable, String name, String displayName, String description, Boolean visible, 
			String locationName, String martName, Integer version, String datasetName, String configName, 
			List<String> targetRangeList, Boolean selectedByDefault, Boolean pointer, String pointedElementName, Boolean checkForNulls, 
			List<String> sourceRangeList) {
		super(mainPartitionTable, name, displayName, description, visible, XML_ELEMENT_NAME, 
				locationName, martName, version, datasetName, configName, targetRangeList, selectedByDefault, 
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
	
	public org.jdom.Element generateXml() throws FunctionalException {
		org.jdom.Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "dataFolderPath", this.dataFolderPath);
		return element;
	}
}
