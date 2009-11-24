package org.biomart.configurator.model.object;

import javax.swing.JComponent;

import org.biomart.configurator.jdomUtils.JDomNodeAdapter;
import org.biomart.configurator.utils.type.McNodeType;

public abstract class McNode {
	protected JDomNodeAdapter node;
	protected McNodeType type;
	
	public int getLevel() {
		return type.getLevel();
	}
	
	public abstract JComponent getGuiComponent(); 
}