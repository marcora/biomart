package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.martRemote.Jsoml;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.data.TreeFilterData;


public class SimpleFilter extends Filter implements Serializable {

	private static final long serialVersionUID = -5644886498979459891L;

	public static void main(String[] args) {}

	protected String displayType = null;
	protected Boolean multiValue = null;	// Not sure applicable for trees
	protected String orderBy = null;	// TODO Make <field> an object so we reference it instead of keeping the name?
	protected String buttonURL = null;
	protected Boolean upload = null;
	
	// For List filters only
	protected ElementList cascadeChildrenElementList = null;
	
	// For Booleans filters
	protected String trueValue = null;
	protected String trueDisplay = null;
	protected String falseValue = null;
	protected String falseDisplay = null;

	// For Partitions filters
	protected Boolean partition = null;
	
	// For internal use only
	protected Boolean tree = null;	//TODO internal use?
	protected TreeFilterData treeFilterData = null;
	
	public SimpleFilter(PartitionTable mainPartitionTable, String name) {
		this(mainPartitionTable, name, false);
	}
	public SimpleFilter(PartitionTable mainPartitionTable, String name, Boolean tree) {
		super(mainPartitionTable, name);
		
		this.cascadeChildrenElementList = new ElementList(true);	// no repetitions
		
		this.tree = tree;
		this.partition = false;	// unless changed later
	}
	
	@Override
	public String toString() {
		return 
			super.toString() + ", " +
			"displayType = " + displayType + ", " + 
			"multiValue = " + multiValue + ", " +
			"orderBy = " + orderBy + ", " +
			"buttonURL = " + buttonURL + ", " +
			"upload = " + upload + ", " +
			
			"cascadeChildrenElementList = " + (cascadeChildrenElementList!=null ? cascadeChildrenElementList.getStringValue() : null) + ", " +
			
			"trueValue = " + trueValue + ", " +
			"trueDisplay = " + trueDisplay + ", " +
			"falseValue = " + falseValue + ", " +
			"falseDisplay = " + falseDisplay + ", " +
			
			"partition = " + partition;
	}

	/*@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		SimpleFilter simpleFilter=(SimpleFilter)object;
		return (
				super.equals(simpleFilter) &&
				(this.displayType==simpleFilter.displayType || (this.displayType!=null && displayType.equals(simpleFilter.displayType))) &&
				(this.orderBy==simpleFilter.orderBy || (this.orderBy!=null && orderBy.equals(simpleFilter.orderBy))) &&
				(this.multiValue==simpleFilter.multiValue || (this.multiValue!=null && multiValue.equals(simpleFilter.multiValue))) &&
				(this.buttonURL==simpleFilter.buttonURL || (this.buttonURL!=null && buttonURL.equals(simpleFilter.buttonURL))) &&
				(this.upload==simpleFilter.upload || (this.upload!=null && upload.equals(simpleFilter.upload))) &&
				
				(this.cascadeChildrenNamesList==simpleFilter.cascadeChildrenNamesList || (this.cascadeChildrenNamesList!=null && cascadeChildrenNamesList.equals(simpleFilter.cascadeChildrenNamesList))) &&
				
				(this.trueValue==simpleFilter.trueValue || (this.trueValue!=null && trueValue.equals(simpleFilter.trueValue))) &&
				(this.trueDisplay==simpleFilter.trueDisplay || (this.trueDisplay!=null && trueDisplay.equals(simpleFilter.trueDisplay))) &&
				(this.falseValue==simpleFilter.falseValue || (this.falseValue!=null && falseValue.equals(simpleFilter.falseValue))) &&
				(this.falseDisplay==simpleFilter.falseDisplay || (this.falseDisplay!=null && falseDisplay.equals(simpleFilter.falseDisplay))) &&
				
				(this.partition==simpleFilter.partition || (this.partition!=null && partition.equals(simpleFilter.partition))) &&
				
				(this.tree==simpleFilter.tree || (this.tree!=null && tree.equals(simpleFilter.tree)))
				//(this.treeFilterData==simpleFilter.treeFilterData || (this.treeFilterData!=null && treeFilterData.equals(simpleFilter.treeFilterData)))				
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==displayType? 0 : displayType.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==orderBy? 0 : orderBy.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==multiValue? 0 : multiValue.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==buttonURL? 0 : buttonURL.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==upload? 0 : upload.hashCode());
		
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==trueValue? 0 : trueValue.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==trueDisplay? 0 : trueDisplay.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==falseValue? 0 : falseValue.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==falseDisplay? 0 : falseDisplay.hashCode());
		
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==partition? 0 : partition.hashCode());
		
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==tree? 0 : tree.hashCode());
		
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==cascadeChildrenNamesList? 0 : cascadeChildrenNamesList.hashCode());
		//hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==treeFilterData? 0 : treeFilterData.hashCode());

		return hash;
	}*/
	
	// List related
	public ElementList getElementList() {
		return this.cascadeChildrenElementList;
	}
	
	// Tree related
	public TreeFilterData getTreeFilterData() {
		return treeFilterData;
	}
	public TreeFilterData setTreeFilterData(TreeFilterData treeFilterData) {
		return this.treeFilterData = treeFilterData;
	}

	public Boolean getTree() {
		return tree;
	}

	public void setTree(Boolean tree) {
		this.tree = tree;
	}

	// Partition related
	public Boolean getPartition() {
		return partition;
	}
	
