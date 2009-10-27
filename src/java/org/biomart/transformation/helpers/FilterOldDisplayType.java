package org.biomart.transformation.helpers;

import org.biomart.common.general.exceptions.FunctionalException;

public enum FilterOldDisplayType {

	LIST			("list"),
	TEXT			("text"),
	CONTAINER		("container");
	
	private String oldDisplayType = null;
	private FilterOldDisplayType(String oldDisplayType) {
		this.oldDisplayType = oldDisplayType;
	}
	public String getOldDisplayType() {
		return this.oldDisplayType;
	}
	public static FilterOldDisplayType getFilterOldDisplayType(String oldDisplayType) throws FunctionalException {
		for (FilterOldDisplayType filterOldDisplayType : FilterOldDisplayType.values()) {
			if (filterOldDisplayType.getOldDisplayType().equals(oldDisplayType)) {
				return filterOldDisplayType;
			}
		}
		throw new FunctionalException("Unknown oldDisplayType for FilterOldDisplayType: " + oldDisplayType);
	}
}
