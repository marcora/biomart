package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldAttribute extends OldElement {

	public static void main(String[] args) {}
	
	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"source", "linkoutURL", "maxLength", "default",
			"pointerAttribute"
	}));
	
	protected String source = null;
	protected String linkoutURL = null;
	protected Integer maxLength = null;
	protected Boolean default_ = null;
	
	protected OldAttribute(Element jdomElement) throws FunctionalException {
		this(jdomElement, 
				jdomElement.getAttributeValue("source"),
				jdomElement.getAttributeValue("linkoutURL"),
				jdomElement.getAttributeValue("maxLength"),
				jdomElement.getAttributeValue("default"),
				jdomElement.getAttributeValue("pointerAttribute")
		);
	}
	
	private OldAttribute(Element jdomElement, String source, String linkoutURL, String maxLength, String default_, String pointerAttribute) throws FunctionalException {
		super(true, jdomElement, pointerAttribute);
		
		this.source = source;
		this.linkoutURL = linkoutURL;
		this.maxLength = TransformationUtils.getIntegerValueFromString(maxLength, "maxLength");
		this.default_ = TransformationUtils.getBooleanValueFromString(default_, "default");
	}
	
	public String getSource() {
		return source;
	}

	public String getLinkoutURL() {
		return linkoutURL;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public Boolean getDefault_() {
		return default_;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setLinkoutURL(String linkoutURL) {
		this.linkoutURL = linkoutURL;
	}

	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	public void setDefault_(Boolean default_) {
		this.default_ = default_;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " +
			"source = " + source + ", " +
			"linkoutURL = " + linkoutURL + ", " +
			"maxLength = " + maxLength + ", " +
			"default = " + default_;
	}
	
	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		OldAttribute oldAttribute=(OldAttribute)object;
		return (
			(this.source==oldAttribute.source || (this.source!=null && source.equals(oldAttribute.source))) &&
			(this.linkoutURL==oldAttribute.linkoutURL || (this.linkoutURL!=null && linkoutURL.equals(oldAttribute.linkoutURL))) &&
			(this.maxLength==oldAttribute.maxLength || (this.maxLength!=null && maxLength.equals(oldAttribute.maxLength))) &&
			(this.default_==oldAttribute.default_ || (this.default_!=null && default_.equals(oldAttribute.default_)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==source? 0 : source.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==linkoutURL? 0 : linkoutURL.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==maxLength? 0 : maxLength.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==default_? 0 : default_.hashCode());
		return hash;
	}

	/*@Override
	public int compare(OldAttribute oldAttribute1, OldAttribute oldAttribute2) {
		if (oldAttribute1==null && oldAttribute2!=null) {
			return -1;
		} else if (oldAttribute1!=null && oldAttribute2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(oldAttribute1.source, oldAttribute2.source);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldAttribute1.linkoutURL, oldAttribute2.linkoutURL);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldAttribute1.maxLength, oldAttribute2.maxLength);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldAttribute1.default, oldAttribute2.default);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(oldAttribute1.oldSpecificAttributeContentList, oldAttribute2.oldSpecificAttributeContentList);
	}

	@Override
	public int compareTo(OldAttribute oldAttribute) {
		return compare(this, oldAttribute);
	}*/
	
	/**
	 * Meant to be overriden!!! TODO
	 *//*
	public Attribute transform() {
		MyUtils.errorProgram();
		return null;
	};
	public void update(Attribute attribute) {
		MyUtils.errorProgram();
	};*/
}