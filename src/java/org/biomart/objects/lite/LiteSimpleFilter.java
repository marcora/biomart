package org.biomart.objects.lite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.data.TreeFilterData;
import org.biomart.objects.objects.Part;
import org.biomart.objects.objects.SimpleFilter;

public class LiteSimpleFilter extends LiteFilter implements Serializable {

	private static final long serialVersionUID = 7606339732698858011L;
	
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

	private TreeFilterData treeFilterData = null;
	
	public LiteSimpleFilter(SimpleFilter simpleFilter) throws FunctionalException {
		this(simpleFilter, null);	// no part = generic (for partition filter for instance)
	}
	public LiteSimpleFilter(SimpleFilter simpleFilter, Part part) throws FunctionalException {
		
		super(simpleFilter, part);
		
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
		
		// Filter data related
		if (simpleFilter.getTreeFilterData()!=null) {	// then simpleFilter.getFilterData() is null
			TreeFilterData treeFilterDataClone = new TreeFilterData(simpleFilter.getTreeFilterData(), part);
			if (treeFilterDataClone.hasData()) {	// may not be the case if parts don't match (not all parts have data)
				this.treeFilterData = treeFilterDataClone;
			}
		}	// else simpleFilter is not a tree
	}

	
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
	
	
	@Override
	public String toString() {
		return 
			super.toString() + ", " +
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
			"treeFilterData = " + (treeFilterData!=null);
	}

	@Override
	protected Jsoml generateExchangeFormat(boolean xml) throws FunctionalException {

		Jsoml jsoml = new Jsoml(xml, this.xmlElementName);
		
		jsoml.setAttribute("name", this.name);
		jsoml.setAttribute("displayName", this.displayName);
		jsoml.setAttribute("description", this.description);
		
		jsoml.setAttribute("default", super.selectedByDefault);
		
		jsoml.setAttribute("qualifier", super.qualifier);
		jsoml.setAttribute("caseSensitive", super.caseSensitive);
		
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

		if (super.filterData!=null) {		
			jsoml.addContent(super.filterData.generateExchangeFormat(xml, true));
		} if (this.treeFilterData!=null) {
			jsoml.addContent(this.treeFilterData.generateExchangeFormat(xml, true));
		}
		
		return jsoml;
	}
}
