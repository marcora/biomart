package org.biomart.configurator.jdomUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.biomart.common.resources.Resources;
import org.jdom.Element;



public class McViewsFilter implements McFilter {

	private Map<String,HashMap<String, String>> filters;
	/**
	 * if true, the node will not show
	 */
	public boolean isFiltered(JDomNodeAdapter obj) { 
	    String name = obj.getNode().getName();
	    if(filters.containsKey(name)) {
	    	HashMap<String, String> conditions = filters.get(name);
	    	if(conditions==null)
	    		return true;
	    	else 
	    		return !isExclusived(obj.getNode(), conditions);
	    	
	    }
	    return false;
	}

	/**
	 * if true, the node will show 
	 * @param node
	 * @param conditions
	 * @return
	 */
	private boolean isExclusived(Element node, Map<String, String> conditions) {
		boolean found = true;
		Iterator<Entry<String, String>> i = conditions.entrySet().iterator();
		while(i.hasNext() && found) {
			Entry<String, String> entry = i.next();
			String attribute = entry.getKey();
			String value = entry.getValue();
			//if the attribute is null, return true;
			if(node.getAttributeValue(attribute)==null)
				return true;
			//handle user
			if(attribute.equals(Resources.get("USER"))) {
				String user = node.getAttributeValue(attribute);
				String[] userArray = user.split(Resources.get("colonseparator"));
				if(!Arrays.asList(userArray).contains(value))
					found = false;
			} else if(!node.getAttributeValue(attribute).equals(value)) {
				found = false;
			}
		}
		return found;
	}

	public McViewsFilter(Map<String, HashMap<String, String>> filterCondition) {
		this.filters = filterCondition;
	}
}
