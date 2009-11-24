package org.biomart.transformation.helpers;


import org.biomart.common.general.exceptions.FunctionalException;
import org.jdom.Element;

public enum ElementChildrenType {
	SPECIFIC_ATTRIBUTE_CONTENT	(TransformationConstants.SPECIFIC_ATTRIBUTE_CONTENT_TYPE),
	
	SPECIFIC_FILTER_CONTENT		(TransformationConstants.SPECIFIC_FILTER_CONTENT_TYPE),	
	EMPTY_SPECIFIC_FILTER_CONTENT	(TransformationConstants.EMPTY_SPECIFIC_FILTER_CONTENT_TYPE),
	
	SPECIFIC_OPTION_CONTENT		(TransformationConstants.SPECIFIC_OPTION_CONTENT_TYPE),
	
	OPTION_FILTER				(TransformationConstants.OPTION_FILTER_TYPE),
	OPTION_VALUE				(TransformationConstants.OPTION_VALUE_TYPE),
	
	PUSH_ACTION					(TransformationConstants.PUSH_ACTION_TYPE);
	
	private String xmlName = null;
	private ElementChildrenType(String xmlName) {
		this.xmlName = xmlName;
	}
	public String getXmlName() {
		return xmlName;
	}
	public static ElementChildrenType getElementChildrenType(Element element) throws FunctionalException {
		String elementName = element.getName();
		for (ElementChildrenType elementChildrenTypeTmp : values()) {
			if (elementName.equals(elementChildrenTypeTmp.getXmlName())) {
				boolean match = false;
				if (ElementChildrenType.OPTION_FILTER.equals(elementChildrenTypeTmp) || 
						ElementChildrenType.OPTION_VALUE.equals(elementChildrenTypeTmp) || 
						ElementChildrenType.EMPTY_SPECIFIC_FILTER_CONTENT.equals(elementChildrenTypeTmp) || 
						ElementChildrenType.SPECIFIC_FILTER_CONTENT.equals(elementChildrenTypeTmp)) {
					if (ElementChildrenType.OPTION_FILTER.equals(elementChildrenTypeTmp) && 
							!TransformationUtils.isOptionValue(element)) {
						 match = true;
					} else if (ElementChildrenType.OPTION_VALUE.equals(elementChildrenTypeTmp) && 
							TransformationUtils.isOptionValue(element)) {
						match = true;
					} else if (ElementChildrenType.EMPTY_SPECIFIC_FILTER_CONTENT.equals(elementChildrenTypeTmp) && 
							TransformationUtils.isEmptySpecificElementContent(element)) {
						 match = true;
					} else if (ElementChildrenType.SPECIFIC_FILTER_CONTENT.equals(elementChildrenTypeTmp) && 
							!TransformationUtils.isEmptySpecificElementContent(element)) {
						match = true;
					}
				} else {
					match = true;
				}
				if (match) {
					return elementChildrenTypeTmp;
				}
			}
		}
		throw new FunctionalException("Unknown/unhandled element child: " + elementName);
	}
}
