package org.biomart.configurator.model.object;

import javax.swing.JComponent;

import org.biomart.builder.model.Column;
import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.utils.type.McNodeType;

public class Filter extends McNode{

	private Column column;
	private String targetTableName;
	private String sourceTableName;
	private String sourceColumnName;
	private String targetColumnName;
	
	public Filter(JDomNodeAdapter node) {
		this.node = node;
		this.type = McNodeType.Filter;
	}
	
	@Override
	//no gui in filter
	public JComponent getGuiComponent() {
		return null;
	}
	
	
}