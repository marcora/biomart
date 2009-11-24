package org.biomart.old.martService.restFulQueries.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biomart.test.linkIndicesTest.LinkIndexesUtils;


public class Filter extends Element implements Serializable {

	private static final long serialVersionUID = -8278218688261570429L;

	private final int THRESHOLD = 20;
	
	public StringBuffer value = null;
	public Filter(String name, Collection<String> collection, boolean useless) {	// useless to distinguish the 2 constructors (when used with null as 2nd param)
		this(name, LinkIndexesUtils.buildMartServiceInList(collection));
	}
	public Filter(String name, StringBuffer value) {
		this(name, String.valueOf(false), null, value); 	// not hidden by default
	}
	public Filter(String name, String hidden, /*String tableName, String fieldName, String keyName*/ Field field, StringBuffer value) {
		super(name, hidden, field);
		this.value = value;
	}
	public static List<Filter> getFilterList(Map<String, StringBuffer> namesAndValues) {
		List<Filter> filterList = new ArrayList<Filter>();
		if (null!=namesAndValues) {
			for (Iterator<String> it = namesAndValues.keySet().iterator(); it.hasNext();) {
				String name = it.next();
				StringBuffer value = namesAndValues.get(name);
				filterList.add(new Filter(name, value));
			}
		}
		return filterList;
	}
	@Override
	public String toString() {
		//return super.toString() + ", value = " + value;
		return toShortString();
	}
	public String toShortString() {
		if (value==null || value.length()<(2*THRESHOLD)) {
			return super.toString() + ", value = " + value;
		} else {
			int size = value.length();
			return super.toString() + ", value = " + value.substring(0, THRESHOLD) + "[...]" + value.substring(size-THRESHOLD);
		}
	}
}
