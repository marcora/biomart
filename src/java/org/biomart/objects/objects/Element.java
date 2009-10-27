package org.biomart.objects.objects;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Namespace;


public class Element extends Containee implements Serializable/* implements Comparable<Element>, Comparator<Element>*/ {

	private static final long serialVersionUID = -2902852019170342721L;

	public static void main(String[] args) {}

	protected Boolean selectedByDefault = null;
	
	protected Boolean pointer = null;
	
	protected String locationName = null;
	protected String martName = null;
	protected Integer version = null;
	protected String datasetName = null;
	protected String configName = null;
	protected String tableName = null;
	protected String keyName = null;
	protected String fieldName = null;
	protected Boolean checkForNulls = null;
	
	protected String pointedElementName = null;
	
	protected Range targetRange = null;
	protected Range sourceRange = null;

	protected PartitionTable mainPartitionTable = null;
	protected List<PartitionTable> otherPartitionTableList = null;
	
	protected Element pointedElement = null;	// For reference
	
	public Element() {} 	// for Serialization
	protected Element(PartitionTable mainPartitionTable, String name, String displayName, String description, Boolean visible, String xmlElementName, Container parentContainer,
			String locationName, String martName, Integer version, String datasetName, String configName, String tableName, String keyName, String fieldName, 
			List<String> targetRangeList, Boolean selectedByDefault, Boolean pointer, String pointedElementName, Boolean checkForNulls, List<String> sourceRangeList) {
		super(name, displayName, description, visible, xmlElementName, parentContainer);
		
		this.locationName = locationName;
		this.martName = martName;
		this.version = version;
		this.datasetName = datasetName;
		this.configName = configName;
		this.tableName = tableName;
		this.keyName = keyName;
		this.fieldName = fieldName;
		this.selectedByDefault = selectedByDefault;
		this.pointer = pointer;
		this.pointedElementName = pointedElementName;
		this.checkForNulls = checkForNulls;
		
		this.targetRange = new Range(true);		//TODO should automatically add rows of mainpartition table?
		this.sourceRange = new Range(false);
		
		this.mainPartitionTable = mainPartitionTable;
		this.otherPartitionTableList = new ArrayList<PartitionTable>();
	}
	
	public Range getTargetRange() {
		return this.targetRange;
	}
	public Range getSourceRange() {
		return this.sourceRange;
	}
	
	/*public void addTargetRange(String partitionTableName, Integer row, boolean visible) {
		this.targetRangeList.add(MartConfiguratorUtils.getRange(partitionTableName, row, visible));
	}
	
	public void addSourceRange(String partitionTableName, Integer row) {
		this.sourceRangeList.add(MartConfiguratorUtils.getRange(partitionTableName, row));
	}*/
	
	public void addOtherPartitionTable(PartitionTable partitionTable) {
		MyUtils.checkStatusProgram(!this.otherPartitionTableList.contains(partitionTable));
		this.otherPartitionTableList.add(partitionTable);
	}

	public String getLocationName() {
		return locationName;
	}

	public String getMartName() {
		return martName;
	}

