package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.martRemote.Jsoml;
import org.biomart.objects.MartConfiguratorUtils;


public class Attribute extends Element implements Serializable {

	private static final long serialVersionUID = 3472755898394368045L;
	
	public static final String XML_ELEMENT_NAME = "attribute";
	public static final McNodeType MC_NODE_TYPE = McNodeType.Attribute;
	
	public static void main(String[] args) {}

	protected String tableName = null;		//TODO consider using the RelationInfo object instead?
	protected String keyName = null;
	protected String fieldName = null;
	
	private Integer maxLength = null;
	private String linkURL = null;

	public Attribute() {}
	public Attribute(PartitionTable mainPartitionTable, String name) {
		this(mainPartitionTable, name, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
	}
	public Attribute(PartitionTable mainPartitionTable, String name, String displayName, String description, Boolean visible, 
			String locationName, String martName, Integer version, String datasetName, String configName, String tableName, String keyName, String fieldName, 
			List<String> targetRangeList, Boolean selectedByDefault, Boolean pointer, String pointedElementName, Boolean checkForNulls, 
			List<String> sourceRangeList,
			Integer maxLength, String linkURL) {
		super(mainPartitionTable, name, displayName, description, visible, XML_ELEMENT_NAME,
				locationName, martName, version, datasetName, configName, targetRangeList, selectedByDefault, 
				pointer, pointedElementName, checkForNulls, sourceRangeList);
		this.tableName = tableName;
		this.keyName = keyName;
		this.fieldName = fieldName;
		this.maxLength = maxLength;
		this.linkURL = linkURL;
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

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public String getLinkURL() {
		return linkURL;
	}

	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	public void setLinkURL(String linkURL) {
		this.linkURL = linkURL;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"tableName = " + tableName + ", " +
			"keyName = " + keyName + ", " +
			"fieldName = " + fieldName + ", " +
			"maxLength = " + maxLength + ", " +
			"linkURL = " + linkURL;
	}
	
	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		Attribute attribute=(Attribute)object;
		return (
				super.equals(attribute) &&
				
				(this.tableName==attribute.tableName || (this.tableName!=null && tableName.equals(attribute.tableName))) &&
				(this.keyName==attribute.keyName || (this.keyName!=null && keyName.equals(attribute.keyName))) &&
				(this.fieldName==attribute.fieldName || (this.fieldName!=null && fieldName.equals(attribute.fieldName)))
		);
	}
	
	public String toSqlString() throws FunctionalException {
		if (super.pointer) {
			throw new FunctionalException("Cannot invoke method from pointer");
		}
		return this.tableName + "." + this.fieldName;
	}
	
	public org.jdom.Element generateXml() throws FunctionalException {
		
		org.jdom.Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "table", this.tableName);
		MartConfiguratorUtils.addAttribute(element, "key", this.keyName);
		MartConfiguratorUtils.addAttribute(element, "field", this.fieldName);
		MartConfiguratorUtils.addAttribute(element, "maxLength", this.maxLength);
		MartConfiguratorUtils.addAttribute(element, "linkURL", this.linkURL);
		
		return element;
	}
	
	
	
	
	
	
	// ===================================== Should be a different class ============================================

	public Attribute(Attribute attribute, Part part) {	// creates a light clone (temporary solution)
		super(attribute, part);
		this.maxLength = attribute.maxLength;
		this.linkURL = MartConfiguratorUtils.replacePartitionReferencesByValues(attribute.linkURL, part);
	}

	public Jsoml generateOutputForWebService(boolean xml) throws FunctionalException {
		Jsoml jsoml = super.generateOutputForWebService(xml);
		
		jsoml.setAttribute("maxLength", this.maxLength);
		jsoml.setAttribute("linkURL", this.linkURL);
		
		return jsoml;
	}
}
