package org.biomart.old.martService.objects;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.old.martService.restFulQueries.objects.Attribute;
import org.biomart.old.martService.restFulQueries.objects.Element;
import org.biomart.old.martService.restFulQueries.objects.Field;
import org.biomart.old.martService.restFulQueries.objects.Filter;




public class Importable extends Portable {

	private static final long serialVersionUID = -3727514020380066405L;

	private String firstAttributeName = null;	// if has an importable (so we can query on it)
	private List<Attribute> attibutesList = null;
	private Boolean completeAttributesList = null;
	private List<Field> fieldList = null;
	
	public Importable(String linkName, String linkType, String linkVersion, String defaultValue, 
			String firstAttributeName, String[] elementList, Map<String, Element> attributesByNameMap, Map<String, Element> filtersByNameMap) {
		super(linkName, linkType, linkVersion, defaultValue, elementList);
		this.firstAttributeName = firstAttributeName;
		super.elementsList = new ArrayList<Element>();
		super.completeElementList = true;
		super.missingElement = false;
		for (String filterName : this.elementNamesList) {
			Filter filter = (Filter)filtersByNameMap.get(filterName.toLowerCase());	// to lower case because a few exceptions
			if (null==filter) {
				filter = new Filter(filterName, null);
				super.completeElementList = false;
				super.missingElement = true;
//System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");				
			} else if (!filter.field.isValid()) {
				super.completeElementList = false;
			}
			super.elementsList.add(filter);
			//super.elementsList.add(new Filter(filterName, null));
		}
		this.completeAttributesList = false;
		if (super.completeElementList) {
			populateAttributesList(attributesByNameMap, filtersByNameMap);
			populateFieldList();
		}
	}

	private void populateFieldList() {
		this.fieldList = new ArrayList<Field>();
		for (Attribute attribute : this.attibutesList) {
			this.fieldList.add(attribute.field);
		}
		Collections.sort(this.fieldList);
	}

	private void populateAttributesList(Map<String, Element> attributesByNameMap, Map<String, Element> filtersByNameMap) {
		this.attibutesList = new ArrayList<Attribute>();
		this.completeAttributesList = true;
		for (String filterName : super.elementNamesList) {
			Filter filter = (Filter)filtersByNameMap.get(filterName);
			if (null==filter) {
				completeAttributesList = false;
				break;
			} else {
				boolean hasCounterpart = false;
				for (Iterator<Element> it = attributesByNameMap.values().iterator(); it.hasNext();) {
					Attribute attribute = (Attribute)it.next();					
					if (filter.field.equals(attribute.field)) {
						this.attibutesList.add(attribute);
						hasCounterpart = true;
						break;
					}
				}
				if (!hasCounterpart) {
					completeAttributesList = false;
					break;
				}
			}
		}
	}	

