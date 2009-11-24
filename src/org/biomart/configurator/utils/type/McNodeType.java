package org.biomart.configurator.utils.type;

public enum McNodeType {
	MARTREGISTRY (0),
	USER (1),
	LOCATION (1),
	MART (2),
	DATASET (3),
	CONTAINER (4),
	PARTITIONTABLE (4),
	FILTER (5),
	ATTRIBUTE (5),
	ATTRIBUTEPOINTER (5);
	
	private int level;
	
	McNodeType(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return this.level;
	}
}