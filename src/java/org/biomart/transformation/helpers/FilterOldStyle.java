package org.biomart.transformation.helpers;

import org.biomart.common.general.exceptions.FunctionalException;


public enum FilterOldStyle {
	MENU			("menu"),
	RADIO			("radio"),
	
	// Invalid styles found in configuration, but tolerated and considered as empty
	/*TEXT			("text"),
	LIST			("list"),
	BOOLEAN			("boolean"),*/
	
	EMPTY			("");
	
	private String oldStyle = null;
	private FilterOldStyle(String oldStyle) {
		this.oldStyle = oldStyle;
	}
	public String getOldStyle() {
		return this.oldStyle;
	}
	public static FilterOldStyle getFilterOldStyle(String oldStyle) throws FunctionalException {
		for (FilterOldStyle filterOldStyle : FilterOldStyle.values()) {
			if (filterOldStyle.getOldStyle().equals(oldStyle)) {
				return filterOldStyle;
			}
		}
		throw new FunctionalException("Unknown oldStyle for FilterOldStyle: " + oldStyle);
	}
}
