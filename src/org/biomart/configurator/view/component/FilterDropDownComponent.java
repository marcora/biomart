package org.biomart.configurator.view.component;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.biomart.configurator.model.object.McFilterObject;


public class FilterDropDownComponent extends JComboBox {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private McFilterObject filterObject;
	
	public FilterDropDownComponent(McFilterObject model) {
		this.filterObject = model;
		init();
	}
	
	private void init() {
		DefaultComboBoxModel cbModel = new DefaultComboBoxModel(this.filterObject.getPartitionTable().getCol(0).toArray());
		this.setModel(cbModel);
	}
	
}