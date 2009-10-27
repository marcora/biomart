package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.HashSet;

import net.sf.json.JSONObject;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.data.TreeFilterData;
import org.jdom.Namespace;


public class SimpleFilter extends Filter implements Serializable/*implements Comparable<SimpleFilter>, Comparator<SimpleFilter> */{

	private static final long serialVersionUID = -5644886498979459891L;

	public static void main(String[] args) {}

	protected String displayType = null;
	protected String orderBy = null;	// TODO Make <field> an object so we reference it instead of keeping the name?
	protected Boolean multiValue = null;	// Not sure applicable for trees
	protected String buttonURL = null;
	protected HashSet<SimpleFilter> cascadeChildren = null;	//TODO move it to listfilter only?
	protected Boolean upload = null;
	
	protected String trueValue = null;
	protected String trueDisplay = null;
	protected String falseValue = null;
	protected String falseDisplay = null;
	
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
	}
	
	@Override
	public String toString() {
		return 
			super.toString() + ", " +
			"displayType = " + displayType + ", " + 
			"multiValue = " + multiValue + ", " +
			
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
				super.equals(simpleFilter)
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
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
	/*public void copyTreeDataRelatedInformation(TreeFilter treeFilter) throws FunctionalException {
		this.dataFolderPath = treeFilter.getDataFolderPath();
		this.treeFilterData = treeFilter.getTreeFilterData();
	}*/
	/*public void setTreeDataFolderPath(String stringDataFolderPath) throws FunctionalException {
		this.dataFolderPath = new File(stringDataFolderPath);
		if (!this.dataFolderPath.exists()) {
			throw new FunctionalException("dataFolderPath does not exist: " + this.dataFolderPath.getAbsolutePath());
		}
		this.treeFilterData = new TreeFilterData(this);
		//TODO consider setting (boolean)tree here...
	}*/

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

	/*@Override
	public int compare(SimpleFilter simpleFilter1, SimpleFilter simpleFilter2) {
		if (simpleFilter1==null && simpleFilter2!=null) {
			return -1;
		} else if (simpleFilter1!=null && simpleFilter2==null) {
			return 1;
		}
		return 0;
	}

	@Override
	public int compareTo(SimpleFilter simpleFilter) {
		return compare(this, simpleFilter);
	}*/
	
	public org.jdom.Element generateXml() {
		org.jdom.Element element = super.generateXml();
		
		/*MyUtils.checkStatusProgram(this.pointer || (null!=this.displayType && 
				this.displayType.getValue()!=null && !MyUtils.isEmpty(this.displayType.getValue())), this.name + ", " + this.displayType);
		if (!this.pointer) {
			element.setAttribute("displayType", this.displayType.getValue());
		}*/
		MyUtils.checkStatusProgram(this.pointer || (null!=this.displayType && 
				this.displayType!=null && !MyUtils.isEmpty(this.displayType)), this.name + ", " + this.displayType);
		if (!this.pointer) {
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
		}
		
		return element;
	}
	public org.jdom.Element generateXmlForWebService() {
		return generateXmlForWebService(null);
	}
	public org.jdom.Element generateXmlForWebService(Namespace namespace) {
		org.jdom.Element jdomObject = super.generateXmlForWebService(namespace);
		
		if (!this.pointer) {
			MartConfiguratorUtils.addAttribute(jdomObject, "orderBy", this.orderBy);
			
			MartConfiguratorUtils.addAttribute(jdomObject, "displayType", this.displayType);
			
			MartConfiguratorUtils.addAttribute(jdomObject, "upload", this.upload);
			
			MartConfiguratorUtils.addAttribute(jdomObject, "multiValue", this.multiValue);
			
			MartConfiguratorUtils.addAttribute(jdomObject, "cascadeChildren", this.cascadeChildrenNamesList);
			
			MartConfiguratorUtils.addAttribute(jdomObject, "buttonURL", this.buttonURL!=null ? this.buttonURL.toString() : null);
			
			MartConfiguratorUtils.addAttribute(jdomObject, "trueValue", this.trueValue);
			MartConfiguratorUtils.addAttribute(jdomObject, "trueDisplay", this.trueDisplay);
			MartConfiguratorUtils.addAttribute(jdomObject, "falseValue", this.falseValue);
			MartConfiguratorUtils.addAttribute(jdomObject, "falseDisplay", this.falseDisplay);
		}
		
		return jdomObject;
	}
	public JSONObject generateJsonForWebService() {
		JSONObject jsonObject = super.generateJsonForWebService();
		
		JSONObject object = (JSONObject)jsonObject.get(super.xmlElementName);
		if (!this.pointer) {
			object.put("orderBy", this.orderBy);
			
			object.put("displayType", this.displayType);
						
			object.put("upload", this.upload);
						
			object.put("multiValue", this.multiValue);
						
			object.put("cascadeChildren", this.cascadeChildrenNamesList);
						
			object.put("buttonURL", this.buttonURL!=null ? this.buttonURL.toString() : null);
						
			object.put("trueValue", this.trueValue);
			object.put("trueDisplay", this.trueDisplay);
			object.put("falseValue", this.falseValue);
			object.put("falseDisplay", this.falseDisplay);
		}
		
		jsonObject.put(super.xmlElementName, object);
		return jsonObject;
	}
}
