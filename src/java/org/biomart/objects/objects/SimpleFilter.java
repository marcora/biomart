package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.HashSet;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.data.TreeFilterData;
import org.jdom.Namespace;


public class SimpleFilter extends Filter implements Serializable {

	private static final long serialVersionUID = -5644886498979459891L;

	public static void main(String[] args) {}

	protected String displayType = null;
	protected String orderBy = null;	// TODO Make <field> an object so we reference it instead of keeping the name?
	protected Boolean multiValue = null;	// Not sure applicable for trees
	protected String buttonURL = null;
	protected Boolean upload = null;
	protected HashSet<SimpleFilter> cascadeChildren = null;
	
	protected String trueValue = null;
	protected String trueDisplay = null;
	protected String falseValue = null;
	protected String falseDisplay = null;
	
	protected Boolean partition = null;
	
	// For internal use only
	protected HashSet<String> cascadeChildrenNamesList = null;
	protected Boolean tree = null;
	protected TreeFilterData treeFilterData = null;
	
	public SimpleFilter(Container parentContainer, PartitionTable mainPartitionTable, String name) {
		this(parentContainer, mainPartitionTable, name, false);
	}
	public SimpleFilter(Container parentContainer, PartitionTable mainPartitionTable, String name, Boolean tree) {
		super(parentContainer, mainPartitionTable, name);
		
		this.cascadeChildren = new HashSet<SimpleFilter>();
		this.cascadeChildrenNamesList = new HashSet<String>();
		this.tree = tree;
		this.partition = false;	// unless changed later
	}
	
	@Override
	public String toString() {
		return 
			super.toString() + ", " +
			"displayType = " + displayType + ", " + 
			"multiValue = " + multiValue + ", " +
			"partition = " + partition + ", " +
			
			"trueValue = " + trueValue + ", " +
			"trueDisplay = " + trueDisplay + ", " +
			"falseValue = " + falseValue + ", " +
			"falseDisplay = " + falseDisplay + ", " +
			
			"upload = " + upload + ", " +
			
			"cascadeChildren = " + (cascadeChildren!=null ? cascadeChildren.size() : null) + ", " +
			
			"buttonURL = " + buttonURL;
	}

