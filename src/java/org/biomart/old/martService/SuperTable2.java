package org.biomart.old.martService;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.old.martService.restFulQueries.objects.Attribute;
import org.biomart.old.martService.restFulQueries.objects.Filter;



public class SuperTable2 {
	public String martServiceServer = null;
	public String virtualSchemaName = null;
	public String datasetName = null;
	public String datasetConfigVersion = null;
	
	// Same if only one => joinAttribute
	public String leftAttributeName = null;		// must be in results
	public String leftFilterName = null;
	public String rightAttributeName = null;	// must be in results
	public String rightFilterName = null;		
	
	public List<String> otherResultAttributeNames = null;	// Must not contain left and right attribute
	
	private List<Attribute> allAttributesList = null;
	
	public SuperTable2(String martServiceServer, String virtualSchemaName, String datasetName, String datasetConfigVersion, String joinAttributeName, String joinFilterName) {
		this(martServiceServer, virtualSchemaName, datasetName, datasetConfigVersion, new ArrayList<String>(Arrays.asList(new String[] {joinAttributeName})), joinAttributeName, joinFilterName);
	}
	public SuperTable2(String martServiceServer, String virtualSchemaName, String datasetName, String datasetConfigVersion, 
			List<String> resultAttributeNames, String joinAttributeName, String joinFilterName) {
		this(martServiceServer, virtualSchemaName, datasetName, datasetConfigVersion, resultAttributeNames, joinAttributeName, joinFilterName, joinAttributeName, joinFilterName);
	}
	public SuperTable2(String martServiceServer, String virtualSchemaName, String datasetName, String datasetConfigVersion, List<String> otherResultAttributeNames, 
			String leftAttributeName, String leftFilterName, String rightAttributeName, String rightFilterName) {
		super();
		this.martServiceServer = martServiceServer;
		this.virtualSchemaName = virtualSchemaName;
		this.datasetName = datasetName;
		this.datasetConfigVersion = datasetConfigVersion;
		this.leftAttributeName = leftAttributeName;
		this.leftFilterName = leftFilterName;
		this.rightAttributeName = rightAttributeName;
		this.rightFilterName = rightFilterName;
		
		this.otherResultAttributeNames = otherResultAttributeNames;
		this.otherResultAttributeNames.remove(leftAttributeName);
		this.otherResultAttributeNames.remove(rightAttributeName);
		
		this.allAttributesList = new ArrayList<Attribute>();
		Attribute leftAttribute = new Attribute(leftAttributeName);
		if (!this.allAttributesList.contains(leftAttribute)) {
			this.allAttributesList.add(leftAttribute);
		}
		Attribute rightAttribute = new Attribute(rightAttributeName);
		if (!this.allAttributesList.contains(rightAttribute)) {
			this.allAttributesList.add(rightAttribute);
		}
		this.allAttributesList.addAll(Attribute.getAttributeList(this.otherResultAttributeNames));
	}
	public List<Attribute> getAllAttributesList() {
		return allAttributesList;
	}
	public Filter getLeftFilter(StringBuffer value) {
		return getFilter(this.leftFilterName, value);
	}
	public Filter getRightFilter(StringBuffer value) {
		return getFilter(this.rightFilterName, value);
	}
	private Filter getFilter(String filterName, StringBuffer value) {
		return new Filter(filterName, value);
	}
	@Override
	public String toString() {
		return 
		"martServiceServer = " + martServiceServer +
		", virtualSchemaName = " + virtualSchemaName +
		", datasetName = " + datasetName +
		", datasetConfigVersion = " + datasetConfigVersion + MyUtils.LINE_SEPARATOR + 
		
		MyUtils.TAB_SEPARATOR + "leftAttributeName = " + leftAttributeName +
		", leftFilterName = " + leftFilterName + MyUtils.LINE_SEPARATOR + 
		MyUtils.TAB_SEPARATOR + "rightAttributeName = " + rightAttributeName +
		", rightFilterName = " + rightFilterName +
		
		MyUtils.TAB_SEPARATOR + "otherResultAttributeNames = " + otherResultAttributeNames + MyUtils.LINE_SEPARATOR +
		
		MyUtils.TAB_SEPARATOR + "allAttributesList = " + this.allAttributesList + MyUtils.LINE_SEPARATOR; 
	}
}
