package org.biomart.objects.objects;


import java.io.Serializable;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.configurator.utils.type.McNodeType;
import org.biomart.martRemote.Jsoml;
import org.biomart.objects.MartConfiguratorUtils;


public class Attribute extends Element implements /*Comparable<Attribute>, Comparator<Attribute>, */Serializable {

	private static final long serialVersionUID = 3472755898394368045L;
	
	public static final String XML_ELEMENT_NAME = "attribute";
	public static final McNodeType MC_NODE_TYPE = McNodeType.Attribute;
	
	public static void main(String[] args) {}

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

	/*@Override
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
	}*/
	
	public org.jdom.Element generateXml() {
		
		org.jdom.Element element = super.generateXml();
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