	@Override
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
	}

	// List related
	public HashSet<String> getCascadeChildrenNamesList() {
		return cascadeChildrenNamesList;
	}

	public HashSet<SimpleFilter> getCascadeChildren() {
		return cascadeChildren;
	}
	public void addCascadeChildren(HashSet<SimpleFilter> cascadeChildren) {
		this.cascadeChildren.addAll(cascadeChildren);
		for (SimpleFilter simpleFilter : cascadeChildren) {
			this.cascadeChildrenNamesList.add(simpleFilter.getName());
		}
	}
	
	public void addCascadeChild(SimpleFilter cascadeChild) {
		this.cascadeChildren.add(cascadeChild);
		this.cascadeChildrenNamesList.add(cascadeChild.getName());
	}
	
	// Tree related
	public TreeFilterData getTreeFilterData() {
		return treeFilterData;
	}
	public TreeFilterData setTreeFilterData(TreeFilterData treeFilterData) {
		return this.treeFilterData = treeFilterData;
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

	public Boolean getMultiValue() {
		return multiValue;
	}

	public void setMultiValue(Boolean multiValue) {
		this.multiValue = multiValue;
	}

	public Boolean getPartition() {
		return partition;
	}
	
	public void setPartition(Boolean partition) {
		this.partition = partition;
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

	public Boolean getUpload() {
		return upload;
	}

	public void setUpload(Boolean upload) {
		this.upload = upload;
	}

	public Boolean getTree() {
		return tree;
	}

	public void setTree(Boolean tree) {
		this.tree = tree;
	}
	
	public org.jdom.Element generateXml() {
		org.jdom.Element element = super.generateXml();
		
		MyUtils.checkStatusProgram(this.partition || this.pointer || (null!=this.displayType && 
				this.displayType!=null && !MyUtils.isEmpty(this.displayType)), this.name + ", " + this.displayType);
		if (!this.pointer && !this.partition) {
			MartConfiguratorUtils.addAttribute(element, "orderBy", this.orderBy);
			
			MartConfiguratorUtils.addAttribute(element, "displayType", this.displayType);
			
			MartConfiguratorUtils.addAttribute(element, "upload", this.upload);
			
			MartConfiguratorUtils.addAttribute(element, "multiValue", this.multiValue);
			
			MartConfiguratorUtils.addAttribute(element, "cascadeChildren", this.cascadeChildrenNamesList);
			
			MartConfiguratorUtils.addAttribute(element, "buttonURL", this.buttonURL!=null ? this.buttonURL.toString() : null);
			MartConfiguratorUtils.addAttribute(element, "dataFolderPath", this.dataFolderPath);
			
			MartConfiguratorUtils.addAttribute(element, "trueValue", this.trueValue);
			MartConfiguratorUtils.addAttribute(element, "trueDisplay", this.trueDisplay);
			MartConfiguratorUtils.addAttribute(element, "falseValue", this.falseValue);
			MartConfiguratorUtils.addAttribute(element, "falseDisplay", this.falseDisplay);
		} else if (!this.pointer && this.partition) {
			MartConfiguratorUtils.addAttribute(element, "partition", this.partition);
		}
		
		return element;
	}
	
	
	// ===================================== Should be a different class ============================================
	
	private org.jdom.Element treeFilterDataElement = null;	// easier to handle than the big map
	public org.jdom.Element getTreeFilterDataElement() {
		return treeFilterDataElement;
	}
	public SimpleFilter(SimpleFilter simpleFilter, Part part) throws CloneNotSupportedException {	// creates a light clone (temporary solution)
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
		
		this.cascadeChildrenNamesList = new HashSet<String>();
		for (String cascadeChildName : simpleFilter.cascadeChildrenNamesList) {
			this.cascadeChildrenNamesList.add(MartConfiguratorUtils.replacePartitionReferencesByValues(cascadeChildName, part));
		}
		
		this.treeFilterDataElement = simpleFilter.treeFilterData!=null ? simpleFilter.treeFilterData.generateXml(true) : null;
	}
	
	public org.jdom.Element generateXmlForWebService() throws FunctionalException {
		return generateXmlForWebService(null);
	}
	public org.jdom.Element generateXmlForWebService(Namespace namespace) throws FunctionalException {
		org.jdom.Element jdomObject = super.generateXmlForWebService(namespace);
		
		MartConfiguratorUtils.addAttribute(jdomObject, "orderBy", this.orderBy);
		
		MartConfiguratorUtils.addAttribute(jdomObject, "displayType", this.displayType);
		
		MartConfiguratorUtils.addAttribute(jdomObject, "upload", this.upload);	
		
		MartConfiguratorUtils.addAttribute(jdomObject, "multiValue", this.multiValue);	
		
		MartConfiguratorUtils.addAttribute(jdomObject, "partition", this.partition);	
		
		MartConfiguratorUtils.addAttribute(jdomObject, "cascadeChildren", this.cascadeChildrenNamesList);
		
		MartConfiguratorUtils.addAttribute(jdomObject, "buttonURL", this.buttonURL);
		
		MartConfiguratorUtils.addAttribute(jdomObject, "trueValue", this.trueValue);
		MartConfiguratorUtils.addAttribute(jdomObject, "trueDisplay", this.trueDisplay);
		MartConfiguratorUtils.addAttribute(jdomObject, "falseValue", this.falseValue);
		MartConfiguratorUtils.addAttribute(jdomObject, "falseDisplay",  this.falseDisplay);

		if (this.treeFilterDataElement!=null) {
			jdomObject.addContent(this.treeFilterDataElement);
		}
		
		return jdomObject;
	}
	public JSONObject generateJsonForWebService() {
		JSONObject jsonObject = super.generateJsonForWebService();
		
		JSONObject object = (JSONObject)jsonObject.get(super.xmlElementName);
		object.put("orderBy", this.orderBy);
		
		object.put("displayType", this.displayType);
					
		object.put("upload", this.upload);
					
		object.put("multiValue", this.multiValue);
		
		object.put("partition", this.partition);
					
		object.put("cascadeChildren", MartConfiguratorUtils.collectionToCommaSeparatedString(this.cascadeChildrenNamesList));
					
		object.put("buttonURL", this.buttonURL);
					
		object.put("trueValue", this.trueValue);
		object.put("trueDisplay", this.trueDisplay);
		object.put("falseValue", this.falseValue);
		object.put("falseDisplay", this.falseDisplay);
		
		jsonObject.put(super.xmlElementName, object);
		return jsonObject;
	}
}
