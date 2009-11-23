package org.biomart.objects.lite;

import java.io.Serializable;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Part;

public class LiteAttribute extends LiteMartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 7606339732698858011L;
	
	public static final String XML_ELEMENT_NAME = "attribute";

	private Boolean selectedByDefault = null;
	
	private Integer maxLength = null;
	private String linkURL = null;
	
	public LiteAttribute(Attribute attribute, Part part) {
		
		super(XML_ELEMENT_NAME,
			part==null ? attribute.getName() : 
				MartConfiguratorUtils.replacePartitionReferencesByValues(attribute.getName(), part),
			part==null ? attribute.getDisplayName() : 
				MartConfiguratorUtils.replacePartitionReferencesByValues(attribute.getDisplayName(), part),
			part==null ? attribute.getDescription() : 
				MartConfiguratorUtils.replacePartitionReferencesByValues(attribute.getDescription(), part),
				null);	// irrelevant for elements
		
		this.selectedByDefault = attribute.getSelectedByDefault();
		
		this.maxLength = attribute.getMaxLength();
		this.linkURL = MartConfiguratorUtils.replacePartitionReferencesByValues(attribute.getLinkURL(), part);
	}
	
	// Properties in super class available for this light object
	public String getDisplayName() {
		return super.displayName;
	}
	public String getDescription() {
		return super.description;
	}

	public Boolean getSelectedByDefault() {
		return selectedByDefault;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public String getLinkURL() {
		return linkURL;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " +
			"selectedByDefault = " + selectedByDefault + ", " +
			"maxLength = " + maxLength + ", " +
			"linkURL = " + linkURL;
	}
	
	@Override
	protected Jsoml generateExchangeFormat(boolean xml) throws FunctionalException {

		Jsoml jsoml = new Jsoml(xml, super.xmlElementName);
		
		jsoml.setAttribute("name", super.name);
		jsoml.setAttribute("displayName", super.displayName);
		jsoml.setAttribute("description", super.description);
		
		jsoml.setAttribute("default", this.selectedByDefault);
		
		jsoml.setAttribute("maxLength", this.maxLength);
		jsoml.setAttribute("linkURL", this.linkURL);
		
		return jsoml;
	}
}
