package org.biomart.transformation.tmp.backwardCompatibility.objects;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.transformation.helpers.FilterOldDisplayType;
import org.biomart.transformation.helpers.FilterOldType;


public class ContainerFilterInfo {

	@Override
	public String toString() {
		return container + MyUtils.TAB_SEPARATOR + cascade;
	}
	public Boolean container = null; 
	public Boolean cascade = null;
	public FilterOldDisplayType subFilterOldDisplayType = null;
	public FilterOldType subFilterOldType = null;
	public TransformationFilterDisplayType subFilterDisplayType = null;
	public ContainerFilterInfo() {
		super();
		this.container = false;
		this.cascade = false;
	}
}