	public void setPartition(Boolean partition) {
		this.partition = partition;
	}
	
	// Boolean related
	public String getFalseDisplay() {
		return falseDisplay;
	}

	public void setFalseDisplay(String falseDisplay) {
		this.falseDisplay = falseDisplay;
	}

	public String getFalseValue() {
		return falseValue;
	}

	public void setFalseValue(String falseValue) {
		this.falseValue = falseValue;
	}

	public String getTrueDisplay() {
		return trueDisplay;
	}

	public void setTrueDisplay(String trueDisplay) {
		this.trueDisplay = trueDisplay;
	}

	public String getTrueValue() {
		return trueValue;
	}

	public void setTrueValue(String trueValue) {
		this.trueValue = trueValue;
	}

	// Other
	public String getDisplayType() {
		return displayType;
	}

	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getButtonURL() {
		return buttonURL;
	}

	public void setButtonURL(String buttonURL) {
		this.buttonURL = buttonURL;
	}

	public Boolean getMultiValue() {
		return multiValue;
	}

	public void setMultiValue(Boolean multiValue) {
		this.multiValue = multiValue;
	}

	public Boolean getUpload() {
		return upload;
	}

	public void setUpload(Boolean upload) {
		this.upload = upload;
	}
	
	public org.jdom.Element generateXml() {
		org.jdom.Element element = super.generateXml();
		
		MyUtils.checkStatusProgram(this.partition || this.pointer || (null!=this.displayType && 
				this.displayType!=null && !MyUtils.isEmpty(this.displayType)), this.name + ", " + this.displayType);
		
		if (!partition) {
			MartConfiguratorUtils.addAttribute(element, "orderBy", this.orderBy);
			
			MartConfiguratorUtils.addAttribute(element, "displayType", this.displayType);
			
			MartConfiguratorUtils.addAttribute(element, "upload", this.upload);
			
			MartConfiguratorUtils.addAttribute(element, "multiValue", this.multiValue);
			
			MartConfiguratorUtils.addAttribute(element, "cascadeChildren", (cascadeChildrenElementList!=null ? cascadeChildrenElementList.getStringValue() : null));
			
			MartConfiguratorUtils.addAttribute(element, "buttonURL", this.buttonURL!=null ? this.buttonURL.toString() : null);
			MartConfiguratorUtils.addAttribute(element, "dataFolderPath", this.dataFolderPath);
			
			MartConfiguratorUtils.addAttribute(element, "trueValue", this.trueValue);
			MartConfiguratorUtils.addAttribute(element, "trueDisplay", this.trueDisplay);
			MartConfiguratorUtils.addAttribute(element, "falseValue", this.falseValue);
			MartConfiguratorUtils.addAttribute(element, "falseDisplay", this.falseDisplay);
		} else {
			MartConfiguratorUtils.addAttribute(element, "partition", this.partition);
		}
		
		return element;
	}
	
	
	// ===================================== Should be a different class ============================================

	private List<String> cascadeChildrenNamesList = null;
	public SimpleFilter(SimpleFilter simpleFilter) throws FunctionalException {
		this(simpleFilter, null, true);
	}
	public SimpleFilter(SimpleFilter simpleFilter, Part part) throws FunctionalException {
		this(simpleFilter, part, false);
	}
	public SimpleFilter(SimpleFilter simpleFilter, Part part, Boolean generic) throws FunctionalException {	// creates a light clone (temporary solution)
		super(simpleFilter, part);
		
		this.displayType = MartConfiguratorUtils.replacePartitionReferencesByValues(simpleFilter.displayType, part);
		this.orderBy = simpleFilter.orderBy;
		this.multiValue = simpleFilter.multiValue;
		this.buttonURL = MartConfiguratorUtils.replacePartitionReferencesByValues(simpleFilter.buttonURL, part);
		this.upload = simpleFilter.upload;
		this.trueValue = MartConfiguratorUtils.replacePartitionReferencesByValues(simpleFilter.trueValue, part);
		this.trueDisplay = MartConfiguratorUtils.replacePartitionReferencesByValues(simpleFilter.trueDisplay, part);
		this.falseValue = MartConfiguratorUtils.replacePartitionReferencesByValues(simpleFilter.falseValue, part);
		this.falseDisplay = MartConfiguratorUtils.replacePartitionReferencesByValues(simpleFilter.falseDisplay, part);
		
		this.cascadeChildrenNamesList = new ArrayList<String>();
		List<String> cascadeChildrenNamesTmp = simpleFilter.getElementList().getElementNames();
		for (String filterName : cascadeChildrenNamesTmp) {
			this.cascadeChildrenNamesList.add(MartConfiguratorUtils.replacePartitionReferencesByValues(filterName, part));
		}
		
		if (simpleFilter.treeFilterData!=null) {
			TreeFilterData treeFilterDataClone = new TreeFilterData(simpleFilter.treeFilterData, part);
			if (treeFilterDataClone.hasData()) {	// may not be the case if parts don't match (not all parts have data)
				this.treeFilterData = treeFilterDataClone;
			}
		}
	}

	public Jsoml generateOutputForWebService(boolean xml) throws FunctionalException {
		Jsoml jsoml = super.generateOutputForWebService(xml);
		
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

		if (this.treeFilterData!=null) {
			jsoml.addContent(this.treeFilterData.generateExchangeFormat(xml, true));
		}
		
		return jsoml;
	}
}