	public Integer getVersion() {
		return version;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public String getConfigName() {
		return configName;
	}

	public String getTableName() {
		return tableName;
	}

	public String getKeyName() {
		return keyName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public Boolean getSelectedByDefault() {
		return selectedByDefault;
	}

	public Boolean getPointer() {
		return pointer;
	}

	public Boolean getCheckForNulls() {
		return checkForNulls;
	}

	public String getPointedElementName() {
		return pointedElementName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public void setMartName(String martName) {
		this.martName = martName;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public void setSelectedByDefault(Boolean selectedByDefault) {
		this.selectedByDefault = selectedByDefault;
	}

	public void setPointer(Boolean pointer) {
		this.pointer = pointer;
	}
	
	public void setCheckForNulls(Boolean checkForNulls) {
		this.checkForNulls = checkForNulls;
	}

	public void setSourceRange(Range sourceRange) {
		this.sourceRange = sourceRange;
	}

	public void setTargetRange(Range targetRange) {
		this.targetRange = targetRange;
	}

	public Element getPointedElement() {
		return pointedElement;
	}

	public void setPointedElement(Element pointedElement) {
		this.pointedElement = pointedElement;
		//MyUtils.checkStatusProgram(this.pointedElementName!=null && this.pointedElementName.equals(pointedElement.getName()));
		this.pointedElementName = pointedElement.getName();
	}
	
	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"locationName = " + locationName + ", " +
			"martName = " + martName + ", " +
			"version = " + version + ", " +
			"datasetName = " + datasetName + ", " +
			"configName = " + configName + ", " +
			"tableName = " + tableName + ", " +
			"keyName = " + keyName + ", " +
			"fieldName = " + fieldName + ", " +
			"selectedByDefault = " + selectedByDefault + ", " +
			"pointer = " + pointer + ", " +
			"pointedElementName = " + pointedElementName + ", " +
			"checkForNulls = " + checkForNulls + ", " +
			"targetRange = " + targetRange + ", " +
			"sourceRange = " + sourceRange;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Element element=(Element)object;
		return (
			(super.equals(element)) &&
			(this.locationName==element.locationName || (this.locationName!=null && locationName.equals(element.locationName))) &&
			(this.martName==element.martName || (this.martName!=null && martName.equals(element.martName))) &&
			(this.version==element.version || (this.version!=null && version.equals(element.version))) &&
			(this.datasetName==element.datasetName || (this.datasetName!=null && datasetName.equals(element.datasetName))) &&
			(this.configName==element.configName || (this.configName!=null && configName.equals(element.configName))) &&
			(this.tableName==element.tableName || (this.tableName!=null && tableName.equals(element.tableName))) &&
			(this.keyName==element.keyName || (this.keyName!=null && keyName.equals(element.keyName))) &&
			(this.fieldName==element.fieldName || (this.fieldName!=null && fieldName.equals(element.fieldName))) &&
			(this.targetRange==element.targetRange || (this.targetRange!=null && targetRange.equals(element.targetRange))) &&
			/*(this.selectedByDefault==element.selectedByDefault || (this.selectedByDefault!=null && selectedByDefault.equals(element.selectedByDefault))) &&*/
			(this.pointer==element.pointer || (this.pointer!=null && pointer.equals(element.pointer))) &&
			(this.pointedElementName==element.pointedElementName || (this.pointedElementName!=null && pointedElementName.equals(element.pointedElementName)))
			/*(this.checkForNulls==element.checkForNulls || (this.checkForNulls!=null && checkForNulls.equals(element.checkForNulls))) &&
			(this.sourceRange==element.sourceRange || (this.sourceRange!=null && sourceRange.equals(element.sourceRange)))*/
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==locationName? 0 : locationName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==martName? 0 : martName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==version? 0 : version.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==datasetName? 0 : datasetName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==configName? 0 : configName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==tableName? 0 : tableName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==keyName? 0 : keyName.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==fieldName? 0 : fieldName.hashCode());
		/*hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==targetRange? 0 : targetRange.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==selectedByDefault? 0 : selectedByDefault.hashCode());*/
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==pointer? 0 : pointer.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==pointedElementName? 0 : pointedElementName.hashCode());
		/*hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==checkForNulls? 0 : checkForNulls.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==sourceRange? 0 : sourceRange.hashCode());*/
		return hash;
	}

	/*@Override
	public int compare(Element element1, Element element2) {
		if (element1==null && element2!=null) {
			return -1;
		} else if (element1!=null && element2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(element1.locationName, element2.locationName);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(element1.martName, element2.martName);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(element1.version, element2.version);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(element1.datasetName, element2.datasetName);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(element1.configName, element2.configName);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(element1.tableName, element2.tableName);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(element1.fieldName, element2.fieldName);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(element1.targetRangeList, element2.targetRangeList);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(element1.selectedByDefault, element2.selectedByDefault);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(element1.pointer, element2.pointer);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(element1.pointedElement, element2.pointedElement);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(element1.sourceRangeList, element2.sourceRangeList);
	}

	@Override
	public int compareTo(Element element) {
		return compare(this, element);
	}*/

	public org.jdom.Element generateXml() {
		if (this instanceof Attribute) {
			return generateXml(Attribute.XML_ELEMENT_NAME);
		} else {
			return generateXml(Filter.XML_ELEMENT_NAME);
		}
	}
	
	private org.jdom.Element generateXml(String pointedElementType) {
		org.jdom.Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "location", this.locationName);
		MartConfiguratorUtils.addAttribute(element, "mart", this.martName);
		MartConfiguratorUtils.addAttribute(element, "version", this.version);
		MartConfiguratorUtils.addAttribute(element, "dataset", this.datasetName);
		MartConfiguratorUtils.addAttribute(element, "config", this.configName);
		MartConfiguratorUtils.addAttribute(element, "table", this.tableName);
		MartConfiguratorUtils.addAttribute(element, "key", this.keyName);
		MartConfiguratorUtils.addAttribute(element, "field", this.fieldName);
		MartConfiguratorUtils.addAttribute(element, "default", this.selectedByDefault);
		MartConfiguratorUtils.addAttribute(element, "pointer", this.pointer);
		MartConfiguratorUtils.addAttribute(element, pointedElementType, this.pointedElementName);
		MartConfiguratorUtils.addAttribute(element, "checkForNulls", this.checkForNulls);
		
	//if (this instanceof Attribute) {//TODO until filter is done
		if (
				/*this.pointer!=null &&	//TODO tmp because of genomic sequence*/ 
				this.pointer
				&& this.sourceRange!=null	//TODO for now -> when broken pointer, no sourceRange
		) {
			this.sourceRange.addXmlAttribute(element, "sourceRange");//TODO until is done
		}
			this.targetRange.addXmlAttribute(element, "targetRange");
	//}
		/*MartConfiguratorUtils.addAttribute(element, "sourceRangeList", this.sourceRangeList, true);
		MartConfiguratorUtils.addAttribute(element, "targetRange", this.targetRangeList, false);*/
		return element;
	}
	
	protected org.jdom.Element generateXmlForWebService() {
		return generateXmlForWebService(null);
	}
	protected org.jdom.Element generateXmlForWebService(Namespace namespace) {
		if (this instanceof Attribute) {
			return generateXmlForWebService(namespace, Attribute.XML_ELEMENT_NAME);
		} else {
			return generateXmlForWebService(namespace, Filter.XML_ELEMENT_NAME);
		}
	}
	private org.jdom.Element generateXmlForWebService(Namespace namespace, String pointedElementType) {
		org.jdom.Element jdomObject = super.generateXmlForWebService(namespace);
		
		MartConfiguratorUtils.addAttribute(jdomObject, "default", this.selectedByDefault);
		
		MartConfiguratorUtils.addAttribute(jdomObject, "pointer", this.pointer);
		MartConfiguratorUtils.addAttribute(jdomObject, pointedElementType, this.pointedElementName);
		
		return jdomObject;
	}
	protected JSONObject generateJsonForWebService() {
		JSONObject object = super.generateJsonForWebService();
		object.remove("visible");	// doesn't apply for Elements
		return object;
	}
}
