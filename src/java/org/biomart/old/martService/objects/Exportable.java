package org.biomart.old.martService.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biomart.old.martService.restFulQueries.objects.Attribute;
import org.biomart.old.martService.restFulQueries.objects.Element;
import org.biomart.old.martService.restFulQueries.objects.Field;
import org.biomart.old.martService.restFulQueries.objects.Filter;




public class Exportable extends Portable {

	private static final long serialVersionUID = 3773793335095182640L;

	private Boolean completeFiltersList = null;
	private List<Filter> filtersList = null;
	private List<Field> fieldList = null;
	
	public Exportable(String linkName, String linkType, String linkVersion, String defaultValue, String[] list, 
			Map<String, Element> attributesByNameMap, Map<String, Element> filtersByNameMap) {
		super(linkName, linkType, linkVersion, defaultValue, list);
		super.elementsList = new ArrayList<Element>();
		super.completeElementList = true;
		super.missingElement = false;
		for (String attributeName : this.elementNamesList) {
			Attribute attribute = (Attribute)attributesByNameMap.get(attributeName.toLowerCase());	// to lower case because a few exceptions
			if (null==attribute) {
				attribute = new Attribute(attributeName);
				super.completeElementList = false;
				super.missingElement = true;
//System.out.println("////////////////////////////////////////////////////////////////////////////////////////////");				
			} else if (!attribute.field.isValid()) {
				super.completeElementList = false;
			}
			super.elementsList.add(attribute);
		}

		this.completeFiltersList = false;
		if (super.completeElementList) {
			populateFiltersList(attributesByNameMap, filtersByNameMap);
			populateFieldList();
		}
	}
	
	private void populateFiltersList(Map<String, Element> attributesByNameMap, Map<String, Element> filtersByNameMap) {
		this.filtersList = new ArrayList<Filter>();
		this.completeFiltersList = true;
		for (String attributeName : super.elementNamesList) {
			Attribute attribute = (Attribute)attributesByNameMap.get(attributeName);
			if (null==attribute) {
				completeFiltersList = false;
				break;
			} else {
				boolean hasCounterpart = false;
				for (Iterator<Element> it = filtersByNameMap.values().iterator(); it.hasNext();) {
					Filter filter = (Filter)it.next();					
					if (attribute.field.equals(filter.field)) {
						this.filtersList.add(filter);
						hasCounterpart = true;
						break;
					}
				}
				if (!hasCounterpart) {
					completeFiltersList = false;
					break;
				}
			}
		}
	}	
	
	private void populateFieldList() {
		this.fieldList = new ArrayList<Field>();
		for (Element element : this.elementsList) {
			Field field = ((Attribute)element).field;
			this.fieldList.add(field);
//System.out.println(((Attribute)element).field);
		}
//System.out.println("sort");
		Collections.sort(this.fieldList);
	}
	public List<Attribute> getAttributesList() {
		List<Attribute> attributesList = new ArrayList<Attribute>();
		for (Element element : super.elementsList) {
			attributesList.add((Attribute) element);
		}
		return attributesList;
	}

	/*public String getExportableStamp() {
		StringBuffer stamp = new StringBuffer();
		for (Element element : this.elementsList) {
			stamp.append(((Attribute)element).getStamp() + ExportableData.INTER_LINK_SIDE_SEPARATOR);
		}
		return stamp.toString();
	}*/
	public List<Field> getFieldList() {
		return this.fieldList;
	}

	public Boolean getCompleteFiltersList() {
		return completeFiltersList;
	}
	public List<Filter> getFiltersList() {
		return filtersList;
	}
	public List<String> getFilterNamesList() {
		List<String> list = new ArrayList<String>();
		for (Filter filter : filtersList) {
			list.add(filter.internalName);
		}
		return list;
	}
}
