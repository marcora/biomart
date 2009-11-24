package org.biomart.old.martService.restFulQueries.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Attribute extends Element implements Serializable {

	private static final long serialVersionUID = 1593224494295571342L;
	
	public Attribute(String name) {
		super(name, String.valueOf(false), null);		// not hidden by default
	}
	public Attribute(String name, String hidden, /*String tableName, String fieldName, String keyName*/ Field field) {
		super(name, hidden, field);
	}
	public static List<Attribute> getAttributeList(List<String> names) {
		List<Attribute> attributeList = new ArrayList<Attribute>();
		if (null!=names) {
			for (String name : names) {
				attributeList.add(new Attribute(name));
			}
		}
		return attributeList;
	}
	/*public String getStamp() {
		return tableConstraint + ExportableData.INTRA_LINK_SIDE_SEPARATOR + keyName + ExportableData.INTRA_LINK_SIDE_SEPARATOR + fieldName;
	}*/
}
