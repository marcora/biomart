package org.biomart.objects.lite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.data.FilterData;
import org.biomart.objects.data.TreeFilterData;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.GroupFilter;
import org.biomart.objects.objects.Part;
import org.biomart.objects.objects.SimpleFilter;

public class LiteFilter extends LiteSimpleMartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 3717403391079076481L;

	private static final String XML_ELEMENT_NAME = "filter";

	// From element
	private Boolean selectedByDefault = null;
	
	// From filter
	private String qualifier = null;
	private Boolean caseSensitive = null;
	

	// From simple filters
	private String displayType = null;
	private String orderBy = null;
	private Boolean multiValue = null;
	private String buttonURL = null;
	private Boolean upload = null;
	private String trueValue = null;
	private String trueDisplay = null;
	private String falseValue = null;
	private String falseDisplay = null;
	
	private Boolean partition = null;
	private List<String> cascadeChildrenNamesList = null;

	// From group filters
	private String logicalOperator = null;
	private String multipleFilter = null;
	private List<String> simpleFilterList = null;
	
	// Data related
	private FilterData filterData = null;	// from filter
	private TreeFilterData treeFilterData = null;	// from group filter
	
	public LiteFilter(Filter filter) throws FunctionalException {
		this(filter, null);	// no part = generic (for partition filter for instance)
	}
	public LiteFilter(Filter filter, Part part) throws FunctionalException {
		
		super(XML_ELEMENT_NAME,
			part==null ? filter.getName() : 
				MartConfiguratorUtils.replacePartitionReferencesByValues(filter.getName(), part),
			part==null ? filter.getDisplayName() : 
				MartConfiguratorUtils.replacePartitionReferencesByValues(filter.getDisplayName(), part),
			part==null ? filter.getDescription() : 
				MartConfiguratorUtils.replacePartitionReferencesByValues(filter.getDescription(), part),
				null);	// irrelevant for elements

		this.selectedByDefault = filter.getSelectedByDefault();
		
		this.qualifier = filter.getQualifier();
		this.caseSensitive = filter.getCaseSensitive();
		
		boolean isSimpleFilter = filter instanceof SimpleFilter;
		
		// From simple filters
		if (isSimpleFilter) {
			SimpleFilter simpleFilter = (SimpleFilter)filter;
			this.displayType = MartConfiguratorUtils.replacePartitionReferencesByValues(simpleFilter.getDisplayType(), part);
			this.orderBy = simpleFilter.getOrderBy();
			this.multiValue = simpleFilter.getMultiValue();
			this.buttonURL = MartConfiguratorUtils.replacePartitionReferencesByValues(simpleFilter.getButtonURL(), part);
			this.upload = simpleFilter.getUpload();
			this.trueValue = MartConfiguratorUtils.replacePartitionReferencesByValues(simpleFilter.getTrueValue(), part);
			this.trueDisplay = MartConfiguratorUtils.replacePartitionReferencesByValues(simpleFilter.getTrueDisplay(), part);
			this.falseValue = MartConfiguratorUtils.replacePartitionReferencesByValues(simpleFilter.getFalseValue(), part);
			this.falseDisplay = MartConfiguratorUtils.replacePartitionReferencesByValues(simpleFilter.getFalseDisplay(), part);
			
			this.partition = simpleFilter.getPartition();
			
			this.cascadeChildrenNamesList = new ArrayList<String>();
			List<String> cascadeChildrenNamesTmp = simpleFilter.getElementList().getElementNames();
			for (String filterName : cascadeChildrenNamesTmp) {
				this.cascadeChildrenNamesList.add(MartConfiguratorUtils.replacePartitionReferencesByValues(filterName, part));
			}
		} else if (filter instanceof GroupFilter) {
			GroupFilter groupFilter = (GroupFilter)filter;
		
			// From group filters
			this.logicalOperator = groupFilter.getLogicalOperator();
			this.multipleFilter = groupFilter.getMultipleFilter();
			
			this.simpleFilterList = new ArrayList<String>();
			List<String> simpleFilterNamesTmp = groupFilter.getElementList().getElementNames();
			for (String filterName : simpleFilterNamesTmp) {
				this.simpleFilterList.add(MartConfiguratorUtils.replacePartitionReferencesByValues(filterName, part));
			}
		}
		
		// Filter data related
		if (filter.getFilterData()!=null) {
			FilterData filterDataClone = new FilterData(filter.getFilterData(), part);
			if (filterDataClone.hasData()) {	// may not be the case if parts don't match (not all parts have data)
				this.filterData = filterDataClone;
			}
		} else if (isSimpleFilter && ((SimpleFilter)filter).getTreeFilterData()!=null) {	// then simpleFilter.getFilterData() is null
			TreeFilterData treeFilterDataClone = new TreeFilterData(((SimpleFilter)filter).getTreeFilterData(), part);
			if (treeFilterDataClone.hasData()) {	// may not be the case if parts don't match (not all parts have data)
				this.treeFilterData = treeFilterDataClone;
			}
		}
	}
	
	// Properties in super class available for this light object
	public String getName() {
		return super.name;
	}
	public String getDisplayName() {
		return super.displayName;
	}
	public String getDescription() {
		return super.description;
	}
	
	// From element
	public Boolean getSelectedByDefault() {
		return selectedByDefault;
	}

	// From filter
	public String getQualifier() {
		return qualifier;
	}
	public Boolean getCaseSensitive() {
		return caseSensitive;
	}
	public FilterData getFilterData() {
		return filterData;
	}
	
	// From simple filter
	public String getDisplayType() {
		return displayType;
	}
	public String getOrderBy() {
		return orderBy;
	}
	public Boolean getMultiValue() {
		return multiValue;
	}
	public String getButtonURL() {
		return buttonURL;
	}
	public Boolean getUpload() {
		return upload;
	}
	public String getTrueValue() {
		return trueValue;
	}
	public String getTrueDisplay() {
		return trueDisplay;
	}
	public String getFalseValue() {
		return falseValue;
	}
	public String getFalseDisplay() {
		return falseDisplay;
	}
	public Boolean getPartition() {
		return partition;
	}
	public TreeFilterData getTreeFilterData() {
		return treeFilterData;
	}
	public List<String> getCascadeChildrenNamesList() {
		return new ArrayList<String>(cascadeChildrenNamesList);
	}

	// From group filter
	public String getLogicalOperator() {
		return logicalOperator;
	}
	public String getMultipleFilter() {
		return multipleFilter;
	}
	public List<String> getSimpleFilterList() {
		return new ArrayList<String>(simpleFilterList);
	}
	
	@Override
	public String toString() {	//TODO
		return 
			super.toString() + ", " +
			
			// From element
			"selectedByDefault = " + selectedByDefault + ", " +
			
			// From filter
			"qualifier = " + qualifier + ", " +
			"caseSensitive = " + caseSensitive + ", " +
			"filterData = " + (filterData!=null) + ", " +
			
			// From simple filter
			"displayType = " + displayType + ", " +
			"orderBy = " + orderBy + ", " +
			"multiValue = " + multiValue + ", " +
			"buttonURL = " + buttonURL + ", " +
			"upload = " + upload + ", " +
			"trueValue = " + trueValue + ", " +
			"trueDisplay = " + trueDisplay + ", " +
			"falseValue = " + falseValue + ", " +
			"falseDisplay = " + falseDisplay + ", " +
			"partition = " + partition + ", " +
			"cascadeChildrenNamesList = " + cascadeChildrenNamesList + ", " +
			"treeFilterData = " + (treeFilterData!=null) + ", " +
			
			// From group filter
			"logicalOperator = " + logicalOperator + ", " +
			"multipleFilter = " + multipleFilter + ", " +
			"simpleFilterList = " + simpleFilterList;
	}
	
	@Override
	protected Jsoml generateExchangeFormat(boolean xml) throws FunctionalException {

		Jsoml jsoml = new Jsoml(xml, this.xmlElementName);
		
		// From super class
		jsoml.setAttribute("name", this.name);
		jsoml.setAttribute("displayName", this.displayName);
		jsoml.setAttribute("description", this.description);
		
		// From element
		jsoml.setAttribute("default", this.selectedByDefault);
		
		// From filter
		jsoml.setAttribute("qualifier", this.qualifier);
		jsoml.setAttribute("caseSensitive", this.caseSensitive);
			
		// From simple filter
		jsoml.setAttribute("orderBy", this.orderBy);
		jsoml.setAttribute("displayType", this.displayType);
		jsoml.setAttribute("upload", this.upload);	
		jsoml.setAttribute("multiValue", this.multiValue);	
		jsoml.setAttribute("partition", this.partition);	
		jsoml.setAttribute("cascadeChildren", this.cascadeChildrenNamesList);
		jsoml.setAttribute("buttonURL", this.buttonURL);
		
		jsoml.setAttribute("trueValue", this.trueValue);
		jsoml.setAttribute("trueDisplay", this.trueDisplay);
		jsoml.setAttribute("falseValue", this.falseValue);
		jsoml.setAttribute("falseDisplay",  this.falseDisplay);

		// From group filter
		jsoml.setAttribute("logicalOperator", this.logicalOperator);
		jsoml.setAttribute("multipleFilter", this.multipleFilter);
		jsoml.setAttribute("filterList", this.simpleFilterList);
		if (this.treeFilterData!=null) {
			jsoml.addContent(this.treeFilterData.generateExchangeFormat(xml, true));
		}
		
		// From both simple and group filter
		if (this.filterData!=null) {		
			jsoml.addContent(this.filterData.generateExchangeFormat(xml, true));
		}
		
		return jsoml;
	}
}
