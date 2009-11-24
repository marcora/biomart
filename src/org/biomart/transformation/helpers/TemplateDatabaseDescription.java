package org.biomart.transformation.helpers;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class TemplateDatabaseDescription implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2966880364499625677L;
	private List<String> tableList = null;
	private Map<String, List<String>> tableColumnMap = null;
	public TemplateDatabaseDescription(List<String> tableList) {
		super();
		this.tableList = tableList;
	}
	public void setTableColumnMap(Map<String, List<String>> tableColumnMap) {
		this.tableColumnMap = tableColumnMap;
	}
	public Map<String, List<String>> getTableColumnMap() {
		return tableColumnMap;
	}
	public List<String> getTableList() {
		return tableList;
	}
}
