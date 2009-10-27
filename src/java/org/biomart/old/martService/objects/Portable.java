package org.biomart.old.martService.objects;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.comparators.StringComparator;
import org.biomart.old.martService.restFulQueries.objects.Element;


public class Portable implements Serializable, Comparable<Portable>, Comparator<Portable>{

	private static final long serialVersionUID = 1547033344749807943L;
	
	public String linkName = null;
	public String linkType = null;
	public String linkVersion = null;
	public Boolean defaultValue = null;
	public List<String> elementNamesList = null;
	protected List<Element> elementsList = null;
	protected Boolean missingElement = null;
	protected Boolean completeElementList = null;
	private Portable() {}	// For comparator only
		
	protected Portable(String linkName, String linkType, String linkVersion, String defaultValue, String[] list) {
		super();
		this.linkName = linkName;
		this.linkType = linkType;
		this.linkVersion = linkVersion==null ? "" : linkVersion;
		this.defaultValue = (defaultValue!=null && defaultValue.equals("1"));
		this.elementNamesList = Arrays.asList(list);
		Collections.sort(this.elementNamesList);
	}
	@Override
	public String toString() {
		return "{" +  
		"linkName = " + linkName + ", " + 
		"linkType = " + linkType + ", " + 
		"linkVersion = " + linkVersion + ", " + 
		"defaultValue = " + defaultValue + ", " + 
		"elementList = " + elementNamesList +
		"}";
	}
	public String toShortString() {
		return linkName + MyUtils.TAB_SEPARATOR + 
		linkType + MyUtils.TAB_SEPARATOR + 
		linkVersion + MyUtils.TAB_SEPARATOR + 
		defaultValue + MyUtils.TAB_SEPARATOR + 
		elementNamesList + MyUtils.TAB_SEPARATOR;
	}
	
	public boolean sameName(Portable impExp) {
		return this.linkName.equalsIgnoreCase(impExp.linkName);
	}
	public boolean sameLength(Portable impExp) {
		return this.elementNamesList.size()==impExp.elementNamesList.size();
	}
	public boolean sameVersionIfBothHaveVersion(Portable impExp) {
		return
		this.linkVersion==null || impExp.linkVersion==null || MyUtils.isEmpty(this.linkVersion) || MyUtils.isEmpty(impExp.linkVersion) || 
		(!MyUtils.isEmpty(this.linkVersion) && !MyUtils.isEmpty(impExp.linkVersion) && this.linkVersion.equalsIgnoreCase(impExp.linkVersion));
	}
	
	public boolean bothNonEmptyLinkVersion(Portable impExp) {
		return !MyUtils.isEmpty(this.linkVersion) && !MyUtils.isEmpty(impExp.linkVersion);
	}
	public boolean sameNonEmptyLinkVersion(Portable impExp) {
		return !MyUtils.isEmpty(this.linkVersion) && !MyUtils.isEmpty(impExp.linkVersion) && this.linkVersion.equalsIgnoreCase(impExp.linkVersion);
	}
	public boolean oneAndOnlyOneNonEmptyLinkVersion(Portable impExp) {
		return (!MyUtils.isEmpty(this.linkVersion) && MyUtils.isEmpty(impExp.linkVersion)) || 
		(MyUtils.isEmpty(this.linkVersion) && !MyUtils.isEmpty(impExp.linkVersion));
	}
	public boolean noLinkVersion(Portable impExp) {
		return MyUtils.isEmpty(this.linkVersion) && MyUtils.isEmpty(impExp.linkVersion);
	}

	public boolean bothDefaultValueTrue(Portable impExp) {
		return this.defaultValue.equals(Boolean.TRUE) && impExp.defaultValue.equals(Boolean.TRUE);
	}
	public boolean oneAndOnlyOneDefaultValueTrue(Portable impExp) {
		return this.defaultValue.equals(Boolean.FALSE) && impExp.defaultValue.equals(Boolean.TRUE) ||
		this.defaultValue.equals(Boolean.TRUE) && impExp.defaultValue.equals(Boolean.FALSE);
	}
	public boolean bothDefaultValueFalse(Portable impExp) {
		return this.defaultValue.equals(Boolean.FALSE) && impExp.defaultValue.equals(Boolean.FALSE);
	}
	@Override
	public boolean equals(Object arg0) {
		Portable portable = (Portable)arg0;
		return this.linkName.equals(portable.linkName) &&
		this.linkType.equals(portable.linkType) && 
		this.linkVersion.equals(portable.linkVersion) && 
		this.defaultValue.equals(portable.defaultValue) && 
		this.elementNamesList.equals(portable.elementNamesList);
	}
	
	public int compareTo(Portable portable) {
		return compare(this, portable);
	}
	public int compare(Portable portable1, Portable portable2) {
    	int compare = portable1.linkName.compareTo(portable2.linkName);
		if (compare!=0) {
			return compare;
		}
		compare = portable1.linkType.compareTo(portable2.linkType);
		if (compare!=0) {
			return compare;
		}
		compare = portable1.linkVersion.compareTo(portable2.linkVersion);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(portable1.defaultValue, portable2.defaultValue);
		if (compare!=0) {
			return compare;
		}
		compare = portable1.defaultValue.compareTo(portable2.defaultValue);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareList(portable1.elementNamesList, portable2.elementNamesList, new StringComparator());
	}

	public List<Element> getElementsList() {
		return elementsList;
	}

	public Boolean getCompleteElementList() {
		return completeElementList;
	}

	public Boolean getMissingElement() {
		return missingElement;
	}
}
