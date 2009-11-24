package org.biomart.configurator.component;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.biomart.common.resources.Resources;
import org.jdom.Element;

public class PartitionedFilterDropDown extends JComboBox {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Element filter;
	
	public PartitionedFilterDropDown(List<ArrayList<String>> data, Element filter, DsInConfigPanel dsContainer) {
		this.filter = filter;
		String name = this.filter.getAttributeValue(Resources.get("NAME"));
		int partitionCol = this.findCurrentPartitionCol(name);
		for(ArrayList<String> row: data) {
			this.addItem(row.get(partitionCol));
		}
		this.addItemListener(dsContainer);
	}
	
	
	private int findCurrentPartitionCol(String name) {
		int index1 = name.indexOf(")");
		if(index1<0)
			return 0;
		String tmpName = name.substring(0,index1);
		int index0 = tmpName.lastIndexOf(Resources.get("COLPREFIX"));
		if(index0<0)
			return 0;
		String colStr = tmpName.substring(index0+1);
		int res = 0;
		try{
			res = Integer.parseInt(colStr);
		}catch(Exception e) {
			return 0;
		}		
		return res-1;
	}	
	
	public String getPartitionTable() {
		return this.filter.getAttributeValue(Resources.get("PARTITIONTABLE"));
	}
	
}