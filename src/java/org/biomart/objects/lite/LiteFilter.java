package org.biomart.objects.lite;

import java.io.Serializable;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.data.FilterData;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.Part;

public abstract class LiteFilter extends LiteSimpleMartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 3717403391079076481L;

	private static final String XML_ELEMENT_NAME = "filter";

	protected Boolean selectedByDefault = null;
	
	protected String qualifier = null;
	protected Boolean caseSensitive = null;
	
	protected FilterData filterData = null;

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
		
		// Filter data related
		if (filter.getFilterData()!=null) {
			FilterData filterDataClone = new FilterData(filter.getFilterData(), part);
			if (filterDataClone.hasData()) {	// may not be the case if parts don't match (not all parts have data)
				this.filterData = filterDataClone;
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
	
	public Boolean getSelectedByDefault() {
		return selectedByDefault;
	}

	public String getQualifier() {
		return qualifier;
	}

	public Boolean getCaseSensitive() {
		return caseSensitive;
	}

	public FilterData getFilterData() {
		return filterData;
	}

	@Override
	public String toString() {	//TODO
		return 
			super.toString() + ", " +
			"selectedByDefault = " + selectedByDefault + ", " +
			"qualifier = " + qualifier + ", " +
			"caseSensitive = " + caseSensitive + ", " +
			"filterData = " + (filterData!=null);
	}
}