	public boolean isOtherDirectionOf(Exportable exportable) {
		return 
		this.linkName.equals(exportable.linkName) &&
		this.linkType.equals(exportable.linkType) && 
		this.linkVersion.equals(exportable.linkVersion) && 
		this.defaultValue.equals(exportable.defaultValue)
		&& this.elementNamesList.size()==exportable.elementNamesList.size()
		/*&& ((!complete && this.linkName.equals(exportable.linkName)) || 
			(complete && 0==CompareUtils.compareList(this.getFieldList(), exportable.getFieldList(), new Field())))*/
		;
	}
	public boolean isOtherDirectionOf2(Exportable exportable) {
		
		/*if (this.completeAttributesList) {
			System.out.println(this.toString());
			System.out.println();
			System.out.println(exportable.toString());
			System.out.println();
			System.out.println(this.getImportableStamp());
			System.out.println();
			System.out.println(exportable.getExportableStamp());
			System.out.println();
			
			if (this.getImportableStamp().equals(exportable.getExportableStamp())) {			
				System.out.println("a");
				MyUtils.pressKeyToContinue();
			} else {
				System.out.println("b");
				//MyUtils.pressKeyToContinue();
			}
		}*/
		
		boolean complete = this.completeAttributesList && this.completeElementList && exportable.completeElementList;
	
		/*if (complete) {
System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		} else {
System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		}*/
		
if (complete) {
	c6++;
	if (this.linkType.equals(exportable.linkType) && 
			this.linkVersion.equals(exportable.linkVersion) && 
			this.defaultValue.equals(exportable.defaultValue) && 
			this.elementNamesList.size()==exportable.elementNamesList.size()) {
/*		System.out.println(this.getFieldList());
		System.out.println(exportable.getFieldList());
		System.out.println(CompareUtils.compareList(this.getFieldList(), exportable.getFieldList(), new Field()));*/
		//MyUtils.pressKeyToContinue();
	}
	c1++;
} else {
	c7++;	
}
		
if (true) {	
	if (this.linkType.equals(exportable.linkType) && 
			this.linkVersion.equals(exportable.linkVersion) && 
			this.defaultValue.equals(exportable.defaultValue) && 
			this.elementNamesList.size()==exportable.elementNamesList.size()
			&& complete && 0==CompareUtils.compareList(this.getFieldList(), exportable.getFieldList(), new Field())) {
		c1++;
	} else if (this.linkType.equals(exportable.linkType) && 
			this.linkVersion.equals(exportable.linkVersion) && 
			this.defaultValue.equals(exportable.defaultValue) && 
			this.elementNamesList.size()==exportable.elementNamesList.size()
			&& ((!complete && this.linkName.equals(exportable.linkName)))) {
		c2++;
	} else {
		c3++;
	}
} else {
	if (this.linkName.equals(exportable.linkName) &&
			this.linkType.equals(exportable.linkType) && 
			this.linkVersion.equals(exportable.linkVersion) && 
			this.defaultValue.equals(exportable.defaultValue) && 
			this.elementNamesList.size()==exportable.elementNamesList.size()) {
		c4++;
	} else {
		c5++;
	}
}

/*
c1 = 13
c2 = 959
c3 = 233	
*/		
		return 
		/*this.linkName.equals(exportable.linkName) &&*/
		this.linkType.equals(exportable.linkType) && 
		this.linkVersion.equals(exportable.linkVersion) && 
		this.defaultValue.equals(exportable.defaultValue)
		&& this.elementNamesList.size()==exportable.elementNamesList.size()
		&& (
				(!complete && this.linkName.equals(exportable.linkName)) || 
			(complete && 0==CompareUtils.compareList(this.getFieldList(), exportable.getFieldList(), new Field())))
		;
	}
public static int c1 = 0;
public static int c2 = 0;
public static int c3 = 0;
public static int c4 = 0;
public static int c5 = 0;

public static int c6 = 0;
public static int c7 = 0;
	/*public String getImportableStamp() {
		StringBuffer stamp = new StringBuffer();
		for (Attribute attribute : this.attibutesList) {
			stamp.append(attribute.getStamp() + ExportableData.INTER_LINK_SIDE_SEPARATOR);
		}
		return stamp.toString();
	}*/
	public List<Field> getFieldList() {
		return this.fieldList;
	}

	public List<Filter> getFiltersList() {
		List<Filter> fitlersList = new ArrayList<Filter>();
		for (Element element : super.elementsList) {
			fitlersList.add((Filter) element);
		}
		return fitlersList;
	}


	public String getFirstAttributeName() {
		return firstAttributeName;
	}


	public void setFirstAttributeName(String firstAttributeName) {
		this.firstAttributeName = firstAttributeName;
	}


	public List<Attribute> getAttibutesList() {
		return attibutesList;
	}
	public List<String> getAttributeNamesList() {
		List<String> list = new ArrayList<String>();
		for (Attribute attribute : attibutesList) {
			list.add(attribute.internalName);
		}
		return list;
	}


	public Boolean getCompleteAttributesList() {
		return completeAttributesList;
	}
}
