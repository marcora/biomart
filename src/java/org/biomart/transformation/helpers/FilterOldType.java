package org.biomart.transformation.helpers;

import org.biomart.common.general.exceptions.FunctionalException;

public enum FilterOldType {

	LIST			("list"),
	TEXT			("text"),
	BOOLEAN			("boolean"),
	ID_LIST			("id_list"),
	BOOLEAN_LIST	("boolean_list"),
	DROP_DOWN_BASIC_FILTER	("drop_down_basic_filter"),
	NUMBER			("number"),
	BOOLEAN_NUM		("boolean_num"),
	
	OTHER			("other"),
	EMPTY			("");
	
	private String oldType = null;
	private FilterOldType(String oldType) {
		this.oldType = oldType;
	}
	public String getOldType() {
		return this.oldType;
	}
	public static FilterOldType getFilterOldType(String oldType) throws FunctionalException {
		for (FilterOldType filterOldType : FilterOldType.values()) {
			if (filterOldType.getOldType().equals(oldType)) {
				return filterOldType;
			}
		}
		throw new FunctionalException("Unknown oldType for FilterOldType: " + oldType);
	}
}
