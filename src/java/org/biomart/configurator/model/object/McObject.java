package org.biomart.configurator.model.object;

import org.biomart.configurator.utils.type.McNodeType;

public abstract class McObject {
	protected McNodeType nodeType;
	protected String name;
	
	public McNodeType getNodeType() {
		return this.nodeType;
	}
	
}