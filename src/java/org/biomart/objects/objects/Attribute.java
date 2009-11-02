package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.List;

import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Namespace;


public class Attribute extends Element implements /*Comparable<Attribute>, Comparator<Attribute>, */Serializable {

	private static final long serialVersionUID = 3472755898394368045L;
	
	public static final String XML_ELEMENT_NAME = "attribute";
	
	public static void main(String[] args) {}

	private Integer maxLength = null;
	private String linkURL = null;

	public Attribute() {}
	public Attribute(Container parentContainer, PartitionTable mainPartitionTable, String name) {
		this(parentContainer, mainPartitionTable, name, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
	}
	public Attribute(Container parentContainer, PartitionTable mainPartitionTable, String name, String displayName, String description, Boolean visible, 
			String locationName, String martName, Integer version, String datasetName, String configName, String tableName, String keyName, String fieldName, 
			List<String> targetRangeList, Boolean selectedByDefault, Boolean pointer, String pointedElementName, Boolean checkForNulls, 
			List<String> sourceRangeList,
			Integer maxLength, String linkURL) {
		super(mainPartitionTable, name, displayName, description, visible, XML_ELEMENT_NAME, parentContainer,
				locationName, martName, version, datasetName, configName, tableName, keyName, fieldName, targetRangeList, selectedByDefault, 
				pointer, pointedElementName, checkForNulls, sourceRangeList);
		this.maxLength = maxLength;
		this.linkURL = linkURL;
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
				(this.maxLength==attribute.maxLength || (this.maxLength!=null && maxLength.equals(attribute.maxLength))) &&
				(this.linkURL==attribute.linkURL || (this.linkURL!=null && linkURL.equals(attribute.linkURL)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==maxLength? 0 : maxLength.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==linkURL? 0 : linkURL.hashCode());
		return hash;
	}

	/*public int compare(Attribute attribute1, Attribute attribute2) {
		if (attribute1==null && attribute2!=null) {
			return -1;
		} else if (attribute1!=null && attribute2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(attribute1.maxLength, attribute2.maxLength);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(attribute1.linkURL, attribute2.linkURL);
	}

	public int compareTo(Attribute attribute) {
		return compare(this, attribute);
	}*/
	
	public org.jdom.Element generateXml() {
		
		org.jdom.Element element = super.generateXml();
		MartConfiguratorUtils.addAttribute(element, "maxLength", this.maxLength);
		MartConfiguratorUtils.addAttribute(element, "linkURL", this.linkURL);
		return element;
	}
	
	
	
	
	// ===================================== Should be a different class ============================================

	public Attribute(Attribute attribute, Part part) throws CloneNotSupportedException {	// creates a light clone (temporary solution)
		super(attribute, part);
		this.maxLength = attribute.maxLength;
		this.linkURL = MartConfiguratorUtils.replacePartitionReferencesByValues(attribute.linkURL, part);
	}
	
	public org.jdom.Element generateXmlForWebService() throws FunctionalException {
		return generateXmlForWebService(null);
	}
	public org.jdom.Element generateXmlForWebService(Namespace namespace) throws FunctionalException {
		org.jdom.Element jdomObject = super.generateXmlForWebService(namespace);
		
		MartConfiguratorUtils.addAttribute(jdomObject, "maxLength", this.maxLength);
		MartConfiguratorUtils.addAttribute(jdomObject, "linkURL", this.linkURL);
		
		return jdomObject;
	}
	public JSONObject generateJsonForWebService() {
		JSONObject jsonObject = super.generateJsonForWebService();
		
		JSONObject object = (JSONObject)jsonObject.get(super.xmlElementName);
		object.put("maxLength", this.maxLength);
		object.put("linkURL", this.linkURL);
		
		jsonObject.put(super.xmlElementName, object);
		return jsonObject;
	}
}
